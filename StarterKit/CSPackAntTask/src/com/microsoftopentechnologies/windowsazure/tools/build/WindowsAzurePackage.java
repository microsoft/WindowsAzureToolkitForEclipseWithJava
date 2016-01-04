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

package com.microsoftopentechnologies.windowsazure.tools.build;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.*;

import com.microsoftopentechnologies.azuremanagementutil.model.StorageService;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureRestUtils;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureServiceManagement;
import com.microsoftopentechnologies.windowsazure.tools.cspack.BinaryPackageCreator;
import com.microsoftopentechnologies.windowsazure.tools.cspack.Configuration;
import com.microsoftopentechnologies.windowsazure.tools.cspack.PackageCreator;
import com.microsoftopentechnologies.windowsazure.tools.build.Utils;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.serviceconfiguration.ConfigurationSettings.Setting;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.serviceconfiguration.Role;
import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.serviceconfiguration.ServiceConfiguration;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * 
 * A class representing windowsazurepackage Ant task.
 * 
 */
public class WindowsAzurePackage extends Task {
	// Constants
	private static final String DEFAULT_DEFINITION_FILE_NAME = "ServiceDefinition.csdef";
	private static final String DEFAULT_CONFIGURATION_FILE_NAME = "ServiceConfiguration.cscfg";
	private static final String DEFAULT_PACKAGE_SUBDIR = "deploy";
	private static final String DEFAULT_EMULATOR_TOOLS_SUBDIR = "emulatorTools";
	private static final String DEFAULT_CLOUD_TOOLS_SUBDIR = "cloudTools";
	private static final String BUILD_ERROR_FILENAME = "BuildFailure.txt";
	public static final String DEFAULT_UTIL_SUBDIR = "util"; // relative to approot
	public static final String UTIL_UNZIP_FILENAME = "unzip.vbs";
	public static final String UTIL_DOWNLOAD_FILENAME = "download.vbs";
	public static final String UTIL_WASH_FILENAME = "wash.cmd";
	public static final String UTIL_WASH_PATH = DEFAULT_UTIL_SUBDIR + "\\" + UTIL_WASH_FILENAME;
    private static final String SDK_PROPERTIES = "sdk.properties";

	public static final String INTERNAL_STARTUP_FILE_NAME = ".startup.cmd";
	private static final String INTERNAL_STARTUP_SUBDIR = "startup";
	public static final String USER_STARTUP_FILE_NAME = "startup.cmd";
	private static final String DEV_PORTAL_SUBDIR = "devPortal";
	private static final String DEV_PORTAL_FILE = "WindowsAzurePortal.url";
	private static final String ENV_PROGRAMFILES_WOW64 = "ProgramW6432";
	private static final String ENV_X86_PROGRAMFILES_WOW64 = "ProgramFiles(x86)";
	private static final String ENV_PROGRAMFILES = "ProgramFiles";
	public static final String STORAGEDLL_SUBDIR = "..\\ref"; // relative to sdk\bin dir
	// storage dll 4.3.0 and its dependencies
	public static final String STORAGEDLL_FILENAME = "Microsoft.WindowsAzure.Storage.dll";
	public static final String DATA_SERV_FILENAME = "Microsoft.Data.Services.Client.dll";
	public static final String DATA_EDM_FILENAME = "Microsoft.Data.Edm.dll";
	public static final String DATA_ODATA_FILENAME = "Microsoft.Data.OData.dll";
	public static final String JSON_FILENAME = "Newtonsoft.Json.dll";
	public static final String SPATIAL_FILENAME = "System.Spatial.dll";
	public static final String CONFIGURATION_FILENAME = "Microsoft.WindowsAzure.Configuration.dll";

	private static final String TEMPLATES_SUBDIR = ".templates";
	private static final String TEMPLATE_TOKEN_SDKDIR = "${SDKDir}";
	private static final String TEMPLATE_TOKEN_EMULATORDIR = "${EmulatorDir}";
	private static final String TEMPLATE_TOKEN_STORAGE_EMULATORDIR = "${StorageEmulatorDir}";
	private static final String TEMPLATE_TOKEN_PROJECTDIR = "${ProjectDir}";
	private static final String TEMPLATE_TOKEN_PACKAGEDIR = "${PackageDir}";
	private static final String TEMPLATE_TOKEN_PACKAGEFILENAME = "${PackageFileName}";
	private static final String TEMPLATE_TOKEN_DEFINITIONFILENAME = "${DefinitionFileName}";
	private static final String TEMPLATE_TOKEN_CONFIGURATIONFILENAME = "${ConfigurationFileName}";
	public static final String TEMPLATE_TOKEN_COMPONENTS_SCRIPT = "${Components}";
	public static final String TEMPLATE_TOKEN_VARIABLES_SCRIPT = "${Variables}";
	public static final String TEMPLATE_TOKEN_USER_STARTUP_SCRIPT = "${UserStartup}";
	private static final String TEMPLATE_TOKEN_PORTALURL = "${PortalURL}";
	// Roles whose cache configuration is development storage
	private static final String DEV_CACHE_CONFIG = "/ServiceConfiguration/Role[ConfigurationSettings/Setting[" +
								"@name='Microsoft.WindowsAzure.Plugins.Caching.ConfigStoreConnectionString' and @value='UseDevelopmentStorage=true']]/@name";
	private String cacheSettingStr = "/ServiceConfiguration/Role[@name='%s']/ConfigurationSettings/Setting[@name='%s']";
	public static final String SET_CONFIGCONN_VAL_CLOULD = "BlobEndpoint=%s;AccountName=%s;AccountKey=%s";
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;

	public static String newline = System.getProperty("line.separator");

	private Vector<WorkerRole> roles;
	private PackageType packageType;
	private String sdkDir;
    private String sdkKit;
	private String emulatorDir;
	private String storageEmulatorDir;
	private String projectDir;
	private String packageDir;
	private String packageFileName;
	private String definitionFileName;
	private String configurationFileName;
	private String emulatorToolsDir;
	private String cloudToolsDir;
	private String templatesDir;
	private String portalURL;
	private String rolePropertiesFileName = null;
	private UseCTPFormat useCtpPackageFormat = UseCTPFormat.AUTO;
	private boolean verifyDownloads = true;
	private Thread downloadManagerThread = null;
	
	private DownloadManager downloadManager;
	
	// To support auto storage in Ant task
	private String publishSettingsPath;
	private String subscriptionId;
	private String storageAccountName;
	private String region;
	private final String auto = "auto";
	List<AutoUpldCmpnts> mdfdCmpntList = new ArrayList<AutoUpldCmpnts>();
	List<String> roleMdfdCache = new ArrayList<String>();
	String settingName = "Microsoft.WindowsAzure.Plugins.Caching.ConfigStoreConnectionString";
	String valueName = "UseDevelopmentStorage=true";

	/**
	 * WindowsAzurePackage constructor
	 */
	public WindowsAzurePackage() {
		this.roles = new Vector<WorkerRole>();
		this.packageType = PackageType.cloud;
		this.definitionFileName = DEFAULT_DEFINITION_FILE_NAME;
		this.configurationFileName = DEFAULT_CONFIGURATION_FILE_NAME;
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream(SDK_PROPERTIES);
            Properties properties = new Properties();
            properties.load(input);
            for (String propertyName : properties.stringPropertyNames()) {
                String propertyValue = properties.getProperty(propertyName);
                System.setProperty(propertyName, propertyValue);
            }
        } catch (IOException ex) {
            log(ex, Project.MSG_WARN);
        }
	}

	public String getPublishSettingsPath() {
		return publishSettingsPath;
	}

	public void setPublishSettingsPath(String publishSettingsPath) {
		this.publishSettingsPath = publishSettingsPath;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getStorageAccountName() {
		return storageAccountName;
	}

	public void setStorageAccountName(String storageAccountName) {
		this.storageAccountName = storageAccountName;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	/**
	 * Sets the Azure portal URL
	 * @param portalURL
	 */
	public void setPortalURL(String portalURL) {
		this.portalURL = portalURL;
	}
	
	/**
	 * Selects the role properties file to associate with each role in the project, if any
	 * @param filePath
	 */
	public void setRolePropertiesFileName(String filePath) {
		this.rolePropertiesFileName = filePath;
	}
	
	/**
	 * Specifies whether the v1.7 package format shouold be used
	 * @param value
	 */
	public void setUseCtpPackageFormat(String value) {
        useCtpPackageFormat = UseCTPFormat.valueOf(value.toUpperCase());
	}
	
	/**
	 * Sets packagetype attribute
	 * @param packageType
	 */
	public void setPackageType(PackageType packageType) {
		this.packageType = packageType;
	}
	public PackageType getPackageType() {
		return this.packageType;
	}

	/**
	 * Sets/gets sdkdir attribute
	 * @param sdkDir
	 */
	public void setSdkDir(String sdkDir) {
		this.sdkDir = sdkDir;
	}
	public File getSdkDir() {
        if (!IS_WINDOWS) {
            return null;
        }
		if(this.sdkDir != null) {
			return new File(this.sdkDir);
		} else
			try {
				if(null != (this.sdkDir = findLatestAzureSdkDir())) {
					return new File(this.sdkDir);
				} else {
					return null;
				}
			} catch (IOException e) {
				return null;
			}
	}

	/**
	 * Sets emulatordir attribute
	 * @param emulatorDir
	 */
	public void setEmulatorDir(String emulatorDir) {
		this.emulatorDir = emulatorDir;
	}
	
	/**
	 * Sets storage emulatordir attribute
	 * @param emulatorDir
	 */
	public void setStorageEmulatorDir(String storageEmulatorDir) {
		this.storageEmulatorDir = storageEmulatorDir;
	}

	/**
	 * Sets projectdir attribute
	 * @param projectDir
	 */
	public void setProjectDir(String projectDir) {
		this.projectDir = projectDir;
	}
	
	public String getProjectDir() {
		return projectDir;
	}

	/**
	 * Sets packagedir attribute
	 * @param packageDir
	 */
	public void setPackageDir(String packageDir) {
		this.packageDir = packageDir;
	}
	public File getPackageDir() {
		return new File(this.packageDir);
	}

    public String getSdkKit() {
        return sdkKit;
    }

    /**
	 * Sets packagefilename attribute
	 * @param packageFileName
	 */
	public void setPackageFileName(String packageFileName) {
		this.packageFileName = packageFileName;
	}

	/**
	 * Sets definitionfilename attribute
	 * @param definitionFileName
	 */
	public void setDefinitionFileName(String definitionFileName) {
		this.definitionFileName = definitionFileName;
	}

	/**
	 * Sets configurationfilename attribute
	 * @param configurationFileName
	 */
	public void setConfigurationFileName(String configurationFileName) {
		this.configurationFileName = configurationFileName;
	}

	/**
	 * Sets emulatortoolsdir attribute
	 * @param emulatorToolsDir
	 */
	public void setEmulatorToolsDir(String emulatorToolsDir) {
		this.emulatorToolsDir = emulatorToolsDir;
	}
	
	/**
	 * Sets cloudtoolsdir attribute
	 * @param emulatorToolsDir
	 */
	public void setCloudToolsDir(String cloudToolsDir) {
		this.cloudToolsDir = cloudToolsDir;
	}
	
	/**
	 * Sets verifydownloads attribute
	 * @param verifyDownloads
	 */
	public void setVerifyDownloads(boolean verifyDownloads) {
		this.verifyDownloads = verifyDownloads;
	}
	public boolean getVerifyDownloads() {
		return this.verifyDownloads;
	}
	
	private String getThrdPartyJdkCloudValue(WorkerRole role) {
		String cldVal = "";
		Vector<StartupEnv> envList = role.getVariables();
		for (int j = 0; j < envList.size(); j++) {
			StartupEnv env = envList.get(j);
			if (env.getName().equalsIgnoreCase("JAVA_HOME")) {
				cldVal = env.getCloudValue();
				if (cldVal != null && !cldVal.isEmpty() && cldVal.contains("zulu")) {
					return cldVal;
				}
			}
		}
		return cldVal;
	}
	
	private String getThrdPartyServerCloudValue(String roleName)
	throws Exception {
		String packageXmlPath = this.projectDir + File.separator + "package.xml";
		Document doc = XMLUtil.parseXMLFile(new File(packageXmlPath));
		return XMLUtil.getThrdPartyServerCloudValue(doc, roleName);
	}

	/**
	 * API to configure caching settings for storage account if mode is 'auto' 
	 * @param roleName
	 * @param storageName
	 * @param curAcc
	 * @throws Exception
	 */
	private void configureAutoCacheStorageAccount(String roleName,	StorageService curAcc) throws Exception {
		String cscfgPath = projectDir + File.separator + DEFAULT_CONFIGURATION_FILE_NAME;
		Document doc = XMLUtil.parseXMLFile(new File(cscfgPath));
		ServiceConfiguration serviceConfiguration = com.microsoftopentechnologies.windowsazure.tools.cspack.Utils.
					parseXmlFile(ServiceConfiguration.class, cscfgPath);
		
		Role role = serviceConfiguration.getRole(roleName);
		if (role != null && role.getConfigurationSettings() != null && role.getConfigurationSettings().getSetting() != null) {
			// Iterate over config settings
			for (Setting setting : role.getConfigurationSettings().getSetting()) {
				if (setting.getName().equalsIgnoreCase(settingName) && setting.getValue().equalsIgnoreCase(valueName)) {
					// Get storage account and create if needed
					if (curAcc == null) {
						if (!Utils.isValidFilePath(publishSettingsPath)) {
							throw new BuildException("Storage account is specified as 'auto', ensure project contains valid Publish information");
						}
						curAcc = createStorageAccountIfNotExists();
					}
					
					roleMdfdCache.add(roleName);
					String value = String.format(SET_CONFIGCONN_VAL_CLOULD,
							curAcc.getStorageAccountProperties().getEndpoints().get(0).toString(),
							curAcc.getServiceName(), curAcc.getPrimaryKey());
					String exp = String.format(cacheSettingStr, role.getName(), setting.getName());
					HashMap<String, String> nodeAttribites = new HashMap<String, String>();
					nodeAttribites.put("name", settingName);
					nodeAttribites.put("value", value);
					XMLUtil.updateElementAttributeValue(doc, exp, nodeAttribites);
					this.log(roleName + " : Temporarily replaced caching 'auto' settings.");
				}
			}
			XMLUtil.saveXMLDocument(cscfgPath, doc);
		}
	}

	/**
	 * API to configure settings for storage account if mode is 'auto'
	 * @throws Exception
	 */
	private void configureAutoCloudUrl() throws Exception {
		mdfdCmpntList.clear();
		roleMdfdCache.clear();
		
		// Applicable only for package type 'cloud'
		if (this.packageType == PackageType.local) {
			// Previous when cloudsrc=auto, starter kit used to consider it as null but now for selected components 
			// need to replace auto with storage account URL. Resetting back to null for emulator.
			replaceAutoURLWithNull();
			return;
		}
		
		StorageService curAcc = null;
		String curKey = null;
		String accUrl = null;
		
		// Iterate over role list.
		Vector<WorkerRole> roles = this.roles;
		if (roles != null && roles.size() > 0) {
			for (int i = 0; i < roles.size(); i++) {
				WorkerRole role = roles.get(i);
	
				// Iterate over component definitions.
				Vector<Component> cmpnntsList = role.getComponents();
				if (cmpnntsList != null && cmpnntsList.size() > 0) {
					for (int j = 0; j < cmpnntsList.size(); j++) {
						Component component = cmpnntsList.get(j);
						CloudUpload mode = component.getCloudUpload();
						// Just to be on the safe side
						boolean replaceAutoURLWithNull=true;
						
						if (mode != null) {
							String cmpntType = component.getType();
							
							if (cmpntType != null) {
							
								if (((cmpntType.equals("jdk.deploy") || cmpntType.equals("server.deploy")) && mode == CloudUpload.AUTO)
																		|| (cmpntType.equals("server.app") && mode == CloudUpload.ALWAYS)) {
									// Check storage account is not specified, i.e URL is auto
									if (component.getCloudSrc() != null && component.getCloudSrc().equalsIgnoreCase("auto")) {
										replaceAutoURLWithNull = false;
										// Get storage account and create if needed.
										if (curAcc == null) {
											// In next release validate rest of the parameters as well.
											if (!Utils.isValidFilePath(publishSettingsPath)) {
												throw new BuildException("Storage account is specified as 'auto', ensure project contains valid Publish information");
											}
											curAcc = createStorageAccountIfNotExists();
											curKey = curAcc.getPrimaryKey();
											accUrl = curAcc.getStorageAccountProperties().getEndpoints().get(0).toString();
										}
									
										if (cmpntType.equals("jdk.deploy")) {
											String cloudValue = getThrdPartyJdkCloudValue(role);
											if (cloudValue != null && !cloudValue.isEmpty() && component.getCloudAltSrc() != null) {
												component.setCloudSrc(Utils.prepareUrlForThirdPartyJdk(cloudValue, accUrl));
											} else {
												component.setCloudSrc(Utils.prepareCloudBlobURL(component.getImportSrc(), accUrl));
											}
										} else if (cmpntType.equals("server.deploy")){
											String cloudValue = getThrdPartyServerCloudValue(role.getName());
											if (component.getCloudAltSrc() != null && cloudValue != null) {
												component.setCloudSrc(Utils.prepareUrlForThirdPartyJdk(cloudValue, accUrl));
											} else {
												component.setCloudSrc(Utils.prepareCloudBlobURL(component.getImportSrc(), accUrl));
											}
										} else {
											component.setCloudSrc(Utils.prepareUrlForApp(component.getImportAs(), accUrl));
										}
					
										component.setCloudKey(curKey);
									
										// Save components that are modified
										AutoUpldCmpnts obj = new AutoUpldCmpnts(role.getName());
									
										/*
										 * Check list contains entry with this role name,
										 * if yes then just add index of entry to list else create new object.
										 */
										if (mdfdCmpntList.contains(obj)) {
											int index = mdfdCmpntList.indexOf(obj);
											AutoUpldCmpnts presentObj =	mdfdCmpntList.get(index);
											if (!presentObj.getCmpntIndices().contains(j)) {
												presentObj.getCmpntIndices().add(j);
											}
										} else {
											mdfdCmpntList.add(obj);
											obj.getCmpntIndices().add(j);
										}
										this.log(role.getName() + " - " + cmpntType + " : Temporarily replaced 'auto' settings.");
									}
								}
							}
						} 
						
						if (replaceAutoURLWithNull) {
							replaceAutoURLWithNull(component);
						}
					}
				}
				// replace -auto cache storage account
				configureAutoCacheStorageAccount(role.getName(), curAcc);
			}
		}
	}
	
	/**
	 * Replaces components cloudsrc with null in all roles if cloudsrc=auto
	 */
	private void replaceAutoURLWithNull() {
		// Iterate over role list.
		Vector<WorkerRole> roles = this.roles;
		
		for (int i = 0; i < roles.size(); i++) {
			WorkerRole role = roles.get(i);

			// Iterate over component definitions.
			Vector<Component> cmpnntsList = role.getComponents();
			if (cmpnntsList != null) {
				for (int j = 0; j < cmpnntsList.size(); j++) {
					replaceAutoURLWithNull(cmpnntsList.get(j));
				}
			}
		}
	}
	
	/**
	 * Replaces components cloudsrc with null if cloudsrc=AUTO_URL
	 */
	private void replaceAutoURLWithNull(Component component) {
		if (component.getCloudSrc() != null && component.getCloudSrc().equals("auto")) {
			component.setCloudSrc(null);
		}
	}
	
	

	private StorageService createStorageAccountIfNotExists() throws Exception {
		File pubFile =  new File(this.publishSettingsPath);
		if (Utils.isNullOrEmpty(subscriptionId)) {
			try {
				subscriptionId = XMLUtil.getDefaultSubscription(pubFile);
			} catch (Exception e) {
				throw new BuildException(e);
			}
		}
		
		StorageService storageAccount = null;
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			WindowsAzureServiceManagement instance = Utils.getServiceInstance();
			
			Thread.currentThread().setContextClassLoader(WindowsAzurePackage.class.getClassLoader());
			
			com.microsoft.windowsazure.Configuration configuration =
					WindowsAzureRestUtils.getConfiguration(pubFile, subscriptionId);
			String managementUrl = XMLUtil.getManagementUrl(pubFile, subscriptionId);
			storageAccount = Utils.createStorageAccountIfNotExists(
					configuration, instance, storageAccountName, region, managementUrl);
		} finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
		
		return storageAccount;
	}

	/**
	 * Executes the task
	 */
	public void execute() throws BuildException {
		try {
			
		// To support auto storage in Ant task
		try {
			configureAutoCloudUrl();
		} catch (Exception e) {
			reportBuildError(e);
		}

		// Initialize and verify attributes
		this.initialize();

		// Ensure that all approot directories are correctly setup
		try {
			this.verifyAppRoots();
		} catch (IOException e) {
			reportBuildError(e);
		}

		// Validate Azure Project Configuration
		checkProjectConfiguration();
		this.log("Verified attributes.");

		// Start verifying downloads if needed, on a separate thread
		startDownloadManagement();
		
		// Include storage library into all roles
		includeStorageClientLibrary();
		
		// Import all components into roles
		importComponents();

		// Generate deployment startup scripts for all components
		try {
			this.createStartupScripts();
		} catch (IOException e) {
			reportBuildError(e);
		}

		// Run cspack.exe
		this.log("Starting package generation...");
        try {
            if (getSdkDir() != null) {
                // Get cspack.exe cmd-line
                List<String> csPackCmdLine = this.createCSPackCommandLine();
                this.runCommandLine(csPackCmdLine);
            } else {
                Configuration configuration = initConfiguration(this);
//				new PackageCreator(configuration).createPackage();
                new BinaryPackageCreator(configuration).createPackage();
			}
        } catch (Exception e) {
            reportBuildError(e);
        }

        this.log("Completed package generation.");

		// Prepare any additional deploy files
		try {
			prepareDeployFiles();
		} catch (IOException e) {
			reportBuildError(e);
		}

		// Wait for the download verifier thread
		try {
			finishDownloadManagement();
		} catch(Exception e) {
			reportBuildError(e);
		}

		/*
		 * Restore components which are updated during build
		 * to original state i.e. again updates cloudurl to "auto"
		 * and removes cloudkey attribute.
		 */
		if (mdfdCmpntList.size() > 0) {
			addAutoCloudUrl();
		}
		if (roleMdfdCache.size() > 0) {
			addAutoSettingsForCache();
		}
		} catch(Exception e) {
			reportBuildError(e);
		} 
	}

	private void addAutoCloudUrl() {
		Vector<WorkerRole> roles = this.roles;
		for (int i = 0; i < roles.size(); i++) {
			WorkerRole role = roles.get(i);
			AutoUpldCmpnts obj = new AutoUpldCmpnts(role.getName());
			// check list has entry with this role name
			if (mdfdCmpntList.contains(obj)) {
				List<Component> cmpnntsList = role.getComponents();
				// get index of components which needs to be updated.
				int index = mdfdCmpntList.indexOf(obj);
				AutoUpldCmpnts presentObj = mdfdCmpntList.get(index);
				List<Integer> indices = presentObj.getCmpntIndices();
				// iterate over indices and update respective components.
				for (int j = 0; j < indices.size(); j++) {
					Component cmpnt = cmpnntsList.get(indices.get(j));
					cmpnt.setCloudSrc(auto);
					cmpnt.setCloudKey("");
				}
			}
		}
	}

	private void addAutoSettingsForCache() throws Exception {
		String cscfgPath = projectDir + File.separator + DEFAULT_CONFIGURATION_FILE_NAME;
		Document doc = XMLUtil.parseXMLFile(new File(cscfgPath));
		Vector<WorkerRole> roles = this.roles;
		for (int i = 0; i < roles.size(); i++) {
			String roleName = roles.get(i).getName();
			if (roleMdfdCache.contains(roleName)) {
				String exp = String.format(cacheSettingStr, roleName, settingName);
				HashMap<String, String> nodeAttribites = new HashMap<String, String>();
				nodeAttribites.put("name", settingName);
				nodeAttribites.put("value", valueName);
				XMLUtil.updateElementAttributeValue(doc, exp, nodeAttribites);
			}
		}
		XMLUtil.saveXMLDocument(cscfgPath, doc);
	}

	private Configuration initConfiguration(WindowsAzurePackage waPackage) {
		Configuration configuration = new Configuration(waPackage);
        configuration.setConfigurationFileName(configurationFileName);
        configuration.setDefinitionFileName(definitionFileName);
        configuration.setEmulatorDir(emulatorDir);
        configuration.setEmulatorToolsDir(emulatorToolsDir);
        configuration.setCloudToolsDir(cloudToolsDir);
        configuration.setPackageDir(packageDir);
        configuration.setPackageFileName(packageFileName);
        configuration.setProjectDir(projectDir);
        configuration.setSdkDir(sdkDir);
        configuration.setSdkKit(sdkKit);
        configuration.setTemplatesDir(templatesDir);

        configuration.init();

        return configuration;
    }

	/**
	 * Reports build error
	 */
	private void reportBuildError(Exception e) {
		e.printStackTrace();
		final File deployPathFile = new File(this.packageDir);
		final File buildLogFile = new File(deployPathFile, BUILD_ERROR_FILENAME);
		if(e == null) {
			return;
		} else if(!deployPathFile.exists() || !deployPathFile.isDirectory()) {
			throw new BuildException(e);
		} else {
			try {
				buildLogFile.createNewFile();
				saveTextFile(buildLogFile, e.getMessage());
				throw new BuildException(e);
			} catch (IOException e1) {
				throw new BuildException(e);
			} 
		}
	}
	
	
	/**
	 * Creates a new instance of WorkerRole
	 * @return New instance of WorkerRole
	 */
	public WorkerRole createWorkerRole() {
		WorkerRole role = new WorkerRole(this);
		roles.addElement(role);
		return role;
	}

	/**
	 * Initializes the task and verifies that all of the required task attributes are set
	 */
	private void initialize() {
		// Verify that we have at least one role defined
		if (this.roles.size() == 0) {
			throw new BuildException("At least one Azure role must be specified");
		}

        if (IS_WINDOWS) {
            // Ensure that we know where to find the SDK
            if (this.sdkDir == null || !new File(this.sdkDir).exists()) {
                try {
                    this.sdkDir = findLatestAzureSdkDir();
                } catch (IOException e) {
                    throw new BuildException(e.getMessage());
                }
            }

            // Ensure that we know where to find the emulator software
            if (this.emulatorDir == null || !new File(this.emulatorDir).exists()) {
                try {
                    this.emulatorDir = findEmulatorDir();
                } catch (IOException e) {
                    throw new BuildException(e.getMessage());
                }
            }

            // Check for storage emulator
            if (this.storageEmulatorDir == null || !new File(this.storageEmulatorDir).exists()) {
                try {
                    this.storageEmulatorDir = findStorageEmulatorDir();
                } catch (IOException e) {
                    throw new BuildException(e.getMessage());
                }
            }
        }
		// Verify that packagefilename is set
		if (this.packageFileName == null) {
			throw new BuildException("The required packagefilename setting is missing");
		}

		// Set projectDir to a default (based on basedir) if not set
		if (this.projectDir == null) {
			this.projectDir = getProject().getBaseDir().getAbsolutePath();
		}

		// Verify that projectdir exists
		File projectPath = new File(this.projectDir);
		if (!projectPath.exists()) {
			throw new BuildException("The projectdir setting points to a directory that does not exist (" + this.projectDir+ ")");
		}

		// Set packageDir to a default (based on projectDir) if not set
		if (this.packageDir == null) {
			this.packageDir = String.format("%s%s%s", this.projectDir, File.separatorChar, DEFAULT_PACKAGE_SUBDIR);
		}

		// Verify that packagedir exists
		File packagePath = new File(this.packageDir);
		if (!packagePath.exists()) {
			throw new BuildException("The package directory does not exist (" + this.packageDir + ")");
		}

		// Set emulatorToolsDir to a default (based on projectDir) if not set
		if (this.emulatorToolsDir == null) {
			this.emulatorToolsDir = String.format("%s%s%s", this.projectDir, File.separatorChar, DEFAULT_EMULATOR_TOOLS_SUBDIR);
		}
		
		// Set cloudToolsDir to a default (based on projectDir) if not set
		if (this.cloudToolsDir == null) {
			this.cloudToolsDir = String.format("%s%s%s", this.projectDir, File.separatorChar, DEFAULT_CLOUD_TOOLS_SUBDIR);
		}

		// Initialize templateDir
		this.templatesDir = String.format("%s%s%s", this.projectDir, File.separator, TEMPLATES_SUBDIR);
		
		// Determine whether to verify downloads depending on network availability
		if(!isNetworkAvailable()) {
			this.verifyDownloads = false;
		}
			
	}
    
	/**
	 * Checks for project configuration Errors and displays error message as part of build output.
	 */
	private void checkProjectConfiguration() {
		Map<ErrorType,List<String>> errorMap = verifyConfiguration();
		if (errorMap != null ) {
			StringBuilder sb = new StringBuilder();
			
			// We just need map values no need of key here , map key may be useful for some other callers of validate API.
			for(List<String> valueList : errorMap.values()) {
				for(String errorMsg : valueList) {
					sb.append(errorMsg);
					sb.append("\n");
				}
			}
			throw new BuildException("Project build failed due to below validation errors \n"+sb);
		}
	}

	/**
	 * Creates command-line used for executing cspack.exe
	 * @return cspack.exe command-line string
	 */
	private List<String> createCSPackCommandLine() {
		List<String> commandArgs = new ArrayList<String>();
		String packageFilePath = String.format("%s%s%s", this.packageDir, File.separatorChar, this.packageFileName);
		
		// The initial cmd-line will include the path to cspack.exe and the csdef file
		String csPackExePath = String.format("%s%scspack.exe", this.sdkDir, File.separatorChar);
		String csdefPath = String.format("%s%s%s", this.projectDir, File.separatorChar, this.definitionFileName);
		commandArgs.add(csPackExePath);
		commandArgs.add(csdefPath);

		if (this.packageType == PackageType.local) {
			commandArgs.add("/copyOnly");
        } else if (this.useCtpPackageFormat == UseCTPFormat.TRUE) {
            commandArgs.add("/useCtpPackageFormat");
		}
		
		// Create cmd-line for roles
		for (Enumeration<WorkerRole> e = this.roles.elements(); e.hasMoreElements();) {
			WorkerRole role = e.nextElement();
			String roleName = role.getName();
			File roleAppRootDir = role.getAppRootDir();

			if (roleName == null) {
				throw new BuildException("The required workerrole name setting is missing");
			}

			if (roleAppRootDir == null) {
				throw new BuildException("The required workerrole approotdir setting is missing");
			}

			commandArgs.add(String.format("/role:%s;%s", roleName, roleAppRootDir.toString()));
			
			if(rolePropertiesFileName != null) {
				commandArgs.add(String.format("/rolePropertiesFile:%s;%s%s%s", roleName, this.projectDir, File.separatorChar, this.rolePropertiesFileName));
			}
		}

		// Add package name
		commandArgs.add(String.format("/out:%s", packageFilePath));

		return commandArgs;
	}

	/**
	 * Ensures that approot directories are properly setup
	 * @throws IOException
	 */
	private void verifyAppRoots() throws IOException {
		for (WorkerRole role : roles) {
			File roleAppRootDir = role.getAppRootDir();
			String roleName = role.getName();

			this.log(String.format("Role \"%s\": Verifying the approot \"%s\"", roleName, roleAppRootDir.toString()));

			// Verify that roleAppRootDir directory exists
			if (!roleAppRootDir.exists()) {
				throw new BuildException("The approotdir setting points to a directory that does not exist");
			}
		}
	}
	
	/**
	 * Inludes storage client library in all roles
	 */
	private void includeStorageClientLibrary() {
		for (WorkerRole role : roles) {
			role.includeStorageClientLibrary();
		}
	}

	/**
	 * Creates a process and executes the provided command-line.
	 * @param commandLine The command-line to execute
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private void runCommandLine(List<String> commandLine) throws InterruptedException, IOException {
		this.log(String.format("Executing '%s'...", commandLine));

		Process process = new ProcessBuilder(commandLine).start();
		this.log("Process started");

		// Capture std-out
		String line;
		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
		try {
			while ((line = input.readLine()) != null) {
				this.log(line);
			}
		} finally {
			input.close();
		}

		this.log("Waiting for process to exit...");

		if (process.waitFor() != 0) {
			throw new BuildException("Process exited with non-zero code");
		}
	}

	/**
	 * Prepares additional deploy files based on templates
	 * @throws IOException
	 */
	private void prepareDeployFiles() throws IOException {
		// Copy cscfg file to the package directory
		copyFile(new File(this.projectDir, this.configurationFileName), new File(this.packageDir, this.configurationFileName));

		// Prepare emulator tools
		prepareEmulatorTools();
		
		// Prepare cloud tools
		prepareCloudTools();

		// Prepare the dev-portal html page 
		createPortalLink();
	}
	
	/**
	 * Prepares portal link file
	 * @throws IOException
	 */
	private void createPortalLink() {
		// Only if creating cloud package
		if (this.packageType != PackageType.cloud || this.portalURL == null) {
			return;
		}
		
		File portalTemplateDirectory = new File(this.templatesDir, DEV_PORTAL_SUBDIR);
		File portalTemplateFile = new File(portalTemplateDirectory, DEV_PORTAL_FILE);		
		File destFile = new File(this.packageDir, DEV_PORTAL_FILE);
		try {
			copyFileReplaceTokens(portalTemplateFile, destFile);
		} catch (IOException e) {
			// Ignore if portal link missing
		} 
	}

	private void copyFileReplaceTokens(File src, File dest) throws IOException
	{
		if(src == null || dest == null || !src.exists() || !src.isFile()) {
			throw new IOException("Missing template file");
		}

		// Load the template file
		String templateText = loadTextFile(src);

		// Replace template tokens
		templateText = replaceTemplateTokens(templateText);

		// Save the result 
		saveTextFile(dest, templateText);
	}
	
	/**
	 * Prepares emulator tools directory
	 * @throws IOException
	 */
	private void prepareEmulatorTools() throws IOException {
		File emulatorToolsDirectory = new File(this.emulatorToolsDir);

		if (this.packageType == PackageType.cloud) {
			// Delete the emulator tools directory for cloud package
			if (emulatorToolsDirectory.exists()) {
				deleteDirectory(emulatorToolsDirectory);
			}
		} else {
			File emulatorTemplateDirectory = new File(this.templatesDir, DEFAULT_EMULATOR_TOOLS_SUBDIR);

			if (!emulatorTemplateDirectory.exists() || !emulatorTemplateDirectory.isDirectory()) {
				throw new IOException("Bad emulator template directory");
			}

			// Create the emulator tools directory if it doesn't exist
			if (!emulatorToolsDirectory.exists()) {
				emulatorToolsDirectory.mkdir();
			}

			// Go through all templates in the directory, and use them to generate scripts
			for (String templateFileName : emulatorTemplateDirectory.list()) {
				File templateFile = new File(emulatorTemplateDirectory, templateFileName);
				File destFile = new File(emulatorToolsDirectory, templateFileName);
				copyFileReplaceTokens(templateFile, destFile);
			}
		}
	}
	
	/**
	 * Prepares cloud tools directory
	 * @throws IOException
	 */
	private void prepareCloudTools() throws IOException {
		File cloudToolsDirectory = new File(this.cloudToolsDir);

		if (this.packageType == PackageType.cloud) {
			File cloudTemplateDirectory = new File(this.templatesDir, DEFAULT_CLOUD_TOOLS_SUBDIR);

			if (!cloudTemplateDirectory.exists() || !cloudTemplateDirectory.isDirectory()) {
				throw new IOException("Bad cloud template directory");
			}

			// Create the cloud tools directory if it doesn't exist
			if (!cloudToolsDirectory.exists()) {
				cloudToolsDirectory.mkdir();
			}

			// Go through all templates in the directory, and use them to generate scripts
			for (String templateFileName : cloudTemplateDirectory.list()) {
				File templateFile = new File(cloudTemplateDirectory, templateFileName);
				File destFile = new File(cloudToolsDirectory, templateFileName);
				// call replace and copy, so that in future if there are any tokens it will be automatically taken care.
				copyFileReplaceTokens(templateFile, destFile);
			}
		} else {
			// Delete the cloud tools directory for local/emulator package
			if (cloudToolsDirectory.exists()) {
				deleteDirectory(cloudToolsDirectory);
			}
		}
	}


	/**
	 * Prepares component imports for all roles
	 * @throws IOException
	 */
	private void importComponents() {
		for (WorkerRole role : roles) {
			this.log(String.format("Role \"%s\": Importing components...", role.getName()));
			role.importComponents();
			this.log(String.format("Role \"%s\": Finished importing components", role.getName()));
		}
	}

	/**
	 * Prepares internal startup scripts for each role
	 * @throws IOException
	 */
	private void createStartupScripts() throws IOException {

		// Load the template file
		File templateFile = new File(this.templatesDir, String.format("%s%s%s", INTERNAL_STARTUP_SUBDIR, File.separator, INTERNAL_STARTUP_FILE_NAME));
		if (!templateFile.exists() || !templateFile.isFile()) {
			throw new BuildException(String.format("Missing internal template file '%s'", templateFile));
		} 

		String templateText = loadTextFile(templateFile);

		// Generate an internal startup script for each role
		for (WorkerRole r : roles) {
			this.log(String.format("Role \"%s\": Generating component deployment script...", r.getName()));
			r.createStartupScript(templateText);
			this.log(String.format("Role \"%s\": Created internal startup script", r.getName()));
		}
	}

	/**
	 * Import as a copy
	 * @param src
	 * @param dest
	 */
	public void copyFile(File src, File dest) {

		Copy copyTask = new Copy();
		copyTask.bindToOwner(this);
		copyTask.init();

		// Determine src and dest type
		if (src.isFile()) {
			copyTask.setFile(src);
			copyTask.setTofile(dest);
		} else if (src.isDirectory()) {
			FileSet fileSet = new FileSet();
			fileSet.setDir(src);
			copyTask.addFileset(fileSet);
			copyTask.setTodir(dest);
		} else {
			throw new BuildException("Component \"%s\" cannot be imported because it is neither a directory nor a file.");
		}
		copyTask.perform();
	}

	/**
	 * Import as a Zip
	 * @param src
	 * @param dest
	 */
	public void zipFile(File src, File dest) {
		Zip zipTask = new Zip();
		zipTask.bindToOwner(this);
		zipTask.init();

		// Determine src and dest type
		if (src.isDirectory()) {
			FileSet fileSet = new FileSet();
			fileSet.setDir(src.getParentFile());
			FilenameSelector filenameSelector = new FilenameSelector();
			filenameSelector.setName(src.getName() + File.separator + "**");
			fileSet.addFilename(filenameSelector);
			zipTask.addFileset(fileSet);
		} else if (src.isFile()) {
			zipTask.setBasedir(src.getParentFile());
			zipTask.setIncludes(src.getName());
		} else {
			throw new BuildException(String.format("Cannot import '%s' because it is not a directory", src));
		}

		zipTask.setUpdate(true);
		zipTask.setCompress(true);
		zipTask.setDestFile(dest);
		zipTask.perform();
	}

	/**
	 * Replaces template tokens in text with corresponding values
	 * @param text Text to replace tokens in
	 */
	private String replaceTemplateTokens(String text) {
        text = text.replace(TEMPLATE_TOKEN_CONFIGURATIONFILENAME, this.configurationFileName);
        text = text.replace(TEMPLATE_TOKEN_DEFINITIONFILENAME, this.definitionFileName);
        text = text.replace(TEMPLATE_TOKEN_PACKAGEDIR, this.packageDir);
        text = text.replace(TEMPLATE_TOKEN_PACKAGEFILENAME, this.packageFileName);
        text = text.replace(TEMPLATE_TOKEN_PROJECTDIR, this.projectDir);
        if (IS_WINDOWS) {
            text = text.replace(TEMPLATE_TOKEN_SDKDIR, this.sdkDir);
		    text = text.replace(TEMPLATE_TOKEN_EMULATORDIR, this.emulatorDir);
            text = text.replace(TEMPLATE_TOKEN_STORAGE_EMULATORDIR, this.storageEmulatorDir);
        }
		if(this.portalURL != null) {
			text = text.replace(TEMPLATE_TOKEN_PORTALURL, this.portalURL);
		}
		return text;
	}

	/**
	 * Loads text from a file
	 * @param file File to load
	 * @return Text from the file
	 * @throws IOException
	 */
	public static String loadTextFile(File file) throws IOException {
		char[] buffer = new char[4096];
		int len;
		StringBuilder input = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(file));

		try {
			while ((len = reader.read(buffer)) != -1) {
				input.append(buffer, 0, len);
			}
		} finally {
			reader.close();
		}

		return input.toString();
	}

	/**
	 * Saves text into a file
	 * @param file File to save the text in
	 * @param text Text to save
	 * @throws IOException
	 */
	public static void saveTextFile(File file, String text) throws IOException {
		FileWriter writer = new FileWriter(file);

		try {
			writer.write(text);
		} finally {
			writer.close();
		}
	}


	/**
	 * Deletes a directory including all of its contents
	 * @param directory Directory to delete
	 */
	public static void deleteDirectory(File directory) {
		if (directory.isFile()) {
			directory.delete();
		} else {
			for (String file : directory.list()) {
				deleteDirectory(new File(directory, file));
			}

			// Delete emptied directory
			directory.delete();
		}
	}

	/**
	 * Discovers the path of the emulator installation directory
	 * @return The emulator installation directory
	 */
	private static String findEmulatorDir() throws IOException {
		// Make sure that we use 64bit Program File even if we're running inside WOW64 process
		String programFilesDir = System.getenv(ENV_PROGRAMFILES_WOW64);
		
		if (programFilesDir == null) {
			programFilesDir = System.getenv(ENV_PROGRAMFILES);
		}

		File emulatorDir = new File(String.format("%s%sMicrosoft SDKs%sAzure%sEmulator", programFilesDir, File.separatorChar, File.separatorChar, File.separatorChar));

		// Check if the Emulator folder exists
		if (emulatorDir.exists()) {
			return emulatorDir.toString();
		} else {
			throw new IOException("Microsoft Azure Storage Emulator is not installed.");			
		}
	}
	
	/**
	 * Discovers the path of the storage emulator installation directory
	 * @return The storage emulator installation directory
	 */
	private static String findStorageEmulatorDir() throws IOException {
		// Make sure that we use 64bit Program File even if we're running inside WOW64 process
		String programFilesDir = System.getenv(ENV_X86_PROGRAMFILES_WOW64);
		
		if (programFilesDir == null) {
			programFilesDir = System.getenv(ENV_PROGRAMFILES);
		}

        File storageEmulatorDir = new File(String.format("%s%sMicrosoft SDKs%sAzure%sStorage Emulator", programFilesDir, File.separatorChar, File.separatorChar, File.separatorChar));

		// Check if the storage Emulator folder exists
		if (storageEmulatorDir.exists()) {
			return storageEmulatorDir.toString();
		} else {
			throw new IOException("Azure SDK v2.8 or later is not installed.");
		}
	}


	/**
	 * Discovers the path of the latest version of Windows Azure SDK
	 * @return The sdk path
	 */
	private static String findLatestAzureSdkDir() throws IOException {
		// Make sure that we use 64bit Program File even if we're running inside WOW64 process
		String programFilesDir = System.getenv(ENV_PROGRAMFILES_WOW64);

		if (programFilesDir == null) {
			programFilesDir = System.getenv(ENV_PROGRAMFILES);
		}

		File sdkDir = new File(String.format("%s%sMicrosoft SDKs%sAzure%s.NET SDK", programFilesDir, File.separatorChar,File.separatorChar,File.separatorChar));

		// Check if the SDK folder exists
		if (!sdkDir.exists()) {
			throw new IOException("Azure SDK v2.8 or later is not installed.");
		}
		
		String[] versionedSDKDirs = sdkDir.list();
		String latestVersionSdkDir = null;
		if(versionedSDKDirs != null && versionedSDKDirs.length > 0 ) {
			TreeSet<String> sortedDirs = new TreeSet<String>(Arrays.asList(versionedSDKDirs));

			// From sorted list , consider the first latest version where we can find cspack.exe in bin directory 
			for (Iterator<String> iterator = sortedDirs.descendingIterator(); iterator.hasNext();) {
				File versionedSdkDir = new File(sdkDir, iterator.next());
				if (versionedSdkDir.isDirectory()) {
					File csPackPath = new File(String.format("%s%sbin%scspack.exe", versionedSdkDir.toString(), File.separatorChar, File.separatorChar));
					if (csPackPath.exists()) {
						latestVersionSdkDir = versionedSdkDir.toString();
						break;
					}
				}
			}
		}

		if (latestVersionSdkDir == null) {
			throw new IOException("Azure SDK v2.8 or later is not installed.");
		}

		return String.format("%s%sbin", latestVersionSdkDir, File.separatorChar);
	}
	
	/** Checks whether network is available
	 * @return
	 */
	public static boolean isNetworkAvailable() {
		return true; 
		// The previously used algorithm below is not reliable and it's not clear what's a good way to test for network connectivity without 
		// actually pinging some address, which we do not want to do
		
		/*
        if (IS_WINDOWS) {
            Enumeration<NetworkInterface> networkInterfaces;
            InetAddress inetAddress;

            try {
                if (null == (networkInterfaces = NetworkInterface.getNetworkInterfaces())) {
                    return false;
                }

                while (networkInterfaces.hasMoreElements()) {
                    Enumeration<InetAddress> internetAddresses = networkInterfaces.nextElement().getInetAddresses();
                    while (internetAddresses.hasMoreElements()) {
                        if (null == (inetAddress = internetAddresses.nextElement())) {
                            return false;
                        } else if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress()) {
                            continue;
                        } else if (!inetAddress.getHostName().equals(inetAddress.getHostAddress())) {
                            return true;
                        }
                    }
                }
            } catch (SocketException e) {
                return false;
            }

            return false;
        } else {
            return true;
        } */
	}
	

	/** Verifies URL exists */
	public static boolean verifyURLAvailable(URL url) {
		if (null == url)
			return false;
		
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			return (200 == connection.getResponseCode());
		} catch (IOException e) {
			return false;
		}
	}
	
	
	/** Starts the download verifier thread
	 */
	private void startDownloadManagement() {
		if(getPackageType() == PackageType.cloud) {
			downloadManager = new DownloadManager(this);
			downloadManagerThread = new Thread(downloadManager);
			downloadManagerThread.start();
		}
	}

	/** Waits for the download verification to finish
	 */
	private void finishDownloadManagement() {
		if(downloadManagerThread != null) {
			try {
				downloadManagerThread.join();
			} catch (InterruptedException e) {
				;
			}
			if(downloadManager != null && downloadManager.exception != null) {
				throw downloadManager.exception;
			}
		}		
	}
	
	
	/**
	 * Class to implement the component download management as a separate thread
	 */
	private class DownloadManager implements Runnable {
		private WindowsAzurePackage windowsAzurePackage = null;
		private WindowsAzureManager windowsAzureManager = null;
		public BuildException exception = null;
		public DownloadManager(WindowsAzurePackage windowsAzurePackage) {
			this.windowsAzurePackage = windowsAzurePackage;
			windowsAzureManager = new WindowsAzureManager();
		}
		
	    public void run() {
	    	// Ensure download availability
			try {
				for(WorkerRole role : windowsAzurePackage.roles) {
					for(Component component : role.getComponents()) {
	    				component.ensureDownload(windowsAzureManager);
					}
				}
			} catch(BuildException e) {
				exception = e;
			}
	    }
	}

	/**
	 * Validates Azure Project configuration
	 * @return a map with enum ErrorType as key and list of error messages as value.  
	 */
	public Map<ErrorType, List<String>> verifyConfiguration() {
		Map<ErrorType, List<String>> errorMap = null;
		
		// For package type CLOUD , check if cache config settings are properly configured
		if (PackageType.cloud == this.packageType) {
			try { 
				NodeList roleNames = XMLUtil.getNodeList(DEV_CACHE_CONFIG, new File(this.projectDir, this.configurationFileName));
				
				if(roleNames.getLength() > 0 ) {
					List<String> errorMessages = new ArrayList<String>();
					
					// Populate error message for each role if storage account info is not configured
					for (int i = 0; i < roleNames.getLength(); i++) {
						errorMessages.add(String.format("Missing storage account information for the caching " +
														"feature enabled in role '%s'", roleNames.item(i).getNodeValue()));
					}
					
					if (errorMap == null)
						errorMap = new HashMap<ErrorType, List<String>>();
					
					// Put entry into error map
					errorMap.put(ErrorType.CACHE_CONFIG, errorMessages);
				}
			} catch(Exception e) {
				throw new BuildException("Error Occured while validating Azure Cache config settings");
			}
		}
		
		return errorMap;
	}
}
