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
package com.microsoftopentechnologies.azuremanagementutil.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;


public class ModelFactory {

	private static final List<Class<?>> CLASSES = new ArrayList<Class<?>>();

	static {
		CLASSES.add(CreateStorageServiceInput.class);
		CLASSES.add(CreateHostedService.class);		
		CLASSES.add(InstanceStatus.class);
		CLASSES.add(KeyName.class);
		CLASSES.add(StorageService.class);
		CLASSES.add(StorageServices.class);
		CLASSES.add(Subscription.class);
	}

	private static Class<?>[] getClasses() {
		Class<?>[] result = new Class<?>[CLASSES.size()];
		CLASSES.toArray(result);
		return result;
	}
	
	public synchronized static JAXBContext createInstance(){
		JAXBContext context = null;
		try {
			context= JAXBContext.newInstance(getClasses());
		} catch (JAXBException e) {			
//			Activator.getDefault().log(Messages.modelFactoryErr,e);
		}
		
		return context;
	}
}
