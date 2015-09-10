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

import java.util.Iterator;
import java.util.Map;

import com.interopbridges.tools.windowsazure.WindowsAzureCacheExpirationPolicy;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureNamedCache;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;


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
