package com.gigaspaces.uiautomation;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class WANewCertificateTest {

	private String dir = null;
	private File privateKey = null;
	private File publicKey = null;
	
	@Before
	public void setUp() throws Exception {
		Utility.bot.closeAllShells();	
		Utility.openWizard(Utility.Operation.PUBLISH);
		dir = Utility.fillNewCertificateDetails();
		Utility.setPfxSettings(dir + Messages.generatedPfxFileName, Messages.pfxSubscriptionId, Messages.remDesktopPassword);
		privateKey = new File(dir + File.separator + Messages.generatedPfxFileName);
		publicKey = new File(dir + File.separator + Messages.generatedCertFileName);
	}
	
	@Test
	public void testNewCertificateGeneration() {
		assertTrue(privateKey.exists());
		assertTrue(publicKey.exists());
	}
	
	@Test
	public void testErrorMessageWhenTryingToImportANewPfx() {
		assertTrue(Utility.isImportErrorWindowDisplayed());	
	}
}
