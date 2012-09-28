/**
* Copyright 2011 Persistent Systems Ltd.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.WAEclipseHelper;
import com.persistent.util.MessageUtil;

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
    private String errorTitle;
    private String errorMessage;
    private Button btnEdit;
    private Button btnRemove;
    /**
     * End point range's minimum value.
     */
    private final static int RANGE_MIN = 1;
    /**
     * End point range's maximum value.
     */
    private final static int RANGE_MAX = 65535;
    private boolean isPageDisplayed = false;

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
            errorTitle = Messages.adRolErrTitle;
            errorMessage = Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
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
        		switch (colIndex) {
        		case 0:
        			result = endpoint.getName();
        			break;
        		case 1:
        			result = endpoint.getEndPointType().toString();
        			break;
        		case 2:
        			if (endpoint.getEndPointType().toString()
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
        			break;
        		default:
        			break;
        		}
        	} catch (WindowsAzureInvalidProjectOperationException e) {
        		MessageUtil.displayErrorDialog(getShell(),
        				Messages.dlgDbgEndPtErrTtl,
        				Messages.endPtTypeErr);
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
                errorTitle = Messages.adRolErrTitle;
                errorMessage = Messages.adRolErrMsgBox1
                        + Messages.adRolErrMsgBox2;
                MessageUtil.displayErrorDialog(getShell(), errorTitle,
                        errorMessage);
                Activator.getDefault().log(errorMessage, e);
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
        	// User changed type to Input
            if (modifiedVal.toString().equals("0")) {
                 WindowsAzureEndpoint stickyIntEndpt = windowsAzureRole
                         .getSessionAffinityInternalEndpoint();
                 String stcIntEndptName = "";
                 if (stickyIntEndpt != null) {
                       stcIntEndptName = stickyIntEndpt.getName();
                 }
                if (endpoint.getName().equalsIgnoreCase(stcIntEndptName)) {
                    StringBuffer msg = new StringBuffer(Messages.ssnAffTypMsg);
                    MessageDialog.openWarning(new Shell(),
                            Messages.dlgTypeTitle, msg.toString());
                } else {
                	/*
                	 * While changing type from Internal to Input
                	 * if private port is in the form of range,
                	 * then assign minimum of that range
                	 * as a private port because private port
                	 * range is not valid for Input endpoint.
                	 */
                	if (oldType.equals(WindowsAzureEndpointType.Internal)
                			&& endpoint.getPrivatePort().contains("-")) {
                		String[] portRange = endpoint.getPrivatePort().split("-");
                		endpoint.setPrivatePort(portRange[0]);
                	}
                	endpoint.setEndPointType(WindowsAzureEndpointType
                			.valueOf(arrType[0]));
                }
            } else {
            	// User changed type to Internal
                String endpointName = endpoint.getName();
                WindowsAzureEndpoint debugEndpt = windowsAzureRole
                        .getDebuggingEndpoint();
                WindowsAzureEndpoint stickyEndpt = windowsAzureRole
                        .getSessionAffinityInputEndpoint();
                WindowsAzureEndpoint stickyIntEndpt = windowsAzureRole
                        .getSessionAffinityInternalEndpoint();

                String dbgEndptName = "";
                String stcEndptName = "";
                String stcIntEndptName = "";

                if (debugEndpt != null) {
                    dbgEndptName = debugEndpt.getName();
                }
                if (stickyEndpt != null) {
                    stcEndptName = stickyEndpt.getName();
                    stcIntEndptName = stickyIntEndpt.getName();
                }
                if (endpointName.equalsIgnoreCase(dbgEndptName)
                        && oldType.equals(
                        		WindowsAzureEndpointType.Input)
                        || endpointName.equalsIgnoreCase(dbgEndptName)
                        && oldType.equals(
                        		WindowsAzureEndpointType.InstanceInput)) {
                    StringBuffer msg = new StringBuffer(Messages.dlgEPDel);
                    msg.append(Messages.dlgEPChangeType);
                    msg.append(Messages.dlgEPDel2);
                    boolean choice = MessageDialog.openQuestion(new Shell(),
                            Messages.dlgTypeTitle, msg.toString());
                    if (choice) {
                        endpoint.setEndPointType(WindowsAzureEndpointType
                                .valueOf(arrType[1]));
                        windowsAzureRole.setDebuggingEndpoint(null);
                    }
                } else if (endpointName.equalsIgnoreCase(stcEndptName)
                        && oldType.equals(WindowsAzureEndpointType.Input)) {
                    StringBuffer msg = new StringBuffer(Messages.ssnAffTypMsg);
                    MessageDialog.openWarning(new Shell(),
                            Messages.dlgTypeTitle, msg.toString());
                } else if (modifiedVal.toString().equals("2")) {
                	// User changed type to InstanceInput
                	if (endpointName.equalsIgnoreCase(stcEndptName)
                            && oldType.equals(WindowsAzureEndpointType.Input)
                            || (endpointName.equalsIgnoreCase(stcIntEndptName)
                            		&& oldType.equals(WindowsAzureEndpointType.
                            				Internal))) {
                        StringBuffer msg = new StringBuffer(
                        		Messages.ssnAffTypMsg);
                        MessageDialog.openWarning(new Shell(),
                                Messages.dlgTypeTitle, msg.toString());
                	} else {
                		/*
                		 * While changing type from Internal to Input
                		 * if private port is in the form of range,
                		 * then assign minimum of that range
                		 * as a private port because private port
                		 * range is not valid for Input endpoint.
                		 */
                		if (oldType.equals(WindowsAzureEndpointType.Internal)
                				&& endpoint.getPrivatePort().contains("-")) {
                			String[] portRange = endpoint.
                					getPrivatePort().split("-");
                			endpoint.setPrivatePort(portRange[0]);
                		}
                		endpoint.setEndPointType(WindowsAzureEndpointType
                				.valueOf(arrType[2]));
                	}
                }
                else {
                    endpoint.setEndPointType(WindowsAzureEndpointType
                            .valueOf(arrType[1]));
                }

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
            // Validate endpoint name
            String endptName = modifiedVal.toString();
            boolean isValid = windowsAzureRole
                    .isAvailableEndpointName(endptName,
                    		endpoint.getEndPointType());
            if (isValid || endptName.equalsIgnoreCase(endpoint.getName())) {
                endpoint.setName(modifiedVal.toString());
            } else {
                errorTitle = Messages.dlgInvdEdPtName1;
                errorMessage = Messages.dlgInvdEdPtName2;
                MessageUtil.displayErrorDialog(getShell(),
                        errorTitle, errorMessage);
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
        	/*
        	 * Check only one '-' is present,
        	 * while specifying range for Instance end point.
        	 * If end point is Internal or Input then,
        	 * it will not satisfy if condition
        	 * and dash count will be zero.
        	 */
        	int dashCnt = 0;
        	if (endpoint.getEndPointType().
        			equals(WindowsAzureEndpointType.InstanceInput)) {
        		dashCnt = StringUtils.countMatches(
        				modifiedVal.toString().trim(), "-");
        		if (dashCnt > 1) {
        			MessageUtil.displayErrorDialog(getShell(),
        					Messages.dlgInvldPort,
        					Messages.dashErrMsg);
        		}
        	}
        	if (dashCnt <= 1) {
        		// Check for valid range 1 to 65535
        		Boolean isPortValid = true;
        		try {
        			/*
        			 * If public port contains '-'
        			 * then split string and
        			 * get two integer values out of it.
        			 * else directly check for value.
        			 */
        			if (dashCnt == 1) {
        				String[] range = modifiedVal.
        						toString().split("-");
        				int rngStart = Integer.
        						parseInt(range[0]);
        				int rngEnd = Integer.parseInt(range[1]);
        				if (!(rngStart >= RANGE_MIN
        						&& rngStart <= RANGE_MAX
        						&& rngEnd >= RANGE_MIN
        						&& rngEnd <= RANGE_MAX)) {
        					isPortValid = false;
        				}
        			} else {
        				int port = Integer.parseInt(
        						modifiedVal.toString());
        				if (!(port >= RANGE_MIN
        						&& port <= RANGE_MAX)) {
        					isPortValid = false;
        				}
        			}
        		} catch (NumberFormatException e) {
        			isPortValid = false;
        		}
        		if (isPortValid) {
        			// Validate port
        			boolean isValid = windowsAzureRole.isValidEndpoint(
        					endpoint.getName(),
        					endpoint.getEndPointType(),
        					endpoint.getPrivatePort(),
        					modifiedVal.toString());
        			if (isValid) {
        				endpoint.setPort(
        						modifiedVal.toString());
        			} else {
        				MessageUtil.displayErrorDialog(
        						getShell(),
        						Messages.dlgInvldPort,
        						Messages.dlgPortInUse);
        			}
        		} else {
        			MessageUtil.displayErrorDialog(getShell(),
        					Messages.dlgInvldPort,
        					Messages.rngErrMsg);
        		}
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
        	/*
        	 * Check only one '-' is present,
        	 * while specifying range for Internal end point.
        	 * If end point is Instance or Input then,
        	 * it will not satisfy if condition
        	 * and dash count will be zero.
        	 */
        	int dashCnt = 0;
        	if (endpoint.getEndPointType().
        			equals(WindowsAzureEndpointType.Internal)) {
        		dashCnt = StringUtils.countMatches(
        				modifiedVal.toString().trim(), "-");
        		if (dashCnt > 1) {
        			MessageUtil.displayErrorDialog(getShell(),
        					Messages.dlgInvldPort,
        					Messages.dashErrMsg);
        		}
        	}
        	if (dashCnt <= 1) {
        		// Check for valid range 1 to 65535
        		Boolean isPortValid = true;
        		try {
        			/*
        			 * If public port contains '-'
        			 * then split string and
        			 * get two integer values out of it.
        			 * else directly check for value.
        			 */
        			if (dashCnt == 1) {
        				String[] range = modifiedVal.
        						toString().split("-");
        				int rngStart = Integer.
        						parseInt(range[0]);
        				int rngEnd = Integer.parseInt(range[1]);
        				if (!(rngStart >= RANGE_MIN
        						&& rngStart <= RANGE_MAX
        						&& rngEnd >= RANGE_MIN
        						&& rngEnd <= RANGE_MAX)) {
        					isPortValid = false;
        				}
        			} else {
        				int port = Integer.
        						parseInt(modifiedVal.toString());
        				if (!(port >= RANGE_MIN
        						&& port <= RANGE_MAX)) {
        					isPortValid = false;
        				}
        			}
        		} catch (NumberFormatException e) {
        			isPortValid = false;
        		}
        		if (isPortValid) {
        			// Validate port
        			boolean isValid = windowsAzureRole.isValidEndpoint(
        					endpoint.getName(),
        					endpoint.getEndPointType(),
        					modifiedVal.toString(),
        					endpoint.getPort());
        			if (isValid) {
        				boolean isDebugEnabled = false;
        				boolean isSuspended = false;
        				WindowsAzureEndpoint endPt =
        						windowsAzureRole.getDebuggingEndpoint();
        				/*
        				 * check if the endpoint is associated with debug,
        				 * if yes then set isDebugEnabled to true and
        				 * store the suspended mode value. Disable debug endpoint
        				 * and then enable it with the modified endpoint.
        				 */
        				if (endPt != null
        						&& endpoint.getName().equalsIgnoreCase(
        								endPt.getName())) {
        					isSuspended = windowsAzureRole.
        							getStartSuspended();
        					windowsAzureRole.setDebuggingEndpoint(null);
        					isDebugEnabled = true;
        				}
        				endpoint.setPrivatePort(
        						modifiedVal.toString());
        				if (isDebugEnabled) {
        					windowsAzureRole.
        					setDebuggingEndpoint(endpoint);
        					windowsAzureRole.
        					setStartSuspended(isSuspended);
        				}
        			} else {
        				MessageUtil.displayErrorDialog(
        						getShell(),
        						Messages.dlgInvldPort,
        						Messages.dlgPortInUse);
        			}
        		} else {
        			MessageUtil.displayErrorDialog(getShell(),
        					Messages.dlgInvldPort,
        					Messages.rngErrMsg);
        		}
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
	            	MessageUtil.displayErrorDialog(getShell(),
	            			Messages.dlgDbgEndPtErrTtl,
	            			Messages.endPtTypeErr);
	            }
        	} else if (property.equals(Messages.dlgColName)) {
        		result = endpoint.getName();
        	} else if (property.equals(
        			Messages.dlgColPubPort)) {
        		result = endpoint.getPort();
        	} else if (property.equals(
        			Messages.dlgColPrivatePort)) {
        		result = endpoint.getPrivatePort();
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
        	if (endpoint.getName().startsWith(Messages.cachEndPtName)
        			&& (property.equals(Messages.dlgColName)
        					|| property.equals(Messages.dlgColPrivatePort)
        					|| property.equals(Messages.dlgColType))) {
        		retVal = false;
        	} else {
        		try {
        			if (endpoint.getEndPointType().toString().equalsIgnoreCase(
        					WindowsAzureEndpointType.Internal.toString())
        					&& property.equals(Messages.dlgColPubPort)) {
        				retVal = false;
        			}
        		} catch (WindowsAzureInvalidProjectOperationException e) {
        			MessageUtil.displayErrorDialog(getShell(),
        					Messages.dlgDbgEndPtErrTtl,
        					Messages.endPtTypeErr);
        		}
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
                WindowsAzureEndpoint stickyEndpt = windowsAzureRole
                        .getSessionAffinityInputEndpoint();
                WindowsAzureEndpoint stickyIntEndpt = windowsAzureRole
                        .getSessionAffinityInternalEndpoint();
                String dbgEndptName = "";
                String stcEndptName = "";
                String stcIntEndptName = "";
                if (debugEndpt != null) {
                    dbgEndptName = debugEndpt.getName();
                }
                if (stickyEndpt != null) {
                    stcEndptName = stickyEndpt.getName();
                    stcIntEndptName = stickyIntEndpt.getName();
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
                	MessageUtil.displayErrorDialog(getShell(),
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
                    boolean choice = MessageDialog.openQuestion(new Shell(),
                            Messages.dlgDelEndPt1, msg.toString());
                    if (choice) {
                        waEndpoint.delete();
                        windowsAzureRole.setDebuggingEndpoint(null);
                        tblViewer.refresh();
                    }
                }
                /*
        		 * Check end point selected for removal
        		 * is associated with Load balancing
        		 * i.e (HTTP session affinity).
        		 */
                else if (waEndpoint.getName().equalsIgnoreCase(stcEndptName)
                        || waEndpoint.getName().
                        equalsIgnoreCase(stcIntEndptName)) {
                    StringBuffer msg = new StringBuffer(Messages.ssnAffDelMsg);
                    boolean choice = MessageDialog.openQuestion(new Shell(),
                            Messages.dlgDelEndPt1, msg.toString());
                    if (choice) {
                    	if (waEndpoint.getEndPointType().
                    			equals(WindowsAzureEndpointType.Input)){
                    		windowsAzureRole.
                    		setSessionAffinityInputEndpoint(null);
                    		waEndpoint.delete();
                    	} else {
                    		windowsAzureRole.
                    		setSessionAffinityInputEndpoint(null);
                    	}
                    	tblViewer.refresh();
                    }
                }
                /*
                 * Normal end point.
                 */
                else {
                    boolean choice = MessageDialog.openQuestion(new Shell(),
                            Messages.dlgDelEndPt1, Messages.dlgDelEndPt2);
                    if (choice) {
                        waEndpoint.delete();
                        tblViewer.refresh();
                    }
                }
            } catch (WindowsAzureInvalidProjectOperationException e) {
                errorTitle = Messages.adRolErrTitle;
                errorMessage = Messages.adRolErrMsgBox1
                        + Messages.adRolErrMsgBox2;
                MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                        errorMessage);
                Activator.getDefault().log(errorMessage, e);
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
    				MessageUtil.displayErrorDialog(getShell(),
    						Messages.cachDsblErTtl,
    						Messages.endPtEdtErMsg);
    			} else {
    				WAEndpointDialog dialog =
    						new WAEndpointDialog(
    								this.getShell(),
    						windowsAzureRole,
    						waEndpoint, true);
    				dialog.open();
    				tblViewer.refresh(true);
    			}
    		} catch (WindowsAzureInvalidProjectOperationException e) {
    			MessageUtil.displayErrorDialog(getShell(),
    					Messages.genErrTitle,
    					Messages.chEndPtErMsg);
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
            errorTitle = Messages.adRolErrTitle;
            errorMessage = Messages.adRolErrMsgBox1
                    + Messages.adRolErrMsgBox2;
            MessageUtil.displayErrorDialog(this.getShell(), errorTitle,
                    errorMessage);
            Activator.getDefault().log(errorMessage, e);
            okToProceed = false;
        }
        if (okToProceed) {
            okToProceed = super.performOk();
        }
        return okToProceed;
    }

}
