package com.gigaspaces.uiautomation;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class WAIllegalManagementAuthenticationTest {
	
	@Before
	public void setUp() throws Exception {
		Utility.bot.closeAllShells();	
		Utility.openWizard(Utility.Operation.PUBLISH);
	}
	
	@Test(expected = Exception.class)
	public void testIllegalPfxSubscriptionId() {
		String projectRootDirPath = Utility.getResourcesFolder();
		Utility.setPfxSettings(projectRootDirPath + Messages.pfxPath, Messages.pfxIllegalSubscriptionId,Messages.pfxSubscriptionPassword);
	}

	@Test(expected = Exception.class)
	public void testIllegalPfxPassword() {
		String projectRootDirPath = Utility.getResourcesFolder();
		Utility.setPfxSettings(projectRootDirPath + Messages.pfxPath, Messages.pfxSubscriptionId,Messages.pfxIllegalSubscriptionPassword);
	}
}
