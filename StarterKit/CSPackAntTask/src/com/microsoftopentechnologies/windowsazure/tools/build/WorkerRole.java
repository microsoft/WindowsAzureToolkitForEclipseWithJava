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
import java.util.Vector;

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

	/**
	 * Constructor
	 */
	public WorkerRole() {
		components = new Vector<Component>();
		variables = new Vector<StartupEnv>();
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
		Component component = new Component();
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

}