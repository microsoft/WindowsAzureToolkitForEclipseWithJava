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

package com.gigaspaces.azure.deploy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.model.CertificateFile;
import com.gigaspaces.azure.model.Container;
import com.gigaspaces.azure.model.CreateDeployment;
import com.gigaspaces.azure.model.CreateHostedService;
import com.gigaspaces.azure.model.CreateStorageServiceInput;
import com.gigaspaces.azure.model.DeployDescriptor;
import com.gigaspaces.azure.model.Deployment;
import com.gigaspaces.azure.model.EnumerationResults;
import com.gigaspaces.azure.model.HostedService;
import com.gigaspaces.azure.model.InstanceStatus;
import com.gigaspaces.azure.model.Operation;
import com.gigaspaces.azure.model.RemoteDesktopDescriptor;
import com.gigaspaces.azure.model.RoleInstance;
import com.gigaspaces.azure.model.Status;
import com.gigaspaces.azure.model.StorageService;
import com.gigaspaces.azure.rest.RestAPIException;
import com.gigaspaces.azure.rest.WindowsAzureRestUtils;
import com.gigaspaces.azure.rest.WindowsAzureServiceManagement;
import com.gigaspaces.azure.rest.WindowsAzureStorageServices;
import com.gigaspaces.azure.util.CommandLineException;
import com.gigaspaces.azure.views.WindowsAzureActivityLogView;
import com.gigaspaces.azure.wizards.WizardCacheManager;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzurePackageType;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.wacommon.utils.Base64;
import com.microsoftopentechnologies.wacommon.utils.EncUtilHelper;

public final class DeploymentManager {

	private final HashMap<String, DeployDescriptor> deployments = new HashMap<String, DeployDescriptor>();

	private static final DeploymentManager DEFAULT_MANAGER = new DeploymentManager();

	public static DeploymentManager getInstance() {
		return DEFAULT_MANAGER;
	}

	private DeploymentManager() {

	}

	public void addDeployment(String name, DeployDescriptor deployment) {
		deployments.put(name, deployment);
	}

	public void removeDeployment(String name) {
		deployments.remove(name);
	}

	public HashMap<String, DeployDescriptor> getDeployments() {
		return deployments;
	}

	public void deploy(IProject selectedProject) throws InterruptedException, DeploymentException {

		DeployDescriptor deploymentDesc = WizardCacheManager.collectConfiguration();

		String deployState = deploymentDesc.getDeployState();
		try {

			int conditionalProgress = 20;
			
			HostedService hostedService = deploymentDesc.getHostedService();
			addDeployment(hostedService.getServiceName(),deploymentDesc);
			
			StorageService storageAccount = deploymentDesc.getStorageAccount();
			

			WindowsAzureServiceManagement service = WizardCacheManager.createServiceManagementHelper();

			openWindowsAzureActivityLogView(deploymentDesc);

			if (deploymentDesc.getDeployMode() == WindowsAzurePackageType.LOCAL) {
				deployToLocalEmulator(selectedProject, deploymentDesc);
				notifyProgress(deploymentDesc.getDeploymentId(), 100,RequestStatus.Succeeded, Messages.deplCompleted);
				return;
			}
			
			// need to improve this check (maybe hostedSerivce.isExisting())?
			if (hostedService.getUrl() == null || hostedService.getUrl().isEmpty()) { // the hosted service was not yet created.
				notifyProgress(deploymentDesc.getDeploymentId(), 5,RequestStatus.InProgress, String.format("%s - %s", Messages.createHostedService, hostedService.getServiceName()));
				createHostedService(hostedService.getServiceName(), hostedService.getServiceName(), 
						hostedService.getHostedServiceProperties().getLocation(), hostedService.getHostedServiceProperties().getDescription());
				conditionalProgress -= 5;
			}
			
			// same goes here
			if (storageAccount.getUrl() == null || storageAccount.getUrl().isEmpty()) { // the storage account was not yet created
				notifyProgress(deploymentDesc.getDeploymentId(), 10,RequestStatus.InProgress, String.format("%s - %s", Messages.createStorageAccount, storageAccount.getServiceName()));
				createStorageAccount(storageAccount.getServiceName(), storageAccount.getServiceName(), 
						storageAccount.getStorageServiceProperties().getLocation(), storageAccount.getStorageServiceProperties().getDescription());
				conditionalProgress -= 10;
			}
			
			checkContainerExistance(deploymentDesc.getSubscriptionId(), service);

			if (deploymentDesc.getRemoteDesktopDescriptor().isEnabled()) {

				notifyProgress(deploymentDesc.getDeploymentId(), 0,RequestStatus.InProgress, Messages.deplUploadCert);

				uploadCertificateIfNeeded(service, deploymentDesc);

				notifyProgress(deploymentDesc.getDeploymentId(), conditionalProgress,RequestStatus.InProgress, Messages.deplConfigRdp);

				configureRemoteDesktop(deploymentDesc);
			} 
			else {
				notifyProgress(deploymentDesc.getDeploymentId(), conditionalProgress,RequestStatus.InProgress, Messages.deplConfigRdp);
			}			

			Notifier notifier = new NotifierImp();

			String targetCspckgName = createCspckTargetName(deploymentDesc);

			notifyProgress(deploymentDesc.getDeploymentId(), 20, RequestStatus.InProgress,
					Messages.uploadingServicePackage);
			
			uploadPackageService(
					WizardCacheManager.createStorageServiceHelper(),
					deploymentDesc.getCspkgFile(), 
					targetCspckgName,
					Messages.eclipseDeployContainer.toLowerCase(),
					deploymentDesc, notifier);

			notifyProgress(deploymentDesc.getDeploymentId(), 20, RequestStatus.InProgress,
					Messages.creatingDeployment);

			String cspkgUrl = String.format(Messages.cspkgUrl, deploymentDesc.getStorageAccount().getServiceName(),
					Messages.eclipseDeployContainer.toLowerCase(), targetCspckgName);
			
			String deploymentName = hostedService.getServiceName() + deployState;
			
			String requestId = createDeployment(deploymentDesc, service,cspkgUrl);
			RequestStatus status = waitForStatus(deploymentDesc.getSubscriptionId(), service, requestId);			
			
			notifyProgress(deploymentDesc.getDeploymentId(), 20, RequestStatus.InProgress,Messages.waitingForDeployment);
			
			Deployment deployment = waitForDeployment(deploymentDesc.getSubscriptionId(), hostedService.getServiceName(), service, deploymentName);

			notifyProgress(deploymentDesc.getDeploymentId(), 20, status,
					deployment.getStatus().toString());

			if (deploymentDesc.isStartRdpOnDeploy()) {
				WindowsAzureRestUtils.getInstance().launchRDP(deployment,deploymentDesc.getRemoteDesktopDescriptor().getUserName());
			}

		}
		catch (Throwable t) {
			notifyProgress(deploymentDesc.getDeploymentId(), 100,
					RequestStatus.Failed, t.getMessage(),
					deploymentDesc.getDeploymentId(),
					deployState);
			if (t instanceof DeploymentException) {
				throw (DeploymentException)t;
			}
			throw new DeploymentException(t.getMessage(), t);
		}
	}

	private void createStorageAccount(final String storageServiceName, final String label, final String location, final String description) throws RestAPIException, InterruptedException, CommandLineException  {
		
		final CreateStorageServiceInput body = new CreateStorageServiceInput(
				storageServiceName, storageServiceName, location);

		body.setDescription(description);
		WizardCacheManager.createStorageAccount(body);
		
	}

	private void createHostedService(final String hostedServiceName, final String label, final String location, final String description) throws RestAPIException, InterruptedException, CommandLineException {
		
		final CreateHostedService body = new CreateHostedService(
				hostedServiceName, label, location);

		body.setDescription(description);
		WizardCacheManager.createHostedService(body);
		
	}

	private void checkContainerExistance(String subscriptionId,
			WindowsAzureServiceManagement service) throws RestAPIException, InterruptedException, CommandLineException {
		WindowsAzureStorageServices storageServices = WizardCacheManager.createStorageServiceHelper();

		try {
			EnumerationResults results = storageServices.listContainers();

			boolean isExist = false;

			for (Container container : results.getContainers()) {
				if (Messages.eclipseDeployContainer.equalsIgnoreCase(container
						.getName())) {
					isExist = true;
					break;
				}
			}

			if (isExist == false) {
				storageServices.createContainer(Messages.eclipseDeployContainer.toLowerCase());
			}
		} 
		catch (InvalidKeyException e) {
		} 

	}

	private Deployment waitForDeployment(String subscriptionId,
			String serviceName, WindowsAzureServiceManagement service, String deploymentName) throws InterruptedException, DeploymentException, RestAPIException, CommandLineException {
		Deployment deployment = null;
		InstanceStatus status = null;
		do {
			Thread.sleep(5000);
			deployment = service.getDeployment(subscriptionId, serviceName,
					deploymentName);

			for (RoleInstance instance : deployment.getRoleInstanceList()) {
				status = instance.getInstanceStatus();
				if (status == InstanceStatus.ReadyRole
						|| status == InstanceStatus.CyclingRole
						|| status == InstanceStatus.FailedStartingVM
						|| status == InstanceStatus.UnresponsiveRole) {
					break;
				}
			}
		} while (status != null && status != InstanceStatus.ReadyRole
				&& status != InstanceStatus.CyclingRole
				&& status != InstanceStatus.FailedStartingVM
				&& status != InstanceStatus.UnresponsiveRole);

		if (status != InstanceStatus.ReadyRole) {
			throw new DeploymentException(status.toString());
		}
		return deployment;
	}

	private RequestStatus waitForStatus(String subscriptionId,WindowsAzureServiceManagement service, String requestId) throws InterruptedException, RestAPIException, CommandLineException {
		Operation op;
		RequestStatus status = null;
		do {
			op = service.getOperationStatus(subscriptionId, requestId);
			status = RequestStatus.valueOf(op.getStatus());

			Activator.getDefault().log(Messages.deplId + op.getID());
			Activator.getDefault().log(Messages.deplStatus + op.getStatus());
			Activator.getDefault().log(Messages.deplHttpStatus + op.getHttpStatusCode());
			if (op.getError() != null) {
				Activator.getDefault().log(Messages.deplErrorCode + op.getError().getCode());
				Activator.getDefault().log(Messages.deplErrorMessage + op.getError().getMessage());
				throw new RestAPIException(op.getError().getCode().toString(), op.getError().getMessage());
			}

			Thread.sleep(5000);

		} while (status == RequestStatus.InProgress);

		return status;
	}

	private String createDeployment(DeployDescriptor deploymentDesc,WindowsAzureServiceManagement service, String cspkgUrl)
					throws UnsupportedEncodingException, IOException,
					FileNotFoundException, RestAPIException, InterruptedException, CommandLineException {

		String label = deploymentDesc.getHostedService().getServiceName(); //$NON-NLS-1$

		label = Base64.encode(label.getBytes(Messages.utfFormat)); //$NON-NLS-1$

		File cscfgFile = new File(deploymentDesc.getCscfgFile());

		byte[] cscfgBuff = new byte[(int) cscfgFile.length()];

		FileInputStream fileInputStream = new FileInputStream(cscfgFile);
		
		DataInputStream dis = new DataInputStream((fileInputStream));
	
		try {
			dis.readFully(cscfgBuff);
			dis.close();
		}
		finally {
			if (dis != null) {
				dis.close();
			}
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
		String configuration = Base64.encode(cscfgBuff); //$NON-NLS-1$
		
		String serviceName = deploymentDesc.getHostedService().getServiceName().toLowerCase();
		String deployState = deploymentDesc.getDeployState().toLowerCase();
		String deploymentName = serviceName + deployState;
		CreateDeployment body = new CreateDeployment(deploymentName, cspkgUrl,label, configuration);

		body.setStartDeployment(true);

		return service.createDeployment(deploymentDesc.getSubscriptionId(),deploymentDesc.getHostedService().getServiceName(),deployState, body);
	}

	private static void uploadPackageService(final WindowsAzureStorageServices service, final String cspkg, String cspckgTargetName,
			final String container, DeployDescriptor deploymentDesc,
			Notifier notifier) throws URISyntaxException, RestAPIException, InterruptedException, CommandLineException, ExecutionException, IOException {
		File file = new File(cspkg);
		service.putBlob(container,cspckgTargetName, file, notifier);
	}

	private String createCspckTargetName(DeployDescriptor deploymentDesc) {
		String cspkgName = String.format(Messages.cspkgName, deploymentDesc.getHostedService().getServiceName(), deploymentDesc.getDeployState());
		return cspkgName;
	}

	private void deployToLocalEmulator(IProject selectedProject,
			DeployDescriptor deploymentDesc) throws DeploymentException {

		WindowsAzureProjectManager waProjManager;
		try {
			waProjManager = WindowsAzureProjectManager.load(new File(
					selectedProject.getLocation().toOSString()));
			waProjManager.deployToEmulator();
		} catch (WindowsAzureInvalidProjectOperationException e) {
			throw new DeploymentException(e);
		}
	}
	
	public void notifyUploadProgress() {
		
	}

	public void notifyProgress(String deploymentId, int progress,
			RequestStatus inprogress, String message, Object... args) {

		DeploymentEventArgs arg = new DeploymentEventArgs(this);
		arg.setId(deploymentId);
		arg.setDeployMessage(String.format(message, args));
		arg.setDeployCompleteness(progress);
		arg.setStartTime(new Date());
		arg.setStatus(inprogress);
		Activator.getDefault().fireDeploymentEvent(arg);
	}

	private void configureRemoteDesktop(DeployDescriptor deploymentDesc) throws DeploymentException {
		DocumentBuilder docBuilder = null;
		Document doc = null;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setIgnoringElementContentWhitespace(true);
		RemoteDesktopDescriptor rdp = deploymentDesc.getRemoteDesktopDescriptor();
		boolean enabled = rdp.isEnabled();
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new DeploymentException(Messages.deplFailedConfigRdp, e);
		}
		File cscfg = new File(WizardCacheManager.getCurrentDeployConfigFile());
		try {
			doc = docBuilder.parse(cscfg);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			if (enabled) {
				configureSettings(doc, xpath, Messages.remoteAccessEnabledSetting, Messages.remoteAccessEnabledSettingVal);
			}
			else {
				configureSettings(doc, xpath, Messages.remoteAccessEnabledSetting, "false");	
			}
			configureSettings(doc, xpath, Messages.remoteFormarderEnabledSetting, Messages.remoteFormarderEnabledSettingVal);
			configureSettings(doc, xpath, Messages.remoteAccessAccountUsername, rdp.getUserName());

			String encPassword = EncUtilHelper.encryptPassword(rdp.getPassword(), rdp.getPublicKey());
			configureSettings(doc, xpath, Messages.remoteAccessAccountEncryptedPassword, encPassword);

			SimpleDateFormat formatter = new SimpleDateFormat(Messages.dateFormat, Locale.getDefault());

			configureSettings(doc, xpath, Messages.remoteAccessAccountExpiration, formatter.format(rdp.getExpirationDate()));

			String thumbptint = EncUtilHelper.getThumbPrint(rdp.getPublicKey());
			configureRdpCertificate(doc, xpath, thumbptint, "sha1"); //$NON-NLS-1$

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, Messages.deplYes);

			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(cscfg);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

		} catch (SAXException e) {
			throw new DeploymentException(Messages.deplFailedConfigRdp, e);
		} catch (IOException e) {
			throw new DeploymentException(Messages.deplFailedConfigRdp, e);
		} catch (TransformerConfigurationException e) {
			throw new DeploymentException(Messages.deplFailedConfigRdp, e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new DeploymentException(Messages.deplFailedConfigRdp, e);
		} catch (TransformerException e) {
			throw new DeploymentException(Messages.deplFailedConfigRdp, e);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			throw new DeploymentException(Messages.deplFailedConfigRdp, e);
		} catch (XPathExpressionException e) {
			throw new DeploymentException(Messages.deplFailedConfigRdp, e);
		} catch (Exception e) {
			throw new DeploymentException(Messages.deplFailedConfigRdp, e);
		}
	}

	private void configureRdpCertificate(Document doc, XPath xpath, String thumbprint, String thumbprintAlgorithm) throws XPathExpressionException {

		ensureCertificationSectionExist(doc, xpath);

		XPathExpression expr;

		ensureConfigurationSettingsSectionExist(doc, xpath);

		expr = xpath.compile(Messages.certificatePath);
		Object result = expr.evaluate(doc, XPathConstants.NODESET);

		NodeList nodes = (NodeList) result;

		if (nodes.getLength() == 0) {
			XPathExpression expr1 = xpath.compile(Messages.certificatesPath);
			result = expr1.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;

			for (int i = 0; i < nodes.getLength(); i++) {
				Element node = doc.createElement(Messages.certificateElem);
				node.setAttribute(Messages.certificateNameAttr,
						Messages.remoteAccessPasswordEncryption);
				node.setAttribute(Messages.thumbprintAttr, thumbprint);
				node.setAttribute(Messages.thumbprintAlg, thumbprintAlgorithm);
				nodes.item(i).appendChild(node);
			}
		} else {
			for (int i = 0; i < nodes.getLength(); i++) {
				nodes.item(i).getAttributes().getNamedItem(Messages.thumbprint)
				.setNodeValue(thumbprint);
				nodes.item(i).getAttributes()
				.getNamedItem(Messages.thumbprintAlg)
				.setNodeValue(thumbprintAlgorithm);
			}
		}
	}

	private void ensureCertificationSectionExist(Document doc, XPath xpath)
			throws XPathExpressionException {
		XPathExpression expr1;

		expr1 = xpath.compile(Messages.certificatesPath);

		Object result = expr1.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		if (nodes.getLength() == 0) {
			XPathExpression expr2 = xpath.compile(Messages.rolePath);
			result = expr2.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				Element node = doc.createElement(Messages.certificatesElem);
				nodes.item(i).appendChild(node);
			}
		}
	}

	private void configureSettings(Document doc, XPath xpath, String key,
			String value) throws XPathExpressionException {
		XPathExpression expr;

		ensureConfigurationSettingsSectionExist(doc, xpath);

		expr = xpath.compile(String.format(Messages.configurationSettingPath,
				key));
		Object result = expr.evaluate(doc, XPathConstants.NODESET);

		NodeList nodes = (NodeList) result;

		if (nodes.getLength() == 0) {
			XPathExpression expr1 = xpath
					.compile(Messages.configurationSettingsPath);
			result = expr1.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;

			for (int i = 0; i < nodes.getLength(); i++) {
				Element node = doc.createElement(Messages.settingElem);
				node.setAttribute(Messages.settingNameAttr, key);
				node.setAttribute(Messages.settingValueAttr, value);

				nodes.item(i).appendChild(node);
			}
		} else {
			for (int i = 0; i < nodes.getLength(); i++) {
				nodes.item(i).getAttributes()
				.getNamedItem(Messages.settingValueAttr)
				.setNodeValue(value);
			}
		}

	}

	private void ensureConfigurationSettingsSectionExist(Document doc,
			XPath xpath) throws XPathExpressionException {

		XPathExpression expr1;

		expr1 = xpath.compile(Messages.configurationSettingsPath);

		Object result = expr1.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		if (nodes.getLength() == 0) {
			XPathExpression expr2 = xpath.compile(Messages.rolePath);
			result = expr2.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				Element node = doc
						.createElement(Messages.configurationSettingsElem);
				nodes.item(i).appendChild(node);
			}
		}

	}

	private void uploadCertificateIfNeeded(WindowsAzureServiceManagement service,DeployDescriptor deploymentDesc) throws DeploymentException {

		try {

			File rdpPfxFile = new File(deploymentDesc.getRemoteDesktopDescriptor().getPrivateKey());
			String azureRemoteDesktopPfxFilePassword = deploymentDesc.getRemoteDesktopDescriptor().getPfxPassword();

			byte[] buff = new byte[(int) rdpPfxFile.length()];

			FileInputStream fileInputStram = null;
			DataInputStream dis = null;
			try {
				fileInputStram = new FileInputStream(rdpPfxFile);
				dis = new DataInputStream(fileInputStram);
				dis.readFully(buff);
			}
			finally {
				if (fileInputStram != null) {
					fileInputStram.close();
				}
				if (dis != null) {
					dis.close();
				}
			}

			CertificateFile certFile = new CertificateFile(buff,azureRemoteDesktopPfxFilePassword);
			service.addCertificate(deploymentDesc.getSubscriptionId(),deploymentDesc.getHostedService().getServiceName(),certFile);
			WindowsAzureRestUtils.getInstance().installPublishSettings(new File(deploymentDesc.getRemoteDesktopDescriptor().getPublicKey()), null);
			
		} catch (Exception e) {
			Activator.getDefault().log(Messages.deplError, e);
			throw new DeploymentException("Error uploading certificate", e);
		}
	}

	private void openWindowsAzureActivityLogView(
			final DeployDescriptor descriptor) {

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					WindowsAzureActivityLogView waView = (WindowsAzureActivityLogView) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(Messages.activityView);

					String desc = String.format(Messages.deplDesc, descriptor.getHostedService().getServiceName(), descriptor.getDeployState());

					waView.addDeployment(descriptor.getDeploymentId(), desc,
							descriptor.getStartTime());

				} catch (PartInitException e) {
					Activator.getDefault().log(Messages.deplCantOpenView, e);
				}

			}
		});

	}

	public void undeploy(final String serviceName, final String deplymentName, final String deploymentState) throws RestAPIException, InterruptedException, CommandLineException {
		try {
			WindowsAzureServiceManagement service = WizardCacheManager.createServiceManagementHelper();

			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					try {
						WindowsAzureActivityLogView waView = (WindowsAzureActivityLogView) PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage()
								.showView(Messages.activityView);

						String desc = String.format(Messages.undeployMsg, serviceName,deploymentState);

						waView.addDeployment(deplymentName, desc, new Date());

					} catch (PartInitException e) {
						Activator.getDefault().log(Messages.deplCantOpenView, e);
					}

				}
			});

			notifyProgress(deplymentName, 50, RequestStatus.InProgress,Messages.stoppingMsg, serviceName);

			String subscriptionId = WizardCacheManager.getCurrentPublishData().getCurrentSubscription().getId();

			String requestId = service.updateDeploymentStatus(subscriptionId,serviceName, deplymentName, Status.Suspended);

			notifyProgress(deplymentName, 30, RequestStatus.InProgress,Messages.undeployProgressMsg, deplymentName);

			waitForStatus(subscriptionId, service, requestId);

			requestId = service.deleteDeployment(subscriptionId, serviceName,deplymentName);

			waitForStatus(subscriptionId, service, requestId);

			notifyProgress(deplymentName, 20, RequestStatus.Succeeded,Messages.undeployCompletedMsg, serviceName);
		} 
		catch (InterruptedException e) {
			Activator.getDefault().log(Messages.deplError, e);
			notifyProgress(deplymentName, 100, RequestStatus.Failed,e.getMessage(), serviceName);
			throw e;
		} 
		catch (RestAPIException e) {
			notifyProgress(deplymentName, 100, RequestStatus.Failed,e.getMessage(), serviceName);
			throw e;
		}
	}
}
