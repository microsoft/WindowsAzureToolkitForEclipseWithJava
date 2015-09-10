/**
 * Copyright (c) Microsoft Corporation
 * 
 * All rights reserved. 
 * 
 * MIT License
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.microsoftopentechnologies.azurecommons.roleoperations;

import java.io.File;
import java.util.Map;

import com.interopbridges.tools.windowsazure.WARoleComponentCloudUploadMode;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;


public class WizardUtilMethods {

	private static String auto = "auto";
	private final static String AUTO_TXT = "(auto)";
	private static String httpEp = PropUtil.getValueFromFile("httpEp");
	private static String dbgEp = PropUtil.getValueFromFile("dbgEp");
	private static String dashAuto = "-auto";
	private final static int CACH_DFLTVAL = 30;
	private final static String DEBUG_PORT = "8090";
	private final static String HTTP_PRV_PORT = "8080";
	private final static String HTTP_PORT = "80";

	public static WindowsAzureRole configureJDKServer(WindowsAzureRole role, Map<String, String> depMap)
			throws Exception {
		try {
			File templateFile = new File(depMap.get("tempFile"));
			if (!(depMap.get("jdkChecked").equalsIgnoreCase("false")
					&& depMap.get("jdkAutoDwnldChecked").
					equalsIgnoreCase("true"))) {
				// Third party JDK name
				if (depMap.get("jdkThrdPartyChecked").equalsIgnoreCase("true")) {
					String jdkName = depMap.get("jdkName");
					role.setJDKSourcePath(depMap.get("jdkLoc"),
							templateFile, jdkName);
					role.setJDKCloudName(jdkName);
				} else {
					role.setJDKSourcePath(depMap.get("jdkLoc"),
							templateFile, "");
				}
				// JDK download group
				// By default auto upload will be selected.
				String jdkTabUrl = depMap.get("jdkUrl");
				if (depMap.get("jdkAutoDwnldChecked").
						equalsIgnoreCase("true")
						|| depMap.get("jdkThrdPartyChecked").equalsIgnoreCase("true")) {
					if (jdkTabUrl.
							equalsIgnoreCase(AUTO_TXT)) {
						jdkTabUrl = auto;
					}
					role.setJDKCloudUploadMode(WARoleComponentCloudUploadMode.auto);
				}
				role.setJDKCloudURL(jdkTabUrl);
				role.setJDKCloudKey(depMap.get("jdkKey"));
				/*
				 * By default package type is local,
				 * hence store JAVA_HOME for cloud.
				 */
				role.setJDKCloudHome(depMap.get("javaHome"));
			}

			// Server
			if (depMap.get("serChecked").equalsIgnoreCase("true")) {
				String srvName = depMap.get("servername");
				if (!srvName.isEmpty()) {
					String srvPriPort = WindowsAzureProjectManager.
							getHttpPort(srvName, templateFile);
					if (role.isValidEndpoint(httpEp,
							WindowsAzureEndpointType.Input,
							srvPriPort, HTTP_PORT)) {
						role.addEndpoint(httpEp,
								WindowsAzureEndpointType.Input,
								srvPriPort, HTTP_PORT);
					}

					role.setServer(srvName, depMap.get("serLoc"), templateFile);
					
					// if its latest server scenario, then don't set cloudkey
					// it should be public download
					boolean setKey = true;
					String altSrcUrl = depMap.get("srvThrdAltSrc");
					if (depMap.get("srvThrdPartyChecked").equalsIgnoreCase("true")) {
						if (altSrcUrl.isEmpty()) {
							setKey = false;
						} else {
							role.setServerCldAltSrc(altSrcUrl);
						}
						String thrdName = depMap.get("srvThrdPartyName");
						if (!thrdName.isEmpty()) {
							role.setServerCloudName(thrdName);
						}
						role.setServerCloudValue(depMap.get("srvHome"));
					}

					String srvTabUrl = depMap.get("srvUrl");
					if (depMap.get("srvAutoDwnldChecked").equalsIgnoreCase("true")
							|| depMap.get("srvThrdPartyChecked").equalsIgnoreCase("true")) {
						if (srvTabUrl.equalsIgnoreCase(AUTO_TXT)) {
							srvTabUrl = auto;
						}
						if (depMap.get("srvThrdPartyChecked").equalsIgnoreCase("true")) {
							if (!altSrcUrl.isEmpty()) {
								// check if its latest server scenario, if no then set cloud upload mode
								role.setServerCloudUploadMode(WARoleComponentCloudUploadMode.auto);
							}
						} else {
							role.setServerCloudUploadMode(WARoleComponentCloudUploadMode.auto);
						}
					}
					role.setServerCloudURL(srvTabUrl);
					if (setKey) {
						role.setServerCloudKey(depMap.get("srvKey"));
					}
					role.setServerCloudHome(depMap.get("srvHome"));
				}
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage(), e);
		}
		return role;
	}

	public static WindowsAzureRole configureKeyFeatures(WindowsAzureRole role, Map<String, Boolean> ftrMap)
			throws Exception {
		try {
			// Enable Key features
			// Session Affinity
			if (ftrMap.get("ssnAffChecked")) {
				WindowsAzureEndpoint httpEndPt =
						role.getEndpoint(httpEp);
				if (httpEndPt == null) {
					/*
					 * server is not enabled.
					 * hence create new endpoint
					 * for session affinity.
					 */
					if (role.isValidEndpoint(httpEp,
							WindowsAzureEndpointType.Input,
							HTTP_PRV_PORT, HTTP_PORT)) {
						httpEndPt = role.addEndpoint(httpEp,
								WindowsAzureEndpointType.Input,
								HTTP_PRV_PORT, HTTP_PORT);
					}
				}
				if (httpEndPt != null) {
					role.
					setSessionAffinityInputEndpoint(httpEndPt);
				}
			}

			// Caching
			if (ftrMap.get("cacheChecked")) {
				role.setCacheMemoryPercent(CACH_DFLTVAL);
				role.setCacheStorageAccountName(dashAuto);
			}

			// Remote Debugging
			if (ftrMap.get("debugChecked")) {
				if (role.isValidEndpoint(dbgEp,
						WindowsAzureEndpointType.Input,
						DEBUG_PORT, DEBUG_PORT)) {
					WindowsAzureEndpoint dbgEndPt =
							role.addEndpoint(dbgEp,
									WindowsAzureEndpointType.Input,
									DEBUG_PORT, DEBUG_PORT);
					if (dbgEndPt != null) {
						role.setDebuggingEndpoint(dbgEndPt);
						role.setStartSuspended(false);
					}
				}
			}

		} catch (Exception e) {
			throw new Exception(e.getMessage(), e);
		}
		return role;
	}
}
