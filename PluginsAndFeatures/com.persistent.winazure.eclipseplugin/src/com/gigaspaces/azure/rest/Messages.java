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
package com.gigaspaces.azure.rest;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.gigaspaces.azure.rest.messages"; //$NON-NLS-1$
	public static String acceptCharsetConst;
	public static String authorization;
	public static String authrztnConst;
	public static String contentLen;
	public static String contentLenConst;
	public static String contentType;
	public static String contentTypeConst;
	public static String date;
	public static String dateTimeFormat;
	public static String delete;
	public static String error;
	public static String get;
	public static String head;
	public static String invalidStrgAcc;
	public static String invalidStorageKey;
	public static String msDateConst;
	public static String msRequestIdConst;
	public static String msVersion1;
	public static String msVersion2;
	public static String msVersion3;
	public static String msVersionConst;
	public static String post;
	public static String put;
	public static String requestId;
	public static String restAPIException;
	public static String timeZone;
	public static String utfFormat;
	public static String version;
	public static String xMsVersion;
	public static String xMsVersion2;
	public static String xMsVersion3;
	public static String dnsNameTaken;
	public static String deserializtnErr;
	public static String upExcpTrce;
	public static String delExcpTrce;
	public static String upErr;
	public static String delErr;
	public static String upTime;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
