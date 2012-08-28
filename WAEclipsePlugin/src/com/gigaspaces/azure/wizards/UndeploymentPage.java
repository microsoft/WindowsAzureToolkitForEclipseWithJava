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

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.model.Base64Persistent;
import com.gigaspaces.azure.model.Deployment;
import com.gigaspaces.azure.model.HostedService;
import com.gigaspaces.azure.model.HostedServices;
import com.gigaspaces.azure.model.Status;
import com.gigaspaces.azure.model.Subscription;
import com.gigaspaces.azure.runnable.FetchDeploymentsForHostedServiceWithProgressWindow;
import com.gigaspaces.azure.runnable.LoadAccountWithProgressWindow;
import com.gigaspaces.azure.util.PreferenceUtil;
import com.gigaspaces.azure.util.PublishData;

public class UndeploymentPage extends WindowsAzurePage {

	private Combo subscriptionCombo;
	private Combo hostedServiceCombo;
	private Combo deploymentCombo;

	private PublishData currentPublishData;

	protected UndeploymentPage(String pageName) {
		super(pageName);
		setTitle(pageName);
	}

	@Override
	protected boolean validatePageComplete() {

		String subscriptionId = subscriptionCombo.getText();

		if (subscriptionId == null || subscriptionId.isEmpty()) {
			setErrorMessage("subscription can not be null or empty");
			return false;
		}

		String serviceName = hostedServiceCombo.getText();

		if (serviceName == null || serviceName.isEmpty()) {
			setErrorMessage(Messages.hostedServiceIsNull);
			return false;
		}

		String deploymentName = deploymentCombo.getText();

		Deployment deployment = (Deployment) deploymentCombo
				.getData(deploymentName);

		if (deploymentName == null || deploymentName.isEmpty()
				|| deployment == null) {
			setErrorMessage(Messages.deploymentIsNull);
			return false;
		}
		String label = new String(Base64Persistent.decode(deployment.getLabel())); //$NON-NLS-1$
		((UndeployWizard) getWizard()).setSettings(serviceName,deployment.getName(), label, deployment.getDeploymentSlot().toString());

		setErrorMessage(null);
		return true;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,Messages.pluginPrefix + Messages.unpublishCommandHelp);

		compositeLayout.verticalSpacing = 20;
		compositeLayout.marginTop = 10;
		compositeLayout.marginBottom = 5;
		compositeLayout.numColumns = 1;
		composite.setLayout(compositeLayout);
		setControl(composite);

		Composite container = createDefaultComposite(composite);

		Label lblSubscription = new Label(container, SWT.NONE);

		lblSubscription.setText("Subscription");

		subscriptionCombo = createCombo(container, SWT.READ_ONLY, -1);



		Label label = new Label(container, SWT.NONE);

		label.setText(Messages.hostedServiceLbl);

		hostedServiceCombo = createCombo(container, SWT.READ_ONLY, -1);


		subscriptionCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				String subscriptionName = ((Combo) e.getSource()).getText();
				currentPublishData = (PublishData) ((Combo) e.getSource()).getData(subscriptionName);
				Subscription sub = WizardCacheManager.findSubscriptionByName(subscriptionName);
				if (currentPublishData != null) {
					currentPublishData.setCurrentSubscription(sub);
					WizardCacheManager.setCurrentPublishData(currentPublishData);
				}

				hostedServiceCombo.setEnabled(false);
				deploymentCombo.setEnabled(false);

				deploymentCombo.removeAll();

				populateHostedServices();

				setPageComplete(validatePageComplete());
			}
		});

		hostedServiceCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				deploymentCombo.removeAll();
				deploymentCombo.setEnabled(false);

				populateDeployment();

				setComponentState();

				setPageComplete(validatePageComplete());
			}
		});

		Label deploymentLbl = new Label(container, SWT.NONE);

		deploymentLbl.setText(Messages.deploymentsLbl);

		deploymentCombo = createCombo(container, SWT.READ_ONLY, -1);

		deploymentCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePageComplete());
			}
		});

	}

	@Override
	public void setVisible(boolean bool) {
		if (PreferenceUtil.isLoaded()  == false) {
			Display.getDefault().syncExec(new LoadAccountWithProgressWindow(null, getShell()));			
		}
		PreferenceUtil.setLoaded(true);
		populateSubscriptionCombo();
		validatePageComplete();
		super.setVisible(bool);
	}

	private void populateDeployment() {

		int sel = hostedServiceCombo.getSelectionIndex();

		if (sel > -1) {

			HostedService hostedService;
			FetchDeploymentsForHostedServiceWithProgressWindow progress = new FetchDeploymentsForHostedServiceWithProgressWindow(null, Display.getDefault().getActiveShell());
			progress.setHostedServiceName(hostedServiceCombo.getText());
			Display.getDefault().syncExec(progress);

			hostedService = progress.getHostedService();

			deploymentCombo.removeAll();

			for (Deployment deployment : hostedService.getDeployments()) {
				if (deployment.getName() == null) {
					continue;
				}
				if (deployment.getStatus().equals(Status.Running)) {
					try {
						String label = new String(Base64Persistent.decode(deployment.getLabel()), "UTF-8"); //$NON-NLS-1$
						String id = label + " - " + deployment.getDeploymentSlot();
						deploymentCombo.add(id);
						deploymentCombo.setData(id, deployment);

					} catch (UnsupportedEncodingException e) {
						Activator.getDefault().log(Messages.error, e);
					}
				}
			}

			if (deploymentCombo.getItemCount() > 0) {
				deploymentCombo.select(0);
			}

			setComponentState();
		}
	}

	private Composite createDefaultComposite(Composite parent) {
		Group composite = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		composite.setLayout(layout);

		return composite;
	}

	public void populateHostedServices() {

		if (currentPublishData != null) {
			Subscription currentSubscription = currentPublishData.getCurrentSubscription();
			HostedServices hostedServices = currentPublishData.getServicesPerSubscription().get(currentSubscription.getId());
			if (hostedServices != null) {

				hostedServiceCombo.removeAll();

				for (HostedService hsd : hostedServices) {
					hostedServiceCombo.add(hsd.getServiceName());
					hostedServiceCombo.setData(hsd.getServiceName(), hsd);
				}

				if (hostedServiceCombo.getItemCount() > 0) {
					String defaultSelection = null;
					HostedService curentHostedService = WizardCacheManager.getCurentHostedService();
					if (curentHostedService != null) {
						defaultSelection = curentHostedService.getServiceName();
					}
					int selection = findSelectionByText(defaultSelection, hostedServiceCombo);
					if (selection != -1) {
						hostedServiceCombo.select(selection);
					}
					else {
						hostedServiceCombo.select(0);
					}
				}
			}
		}		

		setComponentState();
	}

	private void populateSubscriptionCombo() {

		Collection<PublishData> publishes = WizardCacheManager.getPublishDatas();

		Map<String , PublishData> map = new HashMap<String, PublishData>();
		for (PublishData pd : publishes) {
			for (Subscription sub : pd.getPublishProfile().getSubscriptions()) {
				map.put(sub.getName(), pd);
			} 
		}


		subscriptionCombo.removeAll();

		for (Entry<String, PublishData> entry : map.entrySet()) {
			subscriptionCombo.add(entry.getKey());
			subscriptionCombo.setData(entry.getKey(), entry.getValue());
		}				
		if (subscriptionCombo.getItemCount() > 0) {
			String defaultSelection = null;
			PublishData currentPublishData2 = WizardCacheManager.getCurrentPublishData();
			if (currentPublishData2 != null) {
				defaultSelection = currentPublishData2.getCurrentSubscription().getName();
			}
			int selection = findSelectionByText(defaultSelection, subscriptionCombo);
			if (selection != -1) {
				subscriptionCombo.select(selection);
			}
			else {
				subscriptionCombo.select(0);
			}
		}

		setComponentState();
	}

	private int findSelectionByText(String txt, Combo combo) {
		if (txt == null || txt.isEmpty()) return 0;
		for (int i = 0 ; i < combo.getItemCount() ; i++) {
			String itemText = combo.getItem(i);
			if (itemText.equals(txt)) return i;
		}
		return 0;
	}

	private void setComponentState() {

		if(hostedServiceCombo == null) {
			return;			
		}

		hostedServiceCombo.setEnabled(subscriptionCombo.getSelectionIndex() > -1 && hostedServiceCombo.getItemCount() > 0);

		if(deploymentCombo == null) {
			return;			
		}
		deploymentCombo.setEnabled(hostedServiceCombo.getSelectionIndex() > -1 && deploymentCombo.getItemCount() > 0);
	}




}
