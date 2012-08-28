/**
 * Copyright 2011 Persistent Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.persistent.uiautomation;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
@RunWith(SWTBotJunit4ClassRunner.class)
public class ExplorerTest {

    private static SWTWorkbenchBot wabot;

    @BeforeClass
    public static void beforeClass() throws Exception {
        wabot = new SWTWorkbenchBot();
        try {
            wabot.viewByTitle("Welcome").close();
        } catch (Exception e) {
        }
    }

    @Before
    public void setUp() throws Exception {
        wabot.closeAllShells();
        //create a new project called WAExplorer
        if(Utility.isProjExist(Messages.projForExplorer)) {
            //delete existing project
            Utility.selProjFromExplorer(Messages.projForExplorer).select();
            Utility.deleteSelectedProject();
        }
            Utility.createProject(Messages.projForExplorer);

    }

    @After
    public void cleanUp() throws Exception {
        if(Utility.isProjExist(Messages.projForExplorer)) {
        Utility.selProjFromExplorer(Messages.projForExplorer).select();
        Utility.deleteSelectedProject();
        }
    }

    @Test
    public void testPluginEntry() throws Exception {
        assertTrue("testPluginEntry",Utility.isProjExist(Messages.projForExplorer));
    }

    @Test
    public void testRenameProject() throws Exception {
        Utility.selProjFromExplorer(Messages.projForExplorer).select();
        Utility.renameSelectedResource(Messages.newProjForExp);
        assertTrue("testRenameProject", Utility.isProjExist(
                Messages.newProjForExp));
        Utility.selProjFromExplorer(Messages.newProjForExp).select();
        Utility.deleteSelectedProject();

    }

    @Test
    public void testRenameProjectWithExistingName() throws Exception {
        Utility.selProjFromExplorer(Messages.projForExplorer).select();
        wabot.menu("File").menu("Rename...").click();
        SWTBotShell shell = wabot.shell("Rename Resource").activate();
        wabot.textWithLabel("New name:").setText(Messages.projForExplorer);
        assertFalse("testRenameProjectWithExistingName",wabot.button("OK").isEnabled());
        shell.close();
        wabot.waitUntil(shellCloses(shell));

     }

    @Test
    public void testRenameRole() throws Exception {
        SWTBotTreeItem  proj = Utility.selProjFromExplorer(Messages.projForExplorer);
        SWTBotTreeItem role = proj.expand().getNode(Messages.role1).select();
        Utility.renameSelectedResource(Messages.role2);
        //delay to complete rename operation
        wabot.sleep(1000);
        proj = Utility.selProjFromExplorer(Messages.projForExplorer);
        role = proj.expand().getNode(Messages.role2).select();
        assertEquals("testRenameRole", Messages.role2, role.getText());
    }

    @Test
    public void testRenameRoleWithExistingName() throws Exception {
        Utility.createProjWithEp(Messages.projName);
        SWTBotTreeItem  proj = Utility.selProjFromExplorer(Messages.projName);
        proj.expand().getNode(Messages.role1).select();
        wabot.menu("File").menu("Rename...").click();
        SWTBotShell shell = wabot.shell("Rename Resource").activate();
        wabot.textWithLabel("New name:").setText(Messages.role2);
        assertFalse("testRenameRoleWithExistingName",wabot.button("OK").isEnabled());
        shell.close();
        wabot.waitUntil(shellCloses(shell));
        Utility.selProjFromExplorer(Messages.projName).select();
        Utility.deleteSelectedProject();
    }
}
