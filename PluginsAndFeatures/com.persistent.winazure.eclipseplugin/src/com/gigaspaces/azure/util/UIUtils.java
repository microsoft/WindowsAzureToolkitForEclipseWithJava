package com.gigaspaces.azure.util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.gigaspaces.azure.model.Subscription;
import com.gigaspaces.azure.rest.WindowsAzureRestUtils;
import com.gigaspaces.azure.wizards.WizardCacheManager;
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
			data = WindowsAzureRestUtils.parse(file);
		} catch (JAXBException e) {
			MessageUtil.displayErrorDialog(new Shell(),
					Messages.importDlgTitle,
					String.format(Messages.importDlgMsg,
							file.getName(),
							Messages.failedToParse));
			return null;
		}
		String thumbprint;
		try {
			thumbprint = WindowsAzureRestUtils.getInstance().
					installPublishSettings(file, null);
		} catch (InterruptedException e) {
			MessageUtil.displayErrorDialog(new Shell(),
					Messages.importDlgTitle,
					String.format(Messages.importDlgMsg,
							file.getName(),
							Messages.failedToParse));
			return null;
		} catch (CommandLineException e) {
			MessageUtil.displayErrorDialog(new Shell(),
					Messages.importDlgTitle,
					String.format(Messages.importDlgMsg,
							file.getName(),
							e.getMessage()));
			return null;
		}
		if (WizardCacheManager.
				findPublishDataByThumbprint(thumbprint) != null) {
			MessageUtil.displayErrorDialog(new Shell(),
					Messages.loadingCred,
					Messages.credentialsExist);
			return null;
		}
		data.setThumbprint(thumbprint);
		data.setCurrentSubscription(data.getPublishProfile().
				getSubscriptions().get(0));
		return data;
	}
}
