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
using System.Net;
using System.Xml;
using System.Xml.Linq;
using System.IO;
using System.Text.RegularExpressions;
using System.Security.Cryptography.X509Certificates;
using System.Web;
using restutil;
using System.Collections;
using System.Collections.Specialized;
using System.Threading;

namespace Gigaspaces.Rest
{
    class RestCommand : Command
    {
        private class StringWriterUtf8 : StringWriter
        {
            public StringWriterUtf8(StringBuilder sb)
                : base(sb)
            {
            }

            public override Encoding Encoding
            {
                get { return Encoding.UTF8; }
            }
        }


        public static RegexOptions OPTIONS = RegexOptions.IgnoreCase
                                    | RegexOptions.Multiline
                                    | RegexOptions.Singleline
                                    | RegexOptions.IgnorePatternWhitespace
                                    | RegexOptions.CultureInvariant
                                    | RegexOptions.Compiled;

        private Regex urlTster = new Regex("https?://([^.]+.){1,}", OPTIONS);
        private Regex httpMethodTester = new Regex("GET|HEAD|POST|PUT|DELETE", OPTIONS);
        private Regex HeaderSpliter = new Regex("([A-Za-z0-9\\-]+)\\s*:\\s*([^,\\]]+)", OPTIONS);

        private String httpVerb;
        private String url;
        private X509Certificate2 managementCert = null;
        private IDictionary<String, String> headers;
        private byte[] body;
        private RequestTarget type;
        private String storageAccount;
        private String storageKey;
        private bool isTableStorage;

        private int MaxRetries = 5;

        public RestCommand(Arguments arguments)
            : base(arguments)
        {
        }

        public override void Run()
        {

            HttpWebRequest request = null;

            if (type == RequestTarget.Storage)
            {
                request = CreateRESTRequest(httpVerb, url, body, new SortedList<string, string>(headers));
            }
            else
            {
                request = CreateRequest();
            }

            if (request != null)
            {
                request.Timeout = 500000; // 500 seconds timeout in case of slow connections
            }

            HttpWebResponse response = null;

            for (int retry = 0; retry < MaxRetries; retry++)
            {

                try
                {
                    response = (HttpWebResponse)request.GetResponse();
                    break;
                }
                catch (WebException ex)
                {
                    if (retry < (MaxRetries - 1)) // do not continue with the loop if will not be executed anyway. this will avoid the NPE 
                    {
                        if (ShouldRetry(ex))
                        {
                            Thread.Sleep(1000);
                            continue;
                        }
                    }

                    response = (HttpWebResponse)ex.Response;

                    if (response == null)
                    {
                        Console.Error.Write(ex.Status.ToString());
                        return;
                    }

                }

            }

            using (StreamReader reader = new StreamReader(response.GetResponseStream()))
            {
                string result = reader.ReadToEnd();

                XElement element = null;

                if (!string.IsNullOrEmpty(result))
                    element = XElement.Parse(result);

                XDocument document = CreateResponse(response, element);

                document.Declaration = new XDeclaration("1.0", "utf-8", null);

                StringBuilder output = new StringBuilder();

                using (StringWriterUtf8 writer = new StringWriterUtf8(output))
                {
                    document.Save(writer, SaveOptions.None);
                }

                Console.Write(output.ToString());
            }
        }

        public override bool TestOptionSet()
        {
            httpVerb = arguments.GetArgumentValue(Arguments.HTTP_VERB_ARG);

            if (!httpMethodTester.IsMatch(httpVerb))
                return false;

            url = arguments.GetArgumentValue(Arguments.URL_ARG);

            if (!urlTster.IsMatch(url))
                return false;

            type = DetermineRequestType(url);

            if (type == RequestTarget.ServiceManagement)
            {
                String thumbprint = arguments.GetArgumentValue(Arguments.THUMBPRINT_ARG);

                X509Store store = new X509Store(StoreName.My);

                try
                {
                    store.Open(OpenFlags.ReadWrite);

                    X509Certificate2Collection collection = store.Certificates.Find(X509FindType.FindByThumbprint, thumbprint.ToUpper(), false);

                    if (collection.Count < 1)
                        throw new CertificateNotFoundException(thumbprint);

                    managementCert = collection[0];
                }
                finally
                {
                    store.Close();
                }

            }
            else if (type == RequestTarget.Storage)
            {
                storageKey = arguments.GetArgumentValue<InvalidAccessKeyException>(Arguments.KEY_ARGS);

                isTableStorage = false;

                Uri uri = new Uri(url);
                storageAccount = uri.Host.Split('.')[0];

                storageKey = storageKey.Trim();
                storageAccount.Trim();
            }

            String header = arguments.GetArgumentValueWithoutAssertion(Arguments.HEADER_ARG);

            headers = new Dictionary<string, string>();

            if (!string.IsNullOrEmpty(header))
            {
                MatchCollection matches = HeaderSpliter.Matches(header);

                foreach (Match item in matches)
                {
                    string key = item.Groups[1].Value;
                    string value = item.Groups[2].Value;

                    headers.Add(key, value);
                }
            }

            String b = arguments.GetArgumentValueWithoutAssertion(Arguments.BODY_ARG);

            if (!String.IsNullOrEmpty(b))
            {
                body = Encoding.UTF8.GetBytes(Encoding.UTF8.GetString(Convert.FromBase64String(b)));
            }

            String fileBody = arguments.GetArgumentValueWithoutAssertion(Arguments.FILE_BODY_ARG);

            if (!String.IsNullOrEmpty(fileBody))
            {

                if (type == RequestTarget.ServiceManagement) // this means we made a ServiceManagmenet request and specified a file as the body, not a string.
                {

                    String requestBody = readFileToString(fileBody); // this is the base 64 encoded string from the java code

                    String requestBodyXml = Encoding.UTF8.GetString(Convert.FromBase64String(requestBody)); // this is the actual xml

                    body = Encoding.UTF8.GetBytes(requestBodyXml); // this byte array will eventually be written in the response stream. just as before.
                }
                else
                {
                    using (FileStream fs = new FileStream(fileBody, FileMode.Open))
                    {
                        body = new byte[fs.Length];
                        fs.Read(body, 0, body.Length);
                    }
                }

                File.Delete(fileBody);
            }

            return true;
        }

        private String readFileToString(String pathToFile)
        {
            return File.ReadAllText(pathToFile, Encoding.UTF8);
        }


        private RequestTarget DetermineRequestType(String url)
        {
            Uri uri = new Uri(url);
            string[] hostParts = uri.Host.Split('.');
            if (hostParts[1].Equals("blob") || hostParts[1].Equals("table") || hostParts[1].Equals("queue"))
                return RequestTarget.Storage;
            else if (hostParts[0].StartsWith("management"))
                return RequestTarget.ServiceManagement;

            return RequestTarget.Any;
        }

        private HttpWebRequest CreateRESTRequest(string method, string resource, byte[] requestBody = null, SortedList<string, string> headers = null,
            string ifMatch = "", string md5 = "")
        {
            byte[] byteArray = null;
            DateTime now = DateTime.UtcNow;
            string uri = url;

            HttpWebRequest request = HttpWebRequest.Create(uri) as HttpWebRequest;
            request.Method = method;
            request.ContentLength = 0;
            request.Headers.Add("x-ms-date", now.ToString("R", System.Globalization.CultureInfo.InvariantCulture));

            if (isTableStorage)
            {
                request.ContentType = "application/atom+xml";

                request.Headers.Add("DataServiceVersion", "1.0;NetFx");
                request.Headers.Add("MaxDataServiceVersion", "1.0;NetFx");
            }

            if (headers != null)
            {
                foreach (KeyValuePair<string, string> header in headers)
                {
                    if ("Content-Length".Equals(header.Key) || "Content-Type".Equals(header.Key))
                        continue;

                    request.Headers.Add(header.Key, header.Value);
                }
            }

            if (requestBody != null)
            {
                request.Headers.Add("Accept-Charset", "UTF-8");

                byteArray = requestBody;//Encoding.UTF8.GetBytes(requestBody);
                request.ContentLength = byteArray.Length;
            }

            request.Headers.Add("Authorization", AuthorizationHeader(method, now, request, ifMatch, md5));

            if (requestBody != null)
            {
                request.GetRequestStream().Write(byteArray, 0, byteArray.Length);
            }

            return request;
        }

        private System.Net.HttpWebRequest CreateRequest()
        {
            HttpWebRequest request = HttpWebRequest.Create(url) as HttpWebRequest;
            request.Method = httpVerb;
            request.ContentLength = 0;
            DateTime now = DateTime.UtcNow;

            if (type == RequestTarget.Storage)
                request.Headers.Add("x-ms-date", now.ToString("R", System.Globalization.CultureInfo.InvariantCulture));

            foreach (KeyValuePair<string, string> e in headers)
            {
                if (e.Key == "Content-Type")
                {
                    request.ContentType = e.Value;
                }
                else if (e.Key == "Content-Length")
                {
                    //request.ContentLength = long.Parse(e.Value);
                }
                else
                    request.Headers.Add(e.Key, e.Value);
            }


            // add auth if needed
            if (type == RequestTarget.ServiceManagement)
            {
                request.ClientCertificates.Add(managementCert);
            }

            if (body != null)
            {
                request.Headers.Add("Accept-Charset", "UTF-8");
                request.ContentLength = body.Length;
            }

            if (type == RequestTarget.Storage)
            {
                request.Headers.Add("Authorization", AuthorizationHeader(request.Method, now, request));
            }

            if (body != null && body.Length > 0)
                request.GetRequestStream().Write(body, 0, body.Length);

            return request;
        }

        private XDocument CreateResponse(HttpWebResponse res, XElement document)
        {
            XDocument response = new XDocument(new XDeclaration("1.0", "utf-8", null));

            XElement root = new XElement("Response", new XAttribute("verb", res.Method),
                                                     new XAttribute("status", (int)res.StatusCode),
                                                     new XAttribute("description", res.StatusDescription),
                                                     new XAttribute("url", res.ResponseUri));

            response.Add(root);

            foreach (String h in res.Headers)
            {
                root.Add(new XElement("Header", new XAttribute("name", h), new XAttribute("value", res.Headers[h])));
            }
            if (document != null)
                root.Add(document);

            return response;
        }


        private string AuthorizationHeader(string method, DateTime now, HttpWebRequest request, string ifMatch = "", string md5 = "")
        {
            string MessageSignature;

            if (isTableStorage)
            {
                MessageSignature = String.Format("{0}\n\n{1}\n{2}\n{3}",
                    method,
                    "application/atom+xml",
                    now.ToString("R", System.Globalization.CultureInfo.InvariantCulture),
                    GetCanonicalizedResource(request.RequestUri, storageAccount)
                    );
            }
            else
            {
                MessageSignature = String.Format("{0}\n\n\n{1}\n{5}\n\n\n\n{2}\n\n\n\n{3}{4}",
                    method,
                    (method == "GET" || method == "HEAD") ? String.Empty : request.ContentLength.ToString(),
                    ifMatch,
                    GetCanonicalizedHeaders(request),
                    GetCanonicalizedResource(request.RequestUri, storageAccount),
                    md5
                    );
            }
            byte[] SignatureBytes = System.Text.Encoding.UTF8.GetBytes(MessageSignature);
            System.Security.Cryptography.HMACSHA256 SHA256 = new System.Security.Cryptography.HMACSHA256(Convert.FromBase64String(storageKey));
            String AuthorizationHeader = "SharedKey " + storageAccount + ":" + Convert.ToBase64String(SHA256.ComputeHash(SignatureBytes));
            return AuthorizationHeader;
        }

        private string GetCanonicalizedHeaders(HttpWebRequest request)
        {
            ArrayList headerNameList = new ArrayList();
            StringBuilder sb = new StringBuilder();
            foreach (string headerName in request.Headers.Keys)
            {
                if (headerName.ToLowerInvariant().StartsWith("x-ms-", StringComparison.Ordinal))
                {
                    headerNameList.Add(headerName.ToLowerInvariant());
                }
            }
            headerNameList.Sort();
            foreach (string headerName in headerNameList)
            {
                StringBuilder builder = new StringBuilder(headerName);
                string separator = ":";
                foreach (string headerValue in GetHeaderValues(request.Headers, headerName))
                {
                    string trimmedValue = headerValue.Replace("\r\n", String.Empty);
                    builder.Append(separator);
                    builder.Append(trimmedValue);
                    separator = ",";
                }
                sb.Append(builder.ToString());
                sb.Append("\n");
            }
            return sb.ToString();
        }


        private string GetCanonicalizedResource(Uri address, string accountName)
        {
            StringBuilder str = new StringBuilder();
            StringBuilder builder = new StringBuilder("/");
            builder.Append(accountName);
            builder.Append(address.AbsolutePath);
            str.Append(builder.ToString());
            NameValueCollection values2 = new NameValueCollection();
            if (!isTableStorage)
            {
                NameValueCollection values = ParseQueryString(address.Query);

                foreach (string str2 in values.Keys)
                {
                    ArrayList list = new ArrayList(values.GetValues(str2));
                    list.Sort();
                    StringBuilder builder2 = new StringBuilder();
                    foreach (object obj2 in list)
                    {
                        if (builder2.Length > 0)
                        {
                            builder2.Append(",");
                        }
                        builder2.Append(obj2.ToString());
                    }
                    values2.Add((str2 == null) ? str2 : str2.ToLowerInvariant(), builder2.ToString());
                }
            }
            ArrayList list2 = new ArrayList(values2.AllKeys);
            list2.Sort();
            foreach (string str3 in list2)
            {
                StringBuilder builder3 = new StringBuilder(string.Empty);

                builder3.Append(str3);
                builder3.Append(":");
                builder3.Append(Uri.UnescapeDataString(values2[str3]));
                str.Append("\n");
                str.Append(builder3.ToString());

            }
            return str.ToString();
        }


        private ArrayList GetHeaderValues(NameValueCollection headers, string headerName)
        {
            ArrayList list = new ArrayList();
            string[] values = headers.GetValues(headerName);
            if (values != null)
            {
                foreach (string str in values)
                {
                    list.Add(str.TrimStart(null));
                }
            }
            return list;
        }

        private NameValueCollection ParseQueryString(string s)
        {
            NameValueCollection nvc = new NameValueCollection();

            // remove anything other than query string from url
            if (s.Contains("?"))
            {
                s = s.Substring(s.IndexOf('?') + 1);
            }

            foreach (string vp in Regex.Split(s, "&"))
            {
                string[] singlePair = Regex.Split(vp, "=");
                if (singlePair.Length == 2)
                {
                    nvc.Add(singlePair[0], singlePair[1]);
                }
            }

            return nvc;
        }

        private bool ShouldRetry(WebException ex)
        {
            WebExceptionStatus status = ex.Status;
            if ((status == WebExceptionStatus.Timeout))
            {
                return true;
            }
            if ((status == WebExceptionStatus.ConnectFailure))
            {
                return true;
            }
            if ((status == WebExceptionStatus.ConnectionClosed))
            {
                return true;
            }
            return false;
        }
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     