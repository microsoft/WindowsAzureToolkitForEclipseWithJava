/**
 * Copyright (c) Microsoft Corporation
 * 
 * All rights reserved. 
 * 
 * MIT License
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.persistent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileDeleteStrategy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.IStartup;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalog;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalogEntry;
import org.eclipse.wst.xml.core.internal.catalog.provisional.INextCatalog;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.microsoftopentechnologies.azurecommons.startup.WAStartUpUtilMethods;
import com.microsoftopentechnologies.azurecommons.util.WAEclipseHelperMethods;
import com.microsoftopentechnologies.wacommon.storageregistry.PreferenceUtilStrg;
import com.microsoftopentechnologies.wacommon.utils.PluginUtil;

/**
 * This class gets executed after the Workbench initialises.
 */
@SuppressWarnings("restriction")
public class WAStartUp implements IStartup {
    private static final int BUFF_SIZE = 1024;
    private static final String COMPONENTSETS_TYPE = "COMPONENTSETS";
    private static final String PREFERENCESETS_TYPE = "PREFERENCESETS";
    protected static File cmpntFile = new File(WAEclipseHelper.getTemplateFile(Messages.cmpntFileName));
    @Override
    public void earlyStartup() {
        try {
            //Make schema entries in XML Catalog
            makeXMLCatalogEntry();

            //Add resourceChangeListener to listen to changes of resources
            WAResourceChangeListener listener =
                    new WAResourceChangeListener();
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            workspace.addResourceChangeListener(listener,
                    IResourceChangeEvent.POST_CHANGE);

            IWorkspaceRoot root = workspace.getRoot();
            PreferenceUtilStrg.load();
            //Get all the projects in workspace
            IProject[] projects = root.getProjects();
            for (IProject iProject : projects) {
                Activator.getDefault().log(iProject.getName()
                        + "isOpen" + iProject.isOpen(), null);
                if (iProject.isOpen()) {
                    boolean isNature = iProject.hasNature(
                            Messages.stUpProjNature);
                    Activator.getDefault().log(iProject.getName()
                            + "isNature" + isNature, null);
                    if (isNature) {
                        WindowsAzureProjectManager projMngr =
                                WindowsAzureProjectManager.load(
                                        iProject.getLocation().toFile());
                        if (!projMngr.isCurrVersion()) {
                        	WAEclipseHelper.handleProjectUpgrade(iProject,projMngr);
                        }
                        // Correct name if its invalid
                        if (!iProject.getName().
                        		equalsIgnoreCase(projMngr.getProjectName())) {
                        	WAEclipseHelper.
                        	correctProjectName(iProject, projMngr);
                        }
                        projMngr = initializeStorageAccountRegistry(projMngr);
                        projMngr = changeLocalToAuto(projMngr, iProject.getName());
                        // save object so that access key will get saved in PML.
                        projMngr.save();
                    }
                }
            }
            // save preference file.
            PreferenceUtilStrg.save();
            //this code is for copying componentset.xml in plugins folder
            copyPluginComponents();
            // refresh workspace as package.xml may have got changed.
            WAEclipseHelper.refreshWorkspace(Messages.resCLJobName,
            		Messages.resCLExWkspRfrsh);
            // delete %proj% directory from temporary folder during eclipse start
            String tmpPath = System.getProperty("java.io.tmpdir");
            String projPath = String.format("%s%s%s", tmpPath, File.separator, "%proj%");
            File projFile = new File(projPath);
            if (projFile != null) {
            	FileDeleteStrategy.FORCE.delete(projFile);
            }
        } catch (Exception e) {
            /* This is not a user initiated task
               So user should not get any exception prompt.*/
            Activator.getDefault().log(Messages.expErlStrtUp, e);
        }
    }

    /**
     * Storage account registry project open logic.
     * Plugin needs to detect and aggregate the information
     * about the different storage accounts used by the components.
     * If account is not there,
     * then add the storage account, with the key to the registry.
     * If it's there,
     * but the access key is different, then update component's cloud key
     * with the key from the registry.
     * @param projMngr
     * @return
     */
    public static WindowsAzureProjectManager initializeStorageAccountRegistry(
    		WindowsAzureProjectManager projMngr) {
    	try {
    		projMngr = WAStartUpUtilMethods.initializeStorageAccountRegistry(projMngr,
    				WAEclipseHelper.getTemplateFile(Messages.prefFileName));
    	} catch (Exception e) {
    		Activator.getDefault().log(Messages.expStrgReg, e);
    	}
    	return projMngr;
    }

    /**
     * Change include in package deployment to
     * auto upload with auto storage selected.
     * Need these changes while importing old project
     * in new plugin.
     * @param projMngr
     * @return
     */
    public static WindowsAzureProjectManager changeLocalToAuto(
    		WindowsAzureProjectManager projMngr, String projName) {
    	try {
    		projMngr = WAStartUpUtilMethods.changeLocalToAuto(projMngr, projName, cmpntFile);
    	} catch (Exception e) {
    		Activator.getDefault().log(Messages.expLocToAuto, e);
    	}
    	return projMngr;
    }

    /**
     * Creates schema entries in XML Catalog.
     *
     */
    private void makeXMLCatalogEntry() {
        ICatalog catalog = XMLCorePlugin.getDefault().getDefaultXMLCatalog();
        INextCatalog[] nextCatalogs = catalog.getNextCatalogs();
        for (int i = 0; i < nextCatalogs.length; i++) {
            INextCatalog nextCatalog = nextCatalogs[i];
            if (XMLCorePlugin.SYSTEM_CATALOG_ID.equals(nextCatalog.getId())) {
                ICatalog userCatalog = nextCatalog.getReferencedCatalog();
                if (userCatalog != null) {
                    createEntries(userCatalog);
                }
            }
        }
    }

    /**
     * Creates schema entries if not present.
     *
     * @param userCatalog catalog to which entry is to be made
     */
    private void createEntries(final ICatalog userCatalog) {
        String str = getSchemasLocation();

        String defFile = str + File.separator
                + Messages.stUpSerDefSchema;
        File file = new File(defFile);
        URI defFileUri = file.toURI();
        String configFile = str + File.separator
                + Messages.stUpSerConfSchma;
        file = new File(configFile);
        URI configFileUri = file.toURI();
        boolean isServiceDef = false;
        boolean isServiceConfig = false;
        ICatalogEntry[] entries = userCatalog.getCatalogEntries();
        for (ICatalogEntry iCatalogEntry : entries) {
            if (iCatalogEntry.getURI().equals(
                    defFileUri.toString())) {
                isServiceDef = true;
            }
            if (iCatalogEntry.getURI().equals(
                    configFileUri.toString())) {
                isServiceConfig = true;
            }
        }
        if (!isServiceDef) {
            ICatalogEntry catalogEntry =
                    (ICatalogEntry) userCatalog.createCatalogElement(
                            ICatalogEntry.ENTRY_TYPE_URI);
            catalogEntry.setKey(
                    Messages.stUpSerDefKey);
            catalogEntry.setURI(defFileUri.toString());
            userCatalog.addCatalogElement(catalogEntry);
        }
        if (!isServiceConfig) {
            ICatalogEntry catalogEntry =
                    (ICatalogEntry) userCatalog.createCatalogElement(
                            ICatalogEntry.ENTRY_TYPE_URI);
            catalogEntry.setKey(
                    Messages.stUpSerConfigKey);
            catalogEntry.setURI(configFileUri.toString());
            userCatalog.addCatalogElement(catalogEntry);
        }
    }

    /**
     * Gives location of schemas directory of Azure SDK.
     *
     * @return location of schemas directory
     */
    private String getSchemasLocation() {
        String progFile = "";
        try {
        progFile = WindowsAzureProjectManager.getLatestAzureSdkDir();
        progFile = String.format("%s%s%s", progFile, File.separator,
        		Messages.stUpSchemaLoc);
        } catch (IOException e) {
            Activator.getDefault().log(e.getMessage(), e);
        }
        return progFile;
    }

    /**
     * Copies all Eclipse plugin for Azure
     * related files in eclipse plugins folder at startup.
     */
    private void copyPluginComponents() {
        try {
        	String pluginInstLoc = String.format("%s%s%s",
        			PluginUtil.pluginFolder,
        			File.separator, Messages.pluginId);
            if (!new File(pluginInstLoc).exists()) {
                new File(pluginInstLoc).mkdir();
            }
            String cmpntFile = String.format("%s%s%s", pluginInstLoc,
            		File.separator, Messages.cmpntFileName);
            String starterKit = String.format("%s%s%s", pluginInstLoc,
            		File.separator, Messages.starterKitFileName);
            String prefFile = String.format("%s%s%s", pluginInstLoc,
            		File.separator, Messages.prefFileName);

            // upgrade component sets and preference sets
            upgradePluginComponent(cmpntFile,
            		Messages.cmpntFileEntry,
            		Messages.oldCmpntFileEntry,
            		COMPONENTSETS_TYPE);
            upgradePluginComponent(prefFile,
            		Messages.prefFileEntry,
            		Messages.oldPrefFileEntry,
            		PREFERENCESETS_TYPE);

            // Check for WAStarterKitForJava.zip
            if (new File(starterKit).exists()) {
            	new File(starterKit).delete();
            }
            copyResourceFile(Messages.starterKitEntry, starterKit);
        } catch (Exception e) {
            Activator.getDefault().log(e.getMessage(), e);
        }
    }

    /**
     * Checks for pluginComponent file.
     * If exists checks its version.
     * If it has latest version then no upgrade action is needed,
     * else checks with older componentsets.xml,
     * if identical then deletes existing and copies new one
     * else renames existing and copies new one.
     * @param pluginComponent
     * @param resourceFile
     * @param componentType
     * @throws Exception
     */
    private void upgradePluginComponent(String pluginComponentPath, String resource, 
    		String oldResource, String componentType) throws Exception {
        File pluginComponentFile = new File(pluginComponentPath);
        if (pluginComponentFile.exists()) {
        	String pluginComponentVersion = null;
        	String resourceFileVersion = null;
        	File resourceFile = WAEclipseHelper.getResourceAsFile(resource);
        	try {
        		if (COMPONENTSETS_TYPE.equals(componentType)) {
        			pluginComponentVersion = WindowsAzureProjectManager.
        					getComponentSetsVersion(pluginComponentFile);
        			resourceFileVersion = WindowsAzureProjectManager.
        					getComponentSetsVersion(resourceFile);
        		} else {
        			pluginComponentVersion = WindowsAzureProjectManager.
        					getPreferenceSetsVersion(pluginComponentFile);
        			resourceFileVersion = WindowsAzureProjectManager.
        					getPreferenceSetsVersion(resourceFile);
        		}
        	} catch(Exception e ) {
        		Activator.getDefault().log(
        				"Error occurred while getting version of plugin component "
        	+ componentType
        	+ ", considering version as null");
        	}
        	
        	
        	
        	if ((pluginComponentVersion != null
        			&& !pluginComponentVersion.isEmpty())
        			&& pluginComponentVersion.equals(resourceFileVersion)) {
        		// Do not do anything
        	} else {
        		// Check with old plugin component for upgrade scenarios
        		File oldPluginComponentFile = WAEclipseHelper.
        				getResourceAsFile(oldResource);
        		boolean isIdenticalWithOld = WAEclipseHelperMethods.
        				isFilesIdentical(
        						oldPluginComponentFile, pluginComponentFile);
        		if (isIdenticalWithOld) {
        			// Delete old one
        			pluginComponentFile.delete();
        		} else {
        			// Rename old one
        			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        			Date date = new Date();
        			WAEclipseHelperMethods.copyFile(pluginComponentPath,
        					pluginComponentPath
        					+ ".old"
        					+ dateFormat.format(date));
        		}
        		copyResourceFile(resource, pluginComponentPath);
        	}
        } else {
        	copyResourceFile(resource, pluginComponentPath);
        }
    }

    /**
     * Method copy specified file in eclipse plugins folder.
     * @param resourceFile
     * @param destFile
     */
    private void copyResourceFile(String resourceFile, String destFile) {
    	URL url = Activator.getDefault().getBundle()
                .getEntry(resourceFile);
        URL fileURL;
		try {
			fileURL = FileLocator.toFileURL(url);
			URL resolve = FileLocator.resolve(fileURL);
	        File file = new File(resolve.getFile());
	        FileInputStream fis = new FileInputStream(file);
	        File outputFile = new File(destFile);
	        FileOutputStream fos = new FileOutputStream(outputFile);
	        writeFile(fis , fos);
		} catch (IOException e) {
			Activator.getDefault().log(e.getMessage(), e);
		}
    }

    /**
     * Method writes contents of file.
     * @param inStream
     * @param outStream
     * @throws IOException
     */
    public void writeFile(InputStream inStream, OutputStream outStream)
    		throws IOException {

        try {
            byte[] buf = new byte[BUFF_SIZE];
            int len = inStream.read(buf);
            while (len > 0) {
                outStream.write(buf, 0, len);
                len = inStream.read(buf);
            }
        } finally {
            if (inStream != null) {
                inStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
        }
    }
}
