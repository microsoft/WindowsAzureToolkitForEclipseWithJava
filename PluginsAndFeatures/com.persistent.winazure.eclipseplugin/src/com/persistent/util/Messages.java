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
package com.persistent.util;

import org.eclipse.osgi.util.NLS;
/**
 * Stores common strings.
 */
public final class Messages extends NLS {
    private static final String BUNDLE_NAME =
    		"com.persistent.util.messages";
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
	public static String prefFileName;
	public static String prefFileEntry;
	public static String oldCmpntFileEntry;
	public static String oldPrefFileEntry;
	public static String resChgOldPrjOpenTtl;
	public static String resChgOldPrjOpenMsg;
	public static String natJavaEMF;
	public static String natMdCore;
	public static String natFctCore;
	public static String natJava;
	public static String natJs;
	public static String prjSelErr;
	public static String prjSelMsg;
	public static String cmhIdDbg;
	public static String cmhIdEndPts;
	public static String cmhIdEnvVars;
	public static String cmhIdGeneral;
	public static String cmhIdCmpnts;
	public static String cmhIdSrvCnfg;
	public static String cmhLblDbg;
	public static String cmhLblEndPts;
	public static String cmhLblEnvVars;
	public static String cmhLblGeneral;
	public static String cmhLblCmpnts;
	public static String cmhLblSrvCnfg;
	public static String cmhIdLdBlnc;
	public static String cmhLblLdBlnc;
	public static String cmhIdCach;
	public static String cmhLblCach;
	public static String cmhIdLclStg;
	public static String cmhLblLclStg;
	public static String cmhPropFor;
	public static String rolsDlgErr;
	public static String rolsDlgErrMsg;
	public static String cmhIdWinAz;
	public static String cmhIdRoles;
	public static String cmhIdRmtAces;
	public static String cmhIdCrdntls;
	public static String cmhLblWinAz;
	public static String cmhLblRoles;
	public static String cmhLblRmtAces;
	public static String cmhLblSubscrpt;
	public static String projDlgErrMsg;
	public static String dlgDownloadGrp;
	public static String dlgDlChkTxt;
	public static String dlgDlUrlLbl;
	public static String dlgDlStrgAcc;
	public static String dlgDlUrlErrMsg;
	public static String dlgDlUrlErrTtl;
    public static String dlgNtLblHome;
	public static String dplPageJdkChkBtn;
	public static String dbgBrowseBtn;
	public static String dplPageSerChkBtn;
	public static String dplDlgSerBtn;
	public static String srvPathTxt;
	public static String dplPageNameLbl;
	public static String rolsAddBtn;
	public static String rolsRemoveBtn;
	public static String skJarName;
	public static String washFileName;
	public static String lblJavaHome;
	public static String lblHmDir;
	public static String remAccErPwdNtStrg;
	public static String remAccErrTitle;
	public static String remAccErPwd;
	public static String remAccPwdNotStrg;
	public static String remAccDummyPwd;
	public static String cspackErMsg;
	public static String cmhIdStrgAcc;
	public static String cmhLblStrgAcc;
	public static String autoDlJdkCldRdBtnLbl;
	public static String noJdkDplyLbl;
	public static String cldRdBtnLbl;
	public static String autoDlSrvCldRdBtnLbl;
	public static String linkLblAcc;
	public static String eclipseDeployContainer;
	public static String expErlStrtUp;
	public static String expStrgReg;
	public static String expLocToAuto;
	public static String emltrGrp;
	public static String thrdPrtJdkLbl;
	public static String thrdPrtSrvLbl;
	public static String aggTtl;
	public static String aggMsg;
	public static String aggLnk;
	public static String acptBtn;
	public static String bldErFileName;
	public static String proj;
	public static String expClearPref;
	public static String cmhIdCert;
	public static String cmhLblCert;
	public static String cmhIdSsl;
	public static String cmhLblSsl;
	public static String srvTtl;
	public static String srvNoDetectionMsg;
	public static String srvWrngDetectionMsg;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    /**
     * Constructor.
     */
    private Messages() {
        super();
    }
}
