/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using System.IO;
using System.Security.Cryptography.X509Certificates;

namespace Gigaspaces.Rest
{
    class InstallCommand : Command
    {
        private String path;
        private String extension;
        private String password;

        private const String publishSettings = ".publishsettings";
        private const String pfx = ".pfx";
        private const String cer = ".cer";

        public InstallCommand(Arguments arguments) : base(arguments) { }

        public override void Run()
        {
            X509Certificate2 importedCert = null;

            if (publishSettings.Equals(extension))
            {
                XDocument xdoc = XDocument.Load(path);

                var managementCertbase64string =
                    xdoc.Descendants("PublishProfile").Single().Attribute("ManagementCertificate").Value;

                importedCert = new X509Certificate2(
                   Convert.FromBase64String(managementCertbase64string));
            }
            else
            {
                importedCert = new X509Certificate2(path, password,X509KeyStorageFlags.PersistKeySet);
            }

            ImportCertificate(importedCert);

            Console.Write(importedCert.Thumbprint);
        }

        private static void ImportCertificate(X509Certificate2 importedCert)
        {
            X509Store store = new X509Store(StoreName.My, StoreLocation.CurrentUser);

            store.Open(OpenFlags.ReadWrite);

            store.Add(importedCert);
          
            store.Close();
        }

        public override bool TestOptionSet()
        {
            path = arguments.GetArgumentValue(Arguments.PATH_ARG);

            extension = Path.GetExtension(path);

            if (!(publishSettings.Equals(extension) || pfx.Equals(extension) || cer.Equals(extension)))
                return false;

            if (pfx.Equals(extension))
            {
                password = arguments.GetArgumentValue(Arguments.PASSWORD_ARG);
            }

            return true;
        }
    }
}
