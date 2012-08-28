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

public interface WindowsAzureService {

	final static String X_MS_DATE = "x-ms-date";
	final static String AUTHORIZATION = "Authorization";
	final static String ACCEPT_CHARSET = "Accept-Charset"; //$NON-NLS-1$

	final static String X_MS_REQUEST_ID = "x-ms-request-id"; //$NON-NLS-1$
	final static String X_MS_VERSION = "x-ms-version"; //$NON-NLS-1$
	final static String CONTENT_TYPE = "Content-Type"; //$NON-NLS-1$
	final static String CONTENT_LENGTH = "Content-Length"; //$NON-NLS-1$

	// Service Management
	final static String SUBSCRIPTION_ID = "<subscription-id>"; //$NON-NLS-1$
	final static String SERVICE_NAME = "<service-name>"; //$NON-NLS-1$
	final static String REQUEST_ID = "<request-id>"; //$NON-NLS-1$
	final static String DEPLOYMENT_NAME = "<deployment-name>"; //$NON-NLS-1$
	// final static String deployment_slot = "<deployment-slot>";
	final static String DEPLOYMENT_SLOT_NAME = "<deployment-slot-name>"; //$NON-NLS-1$

	final static String Management_Services_URL = "https://management.core.windows.net/<subscription-id>"; //$NON-NLS-1$
	final static String Get_Operation_Status = "/operations/<request-id>"; //$NON-NLS-1$
	final static String List_Storage_Accounts = "/services/storageservices"; //$NON-NLS-1$
	final static String Get_Storage_Keys = "/<service-name>/keys"; //$NON-NLS-1$
	final static String List_Locations = "/locations"; //$NON-NLS-1$
	final static String List_Affinity_Groups = "/affinitygroups"; //$NON-NLS-1$
	final static String List_Hosted_Services = "/services/hostedservices"; //$NON-NLS-1$
	final static String Hosted_Services = "/<service-name>"; //$NON-NLS-1$
	final static String Add_Certificate = "/<service-name>/certificates"; //$NON-NLS-1$
	final static String Create_Deployment = "/deploymentslots/<deployment-slot-name>"; //$NON-NLS-1$
	final static String Deployment_Name = "/deployments/<deployment-name>"; //$NON-NLS-1$

	// Storage Service
	final static String storage_account = "<storage-account>";
	final static String storage_container = "<container>";
	final static String block_id = "<block-id>";

	final static String Storage_Service_URL = "http://<storage-account>.blob.core.windows.net/";
	final static String List_Containers = "?comp=list";
	final static String Put_Blob = "<container>";
	final static String Put_Block = "?comp=block&blockid=<block-id>";
	final static String Put_Block_List = "?comp=blocklist";
}
