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

import com.microsoftopentechnologies.tooling.msservices.model.vm.Endpoint;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.*;

public class EndpointStep extends WizardPage {
    private static Endpoint[] DEFAULT_ENDPOINTS = {
            new Endpoint("Remote Desktop", "TCP", 3389, 3389),
            new Endpoint("Powershell", "TCP", 5986, 5986),
            new Endpoint("Http", "TCP", 80, 80),
            new Endpoint("SSH", "TCP", 22, 22),
            new Endpoint("FTP", "TCP", 21, 21),
            new Endpoint("SMTP", "TCP", 25, 25),
            new Endpoint("MYSQL", "TCP", 3306, 3306),
            new Endpoint("MSSQL", "TCP", 1433, 1433),
            new Endpoint("DNS", "TCP", 53, 53),
            new Endpoint("POP3", "TCP", 110, 110),
            new Endpoint("POP3S", "TCP", 995, 995),
            new Endpoint("Https", "TCP", 443, 443),
            new Endpoint("SMTPS", "TCP", 587, 587),
            new Endpoint("LDAP", "TCP", 389, 389),
            new Endpoint("IMAP", "TCP", 143, 143),
            new Endpoint("WebDeploy", "TCP", 8172, 8172),
    };
    private static String[] COLUMN_NAMES = {"Port Name", "Public Port", "Private Port", "Protocol"};

    private CreateVMWizard wizard;
    private Label endpointLabel;
    private Table endpointsTable;
    private TableViewer tableViewer;
    private Label portNameLabel;
    private Combo portNameComboBox;
    private Button addButton;
    private Label endpointNote;

    private java.util.List<Endpoint> endpointsList = new ArrayList<Endpoint>();


    public EndpointStep(CreateVMWizard wizard) {
        super("Create new Virtual Machine", "Endpoint Settings", null);
        this.wizard = wizard;
    }

    @Override
    public void createControl(Composite parent) {
        GridLayout gridLayout = new GridLayout(2, false);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        Composite container = new Composite(parent, 0);
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);

        wizard.configStepList(container, 4);

        createEndpointSettings(container);

        this.setControl(container);
    }

    private void createEndpointSettings(Composite container) {
        final Composite composite = new Composite(container, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.BEGINNING;
        composite.setLayout(gridLayout);
        composite.setLayoutData(gridData);

        endpointLabel = new Label(composite, SWT.LEFT);
        endpointLabel.setText("Enable the following public endpoints:");

        createEndpointsTable(composite);

        createPortNameCombo(composite);

        endpointNote = new Label(composite, SWT.FILL);
        endpointNote.setText("Note: Enabling public endpoints will make services on the virtual machine accessible from\n" +
                "the internet. Enabling the endpoint does not add the service to the virtual machine.\n" +
                "You should ensure that the service is installed and configured on the virtual machine.");
    }

    private void createEndpointsTable(Composite composite) {
        endpointsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);

        endpointsTable.setHeaderVisible(true);
        endpointsTable.setLinesVisible(true);

        GridData gridData = new GridData();
        gridData.heightHint = 75;
        gridData.horizontalAlignment = SWT.FILL;

        GridLayout gridLayoutTable = new GridLayout();
        gridLayoutTable.numColumns = 5;
        gridLayoutTable.marginRight = 0;
        endpointsTable.setLayout(gridLayoutTable);
        endpointsTable.setLayoutData(gridData);

        TableColumn portName = new TableColumn(endpointsTable, SWT.FILL);
        portName.setText(COLUMN_NAMES[0]);
        portName.setWidth(150);

        TableColumn publicPort = new TableColumn(endpointsTable, SWT.FILL);
        publicPort.setText(COLUMN_NAMES[1]);
        publicPort.setWidth(80);

        TableColumn privatePort = new TableColumn(endpointsTable, SWT.FILL);
        privatePort.setText(COLUMN_NAMES[2]);
        privatePort.setWidth(80);

        TableColumn protocol = new TableColumn(endpointsTable, SWT.FILL);
        protocol.setText(COLUMN_NAMES[3]);
        protocol.setWidth(80);

        TableColumn remove = new TableColumn(endpointsTable, SWT.FILL);

        tableViewer = new TableViewer(endpointsTable);
        tableViewer.setUseHashlookup(true);
        tableViewer.setColumnProperties(COLUMN_NAMES);

        CellEditor[] editors = new CellEditor[4];

        editors[0] = new TextCellEditor(endpointsTable);
        editors[1] = new TextCellEditor(endpointsTable);
        editors[2] = new TextCellEditor(endpointsTable);
        editors[3] = new ComboBoxCellEditor(endpointsTable, new String[] {"TCP", "UDP"}, SWT.READ_ONLY);

        tableViewer.setCellEditors(editors);
        tableViewer.setContentProvider(new EndpointsContentProvider());
        tableViewer.setLabelProvider(new EndpointsLabelProvider());
        tableViewer.setCellModifier(new EndpointsCellModifier());
    }

    /**
     * Content provider class for endpoints table,
     * which determines the input for the table.
     *
     */
    private class EndpointsContentProvider implements IStructuredContentProvider {
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object arg0) {
            return endpointsList.toArray();
        }
    }

    private class EndpointsLabelProvider implements ITableLabelProvider {

        @Override
        public void removeListener(ILabelProviderListener arg0) {
        }

        @Override
        public boolean isLabelProperty(Object arg0, String arg1) {
            return false;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void addListener(ILabelProviderListener arg0) {
        }

        @Override
        public String getColumnText(Object element, int colIndex) {
                Endpoint endpoint = (Endpoint) element;
            switch (colIndex) {
                case 0:
                    return endpoint.getName();
                case 1:
                    return "" + endpoint.getPublicPort();
                case 2:
                    return "" + endpoint.getPrivatePort();
                case 3:
                    return endpoint.getProtocol();
                default:
                    return "";
            }
        }

        @Override
        public Image getColumnImage(Object arg0, int arg1) {
            return null;
        }
    }

    private class EndpointsCellModifier implements ICellModifier {
        @Override
        public void modify(Object waEndpoint, String columnName, Object modifiedVal) {
            TableItem tblItem = (TableItem) waEndpoint;
            Endpoint endpoint = (Endpoint) tblItem.getData();
            if (columnName.equals(COLUMN_NAMES[0])) {
                endpoint.setName(modifiedVal.toString());
            } else if (columnName.equals(COLUMN_NAMES[1])) {
                try {
                    int publicPort = Integer.parseInt(modifiedVal.toString());
                    endpoint.setPublicPort(publicPort);
                } catch (NumberFormatException ignored) {
                }
            } else if (columnName.equals(COLUMN_NAMES[2])) {
                try {
                    int privatePort = Integer.parseInt(modifiedVal.toString());
                    endpoint.setPrivatePort(privatePort);
                } catch (NumberFormatException ignored) {
                }
            } else if (columnName.equals(COLUMN_NAMES[3])) {
                endpoint.setProtocol(modifiedVal.equals(0) ? "TCP" : "UDP");
            }
            tableViewer.refresh();
        }

        public Object getValue(Object element, String property) {
            Endpoint endpoint = (Endpoint) element;

            if (property.equals(COLUMN_NAMES[0])) {
                return endpoint.getName();
            } else if (property.equals(COLUMN_NAMES[1])) {
                return  String.valueOf(endpoint.getPublicPort());
            } else if (property.equals(COLUMN_NAMES[2])) {
                return String.valueOf(endpoint.getPrivatePort());
            } else if (property.equals(COLUMN_NAMES[3])) {
                if ("UDP".equals(endpoint.getProtocol())) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return "";
        }

        /**
         * Determines whether a particular cell can be modified or not.
         * @return boolean
         */
        @Override
        public boolean canModify(Object element, String property) {
            return true;
        }
    }

    private void createPortNameCombo(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        composite.setLayout(gridLayout);
        composite.setLayoutData(gridData);

        portNameLabel = new Label(composite, SWT.LEFT);
        portNameLabel.setText("Port Name");

        portNameComboBox = new Combo(composite, SWT.NONE);
        for (Endpoint endpoint : DEFAULT_ENDPOINTS) {
            portNameComboBox.add(endpoint.getName());
            portNameComboBox.setData(endpoint.getName(), endpoint);
        }

        addButton = new Button(composite, SWT.PUSH);
        addButton.setText("Add");
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Endpoint endpoint = (Endpoint) portNameComboBox.getData(portNameComboBox.getText());
                endpointsList.add(new Endpoint(endpoint.getName(), endpoint.getProtocol(), endpoint.getPrivatePort(), endpoint.getPublicPort()));
                tableViewer.refresh();
            }
        });
    }

    @Override
    public String getTitle() {
        if (wizard.getEndpoints() == null) {
            if (wizard.getVirtualMachineImage().getOperatingSystemType().equals("Windows")) {
                endpointsList.add(new Endpoint("Powershell", "TCP", 5983, 5983));
                endpointsList.add(new Endpoint("Remote Desktop", "TCP", 3389, 3389));
            } else {
                endpointsList.add(new Endpoint("SSH", "TCP", 22, 22));
            }
        } else {
            endpointsList.clear();;
            endpointsList.addAll(wizard.getEndpoints());
        }

        tableViewer.setInput(endpointsList);
        return super.getTitle();
    }

    public java.util.List<Endpoint> getEndpointsList() {
        return endpointsList;
    }

    public IWizardPage getPreviousPage() {
        wizard.setEndpoints(endpointsList);
        return super.getPreviousPage();
    }
}
