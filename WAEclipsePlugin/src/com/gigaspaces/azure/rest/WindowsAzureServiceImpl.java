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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import waeclipseplugin.Activator;
import com.gigaspaces.azure.model.Header;
import com.gigaspaces.azure.model.ModelFactory;
import com.gigaspaces.azure.model.Response;
import com.gigaspaces.azure.util.CommandLineException;

/**
 * The Class WindowsAzureServiceImpl.
 */
public class WindowsAzureServiceImpl implements WindowsAzureService {

	protected JAXBContext context;

	public WindowsAzureServiceImpl() {
		this.context = ModelFactory.createInstance();
	}

	protected byte[] addContentLength(HashMap<String, String> headers,
			Object body) {
		byte[] buff = null;
		try {

			JAXBContext context = JAXBContext.newInstance(body.getClass());
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try {
				context.createMarshaller().marshal(body, stream);
				buff = stream.toByteArray();
				Activator.getDefault().log(new String(buff));

				headers.put(CONTENT_LENGTH, "" + stream.size());

			} finally {
				stream.close();
			}
		} catch (JAXBException e) {
			Activator.getDefault().log(Messages.error, e);
		} catch (IOException e) {
			Activator.getDefault().log(Messages.error, e);
		}

		return buff;
	}

	protected Object deserialize(String xml) throws CommandLineException {
		Object result = null;
		try {
			InputStream is = new ByteArrayInputStream(
					xml.getBytes(Messages.utfFormat));

			Unmarshaller unmarshaller = context.createUnmarshaller();

			result = unmarshaller.unmarshal(is);

		} catch (IOException e) {
			Activator.getDefault().log(Messages.error, e);
			throw new CommandLineException(Messages.deserializationError, e);

		} catch (JAXBException e) {
			throw new CommandLineException(Messages.deserializationError, e);
		}
		return result;
	}

	protected String getXRequestId(Response<?> response) throws RestAPIException {

		for (Header header : response.getHeaders()) {
			if (X_MS_REQUEST_ID.equalsIgnoreCase(header.getName())) {
				return header.getValue();
			}
		}

		throw new RestAPIException(response);

	}

	protected void validateResponse(Response<?> response) throws RestAPIException  {
		if (response.getStatus() >= 200 && response.getStatus() < 203) {
			return;
		}

		if (response.getStatus() == 409) {
			throw new RestAPIConflictException(response);
		}

		throw new RestAPIException(response);
		
	}
}
