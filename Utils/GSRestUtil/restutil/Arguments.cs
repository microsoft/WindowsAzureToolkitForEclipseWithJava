/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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
using System.Text.RegularExpressions;
using System.Collections.Specialized;

namespace Gigaspaces.Rest
{
    public class Arguments{

        public const String REQUEST_ARG = "request";
        public const String KEY_ARGS = "key";
        public const String PATH_ARG = "path";
        public const String INSTALL = "install";
        public const String THUMBPRINT_ARG="thumbprint";
        public const String HTTP_VERB_ARG = "verb";
        public const String URL_ARG = "url";
        public const String HEADER_ARG = "header";
        public const String BODY_ARG = "body";
        public const String FILE_BODY_ARG = "filebody";
        public const String PASSWORD_ARG="password";

        private StringDictionary Parameters;
       
       

        public Arguments(string[] Args)
        {
            Parameters = new StringDictionary();
            Regex Spliter = new Regex(@"^-{1,2}|^/|=|:",RegexOptions.IgnoreCase|RegexOptions.Compiled);

            Regex Remover = new Regex(@"^['""]?(.*?)['""]?$",RegexOptions.IgnoreCase|RegexOptions.Compiled);

            string Parameter = null;
            string[] Parts;

            // Valid parameters forms:
            // {-,/,--}param{ ,=,:}((",')value(",'))
            // Examples: 
            // -param1 value1 --param2 /param3:"Test-:-work" 
            //   /param4=happy -param5 '--=nice=--'
            foreach(string Txt in Args)
            {
                // Look for new parameters (-,/ or --) and a
                // possible enclosed value (=,:)
                Parts = Spliter.Split(Txt,3);

                switch(Parts.Length){
                // Found a value (for the last parameter 
                // found (space separator))
                case 1:
                    if(Parameter != null)
                    {
                        if(!Parameters.ContainsKey(Parameter)) 
                        {
                            Parts[0] = 
                                Remover.Replace(Parts[0], "$1");

                            Parameters.Add(Parameter, Parts[0]);
                        }
                        Parameter=null;
                    }
                    // else Error: no parameter waiting for a value (skipped)
                    break;

                // Found just a parameter
                case 2:
                    // The last parameter is still waiting. 
                    // With no value, set it to true.
                    if(Parameter!=null)
                    {
                        if(!Parameters.ContainsKey(Parameter)) 
                            Parameters.Add(Parameter, "true");
                    }
                    Parameter=Parts[1];
                    break;

                // Parameter with enclosed value
                case 3:
                    // The last parameter is still waiting. 
                    // With no value, set it to true.
                    if(Parameter != null)
                    {
                        if(!Parameters.ContainsKey(Parameter)) 
                            Parameters.Add(Parameter, "true");
                    }

                    Parameter = Parts[1];

                    // Remove possible enclosing characters (",')
                    if(!Parameters.ContainsKey(Parameter))
                    {
                        Parts[2] = Remover.Replace(Parts[2], "$1");
                        Parameters.Add(Parameter, Parts[2]);
                    }

                    Parameter=null;
                    break;
                }
            }
            // In case a parameter is still waiting
            if(Parameter != null)
            {
                if(!Parameters.ContainsKey(Parameter)) 
                    Parameters.Add(Parameter, "true");
            }
        }


        public string this [string Param]
        {
            get
            {
                return(Parameters[Param]);
            }
        }

        public String GetArgumentValueWithoutAssertion(params String[] argumentNames)
        {
            String requestString = null;

            foreach (String arg in argumentNames)
            {
                requestString = this[arg];

                if (!string.IsNullOrEmpty(requestString))
                    break;
            }
            
            return requestString;
        }

        public String GetArgumentValue(params String[] argumentNames)
        {
            String requestString = GetArgumentValueWithoutAssertion(argumentNames);

            Assertions.IsNullOrEmpty(requestString);

            return requestString;
        }

        public String GetArgumentValue<E>(params String[] argumentNames) where E : Exception
        {
            String requestString = GetArgumentValueWithoutAssertion(argumentNames);

            Assertions.IsNullOrEmpty<E>(requestString);

            return requestString;
        }
    }

}
