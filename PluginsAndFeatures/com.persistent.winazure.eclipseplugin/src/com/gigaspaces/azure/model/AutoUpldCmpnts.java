/**
 * Copyright 2013 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gigaspaces.azure.model;

import java.util.ArrayList;
import java.util.List;
/**
 * Model class for storing which components
 * got modified i.e. updating cloudurl and cloudkey
 * of components having upload method as "auto".
 * Purpose is to again restore auto settings
 * after project build is completed.
 */
public class AutoUpldCmpnts {
	String roleName;
	List<Integer> cmpntIndices = new ArrayList<Integer>();

	public AutoUpldCmpnts() {
		super();
	}

	public AutoUpldCmpnts(String roleName) {
		super();
		this.roleName = roleName;
	}

	public String getRoleName() {
		return roleName;
	}

	public List<Integer> getCmpntIndices() {
		return cmpntIndices;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public void setCmpntIndices(List<Integer> cmpntIndices) {
		this.cmpntIndices = cmpntIndices;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		AutoUpldCmpnts cmpnt = (AutoUpldCmpnts) obj;
		return (roleName != null
				&& roleName.equals(cmpnt.getRoleName()));
	}
}
