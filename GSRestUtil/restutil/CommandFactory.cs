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

namespace Gigaspaces.Rest
{
    class  CommandFactory
    {
        public static ICommand CreateCommand(Arguments arguments)
        {
            string c = arguments.GetArgumentValueWithoutAssertion(Arguments.REQUEST_ARG);

            if (!string.IsNullOrEmpty(c))
                return new RestCommand(arguments);

            c = arguments.GetArgumentValueWithoutAssertion(Arguments.INSTALL);
            
            if (!string.IsNullOrEmpty(c))
                return new InstallCommand(arguments);

            throw new UnKnownCommandException();
        }
    }   
}
