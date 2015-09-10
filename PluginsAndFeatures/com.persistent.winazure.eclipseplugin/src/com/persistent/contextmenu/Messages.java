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
package com.persistent.contextmenu;
import org.eclipse.osgi.util.NLS;

/**
 * Stores common strings.
 */
public final class Messages extends NLS {
	private static final String BUNDLE_NAME =
			"com.persistent.contextmenu.messages";
	public static String genCmdId;
	public static String cachCmdId;
	public static String cmpntCmdId;
	public static String dbgCmdId;
	public static String endPtCmdId;
	public static String envVarCmdId;
	public static String ldBalCmdId;
	public static String lclStrCmdId;
	public static String srvConfCmdId;
	public static String genPgId;
	public static String cachPgId;
	public static String cmpntPgId;
	public static String dbgPgId;
	public static String endPtPgId;
	public static String envVarPgId;
	public static String ldBalPgId;
	public static String lclStrPgId;
	public static String srvConfPgId;
	public static String cmhIdWinAz;
	public static String certCmdId;
	public static String certPgId;
	public static String sslCmdId;
	public static String sslPgId;
	public static String cmpntFileName;
	public static String twoProjMsg;
	public static String noProjMsg;
	public static String noJdkSrvMsg;
	public static String title;
	public static String projErr;
	public static String cntxtMenuErr;

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
