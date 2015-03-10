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
