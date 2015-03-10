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

import java.util.ArrayList;
import java.util.List;

public class SupportedOS {
    private String name;
    private String version;

    private static final List<SupportedOS> supportedOSes = new ArrayList<SupportedOS>();
    static {
        String[] oses = System.getProperty("supportedOS").split(",");
        for (String os : oses) {
            String[] os2version = os.split(":");
            supportedOSes.add(new SupportedOS(os2version[0], os2version[1]));
        }
    }

    public SupportedOS(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public static List<SupportedOS> getSupportedOSes() {
        return supportedOSes;
    }
}
