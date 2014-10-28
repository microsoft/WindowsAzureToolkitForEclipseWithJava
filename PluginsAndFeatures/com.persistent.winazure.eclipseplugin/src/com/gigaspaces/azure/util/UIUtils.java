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
import com.microsoftopentechnologies.deploy.util.PublishData;
import com.microsoftopentechnologies.model.Subscription;
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
		gridData.widthHint = 260;
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
			data = com.microsoftopentechnologies.deploy.util.UIUtils.parse(file);
		} catch (JAXBException e) {
			MessageUtil.displayErrorDialog(new Shell(),
					Messages.importDlgTitle,
					String.format(Messages.importDlgMsg,
							file.getName(),
							Messages.failedToParse));
			return null;
		}
		String subscriptionId;
		try {
			subscriptionId = com.microsoftopentechnologies.deploy.util.UIUtils.installPublishSettings(file, data.getSubscriptionIds().get(0), null);
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
		if (WizardCacheManager.findPublishDataBySubscriptionId(subscriptionId) != null) {
            MessageDialog.openInformation(new Shell(), Messages.loadingCred, Messages.credentialsExist);
		}
		data.setCurrentSubscription(data.getPublishProfile().
				getSubscriptions().get(0));
		return data;
	}
}
