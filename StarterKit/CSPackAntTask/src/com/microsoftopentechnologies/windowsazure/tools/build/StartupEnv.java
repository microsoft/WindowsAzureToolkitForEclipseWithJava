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

/**
 * 
 * A Class representing an environment variable setting
 * 
 */
public class StartupEnv {
	private String name;
	private String value;
	private String cloudValue;

	/**
	 * Sets name attribute
	 * @param name
	 *            Name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets name attribute
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets value attribute
	 * @param value
	 *            Value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Gets value attribute
	 * @return
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Sets cloudvalue attribute
	 * @param value
	 *            Value
	 */
	public void setCloudValue(String value) {
		this.cloudValue = value;
	}

	/**
	 * Gets cloudvalue attribute
	 * @return
	 */
	public String getCloudValue() {
		if(this.cloudValue != null) {
			return this.cloudValue;
		} else {
			return this.value;
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
	
	/**
	 * Returns the environment variable setting command line
	 * @return
	 */
	public String createCommandLine(boolean forCloud) {
		if(!forCloud) {
			return String.format("set %s=%s", this.name, this.getValue());
		} else {
			return String.format("set %s=%s", this.name, this.getCloudValue());			
		}
	}
	
	public String createCommandLine() {
		return createCommandLine(false);
	}
}