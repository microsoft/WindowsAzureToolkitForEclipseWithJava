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
package com.persistent.waroles.contextmenu;
import org.eclipse.osgi.util.NLS;

/**
 * Stores common strings.
 */
public final class Messages extends NLS {
	private static final String BUNDLE_NAME =
			"com.persistent.waroles.contextmenu.messages";
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
