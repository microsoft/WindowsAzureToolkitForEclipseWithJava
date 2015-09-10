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
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PackageCreator {
    private Configuration configuration;

    public PackageCreator(Configuration configuration) {
        this.configuration = configuration;
    }

    public void createPackage() throws IOException, JAXBException {
        ServiceConfiguration serviceConfiguration = Utils.parseXmlFile(ServiceConfiguration.class,
                configuration.getProjectDir() + File.separator + configuration.getConfigurationFileName());
        ServiceDefinition serviceDefinition = getServiceDefinition(serviceConfiguration);

        // ServiceDefinition directory
        createServiceDefinitionsFiles(serviceDefinition);

        createRels();

        // NamedStreams directory
        createNamedStreams(serviceDefinition.getWorkerRole());

        Utils.createDirectory(configuration.getLocalContentPath());

        // prepare LocalContent and generate package.xml
        new PackageDefinitionCreator(configuration).createPackageDefinition(serviceDefinition);

        // create [Content_types].xml file
        createContentTypes();

//        configuration.getWaPackage().zipFile(new File(configuration.getPackageDir()), new File(configuration.getPackageDir() + "\\" + configuration.getPackageFileName()));
//        Utils.zipDirectory(new File(configuration.getPackageDir()), new File(configuration.getPackageDir() + File.separator + configuration.getPackageFileName()),
//                configuration.getWaPackage());
        Utils.pack(new File(configuration.getPackageDir()), configuration.getPackageDir() + File.separator + configuration.getPackageFileName(), true);
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
        // for correct templates processing add settings to serviceDefinition object
        for (Role role : serviceDefinition.getWorkerRole()) {
            List<ConfigurationSettings.Setting> settings = getRoleSettings(role.getName(), serviceConfiguration.getRole());
            if (settings != null) {
                role.getSettings().addAll(settings);
            }
            role.setFilename(UUID.nameUUIDFromBytes(role.getName().getBytes()).toString());
        }
        return serviceDefinition;
    }

    private void createNamedStreams(List<Role> roles) throws IOException {
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

    private void createRels() throws IOException {
        Utils.createDirectory(configuration.getPackageDir() + File.separator + "_rels");
        List<Relationship> rels = new ArrayList<Relationship>();
        RelationshipTypes.TypeId typeId = RelationshipTypes.get("package.xml");
        rels.add(new Relationship(typeId, "/package.xml", true));
        Utils.applyTemplateWithPath("_rels/.rels", rels, configuration.getPackageDir());
    }

    private void createContentTypes() throws IOException {
        List<ContentType> model = new ArrayList<ContentType>();

        File localContent = new File(configuration.getLocalContentPath());
        List<File> files = new ArrayList<File>();
        Utils.generateFileList(files, localContent);
        for (File file : files) {
            ContentType contentType = new ContentType("/LocalContent/" + file.getName());
            model.add(contentType);
        }
        Utils.applyTemplateWithPath("[Content_Types].xml", model, configuration.getPackageDir());
    }

    private void createServiceDefinitionsFiles(ServiceDefinition serviceDefinition) throws IOException {
        Utils.createDirectory(configuration.getServiceDefinitionPath());

        Utils.applyTemplateWithPath("ServiceDefinition/ServiceDefinition.rd", serviceDefinition, configuration.getPackageDir());
        if (serviceDefinition.getWorkerRole().size() > 0 && hasEndpoints(serviceDefinition.getWorkerRole())) {
            Utils.applyTemplateWithPath("ServiceDefinition/servicedefinition.rdsc", serviceDefinition, configuration.getPackageDir());
        }

        Utils.applyTemplateWithPath("ServiceDefinition/ServiceDefinition.csdef", serviceDefinition, configuration.getPackageDir());
    }

    private boolean hasEndpoints(List<Role> workerRoles) {
        for (Role role : workerRoles) {
            if (role.getEndpoints() != null && role.getEndpoints().getInputEndpoint().size() > 0) {
                return true;
            }
        }
        return false;
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
