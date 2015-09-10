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