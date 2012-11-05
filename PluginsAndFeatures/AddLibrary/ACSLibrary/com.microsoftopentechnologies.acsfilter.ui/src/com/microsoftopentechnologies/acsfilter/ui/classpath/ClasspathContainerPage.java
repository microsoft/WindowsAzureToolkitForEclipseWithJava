/**
 * Copyright 2012 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoftopentechnologies.acsfilter.ui.classpath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.eclipse.jface.window.Window;

import com.microsoftopentechnologies.wacommon.utils.Base64;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialog;
import com.microsoftopentechnologies.wacommon.commoncontrols.NewCertificateDialogData;
import com.microsoftopentechnologies.acsfilter.ui.activator.Activator;


/**
 * Classpath container page.
 *
 */
public class ClasspathContainerPage extends WizardPage implements
    IClasspathContainerPage, IClasspathContainerPageExtension {

    private IPath entry;
    private Combo combo;
    private boolean finishVal = true;
    private String libLocation = Messages.notFound;
    private Label location;
    private Text certTxt;
    private Text certInfoTxt;
    private Text relTxt;
    private Text acsTxt;
    private Button browseBtn;
    private Button newCertBtn;
    private Label lblLoc;
    private Label certLbl;
    private Label relLbl;
    private Label acsLbl;
    private Button depCheck;
    private Button requiresHttpsCheck;
    private Button embedCertCheck;
    private static final int BUFF_SIZE = 1024;
    private HashMap<String,String> paramMap;

    /**
     * Default constructor.
     */
    public ClasspathContainerPage() {
        super(Messages.title);
        //set title for page
        setTitle(Messages.title);
        //set description for page
        setDescription(Messages.desc);
        entry = new Path(Messages.sdkContainer);
    }

    /**
     * Draws controls for page.
     *
     * @param parent.
     */
    @Override
    public void createControl(Composite parent) {
    	boolean containerIdPresent = false;
        GridLayout gridLayout = new GridLayout();
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(gridLayout);
        container.setLayoutData(gridData);
        
        // display help contents
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.microsoftopentechnologies.acsfilter.ui."
                + "acs_config_dialog");

        gridLayout = new GridLayout();
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        gridLayout.numColumns = 2;
        Composite verContainer = new Composite(container, SWT.NONE);
        verContainer.setLayout(gridLayout);
        verContainer.setLayoutData(gridData);


        //Label for version
        Label lblVersion = new Label(verContainer, SWT.LEFT);
        gridData = new GridData();
        lblVersion.setText(Messages.lblVersion);
        lblVersion.setLayoutData(gridData);


        //Combo to hold the versions
        combo = new Combo(verContainer, SWT.READ_ONLY | SWT.LEFT);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalIndent = 10;
        gridData.horizontalAlignment = SWT.FILL;
        combo.setLayoutData(gridData);
        combo.addSelectionListener(new ComboSelectionListener());
        //get bundles for the sdk
        Bundle[] bundles = Platform.getBundles(Messages.sdkID,
                null);
        if (bundles == null || bundles.length == 0) {
            //if bundles is null set an error msg
            setErrorMessage(Messages.libNotAvail);
            finishVal = false;
        } else {
            try {
                //populate the combo box with available versions of sdk
                populateComboBox(bundles);
                Bundle bundle = bundles[combo.getSelectionIndex()];

                if (bundle == null) {
                    finishVal = false;
                    //if bundle is null set an error msg
                    setErrorMessage(Messages.verNotAvail);
                } else {
                    //locate sdk jar in bundle
                    URL url = FileLocator.find(bundle,
                            new Path(Messages.sdkJar), null);
                    if (url == null) {
                        finishVal = false;
                        setErrorMessage(Messages.verNotAvail);
                    } else {
                        //if jar is found then resolve url and get the location
                        url = FileLocator.resolve(url);
                        File loc = new File(url.getPath());
                        libLocation = loc.getAbsolutePath();
                    }
                }
            } catch (Exception e) {
                //if exception is thrown set an error msg.
                setErrorMessage(Messages.verNotAvail);
                finishVal = false;
            }
        }
        //label for location
        lblLoc = new Label(verContainer, SWT.LEFT);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;

        lblLoc.setLayoutData(gridData);
        lblLoc.setText(Messages.lblLocation);
        //label to hold the sdk jar path
        location = new Label(verContainer, SWT.WRAP | SWT.LEFT);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalIndent = 10;
        gridData.widthHint = 400;
        gridData.horizontalAlignment = SWT.FILL;

        location.setLayoutData(gridData);
        location.setText(libLocation);

        createDepCheckBox(container);
        createACSComponent(container);
        createRealmComponent(container);
        createCertComponent(container);
        //Disabling the control if selected project is not dynamic web project.
        try {
            IProject proj = ACSFilterUtil.getSelectedProject();
            if (!(proj.hasNature(Messages.emfNtr)
                    && proj.hasNature(Messages.mdlCrNtr)
                    && proj.hasNature(Messages.fctNtr)
                    && proj.hasNature(Messages.javaNtr)
                    && proj.hasNature(Messages.jsNtr))) {
                setErrorMessage(Messages.acsInvProjMsg);
                disableAll();
                setPageComplete(false);
            }

        } catch (CoreException e) {
            finishVal = false;
            setErrorMessage(Messages.acsInvProjMsg);

        }

        if (isEdit()) {
        	// Edit library scenario
            try {
                IJavaProject proj1 = JavaCore.create(ACSFilterUtil.getSelectedProject());
                // Iterate over class path entries.
                for (int i = 0; i < proj1.getRawClasspath().length; i++) {
                	/*
                	 * check if class path contains
                	 * our library's container Id
                	 * If contains then it's not
                	 * a case of immediate edit
                	 */
                    if (proj1.getRawClasspath()[i].toString().
                    contains(Messages.sdkContainer)) {
                    	containerIdPresent = true;
                        for (int j = 0; j < proj1.getRawClasspath()[i].
                        getExtraAttributes().length; j++) {
                            if (proj1.getRawClasspath()[i].
                            getExtraAttributes()[j].getName().
                            equalsIgnoreCase(Messages.jstDep)) {
                                depCheck.setSelection(true);
                                break;
                            }
                            depCheck.setSelection(false);
                        }
                        if (proj1.getRawClasspath()[i].
                        		getExtraAttributes().length == 0) {
                            depCheck.setSelection(false);
                        }
                    }
                }
                /*
                 * If contains then it's not a case of immediate edit
                 * retrieve state of deployment assembly check box
                 * which was saved when user clicks on Finish button.
                 */
                if (!containerIdPresent) {
                	depCheck.setSelection(Activator.
                			geteploymentAssemblyState());
                }
                ACSFilterHandler editHandler =
                		new ACSFilterHandler(ACSFilterUtil.getSelectedProject().
                				getFile(Messages.xmlPath).
                				getLocation().toOSString());
                paramMap = editHandler.getAcsFilterParams();
                acsTxt.setText(paramMap.get(Messages.acsAttr));
                relTxt.setText(paramMap.get(Messages.relAttr));
                if (paramMap.get(Messages.certAttr) != null ) {
                	certTxt.setText(paramMap.get(Messages.certAttr));
                	certInfoTxt.setText(getCertInfo(certTxt.getText()));
                } else {
                	certInfoTxt.setText(getEmbeddedCertInfo());
                	embedCertCheck.setSelection(true);
                }
                	
                requiresHttpsCheck.setSelection(!Boolean.
                		valueOf(paramMap.get(Messages.allowHTTPAttr)));
            } catch (Exception e) {
                Activator.getDefault().log(e.getMessage(), e);
            }
        } else {
        	// Add library scenario
        	depCheck.setSelection(true);
        }
        setControl(container);
    }

    /**
     * Method creates Security (certificate and https check) component.
     * @param container
     */
    private void createCertComponent(Composite container) {
        Group certGrp = new Group(container, SWT.SHADOW_ETCHED_IN);
        GridLayout groupGridLayout = new GridLayout();
        groupGridLayout.numColumns = 3;
        groupGridLayout.verticalSpacing = 10;
        groupGridLayout.horizontalSpacing = 0;
        
        GridData groupGridData = new GridData();
        groupGridData.grabExcessHorizontalSpace = true;
        groupGridData.horizontalIndent = 10;
        groupGridData.verticalIndent = 10;
        groupGridData.horizontalAlignment = SWT.FILL;
        
        certGrp.setText(Messages.acsCertGrpTxt);
        certGrp.setLayout(groupGridLayout);
        certGrp.setLayoutData(groupGridData);

        certLbl = new Label(certGrp, SWT.LEFT);
        groupGridData = new GridData();
        groupGridData.grabExcessHorizontalSpace = true;
        groupGridData.horizontalSpan = 3;
        groupGridData.horizontalAlignment = SWT.FILL;
        certLbl.setText(Messages.acsCertLbl);
        certLbl.setLayoutData(groupGridData);

        certTxt = new Text(certGrp, SWT.LEFT | SWT.BORDER);
        groupGridData = new GridData();
        groupGridData.horizontalSpan = 1;
        groupGridData.horizontalAlignment = SWT.FILL;
        groupGridData.grabExcessHorizontalSpace  = true;
        groupGridData.widthHint = 60;
        certTxt.setLayoutData(groupGridData);
        certTxt.addListener(SWT.FocusOut, new Listener() {
           	public void handleEvent(Event arg0) {
           		String certInfo = getCertInfo(certTxt.getText());
           		if(certInfo != null )
           			certInfoTxt.setText(certInfo);
           		else
           			certInfoTxt.setText("");
			}			
         });

        browseBtn = new Button(certGrp, SWT.PUSH);
        groupGridData = new GridData();
        groupGridData.grabExcessHorizontalSpace = false;
        groupGridData.horizontalIndent = 5;
        groupGridData.horizontalAlignment = SWT.FILL;
        groupGridLayout.verticalSpacing = 10;
        groupGridData.widthHint = 65;
        browseBtn.setLayoutData(groupGridData);
        browseBtn.setText(Messages.acsBrwBtn);
        browseBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                browseBtnListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });
        
        newCertBtn = new Button(certGrp, SWT.PUSH);
        groupGridData = new GridData();
        groupGridData.grabExcessHorizontalSpace = false;
        groupGridData.horizontalIndent = 5;
        groupGridData.horizontalAlignment = SWT.FILL;
        groupGridLayout.verticalSpacing = 10;
        groupGridData.widthHint = 65;
        newCertBtn.setLayoutData(groupGridData);
        newCertBtn.setText(Messages.acsnewCertBtn);
        newCertBtn.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
            	newCertBtnListener();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });
        
        //create non editable cert info text
        certInfoTxt = new Text(certGrp, SWT.LEFT | SWT.BORDER | SWT.MULTI );
        certInfoTxt.setEditable(false);
        groupGridData = new GridData();
        groupGridData.horizontalSpan = 3;
        groupGridData.horizontalAlignment = SWT.FILL;
        groupGridData.grabExcessHorizontalSpace  = true;
        groupGridData.widthHint=20;
        groupGridData.heightHint=50;
        certInfoTxt.setLayoutData(groupGridData);
        certInfoTxt.setText(Messages.embedCertDefTxt);
        
        // Create Embed the certificate in the WAR file checkbox
        embedCertCheck = new Button(certGrp, SWT.LEFT | SWT.CHECK);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 3;
        gridData.horizontalAlignment = SWT.FILL;
        embedCertCheck.setText(Messages.embedCert);
        embedCertCheck.setLayoutData(gridData);
        embedCertCheck.setSelection(false);
        
        requiresHttpsCheck = new Button(certGrp, SWT.LEFT | SWT.CHECK);
        requiresHttpsCheck.setText(Messages.requiresHttpsChkBox);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 3;
        gridData.horizontalAlignment = SWT.FILL;
        requiresHttpsCheck.setLayoutData(gridData);
        requiresHttpsCheck.setSelection(true);
        requiresHttpsCheck.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent event) {
            }

            public void widgetSelected(SelectionEvent event) {
                if (event.getSource() instanceof Button) {
                        Button checkBox = (Button) event.getSource();
                        if (checkBox.getSelection()) {
                            //Do nothing
                        } else {
                            boolean choice = MessageDialog.openQuestion(
                            		new Shell(), Messages.requiresHttpsDlgTitle,
                            		Messages.requiresHttpsDlgMsg);
                            if (!choice) {
                                 checkBox.setSelection(true);
                            }
                        }
                 }
            }
        });
   }

    /**
     * Method creates Relying Party Realm component.
     * @param container
     */
    private void createRealmComponent(Composite container) {
        Group relGrp = new Group(container, SWT.SHADOW_ETCHED_IN);
        GridLayout groupGridLayout = new GridLayout();
        GridData groupGridData = new GridData();
        groupGridData.grabExcessHorizontalSpace = true;
        groupGridData.horizontalIndent = 10;
        groupGridData.verticalIndent = 10;
        groupGridData.horizontalAlignment = SWT.FILL;
        groupGridLayout.numColumns = 2;
        groupGridLayout.verticalSpacing = 10;
        relGrp.setText(Messages.acsRelGrpTxt);
        relGrp.setLayout(groupGridLayout);
        relGrp.setLayoutData(groupGridData);

        relLbl = new Label(relGrp, SWT.LEFT);
        groupGridData = new GridData();
        groupGridData.grabExcessHorizontalSpace = true;
        groupGridData.horizontalAlignment = SWT.FILL;
        groupGridLayout.numColumns = 2;
        relLbl.setText(Messages.acsRelLbl);
        relLbl.setLayoutData(groupGridData);

        relTxt = new Text(relGrp, SWT.LEFT | SWT.BORDER);
        groupGridData = new GridData();
        groupGridData.horizontalSpan = 2;
        groupGridData.horizontalAlignment = SWT.FILL;
        groupGridData.grabExcessHorizontalSpace  = true;
        relTxt.setLayoutData(groupGridData);

    }

    /**
     * Method creates ACS Authentication Endpoint component.
     * @param container
     */
    private void createACSComponent(Composite container) {
        Group acsGrp = new Group(container, SWT.SHADOW_ETCHED_IN);
        GridLayout groupGridLayout = new GridLayout();
        GridData groupGridData = new GridData();
        groupGridData.grabExcessHorizontalSpace = true;
        groupGridData.horizontalIndent = 10;
        groupGridData.verticalIndent = 10;
        groupGridData.horizontalAlignment = SWT.FILL;
        groupGridLayout.numColumns = 2;
        groupGridLayout.verticalSpacing = 10;
        acsGrp.setText(Messages.acsGrpTxt);
        acsGrp.setLayout(groupGridLayout);
        acsGrp.setLayoutData(groupGridData);

        acsLbl = new Label(acsGrp, SWT.LEFT);
        groupGridData = new GridData();
        groupGridData.grabExcessHorizontalSpace = true;
        groupGridData.horizontalAlignment = SWT.FILL;
        groupGridLayout.numColumns = 2;
        acsLbl.setText(Messages.acsLbl);
        acsLbl.setLayoutData(groupGridData);

        acsTxt = new Text(acsGrp, SWT.LEFT | SWT.BORDER);
        groupGridData = new GridData();
        groupGridData.horizontalSpan = 2;
        groupGridData.horizontalAlignment = SWT.FILL;
        groupGridData.grabExcessHorizontalSpace  = true;
        acsTxt.setText(Messages.acsTxt);
        acsTxt.setLayoutData(groupGridData);
   }

    /**
     * Method creates Include in the project deployment assembly
     * check box.
     * @param container
     */
    private void createDepCheckBox(Composite container) {
        depCheck = new Button(container, SWT.CHECK);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalIndent = 10;
        gridData.verticalIndent = 10;
        gridData.horizontalSpan = 2;
        gridData.horizontalAlignment = SWT.FILL;
        depCheck.setText(Messages.depChkBox);
        depCheck.setLayoutData(gridData);
//        depCheck.setSelection(true);
    }

    /**
     * Populates the combo with available versions of sdk.
     * @param bundles
     */
    private void populateComboBox(Bundle[] bundles) {
        // iterate over bundles and add an entry to combo
        for (Bundle bundle : bundles) {
            combo.add(String.format(Messages.version1,
                    Integer.toString(bundle.getVersion().getMajor()),
                    Integer.toString(bundle.getVersion().getMinor()),
                    Integer.toString(bundle.getVersion().getMicro())));
        }
        // if current version can not be determined
        // then set the first entry as selected.
//        if (curVersion == null || curVersion.isEmpty()) {
//            combo.select(0);
//        } else {
//            combo.setText(String.format(Messages.version2, curVersion));
//        }
        //For now assuming that we always need to show only the latest version.
        combo.select(0);
    }

    @Override
    public void initialize(IJavaProject proj, IClasspathEntry[] classpathEntry) {
    }

    /**
     * Handles finish click.
     */
    @Override
    public boolean finish() {
    	finishVal = validateValues();
    	/*
    	 * Save state of Deployment Assembly check box
    	 * every time, as user may say immediate edit more than once.
    	 */
    	Activator.setDeploymentAssemblyState(depCheck.getSelection());
        if (finishVal) {
            //append the version to sdk container id.
            entry = entry.append(getSelVersion());
            configureDeployment();
        }
        return finishVal;
    }

    /**
     * Returns the version of selected entry in combo box.
     *
     * @return version.
     */
    private String getSelVersion() {
        Bundle[] bundles = Platform.getBundles(Messages.sdkID, null);
        Bundle bundle = null;
        String version = "";
        if (bundles != null) {
            bundle = bundles[combo.getSelectionIndex()];
            version = String.format("%s.%s.%s",
                    Integer.toString(bundle.getVersion().getMajor()),
                    Integer.toString(bundle.getVersion().getMinor()),
                    Integer.toString(bundle.getVersion().getMicro()));
        }
        return version;
    }

    @Override
    public IClasspathEntry getSelection() {
        IClasspathEntry classPathEntry = null;
        if (depCheck.getSelection()) {
            IClasspathAttribute[] attr = new IClasspathAttribute[1];
            attr[0] = JavaCore.newClasspathAttribute(
            		Messages.jstDep, "/WEB-INF/lib");
            classPathEntry = JavaCore.newContainerEntry(entry,
            		null, attr, true);
        } else {
            classPathEntry =  JavaCore.newContainerEntry(entry);
        }
        return classPathEntry;
    }

    @Override
    public void setSelection(IClasspathEntry selEntry) {
//        if (selEntry != null) {
//            curVersion = selEntry.getPath().segment(1);
//        }
    }

    /**
     * Listener class for Combo box.
     */
    private class ComboSelectionListener implements SelectionListener {

        @Override
        public void widgetDefaultSelected(SelectionEvent arg0) {

        }

        @Override
        public void widgetSelected(SelectionEvent arg0) {
            if (arg0.getSource() instanceof Combo) {
                try {
                    Bundle[] bundles = Platform.getBundles(
                            Messages.sdkID, null);
                    Bundle bundle = bundles[combo.getSelectionIndex()];
                    //locate the sdk jar in bundle
                    URL url = FileLocator.find(bundle,
                            new Path(Messages.sdkJar), null);
                    //if sdk jar not found then set an error msg
                    if (url == null) {
                        libLocation = Messages.notFound;
                        location.setText(libLocation);
                        setErrorMessage(Messages.verNotAvail);
                        finishVal = false;
                    } else {
                        //if jar is found then resolve url and get the location
                        url = FileLocator.resolve(url);
                        File loc = new File(url.getPath());
                        libLocation = loc.getAbsolutePath();
                        location.setText(libLocation);
                        setErrorMessage(null);
                        finishVal = true;
                    }
                } catch (Exception ex) {
                    setErrorMessage(Messages.verNotAvail);
                    finishVal = false;
                }
            }
        }

    }

    /**
     * Listener for Browse button
     * to browse certificate files.
     */
    private void browseBtnListener() {
        FileDialog dialog = new FileDialog(this.getShell());
        String []extensions = new String [1];
        extensions[0] = "*.CER";
        dialog.setFilterExtensions(extensions);
        String file = dialog.open();
        if (file != null) {
            certTxt.setText(file.replace('\\', '/'));
            certInfoTxt.setText(getCertInfo(certTxt.getText()));
        }
    }
    
    /**
     * Listener for new button.
     */
    protected void newCertBtnListener() {
    	NewCertificateDialogData data = new NewCertificateDialogData();
        NewCertificateDialog dialog = new NewCertificateDialog(getShell(),data);
        int returnCode = dialog.open();
        if (returnCode == Window.OK) {
        		String certPath = data.getCerFilePath();
                certTxt.setText(certPath != null ? certPath.replace('\\', '/') : certPath );
            	certInfoTxt.setText(getCertInfo(certTxt.getText()));
        }
    }
    
    private static String getCertInfo(String certURL) {
    	InputStream inputStream = null;
    	String url = certURL;
    	try {
    		url = getCertificatePath(url);
    		if(url == null || url.isEmpty())
    			return null;
    		File file = new File(url);
    		if(!file.exists())
    			return null;
    		
	    	inputStream = new FileInputStream(url);
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			X509Certificate acsCert = (X509Certificate)certificateFactory.generateCertificate(inputStream);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

			StringBuilder certInfo = new StringBuilder();
			certInfo.append(String.format("%1$-10s", "Subject")).append(" : ").append(acsCert.getSubjectDN()).append("\n");
			certInfo.append(String.format("%1$-11s", "Issuer")).append(" : ").append(acsCert.getIssuerDN()).append("\n");
			certInfo.append(String.format("%1$-13s", "Valid")).append(" : ").append(dateFormat.format(acsCert.getNotBefore())).append(" to ")
					.append(dateFormat.format(acsCert.getNotAfter()));
			
			inputStream.close();
			return certInfo.toString();
    	} catch(Exception e) {
    		Activator.getDefault().log(e.getMessage(), e);
    		return null;
    	}finally {
    		try {
    			if(inputStream != null)
    				inputStream.close();
    		}catch(Exception e ){
    			//Die silently,no need to throw any error 
    		}
    	}   
    }

    /**
     * Method validates required values.
     * @return boolean
     */
    private boolean  validateValues() {
        boolean isValid = true;
        boolean isEdit  = isEdit();
        StringBuilder errorMessage = new StringBuilder();

        // Display error if acs login page URL is null. Applicable for first time and edit scenarios.
        if(acsTxt.getText().isEmpty() || acsTxt.getText().equalsIgnoreCase(Messages.acsTxt))
        	errorMessage.append(Messages.acsTxtErr).append("\n");
        
        // Display error if relying part realm is null. Applicable for first time and edit scenarios.
        if(relTxt.getText().isEmpty())
        	errorMessage.append(Messages.relTxtErr).append("\n");
        
        // if certificate location does not end with .cer then display error
        if(!certTxt.getText().isEmpty() &&  !certTxt.getText().toLowerCase().endsWith(".cer"))
        	errorMessage.append(Messages.certTxtInvalidExt).append("\n");
        
        // Display error if cert location is empty for first time and for edit scenarios if 
        // embedded cert option is not selected
        if((!isEdit && certTxt.getText().isEmpty()) 
        		|| (isEdit && certTxt.getText().isEmpty() && embedCertCheck.getSelection() == false))
        	errorMessage.append(Messages.certTxtErr).append("\n");
        
        // For first time , if embedded cert option is selected , display error if file does not exist at source 
        if(!isEdit && !certTxt.getText().isEmpty() && embedCertCheck.getSelection() == true) {
        	if(!new File(getCertificatePath(certTxt.getText())).exists()) {
        		errorMessage.append(Messages.acsNoValidCert).append("\n");
        	}
        }
        
        if(errorMessage.length() > 0) { 
        	setErrorMessage(errorMessage.toString());
        	isValid = false;
        } else {
        	setErrorMessage(null);
        }
        return isValid;
    }

    /**
     * Method adds ACS filter and filter mapping tags in web.xml
     * and saves input values given on ACS library page.
     * In case of edit, populates previously set values.
     */
    private void configureDeployment() {
        //edit library
        if (isEdit()) {
            IJavaProject proj1 = JavaCore.create(ACSFilterUtil.getSelectedProject());
            IClasspathEntry[] entries;
            try {
                entries = proj1.getRawClasspath();
                IClasspathEntry[] newentries =
                		new IClasspathEntry[entries.length];

                for (int i = 0; i < entries.length; i++) {
                    if (entries[i].toString().contains(Messages.sdkContainer)) {
                        if (depCheck.getSelection()) {
                            IClasspathAttribute[] attr =
                            		new IClasspathAttribute[1];
                            attr[0] = JavaCore.newClasspathAttribute(
                            		Messages.jstDep,
                            		"/WEB-INF/lib");
                            newentries[i] = JavaCore.newContainerEntry(entry,
                            		null, attr, true);
                        } else {
                        newentries[i] =  JavaCore.newContainerEntry(entry);
                        }
                    } else {
                        newentries[i] = entries[i];
                    }
                }
                proj1.setRawClasspath(newentries, null);
            } catch (Exception e) {
                 Activator.getDefault().log(e.getMessage(), e);
            }
           }

        ACSFilterHandler handler = null;
        try {
            IProject proj = ACSFilterUtil.getSelectedProject();
        if (proj.getFile(Messages.xmlPath).exists()) {
            handler = new ACSFilterHandler(proj.
            		getFile(Messages.xmlPath).getLocation().toOSString());
            handler.setAcsFilterParams(Messages.acsAttr, acsTxt.getText());
            handler.setAcsFilterParams(Messages.relAttr, relTxt.getText());
            if(!embedCertCheck.getSelection()) {
            	handler.setAcsFilterParams(Messages.certAttr, certTxt.getText());
            	if(getEmbeddedCertInfo() != null)
            		removeEmbedCert(ACSFilterUtil.getSelectedProject());
            } else {
            	handler.removeParamsIfExists(Messages.certAttr);
            	if(!certTxt.getText().isEmpty() ) {
            		String webinfLoc 	= ACSFilterUtil.getSelectedProject().getFolder(Messages.depDirLoc).getLocation().toOSString();
            		String certLoc 		= String.format("%s%s%s", webinfLoc,File.separator, Messages.acsCertLoc);
            		File   destination  = new File(certLoc);
            		if(!destination.getParentFile().exists())
            			destination.getParentFile().mkdir();
            		copy(new File(getCertificatePath(certTxt.getText())),destination );
            	}
            }
            handler.setAcsFilterParams(Messages.secretKeyAttr, generateKey());
            handler.setAcsFilterParams(Messages.allowHTTPAttr,requiresHttpsCheck.getSelection() ? "false" : "true");
        } else {
            boolean choice = MessageDialog.openQuestion(this.getShell(),
                    Messages.depDescTtl, Messages.depDescMsg);
            if (choice) {
            	String path = createWebXml();
            	//copy cert into WEB-INF/cert/_acs_signing.cer location if embed cert is selected 
            	if(embedCertCheck.getSelection()) {
            		String webinfLoc 	= ACSFilterUtil.getSelectedProject().getFolder(Messages.depDirLoc).getLocation().toOSString();
            		String certLoc 		= String.format("%s%s%s", webinfLoc,File.separator, Messages.acsCertLoc);
            		File   destination  = new File(certLoc);
            		if(!destination.getParentFile().exists())
            			destination.getParentFile().mkdir();
            		copy(new File(getCertificatePath(certTxt.getText())),destination );
            	}
            	handler = new ACSFilterHandler(path);
            	handler.setAcsFilterParams(Messages.acsAttr, acsTxt.getText());
            	handler.setAcsFilterParams(Messages.relAttr, relTxt.getText());
            	if(!embedCertCheck.getSelection()) { //Donot make entry if embed cert is selected
            		handler.setAcsFilterParams(Messages.certAttr, certTxt.getText());
            		if(getEmbeddedCertInfo() != null)
            			removeEmbedCert(ACSFilterUtil.getSelectedProject());
            	}
            	handler.setAcsFilterParams(Messages.secretKeyAttr, generateKey());
            	handler.setAcsFilterParams(Messages.allowHTTPAttr,requiresHttpsCheck.getSelection() ? "false" : "true");
            } else {
                finishVal = true;
                return;
            }
        }
        } catch (Exception e) {
            MessageDialog.openError(this.getShell(),
            		Messages.acsErrTtl, Messages.acsErrMsg);
            finishVal = false;
            Activator.getDefault().log(e.getMessage(), e);
        }
        try {
        	handler.save();
        	IWorkspace workspace = ResourcesPlugin.getWorkspace();
        	IWorkspaceRoot root = workspace.getRoot();
        	root.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (Exception e) {
        	Activator.getDefault().log(e.getMessage(), e);
        	MessageDialog.openError(this.getShell(),
        			Messages.acsErrTtl, Messages.saveErrMsg);
        	finishVal = false;
        }
    }

    /**
     * Method generates key using
     * Advanced Encryption Standard algorithm.
     * @return String
     * @throws Exception
     */
    private String generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();

        byte[] keyInBytes = secretKey.getEncoded();
        String key = Base64.encode(keyInBytes);
        return key;
    }

    /**
     * Method creates web.xml (deployment descriptor)
     * in WebContent\WEB-INF folder of dynamic web project
     * if does not present already.
     * @return String
     */
    private String createWebXml() {
        String path = null;
        try {
            String cmpntFileLoc = ACSFilterUtil.getSelectedProject().
            		getFolder(Messages.depDirLoc).
            		getLocation().toOSString();
            String cmpntFile = String.format("%s%s%s", cmpntFileLoc,
            		File.separator, Messages.depFileName);
            if (!new File(cmpntFile).exists()) {
                URL url = Activator.getDefault().getBundle()
                        .getEntry(Messages.resFileLoc);
                URL fileURL = FileLocator.toFileURL(url);
                URL resolve = FileLocator.resolve(fileURL);
                File file = new File(resolve.getFile());
                FileInputStream fis = new FileInputStream(file);
                File outputFile = new File(cmpntFile);
                OutputStream fos = new FileOutputStream(outputFile);
                writeFile(fis , fos);
                path = cmpntFile;
        } else {
        	path = cmpntFile;
        }
        } catch (IOException e) {
            MessageDialog.openError(this.getShell(),
            		Messages.acsErrTtl, Messages.fileCrtErrMsg);
            finishVal = false;
            Activator.getDefault().log(e.getMessage(), e);
        }
       return new File(path).getPath();
    }
    
    private String getEmbeddedCertInfo() {
    	String webinfLoc 	= ACSFilterUtil.getSelectedProject().getFolder(Messages.depDirLoc).getLocation().toOSString();
		String certLoc 		= String.format("%s%s%s", webinfLoc,File.separator, Messages.acsCertLoc);
    	return getCertInfo(certLoc);
    }

   
    public static void copy(File source, final File destination) throws IOException {
        InputStream instream = null;
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }
            String[] kid = source.list();
            for (int i = 0; i < kid.length; i++) {
            	copy(new File(source, kid[i]),
                        new File(destination, kid[i]));
            }
        } else {
            //InputStream instream = null;
            OutputStream out = null;
            try {
            	if(destination != null && destination.isFile() && !destination.getParentFile().exists() )
            		destination.getParentFile().mkdirs();
            	
                instream = new FileInputStream(source);
                out = new FileOutputStream(destination);
                byte[] buf = new byte[BUFF_SIZE];
                int len = instream.read(buf);

                while (len > 0) {
                    out.write(buf, 0, len);
                    len = instream.read(buf);
                }
            } finally {
                if (instream != null) {
                    instream.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
    }
    
    /**
     * Method writes into file.
     * @param inStream
     * @param outStream
     * @throws IOException
     */
    public static void writeFile(InputStream inStream, OutputStream out) throws IOException {
        try {
            byte[] buf = new byte[BUFF_SIZE];
            int len = inStream.read(buf);
            while (len > 0) {
                out.write(buf, 0, len);
                len = inStream.read(buf);
            }
        } finally {
            if (inStream != null) {
                inStream.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
    
    public void removeEmbedCert(IProject iProject) {
    	String webinfLoc 	= iProject.getFolder(Messages.depDirLoc).getLocation().toOSString();
		String certLoc 		= String.format("%s%s%s", webinfLoc,File.separator, Messages.acsCertLoc);
		File   destination  = new File(certLoc);
		if(destination.exists())
			destination.delete();
		if(destination.getParentFile().exists() && destination.getParentFile().list().length == 0)
			destination.getParentFile().delete();
    }
    
    /**
     * Method disables all components on ACS page.
     */
    private void disableAll() {
      acsTxt.setEnabled(false);
      certTxt.setEnabled(false);
      relTxt.setEnabled(false);
      combo.setEnabled(false);
      acsLbl.setEnabled(false);
      certLbl.setEnabled(false);
      lblLoc.setEnabled(false);
      relLbl.setEnabled(false);
      depCheck.setEnabled(false);
      browseBtn.setEnabled(false);
      newCertBtn.setEnabled(false);
      location.setEnabled(false);
      requiresHttpsCheck.setEnabled(false);
      embedCertCheck.setEnabled(false);
      certInfoTxt.setEnabled(false);
    }

    /**
     * @return current window is edit or not
     */
    private boolean isEdit() {
        return getWizard().getWindowTitle().
        		equals(Messages.edtLbrTtl);
    }
    
    /**
     * This method resolves environment variables in path. Format of env variables is ${env.VAR_NAME}
     * e.g. ${env.JAVA_HOME} 
     * @param rawPath
     * @return
     */
    private static String getCertificatePath(String rawPath) {
		String certPath = null;
		String pathToUse = rawPath;
		if (pathToUse != null && pathToUse.length() > 0) {
			pathToUse = pathToUse.replace('\\', '/');
			String[] result = pathToUse.split("/");
			StringBuilder  path = new StringBuilder();

			for (int x = 0; x < result.length; x++) {
				if (result[x].startsWith("${env")) {
					String envValue = System.getenv(result[x].substring("${env.".length(), (result[x].length() - 1)));
					path.append(envValue).append(File.separator);
				} else {
					path.append(result[x]).append(File.separator);
				}
			}
			//Delete last trailing slash
			path = path.deleteCharAt(path.length() - 1);
			certPath = path.toString();
		}
		return certPath;
	}
}
