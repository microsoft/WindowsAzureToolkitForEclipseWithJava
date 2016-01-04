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

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SelectImageStep extends WizardPage {
    private Label imageTypeLabel;
    private Combo imageTypeComboBox;
    private ComboViewer imageTypeViewer;
    private Label imageListLabel;
    private List imageLabelList;
    private Browser imageDescription;

    private Map<Enum, java.util.List<VirtualMachineImage>> virtualMachineImages;

    private CreateVMWizard wizard;

    protected SelectImageStep(CreateVMWizard wizard) {
        super("Select a Virtual Machine Image");
        setTitle("Select a Virtual Machine Image");
        this.wizard = wizard;
    }

    @Override
    public void createControl(Composite parent) {
        GridLayout gridLayout = new GridLayout(3, false);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        Composite container = new Composite(parent, 0);
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);

        wizard.configStepList(container, 1);

        createImageList(container);

        imageDescription = wizard.createImageDescriptor(container);

        this.setControl(container);
    }

    private void createImageList(Composite container) {
        Composite composite = new Composite(container, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.widthHint = 250;
        composite.setLayout(gridLayout);
        composite.setLayoutData(gridData);

        this.imageTypeLabel = new Label(composite, SWT.LEFT);
        this.imageTypeLabel.setText("Image Type");

        this.imageTypeComboBox = new Combo(composite, SWT.READ_ONLY);
        final ArrayList<Object> imageTypeList = new ArrayList<Object>();
        imageTypeList.add("Public Images");
        imageTypeList.addAll(Arrays.asList(PublicImages.values()));
        imageTypeList.add("MSDN Images");
        imageTypeList.addAll(Arrays.asList(MSDNImages.values()));
        imageTypeList.add("Private Images");
        imageTypeList.addAll(Arrays.asList(PrivateImages.values()));
        imageTypeViewer = new ComboViewer(imageTypeComboBox);
        imageTypeViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object o) {
                return o instanceof Enum ? "    " + o.toString() : o.toString();
            }


        });
        imageTypeViewer.setContentProvider(ArrayContentProvider.getInstance());
        imageTypeViewer.setInput(imageTypeList);

//        initImageType("Public Images", PublicImages.values());
//        initImageType("MSDN Images", MSDNImages.values());
//        initImageType("Private Images", PrivateImages.values());
        gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
        imageTypeComboBox.setLayoutData(gridData);
        imageTypeComboBox.select(1);
        imageTypeComboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                fillList();
            }
        });

        this.imageListLabel = new Label(composite, SWT.LEFT);
        this.imageListLabel.setText("Image Label");
        this.imageLabelList = new List(composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
//        gridData = new GridData();
//        gridData.widthHint = 300;
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        imageLabelList.setLayoutData(gridData);


        imageLabelList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                imageLabelSelected();
            }
        });
    }

    private void imageLabelSelected() {
        VirtualMachineImage virtualMachineImage = (VirtualMachineImage) imageLabelList.getData(imageLabelList.getItem(imageLabelList.getSelectionIndex()));
        wizard.setVirtualMachineImage(virtualMachineImage);

        if (virtualMachineImage != null) {
            imageDescription.setText(wizard.getHtmlFromVMImage(virtualMachineImage));
            setPageComplete(true);

            wizard.setSize(null);
        }
    }

    @Override
    public String getTitle() {
        if (virtualMachineImages == null && wizard.getSubscription() != null) {
            imageTypeComboBox.setEnabled(false);
            setPageComplete(false);

            imageLabelList.setItems(new String[]{"loading..."});
            imageLabelList.setEnabled(false);

            DefaultLoader.getIdeHelper().runInBackground(null, "Loading virtual machine images...", false, true, "", new Runnable() {
                @SuppressWarnings("null")
				@Override
                public void run() {
                    try {
                        for (VirtualMachineImage virtualMachineImage : AzureManagerImpl.getManager().getVirtualMachineImages(wizard.getSubscription().getId().toString())) {

                            if (virtualMachineImage.isShowInGui()) {
                                Enum type = null;
                                if (virtualMachineImage.getCategory().equals("Public")) {
                                    for (PublicImages publicImage : PublicImages.values()) {
                                        if (virtualMachineImage.getPublisherName().contains(publicImage.toString())) {
                                            type = publicImage;
                                        } else if (virtualMachineImage.getOperatingSystemType().equals(publicImage.toString())) {
                                            type = publicImage;
                                        }
                                    }

                                    if (type == null) {
                                        type = PublicImages.Other;
                                    }
                                } else if (virtualMachineImage.getCategory().equals("Private")
                                        || virtualMachineImage.getCategory().equals("User")) {
                                    type = PrivateImages.VMImages;
                                } else {
                                    for (MSDNImages msdnImages : MSDNImages.values()) {
                                        if (virtualMachineImage.getPublisherName().contains(msdnImages.toString())) {
                                            type = msdnImages;
                                        } else if (virtualMachineImage.getOperatingSystemType().equals(msdnImages.toString())) {
                                            type = msdnImages;
                                        }
                                    }

                                    if (type == null) {
                                        type = MSDNImages.Other;
                                    }
                                }

                                if (virtualMachineImages == null) {
                                    virtualMachineImages = new HashMap<Enum, java.util.List<VirtualMachineImage>>();
                                }

                                if (!virtualMachineImages.containsKey(type)) {
                                    virtualMachineImages.put(type, new ArrayList<VirtualMachineImage>());
                                }

                                virtualMachineImages.get(type).add(virtualMachineImage);
                            }
                        }

                        DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                imageTypeComboBox.setEnabled(true);
                                imageLabelList.setEnabled(true);

                                imageTypeComboBox.select(1);
                                fillList();
                            }
                        });
                    } catch (AzureCmdException e) {
                        DefaultLoader.getUIHelper().showException("An error occurred while trying to load the virtual machine images list",
                                e, "Error Loading Virtual Machine Images", false, true);
                    }
                }
            });
        }
        return super.getTitle();
    }

//    private void initImageType(String groupName, Object[] values) {
//        imageTypeComboBox.add(groupName);
//        imageTypeComboBox.setData(groupName, groupName);
//        for (Object value : values) {
//            imageTypeComboBox.add(value.toString());
//            imageTypeComboBox.setData(value.toString(), value);
//        }
//    }

    private void fillList() {
        setPageComplete(false);
        imageLabelList.removeAll();

        Object imageType = ((IStructuredSelection) imageTypeViewer.getSelection()).getFirstElement();
        if (!(imageType instanceof Enum)) {
            return;
        }

        java.util.List<VirtualMachineImage> machineImages = virtualMachineImages.get(imageType);
        if (machineImages != null && machineImages.size() > 0) {
            for (VirtualMachineImage image : machineImages) {
                imageLabelList.add(image.toString());
                imageLabelList.setData(image.toString(), image);
            }
            imageLabelList.setSelection(0);
            imageLabelSelected();
        }
    }

// todo: move to serviceexplorer-common.jar?
    private enum PublicImages {
        WindowsServer,
        SharePoint,
        BizTalkServer,
        SQLServer,
        VisualStudio,
        Linux,
        Other;

        @Override
        public String toString() {
            switch (this) {
                case WindowsServer:
                    return "Windows Server";
                case BizTalkServer:
                    return "BizTalk Server";
                case SQLServer:
                    return "SQL Server";
                case VisualStudio:
                    return "Visual Studio";
                default:
                    return super.toString();
            }
        }
    }

    private enum MSDNImages {
        BizTalkServer,
        Dynamics,
        VisualStudio,
        Other;

        @Override
        public String toString() {
            switch (this) {
                case BizTalkServer:
                    return "BizTalk Server";
                case VisualStudio:
                    return "Visual Studio";
                default:
                    return super.toString();
            }

        }
    }

    private enum PrivateImages {
        VMImages;

        @Override
        public String toString() {
            switch (this) {
                case VMImages:
                    return "VM Images";
                default:
                    return super.toString();
            }
        }
    }
}
