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
package com.gigaspaces.azure.wizards;

import javax.swing.event.EventListenerList;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.microsoftopentechnologies.azurecommons.deploy.wizard.ConfigurationEventArgs;
import com.microsoftopentechnologies.azurecommons.deploy.wizard.ConfigurationEventListener;

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

	protected Combo createCombo(Composite container,
			int style, int verticalIndent, int horiAlign, int width) {
		Combo combo = new Combo(container, style);
		GridData comboData = new GridData();
		if (width > 0) {
			comboData.widthHint = width;
		}
		comboData.heightHint = 23;
		comboData.horizontalAlignment = horiAlign;
		comboData.grabExcessHorizontalSpace = true;
		comboData.verticalIndent = verticalIndent;
		combo.setLayoutData(comboData);
		return combo;
	}
}
