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
package com.microsoft.applicationinsights.preference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.microsoftopentechnologies.azurecommons.storageregistry.StorageRegistryUtilMethods;


public class ApplicationInsightsResourceRegistry {
	/**
	 * List of application insights resources,
	 * to be in sync with preference file data.
	 */
	private static List<ApplicationInsightsResource> appInsightsResrcList =
			new ArrayList<ApplicationInsightsResource>();

	public static List<ApplicationInsightsResource> getAppInsightsResrcList() {
		return appInsightsResrcList;
	}

	/**
	 * Method returns array of application insights resources names.
	 * If resource with same name but different subscription name
	 * exists then it will add concatenated string of resource name and subscription name to array.
	 * @return
	 */
	public static String[] getResourcesNamesToDisplay() {
		ArrayList<String> nameList = getResourcesNames();
		List<ApplicationInsightsResource> resourceList = getAppInsightsResrcList();
		String [] nameArr = null;
		// check whether registry entries with same resource names exist
		if (StorageRegistryUtilMethods.chkDuplicateUsingSet(nameList)) {
			List<ApplicationInsightsResourceWithSameName> sameResourceNameList =
					identifyDuplicates(nameList);
			// iterate over same resource name list.
			for (int i = 0; i < sameResourceNameList.size(); i++) {
				ApplicationInsightsResourceWithSameName sameResource = sameResourceNameList.get(i);
				// get list of indices on which entries with same resource name exists.
				List<Integer> indices = sameResource.getIndices();
				for (int j = 0; j < indices.size(); j++) {
					int index = indices.get(j);
					String subName = resourceList.get(index).getSubscriptionName();
					// append subscription name to resource name
					String resourceAndSub = concatenateRsrcAndSubName(nameList.get(index), subName);
					nameList.set(index, resourceAndSub);
				}
			}
		}
		nameArr = nameList.toArray(new String[nameList.size()]);
		return nameArr;
	}

	/**
	 * Method returns array of application insights resources names.
	 * @return
	 */
	public static ArrayList<String> getResourcesNames() {
		ArrayList<String> nameList = new ArrayList<String>();
		// Get list of application insights resources and prepare list of resources names.
		List<ApplicationInsightsResource> resourceList = getAppInsightsResrcList();
		for (Iterator<ApplicationInsightsResource> iterator = resourceList.iterator(); iterator.hasNext();) {
			ApplicationInsightsResource resource = (ApplicationInsightsResource) iterator.next();
			nameList.add(resource.getResourceName());
		}
		return nameList;
	}

	/**
	 * Method checks list of application insights resources one by one,
	 * and if there are more than one entry with
	 * same resource name then create object
	 * of ApplicationInsightsResourceWithSameName with name and indices
	 * where entries are found in list.
	 * @param nameList
	 * @return 
	 */
	public static List<ApplicationInsightsResourceWithSameName> identifyDuplicates(List<String> nameList) {
		List<ApplicationInsightsResourceWithSameName> sameResourceNameList =
				new ArrayList<ApplicationInsightsResourceWithSameName>();
		for (int i = 0; i < nameList.size(); i++) {
			for (int j = i+1; j < nameList.size(); j++) {
				// check if duplicate entry present in list.
				if (i!=j && nameList.get(i).equals(nameList.get(j))) {
					ApplicationInsightsResourceWithSameName resource =
							new ApplicationInsightsResourceWithSameName(nameList.get(i));
					/*
					 * Check same resource name list contains
					 * entry with this resource name,
					 * if yes then just add index of entry to list
					 * else create new object.
					 */
					if (sameResourceNameList.contains(resource)) {
						int resourceIndex = sameResourceNameList.indexOf(resource);
						ApplicationInsightsResourceWithSameName presentResource =
								sameResourceNameList.get(resourceIndex);
						if (!presentResource.getIndices().contains(j)) {
							presentResource.getIndices().add(j);
						}
					} else {
						sameResourceNameList.add(resource);
						resource.getIndices().add(i);
						resource.getIndices().add(j);
					}
				}
			}
		}
		return sameResourceNameList;
	}

	/**
	 * Method returns string which has concatenation of
	 * application insights resource's name and subscription name
	 * for display purpose.
	 * @param resourceName
	 * @param subName
	 * @return String
	 * <sapplication-insight-resource-name> (<subscription name>)
	 */
	public static String concatenateRsrcAndSubName(String resourceName, String subName) {
		String resourceAndSub = resourceName + " (" + subName + ")";
		return resourceAndSub;
	}

	/**
	 * Method returns index of application insight resource from registry,
	 * which has same instrumentation key.
	 * @param key
	 * @return
	 */
	public static int getResourceIndexAsPerKey(String key) {
		List<ApplicationInsightsResource> resourceList = getAppInsightsResrcList();
		for (int i = 0; i < resourceList.size(); i++) {
			if (key.equals(resourceList.get(i).getInstrumentationKey())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Method returns instrumentation key of application insight resource of specific index.
	 * @param index
	 * @return
	 */
	public static String getKeyAsPerIndex(int index) {
		List<ApplicationInsightsResource> resourceList = getAppInsightsResrcList();
		// to do check index exists or not
		return resourceList.get(index).getInstrumentationKey();
	}

	/**
	 * Get list of application insight resources associated with particular subscription.
	 * @param subId
	 * @return
	 */
	public static List<ApplicationInsightsResource> getResourceListAsPerSub(String subId) {
		List<ApplicationInsightsResource> subWiseList = new ArrayList<ApplicationInsightsResource>();
		if (subId != null && !subId.isEmpty()) {
			List<ApplicationInsightsResource> resourceList = getAppInsightsResrcList();
			for (ApplicationInsightsResource resource : resourceList) {
				if (resource.getSubscriptionId().equalsIgnoreCase(subId)) {
					subWiseList.add(resource);
				}
			}
		}
		return subWiseList;
	}
}
