/**
* Copyright 2012 Persistent Systems Ltd.
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
