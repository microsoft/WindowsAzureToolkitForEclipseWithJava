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

package com.microsoftopentechnologies.azurecommons.storageregistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
/**
 * Class has utility methods which will be used
 * to access centralized storage account registry.
 */
public class StorageRegistryUtilMethods {

	private static boolean duplicatePresent = false;
	private static String http = PropUtil.getValueFromFile("http");
	private static String https = PropUtil.getValueFromFile("https");

	public static boolean isDuplicatePresent() {
		return duplicatePresent;
	}

	/**
	 * Method returns array of storage account name.
	 * If storage account with same name but different blob service URL
	 * exists then it will add concatenated string of account name and
	 * blob service URL to array.
	 * @return
	 */
	public static String[] getStorageAccountNames(boolean isAutoUpload) {
		// load storage preferences in respective IDE
		// PreferenceUtilStrg.load();
		ArrayList<String> nameList = new ArrayList<String>();
		/*
		 * Get list of storage accounts from registry
		 * and prepare list of storage account names.
		 */
		List<StorageAccount> accList = StorageAccountRegistry.
				getStrgList();
		for (Iterator<StorageAccount> iterator = accList.iterator(); iterator.hasNext();) {
			StorageAccount storageAccount = (StorageAccount) iterator.next();
			nameList.add(storageAccount.getStrgName());
		}
		String [] nameArr = null;
		/*
		 * check whether registry entries
		 * with same account names
		 */
		if (chkDuplicateUsingSet(nameList)) {
			List<DuplicateAccounts> dupAccList =
					identifyDuplicates(nameList);
			// iterate over duplicate accounts list.
			for (int i = 0; i < dupAccList.size(); i++) {
				DuplicateAccounts dpAcc = dupAccList.get(i);
				/*
				 * get list of indices on which
				 * entries with same account name exists.
				 */
				List<Integer> indices = dpAcc.getIndices();
				for (int j = 0; j < indices.size(); j++) {
					// get URL of that entry
					int index = indices.get(j);
					String url = accList.get(index).getStrgUrl();
					url = getServiceEndpoint(url);
					// append service endpoint to account name
					String accAndUrl = getAccNmSrvcUrlToDisplay(nameList.get(index),
							url);
					nameList.set(index, accAndUrl);
				}
			}
		}
		/*
		 * add (none) or (auto) entry to list.
		 * Add (auto) depending on isAutoUpload variable value i.e.
		 * only if user is on server JDK configuration page
		 * and auto upload option is selected.
		 */
		if (isAutoUpload) {
			nameList.add(0, ("(auto)"));
		} else {
			nameList.add(0, ("(none)"));
		}
		nameArr = nameList.toArray(new String[nameList.size()]);
		return nameArr;
	}

	/**
	 * Method to check whether storage account registry
	 * contains entries with same account names
	 * but different URL text.
	 * @param nameList
	 * @return
	 */
	public static boolean chkDuplicateUsingSet(List<String> nameList) {
		Set<String> nameSet = new HashSet<String>(nameList);
		if (nameSet.size() < nameList.size()) {
			duplicatePresent = true;
		}
		return duplicatePresent;
	}

	/**
	 * Method checks list of storage accounts one by one,
	 * and if there are more than one entry with
	 * same account name then create object
	 * of DuplicateAccounts with name and indices
	 * where entries are found in list.
	 * @param nameList
	 * @return 
	 */
	public static List<DuplicateAccounts> identifyDuplicates(List<String> nameList) {
		List<DuplicateAccounts> dupAccList =
				new ArrayList<DuplicateAccounts>();
		for (int i = 0; i < nameList.size(); i++) {
			for (int j = i+1; j < nameList.size(); j++) {
				// check if duplicate entry present in list.
				if (i!=j && nameList.get(i).equals(nameList.get(j))) {
					DuplicateAccounts acc =
							new DuplicateAccounts(nameList.get(i));
					/*
					 * Check duplicate account list contains
					 * entry with this account name,
					 * if yes then just add index of entry to list
					 * else create new object.
					 */
					if (dupAccList.contains(acc)) {
						int accIndex = dupAccList.indexOf(acc);
						DuplicateAccounts presentAcc =
								dupAccList.get(accIndex);
						if (!presentAcc.getIndices().contains(j)) {
							presentAcc.getIndices().add(j);
						}
					} else {
						dupAccList.add(acc);
						acc.getIndices().add(i);
						acc.getIndices().add(j);
					}
				}
			}
		}
		return dupAccList;
	}

	/**
	 * Method accepts blob URL and
	 * returns name of storage account from it. 
	 * @param url
	 * @return
	 * <storage-account-name>
	 */
	public static String getAccNameFromUrl(String url) {
		String nameInUrl = null;
		if (url.startsWith(http)
				|| url.startsWith(https)) {
			int indexSlash = url.indexOf(':') + 3;
			if (url.contains(".")
					&& url.indexOf('.', indexSlash) >= indexSlash) {
				nameInUrl = url.substring(indexSlash,
						url.indexOf('.'));
			}
		}
		return nameInUrl;
	}

	/**
	 * Method returns service endpoint from URL.
	 * @param url
	 * @return
	 * <blob-service-endpoint>
	 */
	public static String getServiceEndpoint(String url) {
		String serviceEnPt = null;
		int dotIndex = url.indexOf('.') + 1;
		if (url.contains("/")) {
			int slashIndex = url.indexOf('/', dotIndex);
			if (slashIndex >= dotIndex)
				serviceEnPt = url.substring(dotIndex,
						slashIndex);
		}
		return serviceEnPt;
	}

	/**
	 * Method returns string which has concatenation of
	 * storage account name and service endpoint for
	 * display purpose. 
	 * @param accName
	 * @param serviceUrl
	 * @return String
	 * <storage-account-name> (<blob-service-endpoint>)
	 */
	public static String getAccNmSrvcUrlToDisplay(
			String accName, String serviceUrl) {
		String accAndUrl = accName + " (" + serviceUrl + ")";
		return accAndUrl;
	}

	/**
	 * Method returns substring of URL consisting
	 * of storage account name and service endpoint.
	 * @param url
	 * @return
	 * <storage-account-name>.<blob-service-endpoint>
	 */
	public static String getSubStrAccNmSrvcUrlFrmUrl(String url) {
		if (WAEclipseHelperMethods.isContainsBlobEndpointUrl(url)) {
			String serviceUrl = url.substring(url.indexOf(':') + 3,
					url.indexOf('/', url.indexOf('.')));
			return serviceUrl;
		} else {
			return url;
		}
	}

	/**
	 * Method returns service endpoint URL from blob URL.
	 * @param url
	 * @return
	 * http[s]://<storage-account-name>.<blob-service-endpoint>/
	 */
	public static String getServiceEndpointUrl(String url) {
		String serviceUrl = url.substring(0,
				url.indexOf('/', url.indexOf('.')) + 1);
		return serviceUrl;
	}

	/**
	 * Method returns index of storage account from registry,
	 * which has same access key.
	 * @param accessKey
	 * @return
	 */
	public static int getStrgAccIndexAsPerKey(String accessKey) {
		List<StorageAccount> accList = StorageAccountRegistry.
				getStrgList();
		for (int i = 0; i < accList.size(); i++) {
			if (accessKey.equals(accList.get(i).getStrgKey())) {
				return i;
			}
		}
		return -1;
	}
}
