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

namespace Gigaspaces.Rest
{
    class Assertions
    {
        private static void Assert<E>(bool condition, String message) where E : Exception
        {
            if (!condition)
                return;

            throw (E)Activator.CreateInstance(typeof(E), message);
        }

        internal static void IsNullOrEmpty(String value)
        {
            Assert<Exception>(string.IsNullOrEmpty(value), "The value is null or empty");
            Assert<Exception>(string.IsNullOrEmpty(value.Trim()), "The value is a sequence of whitespaces");
        }

        internal static void IsNotNull(Object value)
        {
            Assert<Exception>(value == null, "The value is null");
        }

        internal static void IsNullOrEmpty<E>(string value) where E : Exception
        {
            Assert<E>(value == null, "The value is null");
        }

        internal static void IsTrue(bool value)
        {
            Assert<Exception>(!value, "The value is not true");
        }

        internal static void IsFalse(bool value)
        {
            Assert<Exception>(value, "The value is not false");
        }


    }
}
