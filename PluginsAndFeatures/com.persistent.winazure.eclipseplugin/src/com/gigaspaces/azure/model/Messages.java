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

package com.gigaspaces.azure.model;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	private static final String BUNDLE_NAME =
			"com.gigaspaces.azure.model.messages"; //$NON-NLS-1$
	public static String busy;
	public static String busyRole;
	public static String certificateFormat;
	public static String certificateFormat2;
	public static String creatingRole;
	public static String creatingVm;
	public static String cyclingVm;
	public static String deletingSt;
	public static String deletingVm;
	public static String deployingSt;
	public static String deploymentIdFormat;
	public static String failedStartingVm;
	public static String init;
	public static String invalidAffinityGroup;
	public static String invalidConfiguration;
	public static String invalidDescription;
	public static String invalidLabelLength;
	public static String invalidLabel;
	public static String invalidLabelValue;
	public static String invalidLocation;
	public static String invalidName;
	public static String invalidPackageUrl;
	public static String invalidServiceName;
	public static String modelFactoryErr;
	public static String production;
	public static String ready;
	public static String readyRole;
	public static String restartVm;
	public static String roleStateUnknown;
	public static String runningSt;
	public static String runningTransitionSt;
	public static String staging;
	public static String startingRole;
	public static String startingSt;
	public static String startingVm;
	public static String stopped;
	public static String stoppedVm;
	public static String stopping;
	public static String stoppingRole;
	public static String stoppingVm;
	public static String suspendedSt;
	public static String suspendedTransitionSt;
	public static String suspendingSt;
	public static String unresponsive;
	public static String unresponsiveRole;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
