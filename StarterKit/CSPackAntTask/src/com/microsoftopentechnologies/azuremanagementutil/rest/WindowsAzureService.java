/**
* Copyright 2015 Microsoft Open Technologies, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.microsoftopentechnologies.azuremanagementutil.rest;

public interface WindowsAzureService {

	final static String X_MS_DATE = "x-ms-date";
	final static String AUTHORIZATION = "Authorization";
	final static String ACCEPT_CHARSET = "Accept-Charset";

	final static String X_MS_REQUEST_ID = "x-ms-request-id";
	final static String X_MS_VERSION = "x-ms-version";
	final static String CONTENT_TYPE = "Content-Type";
	final static String CONTENT_LENGTH = "Content-Length";

	// Service Management
	final static String SUBSCRIPTION_ID = "<subscription-id>";
	final static String SERVICE_NAME = "<service-name>";
	final static String REQUEST_ID = "<request-id>";
	final static String DEPLOYMENT_NAME = "<deployment-name>";
	final static String DEPLOYMENT_SLOT_NAME = "<deployment-slot-name>";

	final static String GET_OPERTN_STAT = "/operations/<request-id>";
	final static String LIST_STRG_ACC = "/services/storageservices";
	final static String LIST_AFF_GRPS = "/affinitygroups";
	final static String LIST_HOST_SERV = "/services/hostedservices";
	final static String HOST_SERV = "/<service-name>";
	final static String ADD_CERT = "/<service-name>/certificates";
	final static String CREATE_DPLY = "/deploymentslots/<deployment-slot-name>";
	final static String DPLY_NAME = "/deployments/<deployment-name>";

	// Storage Service
	final static String STRG_ACC = "<storage-account>";
	final static String STRG_CONTAINER = "<container>";
	final static String BLOCK_ID = "<block-id>";

	final static String LIST_CONTAINERS = "?comp=list";
	final static String PUT_BLOB = "<container>";
	final static String PUT_BLOCK = "?comp=block&blockid=<block-id>";
	final static String PUT_BLOCK_LIST = "?comp=blocklist";
	final static int REST_SERVICE_MAX_RETRY_COUNT = 7; 
}
