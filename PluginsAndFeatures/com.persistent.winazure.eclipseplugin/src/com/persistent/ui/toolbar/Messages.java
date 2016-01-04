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
package com.persistent.ui.toolbar;

import org.eclipse.osgi.util.NLS;
/**
 * Stores common strings.
 */
public final class Messages extends NLS {
	 private static final String BUNDLE_NAME =
			 "com.persistent.ui.toolbar.messages";
	 public static String bldErrTtl;
	 public static String bldErrMsg;
	 public static String waEmulator;
	 public static String runEmltrErrTtl;
	 public static String runEmltrErrMsg;
	 public static String bldCldErrTtl;
	 public static String bldCldErrMsg;
	 public static String rstEmltrErrTtl;
	 public static String rstEmuErrMsg;
	 public static String dplyFldErrMsg;
	 public static String runJobTtl;
	 public static String cldJobTtl;
	 public static String pluginFolder;
	 public static String pluginId;
	 public static String pWizStarterKit;
	 public static String rstEmCmd;
	 public static String ioErrMsg;
	 public static String waWizardId;
	 public static String wzrdCrtErMsg;
	 public static String errTtl;
	 public static String getPrefUrlErMsg;
	 public static String newCertMsg;
	 public static String newCertErrTtl;
	 public static String azureLibPropName;
	 public static String noLocalServerMsg;
	 public static String noLocalJDKMsg;
	 public static String instSDK;

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
