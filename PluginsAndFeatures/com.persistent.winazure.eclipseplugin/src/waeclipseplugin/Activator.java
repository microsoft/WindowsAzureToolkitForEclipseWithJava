/**
* Copyright 2011 Persistent Systems Ltd.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

package waeclipseplugin;

import java.io.IOException;
import java.net.URL;

import javax.swing.event.EventListenerList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import com.gigaspaces.azure.deploy.DeploymentEventArgs;
import com.gigaspaces.azure.deploy.DeploymentEventListener;
import com.gigaspaces.azure.deploy.UploadProgressEventArgs;
import com.gigaspaces.azure.deploy.UploadProgressEventListener;
import com.interopbridges.tools.windowsazure.WindowsAzureProjectManager;
import com.interopbridges.tools.windowsazure.WindowsAzureRole;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "WAEclipsePlugin"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private WindowsAzureProjectManager waProjMgr;
    private WindowsAzureRole waRole;
    private boolean isEdit;
    private boolean isSaved;
    private boolean isContextMenu = false;
    
    public static final String SETTINGS_FILE_NAME = Messages.settingsFileName;
	
	public static final String CONSOLE_NAME = Messages.consoleName;

	public static final String DEPLOY_IMAGE = Messages.deploytoAzureImg;

	private static final EventListenerList DEPLOYMENT_EVENT_LISTENERS = new EventListenerList();
	
	private static final EventListenerList UPLOAD_PROGRESS_EVENT_LISTENERS = new EventListenerList();


    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    public void log(String message, Exception excp) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, excp));
    }
    
    public void log(String message) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
	}


    public WindowsAzureProjectManager getWaProjMgr() {
        return waProjMgr;
    }
    
    public void log(String message, Throwable excp) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, excp));
	}


    public void setWaProjMgr(WindowsAzureProjectManager waProjMgr) {
        if (waProjMgr == null) {
            throw new IllegalArgumentException();
        }
        getDefault().waProjMgr = waProjMgr;
    }

    public WindowsAzureRole getWaRole() {
        return waRole;
    }

    public void setWaRole(WindowsAzureRole waRole) {
        if (waRole == null) {
            throw new IllegalArgumentException();
        }
        getDefault().waRole = waRole;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean isEdit) {
        getDefault().isEdit = isEdit;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean var) {
        getDefault().isSaved = var;
    }

    public boolean isContextMenu() {
        return isContextMenu;
    }

    public void setContextMenu(boolean isContextMenu) {
        this.isContextMenu = isContextMenu;
    }
    
    public static ImageDescriptor getImageDescriptor(String location)
			throws IOException {
		URL url = Activator.getDefault().getBundle().getEntry(location);
		URL fileURL = FileLocator.toFileURL(url);
		URL resolve = FileLocator.resolve(fileURL);
		return ImageDescriptor.createFromURL(resolve);
	}

	public static MessageConsole findConsole(String name) {
		ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = consolePlugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		}
		// no console found, so create a new one
		MessageConsole messageConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { messageConsole });
		return messageConsole;
	}
	
	public void addDeploymentEventListener(DeploymentEventListener listener) {
		DEPLOYMENT_EVENT_LISTENERS.add(DeploymentEventListener.class, listener);
	}

	public void removeDeploymentEventListener(DeploymentEventListener listener) {
		DEPLOYMENT_EVENT_LISTENERS.remove(DeploymentEventListener.class, listener);
	}
	
	public void addUploadProgressEventListener(UploadProgressEventListener listener) {
		UPLOAD_PROGRESS_EVENT_LISTENERS.add(UploadProgressEventListener.class, listener);
	}
	
	public void removeUploadProgressEventListener(UploadProgressEventListener listener) {
		UPLOAD_PROGRESS_EVENT_LISTENERS.remove(UploadProgressEventListener.class, listener);		
	}

	public void fireDeploymentEvent(DeploymentEventArgs args) {
		Object[] list = DEPLOYMENT_EVENT_LISTENERS.getListenerList();

		for (int i = 0; i < list.length; i += 2) {
			if (list[i] == DeploymentEventListener.class) {
				((DeploymentEventListener) list[i + 1]).onDeploymentStep(args);
			}
		}
	}

	public void fireUploadProgressEvent(UploadProgressEventArgs args) {
		Object[] list = UPLOAD_PROGRESS_EVENT_LISTENERS.getListenerList();

		for (int i = 0; i < list.length; i += 2) {
			if (list[i] == UploadProgressEventListener.class) {
				((UploadProgressEventListener) list[i + 1]).onUploadProgress(args);
			}
		}
	}



}
