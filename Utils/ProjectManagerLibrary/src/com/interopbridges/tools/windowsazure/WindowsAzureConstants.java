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

package com.interopbridges.tools.windowsazure;

public final class WindowsAzureConstants {

	private WindowsAzureConstants() {
	}

	public static final String SERVICE_NAME = "/ServiceDefinition/@name";
	public static final String WINAZURE_PACKAGE = "/project/target/parallel/windowsazurepackage";
	public static final String WA_PACK_NAME = WINAZURE_PACKAGE
			+ "/workerrole[@name='%s']";

	public static final String PROJECT_TYPE = WINAZURE_PACKAGE
			+ "/@packagetype";
	public static final String CONFIG_FILE_NAME = WINAZURE_PACKAGE
			+ "/@configurationfilename";
	public static final String DEF_FILE_NAME = WINAZURE_PACKAGE
			+ "/@definitionfilename";
	public static final String PORTAL_URL = WINAZURE_PACKAGE + "/@portalurl";

	public static final String CONFIG_OSFAMILY = "/ServiceConfiguration/@osFamily";

	public static final String ROLE = "/ServiceConfiguration/Role";
	public static final String ROLE_NAME = ROLE + "[@name='%s']";
	public static final String ROLE_INSTANCES = ROLE_NAME + "/Instances";
	public static final String ROLE_COUNT = ROLE_INSTANCES + "/@count";
	public static final String CONFIG_SETTING = ROLE
			+ "/ConfigurationSettings/Setting";
	public static final String CERTIFICATE = ROLE + "/Certificates/Certificate";

	public static final String WORKER_ROLE = "/ServiceDefinition/WorkerRole";
	public static final String WR_NAME = WORKER_ROLE + "[@name='%s']";
	public static final String RUNTIME = WORKER_ROLE + "[@name='%s']/Runtime";
	public static final String STARTUP_WR_NAME = WR_NAME + "/Startup";
	public static final String LOCAL_RESOURCES = WR_NAME + "/LocalResources";
	public static final String TASK_CMD_LINE = "util/.start.cmd .startup.cmd";
	public static final String STARTUP_WITH_STARTUP_CMD = STARTUP_WR_NAME
			+ "/Task[@commandLine='" + TASK_CMD_LINE + "']";

	public static final String ENVIRONMENT = STARTUP_WITH_STARTUP_CMD
			+ "/Environment";
	public static final String ENTRYPOINT = RUNTIME + "/EntryPoint";
	public static final String VARIABLE = ENVIRONMENT + "/Variable";
	public static final String JAVA_OPTIONS_ENV_VAR = "_JAVA_OPTIONS";
	public static final String JAVA_HOME_ENV_VAR = "JAVA_HOME";
	public static final String VAR_WITH_SPECIFIC_NAME = ENVIRONMENT
			+ "/Variable[@name='%s']";
	public static final String ROLE_INSTANCE_NODE = VAR_WITH_SPECIFIC_NAME
			+ "/RoleInstanceValue[@xpath=\"/RoleEnvironment/CurrentInstance/"
			+ "LocalResources/LocalResource[@name='%s']/@path\"]";
	public static final String VAR_LS_ENV_NAME = VARIABLE
			+ "[./RoleInstanceValue/@xpath=\"/RoleEnvironment/CurrentInstance/"
			+ "LocalResources/LocalResource[@name='%s']/@path\"]/@name";
	public static final String ROLE_INSTANCE_PATH = "/RoleEnvironment/CurrentInstance/LocalResources/LocalResource[@name='%s']/@path";
	public static final String VARIABLE_JOP = ENVIRONMENT
			+ "/Variable[@name='_JAVA_OPTIONS']";
	public static final String ENDPOINT_WR_NAME = WR_NAME + "/Endpoints";
	public static final String STARTUP_TASK_COMMENTS = "Do not delete this startup task or insert tasks before it. "
			+ "It was created by Azure Plugin for Eclipse with Java to enable proxy configuration. ";
	public static final String DEF_FILE_STARTUP_ELEMENT_NAME = "Startup";
	public static final String DEF_FILE_IMPORTS_ELEMENT_NAME = "Imports";
	public static final String DEF_FILE_IMPORT_ELEMENT_NAME = "Import";

	public static final String SAMPLE_TASK_COMMENT = " Sample startup task calling startup.cmd from the role's approot folder ";
	public static final String SAMPLE_ENTRY_PT_COMMENT = " Sample entry point calling run.cmd from the role's approot folder ";
	public static final String STARTUP_TASK_WR = STARTUP_WR_NAME + "/Task";
	public static final String STARTUP_TASK_CMD = STARTUP_WR_NAME
			+ "/Task[@commandLine='%s %s %s']";
	public static final String STARTUP_TASK_STARTS_WITH = STARTUP_WR_NAME
			+ "/Task[starts-with(@commandLine,'%s')]";
	public static final String TASK_CMD_VALUE = ".arrconfig\\ConfigureARR.cmd %s %s %s";
	public static final String SSL_TASK_CMD_VALUE = ".arrconfig\\ConfigureARR.cmd %s %s %s %s %s";
	public static final String SSL_STORE_NAME = "My";
	public static final String TASK_CMD_ONLY = ".arrconfig\\ConfigureARR.cmd";
	public static final String OLD_TASK_CMD_ONLY = ".sessionaffinity\\ConfigureARR.cmd";
	public static final String DEF_FILE_TASK_ELEMENT_NAME = "Task";
	public static final String DEF_FILE_ENV_NAME = "Environment";
	public static final String DEF_FILE_VAR_ELE_NAME = "Variable";
	public static final String DEF_FILE_ENV_RIV_NAME = "RoleInstanceValue";

	public static final String DEF_FILE_INTERNAL_ELEMENT_NAME = "InternalEndpoint";
	public static final String DEF_FILE_FIXED_ELEMENT_NAME = "FixedPort";

	public static final String INPUTS_WR_NAME = ENDPOINT_WR_NAME
			+ "/InputEndpoint";
	public static final String INTERNAL_WR_NAME = ENDPOINT_WR_NAME
			+ "/InternalEndpoint";
	public static final String INSTANCE_WR_NAME = ENDPOINT_WR_NAME
			+ "/InstanceInputEndpoint";
	public static final String LOCAL_STORAGE = WR_NAME
			+ "/LocalResources/LocalStorage";
	public static final String LS_NAME = WR_NAME
			+ "/LocalResources/LocalStorage[@name='%s']";

	public static final String ENDPOINT = "/ServiceDefinition/WorkerRole[@name='%s']/Endpoints";
	public static final String INTERNAL_ENDPOINT = ENDPOINT
			+ "/InternalEndpoint[@name='%s']";
	public static final String INTERNAL_FIXED_ENDPOINT = ENDPOINT
			+ "/InternalEndpoint[@name='%s']/FixedPort";
	public static final String INPUT_ENDPOINT = ENDPOINT
			+ "/InputEndpoint[@name='%s']";
	public static final String INSTANCE_ENDPOINT = ENDPOINT
			+ "/InstanceInputEndpoint[@name='%s']";

	public static final String PROJ_PROPERTY_DESC = "Stores properties used by Azure project";
	public static final String PROJ_PROPERTY = "/project/target[@name='waprojectproperties']";
	public static final String ROOT_ELEMENT = "/project";
	public static final String PROJ_GLOBAL_PROPERTY = "/project/property[@name='%s']";
	public static final String PROJ_GLOBAL_PROPERTY_LOCATION = "/project/property[@name='%s']/@location";
	public static final String PROJ_GLOBAL_PROPERTY_VALUE = "/project/property[@name='%s']/@value";
	public static final String PROJ_GLOBAL_TARGET = "/project/target[@name='%s']";
	public static final String PROJ_PROPERTY_ELEMENT_NAME = "property";
	public static final String PROJ_PROPERTIES = PROJ_PROPERTY + "/"
			+ PROJ_PROPERTY_ELEMENT_NAME;
	public static final String CREATE_PKG_TARGET = "/project/target[@name='createwapackage']";
	public static final String AZURE_PKG_TASK_DEF = "/project/target[@name='createwapackage']/taskdef[@name='windowsazurepackage']";
	public static final String WA_PACK_ROLE = CREATE_PKG_TARGET
			+ "/parallel/windowsazurepackage/workerrole[@name='%s']";
	public static final String WA_PACK_STARTUPENV = WA_PACK_ROLE
			+ "/startupenv";
	public static final String WA_PACK_SENV_NAME = WA_PACK_ROLE
			+ "/startupenv[@name='%s']";
	public static final String WA_PACK_SENV_TYPE = WA_PACK_ROLE
			+ "/startupenv[@type='%s']";
	public static final String WA_PACK_SENV_NAME_VALUE = WA_PACK_ROLE
			+ "/startupenv[@name='%s'][@value='%s']";
	public static final String EMULATORTOOLSDIR = CREATE_PKG_TARGET
			+ "/parallel/windowsazurepackage/@emulatortoolsdir";
	public static final String PACKAGEDIR = CREATE_PKG_TARGET
			+ "/parallel/windowsazurepackage/@packagedir";
	public static final String WASDKVER_PROP = CREATE_PKG_TARGET
			+ "/property[@name='wasdkversion']";
	public static final String PROJ_REMOTE_ACCESS = PROJ_PROPERTY
			+ "/property[@name='project.enableremoteaccess']";
	public static final String PROJ_REMOTE_DESKTOP = PROJ_PROPERTY
			+ "/property[@name='cert.windowsazureremotedesktop']";
	public static final String PROJ_IMPORT_ACCESS = WORKER_ROLE
			+ "/Imports/Import[@moduleName='RemoteAccess'] | " + WORKER_ROLE
			+ "/Imports/Import[@moduleName='RemoteForwarder']";
	public static final String THUMBPRINT_NOTE = "NOTE: The certificate with the "
			+ "thumbprint 875F1656A34D93B266E71BF19C116C39F16B6987 refers to";
	public static final String IMPORT = WR_NAME + "/Imports";
	public static final String IMPORT_MNANE = IMPORT
			+ "/Import[@moduleName='RemoteAccess']";
	public static final String IMPORT_WEB_DEPLOY = IMPORT
			+ "/Import[@moduleName='WebDeploy']";

	public static final String DATE_FORMAT = "yyyy-MM-dd'T23:59:59.0000000-08:00'";
	public static final String CREATOR_VER = PROJ_PROPERTY
			+ "/property[@name='creator.version']";
	public static final String V17_VERSION = "1.7.0";
	public static final String VERSION = "2.8.0";
	public static final String SA_INPUT_ENDPOINT = PROJ_PROPERTY
			+ "/property[@name='project.%s.sessionaffinity.inputendpoint']";
	public static final String SA_INTERNAL_ENDPOINT = PROJ_PROPERTY
			+ "/property[@name='project.%s.sessionaffinity.internalendpoint']";
	public static final String ARR_INTERNAL_ENDPOINT_SUFFIX = "_ARR_PROXY";
	public static final String ARR_SSL_REDIRECT_ENDPOINT_SUFFIX = "_ARR_PROXY_REDIRECT";

	public static final String SA_INPUT_ENDPOINT_NAME_PROP = "project.%s.sessionaffinity.inputendpoint";
	public static final String SA_INTERNAL_ENDPOINT_NAME_PROP = "project.%s.sessionaffinity.internalendpoint";

	public static final String SSL_INPUT_ENDPOINT = PROJ_PROPERTY
			+ "/property[@name='project.%s.ssloffloading.inputendpoint']";
	public static final String SSL_INTERNAL_ENDPOINT = PROJ_PROPERTY
			+ "/property[@name='project.%s.ssloffloading.internalendpoint']";
	public static final String SSL_REDIRECTION_ENDPOINT = PROJ_PROPERTY
			+ "/property[@name='project.%s.ssloffloading.redirectendpoint']";
	public static final String SSL_CERT_NAME = PROJ_PROPERTY
			+ "/property[@name='project.%s.ssloffloading.cert.name']";
	public static final String SSL_CERT_FINGERPRINT = PROJ_PROPERTY
			+ "/property[@name='project.%s.ssloffloading.cert.fingerprint']";

	public static final String SSL_INPUT_ENDPOINT_NAME_PROP = "project.%s.ssloffloading.inputendpoint";
	public static final String SSL_INTERNAL_ENDPOINT_NAME_PROP = "project.%s.ssloffloading.internalendpoint";
	public static final String SSL_REDIRECT_ENDPOINT_NAME_PROP = "project.%s.ssloffloading.redirectendpoint";
	public static final String SSL_CERT_NAME_PROP = "project.%s.ssloffloading.cert.name";
	public static final String SSL_CERT_FINGERPRINT_PROP = "project.%s.ssloffloading.cert.fingerprint";

	public static final String BLANK_RNAME = "Role name is blank in Definition file";
	public static final String PMGR_NULL = "Project Manager instance is null";
	public static final String EP_NOT_FOUND = "Not able to find EndPoint with specified port";

	public static final String REMOTEACCESS_ENABLED = "Microsoft.WindowsAzure.Plugins.RemoteAccess.Enabled";
	public static final String REMOTEFORWARDER_ENABLED = "Microsoft.WindowsAzure.Plugins.RemoteForwarder.Enabled";
	public static final String REMOTEACCESS_USERNAME = "Microsoft.WindowsAzure.Plugins.RemoteAccess.AccountUsername";
	public static final String REMOTEACCESS_PASSWORD = "Microsoft.WindowsAzure.Plugins.RemoteAccess.AccountEncryptedPassword";
	public static final String REMOTEACCESS_EXPIRY = "Microsoft.WindowsAzure.Plugins.RemoteAccess.AccountExpiration";
	public static final String REMOTEACCESS_FINGERPRINT = "Microsoft.WindowsAzure.Plugins.RemoteAccess.PasswordEncryption";
	public static final String CONFIG_ROLE_SET = ROLE_NAME
			+ "/ConfigurationSettings";
	public static final String RA_ROLE_UNAME = CONFIG_ROLE_SET
			+ "/Setting[@name='" + WindowsAzureConstants.REMOTEACCESS_USERNAME
			+ "']";
	public static final String RA_ROLE_UNAME_VAL = RA_ROLE_UNAME + "/@value";
	public static final String RA_ROLE_PWD = CONFIG_ROLE_SET
			+ "/Setting[@name='" + WindowsAzureConstants.REMOTEACCESS_PASSWORD
			+ "']";
	public static final String RA_ROLE_PWD_VAL = RA_ROLE_PWD + "/@value";
	public static final String RA_ROLE_EXPIRY = CONFIG_ROLE_SET
			+ "/Setting[@name='" + WindowsAzureConstants.REMOTEACCESS_EXPIRY
			+ "']";
	public static final String RA_ROLE_EXPIRY_VAL = RA_ROLE_EXPIRY + "/@value";

	public static final String CERT_ROLE = ROLE_NAME + "/Certificates";
	public static final String RA_ROLE_FPRINT = CERT_ROLE
			+ "/Certificate[@name='"
			+ WindowsAzureConstants.REMOTEACCESS_FINGERPRINT + "']";
	public static final String RA_ROLE_TPRINT_TPRINT = RA_ROLE_FPRINT
			+ "/@thumbprint";
	public static final String FPRINT_ALL = CERTIFICATE + "[@name='"
			+ WindowsAzureConstants.REMOTEACCESS_FINGERPRINT + "']";
	public static final String RA_ROLE_ENABLED = CONFIG_ROLE_SET
			+ "/Setting[@name='" + WindowsAzureConstants.REMOTEACCESS_ENABLED
			+ "']";
	public static final String RF_ENABLED = CONFIG_SETTING + "[@name='"
			+ WindowsAzureConstants.REMOTEFORWARDER_ENABLED + "']";
	// public static final String RA_ROLE_UNAME_VAL = RA_ROLE_UNAME + "/@value";

	public static final String TASK_CMDLINE = "./Task/@commandLine";
	public static final String TASK_EXECCONTXT = "./Task/@executionContext";

	public static final String INVALID_ARG = "Invalid argument.";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_VALUE = "value";
	public static final String ATTR_SIZEINMB = "sizeInMB";
	public static final String ATTR_CLE_ON_ROLE_RECYCLE = "cleanOnRoleRecycle";
	public static final String ATTR_CMD_LINE = "commandLine";
	public static final String ATTR_EXE_CONTEXT = "executionContext";
	public static final String ATTR_TASK_TYPE = "taskType";
	public static final String ATTR_MINPORT = "min";
	public static final String ATTR_MAXPORT = "max";
	public static final String ATTR_PORTALURL = "portalurl";

	public static final int MAX_LS_SIZE_EXTRASMALL = 19456;
	public static final int MAX_LS_SIZE_SMALL = 229376;
	public static final int MAX_LS_SIZE_MEDIUM = 500736;
	public static final int MAX_LS_SIZE_LARGE = 1022976;
	public static final int MAX_LS_SIZE_EXTRALARGE = 2087936;
	public static final int MAX_LS_SIZE_A5 = 500736;
	public static final int MAX_LS_SIZE_A6 = 1022976;
	public static final int MAX_LS_SIZE_A7 = 2087936;
	public static final int MAX_LS_SIZE_A8_A9 = 1855980;
	public static final int MAX_LS_SIZE_STANDARD_D1 = 51200;
	public static final int MAX_LS_SIZE_STANDARD_D2 = 102400;
	public static final int MAX_LS_SIZE_STANDARD_D3 = 204800;
	public static final int MAX_LS_SIZE_STANDARD_D4 = 409600;
	public static final int MAX_LS_SIZE_STANDARD_D11 = 102400;
	public static final int MAX_LS_SIZE_STANDARD_D12 = 204800;
	public static final int MAX_LS_SIZE_STANDARD_D13 = 409600;
	public static final int MAX_LS_SIZE_STANDARD_D14 = 819200;

	public static final String COMPONENT = WA_PACK_NAME + "/component";

	public static final String ATTR_IMETHOD = "importmethod";
	public static final String ATTR_CLOUD_UPLOAD = "cloudupload";
	public static final String ATTR_CLD_ALT_SRC = "cloudaltsrc";
	public static final String ATTR_IPATH = "importsrc";
	public static final String ATTR_DMETHOD = "deploymethod";
	public static final String ATTR_DDIR = "deploydir";
	public static final String ATTR_IMPORTAS = "importas";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_CURL = "cloudsrc";
	public static final String ATTR_CKEY = "cloudkey";
	public static final String ATTR_CMTHD = "cloudmethod";
	public static final String ATTR_CLD_VAL = "cloudvalue";
	public static final String ATTR_LCNS_VAL = "licenseurl";
	public static final String ATTR_HTTP_PORT = "httpport";

	public static final String COMPONENT_IMPORTAS = COMPONENT + "[@"
			+ ATTR_IMPORTAS + "='%s']";
	public static final String COMPONENT_IPATH = COMPONENT + "[@" + ATTR_IPATH
			+ "='%s']";
	public static final String COMPONENT_IPATH_NAME = COMPONENT + "[@"
			+ ATTR_IPATH + "='%s'][not(@" + ATTR_IMPORTAS + ")]";
	public static final String COMPONENT_IPATH_NAME_IAS_EMPTY = COMPONENT
			+ "[@" + ATTR_IPATH + "='%s'][@" + ATTR_IMPORTAS + "='']";
	public static final String COMPONENT_TYPE = COMPONENT + "[@" + ATTR_TYPE
			+ "='%s']";
	public static final String COMPONENT_TYPE_PATH = COMPONENT + "[@" + ATTR_TYPE
			+ "='%s']" +"[@" + ATTR_IPATH + "='%s']";
	public static final String COMPONENT_TYPE_IMPORTAS = COMPONENT_TYPE + "[@"
			+ ATTR_IMPORTAS + "='%s']";

	public static final String ENV_PROGRAMFILES = "ProgramFiles";
	public static final String ENV_PROGRAMFILES_WOW64 = "ProgramFiles";

	public static final String EXCP = "Exception occurred: ";
	public static final String DBG_NOT_ENABLED = "Debugging is not enabled";
	public static final String DBG_STR = "-agentlib:jdwp=transport=dt_socket,server=y,address=";
	public static final String DIR_NOT_CREATED = "Directory not created : ";
	public static final String DIR_NOT_DELETED = "Directory not deleted : ";
	public static final String EXCP_RETRIEVE_DATA = "Exception occurred while retrieving data from ";
	public static final String EXCP_SAVE = "Exception occurred while saving ";
	public static final String EXCP_ROLE_UPDATES = "Exception occurred while doing role updates";
	public static final String EXCP_EMPTY_ROLENAME = "Setting empty role name";
	public static final String EXCP_SET_ROLENAME = "Exception occurred while setting role name";
	public static final String EXCP_GET_INSTANCES = "Exception occurred while getting instance count";
	public static final String EXCP_SET_INSTANCES = "Exception occurred while setting instance count";
	public static final String EXCP_EMPTY_VMSIZE = "Empty VM Size";
	public static final String EXCP_SET_VMSIZE = "Exception occurred while setting VM Size";
	public static final String EXCP_ADD_ENDPOINT = "Exception occurred while adding endpoint";
	public static final String EXCP_RETRIEVING_ENDPOINT_NAME = "Exception occurred while retrieving endpoint name";
	public static final String EXCP_DEL_ROLE = "Exception occurred while deleting role";
	public static final String EXCP_GET_INPUT_ENDPOINT_NAME = "Exception occurred while getting input endpoint name";
	public static final String EXCP_SET_INPUT_ENDPOINT_NAME = "Exception occurred while setting input endpoint name";
	public static final String EXCP_GET_INTERNAL_ENDPOINT_NAME = "Exception occurred while getting internal endpoint name";
	public static final String EXCP_EMPTY_INTERNAL_NAME = "internal endPoint name is empty";
	public static final String EXCP_SET_INTERNAL_ENDPOINT_NAME = "Exception occurred while setting internal endpoint name";
	public static final String EXCP_EMPTY_PORT = "endPoint port is empty";
	public static final String EXCP_EMPTY_PROTOCOL = "endPoint protocol is empty";
	public static final String EXCP_EMPTY_CERT = "endPoint certificate is empty";
	public static final String EXCP_EMPTY_ENDPOINT_NAME = "input endPoint port is empty";
	public static final String EXCP_EMPTY_OR_INVALID_ENDPOINT = "EndPoint is empty or not a valid end point";
	public static final String EXCP_EMPTY_ENDPOINT_OR_CERT = "Invalid Input: Either endpoint value is null or cert value is null";
	public static final String EXCP_SA_ENABLED = "Duplicate request : Session Affinity exists already for the endpoint ";
	public static final String EXCP_SSL_ENABLED = "Duplicate request : SSL configuration exists already for the endpoint ";
	public static final String EXCP_SA_INVALID_PACKAGE = "Session affinity is supported only for package type# Cloud";
	public static final String EXCP_SA_ENDPOINT_TYPE_CHANGE = "Changing the type of endpoint associated with session affinity is not allowed";
	public static final String EXCP_SET_INPUT_PORT = "Exception occurred while setting input port";
	public static final String EXCP_EMPTY_INPUT_LOCAL_PORT = "input endPoint local port is empty";
	public static final String EXCP_SET_LOCAL_PORT = "Exception occurred while setting local port";
	public static final String EXCP_EMPTY_INTERNAL_PORT = "internal endPoint port is empty";
	public static final String EXCP_SET_INTERNAL_PORT = "Exception occurred while setting internal port";
	public static final String EXCP_EMPTY_PRIVATE_PORT = "private endPoint port is empty";
	public static final String EXCP_EMPTY_FIXED_PORT = "internal fixed port is empty";
	public static final String EXCP_SET_INTERNAL_FIXED_PORT = "Exception occurred while setting internal fix port";
	public static final String EXCP_SET_TYPE = "Exception occurred while setting endpoint Type";
	public static final String EXCP_DEL_ENDPOINT = "Exception occurred while deleting endpoint";
	public static final String EXCP_INITIALIZE = "Exception occurred while initializing the project";
	public static final String EXCP_IS_AVAILABLE_ROLENAME = "Exception occurred while validating role";
	public static final String EXCP_IS_AVAILABLE_SERVICE_NAME = "Exception occurred while validating service name";
	public static final String EXCP_IS_AVAILABLE_PORT = "Exception occurred while validating port";
	public static final String EXCP_GET_SERVICE_NAME = "Exception occurred while getting service name";
	public static final String EXCP_SET_SERVICE_NAME = "Exception occurred while setting service name";
	public static final String EXCP_GET_TARGET_OS_NAME = "Exception occurred while getting target os name";
	public static final String EXCP_SET_TARGET_OS_NAME = "Exception occurred while setting target os name";
	public static final String EXCP_GET_ROLE = "Exception occurred while getting roles";
	public static final String EXCP_EMPTY_ROLE = "Role name should not be empty";
	public static final String EXCP_ADD_ROLE = "Exception occurred while adding role";
	public static final String EXCP_GET_PACKAGE_FILE = "Problem while reading package file";
	public static final String EXCP_SET_PROJECT_NAME = "Exception while setting project name";
	public static final String EXCP_GET_PROJECT_NAME = "Exception while getting project name";
	public static final String EXCP_MOVE_PROJ_FROM_TEMP = "Problem while moving project";
	public static final String EXCP_COPY_FILES = "Getting exception when copying SA files";
	public static final String EXCP_SET_PACKAGE_TYPE = "Exception occurred while setting project Type";
	public static final String EXCP_GET_PACKAGE_TYPE = "Exception occurred while getting project Type";
	public static final String EXCP_GET_PORTAL_URL = "Exception occurred while getting portal URL";
	public static final String EXCP_SET_PORTAL_URL = "Exception occurred while setting portal URL";
	public static final String EXCP_GET_PROJECT_PROP = "Exception occurred while getting project properties";
	public static final String EXCP_SET_PROJECT_PROP = "Exception occurred while setting project properties";
	public static final String EXCP_ROLE_FROM_PATH = "Exception occurred while getting role from path.";
	public static final String EXCP_PROJ_NAME_EMPTY = "project Name is empty or null";
	public static final String EXCP_PROJ_LOC_EMPTY = "Project location should not be empty";
	public static final String EXCP_UPDATE_PROJ = "Exception occurred while upgrading project ";
	public static final String EXCP_GET_UNAME = "Exception while getting username in role";
	public static final String EXCP_SET_UNAME = "Exception while setting username in role";
	public static final String EXCP_GET_PWD = "Exception while getting password in role";
	public static final String EXCP_SET_PWD = "Exception while setting password in role";
	public static final String EXCP_GET_EDATE = "Exception while getting expiry date in role";
	public static final String EXCP_SET_EDATE = "Exception while setting expiry date in role";
	public static final String EXCP_GET_THUMBP = "Exception while getting thumbprint in role";
	public static final String EXCP_SET_THUMBP = "Exception while setting thumbprint in role";
	public static final String EXCP_RA_ALLUSER = "Exception occurred while setting status of Remote "
			+ "access configuration";
	public static final String EXCP_RA_GET_ALLUSER = "Exception occurred while getting status of Remote "
			+ "access configuration";
	public static final String EXCP_RA_USER_NAME = "Exception occurred while setting username of Remote "
			+ "access configuration";
	public static final String EXCP_RA_GET_UNAME = "Exception occurred while getting username of Remote "
			+ "access configuration";
	public static final String EXCP_RA_ENC_PWD = "Exception occurred while setting encrypted password of "
			+ "Remote access configuration";
	public static final String EXCP_RA_GET_ENC_PWD = "Exception occurred while getting encrypted password of "
			+ "Remote access configuration";
	public static final String EXCP_RA_EXPIRY = "Exception occurred while setting expiry date of Remote "
			+ "access configuration";
	public static final String EXCP_RA_GET_EXPIRY = "Exception occurred while getting expiry date of Remote "
			+ "access configuration";
	public static final String EXCP_RA_CERT = "Exception occurred while getting certificate path "
			+ "of Remote access configuration";
	public static final String EXCP_RA_SET_CERT = "Exception occurred while Setting certificate path "
			+ "of Remote access configuration";
	public static final String EXCP_RA_GET_FPRINT = "Exception occurred while getting fingerprint of "
			+ "Remote access configuration";
	public static final String EXCP_RA_SET_FPRINT = "Exception occurred while setting fingerprint of "
			+ "Remote access configuration";
	public static final String EXCP_GET_FP = "Exception occurred while getting end points";
	public static final String EXCP_SET_SESSION_AFFINITY = "Exception occurred while configuring session affinity";
	public static final String EXCP_FILE_OPERATION = "Error occurred during file operation";
	public static final String EXCP_ENDPOINT_RENAME = "Error occurred while changing endpoint info in startup task";
	public static final String EXCP_SA_NAME_CHANGE = "Internal Error occurred while updating endpoint name in session affinity files";
	public static final String EXCP_SAMPLE_CREAT = "Exception while creating sample dir";
	public static final String EXCP_REPLACE_FILE = "Exception while replacing files for project upgrade";
	public static final String EXCP_DISABLE_ALL_SA = "Exception while disabling all role";
	public static final int IIS_ARR_PORT = 31221;

	public static final String EXCP_UPDATE_OR_CREATE_ELEMENT = "Exception occurred while updating or creating element";
	public static final String EXCP_NODE_EXISTS_CHECK = "Exception occurred while checking if node is laready present or not";
	public static final String EXCP_COMMENT_NODE = "Exception occurred while creating comment node";
	public static final String EXCP_COMMENT_NODE_DELETE = "Exception occurred while deleting comment node";
	public static final String EXCP_DELETE_ELEMENT = "Exception occurred while updating or creating element";

	public static final int PACKAGE_DOC_SA_PROPERTIES = 1;
	public static final int SA_FILES_COPIED = 2;
	public static final int DEFINITION_DOC_SA_CHANGES = 3;

	public static final String APPROOT_NAME = "approot";
	public static final String SA_FOLDER_NAME = ".arrconfig";
	public static final String OLD_SA_FOLDER_NAME = ".sessionaffinity";
	public static final String SA_CONFIG_FILE = "ARRAgent.exe.config";

	public static final String TEMP_COMPONENTSET = "/componentsets/componentset[@type='%s']";
	public static final String TEMP_COMPONENT = "/componentsets/componentset[@type='%s']/component";
	public static final String COMPONENTSETS_VERSION = "/componentsets/@version";
	public static final String PREFERENCESETS_VERSION = "/preferencesets/@version";
	public static final String TEMP_STARTUPENV = "/componentsets/componentset[@type='%s']/component";
	public static final String TEMP_SERVER_COMP = "/componentsets/componentset[@type='%s'][@name='%s']";
	public static final String DOWNLOADSET = TEMP_SERVER_COMP + "/downloads/download";
	public static final String SERVER_PROP_NAME = "project.%s.server";
	public static final String SERVER_PROP_PATH = PROJ_PROPERTY
			+ "/property[@name='project.%s.server']";
	public static final String SERVER_APP = TEMP_SERVER_COMP
			+ "/component[@type='server.app']";
	public static final String SERVER_START = TEMP_SERVER_COMP
			+ "/component[@type='server.start']";
	public static final String SERVER_HOME = TEMP_SERVER_COMP
			+ "/startupenv[@type='server.home']";

	public static final String SERVER_TYPE = WA_PACK_NAME
			+ "/component[@type='%s']";
	public static final String SERVER_ASSO = WA_PACK_NAME
			+ "/*[contains(@type,'%s')]";
	public static final String EP_INSTANCE_VAR = "/RoleEnvironment/CurrentInstance/Endpoints/Endpoint[@name='%s']/@publicPort";
	public static final String INS_FIX_RANGE = "./AllocatePublicPortFrom/FixedPortRange";
	public static final String INS_FIX_RANGE_EXPR = ENDPOINT
			+ "/InstanceInputEndpoint/AllocatePublicPortFrom/FixedPortRange";
	public static final String PROPERTY_VAL = CREATE_PKG_TARGET
			+ "/property[@name='%s']";

	public static final String SET_CACHESIZEPER = "Microsoft.WindowsAzure.Plugins.Caching.CacheSizePercentage";
	public static final String SET_DIAGLEVEL = "Microsoft.WindowsAzure.Plugins.Caching.DiagnosticLevel";
	public static final String SET_CONFIGCONN = "Microsoft.WindowsAzure.Plugins.Caching.ConfigStoreConnectionString";
	public static final String SET_CONFIGCONN_VAL = "UseDevelopmentStorage=true";
	public static final String SET_CONFIGCONN_VAL_CLOULD = "BlobEndpoint=%s;AccountName=%s;AccountKey=%s";
	public static final String SET_NAMEDCACHE = "Microsoft.WindowsAzure.Plugins.Caching.NamedCaches";
	public static final String SET_NAMEDCACHE_VAL = "{&quot;caches&quot;:[{&quot;name&quot;:&quot;default&quot;,&quot;policy&quot;:{&quot;expiration&quot;:{&quot;defaultTTL&quot;:10,&quot;isExpirable&quot;:true,&quot;type&quot;:1}}}]}";
	public static final String CONFIG_SETTING_ROLE = ROLE_NAME
			+ "/ConfigurationSettings/Setting[@name='%s']";
	public static final String CONFIG_SETTING_ROLE_VAL = CONFIG_SETTING_ROLE
			+ "/@value";
	public static final String CACHE_LS_NAME = "DiagnosticStore";
	public static final String CACHE_LS_PATH = CACHE_LS_NAME + "_PATH";

	public static final String CACHE_ST_ACC_NAME_PROP = "project.%s.cachestorageaccount.name";
	public static final String CACHE_ST_ACC_KEY_PROP = "project.%s.cachestorageaccount.key";
	public static final String CACHE_ST_ACC_URL_PROP = "project.%s.cachestorageaccount.url";
	public static final String INSTANCE_PROPERTY = "project.%s.instances";
	public static final String ROLE_PROP = PROJ_PROPERTY
			+ "/property[@name='%s']";
	public static final String ROLE_PROP_VAL = PROJ_PROPERTY
			+ "/property[@name='%s']/@value";
	public static final String IMPORT_NANE = IMPORT
			+ "/Import[@moduleName='%s']";

	public static final String JDK_CLOUD_HOME = "project.%s.jdk.home.cloud";
	public static final String JDK_LOCAL_HOME = "project.%s.jdk.home.local";
	public static final String SRV_CLOUD_HOME = "project.%s.server.home.cloud";
	public static final String SRV_LOCAL_HOME = "project.%s.server.home.local";

	public static final String THRD_PARTY_JDK_NAME = "project.%s.jdk.name";
	public static final String THRD_PARTY_SRV_NAME = "project.%s.server.name.cloud";

	public static final String MIN_SDK_VERSION = "v2.8";
	public static final String SA_NEW_VERSION_ATTR = "/configuration/runtime/assemblyBinding/dependentAssembly/bindingRedirect/@newVersion";

	public static final String WR_CERTS = WORKER_ROLE
			+ "[@name='%s']/Certificates";
	public static final String WR_CERT = WR_CERTS + "/Certificate[@name='%s']";
	public static final String CERT_ROLE_NAME = CERT_ROLE
			+ "/Certificate[@name='%s']";
	public static final String CERT_LIST = CERT_ROLE + "/Certificate";
	public static final String ATTR_THUMB = "thumbprint";
	public static final String DEF_FPRINT_ALL = WORKER_ROLE
			+ "/Certificates/Certificate" + "[@name='"
			+ WindowsAzureConstants.REMOTEACCESS_FINGERPRINT + "']";
	
	public static final String PROJ_GLOBAL_PROP_PUBLISH_SETTINGS_PATH_NAME = "publishsettingspath";
	public static final String PROJ_GLOBAL_PROP_SUBSCRIPTION_ID_NAME = "subscriptionid";
	public static final String PROJ_GLOBAL_PROP_CLOUD_SERVICE_NAME = "cloudservicename";
	public static final String PROJ_GLOBAL_PROP_REGION_NAME = "region";
	public static final String PROJ_GLOBAL_PROP_STORAGE_ACCOUNT_NAME = "storageaccountname";
	public static final String PROJ_GLOBAL_PROP_DEPLOYMENT_SLOT_NAME = "deploymentslot";
	public static final String PROJ_GLOBAL_PROP_OVERWRITE_PREV_DEPLOYMENT_NAME = "overwritepreviousdeployment";
}
