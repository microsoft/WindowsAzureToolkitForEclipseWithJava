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

package com.microsoftopentechnologies.azurecommons.propertypage;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;


public class RemoteAccess {
	static boolean isPathChanged = false;
	private static String remAccNameNull = PropUtil.getValueFromFile("remAccNameNull");
	private static String remAccExpDateNull = PropUtil.getValueFromFile("remAccExpDateNull");
	private static String remAccInvldPath = PropUtil.getValueFromFile("remAccInvldPath");

	/**
	 * Validates the expiry date.
	 * Expiry date should be greater than current date.
	 *
	 * @param expDate
	 * @param formatter
	 * @return
	 * @throws ParseException
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	public static boolean validateExpDate(String expDate, DateFormat formatter)
			throws ParseException,
			WindowsAzureInvalidProjectOperationException {
		Date userSelected;
		boolean isValid = true;
		long todaySeconds, userDateSeconds;
		userSelected = formatter.parse(expDate);
		userDateSeconds = userSelected.getTime();
		GregorianCalendar todayCal = new GregorianCalendar();
		todaySeconds = todayCal.getTimeInMillis();
		if ((userDateSeconds - todaySeconds) < 0) {
			isValid = false;
		}
		return isValid;
	}

	public static void okToLeave(String newPath,
			boolean isRemoteEnabled,
			String userName,
			String expDate) throws AzureCommonsException {
		File cerFile = new File(newPath);
		if (isRemoteEnabled && userName.isEmpty()) {
			throw new AzureCommonsException(remAccNameNull);
		} else if (isRemoteEnabled && expDate.isEmpty()) {
			throw new AzureCommonsException(remAccExpDateNull);
		} else if (isRemoteEnabled
				&& (!cerFile.exists() || (!(newPath.endsWith(".cer") || newPath.endsWith(".CER"))))) {
			throw new AzureCommonsException(remAccInvldPath);
		}
	}
}

