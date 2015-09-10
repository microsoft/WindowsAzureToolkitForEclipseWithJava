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

package com.microsoftopentechnologies.azurecommons.deploy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Locale;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoft.windowsazure.management.compute.models.CertificateFormat;
import com.microsoft.windowsazure.management.compute.models.DeploymentCreateParameters;
import com.microsoft.windowsazure.management.compute.models.ServiceCertificateCreateParameters;
import com.microsoftopentechnologies.azurecommons.deploy.model.DeployDescriptor;
import com.microsoftopentechnologies.azurecommons.deploy.model.RemoteDesktopDescriptor;
import com.microsoftopentechnologies.azurecommons.exception.DeploymentException;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;
import com.microsoftopentechnologies.azuremanagementutil.model.Notifier;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureServiceManagement;
import com.microsoftopentechnologies.azuremanagementutil.rest.WindowsAzureStorageServices;
import com.microsoftopentechnologies.azurecommons.wacommonutil.CerPfxUtil;
import com.microsoftopentechnologies.azurecommons.wacommonutil.EncUtilHelper;

public class DeploymentManagerUtilMethods {
	static String configurationSettingsElem = PropUtil.getValueFromFile("configurationSettingsElem");
	static String thumbprintAlg = PropUtil.getValueFromFile("thumbprintAlg");
	static String thumbprintAttr = PropUtil.getValueFromFile("thumbprintAttr");
	static String rolePath = PropUtil.getValueFromFile("rolePath");
	static String configurationSettingsPath = PropUtil.getValueFromFile("configurationSettingsPath");
	static String configurationSettingPath = PropUtil.getValueFromFile("configurationSettingPath");
	static String settingElem = PropUtil.getValueFromFile("settingElem");
	static String settingNameAttr = PropUtil.getValueFromFile("settingNameAttr");
	static String settingValueAttr =PropUtil.getValueFromFile("settingValueAttr");
	static String certificatesPath = PropUtil.getValueFromFile("certificatesPath");
	static String certificatesElem = PropUtil.getValueFromFile("certificatesElem");
	static String certificateElem = PropUtil.getValueFromFile("certificateElem");
	static String remoteAccessPasswordEncryption = PropUtil.getValueFromFile("remoteAccessPasswordEncryption");
	static String certificatePath = PropUtil.getValueFromFile("certificatePath");
	static String deplFailedConfigRdp = PropUtil.getValueFromFile("deplFailedConfigRdp");
	static String remoteAccessEnabledSetting = PropUtil.getValueFromFile("remoteAccessEnabledSetting");
	static String remoteFormarderEnabledSetting = PropUtil.getValueFromFile("remoteFormarderEnabledSetting");
	static String remoteAccessAccountUsername = PropUtil.getValueFromFile("remoteAccessAccountUsername");
	static String remoteAccessAccountEncryptedPassword = PropUtil.getValueFromFile("remoteAccessAccountEncryptedPassword");
	static String remoteAccessAccountExpiration = PropUtil.getValueFromFile("remoteAccessAccountExpiration");
	static String dateFormat = PropUtil.getValueFromFile("dateFormat");

	public static void ensureConfigurationSettingsSectionExist(Document doc,
			XPath xpath) throws XPathExpressionException {

		XPathExpression expr1;

		expr1 = xpath.compile(configurationSettingsPath);

		Object result = expr1.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		if (nodes.getLength() == 0) {
			XPathExpression expr2 = xpath.compile(rolePath);
			result = expr2.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				Element node = doc
						.createElement(configurationSettingsElem);
				nodes.item(i).appendChild(node);
			}
		}
	}

	public static void configureSettings(Document doc, XPath xpath, String key,
			String value) throws XPathExpressionException {
		XPathExpression expr;

		ensureConfigurationSettingsSectionExist(doc, xpath);

		expr = xpath.compile(String.format(configurationSettingPath,
				key));
		Object result = expr.evaluate(doc, XPathConstants.NODESET);

		NodeList nodes = (NodeList) result;

		if (nodes.getLength() == 0) {
			XPathExpression expr1 = xpath
					.compile(configurationSettingsPath);
			result = expr1.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;

			for (int i = 0; i < nodes.getLength(); i++) {
				Element node = doc.createElement(settingElem);
				node.setAttribute(settingNameAttr, key);
				node.setAttribute(settingValueAttr, value);

				nodes.item(i).appendChild(node);
			}
		} else {
			for (int i = 0; i < nodes.getLength(); i++) {
				nodes.item(i).getAttributes()
				.getNamedItem(settingValueAttr)
				.setNodeValue(value);
			}
		}
	}

	public static void ensureCertificationSectionExist(Document doc, XPath xpath)
			throws XPathExpressionException {
		XPathExpression expr1;

		expr1 = xpath.compile(certificatesPath);

		Object result = expr1.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		if (nodes.getLength() == 0) {
			XPathExpression expr2 = xpath.compile(rolePath);
			result = expr2.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				Element node = doc.createElement(certificatesElem);
				nodes.item(i).appendChild(node);
			}
		}
	}

	public static void configureRdpCertificate(Document doc, XPath xpath,
			String thumbprint, String thumbprintAlgorithm)
					throws XPathExpressionException {

		ensureCertificationSectionExist(doc, xpath);

		XPathExpression expr;

		ensureConfigurationSettingsSectionExist(doc, xpath);

		expr = xpath.compile(certificatePath);
		Object result = expr.evaluate(doc, XPathConstants.NODESET);

		NodeList nodes = (NodeList) result;

		if (nodes.getLength() == 0) {
			XPathExpression expr1 = xpath.compile(certificatesPath);
			result = expr1.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;

			for (int i = 0; i < nodes.getLength(); i++) {
				Element node = doc.createElement(certificateElem);
				node.setAttribute(settingNameAttr,
						remoteAccessPasswordEncryption);
				node.setAttribute(thumbprintAttr, thumbprint);
				node.setAttribute(thumbprintAlg, thumbprintAlgorithm);
				nodes.item(i).appendChild(node);
			}
		} else {
			for (int i = 0; i < nodes.getLength(); i++) {
				nodes.item(i).getAttributes().getNamedItem(thumbprintAttr)
				.setNodeValue(thumbprint);
				nodes.item(i).getAttributes()
				.getNamedItem(thumbprintAlg)
				.setNodeValue(thumbprintAlgorithm);
			}
		}
	}

	public static void configureRemoteDesktop(DeployDescriptor deploymentDesc,
			String deployFile,
			String encPath) throws DeploymentException {
		DocumentBuilder docBuilder = null;
		Document doc = null;
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setIgnoringElementContentWhitespace(true);
		RemoteDesktopDescriptor rdp = deploymentDesc.getRemoteDesktopDescriptor();
		boolean enabled = rdp.isEnabled();
		try {

			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new DeploymentException(deplFailedConfigRdp, e);
		}
		File cscfg = new File(deployFile);

		try {
			doc = docBuilder.parse(cscfg);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			if (enabled) {
				configureSettings(doc, xpath, remoteAccessEnabledSetting, "true");
			}
			else {
				configureSettings(doc, xpath, remoteAccessEnabledSetting, "false");
			}
			configureSettings(doc, xpath, remoteFormarderEnabledSetting, "true");
			configureSettings(doc, xpath, remoteAccessAccountUsername, rdp.getUserName());
			/** Changes for #645 **/
			String encPassword = null;
			//Ignore cscfg file name and deploy folder
			String projPath 					 =  cscfg.getParentFile().getParent();
			WindowsAzureProjectManager waProjMgr =  WindowsAzureProjectManager.load(new File(projPath));
			encPassword 						 =  waProjMgr.getRemoteAccessEncryptedPassword();

			if (encPassword != null && encPassword.equals(rdp.getPassword())) {
				encPassword = rdp.getPassword();
			} else {
				encPassword =   EncUtilHelper.encryptPassword(rdp.getPassword(), rdp.getPublicKey(), encPath);
			}

			configureSettings(doc, xpath, remoteAccessAccountEncryptedPassword, encPassword);

			SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());

			configureSettings(doc, xpath, remoteAccessAccountExpiration, formatter.format(rdp.getExpirationDate()));

			String thumbptint = CerPfxUtil.getThumbPrint(rdp.getPublicKey());
			configureRdpCertificate(doc, xpath, thumbptint, "sha1"); //$NON-NLS-1$

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(cscfg);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

		} catch (SAXException e) {
			throw new DeploymentException(deplFailedConfigRdp, e);
		} catch (IOException e) {
			throw new DeploymentException(deplFailedConfigRdp, e);
		} catch (TransformerConfigurationException e) {
			throw new DeploymentException(deplFailedConfigRdp, e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new DeploymentException(deplFailedConfigRdp, e);
		} catch (TransformerException e) {
			throw new DeploymentException(deplFailedConfigRdp, e);
		} catch (WindowsAzureInvalidProjectOperationException e) {
			throw new DeploymentException(deplFailedConfigRdp, e);
		} catch (XPathExpressionException e) {
			throw new DeploymentException(deplFailedConfigRdp, e);
		} catch (Exception e) {
			throw new DeploymentException(deplFailedConfigRdp, e);
		}
	}

	public static void uploadCertificateIfNeededGeneric(
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
			throw new DeploymentException("Error uploading certificate", e);
		}
	}

	public static void deletePackage(
			final WindowsAzureStorageServices service,
			final String container,
			String cspckgTargetName,
			Notifier notifier)
					throws Exception {
		service.deleteBlob(container, cspckgTargetName, notifier);
	}

	public static void uploadPackageService(final WindowsAzureStorageServices service,
			final String cspkg, String cspckgTargetName,
			final String container, DeployDescriptor deploymentDesc,
			Notifier notifier) throws Exception {
		File file = new File(cspkg);
		service.putBlob(container,cspckgTargetName, file, notifier);
	}

	public static String createDeployment(DeployDescriptor deploymentDesc,
			WindowsAzureServiceManagement service,
			String cspkgUrl,
			String deploymentName)
					throws Exception {

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
}
