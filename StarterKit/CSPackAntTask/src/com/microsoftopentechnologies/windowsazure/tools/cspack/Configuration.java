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


package com.microsoftopentechnologies.windowsazure.tools.cspack;

import com.microsoftopentechnologies.windowsazure.tools.build.PackageType;
import com.microsoftopentechnologies.windowsazure.tools.build.WindowsAzurePackage;

import java.io.File;

public class Configuration {
    static final String PACKAGE_XML = "package.xml";
    static final String BASE_SDK = "/runtimes";
    static final String PLUGINS_SDK = "/plugins";
    public static final String SDK_KIT = "sdkKit";

    private final WindowsAzurePackage waPackage;
    private PackageType packageType;
    private String sdkDir;
    private String sdkKit = "";
    private String emulatorDir;
    private String projectDir;
    private String packageDir;
    private String packageFileName;
    private String definitionFileName;
    private String configurationFileName;
    private String emulatorToolsDir;
    private String cloudToolsDir;
    private String templatesDir;

    // derived properties
    private String namedStreamsPath;
    private String localContentPath;
    private String serviceDefinitionPath;

    public Configuration(WindowsAzurePackage waPackage) {
        this.waPackage = waPackage;
    }

    public void init() {
        namedStreamsPath = packageDir + File.separator + "NamedStreams";
        localContentPath = packageDir + File.separator + "LocalContent";
        serviceDefinitionPath = packageDir + File.separator + "ServiceDefinition";
    }


    public PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    public String getSdkDir() {
        return sdkDir;
    }

    public void setSdkDir(String sdkDir) {
        this.sdkDir = sdkDir;
    }

    public String getSdkKit() {
        return SDK_KIT;
    }

    public void setSdkKit(String sdkKit) {
        this.sdkKit = sdkKit;
    }

    public String getEmulatorDir() {
        return emulatorDir;
    }

    public void setEmulatorDir(String emulatorDir) {
        this.emulatorDir = emulatorDir;
    }

    public String getProjectDir() {
        return projectDir;
    }

    public void setProjectDir(String projectDir) {
        this.projectDir = projectDir;
    }

    public String getPackageDir() {
        return packageDir;
    }

    public void setPackageDir(String packageDir) {
        this.packageDir = packageDir;
    }

    public String getPackageFileName() {
        return packageFileName;
    }

    public void setPackageFileName(String packageFileName) {
        this.packageFileName = packageFileName;
    }

    public String getDefinitionFileName() {
        return definitionFileName;
    }

    public void setDefinitionFileName(String definitionFileName) {
        this.definitionFileName = definitionFileName;
    }

    public String getConfigurationFileName() {
        return configurationFileName;
    }

    public void setConfigurationFileName(String configurationFileName) {
        this.configurationFileName = configurationFileName;
    }

    public String getEmulatorToolsDir() {
        return emulatorToolsDir;
    }

    public void setEmulatorToolsDir(String emulatorToolsDir) {
        this.emulatorToolsDir = emulatorToolsDir;
    }
    
    public String getCloudToolsDir() {
        return cloudToolsDir;
    }

    public void setCloudToolsDir(String cloudToolsDir) {
        this.cloudToolsDir = cloudToolsDir;
    }


    public String getTemplatesDir() {
        return templatesDir;
    }

    public void setTemplatesDir(String templatesDir) {
        this.templatesDir = templatesDir;
    }

    public String getNamedStreamsPath() {
        return namedStreamsPath;
    }

    public String getLocalContentPath() {
        return localContentPath;
    }

    public String getServiceDefinitionPath() {
        return serviceDefinitionPath;
    }

    public WindowsAzurePackage getWaPackage() {
        return waPackage;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "packageType=" + packageType +
                ", sdkDir='" + sdkDir + '\'' +
                ", sdkKit='" + sdkKit + '\'' +
                ", emulatorDir='" + emulatorDir + '\'' +
                ", projectDir='" + projectDir + '\'' +
                ", packageDir='" + packageDir + '\'' +
                ", packageFileName='" + packageFileName + '\'' +
                ", definitionFileName='" + definitionFileName + '\'' +
                ", configurationFileName='" + configurationFileName + '\'' +
                ", emulatorToolsDir='" + emulatorToolsDir + '\'' +
                ", templatesDir='" + templatesDir + '\'' +
                '}';
    }
}
