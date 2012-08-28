/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import com.gigaspaces.azure.rest.WindowsAzureRestUtils;
import com.gigaspaces.azure.runnable.AccountActionRunnable;
import com.gigaspaces.azure.runnable.CacheAccountWithProgressBar;
import com.gigaspaces.azure.runnable.LoadAccountWithProgressBar;
import com.gigaspaces.azure.util.CommandLineException;
import com.gigaspaces.azure.util.PreferenceUtil;
import com.gigaspaces.azure.util.PublishData;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.persistent.util.MessageUtil;

public class SignInPage extends WindowsAzurePage {

	private static Map<String, Boolean> rememberMydecisions = new HashMap<String, Boolean>();
	private static Map<String, Integer> decisions = new HashMap<String, Integer>();
	
	private Text txtcspkg;
	private Text txtcscfg;
	private Combo storageAccountCmb;
	private Combo storageAccessKeyCmb;
	private Combo hostedServiceCombo;
	private Combo deployStateCmb;

	private String deployState;
	private String deployFileName;
	private String deployConfigFileName;
	private HostedService currentHostedService;

	private PublishData publishData;
	private StorageService currentStorageAccount;

	private WindowsAzurePackageType deployMode = WindowsAzurePackageType.CLOUD;

	private Composite signInDetailsContainer;

	private Combo subscriptionCombo;

	private Button downloadCredentialsBtn;
	private Button importBtn;
	private Button newCertBtn;
	private Button newHostedServiceBtn;
	private Button deploymentFileButton;
	private Button deployConfigFileButton;
	private Button newStorageAccountBtn;

	private IProject selectedProject;

	protected SignInPage() {
		super(Messages.deplWizTitle);
		setTitle(Messages.deplWizTitle);
	}

	public void setSelectedProject(IProject project) {
		this.selectedProject = project;
	}

	@Override
	public void createControl(Composite parent) {

		Composite group = new Composite(parent, SWT.FILL);

		GridLayout layout = new GridLayout();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,Messages.pluginPrefix + Messages.publishCommandHelp);

		layout.numColumns = 1;

		group.setLayout(layout);
		setControl(group);
		setEnabledState(group, true);

		createSignInDetailsWidget(group);

		setComponentState((subscriptionCombo.getData(subscriptionCombo.getText()) != null));

		setPageComplete(validatePageComplete());
	}
	
	private void doLoadPreferences() {
		try {
			getContainer().run(true, true, new LoadAccountWithProgressBar(null,this.getShell()));
		} catch (InvocationTargetException e) {
			MessageUtil.displayErrorDialog(getShell(), Messages.importDlgTitle,e.getMessage());
		} catch (InterruptedException e) {
		}
	}

	private void setComponentState(boolean enabled) {
		subscriptionCombo.setEnabled(enabled);
		storageAccessKeyCmb.setEnabled(enabled);
		storageAccountCmb.setEnabled(enabled);
		newStorageAccountBtn.setEnabled(enabled);
		hostedServiceCombo.setEnabled(enabled);
		if (enabled == false)
			hostedServiceCombo.removeAll();
		deployStateCmb.setEnabled(enabled);
		newHostedServiceBtn.setEnabled(enabled);
//		deploymentFileButton.setEnabled(enabled);
//		deployConfigFileButton.setEnabled(enabled);
//		txtcspkg.setEnabled(enabled);
//		txtcscfg.setEnabled(enabled);
	}

	private void createSignInDetailsWidget(Composite group) {
		signInDetailsContainer = new Composite(group, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.verticalSpacing = 20;
		compositeLayout.marginTop = 10;
		compositeLayout.marginBottom = 5;
		compositeLayout.numColumns = 1;
		signInDetailsContainer.setLayout(compositeLayout);

		Composite container = createDefaultComposite(signInDetailsContainer);
		createSubscriptionIdWidget(container);
		createStorageAccountWidget(container);
		createHostedServiceWidget(container);
		createDeploymentStateWidget(container);
//		createDeploymentFileWidget(container);
	}

	private void setEnabledState(Composite composite, boolean enabledState) {
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
				if (PreferenceUtil.isLoaded() == false) {

					doLoadPreferences();

					PreferenceUtil.setLoaded(true);
					populateSubscriptionCombo();
				}
			}
		});

		if (visible) {
			populateSubscriptionCombo();
		}
		super.setVisible(visible);
	}

	@Override
	protected boolean validatePageComplete() {

		setErrorMessage(null);
		if (deployMode == WindowsAzurePackageType.LOCAL)
			return true;

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


		setErrorMessage(null);

		fireConfigurationEvent(new ConfigurationEventArgs(this,ConfigurationEventArgs.DEPLOY_MODE, deployMode));

		fireConfigurationEvent(new ConfigurationEventArgs(this,ConfigurationEventArgs.SUPSCRIPTION, publishData));

		fireConfigurationEvent(new ConfigurationEventArgs(this,ConfigurationEventArgs.STORAGE_ACCOUNT, currentStorageAccount));

		fireConfigurationEvent(new ConfigurationEventArgs(this,ConfigurationEventArgs.STORAGE_ACCESS_KEY,storageAccessKeyCmb.getText()));

		fireConfigurationEvent(new ConfigurationEventArgs(this,ConfigurationEventArgs.DEPLOY_STATE, deployState));
		
		deployFileName = constructCspckFilePath();
		
		deployConfigFileName = constructCscfgFilePath();

		fireConfigurationEvent(new ConfigurationEventArgs(this,ConfigurationEventArgs.DEPLOY_FILE, deployFileName));

		fireConfigurationEvent(new ConfigurationEventArgs(this,ConfigurationEventArgs.DEPLOY_CONFIG_FILE, deployConfigFileName));

		fireConfigurationEvent(new ConfigurationEventArgs(this,ConfigurationEventArgs.HOSTED_SERVICE, currentHostedService));

		return true;
	}

	private void createStorageAccountWidget(Composite container) {

		GridData dataGrid = new GridData();

		dataGrid.verticalIndent = 20;
		Label label = new Label(container, SWT.NONE);

		label.setText(Messages.deplStorageAccLbl);
		label.setLayoutData(dataGrid);

		storageAccountCmb = createCombo(container, SWT.READ_ONLY, 20);

		GridData gridData = new GridData();
		gridData.widthHint = 80;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalIndent = 20;
		gridData.horizontalAlignment = SWT.FILL;

		newStorageAccountBtn = new Button(container, SWT.PUSH);
		newStorageAccountBtn.setLayoutData(gridData);
		newStorageAccountBtn.setText(Messages.newBtn);

		new Link(container, SWT.NO);
		new Link(container, SWT.NO);

		Label keyLbl = new Label(container, SWT.NONE);

		keyLbl.setText(Messages.deplAccessKeyLbl);

		storageAccessKeyCmb = createCombo(container, SWT.READ_ONLY, -1);

		storageAccessKeyCmb.add(KeyName.Primary.toString());
		storageAccessKeyCmb.add(KeyName.Secondary.toString());

		storageAccessKeyCmb.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				String selected = ((Combo) e.getSource()).getText();
				setStorageKeyAccess(selected);
			}
		});

		storageAccountCmb.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				String storageAccount = ((Combo) e.getSource()).getText();

				currentStorageAccount = (StorageService) ((Combo) e.getSource())
						.getData(storageAccount);

				int sel = storageAccessKeyCmb.getSelectionIndex();

				if (sel < -1)
					sel = 0;

				if (currentStorageAccount != null) {
					storageAccessKeyCmb.select(sel);
					String selected = storageAccessKeyCmb.getText();
					setStorageKeyAccess(selected);
				}

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

						NewStorageAccountDialog storageAccountDialog = new NewStorageAccountDialog(getShell());
						int result = storageAccountDialog.open();

						if (result == 0) {

							populateStorageAccounts();
							selectByText(storageAccountCmb, storageAccountDialog.getStorageAccountName());
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


		new Link(container, SWT.NONE);
		new Link(container, SWT.NONE);
		new Link(container, SWT.NO);

		if (storageAccountCmb.getItemCount() > 0)
			storageAccountCmb.select(0);
	}

	private void setStorageKeyAccess(String selected) {

		if (selected != null && !selected.isEmpty()) {
			KeyName sel = KeyName.valueOf(selected);
			if (sel == KeyName.Primary)
				storageAccessKeyCmb.select(0);
			else
				storageAccessKeyCmb.select(1);
		} else
			storageAccessKeyCmb.select(0);
	}

	private void createSubscriptionIdWidget(Composite container) {

		Label label = new Label(container, SWT.NONE);

		label.setText(Messages.deplSubscriptionLbl);
		subscriptionCombo = createCombo(container, SWT.READ_ONLY, -1);

		populateSubscriptionCombo();

		subscriptionCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String subscriptionName = ((Combo) e.getSource()).getText();
				if ((subscriptionName != null) && (!subscriptionName.isEmpty())) {					
					publishData = (PublishData) ((Combo) e.getSource()).getData(subscriptionName);
					Subscription sub = WizardCacheManager.findSubscriptionByName(subscriptionName);
					if (publishData != null) {
						publishData.setCurrentSubscription(sub);
						WizardCacheManager.setCurrentPublishData(publishData);
					}

					if (storageAccountCmb != null && publishData != null) {

						populateStorageAccounts();

						populateHostedServices();

						setComponentState((subscriptionCombo.getData(subscriptionCombo.getText()) != null));
					}

				}
				setPageComplete(validatePageComplete());
			}

		});

		if (subscriptionCombo.getItemCount() > 0)
			subscriptionCombo.select(0);

		GridData gridData = new GridData();
		gridData.widthHint = 80;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;

		importBtn = new Button(container, SWT.PUSH | SWT.CENTER);
		importBtn.setLayoutData(gridData);

		importBtn.setText(Messages.deplImportLbl);

		importBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				importBtn();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		downloadCredentialsBtn = new Button(container, SWT.PUSH | SWT.CENTER);
		downloadCredentialsBtn.setLayoutData(gridData);

		downloadCredentialsBtn.setText(Messages.downloadCredentialsBut);
		downloadCredentialsBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent selectionevent) {
				downloadEvent();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent selectionevent) {
			}
		});

		newCertBtn = new Button(container, SWT.PUSH | SWT.CENTER);
		newCertBtn.setLayoutData(gridData);

		newCertBtn.setText(Messages.deplNewSubscrLbl);

		newCertBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				newCertCreate();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	protected void downloadEvent() {

		PublishSettingsDialog dialog = new PublishSettingsDialog(getShell());

		dialog.open();
	}

	private void createHostedServiceWidget(Composite container) {

		GridData dataGrid = new GridData();

		dataGrid.verticalIndent = 20;
		Label label = new Label(container, SWT.NONE);

		label.setText(Messages.deplHostedServiceLbl);
		label.setLayoutData(dataGrid);

		hostedServiceCombo = createCombo(container, SWT.READ_ONLY, 20);

		populateHostedServices();

		hostedServiceCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				String hostedServiceName = ((Combo) e.getSource()).getText();
				currentHostedService = (HostedService) ((Combo) e.getSource()).getData(hostedServiceName);
				setPageComplete(validatePageComplete());
			}
		});

		GridData gridData = new GridData();
		gridData.widthHint = 80;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalIndent = 20;
		gridData.horizontalAlignment = SWT.FILL;

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

						int result = hostedService.open();

						if (result == 0) {

							populateHostedServices();
							selectByText(hostedServiceCombo, hostedService.getHostedServiceName());
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

		new Link(container, SWT.NO);
		new Link(container, SWT.NO);
	}

	private void selectByText(Combo combo , String name) {
		if (combo.getItemCount() > 0) {
			int selection = findSelectionByText(name, combo);
			if (selection != -1) {
				combo.select(selection);
			}
			else {
				combo.select(0);
			}
		}	
	}


	public void populateHostedServices() {

		if (publishData != null) {
			String currentSelection = hostedServiceCombo.getText();
			Subscription currentSubscription = publishData.getCurrentSubscription();
			HostedServices hostedServices = publishData.getServicesPerSubscription().get(currentSubscription.getId());
			hostedServiceCombo.removeAll();
			if (hostedServices != null && !hostedServices.isEmpty()) {

				for (HostedService hsd : hostedServices) {
					hostedServiceCombo.add(hsd.getServiceName());
					hostedServiceCombo.setData(hsd.getServiceName(), hsd);
				}
				selectByText(hostedServiceCombo, currentSelection);
			}
		}		
	}

	private void createDeploymentStateWidget(Composite container) {
		String[] items = { Messages.deplStaging, Messages.deplProd };
		Label label = new Label(container, SWT.NONE);

		label.setText(Messages.deplState);
		deployState = getFirstItem(items);

		deployStateCmb = createCombo(container, SWT.READ_ONLY, -1);

		deployStateCmb.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				deployState = ((Combo) e.getSource()).getText();
				setPageComplete(validatePageComplete());
			}
		});

		if (items != null) {
			deployStateCmb.setItems(items);
			if (items.length > 0)
				deployStateCmb.select(0);
		}

		new Link(container, SWT.NO);
		new Link(container, SWT.NO);
		new Link(container, SWT.NO);
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

	protected void newCertCreate() {
		NewCertificateDialog dialog = new NewCertificateDialog(getShell());
		int returnCode = dialog.open();
		if (returnCode == Window.OK) {
			if (WizardCacheManager.getCurrentPublishData() != null) {
				MessageDialog.openWarning(getShell(), Messages.deplWarn, Messages.deplCertWarn);
			}
		}
	}

	protected void populateStorageAccounts() {

		if (publishData != null) {
			String currentSelection = storageAccountCmb.getText();
			Subscription currentSubscription = publishData.getCurrentSubscription();
			StorageServices storageServices = publishData.getStoragesPerSubscription().get(currentSubscription.getId());
			storageAccountCmb.removeAll();
			if (storageServices != null && !storageServices.isEmpty()) {

				for (StorageService storageService : storageServices) {
					storageAccountCmb.add(storageService.getServiceName());
					storageAccountCmb.setData(storageService.getServiceName(), storageService);
				}
			}
			selectByText(storageAccountCmb, currentSelection);
		}
	}

	private void populateSubscriptionCombo() {

		Collection<PublishData> publishes = WizardCacheManager.getPublishDatas();

		Map<String, PublishData> map = new HashMap<String, PublishData>();
		for (PublishData pd : publishes) {
			for (Subscription sub : pd.getPublishProfile().getSubscriptions()) {
				map.put(sub.getName(), pd);
			}
		}

		String currentSelection = subscriptionCombo.getText();

		subscriptionCombo.removeAll();

		for (Entry<String, PublishData> entry : map.entrySet()) {
			subscriptionCombo.add(entry.getKey());
			subscriptionCombo.setData(entry.getKey(), entry.getValue());
		}		
		selectByText(subscriptionCombo, currentSelection);
	}

	private Composite createDefaultComposite(Composite parent) {
		Group composite = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 5;
		composite.setLayout(layout);

		return composite;
	}

	private void showProjectBuildMessageDialog(Shell shell,final IProject selectedProject,final WindowsAzureProjectManager waProjManager, final Composite group) {

		MessageDialogWithToggle dialog = null;
		
		String projectName = selectedProject.getName();
		
		Boolean rememberMyDecisionForSelectedProject = rememberMydecisions.get(projectName);
		
		if (rememberMyDecisionForSelectedProject == null || rememberMyDecisionForSelectedProject == false) {
			dialog = MessageDialogWithToggle.open(
					MessageDialog.QUESTION, shell,
					Messages.deplConfirmConfigChangeMsg,
					Messages.deplFullProjBuildConfirmMsg,
					Messages.deplRememberMyDecisionMsg, false, null, null,
					SWT.SHEET);						
		}

		if (dialog != null) { // user did not want to remember his decision for this project. dialog was opened.
			
			boolean toogleState = dialog.getToggleState();
			if (toogleState == true) {
				rememberMydecisions.put(projectName, true);
			}
			else {
				rememberMydecisions.put(projectName, false);
			}
			decisions.put(projectName, dialog.getReturnCode());
		}
		if  (decisions.get(projectName) == IDialogConstants.YES_ID) {
			
			try {
				getContainer().run(true, true, new IRunnableWithProgress() {
					
					@Override
					public void run(IProgressMonitor arg0) throws InvocationTargetException, InterruptedException {
						Job job = new Job(String.format(Messages.buildingProjTask,selectedProject.getName())) {
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								monitor.beginTask(String.format(Messages.buildingProjTask,selectedProject.getName()), IProgressMonitor.UNKNOWN);
								try {
									waProjManager.setPackageType(deployMode);
									selectedProject.build(IncrementalProjectBuilder.CLEAN_BUILD,monitor);
									waProjManager.save();
									selectedProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,null);
									selectedProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
								} catch (Exception e) {
									Activator.getDefault().log(Messages.error, e);
									super.setName("");
									monitor.done();
									return Status.CANCEL_STATUS;
								}
								super.setName("");
								monitor.done();
								return Status.OK_STATUS;
							}
						};
						job.schedule();	
						job.join();
					}
				});
			} 
			catch (InvocationTargetException e1) {
			} 
			catch (InterruptedException e1) {
			}			
		}
	}

	private void importBtn() {

		FileDialogDelegator dialog = new FileDialogDelegator(getShell());

		dialog.setFilterExtensions(new String[] { Messages.publishSettFileName, Messages.pfxExt });

		String fileName = dialog.open();

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
			subscriptionCombo.removeAll();
			populateSubscriptionCombo();

			int selection = 0;
			selection = findSelectionIndex(publishDataToCache);

			subscriptionCombo.select(selection);
			WizardCacheManager.setCurrentPublishData(publishDataToCache);

			setComponentState((subscriptionCombo.getData(subscriptionCombo.getText()) != null));
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

	private int findSelectionByText(String txt, Combo combo) {
		if (txt == null || txt.isEmpty()) return 0;
		for (int i = 0 ; i < combo.getItemCount() ; i++) {
			String itemText = combo.getItem(i);
			if (itemText.equals(txt)) return i;
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
		PublishData data;
		try {
			data = WindowsAzureRestUtils.parse(file);
		} catch (JAXBException e) {
			MessageUtil.displayErrorDialog(getShell(), Messages.importDlgTitle, String.format(Messages.importDlgMsg, file.getName(), Messages.failedToParse));
			return null;
		}

		String thumbprint;
		try {
			thumbprint = WindowsAzureRestUtils.getInstance().installPublishSettings(file, null);
		} catch (InterruptedException e) {
			MessageUtil.displayErrorDialog(getShell(), Messages.importDlgTitle, String.format(Messages.importDlgMsg, file.getName(), Messages.failedToParse));
			return null;
		} catch (CommandLineException e) {
			MessageUtil.displayErrorDialog(getShell(), Messages.importDlgTitle, String.format(Messages.importDlgMsg, file.getName(), e.getMessage()));
			return null;
		}

		if (WizardCacheManager.findPublishDataByThumbprint(thumbprint) != null) {
			MessageUtil.displayErrorDialog(getShell(), Messages.loadingCred, Messages.credentialsExist);
			return null;
		}
		data.setThumbprint(thumbprint);
		data.setCurrentSubscription(data.getPublishProfile().getSubscriptions().get(0));

		AccountActionRunnable settings = new CacheAccountWithProgressBar(data, getShell(), null);
		doLoad(file, settings);
		return data;
	}

	private void doLoad(File file, AccountActionRunnable settings) {
		try {	
			getContainer().run(true, true, settings);
			PreferenceUtil.save();
		}
		catch (InvocationTargetException e) {
			String message = null;
			if (e.getMessage() == null) {
				message = Messages.genericErrorWhileLoadingCred;
			}
			else {
				message = e.getMessage();
			}
			MessageUtil.displayErrorDialog(getShell(), Messages.importDlgTitle,String.format(Messages.importDlgMsg, file.getName(), message));
		} 
		catch (InterruptedException e) {
		}
	}
}