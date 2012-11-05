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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
@RunWith(SWTBotJunit4ClassRunner.class)
public class WAProjectWizardTest {
    private static SWTWorkbenchBot	wabot;

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
    }

    @After
    public void cleanUp() throws Exception {
    	if (Utility.isProjExist(Messages.projName)) {
    		Utility.selProjFromExplorer(Messages.projName).select();
    		Utility.deleteSelectedProject();
    	}
    }
    
    @Test
    public void testCreateNewProject() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Finish").click();
        wabot.waitUntil(shellCloses(sh));
        wabot.sleep(10000);
        assertTrue(Messages.waProjNotExist, Utility.isProjExist(Messages.projName));
    }

    @Test
    public void testCreateNewProjectWithEp() throws Exception {
        Utility.createProjWithEp(Messages.projName);
        assertTrue(Messages.waProjNotExist, Utility.isProjExist(Messages.projName));
    }


    @Test
    public void testCreateExistingProject() throws Exception {
        Utility.createProject(Messages.projName);
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        assertFalse("testCreateExistingProject",wabot.button("Finish").isEnabled());
        wabot.button("Cancel").click();
    }

    @Test
    public void testCreateProjAtGivenLoc() throws Exception {
        // create proj
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projAtOtherLoc);
        wabot.checkBox("Use default location").click();
        wabot.textWithLabel("Location:").setText(Messages.otherLoc);
        wabot.button("Finish").click();
        wabot.waitUntil(shellCloses(sh));
        wabot.sleep(10000);
        //verify
        SWTBotTreeItem proj1 = Utility.selProjFromExplorer(Messages.projAtOtherLoc);
        proj1.contextMenu("Properties").click();
        SWTBotShell shell1 = wabot.shell("Properties for " + Messages.projAtOtherLoc);
        shell1.activate();
        SWTBotTree properties = shell1.bot().tree();
        properties.getTreeItem("Resource").select();
        assertTrue("testCreateProjAtGivenLoc",wabot.textWithLabel("Location:").getText().equalsIgnoreCase(
                Messages.otherLoc + Messages.projAtOtherLoc));
        wabot.button("Cancel").click();
        Utility.selProjFromExplorer(Messages.projAtOtherLoc).select();
        Utility.deleteSelectedProject();
    }

    @Test
    public void testCreateProjAtGivenLocErr() throws Exception {
        // create proj
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projAtOtherLoc);
        wabot.checkBox("Use default location").click();
        wabot.textWithLabel("Location:").setText(Messages.nonExistingLoc);
        wabot.button("Finish").click();
        wabot.sleep(2000);
        //verify message
        SWTBotShell sh = wabot.shell(Messages.genericErrTtl).activate();
        String msg = sh.getText();
        wabot.button("OK").click();
        wabot.button("Cancel").click();
        assertEquals("testEmptyEpName", msg,
                Messages.genericErrTtl);
    }
    
    //Test Cases for version 1.7 starts from here.
    
    @Test
    //Test Case 140
    public void testEmptyProjectName() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText("");
        assertTrue("testEmptyProjectName", !wabot.button("< Back").isEnabled() &&
        		!wabot.button("Next >").isEnabled() && !wabot.button("Finish").isEnabled() &&
        		wabot.button("Cancel").isEnabled());
        sh.close();
    }
    
    @Test
    //Test Case 141
    public void testEnterProjectName() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        assertTrue("testEnterProjectName", !wabot.button("< Back").isEnabled() &&
        		wabot.button("Next >").isEnabled() && wabot.button("Finish").isEnabled() &&
        		wabot.button("Cancel").isEnabled());
        sh.close();
    }
    
    @Test
    //Test Case 142
    public void testNextClick() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        assertTrue("testNextClick", wabot.button("< Back").isEnabled() &&
        		wabot.button("Next >").isEnabled() && wabot.button("Finish").isEnabled() &&
        		wabot.button("Cancel").isEnabled() && wabot.checkBox(Messages.jdkChkBxTxt).isEnabled());
        sh.close();
    }
    
    @Test
    //Test Case 143
    public void testBackClick() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        wabot.button("< Back").click();
        assertTrue("testBackClick", !wabot.button("< Back").isEnabled() &&
        		wabot.button("Next >").isEnabled() && wabot.button("Finish").isEnabled() &&
        		wabot.button("Cancel").isEnabled());
        sh.close();
    }
    
    @Test
    //Test Case 144
    public void testJdkChecked() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        // Finish is enabled due to auto detect JDK directory
        assertTrue("testJdkChecked", wabot.button("Next >").isEnabled() &&
        		wabot.button(Messages.debugBrowseBtn).isEnabled() && 
        		wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).isEnabled()&&
        		wabot.checkBox(Messages.srvChkBxTxt).isEnabled() && wabot.button("Finish").isEnabled());
        sh.close();
    }
    
    @Test
    //Test Case 145
    public void testJdkCheckUncheck() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        boolean isEnabled = wabot.button("Next >").isEnabled() &&
        		wabot.button("Browse...").isEnabled() && 
        		wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).isEnabled()&&
        		wabot.checkBox(Messages.srvChkBxTxt).isEnabled();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        boolean isDisabled = !wabot.button(Messages.debugBrowseBtn).isEnabled() && 
        		!wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).isEnabled()&&
        		!wabot.checkBox(Messages.srvChkBxTxt).isEnabled() && wabot.button("Finish").isEnabled();
        assertTrue("testJdkCheckUncheck", isEnabled && isDisabled);
        sh.close();
    }
    
    @Test
    //Test Case 147
    public void testValidJdkPath() throws Exception {
    	Utility.createProject(Messages.projName);
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.test);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).setText(Utility.getLoc(Messages.projName, "\\cert"));
        assertTrue("testValidJdkPath", wabot.button("Finish").isEnabled());
        sh.close();
    }
    
    @Test
    //Test Case 148
    public void testSerChecked() throws Exception {
    	Utility.createProject(Messages.projName);
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.test);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).setText(Utility.getLoc(Messages.projName, "\\cert"));
        wabot.checkBox(Messages.srvChkBxTxt).click();
        boolean isEnabled = wabot.comboBoxWithLabelInGroup(Messages.sel, Messages.srv).isEnabled() &&
        		wabot.textWithLabelInGroup(Messages.grpDir, Messages.srv).isEnabled() &&
        		wabot.link().isEnabled() && wabot.button(Messages.debugBrowseBtn).isEnabled() &&
        		wabot.table().isEnabled() && wabot.button(Messages.roleAddBtn).isEnabled() && !wabot.button(Messages.roleRemBtn).isEnabled() &&
        		!wabot.button("Finish").isEnabled();
        assertTrue("testSerChecked", isEnabled);
        sh.close();
    }
    
    @Test
    //Test Case 149
    public void testSerCheckUncheckValidJdk() throws Exception {
    	Utility.createProject(Messages.projName);
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.test);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).setText(Utility.getLoc(Messages.projName, "\\cert"));
        wabot.checkBox(Messages.srvChkBxTxt).click();
        boolean isEnabled = wabot.comboBoxWithLabelInGroup(Messages.sel, Messages.srv).isEnabled() &&
        		wabot.textWithLabelInGroup(Messages.grpDir, Messages.srv).isEnabled() &&
        		wabot.link().isEnabled() && wabot.button(Messages.debugBrowseBtn).isEnabled() &&
        		wabot.table().isEnabled() && wabot.button(Messages.roleAddBtn).isEnabled() && !wabot.button(Messages.roleRemBtn).isEnabled() &&
        		!wabot.button("Finish").isEnabled();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        boolean isDisabled = !wabot.comboBoxWithLabelInGroup(Messages.sel, Messages.srv).isEnabled() &&
        		!wabot.textWithLabelInGroup(Messages.grpDir, Messages.srv).isEnabled() &&
        		!wabot.link().isEnabled() && !wabot.table().isEnabled() && 
        		!wabot.button(Messages.roleAddBtn).isEnabled() && !wabot.button(Messages.roleRemBtn).isEnabled() &&
        		wabot.button("Finish").isEnabled();
        assertTrue("testSerCheckUncheckValidJdk", isEnabled && isDisabled);
        sh.close();
    }
    
    @Test
    //Test Case 151
    public void testValidSerJdkPath() throws Exception {
    	Utility.createProject(Messages.projName);
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.test);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).setText(Utility.getLoc(Messages.projName, "\\cert"));
        wabot.checkBox(Messages.srvChkBxTxt).click();
        wabot.textWithLabelInGroup(Messages.grpDir, Messages.srv).setText(Utility.getLoc(Messages.projName, "\\cert"));
        assertFalse("testValidSerJdkPath", wabot.button("Finish").isEnabled());
        sh.close();
    }
    
    @Test
    //Test Case 152
    public void testEmptySerPath() throws Exception {
    	Utility.createProject(Messages.projName);
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.test);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).setText(Utility.getLoc(Messages.projName, "\\cert"));
        wabot.checkBox(Messages.srvChkBxTxt).click();
        wabot.comboBoxWithLabelInGroup(Messages.sel, Messages.srv).setSelection(Messages.cmbValJet7);
        assertFalse("testEmptySerPath", wabot.button("Finish").isEnabled());
        sh.close();
    }
    
    @Test
    //Test Case 153
    public void testValidSerJdkData() throws Exception {
    	Utility.createProject(Messages.projName);
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.test);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).setText(Utility.getLoc(Messages.projName, "\\cert"));
        wabot.checkBox(Messages.srvChkBxTxt).click();
        wabot.comboBoxWithLabelInGroup(Messages.sel, Messages.srv).setSelection(Messages.cmbValJet7);
        wabot.textWithLabelInGroup(Messages.grpDir, Messages.srv).setText(Utility.getLoc(Messages.projName, "\\cert"));
        assertTrue("testValidSerJdkData", wabot.button("Finish").isEnabled());
        sh.close();
    }
    
    @Test
    //Test Case 154
    public void testJdkDirBlankSerValid() throws Exception {
    	Utility.createProject(Messages.projName);
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.test);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        wabot.comboBoxWithLabelInGroup(Messages.sel, Messages.srv).setSelection(Messages.cmbValJet7);
        wabot.textWithLabelInGroup(Messages.grpDir, Messages.srv).setText(Utility.getLoc(Messages.projName, "\\cert"));
        // Due to JDK path auto discovery, Finish button gets enabled.
        assertTrue("testJdkDirBlankSerValid", wabot.button("Finish").isEnabled());
        sh.close();
    }
    
    @Test
    //Test Case 155
    public void testCustLinkClick() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        wabot.link().click();
        SWTBotShell errShell = wabot.shell(Messages.custLinkTtl).activate();
        String msg = errShell.getText();
        errShell.close();
        assertTrue("testCustLinkClick", msg.equalsIgnoreCase(Messages.custLinkTtl));
        sh.close();
    }
    
    @Test
    //Test Case 156
    public void testCustLinkCancelClick() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        wabot.link().click();
        SWTBotShell errShell = wabot.shell(Messages.custLinkTtl).activate();
        String msg = errShell.getText();
        wabot.button("Cancel").click();
        assertTrue("testCustLinkCancelClick", msg.equalsIgnoreCase(Messages.custLinkTtl) &&
        		wabot.activeShell().getText().equalsIgnoreCase(Messages.newWAProjTtl));
        sh.close();
    }
    
    @Test
    //Test Case 157
    public void testCustLinkOkClick() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        wabot.link().click();
        SWTBotShell errShell = wabot.shell(Messages.custLinkTtl).activate();
        wabot.button("OK").click();
        wabot.waitUntil(shellCloses(errShell));
        assertTrue("testCustLinkOkClick", wabot.activeEditor().getTitle().equalsIgnoreCase(Messages.cmpntFile));
        wabot.activeEditor().close();
    }
    
    @Test
    //Test Case 158
    public void testAddClick() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell appShell = wabot.shell(Messages.addAppTtl).activate();
        String msg = appShell.getText();
        appShell.close();
        assertTrue("testAddClick", msg.equalsIgnoreCase(Messages.addAppTtl));
        sh.close();
    }
    
    @Test
    //Test Case 159
    public void testAddClickFirstRadioEnable() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        wabot.button(Messages.roleAddBtn).click();
        SWTBotShell appShell = wabot.shell(Messages.addAppTtl).activate();
        assertTrue("testAddClickFirstRadioEnable", wabot.radio(0).isSelected() && 
        		wabot.button(Messages.debugBrowseBtn).isEnabled() && !wabot.button("OK").isEnabled());
        appShell.close();
        sh.close();
    }
    
    @Test
    //Test Case 165
    public void testRmvBtnEnblDsbl() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        Boolean disVal = !wabot.button(Messages.roleRemBtn).isEnabled();
        wabot.table().select(0);
		Boolean enblVal = wabot.button(Messages.roleRemBtn).isEnabled();
		assertTrue("testRmvBtnEnblDsbl", disVal
				&& enblVal);
        sh.close();
    }
    
    @Test
    //Test Case 176
    public void testRmvBtnYesPressed() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        wabot.table().select(0);
		wabot.button(Messages.roleRemBtn).click();
		assertFalse("testRmvBtnYesPressed", wabot.table().containsItem(Messages.hlWrld));
        sh.close();
    }
    
    @Test
    //Test Case 177
    public void testAddAppCancelPressed() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        wabot.button(Messages.roleAddBtn).click();
        wabot.shell(Messages.addAppTtl).activate();
		wabot.button("Cancel").click();
		assertTrue("testAddAppCancelPressed", wabot.activeShell().getText().
				equalsIgnoreCase(Messages.newWAProjTtl));
        sh.close();
    }
    
    @Test
    //Test Case 178
    public void testWizNextCancelClick() throws Exception {
    	wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
    	wabot.textWithLabel("Project name:").setText(Messages.projName);
    	wabot.button("Next >").click();
    	wabot.checkBox(Messages.jdkChkBxTxt).click();
    	wabot.checkBox(Messages.srvChkBxTxt).click();
    	wabot.button("Cancel").click();
    	assertFalse("testWizNextCancelClick", Utility.isProjExist(Messages.newWaPrjName));
    }
    
    @Test
    //Test Case 232
    public void testInvalidJdkPath() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.projName);
        wabot.button("Next >").click();
        Boolean finishEnbl = wabot.button("Finish").isEnabled();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).setText(Messages.test);
        Boolean finishDsbl = !wabot.button("Finish").isEnabled();
        assertTrue("testInvalidJdkPath", finishEnbl 
        		&& finishDsbl); 
        sh.close();
    }
    
    @Test
    //Test Case 233
    public void testInvalidSrvPath() throws Exception {
    	Utility.createProject(Messages.projName);
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.test);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).
        setText(Utility.getLoc(Messages.projName, "\\cert"));
        Boolean finishEnbl = wabot.button("Finish").isEnabled();
        wabot.checkBox(Messages.srvChkBxTxt).click();
		wabot.comboBoxWithLabelInGroup(Messages.sel, Messages.srv).setSelection(Messages.cmbValJet7);
		wabot.textWithLabelInGroup(Messages.grpDir, Messages.srv).setText(Messages.test);
        Boolean finishDsbl = !wabot.button("Finish").isEnabled();
        assertTrue("testInvalidSrvPath", finishEnbl 
        		&& finishDsbl); 
        sh.close();
    }
    
    @Test
    //Test Case 256
    public void testUncheckSrvCheckApp() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.test);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        // Uncheck
        wabot.checkBox(Messages.srvChkBxTxt).click();
        Boolean valSrvDisable = !wabot.comboBoxWithLabelInGroup(Messages.sel,
				Messages.srv).isEnabled()
				&& !wabot.textWithLabelInGroup(Messages.grpDir, Messages.srv).isEnabled()
				&& !wabot.link().isEnabled()
				&& !wabot.table().isEnabled()
				&& !wabot.button(Messages.roleAddBtn).isEnabled()
				&& !wabot.button(Messages.roleRemBtn).isEnabled();
		Boolean appPresent = wabot.table().cell(0, 0).
				equalsIgnoreCase(Messages.hlWrld);
		assertTrue("testUncheckSrvCheckApp", valSrvDisable 
				&& appPresent);
        sh.close();
    }
    
    @Test
    //Test Case 257
    public void testUncheckJdkCheckApp() throws Exception {
        wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
        SWTBotShell sh = wabot.shell(Messages.newWAProjTtl).activate();
        wabot.textWithLabel("Project name:").setText(Messages.test);
        wabot.button("Next >").click();
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        wabot.checkBox(Messages.srvChkBxTxt).click();
        // Uncheck
        wabot.checkBox(Messages.jdkChkBxTxt).click();
        Boolean valJdkDisable = !wabot.button(Messages.debugBrowseBtn).isEnabled()
				&& !wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).isEnabled()
				&& !wabot.checkBox(Messages.srvChkBxTxt).isEnabled();
        Boolean valSrvDisable = !wabot.comboBoxWithLabelInGroup(Messages.sel,
				Messages.srv).isEnabled()
				&& !wabot.textWithLabelInGroup(Messages.grpDir, Messages.srv).isEnabled()
				&& !wabot.link().isEnabled()
				&& !wabot.table().isEnabled()
				&& !wabot.button(Messages.roleAddBtn).isEnabled()
				&& !wabot.button(Messages.roleRemBtn).isEnabled();
		Boolean appPresent = wabot.table().cell(0, 0).
				equalsIgnoreCase(Messages.hlWrld);
		assertTrue("testUncheckSrvCheckApp", valJdkDisable
				&& valSrvDisable 
				&& appPresent);
        sh.close();
    }
    
    @Test
    //Test Case 262
    public void testCheckHlwrldApp() throws Exception {
    	wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
    	wabot.shell(Messages.newWAProjTtl).activate();
    	wabot.textWithLabel("Project name:").setText(Messages.projName);
    	wabot.button("Next >").click();
    	Boolean appPresent = wabot.table().cell(0, 0).
    			equalsIgnoreCase(Messages.hlWrld);
    	wabot.button("Finish").click();
    	SWTBotShell propShell = Utility.
    			selSerConfUsingCnxtMenu(Messages.projName);
    	Boolean appSrvConfPresent = wabot.table().cell(0, 0).
    			equalsIgnoreCase(Messages.hlWrld);
    	Utility.selCmpntPage();
    	Boolean cmpntPresent = wabot.table().cell(0, 2).
    			equalsIgnoreCase(Messages.hlWrld);
    	assertTrue("testCheckHlwrldApp", appPresent
    			&& appSrvConfPresent 
    			&& cmpntPresent);
    	propShell.close();
    }
    
    @Test
    //Test Case 263
    public void testCheckHlwrldAppWithJdk() throws Exception {
    	Utility.createProject(Messages.test);
    	wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
    	wabot.shell(Messages.newWAProjTtl).activate();
    	wabot.textWithLabel("Project name:").setText(Messages.projName);
    	wabot.button("Next >").click();
    	Boolean appPresent = wabot.table().cell(0, 0).
    			equalsIgnoreCase(Messages.hlWrld);
    	wabot.checkBox(Messages.jdkChkBxTxt).click();
    	wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).
    	setText(Utility.getLoc(Messages.test, "\\cert"));
    	wabot.button("Finish").click();
    	SWTBotShell propShell = Utility.
    			selSerConfUsingCnxtMenu(Messages.projName);
    	Boolean appSrvConfPresent = wabot.table().cell(0, 0).
    			equalsIgnoreCase(Messages.hlWrld);
    	Utility.selCmpntPage();
    	Boolean cmpntPresent = wabot.table().cell(1, 2).
    			equalsIgnoreCase(Messages.hlWrld);
    	assertTrue("testCheckHlwrldAppWithJdk", appPresent
    			&& appSrvConfPresent 
    			&& cmpntPresent);
    	propShell.close();
    	Utility.selProjFromExplorer(Messages.test).select();
    	Utility.deleteSelectedProject();
    }
    
    @Test
    //Test Case 264
    public void testCheckHlwrldAppWithJdkSrv() throws Exception {
    	Utility.createProject(Messages.test);
    	wabot.menu("File").menu("New").menu(Messages.newWaPrjName).click();
    	wabot.shell(Messages.newWAProjTtl).activate();
    	wabot.textWithLabel("Project name:").setText(Messages.projName);
    	wabot.button("Next >").click();
    	Boolean appPresent = wabot.table().cell(0, 0).
    			equalsIgnoreCase(Messages.hlWrld);
    	wabot.checkBox(Messages.jdkChkBxTxt).click();
    	wabot.textWithLabelInGroup(Messages.grpDir, Messages.jdk).
    	setText(Utility.getLoc(Messages.test, "\\cert"));
    	wabot.checkBox(Messages.srvChkBxTxt).click();
    	wabot.comboBoxWithLabelInGroup(Messages.sel, Messages.srv).setSelection(Messages.cmbValJet7);
    	wabot.textWithLabelInGroup(Messages.grpDir, Messages.srv).
    	setText(Utility.getLoc(Messages.test, "\\cert"));
    	wabot.button("Finish").click(); 
    	SWTBotShell propShell = Utility.
    			selSerConfUsingCnxtMenu(Messages.projName);
    	Boolean appSrvConfPresent = wabot.table().cell(0, 0).
    			equalsIgnoreCase(Messages.hlWrld);
    	Utility.selCmpntPage();
    	Boolean cmpntPresent = wabot.table().cell(2, 2).
    			equalsIgnoreCase(Messages.hlWrld);
    	assertTrue("testCheckHlwrldAppWithJdkSrv", appPresent
    			&& appSrvConfPresent 
    			&& cmpntPresent);
    	propShell.close();
    	Utility.selProjFromExplorer(Messages.test).select();
    	Utility.deleteSelectedProject();
    }
}