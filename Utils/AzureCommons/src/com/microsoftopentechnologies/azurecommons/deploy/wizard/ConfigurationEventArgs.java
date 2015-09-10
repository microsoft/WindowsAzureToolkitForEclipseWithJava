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

package com.microsoftopentechnologies.azurecommons.deploy.wizard;

import java.util.EventObject;

import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;

public class ConfigurationEventArgs extends EventObject {

	private static final long serialVersionUID = -5309150169158237067L;
	public static final String SUBSCRIPTION = PropUtil.getValueFromFile("subscriptionConfig");
	public static final String HOSTED_SERVICE = PropUtil.getValueFromFile("hostedServiceConfig");
	public static final String STORAGE_ACCOUNT = PropUtil.getValueFromFile("storageAccountConfig");
	public static final String REMOTE_DESKTOP = PropUtil.getValueFromFile("remoteDesktopConfig");
	public static final String DEPLOY_FILE = PropUtil.getValueFromFile("deployFileconfig");
	public static final String DEPLOY_CONFIG_FILE = PropUtil.getValueFromFile("deployConfigFileConfig");
	public static final String DEPLOY_STATE = PropUtil.getValueFromFile("deployStateConfig");
	public static final String STORAGE_ACCESS_KEY = PropUtil.getValueFromFile("credentialConfig");
	public static final String DEPLOY_MODE = PropUtil.getValueFromFile("deployModeConfig");
	public static final String UN_PUBLISH = PropUtil.getValueFromFile("unpubchk");
	public static final String CERTIFICATES = PropUtil.getValueFromFile("cert");
	public static final String CONFIG_HTTPS_LINK = PropUtil.getValueFromFile("displayHttpsLink");
	
	private final String key;
	private final Object value;
	
	public ConfigurationEventArgs(Object source, String key, Object value) {
		super(source);		
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
}
