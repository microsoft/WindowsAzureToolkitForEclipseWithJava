package com.microsoftopentechnologies.wacommon.adauth;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME =
			"com.microsoftopentechnologies.wacommon.adauth.messages";
	public static String browserErr;
	public static String ideErr;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
