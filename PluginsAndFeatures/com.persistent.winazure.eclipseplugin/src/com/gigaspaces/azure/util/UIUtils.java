package com.gigaspaces.azure.util;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.gigaspaces.azure.rest.WindowsAzureRestUtils;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.persistent.util.MessageUtil;

public class UIUtils {
	
	public static int findSelectionByText(String txt, Combo combo) {
		if (txt == null || txt.isEmpty()) return 0;
		for (int i = 0 ; i < combo.getItemCount() ; i++) {
			String itemText = combo.getItem(i);
			if (itemText.equals(txt)) return i;
		}
		return 0;
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
		gridData.widthHint = 230;
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
