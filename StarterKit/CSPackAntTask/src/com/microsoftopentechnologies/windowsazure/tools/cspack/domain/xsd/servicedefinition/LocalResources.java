/*
 Copyright 2015 Microsoft Open Technologies, Inc.

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


package com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.servicedefinition;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "localStorage"
})
public class LocalResources {
    @XmlElement(name = "LocalStorage")
    protected List<LocalStorage> localStorage;

    public List<LocalStorage> getLocalStorage() {
        return localStorage;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "cleanOnRoleRecycle",
            "name",
            "sizeInMB"
    })
    public static class LocalStorage {
        @XmlAttribute
        protected String cleanOnRoleRecycle;
        @XmlAttribute
        protected String name;
        @XmlAttribute(name = "sizeInMB")
        protected String sizeInMB;

        public String getCleanOnRoleRecycle() {
            return cleanOnRoleRecycle;
        }

        public String getName() {
            return name;
        }

        public String getSizeInMB() {
            return sizeInMB;
        }

        public String getDefaultSticky() {
            return String.valueOf("false".equals(cleanOnRoleRecycle));
        }
    }
}
