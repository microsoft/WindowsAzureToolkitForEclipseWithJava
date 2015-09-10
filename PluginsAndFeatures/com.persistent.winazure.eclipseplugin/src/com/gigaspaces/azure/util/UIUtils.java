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
package com.gigaspaces.azure.util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureRestUtils;
import com.persistent.util.MessageUtil;

public class UIUtils {
	/**
	 * Find index of text from combo box.
	 * @param txt
	 * @param combo
	 * @return
	 */
	public static int findSelectionByText(String txt,
			Combo combo) {
		if (txt == null
				|| txt.isEmpty()) {
			return 0;
		}
		for (int i = 0; i < combo.getItemCount(); i++) {
			String itemText = combo.getItem(i);
			if (itemText.equals(txt)) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * Select item from combo box as per item name.
	 * By finding out selection index as per name.
	 * @param combo
	 * @param name
	 * @return
	 */
	public static Combo selectByText(
			Combo combo, String name) {
		if (combo.getItemCount() > 0) {
			int selection = findSelectionByText(name, combo);
			if (selection != -1) {
				combo.select(selection);
			} else {
				combo.select(0);
			}
		}
		return combo;
	}

	/**
	 * Method populates subscription names into subscription
	 * combo box.
	 * @param combo
	 * @return
	 */
	public static Combo populateSubscriptionCombo(Combo combo) {
		Collection<PublishData> publishes =
				WizardCacheManager.getPublishDatas();
		if (publishes.size() > 0) {
		Map<String, PublishData> map = new HashMap<String, PublishData>();
		for (PublishData pd : publishes) {
			for (Subscription sub : pd.getPublishProfile().getSubscriptions()) {
				map.put(sub.getName(), pd);
			}
		}

		String currentSelection = combo.getText();
		combo.removeAll();
		for (Entry<String, PublishData> entry : map.entrySet()) {
			combo.add(entry.getKey());
			combo.setData(entry.getKey(), entry.getValue());
		}
		combo = selectByText(
				combo, currentSelection);
		} else if (publishes.isEmpty() || publishes.size() == 0) {
			combo.removeAll();
		}
		return combo;
	}

	/**
	 * Set current subscription and publish data
	 * as per subscription selected in combo box.
	 * @param combo
	 * @return
	 */
	public static PublishData changeCurrentSubAsPerCombo(Combo combo) {
		PublishData publishData = null;
		String subscriptionName = combo.getText();
		if (subscriptionName != null
				&& !subscriptionName.isEmpty()) {
			publishData = (PublishData) (combo.getData(subscriptionName));
			Subscription sub = WizardCacheManager.
					findSubscriptionByName(subscriptionName);
			if (publishData != null) {
				publishData.setCurrentSubscription(sub);
				WizardCacheManager.setCurrentPublishData(publishData);
			}
		}
		return publishData;
	}

	/**
	 * Method creates Import From Publish Settings File
	 * button.
	 * @param parent
	 * @param horiSpan
	 * @return
	 */
	public static Button createImportFromPublishSettingsFileBtn(
			Composite parent, int horiSpan) {
		Button importBtn = new Button(parent, SWT.PUSH | SWT.CENTER);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 3;
		gridData.horizontalSpan = horiSpan;
		gridData.widthHint = 280;
		importBtn.setText(Messages.impFrmPubSetLbl);
		importBtn.setLayoutData(gridData);
		return importBtn;
	}

	/**
	 * Method extracts data from publish settings file
	 * and create Publish data object.
	 * @param file
	 * @return
	 */
	public static PublishData createPublishDataObj(
			File file) {
		PublishData data;
		try {
			data = com.microsoftopentechnologies.azurecommons.deploy.util.UIUtils.parse(file);
		} catch (JAXBException e) {
			MessageUtil.displayErrorDialog(new Shell(),
					Messages.importDlgTitle,
					String.format(Messages.importDlgMsg,
							file.getName(),
							Messages.failedToParse));
			return null;
		}
		try {
			// I am of the opinion that this can be completely removed - need to revisit
			WindowsAzureRestUtils.getConfiguration(file, data.getSubscriptionIds().get(0));
		} catch (Exception e) {
            String errorMessage;
            Throwable cause = e.getCause();
            if (e instanceof RuntimeException && cause != null && cause instanceof ClassNotFoundException
                    && cause.getMessage() != null && cause.getMessage().contains("org.bouncycastle.jce.provider.BouncyCastleProvider")) {
                errorMessage = Messages.importDlgMsgJavaVersion;
            } else {
                errorMessage = Messages.importDlgMsg;
            }
			MessageUtil.displayErrorDialog(new Shell(),
					Messages.importDlgTitle,
					String.format(errorMessage,
							file.getName(),
							Messages.failedToParse));
			return null;
		}
		if (WizardCacheManager.findPublishDataBySubscriptionId(data.getSubscriptionIds().get(0)) != null) {
            MessageDialog.openInformation(new Shell(), Messages.loadingCred, Messages.credentialsExist);
		}
		data.setCurrentSubscription(data.getPublishProfile().
				getSubscriptions().get(0));
		return data;
	}
}
