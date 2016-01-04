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

package com.microsoftopentechnologies.azurecommons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.interopbridges.tools.windowsazure.WARoleComponentCloudUploadMode;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpoint;
import com.interopbridges.tools.windowsazure.WindowsAzureEndpointType;
import com.interopbridges.tools.windowsazure.WindowsAzureInvalidProjectOperationException;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;
import com.interopbridges.tools.windowsazure.WindowsAzureRoleComponent;
import com.microsoftopentechnologies.azurecommons.messagehandler.PropUtil;

public class WAEclipseHelperMethods {

	/**
	 * To delete directory having contents within it.
	 * 
	 * @param dir
	 */
	public static void deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			// directory is empty, then delete it
			if (dir.list().length == 0) {
				dir.delete();
			} else {
				// list all the directory contents
				String[] subFiles = dir.list();
				for (int i = 0; i < subFiles.length; i++) {
					// construct the file structure
					File fileDelete = new File(dir, subFiles[i]);
					// recursive delete
					deleteDirectory(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (dir.list().length == 0) {
					dir.delete();
				}
			}
		} else {
			dir.delete();
		}
	}

	/**
	 * Converts Windows path into regex including wildcards.
	 * 
	 * @param windowsPath
	 * @return
	 */
	private static String windowsPathToRegex(String windowsPath) {
		if (windowsPath == null) {
			return null;
		}

		// Escape special characters
		String regex = windowsPath.replaceAll(
				"([\\\"\\+\\(\\)\\^\\$\\.\\{\\}\\[\\]\\|\\\\])", "\\\\$1");

		// Replace wildcards
		return regex.replace("*", ".*").replace("?", ".");
	}

	/**
	 * Looks for a pattern in a text file
	 * 
	 * @param file
	 * @param pattern
	 * @return True if a pattern is found, else false
	 * @throws FileNotFoundException
	 */
	private static boolean isPatternInFile(File file, String patternText) {
		Scanner fileScanner = null;

		if (file.isDirectory()) {
			return false;
		}
		try {
			fileScanner = new Scanner(file);
			Pattern pattern = Pattern.compile(patternText,
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = null;
			while (fileScanner.hasNextLine()) {
				String line = fileScanner.nextLine();
				matcher = pattern.matcher(line);
				if (matcher.find()) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		} finally {
			if (fileScanner != null) {
				fileScanner.close();
			}
		}
	}

	/**
	 * Returns the server name whose detection patterns is matched under path.
	 * 
	 * @param serverDetectors
	 * @param path
	 * @return
	 */
	public static String detectServer(File path, String templateFilePath) {
		final Map<String, String> serverDetectors;
		final Map<String, String> serverPatterns;

		if (templateFilePath == null || path == null || !path.isDirectory()
				|| !path.exists()) {
			return null;
		}

		// Get the server detectors from the templates
		final File templateFile = new File(templateFilePath);
		try {
			if (null == (serverDetectors = WindowsAzureProjectManager
					.getServerTemplateDetectors(templateFile))) {
				return null;
			} else if (null == (serverPatterns = WindowsAzureProjectManager
					.getServerTemplatePatterns(templateFile))) {
				return null;
			}
		} catch (WindowsAzureInvalidProjectOperationException e) {
			return null;
		}

		// Check each path
		boolean foundSoFar = false;
		String serverName = null;
		File basePathFile = null;
		for (Map.Entry<String, String> entry : serverDetectors.entrySet()) {
			serverName = entry.getKey();
			String pathPatternText = entry.getValue();
			String textPatternText = serverPatterns.get(serverName);
			if (pathPatternText == null || pathPatternText.isEmpty()) {
				continue;
			}

			// Fast path: Check the pattern directly (like, if it has no wild
			// cards)
			basePathFile = new File(path, pathPatternText);
			if (basePathFile.exists()) {
				// Check for the required pattern inside (if any)
				if (basePathFile.isDirectory() || textPatternText == null
						|| isPatternInFile(basePathFile, textPatternText)) {
					return serverName;
				} else {
					continue;
				}
			}

			// Split pattern path into parts and check for existence of each
			// part
			basePathFile = path;
			String[] pathParts = pathPatternText.split("/");
			foundSoFar = false;
			for (int i = 0; i < pathParts.length; i++) {
				String pathPart = pathParts[i];

				// Try direct match first
				File pathPartFile = new File(basePathFile, pathPart);
				if (pathPartFile.exists()) {
					foundSoFar = true;

					// Check for wildcards
				} else if (!pathPart.contains("*") && !pathPart.contains("?")) {
					foundSoFar = false;

					// Wildcards present, so check pattern
				} else {
					String[] fileNames = basePathFile.list();
					String pathPatternRegex = "^"
							+ windowsPathToRegex(pathPart) + "$";
					Pattern pathPattern = Pattern.compile(pathPatternRegex);
					Matcher matcher = pathPattern.matcher("");
					foundSoFar = false;
					for (String fileName : fileNames) {
						matcher.reset(fileName);
						if (matcher.find()) {
							File file;
							foundSoFar = true;

							if (textPatternText == null) {
								// No text pattern to look for inside, so allow
								// for the match to proceed
								break;
							} else if (i != pathParts.length - 1) {
								// Path part not terminal, so allow the match to
								// proceed
								break;
							} else if (!(file = new File(basePathFile, fileName))
									.isFile()) {
								// Terminal path not a file so don't proceed
								// with this file
								continue;
							} else if (isPatternInFile(file, textPatternText)) {
								// Internal text pattern matched, so success
								return serverName;
							}
						}
					}
				}

				// If this path part worked so far, expand the base path and dig
				// deeper
				if (foundSoFar) {
					basePathFile = new File(basePathFile, pathPart);
				} else {
					break;
				}
			}

			// If matched a full path, then success
			if (foundSoFar) {
				return serverName;
			}
		}

		return null;
	}

	/**
	 * Returns default JDK path.
	 * 
	 * @param currentlySelectedDir
	 * @return
	 */
	public static String jdkDefaultDirectory(String currentlySelectedDir) {
		File file;

		// Try currently selected JDK path
		String path = currentlySelectedDir;
		if (path != null && !path.isEmpty()) {
			file = new File(path);
			if (file.isDirectory() && file.exists()) {
				return path;
			}
		}

		// Try JAVA_HOME
		path = System.getenv("JAVA_HOME");
		if (path != null && !path.isEmpty()) {
			file = new File(path);
			if (file.exists() && file.isDirectory()) {
				// Verify presence of javac.exe
				File javacFile = new File(file, "bin" + File.separator
						+ "javac.exe");
				if (javacFile.exists()) {
					return path;
				}
			}
		}

		// Try under %ProgramFiles%\Java
		path = String.format("%s%s%s", System.getenv("ProgramFiles"),
				File.separator, "Java", File.separator);
		file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return "";
		}

		// Find the first entry under Java that contains jdk
		File[] jdkDirs = file.listFiles();
		Arrays.sort(jdkDirs);

		TreeSet<File> sortedDirs = new TreeSet<File>(Arrays.asList(jdkDirs));
		for (Iterator<File> iterator = sortedDirs.descendingIterator(); iterator
				.hasNext();) {
			File latestSdkDir = iterator.next();
			if (latestSdkDir.isDirectory()
					&& latestSdkDir.getName().contains("jdk")) {
				return latestSdkDir.getAbsolutePath();
			}
		}

		return "";
	}

	/**
	 * This API compares if two files content is identical. It ignores extra
	 * spaces and new lines while comparing
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @return
	 * @throws Exception
	 */
	public static boolean isFilesIdentical(File sourceFile, File destFile)
			throws Exception {
		try {
			Scanner sourceFileScanner = new Scanner(sourceFile);
			Scanner destFileScanner = new Scanner(destFile);

			while (sourceFileScanner.hasNext()) {
				/*
				 * If source file is having next token then destination file
				 * also should have next token, else they are not identical.
				 */
				if (!destFileScanner.hasNext()) {
					destFileScanner.close();
					sourceFileScanner.close();
					return false;
				}
				if (!sourceFileScanner.next().equals(destFileScanner.next())) {
					sourceFileScanner.close();
					destFileScanner.close();
					return false;
				}
			}
			/*
			 * Handling the case where source file is empty and destination file
			 * is having text
			 */
			if (destFileScanner.hasNext()) {
				destFileScanner.close();
				sourceFileScanner.close();
				return false;
			} else {
				destFileScanner.close();
				sourceFileScanner.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Method checks if text contains alphanumeric characters, underscore only
	 * and is starting with alphanumeric or underscore. \p{L} any kind of letter
	 * from any language. \p{Nd} a digit zero through nine in any script except
	 * ideographic scripts.
	 * 
	 * @param text
	 * @return Boolean
	 */
	public static Boolean isAlphaNumericUnderscore(String text) {
		Pattern alphaNumUndscor = Pattern
				.compile("^[\\p{L}_]+[\\p{L}\\p{Nd}_]*$");
		Matcher m = alphaNumUndscor.matcher(text);
		return m.matches();
	}

	/**
	 * Method checks if text contains alphanumeric lower case characters and
	 * integers only.
	 * 
	 * @param text
	 * @return
	 */
	public static Boolean isLowerCaseAndInteger(String text) {
		Pattern lowerCaseInteger = Pattern.compile("^[a-z0-9]+$");
		Matcher m = lowerCaseInteger.matcher(text);
		return m.matches();
	}

	/**
	 * Copy file from source to destination.
	 * 
	 * @param source
	 * @param destination
	 * @throws Exception
	 */
	public static void copyFile(String source, String destination)
			throws Exception {
		File f1 = new File(source);
		File f2 = new File(destination);
		copyFile(f1, f2);
	}

	/**
	 * Copy file from source to destination.
	 * 
	 * @param source
	 * @param destination
	 * @throws Exception
	 */
	public static void copyFile(File f1, File f2)
			throws Exception {
		try {
			InputStream in = new FileInputStream(f1);
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * Method checks whether URL is blob URL or not. It should satisfy pattern
	 * http[s]://<storage-account-name> -->(only lower case letters and numbers
	 * allowed) . -->(exactly one dot is required) <blob-service-endpoint>
	 * -->(only lower case letters, numbers and period allowed) / -->(exactly
	 * one forward slash is required) <container-name> --> (only lower case
	 * letters, numbers and '-' allowed) must start with letter or number, no
	 * consecutive dashes allowed must be of 3 through 63 characters long /
	 * -->(exactly one forward slash is required) <blob-name> -->(may contain
	 * upper lower case characters, numbers and punctuation marks) must be of 1
	 * through 1024 characters long
	 * 
	 * @param text
	 * @return
	 */
	public static Boolean isBlobStorageUrl(String text) {
		Pattern blob = Pattern
				.compile("^https?://[a-z0-9]+\\.{1}[a-z0-9.]+/{1}([a-z]|\\d){1}([a-z]|-|\\d){1,61}([a-z]|\\d){1}/{1}[\\w\\p{Punct}]+$");
		Matcher m = blob.matcher(text);
		return m.matches();
	}

	/**
	 * Method checks whether text is valid application insights instrumentation key or not.
	 * Key is of the format "65e3ff09-1pq7-4e0e-9a36-3282d6c5d700"
	 * 8-4-4-4-12 --> eight lower case letters or numbers followed by single dash '-' and so on.
	 * @param text
	 * @return
	 */
	public static Boolean isValidInstrumentationKey(String text) {
		Pattern key = Pattern.compile("([a-z0-9]){8}-{1}([a-z0-9]){4}-{1}([a-z0-9]){4}-{1}([a-z0-9]){4}-{1}([a-z0-9]){12}");
		Matcher m = key.matcher(text);
		return m.matches();
	}

	/**
	 * Method checks whether text is valid storage account access key or not.
	 * "SK3d/vC2dIl3eaVGRs8W61FW43bO1ubDOAHD3s9TysDsq3qSBF4grpz0K2mg0pUKPx87wJwS5A8oaaXpR1VhVg=="
	 * may contain upper lower case characters, numbers and punctuation marks and 88 characters long.
	 * @param text
	 * @return
	 */
	public static Boolean isValidStorageAccAccessKey(String text) {
		Pattern key = Pattern.compile("[\\w\\p{Punct}]{88}");
		Matcher m = key.matcher(text);
		return m.matches();
	}

	public static Boolean isContainsBlobEndpointUrl(String text) {
		Pattern blob = Pattern
				.compile("^https?://[a-z0-9]+\\.{1}[a-z0-9.]+/{1}[\\w\\p{Punct}]*$");
		Matcher m = blob.matcher(text);
		return m.matches();
	}

	/**
	 * API to find the hostname. This API first checks OS type. If Windows then
	 * tries to get hostname from computername environment variable else uses
	 * environment variable hostname.
	 * 
	 * In case if hostname is not found from environment variable then uses java
	 * networking apis
	 * 
	 */
	public static String getHostName() {
		String hostOS = System.getProperty("os.name");
		String hostName = null;

		// Check host Operating System and get value of hostname.
		if (hostOS != null && hostOS.indexOf("Win") >= 0) {
			hostName = System.getenv("COMPUTERNAME");
		} else { // non-windows platforms
			hostName = System.getenv("HOSTNAME");
		}

		// If hostname is still null , use java network apis
		try {
			if (hostName == null || hostName.isEmpty()) {
				hostName = InetAddress.getLocalHost().getHostName();
			}
		} catch (Exception ex) { // catches UnknownHostException
			// just ignore this exception
		}

		if (hostName == null || hostName.isEmpty()) { // most probabily this
			// case won't happen
			hostName = "localhost";
		}

		return hostName;
	}

	public static WindowsAzureEndpoint findEndpointWithPubPort(int pubPort,
			WindowsAzureRole role)
					throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureEndpoint endpt = null;
		for (WindowsAzureEndpoint endpoint : role.getEndpoints()) {
			if (endpoint.getEndPointType().equals(
					WindowsAzureEndpointType.Input)
					&& endpoint.getPort().equalsIgnoreCase(
							String.valueOf(pubPort))
							&& endpoint.getPrivatePort() != null
							&& !endpoint.equals(role.getDebuggingEndpoint())) {
				endpt = endpoint;
				break;
			}
		}
		return endpt;
	}

	public static WindowsAzureEndpoint findEndpointWithPubPortWithAuto(
			int pubPort, WindowsAzureRole role)
					throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureEndpoint endpt = null;
		for (WindowsAzureEndpoint endpoint : role.getEndpoints()) {
			if (endpoint.getEndPointType().equals(
					WindowsAzureEndpointType.Input)
					&& endpoint.getPort().equalsIgnoreCase(
							String.valueOf(pubPort))
							&& !endpoint.equals(role.getDebuggingEndpoint())) {
				endpt = endpoint;
				break;
			}
		}
		return endpt;
	}

	public static WindowsAzureRole findRoleWithEndpntPubPort(int pubPortToChk,
			WindowsAzureProjectManager waProjManager)
					throws WindowsAzureInvalidProjectOperationException {
		WindowsAzureRole roleWithHTTPS = null;
		for (WindowsAzureRole role : waProjManager.getRoles()) {
			for (WindowsAzureEndpoint endpoint : role.getEndpoints()) {
				if (endpoint.getEndPointType().equals(
						WindowsAzureEndpointType.Input)
						|| endpoint.getEndPointType().equals(
								WindowsAzureEndpointType.InstanceInput)) {
					String pubPort = endpoint.getPort();
					if (pubPort.contains("-")) {
						String[] rang = pubPort.split("-");
						Integer min = Integer.valueOf(rang[0]);
						Integer max = Integer.valueOf(rang[1]);
						if (min == pubPortToChk || max == pubPortToChk
								|| (min < pubPortToChk && max > pubPortToChk)) {
							roleWithHTTPS = role;
							break;
						}
					} else if (pubPort.equalsIgnoreCase(String
							.valueOf(pubPortToChk))) {
						roleWithHTTPS = role;
						break;
					}
				}
			}
		}
		return roleWithHTTPS;
	}

	public static String findJdkPathFromRole(WindowsAzureProjectManager waProjManager) {
		String jdkPath = "";
		try {
			// get number of roles in one project
			List<WindowsAzureRole> roleList = waProjManager.getRoles();
			for (int i = 0; i < roleList.size(); i++) {
				String path = roleList.get(i).getJDKSourcePath();
				if (path != null) {
					jdkPath = path;
					break;
				}
			}
		} catch (Exception e) {
			jdkPath = "";
		}
		return jdkPath;
	}

	public static boolean isAutoPresentForRole(WindowsAzureRole role)
			throws WindowsAzureInvalidProjectOperationException {
		// check for caching storage account name
		String name = role.getCacheStorageAccountName();
		if (name != null && !name.isEmpty() && name.equals("-auto")) {
			return true;
		}
		List<WindowsAzureRoleComponent> cmpnntsList = role.getComponents();
		for (int j = 0; j < cmpnntsList.size(); j++) {
			WindowsAzureRoleComponent component = cmpnntsList.get(j);
			String cmpntType = component.getType();
			WARoleComponentCloudUploadMode mode = component.getCloudUploadMode();
			if (((cmpntType.equals(PropUtil.getValueFromFile("typeJdkDply"))
					|| cmpntType.equals(PropUtil.getValueFromFile("typeSrvDply")))
					&& mode != null && mode.equals(WARoleComponentCloudUploadMode.auto))
					|| (cmpntType.equals(PropUtil.getValueFromFile("typeSrvApp"))
							&& mode != null && mode.equals(WARoleComponentCloudUploadMode.always))) {
				if (component.getCloudDownloadURL().equalsIgnoreCase("auto")) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isFirstPackageWithAuto(WindowsAzureProjectManager projMngr)
			throws WindowsAzureInvalidProjectOperationException {
		boolean retVal = true;
		List<WindowsAzureRole> roleList = projMngr.getRoles();
		boolean isAuto = false;
		for (int i = 0; i < roleList.size(); i++) {
			if (isAutoPresentForRole(roleList.get(i))) {
				isAuto = true;
				break;
			}
		}
		if (isAuto) {
			// Check global properties exist in package.xml
			String pubFileLoc = projMngr.getPublishSettingsPath();
			String subId = projMngr.getPublishSubscriptionId();
			String storageAccName = projMngr.getPublishStorageAccountName();
			String region = projMngr.getPublishRegion();
			if (pubFileLoc == null || pubFileLoc.isEmpty()
					|| subId == null || subId.isEmpty()
					|| storageAccName == null || storageAccName.isEmpty()
					|| region == null || region.isEmpty()) {
				retVal = false;
			}
		}
		return retVal;
	}
}
