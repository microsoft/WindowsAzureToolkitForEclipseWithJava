/**
* Copyright 2014 Microsoft Open Technologies, Inc.
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
package com.microsoftopentechnologies.roleoperations;


import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.exception.AzureCommonsException;
import com.microsoftopentechnologies.messagehandler.PropUtil;
import com.microsoftopentechnologies.model.RoleAndEndpoint;
import com.microsoftopentechnologies.util.WAEclipseHelperMethods;


public class WAREndpointsUtilMethods {
	private static String dlgInvdEdPtName2 = PropUtil.getValueFromFile("dlgInvdEdPtName2");
	private static String rngErrMsg = PropUtil.getValueFromFile("rngErrMsg");
	private static String dlgPortInUse = PropUtil.getValueFromFile("dlgPortInUse");
	private static String dashErrMsg = PropUtil.getValueFromFile("dashErrMsg");
	private static String enPtAlphNuMsg = PropUtil.getValueFromFile("enPtAlphNuMsg");
	private static String dbgPort = PropUtil.getValueFromFile("dbgPort");
	/**
	 * End point range's minimum value.
	 */
	private final static int RANGE_MIN = 1;
	/**
	 * End point range's maximum value.
	 */
	private final static int RANGE_MAX = 65535;

	public static boolean modifyPublicPortCheckRange(String modifiedVal, int dashCnt) {

		// Check for valid range 1 to 65535
		Boolean isPortValid = true;
		try {
			/*
			 * If public port contains '-'
			 * then split string and
			 * get two integer values out of it.
			 * else directly check for value.
			 */
			if (dashCnt == 1) {
				String[] range = modifiedVal.split("-");
				int rngStart = Integer.
						parseInt(range[0]);
				int rngEnd = Integer.parseInt(range[1]);
				if (!(rngStart >= RANGE_MIN
						&& rngStart <= RANGE_MAX
						&& rngEnd >= RANGE_MIN
						&& rngEnd <= RANGE_MAX)) {
					isPortValid = false;
				}
			} else {
				int port = Integer.parseInt(modifiedVal);
				if (!(port >= RANGE_MIN
						&& port <= RANGE_MAX)) {
					isPortValid = false;
				}
			}
		} catch (NumberFormatException e) {
			isPortValid = false;
		}
		return isPortValid;
	}

	/**
	 * Handles the modification of endpoint's public port.
	 *
	 * @param endpoint : the endpoint being modified.
	 * @param modifiedVal : new value for endpoint's public port.
	 * @throws AzureCommonsException 
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public static WindowsAzureEndpoint modifyPublicPort(WindowsAzureEndpoint endpoint,
			Object modifiedVal,
			int dashCntPar,
			WindowsAzureRole windowsAzureRole)
					throws WindowsAzureInvalidProjectOperationException, AzureCommonsException {
		/*
		 * Check only one '-' is present,
		 * while specifying range for Instance end point.
		 * If end point is Internal or Input then,
		 * it will not satisfy if condition
		 * and dash count will be zero.
		 */
		int dashCnt = dashCntPar;
		if (endpoint.getEndPointType().
				equals(WindowsAzureEndpointType.InstanceInput)) {
			if (dashCnt > 1) {
				throw new AzureCommonsException(dashErrMsg);
			}
		}
		if (dashCnt <= 1) {
			// Check for valid range 1 to 65535
			Boolean isPortValid = modifyPublicPortCheckRange(
					modifiedVal.toString(), dashCnt);
			if (isPortValid) {
				// Validate port
				boolean isValid = windowsAzureRole.isValidEndpoint(
						endpoint.getName(),
						endpoint.getEndPointType(),
						endpoint.getPrivatePort(),
						modifiedVal.toString());
				if (isValid) {
					endpoint.setPort(
							modifiedVal.toString());
				} else {
					throw new AzureCommonsException(dlgPortInUse);
				}
			} else {
				throw new AzureCommonsException(rngErrMsg);
			}
		}
		return endpoint;
	}

	public static boolean modifyPrivatePortCheckRange(
			String modifiedVal,
			int dashCnt,
			WindowsAzureEndpoint endpoint)
					throws WindowsAzureInvalidProjectOperationException {
		// Check for valid range 1 to 65535
		Boolean isPortValid = true;
		try {
			/*
			 * If public port contains '-'
			 * then split string and
			 * get two integer values out of it.
			 * else directly check for value.
			 */
			if (dashCnt == 1) {
				String[] range = modifiedVal.split("-");
				int rngStart = Integer.
						parseInt(range[0]);
				int rngEnd = Integer.parseInt(range[1]);
				if (!(rngStart >= RANGE_MIN
						&& rngStart <= RANGE_MAX
						&& rngEnd >= RANGE_MIN
						&& rngEnd <= RANGE_MAX)) {
					isPortValid = false;
				}
			} else {
				// no dash
				if (!((modifiedVal.isEmpty()
						|| modifiedVal.equalsIgnoreCase("*"))
						&& (endpoint.getEndPointType().
								equals(WindowsAzureEndpointType.Internal)
								|| endpoint.getEndPointType().
								equals(WindowsAzureEndpointType.Input)))) {
					int port = Integer.
							parseInt(modifiedVal);
					if (!(port >= RANGE_MIN
							&& port <= RANGE_MAX)) {
						isPortValid = false;
					}
				}
			}
		} catch (NumberFormatException e) {
			isPortValid = false;
		}
		return isPortValid;
	}

	/**
	 * Handles the modification of endpoint's private port.
	 *
	 * @param endpoint : the endpoint being modified.
	 * @param modifiedVal : new value for endpoint's private port.
	 * @throws AzureCommonsException 
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public static RoleAndEndpoint modifyPrivatePort(WindowsAzureEndpoint endpoint,
			Object modifiedVal,
			int dashCntPar,
			WindowsAzureRole windowsAzureRole)
					throws WindowsAzureInvalidProjectOperationException, AzureCommonsException {
		/*
		 * Check only one '-' is present,
		 * while specifying range for Internal end point.
		 * If end point is Instance or Input then,
		 * it will not satisfy if condition
		 * and dash count will be zero.
		 */
		int dashCnt = dashCntPar;
		if (endpoint.getEndPointType().
				equals(WindowsAzureEndpointType.Internal)) {
			if (dashCnt > 1) {
				throw new AzureCommonsException(dashErrMsg);
			}
		}
		if (dashCnt <= 1) {
			// Check for valid range 1 to 65535
			Boolean isPortValid = modifyPrivatePortCheckRange(modifiedVal.toString(), dashCnt, endpoint);
			if (isPortValid) {
				// Validate port
				String privatePort = modifiedVal.toString();
				if (privatePort.isEmpty()
						|| privatePort.equalsIgnoreCase("*")) {
					privatePort = null;
				}
				boolean isValid = windowsAzureRole.isValidEndpoint(
						endpoint.getName(),
						endpoint.getEndPointType(),
						privatePort,
						endpoint.getPort());
				if (isValid) {
					boolean canChange = true;
					boolean isDebugEnabled = false;
					boolean isSuspended = false;
					WindowsAzureEndpoint endPt =
							windowsAzureRole.getDebuggingEndpoint();
					/*
					 * check if the endpoint is associated with debug,
					 * if yes then set isDebugEnabled to true and
					 * store the suspended mode value. Disable debug endpoint
					 * and then enable it with the modified endpoint.
					 */
					if (endPt != null
							&& endpoint.getName().equalsIgnoreCase(
									endPt.getName())) {
						if (privatePort == null) {
							canChange = false;
							throw new AzureCommonsException(dbgPort);
						} else {
							isSuspended = windowsAzureRole.
									getStartSuspended();
							windowsAzureRole.setDebuggingEndpoint(null);
							isDebugEnabled = true;
						}
					}
					if (canChange) {
						endpoint.setPrivatePort(privatePort);
						if (isDebugEnabled) {
							windowsAzureRole.
							setDebuggingEndpoint(endpoint);
							windowsAzureRole.
							setStartSuspended(isSuspended);
						}
					}
				} else {
					throw new AzureCommonsException(dlgPortInUse);
				}
			} else {
				throw new AzureCommonsException(rngErrMsg);
			}
		}
		RoleAndEndpoint obj = null;
		if (endpoint != null && windowsAzureRole != null) {
			obj = new RoleAndEndpoint(windowsAzureRole, endpoint);
		}
		return obj;
	}

	/**
	 * Handles the modification of endpoint name.
	 *
	 * @param endpoint : the endpoint being modified.
	 * @param modifiedVal : new value for endpoint name.
	 * @throws AzureCommonsException 
	 * @throws WindowsAzureInvalidProjectOperationException .
	 */
	public static WindowsAzureEndpoint modifyName(WindowsAzureEndpoint endpoint,
			Object modifiedVal,
			WindowsAzureRole windowsAzureRole)
					throws WindowsAzureInvalidProjectOperationException, AzureCommonsException {
		// Validate endpoint name
		String endptName = modifiedVal.toString();
		/*
		 * Check endpoint name contain
		 * alphanumeric and underscore characters only.
		 * Starts with alphabet.
		 */
		if (WAEclipseHelperMethods.isAlphaNumericUnderscore(endptName)) {
			boolean isValid = windowsAzureRole
					.isAvailableEndpointName(endptName,
							endpoint.getEndPointType());
			/*
			 * Check already used endpoint name is given.
			 */
			if (isValid || endptName.equalsIgnoreCase(endpoint.getName())) {
				endpoint.setName(modifiedVal.toString());
			} else {
				throw new AzureCommonsException(dlgInvdEdPtName2);
			}
		} else {
			throw new AzureCommonsException(enPtAlphNuMsg);
		}
		return endpoint;
	}
}
