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

import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.RoleWrapper;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.RuntimeModel;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.servicedefinition.EntryPoint;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.servicedefinition.Role;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.servicedefinition.ServiceDefinition;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.packagexml.*;
import org.apache.tools.ant.BuildException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static com.microsoftopentechnologies.windowsazure.tools.cspack.Configuration.*;

/**
 * All logic about package.xml
 *
 */
public class PackageDefinitionCreator {
    private Configuration configuration;

    public PackageDefinitionCreator(Configuration configuration) {
        this.configuration = configuration;
    }

    PackageDefinitionElement createPackageDefinition(ServiceDefinition serviceDefinition) throws IOException, JAXBException {
        List<Role> workerRoles = serviceDefinition.getWorkerRole();
        PackageDefinitionElement packageDefinition = new PackageDefinitionElement();
        // add <PackageMetaData>
        PackageMetaDataElement packageMetaData = new PackageMetaDataElement();
        KeyValuePairElement keyValuePair = new KeyValuePairElement();
        keyValuePair.setKey("http://schemas.microsoft.com/windowsazure/ProductVersion/");
        keyValuePair.setValue(System.getProperty("sdkVersion"));
        packageMetaData.getKeyValuePair().add(keyValuePair);
        packageDefinition.setPackageMetaData(packageMetaData);
        // add <PackageContents>
        PackageContentsElement packageContents = new PackageContentsElement();
        packageDefinition.setPackageContents(packageContents);

        // add ServiceDefinition directory
        includeDirectory2Package(packageContents, configuration.getServiceDefinitionPath(), "ServiceDefinition");

        // add NamedStreams directory
        includeDirectory2Package(packageContents, configuration.getNamedStreamsPath(), "NamedStreams");

        // add sdk files
        // i.e. add <ContentDefinition> elements, copy files and return newly created <FileDefinition> elements
        String sdkKit = configuration.getSdkKit();
        List<FileDefinitionElement> sdkFileDefinitions = addSdk2Package(sdkKit + BASE_SDK, packageContents, serviceDefinition.isRemoteAccess());
//        addDirectoryToPackage(packageDefinition, BASE_DIRECTORY);

        // Need this because for each sdk file only one <ContentDefinition> is created, but a new <FileDefinition> is added to each role's <LayoutDefinition>
        List<FileDefinitionElement> remoteAccessFiles = null;
        List<FileDefinitionElement> remoteForwarderFiles = null;
        List<FileDefinitionElement> cachingFiles = null;
        List<FileDefinitionElement> webDeployFiles = null;

        if (serviceDefinition.isRemoteAccess()) {
            remoteAccessFiles = addSdkFiles2Package(sdkKit + PLUGINS_SDK, packageContents, "/" + Role.REMOTE_ACCESS, sdkKit.length());
        }
        if (serviceDefinition.isRemoteForwarder()) {
            remoteForwarderFiles = addSdkFiles2Package(sdkKit + PLUGINS_SDK, packageContents, "/" + Role.REMOTE_FORWARDER, sdkKit.length());
        }
        if (serviceDefinition.isCaching()) {
            cachingFiles = addSdkFiles2Package(sdkKit + PLUGINS_SDK, packageContents, "/" + Role.CACHING, sdkKit.length());
        }
        if (serviceDefinition.isWebDeploy()) {
            webDeployFiles = addSdkFiles2Package(sdkKit + PLUGINS_SDK, packageContents, "/" + Role.WEB_DEPLOY, sdkKit.length());
        }

        // add <PackageLayouts>
        PackageLayoutsElement packageLayouts = new PackageLayoutsElement();
        packageDefinition.setPackageLayouts(packageLayouts);
        for (Role role : workerRoles) {
            LayoutDefintionElement layoutDefintion = createLayoutDefinition(role, packageContents);
            // add sdk file definitions
            layoutDefintion.getLayoutDescription().getFileDefinition().addAll(sdkFileDefinitions);
            // add remoteAccess plugin
            if (role.isRemoteAccess()) {
                layoutDefintion.getLayoutDescription().getFileDefinition().addAll(remoteAccessFiles);
            }
            if (role.isRemoteForwarder()) {
                layoutDefintion.getLayoutDescription().getFileDefinition().addAll(remoteForwarderFiles);
            }
            if (role.isCaching()) {
                layoutDefintion.getLayoutDescription().getFileDefinition().addAll(cachingFiles);
            }
            if (role.isWebDeploy()) {
                layoutDefintion.getLayoutDescription().getFileDefinition().addAll(webDeployFiles);
            }
            packageLayouts.getLayoutDefinition().add(layoutDefintion);
        }

        savePackageXml(packageDefinition);

        return packageDefinition;
    }

    private List<FileDefinitionElement> addSdk2Package(String sdkPath, PackageContentsElement packageContents, Boolean remoteAccess) throws IOException {
        List<FileDefinitionElement> sdkFileDefinitions = addSdkFiles2Package(sdkPath, packageContents);
        // add RuntimeSetup.Manifest
        String fileName = Utils.generateUID("RuntimeSetup.Manifest");
        Utils.applyTemplate("LocalContent/Roles/RuntimeSetup.Manifest", remoteAccess, configuration.getLocalContentPath() + File.separator + fileName);
        sdkFileDefinitions.add(addExistingFile2Package(packageContents, fileName, "RuntimeSetup.Manifest"));

        return sdkFileDefinitions;
    }

    private List<FileDefinitionElement> addSdkFiles2Package(String sdkPath, PackageContentsElement packageContents) throws IOException {
        return addSdkFiles2Package(sdkPath, packageContents, "", sdkPath.length());
    }

    private List<FileDefinitionElement> addSdkFiles2Package(String sdkPath, PackageContentsElement packageContents, String subDirectory, int substringLength) throws IOException {
        List<FileDefinitionElement> sdkFileDefinitions = new ArrayList<FileDefinitionElement>();

//        File root = new File(sdkPath + subDirectory);
//        Collection<File> files = new ArrayList<File>();
//        Utils.generateFileList(files, root);

        List<String> fileEntries = null;
        try {
            System.out.println("looking for " + sdkPath + subDirectory);

            String jarName = String.format("%s%s%s", configuration.getProjectDir(), File.separatorChar, ".cspack.jar");
            fileEntries = Utils.getJarEntries(jarName,/*super.getClass().getResource(*/sdkPath + subDirectory/*)*/);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String entryName : fileEntries) {
            String relativePath = entryName.substring(substringLength);
            FileDefinitionElement fileDefinition = addJarEntry2Package(entryName, packageContents, relativePath, "");
            sdkFileDefinitions.add(fileDefinition);
            System.out.println("adding " + entryName);
        }
//        for (File file : files) {
//            String relativePath = file.getPath().substring(substringLength);
//            FileDefinitionElement fileDefinition = addFile2Package(file, packageContents, relativePath, "");
//            sdkFileDefinitions.add(fileDefinition);
//        }
        return sdkFileDefinitions;
    }

    private FileDefinitionElement addJarEntry2Package(String entryName, PackageContentsElement packageContents, String relativePath, String roleName) {
        FileDefinitionElement fileDefinition = null;
        try {
            System.out.println(relativePath);
            String filename = Utils.generateUID(roleName + relativePath);
            File newFile = new File(configuration.getLocalContentPath() + File.separator + filename);
            Utils.copyJarEntry("/" + entryName, newFile);
            fileDefinition = createFileDefinition(relativePath, filename);

            //todo
            ContentDefintionElement contentDefintion = createContentDefintion(newFile, fileDefinition.getFileDescription().getDataContentReference());
            packageContents.getContentDefinition().add(contentDefintion);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileDefinition;
    }

    private FileDefinitionElement addExistingFile2Package(PackageContentsElement packageContents, String newFileName, String fileName) throws IOException {
        File file = new File(configuration.getLocalContentPath() + File.separator + newFileName);
        FileDefinitionElement fileDefinition = createFileDefinition("\\" + fileName, newFileName);
        ContentDefintionElement contentDefintion = createContentDefintion(file, fileDefinition.getFileDescription().getDataContentReference());
        packageContents.getContentDefinition().add(contentDefintion);
        return fileDefinition;
    }

    private LayoutDefintionElement createLayoutDefinition(Role role, PackageContentsElement packageContents) throws IOException {
        LayoutDefintionElement layoutDefintion = new LayoutDefintionElement();

        layoutDefintion.setName("Roles/" + role.getName());
        LayoutDescriptionElement layoutDescription = new LayoutDescriptionElement();

        layoutDescription.getFileDefinition().add(createRoleModel(role, packageContents));
        // for .dll entry point
        if (role.getRuntime() != null && role.getRuntime().getEntryPoint() != null && role.getRuntime().getEntryPoint().getNetFxEntryPoint() != null) {
            layoutDescription.getFileDefinition().add(createEntryPointTxt(role.getRuntime().getEntryPoint().getNetFxEntryPoint(), packageContents));
        }

        // add approot
        addDirectoryToPackage(layoutDescription, packageContents, configuration.getProjectDir() + File.separator + role.getName(), role.getName());

        layoutDefintion.setLayoutDescription(layoutDescription);

        return layoutDefintion;
    }

    private FileDefinitionElement createEntryPointTxt(EntryPoint.NetFxEntryPoint netFxEntryPoint, PackageContentsElement packageContents) throws IOException {
        String fileName = Utils.generateUID(netFxEntryPoint.getAssemblyName() + "__entrypoint.txt");
        Utils.applyTemplate("LocalContent/Roles/__entrypoint.txt", netFxEntryPoint, configuration.getLocalContentPath() + File.separator + fileName);

        FileDefinitionElement fileDefinition = addExistingFile2Package(packageContents, fileName, "__entrypoint.txt");
        return fileDefinition;
    }

    private void addDirectoryToPackage(LayoutDescriptionElement layoutDescription, PackageContentsElement packageContents, String filepath,
                                       String roleName) throws IOException {
        File root = new File(filepath);

        Collection<File> files = new ArrayList<File>();
        Utils.generateFileList(files, root);

        for (File file : files) {
            String relativePath = file.getPath().substring(filepath.length());
            FileDefinitionElement fileDefinition = addFile2Package(file, packageContents, relativePath, roleName);
            layoutDescription.getFileDefinition().add(fileDefinition);
        }
    }

    private FileDefinitionElement addFile2Package(File file, PackageContentsElement packageContents, String relativePath, String roleName) throws IOException {
        System.out.println(relativePath);
        FileDefinitionElement fileDefinition = copyFileAndCreateDefinition(file, relativePath, roleName);
        ContentDefintionElement contentDefintion = createContentDefintion(file, fileDefinition.getFileDescription().getDataContentReference());
        packageContents.getContentDefinition().add(contentDefintion);
        return fileDefinition;
    }

    private FileDefinitionElement createRoleModel(Role role, PackageContentsElement packageContents) throws IOException {

        // no WebRole's so far
        RoleWrapper roleWrapper = new RoleWrapper(role, new RuntimeModel(), false);
        String fileName = Utils.generateUID(role.getName() + "RoleModel.xml");
        Utils.applyTemplate("LocalContent/Roles/RoleModel.xml", roleWrapper, configuration.getLocalContentPath() + File.separator + fileName);

        FileDefinitionElement fileDefinition = addExistingFile2Package(packageContents, fileName, "RoleModel.xml");
        return fileDefinition;
    }

    static ContentDefintionElement createContentDefintion(File file, String relativePath) throws IOException {
        ContentDefintionElement contentDefintion = new ContentDefintionElement();

        contentDefintion.setName(relativePath);

        ContentDescriptionElement contentDescription = new ContentDescriptionElement();
        contentDescription.setLengthInBytes(BigInteger.valueOf(file.length()));
        contentDescription.setIntegrityCheckHashAlgortihm("Sha256");
        contentDescription.setIntegrityCheckHash(Utils.calcHash(file));
        contentDescription.setDataStorePath(relativePath);

        contentDefintion.setContentDescription(contentDescription);
        return contentDefintion;
    }

    private static void includeDirectory2Package(PackageContentsElement packageContents, String path, String subDirectory) throws IOException {
        Collection<File> files = new ArrayList<File>();
        Utils.generateFileList(files, new File(path));

        for (File file : files) {
            String relativePath = file.getPath().substring(path.length());
            packageContents.getContentDefinition().add(createContentDefintion(file, subDirectory + relativePath.replace("\\", "/")));
        }

    }

    private FileDefinitionElement copyFileAndCreateDefinition(File file, String relativePath, String roleName) throws IOException {
        String filename = Utils.generateUID(roleName + relativePath);
        File newFile = new File(configuration.getLocalContentPath() + File.separator + filename);
        configuration.getWaPackage().copyFile(file, newFile);

        return createFileDefinition(relativePath, filename);
    }

    private static FileDefinitionElement createFileDefinition(String relativePath, String filename) throws IOException {
        FileDefinitionElement fileDefinition = new FileDefinitionElement();
        fileDefinition.setFilePath(relativePath.replace("/", "\\"));

        FileDescriptionElement fileDescription = new FileDescriptionElement();
        fileDescription.setDataContentReference("LocalContent/" + filename);
        GregorianCalendar currentTime = (GregorianCalendar) GregorianCalendar.getInstance();
        try {
            fileDescription.setCreatedTimeUtc(DatatypeFactory.newInstance().newXMLGregorianCalendar(currentTime));
            fileDescription.setModifiedTimeUtc(DatatypeFactory.newInstance().newXMLGregorianCalendar(currentTime));
        } catch (DatatypeConfigurationException ex) {
            throw new BuildException(ex);
        }
        fileDescription.setReadOnly(false);
        fileDefinition.setFileDescription(fileDescription);

        return fileDefinition;
    }

    private void savePackageXml(PackageDefinitionElement packageDefinition) throws JAXBException {
        File file = new File(configuration.getPackageDir() + File.separator + PACKAGE_XML);
        JAXBContext jaxbContext = JAXBContext.newInstance(PackageDefinitionElement.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        jaxbMarshaller.marshal(packageDefinition, file);
//            jaxbMarshaller.marshal(packageDefinition, System.out);
    }
}
