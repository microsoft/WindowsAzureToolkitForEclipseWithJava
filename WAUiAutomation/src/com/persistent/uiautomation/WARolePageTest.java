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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
public class WARolePageTest {

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
		if(Utility.isProjExist(Messages.projWithEp)) {
			//delete existing project
			Utility.selProjFromExplorer(Messages.projWithEp).select();
			Utility.deleteSelectedProject();
		}
		Utility.createProjWithEp(Messages.projWithEp);
		wabot.sleep(1000);
		Utility.getPropertyPage(Messages.projWithEp, Messages.rolesPage);
	}

	@After
	public void cleanUp() throws Exception {
		if(Utility.isProjExist(Messages.projWithEp)) {
			Utility.selProjFromExplorer(Messages.projWithEp).select();
			Utility.deleteSelectedProject();
		}
	}

	private static SWTBotShell openRoleWinFromPropPage(String roleName) {
		SWTBotShell shRole = Utility.selectGeneralPage(roleName);
		return shRole;
	}

	@Test
	public void testWaViewRole() throws Exception {
		assertEquals("testWaViewRole", Messages.role1, wabot.table().getTableItem(0).getText());
		wabot.sleep(1000);
		SWTBotShell sh = wabot.activeShell();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh));
	}

	@Test
	public void testWaEditDisable() throws Exception {
		assertFalse("testWaEditDisable",wabot.button(Messages.roleEditBtn).isEnabled());
		SWTBotShell sh = wabot.activeShell();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh));
	}

	@Test
	public void testWaRemoveDisable() throws Exception {
		assertFalse("testWaRemoveDisable",wabot.button(Messages.roleRemBtn).isEnabled());
		SWTBotShell sh = wabot.activeShell();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh));    }

	@Test
	public void testWaEditEnable() throws Exception {
		wabot.table().getTableItem(0).select();
		assertTrue("testWaEditEnable",wabot.button(Messages.roleEditBtn).isEnabled());
		SWTBotShell sh = wabot.activeShell();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh));
	}

	@Test
	public void testWaRemoveEnable() throws Exception {
		assertFalse("testWaRemoveEnable",wabot.button(Messages.roleRemBtn).isEnabled());
		SWTBotShell sh = wabot.activeShell();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh));
	}

	@Test
	public void testEditRoleName() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		wabot.textWithLabel(Messages.roleNameLbl).setText(Messages.waRole);
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(shRole));
		assertEquals("testEditRoleName", Messages.waRole,
				wabot.table().getTableItem(1).getText());
		SWTBotShell sh = wabot.activeShell();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh));
	}

	@Test
	public void testEditVmSize() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		wabot.comboBoxWithLabel(Messages.vmSizeLbl).setSelection("Large");
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(shRole));
		assertEquals("testEditVmSize", "Large", wabot.table().cell(1,1));
		SWTBotShell sh = wabot.activeShell();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh));
	}

	@Test
	public void testEditInstances() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		wabot.textWithLabel(Messages.instance).setFocus();
		wabot.textWithLabel(Messages.instance).setText("3");
		wabot.button("OK").setFocus();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(shRole));
		assertEquals("testEditInstances", "3",
				wabot.table().getTableItem(1).getText(2));
		SWTBotShell sh = wabot.activeShell();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh));
	}

	@Test
	public void testEditInputEpPort() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		//Change Public Port
		wabot.table().click(0, 2);
		wabot.text("1", 0).setText("33");
		//Change Private Port
		wabot.table().click(0, 3);
		wabot.text("1", 0).setText("44");
		wabot.button(Messages.roleAddBtn).setFocus();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(shRole));
		//verify
		role = wabot.table().getTableItem(1).select().getText();
		shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties1 = shRole.bot().tree();
		properties1.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		assertEquals("testEditEpInpToInt public port", "33",
				wabot.table().cell(0, 2));
		assertEquals("testEditEpInpToInt private port", "44",
				wabot.table().cell(0, 3));
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(shRole));
		SWTBotShell sh = wabot.activeShell();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh));
	}

	@Test
	public void testEditInternalEpPort() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		//Change Public Port
		wabot.table().click(1, 3);
		wabot.text("3", 0).setText("33");
		//click on other cell to save the changes from previous cell
		wabot.button(Messages.roleAddBtn).setFocus();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(shRole));
		//verify
		role= wabot.table().getTableItem(1).select().getText();
		shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties1 = shRole.bot().tree();
		properties1.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		assertEquals("testEditInternalEpPort private port", "33",
				wabot.table().cell(1, 3));
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(shRole));
		SWTBotShell sh = wabot.activeShell();
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh));
	}

	@Test
	public void testPublicPortOfInternalEp() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		assertEquals("testPublicPortToInternalEp", "N/A",
				wabot.table().getTableItem(1).getText(2));
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(shRole));
		wabot.button("OK").click();
	}

	@Test
	public void testEditEpInpToInt() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.activeShell().activate();
		wabot.table().click(0, 1);
		wabot.ccomboBox().setSelection(1);
		wabot.button("OK").click();
		//verify
		role = wabot.table().getTableItem(1).select().getText();
		shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties1 = shRole.bot().tree();
		properties1.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		assertEquals("testEditEpInpToInt", "Internal",
				wabot.table().getTableItem(1).getText(1));
		wabot.button("OK").click(); //Edit Role dialog box
		wabot.waitUntil(shellCloses(shRole));
		wabot.button("Cancel").click(); //Role property dialog box
	}

	@Test
	public void testEditEpIntEpToInput() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.table().click(1, 1);
		wabot.ccomboBox().setSelection(0);
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(shRole));
		//verify
		role = wabot.table().getTableItem(1).select().getText();
		shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties1 = shRole.bot().tree();
		properties1.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		assertEquals("testEditEpIntEpToInput", "Input",
				wabot.table().getTableItem(0).getText(1));
		wabot.button("OK").click();  //Edit Role dialog box
		wabot.waitUntil(shellCloses(shRole));
		wabot.button("OK").click(); //Role property dialog box
	}


	@Test
	public void testInvalidEndpointName() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("IntEndPt", "Input", "11", "13");
		SWTBotShell sh = wabot.shell(Messages.invalidEpName).activate();
		String msg = sh.getText();
		wabot.button("OK").click();
		wabot.button("Cancel").click();
		shRole.close();
		assertTrue("testInvalidEndpointName ", msg.equals(Messages.invalidEpName));
		wabot.shell("Properties for " + Messages.projWithEp).close();
	}

	@Test
	public void testEmptyEndpointName() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("", "Input", "11", "13");
		SWTBotShell sh = wabot.shell(Messages.invalidEpName).activate();
		String msg = sh.getText();
		wabot.button("OK").click();
		wabot.button("Cancel").click();
		shRole.close();
		assertTrue("testEmptyEndpointName", msg.equals(Messages.invalidEpName));
		wabot.shell("Properties for " + Messages.projWithEp).close();
	}


	@Test
	public void testInvalidEndpointPort() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("NewEp", "Input", "test", "13");
		SWTBotShell sh = wabot.shell(Messages.invalidEpPort).activate();
		String msg = sh.getText();
		wabot.button("OK").click();
		wabot.button("Cancel").click();
		shRole.close();
		assertTrue("testInvalidEndpointPort", msg.equals(Messages.invalidEpPort));
		wabot.shell("Properties for " + Messages.projWithEp).close();
	}

	@Test
	public void testWithExistingEndpointPort() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("NewEp", "Input", "1", "1");
		SWTBotShell sh = wabot.shell(Messages.invalidEpPort).activate();
		String msg = sh.getText();
		wabot.button("OK").click();
		wabot.button("Cancel").click();
		shRole.close();
		assertTrue("testInvalidEndpointPort", msg.equals(Messages.invalidEpPort));
		wabot.shell("Properties for " + Messages.projWithEp).close();
	}

	@Test
	public void testVmSizeValues() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).select();
		String[] val = wabot.comboBoxWithLabel(Messages.vmSizeLbl).items();
		String[] expectedVal = { "ExtraLarge", "Large", "Medium", "Small",
		"ExtraSmall"};
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(shRole));
		assertArrayEquals("testVmSizeValues", expectedVal, val);
		wabot.shell("Properties for " + Messages.projWithEp).close();
	}

	@Test
	public void testInvalidRolename() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).select();
		wabot.textWithLabel(Messages.roleTxtLbl).setText("");
		SWTBotShell sh = wabot.shell(Messages.invalidRoleName).activate();
		String msg = sh.getText();
		wabot.button("OK").click();
		wabot.button("Cancel").click();
		wabot.waitUntil(shellCloses(shRole));
		assertEquals("testEditEpInstances", msg,
				Messages.invalidRoleName);
		wabot.shell("Properties for " + Messages.projWithEp).close();
	}


	@Test
	public void testInvalidEpName() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.table().click(0, 0);
		wabot.text("IntEndPt").typeText("InlEndPt");
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell sh = wabot.shell(Messages.invalidEpName).activate();
		String msg = sh.getText();
		wabot.button("OK").click();
		wabot.shell(Messages.addEpTtl).activate();
		wabot.button("Cancel").click();
		wabot.button("OK").click();
		wabot.button("OK").click();
		assertEquals("testEditEpInstances", msg,
				Messages.invalidEpName);
	}


	@Test
	public void testEmptyEpName() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotTree properties = openRoleWinFromPropPage(role).bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.table().click(1, 0);
		wabot.text("InlEndPt").setText("");
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell sh = wabot.shell(Messages.invalidEpName).activate();
		String msg = sh.getText();
		wabot.button("OK").click();
		wabot.button("Cancel").click();
		wabot.button("Cancel").click();
		assertEquals("testEmptyEpName", msg,
				Messages.invalidEpName);
		wabot.shell("Properties for " + Messages.projWithEp).close();
	}


	@Test
	public void testEditExistingEpPort() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		//Change Public Port
		wabot.table().click(1, 3);
		wabot.text("3", 0).typeText("1");
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell sh = wabot.shell(Messages.invalidEpPort).activate();
		String msg = sh.getText();
		wabot.button("OK").click();
		wabot.button("Cancel").click();
		wabot.button("Cancel").click();
		assertEquals("testEmptyEpName", msg,
				Messages.invalidEpPort);
		wabot.shell("Properties for " + Messages.projWithEp).close();
	}


	@Test
	public void testRemoveEndpoint() throws Exception {
		String role = wabot.table().getTableItem(1).select().getText();
		SWTBotShell shRole = openRoleWinFromPropPage(role);
		SWTBotTree properties = shRole.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand()
		.getNode(Messages.endptPage).select();
		wabot.table().getTableItem(1).select();
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.delEndPtTtl).activate();
		wabot.button("Yes").click();
		assertFalse("testRemoveEndpoint", wabot.table()
				.containsItem("InlEndPt"));
		shRole.close();
		wabot.button("Cancel").click();
	}

	@Test
	public void testRemoveRole() throws Exception {
		wabot.table().getTableItem(1).select();
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.delRoleTtl).activate();
		wabot.button("Yes").click();
		assertFalse("testRemoveEndpoint", wabot.table()
				.containsItem("WorkerRole2"));
		wabot.button("Cancel").click();

	}


	@Test
	// (New Test Cases for 1.6) test case 1
	public void testRoleDialogPresent() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = wabot.
				shell(Messages.propPageTtl + " " +Messages.role1);
		propShell.activate();
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).select();
		assertTrue("testRoleDialogPresent", wabot.
				label(Messages.roleNameLbl).isVisible()
				&& wabot.label(Messages.vmSizeLbl).isVisible()
				&& wabot.label(Messages.instance).isVisible()
				&& wabot.textWithLabel(Messages.roleNameLbl).
				getText().equals(Messages.role1)
				&& wabot.comboBoxWithLabel(Messages.vmSizeLbl).
				getText().equals("Small")
				&& wabot.textWithLabel(Messages.instance).
				getText().equals("1"));
		propShell.close();
	}


	@Test
	// (New Test Cases for 1.6) test case 3
	public void testEndPtPagePresent() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role2).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = wabot.
				shell(Messages.propPageTtl + " " +Messages.role2);
		propShell.activate();
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.endptPage).select();
		assertTrue("testEndPtPagePresent", wabot.table().
				cell(0, 0).equals("IntEndPt")
				&& wabot.table().cell(0, 1).equals("Input")
				&& wabot.table().cell(0, 2).equals("1")
				&& wabot.table().cell(0, 3).equals("1")
				&& wabot.table().
				cell(1, 0).equals("InlEndPt")
				&& wabot.table().cell(1, 1).equals("Internal")
				&& wabot.table().cell(1, 2).equals("N/A")
				&& wabot.table().cell(1, 3).equals("3"));
		propShell.close();
	}


	@Test
	// (New Test Cases for 1.6) test case 6
	// Same as (New Test Cases for 1.6) test case 1
	public void testRoleDialogOpen() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = wabot.
				shell(Messages.propPageTtl + " " +Messages.role1);
		propShell.activate();
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).select();
		assertTrue("testRoleDialogPresent", wabot.
				label(Messages.roleNameLbl).isVisible()
				&& wabot.label(Messages.vmSizeLbl).isVisible()
				&& wabot.label(Messages.instance).isVisible()
				&& wabot.textWithLabel(Messages.roleNameLbl).
				getText().equals(Messages.role1)
				&& wabot.comboBoxWithLabel(Messages.vmSizeLbl).
				getText().equals("Small")
				&& wabot.textWithLabel(Messages.instance).
				getText().equals("1"));
		propShell.close();
	}


	@Test
	// (New Test Cases for 1.6) test case 8
	public void testEditDialogPresent() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role2).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = wabot.
				shell(Messages.propPageTtl + " " +Messages.role2);
		propShell.activate();
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.endptPage).select();
		wabot.table().select(0);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell editShell = wabot.shell(Messages.endPtEditTtl);
		editShell.activate();
		String confirmMsg = editShell.getText();
		assertTrue("testEditDialogPresent",
				confirmMsg.equals(Messages.endPtEditTtl));
		editShell.close();
		propShell.close();
	}


	@Test
	// (New Test Cases for 1.6) test case 9
	public void testEditEndPtOkPressed() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role2).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = wabot.
				shell(Messages.propPageTtl + " " +Messages.role2);
		propShell.activate();
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.endptPage).select();
		wabot.table().select(0);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell editShell = wabot.shell(Messages.endPtEditTtl);
		editShell.activate();
		wabot.comboBox().setSelection("Internal");
		wabot.button("OK").click();
		assertTrue("testEditEndPtOkPressed", wabot.table().
				cell(0, 2).equals("N/A")
				&& wabot.table().cell(0, 1).equals("Internal"));
		propShell.close();
	}


	@Test
	// (New Test Cases for 1.6) test case 10
	public void testEditEndPtCancelPressed() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role2).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = wabot.
				shell(Messages.propPageTtl + " " +Messages.role2);
		propShell.activate();
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).expand().
		getNode(Messages.endptPage).select();
		wabot.table().select(0);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell editShell = wabot.shell(Messages.endPtEditTtl);
		editShell.activate();
		wabot.comboBox().setSelection("Internal");
		wabot.button("Cancel").click();
		assertTrue("testEditEndPtCancelPressed", wabot.table().
				cell(0, 2).equals("1")
				&& wabot.table().cell(0, 1).equals("Input"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 102
	public void testInstncEndPtEntryInTable() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.table().click(0, 1);
		String[] endPtType = wabot.ccomboBox().items();
		List<String> typeList = Arrays.asList(endPtType);
		assertTrue("testInstncEndPtEntryInTable", typeList.contains(Messages.typeInstnc));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 103
	public void testPublicPortTable() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.table().click(0, 1);
		wabot.ccomboBox().setSelection(Messages.typeInstnc);
		wabot.table().click(0, 0);
		assertTrue("testPublicPortTable", wabot.table().cell(0, 2).contains("-"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 104
	public void testInstncEndPtEntryInDlg() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell shEp = wabot.shell(Messages.addEpTtl);
		shEp.activate();
		String[] endPtType = wabot.comboBox().items();
		List<String> typeList = Arrays.asList(endPtType);
		assertTrue("testInstncEndPtEntryInDlg", typeList.contains(Messages.typeInstnc));
		shEp.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 105
	public void testPublicPortDlgRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
		assertTrue("testPublicPortDlg", wabot.table().cell(1, 2).contains("13-15"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 106
	public void testPublicPortDlgNtRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13", "16");
		assertTrue("testPublicPortDlgNtRange", wabot.table().cell(1, 2).contains("13-13"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 107
	public void testPrvtPortCnstr() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell shEp = wabot.shell(Messages.addEpTtl);
        shEp.activate();
        wabot.textWithLabel("Name:").setText("InstncTest");
        wabot.textWithLabel("Public port:").setText("13-16");
        wabot.comboBox().setSelection(Messages.typeInstnc);
        assertTrue("testPrvtPortCnstr", wabot.textWithLabel("Private port:").
        		getText().equalsIgnoreCase("13"));
        shEp.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 108
	public void testInputToInstanceDlg() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.table().select(0);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell shEp = wabot.shell(Messages.editEpTtl);
		shEp.activate();
		wabot.comboBox().setSelection(Messages.typeInstnc);
		wabot.button("OK").click();
		assertTrue("testInputToInstanceDlg", wabot.table().
				cell(0, 1).contains(Messages.typeInstnc)
				&& wabot.table().cell(0, 2).contains("80-80"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 109
	public void testInternalToInstanceDlg() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InternalPt", "Internal", "N/A", "16");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell shEp = wabot.shell(Messages.editEpTtl);
		shEp.activate();
		wabot.comboBox().setSelection(Messages.typeInstnc);
		wabot.button("OK").click();
		assertTrue("testInternalToInstanceDlg", wabot.table().
				cell(1, 1).contains(Messages.typeInstnc)
				&& wabot.table().cell(1, 2).contains("16-16"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 110
	public void testInputToInstanceTable() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.table().click(0, 1);
		wabot.ccomboBox().setSelection(Messages.typeInstnc);
		wabot.table().click(0, 0);
		assertTrue("testInputToInstanceTable", wabot.table().
				cell(0, 1).contains(Messages.typeInstnc)
				&& wabot.table().cell(0, 2).contains("80-80"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 111
	public void testInternalToInstanceTable() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InternalPt", "Internal", "N/A", "16");
		wabot.table().click(1, 1);
		wabot.ccomboBox().setSelection(Messages.typeInstnc);
		wabot.table().click(0, 0);
		assertTrue("testInternalToInstanceTable", wabot.table().
				cell(1, 1).contains(Messages.typeInstnc)
				&& wabot.table().cell(1, 2).contains("16-16"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 112
	public void testInstanceToInputDlg() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell shEp = wabot.shell(Messages.editEpTtl);
		shEp.activate();
		wabot.comboBox().setSelection("Input");
		wabot.button("OK").click();
		assertTrue("testInstanceToInputDlg", wabot.table().
				cell(1, 1).contains("Input")
				&& wabot.table().cell(1, 2).contains("13"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 113
	public void testInstanceToInternalDlg() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell shEp = wabot.shell(Messages.editEpTtl);
		shEp.activate();
		wabot.comboBox().setSelection("Internal");
		wabot.button("OK").click();
		assertTrue("testInstanceToInternalDlg", wabot.table().
				cell(1, 1).contains("Internal")
				&& wabot.table().cell(1, 2).contains("N/A"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 114
	public void testInPlaceInstanceToInput() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
		wabot.table().click(1, 1);
		wabot.ccomboBox().setSelection("Input");
		wabot.table().click(0, 0);
		assertTrue("testInPlaceInstanceToInput", wabot.table().
				cell(1, 1).contains("Input")
				&& wabot.table().cell(1, 2).contains("13"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 115
	public void testInPlaceInstanceToInternal() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
		wabot.table().click(1, 1);
		wabot.ccomboBox().setSelection("Internal");
		wabot.table().click(0, 0);
		assertTrue("testInPlaceInstanceToInternal", wabot.table().
				cell(1, 1).contains("Internal")
				&& wabot.table().cell(1, 2).contains("N/A"));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 116
	public void testSamePrtPortIntrnl() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InternalPt", "Internal", "N/A", "16");
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		assertTrue("testSamePrtPortIntrnl",
				errMsg.equals(Messages.invalidEpPort));
		// Cancel Add End Point shell
		wabot.shell(Messages.addEpTtl).activate();
		wabot.button("Cancel").click();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 117
	public void testSamePblPort() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "79-81", "16");
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		assertTrue("testSamePblPort",
				errMsg.equals(Messages.invalidEpPort));
		wabot.shell(Messages.addEpTtl).activate();
		wabot.button("Cancel").click();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 118
	public void testInPlaceEditSamePrtPortIntrl() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InternalPt", "Internal", "N/A", "16");
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "26");
		wabot.table().click(2, 3);
	    wabot.text("26", 0).typeText("16");
	    wabot.button(Messages.roleAddBtn).click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditSamePrtPortIntrl",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 119
	public void testInPlaceEditSamePblPort() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
		wabot.table().click(1, 2);
	    wabot.text("13-15", 0).typeText("79-81");
	    wabot.button(Messages.roleAddBtn).click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditSamePblPort",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 126
	public void testAddOvrlpngPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "16");
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest1", Messages.typeInstnc, "8-12", "17");
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
		wabot.shell(Messages.addEpTtl).activate();
        wabot.button("Cancel").click();
        assertTrue("testAddOvrlpngPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 127
	public void testInPlaceEditOvrlpngPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "16");
		wabot.table().click(0, 1);
		wabot.ccomboBox().setSelection(Messages.typeInstnc);
		wabot.table().click(0, 0);
		wabot.table().click(0, 2);
	    wabot.text("80-80", 0).typeText("8-12");
	    wabot.button(Messages.roleAddBtn).click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditOvrlpngPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 128
	public void testEditOvrlpngPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "16");
		wabot.table().select(0);
		wabot.button(Messages.roleEditBtn).click();
		wabot.shell(Messages.editEpTtl).activate();
		wabot.comboBox().setSelection(Messages.typeInstnc);
		wabot.textWithLabel("Public port:").setText("8-12");
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
        wabot.button("Cancel").click();
        assertTrue("testEditOvrlpngPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 129
	public void testEditSamePblPort() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "16");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		wabot.shell(Messages.editEpTtl).activate();
		wabot.comboBox().setSelection(Messages.typeInstnc);
		wabot.textWithLabel("Public port:").setText("80");
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
        wabot.button("Cancel").click();
        assertTrue("testEditSamePblPort",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 130
	public void testAddMax_MinPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "10-6", "16");
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
		wabot.shell(Messages.addEpTtl).activate();
        wabot.button("Cancel").click();
        assertTrue("testAddMax_MinPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 131
	public void testInPlaceEditMax_MinPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "16");
		wabot.table().click(1, 2);
	    wabot.text("5-10", 0).typeText("10-6");
	    wabot.button(Messages.roleAddBtn).click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditMax_MinPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 132
	public void testEditMax_MinPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "16");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		wabot.shell(Messages.editEpTtl).activate();
		wabot.textWithLabel("Public port:").setText("10-6");
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
        wabot.button("Cancel").click();
        assertTrue("testEditMax_MinPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 133
	public void testAddNegPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "-10-6", "16");
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
		wabot.shell(Messages.addEpTtl).activate();
        wabot.button("Cancel").click();
        assertTrue("testAddNegPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 134
	public void testInPlaceEditNegPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "16");
		wabot.table().click(1, 2);
	    wabot.text("5-10", 0).typeText("-10-6");
	    wabot.button(Messages.roleAddBtn).click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditNegPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 135
	public void testEditNegPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "16");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		wabot.shell(Messages.editEpTtl).activate();
		wabot.textWithLabel("Public port:").setText("-10-6");
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
        wabot.button("Cancel").click();
        assertTrue("testEditNegPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 136
	public void testAddCharPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "a-6", "16");
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
		wabot.shell(Messages.addEpTtl).activate();
        wabot.button("Cancel").click();
        assertTrue("testAddNegPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 137
	public void testInPlaceEditCharPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "16");
		wabot.table().click(1, 2);
	    wabot.text("5-10", 0).typeText("a-6");
	    wabot.button(Messages.roleAddBtn).click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
        wabot.button("Cancel").click();
        assertTrue("testInPlaceEditCharPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 138
	public void testEditCharPblPortRange() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "16");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		wabot.shell(Messages.editEpTtl).activate();
		wabot.textWithLabel("Public port:").setText("a-6");
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
        wabot.button("Cancel").click();
        assertTrue("testEditCharPblPortRange",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 139
	public void testEditPrvPubOverlap() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "12-15", "16");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		wabot.shell(Messages.editEpTtl).activate();
		wabot.textWithLabel("Public port:").setText("12-16");
		wabot.textWithLabel("Private port:").setText("17");
		wabot.button("OK").click();
		// Again change endpoint
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		wabot.shell(Messages.editEpTtl).activate();
		wabot.textWithLabel("Public port:").setText("12-17");
		wabot.textWithLabel("Private port:").setText("17");
		wabot.button("OK").click();
        assertTrue("testEditPrvPubOverlap", wabot.table().
				cell(1, 2).contains("12-17")
				&& wabot.table().cell(1, 3).contains("17"));
		propShell.close();
	}

	// Test case fails error comes need to fix in library
	//@Test
	// (New Test Cases for 1.7) test case 167
	public void testEditPubPrtMin() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", "Input", "12", "12");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		wabot.shell(Messages.editEpTtl).activate();
		wabot.comboBox().setSelection(Messages.typeInstnc);
		wabot.textWithLabel("Public port:").setText("10-12");
		wabot.textWithLabel("Private port:").setText("12");
		wabot.button("OK").click();
		// Again change endpoint
        assertTrue("testEditPubPrtMin", wabot.table().
				cell(1, 2).contains("10-12")
				&& wabot.table().cell(1, 3).contains("12"));
		propShell.close();
	}

	// Test case fails error comes need to fix in library
	//@Test
	// (New Test Cases for 1.7) test case 168
	public void testEditPubPrtMax() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", "Input", "12", "12");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		wabot.shell(Messages.editEpTtl).activate();
		wabot.comboBox().setSelection(Messages.typeInstnc);
		wabot.textWithLabel("Public port:").setText("12-16");
		wabot.button("OK").click();
		// Again change endpoint
		assertTrue("testEditPubPrtMax", wabot.table().
				cell(1, 2).contains("12-16")
				&& wabot.table().cell(1, 3).contains("12"));
		propShell.close();
	}
	
	@Test
	// (New Test Cases for 1.7) test case 252
	public void testSamePrtPortInput() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "8080");
		assertTrue("testSamePrtPortInput", wabot.table().cell(1, 3).
				equalsIgnoreCase("8080"));
		propShell.close();
	}
	
	@Test
	// (New Test Cases for 1.7) test case 253
	public void testInPlaceEditSamePrtPortInput() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "13-15", "16");
		wabot.table().click(1, 3);
		wabot.text("16", 0).typeText("8080");
		wabot.table().select(1);
		assertTrue("testInPlaceEditSamePrtPortInput", wabot.table().cell(1, 3).
				equalsIgnoreCase("8080"));
		propShell.close();
	}
	
	@Test
	// (New Test Cases for 1.7) test case 254
	public void testEditSamePrtPortInput() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "16");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		wabot.shell(Messages.editEpTtl).activate();
		wabot.comboBox().setSelection(Messages.typeInstnc);
		wabot.textWithLabel("Private port:").setText("8080");
		wabot.button("OK").click();
        assertTrue("testEditSamePrtPortInput", wabot.table().
        		cell(1, 3).equalsIgnoreCase("8080"));
		propShell.close();
	}
	
	@Test
	// (New Test Cases for 1.7) test case 255
	public void testEditSamePrtPortIntrl() throws Exception {
		Utility.closeProjPropertyPage(Messages.projWithEp);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithEp);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1).select();
		workerRoleNode.contextMenu("Properties").click();
		SWTBotShell propShell = Utility.selEndPtPage();
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InternalPt", "Internal", "N/A", "16");
		wabot.button(Messages.roleAddBtn).click();
		Utility.addEp("InstncTest", Messages.typeInstnc, "5-10", "26");
		wabot.table().select(2);
		wabot.button(Messages.roleEditBtn).click();
		wabot.shell(Messages.editEpTtl).activate();
		wabot.comboBox().setSelection(Messages.typeInstnc);
		wabot.textWithLabel("Private port:").setText("16");
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.invalidEpPort);
		errShell.activate();
		String errMsg = errShell.getText();
		errShell.close();
		// Cancel Add End Point shell
		wabot.button("Cancel").click();
		assertTrue("testEditSamePrtPortIntrl",
				errMsg.equals(Messages.invalidEpPort));
		propShell.close();
	}
}
