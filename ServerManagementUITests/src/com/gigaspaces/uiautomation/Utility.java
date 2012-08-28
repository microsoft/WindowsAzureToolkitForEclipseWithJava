package com.gigaspaces.uiautomation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import com.gigaspaces.uiautomation.ContextMenuHelper.SWTBotMenuHolder;


public class Utility {

    protected static SWTWorkbenchBot bot = new SWTWorkbenchBot();
	protected static long publishTimeOut = 180000;
	protected static long unpublishTimeOut = 120000;
	protected static long endOfTestsTimeOut = 5000;
	
	public static final String USER_TEMP_FOLDER_PATH = System.getProperty("java.io.tmpdir") + "swtbot-test-resources" + File.separator;
	
    protected enum Operation {
		PUBLISH,
		UNPUBLISH
	}
    protected enum SubscriptionFileType {
		PFX,
		PUBLISH_SETTINGS
	}
    protected enum Status {
		COMPLETED,
		FAILED
	}
	
    protected static void createNewWindowsAzureBasicProject() {

		bot.menu(Messages.FileMenuName).menu(Messages.FileNewMenuName).menu(Messages.waNewProjectWizardName).click();

		bot.textWithLabel(Messages.projectNameText).setText(Messages.azureProjName);

		bot.button(Messages.newAzureProjFinishBtn).click();

		bot.sleep(2000);
		
		assertTrue(Utility.isProjExist(Messages.azureProjName));
	}
    
    protected static void createNewWindowsAzureFullProject() {

		bot.menu(Messages.FileMenuName).menu(Messages.FileNewMenuName).menu(Messages.waNewProjectWizardName).click();

		bot.textWithLabel(Messages.projectNameText).setText(Messages.azureProjName);

		addApplicationToProject();
		
		bot.button(Messages.newAzureProjFinishBtn).click();

		bot.sleep(2000);
		
		assertTrue(Utility.isProjExist(Messages.azureProjName));
	}

	
    protected static boolean isProjExist(String name) {
        try {
            SWTBotView pkgExplorer = getProjExplorer();
            SWTBotTree tree = pkgExplorer.bot().tree();
            tree.getTreeItem(name);
            return true;
        } catch (WidgetNotFoundException e) {
            return false;
        }
    }

    protected static SWTBotView getProjExplorer() {
        SWTBotView view;
        if (bot.activePerspective().getLabel().equals("Resource")
                || bot.activePerspective().getLabel().equals("Java EE")) {
        view = bot.viewByTitle("Project Explorer");
        } else {
            view = bot.viewByTitle("Package Explorer");
        }
        return view;

    }

    protected static void publish(SubscriptionFileType subscriptionFileType) throws Exception {	
		// open the publish wizard
		openWizard(Operation.PUBLISH);

		// fill the publish azure project page
		fillPublishAzureProjectPage(subscriptionFileType);
		System.out.println("filled the Publish Azure Project Page settings.");

		// click the publish button

		bot.button(com.gigaspaces.azure.handler.Messages.publish).click();
		System.out.println("clicked on Publish.");

		// check the log view, wait for deploy status
		validateStatus(Status.COMPLETED);
	}

	protected static void unPublish() throws Exception	{
		openWizard(Operation.UNPUBLISH);
		bot.shell(Messages.waUnpublishWizardName).activate();
		bot.comboBox(Integer.valueOf(Messages.unpublishHostedServiceComboBoxIndex)).setSelection(Messages.serviceName);
		getComboBoxWhenAvailable(Integer.valueOf(Messages.unpublishDeploymentsComboBoxIndex), 
				Long.valueOf(Messages.hostedServiceLoadingTimeout)).setSelection(Messages.deploymentName);
		bot.activeShell().bot().button(com.gigaspaces.azure.handler.Messages.unpublish).click();
		System.out.println("Waiting for project undeployment process to end...");
		
		validateStatus(Status.COMPLETED);
	}

	protected static void openWizard(Operation operation) 	{
		//select the azure project
		SWTBotTree treeItem = Utility.getProjExplorer().bot().tree().select(Messages.azureProjName);
		
		// open context menu
		SWTBotMenuHolder holder = new SWTBotMenuHolder();
		ContextMenuHelper helper = new ContextMenuHelper(holder);
		String subMenu = com.gigaspaces.azure.handler.Messages.publish;
		if(operation == Operation.UNPUBLISH)
			subMenu = com.gigaspaces.azure.handler.Messages.unpublish;
		// click on the sub-menu to open the publish/unpublish wizard
		helper.clickOnContextMenu(treeItem, Messages.waManuName, subMenu);
		
		//Activate wizard
		if(operation == Operation.PUBLISH)
		{
			// build
			bot.button(Messages.waBuildConfirmBtn).click();
			System.out.println("building...");
			activateShellWhenAvailable(Messages.waPublishWizardName, Long.valueOf(Messages.buildTimeout));
		}
		else
			bot.shell(Messages.waUnpublishWizardName).activate();

		System.out.println(operation + " wizard shell is active.");
	}
	
	protected static boolean isImportErrorWindowDisplayed() {
		
		try {
			activateShellWhenAvailable("Import...", Long.valueOf(Messages.restCallTimeout));
			return true;
		}
		catch (Exception e) {
			return false;
		}
		
	}

	protected static void activateShellWhenAvailable(final String shellTitle, long timeout) {
		bot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() throws Exception {

				try{
					bot.shell(shellTitle);
					return true;
				}
				catch (Exception e) {}
				return false;
			}

			@Override
			public String getFailureMessage() {
				return "Cannot activate the " + shellTitle + " shell";
			}
		}, timeout);
		bot.shell(shellTitle).activate();
	}

	protected static void fillPublishAzureProjectPage(SubscriptionFileType subscriptionFileType) {	
		if(subscriptionFileType == SubscriptionFileType.PUBLISH_SETTINGS)
		{
			// select the publishSettings file
			String projectRootDirPath = Utility.getResourcesFolder();
			setSubscriptionSettings(projectRootDirPath + Messages.publishSettingsFilePath);
			System.out.println("Added the publishSettings file.");
		}
		else
		{
			// select the pfx file with correct id and password
			String projectRootDirPath = getResourcesFolder();
			setPfxSettings(projectRootDirPath + Messages.pfxPath, Messages.pfxSubscriptionId, Messages.pfxSubscriptionPassword);
			System.out.println("Added the pfx file, subscription and password.");
		}

		// select a service
		selectService(Messages.serviceName);

		// update cspkg and cscfg files
		selectFromWorkspace(Integer.valueOf(Messages.cspkgWorkspaceIndex), com.gigaspaces.azure.wizards.Messages.deployDir, Messages.cspkgFileName);
		selectFromWorkspace(Integer.valueOf(Messages.cscfgWorkspaceIndex), com.gigaspaces.azure.wizards.Messages.deployDir, Messages.cscfgFileName);	

		System.out.println("updated the .cspkg and .cscfg files.");
	}
	
	protected static String fillNewCertificateDetails() {
		
		bot.button(com.gigaspaces.azure.wizards.Messages.deplNewSubscrLbl).click();
		bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.certDlgPwdLbl).setText(Messages.remDesktopPassword);
		bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.certDlgConfPwdLbl).setText(Messages.remDesktopPassword);
		String projectRootDirPath = getResourcesFolder();
		String folderToSave = projectRootDirPath;
		String pfx = folderToSave + Messages.generatedPfxFileName;
		String cert = folderToSave + Messages.generatedCertFileName;
		bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.certDlgPFXLbl).setText(pfx);
		bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.certDlgCertLbl).setText(cert);	
		bot.button("OK").click();		
		return folderToSave;
		
		
	}

	protected static void setSubscriptionSettings(String subscriptionPath) {
		// select the file
		bot.button(com.gigaspaces.azure.wizards.Messages.deplImportLbl).click();
		bot.text().setText(subscriptionPath);
		bot.sleep(5000);
		bot.button(Messages.subscriptionImportConfirmBtn).click();
	}

	protected static void setPfxSettings(String subscriptionPath, String subscriptionId, String SubscriptionPassword) {
		// select the pfx file
		setSubscriptionSettings(subscriptionPath);	
		// insert subscription id and password
		bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.subscriptionIdLbl).setText(subscriptionId);
		bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.pfxFilePasswordLbl).setText(SubscriptionPassword);
		bot.sleep(5000);
		bot.button(com.gigaspaces.azure.wizards.Messages.addBut).click();
		System.out.println("Added the pfx file, subscription and password.");
	}

	protected static void selectService(String serviceName) {
		SWTBotCombo comboBox = getComboBoxWhenAvailable(Integer.valueOf(Messages.hostedServiceComboBoxIndex), Long.valueOf(Messages.subscriptionLoadingTimeout));
		String[] items = comboBox.items();
		if(!Arrays.asList(items).contains(serviceName))
		{
			bot.button(com.gigaspaces.azure.wizards.Messages.newBtn,Integer.valueOf(Messages.newHostedServiceBtnIndex)).click();
			bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.hostedServiceLbl).setText(serviceName);
			bot.comboBox().setSelection(Messages.newHostedServiceLocationText);
			bot.button(com.gigaspaces.azure.wizards.Messages.hostedCreate).click();
			System.out.println(serviceName + " service was created.");
		}	
		comboBox.setSelection(serviceName);
		System.out.println(serviceName + " service was selected.");
	}

	protected static SWTBotCombo getComboBoxWhenAvailable(final int comboBoxIndex, long timeout) {
		bot.waitUntil(new DefaultCondition() {
			@Override
			public boolean test() throws Exception {

				try{
					bot.comboBox(comboBoxIndex);
					return true;
				}
				catch (Exception e) {}
				return false;
			}

			@Override
			public String getFailureMessage() {
				return "Cannot get comboBox index" + comboBoxIndex;
			}
		}, timeout);
		return bot.comboBox(comboBoxIndex);

	}

	protected static void selectFromWorkspace(int index, String nodeToExpand, String fileNameToChoose) {
		bot.button(com.gigaspaces.azure.wizards.Messages.remAccWkspcBtn, index).click();
		SWTBotTreeItem item = bot.tree().expandNode(Messages.azureProjName).expandNode(nodeToExpand);
		item.select(fileNameToChoose).click();
		bot.button(Messages.selectFromWorkspaceConfirmBtn).click();
	}

	protected static void clickButtonWhenAvailable(final String buttonLable, long timeout) {
		bot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() throws Exception {

				try{
					bot.button(buttonLable).click();
					return true;
				}
				catch (Exception e) {}
				return false;
			}

			@Override
			public String getFailureMessage() {
				return "Cannot click on the " + buttonLable + " button";
			}
		}, timeout);
	}

	protected static void fillRemoteDesktopSettingPage(boolean isPublicKeyIllegal, boolean isPrivateKeyIllegal, boolean isPrivateKeyPasswordIllegal) {
		bot.checkBox().click();
		bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.remAccUserName).setText(Messages.remDesktopUserName);
		bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.remAccPassword).setText(Messages.remDesktopPassword);
		bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.remAccConfirmPwd).setText(Messages.remDesktopPassword);
		
		bot.button(Messages.remDesktopCalendarBtn).click();
		Calendar tomorrow = Calendar.getInstance(); 
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		bot.dateTime().setDate(tomorrow.getTime());
		bot.button(Messages.remDesktopCalendarOkBtn).click();

		if(isPublicKeyIllegal) {
			String projectRootDirPath = getResourcesFolder();
			bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.rdpPublicKey).setText(projectRootDirPath + Messages.remDesktopPublicKeyIllegalPath);			
		}
		else {
			selectFromWorkspace(Integer.valueOf(Messages.remDesktopPublicKeyWorkspaceIndex), Messages.remDesktopPublicKeyProjDirName, Messages.remDesktopPublicKeyPath);			
		}
		if (isPrivateKeyIllegal) {
			String projectRootDirPath = getResourcesFolder();
			bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.rdpPrivateKey).setText(projectRootDirPath + Messages.remDesktopPrivateKeyIllegalPath);
		}
		else {
			selectFromWorkspace(Integer.valueOf(Messages.remDesktopPrivateKeyWorkspaceIndex), Messages.remDesktopPrivateKeyProjDirName, Messages.remDesktopPrivateKeyPath);			
		}
		// select the private key
		//enter private key password
		if(isPrivateKeyPasswordIllegal)
			bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.rdpPrivateKeyPassword).setText(Messages.remDesktopPrivateKeyIllegalPassword);
		else
			bot.textWithLabel(com.gigaspaces.azure.wizards.Messages.rdpPrivateKeyPassword).setText(Messages.remDesktopPrivateKeyPassword);

	}
	
	protected static void waitForProgressToFinishFinish() {
		bot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() throws Exception 
			{
				String status = bot.table().cell(Integer.valueOf(Messages.winAzureActivityLogStatusRow),Integer.valueOf(Messages.winAzureActivityLogStatusCol));
				return Messages.completeMessage.equals(status) || Messages.failureMessage.equals(status);
			}

			@Override
			public String getFailureMessage() 
			{
				return "Status is not legal";
			}
		}, Long.valueOf(Messages.deployTimeout));		
	}

	protected static void validateStatus(Status status) {

		// wait until deploy is finish
		bot.viewByTitle(Messages.waActivityLogName);
		bot.table().setFocus();
		waitForProgressToFinishFinish();

		// check the status
		String deployStatus = bot.table().cell(Integer.valueOf(Messages.winAzureActivityLogStatusRow),Integer.valueOf(Messages.winAzureActivityLogStatusCol));
		String expectedStatus = status == Status.COMPLETED ? Messages.completeMessage : Messages.failureMessage;
		assertEquals(expectedStatus, deployStatus);	
	}

	public static void deleteVersionSchemaFromServiceDefinition() {
		SWTBotTree treeItem = Utility.getProjExplorer().bot().tree().select(Messages.azureProjName);

		SWTBotTreeItem item = treeItem.expandNode(Messages.azureProjName).expandNode("ServiceDefinition.csdef");
		item.select();
		item.doubleClick();
		String text = bot.activeEditor().toTextEditor().getText().replace(" schemaVersion=\"2012-05.1.7\"","");
		bot.activeEditor().toTextEditor().setText(text);
		bot.activeEditor().toTextEditor().saveAndClose();
	}

	private static void addApplicationToProject() {
		addWar();
		addJDK();
		addTomcat();
		changeProjectSettings();		
	}
	
	public static String getResourcesFolder() {
		return USER_TEMP_FOLDER_PATH;
	}

	private static void addWar() {
		// TODO Auto-generated method stub
		
	}

	private static void addJDK() {
		// TODO Auto-generated method stub
		
	}

	private static void addTomcat() {
		// TODO Auto-generated method stub
		
	}

	private static void changeProjectSettings() {
		// TODO Auto-generated method stub
		
	}

	public static void validateApplicationPublishSucceeded() {
		// TODO Auto-generated method stub
		
	}
}


