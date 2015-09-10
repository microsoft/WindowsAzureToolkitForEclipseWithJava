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
import java.util.List;
import java.util.Vector;

import com.microsoftopentechnologies.windowsazure.tools.cspack.*;
import com.microsoftopentechnologies.windowsazure.tools.cspack.Utils;

import org.apache.tools.ant.BuildException;

/**
 * 
 * A Class representing workerrole element within windowsazurepackage task
 * 
 */
public class WorkerRole {
	private String name;
	private File approotDir;
	private Vector<Component> components;
	private Vector<StartupEnv> variables;
	private WindowsAzurePackage wapackage;
	
	/**
	 * Constructor
	 */
	public WorkerRole(WindowsAzurePackage wapackage) {
		components = new Vector<Component>();
		variables = new Vector<StartupEnv>();
		this.wapackage = wapackage;
	}

	/**
	 * Returns the WindowsAzurePackage that's the parent of this role
	 * @return
	 */
	public WindowsAzurePackage getPackage() {
		return wapackage;
	}
	
	/**
	 * Returns the variables for this role
	 * @return
	 */
	public Vector<StartupEnv> getVariables() {
		return variables;
	}

	/**
	 * Creates a new instance of an environment variable
	 * @return New instance of Env
	 */
	public StartupEnv createStartupEnv() {
		StartupEnv variable = new StartupEnv();
		variables.addElement(variable);
		return variable;
	}

	/**
	 * Returns the components of this role
	 * @return
	 */
	public Vector<Component> getComponents() {
		return components;
	}

	/**
	 * Creates a new instance of component
	 * @return New instance of Component
	 */
	public Component createComponent() {
		Component component = new Component(this);
		components.addElement(component);
		return component;
	}

	/**
	 * Sets name attribute
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the name attribute
	 * @return
	 */
	String getName() {
		return this.name;
	}

	/**
	 * Sets approotdir attribute
	 * @param appRootDir
	 */
	public void setAppRootDir(File appRootDir) {
		this.approotDir = appRootDir;
	}

	/**
	 * Gets approotdir attribute
	 * @return
	 */
	File getAppRootDir() {
		return this.approotDir;
	}

	/**
	 * Prepares component imports for all roles
	 * @throws IOException
	 */
	public void importComponents() {
		// Get the role's approot directory
		File approotDir = getAppRootDir();
		if (!approotDir.exists() || !approotDir.isDirectory()) {
			throw new BuildException("Missing approot in role " + getName());
		}

		for (Component c : getComponents()) {
			// Verify import settings
			c.verifyImportSettings();

			// Import component into approot
			c.doImport();

			// Verify the import worked
			c.verifyImportSucceeded();
		}
	}
	
	/**
	 * Returns the script for deploying the selected role's components
	 * @return
	 * @throws IOException
	 */
	private String createComponentsDeployScript() throws IOException {
		StringBuilder componentsScript = new StringBuilder();
		String cmdLine;
		
		// Process components
		for (Component component : getComponents()) {

			// Verify deployment settings
			component.verifyDeploySettings();

			// Create component cloud download command line
			if(null != (cmdLine = component.createDownloadCommandLine())) {
				componentsScript.append(cmdLine + WindowsAzurePackage.newline);
			}
			
			// Create component deployment command line
			if(null != (cmdLine = component.createComponentDeployCommandLine())) {
				componentsScript.append(cmdLine + WindowsAzurePackage.newline);
			}
		}
		return componentsScript.toString();
	}
	
	/**
	 * Prepares environment variable settings for the role
	 * @throws IOException
	 */
	private String createVariablesScript() throws IOException {
		StringBuilder variablesScript = new StringBuilder();

		for (StartupEnv v : getVariables()) {
			String cmdLine = v.createCommandLine(wapackage.getPackageType() == PackageType.cloud);
			variablesScript.append(cmdLine + WindowsAzurePackage.newline);
		}

		return variablesScript.toString();
	}
	
	/**
	 * Prepares internal startup scripts for each role
	 * @throws IOException
	 */
	public void createStartupScript(String templateText) throws IOException {
		if(templateText == null) {
			throw new IOException("Missing template file");
		}

		// Generate an internal startup script for each role
		String outputText = templateText;
			
		// Get the role's approot directory
		File approotDir = getAppRootDir();
		if (!approotDir.exists() || !approotDir.isDirectory()) {
			throw new IOException("Missing approot in role " + getName());
		}

		// Get the deploy script for the role's components
		String componentsScript = createComponentsDeployScript();

		// Get the variables script for the role
		String variablesScript = createVariablesScript();

		// Get the user startup script
		String userStartupScript = WindowsAzurePackage.loadTextFile(new File(approotDir, WindowsAzurePackage.USER_STARTUP_FILE_NAME));

		// Replace components token with script
		outputText = outputText.replace(WindowsAzurePackage.TEMPLATE_TOKEN_COMPONENTS_SCRIPT, componentsScript);

		// Replace variables token with script
		outputText = outputText.replace(WindowsAzurePackage.TEMPLATE_TOKEN_VARIABLES_SCRIPT, variablesScript);

		// Replace user startup token with script
		outputText = outputText.replace(WindowsAzurePackage.TEMPLATE_TOKEN_USER_STARTUP_SCRIPT, userStartupScript);

		// Save the internal startup script in approot
		WindowsAzurePackage.saveTextFile(new File(approotDir, WindowsAzurePackage.INTERNAL_STARTUP_FILE_NAME), outputText);
	}
	
	/**
	 * Ensures the presence of the storage client library in util
	 */
	public void includeStorageClientLibrary() {
		// Microsoft.WindowsAzure.Storage.dll (4.3.0)
		includeLibrary(WindowsAzurePackage.STORAGEDLL_FILENAME);
		// Its six dependencies
		includeLibrary(WindowsAzurePackage.DATA_SERV_FILENAME);
		includeLibrary(WindowsAzurePackage.DATA_EDM_FILENAME);
		includeLibrary(WindowsAzurePackage.DATA_ODATA_FILENAME);
		includeLibrary(WindowsAzurePackage.JSON_FILENAME);
		includeLibrary(WindowsAzurePackage.SPATIAL_FILENAME);
		includeLibrary(WindowsAzurePackage.CONFIGURATION_FILENAME);
	}

	public void includeLibrary(String fileNameToInclude) {
		File utilDirectory = new File(getAppRootDir(), WindowsAzurePackage.DEFAULT_UTIL_SUBDIR);
		File fileToInclude = new File(utilDirectory, fileNameToInclude);
		if (fileToInclude.exists() && fileToInclude.isFile()) {
			// Library already included
			return;
		}
		File srcFile;
		if (WindowsAzurePackage.IS_WINDOWS) {
			File cachingFolder = new File(String.format("%s%s%s%s%s", wapackage.getSdkDir(), File.separatorChar, "plugins", File.separatorChar, "Caching"));
			srcFile = new File(cachingFolder, fileNameToInclude);
			if (!srcFile.exists() || !srcFile.isFile()) {
				// Library cannot be found in SDK
				throw new BuildException(String.format(
						"The required %s cannot be found. Make sure you have installed the latest Azure SDK for .NET",
						fileNameToInclude));
			} else {
				wapackage.copyFile(srcFile, fileToInclude);
			}
		} else {
			// copy dll from 'sdkKit' directory - for linux and Mac
			try {
				String jarName = String.format("%s%s%s", wapackage.getProjectDir(), File.separatorChar, ".cspack.jar");
				String sourceFolder = "sdkKit/plugins/Caching";
				List<String> fileEntries = Utils.getJarEntries(jarName, sourceFolder.replace("\\", "/"));
				for (String entryName : fileEntries) {
					if (entryName.equals(fileNameToInclude)) {
						Utils.copyJarEntry("/" + entryName, fileToInclude);
					}
				}
			} catch (IOException e) {
				throw new BuildException(String.format("The required %s cannot be found.", fileNameToInclude), e);
			}
		}
	}
}