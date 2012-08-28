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
package com.gigaspaces.azure.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import waeclipseplugin.Activator;

public class ModelFactory {

	private static final List<Class<?>> classes = new ArrayList<Class<?>>();

	static {
		classes.add(AffinityGroup.class);
		classes.add(AffinityGroups.class);
		classes.add(BlockList.class);
		classes.add(CertificateFile.class);
		classes.add(CertificateFileV2.class);
		classes.add(Code.class);
		classes.add(Container.class);
		classes.add(Containers.class);
		classes.add(CreateDeployment.class);
		classes.add(CreateStorageServiceInput.class);
		classes.add(CreateHostedService.class);		
		classes.add(Deployment.class);
		classes.add(DeploymentSlot.class);
		classes.add(EnumerationResults.class);
		classes.add(Error.class);
		classes.add(HostedService.class);
		classes.add(HostedServiceProperties.class);
		classes.add(HostedServices.class);
		classes.add(InputEndpoint.class);
		classes.add(InstanceStatus.class);
		classes.add(KeyName.class);
		classes.add(Location.class);
		classes.add(Locations.class);
		classes.add(Operation.class);
		classes.add(Property.class);
		classes.add(Role.class);
		classes.add(RoleInstance.class);
		classes.add(Status.class);
		classes.add(StorageService.class);
		classes.add(StorageServiceKeys.class);
		classes.add(StorageServices.class);
		classes.add(Subscription.class);
		classes.add(Response.class);
		classes.add(Header.class);
		classes.add(UpgradeStatus.class);
		classes.add(UpdateDeploymentStatus.class);
	}

	private static Class<?>[] getClasses() {
		Class<?>[] result = new Class<?>[classes.size()];
		classes.toArray(result);
		return result;
	}
	
	public synchronized static JAXBContext createInstance(){
		JAXBContext context=null;
		try {
			context= JAXBContext.newInstance(getClasses());
		} catch (JAXBException e) {			
			Activator.getDefault().log(Messages.ModelFactory_error,e);
		}
		
		return context;
	}
}
