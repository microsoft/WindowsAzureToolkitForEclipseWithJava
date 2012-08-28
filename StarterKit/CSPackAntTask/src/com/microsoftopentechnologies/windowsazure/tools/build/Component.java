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

import java.io.File;

import org.apache.tools.ant.BuildException;

/**
 * 
 * A Class representing component element within workerrole element
 * 
 */
public class Component {
	private String importSrc;
	private String importAs;
	private ImportMethod importMethod = ImportMethod.NONE;
	private DeployMethod deployMethod = DeployMethod.NONE;
	private String deployDir;
	
	/**
	 * Constructor
	 */
	public Component() {
	}

	/**
	 * Sets importas attribute
	 * 
	 * @param importAs
	 */
	public void setImportAs(String importAs) {
		this.importAs = importAs;
	}

	/**
	 * Returns the name of the file or directory that the component will be
	 * imported as, relative to approot
	 * 
	 * @return
	 */
	public String getImportAs() {
		if(this.importAs != null && !this.importAs.isEmpty()) {
			// If import destination has been specified, then return it; treat empty string as null
			return this.importAs;
		} else if(this.importMethod == ImportMethod.NONE) {
			// If no import method and no import destination, then return import source
			return this.importSrc;
		} else if(this.importMethod == ImportMethod.COPY) {
			// If import method is Copy and no import destination, then assume the import source's file name 
			File srcPath = new File(this.importSrc);
			return srcPath.getName();
		} else if(this.importMethod == ImportMethod.ZIP) {
			// If import method is Zip and no import destination, then assume the import souce's file name plus .zip
			File srcPath = new File(this.importSrc);
			return srcPath.getName() + ".zip";
		} else {
			return null;
		}
	}

	/**
	 * Sets deploymethod attribute
	 * 
	 * @param deployMethod
	 */
	public void setDeployMethod(String deployMethod) {
		if (deployMethod == null) {
			throw new BuildException("Missing deployment method");
		}

		if(deployMethod.equalsIgnoreCase("exec")) {
			this.deployMethod = DeployMethod.EXEC;
		} else if(deployMethod.equalsIgnoreCase("copy")) {
			this.deployMethod = DeployMethod.COPY;
		} else if(deployMethod.equalsIgnoreCase("unzip")) {
			this.deployMethod = DeployMethod.UNZIP;
		} else if(deployMethod.equalsIgnoreCase("none")) {
			this.deployMethod = DeployMethod.NONE;
		} else {
			throw new BuildException("Unsupported deployment method: " + deployMethod);			
		}
	}

	/**
	 * Gets deploymethod setting
	 * 
	 * @return
	 */
	public DeployMethod getDeployMethod() {
		return this.deployMethod;
	}

	/**
	 * Sets importmethod attribute
	 * 
	 * @param importMethod
	 */
	public void setImportMethod(String importMethod) {
		if (importMethod == null) {
			throw new BuildException("Missing import method");
		} else if(importMethod.equalsIgnoreCase("none")) {
			this.importMethod = ImportMethod.NONE;
		} else if(importMethod.equalsIgnoreCase("copy")) {
			this.importMethod = ImportMethod.COPY;
		} else if(importMethod.equalsIgnoreCase("zip")) {
			this.importMethod = ImportMethod.ZIP;
		} else if(importMethod.equalsIgnoreCase("auto")) {
			this.importMethod = ImportMethod.AUTO;
		} else {
			throw new BuildException("Unsupported import method: " + importMethod);
		}
	}

	/**
	 * Gets importmethod setting
	 * 
	 * @return
	 */
	public ImportMethod getImportMethod() {
		return this.importMethod;
	}

	/**
	 * Sets importsrc attribute
	 * 
	 * @param srcpath
	 */
	public void setImportSrc(String srcPath) {
		this.importSrc = srcPath;
	}

	/**
	 * Gets the importsrc attribute
	 * 
	 * @return
	 */
	public String getImportSrc() {
		return this.importSrc;
	}

	/**
	 * Sets deployto attribute
	 * 
	 * @param srcpath
	 */
	public void setDeployDir(String deployPath) {
		if(deployPath.isEmpty())
			deployPath = null;
		this.deployDir = deployPath;
	}

	/**
	 * Gets the deployto attribute
	 * 
	 * @return
	 */
	public String getDeployDir() {
		if(this.deployDir != null && !this.deployDir.isEmpty()) {
			return this.deployDir;
		} else if(this.deployMethod == DeployMethod.UNZIP) {
			// If UNZIP is the deployment method, assume current directory as the default
			return ".";
		} else {
			return null;
		}
	}
	
	/** Allows the use of an arbitrary type attribute setting on a component by external tools, but the Ant extension currently 
	 * has no functionality associated with it
	 * 
	 * @param type
	 */
	public void setType(String type) {
		return;
	}
}