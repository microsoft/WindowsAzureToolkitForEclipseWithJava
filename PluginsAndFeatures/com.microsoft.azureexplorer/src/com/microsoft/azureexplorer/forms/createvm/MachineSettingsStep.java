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

import com.microsoftopentechnologies.tooling.msservices.components.DefaultLoader;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoftopentechnologies.tooling.msservices.model.vm.VirtualMachineImage;
import com.microsoftopentechnologies.tooling.msservices.model.vm.VirtualMachineSize;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.*;


public class MachineSettingsStep extends WizardPage {
    private Label vmNameLabel;
    private Text vmNameTextField;
    private Label vmSizeLabel;
    private Combo vmSizeComboBox;
    private Label vmUserLabel;
    private Text vmUserTextField;
    private Label vmPasswordLabel;
    private Text vmPasswordField;
    private Label confirmPasswordLabel;
    private Text confirmPasswordField;
    private Button passwordCheckBox;
    private Label certificateLabel;
    private Button certificateButton;
    private Text certificateField;
    private Button certificateCheckBox;
//    private JPanel certificatePanel;
//    private JPanel passwordPanel;
    private Browser imageDescription;
    private CreateVMWizard wizard;

    private boolean inSetPageComplete = false;

    public MachineSettingsStep(CreateVMWizard wizard) {
        super("Create new Virtual Machine", "Virtual Machine Basic Settings", null);
        this.wizard = wizard;
    }

    @Override
    public void createControl(Composite parent) {
        GridLayout gridLayout = new GridLayout(3, false);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.verticalAlignment = SWT.FILL;
        Composite container = new Composite(parent, 0);
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);

        wizard.configStepList(container, 2);

        createSettings(container);

        imageDescription = wizard.createImageDescriptor(container);

        this.setControl(container);
    }

    private void createSettings(Composite container) {
        final Composite composite = new Composite(container, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = GridData.BEGINNING;
//        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = 250;
        composite.setLayout(gridLayout);
        composite.setLayoutData(gridData);

        vmNameLabel = new Label(composite, SWT.LEFT);
        vmNameLabel.setText("Virtual Machine Name");
        vmNameTextField = new Text(composite, SWT.LEFT | SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        vmNameTextField.setLayoutData(gridData);

        vmSizeLabel = new Label(composite, SWT.LEFT);
        vmSizeLabel.setText("Size");
        vmSizeComboBox = new Combo(composite, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        vmSizeComboBox.setLayoutData(gridData);

        vmUserLabel = new Label(composite, SWT.LEFT);
        vmUserLabel.setText("User name");
        vmUserTextField = new Text(composite, SWT.LEFT | SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        vmUserTextField.setLayoutData(gridData);

        certificateCheckBox = new Button(composite, SWT.CHECK);
        certificateCheckBox.setText("Upload compatible SSH key");
        certificateCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                certificateCheckBoxSelected(certificateCheckBox.getSelection());
            }
        });

        createCertificatePanel(composite);

        passwordCheckBox = new Button(composite, SWT.CHECK);
        passwordCheckBox.setText("Provide a password");
        passwordCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                vmPasswordLabel.setEnabled(passwordCheckBox.getSelection());
                vmPasswordField.setEnabled(passwordCheckBox.getSelection());
                confirmPasswordLabel.setEnabled(passwordCheckBox.getSelection());
                confirmPasswordField.setEnabled(passwordCheckBox.getSelection());

                validateEmptyFields();
            }
        });

        vmPasswordLabel = new Label(composite, SWT.LEFT);
        vmPasswordLabel.setText("Password");
        vmPasswordField = new Text(composite, SWT.PASSWORD | SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        vmPasswordField.setLayoutData(gridData);
        confirmPasswordLabel = new Label(composite, SWT.LEFT);
        confirmPasswordLabel.setText("Confirm");
        confirmPasswordField = new Text(composite, SWT.PASSWORD | SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        confirmPasswordField.setLayoutData(gridData);

        ModifyListener modifyListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                validateEmptyFields();
            }
        };
        vmNameTextField.addModifyListener(modifyListener);
        vmUserTextField.addModifyListener(modifyListener);
        certificateField.addModifyListener(modifyListener);
        vmPasswordField.addModifyListener(modifyListener);
        confirmPasswordField.addModifyListener(modifyListener);
    }

    private void certificateCheckBoxSelected(boolean selected) {
        certificateLabel.setEnabled(selected);
        certificateField.setEnabled(selected);
        certificateButton.setEnabled(selected);

        validateEmptyFields();
    }

    private void createCertificatePanel(Composite composite) {
        Composite panel = new Composite(composite, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = 250;
        panel.setLayout(gridLayout);
        panel.setLayoutData(gridData);

        certificateLabel = new Label(panel, SWT.LEFT);
        certificateLabel.setText("Certificate");
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        certificateLabel.setLayoutData(gridData);

        certificateField = new Text(panel, SWT.LEFT | SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        certificateField.setLayoutData(gridData);
        certificateButton = new Button(panel, SWT.PUSH);
        certificateButton.setText("...");
    }

    @Override
    public String getTitle() {
        final VirtualMachineImage virtualMachineImage = wizard.getVirtualMachineImage();

        if (virtualMachineImage.getOperatingSystemType().equals("Linux")) {
            certificateCheckBox.setEnabled(true);
            passwordCheckBox.setEnabled(true);
            certificateCheckBoxSelected(true);
            passwordCheckBox.setSelection(false);
        } else {
            certificateCheckBoxSelected(false);
            passwordCheckBox.setSelection(true);
            certificateCheckBox.setEnabled(false);
            passwordCheckBox.setEnabled(false);
        }

        validateEmptyFields();

        imageDescription.setText(wizard.getHtmlFromVMImage(virtualMachineImage));

        if (vmSizeComboBox.getItemCount() == 0) {
            vmSizeComboBox.setItems(new String[]{"<Loading...>"});

            DefaultLoader.getIdeHelper().runInBackground(null, "Loading VM sizes...", false, true, "", new Runnable() {
                @Override
                public void run() {
                    try {
                        final java.util.List<VirtualMachineSize> virtualMachineSizes = AzureManagerImpl.getManager().getVirtualMachineSizes(wizard.getSubscription().getId().toString());

                        Collections.sort(virtualMachineSizes, new Comparator<VirtualMachineSize>() {
                            @Override
                            public int compare(VirtualMachineSize t0, VirtualMachineSize t1) {

                                if (t0.getName().contains("Basic") && t1.getName().contains("Basic")) {
                                    return t0.getName().compareTo(t1.getName());
                                } else if (t0.getName().contains("Basic")) {
                                    return -1;
                                } else if (t1.getName().contains("Basic")) {
                                    return 1;
                                }

                                int coreCompare = Integer.valueOf(t0.getCores()).compareTo(t1.getCores());

                                if (coreCompare == 0) {
                                    return Integer.valueOf(t0.getMemoryInMB()).compareTo(t1.getMemoryInMB());
                                } else {
                                    return coreCompare;
                                }
                            }
                        });
                        DefaultLoader.getIdeHelper().invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                vmSizeComboBox.removeAll();
                                for (VirtualMachineSize size : virtualMachineSizes) {
                                    vmSizeComboBox.add(size.toString());
                                    vmSizeComboBox.setData(size.toString(), size);
                                }
                                selectDefaultSize();
                            }
                        });
                    } catch (AzureCmdException e) {
                        DefaultLoader.getUIHelper().showException("An error occurred while trying to load the VM sizes list",
                                e, "Error Loading VM Sizes", false, true);
                    }
                }
            });
        } else {
            selectDefaultSize();
        }
        return super.getTitle();
    }

    @Override
    public IWizardPage getNextPage() {
        if (!inSetPageComplete) {
            String name = vmNameTextField.getText();

            if (name.length() > 15 || name.length() < 3) {
                DefaultLoader.getUIHelper().showError("Invalid virtual machine name. The name must be between 3 and 15 character long.", "Error creating the virtual machine");
                return this;
            }

            if (!name.matches("^[A-Za-z][A-Za-z0-9-]+[A-Za-z0-9]$")) {
                DefaultLoader.getUIHelper().showError("Invalid virtual machine name. The name must start with a letter, \n" +
                        "contain only letters, numbers, and hyphens, " +
                        "and end with a letter or number.", "Error creating the virtual machine");
                return this;
            }

            String password = passwordCheckBox.getSelection() ? vmPasswordField.getText() : "";

            if (passwordCheckBox.getSelection()) {
                String conf = confirmPasswordField.getText();

                if (!password.equals(conf)) {
                    DefaultLoader.getUIHelper().showError("Password confirmation should match password", "Error creating the service");
                    return this;
                }

                if (!password.matches("(?=^.{8,255}$)((?=.*\\d)(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[^A-Za-z0-9])(?=.*[a-z])|(?=.*[^A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z])|(?=.*\\d)(?=.*[A-Z])(?=.*[^A-Za-z0-9]))^.*")) {
                    DefaultLoader.getUIHelper().showError("The password does not conform to complexity requirements.\n" +
                            "It should be at least eight characters long and contain a mixture of upper case, lower case, digits and symbols.", "Error creating the virtual machine");
                    return this;
                }
            }

            String certificate = certificateCheckBox.getSelection() ? certificateField.getText() : "";

            wizard.setName(name);
            wizard.setSize((VirtualMachineSize) vmSizeComboBox.getData(vmSizeComboBox.getText()));
            wizard.setUserName(vmUserTextField.getText());
            wizard.setPassword(password);
            wizard.setCertificate(certificate);
        }
        return super.getNextPage();
    }

    private void selectDefaultSize() {
        DefaultLoader.getIdeHelper().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                String recommendedVMSize = wizard.getVirtualMachineImage().getRecommendedVMSize().isEmpty()
                        ? "Small"
                        : wizard.getVirtualMachineImage().getRecommendedVMSize();
                for (String sizeLabel : vmSizeComboBox.getItems()) {
                    VirtualMachineSize size = (VirtualMachineSize) vmSizeComboBox.getData(sizeLabel);
                    if (size != null && size.getName().equals(recommendedVMSize)) {
                        vmSizeComboBox.setText(sizeLabel);
                    }
                }
            }
        });
    }

    private void validateEmptyFields() {

        boolean allFieldsCompleted = !(
                vmNameTextField.getText().isEmpty()
                        || vmUserTextField.getText().isEmpty()
                        || !(passwordCheckBox.getSelection() || certificateCheckBox.getSelection())
                        || (passwordCheckBox.getSelection() &&
                        (vmPasswordField.getText().length() == 0
                                || confirmPasswordField.getText().length() == 0))
                        || (certificateCheckBox.getSelection() && certificateField.getText().isEmpty()));
        inSetPageComplete = true;
        setPageComplete(allFieldsCompleted);
        inSetPageComplete = false;
    }
}
