/*
 * Copyright (c) Microsoft Open Technologies, Inc.
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.ui.config;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.microsoft.applicationinsights.ui.activator.Activator;
import com.microsoft.applicationinsights.util.AILibraryUtil;
import com.microsoft.applicationinsights.util.AILibraryHandler;

public class AIProjConfigWizardDialog extends TitleAreaDialog {
	private Button aiCheck;
	private Label lblInstrumentationKey;
	private Text txtInstrumentationKey;
	private Link lnkInstrumentationKey;
	private Link lnkAIPrivacy;

	AILibraryHandler handler;
	IProject proj;
	String webxmlPath;
	String aiXMLPath;
	String depDirLoc;
	String aiConfRelDirLoc;

	public AIProjConfigWizardDialog(Shell parentShell) {
		super(parentShell);
		setHelpAvailable(false);
		handler = new AILibraryHandler();
		proj = getSelectedProject();

		try {
			// check if its maven project or simple dynamic web project
			if (proj.hasNature(Messages.natMaven)) {
				webxmlPath = Messages.webxmlPathMaven;
				aiXMLPath = Messages.aiXMLPathMaven;
				depDirLoc = Messages.depDirLocMaven;
				aiConfRelDirLoc = Messages.aiConfRelDirLocMaven;
			} else {
				webxmlPath = Messages.webxmlPath;
				aiXMLPath = Messages.aiXMLPath;
				depDirLoc = Messages.depDirLoc;
				aiConfRelDirLoc = Messages.aiConfRelDirLoc;
			}
			if (proj.getFile(webxmlPath).exists()) {
				handler.parseWebXmlPath(proj.getFile(webxmlPath)
						.getLocation().toOSString());
			}
			if (proj.getFile(aiXMLPath).exists()) {
				handler.parseAIConfXmlPath(proj.getFile(aiXMLPath)
						.getLocation().toOSString());
			}
		} catch (Exception e) {
			// just log and ignore
			Activator.getDefault().log(Messages.aiParseError, e);
		}
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.aiConfigWiz);
		newShell.setLocation(250, 250);
	}

	protected Control createDialogArea(Composite parent) {
		setTitle(Messages.aiTxt);
		setMessage(Messages.aiMsg);
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData gridData = new GridData();
		gridLayout.marginBottom = 50;
		gridData.widthHint = 550;
		gridData.verticalIndent = 10;
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		createAICheckButton(container);
		createAIControl(container);
		createLinks(container);

		if (isEdit()) {
			populateData();
		} else {
			if (aiCheck.getSelection()) {
				txtInstrumentationKey.setEnabled(true);
			} else {
				txtInstrumentationKey.setEnabled(false);
			}
		}

		return super.createDialogArea(parent);
	}

	private void populateData() {
		aiCheck.setSelection(true);
		txtInstrumentationKey.setText(handler.getAIInstrumentationKey());
		txtInstrumentationKey.setEnabled(true);
	}

	private boolean isEdit() {
		try {
			return handler.isAIWebFilterConfigured();
		} catch (Exception e) {
			// just return false if there is any exception
			return false;
		}
	}
	
	private void createAICheckButton(Composite container) {
		aiCheck = new Button(container, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.verticalIndent = 5;
		gridData.horizontalIndent = 5;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		aiCheck.setText(Messages.aiCheckBoxTxt);
		aiCheck.setLayoutData(gridData);

		aiCheck.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (aiCheck.getSelection()) {
					txtInstrumentationKey.setEnabled(true);
					txtInstrumentationKey.setText(handler
							.getAIInstrumentationKey() != null ? handler
							.getAIInstrumentationKey() : "");
				} else {
					txtInstrumentationKey.setText("");
					txtInstrumentationKey.setEnabled(false);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	/**
	 * Creates the cache name component.
	 * 
	 * @param container
	 */
	private void createAIControl(Composite container) {
		lblInstrumentationKey = new Label(container, SWT.LEFT);
		GridData gridData = new GridData();
		gridData.horizontalIndent = 10;
		gridData.verticalIndent = 5;
		lblInstrumentationKey.setText(Messages.lblInstrumentationKey);
		lblInstrumentationKey.setLayoutData(gridData);

		txtInstrumentationKey = new Text(container, SWT.LEFT | SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalIndent = 30;
		gridData.horizontalAlignment = SWT.RIGHT;
		gridData.verticalIndent = 5;
		gridData.widthHint = 500;
		gridData.grabExcessHorizontalSpace = true;
		txtInstrumentationKey.setLayoutData(gridData);
	}
		
	private void createLinks(Composite container) {
		// InstrumentationKey Link
		lnkInstrumentationKey = new Link(container, SWT.RIGHT);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 5;
		lnkInstrumentationKey.setText(Messages.lnkInstrumentationKey);
		lnkInstrumentationKey.setLayoutData(gridData);
		lnkInstrumentationKey.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().
					getExternalBrowser().openURL(new URL(event.text));
				}
				catch (Exception ex) {
					/*
					 * only logging the error in log file
					 * not showing anything to end user
					 */
					Activator.getDefault().log(
							Messages.lnkInstrumentationKey, ex);
				}
			}
		});
		
		// Privacy Link
		lnkAIPrivacy = new Link(container, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 5;
		lnkAIPrivacy.setText(Messages.lnkAIPrivacy);
		lnkAIPrivacy.setLayoutData(gridData);
		lnkAIPrivacy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().
					getExternalBrowser().openURL(new URL(event.text));
				}
				catch (Exception ex) {
					/*
					 * only logging the error in log file
					 * not showing anything to end user
					 */
					Activator.getDefault().log(
							Messages.lnkAIPrivacy, ex);
				}
			}
		});
	}

	@Override
	protected void okPressed() {
		boolean okToProceed = true;

		// validate
		if (aiCheck.getSelection() == true && (txtInstrumentationKey.getText() == null
				|| txtInstrumentationKey.getText().trim().length() == 0)) {
			MessageDialog.openError(getShell(), Messages.aiErrTitle,
					Messages.aiInstrumentationKeyNull);
			okToProceed = false;
		} else if (aiCheck.getSelection() == false) {
			// disable if exists
			try {
				handler.disableAIFilterConfiguration(true);
				handler.removeAIFilterDef();
				handler.save();
			} catch (Exception e) {
				MessageDialog.openError(getShell(), Messages.aiErrTitle,
						Messages.aiConfigRemoveError + e.getLocalizedMessage());
			}
		} else {
			try {
				IJavaProject iJavaProject = JavaCore.create(proj);
				createAIConfiguration(iJavaProject);
				configureAzureSDK(iJavaProject);
			} catch (Exception e) {
				MessageDialog.openError(getShell(), Messages.aiErrTitle,
						Messages.aiConfigError + e.getLocalizedMessage());
				okToProceed = false;
			}
		}

		if (okToProceed) {
			try {
				proj.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			super.okPressed();
		}
	}

	private void createAIConfiguration(IJavaProject proj) throws Exception {
		handleWebXML(proj.getProject(), handler);
		handleAppInsightsXML(proj.getProject(), handler);
		handler.save();
	}

	private void handleWebXML(IProject proj, AILibraryHandler handler)
			throws Exception {
		if (proj.getFile(webxmlPath).exists()) {
			handler.parseWebXmlPath(proj.getFile(webxmlPath)
					.getLocation().toOSString());
			handler.setAIFilterConfig();
		} else { // create web.xml
			boolean choice = MessageDialog.openQuestion(this.getShell(),
					Messages.depDescTtl, Messages.depDescMsg);
			if (choice) {
				String path = AILibraryUtil.createFileIfNotExists(
						Messages.depFileName, depDirLoc,
						Messages.resFileLoc);
				handler.parseWebXmlPath(path);
			} else {
				throw new Exception(
						": Application Insights cannot be configured without creating web.xml ");
			}
		}
	}

	private void handleAppInsightsXML(IProject proj, AILibraryHandler handler)
			throws Exception {
		if (proj.getFile(aiXMLPath).exists()) {
			handler.parseAIConfXmlPath(proj.getFile(aiXMLPath)
					.getLocation().toOSString());
			handler.disableAIFilterConfiguration(false);
		} else { // create ApplicationInsights.xml
			String path = AILibraryUtil.createFileIfNotExists(
					Messages.aiConfFileName, aiConfRelDirLoc,
					Messages.aiConfResFileLoc);
			handler.parseAIConfXmlPath(path);
		}

		if (txtInstrumentationKey.getText() != null
				&& txtInstrumentationKey.getText().length() > 0) {
			handler.setAIInstrumentationKey(txtInstrumentationKey.getText()
					.trim());
		}
	}

	private IProject getSelectedProject() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		ISelectionService service = window.getSelectionService();
		ISelection selection = service.getSelection();
		Object element = null;
		IResource resource;
		IProject selProject = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSel = (IStructuredSelection) selection;
			element = structuredSel.getFirstElement();
		}
		if (element instanceof IProject) {
			resource = (IResource) element;
			selProject = resource.getProject();
		} else if (element instanceof IJavaProject) {
			IJavaProject proj = ((IJavaElement) element).getJavaProject();
			selProject = proj.getProject();
		}
		return selProject;
	}

	private void configureAzureSDK(IJavaProject proj) {
		try {
			IClasspathEntry[] classpath = proj.getRawClasspath();

			for (IClasspathEntry iClasspathEntry : classpath) {
				final IPath containerPath = iClasspathEntry.getPath();
				if (containerPath.toString().contains(Messages.azureSDKcontainerID)) {
					return;
				} 
			}
			
			List<IClasspathEntry> list = new ArrayList<IClasspathEntry>(
					java.util.Arrays.asList(classpath));
			IClasspathAttribute[] attr = new IClasspathAttribute[1];
			attr[0] = JavaCore.newClasspathAttribute(Messages.jstDep,
					"/WEB-INF/lib");
			IClasspathEntry jarEntry = JavaCore.newContainerEntry(
					new Path(Messages.azureSDKcontainerID)
							.append(getLatestSDKVersion()), null, attr,
					false);
			list.add(jarEntry);
			IClasspathEntry[] newClasspath = (IClasspathEntry[]) list
					.toArray(new IClasspathEntry[0]);
			proj.setRawClasspath(newClasspath, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public IClasspathEntry[] getClasspathEntriesOfAzureLibabries(
			IPath containerPath) {
		String sdkID = "com.microsoftopentechnologies.windowsazure.tools.sdk";
		Bundle bundle = Platform.getBundle(sdkID);
		// Search the available SDKs
		Bundle[] bundles = Platform.getBundles(sdkID, null);
		List<IClasspathEntry> listEntries = new ArrayList<IClasspathEntry>();
		if (bundles != null) {
			for (Bundle bundle2 : bundles) {
				if (bundle2.getVersion().toString()
						.startsWith(containerPath.segment(1))) {
					bundle = bundle2;
					break;
				}
			}

			// Get the SDK jar.
			URL sdkJar = FileLocator.find(bundle, new Path(
					"azure-core-0.7.0.jar"), null);
			URL resSdkJar = null;
			IClasspathAttribute[] attr = null;
			try {
				if (sdkJar != null) {
					resSdkJar = FileLocator.resolve(sdkJar);
					// create classpath attribute for java doc, if present
				}
				if (resSdkJar == null) {
					/*
					 * if sdk jar is not present then create an place holder for
					 * sdk jar so that it would be shown as missing file
					 */
					URL bundleLoc = new URL(bundle.getLocation());
					StringBuffer strBfr = new StringBuffer(bundleLoc.getPath());
					strBfr.append(File.separator)
							.append("azure-core-0.7.0.jar");
					URL jarLoc = new URL(strBfr.toString());
					IPath jarPath = new Path(FileLocator.resolve(jarLoc)
							.getPath());
					File jarFile = jarPath.toFile();
					listEntries.add(JavaCore.newLibraryEntry(
							new Path(jarFile.getAbsolutePath()), null, null,
							null, attr, true));
				} else {
					File directory = new File(resSdkJar.getPath());
					// create the library entry for sdk jar
					listEntries.add(JavaCore.newLibraryEntry(
							new Path(directory.getAbsolutePath()), null, null,
							null, attr, true));
					FilenameFilter sdkJarsFilter = new SDKJarsFilter();
					File[] jars = new File(String.format("%s%s%s",
							directory.getParent(), File.separator,
							Messages.depLocation)).listFiles(sdkJarsFilter);
					for (int i = 0; i < jars.length; i++) {
						listEntries.add(JavaCore.newLibraryEntry(new Path(
								jars[i].getAbsolutePath()), null, null, null,
								attr, true));
					}
				}
			} catch (Exception e) {
				listEntries = new ArrayList<IClasspathEntry>();
				Activator.getDefault().log(Messages.excp, e);
			}
		}

		IClasspathEntry[] entries = new IClasspathEntry[listEntries.size()];
		// Return the classpath entries.
		return listEntries.toArray(entries);
	}

	private String getLatestSDKVersion() {
		Bundle bundle = Platform.getBundle(Messages.sdkID);
		String version = "";
		if (bundle != null) {
			version = String.format("%s.%s.%s",
					Integer.toString(bundle.getVersion().getMajor()),
					Integer.toString(bundle.getVersion().getMinor()),
					Integer.toString(bundle.getVersion().getMicro()));
		}
		return version;
	}
}

/**
 * This class acts as a filter to accept jar files only.
 */
class SDKJarsFilter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		return (name.endsWith(".jar") && name.indexOf(Messages.src) == -1);
	}
}
