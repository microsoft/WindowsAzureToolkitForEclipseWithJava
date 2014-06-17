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

package com.gigaspaces.azure.deploy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.OperationStatus;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.compute.models.*;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.model.CertificateUpload;
import com.gigaspaces.azure.model.DeployDescriptor;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;
import com.gigaspaces.azure.model.InstanceStatus;
import com.gigaspaces.azure.model.RemoteDesktopDescriptor;
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
import com.microsoftopentechnologies.wacommon.storageregistry.PreferenceUtilStrg;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccount;
import com.microsoftopentechnologies.wacommon.storageregistry.StorageAccountRegistry;
import com.microsoftopentechnologies.wacommon.utils.CerPfxUtil;
import com.microsoftopentechnologies.wacommon.utils.EncUtilHelper;
import com.microsoftopentechnologies.wacommon.utils.WACommonException;


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
				notifyProgress(deploymentDesc.getDeploymentId(), null, 100, OperationStatus.Succeeded, Messages.deplCompleted);
				return;
			}

			// need to improve this check (maybe hostedSerivce.isExisting())?
			if (hostedService.getUri() == null || hostedService.getUri().toString().isEmpty()) { // the hosted service was not yet created.
				notifyProgress(deploymentDesc.getDeploymentId(), null, 5, OperationStatus.InProgress, String.format("%s - %s", Messages.createHostedService, hostedService.getServiceName()));
				createHostedService(hostedService.getServiceName(), hostedService.getServiceName(),
						hostedService.getProperties().getLocation(), hostedService.getProperties().getDescription());
				conditionalProgress -= 5;
			}

			// same goes here
			if (storageAccount.getUrl() == null || storageAccount.getUrl().isEmpty()) { // the storage account was not yet created
				notifyProgress(deploymentDesc.getDeploymentId(), null, 10, OperationStatus.InProgress, String.format("%s - %s", Messages.createStorageAccount, storageAccount.getServiceName()));
				createStorageAccount(storageAccount.getServiceName(), storageAccount.getServiceName(),
						storageAccount.getStorageAccountProperties().getLocation(), storageAccount.getStorageAccountProperties().getDescription());
				conditionalProgress -= 10;
			}

			checkContainerExistance();

			// upload certificates
			if (deploymentDesc.getCertList() != null) {
			List<CertificateUpload> certList = deploymentDesc.getCertList().getList();
			if (certList != null && certList.size() > 0) {
				for (int i = 0; i < certList.size(); i++) {
					CertificateUpload cert = certList.get(i);
					uploadCertificateIfNeededGeneric(
							service,
							deploymentDesc,
							cert.getPfxPath(),
							cert.getPfxPwd());
					notifyProgress(deploymentDesc.getDeploymentId(),
							null, 0, OperationStatus.InProgress,
							String.format("%s%s", Messages.deplUploadCert,
									cert.getName()));
				}
			}
			}

			if (deploymentDesc.getRemoteDesktopDescriptor().isEnabled()) {

				notifyProgress(deploymentDesc.getDeploymentId(), null, conditionalProgress, OperationStatus.InProgress, Messages.deplConfigRdp);

				configureRemoteDesktop(deploymentDesc);
			}
			else {
				notifyProgress(deploymentDesc.getDeploymentId(), null, conditionalProgress, OperationStatus.InProgress, Messages.deplConfigRdp);
			}

			Notifier notifier = new NotifierImp();

			String targetCspckgName = createCspckTargetName(deploymentDesc);

			notifyProgress(deploymentDesc.getDeploymentId(), null, 20, OperationStatus.InProgress,
					Messages.uploadingServicePackage);

			uploadPackageService(
					WizardCacheManager.createStorageServiceHelper(),
					deploymentDesc.getCspkgFile(),
					targetCspckgName,
					Messages.eclipseDeployContainer.toLowerCase(),
					deploymentDesc, notifier);

			notifyProgress(deploymentDesc.getDeploymentId(), null, 20, OperationStatus.InProgress,
					Messages.creatingDeployment);

			String storageAccountURL = deploymentDesc.getStorageAccount().
                    getStorageAccountProperties().getEndpoints().get(0).toString();

			String cspkgUrl = String.format("%s%s/%s", storageAccountURL,
					Messages.eclipseDeployContainer.toLowerCase(), targetCspckgName);
			/*
			 * To make deployment name unique attach time stamp
			 * to the deployment name.
			 */
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			String deploymentName = String.format("%s%s%s",
					hostedService.getServiceName(),
					deployState,
					dateFormat.format(new Date()));
			String requestId = createDeployment(deploymentDesc, service, cspkgUrl, deploymentName);
			OperationStatus status = waitForStatus(deploymentDesc.getConfiguration(), service, requestId);

			deletePackage(WizardCacheManager.createStorageServiceHelper(),
					Messages.eclipseDeployContainer.toLowerCase(),
					targetCspckgName, notifier);
			notifyProgress(deploymentDesc.getDeploymentId(),
					null, 0, OperationStatus.InProgress, Messages.deletePackage);

			notifyProgress(deploymentDesc.getDeploymentId(), null, 20, OperationStatus.InProgress,Messages.waitingForDeployment);

			DeploymentGetResponse deployment = waitForDeployment(
					deploymentDesc.getConfiguration(),
					hostedService.getServiceName(),
					service,
					deploymentName);

			boolean displayHttpsLink = deploymentDesc.getDisplayHttpsLink();
			
			notifyProgress(deploymentDesc.getDeploymentId(),
                    displayHttpsLink ? deployment.getUri().toString().replaceAll("http://", "https://") : deployment.getUri().toString(),
                    20, status,
                    deployment.getStatus().toString());

			if (deploymentDesc.isStartRdpOnDeploy()) {
				WindowsAzureRestUtils.getInstance().launchRDP(deployment,deploymentDesc.getRemoteDesktopDescriptor().getUserName());
			}
		}
		catch (Throwable t) {
			String msg = (t != null ? t.getMessage() : "");
			if (!msg.startsWith(OperationStatus.Failed.toString())) {
				msg = OperationStatus.Failed.toString() + " : " + msg;
			}
			notifyProgress(deploymentDesc.getDeploymentId(), null, 100,
                    OperationStatus.Failed, msg,
					deploymentDesc.getDeploymentId(),
					deployState);
			if (t instanceof DeploymentException) {
				throw (DeploymentException)t;
			}
			throw new DeploymentException(msg, t);
		}
	}

	private void createStorageAccount(final String storageServiceName, final String label, final String location, final String description)
            throws WACommonException, RestAPIException, InterruptedException, ServiceException, CommandLineException {

        StorageAccountCreateParameters accountParameters = new StorageAccountCreateParameters();
        accountParameters.setName(storageServiceName);
        accountParameters.setLabel(label);
        accountParameters.setLocation(location);
        accountParameters.setDescription(description);

		StorageService storageService = WizardCacheManager.createStorageAccount(accountParameters);
		/*
		 * Add newly created storage account
		 * in centralized storage account registry.
		 */
		StorageAccount storageAccount = new StorageAccount(storageService.getServiceName(),
				storageService.getPrimaryKey(),
				storageService.
                        getStorageAccountProperties().getEndpoints().get(0).toString());
		StorageAccountRegistry.addAccount(storageAccount);
		PreferenceUtilStrg.save();
	}

	private void createHostedService(final String hostedServiceName, final String label, final String location, final String description)
            throws WACommonException, ServiceException {
        HostedServiceCreateParameters createHostedService = new HostedServiceCreateParameters();
        createHostedService.setServiceName(hostedServiceName);
        createHostedService.setLabel(label);
        createHostedService.setLocation(location);
        createHostedService.setDescription(description);

		WizardCacheManager.createHostedService(createHostedService);
	}

	private void checkContainerExistance() throws WACommonException, ServiceException {
		WindowsAzureStorageServices storageServices = WizardCacheManager.createStorageServiceHelper();
        storageServices.createContainer(Messages.eclipseDeployContainer.toLowerCase());
	}

	private DeploymentGetResponse waitForDeployment(Configuration configuration,
                                                    String serviceName, WindowsAzureServiceManagement service, String deploymentName)
					throws WACommonException, ServiceException, InterruptedException, DeploymentException {
		DeploymentGetResponse deployment = null;
		String status = null;
		do {
			Thread.sleep(5000);
			deployment = service.getDeployment(configuration, serviceName, deploymentName);

			for (RoleInstance instance : deployment.getRoleInstances()) {
				status = instance.getInstanceStatus();
				if (InstanceStatus.ReadyRole.getInstanceStatus().equals(status)
						|| InstanceStatus.CyclingRole.getInstanceStatus().equals(status)
						|| InstanceStatus.FailedStartingVM.getInstanceStatus().equals(status)
						|| InstanceStatus.UnresponsiveRole.getInstanceStatus().equals(status)) {
					break;
				}
			}
		} while (status != null && !(InstanceStatus.ReadyRole.getInstanceStatus().equals(status)
				|| InstanceStatus.CyclingRole.getInstanceStatus().equals(status)
				|| InstanceStatus.FailedStartingVM.getInstanceStatus().equals(status)
				|| InstanceStatus.UnresponsiveRole.getInstanceStatus().equals(status)));

		if (!InstanceStatus.ReadyRole.getInstanceStatus().equals(status)) {
			throw new DeploymentException(status);
		}
		return deployment;
	}

	private OperationStatus waitForStatus(Configuration configuration, WindowsAzureServiceManagement service, String requestId)
            throws WACommonException, InterruptedException, RestAPIException, ServiceException {
		OperationStatusResponse op;
		OperationStatus status = null;
		do {
			op = service.getOperationStatus(configuration, requestId);
			status = op.getStatus();

			Activator.getDefault().log(Messages.deplId + op.getId());
			Activator.getDefault().log(Messages.deplStatus + op.getStatus());
			Activator.getDefault().log(Messages.deplHttpStatus + op.getHttpStatusCode());
			if (op.getError() != null) {
                Activator.getDefault().log(Messages.deplErrorMessage + op.getError().getMessage());
                throw new RestAPIException(op.getError().getMessage());
			}

			Thread.sleep(5000);

		} while (status == OperationStatus.InProgress);

		return status;
	}

	private String createDeployment(DeployDescriptor deploymentDesc,
			WindowsAzureServiceManagement service,
			String cspkgUrl,
			String deploymentName)
            throws WACommonException, IOException,
            RestAPIException, InterruptedException, CommandLineException, URISyntaxException, ServiceException {

		String label = deploymentDesc.getHostedService().getServiceName(); //$NON-NLS-1$

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
		String deployState = deploymentDesc.getDeployState().toLowerCase();

        DeploymentCreateParameters parameters = new DeploymentCreateParameters();
        parameters.setName(deploymentName);
        parameters.setPackageUri(new URI(cspkgUrl));
        parameters.setLabel(label);
        parameters.setConfiguration(new String(cscfgBuff));
        parameters.setStartDeployment(true);

        return service.createDeployment(
                deploymentDesc.getConfiguration(),
                deploymentDesc.getHostedService().getServiceName(),
                deployState,
                parameters,
                deploymentDesc.getUnpublish());
    }

	private static void uploadPackageService(final WindowsAzureStorageServices service, final String cspkg, String cspckgTargetName,
			final String container, DeployDescriptor deploymentDesc,
			Notifier notifier) throws WACommonException, URISyntaxException, RestAPIException, InterruptedException,
			CommandLineException, ExecutionException, IOException {
		File file = new File(cspkg);
		service.putBlob(container,cspckgTargetName, file, notifier);
	}

	private static void deletePackage(
			final WindowsAzureStorageServices service,
			final String container,
			String cspckgTargetName,
			Notifier notifier)
					throws CommandLineException, FileNotFoundException {
		service.deleteBlob(container, cspckgTargetName, notifier);
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

	public void notifyProgress(String deploymentId,
			String deploymentURL,
			int progress,
			OperationStatus inprogress, String message, Object... args) {

		DeploymentEventArgs arg = new DeploymentEventArgs(this);
		arg.setId(deploymentId);
		arg.setDeploymentURL(deploymentURL);
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
			/** Changes for #645 **/
			String encPassword = null;
			//Ignore cscfg file name and deploy folder
			String projPath 					 =  cscfg.getParentFile().getParent();
			WindowsAzureProjectManager waProjMgr =  WindowsAzureProjectManager.load(new File(projPath));
			encPassword 						 =  waProjMgr.getRemoteAccessEncryptedPassword();

			if (encPassword != null && encPassword.equals(rdp.getPassword())) {
				encPassword = rdp.getPassword();
			} else {
				encPassword =   EncUtilHelper.encryptPassword(rdp.getPassword(), rdp.getPublicKey());
			}

			configureSettings(doc, xpath, Messages.remoteAccessAccountEncryptedPassword, encPassword);

			SimpleDateFormat formatter = new SimpleDateFormat(Messages.dateFormat, Locale.getDefault());

			configureSettings(doc, xpath, Messages.remoteAccessAccountExpiration, formatter.format(rdp.getExpirationDate()));

			String thumbptint = CerPfxUtil.getThumbPrint(rdp.getPublicKey());
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
	
	private void uploadCertificateIfNeededGeneric(
			WindowsAzureServiceManagement service,
			DeployDescriptor deploymentDesc,
			String pfxPath,
			String pfxPwd)
					throws DeploymentException {
		try {
			File pfxFile = new File(pfxPath);
			byte[] buff = new byte[(int) pfxFile.length()];
			FileInputStream fileInputStram = null;
			DataInputStream dis = null;
			try {
				fileInputStram = new FileInputStream(pfxFile);
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

            ServiceCertificateCreateParameters createParameters = new ServiceCertificateCreateParameters();
            createParameters.setData(buff);
            createParameters.setPassword(pfxPwd);
            createParameters.setCertificateFormat(CertificateFormat.Pfx);

			service.addCertificate(
					deploymentDesc.getConfiguration(),
					deploymentDesc.getHostedService().getServiceName(),
					createParameters);
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

	public void undeploy(final String serviceName,
			final String deplymentName,
			final String deploymentState)
					throws WACommonException,
					RestAPIException,
					InterruptedException, CommandLineException {
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

		Configuration configuration = WizardCacheManager.getCurrentPublishData().getCurrentConfiguration();
		int[] progressArr = new int[]{50, 30, 20};
		unPublish(configuration, serviceName, deplymentName, progressArr);
	}

	/**
	 * Unpublish deployment without notifying user.
	 * @param configuration
	 * @param serviceName
	 * @param deplymentName
	 */
	public void unPublish(
            Configuration configuration,
			String serviceName,
			String deplymentName,
			int[] progressArr) {
		try {
			WindowsAzureServiceManagement service = WizardCacheManager.createServiceManagementHelper();
			notifyProgress(deplymentName, null, progressArr[0], OperationStatus.InProgress,
					Messages.stoppingMsg, serviceName);
			String requestId = service.updateDeploymentStatus(configuration,
					serviceName,
					deplymentName,
                    UpdatedDeploymentStatus.Suspended
            );
			waitForStatus(configuration, service, requestId);
			notifyProgress(deplymentName, null, progressArr[1], OperationStatus.InProgress,
					Messages.undeployProgressMsg, deplymentName);
			requestId = service.deleteDeployment(configuration, serviceName, deplymentName);
			waitForStatus(configuration, service, requestId);
			notifyProgress(deplymentName, null, progressArr[2], OperationStatus.Succeeded,
					Messages.undeployCompletedMsg, serviceName);
		} catch (Exception e) {
			Activator.getDefault().log(Messages.deplError, e);
			notifyProgress(deplymentName, null, 100,
                    OperationStatus.Failed,
					e.getMessage(),
					serviceName);
		}
	}
}
