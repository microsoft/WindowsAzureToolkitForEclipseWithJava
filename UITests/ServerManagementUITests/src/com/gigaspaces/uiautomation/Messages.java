package com.gigaspaces.uiautomation;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.gigaspaces.uiautomation.messages"; //$NON-NLS-1$	
		
	public static String welcomeViewTitle;
	
	public static String FileMenuName;
	public static String FileNewMenuName;
	public static String projectNameText;
	public static String azureProjName;
	public static String waNewProjectWizardName;
	public static String newAzureProjFinishBtn;
	
	public static String waManuName;
	public static String waPublishWizardName;
	public static String waUnpublishWizardName;
	
	public static String waBuildConfirmBtn;
	public static String buildTimeout;
	
	public static String publishSettingsFilePath;
	public static String pfxPath;
	public static String pfxSubscriptionId;
	public static String pfxIllegalSubscriptionId;
	public static String pfxSubscriptionPassword;
	public static String pfxIllegalSubscriptionPassword;
	public static String subscriptionImportConfirmBtn;
	
	public static String subscriptionLoadingTimeout;
	public static String hostedServiceComboBoxIndex;
	public static String serviceName;
	public static String deploymentName;
	public static String newHostedServiceBtnIndex;
	public static String newHostedServiceLocationText;

	public static String selectFromWorkspaceConfirmBtn;
			
	public static String cspkgFileName;
	public static String cspkgWorkspaceIndex;
	public static String cscfgFileName;
	public static String cscfgWorkspaceIndex;

	public static String nextBtn;
	
	public static String remDesktopUserName;
	public static String remDesktopPassword;
	public static String remDesktopCalendarBtn;
	public static String remDesktopCalendarOkBtn;
	
	public static String remDesktopPublicKeyWorkspaceIndex;
	public static String remDesktopPublicKeyProjDirName;
	public static String remDesktopPublicKeyPath;
	public static String remDesktopPublicKeyIllegalPath;
	public static String remDesktopPrivateKeyWorkspaceIndex;
	public static String remDesktopPrivateKeyProjDirName;
	public static String remDesktopPrivateKeyPath;
	public static String remDesktopPrivateKeyIllegalPath;

	public static String remDesktopPrivateKeyPassword;
	public static String remDesktopPrivateKeyIllegalPassword;

	public static String waActivityLogName;
	public static String winAzureActivityLogStatusRow;
	public static String winAzureActivityLogStatusCol;
	public static String deployTimeout;
	public static String undeployTimeout;
	public static String completeMessage;
	public static String failureMessage;
	
	public static String generatedPfxFileName;
	public static String generatedCertFileName;
	
	public static String schemaVersionFileName;
	public static String schemaVersionTextToDelete;
	
	public static String unpublishHostedServiceComboBoxIndex;
	public static String unpublishDeploymentsComboBoxIndex;
	public static String hostedServiceLoadingTimeout;

	public static String restCallTimeout;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		super();
	}
}
