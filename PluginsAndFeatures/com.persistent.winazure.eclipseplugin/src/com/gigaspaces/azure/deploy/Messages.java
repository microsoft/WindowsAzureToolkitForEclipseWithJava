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
 */package com.gigaspaces.azure.deploy;

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
	public static String deletePackage;
	public static String startEvent;
	public static String successEvent;
	public static String failureEvent;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
