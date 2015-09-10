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
