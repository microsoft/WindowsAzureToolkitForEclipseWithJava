package com.gigaspaces.uiautomation;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class WAIllegalRDPAuthenticationTest {
		
	@Before
	public void setUp() throws Exception {
		Utility.bot.closeAllShells();	
		Utility.openWizard(Utility.Operation.PUBLISH);
	}
	
	@Test(expected = Exception.class)
	public void testIllegalRemoteDesktopPfxPassword() {

		Utility.fillPublishAzureProjectPage(Utility.SubscriptionFileType.PUBLISH_SETTINGS);

		Utility.bot.button(Messages.nextBtn).click();

		Utility.fillRemoteDesktopSettingPage(false, false, true);

		Utility.clickButtonWhenAvailable(com.gigaspaces.azure.handler.Messages.publish, Utility.endOfTestsTimeOut);
	}
	
	@AfterClass
	public static void sleep() {
		Utility.bot.sleep(Utility.endOfTestsTimeOut);
	}

}
