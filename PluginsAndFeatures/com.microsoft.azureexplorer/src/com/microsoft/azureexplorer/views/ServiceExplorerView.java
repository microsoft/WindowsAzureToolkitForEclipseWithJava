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
package com.microsoft.azureexplorer.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.microsoft.azureexplorer.Activator;
import com.microsoft.azureexplorer.forms.SubscriptionPropertyPage;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoftopentechnologies.tooling.msservices.helpers.collections.ListChangeListener;
import com.microsoftopentechnologies.tooling.msservices.helpers.collections.ListChangedEvent;
import com.microsoftopentechnologies.tooling.msservices.helpers.collections.ObservableList;
import com.microsoftopentechnologies.tooling.msservices.model.Subscription;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.Node;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.NodeAction;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.azure.AzureServiceModule;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.azure.storage.StorageModule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;

public class ServiceExplorerView extends ViewPart implements PropertyChangeListener {

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "ServiceExplorerView";

    private TreeViewer viewer;
    private Action refreshAction;
    private Action manageSubscriptionAction;
    private Action doubleClickAction;

    private AzureServiceModule azureServiceModule;

	/*
     * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
    class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
        private TreeNode invisibleRoot;

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (parent.equals(getViewSite())) {
                if (invisibleRoot == null) initialize();
                return getChildren(invisibleRoot);
            }
            return getChildren(parent);
        }

        public Object getParent(Object child) {
            if (child instanceof TreeNode) {
                return (((TreeNode) child).node).getParent().getViewData();
            }
            return null;
        }

        public Object[] getChildren(Object parent) {
            if (parent instanceof TreeNode) {
                return ((TreeNode) parent).getChildNodes().toArray();
            }
            return new Object[0];
        }

        public boolean hasChildren(Object parent) {
            if (parent instanceof TreeNode)
                return ((TreeNode) parent).getChildNodes().size() > 0;
            return false;
        }

        private void initialize() {
            azureServiceModule = new AzureServiceModule(null);

            invisibleRoot = new TreeNode(null);
            invisibleRoot.add(createTreeNode(azureServiceModule));

            // kick-off asynchronous load of child nodes on all the modules
            azureServiceModule.load();
        }
    }

    private class TreeNode {
        Node node;
        List<TreeNode> childNodes = new ArrayList<TreeNode>();

        public TreeNode(Node node) {
            this.node = node;
        }

        public void add(TreeNode treeNode) {
            childNodes.add(treeNode);
        }


        public List<TreeNode> getChildNodes() {
            return childNodes;
        }

        public void remove(TreeNode treeNode) {
            childNodes.remove(treeNode);
        }

        @Override
        public String toString() {
            return node.getName();
        }
    }

    private TreeNode createTreeNode(Node node) {
        TreeNode treeNode = new TreeNode(node);

        // associate the TreeNode with the Node via it's "viewData"
        // property; this allows us to quickly retrieve the DefaultMutableTreeNode
        // object associated with a Node
        node.setViewData(treeNode);

        // listen for property change events on the node
        node.addPropertyChangeListener(this);

        // listen for structure changes on the node, i.e. when child nodes are
        // added or removed
        node.getChildNodes().addChangeListener(new NodeListChangeListener(treeNode));

        // create child tree nodes for each child node
        if(node.hasChildNodes()) {
            for (Node childNode : node.getChildNodes()) {
                treeNode.add(createTreeNode(childNode));
//                treeNode.add(createTreeNode(childNode));
            }
        }
        return treeNode;
    }

    private void removeEventHandlers(Node node) {
        node.removePropertyChangeListener(this);

        ObservableList<Node> childNodes = node.getChildNodes();
        childNodes.removeAllChangeListeners();
//
        if(node.hasChildNodes()) {
            // this remove call should cause the NodeListChangeListener object
            // registered on it's child nodes to fire which should recursively
            // clean up event handlers on it's children
            node.removeAllChildNodes();
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        // if we are not running on the dispatch thread then switch
        // to dispatch thread
//        if(!ApplicationManager.getApplication().isDispatchThread()) {
//            ApplicationManager.getApplication().invokeAndWait(new Runnable() {
//                @Override
//                public void run() {
//                    propertyChange(evt);
//                }
//            }, ModalityState.any());
//
//            return;
//        }

        // this event is fired whenever a property on a node in the
        // model changes; we respond by triggering a node change
        // event in the tree's model
        final Node node = (Node) evt.getSource();

        // the treeModel object can be null before it is initialized
        // from createToolWindowContent; we ignore property change
        // notifications till we have a valid model object
//        if(treeModel != null) {
//            treeModel.nodeChanged((TreeNode) node.getViewData());
//        }
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                viewer.refresh((TreeNode) node.getViewData());
            }
        });
    }

    private class NodeListChangeListener implements ListChangeListener {
        private TreeNode treeNode;

        public NodeListChangeListener(TreeNode treeNode) {
            this.treeNode = treeNode;
        }

        @Override
        public void listChanged(final ListChangedEvent e) {
            switch (e.getAction()) {
                case add:
                    // create child tree nodes for the new nodes
                    for(Node childNode : (Collection<Node>)e.getNewItems()) {
                        treeNode.add(createTreeNode(childNode));
                    }
                    break;
                case remove:
                    // unregister all event handlers recursively and remove
                    // child nodes from the tree
                    for(Node childNode : (Collection<Node>)e.getOldItems()) {
                        removeEventHandlers(childNode);

                        // remove this node from the tree
                        treeNode.remove((TreeNode) childNode.getViewData());
                    }
                    break;
            }
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    viewer.refresh(treeNode);
                }
            });
        }
    }

    class ViewLabelProvider extends LabelProvider {

        public String getText(Object obj) {
            return obj.toString();
        }

        public Image getImage(Object obj) {
            if (obj instanceof TreeNode) {
            	String iconPath = ((TreeNode) obj).node.getIconPath();
            	if (iconPath != null) {
            		return Activator.getImageDescriptor("icons/" + iconPath).createImage();//Activator.getDefault().getImageRegistry().get((((Node) obj).getIconPath()));
            	}
            }
            return super.getImage(obj);
        }
    }

    class NameSorter extends ViewerSorter {
    }

    /**
     * The constructor.
     */
    public ServiceExplorerView() {
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setSorter(new NameSorter());
        viewer.setInput(getViewSite());

        // Create the help context id for the viewer's control
        PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.microsoft.azureexplorer.viewer");
        makeActions();
        hookContextMenu();
        hookMouseActions();
        contributeToActionBars();
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                if (viewer.getSelection().isEmpty()) {
                    return;
                }
                if (viewer.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                    Node node = ((TreeNode) selection.getFirstElement()).node;
                    if (node.hasNodeActions()) {
                        for (final NodeAction nodeAction : node.getNodeActions()) {
                            Action action = new Action(nodeAction.getName()) {
                                public void run() {
                                    nodeAction.fireNodeActionEvent();
                                }
                            };
                            action.setEnabled(nodeAction.isEnabled());
                            manager.add(action);
                        }
                    }
                }
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(refreshAction);
        manager.add(new Separator());
        manager.add(manageSubscriptionAction);
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(refreshAction);
        manager.add(manageSubscriptionAction);
        manager.add(new Separator());
    }

    private void makeActions() {
        refreshAction = new Action("Refresh", Activator.getImageDescriptor("icons/refresh.png")) {
            public void run() {
                azureServiceModule.load();
            }
        };
        refreshAction.setToolTipText("Refresh Service List");

        manageSubscriptionAction = new Action("Manage Subscriptions", Activator.getImageDescriptor("icons/azure.png")) {
            public void run() {
                Dialog subscriptionDialog = new SubscriptionPropertyPage(new Shell());
                subscriptionDialog.open();
                azureServiceModule.load();

            }
        };
        manageSubscriptionAction.setToolTipText("Manage Subscriptions");
        doubleClickAction = new Action() {
            public void run() {
                ISelection selection = viewer.getSelection();
                Object obj = ((IStructuredSelection) selection).getFirstElement();
                viewer.expandToLevel(obj, 1);
                try {
                    List<Subscription> subscriptions = AzureManagerImpl.getManager().getFullSubscriptionList();
                    if ((subscriptions == null || subscriptions.isEmpty()) &&
                            (((TreeNode) obj).node instanceof StorageModule || ((TreeNode) obj).node instanceof AzureServiceModule)) {
                        SubscriptionPropertyPage subscriptionsDialog = new SubscriptionPropertyPage(getSite().getShell());
                        subscriptionsDialog.open();
                    }
                } catch (AzureCmdException e) {
                    Activator.getDefault().log(e.getMessage(), e);
                }
            }
        };
    }

    private void hookMouseActions() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
        Tree tree = (Tree) viewer.getControl();
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem item = (TreeItem) e.item;
                Node node = ((TreeNode) item.getData()).node;
                // if the node in question is in a "loading" state then we
                // do not propagate the click event to it
                if (!node.isLoading()) {
                    node.getClickAction().fireNodeActionEvent();
                }
            }
        });
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}