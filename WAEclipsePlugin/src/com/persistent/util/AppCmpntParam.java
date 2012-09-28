/**
 * Copyright 2012 Persistent Systems Ltd.
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
package com.persistent.util;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
/**
 * This Class is a utility class for handling application parameters
 * while adding a component.
 */
public class AppCmpntParam {
    private String impSrc;
    private String impAs;
    private String impMethod;
    private static final String BASE_PATH = "${basedir}\\..";

    /**
     * Method returns import source of application.
     * @return
     */
    public String getImpSrc() {
        return impSrc;
    }

    /**
     * Method sets parameterized value of
     * import source to application.
     * @param impSrc
     */
    public void setImpSrc(String impSrc) {
        String pathLoc = impSrc;
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        if (pathLoc.contains(root.getLocation().toOSString())) {
            String wrkSpcPath = root.getLocation().toOSString();
            String replaceString = pathLoc;
            String subString = impSrc.substring(
                    pathLoc.indexOf(wrkSpcPath),
                    wrkSpcPath.length());
            pathLoc = replaceString.replace(subString,
                    BASE_PATH);
            this.impSrc = pathLoc;
        } else {
        this.impSrc = impSrc;
        }
    }

    /**
     * Method returns import as of application.
     * @return
     */
    public String getImpAs() {
        return impAs;
    }

    /**
     * Method sets parameterized value of
     * import as to application.
     * @param impAs
     */
    public void setImpAs(String impAs) {
        this.impAs = impAs;
    }

    /**
     * Method returns import method of application.
     * @return
     */
    public String getImpMethod() {
        return impMethod;
    }

    /**
     * Method sets parameterized value of
     * import method to application.
     * @param impMethod
     */
    public void setImpMethod(String impMethod) {
        this.impMethod = impMethod;
    }
}
