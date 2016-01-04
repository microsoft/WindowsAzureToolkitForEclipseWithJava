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
package com.microsoft.azureexplorer.forms;


import com.microsoft.azureexplorer.Activator;
import com.microsoftopentechnologies.tooling.msservices.components.DefaultLoader;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoftopentechnologies.tooling.msservices.model.Subscription;
import com.microsoftopentechnologies.tooling.msservices.model.storage.StorageAccount;
import com.microsoftopentechnologies.tooling.msservices.model.vm.AffinityGroup;
import com.microsoftopentechnologies.tooling.msservices.model.vm.Location;

import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

import java.net.URL;
import java.util.Vector;

public class CreateStorageAccountForm extends Dialog {
    private static final String PRICING_LINK = "<a href=\"http://go.microsoft.com/fwlink/?LinkID=400838\">Read more about replication services and pricing details</a>";
    private enum ReplicationTypes {
        Standard_LRS,
        Standard_GRS,
        Standard_RAGRS;

        public String getDescription() {
            switch (this) {
                case Standard_GRS:
                    return "Geo-Redundant";
                case Standard_LRS:
                    return "Locally Redundant";
                case Standard_RAGRS:
                    return "Read Access Geo-Redundant";
            }

            return super.toString();
        }
    }

    private Button buttonOK;
    private Button buttonCancel;

    private Label subscriptionLabel;
    private Combo subscriptionComboBox;
    private Label nameLabel;
    private Text nameTextField;
    private Label regionLabel;
    private Combo regionOrAffinityGroupComboBox;
    private Label replicationLabel;
    private Combo replicationComboBox;
//    private JProgressBar createProgressBar;
    private Link pricingLabel;
    private Label userInfoLabel;

    private ComboViewer regionOrAffinityGroupViewer;

    private Runnable onCreate;
    private Subscription subscription;
    private StorageAccount storageAccount;

    public CreateStorageAccountForm(Shell parentShell, Subscription subscription) {
        super(parentShell);
        this.subscription = subscription;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Create Storage Account");
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        GridData gridData = new GridData();
        gridData.verticalAlignment = SWT.FILL;
        gridData.horizontalAlignment = SWT.FILL;
        parent.setLayoutData(gridData);
        Control ctrl = super.createButtonBar(parent);
        buttonOK = getButton(IDialogConstants.OK_ID);
        buttonOK.setEnabled(false);
        buttonOK.setText("Create");
        buttonCancel = getButton(IDialogConstants.CANCEL_ID);
        buttonCancel.setText("Close");
        return ctrl;
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        container.setLayout(gridLayout);
        GridData gridData = new GridData();
        gridData.widthHint = 350;
        container.setLayoutData(gridData);

        userInfoLabel = new Label(container, SWT.LEFT);

        subscriptionLabel = new Label(container, SWT.LEFT);
        subscriptionLabel.setText("Subscription:");
        subscriptionComboBox = new Combo(container, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        subscriptionComboBox.setLayoutData(gridData);

        nameLabel = new Label(container, SWT.LEFT);
        nameLabel.setText("Name:");
        nameTextField = new Text(container, SWT.LEFT | SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        nameTextField.setLayoutData(gridData);

        regionLabel = new Label(container, SWT.LEFT);
        regionLabel.setText("Region or Affinity Group:");
        regionOrAffinityGroupComboBox = new Combo(container, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        regionOrAffinityGroupComboBox.setLayoutData(gridData);
        regionOrAffinityGroupViewer = new ComboViewer(regionOrAffinityGroupComboBox);
        regionOrAffinityGroupViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof String) {
                    return (String) element;
                } else  if (element instanceof AffinityGroup) {
                    AffinityGroup ag = (AffinityGroup) element;
                    return (String.format("  %s (%s)", ag.getName(), ag.getLocation()));
                } else {
                    return  "  " + element.toString();
                }
            }
        });
        regionOrAffinityGroupViewer.setContentProvider(ArrayContentProvider.getInstance());

        replicationLabel = new Label(container, SWT.LEFT);
        replicationLabel.setText("Replication");
        replicationComboBox = new Combo(container, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        replicationComboBox.setLayoutData(gridData);

        pricingLabel = new Link(container, SWT.LEFT);
        pricingLabel.setText(PRICING_LINK);
        pricingLabel.setLayoutData(gridData);
        pricingLabel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(event.text));
                } catch (Exception ex) {
					/*
					 * only logging the error in log file
					 * not showing anything to end user
					 */
                    Activator.getDefault().log("Error occurred while opening link in default browser.", ex);
                }
            }
        });

        nameTextField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                validateEmptyFields();
            }
        });

        regionOrAffinityGroupComboBox.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                validateEmptyFields();
            }
        });

        if (AzureManagerImpl.getManager().authenticated()) {
            String upn = AzureManagerImpl.getManager().getUserInfo().getUniqueName();
            userInfoLabel.setText("Signed in as: " + (upn.contains("#") ? upn.split("#")[1] : upn));
        } else {
            userInfoLabel.setText("");
        }
        for (ReplicationTypes replicationType : ReplicationTypes.values()) {
            replicationComboBox.add(replicationType.getDescription());
            replicationComboBox.setData(replicationType.getDescription(), replicationType);
        }
        replicationComboBox.select(0);
//        replicationComboBox.setRenderer(new ListCellRendererWrapper<ReplicationTypes>() {
//            @Override
//            public void customize(JList jList, ReplicationTypes replicationTypes, int i, boolean b, boolean b1) {
//                setText(replicationTypes.getDescription());
//            }
//        });
        fillFields();

        return super.createContents(parent);
    }

    private void validateEmptyFields() {
        boolean allFieldsCompleted = !(nameTextField.getText().isEmpty() || regionOrAffinityGroupComboBox.getText().isEmpty());

        buttonOK.setEnabled(allFieldsCompleted);
    }

    @Override
    protected void okPressed() {
        if (nameTextField.getText().length() < 3
                || nameTextField.getText().length() > 24
                || !nameTextField.getText().matches("[a-z0-9]+")) {
            DefaultLoader.getUIHelper().showError("Invalid storage account name. The name should be between 3 and 24 characters long and \n" +
                    "can contain only lowercase letters and numbers.", "Azure Explorer");
            return;
        }
        PluginUtil.showBusy(true, getShell());
//        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//        createProgressBar.setVisible(true);

        try {
            String name = nameTextField.getText();

            Object regionOrAffinityGroup = ((IStructuredSelection) regionOrAffinityGroupViewer.getSelection()).getFirstElement();
            String region = (regionOrAffinityGroup != null && regionOrAffinityGroup instanceof Location) ? regionOrAffinityGroup.toString() : "";
            String affinityGroup = (regionOrAffinityGroup != null && regionOrAffinityGroup instanceof AffinityGroup) ? regionOrAffinityGroup.toString() : "";
            String replication = replicationComboBox.getData(replicationComboBox.getText()).toString();

            storageAccount = new StorageAccount(name, subscription.getId().toString());
            storageAccount.setType(replication);
            storageAccount.setLocation(region);
            storageAccount.setAffinityGroup(affinityGroup);

            AzureManagerImpl.getManager().createStorageAccount(storageAccount);
            AzureManagerImpl.getManager().refreshStorageAccountInformation(storageAccount);

            if (onCreate != null) {
                onCreate.run();
            }
        } catch (AzureCmdException e) {
            storageAccount = null;
            DefaultLoader.getUIHelper().showException("An error occurred while trying to create the specified storage account.", e, "Error Creating Storage Account", false, true);
        }
        PluginUtil.showBusy(false, getShell());

        super.okPressed();
    }

    @Override
    protected void cancelPressed() {

        super.cancelPressed();
    }

    public void fillFields() {

        if (subscription == null) {
            try {
                subscriptionComboBox.setEnabled(true);

                java.util.List<Subscription> fullSubscriptionList = AzureManagerImpl.getManager().getFullSubscriptionList();
                for (Subscription sub : fullSubscriptionList) {
                    subscriptionComboBox.add(sub.getName());
                    subscriptionComboBox.setData(sub.getName(), sub);
                }
                subscriptionComboBox.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        CreateStorageAccountForm.this.subscription = (Subscription) subscriptionComboBox.getData(subscriptionComboBox.getText());
                        loadRegions();
                    }
                });

                if (fullSubscriptionList.size() > 0) {
                    this.subscription = fullSubscriptionList.get(0);
                    subscriptionComboBox.select(0);
                    loadRegions();
                }
            } catch (AzureCmdException e) {
                DefaultLoader.getUIHelper().showException("Error getting subscriptions", e);
            }
        } else {
            subscriptionComboBox.setEnabled(false);
            subscriptionComboBox.add(subscription.getName());
            subscriptionComboBox.select(0);

            loadRegions();
        }
    }

    public void setOnCreate(Runnable onCreate) {
        this.onCreate = onCreate;
    }

    public StorageAccount getStorageAccount() {
        return storageAccount;
    }

    public void loadRegions() {
        regionOrAffinityGroupComboBox.add("<Loading...>");

        DefaultLoader.getIdeHelper().runInBackground(null, "Loading regions...", false, true, "Loading regions...", new Runnable() {
            @Override
            public void run() {
                try {
                    final java.util.List<AffinityGroup> affinityGroups = AzureManagerImpl.getManager().getAffinityGroups(subscription.getId().toString());
                    final java.util.List<Location> locations = AzureManagerImpl.getManager().getLocations(subscription.getId().toString());

                    DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final Vector<Object> vector = new Vector<Object>();
                            vector.add("Regions");
                            vector.addAll(locations);
                            if (affinityGroups.size() > 0) {
                                vector.add("Affinity Groups");
                                vector.addAll(affinityGroups);
                            }
                            regionOrAffinityGroupViewer.setInput(vector);
//                            regionOrAffinityGroupComboBox.setModel(new DefaultComboBoxModel(vector) {
//                                public void setSelectedItem(Object o) {
//                                    if (!(o instanceof String)) {
//                                        super.setSelectedItem(o);
//                                    }
//                                }
//                            });

                            regionOrAffinityGroupComboBox.select(1);
                        }
                    });
                } catch (AzureCmdException e) {
                    DefaultLoader.getUIHelper().showException("An error occurred while trying to load the regions list",
                            e, "Error Loading Regions", false, true);
                }
            }
        });
    }
}
