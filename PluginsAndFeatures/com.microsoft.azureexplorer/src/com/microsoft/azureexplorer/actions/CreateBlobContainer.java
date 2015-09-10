package com.microsoft.azureexplorer.actions;

import org.eclipse.swt.widgets.Shell;

import com.microsoft.azureexplorer.forms.CreateBlobContainerForm;
import com.microsoftopentechnologies.tooling.msservices.helpers.Name;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.azure.storage.BlobModule;
import com.microsoftopentechnologies.tooling.msservices.serviceexplorer.azure.storage.ClientStorageNode;

@Name("Create blob container")
public class CreateBlobContainer extends NodeActionListener {
	private BlobModule blobModule;

	public CreateBlobContainer(BlobModule blobModule) {
		this.blobModule = blobModule;
	}

	@Override
	public void actionPerformed(NodeActionEvent e) {
		CreateBlobContainerForm form = new CreateBlobContainerForm(new Shell(), blobModule.getStorageAccount());

		form.setOnCreate(new Runnable() {
			@Override
			public void run() {
				blobModule.getParent().removeAllChildNodes();
				((ClientStorageNode) blobModule.getParent()).load();
			}
		});
		form.open();
	}
}