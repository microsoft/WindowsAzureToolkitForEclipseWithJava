/**
 * Copyright 2012 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoftopentechnologies.waqpidjava.ui.classpath;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

import com.microsoftopentechnologies.waqpidjava.ui.Activator;

/**
 * Classpath container implementation.
 */
public class ClasspathContainer implements IClasspathContainer {

	private IPath containerPath;

	/**
	 * Constructor.
	 * @param path
	 */
	public ClasspathContainer(IPath path) {
		super();
		this.containerPath = path;
	}

	/**
	 * Returns the classpath entries.
	 */
	@Override
	public IClasspathEntry[] getClasspathEntries() {
		Bundle bundle = Platform.getBundle(Messages.qpidID);
        //Search the available Qpid JAR's
        Bundle[] bundles = Platform.getBundles(Messages.qpidID,
                null);
        List<IClasspathEntry> listEntries = new ArrayList<IClasspathEntry>();
        if (bundles != null) {
            for (Bundle bundle2 : bundles) {
                if (bundle2.getVersion().toString().startsWith(
                        containerPath.segment(1))) {
                    bundle = bundle2;
                    break;
                }
            }

          //Get the Qpid jar.
            URL qpidJar = FileLocator.find(bundle,
                    new Path(Messages.qpidClientJar), null);
            URL resQpidJar = null;
            IClasspathAttribute[] attr = null;
            try {
            	if (qpidJar != null) {
            		resQpidJar = FileLocator.resolve(qpidJar);
            		//create classpath attribute for java doc, if present
            	}
            	if (resQpidJar == null) {
            		/* If Qpid client jar is not present
            		 * then create an place holder for Qpid client jar
            		 * so that it would not be shown as missing file.
            		 */
            		URL bundleLoc = new URL(bundle.getLocation());
            		StringBuffer strBfr =
            				new StringBuffer(bundleLoc.getPath());
            		strBfr.append(File.separator).
            		append(Messages.qpidClientJar);
            		URL jarLoc = new URL(strBfr.toString());
            		IPath jarPath = new Path(
            				FileLocator.resolve(jarLoc).getPath());
            		File jarFile = jarPath.toFile();
            		listEntries.add(JavaCore.newLibraryEntry(new Path(
            				jarFile.getAbsolutePath()),
            				null, null, null, attr, true));
                } else {
                	File directory = new File(resQpidJar.getPath());
                	// create the library entry for qpid jar
                	listEntries.add(JavaCore.newLibraryEntry(new Path(
                			directory.getAbsolutePath()), null,
                			null, null, attr, true));
                	FilenameFilter qpidJarsFilter = new QpidJarsFilter();
                	File[] jars = new File(String.format(
                			"%s%s%s", directory.getParent(),
                			File.separator,
                			Messages.depLocation)).
                			listFiles(qpidJarsFilter);
                	for (int i = 0; i < jars.length; i++) {
                		listEntries.add(JavaCore.newLibraryEntry(
                				new Path(jars[i].getAbsolutePath()),
                				null, null, null, attr, true));
                	}
                }
            } catch (Exception excp) {
                listEntries = new ArrayList<IClasspathEntry>();
                Activator.getDefault().log(Messages.excp, excp);
            }
        }

        IClasspathEntry[] entries = new IClasspathEntry[listEntries.size()];
        //Return the classpath entries.
        return listEntries.toArray(entries);
	}

	@Override
	public String getDescription() {
		return Messages.containerDesc;
	}

	@Override
	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	@Override
	public IPath getPath() {
		return containerPath;
	}

	/**
	 * This class acts as a filter to accept jar files only.
	 */
	class QpidJarsFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".jar")
					&& name.indexOf(Messages.src) == -1);
		}
	}
}
