/*
 Copyright Microsoft Corp.

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
package com.microsoftopentechnologies.windowsazure.tools.cspack.domain;

import java.util.HashMap;
import java.util.Map;

public class RelationshipTypes {
    private static Map<String, TypeId> types = new HashMap<String, TypeId>();
    static {
        types.put("csmx", new TypeId("http://schemas.microsoft.com/opc/2006/sample/required-resource", "SERVICEMODEL"));
        types.put("csdx", new TypeId("http://schemas.microsoft.com/opc/2006/sample/required-resource", "SERVICEDESCRIPTION"));
        types.put("csnsx", new TypeId("http://schemas.microsoft.com/opc/2006/sample/required-resource", "NAMEDSTREAMPACKAGE"));
        types.put("csman", new TypeId("http://schemas.microsoft.com/opc/2006/sample/required-resource", "MANIFEST"));
        types.put("csdef", new TypeId("http://schemas.microsoft.com/opc/2006/sample/required-resource", "SERVICEDESCRIPTION"));
        types.put("rd", new TypeId("http://schemas.microsoft.com/opc/2006/sample/required-resource", "SERVICEMODEL"));
        types.put("Cloud.uar.csman", new TypeId("http://schemas.microsoft.com/ServiceHosting/2009/10/UAR/Manifest", "Re42d9adc9edc47db"));
        types.put(System.getProperty("sdkVersion"), new TypeId("http://schemas.microsoft.com/ServiceHosting/2009/10/UAR/ProductVersion", "R22fdca9f161f438d"));
        types.put("package.xml", new TypeId("http://schemas.microsoft.com/windowsazure/PackageDefinition/Version/2012/03/15", "R2e2c109ac472472f"));
    }

    public static TypeId get(String extension) {
        return types.get(extension);
    }

    public static class TypeId {
        private String type;
        private String id;

        public TypeId(String type, String id) {
            this.type = type;
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }
    }
}
