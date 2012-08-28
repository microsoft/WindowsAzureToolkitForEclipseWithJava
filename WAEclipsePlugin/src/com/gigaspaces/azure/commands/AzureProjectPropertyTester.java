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

package com.gigaspaces.azure.commands;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceNode;

import com.gigaspaces.azure.propertypage.Messages;
import com.persistent.ui.propertypage.WAProjectNature;

public class AzureProjectPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if ("nature".equalsIgnoreCase(property)) {
			IProject obj = (IProject) receiver;
			try {
				return obj.getNature(WAProjectNature.NATURE_ID) != null;
			} catch (CoreException e) {
				return false;
			}
		} else if ("isSubscriptionPerfNode".equalsIgnoreCase(property)
				&& receiver instanceof PreferenceNode) {
			PreferenceNode node = (PreferenceNode) receiver;
			
			if (node.getId().equalsIgnoreCase(Messages.credentialsPageId))
				return true;

		}

		return false;
	}
}
