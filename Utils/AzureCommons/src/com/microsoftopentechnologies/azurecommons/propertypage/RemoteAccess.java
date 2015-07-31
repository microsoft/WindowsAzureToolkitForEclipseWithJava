/**
* Copyright Microsoft Corp.
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

