/*
 Copyright 2013 Microsoft Open Technologies, Inc.
 
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

package com.microsoftopentechnologies.windowsazure.tools.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Arrays;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;
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
	private static final String BUILD_ERROR_FILENAME = "BuildFailure.txt";
	public static final String DEFAULT_UTIL_SUBDIR = "util"; // relative to approot
	public static final String UTIL_UNZIP_FILENAME = "unzip.vbs";
	public static final String UTIL_DOWNLOAD_FILENAME = "download.vbs";
	public static final String UTIL_WASH_FILENAME = "wash.cmd";
	public static final String UTIL_WASH_PATH = DEFAULT_UTIL_SUBDIR + File.separator + UTIL_WASH_FILENAME;
	
	public static final String INTERNAL_STARTUP_FILE_NAME = ".startup.cmd";
	private static final String INTERNAL_STARTUP_SUBDIR = "startup";
	public static final String USER_STARTUP_FILE_NAME = "startup.cmd";
	private static final String DEV_PORTAL_SUBDIR = "devPortal";
	private static final String DEV_PORTAL_FILE = "WindowsAzurePortal.url";
	private static final String ENV_PROGRAMFILES_WOW64 = "ProgramW6432";
	private static final String ENV_PROGRAMFILES = "ProgramFiles";
	public static final String STORAGEDLL_SUBDIR = "..\\ref"; // relative to sdk\bin dir
	public static final String STORAGEDLL_FILENAME = "Microsoft.WindowsAzure.StorageClient.dll";
	private static final String TEMPLATES_SUBDIR = ".templates";
	private static final String TEMPLATE_TOKEN_SDKDIR = "${SDKDir}";
	private static final String TEMPLATE_TOKEN_EMULATORDIR = "${EmulatorDir}";
	private static final String TEMPLATE_TOKEN_PROJECTDIR = "${ProjectDir}";
	private static final String TEMPLATE_TOKEN_PACKAGEDIR = "${PackageDir}";
	private static final String TEMPLATE_TOKEN_PACKAGEFILENAME = "${PackageFileName}";
	private static final String TEMPLATE_TOKEN_DEFINITIONFILENAME = "${DefinitionFileName}";
	private static final String TEMPLATE_TOKEN_CONFIGURATIONFILENAME = "${ConfigurationFileName}";
	public static final String TEMPLATE_TOKEN_COMPONENTS_SCRIPT = "${Components}";
	public static final String TEMPLATE_TOKEN_VARIABLES_SCRIPT = "${Variables}";
	public static final String TEMPLATE_TOKEN_USER_STARTUP_SCRIPT = "${UserStartup}";
	private static final String TEMPLATE_TOKEN_PORTALURL = "${PortalURL}";
	// Xpath expression to get names of workerroles whose cache config is referring to development storage
	private static final String DEV_CACHE_CONFIG = "/ServiceConfiguration/Role[ConfigurationSettings/Setting[" +
			"@name='Microsoft.WindowsAzure.Plugins.Caching.ConfigStoreConnectionString' and @value='UseDevelopmentStorage=true']]/@name";

	public static String newline = System.getProperty("line.separator");

	private Vector<WorkerRole> roles;
	private PackageType packageType;
	private String sdkDir;
	private String emulatorDir;
	private String projectDir;
	private String packageDir;
	private String packageFileName;
	private String definitionFileName;
	private String configurationFileName;
	private String emulatorToolsDir;
	private String templatesDir;
	private String portalURL;
	private String rolePropertiesFileName = null;
	private boolean useCtpPackageFormat = false;
	private boolean verifyDownloads = true;
	private Thread downloadManagerThread = null;
	
	private DownloadManager downloadManager;
	
	/**
	 * WindowsAzurePackage constructor
	 */
	public WindowsAzurePackage() {
		this.roles = new Vector<WorkerRole>();
		this.packageType = PackageType.cloud;
		this.definitionFileName = DEFAULT_DEFINITION_FILE_NAME;
		this.configurationFileName = DEFAULT_CONFIGURATION_FILE_NAME;
	}

	/**
	 * Sets the Windows Azure portal URL
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
	public void setUseCtpPackageFormat(boolean value) {
		useCtpPackageFormat = value;
	}
	
	public boolean getUseCtpPackageFormat() {
		return useCtpPackageFormat;
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
	 * Sets projectdir attribute
	 * @param projectDir
	 */
	public void setProjectDir(String projectDir) {
		this.projectDir = projectDir;
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
	 * Sets verifydownloads attribute
	 * @param verifyDownloads
	 */
	public void setVerifyDownloads(boolean verifyDownloads) {
		this.verifyDownloads = verifyDownloads;
	}
	public boolean getVerifyDownloads() {
		return this.verifyDownloads;
	}

	/**
	 * Executes the task
	 */
	public void execute() throws BuildException {		
		// Initialize and verify attributes
		this.initialize();

		// Get cspack.exe cmd-line
		List<String> csPackCmdLine = this.createCSPackCommandLine();

		// Ensure that all approot directories are correctly setup
		try {
			this.verifyAppRoots();
		} catch (IOException e) {
			reportBuildError(e);
		}

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
			this.runCommandLine(csPackCmdLine);
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
	}

	/**
	 * Reports build error
	 */
	private void reportBuildError(Exception e) {
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
			throw new BuildException("At least one Windows Azure role must be specified");
		}

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

		// Initialize templateDir
		this.templatesDir = String.format("%s%s%s", this.projectDir, File.separator, TEMPLATES_SUBDIR);
		
		// Validate Windows Azure Project Configuration
		checkProjectConfiguration();
		this.log("Verified attributes.");
		
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
		} else if(this.useCtpPackageFormat) {
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
		text = text.replace(TEMPLATE_TOKEN_SDKDIR, this.sdkDir);
		text = text.replace(TEMPLATE_TOKEN_EMULATORDIR, this.emulatorDir);
		
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

		File emulatorDir = new File(String.format("%s%sMicrosoft SDKs%sWindows Azure%sEmulator", programFilesDir, File.separatorChar, File.separatorChar, File.separatorChar));

		// Check if the Emulator folder exists
		if (emulatorDir.exists()) {
			return emulatorDir.toString();
		} else {
			throw new IOException("Windows Azure SDK v1.7 or later is not installed.");			
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

		File sdkDir = new File(String.format("%s%sMicrosoft SDKs%sWindows Azure%s.NET SDK", programFilesDir, File.separatorChar,File.separatorChar,File.separatorChar));

		// Check if the SDK folder exists
		if (!sdkDir.exists()) {
			throw new IOException("Windows Azure SDK v1.7 or later is not installed.");
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
			throw new IOException("Windows Azure SDK v1.7 or later is not installed.");
		}

		return String.format("%s%sbin", latestVersionSdkDir, File.separatorChar);
	}
	
	/** Checks whether network is available
	 * @return
	 */
	public static boolean isNetworkAvailable() {
		Enumeration<NetworkInterface> networkInterfaces;
		InetAddress inetAddress;
		
		try {
			if(null == (networkInterfaces = NetworkInterface.getNetworkInterfaces())) {
				return false;
			}

			while(networkInterfaces.hasMoreElements()) {
				Enumeration<InetAddress> internetAddresses = networkInterfaces.nextElement().getInetAddresses();
				while(internetAddresses.hasMoreElements()) {
					if(null == (inetAddress = internetAddresses.nextElement())) {
						return false;
					} else if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress()) {
						continue;
					} else if (!inetAddress.getHostName().equals(inetAddress.getHostAddress())) {
						return true;
					}
				}
			}
		} catch(SocketException e) {
			return false;
		}
		
		return false;
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
			final File washUtilPath = new File(new File(roles.firstElement().getAppRootDir(), WindowsAzurePackage.DEFAULT_UTIL_SUBDIR), WindowsAzurePackage.UTIL_WASH_FILENAME);
			windowsAzureManager.start(washUtilPath);
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
			} finally {
				if(windowsAzureManager != null) {
					windowsAzureManager.stop();
				}
			}
	    }
	}

	/**
	 * Validates Windows Azure Project configuration
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
				throw new BuildException("Error Occured while validating Windows Azure Cache config settings");
			}
		}
		
		return errorMap;
	}
}
