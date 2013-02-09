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

namespace MicrosoftOpenTechnologies.Tools.EncUtil.CertLibrary
{
    using System;
    using System.Runtime.CompilerServices;
    using System.Runtime.InteropServices;
    using System.Security;
    using System.Security.Cryptography.X509Certificates;

    /// <summary>
    /// Provides cert-related utilities.
    /// </summary>
    internal static class CertUtil
    {
        private const int KeySize = 2048;

        /// <summary>
        /// Create a self-signed certificate.
        /// </summary>
        internal static byte[] CreateSelfSignedCert(
            X500DistinguishedName subjectName, 
            string friendlyName, 
            DateTime startTime, 
            DateTime endTime, 
            SecureString password)
        {
            byte[] pfxData;

            SystemTime startSystemTime = ToSystemTime(startTime);
            SystemTime endSystemTime = ToSystemTime(endTime);
            string containerName = Guid.NewGuid().ToString();

            IntPtr providerContext = IntPtr.Zero;
            IntPtr cryptKey = IntPtr.Zero;
            IntPtr certContext = IntPtr.Zero;
            IntPtr certStore = IntPtr.Zero;
            IntPtr storeCertContext = IntPtr.Zero;
            IntPtr passwordPtr = IntPtr.Zero;

            RuntimeHelpers.PrepareConstrainedRegions();
            try
            {
                Check(NativeMethods.CryptAcquireContextW(
                    out providerContext,
                    containerName,
                    null,
                    1, // PROV_RSA_FULL
                    8)); // CRYPT_NEWKEYSET

                Check(NativeMethods.CryptGenKey(
                    providerContext,
                    1, // AT_KEYEXCHANGE
                    1 | (KeySize << 16), // CRYPT_EXPORTABLE
                    out cryptKey));

                unsafe
                {
                    fixed (char* pContainerName = containerName.ToCharArray())
                    {
                        CryptKeyProviderInformation kpi = new CryptKeyProviderInformation();

                        kpi.ContainerName = (IntPtr)pContainerName;
                        kpi.ProviderType = 1; // PROV_RSA_FULL
                        kpi.KeySpec = 1; // AT_KEYEXCHANGE

                        fixed (byte* pSubjectName = &subjectName.RawData[0])
                        {
                            CryptoApiBlob nameBlob = new CryptoApiBlob(
                                subjectName.RawData.Length,
                                (IntPtr)pSubjectName);

                            certContext = NativeMethods.CertCreateSelfSignCertificate(
                                providerContext,
                                ref nameBlob,
                                0,
                                ref kpi,
                                IntPtr.Zero, // default = SHA1RSA
                                ref startSystemTime,
                                ref endSystemTime,
                                IntPtr.Zero);
                            Check(certContext != IntPtr.Zero);
                        }

                        certStore = NativeMethods.CertOpenStore(
                            "Memory", // sz_CERT_STORE_PROV_MEMORY
                            0,
                            IntPtr.Zero,
                            0x2000, // CERT_STORE_CREATE_NEW_FLAG
                            IntPtr.Zero);
                        Check(certStore != IntPtr.Zero);

                        Check(NativeMethods.CertAddCertificateContextToStore(
                            certStore,
                            certContext,
                            1, // CERT_STORE_ADD_NEW
                            out storeCertContext));

                        NativeMethods.CertSetCertificateContextProperty(
                            storeCertContext,
                            2, // CERT_KEY_PROV_INFO_PROP_ID
                            0,
                            (IntPtr)(&kpi));
                    }

                    fixed (char* pFriendlyName = friendlyName.ToCharArray())
                    {
                        var blob = new CryptoApiBlob(
                            friendlyName.Length * sizeof(char),
                            (IntPtr)pFriendlyName);

                        NativeMethods.CertSetCertificateContextProperty(
                            storeCertContext,
                            11, // CERT_FRIENDLY_NAME_PROP_ID
                            0,
                            (IntPtr)(&blob));
                    }

                    if (password != null && password.Length > 0)
                    {
                        passwordPtr = Marshal.SecureStringToCoTaskMemUnicode(password);
                    }

                    CryptoApiBlob pfxBlob = new CryptoApiBlob();
                    Check(NativeMethods.PFXExportCertStoreEx(
                        certStore,
                        ref pfxBlob,
                        passwordPtr,
                        IntPtr.Zero,
                        7)); // EXPORT_PRIVATE_KEYS | REPORT_NO_PRIVATE_KEY | REPORT_NOT_ABLE_TO_EXPORT_PRIVATE_KEY

                    pfxData = new byte[pfxBlob.DataLength];
                    fixed (byte* pData = pfxData)
                    {
                        pfxBlob.Data = (IntPtr)pData;
                        Check(NativeMethods.PFXExportCertStoreEx(
                            certStore,
                            ref pfxBlob,
                            passwordPtr,
                            IntPtr.Zero,
                            7)); // EXPORT_PRIVATE_KEYS | REPORT_NO_PRIVATE_KEY | REPORT_NOT_ABLE_TO_EXPORT_PRIVATE_KEY
                    }
                }
            }
            finally
            {
                if (passwordPtr != IntPtr.Zero)
                {
                    Marshal.ZeroFreeCoTaskMemUnicode(passwordPtr);
                }

                if (certContext != IntPtr.Zero)
                {
                    NativeMethods.CertFreeCertificateContext(certContext);
                }

                if (storeCertContext != IntPtr.Zero)
                {
                    NativeMethods.CertFreeCertificateContext(storeCertContext);
                }

                if (certStore != IntPtr.Zero)
                {
                    NativeMethods.CertCloseStore(certStore, 0);
                }

                if (cryptKey != IntPtr.Zero)
                {
                    NativeMethods.CryptDestroyKey(cryptKey);
                }

                if (providerContext != IntPtr.Zero)
                {
                    NativeMethods.CryptReleaseContext(providerContext, 0);
                    NativeMethods.CryptAcquireContextW(
                        out providerContext,
                        containerName,
                        null,
                        1, // PROV_RSA_FULL
                        0x10); // CRYPT_DELETEKEYSET
                }
            }

            return pfxData;
        }

        /// <summary>
        /// Converts DateTime to SystemTime
        /// </summary>
        private static SystemTime ToSystemTime(DateTime dateTime)
        {
            long fileTime = dateTime.ToFileTime();
            SystemTime systemTime;
            Check(NativeMethods.FileTimeToSystemTime(ref fileTime, out systemTime));
            return systemTime;
        }

        /// <summary>
        /// Verifies that Win32 call succeeded.  Throws the appropriate exception if Win32 call fails.
        /// </summary>
        private static void Check(bool nativeCallSucceeded)
        {
            if (!nativeCallSucceeded)
            {
                int error = Marshal.GetHRForLastWin32Error();
                Marshal.ThrowExceptionForHR(error);
            }
        }

        [StructLayout(LayoutKind.Sequential)]
        private struct SystemTime
        {
            public short Year;
            public short Month;
            public short DayOfWeek;
            public short Day;
            public short Hour;
            public short Minute;
            public short Second;
            public short Milliseconds;
        }

        [StructLayout(LayoutKind.Sequential)]
        private struct CryptoApiBlob
        {
            public int DataLength;
            public IntPtr Data;

            public CryptoApiBlob(int dataLength, IntPtr data)
            {
                this.DataLength = dataLength;
                this.Data = data;
            }
        }

        [StructLayout(LayoutKind.Sequential)]
        private struct CryptKeyProviderInformation
        {
            public IntPtr ContainerName;
            public IntPtr ProviderName;
            public int ProviderType;
            public int Flags;
            public int ProviderParameterCount;
            public IntPtr ProviderParameters; // PCRYPT_KEY_PROV_PARAM
            public int KeySpec;
        }

        private static class NativeMethods
        {
            [DllImport("kernel32.dll", SetLastError = true, ExactSpelling = true)]
            [return: MarshalAs(UnmanagedType.Bool)]
            public static extern bool FileTimeToSystemTime(
                [In] ref long fileTime,
                out SystemTime systemTime);

            [DllImport("AdvApi32.dll", SetLastError = true, ExactSpelling = true)]
            [return: MarshalAs(UnmanagedType.Bool)]
            public static extern bool CryptAcquireContextW(
                out IntPtr providerContext,
                [MarshalAs(UnmanagedType.LPWStr)] string container,
                [MarshalAs(UnmanagedType.LPWStr)] string provider,
                int providerType,
                int flags);

            [DllImport("AdvApi32.dll", SetLastError = true, ExactSpelling = true)]
            [return: MarshalAs(UnmanagedType.Bool)]
            public static extern bool CryptReleaseContext(
                IntPtr providerContext,
                int flags);

            [DllImport("AdvApi32.dll", SetLastError = true, ExactSpelling = true)]
            [return: MarshalAs(UnmanagedType.Bool)]
            public static extern bool CryptGenKey(
                IntPtr providerContext,
                int algorithmId,
                int flags,
                out IntPtr cryptKeyHandle);

            [DllImport("AdvApi32.dll", SetLastError = true, ExactSpelling = true)]
            [return: MarshalAs(UnmanagedType.Bool)]
            public static extern bool CryptDestroyKey(
                IntPtr cryptKeyHandle);

            [DllImport("Crypt32.dll", SetLastError = true, ExactSpelling = true)]
            public static extern IntPtr CertCreateSelfSignCertificate(
                IntPtr providerHandle,
                [In] ref CryptoApiBlob subjectIssuerBlob,
                int flags,
                [In] ref CryptKeyProviderInformation keyProviderInformation,
                IntPtr signatureAlgorithm,
                [In] ref SystemTime startTime,
                [In] ref SystemTime endTime,
                IntPtr extensions);

            [DllImport("Crypt32.dll", SetLastError = true, ExactSpelling = true)]
            [return: MarshalAs(UnmanagedType.Bool)]
            public static extern bool CertFreeCertificateContext(
                IntPtr certificateContext);

            [DllImport("Crypt32.dll", SetLastError = true, ExactSpelling = true, BestFitMapping = false)]
            public static extern IntPtr CertOpenStore(
                [MarshalAs(UnmanagedType.LPStr)] string storeProvider,
                int messageAndCertificateEncodingType,
                IntPtr cryptProvHandle,
                int flags,
                IntPtr parameters);

            [DllImport("Crypt32.dll", SetLastError = true, ExactSpelling = true)]
            [return: MarshalAs(UnmanagedType.Bool)]
            public static extern bool CertCloseStore(
                IntPtr certificateStoreHandle,
                int flags);

            [DllImport("Crypt32.dll", SetLastError = true, ExactSpelling = true)]
            [return: MarshalAs(UnmanagedType.Bool)]
            public static extern bool CertAddCertificateContextToStore(
                IntPtr certificateStoreHandle,
                IntPtr certificateContext,
                int addDisposition,
                out IntPtr storeContextPtr);

            [DllImport("Crypt32.dll", SetLastError = true, ExactSpelling = true)]
            [return: MarshalAs(UnmanagedType.Bool)]
            public static extern bool CertSetCertificateContextProperty(
                IntPtr certificateContext,
                int propertyId,
                int flags,
                IntPtr data);

            [DllImport("Crypt32.dll", SetLastError = true, ExactSpelling = true)]
            [return: MarshalAs(UnmanagedType.Bool)]
            public static extern bool PFXExportCertStoreEx(
                IntPtr certificateStoreHandle,
                ref CryptoApiBlob pfxBlob,
                IntPtr password,
                IntPtr reserved,
                int flags);
        }
    }
}

