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

import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.servicedefinition.ImportElement;

import java.util.ArrayList;
import java.util.List;

public class SupportData {
    private static final String TRUE = "True";
    private static final String FALSE = "False";
    private static final String ELEVATED = "elevated";

    private String sdkVersion;
    private String netFxVersion;
    private boolean hasElevatedEntrypoint;
    private boolean hasElevatedStartupTaskInCsdef;
    private List<ImportElement> imports = new ArrayList<ImportElement>();

    public SupportData(String sdkVersion, String netFxVersion) {
        this.sdkVersion = sdkVersion;
        this.netFxVersion = netFxVersion;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public String getNetFxVersion() {
        return netFxVersion;
    }

    public String isHasElevatedEntrypoint() {
        return hasElevatedEntrypoint ? TRUE : FALSE;
    }

    public List<ImportElement> getImports() {
        return imports;
    }

    public String isHasElevatedStartupTaskInCsdef() {
        return hasElevatedStartupTaskInCsdef ? TRUE : FALSE;
    }

    public static SupportData getSupportData(RoleWrapper role) {
        SupportData supportData = new SupportData(role.getSdkVersion(), role.getRuntimeModel().getNetFxVersion());

        if (role.getRole().getStartup() != null && role.getRole().getStartup().getTask() != null && role.getRole().getStartup().getTask().size() > 0) {
            // todo? can be multiple tasks? !!!!!
            supportData.hasElevatedStartupTaskInCsdef = ELEVATED.equals(role.getRole().getStartup().getTask().get(0).getExecutionContext());
        }
        supportData.hasElevatedEntrypoint = role.getRole().getRuntime() != null && ELEVATED.equals(role.getRole().getRuntime().getExecutionContext());
        supportData.imports = role.getRole().getImports().getImportElements();
        return supportData;
    }
}
