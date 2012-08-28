package com.interopbridges.tools.windowsazure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.interopbridges.tools.windowsazure.v16.WindowsAzureEndpoint_v16;
import com.interopbridges.tools.windowsazure.v16.WindowsAzureInvalidProjectOperationException_v16;
import com.interopbridges.tools.windowsazure.v16.WindowsAzureLocalStorage_v16;
import com.interopbridges.tools.windowsazure.v16.WindowsAzurePackageType_v16;
import com.interopbridges.tools.windowsazure.v16.WindowsAzureProjectManager_v16;
import com.interopbridges.tools.windowsazure.v16.WindowsAzureRole_v16;

public final class ProjectUpgrader {


	// Upgrade project
	protected static String upgradeProject(File oldProjectFile, String newProjectTemplateLoc) throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureProjectManager newProjMan;
		WindowsAzureProjectManager_v16 oldProjMan;

		if (oldProjectFile == null || newProjectTemplateLoc == null) {
			throw new WindowsAzureInvalidProjectOperationException("Can't upgrade this project");
		}

		try {
			System.out.println("Opening old project: " + oldProjectFile.toString());
			oldProjMan = WindowsAzureProjectManager_v16.load(oldProjectFile);
		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			throw new WindowsAzureInvalidProjectOperationException("Can't open old project");
		}

		try {
			System.out.println("Creating new project: " + newProjectTemplateLoc);
			newProjMan = WindowsAzureProjectManager.create(newProjectTemplateLoc);
			System.out.println("New project created.");
		} catch (WindowsAzureInvalidProjectOperationException e) {
			throw new WindowsAzureInvalidProjectOperationException("Can't create a new project at this location");
		} catch (IOException e) {
			throw new WindowsAzureInvalidProjectOperationException("Can't create a new project at this location");
		}

		// Update project name
		String newProjectName;
		try {
			newProjectName = createNewProjectName(oldProjectFile);

			File newProjectFile = new File(oldProjectFile.getParentFile(), newProjectName);
			boolean isSuccess = true;

			// Move project into target location
			WindowsAzureProjectManager.moveProjFromTemp(newProjectName, oldProjectFile.getParent());
			newProjMan = WindowsAzureProjectManager.load(newProjectFile);

			// Upgrade package type
			isSuccess &= upgradePackageType(oldProjMan, newProjMan);
			System.out.println("Upgraded package type");

			// Upgrade remote access
			isSuccess &= upgradeRemoteAccess(oldProjMan, newProjMan, oldProjectFile, newProjectFile);
			System.out.println("Upgraded remote access");

			// Upgrade service name
			newProjMan.setServiceName(oldProjMan.getServiceName());
			System.out.println("Upgraded service name");

			// Upgrade roles
			isSuccess &= upgradeRoles(oldProjMan, newProjMan, newProjectTemplateLoc);
			System.out.println("Upgraded roles");

			// Save the project
			try {
				newProjMan.save();
			}catch(Exception e ) {
				e.printStackTrace();
			}

			// Upgrade user files
			isSuccess &= upgradeUserFiles(oldProjMan, oldProjectFile, newProjectFile);
			System.out.println("User files upgraded");

			// Upgrade user targets in build script
			isSuccess &= upgradeBuildScriptTargets(oldProjectFile, newProjectFile);
			System.out.println("user ant targets upgraded");

			System.out.println(String.format("Upgraded project saved as \"%s\"", newProjectFile.toString()));
			if (!isSuccess) {
				System.out.println("WARNING: Some errors occurred during project upgrade");
			}
			return newProjectFile.toString();

		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			throw new WindowsAzureInvalidProjectOperationException("Can't derive a new project name");
		}
	}

	// Upgrade user files
	private static boolean upgradeUserFiles(WindowsAzureProjectManager_v16 oldProjectManager, File oldProjectFile, File newProjectFile) {
		boolean isSuccess = true;

		// Copy each role's files
		isSuccess &= upgradeUserFilesInRoles(oldProjectManager, oldProjectFile, newProjectFile);

		// Copy user files in project
		isSuccess &= upgradeUserFilesInProject(oldProjectManager, oldProjectFile, newProjectFile);

		return isSuccess;
	}

	// Upgrade user files in project
	private static boolean upgradeUserFilesInProject(WindowsAzureProjectManager_v16 oldProjectManager, File oldProjectFile, File newProjectFile) {

		if (oldProjectManager == null || oldProjectFile == null || newProjectFile == null)
			return false;

		boolean isSuccess = true;
		System.out.println("Upgrading user files from the old project...");
		for (File oldFile : oldProjectFile.listFiles()) {
			if (oldFile.getName().startsWith(".")) {
				continue; // Skip hidden files
			} else if (oldFile.getName().contentEquals("deploy")) {
				continue; // Skip deploy folder
			} else if (oldFile.getName().contentEquals("emulatorTools")) {
				continue; // Skip emulator tools folder
			} else if (oldFile.getName().contentEquals("samples")) {
				continue; // Skip samples folder
			} else if (oldFile.getName().contentEquals("package.xml")) {
				continue; // Skip package.xml
			} else if (oldFile.getName().endsWith(".cscfg")) {
				continue; // Skip CSCFG
			} else if (oldFile.getName().endsWith(".csdef")) {
				continue; // Skip CSDEF
			} else
				try {
					if (oldProjectManager.roleFromPath(oldFile) != null) {
						continue; // Skip role folders;
					}
				} catch (WindowsAzureInvalidProjectOperationException_v16 e1) {
					isSuccess = false;
				}

			File newFile = new File(newProjectFile, oldFile.getName());
			newFile.delete();
			System.out.println(String.format("\tCopying user file: \"%s\"...", oldFile.getName()));
			try {
				copyFile(oldFile, newFile);
			} catch (IOException e) {
				System.out.println("WARNING: Can't copy " + oldFile.toString());
				isSuccess = false;
			}
		}

		return isSuccess;
	}

	// Upgrade user files in role folders
	private static boolean upgradeUserFilesInRoles(WindowsAzureProjectManager_v16 oldProjectManager, File oldProjectFile, File newProjectFile) {
		boolean isSuccess = true;

		// Copy each role's files
		try {
			for (WindowsAzureRole_v16 oldRole : oldProjectManager.getRoles()) {
				// Copy all user files
				File oldApprootFile = new File(oldProjectFile, String.format("%s%s%s", oldRole.getName(), File.separator, "approot"));
				File newApprootFile = new File(newProjectFile, String.format("%s%s%s", oldRole.getName(), File.separator, "approot"));
				System.out.println(String.format("Upgrading role approot: \"%s\"...", oldApprootFile.toString()));
				for (File oldFile : oldApprootFile.listFiles()) {
					if (oldFile.getName().startsWith(".")) {
						continue; // Skip hidden files
					} else if (oldFile.getName().contentEquals("util") && oldFile.isDirectory()) {
						continue; // Skip util directory
					}

					File newFile = new File(newApprootFile, oldFile.getName());
					newFile.delete();
					System.out.println(String.format("\tCopying user file: \"%s\"...", oldFile.getName()));
					try {
						copyFile(oldFile, newFile);
					} catch (IOException e) {
						System.out.println("WARNING: Can't copy " + oldFile.toString());
						isSuccess = false;
					}
				}
			}
		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			System.out.println("WARNING: Can't copy some user files");
			return false;
		}

		return isSuccess;
	}

	// Copies user targets in package.xml
	private static boolean upgradeBuildScriptTargets(File oldProjectFile, File newProjectFile) throws WindowsAzureInvalidProjectOperationException {
		NodeList userTargets;

		XPath xPath = XPathFactory.newInstance().newXPath();
		Document oldBuildScript = openXMLFile(new File(oldProjectFile, "package.xml"));
		if (oldBuildScript == null) {
			System.out.println("Can't parse old project's package.xml");
			return false;
		}

		try {
			userTargets = (NodeList) xPath.evaluate("/project/target[@name!='createwapackage' and @name!='waprojectproperties']", oldBuildScript, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			System.out.println("Can't find Windows Azure configuration in package.xml");
			return false;
		}

		// Copy user targets
		if (userTargets.getLength() > 0) {
			File newBuildScriptFile = new File(newProjectFile, "package.xml");
			Document newBuildScriptXML = openXMLFile(newBuildScriptFile);
			if (newBuildScriptXML == null) {
				System.out.println("Can't parse new project's package.xml");
				return false;
			}

			Element project = newBuildScriptXML.getDocumentElement();

			for (int i = 0; i < userTargets.getLength(); i++) {
				Node target = newBuildScriptXML.importNode(userTargets.item(i), true);
				project.appendChild(target);
			}

			// Save new package.xml
			try {
				saveXMLFile(newBuildScriptFile.toString(), newBuildScriptXML);
			} catch (IOException e) {
				System.out.println("Can't save new project's package.xml");
				return false;
			}

			System.out.println("Copied user-defined targets in package.xml");
		}

		return true;
	}

	// Upgrade roles
	private static boolean upgradeRoles(WindowsAzureProjectManager_v16 oldProject, WindowsAzureProjectManager newProject, String newProjectTemplateLoc) throws WindowsAzureInvalidProjectOperationException {
		// Hide the default role for now, since the project will require at
		// least one role
		boolean isSuccess = true;

		WindowsAzureRole defaultRole = newProject.getRoles().get(0);
		defaultRole.setName("__$");
		newProject.save();

		try {
			for (WindowsAzureRole_v16 oldRole : oldProject.getRoles()) {
				System.out.println(String.format("Upgrading role %s...", oldRole.getName()));

				// Upgrade basic information
				WindowsAzureRole newRole = newProject.addRole(oldRole.getName(), newProjectTemplateLoc);
				newRole.setInstances(oldRole.getInstances());
				newRole.setVMSize(oldRole.getVMSize());

				// Upgrade role endpoints
				isSuccess &= upgradeRoleEndpoints(oldRole, newRole);

				// Upgrade local storage
				isSuccess &= upgradeLocalStorage(oldRole, newRole);

				// Upgrade debugging
				isSuccess &= upgradeDebugging(oldRole, newRole);

				// Upgrade sticky sessions
				isSuccess &= upgradeStickySessions(oldRole, newRole);

				// Upgrade environment variables
				isSuccess &= upgradeEnvVariables(oldRole, newRole);
			}
		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			throw new WindowsAzureInvalidProjectOperationException("Can't upgrade any roles");
		}

		// Verify at least one role was copied
		if (newProject.getRoles().size() < 2)
			throw new WindowsAzureInvalidProjectOperationException("Can't upgrade any roles");

		// Remove the original role
		defaultRole.delete();

		return isSuccess;
	}

	// Upgrade env variables
	private static boolean upgradeEnvVariables(WindowsAzureRole_v16 oldRole, WindowsAzureRole newRole) throws WindowsAzureInvalidProjectOperationException {
		if (oldRole == null || newRole == null) {
			throw new WindowsAzureInvalidProjectOperationException();
		}

		try {
			for (String envName : oldRole.getRuntimeEnv().keySet()) {
				if (newRole.getRuntimeEnv().containsKey(envName)) {
					continue; // Skip already existing env variables
				} else if (newRole.getIsEnvPreconfigured(envName)) {
					continue; // Skip env variables determined dynamically
				} else {
					String envVal = oldRole.getRuntimeEnv(envName);
					newRole.setRuntimeEnv(envName, envVal);
				}
			}
			return true;

		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			System.out.println("WARNING: Can't upgrade environment variables");
			return false;
		}
	}

	// Upgrade sticky sessions
	private static boolean upgradeStickySessions(WindowsAzureRole_v16 oldRole, WindowsAzureRole newRole) throws WindowsAzureInvalidProjectOperationException {
		if (oldRole == null || newRole == null)
			throw new WindowsAzureInvalidProjectOperationException();

		WindowsAzureEndpoint_v16 oldEndpoint;
		try {
			oldEndpoint = oldRole.getSessionAffinityInputEndpoint();
			if (oldEndpoint == null) {
				newRole.setSessionAffinityInputEndpoint(null);

			} else {
				WindowsAzureEndpoint newEndpoint = newRole.getEndpoint(oldEndpoint.getName());
				if (newEndpoint == null) {
					System.out.println("WARNING: Can't upgrade the sticky sessions setting");
					return false;
				}
				newRole.setSessionAffinityInputEndpoint(newEndpoint);
			}
			return true;

		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			System.out.println("WARNING: Can't upgrade the sticky sessions setting");
			return false;
		}
	}

	// Upgrade debugging
	private static boolean upgradeDebugging(WindowsAzureRole_v16 oldRole, WindowsAzureRole newRole) throws WindowsAzureInvalidProjectOperationException {
		if (oldRole == null || newRole == null)
			throw new WindowsAzureInvalidProjectOperationException();

		WindowsAzureEndpoint_v16 oldDebugEndpoint;
		try {
			oldDebugEndpoint = oldRole.getDebuggingEndpoint();
			if (oldDebugEndpoint == null) {
				newRole.setDebuggingEndpoint(null);
//				newRole.setStartSuspended(false);

			} else {
				WindowsAzureEndpoint newDebugEndpoint = newRole.getEndpoint(oldDebugEndpoint.getName());
				if (newDebugEndpoint == null) { // By now, this endpoint should
												// exist
					System.out.println("WARNING: Can't upgrade remote debugging configuration");
					return false;
				}

				newRole.setDebuggingEndpoint(newDebugEndpoint);
				newRole.setStartSuspended(oldRole.getStartSuspended());
			}
			return true;

		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			System.out.println("WARNING: Can't upgrade remote debugging configuration");
			return false;
		}
	}

	// Upgrade local storage
	private static boolean upgradeLocalStorage(WindowsAzureRole_v16 oldRole, WindowsAzureRole newRole) throws WindowsAzureInvalidProjectOperationException {
		if (oldRole == null || newRole == null)
			throw new WindowsAzureInvalidProjectOperationException();

		try {
			for (WindowsAzureLocalStorage_v16 oldStorage : oldRole.getLocalStorage().values()) {
				newRole.addLocalStorage(oldStorage.getName(), oldStorage.getSize(), oldStorage.getCleanOnRecycle(), oldStorage.getPathEnv());
			}
			return true;

		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			System.out.println("WARNING: Local storage could not be upgraded");
			return false;
		}
	}

	// Upgrade role endpoints
	private static boolean upgradeRoleEndpoints(WindowsAzureRole_v16 oldRole, WindowsAzureRole newRole) throws WindowsAzureInvalidProjectOperationException {
		if (oldRole == null || newRole == null)
			throw new WindowsAzureInvalidProjectOperationException();

		// Copy all endpoints
		try {
			for (WindowsAzureEndpoint_v16 oldEndpoint : oldRole.getEndpoints()) {
				newRole.addEndpoint(oldEndpoint.getName(), convertEndpointType(oldEndpoint), oldEndpoint.getPrivatePort(), oldEndpoint.getPort());
			}
			return true;

		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			System.out.println("WARNING: Endpoints could not be upgraded");
			return false;
		}
	}

	// Converts old endpoint type to new one
	private static WindowsAzureEndpointType convertEndpointType(WindowsAzureEndpoint_v16 endpoint) {
		switch (endpoint.getEndPointType()) {
		case Input:
			return WindowsAzureEndpointType.Input;
		case Internal:
			return WindowsAzureEndpointType.Internal;
		default:
			return null;
		}
	}

	// Upgrades remote access
	private static boolean upgradeRemoteAccess(WindowsAzureProjectManager_v16 oldProject, WindowsAzureProjectManager newProject, File oldProjectFile, File newProjectFile) throws WindowsAzureInvalidProjectOperationException {
		if (oldProject == null || newProject == null || oldProjectFile == null || newProjectFile == null) {
			throw new WindowsAzureInvalidProjectOperationException();
		}

		boolean isSuccess = true;

		// Determine whether remote access should be enabled or not
		if (!upgradeRemoteAccessStatus(oldProject, newProject)) {
			return true;
		}

		// Upgrade remote access certificate
		isSuccess &= upgradeRemoteAccessCertificate(oldProject, newProject, oldProjectFile, newProjectFile);

		// Upgrade remote access cert fingerprint
		try {
			newProject.setRemoteAccessCertificateFingerprint(oldProject.getRemoteAccessCertificateFingerprint());
		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			System.out.println("WARNING: Can't upgrade the remote desktop certificate fingerprint");
			isSuccess &= false;
		}

		// Upgrade remote access login credentials
		isSuccess &= upgradeRemoteAccessLogin(oldProject, newProject);

		return isSuccess;
	}

	// Upgrades remote access use credentials
	private static boolean upgradeRemoteAccessLogin(WindowsAzureProjectManager_v16 oldProject, WindowsAzureProjectManager newProject) throws WindowsAzureInvalidProjectOperationException {
		if (oldProject == null || newProject == null)
			throw new WindowsAzureInvalidProjectOperationException();

		String userID;
		try {
			// Process user name
			userID = oldProject.getRemoteAccessUsername();
			newProject.setRemoteAccessUsername(userID);

			// Process expiration date
			Date expiration = oldProject.getRemoteAccessAccountExpiration();
			newProject.setRemoteAccessAccountExpiration(expiration);

			// Process password
			String password = oldProject.getRemoteAccessEncryptedPassword();
			newProject.setRemoteAccessEncryptedPassword(password);

			return true;

		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			System.out.println("WARNING: Can't read old project's remote access credentials");
			return false;
		}
	}

	// Upgrades remote access cert
	private static boolean upgradeRemoteAccessCertificate(WindowsAzureProjectManager_v16 oldProject, WindowsAzureProjectManager newProject, File oldProjectFile, File newProjectFile) throws WindowsAzureInvalidProjectOperationException {
		if (oldProject == null || newProject == null || oldProjectFile == null || newProjectFile == null) {
			throw new WindowsAzureInvalidProjectOperationException();
		}

		try {
			File oldCertFile = new File(oldProject.getRemoteAccessCertificatePath().replace("${basedir}", oldProjectFile.getCanonicalPath()));
			System.out.println(String.format("Upgrading certificate \"%s\" from project \"%s\"", oldCertFile.getCanonicalPath(), oldProjectFile.getCanonicalPath()));

			// If cert not in other project then just move by reference
			if (!oldCertFile.getCanonicalPath().startsWith(oldProjectFile.getCanonicalPath())) {
				newProject.setRemoteAccessCertificatePath(oldCertFile.getCanonicalPath());
			} else {
				// Ensure cert directory
				File certDir = new File(newProjectFile, "cert");
				if (!certDir.exists()) {
					certDir.mkdir();
				} else if (!certDir.isDirectory()) {
					throw new WindowsAzureInvalidProjectOperationException("Can't upgrade the remote desktop certificate");
				}

				File newCertFile = new File(certDir, oldCertFile.getName());
				copyFile(oldCertFile, newCertFile);
				newProject.setRemoteAccessCertificatePath(newCertFile.getCanonicalPath().replace(newProjectFile.getCanonicalPath(), "${basedir}"));
				System.out.println("Remote desktop cert upgraded to:" + newProject.getRemoteAccessCertificatePath());
			}
			return true;

		} catch (IOException e) {
			throw new WindowsAzureInvalidProjectOperationException("Can't upgrade the remote access certificate reference");
		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			System.out.println("WARNING: Can't read the old project's remote access certificate information");
			return false;
		}
	}

	// Upgrades remote access status
	private static boolean upgradeRemoteAccessStatus(WindowsAzureProjectManager_v16 oldProject, WindowsAzureProjectManager newProject) throws WindowsAzureInvalidProjectOperationException {
		if (oldProject == null || newProject == null)
			throw new WindowsAzureInvalidProjectOperationException();

		// Reset remote access status
		newProject.setRemoteAccessAllRoles(false);
		try {
			if (oldProject.getRemoteAccessAllRoles() == false) {
				return false;
			} else {
				newProject.setRemoteAccessAllRoles(true);
				return true;
			}
		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			System.out.println("WARNING: Unsupported package type in the old project");
			return false;
		}
	}

	// Upgrades package type
	private static boolean upgradePackageType(WindowsAzureProjectManager_v16 oldProject, WindowsAzureProjectManager newProject) throws WindowsAzureInvalidProjectOperationException {
		if (oldProject == null || newProject == null)
			throw new WindowsAzureInvalidProjectOperationException();

		WindowsAzurePackageType_v16 oldPackageType;
		try {
			oldPackageType = oldProject.getPackageType();
			switch (oldPackageType) {
			case CLOUD:
				newProject.setPackageType(WindowsAzurePackageType.CLOUD);
				break;
			case LOCAL:
				newProject.setPackageType(WindowsAzurePackageType.LOCAL);
				break;
			default:
				System.out.println("WARNING: Unsupported package type in the old project");
				return false;
			}
		} catch (WindowsAzureInvalidProjectOperationException_v16 e) {
			System.out.println("WARNING: Can't get the package type from the old project's package.xml");
			return false;
		}

		return true;
	}

	// TODO: Save XML - if this is merged with ProjectMnager then this is not
	// needed
	protected static boolean saveXMLFile(String fileName, Document doc) throws IOException, WindowsAzureInvalidProjectOperationException {
		File xmlFile = null;
		FileOutputStream fos = null;
		Transformer transformer;
		try {
			xmlFile = new File(fileName);
			fos = new FileOutputStream(xmlFile);
			TransformerFactory transFactory = TransformerFactory.newInstance();
			transformer = transFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult destination = new StreamResult(fos);
			// transform source into result will do save
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, destination);
		} catch (Exception excp) {
			throw new WindowsAzureInvalidProjectOperationException("Can't save XML file: " + fileName);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		return true;
	}

	// TODO: Open XML - if this is merged with ProjectManager then this is not
	// needed
	static Document openXMLFile(final File xmlFile) {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilderFactory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(xmlFile);
			return doc;
		} catch (Exception e) {
			return null;
		}
	}

	// Creates new project name automatically
	private static String createNewProjectName(File oldProjectFile) throws WindowsAzureInvalidProjectOperationException, WindowsAzureInvalidProjectOperationException_v16 {
		if (oldProjectFile == null)
			throw new WindowsAzureInvalidProjectOperationException();

		String projName = oldProjectFile.getName();
		if (projName == null) {
			throw new WindowsAzureInvalidProjectOperationException("Can't read the old project's name");
		}

		int i = 1;
		String projNameTemp = projName + "_upgraded";

		// Ensure uniqueness
		while (new File(oldProjectFile.getParentFile(), projNameTemp).exists()) {
			projNameTemp = String.format("%s_upgraded(%s)", projName, String.valueOf(i++));
		}

		return projNameTemp;
	}

	// Copy file
	static void copyFile(File source, File destination) throws IOException {

		if (source.isDirectory()) {
			// Copy directory
			if (!destination.exists()) {
				destination.mkdir();
			}

			File[] subFiles = source.listFiles();
			for (int i = 0; i < subFiles.length; i++) {
				copyFile(subFiles[i], new File(destination, subFiles[i].getName()));
			}

		} else {
			// Copy file
			if (!destination.exists()) {
				destination.createNewFile();
			}

			FileInputStream fIn = null;
			FileOutputStream fOut = null;
			FileChannel sourceChannel = null;
			FileChannel destinationChannel = null;
			try {
				fIn = new FileInputStream(source);
				sourceChannel = fIn.getChannel();
				fOut = new FileOutputStream(destination);
				destinationChannel = fOut.getChannel();
				long transfered = 0;
				long bytes = sourceChannel.size();
				while (transfered < bytes) {
					transfered += destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
					destinationChannel.position(transfered);
				}
			} finally {
				if (sourceChannel != null) {
					sourceChannel.close();
				} else if (fIn != null) {
					fIn.close();
				}
				if (destinationChannel != null) {
					destinationChannel.close();
				} else if (fOut != null) {
					fOut.close();
				}
			}
		}
	}
}
