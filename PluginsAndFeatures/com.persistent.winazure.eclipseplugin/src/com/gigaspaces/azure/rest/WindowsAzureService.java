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

//	final static String MNGMT_SERV_URL = "https://management.core.windows.net/<subscription-id>"; //$NON-NLS-1$
	final static String GET_OPERTN_STAT = "/operations/<request-id>"; //$NON-NLS-1$
	final static String LIST_STRG_ACC = "/services/storageservices"; //$NON-NLS-1$
	final static String GET_STRG_KEYS = "/<service-name>/keys"; //$NON-NLS-1$
	final static String LIST_LOC = "/locations"; //$NON-NLS-1$
	final static String LIST_AFF_GRPS = "/affinitygroups"; //$NON-NLS-1$
	final static String LIST_HOST_SERV = "/services/hostedservices"; //$NON-NLS-1$
	final static String HOST_SERV = "/<service-name>"; //$NON-NLS-1$
	final static String ADD_CERT = "/<service-name>/certificates"; //$NON-NLS-1$
	final static String CREATE_DPLY = "/deploymentslots/<deployment-slot-name>"; //$NON-NLS-1$
	final static String DPLY_NAME = "/deployments/<deployment-name>"; //$NON-NLS-1$

	// Storage Service
	final static String STRG_ACC = "<storage-account>";
	final static String STRG_CONTAINER = "<container>";
	final static String BLOCK_ID = "<block-id>";

//	final static String STRG_SERV_URL = "http://<storage-account>.blob.core.windows.net/";
	final static String LIST_CONTAINERS = "?comp=list";
	final static String PUT_BLOB = "<container>";
	final static String PUT_BLOCK = "?comp=block&blockid=<block-id>";
	final static String PUT_BLOCK_LIST = "?comp=blocklist";
}
