/**
 * Copyright 2012 Persistent Systems Ltd.
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
package com.persistent.ui.projwizard;

import java.net.URL;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;

import waeclipseplugin.Activator;

/**
 * This class creates page for adding key features.
 */
public class WAKeyFeaturesPage extends WizardPage {

	private Button ssnAffCheckBtn;
	private Button cachCheckBtn;
	private Button debugCheckBtn;
	private Label ssnAffLbl;
	private Label cacheLbl;
	private Label debugLbl;
	private Label note;
	private Link ssnAffLink;
	private Link cacheLink;
	private Link debugLink;

	/**
	 * Constructor.
	 * @param pageName
	 */
	protected WAKeyFeaturesPage(String pageName) {
		super(pageName);
		setTitle(Messages.wizPageTitle);
		setDescription(Messages.keyFtrPgMsg);
		setPageComplete(true);
	}

	/**
	 * To draw controls on page.
	 */
	@Override
	public void createControl(Composite parent) {
		// display help contents
		PlatformUI
		.getWorkbench()
		.getHelpSystem()
		.setHelp(parent.getShell(),
				"com.persistent.winazure.eclipseplugin."
						+ "windows_azure_project");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(gridLayout);
		container.setLayoutData(gridData);

		// Create Session Affinity check box and label
		createSsnAffChk(container);
		// Create Caching check box and label
		createCachingChk(container);
		// Create Debugging check box and label
		createDebuggingChk(container);
		createNote(container);
		setControl(container);
	}

	/**
	 * Creates note.
	 * @param container
	 */
	private void createNote(Composite container) {
		note = new Label(container, SWT.LEFT);
		GridData gridData = new GridData();
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalIndent = 50;
		gridData.horizontalIndent = 1;
		gridData.horizontalSpan = 2;
		note.setText(Messages.keyFtrPgNote);
		note.setLayoutData(gridData);
	}

	/**
	 * Creates the debugging check button
	 * and adds selection listener to it.
	 * This check box enables/disables the remote debugging.
	 * @param container
	 */
	private void createDebuggingChk(Composite container) {
		// Check box
		debugCheckBtn = new Button(container, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.verticalIndent = 10;
		gridData.horizontalIndent = 1;
		gridData.grabExcessHorizontalSpace = true;
		debugCheckBtn.setText(Messages.chkLblDebug);
		debugCheckBtn.setLayoutData(gridData);

		// Link
		debugLink = new Link(container, SWT.END);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 170;
		gridData.verticalIndent = 10;
		debugLink.setText(Messages.debugLnk);
		debugLink.setLayoutData(gridData);
		debugLink.addSelectionListener(new SelectionAdapter() {
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
							Messages.lnkOpenErrMsg, ex);
				}
			}
		});

		// Label
		debugLbl = new Label(container, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalIndent = 4;
		gridData.horizontalIndent = 20;
		gridData.horizontalSpan = 2;
		debugLbl.setText(Messages.lblDebug);
		debugLbl.setLayoutData(gridData);
	}

	/**
	 * Creates the cache check button and adds selection listener to it.
	 * This check box enables/disables the caching.
	 * @param container
	 */
	private void createCachingChk(Composite container) {
		// Check box
		cachCheckBtn = new Button(container, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.verticalIndent = 10;
		gridData.horizontalIndent = 1;
		gridData.grabExcessHorizontalSpace = true;
		cachCheckBtn.setText(Messages.chkLblCach);
		cachCheckBtn.setLayoutData(gridData);

		// Link
		cacheLink = new Link(container, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 170;
		gridData.verticalIndent = 10;
		cacheLink.setText(Messages.cachLnk);
		cacheLink.setLayoutData(gridData);
		cacheLink.addSelectionListener(new SelectionAdapter() {
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
							Messages.lnkOpenErrMsg, ex);
				}
			}
		});

		// Label
		cacheLbl = new Label(container, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalIndent = 4;
		gridData.horizontalIndent = 20;
		gridData.horizontalSpan = 2;
		cacheLbl.setText(Messages.lblCache);
		cacheLbl.setLayoutData(gridData);
	}

	/**
	 * Creates the session affinity check button
	 * and adds selection listener to it.
	 * This check box enables/disables the session affinity.
	 * @param container
	 */
	private void createSsnAffChk(Composite container) {
		// Check box
		ssnAffCheckBtn = new Button(container, SWT.CHECK);
		GridData gridData = new GridData();
		gridData.verticalIndent = 5;
		gridData.horizontalIndent = 1;
		gridData.grabExcessHorizontalSpace = true;
		ssnAffCheckBtn.setText(Messages.chkLbSsnAff);
		ssnAffCheckBtn.setLayoutData(gridData);

		// Link
		ssnAffLink = new Link(container, SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 170;
		gridData.verticalIndent = 5;
		ssnAffLink.setText(Messages.ssnAffLnk);
		ssnAffLink.setLayoutData(gridData);
		ssnAffLink.addSelectionListener(new SelectionAdapter() {
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
							Messages.lnkOpenErrMsg, ex);
				}
			}
		});

		// Label
		ssnAffLbl = new Label(container, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalIndent = 4;
		gridData.horizontalIndent = 20;
		gridData.horizontalSpan = 2;
		ssnAffLbl.setText(Messages.lblSsnAff);
		ssnAffLbl.setLayoutData(gridData);
	}

	/**
	 * Return whether session affinity check box is checked or not.
	 * @return boolean
	 */
	public boolean isSsnAffChecked() {
		return ssnAffCheckBtn.getSelection();
	}

	/**
	 * Return whether cache check box is checked or not.
	 * @return boolean
	 */
	public boolean isCacheChecked() {
		return cachCheckBtn.getSelection();
	}

	/**
	 * Return whether debugging check box is checked or not.
	 * @return boolean
	 */
	public boolean isDebugChecked() {
		return debugCheckBtn.getSelection();
	}
}
