/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.gigaspaces.azure.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.model.HostedService;
import com.gigaspaces.azure.model.HostedServices;
import com.gigaspaces.azure.model.KeyName;
import com.gigaspaces.azure.model.StorageService;
import com.gigaspaces.azure.model.StorageServices;
import com.gigaspaces.azure.model.Subscription;
import com.gigaspaces.azure.propertypage.SubscriptionPropertyPage;
import com.gigaspaces.azure.rest.WindowsAzureRestUtils;
import com.gigaspaces.azure.runnable.AccountActionRunnable;
import com.gigaspaces.azure.runnable.CacheAccountWithProgressBar;
import com.gigaspaces.azure.runnable.LoadAccountWithProgressBar;
import com.gigaspaces.azure.util.CommandLineException;
import com.gigaspaces.azure.util.MethodUtils;
import com.gigaspaces.azure.util.PreferenceUtil;
import com.gigaspaces.azure.util.PublishData;
import com.gigaspaces.azure.util.UIUtils;
import com.interopbridges.tools.windowsazure.OSFamilyType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.wacommon.commoncontrols.ImportSubscriptionDialog;
import com.microsoftopentechnologies.wacommon.storageregistry.PreferenceUtilStrg;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import com.persistent.ui.propertypage.WARemoteAccessPropertyPage;
import com.persistent.util.MessageUtil;
import com.persistent.util.WAEclipseHelper;
/**
 * Class creates UI components and their respective
 * listeners of publish wizard page.
 */
public class SignInPage extends WindowsAzurePage {

	private Combo storageAccountCmb;
	private Combo hostedServiceCombo;
	private Combo deployStateCmb;
	private Combo targetOS;
	private String deployState;
	private String deployFileName;
	private String deployConfigFileName;
	private HostedService currentHostedService;
	private PublishData publishData;
	private StorageService currentStorageAccount;
	private WindowsAzurePackageType deployMode = WindowsAzurePackageType.CLOUD;
	private Combo subscriptionCombo;
	private Link subLink;
	private Button newHostedServiceBtn;
	private Button newStorageAccountBtn;
	private String defaultLocation;
	private IProject selectedProject;
	private Button btnImpFrmPubSetFile;
	// Remote Access group
	private Group rdGrp;
	private Label userNameLabel;
	private Label passwordLabel;
	private Label confirmPasswordLbl;
	private Text txtUserName;
	private Text txtPassword;
	private Text txtConfirmPassword;
	private Link encLink;
	private WindowsAzureProjectManager waProjManager;
	private boolean isPwdChanged;
	private Button conToDplyChkBtn;
	public ArrayList<String> newServices = new ArrayList<String>();
	private Button unpublishChBox;

	/**
	 * Constructor.
	 */
	protected SignInPage() {
		super(Messages.deplWizTitle);
		setTitle(Messages.deplWizTitle);
	}
	/**
	 * Returns currently selected project.
	 * @param project
	 */
	public void setSelectedProject(IProject project) {
		this.selectedProject = project;
	}

	@Override
	public void createControl(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().
		setHelp(parent, Messages.pluginPrefix + Messages.publishCommandHelp);
		loadProject();

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		GridData gridData = createGridData(0, 0, 0);
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		setEnabledState(container, true);
		// create Import From Publish settings file button
		createImportBtnCmpnt(container);
		ImportSubscriptionDialog.
		createHorizontalSeparator(container);
		createSubscriptionIdWidget(container);
		createStorageAccountWidget(container);
		createHostedServiceWidget(container);
		createTargetOSWidget(container);
		createDeploymentStateWidget(container);
		createUnpublishCheckBox(container);
		createRemoteDesktopWidget(container);

		setControl(container);
		setComponentState((subscriptionCombo.
				getData(subscriptionCombo.getText()) != null));
		loadDefaultRDPValues();
		//Set current value for target OS
		try {
			targetOS.setText(waProjManager.getOSFamily().getName());
		} catch (WindowsAzureInvalidProjectOperationException e) {
			Activator.getDefault().log(Messages.error, e);
		}
		setPageComplete(validatePageComplete());
	}

	/**
	 * Initialize {@link WindowsAzureProjectManager} object
	 * according to selected project.
	 */
	private void loadProject() {
		try {
			String projectPath = selectedProject.getLocation().toPortableString();
			File projectDir = new File(projectPath);
			waProjManager = WindowsAzureProjectManager.load(projectDir);
		} catch (Exception e) {
			Activator
			.getDefault()
			.log(Messages.projLoadEr, e);
		}
	}

	private void doLoadPreferences() {
		try {
			getContainer().run(true, true,
					new LoadAccountWithProgressBar(null,
							this.getShell()));
		} catch (InvocationTargetException e) {
			MessageUtil.displayErrorDialog(getShell(),
					Messages.importDlgTitle,e.getMessage());
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Method loads configured remote access values
	 * on wizard page.
	 */
	private void loadDefaultRDPValues() {
		try {
			// to update project manager object
			loadProject();
			String uname = waProjManager.getRemoteAccessUsername();
			if (uname != null && !uname.isEmpty()) {
				txtUserName.setText(uname);
				try {
					String pwd = waProjManager.
							getRemoteAccessEncryptedPassword();
					/*
					 * If its dummy password,
					 * then do not show it on UI
					 */
					if (pwd.equals(Messages.remAccDummyPwd)
							|| pwd.isEmpty()) {
						txtPassword.setText("");
						txtConfirmPassword.setText("");
					} else {
						txtPassword.setText(pwd);
						txtConfirmPassword.setText(pwd);
					}
					setEnableRemAccess(true);
				} catch (Exception e) {
					txtPassword.setText("");
					txtConfirmPassword.setText("");
				}
			} else {
				txtUserName.setText("");
				setEnableRemAccess(false);
			}
		} catch (Exception e) {
			txtUserName.setText("");
			setEnableRemAccess(false);
		}
	}

	/**
	 * Enable or disable password fields.
	 * @param status
	 */
	private void setEnableRemAccess(boolean status) {
		txtPassword.setEnabled(status);
		txtConfirmPassword.setEnabled(status);
		passwordLabel.setEnabled(status);
		confirmPasswordLbl.setEnabled(status);
		conToDplyChkBtn.setEnabled(status);
		if (!status) {
			txtPassword.setText("");
			txtConfirmPassword.setText("");
			conToDplyChkBtn.setSelection(false);
		}
	}

	/**
	 * Enable or disable components related to
	 * publish settings.
	 * @param enabled
	 */
	private void setComponentState(boolean enabled) {
		subscriptionCombo.setEnabled(enabled);
		storageAccountCmb.setEnabled(enabled);
		newStorageAccountBtn.setEnabled(enabled);
		hostedServiceCombo.setEnabled(enabled);
		targetOS.setEnabled(enabled);
		if (!enabled) {
			hostedServiceCombo.removeAll();
			storageAccountCmb.removeAll();
			unpublishChBox.setSelection(enabled);
		}
		deployStateCmb.setEnabled(enabled);
		newHostedServiceBtn.setEnabled(enabled);
		unpublishChBox.setEnabled(enabled);
	}

	private void setEnabledState(Composite composite,
			boolean enabledState) {
		composite.setEnabled(enabledState);

		for (Control child : composite.getChildren()) {
			if (child instanceof Composite) {
				setEnabledState((Composite) child, enabledState);
			}
			child.setEnabled(enabledState);

		}
	}

	@Override
	public void setVisible(boolean visible) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!PreferenceUtil.isLoaded()) {
					doLoadPreferences();
					// reload information if its new eclipse session.
					PreferenceUtilStrg.load();
					MethodUtils.prepareListFromPublishData();
					PreferenceUtil.setLoaded(true);
					subscriptionCombo = UIUtils.
							populateSubscriptionCombo(subscriptionCombo);
					if ((subscriptionCombo.
							getData(subscriptionCombo.getText()) != null)) {
						unpublishChBox.setSelection(true);
						setPageComplete(validatePageComplete());
					}
				}
			}
		});

		if (visible) {
			subscriptionCombo = UIUtils.
					populateSubscriptionCombo(subscriptionCombo);
		}
		super.setVisible(visible);
	}

	@Override
	protected boolean validatePageComplete() {

		setErrorMessage(null);
		if (deployMode.equals(WindowsAzurePackageType.LOCAL)) {
			return true;
		}

		if (publishData == null) {
			setErrorMessage(Messages.deplFillSubsciptionId);
			return false;
		}

		if (deployState == null) {
			return false;
		}

		if (currentStorageAccount == null) {
			setErrorMessage(Messages.deplFillStorageAcc);
			return false;
		}

		if (currentHostedService == null) {
			setErrorMessage(Messages.deplFillHostedServiceMsg);
			return false;
		}
		/*
		 * Validation for remote access settings.
		 */
		if (!txtUserName.getText().isEmpty()) {
			String pwd = txtPassword.getText();
			if (pwd == null || pwd.isEmpty()) {
				// password is empty
				setErrorMessage(Messages.rdpPasswordEmpty);
				return false;
			} else {
				String confirm = txtConfirmPassword.getText();
				if (confirm == null || confirm.isEmpty()) {
					// confirm password is empty
					setErrorMessage(Messages.rdpConfirmPasswordEmpty);
					return false;
				} else {
					if (!pwd.equals(confirm)) {
						// password and confirm password do not match.
						setErrorMessage(Messages.rdpPasswordsDontMatch);
						return false;
					}
				}
			}
		}

		setErrorMessage(null);

		fireConfigurationEvent(new ConfigurationEventArgs(this,
				ConfigurationEventArgs.DEPLOY_MODE, deployMode));

		fireConfigurationEvent(new ConfigurationEventArgs(this,
				ConfigurationEventArgs.SUPSCRIPTION, publishData));

		fireConfigurationEvent(new ConfigurationEventArgs(this,
				ConfigurationEventArgs.STORAGE_ACCOUNT,
				currentStorageAccount));
		// Always set key to primary
		fireConfigurationEvent(new ConfigurationEventArgs(this,
				ConfigurationEventArgs.STORAGE_ACCESS_KEY,
				KeyName.Primary.toString()));

		fireConfigurationEvent(new ConfigurationEventArgs(this,
				ConfigurationEventArgs.DEPLOY_STATE,
				deployState));

		fireConfigurationEvent(new ConfigurationEventArgs(this,
				ConfigurationEventArgs.UN_PUBLISH,
				Boolean.valueOf(unpublishChBox.getSelection()).toString()));

		deployFileName = constructCspckFilePath();

		deployConfigFileName = constructCscfgFilePath();

		fireConfigurationEvent(new ConfigurationEventArgs(this,
				ConfigurationEventArgs.DEPLOY_FILE,
				deployFileName));

		fireConfigurationEvent(new ConfigurationEventArgs(this,
				ConfigurationEventArgs.DEPLOY_CONFIG_FILE,
				deployConfigFileName));

		fireConfigurationEvent(new ConfigurationEventArgs(this,
				ConfigurationEventArgs.HOSTED_SERVICE,
				currentHostedService));

		return true;
	}

	/**
	 * Method returns target OS selected by user.
	 * @return
	 */
	public String getTargetOSName() {
		return targetOS.getText();
	}

	/**
	 * Method returns user name
	 * entered by user.
	 * @return
	 */
	public String getRdpUname() {
		return txtUserName.getText().trim();
	}

	/**
	 * Method returns password entered by user.
	 * @return
	 */
	public String getRdpPwd() {
		return txtPassword.getText().trim();
	}

	/**
	 * Method returns state of connect check box.
	 * @return
	 */
	public boolean getConToDplyChkStatus() {
		return conToDplyChkBtn.getSelection();
	}

	/**
	 * Method returns currently selected hosted service.
	 * @return
	 */
	public String getCurrentService() {
		return hostedServiceCombo.getText();
	}

	/**
	 * Method returns currently selected storage account name.
	 * @return
	 */
	public StorageService getCurrentStorageAccount() {
		return currentStorageAccount;
	}

	/**
	 * Method returns new services names, if created by user.
	 * @return
	 */
	public ArrayList<String> getNewServices() {
		return newServices;
	}

	/**
	 * Method creates UI components
	 * of remote access group.
	 * @param parent
	 */
	private void createRemoteDesktopWidget(Composite parent) {
		rdGrp = new Group(parent, SWT.SHADOW_ETCHED_IN);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		GridData gridData = createGridData(0, 5, 3);
		gridData.horizontalSpan = 3;
		rdGrp.setText(Messages.rmtAccLbl);
		rdGrp.setLayout(gridLayout);
		rdGrp.setLayoutData(gridData);
		createUsernameComponent(rdGrp);
		createPasswordComponent(rdGrp);
		createConfPwdComponent(rdGrp);
		createConnectToDeployBtn(rdGrp);
	}

	/**
	 * Method creates check box
	 * of Connect to deployment when ready.
	 * @param container
	 */
	private void createConnectToDeployBtn(Composite container) {
		conToDplyChkBtn = new Button(container, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.verticalIndent = 10;
		gridData.horizontalSpan = 3;
		conToDplyChkBtn.setText(Messages.conDplyLbl);
		conToDplyChkBtn.setLayoutData(gridData);
		conToDplyChkBtn.setSelection(false);
	}

	/**
	 * Method creates UI components
	 * and listener for user name.
	 * @param container
	 */
	private void createUsernameComponent(Composite container) {
		userNameLabel = new Label(container, SWT.LEFT);
		userNameLabel.setText(Messages.remAccUserName);
		txtUserName = new Text(container,
				SWT.LEFT | SWT.BORDER);
		GridData gridData = createGridData(300, 0, 33);
		txtUserName.setLayoutData(gridData);

		txtUserName.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				if (txtUserName.getText().isEmpty()) {
					setEnableRemAccess(false);
				} else {
					setEnableRemAccess(true);
				}
				setPageComplete(validatePageComplete());
			}
		});

		new Link(container, SWT.NO);
	}

	/**
	 * Method creates UI components
	 * and listener for password and encryption link.
	 * @param container
	 */
	private void createPasswordComponent(Composite container) {
		passwordLabel = new Label(container, SWT.LEFT);
		passwordLabel.setText(Messages.certDlgPwdLbl);
		GridData gridData = new GridData();
		gridData.verticalIndent = 10;
		passwordLabel.setLayoutData(gridData);

		txtPassword = new Text(container,
				SWT.LEFT | SWT.PASSWORD | SWT.BORDER);
		gridData = createGridData(300, 10, 33);
		txtPassword.setLayoutData(gridData);

		/*
		 * Listener for key event when user click on password text box
		 * it will set flag for entering the new values.
		 */
		txtPassword.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent event) {
			}

			@Override
			public void keyPressed(KeyEvent event) {
				isPwdChanged = true;
			}
		});

		/*
		 * Listener for handling focus event on password text box on focus gain
		 * text box will blank.on focus lost we will be checking for strong
		 * password. If password has not changed then we will display old
		 * password only.
		 */
		txtPassword.addFocusListener(new PasswordFocusListener());

		txtPassword.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				setPageComplete(validatePageComplete());
			}
		});

		encLink = new Link(container, SWT.RIGHT);
		gridData = createGridData(60, 10, 10);
		encLink.setText(Messages.linkLblEnc);
		encLink.setLayoutData(gridData);
		encLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				encLinkListener();
			}
		});
	}

	/**
	 * Focus listener for password text box.
	 */
	private class PasswordFocusListener implements FocusListener {

		@Override
		//making text box blank on focus gained.
		public void focusGained(FocusEvent arg0) {
			txtPassword.setText("");
		}

		@Override
		public void focusLost(FocusEvent arg0) {
			WAEclipseHelper.checkRdpPwd(isPwdChanged,
					txtPassword, waProjManager,
					false, txtConfirmPassword);
		}
	}

	/**
	 * Listener for Encryption link.
	 * Stores values entered by user before opening
	 * remote access property page and shows saved values.
	 */
	private void encLinkListener() {
		// Make remote access settings
		Activator.getDefault().setIsFromEncLink(true);
		Activator.getDefault().setPubUname(
				txtUserName.getText().trim());
		Activator.getDefault().setPubPwd(
				txtPassword.getText().trim());
		Activator.getDefault().setPubCnfPwd(
				txtConfirmPassword.getText().trim());
		// open remote access dialog
		Object remoteAccess =
				new WARemoteAccessPropertyPage();
		int btnId = WAEclipseHelper.
				openPropertyPageDialog(
						com.persistent.util.Messages.cmhIdRmtAces,
						com.persistent.util.Messages.cmhLblRmtAces,
						remoteAccess);
		if (btnId == Window.OK) {
			loadDefaultRDPValues();
			/*
			 * To handle the case, if you typed
			 * password on Publish wizard --> Encryption link
			 * Remote access --> OK --> Toggle password text boxes
			 */
			isPwdChanged = false;
		}
	}
	/**
	 * Method creates UI components
	 * and listener for confirm password.
	 * @param container
	 */
	private void createConfPwdComponent(Composite container) {
		confirmPasswordLbl = new Label(container, SWT.LEFT);
		confirmPasswordLbl.setText(Messages.certDlgConfPwdLbl);
		GridData gridData = new GridData();
		gridData.verticalIndent = 10;
		confirmPasswordLbl.setLayoutData(gridData);

		txtConfirmPassword = new Text(container,
				SWT.LEFT | SWT.PASSWORD | SWT.BORDER);
		gridData = createGridData(300, 10, 33);
		txtConfirmPassword.setLayoutData(gridData);

		txtConfirmPassword.addFocusListener(new FocusListener() {
	            @Override
	            public void focusLost(FocusEvent event) {
	                try {
	                    if (!isPwdChanged) {
	                        if (txtPassword.getText().isEmpty()) {
	                        	txtConfirmPassword.setText("");
	                        } else {
	                        	txtConfirmPassword.setText(waProjManager.
	                                    getRemoteAccessEncryptedPassword());
	                        }
	                    }
	                } catch (WindowsAzureInvalidProjectOperationException e1) {
	                    PluginUtil.displayErrorDialogAndLog(getShell(),
	                    		Messages.remAccErrTitle,
	                    		Messages.remAccErPwd, e1);
	                }
	            }

	            @Override
	            public void focusGained(FocusEvent event) {
	            	txtConfirmPassword.setText("");
	            }
	        });

		txtConfirmPassword.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				setPageComplete(validatePageComplete());
			}
		});

		new Link(container, SWT.NO);
	}

	/**
	 * Method creates Import From Publish Settings File
	 * button and add listener to it.
	 * @param parent
	 */
	private void createImportBtnCmpnt(Composite parent) {
		btnImpFrmPubSetFile = UIUtils.
				createImportFromPublishSettingsFileBtn(parent, 3);
		btnImpFrmPubSetFile.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				ImportSubscriptionDialog dlg = new
						ImportSubscriptionDialog(new Shell());
				dlg.open();
				String fileName = ImportSubscriptionDialog.
						getPubSetFilePath();
				importBtn(fileName);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
	}

	/**
	 * Method creates UI components
	 * and listener for storage account.
	 * @param container
	 */
	private void createStorageAccountWidget(Composite container) {
		createLabel(container, Messages.deplStorageAccLbl);

		storageAccountCmb = createCombo(
				container, SWT.READ_ONLY, 10, SWT.FILL, 150);

		GridData gridData = createGridData(80, 10, 5);

		newStorageAccountBtn = new Button(container, SWT.PUSH);
		newStorageAccountBtn.setLayoutData(gridData);
		newStorageAccountBtn.setText(Messages.newBtn);

		storageAccountCmb.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				String storageAccount = ((Combo) e.getSource()).getText();
				currentStorageAccount = (StorageService) ((Combo) e.getSource())
						.getData(storageAccount);
				setPageComplete(validatePageComplete());
			}
		});

		newStorageAccountBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String subscription = subscriptionCombo.getText();

				if (subscription != null && !subscription.isEmpty()) {

					PublishData publishData = (PublishData) subscriptionCombo
							.getData(subscription);

					int maxStorageAccounts = publishData
							.getCurrentSubscription().getMaxStorageAccounts();

					String currentSubscriptionId = publishData
							.getCurrentSubscription().getId();
					if (maxStorageAccounts > publishData.getStoragesPerSubscription().get(currentSubscriptionId).size()) {

						NewStorageAccountDialog storageAccountDialog =
								new NewStorageAccountDialog(getShell(), subscription);
						if (defaultLocation != null) { // user has created a hosted service before a storage account
							storageAccountDialog.setDefaultLocation(defaultLocation);
						}
						int result = storageAccountDialog.open();

						if (result == 0) {

							populateStorageAccounts();
							storageAccountCmb = UIUtils.selectByText(
									storageAccountCmb,
									storageAccountDialog.getStorageAccountName());
							defaultLocation = WizardCacheManager.getStorageAccountFromCurrentPublishData(storageAccountDialog.getStorageAccountName()).getStorageServiceProperties().getLocation();
						}
					} else {
						MessageUtil.displayErrorDialog(getShell(),Messages.storageAccountsLimitTitle,Messages.storageAccountsLimitErr);
					}
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		populateStorageAccounts();

		if (storageAccountCmb.getItemCount() > 0)
			storageAccountCmb.select(0);
	}

	/**
	 * Method creates UI components
	 * and listener for subscriptions.
	 * @param container
	 */
	private void createSubscriptionIdWidget(Composite container) {
		createLabel(container, Messages.deplSubscriptionLbl);

		subscriptionCombo = createCombo(
				container, SWT.READ_ONLY, 10, SWT.FILL, 150);
		subscriptionCombo = UIUtils.
				populateSubscriptionCombo(subscriptionCombo);

		subscriptionCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				publishData = UIUtils.changeCurrentSubAsPerCombo((Combo) e.getSource());
				if (storageAccountCmb != null && publishData != null) {
					populateStorageAccounts();
					populateHostedServices();
					setComponentState((subscriptionCombo.
							getData(subscriptionCombo.getText()) != null));
				}
				setPageComplete(validatePageComplete());
			}
		});

		if (subscriptionCombo.getItemCount() > 0)
			subscriptionCombo.select(0);

		GridData gridData = createGridData(80, 10, 10);

		subLink = new Link(container, SWT.RIGHT);
		subLink.setText(Messages.linkLblSub);
		subLink.setLayoutData(gridData);
		subLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Object subscription = new SubscriptionPropertyPage();
				WAEclipseHelper.
				openPropertyPageDialog(
						com.persistent.util.Messages.cmhIdCrdntls,
						com.persistent.util.Messages.cmhLblSubscrpt,
						subscription);
				/*
				 * Update data in every case.
				 * No need to check which button (OK/Cancel)
				 * has been pressed as change is permanent
				 * even though user presses cancel
				 * according to functionality.
				 */
				doLoadPreferences();
				subscriptionCombo = UIUtils.
						populateSubscriptionCombo(subscriptionCombo);
				// update cache of publish data object
				publishData = (PublishData) subscriptionCombo
						.getData(subscriptionCombo.getText());
				// enable and disable components.
				setComponentState((subscriptionCombo.
						getData(subscriptionCombo.getText()) != null));
				setPageComplete(validatePageComplete());
			}
		});
	}

	private Label createLabel(Composite container, String text) {
		Label label = new Label(container, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 3;
		gridData.verticalIndent = 10;
		label.setLayoutData(gridData);
		label.setText(text);
		return label;
	}

	private GridData createGridData(int width,
			int verInd,
			int horInd) {
		GridData gridData = new GridData();
		gridData.widthHint = width;
		gridData.verticalIndent = verInd;
		gridData.horizontalIndent = horInd;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		return gridData;
	}

	/**
	 * Method creates UI components
	 * and listener for services.
	 * @param container
	 */
	private void createHostedServiceWidget(Composite container) {
		createLabel(container, Messages.deplHostedServiceLbl);

		hostedServiceCombo = createCombo(
				container, SWT.READ_ONLY, 10, SWT.FILL, 150);

		populateHostedServices();

		hostedServiceCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				String hostedServiceName = ((Combo) e.getSource()).getText();
				currentHostedService = (HostedService) ((Combo) e.getSource()).getData(hostedServiceName);
				setPageComplete(validatePageComplete());
			}
		});

		GridData gridData = createGridData(80, 10, 5);

		newHostedServiceBtn = new Button(container, SWT.PUSH | SWT.CENTER);
		newHostedServiceBtn.setLayoutData(gridData);
		newHostedServiceBtn.setText(Messages.newBtn);

		newHostedServiceBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String subscription = subscriptionCombo.getText();

				if (subscription != null && !subscription.isEmpty()) {

					PublishData publishData = (PublishData) subscriptionCombo
							.getData(subscription);

					int maxHostedServices = publishData
							.getCurrentSubscription().getMaxHostedServices();

					String currentSubscriptionId = publishData
							.getCurrentSubscription().getId();
					if (maxHostedServices > publishData.getServicesPerSubscription().get(currentSubscriptionId).size()) {

						NewHostedServiceDialog hostedService = new NewHostedServiceDialog(
								getShell());
						if (defaultLocation != null) { // user has created a storage account before creating the hosted service
							hostedService.setDefaultLocation(defaultLocation);
						}

						int result = hostedService.open();

						if (result == 0) {
							populateHostedServices();
							newServices.add(hostedService.getHostedServiceName());
							hostedServiceCombo = UIUtils.
									selectByText(hostedServiceCombo,
											hostedService.getHostedServiceName());
							defaultLocation = WizardCacheManager.getHostedServiceFromCurrentPublishData(hostedService.getHostedServiceName()).getHostedServiceProperties().getLocation();
						}
					} else {
						MessageUtil.displayErrorDialog(getShell(),
								Messages.hostServLimitTitle,
								Messages.hostServLimitErr);
					}
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	public void createTargetOSWidget(Composite container) {
		createLabel(container,
				com.persistent.ui.propertypage.Messages.proPageTgtOSLbl);
		String[] targetOSType =
			{OSFamilyType.WINDOWS_SERVER_2008_R2.getName(),
				OSFamilyType.WINDOWS_SERVER_2012.getName()};
		targetOS = createCombo(
				container, SWT.READ_ONLY, 10, SWT.FILL, 300);
		targetOS.setItems(targetOSType);
		new Link(container, SWT.NO);
	}

	public void populateHostedServices() {
		if (publishData != null) {
			String currentSelection = hostedServiceCombo.getText();
			Subscription currentSubscription = publishData.getCurrentSubscription();
			HostedServices hostedServices = publishData.getServicesPerSubscription().
					get(currentSubscription.getId());
			hostedServiceCombo.removeAll();
			if (hostedServices != null && !hostedServices.isEmpty()) {

				for (HostedService hsd : hostedServices) {
					hostedServiceCombo.add(hsd.getServiceName());
					hostedServiceCombo.setData(hsd.getServiceName(), hsd);
				}
				hostedServiceCombo = UIUtils.selectByText(
						hostedServiceCombo, currentSelection);
			}
		}
	}

	/**
	 * Method creates UI components
	 * and listener for deployment state.
	 * @param container
	 */
	private void createDeploymentStateWidget(Composite container) {
		String[] items = { Messages.deplStaging, Messages.deplProd };
		createLabel(container, Messages.deplState);
		deployState = getFirstItem(items);

		deployStateCmb = createCombo(
				container, SWT.READ_ONLY, 10, SWT.FILL, 300);

		deployStateCmb.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				deployState = ((Combo) e.getSource()).getText();
				if (deployState.equalsIgnoreCase(Messages.deplProd)) {
					unpublishChBox.setSelection(false);
				}
				setPageComplete(validatePageComplete());
			}
		});

		if (items != null) {
			deployStateCmb.setItems(items);
			if (items.length > 0) {
				deployStateCmb.select(0);
			}
		}

		new Link(container, SWT.NO);
	}

	private void createUnpublishCheckBox(Composite container) {
		new Link(container, SWT.NO);
		GridData gridData = new GridData();
		gridData.verticalIndent = 10;
		unpublishChBox = new Button(container, SWT.CHECK);
		unpublishChBox.setText(Messages.unpubPrvDply);
		unpublishChBox.setLayoutData(gridData);
		// by default target environment will be staging
		unpublishChBox.setSelection(true);
		new Link(container, SWT.NO);

		unpublishChBox.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setPageComplete(validatePageComplete());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	private String getFirstItem(String[] items) {
		return items != null && items.length > 0 ? items[0] : null;
	}

	private String constructCspckFilePath() {
		if (selectedProject == null) {
			return null;
		}

		String projectLocation = selectedProject.getLocation().toOSString().replace("/", File.separator);
		return projectLocation + File.separator + Messages.deployDir + File.separator + Messages.cspckDefaultFileName;
	}

	private String constructCscfgFilePath() {
		if (selectedProject == null) {
			return null;
		}
		String projectLocation = selectedProject.getLocation().toOSString().replace("/", File.separator);
		return projectLocation + File.separator + Messages.deployDir + File.separator + Messages.cscfgDefaultFileName;
	}

	protected void populateStorageAccounts() {
		if (publishData != null) {
			String currentSelection = storageAccountCmb.getText();
			Subscription currentSubscription = publishData.getCurrentSubscription();
			StorageServices storageServices = publishData.getStoragesPerSubscription().
					get(currentSubscription.getId());
			storageAccountCmb.removeAll();
			if (storageServices != null && !storageServices.isEmpty()) {

				for (StorageService storageService : storageServices) {
					storageAccountCmb.add(storageService.getServiceName());
					storageAccountCmb.setData(storageService.getServiceName(), storageService);
				}
			}
			storageAccountCmb = UIUtils.selectByText(storageAccountCmb, currentSelection);
		}
	}

	private void importBtn(String fileName) {
		if (fileName != null && !fileName.isEmpty()) {

			File file = new File(fileName);

			PublishData publishDataToCache = null;
			if (file.getName().endsWith(Messages.publishSettExt)) {
				publishDataToCache = handlePublishSettings(file);
			}
			else {
				publishDataToCache = handlePfx(file);
			}

			if (publishDataToCache == null) {
				return;
			}
			/*
			 * logic to set un-pubilsh check box to true
			 * when ever importing publish settings
			 * file for the first time.
			 */
			if (subscriptionCombo.getItemCount() == 0) {
				unpublishChBox.setSelection(true);
			}
			subscriptionCombo.removeAll();
			subscriptionCombo = UIUtils.
					populateSubscriptionCombo(subscriptionCombo);

			int selection = 0;
			selection = findSelectionIndex(publishDataToCache);

			subscriptionCombo.select(selection);
			WizardCacheManager.setCurrentPublishData(publishDataToCache);

			setComponentState((subscriptionCombo.getData(subscriptionCombo.getText()) != null));
			// Make centralized storage registry.
			MethodUtils.prepareListFromPublishData();
		}
	}

	private int findSelectionIndex(PublishData publishDataToCache) {
		for (int i = 0 ; i < subscriptionCombo.getItemCount() ; i++) {
			String subscriptionName = subscriptionCombo.getItem(i);
			for (Subscription subscription : publishDataToCache.getPublishProfile().getSubscriptions()) {
				if (subscriptionName.equals(subscription.getName())) {
					return i;
				}
			}
		}
		return 0;
	}

	private PublishData handlePfx(File file) {
		PublishData data;
		try {
			data = WindowsAzureRestUtils.parsePfx(file);
		} catch (Exception e) {
			MessageUtil.displayErrorDialog(getShell(), Messages.importDlgTitle, String.format(Messages.importDlgMsg, file.getName(), Messages.failedToParse));
			return null;
		}
		PfxDialog pfxDialog = new PfxDialog(file, getShell());
		int result = pfxDialog.open();
		if (result == 0) {
			Subscription sub = new Subscription();

			String subscriptionId = pfxDialog.getSubscriptionId();
			sub.setId(subscriptionId);
			sub.setName(subscriptionId);
			data.setCurrentSubscription(sub);

			data.getPublishProfile().getSubscriptions().add(sub);

			String pfxPassword = pfxDialog.getPfxPassword();
			data.getPublishProfile().setPassword(pfxPassword);
			String thumbprint;
			try {
				thumbprint = WindowsAzureRestUtils.getInstance().installPublishSettings(file, pfxPassword);
			} catch (InterruptedException e) {
				MessageUtil.displayErrorDialog(getShell(), Messages.importDlgTitle, String.format(Messages.importDlgMsg, file.getName(), Messages.failedToParse));
				return null;
			} catch (CommandLineException e) {
				MessageUtil.displayErrorDialog(getShell(), Messages.importDlgTitle, String.format(Messages.importDlgMsg, file.getName(), Messages.failedToParse));
				return null;
			}

			if (WizardCacheManager.findPublishDataByThumbprint(thumbprint) != null) {
				MessageUtil.displayErrorDialog(getShell(), Messages.loadingCred, Messages.credentialsExist);
				return null;
			}

			data.setThumbprint(thumbprint);

			data.reset();

			AccountActionRunnable settings = new CacheAccountWithProgressBar(data, getShell(), null);

			doLoad(file, settings);
			return data;
		}
		return null;
	}

	private PublishData handlePublishSettings(File file) {
		PublishData data = UIUtils.createPublishDataObj(file);
		/*
		 * If data is equal to null,
		 * then publish settings file already exists.
		 * So don't load information again.
		 */
		if (data != null) {
			AccountActionRunnable settings =
					new CacheAccountWithProgressBar(data, getShell(), null);
			doLoad(file, settings);
		}
		return data;
	}

	private void doLoad(File file, AccountActionRunnable settings) {
		try {
			getContainer().run(true, true, settings);
			PreferenceUtil.save();
		} catch (InvocationTargetException e) {
			String message = null;
			if (e.getMessage() == null) {
				message = Messages.genericErrorWhileLoadingCred;
			} else {
				message = e.getMessage();
			}
			MessageUtil.displayErrorDialog(getShell(),
					Messages.importDlgTitle,
					String.format(Messages.importDlgMsg,
							file.getName(), message));
		} catch (InterruptedException e) {
		}
	}
}