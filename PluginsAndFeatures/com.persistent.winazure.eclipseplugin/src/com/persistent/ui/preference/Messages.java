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
package com.persistent.ui.preference;
import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME =
			"com.persistent.ui.preference.messages";
	public static String strColName;
	public static String strColSrvEnpt;
	public static String addBtnText;
	public static String editBtnText;
	public static String removeBtnText;
	public static String edtStrTtl;
	public static String addStrTtl;
	public static String strTxt;
	public static String strNmMsg;
	public static String strDlgNmTxt;
	public static String strAccKeyTxt;
	public static String strUrlTxt;
	public static String strKeyMsg;
	public static String strUrlMsg;
	public static String accRmvTtl;
	public static String accRmvMsg;
	public static String errTtl;
	public static String namelnErMsg;
	public static String nameRxErMsg;
	public static String urlErMsg;
	public static String urlPreErrMsg;
	public static String winAzMsg;
	public static String http;
	public static String https;
	public static String blobEnPt;
	public static String keyErrTtl;
	public static String keyErrMsg;
	public static String actvStLbl;
	public static String urlLbl;
	public static String prtlLbl;
	public static String mngtLbl;
	public static String blbUrlLbl;
	public static String pubSetLbl;
	public static String prefFileName;
	public static String prefFileMsg;
	public static String edtPrefTtl;
	public static String getPrefErMsg;
	public static String setPrefErMsg;
	public static String strAccDlgImg;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	/**
	 * Constructor.
	 */
	private Messages() {
		super();
	}
}