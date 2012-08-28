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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public abstract class WADialog extends TitleAreaDialog {

	public WADialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);

		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setText(Messages.addBut);
			okButton.setEnabled(validateDialog());
			okButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent selectionevent) {
					createButtonClicked();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent selectionevent) {
				}
			});
		}
		return control;
	}

	@Override
	public void setMessage(String newMessage, int type) {
		setErrorMessage(null);
		setOkButton(true);
		super.setMessage(newMessage, type);
	}

	@Override
	public void setErrorMessage(String newErrorMessage) {
		setOkButton(newErrorMessage == null);

		super.setErrorMessage(newErrorMessage);

	}

	protected Button getOkButton() {
		return getButton(IDialogConstants.OK_ID);
	}

	protected void setOkButton(boolean isEnabled) {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null)
			okButton.setEnabled(isEnabled);
	}

	protected abstract boolean validateDialog();

	protected abstract void createButtonClicked();

	class ValidateInputCompletion implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent modifyevent) {
			validateDialog();
		}

	}
}
