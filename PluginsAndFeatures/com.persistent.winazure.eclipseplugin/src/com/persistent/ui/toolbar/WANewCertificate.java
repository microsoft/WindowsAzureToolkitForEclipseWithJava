/**
* Copyright 2014 Microsoft Open Technologies, Inc.
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
package com.persistent.ui.toolbar;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;

import waeclipseplugin.Activator;

import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialog;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialogData;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
/**
 * This class creates new self signed certificates.
 */
public class WANewCertificate extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			NewCertificateDialogData data = new NewCertificateDialogData();
	        NewCertificateDialog dialog =	new NewCertificateDialog(new Shell(), data);	
	        
	        // Open the dialog
	        dialog.open();
			
		} catch (Exception e) {
			PluginUtil.displayErrorDialogAndLog(new Shell(), Messages.newCertErrTtl,
												Messages.newCertMsg, e);
			Activator.getDefault().log(Messages.newCertMsg, e);
		}
		return null;
	}
}
