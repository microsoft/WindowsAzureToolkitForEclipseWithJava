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
import com.microsoftopentechnologies.tooling.msservices.components.DefaultLoader;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoftopentechnologies.tooling.msservices.model.Subscription;
import com.microsoftopentechnologies.tooling.msservices.model.storage.StorageAccount;
import com.microsoftopentechnologies.tooling.msservices.model.vm.*;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.azure.vm.VMNode;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.azure.vm.VMServiceModule;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class CreateVMWizard extends Wizard {
    private static final String BASE_HTML_VM_IMAGE = "<html>\n" +
            "<body style=\"padding: 5px; width: 250px\">\n" +
            "    <p style=\"font-family: 'Segoe UI';font-size: 12pt;font-weight: bold;\">#TITLE#</p>\n" +
            "    <p style=\"font-family: 'Segoe UI';font-size: 8pt; width:200px \">#DESCRIPTION#</p>\n" +
            "    <p>\n" +
            "        <table style='width:200px'>\n" +
            "            <tr>\n" +
            "                <td style=\"font-family: 'Segoe UI';font-size: 8pt;width:60px;vertical-align:top;\"><b>PUBLISHED</b></td>\n" +
            "                <td style=\"font-family: 'Segoe UI';font-size: 8pt;\">#PUBLISH_DATE#</td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "                <td style=\"font-family: 'Segoe UI';font-size: 8pt;vertical-align:top;\"><b>PUBLISHER</b></td>\n" +
            "                <td style=\"font-family: 'Segoe UI';font-size: 8pt;\">#PUBLISH_NAME#</td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "                <td style=\"font-family: 'Segoe UI';font-size: 8pt;vertical-align:top;\"><b>OS FAMILY</b></td>\n" +
            "                <td style =\"font-family: 'Segoe UI';font-size: 8pt;\">#OS#</td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "                <td style=\"font-family: 'Segoe UI';font-size: 8pt;v-align:top;font-weight:bold;\">LOCATION</td>\n" +
            "                <td style=\"font-family: 'Segoe UI';font-size: 8pt;\">#LOCATION#</td>\n" +
            "            </tr>\n" +
            "        </table>\n" +
            "    </p>\n" +
            "    #PRIVACY#\n" +
            "    #LICENCE#\n" +
            "</body>\n" +
            "</html>";

    private VMServiceModule node;

    private Subscription subscription;
    private VirtualMachineImage virtualMachineImage;
    private String name;
    private VirtualMachineSize size;
    private String userName;
    private String password;
    private String certificate;
    private CloudService cloudService;
    private boolean filterByCloudService;
    private StorageAccount storageAccount;
    private VirtualNetwork virtualNetwork;
    private String subnet;
    private String availabilitySet;
    private java.util.List<Endpoint> endpoints;

    private EndpointStep endpointStep;

    public CreateVMWizard(VMServiceModule node) {
        this.node = node;
        setWindowTitle("Create new Virtual Machine");
    }

    @Override
    public void addPages() {
        addPage(new SubscriptionStep(this));
        addPage(new SelectImageStep(this));
        addPage(new MachineSettingsStep(this));
        addPage(new CloudServiceStep(this));
        addPage(endpointStep = new EndpointStep(this));
    }

    @Override
    public boolean performFinish() {
//        final EndpointTableModel tableModel = (EndpointTableModel) endpointsTable.getModel();
        DefaultLoader.getIdeHelper().runInBackground(null, "Creating virtual machine...", false, true, "Creating virtual machine...", new Runnable() {
            @Override
            public void run() {
                try {
                    VirtualMachine virtualMachine = new VirtualMachine(
                            name,
                            cloudService.getName(),
                            cloudService.getProductionDeployment().getName(),
                            availabilitySet,
                            subnet,
                            size.getName(),
                            VirtualMachine.Status.Unknown,
                            subscription.getId()
                    );

                    virtualMachine.getEndpoints().addAll(endpointStep.getEndpointsList());

                    byte[] certData = new byte[0];

                    if (!certificate.isEmpty()) {
                        File certFile = new File(certificate);

                        if (certFile.exists()) {
                            FileInputStream certStream = null;

                            try {
                                certStream = new FileInputStream(certFile);
                                certData = new byte[(int) certFile.length()];

                                if (certStream.read(certData) != certData.length) {
                                    throw new Exception("Unable to process certificate: stream longer than informed size.");
                                }
                            } finally {
                                if (certStream != null) {
                                    try {
                                        certStream.close();
                                    } catch (IOException ignored) {
                                    }
                                }
                            }
                        }
                    }

                    AzureManagerImpl.getManager().createVirtualMachine(virtualMachine,
                            virtualMachineImage,
                            storageAccount,
                            virtualNetwork != null ? virtualNetwork.getName() : "",
                            userName,
                            password,
                            certData);

                    virtualMachine = AzureManagerImpl.getManager().refreshVirtualMachineInformation(virtualMachine);

                    final VirtualMachine vm = virtualMachine;

                    DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                node.addChildNode(new VMNode(node, vm));
                            } catch (AzureCmdException e) {
                                DefaultLoader.getUIHelper().showException("An error occurred while trying to refresh the list of virtual machines",
                                        e,
                                        "Error Refreshing VM List",
                                        false,
                                        true);
                            }
                        }
                    });
                } catch (Exception e) {
                    DefaultLoader.getUIHelper().showException("An error occurred while trying to create the specified virtual machine",
                            e,
                            "Error Creating Virtual Machine",
                            false,
                            true);
                    Activator.getDefault().log("Error Creating Virtual Machine", e);
                }
            }
        });
        return true;
    }

    @Override
    public boolean canFinish() {
        return getContainer().getCurrentPage() instanceof EndpointStep;
    }

    public List configStepList(Composite parent, final int step) {
        GridData gridData = new GridData();
        gridData.widthHint = 100;
//
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.grabExcessVerticalSpace = true;
        List createVmStepsList = new List(parent, SWT.BORDER);
        createVmStepsList.setItems(getStepTitleList());
        createVmStepsList.setSelection(step);
        createVmStepsList.setLayoutData(gridData);
        createVmStepsList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                List l = (List) e.widget;
                l.setSelection(step);
            }
        });
//        createVmStepsList.setEnabled(false);


//        jList.setBorder(new EmptyBorder(10, 0, 10, 0));

//        jList.setCellRenderer(new DefaultListCellRenderer() {
//            @Override
//            public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
//                return super.getListCellRendererComponent(jList, "  " + o.toString(), i, b, b1);
//            }
//        });
//
//        for (MouseListener mouseListener : jList.getMouseListeners()) {
//            jList.removeMouseListener(mouseListener);
//        }
//
//        for (MouseMotionListener mouseMotionListener : jList.getMouseMotionListeners()) {
//            jList.removeMouseMotionListener(mouseMotionListener);
//        }
        return createVmStepsList;
    }

    public Browser createImageDescriptor(Composite container) {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        Browser imageDescription = new Browser(container, SWT.NONE);
        imageDescription.setLayoutData(gridData);
        return imageDescription;
    }

    public String[] getStepTitleList() {
        return new String[]{
                "Subscription",
                "Select Image",
                "Machine Settings",
                "Cloud Service",
                "Endpoints"
        };
    }

    public String getHtmlFromVMImage(VirtualMachineImage virtualMachineImage) {
        String html = BASE_HTML_VM_IMAGE;
        html = html.replace("#TITLE#", virtualMachineImage.getLabel());
        html = html.replace("#DESCRIPTION#", virtualMachineImage.getDescription());
        html = html.replace("#PUBLISH_DATE#", new SimpleDateFormat("dd-M-yyyy").format(virtualMachineImage.getPublishedDate().getTime()));
        html = html.replace("#PUBLISH_NAME#", virtualMachineImage.getPublisherName());
        html = html.replace("#OS#", virtualMachineImage.getOperatingSystemType());
        html = html.replace("#LOCATION#", virtualMachineImage.getLocation());

        html = html.replace("#PRIVACY#", virtualMachineImage.getPrivacyUri().isEmpty()
                ? ""
                : "<p><a href='" + virtualMachineImage.getPrivacyUri() + "' style=\"font-family: 'Segoe UI';font-size: 8pt;\">Privacy statement</a></p>");


        html = html.replace("#LICENCE#", virtualMachineImage.getEulaUri().isEmpty()
                ? ""
                : "<p><a href='" + virtualMachineImage.getEulaUri() + "' style=\"font-family: 'Segoe UI';font-size: 8pt;\">Licence agreement</a></p>");

        return html;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public VirtualMachineImage getVirtualMachineImage() {
        return virtualMachineImage;
    }

    public void setVirtualMachineImage(VirtualMachineImage virtualMachineImage) {
        this.virtualMachineImage = virtualMachineImage;
    }

    public VirtualMachineSize getSize() {
        return size;
    }

    public void setSize(VirtualMachineSize size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public CloudService getCloudService() {
        return cloudService;
    }

    public void setCloudService(CloudService cloudService) {
        this.cloudService = cloudService;
    }

    public boolean isFilterByCloudService() {
        return filterByCloudService;
    }

    public void setFilterByCloudService(boolean filterByCloudService) {
        this.filterByCloudService = filterByCloudService;
    }

    public StorageAccount getStorageAccount() {
        return storageAccount;
    }

    public void setStorageAccount(StorageAccount storageAccount) {
        this.storageAccount = storageAccount;
    }

    public VirtualNetwork getVirtualNetwork() {
        return virtualNetwork;
    }

    public void setVirtualNetwork(VirtualNetwork virtualNetwork) {
        this.virtualNetwork = virtualNetwork;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    public String getAvailabilitySet() {
        return availabilitySet;
    }

    public void setAvailabilitySet(String availabilitySet) {
        this.availabilitySet = availabilitySet;
    }

    public java.util.List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(java.util.List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }
}
