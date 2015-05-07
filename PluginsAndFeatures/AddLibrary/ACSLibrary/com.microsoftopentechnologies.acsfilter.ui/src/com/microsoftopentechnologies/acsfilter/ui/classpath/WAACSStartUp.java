/**
 * Copyright 2015 Microsoft Open Technologies, Inc.
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

import org.eclipse.core.resources.IProject;
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
        
    }

    /**
     * Method removes entries from web.xml.
     * @param proj
     */
    private void removeACSEntries(IProject proj) {
    	try {
    		String xmlPath = Messages.xmlPath;
    		if (proj.hasNature(Messages.natMaven)) {
    			xmlPath = Messages.webxmlPathMaven;
    		}
    		if (proj.getFile(xmlPath).exists()) {
    			ACSFilterHandler handler =
    					new ACSFilterHandler(proj.
    							getFile(xmlPath).getLocation().toOSString());
    			handler.remove();
    			handler.save();
    		}

    		new ClasspathContainerPage().removeEmbedCert(proj);
    	} catch (Exception e) {
    		Activator.getDefault().log(e.getMessage(), e);
    	}
    }
}
