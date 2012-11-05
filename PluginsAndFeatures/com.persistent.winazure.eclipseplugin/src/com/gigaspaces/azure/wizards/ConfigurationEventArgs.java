/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.gigaspaces.azure.wizards;

import java.util.EventObject;

public class ConfigurationEventArgs extends EventObject {

	private static final long serialVersionUID = -5309150169158237067L;
	public static final String SUPSCRIPTION = Messages.subscriptionConfig;
	public static final String HOSTED_SERVICE = Messages.hostedServiceConfig;
	public static final String STORAGE_ACCOUNT = Messages.storageAccountConfig;
	public static final String REMOTE_DESKTOP = Messages.remoteDesktopConfig;
	public static final String DEPLOY_FILE =Messages.deployFileconfig;
	public static final String DEPLOY_CONFIG_FILE = Messages.deployConfigFileConfig;
	public static final String DEPLOY_STATE = Messages.deployStateConfig;
	public static final String STORAGE_ACCESS_KEY = Messages.credentialConfig;
	public static final String DEPLOY_MODE = Messages.deployModeConfig;
	
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
