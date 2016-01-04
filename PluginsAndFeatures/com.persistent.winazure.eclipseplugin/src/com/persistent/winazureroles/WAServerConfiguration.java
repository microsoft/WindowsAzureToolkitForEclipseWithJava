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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.interopbridges.tools.windowsazure.WARoleComponentCloudUploadMode;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.microsoftopentechnologies.azurecommons.roleoperations.JdkSrvConfigUtilMethods;
import com.microsoftopentechnologies.azurecommons.roleoperations.WAServerConfUtilMethods;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.ui.projwizard.WAApplicationDialog;
import com.persistent.util.AppCmpntParam;
import com.persistent.util.JdkSrvConfig;
import com.persistent.util.JdkSrvConfigListener;
import com.persistent.util.ProjectNatureHelper;
import com.persistent.util.WAEclipseHelper;

import waeclipseplugin.Activator;

/**
 * Property page for Server Configuration.
 */
public class WAServerConfiguration extends PropertyPage {
	private WindowsAzureProjectManager waProjManager;
	private WindowsAzureRole windowsAzureRole;
    private ArrayList<AppCmpntParam> appList = new ArrayList<AppCmpntParam>();
	private File cmpntFile = new File(WAEclipseHelper.getTemplateFile(
			Messages.cmpntSetFlName));
	private IWorkspace workspace = ResourcesPlugin.getWorkspace();
	private IWorkspaceRoot root = workspace.getRoot();
	private ArrayList<String> fileToDel = new ArrayList<String>();
	private String finalSrvPath;
	private WindowsAzureRoleComponentImportMethod finalImpMethod;
	private String finalAsName;
	private String finalJdkPath;
	private boolean isPageDisplayed = false;
	private TabFolder folder;
	private TabItem jdkTab;
	private TabItem srvTab;
	private TabItem appTab;
	private final String auto = "auto";
	private int prevTabIndex;
	private static boolean accepted = false;
	private static boolean srvAccepted = false;
	private String jdkPrevName;
	private String srvPrevName;
	private final int HTTP_PORT = 80;
	private String tabToSelect = "";

	private void checkSDKPresenceAndEnable() {
		String sdkVersion = WindowsAzureProjectManager.getLatestAzureVersionForSA();
		if (sdkVersion == null || sdkVersion.isEmpty()) {
			if (!JdkSrvConfig.getJdkCheckBtn().getSelection()) {
				JdkSrvConfig.getJdkCheckBtn().setEnabled(false);
			}
			JdkSrvConfig.getTxtJdk().setEnabled(false);
			JdkSrvConfig.getBtnJdkLoc().setEnabled(false);
		}
	}

	public WAServerConfiguration() {
		super();
	}

	public WAServerConfiguration(String tabToSelect) {
		super();
		this.tabToSelect = tabToSelect;
	}

	@Override
	public String getTitle() {
		if (!isPageDisplayed) {
			return super.getTitle();
		}
		// Check JDK is already enabled or not
		// and if enabled show appropriate values on property page
		try {
			String jdkSrcPath = null;
			jdkSrcPath = windowsAzureRole.getJDKSourcePath();

			if (jdkSrcPath == null) {
				JdkSrvConfig.setEnableJDK(false);
				JdkSrvConfig.setEnableDlGrp(false, false);
			} else {
				if (jdkSrcPath.isEmpty()) {
					JdkSrvConfig.setEnableJDK(false);
				} else {
					JdkSrvConfig.setEnableJDK(true);
					JdkSrvConfig.getTxtJdk().setText(jdkSrcPath);
				}
				String jdkName = windowsAzureRole.getJDKCloudName();
				// project may be using deprecated JDK, hence pass to method
				JdkSrvConfigListener.showThirdPartyJdkNames(true, jdkName);

				String jdkUrl = windowsAzureRole.getJDKCloudURL();
				// JDK download group
				if (jdkUrl != null && !jdkUrl.isEmpty()) {
					// JDK auto upload option configured
					if (JdkSrvConfigUtilMethods.
							isJDKAutoUploadPrevSelected(windowsAzureRole)) {
						JdkSrvConfig.setEnableDlGrp(true, true);
						// check for third party JDK
						if (jdkName.isEmpty()) {
							JdkSrvConfig.getAutoDlRdCldBtn().setSelection(true);
						} else {
							JdkSrvConfig.getAutoDlRdCldBtn().setSelection(false);
							JdkSrvConfig.getThrdPrtJdkBtn().setSelection(true);
							JdkSrvConfigListener.enableThirdPartyJdkCombo(true);
							JdkSrvConfig.getThrdPrtJdkCmb().setText(jdkName);
							/*
							 * License has already been accepted
							 * on wizard or property page previously.
							 */
							accepted = true;
							jdkPrevName = jdkName;
						}
					} else {
						// JDK deploy option configured
						JdkSrvConfig.getAutoDlRdCldBtn().setSelection(false);
						JdkSrvConfig.getDlRdCldBtn().setSelection(true);
						JdkSrvConfig.setEnableDlGrp(true, false);
					}

					// Update URL text box
					if (jdkUrl.equalsIgnoreCase(auto)) {
						jdkUrl = JdkSrvConfig.AUTO_TXT;
					}
					JdkSrvConfig.getTxtUrl().setText(jdkUrl);

					// Update JAVA_HOME text box
					if (waProjManager.getPackageType().
							equals(WindowsAzurePackageType.LOCAL)) {
						JdkSrvConfig.getTxtJavaHome().
						setText(windowsAzureRole.getJDKCloudHome());
					} else {
						JdkSrvConfig.getTxtJavaHome().
						setText(windowsAzureRole.
								getRuntimeEnv(Messages.jvHome));
					}

					// Update note below JDK URL text box
					if (jdkSrcPath.isEmpty()) {
						JdkSrvConfig.getLblDlNoteUrl().
						setText(com.persistent.ui.projwizard.Messages.dlgDlNtLblUrl);
					} else {
						String dirName = new File(jdkSrcPath).getName();
						JdkSrvConfig.getLblDlNoteUrl().
						setText(String.format(com.persistent.ui.projwizard.Messages.dlNtLblDir, dirName));
					}

					// Update storage account combo box.
					String jdkKey = windowsAzureRole.getJDKCloudKey();
					JdkSrvConfig.setCmbStrgAccJdk(JdkSrvConfig.
							populateStrgNameAsPerKey(jdkKey,
									JdkSrvConfig.getCmbStrgAccJdk()));
				}
			}
			checkSDKPresenceAndEnable();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.jdkPathErrTtl,
					Messages.getJdkErrMsg, e);
		}
		// Check Server is already enabled or not
		// and if enabled show appropriate values on property page
		try {
			String srvName = windowsAzureRole.getServerName();
			if (srvName == null) {
				JdkSrvConfig.setEnableServer(false);
				JdkSrvConfig.setEnableDlGrpSrv(false, false);
				JdkSrvConfig.enableApplicationTab(false);
			} else {
				String srvSrcPath = windowsAzureRole.getServerSourcePath();
				String thirdServerName = windowsAzureRole.getServerCloudName();

				JdkSrvConfig.getSerCheckBtn().setSelection(true);
				JdkSrvConfig.setEnableServer(true);
				JdkSrvConfig.getComboServer().setText(srvName);
				JdkSrvConfig.getTxtDir().setText(srvSrcPath);
				JdkSrvConfig.enableApplicationTab(true);
				JdkSrvConfigListener.showThirdPartySrvNames(true, srvName, thirdServerName);
				
				// Server download group
				String srvUrl = windowsAzureRole.getServerCloudURL();
				if (srvUrl != null && !srvUrl.isEmpty()) {
					// server auto upload option configured
					if (JdkSrvConfigUtilMethods.
							isServerAutoUploadPrevSelected(windowsAzureRole)
							|| !thirdServerName.isEmpty()) {
						if (thirdServerName.isEmpty()) {
							JdkSrvConfig.getAutoDlRdCldBtnSrv().setSelection(true);
						} else {
							JdkSrvConfig.getThrdPrtSrvBtn().setSelection(true);
							JdkSrvConfigListener.enableThirdPartySrvCombo(true);
							srvAccepted = true;
							srvPrevName = thirdServerName;
						}
						JdkSrvConfig.setEnableDlGrpSrv(true, true);
						if (!thirdServerName.isEmpty()) {
							JdkSrvConfig.getThrdPrtSrvCmb().setText(thirdServerName);
						}
					} else {
						// server deploy option configured
						JdkSrvConfig.getDlRdCldBtnSrv().setSelection(true);
						JdkSrvConfig.setEnableDlGrpSrv(true, false);
					}
					if (srvUrl.equalsIgnoreCase(auto)) {
						srvUrl = JdkSrvConfig.AUTO_TXT;
					}
					JdkSrvConfig.getTxtUrlSrv().setText(srvUrl);
					// Update server home text box
					if (waProjManager.getPackageType().
							equals(WindowsAzurePackageType.LOCAL)) {
						JdkSrvConfig.getTxtHomeDir().
						setText(windowsAzureRole.getServerCloudHome());
					} else {
						JdkSrvConfig.getTxtHomeDir().
						setText(windowsAzureRole.
								getRuntimeEnv(windowsAzureRole.
										getRuntimeEnvName(
												Messages.typeSrvHm)));
					}
					// Update note below Server URL text box
					if (srvSrcPath.isEmpty()) {
						JdkSrvConfig.getLblDlNoteUrlSrv().
						setText(com.persistent.ui.projwizard.Messages.dlgDlNtLblUrl);
					} else {
						String dirName = new File(srvSrcPath).getName();
						JdkSrvConfig.getLblDlNoteUrlSrv().
						setText(String.format(
								com.persistent.ui.projwizard.Messages.dlNtLblDir, dirName));
					}

					String srvKey = windowsAzureRole.getServerCloudKey();
					JdkSrvConfig.setCmbStrgAccSrv(JdkSrvConfig.
							populateStrgNameAsPerKey(srvKey,
									JdkSrvConfig.getCmbStrgAccSrv()));
					if (!thirdServerName.isEmpty()) {
						String cldSrc = JdkSrvConfig.getThirdPartyServerCloudSrc();
						// check if its latest server scenario then set storage account to (none)
						if (!cldSrc.isEmpty() && Activator.IS_WINDOWS) {
							/*
							 * org.eclipse.swt.widgets.Combo's setItem method
							 * behave weirdly on Linux eclipse.
							 * Hence use only on windows.
							 */
							JdkSrvConfig.getCmbStrgAccSrv().setItem(0, "(none)");
						}
					}
				}
			}
			JdkSrvConfig.checkSDKPresenceAndEnableServer();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.srvErrTtl,
					Messages.getSrvBothErrMsg, e);
		}

		if (JdkSrvConfig.getJdkCheckBtn().getSelection()
				|| JdkSrvConfig.getThrdPrtJdkBtn().getSelection()
				|| JdkSrvConfig.getDlRdCldBtn().getSelection()) {
			JdkSrvConfig.getSerCheckBtn().setEnabled(true);
		}
		
		if (!JdkSrvConfig.getTxtDir().getText().isEmpty()) {
			JdkSrvConfigListener.enforceSameLocalCloudServer();
		}

		if (JdkSrvConfig.getTableViewer() != null) {
			JdkSrvConfig.getTableViewer().refresh();
		}
		
		if (tabToSelect.equalsIgnoreCase(Messages.dplDlgSerTxt)) {
			folder.setSelection(srvTab);
			prevTabIndex = 1;
		} else {
			folder.setSelection(jdkTab);
			prevTabIndex = 0;
		}
		handlePageComplete();
		return super.getTitle();
	}

	/**
	 * Create Server Configuration page and buttons associated with it.
	 *
	 * @param parent : parent composite.
	 * @return control
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				"com.persistent.winazure.eclipseplugin."
						+ "windows_azure_serverconfiguration_page");
		waProjManager = Activator.getDefault().getWaProjMgr();
		windowsAzureRole = Activator.getDefault().getWaRole();
		Activator.getDefault().setSaved(false);

		// Tab controls
		folder = new TabFolder(parent, SWT.NONE);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		folder.setLayoutData(gridData);

		// Tab for JDK
		jdkTab = new TabItem(folder, SWT.NONE);
		jdkTab.setText(Messages.dplDlgJDKGrp);
		jdkTab.setControl(createJDK(folder));

		// Tab for Server
		srvTab = new TabItem(folder, SWT.NONE);
		srvTab.setText(Messages.dplDlgSerTxt);
		srvTab.setControl(createServer(folder));

		// Tab for Application
		appTab = new TabItem(folder, SWT.NONE);
		appTab.setText(Messages.lblApp);
		appTab.setControl(createAppTblCmpnt(folder));

		folder.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (folder.getSelectionIndex() == 0) {
					changeToJdkTab();
				} else if (folder.getSelectionIndex() == 1) {
					changeToSrvTab();
				} else if (folder.getSelectionIndex() == 2) {
					changeToAppTab();
				}
			}
		});

		isPageDisplayed = true;
		return folder;
	}

	private void changeToSrvTab() {
		if (displayLicenseAgreement()) {
			prevTabIndex = 1;
		} else {
			folder.setSelection(jdkTab);
			prevTabIndex = 0;
		}
	}

	private void changeToAppTab() {
		if (prevTabIndex == 0) {
			if (displayLicenseAgreement()) {
				prevTabIndex = 2;
			} else {
				folder.setSelection(jdkTab);
				prevTabIndex = 0;
			}
		} else if (prevTabIndex == 1) {
			// Server to App tab navigation
			if (displayServerLicenseAgreement()) {
				prevTabIndex = 2;
			} else {
				folder.setSelection(srvTab);
				prevTabIndex = 1;
			}
		}
	}
	
	private void changeToJdkTab() {
		if (displayServerLicenseAgreement()) {
			folder.setSelection(jdkTab);
			prevTabIndex = 0;
		} else {
			folder.setSelection(srvTab);
			prevTabIndex = 1;
		}
	}

	/**
	 * Stores properties to remove JDK from approot
	 * if JDK path is updated.
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private void handleJdkDirRemoval()
			throws WindowsAzureInvalidProjectOperationException {
		// deleting JDK entry from approot
		String oldJdkPath = windowsAzureRole.getJDKSourcePath();
		if (oldJdkPath != null
				&& !oldJdkPath.isEmpty()
				&& !fileToDel.contains("jdk")) {
			fileToDel.add("jdk");
			WindowsAzureRoleComponent cmp =
					getPrevCmpnt(Messages.typeJdkDply);
			if (cmp != null) {
				finalJdkPath = cmp.getImportPath();
			}
		}
	}
	
	private void handleServerDirRemoval()
			throws WindowsAzureInvalidProjectOperationException {
		String oldName = windowsAzureRole.getServerName();
		String oldPath = windowsAzureRole.getServerSourcePath();
		// Remove old server from approot
		if (oldName != null
				&& oldPath != null
				&& !oldPath.isEmpty()
				&&  !fileToDel.contains("srv")) {
			fileToDel.add("srv");
			WindowsAzureRoleComponent cmp =
					getPrevCmpnt(Messages.typeSrvDply);
			if (cmp != null) {
				finalSrvPath = cmp.getImportPath();
				finalImpMethod = cmp.getImportMethod();
				finalAsName = cmp.getDeployName();
			}
		}
	}

	/**
	 * Creates the JDK component.
	 *
	 * @param parent Parent container
	 * @return Control
	 */
	private Control createJDK(Composite parent) {
		Control control = JdkSrvConfig.createJDKGrp(parent);

		// listener for JDK location text box.
		JdkSrvConfig.getTxtJdk().
		addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				JdkSrvConfig.updateJDKDlNote();
				handlePageComplete();
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});

		// Modify listener for JDK location text box.
		JdkSrvConfig.getTxtJdk().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				JdkSrvConfigListener.modifyJdkText();
				handlePageComplete();
			}
		});

		// listener for JDK check button.
		JdkSrvConfig.getJdkCheckBtn().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getJdkCheckBtn().getSelection()) {
					// populate third party JDKs whose status in not deprecated
					JdkSrvConfigListener.jdkChkBoxChecked("");
				} else {
					JdkSrvConfigListener.jdkChkBoxUnChecked();
				}
				handlePageComplete();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// listener for JDK browse button.
		JdkSrvConfig.getBtnJdkLoc().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				jdkBrowseBtnListener();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// JDK download group
		// listener for JDK deploy radio button.
		JdkSrvConfig.getDlRdCldBtn().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getDlRdCldBtn().getSelection()) {
					if (!(JdkSrvConfig.getTxtUrl().isEnabled()
							&& WAEclipseHelperMethods.
							isBlobStorageUrl(JdkSrvConfig.getTxtUrl().getText()))) {
						JdkSrvConfig.getTxtUrl().setText(
								JdkSrvConfig.getUrl(
										JdkSrvConfig.getCmbStrgAccJdk()));
						JdkSrvConfigListener.jdkDeployBtnSelected();
					}
				}
				handlePageComplete();
				accepted = false;
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// listener for JDK auto upload radio button.
		JdkSrvConfig.getAutoDlRdCldBtn().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getAutoDlRdCldBtn().getSelection()) {
					// auto upload radio button selected
					JdkSrvConfigListener.
					configureAutoUploadJDKSettings();
				}
				handlePageComplete();
				accepted = false;
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// listener for third party JDK radio button.
		JdkSrvConfig.getThrdPrtJdkBtn().
		addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				/*
				 * Check if third party radio button
				 * is already selected
				 * and user is selecting same radio button again
				 * then do not do any thing.
				 */
				if (!JdkSrvConfig.getThrdPrtJdkCmb().isEnabled()) {
					JdkSrvConfigListener.thirdPartyJdkBtnSelected();
					jdkPrevName = JdkSrvConfig.
							getThrdPrtJdkCmb().getText();
				}
				JdkSrvConfig.getSerCheckBtn().setEnabled(true);
			}
		});

		// listener for JDK URL text.
		JdkSrvConfig.getTxtUrl().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				if (JdkSrvConfig.getAutoDlRdCldBtn().getSelection()
						|| JdkSrvConfig.getThrdPrtJdkBtn().getSelection()) {
					handlePageComplete();
					/*
					 * no need to do any checks if
					 * auto upload or third party JDK is selected
					 */
					return;
				}
				JdkSrvConfigListener.modifyJdkUrlText();
				handlePageComplete();
			}
		});

		// listener for JAVA_HOME text box.
		JdkSrvConfig.getTxtJavaHome().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				handlePageComplete();
			}
		});

		// listener for Accounts link on JDK tab.
		JdkSrvConfig.getAccLinkJdk().
		addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				JdkSrvConfigListener.jdkAccLinkClicked();
				handlePageComplete();
			}
		});

		// listener for storage account combo box on JDK tab.
		JdkSrvConfig.getCmbStrgAccJdk().
		addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JdkSrvConfig.updateJDKDlURL();
				handlePageComplete();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// listener for JDK customize link.
		JdkSrvConfig.getThrdPrtJdkLink().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JdkSrvConfig.custLinkListener(
						Messages.dplSerBtnTtl,
						Messages.dplSerBtnMsg,
						false, getShell(), null, cmpntFile);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// listener for third party JDK combo box.
		JdkSrvConfig.getThrdPrtJdkCmb().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JdkSrvConfigListener.thirdPartyComboListener();
				/*
				 * If JDK name is changed by user then license
				 * has to be accepted again.
				 */
				String currentName = JdkSrvConfig.
						getThrdPrtJdkCmb().getText();
				if (!currentName.equalsIgnoreCase(jdkPrevName)) {
					accepted = false;
					jdkPrevName = currentName;
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		return control;
	}

	/**
	 * Creates the server components.
	 *
	 * @param parent Parent container
	 * @return Control
	 */
	private Control createServer(Composite parent) {
		Control control = JdkSrvConfig.createServerGrp(parent);
		// listener for Server check button.
		JdkSrvConfig.getSerCheckBtn()
		.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getSerCheckBtn().getSelection()) {
					JdkSrvConfigListener.srvChkBoxChecked();
				} else {
					JdkSrvConfigListener.srvChkBoxUnChecked();
				}
				handlePageComplete();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// listener for Server location text box.
		JdkSrvConfig.getTxtDir().
		addFocusListener(new FocusListener() {
			private String oldTxt = "";
			@Override
			public void focusLost(FocusEvent arg0) {
				String path = null;
				File file = null;
				if (!JdkSrvConfig.getComboServer().getText().isEmpty()
						&& !JdkSrvConfig.getTxtDir().
						getText().equalsIgnoreCase(oldTxt)) {
					path = JdkSrvConfig.getTxtDir().getText().trim();
					file = new File(path);
					if (file.exists()
							&& file.isDirectory()) {
						// Server auto-detection
						String serverName = WAEclipseHelper.detectServer(file);
						if (serverName != null) {
							JdkSrvConfig.getComboServer().setText(serverName);
						} else {
							String srvComboTxt = JdkSrvConfig.
									getComboServer().getText();
							if (srvComboTxt != null
									&& !srvComboTxt.isEmpty()) {
								serverName = srvComboTxt;
							}
						}
					}
				}
				JdkSrvConfigListener.updateSrvDlNote();
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				oldTxt = JdkSrvConfig.getTxtDir().getText();
			}
		});

		// Modify listener for Server location text box.
		JdkSrvConfig.getTxtDir().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				JdkSrvConfigListener.modifySrvText();
				handlePageComplete();
			}
		});

		// listener for Server browse button.
		JdkSrvConfig.getBtnSrvLoc().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				serBrowseBtnListener();
				JdkSrvConfigListener.modifySrvText();
				JdkSrvConfigListener.enforceSameLocalCloudServer();
				if (JdkSrvConfig.getThrdPrtSrvBtn().getSelection()) {
					String currentName = JdkSrvConfig.getThrdPrtSrvCmb().getText();
					if (!currentName.equalsIgnoreCase(srvPrevName)) {
						srvAccepted = false;
						srvPrevName = currentName;
					}
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		try {
			String [] servList = WindowsAzureProjectManager.
					getServerTemplateNames(cmpntFile);
			Arrays.sort(servList);
			JdkSrvConfig.getComboServer().setItems(servList);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.srvErrTtl,
					Messages.getSrvNmErrMsg, e);
		}

		// listener for Server type combo box.
		JdkSrvConfig.getComboServer().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JdkSrvConfigListener.enforceSameLocalCloudServer();
				if (JdkSrvConfig.isSrvAutoUploadChecked()) {
					JdkSrvConfig.updateServerHome(JdkSrvConfig.getTxtDir().getText());
				} else if (JdkSrvConfig.getThrdPrtSrvBtn().getSelection()) {
					JdkSrvConfig.updateServerHomeForThirdParty();
					String currentName = JdkSrvConfig.getThrdPrtSrvCmb().getText();
					if (!currentName.equalsIgnoreCase(srvPrevName)) {
						srvAccepted = false;
						srvPrevName = currentName;
					}
				} else if (JdkSrvConfig.isSrvDownloadChecked()) {
					if (JdkSrvConfig.getTxtUrlSrv().getText().isEmpty()) {
						JdkSrvConfig.updateServerHome(JdkSrvConfig.getTxtDir().getText());
					} else {
						JdkSrvConfigListener.modifySrvUrlText();
					}
				}
				handlePageComplete();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		// Modify listener for Server type combo box.
		JdkSrvConfig.getComboServer().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				handlePageComplete();
			}
		});

		// listener for Server customize link.
		JdkSrvConfig.getCustLink().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JdkSrvConfig.custLinkListener(
						Messages.dplSerBtnTtl,
						Messages.dplSerBtnMsg,
						false, getShell(), null, cmpntFile);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		JdkSrvConfig.getThrdPrtSrvLink().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JdkSrvConfig.custLinkListener(
						Messages.dplSerBtnTtl,
						Messages.dplSerBtnMsg,
						false, getShell(), null, cmpntFile);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// Server download group
		// listener for Server deploy radio button.
		JdkSrvConfig.getDlRdCldBtnSrv().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getDlRdCldBtnSrv().getSelection()) {
					if (!(JdkSrvConfig.getTxtUrlSrv().isEnabled()
							&& WAEclipseHelperMethods.
							isBlobStorageUrl(JdkSrvConfig.getTxtUrlSrv().getText()))) {
						JdkSrvConfig.getTxtUrlSrv().setText(
								JdkSrvConfig.getUrl(
										JdkSrvConfig.getCmbStrgAccSrv()));
						JdkSrvConfigListener.srvDeployBtnSelected();
					}
				}
				handlePageComplete();
				srvAccepted = false;
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// listener for Server auto radio button.
		JdkSrvConfig.getAutoDlRdCldBtnSrv().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getAutoDlRdCldBtnSrv()
						.getSelection()) {
					// server auto upload radio button selected
					JdkSrvConfigListener.configureAutoUploadServerSettings();
				}
				handlePageComplete();
				srvAccepted = false;
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		// listener for third party server radio button.
		JdkSrvConfig.getThrdPrtSrvBtn().
		addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				/*
				 * Check if third party radio button
				 * is already selected
				 * and user is selecting same radio button again
				 * then do not do any thing.
				 */
				if (!JdkSrvConfig.getThrdPrtSrvCmb().isEnabled()) {
					JdkSrvConfigListener.thirdPartySrvBtnSelected();
					srvPrevName = JdkSrvConfig.getThrdPrtSrvCmb().getText();
				}
			}
		});


		// listener for Server URL text box.
		JdkSrvConfig.getTxtUrlSrv().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				if (JdkSrvConfig.getAutoDlRdCldBtnSrv().getSelection()) {
					handlePageComplete();
					return;
				}
				JdkSrvConfigListener.modifySrvUrlText();
				handlePageComplete();
			}
		});

		// listener for server home directory text box.
		JdkSrvConfig.getTxtHomeDir().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				handlePageComplete();
			}
		});

		// listener for Accounts link on server tab.
		JdkSrvConfig.getAccLinkSrv().
		addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				JdkSrvConfigListener.srvAccLinkClicked();
				handlePageComplete();
			}
		});

		// listener for storage account combo box on server tab.
		JdkSrvConfig.getCmbStrgAccSrv().
		addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JdkSrvConfig.updateServerDlURL();
				if (JdkSrvConfig.isSrvAutoUploadChecked()) {
					JdkSrvConfig.updateServerHome(JdkSrvConfig.getTxtDir().getText());
				} else if (JdkSrvConfig.getThrdPrtSrvBtn().getSelection()) {
					JdkSrvConfig.updateServerHomeForThirdParty();
				} else if (JdkSrvConfig.isSrvDownloadChecked()) {
					String url = JdkSrvConfig.getTxtUrlSrv().getText().trim();
					if (WAEclipseHelperMethods.isBlobStorageUrl(url) && url.endsWith(".zip")) {
						url = url.substring(0, url.indexOf(".zip"));
						JdkSrvConfig.updateServerHome(url);
					} else {
						JdkSrvConfig.updateServerHome("");
					}
				}
				handlePageComplete();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// listener for third party JDK combo box.
		JdkSrvConfig.getThrdPrtSrvCmb().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JdkSrvConfigListener.thirdPartySrvComboListener();
				/*
				 * If server name is changed by user then license
				 * has to be accepted again.
				 */
				String currentName = JdkSrvConfig.getThrdPrtSrvCmb().getText();
				if (!currentName.equalsIgnoreCase(srvPrevName)) {
					srvAccepted = false;
					srvPrevName = currentName;
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		return control;
	}

	private void handleEndpointSettings(String srvName) {
		try {
			String srvPriPort = WindowsAzureProjectManager.
					getHttpPort(srvName, cmpntFile);
			// Check server's private port already used on role
			int count = 0;
			WindowsAzureEndpoint endptWithPort = null;
			for (WindowsAzureEndpoint endpoint : windowsAzureRole.getEndpoints()) {
				String priPort = endpoint.getPrivatePort();
				if (priPort != null
						&& priPort.equalsIgnoreCase(srvPriPort)) {
					count++;
					endptWithPort = endpoint;
				}
			}

			if (count == 0) {
				// server's private port is not used
				WindowsAzureEndpoint sslEndpt =
						windowsAzureRole.getSslOffloadingInternalEndpoint();
				WindowsAzureEndpoint stickyEndpt = windowsAzureRole.
						getSessionAffinityInternalEndpoint();
				if (sslEndpt != null) {
					sslEndpt.setPrivatePort(srvPriPort);
				} else if (stickyEndpt != null) {
					stickyEndpt.setPrivatePort(srvPriPort);
				} else {
					checkForHttpElseAddEndpt(srvPriPort);
				}
			} else if (count == 1
					&& endptWithPort.getEndPointType().
					equals(WindowsAzureEndpointType.InstanceInput)) {
				// one endpoint is using server's private port
				checkForHttpElseAddEndpt(srvPriPort);
			}
			/*
			 * If two endpoints of type Input and InstanceInput
			 * are using server's private port then don't do anything
			 */
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.srvErrTtl,
					Messages.errSrvPort, e);
		}
	}

	private void checkForHttpElseAddEndpt(String srvPriPort) {
		try {
			WindowsAzureEndpoint httpEndpt = WAEclipseHelperMethods.
					findEndpointWithPubPortWithAuto(HTTP_PORT, windowsAzureRole);
			if (httpEndpt != null) {
				httpEndpt.setPrivatePort(srvPriPort);
			} else {
				WindowsAzureRole httpRole = WAEclipseHelperMethods.
						findRoleWithEndpntPubPort(HTTP_PORT, waProjManager);
				if (httpRole != null) {
					MessageDialog.openWarning(this.getShell(),
							com.persistent.util.Messages.cmhLblSrvCnfg,
							String.format(Messages.srvPortWarn,
									httpRole.getName()));
				} else {
					// create an endpoint
					windowsAzureRole = WAServerConfUtilMethods.
							addEndpt(srvPriPort, windowsAzureRole);
				}
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.srvErrTtl,
					Messages.errSrvPort, e);
		}
	}

	/**
	 * Creates the application table component.
	 * @param parent : container
	 * @return Control
	 */
	private Control createAppTblCmpnt(Composite parent) {
		Control control = JdkSrvConfig.createAppTbl(parent);
		// set different height and width for server configuration page.
		GridData gridData = new GridData();
		gridData.heightHint = 380;
		gridData.horizontalIndent = 2;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		JdkSrvConfig.getTblApp().setLayoutData(gridData);
		JdkSrvConfig.getColName().setWidth(400);

		JdkSrvConfig.getTableViewer().
		setContentProvider(
				new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object arg0) {
				List<WindowsAzureRoleComponent> srvApp1 = null;
				// Get previously added sever applications
				try {
					srvApp1 = windowsAzureRole.getServerApplications();
				} catch (WindowsAzureInvalidProjectOperationException e) {
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.srvErrTtl,
							Messages.getSrvAppErrMsg, e);
				}
				/* Display existing server
				 * applications in Applications table
				 */
				ArrayList<String> appNames = new ArrayList<String>();
				for (int i = 0; i < srvApp1.size(); i++) {
					WindowsAzureRoleComponent cmpnt = srvApp1.get(i);
					appNames.add(cmpnt.getDeployName());
				}
				return (appNames.toArray());
			}
		});

		JdkSrvConfig.getTableViewer().
		setLabelProvider(new ITableLabelProvider() {

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
				if (colIndex == 0) {
					result = element.toString();
				}
				return result;
			}

			@Override
			public Image getColumnImage(Object arg0, int arg1) {
				return null;
			}
		});

		try {
			JdkSrvConfig.getTableViewer().
			setInput(windowsAzureRole.getServerApplications());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.srvErrTtl,
					Messages.getSrvAppErrMsg, e);
		}

		// Add selection listener for Add Button
		JdkSrvConfig.getBtnAdd().
		addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		/* Enable remove button only when
		 * entry from table is selected.
		 */
		JdkSrvConfig.getTblApp().
		addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				JdkSrvConfig.getBtnRemove().setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {

			}
		});

		// Add selection listener for Remove Button
		JdkSrvConfig.getBtnRemove().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				removeButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
		return control;
	}

	@Override
	public boolean okToLeave() {
		boolean okToProceed = false;
		okToProceed = handlePageComplete();
		if (okToProceed) {
			/*
			 * Check if third party JDK is selected
			 * then license is accepted or not.
			 */
			boolean tempAccepted = true;
			if (JdkSrvConfig.getThrdPrtJdkBtn().getSelection()
					&& !accepted) {
				tempAccepted = JdkSrvConfig.createAccLicenseAggDlg(getShell(), true);
				accepted = tempAccepted;
			}
			if (tempAccepted) {
				okToProceed = configureJdkCloudDeployment();
			} else {
				okToProceed = false;
			}

			if (okToProceed) {
				if (JdkSrvConfig.getThrdPrtSrvBtn().getSelection()
						&& !srvAccepted) {
					tempAccepted = JdkSrvConfig.createAccLicenseAggDlg(getShell(), false);
					srvAccepted = tempAccepted;
				}
				if (tempAccepted) {
					okToProceed = configureSrvCloudDeployment();
				} else {
					okToProceed = false;
				}
			}
		}
		boolean retVal = false;
		if (okToProceed) {
			retVal = super.okToLeave();
		}
		return retVal;
	}

	/**
	 * Method will check every entry
	 * server configuration page has valid value
	 * or not. If invalid then will show proper error message.
	 * @return
	 */
	private boolean handlePageComplete() {
		boolean okToProceed = false;
		boolean isJdkValid = true;
		boolean isSrvValid = true;
		// Validation for JDK
		if (JdkSrvConfig.getJdkCheckBtn().getSelection()) {
			if (JdkSrvConfig.getTxtJdk().getText().isEmpty()) {
				isJdkValid = false;
				setErrorMessage(Messages.jdkPathErrMsg);
			} else {
				File file = new File(JdkSrvConfig.
						getTxtJdk().getText());
				if (!file.exists()
						|| !file.isDirectory()) {
					isJdkValid = false;
					setErrorMessage(Messages.jdkPathErrMsg);
				}
			}
		}
		// JDK download group
		// cloud radio button selected
		if (isJdkValid) {
		if (JdkSrvConfig.getDlRdCldBtn().getSelection()) {
			// Validate JDK URL
			String jdkUrl = JdkSrvConfig.getTxtUrl().getText().trim();
			if (jdkUrl.isEmpty()) {
				isJdkValid = false;
				setErrorMessage(Messages.dlgDlUrlErrMsg);
			} else {
				try {
					new URL(jdkUrl);
					if (WAEclipseHelperMethods.isBlobStorageUrl(jdkUrl)) {
						String javaHome = JdkSrvConfig.getTxtJavaHome().
								getText().trim();
						if (javaHome.isEmpty()) {
							isJdkValid = false;
							setErrorMessage(Messages.jvHomeErMsg);
						} else {
							/*
							 * access key is optional,
							 * so can be empty.
							 */
							isJdkValid = true;
							setErrorMessage(null);
						}
					} else {
						isJdkValid = false;
						setErrorMessage(Messages.dlgDlUrlErrMsg);
					}
				} catch (MalformedURLException e) {
					isJdkValid = false;
					setErrorMessage(Messages.dlgDlUrlErrMsg);
				}
			}
		}
		// No Validation needed if auto upload or third party
		// JDK is selected
		// local radio button selected
		else {
			isJdkValid = true;
			setErrorMessage(null);
		}
		}

		// Validation for Server
		if (isJdkValid && JdkSrvConfig.
				getSerCheckBtn().getSelection()) {
			if (JdkSrvConfig.getComboServer().getText().isEmpty()) {
				setErrorMessage(Messages.dplEmtSerMsg);
				isSrvValid = false;
			} else if (JdkSrvConfig.getAutoDlRdCldBtnSrv().getSelection()
					&& JdkSrvConfig.getTxtDir().getText().isEmpty()) {
				setErrorMessage(Messages.dplWrngSerMsg);
				isSrvValid = false;
			} else if (!JdkSrvConfig.getTxtDir().getText().isEmpty()
					&& !(new File(JdkSrvConfig.getTxtDir().getText()).exists())) {
				setErrorMessage(Messages.dplWrngSerMsg);
				isSrvValid = false;
			} else {
				// Server download group
				if (JdkSrvConfig.getDlRdCldBtnSrv().getSelection()) {
					String srvUrl = JdkSrvConfig.
							getTxtUrlSrv().getText().trim();
					if (srvUrl.isEmpty()) {
						isSrvValid = false;
						setErrorMessage(Messages.dlgDlUrlErrMsg);
					} else {
						try {
							// Validate Server URL
							new URL(srvUrl);
							if (WAEclipseHelperMethods.isBlobStorageUrl(srvUrl)) {
								String srvHome = JdkSrvConfig.getTxtHomeDir().
										getText().trim();
								if (srvHome.isEmpty()) {
									isSrvValid = false;
									setErrorMessage(Messages.srvHomeErMsg);
								} else {
									/*
									 * access key is optional,
									 * so can be empty.
									 */
									isSrvValid = true;
									setErrorMessage(null);
								}
							} else {
								isSrvValid = false;
								setErrorMessage(Messages.dlgDlUrlErrMsg);
							}
						} catch (MalformedURLException e) {
							isSrvValid = false;
							setErrorMessage(Messages.dlgDlUrlErrMsg);
						}
					}
				}
				// No validations if auto upload Server is selected
				// local radio button selected
				else {
					isSrvValid = true;
					setErrorMessage(null);
				}
			}
		}
		if (isJdkValid && isSrvValid) {
			setErrorMessage(null);
			okToProceed = true;
		}
		return okToProceed;
	}

	@Override
	public boolean performOk() {
		if (!isPageDisplayed) {
			return super.performOk();
		}
		boolean okToProceed = false;
		boolean isJdkValid = true;
		boolean isSrvValid = true;
		// Validation for JDK
		if (JdkSrvConfig.getJdkCheckBtn().getSelection()) {
			if (JdkSrvConfig.getTxtJdk().getText().isEmpty()) {
				isJdkValid = false;
				PluginUtil.displayErrorDialogAndLog(getShell(),
						Messages.jdkPathErrTtl,
						Messages.jdkPathErrMsg, null);
			} else {
				File file = new File(JdkSrvConfig.getTxtJdk().getText());
				if (!file.exists() || !file.isDirectory()) {
					isJdkValid = false;
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.jdkPathErrTtl,
							Messages.jdkPathErrMsg, null);
				}
			}
		}
		// JDK download group
		// If scenario is "No deployment" then no validation
		boolean isJdkNoDply = JdkSrvConfig.getAutoDlRdCldBtn().getSelection()
				&& !JdkSrvConfig.getJdkCheckBtn().getSelection();
		if (isJdkValid) {
		if (!isJdkNoDply) {
			String jdkUrl = JdkSrvConfig.getTxtUrl().getText().trim();
			if (jdkUrl.isEmpty()) {
				isJdkValid = false;
				PluginUtil.displayErrorDialog(getShell(),
						Messages.dlgDlUrlErrTtl,
						Messages.dlgDlUrlErrMsg);
			} else {
				Boolean isUrlValid = false;
				// JDK auto upload or third party option selected.
				if (JdkSrvConfig.getAutoDlRdCldBtn().getSelection()
						|| JdkSrvConfig.getThrdPrtJdkBtn().getSelection()) {
					if (jdkUrl.equalsIgnoreCase(JdkSrvConfig.AUTO_TXT)) {
						jdkUrl = auto;
					}
					isUrlValid = true;
				} else {
					// JDK cloud option selected
					try {
						new URL(jdkUrl);
						if (WAEclipseHelperMethods.isBlobStorageUrl(jdkUrl)) {
							isUrlValid = true;
						} else {
							PluginUtil.displayErrorDialog(getShell(),
									Messages.dlgDlUrlErrTtl,
									Messages.dlgDlUrlErrMsg);
						}
					} catch (MalformedURLException e) {
						PluginUtil.displayErrorDialog(getShell(),
								Messages.dlgDlUrlErrTtl,
								Messages.dlgDlUrlErrMsg);
					}
				}
				if (isUrlValid) {
					String javaHome = JdkSrvConfig.getTxtJavaHome().getText().trim();
					if (javaHome.isEmpty()) {
						isJdkValid = false;
						PluginUtil.displayErrorDialog(getShell(),
								Messages.genErrTitle,
								Messages.jvHomeErMsg);
					} else {
						boolean tempAccepted = true;
						if (JdkSrvConfig.getThrdPrtJdkBtn().getSelection()
								&& !accepted) {
							tempAccepted = JdkSrvConfig.createAccLicenseAggDlg(getShell(), true);
							accepted = tempAccepted;
						}
						if (tempAccepted) {
							isJdkValid = configureJdkCloudDeployment();
						} else {
							isJdkValid = false;
						}
					}
				} else {
					isJdkValid = false;
				}
			}
		} else {
			isJdkValid = configureJdkCloudDeployment();
		}
		}

		// Validation for Server
		if (isJdkValid) {
			if (JdkSrvConfig.getSerCheckBtn().getSelection()) {
				if (JdkSrvConfig.getComboServer().getText().isEmpty()) {
					isSrvValid = false;
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.srvErrTtl,
							Messages.dplEmtSerMsg, null);
				} else if (JdkSrvConfig.getAutoDlRdCldBtnSrv().getSelection()
						&& JdkSrvConfig.getTxtDir().getText().isEmpty()) {
					isSrvValid = false;
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.srvErrTtl,
							Messages.dplWrngSerMsg, null);
				} else if (!JdkSrvConfig.getTxtDir().getText().isEmpty()
						&& (!new File(JdkSrvConfig.getTxtDir().getText()).exists()
								|| !new File(JdkSrvConfig.getTxtDir().getText()).isAbsolute())) {
					isSrvValid = false;
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.srvErrTtl,
							Messages.dplWrngSerMsg, null);
				} else {
					// Validate Server URL
					String srvUrl = JdkSrvConfig.getTxtUrlSrv().getText().trim();
					if (srvUrl.isEmpty()) {
						isSrvValid = false;
						PluginUtil.displayErrorDialog(getShell(),
								Messages.dlgDlUrlErrTtl,
								Messages.dlgDlUrlErrMsg);
					} else {
						Boolean isSrvUrlValid = false;
						// Server auto upload option selected.
						if (JdkSrvConfig.getAutoDlRdCldBtnSrv().getSelection()
								|| JdkSrvConfig.getThrdPrtSrvBtn().getSelection()) {
							if (srvUrl.equalsIgnoreCase(JdkSrvConfig.AUTO_TXT)) {
								srvUrl = auto;
							}
							isSrvUrlValid = true;
						} else {
							// Server cloud option selected
							try {
								new URL(srvUrl);
								if (WAEclipseHelperMethods.isBlobStorageUrl(srvUrl)) {
									isSrvUrlValid = true;
								} else {
									PluginUtil.displayErrorDialog(getShell(),
											Messages.dlgDlUrlErrTtl,
											Messages.dlgDlUrlErrMsg);
								}
							} catch (MalformedURLException e) {
								PluginUtil.displayErrorDialog(getShell(),
										Messages.dlgDlUrlErrTtl,
										Messages.dlgDlUrlErrMsg);
							}
						}
						if (isSrvUrlValid) {
							String srvHome = JdkSrvConfig.getTxtHomeDir().getText().trim();
							if (srvHome.isEmpty()) {
								isSrvValid = false;
								PluginUtil.displayErrorDialog(getShell(),
										Messages.genErrTitle,
										Messages.srvHomeErMsg);
							} else {
								boolean tempAccepted = true;
								if (JdkSrvConfig.getThrdPrtSrvBtn().getSelection()
										&& !srvAccepted) {
									tempAccepted = JdkSrvConfig.createAccLicenseAggDlg(getShell(), false);
									srvAccepted = tempAccepted;
								}
								if (tempAccepted) {
									isSrvValid = configureSrvCloudDeployment();
								} else {
									isSrvValid = false;
								}
							}
						} else {
							isSrvValid = false;
						}
					}
				}
			} else {
				isSrvValid = configureSrvCloudDeployment();
			}
		}

		if (isJdkValid && isSrvValid) {
			okToProceed = true;
			Activator.getDefault().setSaved(false);
		}
		if (okToProceed) {
			try {
				if (!Activator.getDefault().isSaved()) {
					waProjManager.save();
					/*
					 * Delete files from approot,
					 * whose entry from component table is removed.
					 */
					if (!fileToDel.isEmpty()) {
						for (int i = 0; i < fileToDel.size(); i++) {
							String str = fileToDel.get(i);
							if (str.equalsIgnoreCase("jdk")) {
								deleteJdkDir();
							} else if(str.equalsIgnoreCase("srv")) {
								deleteServerFile();
							} else {
								File file = new File(str);
								if (file.exists()) {
									file.delete();
								}
							}
						}
					}
					fileToDel.clear();
					Activator.getDefault().setSaved(true);
				}
				WAEclipseHelper.refreshWorkspace(
						Messages.rfrshErrTtl,
						Messages.rfrshErrMsg);
				okToProceed = super.performOk();
			} catch (WindowsAzureInvalidProjectOperationException e) {
				okToProceed = false;
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.adRolErrTitle,
						Messages.adRolErrMsgBox1
						+ Messages.adRolErrMsgBox2, e);
			}
		}
		return okToProceed;
	}

	/**
	 * Method configures cloud deployment for JDK
	 * by saving URL, key and cloud method.
	 * @param jdkUrl
	 * @param javaHome
	 * @return
	 */
	private boolean configureJdkCloudDeployment() {
		boolean isValid = true;
		Combo jdkCmb = JdkSrvConfig.getCmbStrgAccJdk();
		String jdkPath = JdkSrvConfig.getTxtJdk().getText().trim();
		String jdkUrl = JdkSrvConfig.getTxtUrl().getText().trim();
		String javaHome = JdkSrvConfig.getTxtJavaHome().
				getText().trim();
		String jdkName = JdkSrvConfig.getThrdPrtJdkCmb().getText();
		try {
			handleJdkDirRemoval();
			handleServerDirRemoval();

			windowsAzureRole = WAServerConfUtilMethods.
					removeJavaHomeSettings(windowsAzureRole, waProjManager);
			windowsAzureRole.setJDKCloudName(null);
			windowsAzureRole.setJDKSourcePath(null, cmpntFile, "");

			if (!(!JdkSrvConfig.getJdkCheckBtn().getSelection()
					&& JdkSrvConfig.getAutoDlRdCldBtn().getSelection())) {
				if (JdkSrvConfig.getThrdPrtJdkBtn().getSelection()) {
					windowsAzureRole.setJDKSourcePath(jdkPath, cmpntFile, jdkName);
				} else {
					windowsAzureRole.setJDKSourcePath(jdkPath, cmpntFile, "");
				}
				// JDK download group
				// By default auto upload will be selected.
				if (JdkSrvConfig.getAutoDlRdCldBtn().getSelection()
						|| JdkSrvConfig.getThrdPrtJdkBtn().getSelection()) {
					if (jdkUrl.
							equalsIgnoreCase(JdkSrvConfig.AUTO_TXT)) {
						jdkUrl = auto;
					}
					if (JdkSrvConfig.getThrdPrtJdkBtn().getSelection()) {
						windowsAzureRole.setJDKCloudName(jdkName);
					}
					windowsAzureRole.setJDKCloudUploadMode(WARoleComponentCloudUploadMode.auto);
				}
				windowsAzureRole.setJDKCloudURL(jdkUrl);
				windowsAzureRole.setJDKCloudKey(JdkSrvConfig.
						getAccessKey(jdkCmb));
				updateJavaHomeAsPerPackageType(javaHome);
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			isValid = false;
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.genErrTitle,
					Messages.urlKeySetErrMsg, e);
		}
		return isValid;
	}

	/**
	 * Method configures cloud deployment for server
	 * by saving URL, key and cloud method.
	 * @param srvUrl
	 * @param srvHome
	 * @return
	 */
	private boolean configureSrvCloudDeployment() {
		boolean isValid = true;
		Combo srvCmb = JdkSrvConfig.getCmbStrgAccSrv();
		String srvPath = JdkSrvConfig.getTxtDir().getText();
		String srvUrl = JdkSrvConfig.getTxtUrlSrv().getText();
		String srvHome = JdkSrvConfig.getTxtHomeDir().getText();
		String srvName = JdkSrvConfig.getServerName();
		try {
			windowsAzureRole = WAServerConfUtilMethods.
					removeServerHomeSettings(windowsAzureRole, waProjManager);
			windowsAzureRole.setServerCloudName(null);
			windowsAzureRole.setServer(null, "", cmpntFile);

			if (JdkSrvConfig.getSerCheckBtn().getSelection()) {
				if (!srvName.isEmpty()) {
					// if its latest server scenario, then don't set cloudkey
					// it should be public download
					boolean setKey = true;
					handleEndpointSettings(srvName);
					windowsAzureRole.setServer(srvName, srvPath, cmpntFile);
					// JDK download group
					// By default auto upload will be selected.
					if (JdkSrvConfig.getAutoDlRdCldBtnSrv().getSelection()
							|| JdkSrvConfig.getThrdPrtSrvBtn().getSelection()) {
						if (srvUrl.equalsIgnoreCase(JdkSrvConfig.AUTO_TXT)) {
							srvUrl = auto;
						}
						if (JdkSrvConfig.getThrdPrtSrvBtn().getSelection()) {
							String altSrcUrl = JdkSrvConfig.getServerCloudAltSource();
							if (altSrcUrl.isEmpty()) {
								setKey = false;
							} else {
								windowsAzureRole.setServerCldAltSrc(altSrcUrl);
								windowsAzureRole.setServerCloudUploadMode(
										WARoleComponentCloudUploadMode.auto);
							}
							windowsAzureRole.setServerCloudName(
									JdkSrvConfig.getThrdPrtSrvCmb().getText());
							windowsAzureRole.setServerCloudValue(srvHome);
						} else {
							windowsAzureRole.setServerCloudUploadMode(
									WARoleComponentCloudUploadMode.auto);
						}
					}
					windowsAzureRole.setServerCloudURL(srvUrl);
					if (setKey) {
						windowsAzureRole.setServerCloudKey(JdkSrvConfig.getAccessKey(srvCmb));
					}
					updateServerHomeAsPerPackageType(srvHome);
				}
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			isValid = false;
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.genErrTitle,
					Messages.urlKeySetErMsgSrv, e);
		}
		return isValid;
	}

	/**
	 * Listener for browse button it is used in file system button.
	 * It will open the file system location.
	 */
	private void jdkBrowseBtnListener() {
		JdkSrvConfig.utilJdkBrowseBtnListener();
	}

	/**
	 * Listener for browse button it is used in file system button.
	 * It will open the file system location.
	 */
	private void serBrowseBtnListener() {
		JdkSrvConfig.utilSerBrowseBtnListener();
	}

	/**
	 * To Add the application to the application list.
	 * @param src : import source location
	 * @param name : import as name
	 * @param method : import method
	 */
	public void addToAppList(String src, String name, String method) {
		AppCmpntParam param = new AppCmpntParam();
		param.setImpSrc(src);
		param.setImpAs(name);
		param.setImpMethod(method);
		appList.add(param);
	}

	/**
	 * @return list of added application AsNames
	 * which is to be set in application table.
	 */
	public ArrayList<String> getAppsAsNames() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < appList.size(); i++) {
			list.add(appList.get(i).getImpAs());
		}
		return list;
	}

	/**
	 * @return appList
	 */
	public ArrayList<AppCmpntParam> getAppsList () {
		return appList;
	}

	/**
	 * Add Application button listener.
	 */
	private void addButtonListener() {
		WAApplicationDialog dialog = new WAApplicationDialog(getShell(),
				null, windowsAzureRole, this);
		dialog.open();
		List<WindowsAzureRoleComponent> srvApp = null;
		try {
			if (!getAppsAsNames().isEmpty()) {
				AppCmpntParam app = getAppsList().get(getAppsList().size() - 1);
				String impSrc = app.getImpSrc();
				String impAs = app.getImpAs();

				try {
					srvApp = windowsAzureRole.getServerApplications();
				} catch (WindowsAzureInvalidProjectOperationException e) {
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.srvErrTtl,
							Messages.getSrvAppErrMsg, e);
				}
				String approotPathSubStr = String.format("%s%s%s%s",
						WAEclipseHelper.getSelectedProject().getName(),
						File.separator,
						windowsAzureRole.getName(),
						Messages.approot);
				boolean needCldAttr = true;
				if (impSrc.contains(approotPathSubStr)) {
					needCldAttr = false;
				}
				if (srvApp.size() == 0) {
					windowsAzureRole.addServerApplication(impSrc, impAs,
							app.getImpMethod(), cmpntFile, needCldAttr);
				} else {
				    boolean isExist = false;
					for (int i = 0; i < srvApp.size(); i++) {
						WindowsAzureRoleComponent c = srvApp.get(i);
						if (impAs.equalsIgnoreCase(c.getDeployName())
								&& impSrc.equalsIgnoreCase(c.getImportPath())) {
						    isExist = true;
						    break;
						}
					}
					if (!isExist) {
                        windowsAzureRole.addServerApplication(impSrc, impAs,
                                app.getImpMethod(), cmpntFile, needCldAttr);
					}
				}
			}
			JdkSrvConfig.getTableViewer()
			.refresh();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.srvErrTtl,
					Messages.addSrvAppErrMsg, e);
		}
	}

	/**
	 * Remove application button listener.
	 */
	private void removeButtonListener() {
		int selIndex = JdkSrvConfig.getTableViewer()
				.getTable().getSelectionIndex();
		if (selIndex > -1) {
			try {
				boolean choice = MessageDialog.openQuestion(getShell(),
						Messages.appRmvTtl, Messages.appRmvMsg);
				if (choice) {
					String cmpntName = JdkSrvConfig.getTableViewer().
							getTable().getItem(selIndex).getText().toString();
					String cmpntPath = String.format("%s%s%s%s%s",
							root.getProject(waProjManager.
									getProjectName()).getLocation(),
							File.separator, windowsAzureRole.getName(),
							Messages.approot, cmpntName);
					windowsAzureRole.removeServerApplication(cmpntName);
					if (!fileToDel.contains(cmpntPath)) {
						fileToDel.add(cmpntPath);
					}
					JdkSrvConfig.getTableViewer().refresh();
					JdkSrvConfigListener.disableRemoveButton();
				}
			} catch (Exception e) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.srvErrTtl,
						Messages.rmvSrvAppErrMsg, e);
			}
		}
	}

	/**
	 * To delete jdk directory which is present inside approot
	 * if JDK source path is modified.
	 */
	private void deleteJdkDir() {
		String jdkPath = "";
		try {
			String jdkDirName = new File(finalJdkPath).getName();
			jdkPath = String.format("%s%s%s%s%s",
					root.getProject(waProjManager.
							getProjectName()).getLocation(),
					File.separator, windowsAzureRole.getName(),
					Messages.approot, jdkDirName);
		} catch (WindowsAzureInvalidProjectOperationException e1) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.jdkPathErrTtl,
					Messages.jdkDirErrMsg, e1);
		}
		File jdkFile = new File(jdkPath);
		if (jdkFile.exists()) {
			WAEclipseHelperMethods.deleteDirectory(jdkFile);
			WAEclipseHelper.refreshWorkspace(
					Messages.rfrshErrTtl,
					Messages.rfrshErrMsg);
		}
	}

	/**
	 *  To delete zip file or directory of
	 *  server which is present inside approot
	 *  if server name or source path is modified.
	 */
	private void deleteServerFile() {
		File srvFile = null;
		try {
			srvFile = new File(String.format("%s%s%s%s%s",
					root.getProject(waProjManager.
							getProjectName()).getLocation(),
					File.separator, windowsAzureRole.getName(),
					Messages.approot, ProjectNatureHelper.
					getAsName(finalSrvPath, finalImpMethod, finalAsName)));
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.genErrTitle,
					Messages.srvFileErr, e);
		}
		if (srvFile.exists()) {
			if (srvFile.isFile()) {
				srvFile.delete();
			} else if (srvFile.isDirectory()) {
				WAEclipseHelperMethods.deleteDirectory(srvFile);
			}
			WAEclipseHelper.refreshWorkspace(
					Messages.rfrshErrTtl,
					Messages.rfrshErrMsg);
		}
	}

	/**
	 * Method returns component object according to component type.
	 * If component not present then returns NULL.
	 * @param cmpntType
	 * @return WindowsAzureRoleComponent
	 */
	private WindowsAzureRoleComponent getPrevCmpnt(String cmpntType) {
		WindowsAzureRoleComponent cmp = null;
		try {
			cmp = WAServerConfUtilMethods.
					getPrevCmpnt(cmpntType, windowsAzureRole);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.cmpntSetErrTtl,
					Messages.cmpntgetErrMsg, e);
		}
		return cmp;
	}

	/**
	 * Method updates java home,
	 * according to current package type.
	 * Method will get called when user click
	 * on OK button or tries to navigate to other page.
	 * @param javaHome
	 */
	private void updateJavaHomeAsPerPackageType(String javaHome) {
		try {
			windowsAzureRole = WAServerConfUtilMethods.
					updateJavaHome(javaHome, windowsAzureRole, waProjManager,
							JdkSrvConfig.getTxtJdk().getText().trim(), cmpntFile);
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.genErrTitle,
					Messages.jvHomeErr, e);
		}
	}

	/**
	 * Method updates server home,
	 * according to current package type.
	 * Method will get called when user click
	 * on OK button or tries to navigate to other page.
	 * @param srvHome
	 */
	private void updateServerHomeAsPerPackageType(String srvHome) {
		try {
			windowsAzureRole = WAServerConfUtilMethods.updateServerHome(srvHome,
					windowsAzureRole, waProjManager,
					JdkSrvConfig.getTxtDir().getText().trim(),
					JdkSrvConfig.getServerName(), cmpntFile);
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.genErrTitle,
					Messages.srvHomeErr, e);
		}
	}

	/**
	 * If user is trying to move from JDK tab
	 * and third party JDK is selected
	 * but license is not accepted till now
	 * then show license agreement dialog.
	 * @return boolean
	 * true : license accepted
	 * false : license not accepted
	 */
	private boolean displayLicenseAgreement() {
		boolean temp = true;
		if (prevTabIndex == 0
				&& JdkSrvConfig.getThrdPrtJdkBtn().getSelection()
				&& !accepted) {
			temp = JdkSrvConfig.createAccLicenseAggDlg(getShell(), true);
			accepted =  temp;
		}
		return temp;
	}

	private boolean displayServerLicenseAgreement() {
		boolean temp = true;
		if (prevTabIndex == 1
				&& JdkSrvConfig.getThrdPrtSrvBtn().getSelection()
				&& !srvAccepted) {
			temp = JdkSrvConfig.createAccLicenseAggDlg(getShell(), false);
			srvAccepted =  temp;
		}
		return temp;
	}
}

