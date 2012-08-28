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
public class WADebugConfigurationTest {
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
		if (Utility.isProjExist(Messages.roleProj)) {
			// delete existing project
			Utility.selProjFromExplorer(Messages.roleProj).select();
			Utility.deleteSelectedProject();
		}
		Utility.createProject(Messages.roleProj);
		Utility.getPropertyPage(Messages.roleProj, Messages.rolesPage);
	}

	@After
	public void cleanUp() throws Exception {
		if(Utility.isProjExist(Messages.roleProj)) {
			Utility.selProjFromExplorer(Messages.roleProj).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// test case 107
	public void testDebugPresent() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		assertTrue("testDebugPresent", wabot.checkBox(Messages.debugLabel)
				.isVisible());
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 108
	public void testEnableDebug() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		wabot.checkBox(Messages.debugLabel).click();
		assertTrue("testEnableDebug", wabot.label(Messages.dbgEndPtLbl)
				.isEnabled()
				&& wabot.button(Messages.dbgCreateBtn).isEnabled()
				&& wabot.checkBox(Messages.dbgJVMChkLbl).isEnabled());
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 109
	public void testEnableDebugCheck() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		wabot.checkBox(Messages.debugLabel).click();
		String dbgEndPt = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl)
				.getText();
		assertTrue("testEnableDebugCheck",
				dbgEndPt.equals(Messages.dbgEndptStr));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 110
	public void testEnableDisableDebug() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		Utility.disableDebugOption(Messages.roleProj);
		assertTrue("testEnableDisableDebug",
				!wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).isEnabled()
				&& !wabot.checkBox(Messages.debugLabel).isChecked());
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 111
	public void testDebugEnabledAlready() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button("OK").click();
		role = wabot.table().getTableItem(0).select().getText();
		shell = Utility.selectDebugPage(role);
		String dbgEndPt = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl)
				.getText();
		assertTrue("testDebugEnabledAlready",
				wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).isEnabled()
				&& wabot.checkBox(Messages.debugLabel).isChecked()
				&& wabot.comboBoxWithLabel(Messages.dbgEndPtLbl)
				.getText().equals(dbgEndPt));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 112
	public void testDbgDisabledEndPtExists() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button("OK").click();
		role = wabot.table().getTableItem(0).select().getText();
		shell = Utility.selectDebugPage(role);
		Utility.disableDebugOption(Messages.roleProj);
		wabot.shell(Messages.endPtAssoErrTtl).activate();
		wabot.button("No").click();
		wabot.button("OK").click();
		role = wabot.table().getTableItem(0).select().getText();
		shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		assertTrue("testDbgDisabledEndPtExists",
				wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).isEnabled()
				&& wabot.checkBox(Messages.debugLabel).isChecked()
				&& wabot.comboBoxWithLabel(Messages.dbgEndPtLbl)
				.getText().equals(Messages.dbgEndptStr1));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 113
	public void testDbgEnabledEndPtRemove() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button("OK").click();
		role = wabot.table().getTableItem(0).select().getText();
		shell = Utility.selectDebugPage(role);
		Utility.disableDebugOption(Messages.roleProj);
		SWTBotShell errShell = wabot.shell(Messages.endPtAssoErrTtl).activate();
		String msg = errShell.getText();
		wabot.button("Yes").click();
		assertTrue(
				"testDbgEnabledEndPtRemove",
				!wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).isEnabled()
				&& !wabot.checkBox(Messages.debugLabel).isChecked()
				&& !wabot.comboBoxWithLabel(Messages.dbgEndPtLbl)
				.isEnabled()
				&& msg.equals(Messages.endPtAssoErrTtl));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 114
	public void testDbgEnabledEndPtNotRemove() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button("OK").click();
		role = wabot.table().getTableItem(0).select().getText();
		shell = Utility.selectDebugPage(role);
		Utility.disableDebugOption(Messages.roleProj);
		wabot.shell(Messages.endPtAssoErrTtl).activate();
		wabot.button("No").click();
		SWTBotTree properties = shell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.activeShell().activate();
		String row = wabot.table().getTableItem(1).getText();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.dbgEndPtName).select();
		assertTrue("testDbgEnabledEndPtNotRemove",
				!wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).isEnabled()
				&& !wabot.checkBox(Messages.debugLabel).isChecked()
				&& row.equals(Messages.dbgEndPtName));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 115
	public void testDbgJVMChecked() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.checkBox(Messages.dbgJVMChkLbl).select();
		wabot.button("OK").click();
		role = wabot.table().getTableItem(0).select().getText();
		shell = Utility.selectDebugPage(role);
		assertTrue("testDbgJVMChecked", wabot.checkBox(Messages.dbgJVMChkLbl)
				.isChecked());
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 116
	public void testDbgJVMUnchecked() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.checkBox(Messages.dbgJVMChkLbl).select();
		wabot.button("OK").click();
		role = wabot.table().getTableItem(0).select().getText();
		shell = Utility.selectDebugPage(role);
		wabot.checkBox(Messages.dbgJVMChkLbl).click();
		wabot.button("OK").click();
		role = wabot.table().getTableItem(0).select().getText();
		shell = Utility.selectDebugPage(role);
		assertFalse("testDbgJVMUnchecked" , wabot.checkBox(Messages.dbgJVMChkLbl).isChecked());
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 117
	public void testCreateDebugConfClick() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button(Messages.dbgCreateBtn).click();
		wabot.shell(Messages.debugCnfTtl).activate();
		wabot.sleep(1000);
		assertTrue("testCreateDebugConfClick",
				wabot.textWithLabel(Messages.debugProjLbl).isEnabled()
				&& wabot.checkBox(Messages.debugEmuChkBox).isEnabled());
		wabot.shell(Messages.debugCnfTtl).close();
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 118
	public void testCreateDebugBrowseButton() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		Utility.createJavaProject(Messages.javaProjName);
		Utility.selProjFromExplorer(Messages.roleProj);
		Utility.getPropertyPage(Messages.roleProj, Messages.rolesPage);
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell1 = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button(Messages.dbgCreateBtn).click();
		wabot.shell(Messages.debugCnfTtl).activate();
		wabot.button(Messages.debugBrowseBtn).click();
		SWTBotShell shell = wabot.shell(Messages.projSelTtl).activate();
		String msg = shell.getText();
		shell.close();
		assertTrue("testCreateDebugBrowseButton",
				msg.equals(Messages.projSelTtl));
		wabot.shell(Messages.debugCnfTtl).close();
		shell1.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
		Utility.selProjFromExplorer(Messages.javaProjName).select();
		Utility.deleteSelectedProject();
	}

	@Test
	// test case 119
	public void testCreateDebugCloudCheckBoxChecked() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button(Messages.dbgCreateBtn).click();
		wabot.shell(Messages.debugCnfTtl).activate();
		wabot.checkBox(Messages.debugCldChkBox).select();
		assertTrue("testCreateDebugCloudCheckBoxChecked",
				wabot.textWithLabel(Messages.debugHostLbl).isEnabled());
		wabot.shell(Messages.debugCnfTtl).close();
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 120
	public void testCreateDebugCloudCheckBoxUnchecked() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button(Messages.dbgCreateBtn).click();
		wabot.shell(Messages.debugCnfTtl).activate();
		wabot.checkBox(Messages.debugCldChkBox).select();
		wabot.checkBox(Messages.debugCldChkBox).click();
		assertTrue("testCreateDebugCloudCheckBoxUnchecked", !wabot
				.textWithLabel(Messages.debugHostLbl).isEnabled());
		wabot.shell(Messages.debugCnfTtl).close();
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 121
	public void testCreateDebugNoValues() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button(Messages.dbgCreateBtn).click();
		wabot.shell(Messages.debugCnfTtl).activate();
		assertTrue("testCreateDebugNoValues", !wabot.button("OK").isEnabled());
		wabot.shell(Messages.debugCnfTtl).close();
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 122
	public void testCreateDebugBothCheckBoxesOff() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button(Messages.dbgCreateBtn).click();
		wabot.shell(Messages.debugCnfTtl).activate();
		wabot.checkBox(Messages.debugEmuChkBox).click();
		wabot.textWithLabel(Messages.debugProjLbl).setText(Messages.roleProj);
		assertTrue("testCreateDebugBothCheckBoxesOff", !wabot.button("OK")
				.isEnabled());
		wabot.shell(Messages.debugCnfTtl).close();
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 123
	public void testCreateDebugClocuCheckedNoHost() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button(Messages.dbgCreateBtn).click();
		wabot.shell(Messages.debugCnfTtl).activate();
		wabot.checkBox(Messages.debugCldChkBox).select();
		wabot.textWithLabel(Messages.debugProjLbl).setText(Messages.roleProj);
		assertTrue("testCreateDebugClocuCheckedNoHost", !wabot.button("OK")
				.isEnabled());
		wabot.shell(Messages.debugCnfTtl).close();
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 124
	public void testCreateDebugInvalidProject() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button(Messages.dbgCreateBtn).click();
		wabot.shell(Messages.debugCnfTtl).activate();
		wabot.textWithLabel(Messages.debugProjLbl).setText(Messages.roleProj);
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.dbgInvdProjTtl).activate();
		String msg = errShell.getText();
		wabot.button("OK").click();
		assertTrue("testCreateDebugInvalidProject",
				msg.equals(Messages.dbgInvdProjTtl));
		wabot.shell(Messages.debugCnfTtl).close();
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 125
	public void testCreateDebugValidValues() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		Utility.createJavaProject(Messages.javaProjName);
		Utility.selProjFromExplorer(Messages.roleProj);
		Utility.getPropertyPage(Messages.roleProj, Messages.rolesPage);
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button(Messages.dbgCreateBtn).click();
		wabot.shell(Messages.debugCnfTtl).activate();
		wabot.textWithLabel(Messages.debugProjLbl).setText(
				Messages.javaProjName);
		wabot.button("OK").click();
		SWTBotShell cnfShell = wabot.shell(Messages.debugCnfTtl).activate();
		String msg = cnfShell.getText();
		wabot.button("OK").click();
		assertTrue("testCreateDebugValidValues",
				msg.equals(Messages.debugCnfTtl));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
		Utility.selProjFromExplorer(Messages.javaProjName).select();
		Utility.deleteSelectedProject();
	}

	@Test
	// test case 127
	public void testDbgAssocEndPtNotRemove() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button("OK").click();
		role = wabot.table().getTableItem(0).select().getText();
		shell = Utility.selectDebugPage(role);
		Utility.disableDebugOption(Messages.roleProj);
		wabot.shell(Messages.endPtAssoErrTtl).activate();
		wabot.button("No").click();
		Utility.enableDebugOption(Messages.roleProj);
		SWTBotTree properties = shell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.activeShell().activate();
		wabot.table().click(2, 0);
		wabot.text(Messages.dbgEndPtName + 1, 0).setText(Messages.newEndPtName);
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.dbgEndPtName).select();
		assertTrue("testDbgAssocEndPtNotRemove",
				wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).getText()
				.startsWith(Messages.newEndPtName));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 128
	public void testDbgAssocEndPtRemove() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button("OK").click();
		role = wabot.table().getTableItem(0).select().getText();
		shell = Utility.selectDebugPage(role);
		SWTBotTree properties = shell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.activeShell().activate();
		wabot.table().select(1);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.delEndPtTtl).activate();
		wabot.button("Yes").click();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.dbgEndPtName).select();
		assertFalse(
				"testDbgAssocEndPtRemove",
				wabot.checkBox(Messages.debugLabel).isChecked());
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 135
	public void testDbgEndPtWithInputOnly() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		Utility.createProjWithEp(Messages.roleProjWithEndPt);
		Utility.getPropertyPage(Messages.roleProjWithEndPt, Messages.rolesPage);
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProjWithEndPt);
		String[] endpts = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList = Arrays.asList(endpts);
		assertFalse("testDbgEndPtWithInputOnly", endList.contains("InlEndPt"));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProjWithEndPt);
		Utility.selProjFromExplorer(Messages.roleProjWithEndPt).select();
		Utility.deleteSelectedProject();
	}

	@Test
	// test case 136
	public void testDbgEndPtChangeInputToInternal() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		Utility.createProjWithEp(Messages.roleProjWithEndPt);
		Utility.getPropertyPage(Messages.roleProjWithEndPt, Messages.rolesPage);
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProjWithEndPt);
		SWTBotTree properties = shell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.activeShell().activate();
		wabot.table().click(0, 1);
		wabot.ccomboBox().setSelection(1);
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.dbgEndPtName).select();
		String[] endpts = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList = Arrays.asList(endpts);
		assertFalse("testDbgEndPtChangeInputToInternal",
				endList.contains("IntEndPtEndPt (public:1,private:1)"));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProjWithEndPt);
		Utility.selProjFromExplorer(Messages.roleProjWithEndPt).select();
		Utility.deleteSelectedProject();
	}

	@Test
	// test case 137
	public void testDbgAssocEndPtChangeInputToInternal() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		SWTBotTree properties = shell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.activeShell().activate();
		wabot.table().click(1, 1);
		wabot.ccomboBox().setSelection(1);
		wabot.button(Messages.roleAddBtn).click();
		wabot.shell(Messages.dbgEpCnfTtl).activate();
		wabot.button("Yes").click();
		wabot.shell(Messages.addEpTtl).activate();
		wabot.button("Cancel").click();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.dbgEndPtName).select();
		assertFalse("testDbgAssocEndPtChangeInputToInternal",
				wabot.checkBox(Messages.debugLabel).isChecked());
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 138
	public void testDbgEndPtChangeInternalToInput() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		Utility.createProjWithEp(Messages.roleProjWithEndPt);
		Utility.getPropertyPage(Messages.roleProjWithEndPt, Messages.rolesPage);
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProjWithEndPt);
		SWTBotTree properties = shell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.activeShell().activate();
		wabot.table().click(1, 1);
		wabot.ccomboBox().setSelection(0);
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.dbgEndPtName).select();
		String[] endpts = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList = Arrays.asList(endpts);
		assertTrue("testDbgEndPtChangeInternalToInput",
				endList.contains("InlEndPt (public:3,private:3)"));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProjWithEndPt);
		Utility.selProjFromExplorer(Messages.roleProjWithEndPt).select();
		Utility.deleteSelectedProject();
	}

	@Test
	// test case 144
	public void testDbgRemoveOtherEndPoint() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		Utility.createProjWithEp(Messages.roleProjWithEndPt);
		Utility.getPropertyPage(Messages.roleProjWithEndPt, Messages.rolesPage);
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProjWithEndPt);
		SWTBotTree properties = shell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.activeShell().activate();
		wabot.table().select(0);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.delEndPtTtl).activate();
		wabot.button("Yes").click();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.dbgEndPtName).select();
		String[] endpts = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList = Arrays.asList(endpts);
		assertTrue("testDbgRemoveOtherEndPoint", !endList.contains("IntEndPt"));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProjWithEndPt);
		Utility.selProjFromExplorer(Messages.roleProjWithEndPt).select();
		Utility.deleteSelectedProject();
	}

	@Test
	// test case 145
	public void testDbgEnabledAddEndPt() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		SWTBotTree properties = shell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.activeShell().activate();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("a", "Internal", "11", "12");
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.dbgEndPtName).select();
		String[] endpts = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList = Arrays.asList(endpts);
		assertTrue("testDbgEnabledAddEndPt", !endList.contains("a"));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}

	@Test
	// test case 146
	public void testCrtDbgConfEmuAndCloud() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		Utility.createJavaProject(Messages.javaProjName);
		Utility.selProjFromExplorer(Messages.roleProj);
		Utility.getPropertyPage(Messages.roleProj, Messages.rolesPage);
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		wabot.button(Messages.dbgCreateBtn).click();
		wabot.shell(Messages.debugCnfTtl).activate();
		wabot.textWithLabel(Messages.debugProjLbl).setText(
				Messages.javaProjName);
		wabot.checkBox(Messages.debugCldChkBox).click();
		wabot.textWithLabel(Messages.debugHostLbl).setText("abc");
		wabot.button("OK").click();
		SWTBotShell cnfShell = wabot.shell(Messages.debugCnfTtl).activate();
		String msg = cnfShell.getText();
		wabot.button("OK").click();
		assertTrue("testCrtDbgConfEmuAndCloud",
				msg.equals(Messages.debugCnfTtl));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
		Utility.selProjFromExplorer(Messages.javaProjName).select();
		Utility.deleteSelectedProject();
	}

	@Test
	// test case 147
	public void testDbgEnabledAddEndPtCancelPressed() throws Exception {
		String role = wabot.table().getTableItem(0).select().getText();
		SWTBotShell shell = Utility.selectDebugPage(role);
		Utility.enableDebugOption(Messages.roleProj);
		SWTBotTree properties = shell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.activeShell().activate();
		wabot.button(Messages.roleAddBtn).click();
		wabot.shell(Messages.addEpTtl).activate();
		wabot.button("Cancel").click();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.dbgEndPtName).select();
		String[] endpts = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList = Arrays.asList(endpts);
		assertTrue("testDbgEnabledAddEndPtCancelPressed",
				!endList.contains("a"));
		shell.close();
		Utility.closeProjPropertyPage(Messages.roleProj);
	}


	@Test
	// (New Test Cases for 1.6) test case 2
	public void testDebugDialogPresent() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.roleProj);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = wabot.
				shell(String.format("%s%s%s", Messages.propPageTtl,
						" ", Messages.role1));
		propShell.activate();
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.dbgEndPtName).select();
		boolean debugVal1 = false;
		debugVal1 = !wabot.
				comboBoxWithLabel(Messages.dbgEndPtLbl).isEnabled()
				&& !wabot.button(Messages.dbgCreateBtn).isEnabled();
		wabot.checkBox(Messages.debugLabel).click();
		boolean debugVal2 = false;
		debugVal2 = wabot.
				comboBoxWithLabel(Messages.dbgEndPtLbl).getText().
				equals(Messages.dbgEndptStr)
				&& wabot.button(Messages.dbgCreateBtn).isEnabled();
		assertTrue("testDebugDialogPresent", debugVal1 && debugVal2);
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 120
	public void testDbgEndPtList() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.roleProj);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InternalTest", "Internal", "N/A", "166");
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.dbgEndPtName).select();
		Utility.enableDebugOption(Messages.roleProj);
		String[] endpts = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList = Arrays.asList(endpts);
		assertTrue("testDbgEndPtList", endList.contains(Messages.dbgEndptStr)
				&& endList.contains(Messages.httpEndPtStr)
				&& endList.contains(Messages.instcEndPtStr)
				&& !endList.contains("InternalTest"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 121
	public void testDbgEndPtListInstcToInput() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.roleProj);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.dbgEndPtName).select();
		Utility.enableDebugOption(Messages.roleProj);
		String[] endpts = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList = Arrays.asList(endpts);
		Boolean val1 = endList.contains(Messages.instcEndPtStr);
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		// Change type to input
		wabot.table().click(1, 1);
		wabot.ccomboBox().setSelection("Input");
		wabot.table().click(0, 0);
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.dbgEndPtName).select();
		String[] endpts1 = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList1 = Arrays.asList(endpts1);
		assertTrue("testDbgEndPtListInstcToInput", val1
				&& endList1.contains(Messages.dbgEndptStr)
				&& endList1.contains(Messages.httpEndPtStr)
				&& endList1.contains(Messages.instcMdfdStr));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 122
	public void testDbgEndPtListInstcToInternal() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.roleProj);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.dbgEndPtName).select();
		Utility.enableDebugOption(Messages.roleProj);
		String[] endpts = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList = Arrays.asList(endpts);
		Boolean val1 = endList.contains(Messages.instcEndPtStr);
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		// Change type to internal
		wabot.table().click(1, 1);
		wabot.ccomboBox().setSelection("Internal");
		wabot.table().click(0, 0);
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.dbgEndPtName).select();
		String[] endpts1 = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList1 = Arrays.asList(endpts1);
		assertTrue("testDbgEndPtListInstcToInternal", val1
				&& !endList1.contains("InstncTest"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 123
	public void testDbgEndPtListInputToInstc() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.roleProj);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selDebugPage();
		Utility.enableDebugOption(Messages.roleProj);
		String[] endpts = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList = Arrays.asList(endpts);
		Boolean val1 = endList.contains(Messages.httpEndPtStr);
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		// Change type to instance
		wabot.table().click(0, 1);
		wabot.ccomboBox().setSelection(Messages.typeInstnc);
		wabot.table().click(0, 0);
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.dbgEndPtName).select();
		String[] endpts1 = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList1 = Arrays.asList(endpts1);
		assertTrue("testDbgEndPtListInputToInstc", val1
				&& endList1.contains("http (public:80-80,private:8080)"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 124
	public void testDbgEndPtListIntToInstc() throws Exception {
		Utility.closeProjPropertyPage(Messages.roleProj);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.roleProj);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InternalTest", "Internal", "N/A", "166");
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.dbgEndPtName).select();
		Utility.enableDebugOption(Messages.roleProj);
		String[] endpts = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList = Arrays.asList(endpts);
		Boolean val1 = !endList.contains("InternalTest");
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		// Change type to internal
		wabot.table().click(1, 1);
		wabot.ccomboBox().setSelection(Messages.typeInstnc);
		wabot.table().click(0, 0);
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.dbgEndPtName).select();
		String[] endpts1 = wabot.comboBoxWithLabel(Messages.dbgEndPtLbl).items();
		List<String> endList1 = Arrays.asList(endpts1);
		assertTrue("testDbgEndPtListIntToInstc", val1
				&& endList1.contains("InternalTest (public:166-166,private:166)"));
		propShell.close();
	}

}
