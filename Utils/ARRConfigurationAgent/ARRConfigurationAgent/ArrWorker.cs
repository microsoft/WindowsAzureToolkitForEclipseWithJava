/*
Copyright 2013 Microsoft Open Technologies, Inc.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

namespace MicrosoftOpenTechnologies.Tools.ARRAgent
{
    using System;
    using System.Collections.Generic;
    using System.Diagnostics;
    using System.Globalization;
    using System.Linq;
    using System.Net;
    using Microsoft.Web.Administration;
    using Microsoft.WindowsAzure.ServiceRuntime;
    using System.Security.Cryptography.X509Certificates;

    /// <summary>
    /// Configures IIS to use ARR server farm.
    /// We assume that this agent runs right after vanilla installation of IIS + ARR.
    /// </summary>
    internal static class ArrWorker
    {
        // ARR TimeOut for connection. Format: hh:mm:ss
        private static string ARR_TIME_OUT = "00:03:00";
        /// <summary>
        /// Starts the ArrWorker agent.
        /// This method returns as soon as IIS is configured to forward http traffic to ARR farm.
        /// </summary>
        internal static void Start(string arrEndpointName, string serverEndPointName, bool enableAffinity, string certHash, 
                                   string certStoreName, string httpRedirectEndPointName)
        {
            RoleInstanceEndpoint arrEndpoint;
            RoleInstanceEndpoint httpRedirectEndPoint = null;

            // Get the arr endpoint on which we expect to see http traffic.
            if (!RoleEnvironment.CurrentRoleInstance.InstanceEndpoints.TryGetValue(arrEndpointName, out arrEndpoint))
            {
                throw new InvalidOperationException("Invalid ARR endpoint name");
            }

            if (httpRedirectEndPointName != null && !RoleEnvironment.CurrentRoleInstance.InstanceEndpoints.TryGetValue(httpRedirectEndPointName, out httpRedirectEndPoint))
            {
                throw new InvalidOperationException("Invalid ARR http redirect endpoint name");
            }

            // Do one-time IIS/ARR setup.
            ConfigureOnce(serverEndPointName, arrEndpoint.IPEndpoint, certHash, certStoreName);

            // Do one-time configuration of HTTP redirect
            if (httpRedirectEndPointName != null)
            {
                ConfigureHTTPRedirectForARR(httpRedirectEndPointName, httpRedirectEndPoint.IPEndpoint, arrEndpoint.PublicIPEndpoint);
            }

            // Update the server farm based on the discovered server endpoint instances.
            UpdateFarm(serverEndPointName, enableAffinity);

            // Listen for RoleEnvironmentTopologyChange events, and update the server
            // farm when there are any changes.
            RoleEnvironment.Changed += (s, e) =>
            {
                if (e.Changes.Any(change => change is RoleEnvironmentTopologyChange))
                {
                    UpdateFarm(serverEndPointName, enableAffinity);
                }
            };
        }

        /// <summary>
        /// Performs one-time configuration of IIS/ARR.
        /// </summary>
        private static void ConfigureOnce(string serverEndpointName, IPEndPoint bindInfo, string certHash, string certStoreName)
        {
            using (ServerManager sm = new ServerManager())
            {
                Configuration config = sm.GetApplicationHostConfiguration();
                ConfigurationSection rules = config.GetSection("system.webServer/rewrite/rules");
                ConfigurationSection allowedServerVariables = config.GetSection("system.webServer/rewrite/allowedServerVariables");

                // Check if we already have a rewrite rule for this farm.
                bool ruleFound = rules.GetCollection().Any(e =>
                {
                    ConfigurationAttribute name = e.Attributes["name"];
                    return name != null && name.Value.ToString().Equals(
                        serverEndpointName,
                        StringComparison.OrdinalIgnoreCase);
                });

                if (!ruleFound)
                {
                    ConfigurationElement rule = rules.GetCollection().CreateElement("rule");
                    rule.SetAttributeValue("name", serverEndpointName);
                    rule.SetAttributeValue("stopProcessing", true);
                    rule.GetChildElement("match").SetAttributeValue("url", ".*");
                    rule.GetChildElement("action").SetAttributeValue("type", "Rewrite");
                    rule.GetChildElement("action").SetAttributeValue(
                        "url",
                        string.Format(CultureInfo.InvariantCulture, @"http://{0}/{{R:0}}", serverEndpointName));

                    // Add conditions
                    var conditions = rule.GetChildElement("conditions");

                    var condition = conditions.GetCollection().CreateElement("add");
                    condition.SetAttributeValue("input", "{SERVER_PORT}");
                    condition.SetAttributeValue("pattern", bindInfo.Port);
                    conditions.GetCollection().Add(condition);

                    rules.GetCollection().Add(rule);

                    // Add server variables
                    if (certHash != null)
                    {
                        // Add server variables
                        var serverVariables = rule.GetChildElement("serverVariables");

                        var serverVariable = serverVariables.GetCollection().CreateElement("set");
                        serverVariable.SetAttributeValue("name", "HTTP_X_FORWARDED_PROTO");
                        serverVariable.SetAttributeValue("value", "https");
                        serverVariable.SetAttributeValue("replace", "false");
                        serverVariables.GetCollection().Add(serverVariable);

                        // Make an entry in allowed server variables
                        var allowedServerVariable = allowedServerVariables.GetCollection().CreateElement("add");
                        allowedServerVariable.SetAttributeValue("name", "HTTP_X_FORWARDED_PROTO");
                        allowedServerVariables.GetCollection().Add(allowedServerVariable);
                    }



                    // Ensure that the default app pool is set to classic mode.
                    Debug.Assert(sm.ApplicationPools["DefaultAppPool"] != null, "DefaultAppPool is not present");
                    sm.ApplicationPools["DefaultAppPool"].ManagedPipelineMode =
                        ManagedPipelineMode.Classic;

                    // Make sure that we have a binging in the default web site, which listens on ARR port.
                    Debug.Assert(sm.Sites["Default Web Site"] != null, "Default Web Site is not present");
                    foreach (Binding binding in sm.Sites["Default Web Site"].Bindings)
                    {
                        if (binding.Protocol.Equals("http", StringComparison.OrdinalIgnoreCase))
                        {
                            var bindingInformation = string.Format(
                                CultureInfo.InvariantCulture,
                                "{0}:{1}:",
                                bindInfo.Address.ToString(),
                                bindInfo.Port);

                            if (certHash!=null)
                            {
                                byte[] certHashInbytes = getCertHashInbytes(certHash, certStoreName);
                                var httpsBinding = sm.Sites["Default Web Site"].Bindings.Add(bindingInformation, certHashInbytes, certStoreName);
                                httpsBinding.Protocol = "https";
                                sm.Sites["Default Web Site"].Bindings.Remove(binding);
                            }
                            else
                            {
                                binding["bindingInformation"] = bindingInformation;
                            }
                            break;
                        }
                    }
                }

                sm.CommitChanges();
            }
        }

        // Performs one-time configuration of IIS/ARR.
        private static void ConfigureHTTPRedirectForARR(String httpRedirectEndPointName, IPEndPoint redirectBindInfo, IPEndPoint arrPublicEP)
        {
            using (ServerManager sm = new ServerManager())
            {
                Configuration config = sm.GetApplicationHostConfiguration();
                ConfigurationSection rules = config.GetSection("system.webServer/rewrite/rules");

                // Check if we already have a rewrite rule for this farm.
                bool ruleFound = rules.GetCollection().Any(e =>
                {
                    ConfigurationAttribute name = e.Attributes["name"];
                    return name != null && name.Value.ToString().Equals(
                        httpRedirectEndPointName,
                        StringComparison.OrdinalIgnoreCase);
                });

                if (!ruleFound)
                {
                    int arrPublicPort = arrPublicEP.Port;

                    ConfigurationElement rule = rules.GetCollection().CreateElement("rule");
                    rule.SetAttributeValue("name", httpRedirectEndPointName);
                    rule.SetAttributeValue("stopProcessing", true);
                    rule.GetChildElement("match").SetAttributeValue("url", ".*");
                    rule.GetChildElement("action").SetAttributeValue("type", "Redirect");
                    if (arrPublicPort == 443)
                    {
                        rule.GetChildElement("action").SetAttributeValue("url", "https://{SERVER_NAME}{HTTP_URL}");                            
                    }
                    else
                    {
                        rule.GetChildElement("action").SetAttributeValue("url", "https://{SERVER_NAME}:"+arrPublicPort+"{HTTP_URL}");
                    }
                    /* rule.GetChildElement("action").SetAttributeValue(
                        "url",
                        string.Format(CultureInfo.InvariantCulture, @"https://{0}/{{R:0}}", "{HTTP_HOST}")); */

                    // Add conditions
                    var conditions = rule.GetChildElement("conditions");

                    var condition = conditions.GetCollection().CreateElement("add");
                    condition.SetAttributeValue("input", "{HTTPS}");
                    condition.SetAttributeValue("pattern", "OFF");
                    conditions.GetCollection().Add(condition);

                    rules.GetCollection().Add(rule);

                    // Ensure that the default app pool is set to classic mode.
                    Debug.Assert(sm.ApplicationPools["DefaultAppPool"] != null, "DefaultAppPool is not present");
                    sm.ApplicationPools["DefaultAppPool"].ManagedPipelineMode =
                        ManagedPipelineMode.Classic;

                    // Make sure that we have a binding in the default web site, which listens on ARR port for http redirect
                    Debug.Assert(sm.Sites["Default Web Site"] != null, "Default Web Site is not present");

                    BindingCollection bindingCollection = sm.Sites["Default Web Site"].Bindings;

                    Binding binding = sm.Sites["Default Web Site"].Bindings.CreateElement("binding");

                    binding["protocol"] = "http";

                    var bindingInformation = string.Format(
                                CultureInfo.InvariantCulture,
                                "{0}:{1}:",
                                redirectBindInfo.Address.ToString(),
                                redirectBindInfo.Port);
                    binding["bindingInformation"] = bindingInformation;


                    bindingCollection.Add(binding);


                }

                sm.CommitChanges();
            }
        }


        // Finds certHash in bytes , if exists returns hash in bytes else throws an exception.
        private static byte[] getCertHashInbytes(String certHash, String storeName)
        {
            var store = new X509Store(storeName, StoreLocation.LocalMachine);
            store.Open(OpenFlags.OpenExistingOnly);

            var certificate = store.Certificates.Find(X509FindType.FindByThumbprint,
                                                          certHash, false)
                                                          .OfType<X509Certificate>().FirstOrDefault();
            if (certificate != null)
                return certificate.GetCertHash();
            else
                throw new InvalidOperationException("No Certificate is found with hash " + certHash);


        }

        /// <summary>
        /// Updates the ARR server farm based on instances of the current role.
        /// </summary>
        private static void UpdateFarm(string serverEndpointName, Boolean enableAffinity)
        {
            // Get the list of server endpoints for every instance in the role.
            List<IPEndPoint> serverEndpoints = new List<IPEndPoint>();
            if (enableAffinity) // Iterate throw all roles if we need to enable affinity
            {
                foreach (RoleInstance instance in RoleEnvironment.CurrentRoleInstance.Role.Instances)
                {
                    RoleInstanceEndpoint serverEndpoint;
                    if (!instance.InstanceEndpoints.TryGetValue(serverEndpointName, out serverEndpoint))
                    {
                        throw new InvalidOperationException("Invalid server endpoint name");
                    }

                    serverEndpoints.Add(serverEndpoint.IPEndpoint);
                }
            }
            else
            {
                RoleInstance instance = RoleEnvironment.CurrentRoleInstance;
                RoleInstanceEndpoint serverEndpoint;
                if (!instance.InstanceEndpoints.TryGetValue(serverEndpointName, out serverEndpoint))
                {
                    throw new InvalidOperationException("Invalid server endpoint name");
                }

                serverEndpoints.Add(serverEndpoint.IPEndpoint);

            }

                // Update the webfarm with the list of endpoints.
                using (ServerManager sm = new ServerManager())
                {
                    ConfigurationElementCollection farm = GetFarmElement(sm, serverEndpointName, enableAffinity).GetCollection();

                    // Remove all server elements from the farm.
                    farm.Clear();

                    // Add each endpoint to the farm.
                    foreach (IPEndPoint endPoint in serverEndpoints)
                    {
                        ConfigurationElement server = farm.CreateElement("server");
                        server.SetAttributeValue("address", endPoint.Address.ToString());
                        server.SetAttributeValue("enabled", true);
                        server.GetChildElement("applicationRequestRouting").SetAttributeValue("httpPort", endPoint.Port);
                        farm.Add(server);
                    }

                    sm.CommitChanges();
                }
        }

        /// <summary>
        /// Gets or creates a webFarm element from webFarms.
        /// </summary>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Globalization", "CA1305:SpecifyIFormatProvider", MessageId = "System.TimeSpan.Parse(System.String)")]
        private static ConfigurationElement GetFarmElement(ServerManager sm, string serverEndpointName, Boolean enableAffinity)
        {
            ConfigurationElement farmElement = null;
            Configuration config = sm.GetApplicationHostConfiguration();
            ConfigurationSection webFarms = config.GetSection("webFarms");
            Debug.Assert(webFarms != null, "webFarms element is not present");

            ConfigurationElementCollection webFarmsCollection = webFarms.GetCollection();

            // See if we already have a farm.
            foreach (ConfigurationElement farm in webFarmsCollection)
            {
                ConfigurationAttribute name = farm.Attributes["name"];
                if (name != null && name.Value.ToString().Equals(
                    serverEndpointName,
                    StringComparison.OrdinalIgnoreCase))
                {
                    farmElement = farm;
                    break;
                }
            }

            if (farmElement == null)
            {
                farmElement = webFarmsCollection.CreateElement("webFarm");
                farmElement.SetAttributeValue("name", serverEndpointName);
                farmElement.SetAttributeValue("enabled", "true");
                if (enableAffinity)
                {
                    farmElement.GetChildElement("applicationRequestRouting").
                    GetChildElement("affinity").SetAttributeValue("useCookie", true);
                    farmElement.GetChildElement("applicationRequestRouting").
                        GetChildElement("affinity").SetAttributeValue("cookieName", "ARRWAP4EJ");
                    farmElement.GetChildElement("applicationRequestRouting").
                        GetChildElement("loadBalancing").SetAttributeValue("algorithm", "WeightedRoundRobin");
                }
                farmElement.GetChildElement("applicationRequestRouting").
                    GetChildElement("protocol").SetAttributeValue("timeout", TimeSpan.Parse(ARR_TIME_OUT));

                webFarmsCollection.Add(farmElement);
            }

            return farmElement;
        }
    }
}