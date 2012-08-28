package com.gigaspaces.uiautomation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({WANewCertificateTest.class,
	WAIllegalManagementAuthenticationTest.class,
	WAIllegalRDPAuthenticationTest.class,
	WAPublishBasicAppTest.class
    })
public class AllTestsRunner {
		
	@BeforeClass
	public static void copyTestResourcesToTempDir() throws IOException {
		
		copyResourceToTempDir(Messages.publishSettingsFilePath);
		copyResourceToTempDir(Messages.pfxPath);
		copyResourceToTempDir(Messages.remDesktopPublicKeyIllegalPath);
		copyResourceToTempDir(Messages.remDesktopPrivateKeyIllegalPath);
		
	}
	
	private static void copyResourceToTempDir(String resourceFileName) throws IOException {
		
		String userTemp = Utility.USER_TEMP_FOLDER_PATH;
		
		File resource = new File(userTemp  + resourceFileName);
		if (!resource.exists()) {
			new File(userTemp).mkdirs();
			resource.createNewFile();
		}
		
		InputStream input = AllTestsRunner.class.getResourceAsStream("/testResources/" + resourceFileName);
		
		FileOutputStream output = new FileOutputStream(resource);

		try {
			int b = 0;

			while ((b = input.read()) > -1) {
				output.write(b);
			}

		} finally {
			if (input != null)
				input.close();
			if (output != null)
				output.close();
		}

		
	}
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		System.setProperty("com.gigaspaces.azure.isTest", "true");
		Utility.bot = new SWTWorkbenchBot();
		
		Utility.bot.viewByTitle(Messages.welcomeViewTitle).close();
		Utility.bot.closeAllShells();
		
		Utility.createNewWindowsAzureBasicProject();
		Utility.deleteVersionSchemaFromServiceDefinition();

	}
	
	@AfterClass
	public static void sleep() {
		
		File resources = new File(Utility.getResourcesFolder());
		if (resources.exists() && resources.isDirectory()) {
			for (File f : resources.listFiles()) {
				System.out.println("deleting " + f.getAbsolutePath());
				f.delete();
			}
			resources.delete();
		}
	}
}
