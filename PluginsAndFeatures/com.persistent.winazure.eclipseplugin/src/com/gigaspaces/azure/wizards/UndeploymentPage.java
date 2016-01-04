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
package com.gigaspaces.azure.wizards;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.gigaspaces.azure.runnable.FetchDeploymentsForHostedServiceWithProgressWindow;
import com.gigaspaces.azure.runnable.LoadAccountWithProgressWindow;
import com.gigaspaces.azure.util.PreferenceUtil;
import com.gigaspaces.azure.util.UIUtils;
import com.microsoft.windowsazure.management.compute.models.DeploymentStatus;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetDetailedResponse;
import com.microsoft.windowsazure.management.compute.models.HostedServiceGetDetailedResponse.Deployment;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.microsoftopentechnologies.azurecommons.deploy.util.PublishData;
import com.microsoftopentechnologies.azuremanagementutil.model.Subscription;

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
		String label = deployment.getLabel();
		((UndeployWizard) getWizard()).setSettings(serviceName,deployment.getName(), label, deployment.getDeploymentSlot().toString());

		setErrorMessage(null);
		return true;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				Messages.pluginPrefix + Messages.unpublishCommandHelp);
		compositeLayout.numColumns = 2;
		composite.setLayout(compositeLayout);
		setControl(composite);

		Label lblSubscription = new Label(composite, SWT.NONE);
		lblSubscription.setText("Subscription:");
		subscriptionCombo = createCombo(
				composite, SWT.READ_ONLY, -1, SWT.FILL, 0);

		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.hostedServiceLbl);
		hostedServiceCombo = createCombo(
				composite, SWT.READ_ONLY, -1, SWT.FILL, 0);


		subscriptionCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				currentPublishData = UIUtils.
						changeCurrentSubAsPerCombo((Combo) e.getSource());

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

		Label deploymentLbl = new Label(composite, SWT.NONE);
		deploymentLbl.setText(Messages.deploymentsLbl);
		deploymentCombo = createCombo(
				composite, SWT.READ_ONLY, -1, SWT.FILL, 0);
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
		populateSubscriptionCombo();
		validatePageComplete();
		super.setVisible(bool);
	}

	private void populateDeployment() {

		int sel = hostedServiceCombo.getSelectionIndex();

		if (sel > -1) {

            HostedServiceGetDetailedResponse hostedServiceDetailed;
			FetchDeploymentsForHostedServiceWithProgressWindow progress = new FetchDeploymentsForHostedServiceWithProgressWindow(null, Display.getDefault().getActiveShell());
			progress.setHostedServiceName(hostedServiceCombo.getText());
			Display.getDefault().syncExec(progress);

			hostedServiceDetailed = progress.getHostedServiceDetailed();

			deploymentCombo.removeAll();

			if (hostedServiceDetailed != null) {
			for (Deployment deployment : hostedServiceDetailed.getDeployments()) {
				if (deployment.getName() == null) {
					continue;
				}
				if (!deployment.getStatus().equals(DeploymentStatus.Deleting)) {
                    String label = deployment.getLabel();
                    String id = label + " - " + deployment.getDeploymentSlot();
                    deploymentCombo.add(id);
                    deploymentCombo.setData(id, deployment);
                }
			}
			}

			if (deploymentCombo.getItemCount() > 0) {
				deploymentCombo.select(0);
			}

			setComponentState();
		}
	}

	public void populateHostedServices() {
		if (currentPublishData != null) {
			Subscription currentSubscription = currentPublishData.getCurrentSubscription();
			List<HostedService> hostedServices = currentPublishData.getServicesPerSubscription().get(currentSubscription.getId());
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
					int selection = UIUtils.findSelectionByText(defaultSelection, hostedServiceCombo);
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
			int selection = UIUtils.findSelectionByText(defaultSelection, subscriptionCombo);
			if (selection != -1) {
				subscriptionCombo.select(selection);
			}
			else {
				subscriptionCombo.select(0);
			}
		}

		setComponentState();
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
