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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import waeclipseplugin.Activator;

public abstract class WARest {

	class FindReplacePatten{
		public String pattern;
		public String value;
		
		public String replace(String content){
		  return content.replace(pattern, value);
		}
	}
	
	public final static String X_MS_REQUEST_ID = Messages.requestId; 
	public final static String X_MS_VERSION = Messages.version; 
	public final static String X_MS_DATE = Messages.date; 
	public final static String CONTENT_TYPE = Messages.contentType; 
	public final static String ACCEPT_CHARSET = "Accept-Charset";  //$NON-NLS-1$
	public final static String CONTENT_LENGTH = Messages.contentLen; 
	public final static String AUTHORIZATION = Messages.authorization; 

	public final static DateFormat DATE_TIME_FORMAT = new SimpleDateFormat(
			Messages.dateTimeFormat, Locale.US); 

//	protected WARest() {
//		dateTimeFormatter.setTimeZone(TimeZone.getTimeZone(Messages.timeZone)); 
//	}

	protected void addXMsVer2011(HashMap<String, Object> headers) {
		// addx_ms_version2011_08_01
		headers.put(X_MS_VERSION, Messages.msVersion1); 
	}

	protected void addXMsVer2010(HashMap<String, Object> headers) {
		// addx_ms_version2010_10_28
		headers.put(X_MS_VERSION, Messages.msVersion2); 
	}

	protected void addXMsVer2009(HashMap<String, Object> headers) {
		// addx_ms_version2009_10_01
		headers.put(X_MS_VERSION, Messages.msVersion3); 
	}
	
	protected String path(String[] path, String[][] kv) {

		String p1 = "";  //$NON-NLS-1$

		for (int i = 0; i < path.length; i++) {

			if (kv[i] == null)
			{
				p1 = p1.concat(path[i]);
				continue;
			}

			p1 = p1.concat(path[i]).replace(kv[i][0], kv[i][1]);
		}

		return p1;
	}

	protected String path(String pattern,String key,String value){
		
		return pattern.replace(key, value);
	}
		
	protected byte[] addContentLength(HashMap<String, Object> headers, Object body) {
		byte [] buff = null;
		try {
			
			JAXBContext context = JAXBContext.newInstance(body.getClass());
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try {
				context.createMarshaller().marshal(body, stream);
				buff = stream.toByteArray();
				Activator.getDefault().log(new String(buff));
				
				headers.put(CONTENT_LENGTH, stream.size());

			} finally {
				stream.close();
			}
		} 
		catch (JAXBException e) {
			Activator.getDefault().log(Messages.error,e);
		} 
		catch (IOException e) {
			Activator.getDefault().log(Messages.error,e);
		}
		
		return buff;
	}
}
