/**
 * Copyright 2013 Persistent Systems Ltd.
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WARoleComponentCloudUploadMode;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponentImportMethod;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.ui.projwizard.WAApplicationDialog;
import com.persistent.util.AppCmpntParam;
import com.persistent.util.JdkSrvConfig;
import com.persistent.util.JdkSrvConfigListener;
import com.persistent.util.ProjectNatureHelper;
import com.persistent.util.WAEclipseHelper;

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
	private final String auto = "auto";
	private int prevTabIndex;
	private static boolean accepted = false;
	private String jdkPrevName;

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
			} else {
				JdkSrvConfig.setEnableJDK(true);
				JdkSrvConfig.
				getTxtJdk().setText(jdkSrcPath);
				JdkSrvConfigListener.showThirdPartyJdkNames(true);
				String jdkUrl = windowsAzureRole.getJDKCloudURL();
				// JDK download group
				if (jdkUrl != null && !jdkUrl.isEmpty()) {
					// JDK auto upload option configured
					if (JdkSrvConfig.
							isJDKAutoUploadPrevSelected(windowsAzureRole)) {
						// check for third party JDK
						String jdkName = windowsAzureRole.getJDKCloudName();
						if (jdkName.isEmpty()) {
							JdkSrvConfig.getAutoDlRdCldBtn().setSelection(true);
						} else {
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
						JdkSrvConfig.setEnableDlGrp(true, true);
					} else {
						// JDK deploy option configured
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
					String dirName = new File(jdkSrcPath).getName();
					JdkSrvConfig.getLblDlNoteUrl().
					setText(String.format(
							Messages.dlNtLblDir, dirName));

					// Update storage account combo box.
					String jdkKey = windowsAzureRole.getJDKCloudKey();
					JdkSrvConfig.setCmbStrgAccJdk(JdkSrvConfig.
							populateStrgNameAsPerKey(jdkKey,
									JdkSrvConfig.getCmbStrgAccJdk()));
				}
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.jdkPathErrTtl,
					Messages.getJdkErrMsg, e);
		}
		// Check Server is already enabled or not
		// and if enabled show appropriate values on property page
		try {
			String srvSrcPath = null;
			String srvName = null;

			srvSrcPath = windowsAzureRole.getServerSourcePath();
			srvName = windowsAzureRole.getServerName();

			if (srvSrcPath != null && srvName != null) {
				JdkSrvConfig.getSerCheckBtn().setSelection(true);
				JdkSrvConfig.setEnableServer(true);
				JdkSrvConfig.getComboServer().setText(srvName);
				JdkSrvConfig.getTxtDir().setText(srvSrcPath);
				// Server download group
				String srvUrl = windowsAzureRole.getServerCloudURL();
				if (srvUrl != null && !srvUrl.isEmpty()) {
					// server auto upload option configured
					if (JdkSrvConfig.
							isServerAutoUploadPrevSelected(windowsAzureRole)) {
						JdkSrvConfig.getAutoDlRdCldBtnSrv().setSelection(true);
						JdkSrvConfig.setEnableDlGrpSrv(true, true);
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
					String dirName = new File(srvSrcPath).getName();
					JdkSrvConfig.getLblDlNoteUrlSrv().
					setText(String.format(
							Messages.dlNtLblDir, dirName));
					String srvKey = windowsAzureRole.getServerCloudKey();
					JdkSrvConfig.setCmbStrgAccSrv(JdkSrvConfig.
							populateStrgNameAsPerKey(srvKey,
									JdkSrvConfig.getCmbStrgAccSrv()));
				}
			} else {
				JdkSrvConfig.setEnableServer(false);
				JdkSrvConfig.setEnableDlGrpSrv(false, false);
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(getShell(),
					Messages.srvErrTtl,
					Messages.getSrvBothErrMsg, e);
		}

		if (JdkSrvConfig.getJdkCheckBtn().getSelection()) {
			JdkSrvConfig.getSerCheckBtn().setEnabled(true);
		} else {
			JdkSrvConfig.setEnableDlGrp(false, false);
			JdkSrvConfig.setEnableServer(false);
			JdkSrvConfig.setEnableDlGrpSrv(false, false);
		}

		if (JdkSrvConfig.getTableViewer() != null) {
			JdkSrvConfig.getTableViewer().refresh();
		}

		// by default set tab to JDK
		folder.setSelection(jdkTab);
		prevTabIndex = 0;
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
		TabItem srvTab = new TabItem(folder, SWT.NONE);
		srvTab.setText(Messages.dplDlgSerTxt);
		srvTab.setControl(createServer(folder));

		// Tab for Application
		TabItem appTab = new TabItem(folder, SWT.NONE);
		appTab.setText(Messages.lblApp);
		appTab.setControl(createAppTblCmpnt(folder));

		folder.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (folder.getSelectionIndex() == 0) {
					prevTabIndex = 0;
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
		if (displayLicenseAgreement()) {
			prevTabIndex = 2;
		} else {
			folder.setSelection(jdkTab);
			prevTabIndex = 0;
		}
	}

	/** Sets the JDK.
	 * @param directory
	 * @throws WindowsAzureInvalidProjectOperationException
	 */
	private void setJDK(String jdkPath)
			throws WindowsAzureInvalidProjectOperationException {
		if (jdkPath != null && !jdkPath.isEmpty()) {
			// deleting JDK entry from approot
			if (windowsAzureRole.getJDKSourcePath() != null
					&& !fileToDel.contains("jdk")) {
				fileToDel.add("jdk");
				WindowsAzureRoleComponent cmp =
						getPrevCmpnt(Messages.typeJdkDply);
				if (cmp != null) {
					finalJdkPath = cmp.getImportPath();
				}
			}
			File jdkFile = new File(jdkPath);
			if (jdkFile.exists() && jdkFile.isDirectory()) {
				windowsAzureRole.setJDKSourcePath(jdkPath,
						cmpntFile, "");
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
			private String oldTxt = "";
			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					String jdkPath = JdkSrvConfig.getTxtJdk().getText();
					if (!jdkPath.equalsIgnoreCase(oldTxt)) {
						setJDK(jdkPath.trim());
					}
					JdkSrvConfigListener.focusLostJdkText(jdkPath);
				} catch (WindowsAzureInvalidProjectOperationException e) {
					PluginUtil.displayErrorDialogAndLog(getShell(),
							Messages.jdkPathErrTtl,
							Messages.setJdkErrMsg, e);
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				oldTxt = JdkSrvConfig.getTxtJdk().getText();
			}
		});

		// Modify listener for JDK location text box.
		JdkSrvConfig.getTxtJdk().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				JdkSrvConfigListener.modifyJdkText(
						windowsAzureRole, Messages.dlNtLblDir);
				handlePageComplete();
			}
		});

		// listener for JDK check button.
		JdkSrvConfig.getJdkCheckBtn().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getJdkCheckBtn().getSelection()) {
					try {
						setJDK(JdkSrvConfigListener.jdkChkBoxChecked(windowsAzureRole));
					} catch (WindowsAzureInvalidProjectOperationException e) {
						PluginUtil.displayErrorDialogAndLog(
								getShell(),
								Messages.jdkPathErrTtl,
								Messages.jdkDirErrMsg, e);
					}
				} else {
					try {
						// deleting JDK entry from approot
						if (windowsAzureRole.getJDKSourcePath() != null
								&& !fileToDel.contains("jdk")) {
							fileToDel.add("jdk");
							WindowsAzureRoleComponent cmp =
									getPrevCmpnt(Messages.typeJdkDply);
							if (cmp != null) {
								finalJdkPath = cmp.getImportPath();
							}
						}
						// Remove JAVA_HOME settings
						removeJavaHomeSettings();
						if (windowsAzureRole.getServerName() != null
								&& windowsAzureRole.
								getServerSourcePath() != null) {
							removeServerHomeSettings();
						}
						// Remove server setting
						updateServer(null, null, cmpntFile);
						// Remove JDK name property
						windowsAzureRole.setJDKCloudName(null);
						// JDK URL and key will get removed if present.
						windowsAzureRole.setJDKSourcePath(null, cmpntFile, "");
					} catch (WindowsAzureInvalidProjectOperationException e) {
						PluginUtil.displayErrorDialogAndLog(
								getShell(),
								Messages.jdkPathErrTtl,
								Messages.setJdkErrMsg, e);
					}
					JdkSrvConfigListener.jdkChkBoxUnChecked();
					accepted = false;
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
					JdkSrvConfig.getTxtUrl().setText(
							JdkSrvConfig.getUrl(
									JdkSrvConfig.getCmbStrgAccJdk()));
					JdkSrvConfigListener.jdkDeployBtnSelected(windowsAzureRole);
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
					configureAutoUploadJDKSettings(windowsAzureRole,
							Messages.dlNtLblDir);
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
					JdkSrvConfigListener.thirdPartyJdkBtnSelected(
							windowsAzureRole, Messages.dlNtLblDir);
					jdkPrevName = JdkSrvConfig.
							getThrdPrtJdkCmb().getText();
				}
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
					JdkSrvConfigListener.srvChkBoxChecked(
							windowsAzureRole,
							Messages.dlNtLblDir);
				} else {
					// Remove server home settings
					removeServerHomeSettings();
					// Remove server setting
					updateServer(null, null, cmpntFile);
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
						updateServer(serverName,
								path, cmpntFile);
					}
				}
				path = JdkSrvConfig.getTxtDir().getText().trim();
				JdkSrvConfigListener.
				focusLostSrvText(path,
						Messages.dlNtLblDir,
						Messages.dlgDlNtLblUrl);
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
				JdkSrvConfigListener.modifySrvText(
						windowsAzureRole,
						Messages.dlNtLblDir);
				handlePageComplete();
			}
		});

		// listener for Server browse button.
		JdkSrvConfig.getBtnSrvLoc().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				serBrowseBtnListener();
				JdkSrvConfigListener.modifySrvText(windowsAzureRole,
						Messages.dlNtLblDir);
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
				updateServer(JdkSrvConfig.getComboServer().getText(),
						JdkSrvConfig.getTxtDir().getText(),
						cmpntFile);
				if (JdkSrvConfig.isSrvDownloadChecked()
						|| JdkSrvConfig.isSrvAutoUploadChecked()) {
					JdkSrvConfig.updateServerHome(windowsAzureRole);
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

		// Server download group
		// listener for Server deploy radio button.
		JdkSrvConfig.getDlRdCldBtnSrv().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getDlRdCldBtnSrv()
						.getSelection()) {
					JdkSrvConfigListener.
					srvDeployBtnSelected(windowsAzureRole,
							Messages.dlNtLblDir);
				}
				handlePageComplete();
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
					JdkSrvConfigListener.configureAutoUploadServerSettings(
							windowsAzureRole,
							Messages.dlNtLblDir);
				} else {
					/*
					 * server auto upload radio button unselected
					 * and deploy button selected.
					 */
					if (JdkSrvConfig.getDlRdCldBtnSrv().getSelection()) {
						JdkSrvConfig.getTxtUrlSrv().setText(
								JdkSrvConfig.getUrl(
										JdkSrvConfig.getCmbStrgAccSrv()));
						return;
					}
				}
				handlePageComplete();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
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
				JdkSrvConfig.updateServerHome(windowsAzureRole);
				handlePageComplete();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		return control;
	}

	/**
	 * Updates server settings when UI controls change.
	 * @param newName
	 * @param newPath
	 * @param componentFile
	 */
	private void updateServer(String newName,
			String newPath,
			File componentFile) {
		try {
			String oldName = windowsAzureRole.getServerName();
			String oldPath = windowsAzureRole.getServerSourcePath();
			String path = newPath;

			if (newName != null
					&& path != null
					&& newName.equalsIgnoreCase(oldName)
					&& path.equalsIgnoreCase(oldPath)) {
				return;
			}

			// Remove old server from approot
			if (windowsAzureRole.getServerName() != null
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

			if (path == null
					|| path.isEmpty()) {
				path = Messages.dummySrvPath;
			}

			// Remove the current server if any
			windowsAzureRole.setServer(null,
					Messages.dummySrvPath,
					componentFile);

			// Add the new server if desired
			if (newName != null
					&& path != null) {
				windowsAzureRole.setServer(newName,
						path, componentFile);
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.srvErrTtl,
					Messages.setSrvNmErrMsg, e);
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

		/* Enable edit and remove button only when
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
			if (JdkSrvConfig.getJdkCheckBtn().getSelection()) {
				/*
				 * Check if third party JDK is selected
				 * then license is accepted or not.
				 */
				boolean tempAccepted = true;
				if (JdkSrvConfig.getThrdPrtJdkBtn().getSelection()
						&& !accepted) {
					tempAccepted = JdkSrvConfig.createAccLicenseAggDlg();
					accepted = tempAccepted;
				}
				if (tempAccepted) {
					String jdkUrl = JdkSrvConfig.
							getTxtUrl().getText().trim();
					if ((JdkSrvConfig.getAutoDlRdCldBtn().getSelection()
							|| JdkSrvConfig.getThrdPrtJdkBtn().getSelection())
							&& jdkUrl.equalsIgnoreCase(JdkSrvConfig.AUTO_TXT)) {
						jdkUrl = auto;
					}
					okToProceed = configureJdkCloudDeployment(jdkUrl,
							JdkSrvConfig.getTxtJavaHome().
							getText().trim());
				} else {
					okToProceed = false;
				}
			}

			if (okToProceed
					&& JdkSrvConfig.getJdkCheckBtn().getSelection()
					&& JdkSrvConfig.
					getSerCheckBtn().getSelection()) {
				String srvUrl = JdkSrvConfig.
						getTxtUrlSrv().getText().trim();
				if (JdkSrvConfig.getAutoDlRdCldBtnSrv().getSelection()
						&& srvUrl.equalsIgnoreCase(JdkSrvConfig.AUTO_TXT)) {
					srvUrl = auto;
				}
				okToProceed = configureSrvCloudDeployment(srvUrl,
						JdkSrvConfig.getTxtHomeDir().
						getText().trim());
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
				if (file.exists()
						&& file.isDirectory()) {
					// JDK download group
					// cloud radio button selected
					if (JdkSrvConfig.getDlRdCldBtn().getSelection()) {
						// Validate JDK URL
						String jdkUrl = JdkSrvConfig.
								getTxtUrl().getText().trim();
						if (jdkUrl.isEmpty()) {
							isJdkValid = false;
							setErrorMessage(Messages.dlgDlUrlErrMsg);
						} else {
							try {
								new URL(jdkUrl);
								if (WAEclipseHelper.isBlobStorageUrl(jdkUrl)) {
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
				} else {
					isJdkValid = false;
					setErrorMessage(Messages.jdkPathErrMsg);
				}
			}
		}

		// Validation for Server
		if (isJdkValid && JdkSrvConfig.
				getSerCheckBtn().getSelection()) {
			if (JdkSrvConfig.getComboServer().getText().isEmpty()) {
				setErrorMessage(Messages.dplEmtSerMsg);
				isSrvValid = false;
			} else if (JdkSrvConfig.getTxtDir().getText().isEmpty()) {
				setErrorMessage(Messages.dplWrngSerMsg);
				isSrvValid = false;
			} else if ((new File(JdkSrvConfig.
					getTxtDir().getText()).exists())
					&& (new File(JdkSrvConfig.getTxtDir().
							getText()).isDirectory())) {
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
							if (WAEclipseHelper.isBlobStorageUrl(srvUrl)) {
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
			} else {
				setErrorMessage(Messages.dplWrngSerMsg);
				isSrvValid = false;
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
				if (file.exists() && file.isDirectory()) {
					// JDK download group
					// Validate JDK URL
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
								if (WAEclipseHelper.isBlobStorageUrl(jdkUrl)) {
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
							String javaHome = JdkSrvConfig.getTxtJavaHome().
									getText().trim();
							if (javaHome.isEmpty()) {
								isJdkValid = false;
								PluginUtil.displayErrorDialog(getShell(),
										Messages.genErrTitle,
										Messages.jvHomeErMsg);
							} else {
								boolean tempAccepted = true;
								if (JdkSrvConfig.getThrdPrtJdkBtn().getSelection()
										&& !accepted) {
									tempAccepted = JdkSrvConfig.createAccLicenseAggDlg();
									accepted = tempAccepted;
								}
								if (tempAccepted) {
									isJdkValid = configureJdkCloudDeployment(jdkUrl, javaHome);
								} else {
									isJdkValid = false;
								}
							}
						} else {
							isJdkValid = false;
						}
					}
				} else {
					isJdkValid = false;
					PluginUtil.displayErrorDialogAndLog(
							getShell(),
							Messages.jdkPathErrTtl,
							Messages.jdkPathErrMsg, null);
				}
			}
		}

		// Validation for Server
		if (isJdkValid && JdkSrvConfig.getSerCheckBtn().getSelection()) {
			if (JdkSrvConfig.getComboServer().getText().isEmpty()) {
				isSrvValid = false;
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.srvErrTtl,
						Messages.dplEmtSerMsg, null);
			} else if (JdkSrvConfig.getTxtDir().getText().isEmpty()) {
				isSrvValid = false;
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.srvErrTtl,
						Messages.dplWrngSerMsg, null);
			} else if ((new File(JdkSrvConfig.
					getTxtDir().getText()).exists())
					&& (new File(JdkSrvConfig.
							getTxtDir().getText()).isAbsolute())) {
				// Server download group
				if (JdkSrvConfig.getDlRdCldBtnSrv().getSelection() || 
						JdkSrvConfig.getAutoDlRdCldBtnSrv().getSelection()) {
					// Validate Server URL
					String srvUrl = JdkSrvConfig.
							getTxtUrlSrv().getText().trim();
					if (srvUrl.isEmpty()) {
						isSrvValid = false;
						PluginUtil.displayErrorDialog(getShell(),
								Messages.dlgDlUrlErrTtl,
								Messages.dlgDlUrlErrMsg);
					} else {
						Boolean isSrvUrlValid = false;
						// Server auto upload option selected.
						if (JdkSrvConfig.getAutoDlRdCldBtnSrv().getSelection()) {
							if (srvUrl.equalsIgnoreCase(JdkSrvConfig.AUTO_TXT)) {
								srvUrl = auto;
							}
							isSrvUrlValid = true;
						} else {
							// Server cloud option selected
							try {
								new URL(srvUrl);
								if (WAEclipseHelper.isBlobStorageUrl(srvUrl)) {
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
							String srvHome = JdkSrvConfig.getTxtHomeDir().
									getText().trim();
							if (srvHome.isEmpty()) {
								isSrvValid = false;
								PluginUtil.displayErrorDialog(getShell(),
										Messages.genErrTitle,
										Messages.srvHomeErMsg);
							} else {
								isSrvValid = configureSrvCloudDeployment(srvUrl, srvHome);
							}
						} else {
							isSrvValid = false;
						}
					}
				} else {
					isSrvValid = true;
				}
			} else {
				isSrvValid = false;
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.srvErrTtl,
						Messages.dplWrngSerMsg, null);
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
	private boolean configureJdkCloudDeployment(
			String jdkUrl, String javaHome) {
		boolean isValid = true;
		Combo jdkCmb = JdkSrvConfig.getCmbStrgAccJdk();
		try {
			windowsAzureRole.setJDKCloudURL(jdkUrl);
			updateJavaHome(javaHome);
			windowsAzureRole.setJDKCloudKey(JdkSrvConfig.
					getAccessKey(jdkCmb));
			/*
			 * If third party radio button selected.
			 */
			if (JdkSrvConfig.getThrdPrtJdkBtn().getSelection()) {
				windowsAzureRole.setJDKCloudUploadMode(
						WARoleComponentCloudUploadMode.AUTO);
				/*
				 * Create JDK name property.
				 * Set cloudvalue in environment variable
				 * Set cloudaltsrc in component
				 */
				String jdkName = JdkSrvConfig.getThrdPrtJdkCmb().getText();
				windowsAzureRole.setJDKCloudName(jdkName);
				windowsAzureRole.setJdkCloudValue(
						WindowsAzureProjectManager.
						getCloudValue(jdkName, cmpntFile));
				windowsAzureRole.setJdkCldAltSrc(WindowsAzureProjectManager.
						getCloudAltSrc(jdkName, cmpntFile));
			} else {
				windowsAzureRole.setJDKCloudName(null);
				windowsAzureRole.setJdkCloudValue(null);
				windowsAzureRole.setJdkCldAltSrc(null);
				if (JdkSrvConfig.getAutoDlRdCldBtn().getSelection()) {
					windowsAzureRole.setJDKCloudUploadMode(
							WARoleComponentCloudUploadMode.AUTO);
				} else {
					windowsAzureRole.setJDKCloudUploadMode(null);
				}
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			isValid = false;
			PluginUtil.displayErrorDialog(getShell(),
					Messages.genErrTitle,
					Messages.urlKeySetErrMsg);
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
	private boolean configureSrvCloudDeployment(
			String srvUrl, String srvHome) {
		boolean isValid = true;
		Combo srvCmb = JdkSrvConfig.getCmbStrgAccSrv();
		try {
			windowsAzureRole.setServerCloudURL(srvUrl);
			updateServerHome(srvHome);
			windowsAzureRole.setServerCloudKey(JdkSrvConfig.
					getAccessKey(srvCmb));
			if (JdkSrvConfig.getAutoDlRdCldBtnSrv().getSelection()) {
				windowsAzureRole.setServerCloudUploadMode(
						WARoleComponentCloudUploadMode.AUTO);
			} else {
				windowsAzureRole.setServerCloudUploadMode(null);
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			isValid = false;
			PluginUtil.displayErrorDialog(getShell(),
					Messages.genErrTitle,
					Messages.urlKeySetErMsgSrv);
		}
		return isValid;
	}

	/**
	 * Listener for browse button it is used in file system button.
	 * It will open the file system location.
	 */
	private void jdkBrowseBtnListener() {
		String oldTxt = JdkSrvConfig.getTxtJdk().getText();
		String directory = JdkSrvConfig.
				utilJdkBrowseBtnListener(Messages.dlNtLblDir);
		if (directory != null
				&& !directory.equalsIgnoreCase(oldTxt)) {
			try {
				setJDK(directory);
			}  catch (WindowsAzureInvalidProjectOperationException e) {
				PluginUtil.displayErrorDialogAndLog(
						getShell(),
						Messages.jdkPathErrTtl,
						Messages.setJdkErrMsg, e);
			}
		}
	}

	/**
	 * Listener for browse button it is used in file system button.
	 * It will open the file system location.
	 */
	private void serBrowseBtnListener() {
		String directory = JdkSrvConfig.utilSerBrowseBtnListener(
				Messages.dlNtLblDir);
		/*
		 * Check server configured previously
		 * and now server name is changed.
		 */
		if (directory != null) {
			updateServer(JdkSrvConfig.getComboServer().getText(),
					JdkSrvConfig.getTxtDir().getText(), cmpntFile);
		}
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
				if (srvApp.size() == 0) {
					windowsAzureRole.addServerApplication(impSrc, impAs,
							app.getImpMethod(), cmpntFile);
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
                                app.getImpMethod(), cmpntFile);
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
				boolean choice = MessageDialog.openQuestion(new Shell(),
						Messages.appRmvTtl, Messages.appRmvMsg);
				if (choice) {
					String cmpntName = JdkSrvConfig.getTableViewer().
							getTable().getItem(selIndex).getText().toString();
					String cmpntPath = String.format("%s%s%s%s%s",
							root.getProject(waProjManager.
									getProjectName()).getLocation(),
							"\\", windowsAzureRole.getName(),
							Messages.approot, cmpntName);
					windowsAzureRole.removeServerApplication(cmpntName);
					if (!fileToDel.contains(cmpntPath)) {
						fileToDel.add(cmpntPath);
					}
					JdkSrvConfig.getTableViewer().refresh();
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
					"\\", windowsAzureRole.getName(),
					Messages.approot, jdkDirName);
		} catch (WindowsAzureInvalidProjectOperationException e1) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.jdkPathErrTtl,
					Messages.jdkDirErrMsg, e1);
		}
		File jdkFile = new File(jdkPath);
		if (jdkFile.exists()) {
			WAEclipseHelper.deleteDirectory(jdkFile);
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
					"\\", windowsAzureRole.getName(),
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
				WAEclipseHelper.deleteDirectory(srvFile);
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
		List<WindowsAzureRoleComponent> listComponents = null;
		WindowsAzureRoleComponent cmp = null;
		try {
			listComponents = windowsAzureRole.getComponents();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			PluginUtil.displayErrorDialogAndLog(
					getShell(),
					Messages.cmpntSetErrTtl,
					Messages.cmpntgetErrMsg, e);
		}
		for (int i = 0; i < listComponents.size(); i++) {
			if (listComponents.get(i).getType().
					equalsIgnoreCase(cmpntType)) {
				cmp = listComponents.get(i);
			}
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
	private void updateJavaHome(String javaHome) {
		try {
			if (waProjManager.getPackageType().
					equals(WindowsAzurePackageType.LOCAL)) {
				windowsAzureRole.setJDKCloudHome(
						javaHome);
				windowsAzureRole.setJDKLocalHome(null);
			} else {
				windowsAzureRole.
				setRuntimeEnv(Messages.jvHome, javaHome);
				windowsAzureRole.setJDKLocalHome(
						windowsAzureRole.constructJdkHome(
								JdkSrvConfig.getTxtJdk().getText().trim(),
								cmpntFile));
			}
		} catch (Exception e) {
			PluginUtil.displayErrorDialog(
					getShell(),
					Messages.genErrTitle,
					Messages.jvHomeErr);
		}
	}

	/**
	 * Method updates server home,
	 * according to current package type.
	 * Method will get called when user click
	 * on OK button or tries to navigate to other page.
	 * @param srvHome
	 */
	private void updateServerHome(String srvHome) {
		try {
			if (waProjManager.getPackageType().
					equals(WindowsAzurePackageType.LOCAL)) {
				windowsAzureRole.setServerCloudHome(
						srvHome);
				windowsAzureRole.setServerLocalHome(null);
			} else {
				windowsAzureRole.
				setRuntimeEnv(windowsAzureRole.
						getRuntimeEnvName(
								Messages.typeSrvHm),
								srvHome);
				windowsAzureRole.setServerLocalHome(
						windowsAzureRole.constructServerHome(
								JdkSrvConfig.getComboServer().getText(),
								JdkSrvConfig.getTxtDir().getText().trim(),
								cmpntFile));
			}
		} catch (Exception e) {
			PluginUtil.displayErrorDialog(
					getShell(),
					Messages.genErrTitle,
					Messages.srvHomeErr);
		}
	}

	/**
	 * Method removes java home settings,
	 * according to current package type.
	 * Method will get called on the event of
	 * check box uncheck.
	 */
	private void removeJavaHomeSettings() {
		try {
			if (waProjManager.getPackageType().
					equals(WindowsAzurePackageType.LOCAL)) {
				windowsAzureRole.setJDKCloudHome(null);
			} else {
				String localVal =
						windowsAzureRole.getJDKLocalHome();
				windowsAzureRole.
				setRuntimeEnv(Messages.jvHome, localVal);
				windowsAzureRole.setJDKLocalHome(null);
			}
		} catch (Exception e) {
			PluginUtil.displayErrorDialog(
					getShell(),
					Messages.genErrTitle,
					Messages.jvHomeErr);
		}
	}

	/**
	 * Method removes server home settings,
	 * according to current package type.
	 * Method will get called on the event of
	 * check box uncheck.
	 */
	private void removeServerHomeSettings() {
		try {
			if (waProjManager.getPackageType().
					equals(WindowsAzurePackageType.LOCAL)) {
				windowsAzureRole.setServerCloudHome(null);
			} else {
				String localVal =
						windowsAzureRole.getServerLocalHome();
				windowsAzureRole.
				setRuntimeEnv(windowsAzureRole.
						getRuntimeEnvName(Messages.typeSrvHm),
						localVal);
				windowsAzureRole.setServerLocalHome(null);
			}
		} catch (Exception e) {
			PluginUtil.displayErrorDialog(
					getShell(),
					Messages.genErrTitle,
					Messages.srvHomeErr);
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
			temp = JdkSrvConfig.createAccLicenseAggDlg();
			accepted =  temp;
		}
		return temp;
	}
}

