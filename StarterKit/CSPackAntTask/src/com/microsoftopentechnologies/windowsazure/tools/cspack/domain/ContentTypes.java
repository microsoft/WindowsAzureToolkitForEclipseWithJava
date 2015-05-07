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
package com.microsoftopentechnologies.windowsazure.tools.cspack.domain;

import java.util.HashMap;
import java.util.Map;

public class ContentTypes {
    private static Map<String, String> types = new HashMap<String, String>();
    static {
        types.put("cssx", "binary/software");
        types.put("rels", "application/vnd.openxmlformats-package.relationships+xml");
        types.put("csman", "text/ucmanifest");
        types.put("csdef", "text/servicedescription");
        types.put("rd", "text/servicemodel");
        types.put("rdsc", "text/servicecontract");
        types.put("other", "user/user");
    }

    public static String getType(String extension) {
        return types.get(extension);
    }
}
