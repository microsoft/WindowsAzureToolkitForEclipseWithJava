/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.gigaspaces.azure.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class RoleInstanceList implements Iterable<RoleInstance> {

	private List<RoleInstance> roleInstanceList = new ArrayList<RoleInstance>();
	
	@Override
	public Iterator<RoleInstance> iterator() {	
		return roleInstanceList.iterator();
	}

	@XmlElement(name="RoleInstance")
	public List<RoleInstance> getRoleInstanceList() {
		return roleInstanceList;
	}

	public void setRoleInstanceList(List<RoleInstance> roleInstanceList) {
		this.roleInstanceList = roleInstanceList;
	}

}
