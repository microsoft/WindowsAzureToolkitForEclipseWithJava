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
package com.microsoft.azureexplorer;

import com.google.common.collect.ImmutableList;
import com.microsoft.azureexplorer.actions.*;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.Node;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.azure.AzureServiceModule;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.azure.storage.*;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.azure.vm.VMServiceModule;

import java.util.HashMap;
import java.util.Map;

public class NodeActionsMap {
    public static final Map<Class<? extends Node>, ImmutableList<Class<? extends NodeActionListener>>> node2Actions =
            new HashMap<Class<? extends Node>, ImmutableList<Class<? extends NodeActionListener>>>();
    static {
        node2Actions.put(AzureServiceModule.class, new ImmutableList.Builder().add(ManageSubscriptionsAction.class).build());
        node2Actions.put(VMServiceModule.class, new ImmutableList.Builder().add(CreateVMAction.class).build());
        node2Actions.put(TableModule.class, new ImmutableList.Builder().add(CreateTableAction.class).build());
        node2Actions.put(QueueModule.class, new ImmutableList.Builder().add(CreateQueueAction.class).build());
//        node2Actions.put(TableScriptNode.class, new ImmutableList.Builder().add(UpdateScriptAction.class).build());
//        node2Actions.put(CustomAPINode.class, new ImmutableList.Builder().add(UpdateCustomAPIAction.class, EditCustomAPIAction.class).build());
//        node2Actions.put(ScheduledJobNode.class, new ImmutableList.Builder().add(UpdateJobAction.class, EditJobAction.class).build());
        node2Actions.put(BlobModule.class, new ImmutableList.Builder().add(CreateBlobContainer.class).build());
        node2Actions.put(StorageModule.class, new ImmutableList.Builder().add(CreateStorageAccountAction.class, AttachExternalStorageAccountAction.class).build());
//        node2Actions.put(ExternalStorageNode.class, new ImmutableList.Builder().add(ConfirmDialogAction.class, ModifyExternalStorageAccountAction.class).build());
    }
}