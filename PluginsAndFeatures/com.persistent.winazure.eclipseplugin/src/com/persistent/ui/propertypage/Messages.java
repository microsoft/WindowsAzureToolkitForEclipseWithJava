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
package com.persistent.ui.propertypage;

import org.eclipse.osgi.util.NLS;
/**
 * Stores common strings.
 */
public final class Messages extends NLS {
    private static final String BUNDLE_NAME =
    		"com.persistent.ui.propertypage.messages";
    public static String proPageErrMsgBox1;
    public static String proPageErrMsgBox2;
    public static String proPageErrTitle;
    public static String proPageServName;
    public static String remAccUserName;
    public static String remAccPassword;
    public static String remAccConfirmPwd;
    public static String remAccChkBoxTxt;
    public static String remAccExpDate;
    public static String remAccGrpTxt;
    public static String remAccPath;
    public static String remAccNewBtn;
    public static String remAccWkspcBtn;
    public static String remAccFileSysBtn;
    public static String remAccNote;
    public static String remAccWrkspcTitle;
    public static String certDlgNewCertTxt;
    public static String certDlgNewCertMsg;
    public static String certDlgAlias;
    public static String certDlgPwdLbl;
    public static String certDlgConfPwdLbl;
    public static String certDlgCertLbl;
    public static String certDlgPFXLbl;
    public static String certDlgGrpLbl;
    public static String certDlgBrowseBtn;
    public static String certDlgBrowFldr;
    public static String certDlgBrwDir;
    public static String proPageBldForLbl;
    public static String remAccNameNull;
    public static String remAccPathNull;
    public static String remAccExpDateNull;
    public static String remAccPwdNotMatch;
    public static String certDlgPwNull;
    public static String certDlgCfPwdNull;
    public static String certDlgPwdNtMtch;
    public static String certDlgCerNull;
    public static String certDlgPFXNull;
    public static String remAccDateFormat;
    public static String remAccDateWrong;
    public static String remAccInvldPath;
    public static String remAccDataInc;
    public static String remAccErCreateCer;
    public static String proPageBFEmul;
    public static String proPageBFCloud;
    public static String proPageRefMsg;
    public static String proPageRefWarn;
    public static String remAccErAllRoles;
    public static String remAccErUserName;
    public static String certDlgPwdNtCorr;
    public static String remAccErPwd;
    public static String remAccErExpDate;
    public static String remAccErDateParse;
    public static String remAccErCertPath;
    public static String remAccErProjLoad;
    public static String remAccErrTmbPrint;
    public static String remAccErConfigErr;
    public static String remAccErXMLParse;
    public static String remAccDummyPwd;
    public static String remAccPwdMstChng;
    public static String remAccErrPwd;
    public static String remAccErTmbPrint;
    public static String remAccInvdFilExt;
    public static String remAccWarnPwd;
    public static String remAccErTxtTitle;
    public static String certDlgImg;
    public static String remAccWkspWrngSel;
    public static String remAccErrTitle;
    public static String certDlgWrongTitle;
    public static String remAccErProjList;
    public static String remAccSelExpDate;
    public static String remAccSyntaxErr;
    public static String remAccWarning;
    public static String remAccWarnMsg;
    public static String certDlgImgErr;
    public static String certDlgPwdWrong;
    public static String certDlgPwNtMatch;
    public static String proPageTgtOSLbl;
    public static String waProjNature;
    public static String usedBySSL;
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
