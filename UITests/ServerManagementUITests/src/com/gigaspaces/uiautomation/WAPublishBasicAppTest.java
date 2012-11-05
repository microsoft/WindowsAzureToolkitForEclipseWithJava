package com.gigaspaces.uiautomation;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class WAPublishBasicAppTest {
	
	@Before
	public void setUp() throws Exception {
		Utility.bot.closeAllShells();
	}
	
	@Test
	public void testWindowsAzureDeployWithPfxAndRemoteDesktop() throws Exception {	
		Utility.publish(Utility.SubscriptionFileType.PFX);
		Utility.bot.sleep(Utility.publishTimeOut);
		Utility.bot.viewByTitle(Messages.waActivityLogName).close();
		Utility.unPublish();	
	}
	
	@Test
	public void testWindowsAzureDeployWithPublishSettingsAndRemoteDesktop() throws Exception {
		Utility.publish(Utility.SubscriptionFileType.PUBLISH_SETTINGS);
		Utility.bot.sleep(Utility.publishTimeOut);
		Utility.bot.viewByTitle(Messages.waActivityLogName).close();
		Utility.unPublish();		
	}
	
	@After
	public void closeView() {
		Utility.bot.sleep(Utility.unpublishTimeOut);
		Utility.bot.viewByTitle(Messages.waActivityLogName).close();
	}
}
