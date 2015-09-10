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


import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.*;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.serviceconfiguration.ConfigurationSettings;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.serviceconfiguration.ServiceConfiguration;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.servicedefinition.Endpoints;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.servicedefinition.Role;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.servicedefinition.ServiceDefinition;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.*;

import static com.microsoftopentechnologies.windowsazure.tools.cspack.Configuration.*;

public class BinaryPackageCreator {
    private Configuration configuration;

    public BinaryPackageCreator(Configuration configuration) {
        this.configuration = configuration;
    }

    public void createPackage() throws IOException, JAXBException {
        ServiceConfiguration serviceConfiguration = Utils.parseXmlFile(ServiceConfiguration.class,
                configuration.getProjectDir() + File.separator + configuration.getConfigurationFileName());
        ServiceDefinition serviceDefinition = getServiceDefinition(serviceConfiguration);

        createNamedStreamPackage(serviceDefinition.getWorkerRole());

        createServiceDefinitionPackage(serviceDefinition);

        createServiceModelPackage(serviceDefinition);

        createWorkerRolePackage(serviceDefinition);

        createRelsCsmanAndContentTypes(configuration.getPackageDir(), "cb832ab2-764a-4dcb-adc9-c7abf4593de9.csman", configuration.getPackageFileName(), false, true, serviceDefinition.getWorkerRole());
    }

    private void createWorkerRolePackage(ServiceDefinition serviceDefinition) throws IOException {
        for (Role role : serviceDefinition.getWorkerRole()) {
            String roleFolder = configuration.getPackageDir() + File.separator + role.getName();
            Utils.createDirectory(roleFolder);
            RoleWrapper roleWrapper = new RoleWrapper(role, new RuntimeModel(), false);
            Utils.applyTemplate("LocalContent/Roles/RoleModel.xml", roleWrapper, roleFolder + File.separator + "RoleModel.xml");
            Utils.applyTemplate("LocalContent/Roles/RuntimeSetup.Manifest", serviceDefinition.isRemoteAccess(), roleFolder + File.separator + "RuntimeSetup.Manifest");

            String sdkKit = configuration.getSdkKit();
            addDirectoryToPackage(configuration.getProjectDir() + File.separator + role.getName() + File.separator + "approot", "approot", role.getName());
            addSdkEntryToPackage(sdkKit + BASE_SDK, role.getName(), (sdkKit + BASE_SDK).length());
            if (role.isRemoteAccess()) {
                addSdkEntryToPackage(sdkKit + PLUGINS_SDK + File.separator + Role.REMOTE_ACCESS, role.getName(), SDK_KIT.length());
            }
            if (role.isRemoteForwarder()) {
                addSdkEntryToPackage(sdkKit + PLUGINS_SDK + File.separator + Role.REMOTE_FORWARDER, role.getName(), SDK_KIT.length());
            }
            if (role.isCaching()) {
                addSdkEntryToPackage(sdkKit + PLUGINS_SDK + File.separator + Role.CACHING, role.getName(), SDK_KIT.length());
            }
            if (role.isWebDeploy()) {
                addSdkEntryToPackage(sdkKit + PLUGINS_SDK + File.separator + Role.WEB_DEPLOY, role.getName(), SDK_KIT.length());
            }
            createRelsCsmanAndContentTypes(roleFolder, role.getName() + File.separator + "98663f99-fdef-4cc2-9430-69489a29cc1f.csman",
                    role.getName() + "_" + role.getFilename() + ".cssx", true, false, null);
        }
    }

    private void addDirectoryToPackage(String sourceFolder, String destFolder, String roleName) {
        configuration.getWaPackage().copyFile(new File(sourceFolder), new File(configuration.getPackageDir() + File.separator + roleName + File.separator + destFolder));
    }

    private void addSdkEntryToPackage(String sourceFolder, String roleName, int substringLength) {
        List<String> fileEntries = null;
        try {
            String jarName = String.format("%s%s%s", configuration.getProjectDir(), File.separatorChar, ".cspack.jar");
            fileEntries = Utils.getJarEntries(jarName, sourceFolder.replace("\\", "/"));

            for (String entryName : fileEntries) {
                File newFile = new File(configuration.getPackageDir() + File.separator + roleName + File.separator + entryName.substring(substringLength));
                newFile.getParentFile().mkdirs();
                Utils.copyJarEntry("/" + entryName, newFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createServiceModelPackage(ServiceDefinition serviceDefinition) throws IOException {
        Utils.createDirectory(configuration.getPackageDir() + File.separator + "ServiceModel");
        Utils.applyTemplate("ServiceDefinition/ServiceDefinition.rd", serviceDefinition, configuration.getPackageDir() + File.separator +
                "ServiceModel" + File.separator + "ServiceDefinition.rd");
        Utils.applyTemplate("ServiceDefinition/servicedefinition.rdsc", serviceDefinition, configuration.getPackageDir() + File.separator +
                "ServiceModel" + File.separator + "servicedefinition.rdsc");
        createRelsCsmanAndContentTypes(configuration.getPackageDir() + File.separator + "ServiceModel", "ServiceModel" + File.separator + "024a7a07-08d1-4701-bfc8-9d7f82485c5d.csman",
                "SMPackage_37a5fe60-0e71-499f-bb14-8156682a2cba.csmx", true, false, serviceDefinition.getWorkerRole());
    }

    private void createServiceDefinitionPackage(ServiceDefinition serviceDefinition) throws IOException {
        Utils.createDirectory(configuration.getServiceDefinitionPath());
        Utils.applyTemplateWithPath("ServiceDefinition/ServiceDefinition.csdef", serviceDefinition, configuration.getPackageDir());
        createRelsCsmanAndContentTypes(configuration.getServiceDefinitionPath(), "ServiceDefinition" + File.separator + "bf38f5e9-a5eb-4b03-a80a-8a0ddd3d9db8.csman",
                "SDPackage_2cff6ae5-8ec9-425c-b373-cf0407969e6d.csdx", true, false, serviceDefinition.getWorkerRole());
    }

    private void createRelsCsmanAndContentTypes(String folder, String fileName, String archiveName, boolean deleteSourceFolder, boolean isRoot,
                                                List<Role> roles) throws IOException {
        List<File> files = new ArrayList<File>();
        Utils.generateFileList(files, new File(folder));

        List<FileWithHash> filesWithHash = new ArrayList<FileWithHash>();
        for (File file : files) {
            if (file.isFile() && !file.getName().equals(".rels")) {
                FileWithHash fileWithHash = new FileWithHash(file.getPath().substring(folder.length() + 1).replaceAll("[\\\\]", "/"), Utils.calcHexHash(file));
                filesWithHash.add(fileWithHash);
            }
        }
        Utils.applyTemplate("csman.csman", new FilesWithHash(filesWithHash, isRoot, roles), configuration.getPackageDir() + File.separator + fileName);
        files = new ArrayList<File>();
        Utils.generateFileList(files, new File(folder));
        createRels(folder, files, isRoot);

        files = new ArrayList<File>();
        Utils.generateFileList(files, new File(folder));
        createContentTypes2(files, folder);
        Utils.pack(new File(folder), configuration.getPackageDir() + File.separator + archiveName, true);
        if (deleteSourceFolder) {
            Utils.delete(new File(folder));
        }
    }

    private void createNamedStreamPackage(List<Role> roles) throws IOException {
        Utils.createDirectory(configuration.getNamedStreamsPath());
        for (Role role : roles) {
            createRequiredFeatures(new RoleWrapper(role, new RuntimeModel(), false));
        }
        for (Role role : roles) {
            createSupportedOs(role);
        }
        for (Role role : roles) {
            createSuppportData(new RoleWrapper(role, new RuntimeModel(), false));
        }
        createRelsCsmanAndContentTypes(configuration.getNamedStreamsPath(), "NamedStreams" + File.separator + "7e069539-0350-4dbb-9edc-6ad660bc115d.csman",
                "NamedStreamPackage_285fe43b-7fcf-4ddf-b80b-f242e0ee0e3d.csnsx", true, false, null);
    }

    private void createRels(String folder, List<File> files, boolean isRoot) throws IOException {
        Utils.createDirectory(folder + File.separator + "_rels");
        List<Relationship> rels = new ArrayList<Relationship>();
        RelationshipTypes.TypeId typeId;
        for (File file : files) {
            if (file.getName().equals("Cloud.uar.csman")) {
                typeId = RelationshipTypes.get(file.getName()); // special case for Cloud.uar.csman
            } else {
                typeId = RelationshipTypes.get(file.getName().substring(file.getName().lastIndexOf('.') + 1));
            }
            if (typeId != null) {
                rels.add(new Relationship(typeId, "/" + file.getName()));
            }
        }
        if (isRoot) {
            String sdkVersion = System.getProperty("sdkVersion");
            RelationshipTypes.TypeId sdkTypeId = RelationshipTypes.get(sdkVersion);
            rels.add(new Relationship(sdkTypeId, "http://schemas.microsoft.com:80/ServiceHosting/2009/10/UAR/ProductVersion#" + sdkVersion, true));
        }
        Utils.applyTemplateWithPath("_rels/.rels", rels, folder);
    }

    private ServiceDefinition getServiceDefinition(ServiceConfiguration serviceConfiguration) throws JAXBException {
        ServiceDefinition serviceDefinition = Utils.parseXmlFile(ServiceDefinition.class,
                configuration.getProjectDir() + File.separator + configuration.getDefinitionFileName());
        // add endpoints for caching
        for (Role role : serviceDefinition.getWorkerRole()) {
            // this is a hack to make config files generation easier; these ports are auto-generated to enable caching
            if (role.isCaching()) {
                List<Endpoints.InternalEndpoint> internalEndpoints = role.getEndpoints().getInternalEndpoint();
                internalEndpoints.add(new Endpoints.InternalEndpoint("Microsoft.WindowsAzure.Plugins.Caching.cacheArbitrationPort", "tcp"));
                internalEndpoints.add(new Endpoints.InternalEndpoint("Microsoft.WindowsAzure.Plugins.Caching.cacheClusterPort", "tcp"));
                internalEndpoints.add(new Endpoints.InternalEndpoint("Microsoft.WindowsAzure.Plugins.Caching.cacheReplicationPort", "tcp"));
                internalEndpoints.add(new Endpoints.InternalEndpoint("Microsoft.WindowsAzure.Plugins.Caching.cacheServicePortInternal", "tcp"));
                internalEndpoints.add(new Endpoints.InternalEndpoint("Microsoft.WindowsAzure.Plugins.Caching.cacheSocketPort", "tcp"));
            }
        }
        // for correct templates processing add settings to serviceDefinition object; also add role package filename
        for (Role role : serviceDefinition.getWorkerRole()) {
            List<ConfigurationSettings.Setting> settings = getRoleSettings(role.getName(), serviceConfiguration.getRole());
            if (settings != null) {
                role.getSettings().addAll(settings);
            }
            role.setFilename(UUID.nameUUIDFromBytes(role.getName().getBytes()).toString());
        }
        return serviceDefinition;
    }

    private void createSuppportData(RoleWrapper role) throws IOException {
        Utils.createDirectory(configuration.getNamedStreamsPath() + File.separator + "SupportData");
        Utils.createDirectory(configuration.getNamedStreamsPath() + File.separator + "SupportData" + File.separator + role.getRole().getName());
        Utils.applyTemplate("NamedStreams/SupportData/1.0",
                SupportData.getSupportData(role), configuration.getNamedStreamsPath() + File.separator +
                        "SupportData" + File.separator + role.getRole().getName() + File.separator + "1.0"
        );
    }

    private void createSupportedOs(Role role) throws IOException {
        Utils.createDirectory(configuration.getNamedStreamsPath() + File.separator + "SupportedOSes");
        Utils.createDirectory(configuration.getNamedStreamsPath() + File.separator + "SupportedOSes" + File.separator + role.getName());
        Utils.applyTemplate("NamedStreams/SupportedOsFamilies/1.0",
                SupportedOS.getSupportedOSes(), configuration.getNamedStreamsPath() + File.separator + "SupportedOSes" + File.separator +
                        role.getName() + File.separator + "1.0");
    }

    private void createRequiredFeatures(RoleWrapper role) throws IOException {
        Utils.createDirectory(configuration.getNamedStreamsPath() + File.separator + "RequiredFeatures");
        Utils.createDirectory(configuration.getNamedStreamsPath() + File.separator + "RequiredFeatures" +
                File.separator + role.getRole().getName());
        Utils.applyTemplate("NamedStreams/RequiredFeatures/1.0",
                RequiredFeature.getRequiredFeatures(role.getRuntimeModel()),
                configuration.getNamedStreamsPath() + File.separator + "RequiredFeatures" + File.separator +
                        role.getRole().getName() + File.separator + "1.0");
    }

    private void createContentTypes2(Collection<File> files, String path) throws IOException {
        Map<String, ContentType> model = new HashMap<String, ContentType>();
        boolean hasCloudUarCsman = false;
        for (File file : files) {
            if ("Cloud.uar.csman".equals(file.getName())) {
                hasCloudUarCsman = true;
            }
            String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            String type = ContentTypes.getType(extension);
            if (type == null) {
                type = ContentTypes.getType("other");
            }
            ContentType contentType = new ContentType(extension, type);
            model.put(extension, contentType);
        }
        List<ContentType> override = null;
        if (hasCloudUarCsman) {
            model.put("csman", new ContentType("csman", "user/user"));
            override = Arrays.asList(new ContentType("/98663f99-fdef-4cc2-9430-69489a29cc1f.csman", "text/ucmanifest"));
        }
        Utils.applyTemplate("[Content_Types]_binary.xml", new ContenTypesObject(model.values(), override), path + File.separator + "[Content_Types].xml");
    }

    private List<ConfigurationSettings.Setting> getRoleSettings(String name,
                                                                List<com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.serviceconfiguration.Role> roles) {
        for (com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.serviceconfiguration.Role role : roles) {
            if (role.getName().equals(name)) {
                return role.getConfigurationSettings() == null ? null : role.getConfigurationSettings().getSetting();
            }
        }
        return null;
    }
}
