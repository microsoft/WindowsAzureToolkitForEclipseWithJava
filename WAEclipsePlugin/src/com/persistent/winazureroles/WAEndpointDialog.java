/**
* Copyright 2011 Persistent Systems Ltd.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.persistent.winazureroles;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.MessageUtil;

/**
 * This class creates a dialog to edit/add an Endpoint.
 */
public class WAEndpointDialog extends org.eclipse.jface.dialogs.Dialog {

    private Text txtName;
    private Combo comboType;
    // Public port controls
    private Text txtPublicPort;
    private Text txtPublicPortRangeEnd;
    private Label lblPublicPortRangeSeparator;
    private Label lblPublicPort;
    // private port controls
    private Text txtPrivatePort;
    private Text txtPrivatePortRangeEnd;
    private Label lblPrivatePortRangeSeparator;
    private Label lblPrivatePort;

    private WindowsAzureRole windowsAzureRole;
    private WindowsAzureEndpoint waEndpt;
    private static String[] arrType = {
    	WindowsAzureEndpointType.Internal.toString(),
    	WindowsAzureEndpointType.Input.toString(),
        WindowsAzureEndpointType.InstanceInput.toString()};
    private boolean isSamePort;
    private boolean isEditEndpt = false;

    private final static int DIALOG_WIDTH = 250;
    private final static int DIALOG_LEFT_MARGIN = 10;
    private final static int RANGE_MIN = 1;
    private final static int RANGE_MAX = 65535;

    /**
     * Constructor to be called while adding an endpoint.
     * @param parent
     * @param windowsAzureRole
     */
    public WAEndpointDialog(Shell parent,
    		WindowsAzureRole windowsAzureRole) {
        super(parent);
        this.windowsAzureRole = windowsAzureRole;
    }

    /**
     * Constructor to be called while editing an endpoint.
     * @param parent
     * @param windowsAzureRole
     * @param waEndpoint
     * @param isEditEndpt
     */
    public WAEndpointDialog(Shell parent,
    		WindowsAzureRole windowsAzureRole,
            WindowsAzureEndpoint waEndpoint,
            boolean isEditEndpt) {
        super(parent);
        this.isEditEndpt = isEditEndpt;
        this.waEndpt = waEndpoint;
        this.windowsAzureRole = windowsAzureRole;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        String title = Messages.adRolEndPtTitle;
        if (isEditEndpt) {
            title = Messages.endptEditTitle;
        }
        newShell.setText(title);
    }

    /**
     * Method creates UI controls of dialog.
     */
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        container.setLayout(gridLayout);
        GridData gridData = new GridData();
        gridData.widthHint = DIALOG_WIDTH;
        container.setLayoutData(gridData);

        Label lblName = new Label(container, SWT.LEFT);
        gridData = new GridData();
        gridData.horizontalIndent = DIALOG_LEFT_MARGIN;
        lblName.setLayoutData(gridData);
        lblName.setText(Messages.adRolName);

        txtName = new Text(container, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        txtName.setLayoutData(gridData);

        // Type of endpoint control
        createEndptTypeComponent(container);

        // Public port controls
        createPublicPortComponent(container);

        // Private port controls
        createPrivatePortComponent(container);

        if (isEditEndpt) {
            txtName.setText(waEndpt.getName());
            // private port
            String[] prvPortRange = waEndpt.getPrivatePort().split("-");
            txtPrivatePort.setText(prvPortRange[0]);
            if (prvPortRange.length > 1) {
            	txtPrivatePortRangeEnd.setText(prvPortRange[1]);
            }
            // type
            try {
            	comboType.setText(waEndpt.getEndPointType().toString());
            } catch (WindowsAzureInvalidProjectOperationException e) {
            	MessageUtil.displayErrorDialog(this.getShell(),
            			Messages.dlgDbgEndPtErrTtl,
            			Messages.endPtTypeErr);
            }
            // Public port
            String[] portRange = waEndpt.getPort().split("-");
            txtPublicPort.setText(portRange[0]);
            if (portRange.length > 1) {
            	txtPublicPortRangeEnd.setText(portRange[1]);
            }
        }

        enableControlsDependingOnEnpointType(comboType.getText());
        return super.createContents(parent);
    }

    /**
     * Enabled/disables controls depending on endpoint type.
     */
    private void enableControlsDependingOnEnpointType(
    		String endpointType) {
        if (endpointType.equalsIgnoreCase(
        		WindowsAzureEndpointType.Internal.toString())) {
        	// Internal port selected
        	// Handling for private port
        	lblPrivatePort.setText(Messages.adRolPrvPortRng);
        	lblPrivatePortRangeSeparator.setEnabled(true);
        	txtPrivatePortRangeEnd.setEnabled(true);
        	// Handling for public port
            lblPublicPort.setEnabled(false);
            lblPublicPort.setText(Messages.adRolPubPortRange);
            txtPublicPort.setEnabled(false);
            txtPublicPort.setText("");
            txtPublicPortRangeEnd.setText("");
            lblPublicPortRangeSeparator.setEnabled(false);
            txtPublicPortRangeEnd.setEnabled(false);
        } else if (endpointType.equalsIgnoreCase(
        		WindowsAzureEndpointType.Input.toString())) {
        	// Input port selected
        	// Handling for private port
        	lblPrivatePort.setText(Messages.adRolPrivatePort);
        	lblPrivatePortRangeSeparator.setEnabled(false);
        	txtPrivatePortRangeEnd.setEnabled(false);
        	txtPrivatePortRangeEnd.setText("");
        	// Handling for public port
            lblPublicPort.setEnabled(true);
            lblPublicPort.setText(Messages.adRolPubPort);
            txtPublicPort.setEnabled(true);
            lblPublicPortRangeSeparator.setEnabled(false);
            txtPublicPortRangeEnd.setEnabled(false);
            txtPublicPortRangeEnd.setText("");
        }  else {
        	// Instance input point selected
        	// Handling for private port
        	lblPrivatePort.setText(Messages.adRolPrivatePort);
        	lblPrivatePortRangeSeparator.setEnabled(false);
        	txtPrivatePortRangeEnd.setEnabled(false);
        	txtPrivatePortRangeEnd.setText("");
        	// Handling for public port
            lblPublicPort.setEnabled(true);
            lblPublicPort.setText(Messages.adRolPubPortRange);
            txtPublicPort.setEnabled(true);
            lblPublicPortRangeSeparator.setEnabled(true);
            txtPublicPortRangeEnd.setEnabled(true);
        }
    }

    /**
     * Creates an endpoint type component consisting of label and combo box.
     * Also adds a selection listener to combo box.
     *
     * @param container
     */
    private void createEndptTypeComponent(Composite container) {
        Label lblType = new Label(container, SWT.LEFT);
        GridData gridData = new GridData();
        gridData.horizontalIndent = DIALOG_LEFT_MARGIN;
        lblType.setLayoutData(gridData);
        lblType.setText(Messages.adRolType);

        comboType = new Combo(container, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        comboType.setLayoutData(gridData);
        comboType.setItems(arrType);
        comboType.setText(arrType[0]);

        final Combo comboTemp = comboType;

        comboType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
            	enableControlsDependingOnEnpointType(comboTemp.getText());
            }
        });
    }

    /**
     * Creates a public port component consisting of label and text box.
     * Also adds a focus listener to the text box.
     *
     * @param container
     */
    private void createPublicPortComponent(Composite container) {
        // Public port range
        lblPublicPort = new Label(container, SWT.LEFT);
        GridData gridData = new GridData();
        gridData.horizontalIndent = DIALOG_LEFT_MARGIN;
        gridData.widthHint = DIALOG_WIDTH / 2 - DIALOG_LEFT_MARGIN;
        lblPublicPort.setLayoutData(gridData);
        lblPublicPort.setText(Messages.adRolPubPort);

        Composite rangeContainer = new Composite(container, SWT.FILL);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        rangeContainer.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(3,
        		false);
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        rangeContainer.setLayout(gridLayout);

        // Public port range start
        txtPublicPort = new Text(rangeContainer, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = DIALOG_WIDTH / 4;
        txtPublicPort.setLayoutData(gridData);

        // Public port range separator
        lblPublicPortRangeSeparator = new Label(rangeContainer, SWT.CENTER);
        gridData = new GridData();
        lblPublicPortRangeSeparator.setLayoutData(gridData);
        lblPublicPortRangeSeparator.setText("-");

        // Public port range end
        txtPublicPortRangeEnd = new Text(rangeContainer,
        		SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = DIALOG_WIDTH / 4;
        txtPublicPortRangeEnd.setLayoutData(gridData);

        final Combo comboTemp = comboType;
        txtPublicPort.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent event) {
                if (!comboTemp.getText().equalsIgnoreCase(
                        WindowsAzureEndpointType.Internal.toString())
                        && (txtPrivatePort.getText().isEmpty()
                        || isSamePort)) {
                    if (txtPublicPort.getText().contains("-")) {
                    	txtPrivatePort.setText(
                    			txtPublicPort.getText().
                    			split("-")[0]);
                    } else {
                    	txtPrivatePort.setText(txtPublicPort.getText());
                    }
                }
            }
            @Override
            public void focusGained(FocusEvent event) {
                String oldPort = txtPublicPort.getText();
                isSamePort = false;
                if (txtPrivatePort.getText().equals(oldPort)) {
                    isSamePort = true;
                }
            }
        });
    }

    /**
     * Creates a private port component consisting of label and text box.
     * Also adds a focus listener to the text box.
     * @param container
     */
    private void createPrivatePortComponent(Composite container) {
    	lblPrivatePort = new Label(container, SWT.LEFT);
    	GridData gridData = new GridData();
        gridData.horizontalIndent = DIALOG_LEFT_MARGIN;
        gridData.widthHint = DIALOG_WIDTH / 2 - DIALOG_LEFT_MARGIN;
        lblPrivatePort.setLayoutData(gridData);
        lblPrivatePort.setText(Messages.adRolPrivatePort);

        Composite rangeContainer = new Composite(container, SWT.FILL);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        rangeContainer.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(3,
        		false);
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        rangeContainer.setLayout(gridLayout);

        // Private port range start
        txtPrivatePort = new Text(rangeContainer, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = DIALOG_WIDTH / 4;
        txtPrivatePort.setLayoutData(gridData);

        // Private port range separator
        lblPrivatePortRangeSeparator = new Label(rangeContainer, SWT.CENTER);
        gridData = new GridData();
        lblPrivatePortRangeSeparator.setLayoutData(gridData);
        lblPrivatePortRangeSeparator.setText("-");

        // Private port range end
        txtPrivatePortRangeEnd = new Text(rangeContainer,
        		SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = DIALOG_WIDTH / 4;
        txtPrivatePortRangeEnd.setLayoutData(gridData);
    }

    /**
     * Returns combined public port range as a single string.
     * Applicable only when endpoint type is InstanceInput.
     * @return String
     */
    private String combinePublicPortRange() {
    	String portRange;
    	String pubPortStart = txtPublicPort.getText().trim();
    	String pubPortEnd = txtPublicPortRangeEnd.getText().trim();
    	if (comboType.getText().equalsIgnoreCase(
    			WindowsAzureEndpointType.
    			InstanceInput.toString())) {
    		/*
    		 * Always combine both values as
    		 * Instance endpoint stores public port
    		 * in the format of range always.
    		 */
    		if (pubPortEnd.isEmpty()) {
    			pubPortEnd = pubPortStart;
    		}
    		portRange = String.format("%s-%s",
    				pubPortStart, pubPortEnd);
    	} else {
    		portRange = pubPortStart;
    	}
    	return portRange;
    }

    /**
     * Returns combined private port range as a single string.
     * Applicable only when endpoint type is Internal.
     * @return String
     */
    private String combinePrivatePortRange() {
    	String prvPortRange;
    	String prvPortStart = txtPrivatePort.getText().trim();
    	String prvPortEnd = txtPrivatePortRangeEnd.getText().trim();
    	if (comboType.getText().equalsIgnoreCase(
    			WindowsAzureEndpointType.
    			Internal.toString())) {
    		prvPortRange = prvPortStart;
    		/*
    		 * If user has given range's end value
    		 * then only combine both values, and make range
    		 * otherwise only pass single value.
    		 * (For Single value & range we use different tags)
    		 */
    		if (!prvPortEnd.isEmpty()) {
    			prvPortRange = String.format("%s-%s",
    					prvPortRange, prvPortEnd);
    		}
    	} else {
    		prvPortRange = prvPortStart;
    	}
    	return prvPortRange;
    }

    @Override
    protected void okPressed() {
        boolean okToProceed = true;
        Boolean isDash = false;
        try {
            if (isEditEndpt) {
                //Edit an endpoint scenario
                okToProceed = editEndpt();
            } else {
                //Add an endpoint scenario
            	// validate name
            	WindowsAzureEndpointType endPtType = WindowsAzureEndpointType.
                		valueOf(comboType.getText());
                boolean isValidName = windowsAzureRole.isAvailableEndpointName(
                		txtName.getText(),
                		endPtType);
                if (isValidName) {
                	if (endPtType.equals(WindowsAzureEndpointType.
                			InstanceInput)
                			|| endPtType.equals(WindowsAzureEndpointType.
                					Internal)) {
                		isDash = isDashPresent(endPtType);
                	}
                	if (isDash) {
                		MessageUtil.displayErrorDialog(this.getShell(),
            					Messages.dlgInvldPort,
            					Messages.portRangeErrMsg);
            			okToProceed = false;
                	} else {
                		// Check for valid range 1 to 65535
                		if (isValidPortRange(endPtType)) {
                			// Combine port range
                			String publicPort = combinePublicPortRange();
                			String privatePort = combinePrivatePortRange();

                			// Validate and commit endpoint addition
                			if (windowsAzureRole.isValidEndpoint(
                					txtName.getText().trim(),
                					endPtType,
                					privatePort, publicPort)) {
                				windowsAzureRole.addEndpoint(
                						txtName.getText().trim(),
                						endPtType,
                						privatePort, publicPort);
                			} else {
                				MessageUtil.displayErrorDialog(this.getShell(),
                						Messages.dlgInvldPort,
                						Messages.dlgPortInUse);
                				okToProceed = false;
                			}
                		} else {
                			MessageUtil.displayErrorDialog(this.getShell(),
                					Messages.dlgInvldPort,
                					Messages.rngErrMsg);
                			okToProceed = false;
                		}
                	}
                } else {
                	MessageUtil.displayErrorDialog(this.getShell(),
                			Messages.dlgInvdEdPtName1,
                			Messages.dlgInvdEdPtName2);
                	okToProceed = false;
                }
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	MessageUtil.displayErrorDialogAndLog(this.getShell(),
        			Messages.rolsErr,
        			Messages.adRolErrMsgBox1 + Messages.adRolErrMsgBox2,
        			e);
        }
        if (okToProceed) {
            super.okPressed();
        }
    }

    /**
     * This method edits an endpoint.
     * For editing it also validates endpoint name and ports.
     *
     * @return retVal : false if any error occurs.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean editEndpt()
            throws WindowsAzureInvalidProjectOperationException {
        boolean retVal = true;
        String oldEndptName = waEndpt.getName();
        if (!oldEndptName.equalsIgnoreCase(txtName.getText())) {
            //validate endpoint name
            boolean isValid = windowsAzureRole.isAvailableEndpointName(
            		txtName.getText(),
            		WindowsAzureEndpointType.
            		valueOf(comboType.getText()));
            if (!isValid) {
                //if name is not valid
                MessageUtil.displayErrorDialog(this.getShell(),
                		Messages.dlgInvdEdPtName1,
                		Messages.dlgInvdEdPtName2);
                retVal = false;
            }
        }
        if (retVal) {
            retVal = validatePorts(oldEndptName);
        }
        return retVal;
    }

    /**
     * Validates public and private ports.
     * And also makes changes corresponding to the debug endpoint.
     *
     * @param oldEndptName : old name of the endpoint.
     * @return retVal : false if any error occurs.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean validatePorts(String oldEndptName)
            throws WindowsAzureInvalidProjectOperationException {
        boolean retVal = true;
        boolean isDash = false;
        WindowsAzureEndpointType oldType = waEndpt.getEndPointType();
        WindowsAzureEndpoint debugEndpt = windowsAzureRole.
        		getDebuggingEndpoint();
        WindowsAzureEndpoint stickyEndpt = windowsAzureRole.
        		getSessionAffinityInputEndpoint();
        WindowsAzureEndpoint stickyIntEndpt = windowsAzureRole.
        		getSessionAffinityInternalEndpoint();
        String stcEndptName = "";
        String dbgEndptName = "";
        String stcIntEndptName = "";
        if (debugEndpt != null) {
            //get the debugging endpoint name
            dbgEndptName = debugEndpt.getName();
        }
        if (stickyEndpt != null) {
            stcEndptName = stickyEndpt.getName();
            stcIntEndptName = stickyIntEndpt.getName();
        }

        WindowsAzureEndpointType newType =
        		WindowsAzureEndpointType.valueOf(comboType.getText());
        if (newType.equals(WindowsAzureEndpointType.InstanceInput)
    			|| newType.equals(WindowsAzureEndpointType.Internal)) {
    		isDash = isDashPresent(newType);
    	}
        if (isDash) {
        	MessageUtil.displayErrorDialog(this.getShell(),
        			Messages.dlgInvldPort,
        			Messages.portRangeErrMsg);
        	retVal = false;
        } else {
        	// Check for valid range 1 to 65535
        	if (isValidPortRange(newType)) {
        		//validate ports
        		String publicPort = combinePublicPortRange();
        		String privatePort = combinePrivatePortRange();

        		boolean isValidendpoint = windowsAzureRole.isValidEndpoint(
        				oldEndptName,
        				newType,
        				privatePort, publicPort);
        		if (isValidendpoint) {
        			if (oldEndptName.equalsIgnoreCase(dbgEndptName)) {
        				retVal = handleChangeForDebugEndpt(oldType);
        			}
        			if (oldEndptName.equalsIgnoreCase(stcEndptName)) {
        				retVal = handleChangeForStickyEndpt(oldType);
        			}
        			if (oldEndptName.equalsIgnoreCase(stcIntEndptName)) {
        				retVal = handleChangeForStickyEndpt(oldType);
        			}
        			if (retVal) {
        				//set the new values in the endpoint object.
        				waEndpt.setEndPointType(WindowsAzureEndpointType
        						.valueOf(comboType.getText()));
        				waEndpt.setName(txtName.getText());
        				/*
        				 * Type is Input or Instance then
        				 * set public port as well as private port.
        				 */
        				if (comboType.getText().equalsIgnoreCase(
        						WindowsAzureEndpointType.Input.toString())
        						|| comboType.getText().equalsIgnoreCase(
        								WindowsAzureEndpointType.
        								InstanceInput.toString())) {
        					waEndpt.setPort(publicPort);
        				}
        				/*
        				 * Type is Internal then
        				 * set private port only.
        				 */
        				waEndpt.setPrivatePort(privatePort);
        			}
        		} else {
        			MessageUtil.displayErrorDialog(this.getShell(),
        					Messages.dlgInvldPort,
        					Messages.dlgPortInUse);
        			retVal = false;
        		}
        	} else {
        		MessageUtil.displayErrorDialog(this.getShell(),
        				Messages.dlgInvldPort,
        				Messages.rngErrMsg);
        		retVal = false;
        	}
        }
        return retVal;
    }

    /**
     * Disables the debugging if debug endpoint's type is changed to 'Internal',
     * and if private port is modified then assigns the new debugging port
     * by setting the modified endpoint as a debugging endpoint.
     *
     * @param oldType : old type of the endpoint.
     * @return retVal : false if any error occurs.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean handleChangeForDebugEndpt(
    		WindowsAzureEndpointType oldType)
            throws WindowsAzureInvalidProjectOperationException {
        boolean retVal = true;
        if (oldType.equals(WindowsAzureEndpointType.Input)
                && comboType.getText().equalsIgnoreCase(
                WindowsAzureEndpointType.Internal.toString())) {
            StringBuffer msg = new StringBuffer(Messages.dlgEPDel);
            msg.append(Messages.dlgEPChangeType);
            msg.append(Messages.dlgEPDel2);
            boolean choice = MessageDialog.
            		openQuestion(new Shell(),
            				Messages.dlgTypeTitle,
            				msg.toString());
            if (choice) {
                waEndpt.setEndPointType(
                		WindowsAzureEndpointType.
                		valueOf(comboType.getText()));
                windowsAzureRole.setDebuggingEndpoint(null);
            } else {
                retVal = false;
            }
        } else if (!waEndpt.getPrivatePort().
        		equalsIgnoreCase(txtPrivatePort.getText())) {
            boolean isSuspended = windowsAzureRole.getStartSuspended();
            windowsAzureRole.setDebuggingEndpoint(null);
            waEndpt.setPrivatePort(txtPrivatePort.getText());
            windowsAzureRole.setDebuggingEndpoint(waEndpt);
            windowsAzureRole.setStartSuspended(isSuspended);
        }
        return retVal;
    }

    /**
     * Disables the session affinity
     * if endpoint's type is changed to 'Internal'.
     *
     * @param oldType : old type of the end point.
     * @return retVal : false if any error occurs.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    private boolean handleChangeForStickyEndpt(
    		WindowsAzureEndpointType oldType)
            throws WindowsAzureInvalidProjectOperationException {
        boolean retVal = true;
        return retVal;
    }

    /**
     * Method checks if text box contains '-' character.
     * @param type
     * @return boolean
     */
    private boolean isDashPresent(WindowsAzureEndpointType type) {
    	boolean isPresent = false;
    	if (type.equals(WindowsAzureEndpointType.Internal)) {
    		if (txtPrivatePort.getText().contains("-")
    				|| txtPrivatePortRangeEnd.
    				getText().contains("-")) {
    			isPresent = true;
    		} else {
    			isPresent = false;
    		}
    	} else if (type.equals(WindowsAzureEndpointType.InstanceInput)) {
    		if (txtPublicPort.getText().contains("-")
    				|| txtPublicPortRangeEnd.
    				getText().contains("-")) {
    			isPresent = true;
    		} else {
    			isPresent = false;
    		}
    	}
    	return isPresent;
    }

    /**
     * Method checks if port is within valid range 1 to 65535 or not.
     * @param type
     * @return boolean
     */
    private boolean isValidPortRange(WindowsAzureEndpointType type) {
    	boolean isValid = true;
    	try {
    		if (type.equals(WindowsAzureEndpointType.Internal)) {
    			int rngStart = Integer.
    					parseInt(txtPrivatePort.getText());
    			if (rngStart >= RANGE_MIN
    					&& rngStart <= RANGE_MAX) {
    				if (!txtPrivatePortRangeEnd.
    						getText().equals("")) {
    					int rngEnd = Integer.
    							parseInt(txtPrivatePortRangeEnd.getText());
    					if (!(rngEnd >= RANGE_MIN
    							&& rngEnd <= RANGE_MAX)) {
    						isValid = false;
    					}
    				}
    			} else {
    				isValid = false;
    			}
    		} else if (type.equals(WindowsAzureEndpointType.Input)) {
    			int pubPort = Integer.parseInt(txtPublicPort.getText());
    			int priPort = Integer.parseInt(txtPrivatePort.getText());
    			if (!(pubPort >= RANGE_MIN
    					&& pubPort <= RANGE_MAX
    					&& priPort >= RANGE_MIN
    					&& priPort <= RANGE_MAX)) {
    				isValid = false;
    			}
    		} else if (type.equals(WindowsAzureEndpointType.InstanceInput)) {
    			int priPort = Integer.parseInt(txtPrivatePort.getText());
    			if (priPort >= RANGE_MIN
    					&& priPort <= RANGE_MAX) {
    				int rngStart = Integer.parseInt(txtPublicPort.getText());
    				if (rngStart >= RANGE_MIN
    						&& rngStart <= RANGE_MAX) {
    					if (!txtPublicPortRangeEnd.getText().equals("")) {
    						int rngEnd = Integer.parseInt(
    								txtPublicPortRangeEnd.getText());
    						if (!(rngEnd >= RANGE_MIN
    								&& rngEnd <= RANGE_MAX)) {
    							isValid = false;
    						}
    					}
    				} else {
    					isValid = false;
    				}
    			} else {
    				isValid = false;
    			}
    		}
    	} catch (NumberFormatException e) {
    		isValid = false;
    	}
    	return isValid;
    }
}
