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
package com.persistent.ui.preference;

import java.io.File;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.microsoftopentechnologies.wacommon.utils.PreferenceSetUtil;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;
import com.persistent.util.JdkSrvConfig;
import com.persistent.util.WAEclipseHelper;
/**
 * Class for Service end point preference page.
 * Creates UI components and their listeners.
 */
public class ServiceEndptPreferencePage
extends PreferencePage implements IWorkbenchPreferencePage {
	private Text txtPortal;
	private Text txtMangmnt;
	private Text txtBlobUrl;
	private Text txtPubSet;
	private Button editBtn;
	private Combo prefNameCmb;
	private File preferenceFile = new File(WAEclipseHelper.
			getTemplateFile(Messages.prefFileName));
	private String valOkToLeave = "";

	@Override
	public void init(IWorkbench arg0) {

	}

	@Override
	public String getTitle() {
		setToDefaultName();
		populateValues();
		valOkToLeave = "";
		return super.getTitle();
	}

	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayout(gridLayout);
		composite.setLayoutData(gridData);
		createActiveSetComponent(composite);
		createGridComponents(composite);
		return null;
	}

	/**
	 * Method creates active set combo box and edit button
	 * to edit preferencesets.xml file.
	 * @param parent
	 */
	private void createActiveSetComponent(Composite parent) {
		createLabel(parent, Messages.actvStLbl);

		prefNameCmb = createCombo(parent);
		prefNameCmb.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				populateValues();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		editBtn = createButton(parent);
		editBtn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JdkSrvConfig.custLinkListener(
						Messages.edtPrefTtl,
						Messages.prefFileMsg,
						false,
						getShell(),
						null,
						preferenceFile);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 * Method sets extracted <preferenceset> names to
	 * active set combo box. By default, it is set to
	 * the default setting of the parent <preferencesets> element.
	 * But if user is visiting service endpoint page
	 * after okToLeave then value modified by user is populated.
	 */
	private void setToDefaultName() {
		try {
			prefNameCmb.setItems(PreferenceSetUtil.
					getPrefSetNameArr());
			if (!valOkToLeave.isEmpty()) {
				prefNameCmb.setText(valOkToLeave);
			} else {
				prefNameCmb.setText(PreferenceSetUtil.
						getSelectedPreferenceSetName());
			}
		} catch (WACommonException e) {
			PluginUtil.displayErrorDialog(getShell(),
					Messages.errTtl,
					Messages.getPrefErMsg);
		}
	}

	/**
	 * Blob, Management, Portal
	 * and Publish Settings
	 * values from preferencesets.xml
	 * will be populated according to active set value.
	 */
	private void populateValues() {
		String nameInCombo = prefNameCmb.getText();
		try {
			txtPortal.setText(PreferenceSetUtil.
					getSelectedPortalURL(nameInCombo));
			txtMangmnt.setText(PreferenceSetUtil.
					getManagementURL(nameInCombo));
			txtBlobUrl.setText(PreferenceSetUtil.
					getBlobServiceURL(nameInCombo));
			txtPubSet.setText(PreferenceSetUtil.
					getSelectedPublishSettingsURL(nameInCombo));
		} catch (WACommonException e) {
			PluginUtil.displayErrorDialog(getShell(),
					Messages.errTtl,
					Messages.getPrefErMsg);
		}
	}

	/**
	 * Method to create label.
	 * @param parent
	 * @param text
	 * @return
	 */
	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		GridData gridData = new GridData();
		label.setText(text);
		label.setLayoutData(gridData);
		return label;
	}

	/**
	 * Method to create combo box.
	 * @param parent
	 * @return
	 */
	public static Combo createCombo(Composite parent) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 34;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		combo.setLayoutData(gridData);
		return combo;
	}

	/**
	 * Method to create text box.
	 * @param parent
	 * @return
	 */
	private Text createTextBox(Composite parent) {
		Text text = new Text(parent, SWT.LEFT
				| SWT.BORDER | SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		text.setLayoutData(gridData);
		return text;
	}

	/**
	 * Method to create button.
	 * @param parent
	 * @return
	 */
	private Button createButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
		GridData gridData = new GridData();
		gridData.widthHint = 60;
		button.setText(Messages.editBtnText);
		button.setLayoutData(gridData);
		return button;
	}

	/**
	 * Method creates URL's grid, labels and respective
	 * text boxes in which Blob, Management, Portal
	 * and Publish Settings
	 * values from preferencesets.xml
	 * will be populated according to active set value.
	 * @param parent
	 */
	private void createGridComponents(Composite parent) {
		Group group = JdkSrvConfig.
				createGroup(parent, 2, Messages.urlLbl);
		createLabel(group, Messages.prtlLbl);
		txtPortal = createTextBox(group);
		createLabel(group, Messages.mngtLbl);
		txtMangmnt = createTextBox(group);
		createLabel(group, Messages.blbUrlLbl);
		txtBlobUrl = createTextBox(group);
		createLabel(group, Messages.pubSetLbl);
		txtPubSet = createTextBox(group);
	}

	@Override
	public boolean performOk() {
		handlePerformOkAndOkToLeave(true);
		return super.performOk();
	}

	@Override
	public boolean okToLeave() {
		handlePerformOkAndOkToLeave(false);
		return super.okToLeave();
	}

	/**
	 * Method will get called when OK button is clicked
	 * or you leave page.
	 * 1. Invoked from performOk --> update default value in preferenceset
	 * 2. Invoked from okToLeave --> save value of combo box in variable
	 * to populate it in future, when user visits service endpoint page
	 * again after visiting some other page.
	 * Do not update preferenceset.xml immediately on the event of okToLeave
	 * as user may press cancel.
	 * @param isPerformOk
	 */
	private void handlePerformOkAndOkToLeave(boolean isPerformOk) {
		try {
			String cmbValue = prefNameCmb.getText();
			/*
			 * Do not check default value from preferenceset
			 * is equal to value in combo box,
			 * as user may have deleted default attribute
			 * but still we need to create attribute.
			 */
			if (isPerformOk) {
				PreferenceSetUtil.setPrefDefault(cmbValue);
			} else {
				valOkToLeave = cmbValue;
			}
		} catch (WACommonException e) {
			PluginUtil.displayErrorDialog(getShell(),
					Messages.errTtl,
					Messages.setPrefErMsg);
		}
	}
}
