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

namespace MicrosoftOpenTechnologies.Tools.SessionAffinityAgent
{
    using System;
    using System.Threading;

    /// <summary>
    /// Entrypoint class.
    /// </summary>
    internal class Program
    {
        private static bool blockStartup;
        private static string arrEndpoint;
        private static string certStoreName;
        private static byte[] certHash;
        private static string serverEndpoint;

        /// <summary>
        /// Entrypoint method
        /// </summary>
        internal static void Main(string[] args)
        {
            try
            {
                // Parse cmd-line arguments
                ParseArgs(args);

                if (blockStartup)
                {
                    bool success = SynchManager.WaitEvents();
                    Environment.ExitCode = success ? 0 : 100;
                }
                else
                {
                    bool succeeded = false;
                    try
                    {
                        ArrWorker.Start(arrEndpoint, serverEndpoint, certHash, certStoreName);
                        succeeded = true;
                    }
                    finally
                    {
                        if (succeeded)
                        {
                            SynchManager.SignalSuccess();
                        }
                        else
                        {
                            SynchManager.SignalFailure();
                        }
                    }

                    Thread.Sleep(Timeout.Infinite);
                }
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine(ex);
                Environment.ExitCode = 100;
            }
        }

        /// <summary>
        /// Parses arguments
        /// </summary>
        private static void ParseArgs(string[] args)
        {
            if (args.Length == 0)
            {
                throw new InvalidOperationException("Incorrect arguments specified");
            }

            if (args[0].Equals("-blockstartup", StringComparison.OrdinalIgnoreCase))
            {
                blockStartup = true;
            }
            else if (args.Length == 2 || args.Length == 4)
            {
                arrEndpoint = args[0];
                serverEndpoint = args[1];
                if (args.Length == 4)
                {
                    certHash = args[2].ConvertHexToBytes();
                    certStoreName = args[3];
                }
            }
            else
            {
                {
                    throw new InvalidOperationException("Incorrect arguments specified");
                }
            }
        }
    }

    static class HexExtention
    {
        private static int H2N(char c)
        {
            if (c >= '0' && c <= '9')
                return c - '0';
            if (c >= 'A' && c <= 'F')
                return c - 'A' + 10;
            if (c >= 'a' && c <= 'f')
                return c - 'a' + 10;
            throw new ArgumentException(String.Format("c:{0}", c));
        }

        public static byte[] ConvertHexToBytes(this string hex)
        {
            var chars = hex.ToCharArray();
            if (chars.Length % 2 != 0)
                throw new ArgumentException("not even length");
            var ret = new byte[chars.Length / 2];
            for (var i = 0; i < chars.Length; i += 2)
            {
                var h = H2N(chars[i]) << 4;
                var l = H2N(chars[i + 1]);
                ret[i / 2] = (byte)(h + l);
            }
            return ret;
        }
    }

}
