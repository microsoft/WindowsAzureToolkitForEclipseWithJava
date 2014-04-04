/**
 * Copyright 2014 Microsoft Open Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoftopentechnologies.wacommon.utils;


import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
    private static final String BUNDLE_NAME =
    		"com.microsoftopentechnologies.wacommon.utils.messages";
    
    /* Common messages - start */
	public static String pluginFolder;
	public static String waCommonFolderID;
	public static String encFileName;
	public static String encFileEntry;
	/* Common messages - end */
	
    /* EncUtilHelper messages - start */
	public static String encUtilErrMsg;
    /* EncUtilHelper messages - end */
	
	/* Base64 messages - start */
	public static String base64InvldStr;
	/* Base64 messages - end */
	public static String nameNtErMsg;
	public static String nameGetErMsg;
	public static String pubUrlNtErMsg;
	public static String pubUrlGetErMsg;
	public static String portUrlGetErMsg;
	public static String blbUrlNtErMsg;
	public static String blbUrlGetErMsg;
	public static String mngUrlNtErMsg;
	public static String mngUrlGetErMsg;
	public static String parseErMsg;
	public static String inValArg;
	public static String err;
	public static String prefFileName;
	public static String bundleName;
	public static String nameSetErMsg;
	public static String saveErMsg;
	public static String sdkLibBundleName;
	public static String SDKLocErrMsg;
	public static String sdkLibBaseJar;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        super();
    }
}
