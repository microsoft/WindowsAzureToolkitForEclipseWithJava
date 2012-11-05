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

public class WALocalStorageTest {
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
        if (Utility.isProjExist(Messages.projWithLclStr)) {
            // delete existing project
            Utility.selProjFromExplorer(Messages.projWithLclStr).select();
            Utility.deleteSelectedProject();
        }
        Utility.createProjWithLocalStorage(Messages.projWithLclStr);
    }

    @After
     public void cleanUp() throws Exception {
        if(Utility.isProjExist(Messages.projWithLclStr)) {
            Utility.selProjFromExplorer(Messages.projWithLclStr).select();
            Utility.deleteSelectedProject();
            }
        }

    @Test
    // (New Test Cases for 1.6) test case 5
    public void testLclStrPropertyPagePresent() throws Exception {
        /*To select Local Storage Property Page using
         * context menu of role folder WorkerRole2
         */
        SWTBotShell propShell = Utility.
             selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                     Messages.role2);
        assertTrue("testLclStrPropertyPagePresent", wabot.table().isEnabled()
             && wabot.button(Messages.roleAddBtn).isEnabled());
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 25
    public void testAddBtnPresent() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        assertTrue("testAddBtnPresent",
                wabot.button(Messages.roleAddBtn).isEnabled());
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 26
    public void testAddBtnPressed() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        boolean value = false;
        value=wabot.label(Messages.lclStrRsrNameLbl).isVisible()
                 && wabot.label(Messages.lclStrSizeLbl).isVisible()
                 && wabot.label(Messages.lclStrDirPathLbl).isVisible()
                 && wabot.textWithLabel(Messages.lclStrRsrNameLbl).isEnabled()
                 && wabot.textWithLabel(Messages.lclStrSizeLbl).isEnabled()
                 && wabot.textWithLabel(Messages.lclStrDirPathLbl).isEnabled()
                 && wabot.button("OK").isEnabled()
                 && wabot.button("Cancel").isEnabled();
        assertTrue("testAddBtnPressed",value);
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 27
    public void testRsrNameText() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        assertTrue("testRsrNameText", wabot.
                textWithLabel(Messages.lclStrRsrNameLbl).getText().
                equals("LocalStorage1"));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 28
    public void testSizeText() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        assertTrue("testSizeText", wabot.
                textWithLabel(Messages.lclStrSizeLbl).getText().equals("1"));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 29
    public void testSizeNote() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        assertTrue("testSizeNote", wabot.label(Messages.lclStrSizeNote).isVisible());
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 30
    public void testChkUnchecked() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        assertTrue("testChkUnchecked", !wabot.checkBox().isChecked());
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 31
    public void testPathText() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        assertTrue("testPathText", wabot.
                textWithLabel(Messages.lclStrDirPathLbl).getText().
                equals("LocalStorage1_PATH")
                && !wabot.checkBox().isChecked());
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 32
    public void testRsrNameBlankAddBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrRsrNameLbl).setText("");
        assertTrue("testRsrNameBlankAddBtn", !wabot.button("OK").isEnabled());
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 33
    public void testSizeBlankAddBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).setText("");
        assertTrue("testSizeBlankAddBtn", !wabot.button("OK").isEnabled());
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 34
    public void testRsrNameIncrement() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        assertTrue("testRsrNameIncrement",wabot.
                textWithLabel(Messages.lclStrRsrNameLbl).getText().
                equals("LocalStorage2"));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 35
    public void testSizeZeroAddBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).setText("0");
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        assertTrue("testSizeZeroAddBtn",
                    errMsg.equals(Messages.sizeErrTtl));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 36
    // throws NumberFormatException
    public void testSizeStringAddBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).
            setText(Messages.sizeStrVal);
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        assertTrue("testSizeStringAddBtn",
                    errMsg.equals(Messages.sizeErrTtl));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 37
    public void testSizeNegValAddBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).
            setText(Messages.sizeNegVal);
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        assertTrue("testSizeNegValAddBtn",
                    errMsg.equals(Messages.sizeErrTtl));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 38
    public void testSizeGtMaxVal() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).
            setText(Messages.sizeMaxVal);
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeLimitErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        assertTrue("testSizeGtMaxVal",
                    errMsg.equals(Messages.sizeLimitErrTtl));
        errShell.close();
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 39
    public void testSizeGtMaxValYesPressed() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).
            setText(Messages.sizeMaxVal);
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeLimitErrTtl);
        errShell.activate();
        wabot.button("Yes").click();
        assertTrue("testSizeGtMaxValYesPressed", wabot.table().
                cell(0,1).equals(Messages.sizeMaxVal));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 40
    public void testSizeGtMaxValNoPressed() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).
            setText(Messages.sizeMaxVal);
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeLimitErrTtl);
        errShell.activate();
        wabot.button("No").click();
        assertTrue("testSizeGtMaxValNoPressedEditBtn",
        		wabot.textWithLabel(Messages.lclStrSizeLbl).getText().
        		equals(Messages.sizeMaxVal));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 41
    public void testRsrNameExistAddBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrRsrNameLbl).
            setText("LocalStorage1");
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.rsrNameErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        assertTrue("testRsrNameExistAddBtn",
                    errMsg.equals(Messages.rsrNameErrTtl));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 42
    public void testCheckChkBoxAddBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.checkBox().click();
        wabot.button("OK").click();
        assertTrue("testCheckChkBoxAddBtn", wabot.table().cell(0,2).
                equals("Clean"));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 43
    public void testUnCheckChkBoxAddBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role1);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.button("OK").click();
        assertTrue("testUnCheckChkBoxAddBtn", wabot.table().cell(0,2).
                equals("Preserve"));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 44
    public void testRemoveDisable() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        assertTrue("testRemoveDisable",
                !wabot.button(Messages.roleRemBtn).isEnabled());
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 45
    public void testRemoveEnable() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0,0);
        assertTrue("testRemoveEnable",
                wabot.button(Messages.roleRemBtn).isEnabled());
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 46
    public void testRemovePressed() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0,0);
        wabot.button(Messages.roleRemBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.remLclStrTtl);
        lclStrShell.activate();
        String confirmMsg = lclStrShell.getText();
        assertTrue("testRemovePressed",
                confirmMsg.equals(Messages.remLclStrTtl)
                && wabot.button("Yes").isEnabled()
                && wabot.button("No").isEnabled());
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 47
    public void testRemoveNoPressed() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0,0);
        wabot.button(Messages.roleRemBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.remLclStrTtl);
        lclStrShell.activate();
        wabot.button("No").click();
        assertTrue("testRemoveNoPressed", wabot.
                table().cell(0,0).equals("LocalStorage1"));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 48
    public void testRemoveYesPressed() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0,0);
        wabot.button(Messages.roleRemBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.remLclStrTtl);
        lclStrShell.activate();
        wabot.button("Yes").click();
        assertFalse("testRemoveYesPressed", wabot.table()
                .containsItem("LocalStorage1"));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 49
    public void testInPlaceEditRsrNameExist() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.button("OK").click();
        //Edit table by giving already existing Environment variable name
        wabot.table().click(1, 0);
        wabot.text("LocalStorage2", 0).typeText("LocalStorage1");
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell errShell = wabot.shell(Messages.rsrNameErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        // Cancel Add Local storage shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditRsrNameExist",
                    errMsg.equals(Messages.rsrNameErrTtl));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 50
    public void testInPlaceEditRsrName() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0, 0);
        wabot.text("LocalStorage1", 0).typeText(Messages.rsrNameEditVal);
        wabot.table().click(0, 0);
        assertTrue("testInPlaceEditRsrName",wabot.
                table().cell(0, 0).equals(Messages.rsrNameEditVal));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 51
    public void testInPlaceEditSizeZero() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0, 1);
        wabot.text("1", 0).setText("0");
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell errShell = wabot.shell(Messages.sizeErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        // Cancel Add Local storage shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditSizeZero",
                    errMsg.equals(Messages.sizeErrTtl));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 52
    // throws NumberFormatException
    public void testInPlaceEditSizeString() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0, 1);
        wabot.text("1", 0).setText(Messages.sizeStrVal);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell errShell = wabot.shell(Messages.sizeErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        // Cancel Add Local storage shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditSizeString",
                    errMsg.equals(Messages.sizeErrTtl));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 53
    public void testInPlaceEditSizeNegVal() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0, 1);
        wabot.text("1", 0).setText(Messages.sizeNegVal);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell errShell = wabot.shell(Messages.sizeErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        // Cancel Add Local storage shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditSizeNegVal",
                    errMsg.equals(Messages.sizeErrTtl));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 54
    public void testInPlaceEditSizeMaxVal() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0, 1);
        wabot.text("1", 0).setText(Messages.sizeMaxVal);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell errShell = wabot.shell(Messages.sizeLimitErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        errShell.close();
        // Cancel Add Local storage shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditSizeMaxVal",
                    errMsg.equals(Messages.sizeLimitErrTtl));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 55
    public void testInPlaceEditSizeMaxValNoPressesd() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0, 1);
        wabot.text("1", 0).setText(Messages.sizeMaxVal);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell errShell = wabot.shell(Messages.sizeLimitErrTtl);
        errShell.activate();
        wabot.button("No").click();
        // Cancel Add Local storage shell
        wabot.button("Cancel").click();
        assertFalse("testInPlaceEditSizeMaxValNoPressesd", wabot.table()
                .containsItem(Messages.sizeMaxVal));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 56
    public void testInPlaceEditSizeMaxValYesPressesd() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0, 1);
        wabot.text("1", 0).setText(Messages.sizeMaxVal);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell errShell = wabot.shell(Messages.sizeLimitErrTtl);
        errShell.activate();
        wabot.button("Yes").click();
        // Cancel Add Local storage shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditSizeMaxValYesPressesd", wabot.
                table().cell(0, 1).equals(Messages.sizeMaxVal));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 57
    public void testInPlaceEditSize() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0, 1);
        wabot.text("1", 0).setText(Messages.sizeValidVal);
        wabot.table().click(0, 0);
        assertTrue("testInPlaceEditSize", wabot.
                table().cell(0, 1).equals(Messages.sizeValidVal));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 58
    public void testInPlacEditRecyclePreserve() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().click(0, 0);
        // Edit On Recycle property from Preserve to Clean
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.checkBox().select();
        wabot.button("OK").click();
        // In place edit to Preserve
        wabot.table().click(0, 2);
        wabot.ccomboBox().setSelection(1);
        wabot.table().click(0, 0);
        assertTrue("testInPlacEditRecyclePreserve", wabot.
                table().cell(0, 2).equals("Preserve"));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 59
    public void testInPlacEditRecycleClean() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        // In place edit to Clean
        wabot.table().click(0, 2);
        wabot.ccomboBox().setSelection(0);
        wabot.table().click(0, 0);
        assertTrue("testInPlacEditRecycleClean", wabot.
                table().cell(0, 2).equals("Clean"));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 60
    public void testInPlaceEditEnvVarExist() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.button("OK").click();
        //Edit table by giving already existing Environment variable name
        wabot.table().click(1, 3);
        wabot.text("LocalStorage2_PATH", 0).typeText("LocalStorage1_PATH");
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell errShell = wabot.shell(Messages.envVarErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        // Cancel Add Local storage shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditEnvVarExist",
                    errMsg.equals(Messages.envVarErrTtl));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 61
    public void testInPlaceEditEnvVar() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.button("OK").click();
        wabot.table().click(1, 3);
        wabot.text("LocalStorage2_PATH", 0).
            typeText(Messages.rsrEnvVarEditVal);
        wabot.table().click(0, 0);
        assertTrue("testInPlaceEditEnvVar", wabot.
                table().cell(1, 3).equals(Messages.rsrEnvVarEditVal));
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 62
    public void testEditBtnPresent() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        assertTrue("testEditBtnPresent",
                wabot.button(Messages.roleEditBtn).isVisible());
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 63
    public void testEditDisabled() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        assertTrue("testEditDisabled",
                !wabot.button(Messages.roleEditBtn).isEnabled());
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 64
    public void testEditEnabled() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        assertTrue("testEditEnabled",
                wabot.button(Messages.roleEditBtn).isEnabled());
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 65
    public void testEditPressed() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        boolean value = false;
        value = wabot.label(Messages.lclStrRsrNameLbl).isVisible()
                 && wabot.label(Messages.lclStrSizeLbl).isVisible()
                 && wabot.label(Messages.lclStrDirPathLbl).isVisible()
                 && wabot.textWithLabel(Messages.lclStrRsrNameLbl).isEnabled()
                 && wabot.textWithLabel(Messages.lclStrSizeLbl).isEnabled()
                 && wabot.textWithLabel(Messages.lclStrDirPathLbl).isEnabled()
                 && wabot.button("OK").isEnabled()
                 && wabot.button("Cancel").isEnabled();
        assertTrue("testEditPressed",value);
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 66
    public void testEditDefaultVal() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        // Get text from table cells
        String rsrName = wabot.table().cell(0,0);
        String size = wabot.table().cell(0,1);
        String path =  wabot.table().cell(0,3);
        wabot.table().click(0, 0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        boolean value = false;
        value = wabot.textWithLabel(Messages.lclStrRsrNameLbl).
                    getText().equals(rsrName)
                 && wabot.textWithLabel(Messages.lclStrSizeLbl).
                     getText().equals(size)
                 && wabot.textWithLabel(Messages.lclStrDirPathLbl).
                     getText().equals(path)
                 && wabot.button("OK").isEnabled()
                 && wabot.button("Cancel").isEnabled();
        assertTrue("testEditDefaultVal",value);
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 67
    public void testRsrNameBlankEditBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrRsrNameLbl).setText("");
        assertTrue("testRsrNameBlankEditBtn", !wabot.button("OK").isEnabled());
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 68
    public void testSizeBlankEditBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).setText("");
        assertTrue("testSizeBlankEditBtn", !wabot.button("OK").isEnabled());
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 69
    public void testSizeZeroEditBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).setText("0");
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        assertTrue("testSizeZeroEditBtn",
                    errMsg.equals(Messages.sizeErrTtl));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 70
    // throws NumberFormatException
    public void testSizeStringEditBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).
            setText(Messages.sizeStrVal);
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        assertTrue("testSizeStringEditBtn",
                    errMsg.equals(Messages.sizeErrTtl));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 71
    public void testSizeNegValEditBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).
            setText(Messages.sizeNegVal);
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        wabot.button("OK").click();
        assertTrue("testSizeNegValEditBtn",
                    errMsg.equals(Messages.sizeErrTtl));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 72
    public void testSizeGtMaxValEditBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).
            setText(Messages.sizeMaxVal);
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeLimitErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        assertTrue("testSizeGtMaxValEditBtn",
                    errMsg.equals(Messages.sizeLimitErrTtl));
        errShell.close();
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 73
    public void testSizeGtMaxValYesPressedEditBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).
            setText(Messages.sizeMaxVal);
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeLimitErrTtl);
        errShell.activate();
        wabot.button("Yes").click();
        assertTrue("testSizeGtMaxValYesPressedEditBtn", wabot.table().
                cell(0,1).equals(Messages.sizeMaxVal));
        propShell.close();
    }

    @Test
    public void testSizeGtMaxValNoPressedEditBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrSizeLbl).
            setText(Messages.sizeMaxVal);
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.sizeLimitErrTtl);
        errShell.activate();
        wabot.button("No").click();
        lclStrShell.activate();
        assertTrue("testSizeGtMaxValNoPressedEditBtn",
        		wabot.textWithLabel(Messages.lclStrSizeLbl).getText().
        		equals(Messages.sizeMaxVal));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 74
    public void testRsrNameExistEditBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.button(Messages.roleAddBtn).click();
        wabot.shell(Messages.lclStrTtl).activate();
        wabot.button("OK").click();
        wabot.table().select(1);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.textWithLabel(Messages.lclStrRsrNameLbl).
        setText("LocalStorage1");
        wabot.button("OK").click();
        SWTBotShell errShell = wabot.shell(Messages.rsrNameErrTtl);
        errShell.activate();
        String errMsg = errShell.getText();
        assertTrue("testRsrNameExistEditBtn",
                    errMsg.equals(Messages.rsrNameErrTtl));
        wabot.button("OK").click();
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 75
    public void testCheckChkBoxEditBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.checkBox().click();
        wabot.button("OK").click();
        assertTrue("testCheckChkBoxEditBtn", wabot.table().cell(0,2).
                equals("Clean"));
        lclStrShell.close();
        propShell.close();
    }

    @Test
    // (New Test Cases for 1.6) test case 76
    public void testUnCheckChkBoxEditBtn() throws Exception {
        SWTBotShell propShell = Utility.
                 selLclStrPageUsingCnxtMenu(Messages.projWithLclStr,
                         Messages.role2);
        wabot.table().select(0);
        wabot.button(Messages.roleEditBtn).click();
        SWTBotShell lclStrShell = wabot.shell(Messages.lclStrTtl);
        lclStrShell.activate();
        wabot.button("OK").click();
        assertTrue("testUnCheckChkBoxEditBtn", wabot.table().cell(0,2).
                equals("Preserve"));
        lclStrShell.close();
        propShell.close();
    }

}
