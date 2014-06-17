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
package com.gigaspaces.azure.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import waeclipseplugin.Activator;

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
			Activator.getDefault().log(Messages.modelFactoryErr,e);
		}
		
		return context;
	}
}
