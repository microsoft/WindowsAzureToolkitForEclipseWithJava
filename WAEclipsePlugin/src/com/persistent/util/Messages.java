/**
* Copyright 2011 Persistent Systems Ltd.
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
package com.persistent.util;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
    private static final String BUNDLE_NAME = "com.persistent.util.messages"; //$NON-NLS-1$
    public static String propErr;
    public static String propIdDbg;
    public static String propIdEndPts;
    public static String propIdGeneral;
    public static String propRoleFolder;
    public static String propRolePrefNode;
    public static String pXMLAttrDir;
    public static String pXMLAttrLoc;
    public static String pXMLParseExcp;
    public static String pXMLProjAttr;
    public static String pXMLWinAzureProj;
    public static String pXMLWorkLoc;
    public static String resCLExFolderRem;
    public static String resCLExFoldRename;
    public static String resCLExInVisit;
    public static String resCLExProjRename;
    public static String resCLExProjUpgrd;
    public static String resCLExWkspRfrsh;
    public static String resCLExtToolBldr;
    public static String resCLJobName;
    public static String resCLLaunchFile;
    public static String resCLPkgXML;
    public static String stUpProjNature;
    public static String stUpSchemaLoc;
    public static String stUpSerConfigKey;
    public static String stUpSerConfSchma;
    public static String stUpSerDefKey;
    public static String stUpSerDefSchema;
    public static String encUtilErrMsg;
    public static String resChgPrjUpgTtl;
    public static String resChgPrjUpgMsg;
    public static String propWebProj;
    public static String cmpntFileName;
    public static String cmpntFileEntry;
    public static String encFileName;
    public static String encFileEntry;
    public static String pluginFolder;
    public static String pluginId;
    public static String pWizStarterKitLoc;
	public static String resChgPrjUpgMsgConfirm;
	public static String pWizToolBuilder;
	public static String pWizLaunchFile;
	public static String pWizWinAzureProj;
	public static String sdkInsTtl;
	public static String sdkInsMsg;
	public static String sdkInsUrl;
	public static String resCLExSDKIns;
	public static String starterKitEntry;
	public static String starterKitFileName;
	public static String upgrdJobTtl;
	public static String closeProj;
	public static String cnslName;
	public static String restFileName;
	public static String restFileEntry;
	public static String oldCmpntFileEntry;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        super();
    }
}
