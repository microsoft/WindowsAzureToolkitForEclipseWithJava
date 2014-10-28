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
package com.microsoftopentechnologies.model;

import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;

public class RoleAndEndpoint {
	private WindowsAzureRole role;
	private WindowsAzureEndpoint endPt;

	public RoleAndEndpoint() {
		super();
	}

	public RoleAndEndpoint(WindowsAzureRole role, WindowsAzureEndpoint endPt) {
		super();
		this.role = role;
		this.endPt = endPt;
	}

	public WindowsAzureRole getRole() {
		return role;
	}

	public void setRole(WindowsAzureRole role) {
		this.role = role;
	}

	public WindowsAzureEndpoint getEndPt() {
		return endPt;
	}

	public void setEndPt(WindowsAzureEndpoint endPt) {
		this.endPt = endPt;
	}
}
