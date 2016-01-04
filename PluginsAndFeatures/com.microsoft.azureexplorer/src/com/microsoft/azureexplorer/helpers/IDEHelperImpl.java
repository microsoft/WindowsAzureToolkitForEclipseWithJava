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

import com.google.common.collect.ImmutableMap;
import com.microsoft.azureexplorer.Activator;
import com.microsoft.azureexplorer.editors.BlobExplorerFileEditor;
import com.microsoft.azureexplorer.editors.QueueFileEditor;
import com.microsoft.azureexplorer.editors.StorageEditorInput;
import com.microsoft.azureexplorer.editors.TableFileEditor;
import com.microsoft.azureexplorer.forms.OpenSSLFinderForm;
import com.microsoftopentechnologies.tooling.msservices.components.DefaultLoader;
import com.microsoftopentechnologies.tooling.msservices.helpers.IDEHelper;
import com.microsoftopentechnologies.tooling.msservices.model.storage.*;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.Node;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class IDEHelperImpl implements IDEHelper {
    private Map<Class<? extends StorageServiceTreeItem>, String> type2Editor = ImmutableMap.of(BlobContainer.class, "com.microsoft.azureexplorer.editors.BlobExplorerFileEditor",
            Queue.class, "com.microsoft.azureexplorer.editors.QueueFileEditor",
            Table.class, "com.microsoft.azureexplorer.editors.TableFileEditor");

    @Override
    public void openFile(File file, Node node) {

    }

    @Override
    public void runInBackground(Object project, String name, boolean canBeCancelled, boolean isIndeterminate, final String indicatorText, final Runnable runnable) {
        Job job = new Job(name) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(indicatorText, IProgressMonitor.UNKNOWN);
                try {
                    runnable.run();
                } catch (Exception ex) {
                    monitor.done();
                    return Status.CANCEL_STATUS;
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    @Override
    public void saveFile(File file, ByteArrayOutputStream byteArrayOutputStream, Node node) {

    }

    @Override
    public void replaceInFile(Object module, Pair<String, String>... replace) {

    }

    @Override
    public void copyJarFiles2Module(Object moduleObject, File zipFile, String zipPath) throws IOException {

    }

    @Override
    public boolean isFileEditing(Object projectObject, File file) {
        return false;
    }

    @Override
    public <T extends StorageServiceTreeItem> void openItem(Object projectObject, final ClientStorageAccount storageAccount, final T item, String itemType, String itemName, String iconName) {
//        Display.getDefault().syncExec(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    BlobExplorerView view = (BlobExplorerView) PlatformUI
//                            .getWorkbench().getActiveWorkbenchWindow()
//                            .getActivePage().showView("com.microsoft.azureexplorer.views.BlobExplorerView");
//                    view.init(storageAccount, (BlobContainer) blobContainer);
//                } catch (PartInitException e) {
//                    Activator.getDefault().log("Error opening container", e);
//                }
//            }
//        });
        IWorkbench workbench=PlatformUI.getWorkbench();
        IEditorDescriptor editorDescriptor=workbench.getEditorRegistry().findEditor(type2Editor.get(item.getClass()));
        try {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorPart newEditor = page.openEditor(new StorageEditorInput(storageAccount, item), editorDescriptor.getId());
        } catch (PartInitException e) {
        	Activator.getDefault().log("Error opening " + item.getName(), e);
        }
    }

    @Override
    public void openItem(Object projectObject, Object itemVirtualFile) {

    }

    @Override
    public <T extends StorageServiceTreeItem> Object getOpenedFile(Object projectObject, ClientStorageAccount storageAccount, T blobContainer) {
        return null;
    }

    @Override
    public void closeFile(Object projectObject, Object openedFile) {
//        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//        page.closeEditor((IEditorPart) openedFile, false);
    }

    @Override
    public void refreshQueue(Object projectObject, final ClientStorageAccount storageAccount, final Queue queue) {
        IWorkbench workbench=PlatformUI.getWorkbench();
        final IEditorDescriptor editorDescriptor=workbench.getEditorRegistry()
                .findEditor("com.microsoft.azureexplorer.editors.QueueFileEditor");
        DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    QueueFileEditor newEditor = (QueueFileEditor) page.openEditor(new StorageEditorInput(storageAccount, queue), editorDescriptor.getId());
                    newEditor.fillGrid();
                } catch (PartInitException e) {
                    Activator.getDefault().log("Error opening container", e);
                }
            }
        });
    }

    @Override
    public void refreshBlobs(Object projectObject, final ClientStorageAccount storageAccount, final BlobContainer container) {
        IWorkbench workbench=PlatformUI.getWorkbench();
        final IEditorDescriptor editorDescriptor=workbench.getEditorRegistry()
                .findEditor("com.microsoft.azureexplorer.editors.BlobExplorerFileEditor");
        DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    BlobExplorerFileEditor newEditor = (BlobExplorerFileEditor) page.openEditor(new StorageEditorInput(storageAccount, container), editorDescriptor.getId());
                    newEditor.fillGrid();
                } catch (PartInitException e) {
                    Activator.getDefault().log("Error opening container", e);
                }
            }
        });
    }

    @Override
    public void refreshTable(Object projectObject, final ClientStorageAccount storageAccount, final Table table) {
        IWorkbench workbench=PlatformUI.getWorkbench();
        final IEditorDescriptor editorDescriptor=workbench.getEditorRegistry()
                .findEditor("com.microsoft.azureexplorer.editors.TableFileEditor");
        DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
            public void run() {
                try {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    TableFileEditor newEditor = (TableFileEditor) page.openEditor(new StorageEditorInput(storageAccount, table), editorDescriptor.getId());
                    newEditor.fillGrid();
                } catch (PartInitException e) {
                    Activator.getDefault().log("Error opening container", e);
                }
            }
        });
    }

    @Override
    public void invokeLater(Runnable runnable) {
        Display.getDefault().asyncExec(runnable);
    }

    @Override
    public void invokeAndWait(Runnable runnable) {
        Display.getDefault().syncExec(runnable);
    }

    @Override
    public void executeOnPooledThread(final Runnable runnable) {
        Job job = new Job("Loading...") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("", IProgressMonitor.UNKNOWN);
                try {
                    runnable.run();
                } catch (Exception ex) {
                    monitor.done();
                    return Status.CANCEL_STATUS;
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    public String getProperty(Object projectObject, String name) {
        return null;
    }

    public String getProperty(Object projectObject, String name, String defaultValue) {
        return null;
    }

    public void setProperty(Object projectObject, String name, String value) {

    }

    public void unsetProperty(Object projectObject, String name) {

    }

    public boolean isPropertySet(Object projectObject, String name) {
        return false;
    }

    @Override
    public String getProperty(String name) {
        return PreferenceUtil.loadPreference(name);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        return PreferenceUtil.loadPreference(name, defaultValue);
    }

    @Override
    public void setProperty(String name, String value) {
         PreferenceUtil.savePreference(name, value);
    }

    @Override
    public void unsetProperty(String name) {
        PreferenceUtil.unsetPreference(name);
    }

    @Override
    public boolean isPropertySet(String name) {
        return false;
    }

    @Override
    public String promptForOpenSSLPath() {
        OpenSSLFinderForm openSSLFinderForm = new OpenSSLFinderForm(new Shell());
        openSSLFinderForm.open();

        return getProperty("MSOpenSSLPath", "");
    }

    @Override
    public String[] getProperties(String name) {
        return PreferenceUtil.loadPreferences(name);
    }

    @Override
    public void setProperties(String name, String[] value) {
        PreferenceUtil.savePreferences(name, value);
    }
}
