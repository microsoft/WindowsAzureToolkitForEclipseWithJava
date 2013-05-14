// CHECKSTYLE:OFF


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

package com.gigaspaces.azure.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.core.runtime.Platform;

import waeclipseplugin.Activator;
import com.microsoftopentechnologies.wacommon.utils.Base64;
import com.gigaspaces.azure.model.Deployment;
import com.gigaspaces.azure.model.ModelFactory;
import com.gigaspaces.azure.model.RoleInstance;
import com.gigaspaces.azure.util.CommandLineException;
import com.gigaspaces.azure.util.Messages;
import com.gigaspaces.azure.util.PublishData;
import com.gigaspaces.azure.util.PublishProfile;

public class WindowsAzureRestUtils {

	private static String pluginFolder;
	private static String uri;

	private static WindowsAzureRestUtils instance;
	private static final String CHAR_ENCODING = "UTF-8";

	public static synchronized WindowsAzureRestUtils getInstance() {

		if (instance == null) {
			String eclipseInstallation = Platform.getInstallLocation().getURL()
					.getPath().toString();
			if (eclipseInstallation.charAt(0) == '/'
					|| eclipseInstallation.charAt(0) == '\\') {
				eclipseInstallation = eclipseInstallation.substring(1);
			}
			eclipseInstallation = eclipseInstallation.replace("/",
					File.separator);
			pluginFolder = String.format("%s%s%s%s%s", eclipseInstallation,
					File.separator, com.persistent.util.Messages.pluginFolder,
					File.separator, com.persistent.util.Messages.pluginId);

			uri = getRestEXE();

			instance = new WindowsAzureRestUtils();
		}

		return instance;
	}

	public String runRest(HttpVerb method, String url,
			HashMap<String, String> headers, Object body, String thumbprint)
					throws InterruptedException, CommandLineException {
		return runRest(method, url, headers, body, thumbprint, false);
	}
    
    // this method is currently used just for the CreateDeployment request. who's body may be very long so we dont want to pass it as an xml string to the command line.
	public String runRest(HttpVerb method, String url,
			HashMap<String, String> headers, Object body, String thumbprint, boolean fileBody)
					throws InterruptedException, CommandLineException {
		List<String> commandArgs = new ArrayList<String>();
		commandArgs.add(uri);
		commandArgs.add("--request");
		commandArgs.add("/verb:" + method);
		commandArgs.add("/url:" + url);
		commandArgs.add("/thumbprint:" + thumbprint);

//		StringBuilder command = new StringBuilder();
//		command.append("" + uri + " ");
//		command.append("--request ");
//		command.append("/verb:" + method + " ");
//		command.append("/url:\"" + url + "\" ");
//
//		command.append("/thumbprint:\"" + thumbprint + "\" ");
		List<String> headersList = appendHeades(headers);
		commandArgs.addAll(headersList);
		
		if (fileBody) {

			String base64Body = objectToBase64EncodedString(body);

			File bodyFile;
			try {
				bodyFile = writeXmlToFile(base64Body); // save the base 64 encoded body string to a file. this file we be read by the .NET code and decoded.
			} catch (IOException e) {
				throw new CommandLineException(e);
			}

//			command.append("/filebody:\"" + bodyFile + "\" "); // appending the filepath of the file containing the body to the command. this is the fix for bug 454, shortning the command greatly.
			commandArgs.add("/filebody:" + bodyFile);
		} else {
			String bodyContent = appendBody(body);
			
			if (bodyContent != null) {
				commandArgs.add(bodyContent);
			}
		}

		return execute(commandArgs);
	}

	private String objectToBase64EncodedString(Object object) throws CommandLineException {

		JAXBContext context = ModelFactory.createInstance();

		try {
			StringWriter sw = new StringWriter();

			Marshaller m = context.createMarshaller();

			m.marshal(object, sw);

			String xmlString = sw.getBuffer().toString()
					.replace("ns1:", "").replace(":ns1", "")
					.replace("ns2:", "").replace(":ns2", "")
					.replace("ns3:", "").replace(":ns3", "");

			xmlString = Base64
					.encode(xmlString.getBytes("UTF-8"));
			return xmlString;
		} catch (JAXBException e) {
			Activator.getDefault().log(Messages.error, e);
			throw new CommandLineException(e);
		} catch (UnsupportedEncodingException e) {
			Activator.getDefault().log(Messages.error, e);
			throw new CommandLineException(e);
		}	
	}

	private File writeXmlToFile(String xml) throws IOException {
		
		String userHomeDir = System.getProperty("user.home"); // save the request to the user home directory.
															  // we don't want users to be able to view other users requests.
		
		File temp = File.createTempFile(System.currentTimeMillis() + "_b", ".body", new File(
				userHomeDir));

		BufferedWriter writer = null;
		try {
			
			writer = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(temp),CHAR_ENCODING));
			writer.write( xml);

		} finally {
			if ( writer != null) {
				writer.close( );
			}
		}
		return temp;
	}

	private String appendBody(Object body) throws CommandLineException {
		if (body != null) {

			JAXBContext context = ModelFactory.createInstance();

			try {
				StringWriter sw = new StringWriter();

				Marshaller m = context.createMarshaller();

				m.marshal(body, sw);

				String xmlString = sw.getBuffer().toString()
						.replace("ns1:", "").replace(":ns1", "")
						.replace("ns2:", "").replace(":ns2", "")
						.replace("ns3:", "").replace(":ns3", "");

				xmlString = Base64
						.encode(xmlString.getBytes("UTF-8"));

				return "/body:" + xmlString;
			} catch (JAXBException e) {
				Activator.getDefault().log(Messages.error, e);
				throw new CommandLineException(e);
			} catch (UnsupportedEncodingException e) {
				Activator.getDefault().log(Messages.error, e);
				throw new CommandLineException(e);
			}

		}
		return null;
	}

	private static String getRestEXE() {

		try {

			File pluginDir = new File(pluginFolder);
			if (!pluginDir.exists()) {
				pluginDir.mkdirs();
			}
			File restUtils = new File(pluginFolder, "restutil.exe");

			boolean fileCreated = false;
			if (!restUtils.exists()) {
				fileCreated = restUtils.createNewFile();
			}

			InputStream input = Activator.class.getClassLoader()
					.getResourceAsStream("restutil.exe");

			FileOutputStream output = null;

			try {
				if (fileCreated) {
					output = new FileOutputStream(restUtils);
					int b = 0;

					while ((b = input.read()) > -1) {
						output.write(b);
					}
				}

			} finally {
				if (input != null)
					input.close();
				if (output != null)
					output.close();
			}

		} catch (FileNotFoundException e) {
			Activator.getDefault().log(Messages.error, e);
		} catch (IOException e) {
			Activator.getDefault().log(Messages.error, e);
		}

		return '"' + pluginFolder + File.separator + "restutil.exe" + '"';
	}

	private List<String> appendHeades(HashMap<String, String> headers) {
		List<String> commandArgs = new ArrayList<String>();
		if (headers != null && headers.size() > 0) {
			Iterator<Entry<String, String>> iterator = headers.entrySet()
					.iterator();

			StringBuilder head = new StringBuilder();

			for (int i = 0; iterator.hasNext(); i++) {
				Entry<String, String> e = iterator.next();
				head.append(e.getKey() + ":" + e.getValue());
				if (i < headers.size() - 1) {
					head.append(',');
				}
			}

			commandArgs.add("/header:" + head.toString());
		}
		return commandArgs;
	}

	private String execute(List<String> command) throws CommandLineException,
	InterruptedException {
		StringBuilder result = new StringBuilder();
		String error = "";
		InputStream inputStream = null;
		InputStream errorStream = null;
		BufferedReader br = null;
		BufferedReader ebr = null;
		Process process = null;
		try {
			process = new ProcessBuilder(command).start();

			inputStream = process.getInputStream();
			errorStream = process.getErrorStream();
			br = new BufferedReader(new InputStreamReader(inputStream));

			String line = null;
			while ((line = br.readLine()) != null) {

				result.append(line);
			}

			process.waitFor();
			ebr = new BufferedReader(new InputStreamReader(errorStream));
			error = ebr.readLine();
			if (error != null && (!error.equals(""))) {
				Activator.getDefault().log(error + command, null);
				throw new CommandLineException(error);
			}
		} catch (CommandLineException e) {
			Activator.getDefault().log(Messages.error, e);
			throw new CommandLineException(e.getMessage(), e);
		} catch (IOException e) {
			Activator.getDefault().log(Messages.error, e);
			throw new CommandLineException(e.getMessage(), e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (errorStream != null) {
					errorStream.close();
				}
				if (br != null) {
					br.close();
				}
				if (ebr != null) {
					ebr.close();
				}
			} catch (IOException e) { //TBD: Need to revisit this for better handling
				Activator.getDefault().log(Messages.error, e);
				throw new CommandLineException(e.getMessage(), e);
			}
		}
		return result.toString();
	}

	public String installPublishSettings(File file, String password)
			throws InterruptedException, CommandLineException {
		List<String> commandArgs = new ArrayList<String>();
		commandArgs.add(uri);
		commandArgs.add("--install");
		commandArgs.add("/path:" + file.getPath());
//		StringBuilder command = new StringBuilder();
//		command.append("" + uri + " ");
//		command.append("--install ");
//		command.append("/path:\"" + file.getPath() + "\" ");

		if (password != null && !password.isEmpty()) {
//			command.append("/password:\"" + password + "\"");
			commandArgs.add("/password:" + password);
		}
		return execute(commandArgs);
	}

//	private String execute(StringBuilder command) throws InterruptedException,
//	CommandLineException {
//		String result = null;
//		try {
//			result = execute(command.toString());
//		} catch (CommandLineException e) {
//			Activator.getDefault().log(Messages.error, e);
//			throw new CommandLineException(e.getMessage(), e);
//		} catch (IOException e) {
//			Activator.getDefault().log(Messages.error, e);
//			throw new CommandLineException(e.getMessage(), e);
//		}
//		return result;
//	}

	public String runStorage(HttpVerb method, String url, String storageKey,
			HashMap<String, String> headers, Object body)
					throws InterruptedException, CommandLineException {
		List<String> commandArgs = new ArrayList<String>();
		commandArgs.add(uri);
		commandArgs.add("--request");
		commandArgs.add("/verb:" + method);
		commandArgs.add("/url:" + url);
		commandArgs.add("/key:" + storageKey);
		
//		StringBuilder command = new StringBuilder();
//		command.append("" + uri + " ");
//		command.append("--request ");
//		command.append("/verb:" + method + " ");
//		command.append("/url:\"" + url + "\" ");
//		command.append("/key:\"" + storageKey + "\" ");

		List<String> headersList = appendHeades(headers);
		if (headersList.size() > 0) {
			commandArgs.addAll(headersList);
		}

		if (body != null) {

			if (body instanceof byte[]) {
				File temp;
				try {
					
					String userHomeDir = System.getProperty("user.home");
					
					temp = File.createTempFile(System.currentTimeMillis() + "_b", ".chunck", new File(
							userHomeDir));

					FileOutputStream output = new FileOutputStream(temp);

					byte[] buff = (byte[]) body;

					try {
						output.write(buff, 0, buff.length);
					} finally {
						output.close();
					}
//					command.append("/filebody:\"" + temp + "\" ");
					commandArgs.add("/filebody:" + temp);

				} catch (IOException e) {
					Activator.getDefault().log(Messages.error, e);
					throw new CommandLineException(e);
				}

			} else {
				String bodyContent = appendBody(body);
				if (bodyContent != null) {
					commandArgs.add(bodyContent);
				}
			}
		}
		return execute(commandArgs);
	}

	public void launchRDP(Deployment deployment, String userName) {
		try {

			List<RoleInstance> instances = deployment.getRoleInstanceList()
					.getRoleInstanceList();

			RoleInstance instance = instances.get(0);

			StringBuilder command = new StringBuilder();

			URL url = new URL(deployment.getUrl());

			command.append("full address:s:" + url.getHost() + "\r\n");
			command.append("username:s:" + userName + "\r\n");
			command.append(String.format(
					"LoadBalanceInfo:s:Cookie: mstshash=%s#%s",
					instance.getRoleName(), instance.getInstanceName()));

			String fileName = String.format("%s\\%s-%s.rdp", pluginFolder,
					new String(Base64.decode(deployment.getLabel()),
							"UTF-8"), instance.getInstanceName());

			File file = new File(fileName);
			boolean fileCreated = false;
			if (!file.exists()) {
				fileCreated = file.createNewFile();
			}

			if (fileCreated) {
				FileOutputStream output = new FileOutputStream(file);

				output.write(command.toString().getBytes("UTF-8"));

				output.close();
				String commandArgs[] = {"cmd.exe","/C",fileName};
				new ProcessBuilder(commandArgs).start();
			}

		} catch (IOException e) {
			Activator.getDefault().log(Messages.error, e);
		}
	}

	public static PublishData parse(File file) throws JAXBException {

		PublishData publishData = null;

		try {
			JAXBContext context = JAXBContext.newInstance(PublishData.class);

			publishData = (PublishData) context.createUnmarshaller().unmarshal(
					file);
		} catch (JAXBException e) {
			Activator.getDefault().log(Messages.error, e);
			throw e;
		}

		return publishData;
	}

	public static PublishData parsePfx(File file) throws Exception {
		PublishData publishData = new PublishData();

		PublishProfile profile = new PublishProfile();
		FileInputStream input = null;
		DataInputStream dis = null;

		byte[] buff = new byte[(int) file.length()];
		try {
			input = new FileInputStream(file);
			dis = new DataInputStream(input);
			dis.readFully(buff);
			profile.setManagementCertificate(Base64.encode(buff)); //$NON-NLS-1$
		} catch (FileNotFoundException e) {
			Activator.getDefault().log(Messages.error, e);
			throw e;
		} catch (IOException e) {
			Activator.getDefault().log(Messages.error, e);
			throw e;
		} finally {
			if (input != null) {
				input.close();
			}
			if (dis != null) {
				dis.close();
			}
		}

		publishData.setPublishProfile(profile);

		return publishData;
	}

}
