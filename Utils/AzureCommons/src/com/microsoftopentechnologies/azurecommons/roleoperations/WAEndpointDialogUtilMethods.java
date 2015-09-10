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

package com.microsoftopentechnologies.azurecommons.roleoperations;

import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;

public class WAEndpointDialogUtilMethods {
	private final static int RANGE_MIN = 1;
	private final static int RANGE_MAX = 65535;
	private static String auto = "(auto)";

	/**
	 * Returns combined public port range as a single string.
	 * Applicable only when endpoint type is InstanceInput.
	 * @return String
	 */
	public static String combinePublicPortRange(String pubPortStart,
			String pubPortEnd,
			String comboTypeTxt) {
		String portRange;
		if (comboTypeTxt.equalsIgnoreCase(
				WindowsAzureEndpointType.
				InstanceInput.toString())) {
			/*
			 * Always combine both values as
			 * Instance endpoint stores public port
			 * in the format of range always.
			 */
			if (pubPortEnd.isEmpty()) {
				pubPortEnd = pubPortStart;
			}
			portRange = String.format("%s-%s",
					pubPortStart, pubPortEnd);
		} else {
			portRange = pubPortStart;
		}
		return portRange;
	}

	/**
	 * Returns combined private port range as a single string.
	 * Applicable only when endpoint type is Internal.
	 * @return String
	 */
	public static String combinePrivatePortRange(String prvPortStart,
			String prvPortEnd,
			String comboTypeTxt) {
		String prvPortRange;
		if (comboTypeTxt.equalsIgnoreCase(
				WindowsAzureEndpointType.
				Internal.toString())) {
			prvPortRange = prvPortStart;
			/*
			 * If user has given range's end value
			 * then only combine both values, and make range
			 * otherwise only pass single value.
			 * (For Single value & range we use different tags)
			 */
			if (!prvPortEnd.isEmpty()) {
				prvPortRange = String.format("%s-%s",
						prvPortRange, prvPortEnd);
			}
		} else {
			prvPortRange = prvPortStart;
		}
		return prvPortRange;
	}

	/**
	 * Method checks if port is within valid range 1 to 65535 or not.
	 * @param type
	 * @return boolean
	 */
	public static boolean isValidPortRange(WindowsAzureEndpointType type,
			String txtPrivatePort,
			String txtPrivatePortRangeEnd,
			String txtPublicPort,
			String txtPublicPortRangeEnd) {
		boolean isValid = true;
		try {
			if (type.equals(WindowsAzureEndpointType.Internal)) {
				if (txtPrivatePort.equalsIgnoreCase(auto)) {
					if (!txtPrivatePortRangeEnd.isEmpty()) {
						isValid = false;
					}
				} else {
					// (auto) is not given
					int rngStart = Integer.
							parseInt(txtPrivatePort);
					if (rngStart >= RANGE_MIN
							&& rngStart <= RANGE_MAX) {
						if (!txtPrivatePortRangeEnd.equals("")) {
							int rngEnd = Integer.
									parseInt(txtPrivatePortRangeEnd);
							if (!(rngEnd >= RANGE_MIN
									&& rngEnd <= RANGE_MAX)) {
								isValid = false;
							}
						}
					} else {
						isValid = false;
					}
				}
			} else if (type.equals(WindowsAzureEndpointType.Input)) {
				int pubPort = Integer.parseInt(txtPublicPort);
				/*
				 * If private port is auto then check for public port validity
				 * else both
				 */
				if (txtPrivatePort.equalsIgnoreCase(auto)) {
					if (!(pubPort >= RANGE_MIN
							&& pubPort <= RANGE_MAX)) {
						isValid = false;
					}
				} else {
					int priPort = Integer.parseInt(txtPrivatePort);
					if (!(pubPort >= RANGE_MIN
							&& pubPort <= RANGE_MAX
							&& priPort >= RANGE_MIN
							&& priPort <= RANGE_MAX)) {
						isValid = false;
					}
				}
			} else if (type.equals(WindowsAzureEndpointType.InstanceInput)) {
				int priPort = Integer.parseInt(txtPrivatePort);
				if (priPort >= RANGE_MIN
						&& priPort <= RANGE_MAX) {
					int rngStart = Integer.parseInt(txtPublicPort);
					if (rngStart >= RANGE_MIN
							&& rngStart <= RANGE_MAX) {
						if (!txtPublicPortRangeEnd.equals("")) {
							int rngEnd = Integer.parseInt(
									txtPublicPortRangeEnd);
							if (!(rngEnd >= RANGE_MIN
									&& rngEnd <= RANGE_MAX)) {
								isValid = false;
							}
						}
					} else {
						isValid = false;
					}
				} else {
					isValid = false;
				}
			}
		} catch (NumberFormatException e) {
			isValid = false;
		}
		return isValid;
	}

	/**
	 * Method checks if text box contains '-' character.
	 * @param type
	 * @return boolean
	 */
	public static boolean isDashPresent(WindowsAzureEndpointType type,
			String txtPrivatePort,
			String txtPrivatePortRangeEnd,
			String txtPublicPort,
			String txtPublicPortRangeEnd) {
		boolean isPresent = false;
		if (type.equals(WindowsAzureEndpointType.Internal)) {
			if (txtPrivatePort.contains("-")
					|| txtPrivatePortRangeEnd.contains("-")) {
				isPresent = true;
			} else {
				isPresent = false;
			}
		} else if (type.equals(WindowsAzureEndpointType.InstanceInput)) {
			if (txtPublicPort.contains("-")
					|| txtPublicPortRangeEnd.contains("-")) {
				isPresent = true;
			} else {
				isPresent = false;
			}
		}
		return isPresent;
	}
}
