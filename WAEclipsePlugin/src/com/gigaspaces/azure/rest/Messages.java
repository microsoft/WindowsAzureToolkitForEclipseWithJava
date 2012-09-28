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

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
