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
/**
 * Class has methods to display and log the error.
 */
public class MessageUtil {

    /**
     * This method will display the error message box
     * when any error occurs.
     *
     * @param shell       parent shell
     * @param title       the text or title of the window.
     * @param message     the message which is to be displayed
     */
    public static void displayErrorDialog(Shell shell,
    		String title, String message) {
         MessageDialog.openError(shell, title, message);
    }

    /**
     * This method will display the error message box
     * when any error occurs and also logs error.
     * @param shell
     * @param title : Error title
     * @param message : Error message
     * @param e : exception
     */
    public static void displayErrorDialogAndLog(Shell shell,
    		String title, String message,
    		Exception e) {
    	Activator.getDefault().log(message, e);
    	displayErrorDialog(shell, title, message);
    }
}
