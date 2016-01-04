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
import com.microsoftopentechnologies.azurecommons.roleoperations.WAEndpointDialogUtilMethods;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

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
    private final static int DIALOG_LEFT_MARGIN = 5;
    private static String auto = "(auto)";

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

        // Edit Endpoint scenario
        if (isEditEndpt) {
        	txtName.setText(waEndpt.getName());
        	// type
        	WindowsAzureEndpointType type = null;
        	try {
        		type = waEndpt.getEndPointType();
        		comboType.setText(type.toString());
        	} catch (WindowsAzureInvalidProjectOperationException e) {
        		PluginUtil.displayErrorDialogAndLog(
        				this.getShell(),
        				Messages.dlgDbgEndPtErrTtl,
        				Messages.endPtTypeErr, e);
        	}
            // private port
            String prvPort = waEndpt.getPrivatePort();
            if (prvPort == null
            		&& !type.equals(
            				WindowsAzureEndpointType.InstanceInput)) {
            	txtPrivatePort.setText(auto);
            } else {
            	String[] prvPortRange = prvPort.split("-");
            	txtPrivatePort.setText(prvPortRange[0]);
            	if (prvPortRange.length > 1) {
            		txtPrivatePortRangeEnd.setText(prvPortRange[1]);
            	}
            }
            
            // Public port
            String[] portRange = waEndpt.getPort().split("-");
            txtPublicPort.setText(portRange[0]);
            if (portRange.length > 1) {
            	txtPublicPortRangeEnd.setText(portRange[1]);
            }
        } else {
        	/*
        	 * Add Endpoint scenario.
        	 * Endpoint type is Internal for the first time.
        	 */
        	txtPrivatePort.setText(auto);
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
        		String comboTxt = comboTemp.getText();
        		String portTxt = txtPrivatePort.getText();
        		enableControlsDependingOnEnpointType(comboTxt);
        		/*
        		 * auto not allowed for InstanceInput endpoint,
        		 * hence clear it.
        		 */
        		if (comboTxt.equalsIgnoreCase(
        				WindowsAzureEndpointType.InstanceInput.toString())
        				&& portTxt.equalsIgnoreCase(auto)) {
        			txtPrivatePort.setText("");
        		} else if (comboTxt.equalsIgnoreCase(
        				WindowsAzureEndpointType.Input.toString())
        				&& (portTxt.isEmpty()
        						|| portTxt.equalsIgnoreCase("*"))) {
        			txtPrivatePort.setText(auto);
        		} else if (comboTxt.equalsIgnoreCase(
        				WindowsAzureEndpointType.Internal.toString())
        				&& (portTxt.isEmpty()
        						|| portTxt.equalsIgnoreCase("*"))
        				&& txtPrivatePortRangeEnd.getText().isEmpty()) {
        			txtPrivatePort.setText(auto);
        		}
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

        txtPrivatePort.addFocusListener(new FocusListener() {
        	@Override
        	public void focusLost(FocusEvent arg0) {
        		focusLostMethod();
        	}

        	@Override
        	public void focusGained(FocusEvent arg0) {
        		if (txtPrivatePort.getText().equalsIgnoreCase(auto)) {
        			txtPrivatePort.setText("");
        		}
        	}
        });

        txtPrivatePortRangeEnd.addFocusListener(new FocusListener() {
        	@Override
        	public void focusLost(FocusEvent arg0) {
        		focusLostMethod();
        	}

        	@Override
        	public void focusGained(FocusEvent arg0) {
        		if (txtPrivatePort.getText().equalsIgnoreCase(auto)) {
        			txtPrivatePort.setText("");
        		}
        	}
        });
    }

    private void focusLostMethod() {
    	String text = txtPrivatePort.getText();
    	if ((text.isEmpty()
    			|| text.equalsIgnoreCase("*"))
    			&& txtPrivatePortRangeEnd.getText().isEmpty()
    			&& !comboType.getText().equalsIgnoreCase(
    					WindowsAzureEndpointType.
    					InstanceInput.toString())) {
    		txtPrivatePort.setText(auto);
    	}
    }

    /**
     * Returns combined public port range as a single string.
     * Applicable only when endpoint type is InstanceInput.
     * @return String
     */
    private String combinePublicPortRange() {
    	String portRange = WAEndpointDialogUtilMethods.
    			combinePublicPortRange(
    					txtPublicPort.getText().trim(),
    					txtPublicPortRangeEnd.getText().trim(),
    					comboType.getText());
    	return portRange;
    }

    /**
     * Returns combined private port range as a single string.
     * Applicable only when endpoint type is Internal.
     * @return String
     */
    private String combinePrivatePortRange() {
    	String prvPortRange = WAEndpointDialogUtilMethods.
    			combinePrivatePortRange(txtPrivatePort.getText().trim(),
    					txtPrivatePortRangeEnd.getText().trim(),
    					comboType.getText());
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
            	String endPtName = txtName.getText().trim();
            	/*
            	 * Check endpoint name contain
            	 * alphanumeric and underscore characters only.
            	 * Starts with alphabet.
            	 */
            	if (WAEclipseHelperMethods.isAlphaNumericUnderscore(endPtName)) {
            		boolean isValidName = windowsAzureRole.
            				isAvailableEndpointName(
            						endPtName,
            						endPtType);
            		/*
            		 * Check already used endpoint name is given.
            		 */
            		if (isValidName) {
            			if (endPtType.equals(WindowsAzureEndpointType.
            					InstanceInput)
            					|| endPtType.equals(WindowsAzureEndpointType.
            							Internal)) {
            				isDash = isDashPresent(endPtType);
            			}
            			if (isDash) {
            				PluginUtil.displayErrorDialog(
            						this.getShell(),
            						Messages.dlgInvldPort,
            						Messages.portRangeErrMsg);
            				okToProceed = false;
            			} else {
            				// Check for valid range 1 to 65535
            				if (isValidPortRange(endPtType)) {
            					// Combine port range
            					String publicPort = combinePublicPortRange();
            					String privatePort = combinePrivatePortRange();
            					if (privatePort.equalsIgnoreCase(auto)) {
            						privatePort = null;
            					}
            					// Validate and commit endpoint addition
            					if (windowsAzureRole.isValidEndpoint(
            							endPtName,
            							endPtType,
            							privatePort, publicPort)) {
            						windowsAzureRole.addEndpoint(
            								endPtName,
            								endPtType,
            								privatePort, publicPort);
            					} else {
            						PluginUtil.displayErrorDialog(
            								this.getShell(),
            								Messages.dlgInvldPort,
            								Messages.dlgPortInUse);
            						okToProceed = false;
            					}
            				} else {
            					PluginUtil.displayErrorDialog(
            							this.getShell(),
            							Messages.dlgInvldPort,
            							Messages.rngErrMsg);
            					okToProceed = false;
            				}
            			}
            		} else {
            			PluginUtil.displayErrorDialog(this.getShell(),
            					Messages.dlgInvdEdPtName1,
            					Messages.dlgInvdEdPtName2);
            			okToProceed = false;
            		}
            	} else {
            		PluginUtil.displayErrorDialog(this.getShell(),
            				Messages.dlgInvdEdPtName1,
            				Messages.enPtAlphNuMsg);
            		okToProceed = false;
            	}
            }
        } catch (WindowsAzureInvalidProjectOperationException e) {
        	PluginUtil.displayErrorDialogAndLog(this.getShell(),
        			Messages.rolsErr,
        			Messages.adRolErrMsgBox1
        			+ Messages.adRolErrMsgBox2,
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
        String newEndptName = txtName.getText().trim();
        //validate endpoint name
        /*
         * Ignore if end point name is not edited.
         */
        if (!oldEndptName.equalsIgnoreCase(newEndptName)) {
        	/*
        	 * Check endpoint name contain
        	 * alphanumeric and underscore characters only.
        	 * Starts with alphabet.
        	 */
        	if (WAEclipseHelperMethods.isAlphaNumericUnderscore(newEndptName)) {
        		/*
        		 * Check already used endpoint name is given.
        		 */
        		boolean isValid = windowsAzureRole.isAvailableEndpointName(
        				newEndptName,
        				WindowsAzureEndpointType.
        				valueOf(comboType.getText()));
        		if (!isValid) {
        			//if name is not valid
        			PluginUtil.displayErrorDialog(this.getShell(),
        					Messages.dlgInvdEdPtName1,
        					Messages.dlgInvdEdPtName2);
        			retVal = false;
        		}
        	} else {
        		PluginUtil.displayErrorDialog(this.getShell(),
        				Messages.dlgInvdEdPtName1,
        				Messages.enPtAlphNuMsg);
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
        	PluginUtil.displayErrorDialog(this.getShell(),
        			Messages.dlgInvldPort,
        			Messages.portRangeErrMsg);
        	retVal = false;
        } else {
        	// Check for valid range 1 to 65535
        	if (isValidPortRange(newType)) {
        		//validate ports
        		String publicPort = combinePublicPortRange();
        		String privatePort = combinePrivatePortRange();
        		if (privatePort.equalsIgnoreCase(auto)) {
        			privatePort = null;
        		}

        		boolean isValidendpoint = windowsAzureRole.isValidEndpoint(
        				oldEndptName,
        				newType,
        				privatePort, publicPort);
        		if (isValidendpoint) {
        			if (oldEndptName.equalsIgnoreCase(dbgEndptName)) {
        				retVal = handleChangeForDebugEndpt(oldType, privatePort);
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
        			PluginUtil.displayErrorDialog(this.getShell(),
        					Messages.dlgInvldPort,
        					Messages.dlgPortInUse);
        			retVal = false;
        		}
        	} else {
        		PluginUtil.displayErrorDialog(this.getShell(),
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
    		WindowsAzureEndpointType oldType, String privatePort)
            throws WindowsAzureInvalidProjectOperationException {
        boolean retVal = true;
        if (oldType.equals(WindowsAzureEndpointType.Input)
                && comboType.getText().equalsIgnoreCase(
                WindowsAzureEndpointType.Internal.toString())) {
        	boolean choice = MessageDialog.
        			openQuestion(getShell(),
        					Messages.dlgTypeTitle,
        					String.format("%s%s%s",
        							Messages.dlgEPDel,
        							Messages.dlgEPChangeType,
        							Messages.dlgEPDel2));
            if (choice) {
                waEndpt.setEndPointType(
                		WindowsAzureEndpointType.
                		valueOf(comboType.getText()));
                windowsAzureRole.setDebuggingEndpoint(null);
            } else {
                retVal = false;
            }
        } else if (privatePort == null) {
        	PluginUtil.displayErrorDialog(
    				getShell(),
    				Messages.dlgInvldPort,
    				Messages.dbgPort);
        	retVal = false;
        } else if (!waEndpt.getPrivatePort().
        		equalsIgnoreCase(privatePort)) {
        	boolean isSuspended = windowsAzureRole.getStartSuspended();
        	windowsAzureRole.setDebuggingEndpoint(null);
        	waEndpt.setPrivatePort(privatePort);
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
    	boolean isPresent = WAEndpointDialogUtilMethods.
    			isDashPresent(type,
    					txtPrivatePort.getText().trim(),
    					txtPrivatePortRangeEnd.getText().trim(),
    					txtPublicPort.getText().trim(),
    					txtPublicPortRangeEnd.getText().trim());
    	return isPresent;
    }

    /**
     * Method checks if port is within valid range 1 to 65535 or not.
     * @param type
     * @return boolean
     */
    private boolean isValidPortRange(WindowsAzureEndpointType type) {
    	boolean isValid = WAEndpointDialogUtilMethods.
    			isValidPortRange(type,
    					txtPrivatePort.getText().trim(),
    					txtPrivatePortRangeEnd.getText().trim(),
    					txtPublicPort.getText().trim(),
    					txtPublicPortRangeEnd.getText().trim());
    	return isValid;
    }
}
