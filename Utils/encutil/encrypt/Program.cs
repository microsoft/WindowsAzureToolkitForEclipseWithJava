/*
 Copyright 2012 Microsoft Open Technologies, Inc.
 
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

namespace MicrosoftOpenTechnologies.Tools.EncUtil
{
    using System;
    using System.Diagnostics;
    using System.Globalization;
    using System.IO;
    using System.Runtime.InteropServices;
    using System.Security;
    using System.Security.Cryptography.Pkcs;
    using System.Security.Cryptography.X509Certificates;
    using System.Text;
    using MicrosoftOpenTechnologies.Tools.EncUtil.CertLibrary;

    /// <summary>
    /// Entrypont class
    /// </summary>
    internal class Program
    {
        private static readonly string[] OperationNames = new[] { "-encrypt", "-thumbprint", "-create" };
        private static readonly string[] OptionNames = new[] { "-alias", "-cert", "-pfx", "-pwd", "-text", "-exp" };
        private const string CommandName = "encutil";
        private const string HelpText =
@"Usage:
{0} {1} {2} <cert_file>
{0} {3} {4} <text> {2} <cert_file>
{0} {5} {6} <key-alias> {7} <password> {8} <pfx_file> {2} <cert_file> [{9} <exp_date>]

Options:
 {6}  <key_alias>	--Alias of the key.
 {2}  <cert_file>	--X.509 certificate file
 {5} 		--Creates a self-signed RSA certificate and saves as a
			  X.509 certificate file and a PFX file.
 {3} 		--Returns encrypted, base-64 encoded text using the
			  certificate's public key.
 {9}  <exp_date>	--Expiration date for the created certificate.
 {8}  <pfx_file>	--PFX file.
 {7}  <password>	--Password for the private key.
 {4}  <text>		--The text to encrypt when using the -encrypt option.
 {1} 		--Computes the SHA1 thumbprint of the X.509 certificate.
";

        private static Operation operation;
        private static string certFileName;
        private static string pfxFileName;
        private static string keyAlias;
        private static SecureString password;
        private static SecureString contentToEncrypt;
        private static DateTime expirationDate = DateTime.MaxValue;

        /// <summary>
        /// Entrypoint method
        /// </summary>
        internal static void Main(string[] args)
        {
            try
            {
                // Parse cmd-line arguments
                if (ParseArgs(args))
                {
                    // Display help if requested
                    DisplayHelp();
                }
                else
                {
                    string output = null;

                    // Run the appropriate command and capture the output
                    switch (operation)
                    {
                        case Operation.Encrypt:
                            output = Encrypt();
                            break;

                        case Operation.GetThumbprint:
                            output = GetThumbprint();
                            break;

                        case Operation.Create:
                            Create();
                            break;
                    }

                    // Send the output to stdout
                    Console.Out.WriteLine(output);
                    Environment.ExitCode = 0;
                }
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine(ex.Message);
                Environment.ExitCode = 100;
            }
        }

        /// <summary>
        /// Parses arguments passed into encutil
        /// </summary>
        private static bool ParseArgs(string[] args)
        {
            Debug.Assert(Enum.GetValues(typeof(Operation)).Length == OperationNames.Length, "Mismatch between Operation enum and operationNames");
            Debug.Assert(Enum.GetValues(typeof(Option)).Length == OptionNames.Length, "Mismatch between Option enum and optionNames");

            if (args.Length == 0)
            {
                throw new InvalidOperationException("Incorrect arguments specified");
            }

            string operationString = args[0];

            // Check for help args
            if (operationString.Equals("-help", StringComparison.OrdinalIgnoreCase) || 
                operationString.Equals("/help", StringComparison.OrdinalIgnoreCase) || 
                operationString.Equals("help", StringComparison.OrdinalIgnoreCase) ||
                operationString.Equals("-?", StringComparison.OrdinalIgnoreCase) || 
                operationString.Equals("/?", StringComparison.OrdinalIgnoreCase) || 
                operationString.Equals("?", StringComparison.OrdinalIgnoreCase))
            {
                return true;
            }

            if (args.Length < 3)
            {
                throw new InvalidOperationException("Incorrect arguments specified");
            }

            // First arg should correspond to an Operation
            if (!GetOperation(operationString, out operation))
            {
                throw new InvalidOperationException("Incorrect arguments specified");
            }

            // Go through remaining args and record their values
            for (int i = 1; i < args.Length; i++)
            {
                string name = args[i];

                Option option;
                if (!GetOption(name, out option))
                {
                    throw new InvalidOperationException("Invalid option specified");
                }

                if (i + 1 >= args.Length)
                {
                    throw new InvalidOperationException("Invalid option specified");
                }

                string value = args[++i];

                switch (option)
                {
                    case Option.Alias:
                        // Key alias
                        keyAlias = value;
                        break;

                    case Option.ExpirationDate:
                        // Expiration date
                        expirationDate = DateTime.Parse(value, CultureInfo.InvariantCulture);
                        break;

                    case Option.CerFile:
                        // Certificate file
                        certFileName = value;
                        break;

                    case Option.PfxFile:
                        // PFX file
                        pfxFileName = value;
                        break;

                    case Option.Password:
                        // Key password
                        SetSecureString(value, ref password);
                        value = null;
                        break;

                    case Option.Text:
                        // Text to encrypt
                        SetSecureString(value, ref contentToEncrypt);
                        value = null;
                        break;
                }
            }

            // Verify that enough args were collected
            ValidateArgs();

            return false;
        }

        /// <summary>
        /// Validates that args have correctly been set
        /// </summary>
        private static void ValidateArgs()
        {
            if (certFileName == null)
            {
                throw new InvalidOperationException("Cert file name was not specified");
            }

            if (operation == Operation.Encrypt)
            {
                if (contentToEncrypt == null)
                {
                    throw new InvalidOperationException("Content to encrypt was not specified");
                }
            }

            if (operation == Operation.Create)
            {
                if (keyAlias == null)
                {
                    throw new InvalidOperationException("Key alias was not specified");
                }
                else if (pfxFileName == null)
                {
                    throw new InvalidOperationException("PFX file name was not specified");
                }
                else if (password == null)
                {
                    throw new InvalidOperationException("Private key password was not specified");
                }
                else if (expirationDate == DateTime.MaxValue)
                {
                    // Default to expiration date of 10 years from now
                    expirationDate = DateTime.Now.AddYears(10);
                }
            }
        }

        /// <summary>
        /// Sends help string to stderr
        /// </summary>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA2204:Literals should be spelled correctly")]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Globalization", "CA1303:Do not pass literals as localized parameters", MessageId = "System.Console.Write(System.String)")]
        private static void DisplayHelp()
        {
            Console.Write(string.Format(
                CultureInfo.InvariantCulture,
                HelpText,
                CommandName,
                GetOperationName(Operation.GetThumbprint),
                GetOptionName(Option.CerFile),
                GetOperationName(Operation.Encrypt),
                GetOptionName(Option.Text),
                GetOperationName(Operation.Create),
                GetOptionName(Option.Alias),
                GetOptionName(Option.Password),
                GetOptionName(Option.PfxFile),
                GetOptionName(Option.ExpirationDate)));
        }

        /// <summary>
        /// Creates a new certificate and saves it as X.509 cert file and a PFX file
        /// </summary>
        private static void Create()
        {
            // Create a self-signed cert
            byte[] pfxBytes = CertUtil.CreateSelfSignedCert(
                new X500DistinguishedName("CN=Windows Azure Tools"),
                keyAlias,
                DateTime.Now,
                expirationDate,
                password);

            // Write the cert into the .pfx file
            File.WriteAllBytes(pfxFileName, pfxBytes);

            // Export as x.509 cert (without the public key)
            byte[] cerBytes = new X509Certificate2(pfxBytes, password).Export(X509ContentType.Cert);

            // Write into the .cer file
            File.WriteAllBytes(certFileName, cerBytes);
        }

        /// <summary>
        /// Encrypts the string supplied as -content on cmd-line, and encodes in base64
        /// </summary>
        private static string Encrypt()
        {
            X509Certificate2 cert = new X509Certificate2(certFileName);

            string content = GetString(contentToEncrypt);
            var contentBytes = Encoding.UTF8.GetBytes(content);
            ContentInfo contentInfo = new ContentInfo(contentBytes);
            content = null;
            contentBytes = null;

            EnvelopedCms envelope = new EnvelopedCms(contentInfo);
            envelope.Encrypt(new CmsRecipient(cert));
            string encryptedContent = Convert.ToBase64String(envelope.Encode());
            return encryptedContent;
        }

        /// <summary>
        /// Gets the thumbprint of the specified cert
        /// </summary>
        private static string GetThumbprint()
        {
            X509Certificate2 cert = new X509Certificate2(certFileName);
            return cert.Thumbprint;
        }

        /// <summary>
        /// Sets the SecureString with the provided value
        /// </summary>
        private static void SetSecureString(string value, ref SecureString secureString)
        {
            unsafe
            {
                fixed (char* valuePtr = value.ToCharArray())
                {
                    secureString = new SecureString(valuePtr, value.Length);
                }
            }

            secureString.MakeReadOnly();
        }

        /// <summary>
        /// Gets the string representation of the content to encrypt
        /// </summary>
        private static string GetString(SecureString secureString)
        {
            IntPtr ptrContent = Marshal.SecureStringToGlobalAllocUnicode(secureString);

            try
            {
                unsafe
                {
                    return new string((char*)ptrContent);
                }
            }
            finally
            {
                Marshal.FreeHGlobal(ptrContent);
            }
        }

        /// <summary>
        /// Gets the name of the operation based on Operation enum
        /// </summary>
        private static string GetOperationName(Operation operation)
        {
            return OperationNames[(int)operation];
        }

        /// <summary>
        /// Gets the name of the option based on Option enum
        /// </summary>
        private static string GetOptionName(Option option)
        {
            return OptionNames[(int)option];
        }

        /// <summary>
        /// Gets the Operation enum based operation name
        /// </summary>
        private static bool GetOperation(string operationName, out Operation operation)
        {
            for (int i = 0; i < OperationNames.Length; i++)
            {
                if (operationName.Equals(OperationNames[i], StringComparison.OrdinalIgnoreCase))
                {
                    operation = (Operation)i;
                    return true;
                }
            }

            operation = (Operation)int.MaxValue;
            return false;
        }

        /// <summary>
        /// Gets the Option enum based option name
        /// </summary>
        private static bool GetOption(string optionName, out Option option)
        {
            for (int i = 0; i < OptionNames.Length; i++)
            {
                if (optionName.Equals(OptionNames[i], StringComparison.OrdinalIgnoreCase))
                {
                    option = (Option)i;
                    return true;
                }
            }

            option = (Option)int.MaxValue;
            return false;
        }
    }
}
