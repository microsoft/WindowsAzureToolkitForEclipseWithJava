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

import java.io.File;
import java.io.IOException;
import java.util.Vector;

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
		File utilDirectory = new File(getAppRootDir(), WindowsAzurePackage.DEFAULT_UTIL_SUBDIR);
		File storageClientDestFile = new File(utilDirectory, WindowsAzurePackage.STORAGEDLL_FILENAME);
		File storageClientSrcFile = new File(new File(wapackage.getSdkDir(), WindowsAzurePackage.STORAGEDLL_SUBDIR), WindowsAzurePackage.STORAGEDLL_FILENAME);
		if(storageClientDestFile.exists() && storageClientDestFile.isFile()) {
			// Library already included
			return;
		} else if(!storageClientSrcFile.exists() || !storageClientSrcFile.isFile()) {
			// Library cannot be found in SDK
			throw new BuildException("The required StorageClient.dll cannot be found. Make sure you have installed the latest Windows Azure SDK for .NET");
		} else {
			wapackage.copyFile(storageClientSrcFile, storageClientDestFile);
		}
	}
}