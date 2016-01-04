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

import com.microsoft.azureexplorer.forms.CreateCloudServiceForm;
import com.microsoft.azureexplorer.forms.CreateStorageAccountForm;
import com.microsoftopentechnologies.tooling.msservices.components.DefaultLoader;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoftopentechnologies.tooling.msservices.model.storage.StorageAccount;
import com.microsoftopentechnologies.tooling.msservices.model.vm.CloudService;
import com.microsoftopentechnologies.tooling.msservices.model.vm.VirtualMachineImage;
import com.microsoftopentechnologies.tooling.msservices.model.vm.VirtualNetwork;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CloudServiceStep extends WizardPage {
    private static final String PRODUCTION = "Production";
    private static final String NONE_OPTION = "(None)";

    private CreateVMWizard wizard;

    private Label cloudServiceLabel;
    private Combo cloudServiceComboBox;
    private Label storageLabel;
    private Combo storageComboBox;
    private Button availabilitySetCheckBox;
    private Combo availabilityComboBox;
    private Label networkLabel;
    private Combo networkComboBox;
    private Label subnetLabel;
    private Combo subnetComboBox;
    private Browser imageDescription;

    private Map<String, CloudService> cloudServices;
    private final Lock csLock = new ReentrantLock();
    private final Condition csInitialized = csLock.newCondition();

    private Map<String, VirtualNetwork> virtualNetworks;
    private final Lock vnLock = new ReentrantLock();
    private final Condition vnInitialized = vnLock.newCondition();

    private Map<String, StorageAccount> storageAccounts;
    private final Lock saLock = new ReentrantLock();
    private final Condition saInitialized = saLock.newCondition();

    protected CloudServiceStep(CreateVMWizard wizard) {
        super("Create new Virtual Machine", "Cloud Service Settings", null);
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

        wizard.configStepList(container, 3);

        createCloudServiceSettings(container);

        imageDescription = wizard.createImageDescriptor(container);
//        cloudServiceComboBox.setRenderer(new ListCellRendererWrapper<Object>() {
//            @Override
//            public void customize(JList jList, Object o, int i, boolean b, boolean b1) {
//                if (o instanceof CloudService) {
//                    CloudService cs = (CloudService) o;
//
//                    if (cs.getProductionDeployment().getVirtualNetwork().isEmpty()) {
//                        setText(String.format("%s (%s)", cs.getName(),
//                                !cs.getLocation().isEmpty() ? cs.getLocation() : cs.getAffinityGroup()));
//                    } else {
//                        setText(String.format("%s (%s - %s)", cs.getName(),
//                                cs.getProductionDeployment().getVirtualNetwork(),
//                                !cs.getLocation().isEmpty() ? cs.getLocation() : cs.getAffinityGroup()));
//                    }
//                }
//            }
//        });
//
//        storageComboBox.setRenderer(new ListCellRendererWrapper<Object>() {
//            @Override
//            public void customize(JList jList, Object o, int i, boolean b, boolean b1) {
//                if (o instanceof StorageAccount) {
//                    StorageAccount sa = (StorageAccount) o;
//                    setText(String.format("%s (%s)", sa.getName(),
//                            !sa.getLocation().isEmpty() ? sa.getLocation() : sa.getAffinityGroup()));
//                }
//            }
//        });


        storageComboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                validateNext();
            }
        });

//        networkComboBox.setRenderer(new ListCellRendererWrapper<Object>() {
//            @Override
//            public void customize(JList jList, Object o, int i, boolean b, boolean b1) {
//                if (o instanceof VirtualNetwork) {
//                    VirtualNetwork vn = (VirtualNetwork) o;
//                    setText(String.format("%s (%s)", vn.getName(), vn.getLocation()));
//                }
//            }
//        });

        subnetComboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                wizard.setSubnet(subnetComboBox.getText());
                validateNext();
            }
        });

        availabilitySetCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                availabilityComboBox.setEnabled(availabilitySetCheckBox.getSelection());
            }
        });

        this.setControl(container);
    }

    private void createCloudServiceSettings(Composite container) {
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

        cloudServiceLabel = new Label(composite, SWT.LEFT);
        cloudServiceLabel.setText("Cloud service");
        cloudServiceComboBox = new Combo(composite, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        cloudServiceComboBox.setLayoutData(gridData);

        networkLabel = new Label(composite, SWT.LEFT);
        networkLabel.setText("Virtual Network");
        networkComboBox = new Combo(composite, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        networkComboBox.setLayoutData(gridData);

        subnetLabel = new Label(composite, SWT.LEFT);
        subnetLabel.setText("Subnet");
        subnetComboBox = new Combo(composite, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        subnetComboBox.setLayoutData(gridData);

        storageLabel = new Label(composite, SWT.LEFT);
        storageLabel.setText("Storage account");
        storageComboBox = new Combo(composite, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        storageComboBox.setLayoutData(gridData);

        availabilitySetCheckBox = new Button(composite, SWT.CHECK);
        availabilitySetCheckBox.setText("Specify an availability set");
        availabilityComboBox = new Combo(composite, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        availabilityComboBox.setLayoutData(gridData);
    }

    @Override
    public String getTitle() {
        setPageComplete(false);

        final VirtualMachineImage virtualMachineImage = wizard.getVirtualMachineImage();
        imageDescription.setText(wizard.getHtmlFromVMImage(virtualMachineImage));

        retrieveCloudServices(wizard.getVirtualNetwork(), wizard.isFilterByCloudService());
        retrieveVirtualNetworks();
        retrieveStorageAccounts(wizard.getCloudService());

        if (wizard.isFilterByCloudService()) {
            fillCloudServices(null, true);
        } else {
            fillVirtualNetworks(null, true);
        }
        return super.getTitle();
    }

    @Override
    public IWizardPage getNextPage() {
        if (!(storageComboBox.getData(storageComboBox.getText()) instanceof StorageAccount)) {
            DefaultLoader.getUIHelper().showError("Must select a storage account", "Error creating the virtual machine");
            return this;
        }

        wizard.setCloudService((CloudService) cloudServiceComboBox.getData(cloudServiceComboBox.getText()));
        wizard.setStorageAccount((StorageAccount) storageComboBox.getData(storageComboBox.getText()));
        wizard.setVirtualNetwork((VirtualNetwork) networkComboBox.getData(networkComboBox.getText()));
        wizard.setSubnet(subnetComboBox.isEnabled() ? subnetComboBox.getText() : "");
//        wizard.setAvailabilitySet(availabilitySetCheckBox.getSelection() ?
//                (availabilityComboBox.getText() == null
//                        ? availabilityComboBox.getEditor().getItem().toString()
//                        : availabilityComboBox.getText())
//                : "");
        wizard.setAvailabilitySet(availabilityComboBox.getText());

        return super.getNextPage();
    }

    private void retrieveCloudServices(final VirtualNetwork selectedVN, final boolean cascade) {
        DefaultLoader.getIdeHelper().runInBackground(null, "Loading cloud services...", false, true, "Loading cloud services...", new Runnable() {
            @Override
            public void run() {
                csLock.lock();
                try {
                    if (cloudServices == null) {
                        try {
                            List<CloudService> services = AzureManagerImpl.getManager().getCloudServices(wizard.getSubscription().getId().toString());
                            cloudServices = new TreeMap<String, CloudService>();

                            for (CloudService cloudService : services) {
                                if (cloudService.getProductionDeployment().getComputeRoles().size() == 0) {
                                    cloudServices.put(cloudService.getName(), cloudService);
                                }
                            }
                            csInitialized.signalAll();
                        } catch (AzureCmdException e) {
                            cloudServices = null;
                            DefaultLoader.getUIHelper().showException("An error occurred while trying to retrieve the cloud services list",
                                    e, "Error Retrieving Cloud Services", false, true);
                        }
                    }
                } finally {
                    csLock.unlock();
                }
            }
        });
        if (cloudServices == null) {
            final String createCS = "<< Create new cloud service >>";
           cloudServiceComboBox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (createCS.equals(cloudServiceComboBox.getText())) {
                        showNewCloudServiceForm(selectedVN, cascade);
                    }
                }
            });
            DefaultLoader.getIdeHelper().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    cloudServiceComboBox.setItems(new String[]{createCS, "<Loading...>"});
                }
            });
        }
    }

    private void fillCloudServices(final VirtualNetwork selectedVN,
                                   final boolean cascade) {
        DefaultLoader.getIdeHelper().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                CloudService selectedCS = wizard.getCloudService();
                wizard.setFilterByCloudService(cascade);

                csLock.lock();

                try {
                    while (cloudServices == null) {
                        csInitialized.await();
                    }

                    if (selectedCS != null && !cloudServices.containsKey(selectedCS.getName())) {
                        cloudServices.put(selectedCS.getName(), selectedCS);
                    }
                } catch (InterruptedException e) {
                    DefaultLoader.getUIHelper().showException("An error occurred while trying to load the cloud services list", e,
                            "Error Loading Cloud Services", false, true);
                } finally {
                    csLock.unlock();
                }

                refreshCloudServices(selectedCS, selectedVN, cascade);
            }
        });
    }

    private void refreshCloudServices(final CloudService selectedCS,
                                      final VirtualNetwork selectedVN,
                                      final boolean cascade) {
//        final DefaultComboBoxModel refreshedCSModel = getCloudServiceModel(selectedCS, selectedVN, cascade);
        final Collection<CloudService> services = filterCS(selectedVN);

        DefaultLoader.getIdeHelper().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                populateCloudServiceCombo(selectedCS, selectedVN, cascade, services);
//                cloudServiceComboBox.setModel(refreshedCSModel);
            }
        });
    }

    private void populateCloudServiceCombo(CloudService selectedCS, final VirtualNetwork selectedVN,
                                                      final boolean cascade, Collection<CloudService> services) {
//        Collection<CloudService> services = filterCS(selectedVN);
        cloudServiceComboBox.removeAll();
        final String createCS = "<< Create new cloud service >>";
        cloudServiceComboBox.add(createCS);
        for (CloudService cloudService : services) {
            cloudServiceComboBox.add(cloudService.getName());
            cloudServiceComboBox.setData(cloudService.getName(), cloudService);
        }
        final String clear = "(Clear selection...)";
        cloudServiceComboBox.addSelectionListener(new SelectionAdapter() {
            private boolean doCascade = cascade;
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (clear.equals(cloudServiceComboBox.getText())) {
                    cloudServiceComboBox.remove(clear);
                    cloudServiceComboBox.setText("");
//                    removeElement(o);
//                    setSelectedItem(null);
                } else {
                    if (createCS.equals(cloudServiceComboBox.getText())) {
                        showNewCloudServiceForm(selectedVN, doCascade);
                    } else if ((CloudService) cloudServiceComboBox.getData(cloudServiceComboBox.getText()) != null) {
                        CloudService cloudService = (CloudService) cloudServiceComboBox.getData(cloudServiceComboBox.getText());
                        wizard.setCloudService(cloudService);

                        if (cloudServiceComboBox.indexOf(clear) == -1) {
                            cloudServiceComboBox.add(clear);
                        }


                        if (doCascade) {
                            fillVirtualNetworks(cloudService, false);
                        }
                        fillStorage((CloudService) cloudServiceComboBox.getData(cloudServiceComboBox.getText()));
                        fillAvailabilitySets(cloudService);
                    } else {
                        wizard.setCloudService(null);

                        if (doCascade) {
                            fillVirtualNetworks(null, false);
                        }

                        fillStorage(null);
                        fillAvailabilitySets(null);
                    }

                    doCascade = doCascade || selectedVN == null;
                }
            }
        });


//        DefaultComboBoxModel refreshedCSModel = new DefaultComboBoxModel(services.toArray()) {
//            private final String clear = "(Clear selection...)";
//            private boolean doCascade = cascade;
//
//            @Override
//            public void setSelectedItem(Object o) {
//                if (clear.equals(o)) {
//                    removeElement(o);
//                    setSelectedItem(null);
//                } else {
//                    if (createCS.equals(o)) {
//                        showNewCloudServiceForm(selectedVN, doCascade);
//                    } else if (o instanceof CloudService) {
//                        super.setSelectedItem(o);
//                        wizard.setCloudService((CloudService) o);
//
//                        if (getIndexOf(clear) == -1) {
//                            addElement(clear);
//                        }
//
//                        if (doCascade) {
//                            fillVirtualNetworks((CloudService) o, false);
//                        }
//
//                        fillStorage((CloudService) o);
//                        fillAvailabilitySets((CloudService) o);
//                    } else {
//                        super.setSelectedItem(o);
//                        wizard.setCloudService(null);
//
//                        if (doCascade) {
//                            fillVirtualNetworks(null, false);
//                        }
//
//                        fillStorage(null);
//                        fillAvailabilitySets(null);
//                    }
//
//                    doCascade = doCascade || selectedVN == null;
//                }
//            }
//        };

//        refreshedCSModel.insertElementAt(createCS, 0);

        if (selectedCS != null && services.contains(selectedCS) && (cascade || selectedVN != null)) {
//            refreshedCSModel.setSelectedItem(selectedCS);
            cloudServiceComboBox.setText(selectedCS.getName());
        } else {
            wizard.setCloudService(null);
            cloudServiceComboBox.setText("");
        }
    }

    private Collection<CloudService> filterCS(VirtualNetwork selectedVN) {
        Collection<CloudService> services = selectedVN == null ? cloudServices.values() : new Vector<CloudService>();

        if (selectedVN != null) {
            for (CloudService cloudService : cloudServices.values()) {
                if ((isDeploymentEmpty(cloudService, PRODUCTION) && areSameRegion(cloudService, selectedVN)) ||
                        areSameNetwork(cloudService, selectedVN)) {
                    services.add(cloudService);
                }
            }
        }

        return services;
    }

    private void retrieveVirtualNetworks() {
        DefaultLoader.getIdeHelper().runInBackground(null, "Loading virtual networks...", false, true, "Loading virtual networks...", new Runnable() {
            @Override
            public void run() {
                vnLock.lock();

                try {
                    if (virtualNetworks == null) {
                        try {
                            List<VirtualNetwork> networks = AzureManagerImpl.getManager().getVirtualNetworks(wizard.getSubscription().getId().toString());
                            virtualNetworks = new TreeMap<String, VirtualNetwork>();

                            for (VirtualNetwork virtualNetwork : networks) {
                                virtualNetworks.put(virtualNetwork.getName(), virtualNetwork);
                            }

                            vnInitialized.signalAll();
                        } catch (AzureCmdException e) {
                            virtualNetworks = null;
                            DefaultLoader.getUIHelper().showException("An error occurred while trying to retrieve the virtual networks list",
                                    e, "Error Retrieving Virtual Networks", false, true);
                        }
                    }
                } finally {
                    vnLock.unlock();
                }
            }
        });

        if (virtualNetworks == null) {
            DefaultLoader.getIdeHelper().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    networkComboBox.setItems(new String[]{"<Loading...>"});

                    subnetComboBox.removeAll();
                    subnetComboBox.setEnabled(false);
                }
            });
        }
    }

    private void fillVirtualNetworks(final CloudService selectedCS, final boolean cascade) {
        DefaultLoader.getIdeHelper().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                VirtualNetwork selectedVN = wizard.getVirtualNetwork();
                String selectedSN = wizard.getSubnet();

                vnLock.lock();

                try {
                    while (virtualNetworks == null) {
                        vnInitialized.await();
                    }
                } catch (InterruptedException e) {
                    DefaultLoader.getUIHelper().showException("An error occurred while trying load the virtual networks list", e,
                            "Error Loading Virtual Networks", false, true);
                } finally {
                    vnLock.unlock();
                }

                refreshVirtualNetworks(selectedCS, selectedVN, selectedSN, cascade);
            }
        });
    }

    private void refreshVirtualNetworks(final CloudService selectedCS,
                                        final VirtualNetwork selectedVN,
                                        final String selectedSN,
                                        final boolean cascade) {
        final Vector<VirtualNetwork> networks = filterVN(selectedCS);
        DefaultLoader.getIdeHelper().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                populateVirtualNetworkCombo(selectedCS, selectedVN, selectedSN, cascade, networks);
                networkComboBox.setEnabled(selectedCS == null || isDeploymentEmpty(selectedCS, PRODUCTION));
            }
        });
    }

    private void populateVirtualNetworkCombo(final CloudService selectedCS,
                                                        VirtualNetwork selectedVN,
                                                        final String selectedSN,
                                                        final boolean cascade,
                                                        Vector<VirtualNetwork> networks) {
        if (selectedCS != null && !selectedCS.getProductionDeployment().getVirtualNetwork().isEmpty()) {
            selectedVN = networks.size() == 1 ? networks.get(0) : null;
        }
        networkComboBox.removeAll();
        for (VirtualNetwork network : networks) {
            networkComboBox.add(network.getName());
            networkComboBox.setData(network.getName(), network);
        }
        networkComboBox.addSelectionListener(new SelectionAdapter() {
            private boolean doCascade = cascade;
            @Override
            public void widgetSelected(SelectionEvent e) {
                doCascade = selectNetwork(selectedCS, selectedSN, doCascade);
            }
        });
        if (selectedVN != null && networks.contains(selectedVN) && (cascade || selectedCS != null)) {
            networkComboBox.setText(selectedVN.getName());
            selectNetwork(selectedCS, selectedSN, cascade);
        } else {
//            wizard.setVirtualNetwork(null);
            networkComboBox.setText("");
            selectNetwork(selectedCS, selectedSN, cascade);
        }
    }

    private boolean selectNetwork(CloudService selectedCS, final String selectedSN, boolean doCascade) {
        if (NONE_OPTION.equals(networkComboBox.getText())) {
            networkComboBox.remove(NONE_OPTION);
            networkComboBox.setText("");
        } else {
            if (networkComboBox.getData(networkComboBox.getText()) != null) {
                final VirtualNetwork virtualNetwork = (VirtualNetwork) networkComboBox.getData(networkComboBox.getText());
                wizard.setVirtualNetwork(virtualNetwork);

                if (networkComboBox.indexOf(NONE_OPTION) == -1) {
                    networkComboBox.add(NONE_OPTION, 0);
                }

                DefaultLoader.getIdeHelper().invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        boolean validSubnet = false;

                        subnetComboBox.removeAll();

                        for (String subnet : virtualNetwork.getSubnets()) {
                            subnetComboBox.add(subnet);

                            if (subnet.equals(selectedSN)) {
                                validSubnet = true;
                            }
                        }

                        if (validSubnet) {
                            subnetComboBox.setText(selectedSN);
                        } else {
                            wizard.setSubnet(null);
                            subnetComboBox.setText(null);
                        }

                        subnetComboBox.setEnabled(true);
                    }
                });

                if (doCascade) {
                    fillCloudServices(virtualNetwork, false);
                }
            } else {
                wizard.setVirtualNetwork(null);

                DefaultLoader.getIdeHelper().invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        subnetComboBox.removeAll();
                        subnetComboBox.setEnabled(false);
                    }
                });

                if (doCascade) {
                    fillCloudServices(null, false);
                }
            }
            doCascade = doCascade || selectedCS == null;
        }
        return doCascade;
    }

    private Vector<VirtualNetwork> filterVN(CloudService selectedCS) {
        Vector<VirtualNetwork> networks = selectedCS == null ?
                new Vector<VirtualNetwork>(virtualNetworks.values()) :
                new Vector<VirtualNetwork>();

        if (selectedCS != null) {
            if (isDeploymentEmpty(selectedCS, PRODUCTION)) {
                for (VirtualNetwork virtualNetwork : virtualNetworks.values()) {
                    if (areSameRegion(selectedCS, virtualNetwork)) {
                        networks.add(virtualNetwork);
                    }
                }
            } else if (!selectedCS.getProductionDeployment().getVirtualNetwork().isEmpty()) {
                for (VirtualNetwork virtualNetwork : virtualNetworks.values()) {
                    if (areSameNetwork(selectedCS, virtualNetwork)) {
                        networks.add(virtualNetwork);
                        break;
                    }
                }
            }
        }

        return networks;
    }

    private void retrieveStorageAccounts(final CloudService selectedCS) {
        DefaultLoader.getIdeHelper().runInBackground(null, "Loading storage accounts...", false, true, "Loading storage accounts...", new Runnable() {
            @Override
            public void run() {
                saLock.lock();

                try {
                    if (storageAccounts == null) {
                        try {
                            List<StorageAccount> accounts = AzureManagerImpl.getManager().getStorageAccounts(wizard.getSubscription().getId().toString());
                            storageAccounts = new TreeMap<String, StorageAccount>();

                            for (StorageAccount storageAccount : accounts) {
                                storageAccounts.put(storageAccount.getName(), storageAccount);
                            }

                            saInitialized.signalAll();
                        } catch (AzureCmdException e) {
                            storageAccounts = null;
                            DefaultLoader.getUIHelper().showException("An error occurred while trying to retrieve the storage accounts list",
                                    e, "Error Retrieving Storage Accounts", false, true);
                        }
                    }
                } finally {
                    saLock.unlock();
                }
            }
        });

        if (storageAccounts == null) {
            final String createSA = "<< Create new storage account >>";

            storageComboBox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (createSA.equals(cloudServiceComboBox.getText())) {
                        showNewStorageForm(selectedCS);
                    }
                }
            });
//            loadingSAModel.setSelectedItem(null);

            DefaultLoader.getIdeHelper().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    storageComboBox.setItems(new String[]{createSA, "<Loading...>"});
                }
            });
        }
    }

    private void fillStorage(final CloudService selectedCS) {
        DefaultLoader.getIdeHelper().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                StorageAccount selectedSA = wizard.getStorageAccount();

                saLock.lock();

                try {
                    while (storageAccounts == null) {
                        saInitialized.await();
                    }

                    if (selectedSA != null && !storageAccounts.containsKey(selectedSA.getName())) {
                        storageAccounts.put(selectedSA.getName(), selectedSA);
                    }
                } catch (InterruptedException e) {
                    DefaultLoader.getUIHelper().showException("An error occurred while trying load the storage accounts list", e,
                            "Error Loading Storage Accounts", false, true);
                } finally {
                    saLock.unlock();
                }

                refreshStorageAccounts(selectedCS, selectedSA);
            }
        });
    }

    private void refreshStorageAccounts(final CloudService selectedCS, final StorageAccount selectedSA) {
        final Vector<StorageAccount> accounts = filterSA(selectedCS);

        DefaultLoader.getIdeHelper().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                populateStorageAccountCombo(selectedCS, selectedSA, accounts);
                setPageComplete(selectedCS != null &&
                        selectedSA != null &&
                        selectedSA.getLocation().equals(selectedCS.getLocation()));
            }
        });
    }

    private void populateStorageAccountCombo(final CloudService selectedCS, StorageAccount selectedSA, Vector<StorageAccount> accounts) {
        storageComboBox.removeAll();
        final String createSA = "<< Create new storage account >>";
        storageComboBox.add(createSA);
        for (StorageAccount storageAccount : accounts) {
            storageComboBox.add(storageAccount.getName());
            storageComboBox.setData(storageAccount.getName(), storageAccount);
        }
        storageComboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (createSA.equals(storageComboBox.getText())) {
                    showNewStorageForm(selectedCS);
                } else {
                    wizard.setStorageAccount((StorageAccount) storageComboBox.getData(storageComboBox.getText()));
                }
            }
        });

//        refreshedSAModel.insertElementAt(createSA, 0);

        if (accounts.contains(selectedSA)) {
            storageComboBox.setText(selectedSA.getName());
        } else {
            storageComboBox.setText("");
            wizard.setStorageAccount(null);
        }
    }

    private Vector<StorageAccount> filterSA(CloudService selectedCS) {
        Vector<StorageAccount> accounts = new Vector<StorageAccount>();

        if (selectedCS != null) {
            for (StorageAccount storageAccount : storageAccounts.values()) {
                if ((!storageAccount.getLocation().isEmpty() &&
                        storageAccount.getLocation().equals(selectedCS.getLocation())) ||
                        (!storageAccount.getAffinityGroup().isEmpty() &&
                                storageAccount.getAffinityGroup().equals(selectedCS.getAffinityGroup()))) {
                    accounts.add(storageAccount);
                }
            }
        }

        return accounts;
    }

    private void fillAvailabilitySets(final CloudService selectedCS) {
        DefaultLoader.getIdeHelper().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                if (selectedCS != null) {
                    String[] items = new String[selectedCS.getProductionDeployment().getAvailabilitySets().size()];
                    availabilityComboBox.setItems(selectedCS.getProductionDeployment().getAvailabilitySets().toArray(items));
                } else {
                    availabilityComboBox.setItems(new String[]{});
                }
            }
        });
    }

    private void showNewCloudServiceForm(final VirtualNetwork selectedVN, final boolean cascade) {
        final CreateCloudServiceForm form = new CreateCloudServiceForm(new Shell(), wizard.getSubscription());

        form.setOnCreate(new Runnable() {
            @Override
            public void run() {
                DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        CloudService newCloudService = form.getCloudService();

                        if (newCloudService != null) {
                            wizard.setCloudService(newCloudService);
                            fillCloudServices(selectedVN, cascade);
                        }
                    }
                });
            }
        });

        form.open();
    }

    private void showNewStorageForm(final CloudService selectedCS) {
        final CreateStorageAccountForm form = new CreateStorageAccountForm(new Shell(), wizard.getSubscription());
        
        form.setOnCreate(new Runnable() {
            @Override
            public void run() {
                DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        StorageAccount newStorageAccount = form.getStorageAccount();

                        if (newStorageAccount != null) {
                            wizard.setStorageAccount(newStorageAccount);
                            fillStorage(selectedCS);
                        }
                    }
                });
            }
        });
        form.open();
    }

    private static boolean isDeploymentEmpty(CloudService cloudService, String deploymentSlot) {
        if (deploymentSlot.equals(PRODUCTION)) {
            return cloudService.getProductionDeployment().getName().isEmpty();
        } else {
            return cloudService.getStagingDeployment().getName().isEmpty();
        }

    }

    private static boolean areSameRegion(CloudService cloudService, VirtualNetwork virtualNetwork) {
        return (!virtualNetwork.getLocation().isEmpty() &&
                virtualNetwork.getLocation().equals(cloudService.getLocation())) ||
                (!virtualNetwork.getAffinityGroup().isEmpty() &&
                        virtualNetwork.getAffinityGroup().equals(cloudService.getAffinityGroup()));
    }

    private static boolean areSameNetwork(CloudService cloudService, VirtualNetwork virtualNetwork) {
        return virtualNetwork.getName().equals(cloudService.getProductionDeployment().getVirtualNetwork());
    }

    private void validateNext() {
        setPageComplete(storageComboBox.getData(storageComboBox.getText()) instanceof StorageAccount &&
                (!subnetComboBox.isEnabled() || !subnetComboBox.getText().isEmpty()));
    }
}
