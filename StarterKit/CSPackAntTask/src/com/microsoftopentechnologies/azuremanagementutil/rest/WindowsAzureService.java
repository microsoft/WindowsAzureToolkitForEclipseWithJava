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
