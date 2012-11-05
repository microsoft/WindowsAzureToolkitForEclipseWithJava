/**
 * Copyright 2012 Microsoft Open Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoftopentechnologies.wacommon.commoncontrols;


import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
    private static final String BUNDLE_NAME =
    		"com.microsoftopentechnologies.wacommon.commoncontrols.messages";
    
    /* New Certificate Dialog messages - start*/
    public static String newCertDlgCertTxt;
    public static String newCertDlgCertMsg;
    public static String newCertDlgPwdLbl;
    public static String newCertDlgCnfPwdLbl;
    public static String newCertDlgGrpLbl;
    public static String newCertDlgPFXLbl;
    public static String newCertDlgBrwsBtn;
    public static String newCertDlgCertLbl;
    public static String newCertDlgBrwFldr;
    public static String newCertDlgCrtErTtl;
    public static String newCertDlgPwNul;
    public static String newCertDlgCfPwNul;
    public static String newCertDlgPwdWrng;
    public static String newCertDlgPwNtCor;
    public static String newCertDlgPwNtMtch;
    public static String newCerDlgPwNtMsg;
    public static String newCertDlgCerNul;
    public static String newCertDlgPFXNull;
    public static String newCerDlgInvldPth;
    public static String newCerDlgInvdFlExt;
    public static String newCertDlgAlias;
    public static String newCerDlgCrtCerEr;
    /* New Certificate Dialog messages - end*/
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        super();
    }
}
