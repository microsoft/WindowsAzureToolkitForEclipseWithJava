/**
 * Copyright 2011 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalog;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalogEntry;
import org.eclipse.wst.xml.core.internal.catalog.provisional.INextCatalog;

import waeclipseplugin.Activator;

import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;



/**
 * This class gets executed after the Workbench initialises.
 */
@SuppressWarnings("restriction")
public class WAStartUp implements IStartup {
    private static final int BUFF_SIZE = 1024;
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
                            iProject.close(null);
                        }
                        // Correct name if its invalid
                        if (!iProject.getName().
                        		equalsIgnoreCase(projMngr.getProjectName())) {
                        	WAEclipseHelper.
                        	correctProjectName(iProject, projMngr);
                        }

                    }
                }
            }
            //this code is for copying componentset.xml in plugins folder
            copyPluginComponents();
        } catch (Exception e) {
            /* This is not a user initiated task
               So user should not get any exception prompt.*/
            Activator.getDefault().log("Exception in early startup", e);
        }
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
     * Copies all Eclipse plugin for Windows Azure
     * related files in eclipse plugins folder at startup.
     */
    private void copyPluginComponents() {
        try {
            String pluginInstLoc = String.format("%s%s%s%s%s",
            		Platform.getInstallLocation().getURL().getPath().toString(),
                    File.separator, Messages.pluginFolder,
                    File.separator, Messages.pluginId);
            if (!new File(pluginInstLoc).exists()) {
                new File(pluginInstLoc).mkdir();
            }
            String cmpntFile = String.format("%s%s%s", pluginInstLoc,
            		File.separator, Messages.cmpntFileName);
            String enctFile = String.format("%s%s%s", pluginInstLoc,
            		File.separator, Messages.encFileName);
            String starterKit = String.format("%s%s%s", pluginInstLoc,
            		File.separator, Messages.starterKitFileName);
            String restFile = String.format("%s%s%s", pluginInstLoc,
            		File.separator, Messages.restFileName);

            /*
             *  Check for componentsets.xml.
             *  If exists compare if it is same like V1.7.2 componentsets.xml
             *  if same copy V1.7.3 componentsets.xml,
             *  else just ignore.
             */
            if (new File(cmpntFile).exists()) {
            	File oldCmpntFile   = WAEclipseHelper.
            			getResourceAsFile(Messages.oldCmpntFileEntry);
            	boolean isIdentical = WAEclipseHelper.
            			isFilesIdentical(oldCmpntFile,
            					new File(cmpntFile));
            	if (isIdentical) {
            		/*
            		 * If identical then only copy new componentsets.xml
            		 * else not needed.
            		 */
            		new File(cmpntFile).delete();
					copyResourceFile(Messages.cmpntFileEntry,
							cmpntFile);
            	}
            } else {
            	copyResourceFile(Messages.cmpntFileEntry, cmpntFile);
            }

            // Check for encutil.exe
            if (new File(enctFile).exists()) {
            	new File(enctFile).delete();
            }
            copyResourceFile(Messages.encFileEntry, enctFile);

            // Check for WAStarterKitForJava.zip
            if (new File(starterKit).exists()) {
            	new File(starterKit).delete();
            }
            copyResourceFile(Messages.starterKitEntry, starterKit);

            // Check for restutil.exe
            if (new File(restFile).exists()) {
            	new File(restFile).delete();
            }
            copyResourceFile(Messages.restFileEntry, restFile);

        } catch (Exception e) {
            Activator.getDefault().log(e.getMessage(), e);
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
