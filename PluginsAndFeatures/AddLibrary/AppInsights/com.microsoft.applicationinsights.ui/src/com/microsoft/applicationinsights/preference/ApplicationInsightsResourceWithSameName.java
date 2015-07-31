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
import java.util.List;

public class ApplicationInsightsResourceWithSameName {
	String resourceName;
	List<Integer> indices = new ArrayList<Integer>();

	public ApplicationInsightsResourceWithSameName() {
		super();
	}

	public ApplicationInsightsResourceWithSameName(String resourceName) {
		super();
		this.resourceName = resourceName;
	}

	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
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
		ApplicationInsightsResourceWithSameName resource =
				(ApplicationInsightsResourceWithSameName) obj;
		return (resourceName != null
				&& resourceName.equals(resource.getResourceName()));
	}
}
