/**
 * Copyright 2011 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.interopbridges.tools.windowsazure.v16;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class representing Windows Azure project.
 */
public class WindowsAzureProjectManager_v16 {
    private String configFilePath = "";
    private String defFilePath = "";
    private String packageFilePath = "";
    private Document packageFileDoc;
    private Document configFileDoc;
    private Document definitionFileDoc;
    protected String projDirPath;
    private WindowsAzureProjectManager_v16 winAzureProjMgr = null;
    protected List<WindowsAzureRole_v16> roleList = new ArrayList<WindowsAzureRole_v16>();
    protected Map<String, String[]> mapActivity = new HashMap<String, String[]>();
    private static final int BUFF_SIZE = 1024;

    private static enum WAvmSize {EXTRASMALL, SMALL, MEDIUM, LARGE, EXTRALARGE };
    private static String[] vmSize = {"extrasmall", "small", "medium", "large", "extralarge"};
    private static Set<String> waVmSize = new HashSet<String>(Arrays.asList(vmSize));

    WindowsAzureProjectManager_v16(File projDirectoryPath)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        XPath xPath = XPathFactory.newInstance().newXPath();
        packageFilePath = String.format("%s%s%s", projDirectoryPath,
                File.separator, "package.xml");
        packageFileDoc = ParserXMLUtility_v16.parseXMLFile(packageFilePath);
        projDirPath = projDirectoryPath.toString();
        roleList = new ArrayList<WindowsAzureRole_v16>();
        if (packageFileDoc != null) {
            try {
                configFilePath = String.format("%s%s%s", projDirectoryPath,
                        File.separator, xPath.evaluate(
                                WindowsAzureConstants_v16.CONFIG_FILE_NAME,
                                packageFileDoc));
                defFilePath = String.format("%s%s%s", projDirectoryPath,
                        File.separator,  xPath.evaluate(
                                WindowsAzureConstants_v16.DEF_FILE_NAME,
                                packageFileDoc));
            } catch (Exception ex) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.EXCP_RETRIEVE_DATA, ex);
            }
        }
    }

    /**
     * Creates a new instance of WindowsAzureProjectManager.
     *
     * @param projectDirectoryPath
     * @param fileName .
     * @return WindowsAzureProjectManager instance .
     * @throws WindowsAzureInvalidProjectOperationException .
     * @throws IOException .
     */
    public static WindowsAzureProjectManager_v16 create(String fileName)
    throws WindowsAzureInvalidProjectOperationException_v16, IOException {

        if ((fileName == null) || (fileName.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        ZipFile zipFile = new ZipFile(fileName);
        String tmpPath = System.getProperty("java.io.tmpdir");
        String projPath = String.format("%s%s%s", tmpPath,
                File.separator, "%proj%");
        File projFile = new File(projPath);
        if (projFile != null) {
            deleteDir(projFile);
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        String entryName;
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            entryName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                (new File(tmpPath.concat(entryName))).mkdir();
                continue;
            }

            String outputPath = tmpPath.concat(entryName);

            File outputFile = new File(outputPath);
            dataTransfer(zipFile.getInputStream(zipEntry),
                    new BufferedOutputStream(new FileOutputStream(outputFile)));
        }
        zipFile.close();

        tmpPath = System.getProperty("java.io.tmpdir");
        try {
            WindowsAzureProjectManager_v16 winAzureProjMgr =
                new WindowsAzureProjectManager_v16(new File(String.format("%s%s%s",
                        tmpPath, File.separator, "%proj%")));
            winAzureProjMgr.setWindowsAzureProjMgr(winAzureProjMgr);
            return winAzureProjMgr;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_INITIALIZE, ex);
        }
    }

    /**
     * Loads and deserializes WindowsAzureProjectManager from specified location
     * on disk.
     *
     * @param projDirectoryPath
     * @return WindowsAzureProjectManager instance.
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public static WindowsAzureProjectManager_v16 load(File projDirectoryPath)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        /**
         * Read project xml file and get the following parameters
         * configurationfilename definitionfilename
         */
        if (projDirectoryPath == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        WindowsAzureProjectManager_v16 waProjectManager;
        try {
            waProjectManager = new WindowsAzureProjectManager_v16(
                    projDirectoryPath);
            waProjectManager
            .setWindowsAzureProjMgr(waProjectManager);
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RETRIEVE_DATA, ex);
        }
        return waProjectManager;
    }

    /**
     * Validates the specified role name.
     *
     * @param roleName
     * @return true if the role name is valid; false otherwise.
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public Boolean isAvailableRoleName(String roleName)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        Boolean isAvaRname = true;
        try {

            if ((roleName == null)
                    || (roleName.isEmpty() || (roleName.isEmpty()))) {
                isAvaRname = false;
            } else {
                List<WindowsAzureRole_v16> roles = getWindowsAzureProjMgr()
                .getRoles();
                for (int i = 0; i < roles.size(); i++) {
                    if (roles.get(i).getName().equalsIgnoreCase(roleName)) {
                        isAvaRname = false;
                    }
                }
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_IS_AVAILABLE_ROLENAME, e);
        }
        return isAvaRname;
    }

    /**
     * Validates the specified service name.
     *
     * @param serviceName
     * @return true if the service name is valid; false otherwise.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public Boolean isValidServiceName(String serviceName)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        Boolean isAvaSname = true;
        if ((serviceName == null) || (serviceName.isEmpty())) {
            isAvaSname = false;
        } else {
            try {
                if (winAzureProjMgr.getServiceName().equalsIgnoreCase(
                        serviceName)) {
                    isAvaSname = false;
                }
            } catch (Exception e) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.EXCP_IS_AVAILABLE_SERVICE_NAME,
                        e);
            }
        }
        return isAvaSname;
    }

    /**
     * Validates the specified port.
     *
     * @param port
     * @param endpointType
     * @return true if the port if valid; false otherwise.
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public Boolean isValidPort(String port,
            WindowsAzureEndpointType_v16 endpointType)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        Boolean isValidPort = true;
        try {

            if ((port == null) || port.isEmpty()
                    || (endpointType == null)) {
                isValidPort = false;
            } else if (isValidPort) {
                try {
                    Integer.parseInt(port);
                } catch (NumberFormatException nExcp) {
                    isValidPort = false;
                }
            }
            if (isValidPort) {
                List<WindowsAzureRole_v16> roles = getWindowsAzureProjMgr()
                .getRoles();
                for (int i = 0; i < roles.size(); i++) {
                    List<WindowsAzureEndpoint_v16> endPoints = roles.get(i)
                    .getEndpoints();
                    for (int nEndpoint = 0; nEndpoint < endPoints.size();
                    nEndpoint++) {

                        if (endPoints.get(nEndpoint).getInputPort()
                                .equalsIgnoreCase(port)
                                || endPoints.get(nEndpoint).getInputLocalPort()
                                .equalsIgnoreCase(port)
                                || endPoints.get(nEndpoint).getInternalPort()
                                .equalsIgnoreCase(port)
                                || endPoints.get(nEndpoint)
                                .getInternalFixedPort()
                                .equalsIgnoreCase(port)) {
                            isValidPort = false;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_IS_AVAILABLE_PORT, ex);
        }
        return isValidPort;
    }

    /**
     * Serializes and saves WindowsAzureProjectManager to disk.
     *
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void save() throws WindowsAzureInvalidProjectOperationException_v16 {
        try {

            ParserXMLUtility_v16.saveXMLFile(configFilePath, getConfigFileDoc());
            ParserXMLUtility_v16.saveXMLFile(defFilePath, getdefinitionFileDoc());
            ParserXMLUtility_v16.saveXMLFile(packageFilePath, getPackageFileDoc());

            if (mapActivity.containsKey("add")) {
                String[] value = (String[]) mapActivity.get("add");
                String path = String.format("%s%s%s", projDirPath,
                        File.separator, value[0]);
                boolean success = new File(path).mkdir();
                if (!success) {
                    throw new WindowsAzureInvalidProjectOperationException_v16(
                            WindowsAzureConstants_v16.DIR_NOT_CREATED + path);
                }
                success = new File(String.format("%s%s%s%s%s", path,
                        File.separator, "approot", File.separator,
                "util")).mkdirs();
                if (!success) {
                    throw new WindowsAzureInvalidProjectOperationException_v16(
                            WindowsAzureConstants_v16.DIR_NOT_CREATED + path);
                }
                //
                String utilPath = String.format("%s%s%s%s%s%s", path,
                    File.separator, "approot", File.separator,
                    "util", File.separator);

                //Copy to Approot
                copyResourceFile("/Role/HelloWorld.war", String.format("%s%s%s%s%s",
                        path, File.separator, "appRoot", File.separator, "HelloWorld.war"));
                copyResourceFile("/Role/startup.cmd", String.format("%s%s%s%s%s",
                        path, File.separator, "appRoot", File.separator, "startup.cmd"));
                copyResourceFile("/Role/run.cmd", String.format("%s%s%s%s%s",
                        path, File.separator, "appRoot", File.separator,
                        "run.cmd"));

                // Copy whileproc
                copyResourceFile("/Role/whileproc.cmd", utilPath + "whileproc.cmd");
                // Copy unzip.vbs
                copyResourceFile("/Role/unzip.vbs", utilPath + "unzip.vbs");
                // Copy download.vbs
                copyResourceFile("/Role/download.vbs", utilPath + "download.vbs");
                mapActivity.remove("add");
                copyResourceFile("/Role/.start.cmd", utilPath + ".start.cmd");

                mapActivity.remove("add");
            }
            if (mapActivity.containsKey("rename")) {
                String[] value = (String[]) mapActivity.get("rename");
                boolean success = new File(value[0]).renameTo(new File(
                        value[1]));
                if (!success) {
                    throw new WindowsAzureInvalidProjectOperationException_v16(
                            "Not able to rename from " + value[0] + "to : "
                            + value[1]);
                }
                mapActivity.remove("rename");
            }
            if (mapActivity.containsKey("delete")) {
                String[] value = (String[]) mapActivity.get("delete");
                File file = new File(projDirPath + File.separator + value[0]);
                if (file.exists()) {
                    boolean success = deleteDir(file);
                    if (!success) {
                        throw new WindowsAzureInvalidProjectOperationException_v16(
                                WindowsAzureConstants_v16.DIR_NOT_DELETED);
                    }
                }
                mapActivity.remove("delete");
            }
            if (mapActivity.containsKey("addSAFilesForRole")) {
              String[] value = (String[]) mapActivity.get("addSAFilesForRole");
              copySAResources(value[0]);
              mapActivity.remove("addSAFilesForRole");

            }
            if (mapActivity.containsKey("delSAFilesForRole")) {
                String[] value = (String[]) mapActivity.get("delSAFilesForRole");
                String dirPath= String.format("%s%s%s%s%s%s%s", projDirPath,File.separator,value[0],File.separator,
                        WindowsAzureConstants_v16.APPROOT_NAME,File.separator,WindowsAzureConstants_v16.SA_FOLDER_NAME);
                File file = new File(dirPath);
                WindowsAzureProjectManager_v16.deleteDir(file);
                mapActivity.remove("delSAFilesForRole");

            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SAVE, ex);
        }
    }

    /**
     * Gets the service name.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public String getServiceName()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        String sName = "";
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            if (getdefinitionFileDoc() != null) {
                sName =  xPath.evaluate(WindowsAzureConstants_v16.SERVICE_NAME,
                        getdefinitionFileDoc());
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_GET_SERVICE_NAME, ex);
        }
        return sName;
    }

    /**
     * Sets the service name.
     *
     * @param serviceName
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setServiceName(String serviceName)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((serviceName == null) || serviceName.isEmpty()) {
            throw new IllegalArgumentException(WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            if (getdefinitionFileDoc() != null) {
                Node root = getdefinitionFileDoc().getDocumentElement();
                NamedNodeMap nMap = root.getAttributes();
                nMap.getNamedItem("name").setNodeValue(serviceName);
            }
            if (getConfigFileDoc() != null) {
                Node root = getConfigFileDoc().getDocumentElement();
                NamedNodeMap nMap = root.getAttributes();
                nMap.getNamedItem("serviceName").setNodeValue(serviceName);
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SET_SERVICE_NAME, ex);
        }
    }

    /**
     * Gets the package type.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public WindowsAzurePackageType_v16 getPackageType()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expr = WindowsAzureConstants_v16.PROJECT_TYPE;
            Document doc = getWindowsAzureProjMgr().packageFileDoc;
            String packageType = xPath.evaluate(expr, doc);
            return WindowsAzurePackageType_v16.valueOf(packageType.toUpperCase(
                    Locale.getDefault()));
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_GET_PACKAGE_TYPE, ex);
        }
    }

    /**
     * Sets the package type.
     *
     * @param packageType .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public void setPackageType(WindowsAzurePackageType_v16 packageType)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (packageType == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.evaluate(
                    WindowsAzureConstants_v16.PROJECT_TYPE,
                    getWindowsAzureProjMgr().packageFileDoc,
                    XPathConstants.NODE);
            node.setNodeValue(packageType.name().toLowerCase());
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SET_PACKAGE_TYPE, ex);
        }
        
        
    }

    /**
     * Gets the list of roles that are associated with this instance of
     * WindowsAzureProjectManager.
     *
     * @return The list of roles.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    public List<WindowsAzureRole_v16> getRoles()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            if (roleList.isEmpty()) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                NodeList nodeList = (NodeList) xPath.evaluate(
                        WindowsAzureConstants_v16.WORKER_ROLE,
                        getdefinitionFileDoc(),
                        XPathConstants.NODESET);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element workerRole = (Element) nodeList.item(i);
                    WindowsAzureRole_v16 winAzureRole = new WindowsAzureRole_v16(
                            this);
                    String roleName = workerRole.getAttribute("name");
                    if (roleName.isEmpty()) {
                        throw new WindowsAzureInvalidProjectOperationException_v16(
                                WindowsAzureConstants_v16.BLANK_RNAME);
                    }
                    winAzureRole.setName(workerRole.getAttribute("name"));
                    String vmSize = "";
                    vmSize = workerRole.getAttribute("vmsize");
                    if (vmSize.isEmpty()) {
                        vmSize = "Small";
                    }
                    winAzureRole.setVMSize(vmSize);
                    winAzureRole.setEndpoints(winAzureRole.getEndpoints());
                    roleList.add(winAzureRole);
                }
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_GET_ROLE, ex);
        }
        return roleList;
    }

    /**
     * Adds a new role to this instance of WindowsAzureProjectManager.
     *
     * @param roleName
     * @return A new instance of WindowsAzureRole.
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public WindowsAzureRole_v16 addRole(String roleName)
    throws WindowsAzureInvalidProjectOperationException_v16 {

        if ((roleName == null) || (roleName.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            String username = "";
            String password = "";
            Date expirydate = null;
            String fingerprint = "";
            boolean isRAccess = getRemoteAccessAllRoles();
            if (isRAccess) {
                username = getRemoteAccessUsername();
                password = getRemoteAccessEncryptedPassword();
                expirydate = getRemoteAccessAccountExpiration();
                fingerprint = getRemoteAccessCertificateFingerprint();
            }
            WindowsAzureRole_v16 newWinAzureRole = new WindowsAzureRole_v16(this);
            XPath xPath = XPathFactory.newInstance().newXPath();
            // Add in definition File
            Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
            Element eleRole = doc.createElement("WorkerRole");
            eleRole.setAttribute("name", roleName);
            eleRole.setAttribute("vmsize", "");
            eleRole.setAttribute("enableNativeCodeExecution", "true");
            Element root = doc.getDocumentElement();
            root.appendChild(eleRole);

            //Add startup tag:
            Element eleStartup = doc.createElement(
                    WindowsAzureConstants_v16.DEF_FILE_STARTUP_ELEMENT_NAME);
            eleRole.appendChild(eleStartup);

            //Add task
            Comment sampleTask = doc.createComment(WindowsAzureConstants_v16.SAMPLE_TASK_COMMENT);
            eleStartup.appendChild(sampleTask);
            Element eleTask = doc.createElement(WindowsAzureConstants_v16.DEF_FILE_TASK_ELEMENT_NAME);
            eleTask.setAttribute(WindowsAzureConstants_v16.ATTR_CMD_LINE, WindowsAzureConstants_v16.TASK_CMD_LINE);
            eleTask.setAttribute(WindowsAzureConstants_v16.ATTR_EXE_CONTEXT, "elevated");
            eleTask.setAttribute(WindowsAzureConstants_v16.ATTR_TASK_TYPE, "simple");
            eleStartup.appendChild(eleTask);

            //add <RunTime> tag
            Element eleRunTime = doc.createElement("Runtime");
            eleRunTime.setAttribute("executionContext", "elevated");
            Element eleEntryPt = doc.createElement("EntryPoint");
            Comment sampleEntryPt = doc.createComment(WindowsAzureConstants_v16.SAMPLE_ENTRY_PT_COMMENT);
            Element elePrpgEpt = doc.createElement("ProgramEntryPoint");
            elePrpgEpt.setAttribute("commandLine", "startup.cmd");
            elePrpgEpt.setAttribute("setReadyOnProcessStart", "true");
            eleRunTime.appendChild(eleEntryPt);
            eleEntryPt.appendChild(sampleEntryPt);
            eleEntryPt.appendChild(elePrpgEpt);
            eleRole.appendChild(eleRunTime);

            if (isRAccess) {
                Element eleImports = doc.createElement("Imports");
                Element eleAccess = doc.createElement("Import");
                eleAccess.setAttribute("moduleName", "RemoteAccess");
                eleImports.appendChild(eleAccess);
                eleRole.appendChild(eleImports);
            }
            Element eleEndpoints = doc.createElement("Endpoints");
            eleRole.appendChild(eleEndpoints);


            newWinAzureRole.setName(roleName);
            roleList.add(newWinAzureRole);

            // Add in Config file

            doc = getWindowsAzureProjMgr().getConfigFileDoc();
            eleRole = doc.createElement("Role");
            eleRole.setAttribute("name", roleName);
            Element instances = doc.createElement("Instances");
            instances.setAttribute("count", "");
            eleRole.appendChild(instances);
            doc.getDocumentElement().appendChild(eleRole);

            if (isRAccess) {
                Element configSettings = doc
                .createElement("ConfigurationSettings");
                Element eleSettingEnabled = doc.createElement("Setting");
                eleSettingEnabled.setAttribute("name",
                        WindowsAzureConstants_v16.REMOTEACCESS_ENABLED);
                eleSettingEnabled.setAttribute("value", "true");
                configSettings.appendChild(eleSettingEnabled);
                eleRole.appendChild(configSettings);
                Element certificates = doc.createElement("Certificates");
                eleRole.appendChild(certificates);
                newWinAzureRole.setAccUsername(username);
                newWinAzureRole.setAccPassword(password);
                DateFormat formatter = new SimpleDateFormat(
                        WindowsAzureConstants_v16.DATE_FORMAT, Locale.getDefault());
                String dateStr = formatter.format(expirydate);
                newWinAzureRole.setAccExpiryDate(dateStr);
                newWinAzureRole.setThumbprint(fingerprint);
            }

            // Add entry in package.xml
            doc = getPackageFileDoc();
            eleRole = doc.createElement("workerrole");
            eleRole.setAttribute("approotdir",
                    String.format("%s%s%s", "${basedir}\\", roleName,
                    "\\approot"));
            eleRole.setAttribute("name", roleName);
            Node node = (Node) xPath.evaluate(
                    WindowsAzureConstants_v16.WINAZURE_PACKAGE, doc,
                    XPathConstants.NODE);
            node.appendChild(eleRole);

            String[] value = {roleName};
            mapActivity.put("add", value);
            return newWinAzureRole;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_ADD_ROLE, ex);
        }
    }

    /**
     * Returns the WindowsAzureRole object corresponding to the role pointed at
     * by path parameter or null if the folder pointed by path is not a role
     * folder.
     *
     * @param path
     * @return WindowsAzureRole object
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public WindowsAzureRole_v16 roleFromPath(File path)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (path == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        WindowsAzureRole_v16 role = null;
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWindowsAzureProjMgr().getPackageFileDoc();
            String expr = String.format(
                    WindowsAzureConstants_v16.WA_PACK_NAME,
                    path.getName());
            Node nodeRole = (Node) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (nodeRole != null) {
                List<WindowsAzureRole_v16> listRoles = getRoles();
                for (Iterator<WindowsAzureRole_v16> iterator = listRoles.iterator();
                iterator.hasNext();) {
                    WindowsAzureRole_v16 windowsAzureRole = iterator.next();
                    if (windowsAzureRole.getName().equalsIgnoreCase(
                            path.getName())) {
                        role = windowsAzureRole;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_ROLE_FROM_PATH, e);
        }
        return role;
    }

    /**
     * This method enables/disables the Remote access support.
     *
     * @param value
     *            : if true enables Remote access else disables it.
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setRemoteAccessAllRoles(Boolean value)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            if (value) {
                // Create entries in configuration (cscfg) file
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWindowsAzureProjMgr().getConfigFileDoc();
                NodeList listRoles = (NodeList) xPath
                .evaluate(WindowsAzureConstants_v16.ROLE, doc,
                        XPathConstants.NODESET);
                for (int i = 0; i < listRoles.getLength(); i++) {
                    Element element = (Element) listRoles.item(i);
                    String expr = String.format(
                            WindowsAzureConstants_v16.CONFIG_ROLE_SET,
                            element.getAttribute("name"));
                    Element eleConfigSettings = (Element) xPath.evaluate(expr,
                            doc, XPathConstants.NODE);
                    if (eleConfigSettings == null) {
                        eleConfigSettings = doc
                        .createElement("ConfigurationSettings");
                        element.appendChild(eleConfigSettings);
                    }

                    createRAEnableSetting(xPath, doc, eleConfigSettings,
                            element);
                    expr =  String.format(WindowsAzureConstants_v16.CERT_ROLE,
                            element.getAttribute("name"));
                    Element eleCertificates = (Element) xPath.evaluate(expr,
                            doc, XPathConstants.NODE);
                    if (eleCertificates == null) {
                        eleCertificates = doc.createElement("Certificates");
                        element.appendChild(eleCertificates);
                    }
                }
                // Create entries in definition (csdef) file
                createEntriesForDefFile();

                // Check for RemoteForwarder Setting and Import, if not present
                // add it to the first role
                addRemoteForwarder();

                // Create entries in package.xml
                doc = getWindowsAzureProjMgr().getPackageFileDoc();
                xPath = XPathFactory.newInstance().newXPath();
                String expr = WindowsAzureConstants_v16.PROJ_PROPERTY;
                Element eleProjProperty = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                // If waprojectproperties target not present
                if (eleProjProperty == null) {
                    Element eleProj = doc.createElement("target");
                    eleProj.setAttribute("description",
                            WindowsAzureConstants_v16.PROJ_PROPERTY_DESC);
                    eleProj.setAttribute("name", "waprojectproperties");
                    Element eleProperty = doc.createElement("property");
                    eleProperty.setAttribute("name",
                    "project.enableremoteaccess");
                    eleProperty.setAttribute("value", "true");
                    eleProj.appendChild(eleProperty);

                    expr = "/project";
                    Element root = (Element) xPath.evaluate(expr, doc,
                            XPathConstants.NODE);
                    expr = WindowsAzureConstants_v16.CREATE_PKG_TARGET;
                    Element eleTarget = (Element) xPath.evaluate(expr, doc,
                            XPathConstants.NODE);
                    root.insertBefore(eleProj, eleTarget);
                } else {
                    xPath = XPathFactory.newInstance().newXPath();
                    expr = WindowsAzureConstants_v16.PROJ_REMOTE_ACCESS;
                    Element eleRemoteAccess = (Element) xPath.evaluate(expr,
                            doc, XPathConstants.NODE);
                    if (eleRemoteAccess == null) {
                        eleRemoteAccess = doc.createElement("property");
                        eleRemoteAccess.setAttribute("name",
                        "project.enableremoteaccess");
                        eleRemoteAccess.setAttribute("value", "true");
                        eleProjProperty.appendChild(eleRemoteAccess);
                    } else {
                        eleRemoteAccess.setAttribute("value", "true");
                    }
                }

            } else {
                // Remove entries from configuration (cscfg) file
                removeEntriesFromConfigFile(WindowsAzureConstants_v16.REMOTEACCESS_ENABLED);
                removeEntriesFromConfigFile(WindowsAzureConstants_v16.REMOTEACCESS_USERNAME);
                removeEntriesFromConfigFile(WindowsAzureConstants_v16.REMOTEACCESS_PASSWORD);
                removeEntriesFromConfigFile(WindowsAzureConstants_v16.REMOTEACCESS_EXPIRY);
                removeEntriesFromConfigFile(WindowsAzureConstants_v16.REMOTEFORWARDER_ENABLED);
                removeCertificatesFromConfigFile();

                // Remove entries from package file
                XPath xPath = XPathFactory.newInstance().newXPath();
                String expr = WindowsAzureConstants_v16.PROJ_REMOTE_ACCESS;
                Document doc = getWindowsAzureProjMgr().getPackageFileDoc();
                Element element = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                if (element != null) {
                    element.getParentNode().removeChild(element);
                }
                xPath = XPathFactory.newInstance().newXPath();
                expr = WindowsAzureConstants_v16.PROJ_REMOTE_DESKTOP;
                element = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                if (element != null) {
                    element.getParentNode().removeChild(element);
                }
                // Remove entries from definition (csdef) file
                doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
                xPath = XPathFactory.newInstance().newXPath();
                expr = WindowsAzureConstants_v16.PROJ_IMPORT_ACCESS;
                NodeList listImport = (NodeList) xPath.evaluate(expr, doc,
                        XPathConstants.NODESET);
                for (int i = 0; i < listImport.getLength(); i++) {
                    element = (Element) listImport.item(i);
                    element.getParentNode().removeChild(element);
                }
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_ALLUSER, e);
        }
    }

    /**
     * Creates entries in definition file to enable remote access.
     *
     * @throws WindowsAzureInvalidProjectOperationException_v16
     * @throws XPathExpressionException
     */
    private void createEntriesForDefFile()
    throws WindowsAzureInvalidProjectOperationException_v16,
    XPathExpressionException {
        Document doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expr = WindowsAzureConstants_v16.WORKER_ROLE;
        NodeList listWorkerRoles = (NodeList) xPath.evaluate(expr, doc,
                XPathConstants.NODESET);
        for (int i = 0; i < listWorkerRoles.getLength(); i++) {
            Element element = (Element) listWorkerRoles.item(i);

            expr = String.format(WindowsAzureConstants_v16.IMPORT,
                    element.getAttribute("name"));
            Element eleImports = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleImports == null) {
                eleImports = doc.createElement("Imports");
                element.appendChild(eleImports);
            }
            expr = String.format(WindowsAzureConstants_v16.IMPORT_MNANE,
                    element.getAttribute("name"));
            Element eleAccess = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleAccess == null) {
                eleAccess = doc.createElement("Import");
                eleAccess.setAttribute("moduleName", "RemoteAccess");
                eleImports.appendChild(eleAccess);
            }
        }
    }

    /**
     * Removes Certificates node from configuration file when remote access is
     * disabled.
     *
     * @throws XPathExpressionException
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    private void removeCertificatesFromConfigFile()
    throws XPathExpressionException,
    WindowsAzureInvalidProjectOperationException_v16 {
        Document doc = getWindowsAzureProjMgr().getConfigFileDoc();
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expr = WindowsAzureConstants_v16.FPRINT_ALL;
        NodeList listCertificates = (NodeList) xPath.evaluate(expr, doc,
                XPathConstants.NODESET);
        for (int i = 0; i < listCertificates.getLength(); i++) {
            Element ele = (Element) listCertificates.item(i);
            ele.getParentNode().removeChild(ele);
        }
    }

    /**
     * Creates setting node in configuration file to set enable remote access
     * property to true.
     *
     * @param xPath
     * @param doc
     * @param eleConfigSettings
     * @param element
     * @throws XPathExpressionException
     */
    private void createRAEnableSetting(XPath xPath, Document doc,
            Element eleConfigSettings, Element element)
    throws XPathExpressionException {
        String expr = String.format(WindowsAzureConstants_v16.RA_ROLE_ENABLED,
                element.getAttribute("name"));
        Element eleSettingEnabled = (Element) xPath.evaluate(expr,
                doc, XPathConstants.NODE);
        if (eleSettingEnabled == null) {
            eleSettingEnabled = doc.createElement("Setting");
            eleSettingEnabled.setAttribute("name",
                    WindowsAzureConstants_v16.REMOTEACCESS_ENABLED);
            eleSettingEnabled.setAttribute("value", "true");
            eleConfigSettings.appendChild(eleSettingEnabled);
        } else {
            eleSettingEnabled.setAttribute("value", "true");
        }
    }

    /**
     * Creates remote forwarder setting node in configuration file.
     *
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    protected void addRemoteForwarder()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWindowsAzureProjMgr().getConfigFileDoc();
            String expr = WindowsAzureConstants_v16.RF_ENABLED;
            Element eleRemForwarder = (Element) xPath.evaluate(expr,
                    doc, XPathConstants.NODE);
            if (eleRemForwarder == null) {
                eleRemForwarder = doc.createElement("Setting");
                eleRemForwarder.setAttribute("name",
                        WindowsAzureConstants_v16.REMOTEFORWARDER_ENABLED);
                eleRemForwarder.setAttribute("value", "true");

                Element parent = (Element) xPath.evaluate(
                        WindowsAzureConstants_v16.ROLE
                        + "[1]/ConfigurationSettings",
                        doc, XPathConstants.NODE);
                parent.appendChild(eleRemForwarder);
            } else {
                eleRemForwarder.setAttribute("value", "true");
            }

            expr = WindowsAzureConstants_v16.WORKER_ROLE
            + "/Imports/Import[@moduleName='RemoteForwarder']";
            doc = getWindowsAzureProjMgr().getdefinitionFileDoc();
            Element eleForwarder = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleForwarder == null) {
                eleForwarder = doc.createElement("Import");
                eleForwarder.setAttribute("moduleName", "RemoteForwarder");
                expr = WindowsAzureConstants_v16.WORKER_ROLE + "[1]/Imports";
                Element eleImports = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                eleImports.appendChild(eleForwarder);
            }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP, ex);
        }
    }

    /**
     * Removes entries from configuration file to disable the remote access.
     *
     * @param setting
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    private void removeEntriesFromConfigFile(String setting)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
        Document doc = getWindowsAzureProjMgr().getConfigFileDoc();
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expr = String.format("%s%s%s%s",
                WindowsAzureConstants_v16.CONFIG_SETTING, "[@name='",
                setting, "']");
        NodeList listSetting = (NodeList) xPath.evaluate(expr, doc,
                XPathConstants.NODESET);
        for (int i = 0; i < listSetting.getLength(); i++) {
            Element ele = (Element) listSetting.item(i);
            ele.getParentNode().removeChild(ele);
        }
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP, ex);
        }
    }

    /**
     * Returns whether Remote access support is enabled/disabled.
     *
     * @return true if Remote access is enabled else false.
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public Boolean getRemoteAccessAllRoles()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        Boolean isRemoteAccess = false;
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expr = WindowsAzureConstants_v16.PROJ_REMOTE_ACCESS + "/@value";
            Document doc = getWindowsAzureProjMgr().packageFileDoc;
            String remAccStatus = xPath.evaluate(expr, doc);
            if (remAccStatus.equalsIgnoreCase("true")) {
                isRemoteAccess = true;
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_GET_ALLUSER, e);
        }
        return isRemoteAccess;
    }

    /**
     * Returns the user name for Remote access configuration.
     *
     * @return remoteAccessUsername username for Remote access
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public String getRemoteAccessUsername()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWindowsAzureProjMgr().getConfigFileDoc();
            String attrName = WindowsAzureConstants_v16.REMOTEACCESS_USERNAME;
            String expr = String.format("%s%s%s%s",
                    WindowsAzureConstants_v16.CONFIG_SETTING, "[@name='",
                    attrName, "']");
            NodeList listUsername = (NodeList) xPath.evaluate(expr, doc,
                    XPathConstants.NODESET);
            List<WindowsAzureRole_v16> listRoles = getRoles();
            List<String> listValue = new ArrayList<String>();
            for (Iterator<WindowsAzureRole_v16> iterator = listRoles.iterator();
            iterator.hasNext();) {
                WindowsAzureRole_v16 windowsAzureRole = iterator.next();
                listValue.add(windowsAzureRole.getAccUsername());
            }
            int numAttr = listUsername.getLength();
            String remAccUsername = checkValidAttribute(numAttr,
                    attrName, listValue);
            return remAccUsername;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_GET_UNAME, e);
        }
    }

    /**
     * Checks if the specified attribute of ConfigurationSettings is same for
     * each role.
     *
     * @param numValues
     * @param attrName
     * @param listValue
     * @return
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    private String checkValidAttribute(int numValues, String attrName,
            List<String> listValue) throws WindowsAzureInvalidProjectOperationException_v16 {
        String attrVal = "";
        try {
            if (numValues != 0) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                Document doc = getWindowsAzureProjMgr().getConfigFileDoc();
                int noOfRoles = ((NodeList) xPath.evaluate(
                        WindowsAzureConstants_v16.ROLE,
                        doc, XPathConstants.NODESET)).getLength();
                // Check if the attribute is present for each role
                if (noOfRoles == numValues) {
                    String expr = String.format("%s%s%s%s",
                            WindowsAzureConstants_v16.CONFIG_SETTING, "[@name='",
                            attrName, "']/@value");
                    attrVal = xPath.evaluate(expr, doc);
                    // Check if the attribute is having same value for each role
                    for (int i = 0; i < listValue.size(); i++) {
                        String value = listValue.get(i);
                        if (!attrVal.equals(value)) {
                            throw new
                            WindowsAzureInvalidProjectOperationException_v16();
                        }
                    }
                } else {
                    throw new WindowsAzureInvalidProjectOperationException_v16();
                }
            }
            return attrVal;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    "Exception", ex);
        }
    }
    /**
     * Sets the user name for Remote access configuration.
     *
     * @param name
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setRemoteAccessUsername(String name)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(WindowsAzureConstants_v16
                    .INVALID_ARG);
        }
        try {
            List<WindowsAzureRole_v16> listRoles = getRoles();
            for (Iterator<WindowsAzureRole_v16> iterator = listRoles.iterator();
            iterator.hasNext();) {
                WindowsAzureRole_v16 windowsAzureRole = iterator.next();
                windowsAzureRole.setAccUsername(name);
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_USER_NAME, e);
        }
    }

    /**
     * Returns the encrypted password for Remote access configuration.
     *
     * @return remoteAccessEncrypPwd
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public String getRemoteAccessEncryptedPassword()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWindowsAzureProjMgr().getConfigFileDoc();
            String attrName = WindowsAzureConstants_v16.REMOTEACCESS_PASSWORD;
            String expr = String.format("%s%s%s%s",
                    WindowsAzureConstants_v16.CONFIG_SETTING, "[@name='",
                    attrName, "']");
            NodeList listEncrypPwd = (NodeList) xPath.evaluate(expr, doc,
                    XPathConstants.NODESET);
            int numAttr = listEncrypPwd.getLength();
            List<WindowsAzureRole_v16> listRoles = getRoles();
            List<String> listValue = new ArrayList<String>();
            for (Iterator<WindowsAzureRole_v16> iterator = listRoles.iterator();
            iterator.hasNext();) {
                WindowsAzureRole_v16 windowsAzureRole = iterator.next();
                listValue.add(windowsAzureRole.getAccPassword());
            }
            String remAccEncrypPwd = checkValidAttribute(numAttr,
                    attrName, listValue);
            return remAccEncrypPwd;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_GET_ENC_PWD, e);
        }
    }

    /**
     * Sets the encrypted password for Remote access configuration.
     *
     * @param password
     *            the password to be set
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setRemoteAccessEncryptedPassword(String password)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (password == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            List<WindowsAzureRole_v16> listRoles = getRoles();
            for (Iterator<WindowsAzureRole_v16> iterator = listRoles.iterator();
            iterator.hasNext();) {
                WindowsAzureRole_v16 windowsAzureRole = iterator.next();
                windowsAzureRole.setAccPassword(password);
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_ENC_PWD, e);
        }
    }

    /**
     * Returns the expiration date for remote access account.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public Date getRemoteAccessAccountExpiration()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWindowsAzureProjMgr().getConfigFileDoc();
            String attrName = WindowsAzureConstants_v16.REMOTEACCESS_EXPIRY;
            String expr = String.format("%s%s%s%s",
                    WindowsAzureConstants_v16.CONFIG_SETTING, "[@name='",
                    attrName, "']");
            NodeList listAccExpiration = (NodeList) xPath.evaluate(expr, doc,
                    XPathConstants.NODESET);
            int numAttr = listAccExpiration.getLength();
            List<WindowsAzureRole_v16> listRoles = getRoles();
            List<String> listValue = new ArrayList<String>();
            for (Iterator<WindowsAzureRole_v16> iterator = listRoles.iterator();
            iterator.hasNext();) {
                WindowsAzureRole_v16 windowsAzureRole = iterator.next();
                listValue.add(windowsAzureRole.getAccExpiryDate());
            }
            String remeAccessAccExp = checkValidAttribute(numAttr,
                    attrName, listValue);
            Date date = null;
            if (!remeAccessAccExp.isEmpty()) {
                DateFormat formatter = new SimpleDateFormat(
                        WindowsAzureConstants_v16.DATE_FORMAT,
                        Locale.getDefault());
                date = formatter.parse(remeAccessAccExp);
            }
            return date;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_GET_EXPIRY, e);
        }
    }

    /**
     * Sets the expiration date for remote access account.
     *
     * @param date
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setRemoteAccessAccountExpiration(Date date)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (date == null) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            DateFormat formatter = new SimpleDateFormat(
                    WindowsAzureConstants_v16.DATE_FORMAT, Locale.getDefault());
            String dateStr = formatter.format(date);
            List<WindowsAzureRole_v16> listRoles = getRoles();
            for (Iterator<WindowsAzureRole_v16> iterator = listRoles.iterator();
            iterator.hasNext();) {
                WindowsAzureRole_v16 windowsAzureRole = iterator.next();
                windowsAzureRole.setAccExpiryDate(dateStr);
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_EXPIRY, e);
        }
    }

    /**
     * Returns the location of the certificate file for Remote access password
     * signing and thumbprint generation.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public String getRemoteAccessCertificatePath()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expr = WindowsAzureConstants_v16.PROJ_REMOTE_DESKTOP + "/@value";
            Document doc = getWindowsAzureProjMgr().packageFileDoc;
            return xPath.evaluate(expr, doc);

        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_CERT, e);
        }
    }

    /**
     * Sets the location of the certificate file for Remote access password
     * signing and thumbprint generation.
     *
     * @param path
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setRemoteAccessCertificatePath(String path)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            Document doc = getWindowsAzureProjMgr().getPackageFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expr = WindowsAzureConstants_v16.PROJ_PROPERTY;
            Element eleProjProperty = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);

            xPath = XPathFactory.newInstance().newXPath();
            expr = WindowsAzureConstants_v16.PROJ_REMOTE_DESKTOP;
            Element eleCerPath = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (eleCerPath == null) {
                eleCerPath = doc.createElement("property");
                eleCerPath.setAttribute("name",
                "cert.windowsazureremotedesktop");
                eleCerPath.setAttribute("value", path);
                eleProjProperty.appendChild(eleCerPath);
            } else {
                eleCerPath.setAttribute("value", path);
            }
            //Delete the comment corresponding to default .cer
            xPath = XPathFactory.newInstance().newXPath();
            expr = String.format("%s%s%s%s%s",
                    WindowsAzureConstants_v16.PROJ_PROPERTY, "/comment()",
                    "[contains(.,'", WindowsAzureConstants_v16.THUMBPRINT_NOTE,
            "')]");
            Node node = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
            if (node != null) {
                node.getParentNode().removeChild(node);
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_SET_CERT, e);
        }
    }

    /**
     * Returns the fingerprint of the certificate.
     *
     * @return
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public String getRemoteAccessCertificateFingerprint()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document doc = getWindowsAzureProjMgr().getConfigFileDoc();
            String attrName = WindowsAzureConstants_v16.REMOTEACCESS_FINGERPRINT;
            String expr = String.format("%s%s%s%s",
                    WindowsAzureConstants_v16.CERTIFICATE, "[@name='",
                    attrName, "']");
            NodeList listFingerprint = (NodeList) xPath.evaluate(expr, doc,
                    XPathConstants.NODESET);
            int numAttr = listFingerprint.getLength();
            List<WindowsAzureRole_v16> listRoles = getRoles();
            List<String> listValue = new ArrayList<String>();
            for (Iterator<WindowsAzureRole_v16> iterator = listRoles.iterator();
            iterator.hasNext();) {
                WindowsAzureRole_v16 windowsAzureRole = iterator.next();
                listValue.add(windowsAzureRole.getThumbprint());
            }
            String remAccFingerprint = checkValidCerAttribute(numAttr,
                    attrName, listValue);
            return remAccFingerprint;
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_GET_FPRINT, e);
        }
    }

    /**
     * Check if the Certificate element is present for each role and is having
     * same value for each role.
     *
     * @param numAttr
     * @param attrName
     * @param listValue
     * @return
     * @throws Exception
     */
    private String checkValidCerAttribute(int numAttr, String attrName,
            List<String> listValue)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
        String attrVal = "";
        if (numAttr != 0) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Document doc = getWindowsAzureProjMgr().getConfigFileDoc();
        int noOfRoles = ((NodeList) xPath.evaluate(WindowsAzureConstants_v16.ROLE,
                doc, XPathConstants.NODESET)).getLength();
        // Check if the attribute is present for each role
        if (noOfRoles == numAttr) {
            String expr = String.format("%s%s%s%s",
                    WindowsAzureConstants_v16.CERTIFICATE, "[@name='",
                    attrName, "']/@thumbprint");
            attrVal = xPath.evaluate(expr, doc);
            // Check if the attribute is having same value for each role
            for (int i = 0; i < listValue.size(); i++) {
                String value = listValue.get(i);
                if (!attrVal.equals(value)) {
                    throw new WindowsAzureInvalidProjectOperationException_v16();
                }
            }
        } else {
            throw new WindowsAzureInvalidProjectOperationException_v16();
        }
        }
        return attrVal;
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    "Exception", ex);
        }
    }

    /**
     * Sets the fingerprint of the certificate.
     *
     * @param fingerprint
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setRemoteAccessCertificateFingerprint(String fingerprint)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (fingerprint == null || fingerprint.isEmpty()) {
            throw new IllegalArgumentException(WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            List<WindowsAzureRole_v16> listRoles = getRoles();
            for (Iterator<WindowsAzureRole_v16> iterator = listRoles.iterator();
            iterator.hasNext();) {
                WindowsAzureRole_v16 windowsAzureRole = iterator.next();
                windowsAzureRole.setThumbprint(fingerprint);
            }
            // Delete the comment corresponding to default .cer
            Document doc = getWindowsAzureProjMgr().getConfigFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expr = String.format("%s%s%s%s", WindowsAzureConstants_v16.ROLE,
                    "/Certificates/comment()[contains(.,'",
                    WindowsAzureConstants_v16.THUMBPRINT_NOTE, "')]");
            Node node = (Node) xPath.evaluate(expr, doc, XPathConstants.NODE);
            if (node != null) {
                node.getParentNode().removeChild(node);
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_RA_SET_FPRINT, e);
        }
    }

    /**
     * Upgrade the projects created with older version of plugin.
     *
     */
    public void upgradeProject()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            int version = 0;
            String currVersion = WindowsAzureConstants_v16.VERSION;
            Document doc = getWindowsAzureProjMgr().getPackageFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expr = WindowsAzureConstants_v16.CREATOR_VER + "/@value";
            String strVersion = xPath.evaluate(expr, doc);
            if (!strVersion.isEmpty()) {
                strVersion = strVersion.replace(".", "");
                Integer integer = Integer.valueOf(strVersion);
                version = integer.intValue();
            }
            strVersion = currVersion.replace(".", "");
            Integer integer = Integer.valueOf(strVersion);
            int intCurrVersion = integer.intValue();
            if (version == 0 || version < intCurrVersion) {

                replaceFiles();
                updateVersionNo(currVersion);
                iterateOverRoles();
                removeEntryPointDLL();
                updateSDKVersion();
                copySampleFiles();

            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_UPDATE_PROJ, e);
        }
    }

    private void replaceFiles() throws WindowsAzureInvalidProjectOperationException_v16 {
        /**Replace .cspack.jar, ResetEmulator.cmd, RunInEmulator.cmd,
        WindowsAzurePortal.url, .elevate.vbs */

        try {
            replaceCSPack(projDirPath);

            String emulatorPath = String.format("%s%s%s%s%s", projDirPath,
                    File.separator, ".templates", File.separator,
                    "emulatorTools");
            File emulatorTools = new File(emulatorPath);
            if (!emulatorTools.exists()) {
                emulatorTools.mkdirs();
            }
            //replaceEmulatorFile(emulatorPath, "DisplayEmulatorUI.cmd");
            replaceEmulatorFile(emulatorPath, "ResetEmulator.cmd");
            replaceEmulatorFile(emulatorPath, "RunInEmulator.cmd");
            replaceEmulatorFile(emulatorPath, ".elevate.vbs");

            replacePortalURL(projDirPath);

        } catch (IOException e) {
           throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_REPLACE_FILE, e);
        }
    }

    /**
     * Updates the sdk version property in package.xml to
     * the latest version.
     *
     * @throws WindowsAzureInvalidProjectOperationException_v16
     * @throws XPathExpressionException
     */
    private void updateSDKVersion()
    throws WindowsAzureInvalidProjectOperationException_v16, XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Document doc = getPackageFileDoc();
        String expr = WindowsAzureConstants_v16.WASDKVER_PROP;
        Element sdkVer = (Element) xPath.evaluate(expr, doc, XPathConstants.NODE);
        if (sdkVer != null) {
            sdkVer.setAttribute("value", WindowsAzureConstants_v16.SDK_VERSION);
        }
    }

    /**
     * Replaces the old cspack.jar with new one.
     *
     * @param path : project location
     * @throws IOException
     */
    private void replaceCSPack(String path) throws IOException {
        String jarPath = String.format("%s%s%s", path, File.separator,
            ".cspack.jar");
        copyResourceFile("/upgrade/.cspack.jar", jarPath);
    }

    /**
     * Replaces the old WindowsAzurePortal.url with new one.
     *
     * @param path : project location
     * @throws IOException
     */
    private void replacePortalURL(String path) throws IOException {
        String devPPath = String.format("%s%s%s%s%s", path,
            File.separator, ".templates", File.separator, "devPortal");
        File devPortal = new File(devPPath);
        if (!devPortal.exists()) {
            devPortal.mkdirs();
        }
        String urlPath = String.format("%s%s%s", devPPath, File.separator,
            "WindowsAzurePortal.url");
        copyResourceFile("/upgrade/WindowsAzurePortal.url", urlPath);
    }

    /**
     * Replaces the specified emulator file.
     *
     * @param emulatorPath : emulatorTools folder location.
     * @param file : name of the emulator tool.
     * @throws IOException
     */
    private void replaceEmulatorFile(String emulatorPath, String file)
    throws IOException {
        copyResourceFile(String.format("/upgrade/%s", file),
                String.format("%s%s%s", emulatorPath, File.separator, file));
    }

 /**
     * This method copy all sample file in project directory.
     * @throws IOException .
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    private void copySampleFiles()
    throws IOException, WindowsAzureInvalidProjectOperationException_v16 {
        String samplePath = String.format("%s%s%s%s", projDirPath,
                File.separator, "samples", File.separator);
        File file = new File(samplePath);
        if (!file.exists()) {
            boolean success = new File(samplePath).mkdir();
            if (!success) {
                throw  new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.EXCP_SAMPLE_CREAT);
            }
        }
        copyResourceFile(WindowsAzureConstants_v16.ST_CUSTOM_PATH, String.format(
                "%s%s", samplePath, WindowsAzureConstants_v16.ST_CUSTOM));

        //Copy sample files and mark them read only
        String stTomcat7 = String.format("%s%s", samplePath,
                WindowsAzureConstants_v16.ST_TOMCAT);
        copyResourceFile(WindowsAzureConstants_v16.ST_TOMCAT_PATH, stTomcat7);
        new File(stTomcat7).setReadOnly();

        String stFish = String.format("%s%s", samplePath,
                WindowsAzureConstants_v16.ST_GLASSFISH);
        copyResourceFile(WindowsAzureConstants_v16.ST_GLASSFISH_PATH, stFish);
        new File(stFish).setReadOnly();

        String stJBoss6 = String.format("%s%s", samplePath,
                WindowsAzureConstants_v16.ST_JBOSS6);
        copyResourceFile(WindowsAzureConstants_v16.ST_JBOSS6_PATH, stJBoss6);
        new File(stJBoss6).setReadOnly();

        String stJboss7 = String.format("%s%s", samplePath,
                WindowsAzureConstants_v16.ST_JBOSS7);
        copyResourceFile(WindowsAzureConstants_v16.ST_JBOSS7_PATH, stJboss7);
        new File(stJboss7).setReadOnly();

        String stJetty = String.format("%s%s", samplePath,
                WindowsAzureConstants_v16.ST_JETTY);
        copyResourceFile(WindowsAzureConstants_v16.ST_JETTY_PATH, stJetty);
        new File(stJetty).setReadOnly();
    }
    /**
     * Updates the version no. in package.xml to latest version.
     *
     * @param currVersion : latest version.
     * @throws WindowsAzureInvalidProjectOperationException_v16
     * @throws XPathExpressionException
     */
    private void updateVersionNo(String currVersion)
    throws WindowsAzureInvalidProjectOperationException_v16, XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Document doc = getWindowsAzureProjMgr().getPackageFileDoc();
        String expr = WindowsAzureConstants_v16.CREATOR_VER;
        Element element = (Element) xPath.evaluate(expr, doc,
                XPathConstants.NODE);
        if (element == null) {
            element = doc.createElement("property");
            element.setAttribute("name", "creator.version");
            element.setAttribute("value", currVersion);
            expr = WindowsAzureConstants_v16.PROJ_PROPERTY;
            Element target = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);
            if (target == null) {
                target = doc.createElement("target");
                target.setAttribute("description",
                        WindowsAzureConstants_v16.PROJ_PROPERTY_DESC);
                target.setAttribute("name", "waprojectproperties");

                expr = "/project";
                Element root = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                expr = WindowsAzureConstants_v16.CREATE_PKG_TARGET;
                Element eleTarget = (Element) xPath.evaluate(expr, doc,
                        XPathConstants.NODE);
                root.insertBefore(target, eleTarget);
            }
            target.appendChild(element);
        } else {
            element.setAttribute("value", currVersion);
        }
    }

    /**
     * Iterates over each role folder and
     *  a) removes log.cmd
     *  b) adds heartbeat.vbs
     * @throws WindowsAzureInvalidProjectOperationException_v16
     * @throws IOException
     */
    private void iterateOverRoles()
    throws WindowsAzureInvalidProjectOperationException_v16, IOException {
        List<WindowsAzureRole_v16> roles = getWindowsAzureProjMgr().getRoles();
        for (WindowsAzureRole_v16 winAzureRole : roles) {
            String rolePath = String.format("%s%s%s", projDirPath,
                File.separator, winAzureRole.getName());
            String utilPath = String.format("%s%s%s%s%s", rolePath,
                File.separator, "approot", File.separator, "util");

            // Delete log file
            String logPath = String.format("%s%s%s", utilPath, File.separator,
                "log.cmd");
            File logFile = new File(logPath);
            if (logFile.exists()) {
                logFile.delete();
            }
            //Delete HelloWorld.zip
            String helloWorldZipPath = String.format("%s%s%s%s%s", rolePath,
                    File.separator, "approot", File.separator, "HelloWorld.zip");
            File zipFile = new File(helloWorldZipPath);
            if (zipFile.exists()) {
                zipFile.delete();
            }

            //Add HelloWorld.war
            String helloWorldWarpath = String.format("%s%s%s%s%s", rolePath,
                    File.separator,"approot", File.separator, "HelloWorld.war");
            copyResourceFile("/Role/HelloWorld.war", helloWorldWarpath);

            //Add run.cmd
            String runCmdpath = String.format("%s%s%s%s%s", rolePath,
                    File.separator, "approot", File.separator, "run.cmd");
            copyResourceFile("/Role/run.cmd", runCmdpath);

            //Add .start.cmd
            String startCmdPath = String.format("%s%s%s", utilPath,
                    File.separator, ".start.cmd");
            copyResourceFile("/Role/.start.cmd", startCmdPath);
            //Add whileproc.cmd
            String whileprocCmdPath = String.format("%s%s%s", utilPath,
                    File.separator, "whileproc.cmd");
            copyResourceFile("/Role/whileproc.cmd", whileprocCmdPath);
        }
    }

    /**
     * Remove ".WorkerRoleEntryPoint.dll" if present
     * and the "entrypointdllpath" attribute from workerrole node in package.xml
     *
     * @throws XPathExpressionException
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    private void removeEntryPointDLL()
    throws XPathExpressionException, WindowsAzureInvalidProjectOperationException_v16 {
        String entryDLLPath = String.format("%s%s%s", projDirPath,
            File.separator, ".WorkerRoleEntryPoint.dll");
        File entryDLLFile = new File(entryDLLPath);
        if (entryDLLFile.exists()) {
            entryDLLFile.delete();
        }

        XPath xPath = XPathFactory.newInstance().newXPath();
        Document doc = getPackageFileDoc();
        String expr = String.format("%s/workerrole",
            WindowsAzureConstants_v16.WINAZURE_PACKAGE);
        NodeList listEntryDLL = (NodeList) xPath.evaluate(expr,
                doc, XPathConstants.NODESET);
        for (int i = 0; i < listEntryDLL.getLength(); i++) {
            Element role = (Element) listEntryDLL.item(i);
            role.removeAttribute("entrypointdllpath");
        }
    }

    /**
     * Copy resource file to a specified location.
     *
     * @param input : the resource file.
     * @param output : the path where the file is to be copied.
     * @throws IOException
     */
    private void copyResourceFile(String input, String output)
    throws IOException {
        InputStream urlStream = this.getClass().getResourceAsStream(input);
        OutputStream out = new FileOutputStream(new File(output));
        ParserXMLUtility_v16.writeFile(urlStream, out);
    }

    //Copy SA related files to project directory
    protected void copySAResources(String roleName) throws IOException,WindowsAzureInvalidProjectOperationException_v16 {
        String destPath = String.format("%s%s%s%s%s%s%s%s", projDirPath,File.separator,roleName,File.separator,
                          WindowsAzureConstants_v16.APPROOT_NAME,File.separator,WindowsAzureConstants_v16.SA_FOLDER_NAME,File.separator);

        File f = new File(destPath);
        if(!f.exists()) {
            boolean result = f.mkdirs();

            if (!result) {
                throw new WindowsAzureInvalidProjectOperationException_v16(WindowsAzureConstants_v16.DIR_NOT_CREATED+destPath);
            }
        }

        copyResourceFile("/sessionaffinity/ConfigureARR.cmd", destPath + "ConfigureARR.cmd");
        copyResourceFile("/sessionaffinity/SessionAffinityAgent.exe", destPath + "SessionAffinityAgent.exe");

        /* Uncomment this code if we need to distribute webpicmd instead of downloading
        copyResourceFile("/sessionaffinity/Microsoft.Web.PlatformInstaller.UI.dll", destPath + "Microsoft.Web.PlatformInstaller.UI.dll");
        copyResourceFile("/sessionaffinity/WebpiCmdLine.exe", destPath + "WebpiCmdLine.exe"); */
    }


    /**
     * Gets document for ServiceConfiguration.cscfg.
     *
     * @return document object
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    protected Document getConfigFileDoc()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (getWindowsAzureProjMgr().configFileDoc == null) {
            getWindowsAzureProjMgr().configFileDoc = ParserXMLUtility_v16
            .parseXMLFile(getWindowsAzureProjMgr().configFilePath);
        }
        return getWindowsAzureProjMgr().configFileDoc;
    }

    /**
     * Gets document for package.xml.
     *
     * @return document object
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    protected Document getPackageFileDoc()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (null == getWindowsAzureProjMgr().packageFileDoc) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_GET_PACKAGE_FILE);
        }
        return getWindowsAzureProjMgr().packageFileDoc;
    }

    /**
     * Gets document for ServiceDefinition.csdef.
     *
     * @return document object
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */

    protected Document getdefinitionFileDoc()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if (getWindowsAzureProjMgr().definitionFileDoc == null) {
            getWindowsAzureProjMgr().definitionFileDoc = ParserXMLUtility_v16
            .parseXMLFile(getWindowsAzureProjMgr().defFilePath);
            if (definitionFileDoc == null) {
                throw new WindowsAzureInvalidProjectOperationException_v16(
                        WindowsAzureConstants_v16.EXCP_RETRIEVE_DATA
                        + "ServiceDefinition.csdef");
            }
        }
        return getWindowsAzureProjMgr().definitionFileDoc;
    }

    protected WindowsAzureProjectManager_v16 getWindowsAzureProjMgr() {
        return winAzureProjMgr;
    }

    protected void setWindowsAzureProjMgr(
            WindowsAzureProjectManager_v16 waProjMgr) {
        this.winAzureProjMgr = waProjMgr;
    }

    /**
     * Sets document for ServiceDefinition.csdef.
     *
     * @param projectName
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void setProjectName(String projectName)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((projectName == null) || (projectName.isEmpty())) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            Node root = getPackageFileDoc().getDocumentElement();
            NamedNodeMap nMap = root.getAttributes();
            if (nMap != null) {
                nMap.getNamedItem("name").setNodeValue(projectName);
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_SET_PROJECT_NAME, e);
        }
    }

    /**
     * Sets document for ServiceDefinition.csdef.
     *
     * @param projectName
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public String getProjectName()
    throws WindowsAzureInvalidProjectOperationException_v16 {
        String projName = "";
        try {
            Node root = getPackageFileDoc().getDocumentElement();
            NamedNodeMap nMap = root.getAttributes();
            if (nMap != null) {
                projName = nMap.getNamedItem("name").getNodeValue();
            }
        } catch (Exception e) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_GET_PROJECT_NAME, e);
        }
        return projName;
    }

    /**
     * get Sticky session is enabled or not for any worker of this project
     * @return true if any of the role has enabled session affinity else false
     */
    public boolean getSessionAffinityStatus() {
        boolean status = false;
        try {
            List<WindowsAzureRole_v16> roles;
            roles = getRoles();
            for (WindowsAzureRole_v16 winAzureRole : roles) {
                WindowsAzureEndpoint_v16 wEp =
                        winAzureRole.getSessionAffinityInputEndpoint();
                if (wEp != null) {
                    status = true;
                    break;
                }
            }
        } catch (WindowsAzureInvalidProjectOperationException_v16 e) {
            status = false;
        }
        return status;
    }

    /**
     * This API is for disabling session affinity for all roles
     * @throws WindowsAzureInvalidProjectOperationException_v16
     */
    public void disableSessionAffinity()
            throws WindowsAzureInvalidProjectOperationException_v16 {
        try {
            List<WindowsAzureRole_v16> roles;
            roles = getRoles();
            for (WindowsAzureRole_v16 winAzureRole : roles) {
                winAzureRole.setSessionAffinityInputEndpoint(null);
            }
        } catch (WindowsAzureInvalidProjectOperationException_v16 ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_DISABLE_ALL_SA,  ex);
        }
    }
    /**
     * move project from temp location to given location.
     *
     * @param projName .
     * @param projLocation .
     * @throws Exception .
     */
    public static void moveProjFromTemp(String projName, String projLocation)
    throws WindowsAzureInvalidProjectOperationException_v16 {
        if ((projName == null) || projName.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        if ((projLocation == null) || projLocation.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        try {
            String tmpPath = System.getProperty("java.io.tmpdir");
            String source = String.format("%s%s%s", tmpPath, File.separator,
            "%proj%");
            String dest = String.format("%s%s%s", projLocation, File.separator,
                    projName);
            ParserXMLUtility_v16.copyDir(new File(source), new File(dest));
            String projXML = String.format("%s%s%s%s%s", projLocation,
                    File.separator, projName, File.separator, "package.xml");
            Document projDoc = ParserXMLUtility_v16.parseXMLFile(projXML);

            Node rootProj = projDoc.getDocumentElement();
            NamedNodeMap nMap = rootProj.getAttributes();
            if (nMap != null) {
                nMap.getNamedItem("name").setNodeValue(projName);
            }
            ParserXMLUtility_v16.saveXMLFile(projXML, projDoc);

            String samplePath = String.format("%s%s%s%s", dest,
                    File.separator, "samples", File.separator);
            File file = new File(samplePath);
            if (!file.exists()) {
                boolean success = new File(samplePath).mkdir();
                if (!success) {
                    throw  new WindowsAzureInvalidProjectOperationException_v16(
                            WindowsAzureConstants_v16.EXCP_SAMPLE_CREAT);
                }
            }

            //Mark Sample files read only
            String stTomcat7 = String.format("%s%s", samplePath,
                    WindowsAzureConstants_v16.ST_TOMCAT);
            new File(stTomcat7).setReadOnly();

            String stFish = String.format("%s%s", samplePath,
                    WindowsAzureConstants_v16.ST_GLASSFISH);
            new File(stFish).setReadOnly();

            String stJBoss6 = String.format("%s%s", samplePath,
                    WindowsAzureConstants_v16.ST_JBOSS6);
            new File(stJBoss6).setReadOnly();

            String stJboss7 = String.format("%s%s", samplePath,
                    WindowsAzureConstants_v16.ST_JBOSS7);
            new File(stJboss7).setReadOnly();

            String stJetty = String.format("%s%s", samplePath,
                    WindowsAzureConstants_v16.ST_JETTY);
            new File(stJetty).setReadOnly();


        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException_v16(
                    WindowsAzureConstants_v16.EXCP_MOVE_PROJ_FROM_TEMP, ex);
        }
    }

/**
 *
 * @param vmSize
 * @return maxLsSize local storage max size value
 */
    public static int getMaxLocalStorageSize(String vmSize) {
        if ((vmSize == null) ||  (!waVmSize.contains(vmSize.toLowerCase()))) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants_v16.INVALID_ARG);
        }
        int maxLsSize = 0;
        switch (WAvmSize.valueOf(vmSize.toUpperCase())) {
        case EXTRASMALL   :
            maxLsSize = WindowsAzureConstants_v16.MAX_LS_SIZE_EXTRASMALL;
            break;
        case SMALL   :
            maxLsSize = WindowsAzureConstants_v16.MAX_LS_SIZE_SMALL;
            break;
        case MEDIUM   :
            maxLsSize = WindowsAzureConstants_v16.MAX_LS_SIZE_MEDIUM;
            break;
        case LARGE   :
            maxLsSize = WindowsAzureConstants_v16.MAX_LS_SIZE_LARGE;
            break;
        default:
            maxLsSize = WindowsAzureConstants_v16.MAX_LS_SIZE_EXTRALARGE;
        }
        return maxLsSize;
    }


    private static void dataTransfer(InputStream inputStream,
            BufferedOutputStream buffOutputStream) throws IOException {
        byte[] buffer = new byte[BUFF_SIZE];
        int transferLen = inputStream.read(buffer);

        while (transferLen >= 0) {
            buffOutputStream.write(buffer, 0, transferLen);
            transferLen = inputStream.read(buffer);
        }
        inputStream.close();
        buffOutputStream.close();
    }

    protected static boolean deleteDir(File dir) {
        boolean status = true;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    status = false;
                }
            }
        }
        // The directory is now empty so delete it
        if (status) {
           status = dir.delete();
        }
        return status;
    }
}
