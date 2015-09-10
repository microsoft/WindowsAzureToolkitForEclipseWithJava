/**
 * Copyright (c) Microsoft Corporation
 * 
 * All rights reserved. 
 * 
 * MIT License
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
