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

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureCertificate;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.microsoftopentechnologies.azurecommons.roleoperations.WASSLOffloadingUtilMethods;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.util.WAEclipseHelper;

public class WASSLOffloading extends PropertyPage {

	private WindowsAzureProjectManager waProjManager;
	private WindowsAzureRole waRole;
	private List<WindowsAzureEndpoint> endpointsList;
	private Button btnSSLOffloading;
	private Label lblEndptToUse;
	private Combo comboEndpt;
	private Label lblCert;
	private Combo comboCert;
	private Link linkEndpoint;
	private Link linkCert;
	private boolean isPageDisplayed = false;
	private final int HTTP_PORT = 80;
	private final int HTTPS_PORT = 443;
	private final int HTTPS_NXT_PORT = 8443;

	@Override
	public String getTitle() {
		if (!isPageDisplayed) {
			return super.getTitle();
		}
		try {
			if (waRole != null) {
				WindowsAzureEndpoint sslEndpt = waRole.getSslOffloadingInputEndpoint();
				if (sslEndpt == null) {
					btnSSLOffloading.setSelection(false);
					enableDisableControls(false);
				} else {
					btnSSLOffloading.setSelection(true);
					enableDisableControls(true);
					populateCertList();
					populateEndPointList();
					comboCert.setText(waRole.getSslOffloadingCert().getName());
					comboEndpt.setText(String.format(Messages.dbgEndPtStr,
							sslEndpt.getName(),
							sslEndpt.getPort(),
							sslEndpt.getPrivatePort()));
					isEditableEndpointCombo(sslEndpt);
				}
			}
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.adRolErrTitle,
					Messages.dlgDbgErr, e);
		}
		return super.getTitle();
	}

	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.persistent.winazure.eclipseplugin."
						+ "windows_azure_ssl_offloading_page");
		waProjManager = Activator.getDefault().getWaProjMgr();
		waRole = Activator.getDefault().getWaRole();
		Activator.getDefault().setSaved(false);

		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		createChkBox(container);
		createEndpointComponents(container);
		createCertComponents(container);
		createNote(container);

		isPageDisplayed = true;
		return container;
	}

	private void createChkBox(Composite container) {
		btnSSLOffloading = new Button(container, SWT.CHECK);
		btnSSLOffloading.setText(Messages.lbEnblSslOff);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		btnSSLOffloading.setLayoutData(gridData);
		btnSSLOffloading.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				if (event.getSource() instanceof Button) {
					Button checkBox = (Button) event.getSource();
					if (checkBox.getSelection()) {
						enableDisableControls(true);
						enableSslOffloading();
					} else {
						try {
							enableDisableControls(false);
							waRole.setSslOffloading(null, null);
						} catch (WindowsAzureInvalidProjectOperationException ex) {
							PluginUtil.displayErrorDialogAndLog(
									getShell(),
									Messages.adRolErrTitle,
									Messages.adRolErrMsgBox1
									+ Messages.adRolErrMsgBox2, ex);
						}
					}
				}
				removeErrorMsg();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	protected void enableSslOffloading() {
		try {
			WindowsAzureEndpoint endpt = null;
			populateEndPointList();
			populateCertList();
			endpt = findInputEndpt();
			populateEndPointList();
			/*
			 * If Endpoint is null,
			 * 1. Check if session affinity
			 * is enabled or no appropriate endpoints to populate,
			 * if yes then uncheck
			 * SSL check box
			 * 2. Else don't do anything
			 * keep blank in combo box.
			 */
			if (endpt == null) {
				if (waRole.getSessionAffinityInputEndpoint() != null) {
					btnSSLOffloading.setSelection(false);
					enableDisableControls(false);
				}
				/*
				 * No endpoints appropriate for SSL offloading,
				 * neither user wants to create new endpoint
				 * nor there is single valid endpoint on that role to list in endpoint combo box
				 * (i.e. zero endpoints on that role or all endpoints of type internal)
				 * then just prompt and exit
				 */
				else if (comboEndpt.getItemCount() < 1) {
					MessageDialog.openWarning(this.getShell(),
							Messages.sslTtl,
							Messages.noEndPtMsg);
					btnSSLOffloading.setSelection(false);
					enableDisableControls(false);
				}
			} else {
				comboEndpt.setText(String.format(Messages.dbgEndPtStr,
						endpt.getName(),
						endpt.getPort(),
						endpt.getPrivatePort()));
				isEditableEndpointCombo(endpt);
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					this.getShell(),
					Messages.adRolErrTitle,
					Messages.adRolErrMsgBox1
					+ Messages.adRolErrMsgBox2, e);
		}
	}

	private WindowsAzureEndpoint findInputEndpt()
			throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureEndpoint endpt = null;
		WindowsAzureEndpoint sessionAffEndPt = waRole.getSessionAffinityInputEndpoint();
		// check session affinity is already enabled, then consider same endpoint
		if (sessionAffEndPt != null) {
			// check port of session affinity endpoint
			String stSesPubPort = sessionAffEndPt.getPort();
			if (stSesPubPort.equalsIgnoreCase(String.valueOf(HTTP_PORT))) {
				// check 443 is already available on same role (input enpoint)
				WindowsAzureEndpoint httpsEndPt = WAEclipseHelperMethods.
						findEndpointWithPubPort(HTTPS_PORT, waRole);
				if (httpsEndPt != null) {
					/*
					 * If HTTPS endpoint with public port 443,
					 * is present on same role then show warning
					 */
					MessageDialog.openWarning(this.getShell(), Messages.sslTtl,
							String.format(Messages.httpsPresentSt,
									httpsEndPt.getName(),
									httpsEndPt.getPort(),
									httpsEndPt.getName()));
					endpt = null;
				} else {
					/*
					 * Check if 443 is used on same role (instance endpoint)
					 * or any other role
					 * if yes then consider 8443.
					 */
					int portToUse = HTTPS_PORT;
					if (WAEclipseHelperMethods.findRoleWithEndpntPubPort(HTTPS_PORT, waProjManager) != null) {
						// need to use 8443
						int pubPort = HTTPS_NXT_PORT;
						while (!waProjManager.isValidPort(
								String.valueOf(pubPort),
								WindowsAzureEndpointType.Input)) {
							pubPort++;
						}
						portToUse = pubPort;
					}
					boolean yes = MessageDialog.openQuestion(this.getShell(),
							Messages.sslTtl,
							Messages.sslhttp
							.replace("${epName}", sessionAffEndPt.getName())
							.replace("${pubPort}", String.valueOf(portToUse))
							.replace("${privPort}", sessionAffEndPt.getPrivatePort()));
					if (yes) {
						sessionAffEndPt.setPort(String.valueOf(portToUse));
						endpt = sessionAffEndPt;
					} else {
						// no button pressed
						endpt = null;
					}
				}
			} else {
				// port is other than 80, then directly consider it.
				endpt = sessionAffEndPt;
			}
		} else {
			// check this role uses public port 443
			endpt = WAEclipseHelperMethods.findEndpointWithPubPort(HTTPS_PORT, waRole);
			if (endpt != null) {
				// endpoint on this role uses public port 443
				MessageDialog.openWarning(this.getShell(), Messages.sslTtl, Messages.sslWarnMsg);
			} else {
				// check if another role uses 443 as a public port
				WindowsAzureRole roleWithHTTPS = WAEclipseHelperMethods.
						findRoleWithEndpntPubPort(HTTPS_PORT, waProjManager);
				if (roleWithHTTPS != null) {
					// another role uses 443 as a public port
					// 1. If this role uses public port 80
					endpt = WAEclipseHelperMethods.findEndpointWithPubPort(HTTP_PORT, waRole);
					if (endpt != null) {
						/*
						 * endpoint on this role uses public port 80
						 * and 443 has been used on some other role then set to 8443
						 * or some suitable public port
						 */
						int pubPort = HTTPS_NXT_PORT;
						while (!waProjManager.isValidPort(
								String.valueOf(pubPort),
								WindowsAzureEndpointType.Input)) {
							pubPort++;
						}
						boolean yes = MessageDialog.openQuestion(this.getShell(),
								Messages.sslTtl,
								Messages.sslhttp
								.replace("${epName}", endpt.getName())
								.replace("${pubPort}", String.valueOf(pubPort))
								.replace("${privPort}", endpt.getPrivatePort()));
						if (yes) {
							endpt.setPort(String.valueOf(pubPort));
						} else {
							// no button pressed
							endpt = null;
						}
					} else {
						// 2. Ask for creating new endpoint
						List<String> endPtData = WASSLOffloadingUtilMethods.
								prepareEndpt(HTTPS_NXT_PORT,
										waRole, waProjManager);
						boolean yes = MessageDialog.openQuestion(this.getShell(),
								Messages.sslTtl,
								String.format(Messages.sslNoHttp,
										endPtData.get(0),
										endPtData.get(1),
										endPtData.get(2)));
						if (yes) {
							endpt = waRole.addEndpoint(endPtData.get(0),
									WindowsAzureEndpointType.Input,
									endPtData.get(2),
									endPtData.get(1));
						} else {
							// no button pressed
							endpt = null;
						}
					}
				} else {
					// no public port 443 on this role, nor on other any role
					// 1. If this role uses public port 80
					endpt = WAEclipseHelperMethods.findEndpointWithPubPort(HTTP_PORT, waRole);
					if (endpt != null) {
						// endpoint on this role uses public port 80
						boolean yes = MessageDialog.openQuestion(this.getShell(),
								Messages.sslTtl,
								Messages.sslhttp
								.replace("${epName}", endpt.getName())
								.replace("${pubPort}", String.valueOf(HTTPS_PORT))
								.replace("${privPort}", endpt.getPrivatePort()));
						if (yes) {
							endpt.setPort(String.valueOf(HTTPS_PORT));
						} else {
							// no button pressed
							endpt = null;
						}
					} else {
						// 2. Ask for creating new endpoint
						List<String> endPtData = WASSLOffloadingUtilMethods.
								prepareEndpt(HTTPS_PORT, waRole, waProjManager);
						boolean yes = MessageDialog.openQuestion(this.getShell(),
								Messages.sslTtl,
								String.format(Messages.sslNoHttp,
										endPtData.get(0),
										endPtData.get(1),
										endPtData.get(2)));
						if (yes) {
							endpt = waRole.addEndpoint(endPtData.get(0),
									WindowsAzureEndpointType.Input,
									endPtData.get(2),
									endPtData.get(1));
						} else {
							// no button pressed
							endpt = null;
						}
					}
				}
			}
		}
		return endpt;
	}

	private void createEndpointComponents(Composite container) {
		lblEndptToUse = createLabel(container, Messages.lbEndptToUse, false);

		comboEndpt = createCombo(container, false);
		comboEndpt.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				endpointComboListener();
				removeErrorMsg();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		linkEndpoint = new Link(container, SWT.RIGHT);
		linkEndpoint.setText(Messages.linkLblEndpt);
		linkEndpoint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				endpointLinkListener();
			}
		});
	}

	private void endpointComboListener() {
		try {
			String newText = comboEndpt.getText();
			int port = Integer.valueOf(newText.substring(newText.indexOf(":") + 1, newText.indexOf(",")));
			if (port == HTTPS_PORT) {
				// user trying to set endpoint with public port 443
				MessageDialog.openWarning(this.getShell(), Messages.sslTtl, Messages.sslWarnMsg);
			} else if (port == HTTP_PORT) {
				WindowsAzureEndpoint httpsEndPt = WAEclipseHelperMethods.findEndpointWithPubPort(HTTPS_PORT, waRole);
				if (httpsEndPt != null) {
					/*
					 * If HTTPS endpoint with public port 443,
					 * is present on same role and listed in endpoint combo box
					 * then show warning
					 */
					MessageDialog.openWarning(this.getShell(), Messages.sslTtl,
							String.format(Messages.httpsPresent,
									httpsEndPt.getName(),
									httpsEndPt.getPort()));
					comboEndpt.deselectAll();
				} else {
					WindowsAzureRole role = WAEclipseHelperMethods.
							findRoleWithEndpntPubPort(HTTPS_PORT, waProjManager);
					WindowsAzureEndpoint httpEndPt = WAEclipseHelperMethods.findEndpointWithPubPort(HTTP_PORT, waRole);
					int pubPort = HTTPS_NXT_PORT;
					if (role != null) {
						/*
						 * Else if endpoint with public port 443
						 * is already used by some other role or
						 * on same role but with type InstanceInput
						 * then prompt for changing port 80
						 * with the next available public port starting with 8443
						 * across all roles
						 */
						while (!waProjManager.isValidPort(
								String.valueOf(pubPort),
								WindowsAzureEndpointType.Input)) {
							pubPort++;
						}
					} else {
						// Else prompt for changing port 80 with 443 across all roles
						pubPort = HTTPS_PORT;
					}
					boolean yes = MessageDialog.openQuestion(this.getShell(),
							Messages.sslTtl,
							Messages.sslhttp
								.replace("${epName}", httpEndPt.getName())
								.replace("${pubPort}", String.valueOf(pubPort))
								.replace("${privPort}", httpEndPt.getPrivatePort()));
					if (yes) {
						httpEndPt.setPort(String.valueOf(pubPort));
						populateEndPointList();
						comboEndpt.setText(String.format(Messages.dbgEndPtStr,
								httpEndPt.getName(),
								httpEndPt.getPort(),
								httpEndPt.getPrivatePort()));
						isEditableEndpointCombo(httpEndPt);
					} else {
						comboEndpt.deselectAll();
					}
				}
			}
		} catch (Exception e) {
			Activator.getDefault().log(Messages.sslTtl, e);
		}
	}

	private void endpointLinkListener() {
		// open remote access dialog
		String curSel = comboEndpt.getText();
		Object endpoint =
				new WAREndpoints();
		int btnId = PluginUtil.
				openPropertyPageDialog(
						com.persistent.util.Messages.cmhIdEndPts,
						com.persistent.util.Messages.cmhLblEndPts,
						endpoint);
		if (btnId == Window.OK
				&& btnSSLOffloading.getSelection()) {
			try {
				populateEndPointList();
				String[] endPtNames = comboEndpt.getItems();
				for (int i = 0; i < endPtNames.length; i++) {
					if (endPtNames[i].equalsIgnoreCase(curSel)) {
						comboEndpt.setText(curSel);
						break;
					}
				}
			} catch (WindowsAzureInvalidProjectOperationException e) {
				Activator.getDefault().log(Messages.dlgDbgEndPtErrTtl, e);
			}
		}
	}

	private Label createLabel(Composite container, String text, boolean isLower) {
		Label label = new Label(container, SWT.LEFT);
		label.setText(text);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 17;
		if (isLower) {
			gridData.verticalIndent = 5;
		}
		label.setLayoutData(gridData);
		return label;
	}

	private Combo createCombo(Composite container, boolean isLower) {
		Combo combo = new Combo(container, SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.widthHint = 280;
		gridData.horizontalIndent = 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		if (isLower) {
			gridData.verticalIndent = 5;
		}
		combo.setLayoutData(gridData);
		return combo;
	}

	private void createCertComponents(Composite container) {
		lblCert = createLabel(container, Messages.lbSslCert, true);

		comboCert = createCombo(container, true);
		comboCert.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				removeErrorMsg();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		linkCert = new Link(container, SWT.RIGHT);
		linkCert.setText(Messages.linkLblCert);
		linkCert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				certLinkListener();
			}
		});
	}

	private void certLinkListener() {
		// open remote access dialog
		String curSel = comboCert.getText();
		Object cert =
				new WARCertificates();
		int btnId = PluginUtil.
				openPropertyPageDialog(
						com.persistent.util.Messages.cmhIdCert,
						com.persistent.util.Messages.cmhLblCert,
						cert);
		if (btnId == Window.OK
				&& btnSSLOffloading.getSelection()) {
			try {
				String pageSel = WARCertificates.getSelCertName();
				String nameToSet;
				if (pageSel != null && !pageSel.isEmpty()) {
					nameToSet = pageSel;
				} else {
					nameToSet = curSel;
				}
				populateCertList();
				if (nameToSet.
						equalsIgnoreCase(
								com.gigaspaces.azure.deploy.
								Messages.remoteAccessPasswordEncryption)) {
					PluginUtil.displayErrorDialog(this.getShell(),
							Messages.genErrTitle,
							Messages.usedByRemAcc);
				} else {
					String[] names = comboCert.getItems();
					for (int i = 0; i < names.length; i++) {
						if (names[i].equalsIgnoreCase(nameToSet)) {
							comboCert.setText(nameToSet);
							break;
						}
					}
				}
				removeErrorMsg();
			} catch (WindowsAzureInvalidProjectOperationException e) {
				Activator.getDefault().log(Messages.certErrTtl, e);
			}
		}
	}

	private void createNote(Composite container) {
		Link linkNote = new Link(container, SWT.LEFT);
		linkNote.setText(Messages.lbSslNote);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.verticalIndent = 15;
		linkNote.setLayoutData(gridData);
		linkNote.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().
					getExternalBrowser().openURL(new URL(event.text));
				} catch (Exception ex) {
					/*
					 * only logging the error in log file
					 * not showing anything to end user.
					 */
					Activator.getDefault().log(
							Messages.lnkOpenErrMsg, ex);
				}
			}
		});
	}

	private void enableDisableControls(boolean value) {
		lblEndptToUse.setEnabled(value);
		lblCert.setEnabled(value);
		comboEndpt.setEnabled(value);
		comboCert.setEnabled(value);
		if (!value) {
			comboEndpt.removeAll();
			comboCert.removeAll();
		}
	}

	/**
	 * Populates endpoints having type input in combo box.
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private void populateEndPointList()
			throws WindowsAzureInvalidProjectOperationException {
		endpointsList = waRole.getEndpoints();
		comboEndpt.removeAll();
		for (WindowsAzureEndpoint endpoint : endpointsList) {
			if (endpoint.getEndPointType().
					equals(WindowsAzureEndpointType.Input)
					&& endpoint.getPrivatePort() != null
					&& !endpoint.equals(waRole.getDebuggingEndpoint())) {
				comboEndpt.add(String.format(Messages.dbgEndPtStr,
						endpoint.getName(),
						endpoint.getPort(),
						endpoint.getPrivatePort()));
			}
		}
	}

	private void populateCertList()
			throws WindowsAzureInvalidProjectOperationException {
		comboCert.removeAll();
		for (Iterator<Entry<String, WindowsAzureCertificate>> iterator =
				waRole.getCertificates().entrySet().iterator();
				iterator.hasNext();) {
			WindowsAzureCertificate cert = iterator.next().getValue();
			if (!cert.isRemoteAccess()) {
				comboCert.add(cert.getName());
			}
		}
	}

	private boolean handlePageComplete(boolean isOkToLeave) {
		boolean okToProceed = true;
		if (btnSSLOffloading.getSelection()) {
			String endPtStr = comboEndpt.getText().trim();
			String certStr = comboCert.getText().trim();
			if (endPtStr == null
					|| certStr == null
					|| endPtStr.isEmpty()
					|| certStr.isEmpty()) {
				okToProceed = false;
				if (isOkToLeave) {
					setErrorMessage(Messages.eptNmEndPtMsg);
				} else {
					PluginUtil.displayErrorDialog(this.getShell(),
							Messages.genErrTitle,
							Messages.eptNmEndPtMsg);
				}
			} else {
				String endpointName = endPtStr.substring(0, endPtStr.indexOf("(") -1);
				try {
					WindowsAzureEndpoint newEndPt = waRole.getEndpoint(endpointName);
					WindowsAzureCertificate newCert = waRole.getCertificate(certStr);
					// check if SSL offloading is already configured
					WindowsAzureEndpoint oldEndPt = waRole.getSslOffloadingInputEndpoint();
					WindowsAzureCertificate oldCert = waRole.getSslOffloadingCert();
					if (newEndPt != null
							&& newCert != null) {
						if (oldEndPt != null && oldCert != null) {
							if (!oldEndPt.getName().equalsIgnoreCase(newEndPt.getName())) {
								waRole.setSslOffloading(newEndPt, newCert);
							} else if(!oldCert.getName().equalsIgnoreCase(newCert.getName())) {
								waRole.setSslOffloadingCert(newCert);
							}
						} else {
							waRole.setSslOffloading(newEndPt, newCert);
						}
					} else {
						okToProceed = false;
					}
				} catch (WindowsAzureInvalidProjectOperationException e) {
					okToProceed = false;
				}
			}
		}
		return okToProceed;
	}

	@Override
	public boolean okToLeave() {
		boolean okToProceed = handlePageComplete(true);
		boolean retVal = false;
		if (okToProceed) {
			retVal = super.okToLeave();
		}
		return retVal;
	}

	@Override
	public boolean performOk() {
		if (!isPageDisplayed) {
			return super.performOk();
		}
		boolean okToProceed = handlePageComplete(false);
		if (okToProceed) {
			try {
				waProjManager.save();
				Activator.getDefault().setSaved(true);
				WAEclipseHelper.refreshWorkspace(
						Messages.rolsRefTitle, Messages.rolsRefMsg);
				okToProceed = super.performOk();
			} catch (WindowsAzureInvalidProjectOperationException e) {
				PluginUtil.displayErrorDialogAndLog(
						this.getShell(),
						Messages.adRolErrTitle,
						Messages.adRolErrMsgBox1
						+ Messages.adRolErrMsgBox2, e);
				okToProceed = false;
			}
		}
		return okToProceed;
	}

	private void isEditableEndpointCombo(WindowsAzureEndpoint endPt)
			throws WindowsAzureInvalidProjectOperationException {
		if (endPt.equals(waRole.getSessionAffinityInputEndpoint())) {
			comboEndpt.setEnabled(false);
		} else {
			comboEndpt.setEnabled(true);
		}
	}

	private void removeErrorMsg() {
		String endPtStr = comboEndpt.getText().trim();
		String certStr = comboCert.getText().trim();
		if (btnSSLOffloading.getSelection()) {
			if (endPtStr != null
					&& !endPtStr.isEmpty()
					&& certStr != null
					&& !certStr.isEmpty()) {
				setErrorMessage(null);
			}
		} else {
			setErrorMessage(null);
		}
	}
}
