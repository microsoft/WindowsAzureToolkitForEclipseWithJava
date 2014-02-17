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
package com.microsoftopentechnologies.wacommon.storageregistry;

import java.util.ArrayList;
import java.util.List;
/**
 * Model class to store information
 * if we have storage accounts with same name
 * but different service endpoint URL in
 * storage registry.
 */
public class DuplicateAccounts {
	String accountName;
	List<Integer> indices = new ArrayList<Integer>();
	
	public DuplicateAccounts() {
		super();
	}

	public DuplicateAccounts(String accountName) {
		super();
		this.accountName = accountName;
	}

	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public List<Integer> getIndices() {
		return indices;
	}
	public void setIndices(List<Integer> indices) {
		this.indices = indices;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		DuplicateAccounts dpAcc = (DuplicateAccounts) obj;
		return (accountName != null
				&& accountName.equals(dpAcc.getAccountName()));
	}
}
