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
