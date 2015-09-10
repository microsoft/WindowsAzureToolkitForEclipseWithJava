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

public class CacheDialogUtilMethods {
	private static String expPolAbs = PropUtil.getValueFromFile("expPolAbs");
	private static String expPolNvrExp = PropUtil.getValueFromFile("expPolNvrExp");
	private static String expPolSlWn = PropUtil.getValueFromFile("expPolSlWn");
	private static String cachEndPtName = PropUtil.getValueFromFile("cachEndPtName");
	private static String cachNameErrMsg = PropUtil.getValueFromFile("cachNameErrMsg");
	private static String chNameAlphNuMsg = PropUtil.getValueFromFile("chNameAlphNuMsg");
	private static String cachSetErrMsg = PropUtil.getValueFromFile("cachSetErrMsg");
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
	/**
	 * Validates the Minutes to live attribute of named cache.
	 * Value must be numeric and should be at least 0
	 * @param minToLive
	 * @return Boolean
	 */
	public static Boolean validateMtl(String minToLive) {
		Boolean isVallidMtl = false;
		try {
			int mtl = Integer.parseInt(minToLive);
			if (mtl > 0) {
				isVallidMtl = true;
			} else {
				isVallidMtl = false;
			}
		} catch (NumberFormatException e) {
			isVallidMtl = false;
		}
		return isVallidMtl;
	}

	/**
	 * Mapping of expiration policies stored in project manager object
	 * to values shown on UI.
	 * @param cache
	 * @return
	 */
	public static String getExpPolStr(WindowsAzureNamedCache cache) {
		String expPolStr;
		if (cache.getExpirationPolicy().
				equals(WindowsAzureCacheExpirationPolicy.
						NEVER_EXPIRES)) {
			expPolStr = expPolNvrExp;
		} else if (cache.getExpirationPolicy().
				equals(WindowsAzureCacheExpirationPolicy.
						ABSOLUTE)) {
			expPolStr = expPolAbs;
		} else {
			expPolStr = expPolSlWn;
		}
		return expPolStr;
	}

	public static WindowsAzureCacheExpirationPolicy getExpPolObject(String expPolCmbTxt) {
		/*
		 * Mapping of expiration policies shown on UI
		 * to actual values stored in project manager object
		 */
		WindowsAzureCacheExpirationPolicy expPol;
		if (expPolCmbTxt.equals(expPolNvrExp)) {
			expPol = WindowsAzureCacheExpirationPolicy.
					NEVER_EXPIRES;
		} else if (expPolCmbTxt.equals(expPolAbs)) {
			expPol = WindowsAzureCacheExpirationPolicy.ABSOLUTE;
		} else {
			expPol = WindowsAzureCacheExpirationPolicy.
					SLIDING_WINDOW;
		}
		return expPol;
	}

	public static void setCacheAttributes(WindowsAzureNamedCache namedCache,
			String expPolCmbTxt,
			boolean backup,
			WindowsAzureCacheExpirationPolicy expPol,
			String mtlTxt) throws WindowsAzureInvalidProjectOperationException {
		namedCache.setBackups(backup);
		namedCache.setExpirationPolicy(expPol);
		/*
		 * Check if expiration policy is
		 * NEVER_EXPIRES then set value to zero
		 * else set value of minutes to live
		 * property specified in text box.
		 */
		if (expPolCmbTxt.equals(expPolNvrExp)) {
			namedCache.setMinutesToLive(0);
		} else {
			namedCache.setMinutesToLive(Integer.
					parseInt(mtlTxt));
		}
	}

	/**
	 * Validates the name of cache.
	 * @param name
	 * @return Boolean
	 * @throws AzureCommonsException 
	 */
	public static Boolean isValidName(String name,
			Map<String, WindowsAzureNamedCache> cacheMap,
			boolean isEdit,
			String cacheName) throws AzureCommonsException {
		boolean retVal = true;
		StringBuffer strBfr = new StringBuffer(name);
		try {
			boolean isValidName = true;
			/*
			 * Check cache name contains alphanumeric characters,
			 * underscore and starts with alphabet only.
			 */
			if (WAEclipseHelperMethods.
					isAlphaNumericUnderscore(name)) {
				for (Iterator<String> iterator =
						cacheMap.keySet().iterator();
						iterator.hasNext();) {
					String key = iterator.next();
					if (key.equalsIgnoreCase(
							strBfr.toString())) {
						isValidName = false;
						break;
					}
				}
				if (!isValidName
						&& !(isEdit && strBfr.toString().
								equalsIgnoreCase(cacheName))) {
					retVal = false;
					throw new AzureCommonsException(cachNameErrMsg);
				}
			} else {
				retVal = false;
				throw new AzureCommonsException(chNameAlphNuMsg);
			}
		} catch (Exception e) {
			retVal = false;
			throw new AzureCommonsException(cachSetErrMsg);
		}
		return retVal;
	}

	/**
	 * Validates the port number of named cache.
	 * Positive integer between 1 to 65535 is allowed.
	 * @param port
	 * @return Boolean
	 * @throws AzureCommonsException 
	 */
	public static Boolean validatePort(String port,
			Map<String, WindowsAzureNamedCache> cacheMap,
			String cacheName,
			boolean isEdit,
			String cacheTxt,
			WindowsAzureRole windowsAzureRole) throws AzureCommonsException {
		Boolean isValidPortRng = false;
		Boolean isValidPort = true;
		try {
			int portNum = Integer.parseInt(port);
			if (RANGE_MIN <= portNum
					&& portNum <= RANGE_MAX) {
				/*
				 * Check whether end point of same name or port
				 * exist already.
				 */
				WindowsAzureNamedCache namedCache =
						cacheMap.get(cacheName);
				Boolean isValidEp = windowsAzureRole.isValidEndpoint(
						String.format("%s%s", cachEndPtName,
								cacheTxt),
								WindowsAzureEndpointType.Internal,
								port, "");
				if (isValidEp) {
					isValidPortRng = true;
				} else if (!isValidEp
						&& (isEdit && namedCache.getEndpoint().
								getPrivatePort().equals(port))) {
					isValidPortRng = true;
				} else {
					isValidPort = false;
				}
			} else {
				isValidPortRng = false;
			}
		} catch (NumberFormatException e) {
			isValidPortRng = false;
		} catch (Exception e) {
			isValidPortRng = false;
		}
		if (!isValidPort) {
			throw new AzureCommonsException(dlgPortInUse);
		} else if (!isValidPortRng) {
			throw new AzureCommonsException(rngErrMsg);
		}
		return isValidPortRng;
	}
}
