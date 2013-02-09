/**
 * Copyright 2013 Persistent Systems Ltd.
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
package com.persistent.ui.projwizard;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.persistent.util.AppCmpntParam;
import com.persistent.util.JdkSrvConfig;
import com.persistent.util.WAEclipseHelper;
/**
 * Class creates wizard page which has
 * JDK, Server and Application tabs.
 * Also has listeners for UI components.
 */
public class WATabPage extends WizardPage {

	private static TabFolder folder;
	private TabItem srvTab;
	private static TabItem jdkTab;
	private TabItem appTab;
	private ArrayList<AppCmpntParam> appList =
			new ArrayList<AppCmpntParam>();
	private Object pageObj;
	private boolean isWizard;
	private WindowsAzureRole waRole;
	private boolean inHandlePgComplete = false;
	private boolean inHndlPgCmpltBackBtn = false;

	/**
	 * Constructor.
	 * @param pageName
	 * @param role
	 * @param pageObj
	 * @param isWizard
	 */
	protected WATabPage(String pageName,
			WindowsAzureRole role, Object pageObj,
			boolean isWizard) {
		super(pageName);
		this.waRole = role;
		this.pageObj = pageObj;
		this.isWizard = isWizard;
		setTitle(Messages.wizPageTitle);
		setDescription(Messages.dplPageJdkMsg);
		setPageComplete(true);
		if (!Activator.getDefault().isContextMenu()) {
			try {
				AppCmpntParam acp = new AppCmpntParam();
				acp.setImpAs(waRole.getComponents().
						get(0).getDeployName());
				appList.add(acp);
			} catch (WindowsAzureInvalidProjectOperationException e) {
				Activator.getDefault().log(e.getMessage());
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		// display help contents
		PlatformUI
		.getWorkbench()
		.getHelpSystem()
		.setHelp(parent.getShell(),
				"com.persistent.winazure.eclipseplugin."
						+ "windows_azure_project");
		// Tab controls
		folder = new TabFolder(parent, SWT.NONE);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		folder.setLayoutData(gridData);

		// Tab for JDK
		jdkTab = new TabItem(folder, SWT.NONE);
		jdkTab.setText(Messages.dplPageJDKGrp);
		jdkTab.setControl(createJDK(folder));

		// Tab for Server
		srvTab = new TabItem(folder, SWT.NONE);
		srvTab.setText(Messages.dplPageSerTxt);
		srvTab.setControl(createServer(folder));

		// Tab for Application
		appTab = new TabItem(folder, SWT.NONE);
		appTab.setText(Messages.dplPageAppLbl);
		appTab.setControl(createAppTblCmpnt(folder));

		/*
		 * Set the page description
		 * according to the tab selected by user.
		 */
		folder.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (folder.getSelectionIndex() == 0) {
					getWizard().getPage(Messages.tbPg).
					setDescription(Messages.dplPageJdkMsg);
				} else if (folder.getSelectionIndex() == 1) {
					getWizard().getPage(Messages.tbPg).
					setDescription(Messages.dplPageSrvMsg);
				} else if (folder.getSelectionIndex() == 2) {
					getWizard().getPage(Messages.tbPg).
					setDescription(Messages.dplPageAppMsg);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});

		/*
		 * If wizard activated through right click on
		 * Dynamic web project then
		 * enable JDK and Server components.
		 */
		if (Activator.getDefault().isContextMenu()) {
			JdkSrvConfig.setEnableJDK(true);
			JdkSrvConfig.getTxtJdk().
			setText(WAEclipseHelper.jdkDefaultDirectory(null));
			JdkSrvConfig.getDlCheckBtn().
			setEnabled(true);
			JdkSrvConfig.setEnableServer(true);
			String file = WAEclipseHelper.getTemplateFile();
			File cmpntFile = new File(file);
			try {
				String[] servList = WindowsAzureProjectManager.
			    getServerTemplateNames(cmpntFile);
			    
				Arrays.sort(servList);

			    JdkSrvConfig.getComboServer().setItems(servList);
			} catch (WindowsAzureInvalidProjectOperationException e) {
			    Activator.getDefault().log(e.getMessage());
			}
			JdkSrvConfig.getSerCheckBtn().setEnabled(true);
			JdkSrvConfig.getSerCheckBtn().setSelection(true);
			JdkSrvConfig.getDlCheckBtnSrv().setEnabled(true);
			handlePageComplete();
		}

		setControl(folder);
		// Set by default tab selection to JDK
		folder.setSelection(jdkTab);
	}

	/**
	 * Method checks which page to display next.
	 * Depending on which tab is active,
	 * and the state of the check boxes,
	 * it either activates the next tab
	 * or jumps to the next screen
	 * (Key feature page) in the wizard.
	 */
	@Override
	public IWizardPage getNextPage() {
		int tabIndex = folder.getSelectionIndex();
		IWizardPage page;
		/*
		 * If getNextPage() is called due to
		 * setPageComplete() method
		 * then don't check anything.
		 * Be on same page.
		 */
		if (inHandlePgComplete) {
			folder.setSelection(tabIndex);
			page = getWizard().getPage(Messages.tbPg);
		} else {
			/*
			 * Next button has been clicked.
			 */
			if (tabIndex == 0
					&& isJdkChecked()
					&& !getJdkLoc().isEmpty()) {
				folder.setSelection(srvTab);
				page = getWizard().getPage(Messages.tbPg);
				page.setDescription(Messages.dplPageSrvMsg);
			} else if (tabIndex == 1
					&& isSrvChecked()
					&& !getServerName().isEmpty()
					&& !getServerLoc().isEmpty()
					&& isJdkChecked()) {
				folder.setSelection(appTab);
				page = getWizard().getPage(Messages.tbPg);
				page.setDescription(Messages.dplPageAppMsg);
			} else {
				page = getWizard().getPage(Messages.keyPg);
			}
		}
		inHandlePgComplete = false;
		return page;
	}

	@Override
	public IWizardPage getPreviousPage() {
		int tabIndex = folder.getSelectionIndex();
		IWizardPage page;
		/*
		 * If getNextPage() is called due to
		 * setPageComplete() method
		 * then don't check anything.
		 * Be on same page.
		 */
		if (inHndlPgCmpltBackBtn) {
			folder.setSelection(tabIndex);
			page = getWizard().getPage(Messages.tbPg);
		} else {
			/*
			 * Back button has been clicked.
			 */
			if (tabIndex == 1) {
				page = getWizard().getPage(Messages.tbPg);
				folder.setSelection(jdkTab);
			} else if (tabIndex == 2) {
				page = getWizard().getPage(Messages.tbPg);
				folder.setSelection(srvTab);
			} else {
				page = super.getPreviousPage();
			}
		}
		inHndlPgCmpltBackBtn = false;
		return page;
	}

	/**
	 * Handles the page complete event of deploy page.
	 * Validates all the fields.
	 */
	public void handlePageComplete() {
		boolean isJdkValid = false;
		inHandlePgComplete = true;
		inHndlPgCmpltBackBtn = true;
		// JDK
		if (JdkSrvConfig.getJdkCheckBtn().getSelection()) {
			if (JdkSrvConfig.getTxtJdk().getText().isEmpty()) {
				setPageComplete(false);
			} else {
				File file = new File(
						JdkSrvConfig.getTxtJdk().getText());
				if (!file.exists()) {
					setErrorMessage(Messages.dplWrngJdkMsg);
					setPageComplete(false);
				} else {
					// JDK download group
					if (JdkSrvConfig.getDlCheckBtn().getSelection()) {
						// Validate JDK URL
						if (getJdkUrl().isEmpty()) {
							setErrorMessage(Messages.dlgDlUrlErrMsg);
							setPageComplete(false);
						} else {
							try {
								new URL(getJdkUrl());
								/*
								 * Validate JDK access key.
								 * Space not allowed. 
								 */
								if (!getJdkKey().isEmpty()
										&& getJdkKey().
										trim().contains(" ")) {
									setPageComplete(false);
									setErrorMessage(Messages.dlgDlKeyErrMsg);
								} else {
									String javaHome = JdkSrvConfig.getTxtJavaHome().
											getText().trim();
									if (javaHome.isEmpty()) {
										setPageComplete(false);
										setErrorMessage(Messages.jvHomeErMsg);
									} else {
										/*
										 * access key is optional,
										 * so can be empty.
										 */
										setErrorMessage(null);
										setPageComplete(true);
										isJdkValid = true;
									}
								}
							} catch (MalformedURLException e) {
								setErrorMessage(Messages.dlgDlUrlErrMsg);
								setPageComplete(false);
							}
						}
					} else {
						setErrorMessage(null);
						setPageComplete(true);
						isJdkValid = true;
					}
				}
			}
			// Server
			if (isJdkValid && JdkSrvConfig.getSerCheckBtn().getSelection()) {
				inHandlePgComplete = true;
				inHndlPgCmpltBackBtn = true;
				if (JdkSrvConfig.getComboServer().getText().isEmpty()) {
					setErrorMessage(Messages.dplEmtSerMsg);
					setPageComplete(false);
				} else if (JdkSrvConfig.getTxtDir().getText().isEmpty()) {
					setErrorMessage(Messages.dplEmtSerPtMsg);
					setPageComplete(false);
				} else if (!(new File(JdkSrvConfig.getTxtDir().
						getText()).exists())) {
					setErrorMessage(Messages.dplWrngSerMsg);
					setPageComplete(false);
				} else {
					// Server download group
					if (JdkSrvConfig.
							getDlCheckBtnSrv().getSelection()) {
						if (getSrvUrl().isEmpty()) {
							setErrorMessage(Messages.dlgDlUrlErrMsg);
							setPageComplete(false);
						} else {
							try {
								// Validate Server URL
								new URL(getSrvUrl());
								/*
								 * Validate Server access key.
								 * Space not allowed.
								 */
								if (!getSrvKey().trim().isEmpty()
										&& getSrvKey().
										trim().contains(" ")) {
									setPageComplete(false);
									setErrorMessage(Messages.dlgDlKeyErrMsg);
								} else {
									String srvHome = JdkSrvConfig.getTxtHomeDir().
											getText().trim();
									if (srvHome.isEmpty()) {
										setPageComplete(false);
										setErrorMessage(Messages.srvHomeErMsg);
									} else {
										setErrorMessage(null);
										setPageComplete(true);
									}
								}
							} catch (MalformedURLException e) {
								setErrorMessage(Messages.dlgDlUrlErrMsg);
								setPageComplete(false);
							}
						}
					} else {
						setErrorMessage(null);
						setPageComplete(true);
					}
				}
			}
		} else {
			setPageComplete(true);
		}
	}

	/**
	 * Creates the JDK component.
	 * @param parent : parent container
	 * @return Control
	 */
	Control createJDK(Composite parent) {
		Control control = JdkSrvConfig.createJDKGrp(parent);
		// listener for JDK check button.
		JdkSrvConfig.getJdkCheckBtn().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getJdkCheckBtn().
						getSelection()) {
					JdkSrvConfig.setEnableJDK(true);
					JdkSrvConfig.getTxtJdk().
					setText(WAEclipseHelper.
							jdkDefaultDirectory(null));
					JdkSrvConfig.getSerCheckBtn().
					setEnabled(true);
					JdkSrvConfig.getDlCheckBtn().
					setEnabled(true);
					handlePageComplete();
				} else {
					JdkSrvConfig.
					getSerCheckBtn().setSelection(false);
					JdkSrvConfig.setEnableJDK(false);
					JdkSrvConfig.setEnableServer(false);
					JdkSrvConfig.setEnableDlGrp(false);
					JdkSrvConfig.setEnableDlGrpSrv(false);
					handlePageComplete();
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		JdkSrvConfig.getTxtJdk().setEnabled(false);
		// Modify listener for JDK location text box.
		JdkSrvConfig.getTxtJdk().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				handlePageComplete();
			}
		});

		// Focus listener for JDK location text box.
		JdkSrvConfig.getTxtJdk().
		addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				String jdkPath = JdkSrvConfig.getTxtJdk().getText();
				// Update note below URL text box
				File file = new File(jdkPath);
				if (isJdkDownloadChecked()
						&& !jdkPath.isEmpty()
						&& file.exists()) {
					String dirName = file.getName();
					JdkSrvConfig.getLblDlNoteUrl().
					setText(String.format(
							Messages.dlNtLblDir, dirName));
				} else {
					JdkSrvConfig.getLblDlNoteUrl().
					setText(Messages.dlgDlNtLblUrl);
				}
			}
		});

		JdkSrvConfig.getBtnJdkLoc().setEnabled(false);
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
		// listener for JDK download check box.
		JdkSrvConfig.getDlCheckBtn().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getDlCheckBtn().getSelection()) {
					JdkSrvConfig.setEnableDlGrp(true);
					handlePageComplete();
					// Update note below URL text box
					String jdkPath = JdkSrvConfig.getTxtJdk().
							getText();
					File file = new File(jdkPath);
					if (!jdkPath.isEmpty() && file.exists()) {
						String dirName = file.getName();
						JdkSrvConfig.getLblDlNoteUrl().
						setText(String.format(
								Messages.dlNtLblDir, dirName));
					}
					// set JAVA_HOME text box value
					try {
						String jdkHome = waRole.constructJdkHome(jdkPath,
								new File(WAEclipseHelper.getTemplateFile()));
						JdkSrvConfig.getTxtJavaHome().setText(jdkHome);
					} catch (WindowsAzureInvalidProjectOperationException e) {
						Activator.getDefault().log(e.getMessage());
					}
				} else {
					JdkSrvConfig.setEnableDlGrp(false);
					JdkSrvConfig.getDlCheckBtn().setEnabled(true);
					handlePageComplete();
					// Update note below URL text box
					JdkSrvConfig.getLblDlNoteUrl().
					setText(Messages.dlgDlNtLblUrl);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// listener for JDK URL text.
		JdkSrvConfig.getTxtUrl().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				handlePageComplete();
			}
		});

		// listener for JDK access key text.
		JdkSrvConfig.getTxtKey().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
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

		return control;
	}

	/**
	 * Listener for JDK browse button it is used in file system button.
	 * It will open the file system location.
	 */
	protected void jdkBrowseBtnListener() {
		try {
			String path = WAEclipseHelper.
					jdkDefaultDirectory(
							JdkSrvConfig.getTxtJdk().getText());
			DirectoryDialog dialog =
					new DirectoryDialog(new Shell());
			if (path != null) {
				File file = new File(path);
				if (!path.isEmpty()
						&& file.exists()
						&& file.isDirectory()) {
					dialog.setFilterPath(path);
				}
			}

			String directory = dialog.open();
			if (directory != null) {
				JdkSrvConfig.getTxtJdk().setText(directory);
				// Update note below URL text box
				if (isJdkDownloadChecked()) {
					String dirName = new File(directory).getName();
					JdkSrvConfig.getLblDlNoteUrl().
					setText(String.format(
							Messages.dlNtLblDir, dirName));
				}
			}
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
	}

	/**
	 * Creates the server components.
	 * @param parent : parent container
	 * @return Control
	 */
	Control createServer(Composite parent) {
		Control control = JdkSrvConfig.createServerGrp(parent);
		// listener for Server check button.
		JdkSrvConfig.getSerCheckBtn()
		.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getSerCheckBtn().getSelection()) {
					JdkSrvConfig.getSerCheckBtn().setSelection(true);
					JdkSrvConfig.getDlCheckBtnSrv().setEnabled(true);
					JdkSrvConfig.setEnableServer(true);
					String file = WAEclipseHelper.getTemplateFile();
					File cmpntFile = new File(file);
					try {
						String[] servList =
								WindowsAzureProjectManager.
								getServerTemplateNames(cmpntFile);
						Arrays.sort(servList);
						JdkSrvConfig.getComboServer().setItems(servList);
					} catch (WindowsAzureInvalidProjectOperationException e) {
						Activator.getDefault().log(e.getMessage());
					}
					handlePageComplete();
				} else {
					if (appList.isEmpty()) {
						JdkSrvConfig.getSerCheckBtn().setSelection(false);
						JdkSrvConfig.setEnableServer(false);
						JdkSrvConfig.setEnableDlGrpSrv(false);
						JdkSrvConfig.getSerCheckBtn().setEnabled(true);
						handlePageComplete();
					} else {
						JdkSrvConfig.getSerCheckBtn().setSelection(false);
						JdkSrvConfig.setEnableServer(false);
						JdkSrvConfig.setEnableDlGrpSrv(false);
						JdkSrvConfig.getSerCheckBtn().setEnabled(true);
						handlePageComplete();
					}
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		// Modify listener for Server location text box.
		JdkSrvConfig.getTxtDir().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				handlePageComplete();
			}
		});

		// Focus listener for Server location text box.
		JdkSrvConfig.getTxtDir().
		addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				// Update note below URL text box
				String path = JdkSrvConfig.
						getTxtDir().getText().trim();
				File file = new File(path);
				if (JdkSrvConfig.getDlCheckBtnSrv().
						getSelection()
						&& !path.isEmpty()
						&& file.exists()) {
					String dirName = file.getName();
					JdkSrvConfig.getLblDlNoteUrlSrv().
					setText(String.format(
							Messages.dlNtLblDir, dirName));
				} else {
					JdkSrvConfig.getLblDlNoteUrlSrv().
					setText(Messages.dlgDlNtLblUrl);
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});

		// listener for Server browse button.
		JdkSrvConfig.getBtnSrvLoc().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				serBrowseBtnListener();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// listener for Server type combo box.
		JdkSrvConfig.getComboServer().
		addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				handlePageComplete();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		// listener for Server customize link.
		JdkSrvConfig.getCustLink()
		.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				serBtnListener();
			}
		});

		// Server download group
		// listener for Server download check box.
		JdkSrvConfig.getDlCheckBtnSrv().
		addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (JdkSrvConfig.getDlCheckBtnSrv()
						.getSelection()) {
					JdkSrvConfig.setEnableDlGrpSrv(true);
					handlePageComplete();
					// Update note below server URL text box
					String srvPath = JdkSrvConfig.getTxtDir().
							getText();
					File file = new File(srvPath);
					if (!srvPath.isEmpty() && file.exists()) {
						String dirName = file.getName();
						JdkSrvConfig.getLblDlNoteUrlSrv().
						setText(String.format(
								Messages.dlNtLblDir, dirName));
					}
					// set server home directory text box value
					try {
						String srvHome = waRole.constructServerHome(
								JdkSrvConfig.getComboServer().getText(),
								srvPath,
								new File(WAEclipseHelper.getTemplateFile()));
						JdkSrvConfig.getTxtHomeDir().setText(srvHome);
					} catch (WindowsAzureInvalidProjectOperationException e) {
						Activator.getDefault().log(e.getMessage());
					}
				} else {
					JdkSrvConfig.setEnableDlGrpSrv(false);
					JdkSrvConfig.getDlCheckBtnSrv().setEnabled(true);
					handlePageComplete();
					// Update note below server URL text box
					JdkSrvConfig.getLblDlNoteUrlSrv().
					setText(Messages.dlgDlNtLblUrl);
				}
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
				handlePageComplete();
			}
		});

		// listener for Server access key text box.
		JdkSrvConfig.getTxtKeySrv().
		addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
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

		return control;
	}

	/**
	 * Listener for server browse button it is used in file system button.
	 * It will open the file system location.
	 */
	protected void serBrowseBtnListener() {
		try {
			String path = JdkSrvConfig.getTxtDir().getText();
			DirectoryDialog dialog =
					new DirectoryDialog(new Shell());
			if (path != null) {
				File file = new File(path);
				if (!path.isEmpty()
						&& file.exists()
						&& file.isDirectory()) {
					dialog.setFilterPath(path);
				}
			}
			String directory = dialog.open();
			if (directory != null) {
				JdkSrvConfig.getTxtDir().setText(directory);
				// Update note below server URL text box
				if (isSrvDownloadChecked()) {
					String dirName = new File(directory).getName();
					JdkSrvConfig.getLblDlNoteUrlSrv().
					setText(String.format(
							Messages.dlNtLblDir, dirName));
				}
				// Auto detect server family
				String serverName = WAEclipseHelper.
						detectServer(new File(directory));
				if (serverName != null
						&& !serverName.isEmpty()) {
					JdkSrvConfig.getComboServer().
					setText(serverName);
				} else {
					JdkSrvConfig.getComboServer().
					clearSelection();
				}
			}
			handlePageComplete();
		} catch (Exception e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
	}

	/**
	 * Creates the application table component.
	 * @param parent : container
	 * @return
	 */
	Control createAppTblCmpnt(Composite parent) {
		Control control = JdkSrvConfig.createAppTbl(parent);
		JdkSrvConfig.getTableViewer()
		.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer arg0,
					Object arg1, Object arg2) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object arg0) {
				return getAppsAsNames().toArray();
			}
		});

		JdkSrvConfig.getTableViewer()
		.setLabelProvider(new ITableLabelProvider() {
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

		JdkSrvConfig.getTableViewer().
		setInput(getAppsAsNames());

		// Add selection listener for Add Button
		JdkSrvConfig.getBtnAdd()
		.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addButtonListener();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		JdkSrvConfig.getTblApp().
		addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				JdkSrvConfig.getBtnRemove().
				setEnabled(true);
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

		// By default disable server component
		// as JDK not selected.
		if (isJdkChecked()) {
			JdkSrvConfig.setEnableServer(true);
		} else {
			JdkSrvConfig.setEnableServer(false);
		}
		return control;
	}

	/**
	 * Add Application button listener.
	 */
	private void addButtonListener() {
		WAApplicationDialog dialog = new WAApplicationDialog(getShell(),
				this, waRole, null);
		dialog.open();
		JdkSrvConfig.getTableViewer().refresh();
	}

	/**
	 * Remove application button listener.
	 */
	private void removeButtonListener() {
		int selIndex = JdkSrvConfig.getTableViewer()
				.getTable().getSelectionIndex();
		if (selIndex > -1) {
			try {
				appList.remove(selIndex);
				JdkSrvConfig.getTableViewer().
				refresh();
			} catch (Exception e) {
				Activator.getDefault().log(e.getMessage(), e);
			}
		}
	}

	/**
	 * Server button listener. This will close the wizard and open the
	 * componentssets.xml in default editor.
	 */
	private void serBtnListener() {
		boolean choice = MessageDialog.openConfirm(getShell(),
				Messages.dplSerBtnTtl, Messages.dplSerBtnMsg);
		if (choice) {
			try {
				if (isWizard) {
					WAProjectWizard wiz =
							(WAProjectWizard) pageObj;
					wiz.getShell().close();
				}
				String file = WAEclipseHelper.getTemplateFile();
				File cmpntFile = new File(file);
				if (cmpntFile.exists() && cmpntFile.isFile()) {
					IFileStore store = EFS.getLocalFileSystem().
							getStore(cmpntFile.toURI());
					IWorkbenchPage benchPage = PlatformUI.
							getWorkbench().getActiveWorkbenchWindow()
							.getActivePage();
					IDE.openEditorOnFileStore(benchPage, store);
				}
			} catch (PartInitException e) {
				Activator.getDefault().log(e.getMessage(), e);
			}
		}
	}

	/**
	 * Gives server installation location specified by user.
	 * @return server home location
	 */
	public String getServerLoc() {
		return JdkSrvConfig.getTxtDir().getText().trim();
	}

	/**
	 * Gives server name selected by user.
	 * @return serverName
	 */
	public String getServerName() {
		return JdkSrvConfig.getComboServer().getText();
	}

	/**
	 * Return whether Server check box is checked or not.
	 * @return boolean
	 */
	public static boolean isSrvChecked() {
		return JdkSrvConfig.getSerCheckBtn().getSelection();
	}


	/**
	 * Adds the application to the application list.
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
	 * @return added application Asnames which is to be set in table.
	 */
	public ArrayList<String> getAppsAsNames() {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < appList.size(); i++) {
			list.add(appList.get(i).getImpAs());
		}
		return list;
	}

	/**
	 * @return applist
	 */
	public ArrayList<AppCmpntParam> getAppsList() {
		return appList;
	}

	/**
	 * Gives JDK location specified by user.
	 * @return JDK home location
	 */
	public String getJdkLoc() {
		return JdkSrvConfig.getTxtJdk().getText().trim();
	}

	/**
	 * Return JDK URL specified by user.
	 * @return JDK URL
	 */
	public String getJdkUrl() {
		return JdkSrvConfig.getTxtUrl().getText().trim();
	}

	/**
	 * Return JDK access key specified by user.
	 * @return JDK access key
	 */
	public String getJdkKey() {
		return JdkSrvConfig.getTxtKey().getText().trim();
	}

	/**
	 * Return whether JDK check box is checked or not.
	 * @return boolean
	 */
	public static boolean isJdkChecked() {
		return JdkSrvConfig.getJdkCheckBtn().getSelection();
	}

	/**
	 * Return whether JDK download group
	 * check box is checked or not.
	 * @return
	 */
	public static boolean isJdkDownloadChecked() {
		return JdkSrvConfig.getDlCheckBtn().getSelection();
	}

	/**
	 * Return Server URL specified by user.
	 * @return Server URL
	 */
	public String getSrvUrl() {
		return JdkSrvConfig.getTxtUrlSrv().getText().trim();
	}

	/**
	 * Return Java Home specified by user.
	 * @return
	 */
	public String getJavaHome() {
		return JdkSrvConfig.getTxtJavaHome().getText().trim();
	}

	/**
	 * Return Server Home specified by user.
	 * @return
	 */
	public String getSrvHomeDir() {
		return JdkSrvConfig.getTxtHomeDir().getText().trim();
	}

	/**
	 * Return Server access key specified by user.
	 * @return server access key
	 */
	public String getSrvKey() {
		return JdkSrvConfig.getTxtKeySrv().getText().trim();
	}

	/**
	 * Return whether Server download group
	 * check box is checked or not.
	 * @return
	 */
	public static boolean isSrvDownloadChecked() {
		return JdkSrvConfig.getDlCheckBtnSrv().getSelection();
	}

	/**
	 * Method returns Tabfolder.
	 * @return
	 */
	public static TabFolder getFolder() {
		return folder;
	}

	/**
	 * Method returns JDK tab.
	 * @return
	 */
	public static TabItem getJdkTab() {
		return jdkTab;
	}
}
