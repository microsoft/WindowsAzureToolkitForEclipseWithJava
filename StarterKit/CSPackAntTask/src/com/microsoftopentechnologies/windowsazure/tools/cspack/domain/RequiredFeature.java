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

public class RequiredFeature {
    private String name;
    private String version;

    private static final Map<String, RequiredFeature> features = new HashMap<String, RequiredFeature>();

    static {
        String[] requiredFeatures = System.getProperty("requiredFeature").split(",");
        for (String requiredFeature : requiredFeatures) {
            String[] rFeature = requiredFeature.split(":");
            features.put(rFeature[0], new RequiredFeature(rFeature[1], rFeature[2]));
        }
    }

    public RequiredFeature(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public static RequiredFeature getRequiredFeatures(RuntimeModel runtimeModel) {
        RequiredFeature requiredFeature = features.get(runtimeModel.getNetFxVersion());
        if (requiredFeature == null) {
            throw new RuntimeException("'Invalid .NET FX version'");
        }
        return requiredFeature;
    }
}
