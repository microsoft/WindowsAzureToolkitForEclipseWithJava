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

package com.gigaspaces.azure.wizards;

import javax.swing.event.EventListenerList;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public abstract class WindowsAzurePage extends WizardPage {

	private static final EventListenerList LISTENERS = new EventListenerList();

	protected WindowsAzurePage(String pageName) {		
		super(pageName);
	}


	
	public static void addConfigurationEventListener(ConfigurationEventListener listener) {
		LISTENERS.add(ConfigurationEventListener.class, listener);
	}

	public void removeConfigurationEventListener(ConfigurationEventListener listener) {
		LISTENERS.remove(ConfigurationEventListener.class, listener);
	}

	protected void fireConfigurationEvent(ConfigurationEventArgs config) {
		Object[] list = LISTENERS.getListenerList();
		
		for (int i = 0; i < list.length; i += 2) {
			if (list[i] == ConfigurationEventListener.class) {
				((ConfigurationEventListener) list[i + 1]).onConfigurationChanged(config);
			}
		}
	}

	protected abstract boolean validatePageComplete();

	@Override
	public abstract void createControl(Composite arg0);

	protected Combo createCombo(Composite container) {
		return createCombo(container, SWT.NONE,0);
	}

	protected Combo createCombo(Composite container, int style, int verticalIndent) {
		Combo combo = new Combo(container, style);

		GridData comboData = new GridData();
		comboData.widthHint = 200;
		comboData.heightHint = 23;
		comboData.horizontalAlignment = GridData.BEGINNING;
		comboData.grabExcessHorizontalSpace = true;	
		comboData.verticalIndent = verticalIndent;
		combo.setLayoutData(comboData);

		return combo;
	}
}
