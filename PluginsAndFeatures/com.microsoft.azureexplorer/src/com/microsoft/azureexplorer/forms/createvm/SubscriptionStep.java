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
package com.microsoft.azureexplorer.forms.createvm;

import com.microsoft.azureexplorer.Activator;
import com.microsoft.azureexplorer.forms.SubscriptionPropertyPage;
import com.microsoftopentechnologies.tooling.msservices.components.DefaultLoader;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureManager;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoftopentechnologies.tooling.msservices.model.Subscription;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.Vector;


public class SubscriptionStep extends WizardPage {
    private Button buttonLogin;
    private Label userInfoLabel;
    private Label subscriptionLabel;
    private Combo subscriptionComboBox;

    private CreateVMWizard wizard;

    protected SubscriptionStep(CreateVMWizard wizard) {
        super("Create new Virtual Machine", "Choose a Subscription", null);
        this.wizard = wizard;
    }

    @Override
    public void createControl(Composite parent) {
        GridLayout gridLayout = new GridLayout(3, false);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = GridData.BEGINNING;
        Composite container = new Composite(parent, 0);
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);

        wizard.configStepList(container, 0);

        createSubscriptionCombo(container);

        this.buttonLogin = new Button(container, SWT.PUSH);
        this.buttonLogin.setImage(Activator.getImageDescriptor("icons/settings.png").createImage());
        gridData = new GridData();
        gridData.horizontalIndent = 5;
        gridData.widthHint = 50;
        gridData.heightHint = 40;
        gridData.verticalAlignment = GridData.BEGINNING;
        buttonLogin.setLayoutData(gridData);
        buttonLogin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Dialog subscriptionsDialog = new SubscriptionPropertyPage(new Shell());
                subscriptionsDialog.open();
                loadSubscriptions();
            }
        });

        this.setControl(container);
    }

    private void createSubscriptionCombo(Composite container) {
        Composite composite = new Composite(container, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = GridData.BEGINNING;

        gridData.grabExcessHorizontalSpace = true;
        composite.setLayout(gridLayout);
        composite.setLayoutData(gridData);

        this.userInfoLabel = new Label(composite, SWT.LEFT);

        this.subscriptionLabel = new Label(composite, SWT.LEFT);
        this.subscriptionLabel.setText("Choose the subscription to use when creating the new virtual machine");

        this.subscriptionComboBox = new Combo(composite, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.widthHint = 382;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        subscriptionComboBox.setLayoutData(gridData);
        subscriptionComboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.text != null && e.text.length() > 0) {
                    wizard.setSubscription((Subscription) subscriptionComboBox.getData(e.text));
                }
            }
        });
        loadSubscriptions();
    }

    private void loadSubscriptions() {
        try {
            AzureManager manager = AzureManagerImpl.getManager();

            if (manager.authenticated()) {
                String upn = manager.getUserInfo().getUniqueName();
                userInfoLabel.setText("Signed in as: " + (upn.contains("#") ? upn.split("#")[1] : upn));
            } else {
                userInfoLabel.setText("");
            }

            java.util.List<Subscription> subscriptionList = manager.getSubscriptionList();

            final Vector<Subscription> subscriptions = new Vector<Subscription>((subscriptionList == null) ? new Vector<Subscription>() : subscriptionList);
            for (Subscription subscription : subscriptions) {
                subscriptionComboBox.add(subscription.getName());
                subscriptionComboBox.setData(subscription.getName(), subscription);
            }

            if (!subscriptions.isEmpty()) {
                subscriptionComboBox.select(0);
                wizard.setSubscription((Subscription) subscriptionComboBox.getData(subscriptionComboBox.getText()));
            }
            setPageComplete(!subscriptions.isEmpty());
        } catch (AzureCmdException e) {
            DefaultLoader.getUIHelper().showException("An error occurred while trying to load the subscriptions list",
                    e, "Error Loading Subscriptions", false, true);
        }
    }
}
