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

package com.gigaspaces.azure.deploy;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME =
			"com.gigaspaces.azure.deploy.messages"; //$NON-NLS-1$

	public static String createHostedService;

	public static String createStorageAccount;
		
	public static String activityView;

	public static String certificateElem;

	public static String certificateNameAttr;

	public static String certificatePath;

	public static String certificatesElem;

	public static String certificatesPath;	

	public static String configurationSettingPath;
	public static String configurationSettingsElem;

	public static String configurationSettingsPath;

	public static String cspkgName;

	public static String cspkgUrl;

	public static String failed;
	public static String inProgress;
	
	public static String dateFormatEventArgs;

	public static String dateFormat;

	public static String deplCantOpenView;
	public static String deplCompleted;
	public static String deplConfigRdp;
	public static String deplDesc;
	public static String deplError;
	public static String deplErrorCode;
	public static String deplErrorMessage;
	public static String deplFailed;
	public static String deplFailedConfigRdp;
	public static String deplHttpStatus;
	public static String deplId;
	public static String deplOfFailed;
	public static String deplStatus;
	public static String deplUploadCert;
	public static String deplYes;
	public static String remoteAccessAccountEncryptedPassword;

	public static String remoteAccessAccountExpiration;

	public static String remoteAccessAccountUsername;

	public static String remoteAccessEnabledSetting;

	public static String remoteAccessEnabledSettingVal;

	public static String remoteAccessPasswordEncryption;

	public static String remoteAccessPasswordEncryptionPath;

	public static String remoteFormarderEnabledSetting;

	public static String remoteFormarderEnabledSettingVal;

	public static String rolePath;

	public static String settingElem;

	public static String settingNameAttr;

	public static String settingValueAttr;

	public static String stoppingMsg;

	public static String eclipseDeployContainer;

	public static String thumbprint;

	public static String thumbprintAlg;

	public static String thumbprintAttr;

	public static String toStringFormat;

	public static String undeployCompletedMsg;

	public static String undeployFailedMsg;

	public static String undeployMsg;

	public static String undeployProgressMsg;

	public static String utfFormat;

	public static String yesProp;

	public static String succeeded;

	public static String uploadPackage;

	public static String uploadingServicePackage;

	public static String creatingDeployment;

	public static String waitingForDeployment;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
