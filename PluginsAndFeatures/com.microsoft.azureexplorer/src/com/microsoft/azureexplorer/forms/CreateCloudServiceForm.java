package com.microsoft.azureexplorer.forms;

import com.microsoftopentechnologies.tooling.msservices.components.DefaultLoader;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureCmdException;
import com.microsoftopentechnologies.tooling.msservices.helpers.azure.AzureManagerImpl;
import com.microsoftopentechnologies.tooling.msservices.model.Subscription;
import com.microsoftopentechnologies.tooling.msservices.model.vm.AffinityGroup;
import com.microsoftopentechnologies.tooling.msservices.model.vm.CloudService;
import com.microsoftopentechnologies.tooling.msservices.model.vm.Location;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.List;
import java.util.Vector;

public class CreateCloudServiceForm extends Dialog {
    private Button buttonOK;
    private Button buttonCancel;
    private Label subscriptionLabel;
    private Combo subscriptionComboBox;
    private Label nameLabel;
    private Text nameTextField;
    private Label regionLabel;
    private Combo regionOrAffinityGroupComboBox;
    private ComboViewer regionOrAffinityGroupViewer;

    private Subscription subscription;
    private CloudService cloudService;
    private Runnable onCreate;

    public CreateCloudServiceForm(Shell parentShell, Subscription subscription) {
        super(parentShell);
        this.subscription = subscription;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Create Cloud Service");
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        GridData gridData = new GridData();
        gridData.verticalAlignment = SWT.FILL;
        gridData.horizontalAlignment = SWT.FILL;
        parent.setLayoutData(gridData);
        Control ctrl = super.createButtonBar(parent);
        buttonOK = getButton(IDialogConstants.OK_ID);
        buttonOK.setEnabled(false);
        buttonOK.setText("Create");
        buttonCancel = getButton(IDialogConstants.CANCEL_ID);
        buttonCancel.setText("Close");
        return ctrl;
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        container.setLayout(gridLayout);
        GridData gridData = new GridData();
        gridData.widthHint = 350;
        container.setLayoutData(gridData);

        subscriptionLabel = new Label(container, SWT.LEFT);
        subscriptionLabel.setText("Subscription:");
        subscriptionComboBox = new Combo(container, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        subscriptionComboBox.setLayoutData(gridData);

        nameLabel = new Label(container, SWT.LEFT);
        nameLabel.setText("Name:");
        nameTextField = new Text(container, SWT.LEFT | SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        nameTextField.setLayoutData(gridData);

        regionLabel = new Label(container, SWT.LEFT);
        regionLabel.setText("Region or Affinity Group:");
        regionOrAffinityGroupComboBox = new Combo(container, SWT.READ_ONLY);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        regionOrAffinityGroupComboBox.setLayoutData(gridData);
        regionOrAffinityGroupViewer = new ComboViewer(regionOrAffinityGroupComboBox);
        regionOrAffinityGroupViewer.setContentProvider(ArrayContentProvider.getInstance());
        regionOrAffinityGroupViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof String) {
                    return (String) element;
                } else  if (element instanceof AffinityGroup) {
                    AffinityGroup ag = (AffinityGroup) element;
                    return (String.format("  %s (%s)", ag.getName(), ag.getLocation()));
                } else {
                    return  "  " + element.toString();
                }
            }
        });
//        contentPane.registerKeyboardAction(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onCancel();
//            }
//        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        nameTextField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                validateEmptyFields();
            }
        });

        regionOrAffinityGroupComboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                validateEmptyFields();
            }
        });
        fillFields();
        return super.createContents(parent);
    }

    private void validateEmptyFields() {
        boolean allFieldsCompleted = !(
                nameTextField.getText().isEmpty() ||
                        ((IStructuredSelection) regionOrAffinityGroupViewer.getSelection()).getFirstElement() instanceof String);

        buttonOK.setEnabled(allFieldsCompleted);
    }

    public void fillFields() {
        subscriptionComboBox.add(subscription.getName());
        subscriptionComboBox.setEnabled(false);
        subscriptionComboBox.select(0);

        regionOrAffinityGroupComboBox.add("<Loading...>");

        DefaultLoader.getIdeHelper().runInBackground(null, "Loading regions...", false, true, "Loading regions...", new Runnable() {
            @Override
            public void run() {
                try {
                    final List<AffinityGroup> affinityGroups = AzureManagerImpl.getManager().getAffinityGroups(subscription.getId());
                    final List<Location> locations = AzureManagerImpl.getManager().getLocations(subscription.getId());

                    DefaultLoader.getIdeHelper().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final Vector<Object> vector = new Vector<Object>();
                            vector.add("Regions");
                            vector.addAll(locations);
                            if (affinityGroups.size() > 0) {
                                vector.add("Affinity Groups");
                                vector.addAll(affinityGroups);
                            }
                            regionOrAffinityGroupViewer.setInput(vector);
                            regionOrAffinityGroupComboBox.select(1);
                        }
                    });
                } catch (AzureCmdException e) {
                    DefaultLoader.getUIHelper().showException("An error occurred while trying to load the regions list",
                            e, "Error Loading Regions", false, true);
                }
            }
        });
    }

    @Override
    protected void okPressed() {
        if (!nameTextField.getText().matches("^[A-Za-z0-9][A-Za-z0-9-]+[A-Za-z0-9]$")) {
            DefaultLoader.getUIHelper().showError("Invalid cloud service name. Cloud service name must start with a letter or number, \n" +
                    "contain only letters, numbers, and hyphens, and end with a letter or number.", "Azure Explorer");
            return;
        }

        PluginUtil.showBusy(true, getShell());

        try {
            String name = nameTextField.getText();
            Object regionOrAffinity = ((IStructuredSelection) regionOrAffinityGroupViewer.getSelection()).getFirstElement();
            String location = (regionOrAffinity != null && regionOrAffinity instanceof Location) ?
                    ((Location) regionOrAffinity).getName() :
                    "";
            String affinityGroup = (regionOrAffinity != null && regionOrAffinity instanceof AffinityGroup) ?
                    ((AffinityGroup) regionOrAffinity).getName() :
                    "";

            cloudService = new CloudService(name, location, affinityGroup, subscription.getId());
            AzureManagerImpl.getManager().createCloudService(cloudService);
        } catch (Exception e) {
            cloudService = null;
            DefaultLoader.getUIHelper().showException("An error occurred while trying to create the specified cloud service", e, "Error Creating Storage Account", false, true);
        }

        onCreate.run();
        PluginUtil.showBusy(false, getShell());

        super.okPressed();
    }

    public CloudService getCloudService() {
        return cloudService;
    }

    public void setOnCreate(Runnable onCreate) {
        this.onCreate = onCreate;
    }
}