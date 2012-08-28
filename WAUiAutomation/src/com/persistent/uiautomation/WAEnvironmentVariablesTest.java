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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)

public class WAEnvironmentVariablesTest {
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
        if (Utility.isProjExist(Messages.projWithEnvVar)) {
            // delete existing project
            Utility.selProjFromExplorer(Messages.projWithEnvVar).select();
            Utility.deleteSelectedProject();
        }
        Utility.createProjWithEnvVar(Messages.projWithEnvVar);
    }

     @After
     public void cleanUp() throws Exception {
         if(Utility.isProjExist(Messages.projWithEnvVar)) {
             Utility.selProjFromExplorer(Messages.projWithEnvVar).select();
             Utility.deleteSelectedProject();
             }
        }

     @Test
        // (New Test Cases for 1.6) test case 4
     public void testEnvVarPropertyPagePresent() throws Exception {
         /*To select Environment Variable Property Page using
          * context menu of role folder WorkerRole2
          */
         SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         assertTrue("testEnvVarPropertyPagePresent",wabot.table().isEnabled()
                 && wabot.button(Messages.roleAddBtn).isEnabled());
         propShell.close();
     }

     @Test
         // (New Test Cases for 1.6) test case 11
     public void testEditRemoveDisable() throws Exception {
         SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         assertTrue("testEditRemoveDisable",!wabot.button(Messages.roleEditBtn).isEnabled()
                 && !wabot.button(Messages.roleRemBtn).isEnabled()
                 && wabot.button(Messages.roleAddBtn).isEnabled());
         propShell.close();
     }

     @Test
         // (New Test Cases for 1.6) test case 12
     public void testEditRemoveEnable() throws Exception {
         SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         //select first row from table
         wabot.table().click(0,0);
         assertTrue("testEditRemoveDisable",wabot.button(Messages.roleEditBtn).isEnabled()
                 && wabot.button(Messages.roleRemBtn).isEnabled()
                 && wabot.button(Messages.roleAddBtn).isEnabled());
         propShell.close();
     }

     @Test
         // (New Test Cases for 1.6) test case 13
     public void testEnvVarAddPressed() throws Exception {
         SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         wabot.button(Messages.roleAddBtn).click();
         SWTBotShell envVarShell = wabot.shell(Messages.envVarAddTtl);
         envVarShell.activate();
         boolean value = false;
         value=wabot.label(Messages.envVarNameLbl).isVisible()
                 && wabot.label(Messages.envVarValLbl).isVisible()
                 && wabot.textWithLabel(Messages.envVarNameLbl).isEnabled()
                 && wabot.textWithLabel(Messages.envVarValLbl).isEnabled()
                 && !wabot.button("OK").isEnabled()
                 && wabot.button("Cancel").isEnabled();
         assertTrue("testEnvVarAddPressed",value);
         envVarShell.close();
         propShell.close();
     }

     @Test
         // (New Test Cases for 1.6) test case 14
     public void testAddEnvVar() throws Exception {
         SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         Utility.addEnvVar(Messages.envVarNxtName,Messages.envVarNxtVal);
         assertTrue("testAddEnvVar",wabot.table().
                 cell(1,0).equals(Messages.envVarNxtName)
                 && wabot.table().cell(1,1).equals(Messages.envVarNxtVal));
         propShell.close();
     }

     @Test
         // (New Test Cases for 1.6) test case 15
     public void testEnvVarInUse() throws Exception {
         SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         Utility.addEnvVar(Messages.envVarName,Messages.envVarNxtVal);
         SWTBotShell errShell = wabot.shell(Messages.varInUseErrTtl);
         errShell.activate();
         String errMsg = errShell.getText();
         wabot.button("OK").click();
         assertTrue("testEnvVarAlreadyInUse",
                    errMsg.equals(Messages.varInUseErrTtl));
         //Close environment variable's and Property Page's shell
         SWTBotShell envVarShell = wabot.shell(Messages.envVarAddTtl).activate();
         envVarShell.close();
         propShell.close();
     }

    @Test
         // (New Test Cases for 1.6) test case 16
     public void testEnvVarOkDisabled() throws Exception {
         SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         wabot.button(Messages.roleAddBtn).click();
         SWTBotShell envVarShell = wabot.shell(Messages.envVarAddTtl);
         envVarShell.activate();
         assertTrue("testEnvVarAddPressed",!wabot.button("OK").isEnabled());
         envVarShell.close();
         propShell.close();
     }

    @Test
         // (New Test Cases for 1.6) test case 17
     public void testEnvVarOkEnabled() throws Exception {
         SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         wabot.button(Messages.roleAddBtn).click();
         SWTBotShell envVarShell = wabot.shell(Messages.envVarAddTtl);
         envVarShell.activate();
         wabot.textWithLabel(Messages.envVarNameLbl).setText(Messages.envVarName);
         assertTrue("testEnvVarAddPressed", wabot.button("OK").isEnabled());
         envVarShell.close();
         propShell.close();
     }

     @Test
         // (New Test Cases for 1.6) test case 18
     public void testEnvVarValInPlaceEdit() throws Exception {
         Utility.selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         wabot.table().click(0, 0);
         wabot.text(Messages.envVarName, 0).setText(Messages.envVarEditName);
         wabot.table().click(0, 1);
         wabot.text(Messages.envVarVal, 0).setText(Messages.envVarEditVal);
         wabot.button("OK").setFocus();
         wabot.button("OK").click();
         //Verify Edited Values in table
         SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         assertTrue("testEnvVarValInPlaceEdit",wabot.table().cell(0,0).
                 equals(Messages.envVarEditName)
                 && wabot.table().cell(0,1).equals(Messages.envVarEditVal));
         propShell.close();
     }

     @Test
         // (New Test Cases for 1.6) test case 19
     public void testEnvVarValInPlaceEditWrongVal() throws Exception {
         SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         Utility.addEnvVar(Messages.envVarNxtName,Messages.envVarNxtVal);
         //Edit table by giving already existing Environment variable name
         wabot.table().click(1, 0);
         wabot.text(Messages.envVarNxtName, 0).typeText(Messages.envVarName);
         wabot.button(Messages.roleAddBtn).click();
         // Handle Error shell
         SWTBotShell errShell = wabot.shell(Messages.varInUseErrTtl);
         errShell.activate();
         String errMsg = errShell.getText();
         wabot.button("OK").click();
         // Cancel Add Environment Variable shell
         wabot.button("Cancel").click();
         assertTrue("testEnvVarValInPlaceEditWrongValue",
                    errMsg.equals(Messages.varInUseErrTtl));
         propShell.close();
     }

     @Test
        // (New Test Cases for 1.6) test case 20
     public void testEnvVarEditPressed() throws Exception {
        SWTBotShell propShell = Utility.
                selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
        // Get text from table cell
        String valName = wabot.table().cell(0,0);
        String valValue = wabot.table().cell(0,1);
        // select first row from table
        wabot.table().click(0, 0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell envVarEditShell = wabot.shell(Messages.envVarEditTtl);
        envVarEditShell.activate();
        boolean value = false;
        // Verify text in text box and table cell is same initially
        value=wabot.textWithLabel(Messages.envVarNameLbl).getText().equals(valName)
                && wabot.textWithLabel(Messages.envVarValLbl).getText().equals(valValue)
                && wabot.button("OK").isEnabled()
                && wabot.button("Cancel").isEnabled()
                && wabot.label(Messages.envVarNameLbl).isVisible()
                && wabot.label(Messages.envVarValLbl).isVisible();
        assertTrue("testEnvVarEditPressed",value);
        envVarEditShell.close();
        propShell.close();
    }

     @Test
         // (New Test Cases for 1.6) test case 21
     public void testEnvVarAlreadyInUseEdit() throws Exception {
         SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
         Utility.addEnvVar(Messages.envVarNxtName,Messages.envVarNxtVal);
         wabot.table().click(1, 0);
         wabot.button(Messages.roleEditBtn).click();
         SWTBotShell envVarEditShell = wabot.shell(Messages.envVarEditTtl);
         envVarEditShell.activate();
         wabot.textWithLabel(Messages.envVarNameLbl).setText(Messages.envVarName);
         wabot.button("OK").click();
         SWTBotShell errShell = wabot.shell(Messages.varInUseErrTtl);
         errShell.activate();
         String errMsg = errShell.getText();
         wabot.button("OK").click();
         assertTrue("testEnvVarAlreadyInUseEdit",
                    errMsg.equals(Messages.varInUseErrTtl));
         //Close environment variable's and Property Page's shell
         envVarEditShell.close();
         propShell.close();
     }

    @Test
         // (New Test Cases for 1.6) test case 22
    public void testEnvVarRemovePressed() throws Exception {
        SWTBotShell propShell = Utility.
             selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
        wabot.table().click(0, 0);
        wabot.button(Messages.roleRemBtn).click();
        SWTBotShell envVarRemoveShell = wabot.shell(Messages.envVarRemTtl);
        envVarRemoveShell.activate();
        String confirmMsg = envVarRemoveShell.getText();
        assertTrue("testEnvVarRemovePressed",
                confirmMsg.equals(Messages.envVarRemTtl));
        //Close EnvVar remove confirmation and Property Page's shell
        envVarRemoveShell.close();
        propShell.close();
    }

    @Test
        // (New Test Cases for 1.6) test case 23
    public void testEnvVarRemoveNoPressed() throws Exception {
        Utility.selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
        // Get text from table cell
        String valName = wabot.table().cell(0,0);
        String valValue = wabot.table().cell(0,1);
        wabot.table().click(0, 0);
        wabot.button(Messages.roleRemBtn).click();
        SWTBotShell envVarRemoveShell = wabot.shell(Messages.envVarRemTtl);
        envVarRemoveShell.activate();
        wabot.button("No").click();
        wabot.button("OK").click();
        //Verify entry not removed from table
        SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
        assertTrue("testEnvVarRemoveNoPressed",wabot.table().cell(0,0).
                 equals(valName)
                 && wabot.table().cell(0,1).equals(valValue));
        propShell.close();
    }

    @Test
        // (New Test Cases for 1.6) test case 24
    public void testEnvVarRemoveYesPressed() throws Exception {
        Utility.selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
        wabot.table().click(0, 0);
        wabot.button(Messages.roleRemBtn).click();
        SWTBotShell envVarRemoveShell = wabot.shell(Messages.envVarRemTtl);
        envVarRemoveShell.activate();
        wabot.button("Yes").click();
        wabot.button("OK").click();
        //Verify entry removed from table
        SWTBotShell propShell = Utility.
                 selectEnvVarPageUsingContextMenu(Messages.projWithEnvVar);
        assertFalse("testEnvVarRemoveYesPressed", wabot.table()
                .containsItem(Messages.envVarName));
        propShell.close();
    }

}
