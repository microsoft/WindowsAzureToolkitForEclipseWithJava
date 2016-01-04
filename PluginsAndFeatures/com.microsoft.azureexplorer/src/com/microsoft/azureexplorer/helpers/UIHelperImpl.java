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
package com.microsoft.azureexplorer.helpers;

import com.microsoftopentechnologies.tooling.msservices.helpers.NotNull;
import com.microsoftopentechnologies.tooling.msservices.helpers.UIHelper;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import java.io.File;
import java.text.DecimalFormat;

public class UIHelperImpl implements UIHelper {
    @Override
    public void showException(final String message, final Throwable ex) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                PluginUtil.displayErrorDialog(null, message, ex.getMessage());
            }
        });
    }

    @Override
    public void showException(final String message, final Throwable ex, final String title) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                PluginUtil.displayErrorDialog(null, title, message);
            }
        });
    }

    @Override
    public void showException(final String message,
                              final Throwable ex,
                              final String title,
                              final boolean appendEx,
                              final boolean suggestDetail) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                PluginUtil.displayErrorDialog(null, title, message);
            }
        });
    }

    @Override
    public void showError(final String message, final String title) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                PluginUtil.displayErrorDialog(null, title, message);
            }
        });
    }

    @Override
    public boolean showConfirmation(@NotNull String message, @NotNull String title, @NotNull String[] options, String defaultOption) {
        boolean choice = MessageDialog.openConfirm(new Shell(),
                title,
                message);

        return choice;
    }

    @Override
    public File showFileChooser(String title) {
        FileDialog dialog = new FileDialog(new Shell(), SWT.SAVE);
        dialog.setOverwrite(true);
//        IProject selProject = PluginUtil.getSelectedProject();
//        if (selProject != null) {
//            String path = selProject.getLocation().toPortableString();
//            dialog.setFilterPath(path);
//        }
        dialog.setText(title);
        String fileName = dialog.open();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        } else {
            return new File(fileName);
        }
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
