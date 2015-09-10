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
