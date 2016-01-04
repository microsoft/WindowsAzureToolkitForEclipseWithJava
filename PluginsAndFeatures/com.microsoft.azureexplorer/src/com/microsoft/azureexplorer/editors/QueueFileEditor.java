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
package com.microsoft.azureexplorer.editors;

import com.microsoft.azureexplorer.Activator;
import com.microsoft.azureexplorer.forms.QueueMessageForm;
import com.microsoft.azureexplorer.forms.ViewMessageForm;
import com.microsoft.azureexplorer.helpers.UIHelperImpl;
import com.microsoftopentechnologies.tooling.msservices.components.DefaultLoader;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.sdk.StorageClientSDKManagerImpl;
import com.microsoftopentechnologies.tooling.msservices.model.storage.ClientStorageAccount;
import com.microsoftopentechnologies.tooling.msservices.model.storage.Queue;
import com.microsoftopentechnologies.tooling.msservices.model.storage.QueueMessage;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.EventHelper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import java.text.SimpleDateFormat;
import java.util.List;

public class QueueFileEditor extends EditorPart {

    private ClientStorageAccount storageAccount;
    private Queue queue;
    private Button dequeueMessageButton;
    private Button refreshButton;
    private Button addMessageButton;
    private Button clearQueueButton;
    private Table queueTable;
    private TableViewer tableViewer;
    private List<QueueMessage> queueMessages;

    private EventHelper.EventWaitHandle subscriptionsChanged;
    private boolean registeredSubscriptionsChanged;
    private final Object subscriptionsChangedSync = new Object();

    @Override
    public void doSave(IProgressMonitor iProgressMonitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        storageAccount = ((StorageEditorInput) input).getStorageAccount();
        queue = (Queue) ((StorageEditorInput) input).getItem();
        setPartName(queue.getName() + " [Queue]");
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite composite) {
        composite.setLayout(new GridLayout());
        createToolbar(composite);
        createTable(composite);
        createTablePopUp(composite);
    }

    private void createToolbar(Composite parent) {
        GridLayout gridLayout = new GridLayout(1, false);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        Composite container = new Composite(parent, 0);
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);


        RowLayout rowLayout = new RowLayout();
        rowLayout.type = SWT.HORIZONTAL;
        rowLayout.wrap = false;

        Composite buttonsContainer = new Composite(container, SWT.NONE);
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.RIGHT;
        buttonsContainer.setLayout(rowLayout);

        refreshButton = new Button(buttonsContainer, SWT.PUSH);
        refreshButton.setImage(Activator.getImageDescriptor("icons/storagerefresh.png").createImage());
        refreshButton.setToolTipText("Refresh");

        addMessageButton = new Button(buttonsContainer, SWT.PUSH);
        addMessageButton.setImage(Activator.getImageDescriptor("icons/newmessage_queue.png").createImage());
        addMessageButton.setToolTipText("Add");

        dequeueMessageButton = new Button(buttonsContainer, SWT.PUSH);
        dequeueMessageButton.setImage(Activator.getImageDescriptor("icons/dequeue.png").createImage());
        dequeueMessageButton.setToolTipText("Dequeue");

        clearQueueButton = new Button(buttonsContainer, SWT.PUSH);
        clearQueueButton.setImage(Activator.getImageDescriptor("icons/clearqueue.png").createImage());
        clearQueueButton.setToolTipText("Clear queue");

        refreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fillGrid();
            }
        });
        dequeueMessageButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dequeueFirstMessage();
            }
        });
        addMessageButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                QueueMessageForm queueMessageForm = new QueueMessageForm(new Shell(), storageAccount, queue);
                queueMessageForm.setOnAddedMessage(new Runnable() {
                    @Override
                    public void run() {
                        fillGrid();
                    }
                });

                queueMessageForm.open();

            }
        });
        clearQueueButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean optionDialog = DefaultLoader.getUIHelper().showConfirmation(
                        "Are you sure you want to clear the queue \"" + queue.getName() + "\"?",
                        "Service explorer",
                        new String[]{"Yes", "No"},
                        null);

                if (optionDialog) {
                    DefaultLoader.getIdeHelper().runInBackground(null, "Clearing queue messages", false, true, "Clearing queue messages", new Runnable() {
                        public void run() {
                            try {

                                StorageClientSDKManagerImpl.getManager().clearQueue(storageAccount, queue);

                                DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        fillGrid();
                                    }
                                });
                            } catch (AzureCmdException e) {
                                DefaultLoader.getUIHelper().showException("Error clearing queue messages", e, "Service Explorer", false, true);
                            }
                        }
                    });
                }
            }
        });
    }

    private void createTable(Composite parent) {
        queueTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);

        queueTable.setHeaderVisible(true);
        queueTable.setLinesVisible(true);

        GridData gridData = new GridData();
//        gridData.heightHint = 75;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;

        GridLayout gridLayoutTable = new GridLayout();
        gridLayoutTable.numColumns = 6;
        gridLayoutTable.marginRight = 0;
        queueTable.setLayout(gridLayoutTable);
        queueTable.setLayoutData(gridData);
        for (int i = 0; i < 6; i++) {
            TableColumn column = new TableColumn(queueTable, SWT.FILL);
            column.setWidth(100);
        }

        queueTable.getColumn(0).setText("Id");
        queueTable.getColumn(1).setText("Message Text Preview");
        queueTable.getColumn(2).setText("Size");
        queueTable.getColumn(3).setText("Insertion Time (UTC)");
        queueTable.getColumn(4).setText("Expiration Time (UTC)");
        queueTable.getColumn(5).setText("Dequeue count");

        tableViewer = new TableViewer(queueTable);
        tableViewer.setContentProvider(new QueueContentProvider());
        tableViewer.setLabelProvider(new QueueLabelProvider());

        fillGrid();
    }

    private void createTablePopUp(Composite parent) {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                if (tableViewer.getSelection().isEmpty()) {
                    return;
                }
                if (tableViewer.getSelection() instanceof IStructuredSelection) {
                    Action action = new Action("Open") {
                        @Override
                        public void run() {
                            viewMessageText();
                        }
                    };
                    manager.add(action);
                    action = new Action("Dequeue") {
                        @Override
                        public void run() {
                            dequeueFirstMessage();
                        }
                    };
                    if (queueTable.getSelectionIndex() != 0) {
                        action.setEnabled(false);
                    }
                    manager.add(action);
                }
            }
        });
        Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
        tableViewer.getControl().setMenu(menu);
    }

    public void fillGrid() {
        DefaultLoader.getIdeHelper().runInBackground(null, "Loading queue messages", false, true, "Loading queue messages", new Runnable() {
            public void run() {
                try {
                    queueMessages = StorageClientSDKManagerImpl.getManager().getQueueMessages(storageAccount, queue);

                    DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            clearQueueButton.setEnabled(queueMessages.size() != 0);
                            dequeueMessageButton.setEnabled(queueMessages.size() != 0);
                            tableViewer.setInput(queueMessages);
                        }
                    });

                } catch (AzureCmdException e) {
                    DefaultLoader.getUIHelper().showException("Error getting queue messages", e, "Service Explorer", false, true);
                }
            }
        });
    }

    private void dequeueFirstMessage() {
        if (DefaultLoader.getUIHelper().showConfirmation(
                "Are you sure you want to dequeue the first message in the queue?",
                "Service Explorer",
                new String[] {"Yes", "No"}, null)) {
            DefaultLoader.getIdeHelper().runInBackground(null, "Dequeuing message", false, true, "Dequeuing message", new Runnable() {
                public void run() {
                    try {
                        StorageClientSDKManagerImpl.getManager().dequeueFirstQueueMessage(storageAccount, queue);

                        DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                fillGrid();
                            }
                        });
                    } catch (AzureCmdException e) {
                        DefaultLoader.getUIHelper().showException("Error dequeuing messages", e, "Service Explorer", false, true);
                    }
                }
            });
        }
    }

    private void viewMessageText() {
        QueueMessage message = (QueueMessage) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
        ViewMessageForm viewMessageForm = new ViewMessageForm(new Shell(), message.getContent());
        viewMessageForm.open();
    }

    @Override
    public void setFocus() {
    }

    private class QueueContentProvider implements IStructuredContentProvider {
        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object o, Object o1) {
        }

        public Object[] getElements(Object o) {
            return queueMessages.toArray();
        }
    }

    private class QueueLabelProvider implements ITableLabelProvider {
        public void addListener(ILabelProviderListener iLabelProviderListener) {
        }

        public void dispose() {
        }

        public boolean isLabelProperty(Object o, String s) {
            return false;
        }

        public void removeListener(ILabelProviderListener iLabelProviderListener) {
        }

        public Image getColumnImage(Object o, int i) {
            return null;
        }

        public String getColumnText(Object o, int i) {
            QueueMessage queueMessage = (QueueMessage) o;
            switch (i) {
                case 0:
                    return queueMessage.getId();
                case 1:
                    return queueMessage.getContent();
                case 2:
                    return UIHelperImpl.readableFileSize(queueMessage.getContent().length());
                case 3:
                    return new SimpleDateFormat().format(queueMessage.getInsertionTime().getTime());
                case 4:
                    return new SimpleDateFormat().format(queueMessage.getExpirationTime().getTime());
                case 5:
                    return String.valueOf(queueMessage.getDequeueCount());
                default:
                    return "";
            }
        }
    }
}
