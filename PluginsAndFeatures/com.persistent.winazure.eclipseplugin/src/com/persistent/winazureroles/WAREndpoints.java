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
package com.persistent.winazureroles;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.exception.AzureCommonsException;
import com.microsoftopentechnologies.azurecommons.model.RoleAndEndpoint;
import com.microsoftopentechnologies.azurecommons.roleoperations.WAREndpointsUtilMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;

import waeclipseplugin.Activator;

/**
 * Property page for Endpoints table.
 */
public class WAREndpoints extends PropertyPage {

    private WindowsAzureProjectManager waProjManager;
    private WindowsAzureRole windowsAzureRole;
    private Table tblEndpoints;
    private TableViewer tblViewer;
    private static String[] arrType = {
        WindowsAzureEndpointType.Input.toString(),
        WindowsAzureEndpointType.Internal.toString(),
        WindowsAzureEndpointType.InstanceInput.toString()};
    private static List<WindowsAzureEndpoint> listEndPoints;
    private Button btnEdit;
    private Button btnRemove;
    private boolean isPageDisplayed = false;
    private static String auto = "(auto)";

    @Override
    public String getTitle() {
    	if (isPageDisplayed 
    			&& tblViewer != null) {
    		tblViewer.refresh();
    	}
        return super.getTitle();
    }

    /**
     * Create endpoints table and buttons associated with it.
     *
     * @param parent : parent composite.
     * @return control
     */
    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.persistent.winazure.eclipseplugin."
                + "windows_azure_endpoint_page");
        waProjManager = Activator.getDefault().getWaProjMgr();
        windowsAzureRole = Activator.getDefault().getWaRole();
        Activator.getDefault().setSaved(false);

        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);

        tblEndpoints = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
        tblEndpoints.setHeaderVisible(true);
        tblEndpoints.setLinesVisible(true);
        gridData = new GridData();
        gridData.heightHint = 380;
        gridData.horizontalIndent = 3;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        tblEndpoints.setLayoutData(gridData);

        TableColumn colName = new TableColumn(tblEndpoints, SWT.FILL);
        colName.setText(Messages.dlgColName);
        colName.setWidth(230);

        TableColumn colType = new TableColumn(tblEndpoints, SWT.FILL);
        colType.setText(Messages.dlgColType);
        colType.setWidth(80);

        TableColumn colPublicPort =
                new TableColumn(tblEndpoints, SWT.FILL);
        colPublicPort.setText(Messages.dlgColPubPort);
        colPublicPort.setWidth(80);

        TableColumn colPrivatePort =
                new TableColumn(tblEndpoints, SWT.FILL);
        colPrivatePort.setText(Messages.dlgColPrivatePort);
        colPrivatePort.setWidth(80);

        tblEndpoints.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                btnEdit.setEnabled(true);
                btnRemove.setEnabled(true);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });

        Composite containerButtons =
                new Composite(container, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.END;
        gridData.verticalAlignment = GridData.BEGINNING;
        containerButtons.setLayout(gridLayout);
        containerButtons.setLayoutData(gridData);

        createAddButton(containerButtons);
        createEditButton(containerButtons);
        createRemoveButton(containerButtons);

        createTableViewer();
        tblViewer.setContentProvider(new EPTableContentProvider());
        tblViewer.setLabelProvider(new EPTableLabelProvider());
        tblViewer.setCellModifier(new CellModifier());

        try {
            //Get endpoints to be displayed in endpoints table
            listEndPoints = windowsAzureRole.getEndpoints();
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	PluginUtil.displayErrorDialogAndLog(
        			this.getShell(),
        			Messages.adRolErrTitle,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2, e);
        }
        // set listEndpoints as input to tblViewer
        tblViewer.setInput(listEndPoints.toArray());
        isPageDisplayed = true;
        return container;
    }

    /**
     * Creates 'Add' button and adds selection listener to it.
     *
     * @param containerButtons
     */
    private void createAddButton(Composite containerButtons) {
        Button btnAdd = new Button(containerButtons, SWT.PUSH);
        btnAdd.setText(Messages.rolsAddBtn);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        btnAdd.setLayoutData(gridData);
        btnAdd.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                addBtnListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });
    }

    /**
     * Creates 'Edit' button and adds selection listener to it.
     *
     * @param containerButtons
     */
    private void createEditButton(Composite containerButtons) {
        btnEdit = new Button(containerButtons, SWT.PUSH);
        btnEdit.setText(Messages.rolsEditBtn);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        btnEdit.setLayoutData(gridData);
        btnEdit.setEnabled(false);
        btnEdit.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                editBtnListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });
    }

    /**
     * Creates 'Remove' button and adds selection listener to it.
     *
     * @param containerButtons
     */
    private void createRemoveButton(Composite containerButtons) {
        btnRemove = new Button(containerButtons, SWT.PUSH);
        btnRemove.setText(Messages.dlgBtnRemove);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        btnRemove.setLayoutData(gridData);
        btnRemove.setEnabled(false);
        btnRemove.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                removeBtnListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

            }
        });
    }

    /**
     * Content provider class for endpoints table,
     * which determines the input for the table.
     *
     */
    private class EPTableContentProvider implements IStructuredContentProvider {

        @Override
        public void inputChanged(Viewer viewer,
                Object oldInput, Object newInput) {

        }

        @Override
        public void dispose() {

        }

        @Override
        public Object[] getElements(Object arg0) {
            return listEndPoints.toArray();
        }
    }

    /**
     * Label provider class for endpoints table,
     * to provide column names.
     *
     */
    private class EPTableLabelProvider implements ITableLabelProvider {

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
        	String result = "";
        	try {
        		WindowsAzureEndpoint endpoint =
        				(WindowsAzureEndpoint) element;
        		String type = endpoint.getEndPointType().toString();
        		switch (colIndex) {
        		case 0:
        			result = endpoint.getName();
        			break;
        		case 1:
        			result = type;
        			break;
        		case 2:
        			if (type
        					.equalsIgnoreCase(
        							WindowsAzureEndpointType.Input.toString())
        							|| endpoint.getEndPointType().toString()
        							.equalsIgnoreCase(
        									WindowsAzureEndpointType.
        									InstanceInput.toString())) {
        				result = endpoint.getPort();
        			} else {
        				result = Messages.dlgDbgNA;
        			}
        			break;
        		case 3:
        			result = endpoint.getPrivatePort();
        			if (result == null
        					&& (type.equalsIgnoreCase(WindowsAzureEndpointType.Input.toString())
        							|| type.equalsIgnoreCase(WindowsAzureEndpointType.Internal.toString()))) {
        				result = auto;
        			}
        			break;
        		default:
        			break;
        		}
        	} catch (WindowsAzureInvalidProjectOperationException e) {
        		PluginUtil.displayErrorDialogAndLog(getShell(),
        				Messages.dlgDbgEndPtErrTtl,
        				Messages.endPtTypeErr, e);
        	}
        	return result;
        }

        @Override
        public Image getColumnImage(Object arg0, int arg1) {
            return null;
        }
    }

    /**
     * Cell modifier class for endpoints table,
     * which implements the in-place editing for cells.
     *
     */
    private class CellModifier implements ICellModifier {
        @Override
        public void modify(Object waEndpoint, String columnName,
                Object modifiedVal) {
            TableItem tblItem = (TableItem) waEndpoint;
            WindowsAzureEndpoint endpoint = (WindowsAzureEndpoint) tblItem
                    .getData();
            try {
                if (columnName.equals(Messages.dlgColType)) {
                    modifyType(endpoint, modifiedVal);
                } else if (columnName.equals(Messages.dlgColName)) {
                    modifyName(endpoint, modifiedVal);
                } else if (columnName.equals(Messages.dlgColPubPort)) {
                    modifyPublicPort(endpoint, modifiedVal);
                } else if (columnName.equals(Messages.dlgColPrivatePort)) {
                    modifyPrivatePort(endpoint, modifiedVal);
                }
            } catch (Exception e) {
            	PluginUtil.displayErrorDialogAndLog(
            			getShell(),
            			Messages.adRolErrTitle,
            			Messages.adRolErrMsgBox1
            			+ Messages.adRolErrMsgBox2, e);
            }
            tblViewer.refresh();
        }

        /**
         * Handles the modification of endpoint type.
         *
         * @param endpoint : the endpoint being modified.
         * @param modifiedVal : new value for endpoint type.
         * @throws WindowsAzureInvalidProjectOperationException .
         */
        private void modifyType(WindowsAzureEndpoint endpoint,
        		Object modifiedVal)
        				throws WindowsAzureInvalidProjectOperationException {
        	WindowsAzureEndpointType oldType = endpoint.getEndPointType();
        	String endpointName = endpoint.getName();
        	String prvPort = endpoint.getPrivatePort();
        	String pubPort = endpoint.getPort();
        	String modifiedValStr = modifiedVal.toString();

        	if (modifiedValStr.equals("0")) {
        		// User changed type to Input
        		/*
        		 * While changing type from Internal
        		 * if private port is in the form of range,
        		 * then assign minimum of that range
        		 * as a private port because private port
        		 * range is not valid for other types of endpoints.
        		 */
        		if (oldType.equals(WindowsAzureEndpointType.Internal)) {
        			if (prvPort.contains("-")) {
        				String[] portRange = prvPort.split("-");
        				prvPort = portRange[0];
        			}
        			pubPort = prvPort;
        		} else if (oldType.equals(WindowsAzureEndpointType.InstanceInput)
        				&& pubPort.contains("-")) {
        			String[] portRange = pubPort.split("-");
        			pubPort = portRange[0];
        		}
        		setEndpointType(endpoint, WindowsAzureEndpointType.Input, prvPort, pubPort);
        	} else if (modifiedValStr.equals("1")) {
        		// User changed type to Internal
        		WindowsAzureEndpoint debugEndpt = windowsAzureRole
        				.getDebuggingEndpoint();
        		String dbgEndptName = "";
        		if (debugEndpt != null) {
        			dbgEndptName = debugEndpt.getName();
        		}
        		Boolean disableDebug = false;
        		if (endpointName.equalsIgnoreCase(dbgEndptName)) {
        			boolean choice = MessageDialog.openQuestion(getShell(),
        					Messages.dlgTypeTitle,
        					String.format("%s%s%s",
        							Messages.dlgEPDel,
        							Messages.dlgEPChangeType,
        							Messages.dlgEPDel2));
        			if (choice) {
        				disableDebug = true;
        			}
        		}
        		setEndpointType(endpoint, WindowsAzureEndpointType.Internal, prvPort, "");
        		if (disableDebug) {
        			windowsAzureRole.setDebuggingEndpoint(null);
        		}
        	} else if (modifiedValStr.equals("2")) {
        		// User changed type to InstanceInput
        		Boolean changeType = true;
        		if (oldType.equals(WindowsAzureEndpointType.Internal)) {
        			if (prvPort.contains("-")) {
        				String[] portRange = prvPort.split("-");
        				prvPort = portRange[0];
        			}
        			pubPort = prvPort;
        		} else if (oldType.equals(WindowsAzureEndpointType.Input)
        				&& prvPort == null) {
        			if (windowsAzureRole.isValidEndpoint(
        					endpointName,
        					WindowsAzureEndpointType.InstanceInput,
        					pubPort, pubPort)) {
        				prvPort = pubPort;
        			} else {
        				changeType = false;
        				MessageDialog.openWarning(getShell(),
        						Messages.dlgTypeTitle,
        						String.format(Messages.inpInstTypeMsg, pubPort));
        			}
        		}
        		if (changeType) {
        			setEndpointType(endpoint, WindowsAzureEndpointType.InstanceInput, prvPort, pubPort);
        		}
        	}
        }

        private void setEndpointType(WindowsAzureEndpoint endpoint,
        		WindowsAzureEndpointType type, String prvPort, String pubPort)
        				throws WindowsAzureInvalidProjectOperationException {
        	if (windowsAzureRole.isValidEndpoint(
        			endpoint.getName(),
        			type,
        			prvPort,
        			pubPort)) {
        		endpoint.setPrivatePort(prvPort);
        		endpoint.setEndPointType(type);
        	} else {
        		MessageDialog.openInformation(getShell(),
        				Messages.dlgTypeTitle, Messages.changeErr);
        	}
        }

        /**
         * Handles the modification of endpoint name.
         *
         * @param endpoint : the endpoint being modified.
         * @param modifiedVal : new value for endpoint name.
         * @throws WindowsAzureInvalidProjectOperationException .
         */
        private void modifyName(WindowsAzureEndpoint endpoint,
        		Object modifiedVal)
        				throws WindowsAzureInvalidProjectOperationException {
        	try {
        		WAREndpointsUtilMethods.modifyName(endpoint, modifiedVal, windowsAzureRole);
        	} catch (AzureCommonsException e) {
        		PluginUtil.displayErrorDialogAndLog(
        				getShell(), Messages.dlgInvdEdPtName1, e.getMessage(), e);
        	}
        }

        /**
         * Handles the modification of endpoint's public port.
         *
         * @param endpoint : the endpoint being modified.
         * @param modifiedVal : new value for endpoint's public port.
         * @throws WindowsAzureInvalidProjectOperationException .
         */
        private void modifyPublicPort(WindowsAzureEndpoint endpoint,
        		Object modifiedVal)
        				throws WindowsAzureInvalidProjectOperationException {

        	int dashCnt = StringUtils.countMatches(
        			modifiedVal.toString().trim(), "-");
        	try {
        		endpoint = WAREndpointsUtilMethods.modifyPublicPort(endpoint,
        				modifiedVal,
        				dashCnt, windowsAzureRole);
        	} catch (AzureCommonsException e) {
        		PluginUtil.displayErrorDialogAndLog(getShell(),
        				Messages.dlgInvldPort,
        				e.getMessage(), e);
        	}
        }

        /**
         * Handles the modification of endpoint's private port.
         *
         * @param endpoint : the endpoint being modified.
         * @param modifiedVal : new value for endpoint's private port.
         * @throws WindowsAzureInvalidProjectOperationException .
         */
        private void modifyPrivatePort(WindowsAzureEndpoint endpoint,
        		Object modifiedVal)
        				throws WindowsAzureInvalidProjectOperationException {
        	int dashCnt = StringUtils.countMatches(
        			modifiedVal.toString().trim(), "-");
        	try {
        		RoleAndEndpoint obj = WAREndpointsUtilMethods.
        				modifyPrivatePort(
        				endpoint, modifiedVal, dashCnt, windowsAzureRole);
        		if (obj != null) {
        			endpoint = obj.getEndPt();
        			windowsAzureRole = obj.getRole();
        		}
        	} catch (AzureCommonsException e1) {
        		PluginUtil.displayErrorDialogAndLog(getShell(),
        				Messages.dlgInvldPort, e1.getMessage(), e1);
        	}
        }

        /**
         * Return attribute values of selected row of the table.
         * @param element
         * @param property : column name
         * @return Object
         */
        public Object getValue(Object element, String property) {
        	Object result = null;
        	WindowsAzureEndpoint endpoint = (WindowsAzureEndpoint) element;

        	if (property.equals(Messages.dlgColType)) {
        		try {
					if (endpoint.getEndPointType().
							toString().equalsIgnoreCase(
							WindowsAzureEndpointType.Input.toString())) {
						result = 0;
					} else {
						result = 1;
					}
				} catch (WindowsAzureInvalidProjectOperationException e) {
	            	PluginUtil.displayErrorDialogAndLog(getShell(),
	            			Messages.dlgDbgEndPtErrTtl,
	            			Messages.endPtTypeErr, e);
	            }
        	} else if (property.equals(Messages.dlgColName)) {
        		result = endpoint.getName();
        	} else if (property.equals(
        			Messages.dlgColPubPort)) {
        		result = endpoint.getPort();
        	} else if (property.equals(
        			Messages.dlgColPrivatePort)) {
        		result = endpoint.getPrivatePort();
        		if (result == null) {
        			result = "";
        		}
        	}
        	return result;
        }

        /**
         * Determines whether a particular cell can be modified or not.
         * @return boolean
         */
        @Override
        public boolean canModify(Object element, String property) {
        	boolean retVal = true;
        	WindowsAzureEndpoint endpoint = (WindowsAzureEndpoint) element;
        	/*
        	 * If end point selected for in place editing
        	 * is related to caching then don't allow.
        	 */
        	try {
        	if (endpoint.getName().startsWith(Messages.cachEndPtName)
        			&& (property.equals(Messages.dlgColName)
        					|| property.equals(Messages.dlgColPrivatePort)
        					|| property.equals(Messages.dlgColType))) {
        		retVal = false;
        	} else if (endpoint.isStickySessionEndpoint()
        			|| endpoint.isSSLEndpoint()
        			|| endpoint.isSSLRedirectEndPoint()) {
        		retVal = false;
        	} else {
        			if (endpoint.getEndPointType().toString().equalsIgnoreCase(
        					WindowsAzureEndpointType.Internal.toString())) {
        				if (property.equals(Messages.dlgColPubPort)
        						|| (property.equals(Messages.dlgColType)
        								&& endpoint.getPrivatePort() == null)) {
        					retVal = false;
        				}
        			}
        		}
        	} catch (WindowsAzureInvalidProjectOperationException e) {
    			PluginUtil.displayErrorDialogAndLog(getShell(),
    					Messages.dlgDbgEndPtErrTtl,
    					Messages.endPtTypeErr, e);
    		}
        	return retVal;
        }
    }

    /**
     * Listener method for remove button which
     * deletes the selected endpoint.
     */
    protected void removeBtnListener() {
        int selIndex = tblViewer.getTable().getSelectionIndex();
        if (selIndex > -1) {
            try {
                WindowsAzureEndpoint debugEndpt = windowsAzureRole
                                                    .getDebuggingEndpoint();
                String dbgEndptName = "";
                if (debugEndpt != null) {
                    dbgEndptName = debugEndpt.getName();
                }
                // delete the selected endpoint
                WindowsAzureEndpoint waEndpoint = listEndPoints
                        .get(selIndex);
                /*
        		 * Check end point selected for removal
        		 * is associated with Caching then give error
        		 * and does not allow to remove.
        		 */
                if (waEndpoint.isCachingEndPoint()) {
                	PluginUtil.displayErrorDialog(getShell(),
                			Messages.cachDsblErTtl,
                			Messages.endPtRmvErMsg);
                }
                /*
        		 * Check end point selected for removal
        		 * is associated with Debugging.
        		 */
                else if (waEndpoint.getName().equalsIgnoreCase(dbgEndptName)) {
                	StringBuffer msg = new StringBuffer(Messages.dlgEPDel);
                	msg.append(Messages.dlgEPDel1);
                	msg.append(Messages.dlgEPDel2);
                    boolean choice = MessageDialog.openQuestion(getShell(),
                            Messages.dlgDelEndPt1, msg.toString());
                    if (choice) {
                        waEndpoint.delete();
                        windowsAzureRole.setDebuggingEndpoint(null);
                    }
                }
                /*
                 * Endpoint associated with both SSL
                 * and Session affinity
                 */
                else if (waEndpoint.isStickySessionEndpoint()
                		&& waEndpoint.isSSLEndpoint()) {
                	boolean choice = MessageDialog.openConfirm(getShell(),
                            Messages.dlgDelEndPt1, Messages.bothDelMsg);
                    if (choice) {
                    	if (waEndpoint.getEndPointType().
                    			equals(WindowsAzureEndpointType.Input)) {
                    		windowsAzureRole.
                    		setSessionAffinityInputEndpoint(null);
                    		windowsAzureRole.setSslOffloading(null, null);
                    		waEndpoint.delete();
                    	} else {
                    		windowsAzureRole.
                    		setSessionAffinityInputEndpoint(null);
                    		windowsAzureRole.setSslOffloading(null, null);
                    	}
                    }
                }
                /*
        		 * Check end point selected for removal
        		 * is associated with Load balancing
        		 * i.e (HTTP session affinity).
        		 */
                else if (waEndpoint.isStickySessionEndpoint()) {
                    StringBuffer msg = new StringBuffer(Messages.ssnAffDelMsg);
                    boolean choice = MessageDialog.openConfirm(getShell(),
                            Messages.dlgDelEndPt1, msg.toString());
                    if (choice) {
                    	if (waEndpoint.getEndPointType().
                    			equals(WindowsAzureEndpointType.Input)) {
                    		windowsAzureRole.
                    		setSessionAffinityInputEndpoint(null);
                    		waEndpoint.delete();
                    	} else {
                    		windowsAzureRole.
                    		setSessionAffinityInputEndpoint(null);
                    	}
                    }
                }
                /*
                 * Endpoint associated with SSL
                 */
                else if (waEndpoint.isSSLEndpoint()) {
                	boolean choice = MessageDialog.openConfirm(getShell(),
                            Messages.dlgDelEndPt1, Messages.sslDelMsg);
                    if (choice) {
                    	if (waEndpoint.getEndPointType().
                    			equals(WindowsAzureEndpointType.Input)) {
                    		windowsAzureRole.setSslOffloading(null, null);
                    		waEndpoint.delete();
                    	} else {
                    		windowsAzureRole.setSslOffloading(null, null);
                    	}
                    }
                }
                /*
                 * Endpoint associated with SSL redirection.
                 */
                else if (waEndpoint.isSSLRedirectEndPoint()) {
                	boolean choice = MessageDialog.openConfirm(getShell(),
                            Messages.dlgDelEndPt1, Messages.sslRedirectDelMsg);
                    if (choice) {
                    	windowsAzureRole.deleteSslOffloadingRedirectionEndpoint();
                    }
                }
                /*
                 * Normal end point.
                 */
                else {
                    boolean choice = MessageDialog.openQuestion(getShell(),
                            Messages.dlgDelEndPt1, Messages.dlgDelEndPt2);
                    if (choice) {
                        waEndpoint.delete();
                    }
                }
                tblViewer.refresh();
                if (tblEndpoints.getItemCount() == 0) {
                	// table is empty i.e. number of rows = 0
                	btnRemove.setEnabled(false);
                	btnEdit.setEnabled(false);
                }
            } catch (WindowsAzureInvalidProjectOperationException e) {
            	PluginUtil.displayErrorDialogAndLog(
            			this.getShell(),
            			Messages.adRolErrTitle,
            			Messages.adRolErrMsgBox1
            			+ Messages.adRolErrMsgBox2, e);
            }
        }
    }

    /**
     * Listener method for add button which opens a dialog
     * to add an endpoint.
     */
    protected void addBtnListener() {
        WAEndpointDialog dialog = new WAEndpointDialog(this.getShell(),
                windowsAzureRole);
        dialog.open();
        tblViewer.refresh(true);
    }

    /**
     * Listener method for edit button which opens a dialog
     * to edit an endpoint.
     */
    protected void editBtnListener() {
    	int selIndex = tblViewer.getTable().getSelectionIndex();
    	if (selIndex > -1) {
    		WindowsAzureEndpoint waEndpoint = listEndPoints
    				.get(selIndex);
    		/*
    		 * Check end point selected for modification
    		 * is associated with caching then give error
    		 * and does not allow to edit.
    		 */
    		try {
    			if (waEndpoint.isCachingEndPoint()) {
    				PluginUtil.displayErrorDialog(getShell(),
    						Messages.cachDsblErTtl,
    						Messages.endPtEdtErMsg);
    			} else if (waEndpoint.isStickySessionEndpoint()
    					&& waEndpoint.isSSLEndpoint()) {
    				PluginUtil.displayErrorDialog(getShell(),
    						Messages.sslSesDsbl,
    						Messages.sslSesAffMsg);
    			} else if (waEndpoint.isStickySessionEndpoint()) {
    				PluginUtil.displayErrorDialog(getShell(),
    						Messages.sesAffDsblErTl,
    						Messages.sesAffMsg);
    			} else if(waEndpoint.isSSLEndpoint()) {
    				PluginUtil.displayErrorDialog(getShell(),
    						Messages.sslDsblErTl,
    						Messages.sslMsg);
    			} else if(waEndpoint.isSSLRedirectEndPoint()) { 
    				PluginUtil.displayErrorDialog(getShell(),
    						Messages.sslDsblErTl,
    						Messages.sslMsg);
    			}else {WAEndpointDialog dialog =
    						new WAEndpointDialog(
    								this.getShell(),
    						windowsAzureRole,
    						waEndpoint, true);
    				dialog.open();
    				tblViewer.refresh(true);
    			}
    		} catch (WindowsAzureInvalidProjectOperationException e) {
    			PluginUtil.displayErrorDialogAndLog(getShell(),
    					Messages.genErrTitle,
    					Messages.chEndPtErMsg, e);
    		}
    	}
    }

    /**
     * Create TableViewer for endpoints table.
     */
    private void createTableViewer() {
        tblViewer = new TableViewer(tblEndpoints);

        tblViewer.setUseHashlookup(true);
        tblViewer.setColumnProperties(new String[] {
                Messages.dlgColName,
                Messages.dlgColType,
                Messages.dlgColPubPort,
                Messages.dlgColPrivatePort });

        CellEditor[] editors = new CellEditor[4];

        editors[0] = new TextCellEditor(tblEndpoints);
        editors[1] = new ComboBoxCellEditor(tblEndpoints, arrType,
                SWT.READ_ONLY);
        editors[2] = new TextCellEditor(tblEndpoints);
        editors[3] = new TextCellEditor(tblEndpoints);

        tblViewer.setCellEditors(editors);
    }

    @Override
    public boolean performOk() {
    	if (!isPageDisplayed) {
    		return super.performOk();
    	}
        boolean okToProceed = true;
        try {
            if (!Activator.getDefault().isSaved()) {
                waProjManager.save();
                Activator.getDefault().setSaved(true);
            }
            WAEclipseHelper.refreshWorkspace(
            		Messages.rolsRefTitle, Messages.rolsRefMsg);
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	PluginUtil.displayErrorDialogAndLog(
        			this.getShell(),
        			Messages.adRolErrTitle,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2, e);
            okToProceed = false;
        }
        if (okToProceed) {
            okToProceed = super.performOk();
        }
        return okToProceed;
    }
}
