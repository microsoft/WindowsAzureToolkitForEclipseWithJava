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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import waeclipseplugin.Activator;
import com.persistent.winazureroles.Messages;

public class MessageUtil {

    /**
     * This method will display the error message box when any error occurs.It takes two parameters
     *
     * @param shell       parent shell
     * @param title       the text or title of the window.
     * @param message     the message which is to be displayed
     */
    public static void displayErrorDialog (Shell shell , String title , String message ){
         MessageDialog.openError(shell, title, message);
    }
    
    public static void displayErrorDialogAndLog(Shell shell, String title, String message, Exception e) { 
    	Activator.getDefault().log(Messages.jdkDirErrMsg, e); 
    	displayErrorDialog(shell, title, message);	
    } 
}
