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
