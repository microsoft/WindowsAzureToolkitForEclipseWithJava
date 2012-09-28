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
package com.persistent.ui.propertypage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.persistent.winazureroles.WARoles;

/**
 *
 * This class creates property page for Roles under Windows Azure in properties
 */
public class WARolesPropertyPage extends PropertyPage {

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        //display help content
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                "com.persistent.winazure.eclipseplugin." +
                "windows_azure_project_roles");
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        composite.setLayout(gridLayout);
        composite.setLayoutData(gridData);

        //draws roles table and buttons
        WARoles.displayRoles(composite, false);

        return composite;
    }

    @Override
    public boolean performOk() {
        return super.performOk();
    }

}
