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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)

public class WALoadBalancingTest {
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
        if (Utility.isProjExist(Messages.projWithLoadBlc)) {
            // delete existing project
            Utility.selProjFromExplorer(Messages.projWithLoadBlc).select();
            Utility.deleteSelectedProject();
        }
        Utility.createProject(Messages.projWithLoadBlc);
    }

     @After
     public void cleanUp() throws Exception {
         if(Utility.isProjExist(Messages.projWithLoadBlc)) {
             Utility.selProjFromExplorer(Messages.projWithLoadBlc).select();
             Utility.deleteSelectedProject();
             }
        }

     @Test
     // (New Test Cases for 1.6) test case 7
     public void testStickyChkPresent() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         assertTrue("testStickyChkPresent", wabot.
                 checkBox().isEnabled());
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 78
     public void testLoadBlcNodePresent() throws Exception {
        SWTBotTreeItem proj1 = Utility.
                selProjFromExplorer(Messages.projWithLoadBlc);
        SWTBotTreeItem workerRoleNode = proj1.expand().
                getNode(Messages.role1).select();
        workerRoleNode.contextMenu("Properties").click();
        SWTBotShell propShell = wabot.
                shell(String.format("%s%s%s", Messages.propPageTtl,
						" ", Messages.role1));
        propShell.activate();
        SWTBotTree properties = propShell.bot().tree();
        SWTBotTreeItem node = properties.getTreeItem(Messages.roleTreeRoot).
                expand().getNode(Messages.loadBlcPage);
        assertTrue(" testLoadBlcNodePresent", node.getText().
                equals(Messages.loadBlcPage));
        propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 79
     public void testLoadBclPrpPagePresent() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         assertTrue("testLoadBclPrpPagePresent", wabot.
                 checkBox().isEnabled());
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 80
     public void testEndPtComboDisabled() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         assertTrue("testEndPtComboDisabled", !wabot.comboBox().isEnabled());
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 81
     public void testChkChecked() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         Boolean val1 = wabot.comboBox().isEnabled();
         Utility.selEndPtPage();
         Boolean val2 = wabot.table().cell(1, 0).equals(Messages.sesAfinityStr);
         assertTrue("testChkChecked", val1 && val2);
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 82
     public void testStkSesEnabledReopenPage() throws Exception {
         Utility.selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         // Navigate to other page
         Utility.selEndPtPage();
         Utility.selLoadBlcPage();
         String str1= wabot.comboBox().getText();
         wabot.button("OK").click();
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         String str2= wabot.comboBox().getText();
         assertTrue("testStkSesEnabledReopenPage", str1.equals(str2));
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 83
     public void testStkSesDisabledReopenPage() throws Exception {
         Utility.selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         // Enable
         wabot.checkBox().select();
         // Disable
         wabot.checkBox().click();
         Boolean val1 = !wabot.comboBox().isEnabled();
         wabot.button("OK").click();
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         Boolean val2 = !wabot.comboBox().isEnabled();
         assertTrue("testStkSesDisabledReopenPage", val1 && val2);
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 84
     public void testInputEndPtRemove() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         Utility.selEndPtPage();
         wabot.table().select(0);
         wabot.button(Messages.roleRemBtn).click();
         SWTBotShell cnfrmShell = wabot.shell(Messages.delEndPtTtl);
         cnfrmShell.activate();
         String confirmMsg = cnfrmShell.getText();
         assertTrue("testInputEndPtRemove",
                 confirmMsg.equals(Messages.delEndPtTtl));
         cnfrmShell.close();
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 85
     public void testInputEndPtRemNoPressed() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         Utility.selEndPtPage();
         wabot.table().select(0);
         wabot.button(Messages.roleRemBtn).click();
         SWTBotShell cnfrmShell = wabot.shell(Messages.delEndPtTtl);
         cnfrmShell.activate();
         wabot.button("No").click();
         assertTrue("testInputEndPtRemNoPressed", wabot.table().
                 cell(0, 0).equals(Messages.intEndPt));
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 86
     public void testInputEndPtRemYesPressed() throws Exception {
         Utility.selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         Utility.selEndPtPage();
         wabot.table().select(0);
         wabot.button(Messages.roleRemBtn).click();
         SWTBotShell cnfrmShell = wabot.shell(Messages.delEndPtTtl);
         cnfrmShell.activate();
         wabot.button("Yes").click();
         Boolean val1 = !wabot.table().containsItem(Messages.sesAfinityStr);
         Utility.selLoadBlcPage();
         Boolean val2 = !wabot.comboBox().isEnabled();
         assertTrue("testInputEndPtRemYesPressed", val1 && val2); 
         wabot.button("OK").click();
     }

     @Test
     // (New Test Cases for 1.6) test case 87
     public void testInternalEndPtRemove() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         Utility.selEndPtPage();
         wabot.table().select(1);
         wabot.button(Messages.roleRemBtn).click();
         SWTBotShell cnfrmShell = wabot.shell(Messages.delEndPtTtl);
         cnfrmShell.activate();
         String confirmMsg = cnfrmShell.getText();
         assertTrue("testInternalEndPtRemove",
                 confirmMsg.equals(Messages.delEndPtTtl));
         cnfrmShell.close();
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 88
     public void testInternalEndPtRemNoPressed() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         Utility.selEndPtPage();
         wabot.table().select(1);
         wabot.button(Messages.roleRemBtn).click();
         SWTBotShell cnfrmShell = wabot.shell(Messages.delEndPtTtl);
         cnfrmShell.activate();
         wabot.button("No").click();
         assertTrue("testInternalEndPtRemNoPressed", wabot.table().
                 cell(1, 0).equals(Messages.sesAfinityStr));
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 89
     public void testInternalEndPtRemYesPressed() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         Utility.selEndPtPage();
         wabot.table().select(1);
         wabot.button(Messages.roleRemBtn).click();
         SWTBotShell cnfrmShell = wabot.shell(Messages.delEndPtTtl);
         cnfrmShell.activate();
         wabot.button("Yes").click();
         Boolean val1 = !wabot.table().containsItem(Messages.sesAfinityStr);
         Utility.selLoadBlcPage();
         Boolean val2 = !wabot.comboBox().isEnabled();
         assertTrue("testInternalEndPtRemYesPressed", val1 && val2);
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 90
     public void testChangeTypeInternalToInput() throws Exception {
         Utility.selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         Utility.selEndPtPage();
         wabot.table().click(1, 1);
         wabot.ccomboBox().setSelection(0);
         // OK Does not work so click on Add...
         wabot.button(Messages.roleAddBtn).click();
         SWTBotShell errShell = wabot.shell(Messages.dbgEpCnfTtl);
         errShell.activate();
         String errMsg = errShell.getText();
         assertTrue("testChangeTypeInternalToInput",
                 errMsg.equals(Messages.dbgEpCnfTtl));
         wabot.button("OK").click();
         // Cancel Add Local storage shell
         wabot.button("Cancel").click();
         wabot.button("OK").click();
     }

     @Test
     // (New Test Cases for 1.6) test case 91
     public void testChangeTypeInputToInternal() throws Exception {
         Utility.selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         Utility.selEndPtPage();
         wabot.table().click(0, 1);
         wabot.ccomboBox().setSelection(1);
         // OK Does not work so click on Add...
         wabot.button(Messages.roleAddBtn).click();
         SWTBotShell errShell = wabot.shell(Messages.dbgEpCnfTtl);
         errShell.activate();
         String errMsg = errShell.getText();
         assertTrue("testChangeTypeInputToInternal",
                 errMsg.equals(Messages.dbgEpCnfTtl));
         wabot.button("OK").click();
         // Cancel Add Local storage shell
         wabot.button("Cancel").click();
         wabot.button("OK").click();
     }

     @Test
     // (New Test Cases for 1.6) test case 92
     public void testDebugStkSesEndPtNotEql() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         String loadBlcStr = wabot.comboBox().getText();
         Utility.selDebugPage();
         wabot.checkBox().select();
         String debugStr = wabot.comboBox().getText();
         assertTrue("testDebugStkSesEndPtNotEql",
                 !loadBlcStr.equals(debugStr));
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 93
     public void testStkSesDebugEndPtNotEql() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         Utility.selDebugPage();
         wabot.checkBox().select();
         String debugStr = wabot.comboBox().getText();
         Utility.selLoadBlcPage();
         wabot.checkBox().select();
         String loadBlcStr = wabot.comboBox().getText();
         assertTrue("testStkSesDebugEndPtNotEql",
                 !debugStr.equals(loadBlcStr));
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 97
     public void testDefaultEndPt() throws Exception {
         SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
         wabot.checkBox().select();
         assertTrue("testDefaultEndPt", wabot.comboBox().isEnabled()
                 && wabot.comboBox().getText().equals(Messages.httpEndPtStr));
         propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 98
     public void testFirstInputEndPt() throws Exception {
        SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
        // Add two end points of type input
        Utility.selEndPtPage();
        wabot.button(Messages.roleAddBtn).click();
        Utility.addEp("IntEndPt1", "Input", "11", "13");
        wabot.button(Messages.roleAddBtn).click();
        Utility.addEp("IntEndPt2", "Input", "21", "23");
        // Delete default http end point
        wabot.table().click(0, 0);
        wabot.button(Messages.roleRemBtn).click();
        SWTBotShell cnfrmShell = wabot.shell(Messages.delEndPtTtl);
        cnfrmShell.activate();
        wabot.button("Yes").click();
        // Enable Load Balancing option
        Utility.selLoadBlcPage();
        wabot.checkBox().select();
        assertTrue("testFirstInputEndPt", wabot.comboBox().
                getText().equals(Messages.inputEndPtStr));
        propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 99
     public void testNoInputEndPtPresent() throws Exception {
        SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
        // Delete default http end point
        Utility.selEndPtPage();
        wabot.table().click(0, 0);
        wabot.button(Messages.roleRemBtn).click();
        SWTBotShell cnfrmShell = wabot.shell(Messages.delEndPtTtl);
        cnfrmShell.activate();
        wabot.button("Yes").click();
        // Enable Load Balancing option
        Utility.selLoadBlcPage();
        wabot.checkBox().select();
        SWTBotShell errShell = wabot.shell(Messages.intEndPtErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        assertTrue("testNoInputEndPtPresent",
                 errMsg.equals(Messages.intEndPtErrTtl));
        errShell.close();
        propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 100
     public void testNoInputEndPtCancel() throws Exception {
        SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
        // Delete default http end point
        Utility.selEndPtPage();
        wabot.table().click(0, 0);
        wabot.button(Messages.roleRemBtn).click();
        SWTBotShell cnfrmShell = wabot.shell(Messages.delEndPtTtl);
        cnfrmShell.activate();
        wabot.button("Yes").click();
        // Enable Load Balancing option
        Utility.selLoadBlcPage();
        wabot.checkBox().select();
        SWTBotShell errShell = wabot.shell(Messages.intEndPtErrTtl);
        errShell.activate();
        wabot.button("Cancel").click();
        assertTrue("testNoInputEndPtCancel", !wabot.comboBox().isEnabled()
                 && !wabot.checkBox().isChecked());
        propShell.close();
     }

     @Test
     // (New Test Cases for 1.6) test case 101
     public void testNoInputEndPtOK() throws Exception {
        SWTBotShell propShell = Utility.
                 selLoadBlcUsingCnxtMenu(Messages.projWithLoadBlc);
        // Delete default http end point
        Utility.selEndPtPage();
        wabot.table().click(0, 0);
        wabot.button(Messages.roleRemBtn).click();
        SWTBotShell cnfrmShell = wabot.shell(Messages.delEndPtTtl);
        cnfrmShell.activate();
        wabot.button("Yes").click();
        // Enable Load Balancing option
        Utility.selLoadBlcPage();
        wabot.checkBox().select();
        SWTBotShell errShell = wabot.shell(Messages.intEndPtErrTtl);
        errShell.activate();
        wabot.button("OK").click();
        Boolean comboVal = wabot.comboBox().getText().
                equals(Messages.httpEndPtStr);
        Utility.selEndPtPage();
        assertTrue("testNoInputEndPtOK", wabot.table().
                     cell(0, 0).equals(Messages.intEndPt)
                 && wabot.table().
                     cell(1, 0).equals(Messages.sesAfinityStr)
                 && comboVal);
        propShell.close();
     }

     @Test
     // (New Test Cases for 1.7) test case 125
     public void testEndPtList() throws Exception {
    	 SWTBotTreeItem proj1 = Utility.
    			 selProjFromExplorer(Messages.projWithLoadBlc);
    	 SWTBotTreeItem workerRoleNode = proj1.expand().
    			 getNode(Messages.role1).select();
    	 workerRoleNode.contextMenu("Properties").click();
    	 SWTBotShell propShell = Utility.selEndPtPage();
    	 wabot.button(Messages.roleAddBtn).click();
    	 Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
    	 wabot.button(Messages.roleAddBtn).click();
    	 Utility.addEp("InputTest", "Input", "111", "111");
    	 Utility.selLoadBlcPage();
    	 wabot.checkBox().select();
    	 String[] endpts = wabot.comboBox().items();
    	 List<String> endList = Arrays.asList(endpts);
    	 assertTrue("testEndPtList", endList.contains(Messages.httpEndPtStr)
    			 && endList.contains("InputTest (public:111,private:111)")
    			 && !endList.contains("InstncTest (public:13-15,private:16)"));
    	 propShell.close();
     }
}
