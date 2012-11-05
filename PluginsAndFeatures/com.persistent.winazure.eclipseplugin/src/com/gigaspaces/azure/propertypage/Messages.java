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

package com.gigaspaces.azure.propertypage;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.gigaspaces.azure.propertypage.messages"; //$NON-NLS-1$
	public static String addBtnText;
	public static String credentialDlgSubscriptionId;
	public static String credentialDlgThumbprint;
	public static String credentialsDlgTitle;
	public static String credentialsNodeText;
	public static String credentialsPageId;
	public static String credPrepTitle;
	public static String editBtnText;
	public static String emoveBtnText;
	public static String error;
	public static String saveCredFailedMsg;
	public static String subscriptionColName;
	public static String subscriptionIdColName;
	public static String thumbprintColName;
	public static String loadingCred;
	public static String loadingAccountError;
	public static String loadingCredentialsError;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
