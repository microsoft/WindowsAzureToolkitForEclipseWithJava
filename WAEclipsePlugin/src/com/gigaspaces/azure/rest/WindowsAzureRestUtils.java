// CHECKSTYLE:OFF


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

package com.gigaspaces.azure.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.core.runtime.Platform;

import waeclipseplugin.Activator;
import com.gigaspaces.azure.model.Base64Persistent;
import com.gigaspaces.azure.model.Deployment;
import com.gigaspaces.azure.model.ModelFactory;
import com.gigaspaces.azure.model.RoleInstance;
import com.gigaspaces.azure.util.CommandLineException;
import com.gigaspaces.azure.util.Messages;
import com.gigaspaces.azure.util.PublishData;
import com.gigaspaces.azure.util.PublishProfile;

public class WindowsAzureRestUtils {

	private static String PLUGIN_FOLDER;
	private static String URI;

	private static WindowsAzureRestUtils INSTANCE;

	public static synchronized WindowsAzureRestUtils getInstance() {

		if (INSTANCE == null) {
			String eclipseInstallation = Platform.getInstallLocation().getURL()
					.getPath().toString();
			if (eclipseInstallation.charAt(0) == '/'
					|| eclipseInstallation.charAt(0) == '\\') {
				eclipseInstallation = eclipseInstallation.substring(1);
			}
			eclipseInstallation = eclipseInstallation.replace("/",
					File.separator);
			PLUGIN_FOLDER = String.format("%s%s%s%s%s", eclipseInstallation,
					File.separator, com.persistent.util.Messages.pluginFolder,
					File.separator, com.persistent.util.Messages.pluginId);

			URI = getRestEXE();

			INSTANCE = new WindowsAzureRestUtils();
		}

		return INSTANCE;
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
		StringBuilder command = new StringBuilder();

		command.append("" + URI + " ");
		command.append("--request ");
		command.append("/verb:" + method + " ");
		command.append("/url:\"" + url + "\" ");

		command.append("/thumbprint:\"" + thumbprint + "\" ");
		appendHeades(headers, command);
		if (fileBody) {

			String base64Body = objectToBase64EncodedString(body);

			File bodyFile;
			try {
				bodyFile = writeXmlToFile(base64Body); // save the base 64 encoded body string to a file. this file we be read by the .NET code and decoded.
			} catch (IOException e) {
				throw new CommandLineException(e);
			}

			command.append("/filebody:\"" + bodyFile + "\" "); // appending the filepath of the file containing the body to the command. this is the fix for bug 454, shortning the command greatly.

		} else {
			appendBody(body, command);
		}

		return execute(command);
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

			xmlString = Base64Persistent
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
			writer = new BufferedWriter( new FileWriter( temp.getAbsolutePath()));
			writer.write( xml);

		} finally {
			if ( writer != null) {
				writer.close( );
			}
		}
		return temp;
	}

	private void appendBody(Object body, StringBuilder command) throws CommandLineException {
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

				xmlString = Base64Persistent
						.encode(xmlString.getBytes("UTF-8"));

				command.append(" /body:\"" + xmlString + "\" ");
			} catch (JAXBException e) {
				Activator.getDefault().log(Messages.error, e);
				throw new CommandLineException(e);
			} catch (UnsupportedEncodingException e) {
				Activator.getDefault().log(Messages.error, e);
				throw new CommandLineException(e);
			}

		}
	}

	private static String getRestEXE() {

		try {

			File pluginDir = new File(PLUGIN_FOLDER);
			if (!pluginDir.exists()) {
				pluginDir.mkdirs();
			}
			File restUtils = new File(PLUGIN_FOLDER, "restutil.exe");

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

		return '"' + PLUGIN_FOLDER + File.separator + "restutil.exe" + '"';
	}

	private void appendHeades(HashMap<String, String> headers,
			StringBuilder command) {
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

			command.append(" /header:\"" + head.toString() + "\" ");
		}
	}

	private String execute(String command) throws CommandLineException,
	IOException, InterruptedException {
		StringBuilder result = new StringBuilder();
		String error = "";
		Runtime runtime = Runtime.getRuntime();
		InputStream inputStream = null;
		InputStream errorStream = null;
		BufferedReader br = null;
		BufferedReader ebr = null;
		Process process = runtime.exec(command);
		try {

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
		} finally {
			process.destroy();
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
		}
		return result.toString();
	}

	public String installPublishSettings(File file, String password)
			throws InterruptedException, CommandLineException {
		StringBuilder command = new StringBuilder();
		command.append("" + URI + " ");
		command.append("--install ");
		command.append("/path:\"" + file.getPath() + "\" ");

		if (password != null && !password.isEmpty()) {
			command.append("/password:\"" + password + "\"");
		}
		return execute(command);
	}

	private String execute(StringBuilder command) throws InterruptedException,
	CommandLineException {
		String result = null;
		try {
			result = execute(command.toString());
		} catch (CommandLineException e) {
			Activator.getDefault().log(Messages.error, e);
			throw new CommandLineException(e.getMessage(), e);
		} catch (IOException e) {
			Activator.getDefault().log(Messages.error, e);
			throw new CommandLineException(e.getMessage(), e);
		}
		return result;
	}

	public String runStorage(HttpVerb method, String url, String storageKey,
			HashMap<String, String> headers, Object body)
					throws InterruptedException, CommandLineException {
		StringBuilder command = new StringBuilder();

		command.append("" + URI + " ");
		command.append("--request ");
		command.append("/verb:" + method + " ");
		command.append("/url:\"" + url + "\" ");
		command.append("/key:\"" + storageKey + "\" ");

		appendHeades(headers, command);

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
					command.append("/filebody:\"" + temp + "\" ");

				} catch (IOException e) {
					Activator.getDefault().log(Messages.error, e);
					throw new CommandLineException(e);
				}

			} else {
				appendBody(body, command);
			}
		}
		return execute(command);
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

			String fileName = String.format("%s\\%s-%s.rdp", PLUGIN_FOLDER,
					new String(Base64Persistent.decode(deployment.getLabel()),
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

				Runtime.getRuntime().exec("cmd.exe /C \"" + fileName + "\"");
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
			profile.setManagementCertificate(Base64Persistent.encode(buff)); //$NON-NLS-1$
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
