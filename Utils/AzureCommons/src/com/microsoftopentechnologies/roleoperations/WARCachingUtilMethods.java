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

import java.util.Iterator;
import java.util.Map;

import com.interopbridges.tools.windowsazure.WindowsAzureCacheExpirationPolicy;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureNamedCache;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.exception.AzureCommonsException;
import com.microsoftopentechnologies.messagehandler.PropUtil;
import com.microsoftopentechnologies.util.WAEclipseHelperMethods;


public class WARCachingUtilMethods {
	private final static int DEFAULT_TTL = 10;
	private static String cachNameEmpMsg = PropUtil.getValueFromFile("cachNameEmpMsg");
	private static String cachNameErrMsg = PropUtil.getValueFromFile("cachNameErrMsg");
	private static String chNameAlphNuMsg = PropUtil.getValueFromFile("chNameAlphNuMsg");
	private static String cachEndPtName = PropUtil.getValueFromFile("cachEndPtName");
	private static String dlgPortInUse = PropUtil.getValueFromFile("dlgPortInUse");
	private static String rngErrMsg = PropUtil.getValueFromFile("rngErrMsg");
	/**
	 * End point range's minimum value.
	 */
	private final static int RANGE_MIN = 1;
	/**
	 * End point range's maximum value.
	 */
	private final static int RANGE_MAX = 65535;

	public static boolean validateMtl(String mtlTxt) {
		Boolean isVallidMtl = false;
		int mtl = 0;
		if (mtlTxt.isEmpty()) {
			isVallidMtl = false;
		} else {
			try {
				mtl = Integer.parseInt(mtlTxt);
				if (mtl > 0) {
					isVallidMtl = true;
				} else {
					isVallidMtl = false;
				}
			} catch (NumberFormatException e) {
				isVallidMtl = false;
			}
		}
		return isVallidMtl;
	}

	public static WindowsAzureNamedCache modifyExpirationPol(
			WindowsAzureNamedCache cache,
			String modifiedValStr)
					throws WindowsAzureInvalidProjectOperationException {
		// NEVER_EXPIRES
		if (modifiedValStr.equals("0")) {
			cache.setExpirationPolicy(
					WindowsAzureCacheExpirationPolicy.
					NEVER_EXPIRES);
			/*
			 * If expiration policy is set to
			 * NEVER_EXPIRES then set MTL to zero
			 */
			cache.setMinutesToLive(0);
		}
		// ABSOLUTE
		else if (modifiedValStr.equals("1")) {
			cache.setExpirationPolicy(
					WindowsAzureCacheExpirationPolicy.
					ABSOLUTE);
			if (cache.getMinutesToLive() == 0) {
				cache.setMinutesToLive(DEFAULT_TTL);
			}
		}
		// SLIDING_WINDOW
		else if (modifiedValStr.equals("2")) {
			cache.setExpirationPolicy(
					WindowsAzureCacheExpirationPolicy.
					SLIDING_WINDOW);
			if (cache.getMinutesToLive() == 0) {
				cache.setMinutesToLive(DEFAULT_TTL);
			}
		}
		return cache;
	}

	/**
	 * Handles the modification of named cache name.
	 * @param cache : the cache being modified
	 * @param modifiedVal : new value for cache name
	 * @throws WindowsAzureInvalidProjectOperationException
	 * @throws AzureCommonsException 
	 */
	public static WindowsAzureNamedCache modifyCacheName(
			WindowsAzureNamedCache cache,
			Object modifiedVal,
			Map<String, WindowsAzureNamedCache> mapCache)
					throws WindowsAzureInvalidProjectOperationException, AzureCommonsException {
		if (modifiedVal.toString().isEmpty()) {
			throw new AzureCommonsException(cachNameEmpMsg);
		} else {
			StringBuffer strBfr =
					new StringBuffer(modifiedVal.toString());
			boolean isValidName = true;
			/*
			 * Check cache name contains alphanumeric characters,
			 * underscore and starts with alphabet only.
			 */
			if (WAEclipseHelperMethods.
					isAlphaNumericUnderscore(
							modifiedVal.toString())) {
				for (Iterator<String> iterator =
						mapCache.keySet().iterator();
						iterator.hasNext();) {
					String key = iterator.next();
					if (key.equalsIgnoreCase(strBfr.toString())) {
						isValidName = false;
						break;
					}
				}
				if (isValidName
						|| modifiedVal.toString().equalsIgnoreCase(
								cache.getName())) {
					cache.setName(modifiedVal.toString());
				} else {
					throw new AzureCommonsException(cachNameErrMsg);
				}
			} else {
				throw new AzureCommonsException(chNameAlphNuMsg);
			}
		}
		return cache;
	}

	/**
	 * Handles the modification of named cache port.
	 * @param cache : the cache being modified
	 * @param modifiedVal : new value for port
	 * @throws WindowsAzureInvalidProjectOperationException
	 * @throws AzureCommonsException 
	 */
	public static WindowsAzureNamedCache modifyPort(
			WindowsAzureNamedCache cache,
			Object modifiedVal,
			WindowsAzureRole wARole)
					throws WindowsAzureInvalidProjectOperationException, AzureCommonsException {
		Boolean isValidPort = false;
		String portTxt = modifiedVal.toString();
		if (portTxt.isEmpty()) {
			isValidPort = false;
		} else {
			try {
				int portNum = Integer.parseInt(portTxt);
				if (RANGE_MIN <= portNum
						&& portNum <= RANGE_MAX) {
					/*
					 * Check whether end point of same name or port
					 * exist already.
					 */
					Boolean isValidEp = wARole.isValidEndpoint(
							String.format("%s%s", cachEndPtName,
									cache.getName()),
									WindowsAzureEndpointType.Internal,
									portTxt, "");
					if (isValidEp) {
						isValidPort = true;
					} else {
						throw new AzureCommonsException(dlgPortInUse);
					}
				} else {
					isValidPort = false;
				}
			} catch (NumberFormatException e) {
				isValidPort = false;
			}
		}
		if (isValidPort) {
			cache.getEndpoint().setPrivatePort(portTxt);
		} else {
			throw new AzureCommonsException(rngErrMsg);
		}
		return cache;
	}
}
