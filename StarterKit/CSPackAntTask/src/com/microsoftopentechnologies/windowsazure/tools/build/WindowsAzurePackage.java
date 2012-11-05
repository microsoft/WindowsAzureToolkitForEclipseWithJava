/*
 Copyright 2012 Microsoft Open Technologies, Inc.
 
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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Arrays;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;

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
	private static final String DEFAULT_UTIL_SUBDIR = "util";
	private static final String UNZIP_SCRIPT_FILE_NAME = "unzip.vbs";
	private static final String INTERNAL_STARTUP_FILE_NAME = ".startup.cmd";
	private static final String INTERNAL_STARTUP_SUBDIR = "startup";
	private static final String USER_STARTUP_FILE_NAME = "startup.cmd";
	private static final String DEV_PORTAL_SUBDIR = "devPortal";
	private static final String DEV_PORTAL_FILE = "WindowsAzurePortal.url";
	private static final String ENV_PROGRAMFILES_WOW64 = "ProgramW6432";
	private static final String ENV_PROGRAMFILES = "ProgramFiles";
	private static final String TEMPLATES_SUBDIR = ".templates";
	private static final String TEMPLATE_TOKEN_SDKDIR = "${SDKDir}";
	private static final String TEMPLATE_TOKEN_EMULATORDIR = "${EmulatorDir}";
	private static final String TEMPLATE_TOKEN_PROJECTDIR = "${ProjectDir}";
	private static final String TEMPLATE_TOKEN_PACKAGEDIR = "${PackageDir}";
	private static final String TEMPLATE_TOKEN_PACKAGEFILENAME = "${PackageFileName}";
	private static final String TEMPLATE_TOKEN_DEFINITIONFILENAME = "${DefinitionFileName}";
	private static final String TEMPLATE_TOKEN_CONFIGURATIONFILENAME = "${ConfigurationFileName}";
	private static final String TEMPLATE_TOKEN_COMPONENTS_SCRIPT = "${Components}";
	private static final String TEMPLATE_TOKEN_VARIABLES_SCRIPT = "${Variables}";
	private static final String TEMPLATE_TOKEN_USER_STARTUP_SCRIPT = "${UserStartup}";
	private static final String TEMPLATE_TOKEN_PORTALURL = "${PortalURL}";

	private static String newline = System.getProperty("line.separator");

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

	/**
	 * Sets sdkdir attribute
	 * @param sdkDir
	 */
	public void setSdkDir(String sdkDir) {
		this.sdkDir = sdkDir;
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
	 * Executes the task
	 */
	public void execute() throws BuildException {
		// Initialize and verify attributes
		this.initialize();

		// Get cspack.exe cmd-line
		String csPackCmdLine = this.createCSPackCommandLine();

		// Ensure that all approot directories are correctly setup
		try {
			this.ensureAppRoots();
		} catch (IOException e) {
			throw new BuildException(e);
		}

		// Import all components into roles
		prepareComponentImports();

		// Generate deployment startup scripts for all components
		try {
			this.prepareStartupScripts();
		} catch (IOException e) {
			throw new BuildException(e);
		}

		// Run cspack.exe
		this.log("Starting package generation...");

		try {
			this.runCommandLine(csPackCmdLine);
		} catch (InterruptedException e) {
			throw new BuildException(e);
		} catch (IOException e) {
			throw new BuildException(e);
		}

		this.log("Completed package generation.");

		// Prepare any additional deploy files
		try {
			prepareDeployFiles();
		} catch (IOException e) {
			throw new BuildException(e);
		}
	}

	/**
	 * Creates a new instance of WorkerRole
	 * @return New instance of WorkerRole
	 */
	public WorkerRole createWorkerRole() {
		WorkerRole role = new WorkerRole();
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

		this.log("Verified attributes.");
	}

	/**
	 * Creates command-line used for executing cspack.exe
	 * @return cspack.exe command-line string
	 */
	private String createCSPackCommandLine() {
		String packageFilePath = String.format("\"%s%s%s\"", this.packageDir, File.separatorChar, this.packageFileName);

		// The initial cmd-line will include the path to cspack.exe and the csdef file
		StringBuilder csPackCmdLine = new StringBuilder(String.format("\"%s%scspack.exe\" \"%s%s%s\"", this.sdkDir, File.separatorChar, this.projectDir, File.separatorChar, this.definitionFileName));

		if (this.packageType == PackageType.local) {
			csPackCmdLine.append(" /copyOnly");
		} else if(this.useCtpPackageFormat) {
			csPackCmdLine.append(" /useCtpPackageFormat");
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

			csPackCmdLine.append(String.format(" /role:%s;\"%s\"", roleName, roleAppRootDir.toString()));
			
			if(rolePropertiesFileName != null) {
				csPackCmdLine.append(String.format(" /rolePropertiesFile:%s;\"%s%s%s\"", roleName, this.projectDir, File.separatorChar, this.rolePropertiesFileName));
			}
		}

		// Add package name
		csPackCmdLine.append(String.format(" /out:%s", packageFilePath));

		return csPackCmdLine.toString();
	}

	/**
	 * Ensures that approot directories are properly setup
	 * @throws IOException
	 */
	private void ensureAppRoots() throws IOException {
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
	 * Creates a process and executes the provided command-line.
	 * @param commandLine The command-line to execute
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private void runCommandLine(String commandLine) throws InterruptedException, IOException {
		this.log(String.format("Executing '%s'...", commandLine));

		Process process = Runtime.getRuntime().exec(commandLine);
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
		copyFile(String.format("%s%s%s", this.projectDir, File.separatorChar, this.configurationFileName), String.format("%s%s%s", this.packageDir, File.separatorChar, this.configurationFileName));

		// Prepare emulator tools
		prepareEmulatorTools();

		// Prepare the dev-portal html page 
		preparePortalLink();
	}
	
	/**
	 * Prepares portal link file
	 * @throws IOException
	 */
	private void preparePortalLink() {
		// Only if creating cloud package
		if (this.packageType != PackageType.cloud || this.portalURL == null) {
			return;
		}
		
		File portalTemplateDirectory = new File(this.templatesDir, DEV_PORTAL_SUBDIR);
		File portalTemplateFile = new File(portalTemplateDirectory, DEV_PORTAL_FILE);		
		File destFile = new File(this.packageDir, DEV_PORTAL_FILE);
		try {
			CopyFileReplaceTokens(portalTemplateFile, destFile);
		} catch (IOException e) {
			// Ignore if portal link missing
		} 
	}

	private void CopyFileReplaceTokens(File src, File dest) throws IOException
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
				CopyFileReplaceTokens(templateFile, destFile);
			}
		}
	}

	/**
	 * Copies the content of one file on disk to another file on disk
	 * @param srcPath source file path
	 * @param dstPath destination file path
	 * @throws IOException
	 */
	private void copyFile(String srcPath, String dstPath) {
		Copy copyTask = new Copy();
		copyTask.bindToOwner(this);
		copyTask.init();
		copyTask.setFile(new File(srcPath));
		copyTask.setTofile(new File(dstPath));
		copyTask.perform();
	}

	/**
	 * Prepares environment variable settings for each role
	 * @throws IOException
	 */
	private static String createVariablesScript(WorkerRole role) throws IOException {

		if (role == null) {
			return null;
		}

		StringBuilder variablesScript = new StringBuilder();

		for (StartupEnv v : role.getVariables()) {
			String cmdLine = createVariableCommandLine(v.getName(), v.getValue());
			variablesScript.append(cmdLine);
			variablesScript.append(newline);
		}

		return variablesScript.toString();
	}

	/**
	 * Ensures the component's deployment settings are ok and it is ready to be deployed
	 * @param component
	 */
	private static void verifyComponentDeploySettings(Component component, File approot) {

		if (component == null || approot == null) {
			throw new BuildException("Missing component or approot due to an unknown internal error");
		}

		DeployMethod deployMethod = component.getDeployMethod();
		ImportMethod importMethod = component.getImportMethod();
		File deployFile = new File(approot, component.getImportAs());

		// Ensure default value for deploy method
		if (importMethod == ImportMethod.ZIP && deployMethod == DeployMethod.EXEC) {
			// It doesn't make sense to call exec on a zip
			throw new BuildException(String.format("Deployment method '%s' cannot be used with the import method '%s' for component '%s'", deployMethod.toString().toLowerCase(), importMethod.toString().toLowerCase(), deployFile));

		} else if (deployMethod == null) {
			// Missing deploy method is a problem
			throw new BuildException(String.format("Missing deployment method for component '%s'", deployFile));

		} else if (deployMethod != DeployMethod.EXEC && !deployFile.exists()) {
			// Validate that deployment already exists in approot, unless its deployment method is EXEC, in which case skip this check, since it could be an arbitrary commandline
			throw new BuildException(String.format("Cannot find component '%s'", deployFile));

		} else if (component.getDeployDir() == null && (deployMethod == DeployMethod.COPY || deployMethod ==  DeployMethod.UNZIP)) {
			// Missing deploy directory for COPY or UNZIP (not required for EXEC and NONE)
			throw new BuildException(String.format("Missing deployment directory for component '%s'", component.getImportAs()));
		}
	}

	/**
	 * Returns the script for deploying the selected role's components
	 * @param role
	 * @return
	 * @throws IOException
	 */
	private static String createComponentsDeployScript(WorkerRole role) throws IOException {

		if (role == null) {
			return null;
		}

		StringBuilder componentsScript = new StringBuilder();

		// Process components
		for (Component component : role.getComponents()) {

			// Verify deployment settings
			verifyComponentDeploySettings(component, role.getAppRootDir());

			// Create component deployment command line
			String cmdLine = createComponentDeployCommandLine(component.getImportAs(), component.getDeployMethod(), component.getDeployDir());
			
			// Output script, if any
			if(cmdLine != null) {
				componentsScript.append(cmdLine);
				componentsScript.append(newline);
			}
		}

		return componentsScript.toString();
	}

	/**
	 * Validates component import settings
	 * @param component
	 * @param approotDir
	 */
	private static void verifyComponentImportSettings(Component component, File approotDir) {
		// Validate parameters
		if (component.getImportSrc() == null && component.getImportMethod() != ImportMethod.NONE) {
			// Missing import source
			throw new BuildException("Missing import source");
		} else if (component.getImportAs() == null) {
			// Missing importAs name
			throw new BuildException(String.format("Missing import destination for component '%s'", component.getImportSrc()));
		} else if (component.getImportMethod() == null) {
			// Missing import method
			throw new BuildException(String.format("Missing import method for component '%s'", component.getImportAs()));
		}
	}

	/**
	 * Prepares component imports for all roles
	 * @throws IOException
	 */
	private void prepareComponentImports() {
		for (WorkerRole role : roles) {
			this.log(String.format("Role \"%s\": Importing components...", role.getName()));

			// Get the role's approot directory
			File approotDir = role.getAppRootDir();
			if (!approotDir.exists() || !approotDir.isDirectory()) {
				throw new BuildException("Missing approot in role " + role.getName());
			}

			for (Component c : role.getComponents()) {
				// Verify import settings
				verifyComponentImportSettings(c, approotDir);

				// Import component into approot
				importComponent(c, approotDir);

				// Verify the import worked
				verifyComponentImportSucceeded(c, approotDir);

			}

			this.log(String.format("Role \"%s\": Finished importing components", role.getName()));
		}
	}

	/**
	 * Prepares internal startup scripts for each role
	 * @throws IOException
	 */
	private void prepareStartupScripts() throws IOException {

		// Load the template file
		File templateFile = new File(this.templatesDir, String.format("%s%s%s", INTERNAL_STARTUP_SUBDIR, File.separator, INTERNAL_STARTUP_FILE_NAME));
		if (!templateFile.exists() || !templateFile.isFile()) {
			throw new BuildException(String.format("Missing internal template file '%s'", templateFile));
		} 

		String templateText = loadTextFile(templateFile);

		// Generate an internal startup script for each role
		for (WorkerRole r : roles) {
			String outputText = templateText;
			
			// Get the role's approot directory
			File approotDir = r.getAppRootDir();
			if (!approotDir.exists() || !approotDir.isDirectory()) {
				throw new IOException("Missing approot in role " + r.getName());
			}

			// Get the deploy script for the role's components
			this.log(String.format("Role \"%s\": Generating component deployment script...", r.getName()));
			String componentsScript = createComponentsDeployScript(r);

			// Get the variables script for the role
			String variablesScript = createVariablesScript(r);

			// Get the user startup script
			String userStartupScript = loadTextFile(new File(approotDir, USER_STARTUP_FILE_NAME));

			// Replace components token with script
			outputText = outputText.replace(TEMPLATE_TOKEN_COMPONENTS_SCRIPT, componentsScript);

			// Replace variables token with script
			outputText = outputText.replace(TEMPLATE_TOKEN_VARIABLES_SCRIPT, variablesScript);

			// Replace user startup token with script
			outputText = outputText.replace(TEMPLATE_TOKEN_USER_STARTUP_SCRIPT, userStartupScript);

			// Save the internal startup script in approot
			saveTextFile(new File(approotDir, INTERNAL_STARTUP_FILE_NAME), outputText);

			this.log(String.format("Role \"%s\": Created internal startup script", r.getName()));
		}
	}

	/**
	 * Import as a copy
	 * @param src
	 * @param dest
	 */
	private void importComponentAsCopy(File src, File dest) {

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
	private void importComponentAsZip(File src, File dest) {
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
		zipTask.setCompress(false); // No compression to dramatically improve build and deployment performance
		zipTask.setDestFile(dest);
		zipTask.perform();
	}

	/**
	 * Imports a component into the role's approot
	 * @param component
	 * @param approotDir
	 */
	private void importComponent(Component component, File approotDir) {
		// Ignore no import method
		ImportMethod importMethod = component.getImportMethod();
		if(importMethod == ImportMethod.NONE) {
			return;
		}
		
        String fileName = component.getImportAs();

        // Strip out command line parameters if any, but only for deploymethod=EXEC
		if(component.getDeployMethod() == DeployMethod.EXEC && fileName != null)
               fileName = fileName.split(" ")[0];
        
        File destFile = new File(approotDir, fileName);
		File srcFile = new File(component.getImportSrc());

		// If relative path, make it relative to approot
		if (!srcFile.isAbsolute()) {
			srcFile = new File(approotDir, srcFile.getPath());
		}

		if (!srcFile.exists())
			throw new BuildException(String.format("Failed to find component \"%s\"", srcFile.getPath()));

		if (importMethod == ImportMethod.COPY) {
			// Component import method: copy
			importComponentAsCopy(srcFile, destFile);

		} else if (importMethod == ImportMethod.ZIP) {
			// Component import method: zip
			importComponentAsZip(srcFile, destFile);
		}
	}

	/**
	 * Verifies component import into the approot
	 * @param component
	 * @param approot
	 */
	private void verifyComponentImportSucceeded(Component component, File approot) {
		if (component == null || approot == null) {
			throw new BuildException("Internal failure for unknown reason");
		} else if (component.getImportAs() == null) {
			this.log(String.format("\tNothing to import for component '%s'", component.getImportSrc()));
		} else if (component.getImportMethod() != ImportMethod.NONE) {
			// Confirm that the file actually got imported into the approot, unless import method is NONE
			String fileName = component.getImportAs();
			
			// Strip out command line parameters if any, but for deploymethod=EXEC only
			if(fileName != null && component.getDeployMethod() == DeployMethod.EXEC) {
				fileName = fileName.split(" ")[0]; 
			}
			
			File destFile = new File(approot, fileName);
			if (destFile.exists()) {
				this.log(String.format("\tImported as '%s' from \"%s\"", fileName, component.getImportSrc()));
			} else {
				throw new BuildException(String.format("Failed importing component '%s' as '%s' into 'approot\\%s'", component.getImportSrc(), component.getImportMethod(), fileName));
			}
		}
	}

	/**
	 * Returns the environment variable setting commandline
	 * @param name
	 * @param value
	 * @return
	 */
	private static String createVariableCommandLine(String name, String value) {
		return String.format("set %s=%s", name, value);
	}

	/**
	 * Returns the component deployment commandline
	 * @param destFile
	 * @param deployMethod
	 * @param deployPath
	 * @return
	 */
	private static String createComponentDeployCommandLine(String importedPath, DeployMethod deployMethod, String deployPath) {
		File destFile = new File(importedPath);
		String cmdLineTemplate; 
		
		switch(deployMethod)
		{
		case COPY:
			// Support for deploy method: copy - ensuring non-existent target directories get created as needed
			cmdLineTemplate = "if exist \"$destName\"\\* (echo d | xcopy /y /e \"$destName\" \"$deployPath\\$destName\") else (echo f | xcopy /y \"$destName\" \"$deployPath\\$destName\")";
			return cmdLineTemplate
					.replace("$destName", destFile.getName())
					.replace("$deployPath", deployPath);
		case UNZIP:
			// Support for deploy method: unzip return
			cmdLineTemplate = "cscript /NoLogo $utilSubdir\\$unzipFilename \"$destName\" $deployPath";
			return cmdLineTemplate
					.replace("$utilSubdir", DEFAULT_UTIL_SUBDIR)
					.replace("$unzipFilename", UNZIP_SCRIPT_FILE_NAME)
					.replace("$destName", destFile.getName())
					.replace("$deployPath", deployPath);
		case EXEC:
			// Support for deploy method: exec
			StringBuilder s = new StringBuilder("start \"Windows Azure\" ");
			
			// If deploy dir specified, treat it as a change directory request
			if(deployPath != null) {
				s.append("/D\"");
				s.append(deployPath);
				s.append("\" ");
			}
			s.append(importedPath);
			return s.toString();
		case NONE:
			// Ignore if deploymethod is NONE
			return null;
		default:
			throw new BuildException("Unsupported deployment method");
		}
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
	private static String loadTextFile(File file) throws IOException {
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
	private static void saveTextFile(File file, String text) throws IOException {
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
	private static void deleteDirectory(File directory) {
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
}
