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
package com.persistent.builder;

import org.eclipse.osgi.util.NLS;
/**
 * Stores common strings.
 */
public final class Messages extends NLS {
    private static final String BUNDLE_NAME =
    		"com.persistent.builder.messages";
    public static String bldErrMsg;
    public static String addBldErrMsg;
    public static String crtErrMsg;
    public static String jarErrMsg;
    public static String flNtFdErrMsg;
    public static String impErrMsg;
    public static String basePath;
    public static String pathErrMsg;
    public static String rfrshErrTtl;
    public static String rfrshErrMsg;
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
