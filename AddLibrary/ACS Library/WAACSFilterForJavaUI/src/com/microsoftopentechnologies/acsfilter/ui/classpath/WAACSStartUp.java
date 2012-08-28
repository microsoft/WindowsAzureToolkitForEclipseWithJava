/**
 * Copyright 2012 Persistent Systems Ltd.
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
package com.microsoftopentechnologies.acsfilter.ui.classpath;

import java.io.File;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IStartup;
import com.microsoftopentechnologies.acsfilter.ui.activator.Activator;

/**
 * This class gets executed after the Workbench initializes.
 */
public class WAACSStartUp implements IStartup {
    @Override
    public void earlyStartup() {
        IElementChangedListener listener = new IElementChangedListener()
        {
        	public void elementChanged( final ElementChangedEvent event ) {
        		if (event.getSource().toString().contains(Messages.sdkJar)
        				&& event.getDelta().getAffectedChildren()[0].
        				getAffectedChildren()[0].getFlags()
        				== IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) {
        			IProject proj = event.getDelta().getAffectedChildren()[0].
        					getResourceDeltas()[0].getResource().getProject();
        			removeACSEntries(proj);        			
        		}
        	}
        };
        JavaCore.addElementChangedListener(listener,
        		ElementChangedEvent.POST_CHANGE);
        
        //this code is for copying encutil.exe in plugins folder
        copyPluginComponents();
    }

    private void copyPluginComponents() {
        try {
            String pluginInstLoc = String.format("%s%s%s%s%s",
            		Platform.getInstallLocation().getURL().getPath().toString(),
                    File.separator, Messages.pluginFolder,
                    File.separator, Messages.pluginId);
            if (!new File(pluginInstLoc).exists()) {
                new File(pluginInstLoc).mkdir();
            }
            String enctFile = String.format("%s%s%s", pluginInstLoc,
            		File.separator, Messages.encFileName);

            // Check for encutil.exe
            if (new File(enctFile).exists()) {
            	new File(enctFile).delete();
            }
            ACSFilterUtil.copyResourceFile(Messages.encFileEntry,enctFile);
        } catch (Exception e) {
            Activator.getDefault().log(e.getMessage(), e);
        }
		
	}

	/**
     * Method removes entries from web.xml.
     * @param proj
     */
    private void removeACSEntries(IProject proj) {
        if (proj.getFile(Messages.xmlPath).exists()) {
            try {
            	ACSFilterHandler handler =
            			new ACSFilterHandler(proj.
            					getFile(Messages.xmlPath).getLocation().toOSString());
                handler.remove();
                handler.save();
            } catch (Exception e) {
                Activator.getDefault().log(e.getMessage(), e);
            }
        }
       
        new ClasspathContainerPage().removeEmbedCert(proj);

    }
}
