/**
* Copyright 2014 Microsoft Open Technologies, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.microsoftopentechnologies.deploy.wizard;

import java.util.EventObject;

import com.microsoftopentechnologies.messagehandler.PropUtil;

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
