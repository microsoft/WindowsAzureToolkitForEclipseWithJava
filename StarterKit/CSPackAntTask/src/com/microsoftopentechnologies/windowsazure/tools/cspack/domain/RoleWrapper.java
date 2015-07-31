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

import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.servicedefinition.Role;

public class RoleWrapper {
    private final Role role;
    private final RuntimeModel runtimeModel;
    private final boolean webRole;
    private final String sdkVersion = System.getProperty("sdkVersion");

    public RoleWrapper(Role role, RuntimeModel runtimeModel, boolean webRole) {
        this.role = role;
        this.runtimeModel = runtimeModel;
        this.webRole = webRole;
    }

    public Role getRole() {
        return role;
    }

    public RuntimeModel getRuntimeModel() {
        return runtimeModel;
    }

    public boolean isWebRole() {
        return webRole;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }
}
