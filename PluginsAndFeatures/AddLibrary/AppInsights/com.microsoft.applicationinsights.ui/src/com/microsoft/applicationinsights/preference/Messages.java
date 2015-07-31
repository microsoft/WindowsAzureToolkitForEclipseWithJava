/**
* Copyright Microsoft Corp.
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
package com.microsoft.applicationinsights.preference;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.microsoft.applicationinsights.preference.messages";
	public static String imprtAzureLbl;
	public static String resrcName;
	public static String instrKey;
	public static String btnNewLbl;
	public static String btnAddLbl;
	public static String btnDtlsLbl;
	public static String btnRmvLbl;
	public static String name;
	public static String sub;
	public static String resGrp;
	public static String region;
	public static String appTtl;
	public static String unknown;
	public static String addKeyTtl;
	public static String addKeyMsg;
	public static String newKeyTtl;
	public static String newKeyMsg;
	public static String key;
	public static String sameKeyErrMsg;
	public static String sameNameErrMsg;
	public static String rsrcRmvMsg;
	public static String resCreateErrMsg;
	public static String noSubErrMsg;
	public static String noResGrpErrMsg;
	public static String getSubIdErrMsg;
	public static String getValuesErrMsg;
	public static String keyErrMsg;
	public static String importErrMsg;
	public static String loadErrMsg;
	public static String saveErrMsg;
	public static String rsrcUseMsg;
	public static String signInErr;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
