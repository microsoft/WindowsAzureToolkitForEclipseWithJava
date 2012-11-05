/**
 * Copyright 2012 Persistent Systems Ltd.
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

public class WAComponentTest {
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
		if (Utility.isProjExist(Messages.projWithCmpnt)) {
			// delete existing project
			Utility.selProjFromExplorer(Messages.projWithCmpnt).select();
			Utility.deleteSelectedProject();
		}
	}

	@After
	public void cleanUp() throws Exception {
		if (Utility.isProjExist(Messages.projWithCmpnt)) {
			Utility.selProjFromExplorer(Messages.projWithCmpnt).select();
			Utility.deleteSelectedProject();
		}
	}
	
	@Test
	// (New Test Cases for 1.7) test case 1
	public void testCmpntPropertyPagePresent() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		assertTrue("testCmpntPropertyPagePresent",
				wabot.table().isEnabled()
				&& wabot.button(Messages.roleAddBtn).isEnabled());
		propShell.close();
	}

	//@Test
	// (New Test Cases for 1.7) test case 2
	// Re check
	public void testCmpntTableNotEditable() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		// Try to edit component
		String oldVal = wabot.table().getTableItem(1).getText();
		wabot.table().doubleClick(1, 0);

		//wabot.text(oldVal, 1).getText();
		//setText("ValToCheck");
		assertTrue("testCmpntTableNotEditable", wabot.table().cell(1, 0).equals(oldVal));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 3
	public void testAddBtnPresent() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		assertTrue("testAddBtnPresent",
				wabot.button(Messages.roleAddBtn).isEnabled());
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 4
	public void testAddBtnPressed() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		boolean value = false;
		value = wabot.label(Messages.frmPathLbl).isVisible()
				&& wabot.label(Messages.asNameLbl).isVisible()
				&& wabot.label(Messages.toDirLbl).isVisible()
				&& wabot.textWithLabel(Messages.frmPathLbl).isEnabled()
				&& wabot.textWithLabel(Messages.asNameLbl).isEnabled()
				&& wabot.textWithLabel(Messages.toDirLbl).isEnabled()
				&& wabot.textWithLabel(Messages.toDirLbl).
				getText().equals(Messages.aprootPath)
				&& wabot.button("OK").isEnabled()
				&& wabot.button("Cancel").isEnabled();
		assertTrue("testAddBtnPressed",value);
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 5
	public void testImpDplyMethodsAddBtn() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		String[] impMthds = wabot.comboBox(0).items();
		List<String> impMthdList = Arrays.asList(impMthds);
		Boolean impMthdVal = impMthdList.contains(Messages.cmbValJar)
				&& impMthdList.contains(Messages.cmbValWar)
				&& impMthdList.contains(Messages.cmbValEar)
				&& impMthdList.contains(Messages.cmbValCopy)
				&& impMthdList.contains(Messages.cmbValZip)
				&& impMthdList.contains(Messages.cmbValNone);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValUnzip)
				&& dplyMthdList.contains(Messages.cmbValNone)
				&& dplyMthdList.contains(Messages.cmbValExec);
		assertTrue("testImpDplyMethodsAddBtn", impMthdVal
				&& dplyMthdVal
				&& wabot.comboBox(0).getText().equalsIgnoreCase(Messages.cmbValJar)
				&& wabot.comboBox(1).getText().equalsIgnoreCase(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 6
	public void testAsNameFrmPathBlankAddBtn() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.invdValErTtl);
		errShell.activate();
		String errMsg = errShell.getText();
		assertTrue("testAsNameFrmPathBlankAddBtn",
				errMsg.equals(Messages.invdValErTtl));
		errShell.close();
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 7
	public void testFrmPathToDirBlankAddBtn() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.asNameLbl).setText("test.jar");
		wabot.button("OK").click();
		assertTrue("testFrmPathToDirBlankAddBtn", wabot.table().
				cell(1, 1).equalsIgnoreCase(Messages.aprootPath)
				&& wabot.table().
				cell(1, 4).equalsIgnoreCase(Messages.aprootPath));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 8
	public void testAaNameBlankAddBtn() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.textWithLabel(Messages.toDirLbl).setFocus();
		wabot.button("OK").click();
		assertTrue("testAaNameBlankAddBtn", wabot.table().
				cell(1, 2).equalsIgnoreCase(""));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 9
	public void testWrkspcePressed() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		assertTrue("testWrkspcePressed", wabot.button("Cancel").isEnabled()
				&& wabot.button("OK").isVisible());
		projSelShell.close();
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 10
	public void testWrkspceOKPressed() throws Exception {
		Utility.createDynamicWebProj(Messages.projWAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.projWAR);
		wabot.button("OK").click();
		assertTrue("testWrkspceOKPressed", wabot.textWithLabel(Messages.frmPathLbl).
				getText().endsWith(Messages.projWAR));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projWAR)) {
			Utility.selProjFromExplorer(Messages.projWAR).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 11
	public void testWrkspceCancelPressed() throws Exception {
		Utility.createDynamicWebProj(Messages.projWAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.projWAR);
		wabot.button("Cancel").click();
		assertTrue("testWrkspceCancelPressed", wabot.
				textWithLabel(Messages.frmPathLbl).getText().isEmpty());
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projWAR)) {
			Utility.selProjFromExplorer(Messages.projWAR).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 12
	public void testWrkspceJARCopy() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		String[] impMthds = wabot.comboBox(0).items();
		List<String> impMthdList = Arrays.asList(impMthds);
		Boolean impMthdVal = impMthdList.contains(Messages.cmbValJar)
				&& impMthdList.contains(Messages.cmbValCopy);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone);
		assertTrue("testWrkspceJARCopy", impMthdVal
				&& dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValJar)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 13
	public void testWrkspceCopyCopy() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		assertTrue("testWrkspceCopyCopy", wabot.
				comboBox(0).getText().equals(Messages.cmbValCopy)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 15
	public void testRnameHlwrd() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.table().select(0);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntEdtShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntEdtShell.activate();
		wabot.textWithLabel(Messages.asNameLbl).setText("");
		wabot.textWithLabel(Messages.asNameLbl).typeText(Messages.hlWrldRnm);
		wabot.button("OK").click();
		wabot.button("OK").click();
		// Delay for rename operation
		wabot.sleep(1000);
		SWTBotTreeItem proj = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		assertTrue("testRnameHlwrd", approotCntnts.
				contains(Messages.hlWrldRnm));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 16
	public void testWrkspceWARCopy() throws Exception {
		Utility.createDynamicWebProj(Messages.projWAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.projWAR);
		wabot.button("OK").click();
		String[] impMthds = wabot.comboBox(0).items();
		List<String> impMthdList = Arrays.asList(impMthds);
		Boolean impMthdVal = impMthdList.contains(Messages.cmbValWar)
				&& impMthdList.contains(Messages.cmbValCopy);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone);
		assertTrue("testWrkspceWARCopy", impMthdVal
				&& dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValWar)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projWAR)) {
			Utility.selProjFromExplorer(Messages.projWAR).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 17
	public void testWrkspceEARCopy() throws Exception {
		Utility.createEnterpriseAppProj(Messages.projEAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.projEAR);
		wabot.button("OK").click();
		String[] impMthds = wabot.comboBox(0).items();
		List<String> impMthdList = Arrays.asList(impMthds);
		Boolean impMthdVal = impMthdList.contains(Messages.cmbValEar)
				&& impMthdList.contains(Messages.cmbValCopy);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone);
		assertTrue("testWrkspceEARCopy", impMthdVal
				&& dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValEar)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projEAR)) {
			Utility.selProjFromExplorer(Messages.projEAR).select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Client").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Connector").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "EJB").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Web").select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 18
	public void testWrkspceJARAsName() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testWrkspceJARAsName",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(String.format("%s%s%s", Messages.javaProjName,
						".", Messages.cmbValJar.toLowerCase())));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 19
	public void testWrkspceWARAsName() throws Exception {
		Utility.createDynamicWebProj(Messages.projWAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.projWAR);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testWrkspceWARAsName",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(String.format("%s%s%s", Messages.projWAR,
						".", Messages.cmbValWar.toLowerCase())));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projWAR)) {
			Utility.selProjFromExplorer(Messages.projWAR).select();
			Utility.deleteSelectedProject();
		}
	}
	
	@Test
	// (New Test Cases for 1.7) test case 20
	public void testWrkspceEARAsName() throws Exception {
		Utility.createEnterpriseAppProj(Messages.projEAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.projEAR);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testWrkspceEARAsName",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(String.format("%s%s%s", Messages.projEAR,
						".",Messages.cmbValEar.toLowerCase())));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projEAR)) {
			Utility.selProjFromExplorer(Messages.projEAR).select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Client").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Connector").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "EJB").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Web").select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 21
	public void testFileNoneExec() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt,
						String.format("%s%s%s", "\\", Messages.role1,
								Messages.startupFile)));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.comboBox(0).setSelection(Messages.cmbValNone);
		wabot.comboBox(1).setSelection(Messages.cmbValExec);
		assertTrue("testFileNoneExec", wabot.
				comboBox(0).getText().equals(Messages.cmbValNone)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValExec));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 22
	public void testAddOKPressed() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("OK").click();
		boolean val = wabot.table().cell(1, 0).
				equals(Messages.cmbValJar.toUpperCase())
				&& wabot.table().cell(1, 2).
				equals(String.format("%s%s%s", Messages.javaProjName,
						".", Messages.cmbValJar.toLowerCase()))
						&& wabot.table().cell(1, 3).
						equals(Messages.cmbValCopy.toLowerCase());
		assertTrue("testAddOKPressed", val);
		propShell.close();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 23
	public void testAddCancelPressed() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		cmpntShell.activate();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("Cancel").click();
		assertFalse("testAddCancelPressed", wabot.table()
				.containsItem(String.format("%s%s%s", Messages.javaProjName,
						".", Messages.cmbValJar.toLowerCase())));
		propShell.close();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 24
	public void testSameAsName() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		// Try to add same component with same As name
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.dplyNameErrTtl);
		errShell.activate();
		String errMsg = errShell.getText();
		assertTrue("testSameAsName",
				errMsg.equals(Messages.dplyNameErrTtl));
		errShell.close();
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 25
	public void testInvalidFrmPath() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl).activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText("InvalidFrmPath");
		wabot.textWithLabel(Messages.toDirLbl).setFocus();
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.frmPathErrTtl).activate();
		String msg  = errShell.getText();
		wabot.button("OK").click();
		assertTrue("testInvalidFrmPath",
				msg.equalsIgnoreCase(Messages.frmPathErrTtl)
				&& wabot.comboBox(0).getText().equalsIgnoreCase(Messages.cmbValNone)
				&& wabot.comboBox(1).getText().equalsIgnoreCase(Messages.cmbValNone));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 26
	public void testFileCopyCopy() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt,
						String.format("%s%s%s", "\\", Messages.role1,
								Messages.startupFile)));
		wabot.textWithLabel(Messages.toDirLbl).setFocus();
		String[] impMthds = wabot.comboBox(0).items();
		List<String> impMthdList = Arrays.asList(impMthds);
		Boolean impMthdVal = impMthdList.contains(Messages.cmbValCopy)
				&& impMthdList.contains(Messages.cmbValZip)
				&& impMthdList.contains(Messages.cmbValNone);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone)
				&& dplyMthdList.contains(Messages.cmbValExec);
		assertTrue("testFileCopyCopy", impMthdVal
				&& dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValCopy)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 27
	public void testFileZipUnzip() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt,
						String.format("%s%s%s", "\\", Messages.role1, Messages.startupFile)));
		wabot.comboBox(0).setSelection(Messages.cmbValZip);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone)
				&& dplyMthdList.contains(Messages.cmbValUnzip);
		assertTrue("testFileZipUnzip", dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValZip)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValUnzip));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 28
	public void testFileNone() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt,
						String.format("%s%s%s", "\\", Messages.role1, Messages.startupFile)));
		wabot.comboBox(0).setSelection(Messages.cmbValNone);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone);
		assertTrue("testFileNone", dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValNone)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValNone));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 29
	public void testFileCopyExec() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt,
						String.format("%s%s%s", "\\", Messages.role1, Messages.startupFile)));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		wabot.comboBox(1).setSelection(Messages.cmbValExec);
		assertTrue("testFileCopyExec", wabot.
				comboBox(0).getText().equals(Messages.cmbValCopy)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValExec));
		cmpntShell.close();
		propShell.close();
	}
	
	@Test
	// (New Test Cases for 1.7) test case 30
	public void testFileNotExec() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\package.xml"));
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		String[] endpts = wabot.comboBox(1).items();
		List<String> endList = Arrays.asList(endpts);
//		assertTrue("testFileNotExec", !endList.contains(Messages.cmbValExec));
		//According to user feed back, all the options are allowed.
		assertTrue("testFileNotExec", endList.contains(Messages.cmbValExec));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 31
	public void testDirZipUnzip() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		String[] impMthds = wabot.comboBox(0).items();
		List<String> impMthdList = Arrays.asList(impMthds);
		Boolean impMthdVal = impMthdList.contains(Messages.cmbValCopy)
				&& impMthdList.contains(Messages.cmbValZip)
				&& impMthdList.contains(Messages.cmbValNone);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone)
				&& dplyMthdList.contains(Messages.cmbValUnzip);
		assertTrue("testDirZipUnzip", impMthdVal
				&& dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValZip)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValUnzip));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 32
	public void testDirCopyCopy() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone);
		assertTrue("testDirCopyCopy", dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValCopy)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 33
	public void testDirNoneCopy() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.comboBox(0).setSelection(Messages.cmbValNone);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone);
		assertTrue("testDirNoneCopy", dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValNone)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValNone));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 34
	public void testEditRmvDisable() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		assertTrue("testEditRmvDisable",
				!wabot.button(Messages.roleEditBtn).isEnabled()
				&& !wabot.button(Messages.roleRemBtn).isEnabled());
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 35
	public void testEditRmvEnable() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(0);
		assertTrue("testEditRmvEnable",
				wabot.button(Messages.roleEditBtn).isEnabled()
				&& wabot.button(Messages.roleRemBtn).isEnabled());
		propShell.close();
	}
	
	@Test
	// (New Test Cases for 1.7) test case 36
	public void testEditPressed() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		wabot.sleep(1000);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.sleep(1000);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.sleep(1000);
		String impMethod = wabot.table().cell(1, 0);
		String frmPath =  wabot.table().cell(1, 1);
		String asName = wabot.table().cell(1, 2);
		String dplyMethod = wabot.table().cell(1, 3);
		String toDir = wabot.table().cell(1, 4);
		wabot.sleep(1000);
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		wabot.sleep(1000);
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.sleep(1000);
		boolean val = wabot.textWithLabel(Messages.frmPathLbl).getText().equals(frmPath)
				&& wabot.textWithLabel(Messages.asNameLbl).getText().equals(asName)
				&& wabot.textWithLabel(Messages.toDirLbl).getText().equals(toDir)
				&& wabot.comboBox(1).getText().equals(dplyMethod)
				&& wabot.comboBox(0).getText().equals(impMethod);
		assertTrue("testEditPressed", val);
		wabot.sleep(1000);
		wabot.button("OK").click();
		wabot.sleep(1000);
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 37
	public void testFrmPathAsNameBlankEditBtn() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText("");
		wabot.textWithLabel(Messages.asNameLbl).setText("");
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.invdValErTtl);
		errShell.activate();
		String errMsg = errShell.getText();
		assertTrue("testFrmPathAsNameBlankEditBtn",
				errMsg.equals(Messages.invdValErTtl));
		errShell.close();
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 38
	public void testFrmPathToDirBlankEditBtn() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText("");
		wabot.textWithLabel(Messages.toDirLbl).setText("");
		wabot.button("OK").click();
		assertTrue("testFrmPathToDirBlankEditBtn", wabot.table().
				cell(1, 1).equalsIgnoreCase(Messages.aprootPath)
				&& wabot.table().
				cell(1, 4).equalsIgnoreCase(Messages.aprootPath));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 39
	public void testAsNameBlankEditBtn() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.asNameLbl).setText("");
		wabot.button("OK").click();
		assertTrue("testAsNameBlankEditBtn", wabot.table().
				cell(1, 2).equalsIgnoreCase(""));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 40
	public void testEditHlwrd() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.table().select(0);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntEdtShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntEdtShell.activate();
		wabot.textWithLabel(Messages.asNameLbl).setText("");
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.invdValErTtl);
		errShell.activate();
		String errMsg = errShell.getText();
		assertTrue("testEditHlwrd",
				errMsg.equals(Messages.invdValErTtl));
		errShell.close();
		cmpntEdtShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 41
	public void testWrkspcePressedEditBtn() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		assertTrue("testWrkspcePressedEditBtn", wabot.button("Cancel").isEnabled()
				&& wabot.button("OK").isVisible());
		projSelShell.close();
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 42
	public void testWrkspceOKPressedEditBtn() throws Exception {
		Utility.createDynamicWebProj(Messages.projWAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		wabot.activeShell().bot().table().select(Messages.projWAR);
		wabot.button("OK").click();
		assertTrue("testWrkspceOKPressedEditBtn", wabot.
				textWithLabel(Messages.frmPathLbl).
				getText().endsWith(Messages.projWAR));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projWAR)) {
			Utility.selProjFromExplorer(Messages.projWAR).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 43
	public void testWrkspceCancelPressedEditBtn() throws Exception {
		Utility.createDynamicWebProj(Messages.projWAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		wabot.activeShell().bot().table().select(Messages.projWAR);
		wabot.button("Cancel").click();
		assertTrue("testWrkspceCancelPressedEditBtn", wabot.
				textWithLabel(Messages.frmPathLbl).getText().
				equals(String.format("%s%s%s", Messages.basePath,
						Messages.projWithCmpnt, "\\cert")));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projWAR)) {
			Utility.selProjFromExplorer(Messages.projWAR).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 44
	public void testEditDirWrkspceJAR() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		String[] impMthds = wabot.comboBox(0).items();
		List<String> impMthdList = Arrays.asList(impMthds);
		Boolean impMthdVal = impMthdList.contains(Messages.cmbValJar)
				&& impMthdList.contains(Messages.cmbValCopy);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone);
		assertTrue("testEditDirWrkspceJAR", impMthdVal
				&& dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValJar)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy)
				&& wabot.textWithLabel(Messages.frmPathLbl).
				getText().endsWith(Messages.javaProjName));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 45
	public void testEditDirWrkspceWAR() throws Exception {
		Utility.createDynamicWebProj(Messages.projWAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		wabot.activeShell().bot().table().select(Messages.projWAR);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		String[] impMthds = wabot.comboBox(0).items();
		List<String> impMthdList = Arrays.asList(impMthds);
		Boolean impMthdVal = impMthdList.contains(Messages.cmbValWar)
				&& impMthdList.contains(Messages.cmbValCopy);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone);
		assertTrue("testEditDirWrkspceWAR", impMthdVal
				&& dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValWar)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy)
				&& wabot.textWithLabel(Messages.frmPathLbl).
				getText().endsWith(Messages.projWAR));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projWAR)) {
			Utility.selProjFromExplorer(Messages.projWAR).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 46
	public void testEditDirWrkspceEAR() throws Exception {
		Utility.createEnterpriseAppProj(Messages.projEAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		wabot.activeShell().bot().table().select(Messages.projEAR);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		String[] impMthds = wabot.comboBox(0).items();
		List<String> impMthdList = Arrays.asList(impMthds);
		Boolean impMthdVal = impMthdList.contains(Messages.cmbValEar)
				&& impMthdList.contains(Messages.cmbValCopy);
		String[] dplyMthds = wabot.comboBox(1).items();
		List<String> dplyMthdList = Arrays.asList(dplyMthds);
		Boolean dplyMthdVal = dplyMthdList.contains(Messages.cmbValCopy)
				&& dplyMthdList.contains(Messages.cmbValNone);
		assertTrue("testEditDirWrkspceEAR", impMthdVal
				&& dplyMthdVal
				&& wabot.comboBox(0).getText().equals(Messages.cmbValEar)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy)
				&& wabot.textWithLabel(Messages.frmPathLbl).
				getText().endsWith(Messages.projEAR));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projEAR)) {
			Utility.selProjFromExplorer(Messages.projEAR).select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Client").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Connector").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "EJB").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Web").select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 47
	public void testEditDirFile() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText("");
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\package.xml"));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testEditDirFile", wabot.
				comboBox(0).getText().equals(Messages.cmbValCopy)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 48
	public void testEditFileWrkspceJAR() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\package.xml");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testEditFileWrkspceJAR", wabot.
				comboBox(0).getText().equals(Messages.cmbValJar)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 49
	public void testEditFileWAR() throws Exception {
		Utility.createDynamicWebProj(Messages.projWAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\package.xml");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		wabot.activeShell().bot().table().select(Messages.projWAR);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testEditFileWAR", wabot.
				comboBox(0).getText().equals(Messages.cmbValWar)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projWAR)) {
			Utility.selProjFromExplorer(Messages.projWAR).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 50
	public void testEditFileEAR() throws Exception {
		Utility.createEnterpriseAppProj(Messages.projEAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\package.xml");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		wabot.activeShell().bot().table().select(Messages.projEAR);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testEditFileEAR", wabot.
				comboBox(0).getText().equals(Messages.cmbValEar)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projEAR)) {
			Utility.selProjFromExplorer(Messages.projEAR).select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Client").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Connector").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "EJB").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Web").select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 51
	public void testEditFileDir() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\package.xml");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText("");
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testEditFileDir", wabot.
				comboBox(0).getText().equals(Messages.cmbValZip)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValUnzip));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 52
	public void testEditWrkspcDir() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		wabot.shell(Messages.cmpntAddTtl).activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("OK").click();
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText("");
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testEditWrkspcDir", wabot.
				comboBox(0).getText().equals(Messages.cmbValZip)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValUnzip));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 53
	public void testEditWrkspcFile() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		wabot.shell(Messages.cmpntAddTtl).activate();
		wabot.button(Messages.wrkSpcButton).click();
		SWTBotShell projSelShell = wabot.shell(Messages.projSelTtl);
		projSelShell.activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("OK").click();
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText("");
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\package.xml"));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testEditWrkspcFile", wabot.
				comboBox(0).getText().equals(Messages.cmbValCopy)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValCopy));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 54
	public void testEditOKPressed() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\package.xml");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText("");
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.textWithLabel(Messages.asNameLbl).setText("");
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		String impMethod = wabot.comboBox(0).getText();
		String asName = wabot.textWithLabel(Messages.asNameLbl).getText();
		String dplyMethod = wabot.comboBox(1).getText();
		String toDir = wabot.textWithLabel(Messages.toDirLbl).getText();
		wabot.button("OK").click();
		boolean val = wabot.table().cell(1, 0).equals(impMethod)
				&& wabot.table().cell(1, 1).equals(String.
						format("%s%s%s", Messages.basePath,
								Messages.projWithCmpnt, "\\cert"))
								&& wabot.table().cell(1, 2).equals(asName)
								&& wabot.table().cell(1, 3).equals(dplyMethod)
								&& wabot.table().cell(1, 4).equals(toDir);
		assertTrue("testEditOKPressed", val);
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 55
	public void testEditCancelPressed() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\package.xml");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText("");
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("Cancel").click();
		assertFalse("testEditCancelPressed", wabot.table()
				.containsItem(String.
						format("%s%s%s", Messages.basePath,
								Messages.projWithCmpnt, "\\cert")));
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 56
	public void testEditSameAsName() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\package.xml");
		Utility.addCmpnt(Messages.projWithCmpnt, "cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText("");
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.textWithLabel(Messages.asNameLbl).setText("");
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.dplyNameErrTtl);
		errShell.activate();
		String errMsg = errShell.getText();
		assertTrue("testEditSameAsName",
				errMsg.equals(Messages.dplyNameErrTtl));
		errShell.close();
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 57
	public void testEditInvalidFrmPath() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText("");
		wabot.textWithLabel(Messages.frmPathLbl).typeText("InvalidFrmPath");
		wabot.textWithLabel(Messages.toDirLbl).setFocus();
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.frmPathErrTtl);
		errShell.activate();
		boolean val = errShell.getText().equals(Messages.frmPathErrTtl);
		errShell.close();
		assertTrue("testEditInvalidFrmPath", val
				&& wabot.comboBox(0).getText().equals(Messages.cmbValNone)
				&& wabot.comboBox(1).getText().equals(Messages.cmbValNone));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 58
	public void testEditCopyToZipFile() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		wabot.shell(Messages.cmpntAddTtl).activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt,
						String.format("%s%s%s", "//", Messages.role1,
								Messages.startupFile)));
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.comboBox(1).setSelection(Messages.cmbValExec);
		wabot.button("OK").click();
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.comboBox(0).setSelection(Messages.cmbValZip);
		boolean val1 = wabot.comboBox(1).getText().equals(Messages.cmbValUnzip);
		assertTrue("testEditCopyToZipFile", val1);
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 59
	public void testRemovePressed() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		wabot.sleep(1000);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.sleep(1000);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.sleep(1000);
		wabot.table().select(1);
		wabot.sleep(1000);
		wabot.button(Messages.roleRemBtn).click();
		wabot.sleep(1000);
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntRemTtl);
		wabot.sleep(1000);
		cmpntShell.activate();
		wabot.sleep(1000);
		String confirmMsg = cmpntShell.getText();
		wabot.sleep(1000);
		assertTrue("testRemovePressed",
				confirmMsg.equals(Messages.cmpntRemTtl)
				&& wabot.button("Yes").isEnabled()
				&& wabot.button("No").isEnabled());
		wabot.sleep(1000);
		cmpntShell.close();
		wabot.sleep(1000);
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.6) test case 60
	public void testRemoveNoPressed() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		wabot.sleep(1000);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.sleep(1000);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.sleep(1000);
		wabot.table().select(1);
		wabot.button(Messages.roleRemBtn).click();
		wabot.sleep(1000);
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntRemTtl);
		cmpntShell.activate();
		wabot.sleep(1000);
		wabot.button("No").click();
		wabot.sleep(1000);
		assertTrue("testRemoveNoPressed", wabot.table().
				cell(1, 1).equals(String.format("%s%s%s", Messages.basePath,
						Messages.projWithCmpnt, "\\cert")));
		wabot.sleep(1000);
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.6) test case 61
	public void testRemoveYesPressed() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		wabot.sleep(1000);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.sleep(1000);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.sleep(1000);
		wabot.table().select(1);
		wabot.button(Messages.roleRemBtn).click();
		wabot.sleep(1000);
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntRemTtl);
		cmpntShell.activate();
		wabot.button("Yes").click();
		wabot.sleep(1000);
		assertFalse("testRemoveYesPressed", wabot.table().
				containsItem(String.format("%s%s%s", Messages.basePath,
						Messages.projWithCmpnt, "\\cert")));
		wabot.sleep(1000);
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 62
	public void testExportAsJAR() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("OK").click();
		wabot.button("OK").click();
		// Require sleep otherwise JAR is not created correctly
		wabot.sleep(5000);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		assertTrue("testExportAsJAR", approotCntnts.
				contains(String.format("%s%s%s", Messages.javaProjName,
						".", Messages.cmbValJar.toLowerCase())));
		// Remove component
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.table().select(1);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.cmpntRemTtl).activate();
		wabot.button("Yes").click();
		wabot.button("OK").click();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 63
	public void testExportAsWAR() throws Exception {
		Utility.createDynamicWebProj(Messages.projWAR);
		Utility.createProject(Messages.projWithCmpnt);
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.projWAR);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("OK").click();
		wabot.button("OK").click();
		// Require sleep otherwise WAR is not created correctly
		wabot.sleep(5000);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		assertTrue("testExportAsWAR", approotCntnts.
				contains(String.format("%s%s%s", Messages.projWAR,
						".", Messages.cmbValWar.toLowerCase())));
		// Remove component
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.table().select(1);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.cmpntRemTtl).activate();
		wabot.button("Yes").click();
		wabot.button("OK").click();
		if (Utility.isProjExist(Messages.projWAR)) {
			Utility.selProjFromExplorer(Messages.projWAR).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 64
	public void testExportAsEAR() throws Exception {
		Utility.createEnterpriseAppProj(Messages.projEAR);
		Utility.createProject(Messages.projWithCmpnt);
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.projEAR);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("OK").click();
		wabot.button("OK").click();
		// Require sleep otherwise EAR is not created correctly
		wabot.sleep(5000);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		assertTrue("testExportAsEAR", approotCntnts.
				contains(String.format("%s%s%s", Messages.projEAR,
						".", Messages.cmbValEar.toLowerCase())));
		// Remove component
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.table().select(1);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.cmpntRemTtl).activate();
		wabot.button("Yes").click();
		wabot.button("OK").click();
		if (Utility.isProjExist(Messages.projEAR)) {
			Utility.selProjFromExplorer(Messages.projEAR).select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Client").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Connector").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "EJB").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Web").select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 65
	public void testRemCmpntImpMthdAuto() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("OK").click();
		wabot.button("OK").click();
		// Require sleep otherwise JAR is not created correctly
		wabot.sleep(5000);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj1.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		Boolean valFilePresent = approotCntnts.
				contains(String.format("%s%s%s", Messages.javaProjName,
						".", Messages.cmbValJar.toLowerCase()));
		// Remove component
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.table().select(1);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.cmpntRemTtl).activate();
		wabot.button("Yes").click();
		wabot.button("OK").click();
		SWTBotTreeItem proj2 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode1 = proj2.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode1 = workerRoleNode1.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts1 = appRootNode1.expand().getNodes();
		Boolean valFileDelete = !approotCntnts1.
				contains(String.format("%s%s%s", Messages.javaProjName,
						".", Messages.cmbValJar.toLowerCase()));
		assertTrue("testRemCmpntImpMthdAuto", valFilePresent
				&& valFileDelete);
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 66
	public void testRmvCmpntImpSrcAprot() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.table().select(0);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.cmpntRemTtl).activate();
		wabot.button("Yes").click();
		SWTBotShell cmpntSrcShell = wabot.shell(Messages.cmpntSrcRmvTtl);
		cmpntSrcShell.activate();
		String errMsg = cmpntSrcShell.getText();
		assertTrue("testRmvCmpntImpSrcAprot",
				errMsg.equals(Messages.cmpntSrcRmvTtl));
		cmpntSrcShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 67
	public void testRmvCmpntImpSrcAprotYes() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.table().select(0);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.cmpntRemTtl).activate();
		wabot.button("Yes").click();
		SWTBotShell cmpntSrcShell = wabot.shell(Messages.cmpntSrcRmvTtl);
		cmpntSrcShell.activate();
		wabot.button("Yes").click();
		Boolean valTable = !wabot.table().containsItem(Messages.hlWrld);
		wabot.button("OK").click();
		SWTBotTreeItem proj = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		Boolean valFileDelete = !approotCntnts.
				contains(Messages.hlWrld);
		assertTrue("testRmvCmpntImpSrcAprotYes", valTable
				&& valFileDelete);
		cmpntSrcShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 68
	public void testWABuilderEntry() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotTreeItem proj = Utility.selProjFromExplorer(Messages.projWithCmpnt).select();
		proj.contextMenu("Properties").click();
		SWTBotShell propShell = wabot.
				shell(Messages.propPageTtl + " " +Messages.projWithCmpnt);
		propShell.activate();
		SWTBotTree properties = propShell.bot().tree();
		SWTBotTreeItem  item = properties.getTreeItem("Builders");
		item.select();
		assertTrue("testWABuilderEntry", wabot.table(0).getTableItem(0).isChecked());
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 69
	public void testProjCopyAsName() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testProjCopyAsName", wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(Messages.javaProjName));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 70
	public void testRmvCmpntImpSrcAprotNo() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.table().select(0);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.cmpntRemTtl).activate();
		wabot.button("Yes").click();
		SWTBotShell cmpntSrcShell = wabot.shell(Messages.cmpntSrcRmvTtl);
		cmpntSrcShell.activate();
		wabot.button("No").click();
		Boolean valTable = !wabot.table().containsItem(Messages.hlWrld);
		wabot.button("OK").click();
		SWTBotTreeItem proj = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		Boolean valFileDelete = approotCntnts.
				contains(Messages.hlWrld);
		assertTrue("testRmvCmpntImpSrcAprotNo", valTable
				&& valFileDelete);
		cmpntSrcShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 71
	public void testRmvCmpntImpSrcAprotCancel() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.table().select(0);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.cmpntRemTtl).activate();
		wabot.button("Yes").click();
		SWTBotShell cmpntSrcShell = wabot.shell(Messages.cmpntSrcRmvTtl);
		cmpntSrcShell.activate();
		wabot.button("Cancel").click();
		Boolean valTable = wabot.table().cell(0, 2).equalsIgnoreCase(Messages.hlWrld);
		wabot.button("OK").click();
		SWTBotTreeItem proj = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		Boolean valFileDelete = approotCntnts.
				contains(Messages.hlWrld);
		assertTrue("testRmvCmpntImpSrcAprotCancel", valTable
				&& valFileDelete);
		cmpntSrcShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 72
	public void testFileCopyAsName() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt,
						String.format("%s%s%s", "\\", Messages.role1, Messages.startupFile)));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testFileCopyAsName",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(Messages.startupFile.substring(Messages.
						startupFile.lastIndexOf('\\') + 1)));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 73
	public void testFileNoneAsName() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt,
						String.format("%s%s%s", "\\", Messages.role1, Messages.startupFile)));
		wabot.comboBox(0).setSelection(Messages.cmbValNone);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testFileNoneAsName",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(Messages.startupFile.substring(Messages.
						startupFile.lastIndexOf('\\') + 1)));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 74
	public void testFileZipAsName() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		// do not use typeText otherwise test case fails
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt,
						String.format("%s%s%s", "\\", Messages.role1, Messages.startupFile)));
		wabot.comboBox(0).setSelection(Messages.cmbValZip);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testFileZipAsName",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(String.format("%s%s%s", Messages.startupFile.substring(
						Messages.startupFile.lastIndexOf('\\') + 1,
						Messages.startupFile.lastIndexOf('.')),
						".", Messages.cmbValZip.toLowerCase())));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 75
	public void testDirZipAsName() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.comboBox(0).setSelection(Messages.cmbValZip);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testDirZipAsName",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(String.format("%s%s%s", wabot.textWithLabel(
						Messages.frmPathLbl).getText().substring(wabot.textWithLabel(
								Messages.frmPathLbl).getText().lastIndexOf('\\') + 1),
								".", Messages.cmbValZip.toLowerCase())));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 76
	public void testDirCopyAsName() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testDirCopyAsName",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(wabot.textWithLabel(
						Messages.frmPathLbl).getText().substring(wabot.textWithLabel(
								Messages.frmPathLbl).getText().lastIndexOf('\\') + 1)));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 77
	public void testDirNoneAsName() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.comboBox(0).setSelection(Messages.cmbValNone);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testDirNoneAsName",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(wabot.textWithLabel(
						Messages.frmPathLbl).getText().substring(wabot.textWithLabel(
								Messages.frmPathLbl).getText().lastIndexOf('\\') + 1)));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 78
	public void testJARAsNameEdit() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setText("");
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testJARAsNameEdit",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(String.format("%s%s%s", Messages.javaProjName, ".",
						Messages.cmbValJar.toLowerCase())));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 79
	public void testWARAsNameEdit() throws Exception {
		Utility.createDynamicWebProj(Messages.projWAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.projWAR);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setText("");
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testWARAsNameEdit",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(String.format("%s%s%s", Messages.projWAR, ".",
						Messages.cmbValWar.toLowerCase())));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projWAR)) {
			Utility.selProjFromExplorer(Messages.projWAR).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 80
	public void testEARAsNameEdit() throws Exception {
		Utility.createEnterpriseAppProj(Messages.projEAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\cert");
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.projEAR);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setText("");
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		assertTrue("testEARAsNameEdit",
				wabot.textWithLabel(Messages.asNameLbl).getText().
				equals(String.format("%s%s%s", Messages.projEAR,
						".", Messages.cmbValEar.toLowerCase())));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projEAR)) {
			Utility.selProjFromExplorer(Messages.projEAR).select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Client").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Connector").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "EJB").select();
			Utility.deleteSelectedProject();
			Utility.selProjFromExplorer(Messages.projEAR + "Web").select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 81
	public void testWARToCopy() throws Exception {
		Utility.createDynamicWebProj(Messages.projWAR);
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.projWAR);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		// According to our new implementation we do not show error.
		// if we change import method and as name is not valid
		// w.r.t new method then we make as name empty
		assertTrue("testWARToCopy",
				wabot.textWithLabel(Messages.asNameLbl).
				getText().equals(""));
		cmpntShell.close();
		propShell.close();
		if (Utility.isProjExist(Messages.projWAR)) {
			Utility.selProjFromExplorer(Messages.projWAR).select();
			Utility.deleteSelectedProject();
		}
	}
	
	@Test
	// (New Test Cases for 1.7) test case 82
	public void testDelFileDirCmpnt() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		Utility.
		selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		// add directory component
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		String output = wabot.textWithLabel(Messages.frmPathLbl).getText().
				substring(wabot.textWithLabel(Messages.frmPathLbl).getText().
						lastIndexOf("\\") + 1);
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("OK").click();
		Utility.addCmpnt(Messages.projWithCmpnt, "\\package.xml");
		wabot.button("OK").click();
		// Build project
		Utility.selProjFromExplorer(Messages.projWithCmpnt);
		wabot.menu("Project", 1).menu("Build Project").click();
		// Require wait for building project
		wabot.shell("Build Project").activate();
		wabot.waitUntil(shellCloses(wabot.shell("Build Project")));
		SWTBotTreeItem proj = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		Boolean valPresent = approotCntnts.contains("package.xml")
				&& approotCntnts.contains(output);
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.table().select(1);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.cmpntRemTtl).activate();
		wabot.button("Yes").click();
		wabot.table().select(1);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.cmpntRemTtl).activate();
		wabot.button("Yes").click();
		wabot.button("OK").click();
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode1 = proj1.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode1 = workerRoleNode1.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts1 = appRootNode1.expand().getNodes();
		Boolean valDel = !approotCntnts1.contains("package.xml")
				&& !approotCntnts1.contains(output);
		assertTrue("testDelFileDirCmpnt", valPresent
				&& valDel);
	}

	@Test
	// (New Test Cases for 1.7) test case 83
	public void testRmvJdkCmpnt() throws Exception {
		Utility.createProjectSrvJDK(Messages.projWithCmpnt, Messages.cmbValJet7);
		SWTBotShell propShell = Utility.
		selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.table().select(0);
		wabot.button(Messages.roleRemBtn).click();
		SWTBotShell warnShell = wabot.shell(Messages.jdkDsblErrTtl);
		warnShell.activate();
		assertTrue("testRmvJdkCmpnt", warnShell.getText().
				equals(Messages.jdkDsblErrTtl));
		wabot.button("OK").click();
		propShell.close();
		if (Utility.isProjExist(Messages.test)) {
			// delete existing project
			Utility.selProjFromExplorer(Messages.test).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 84
	public void testRmvSrvCmpnt() throws Exception {
		Utility.createProjectSrvJDK(Messages.projWithCmpnt, Messages.cmbValJet7);
		SWTBotShell propShell = Utility.
		selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.table().select(1);
		wabot.button(Messages.roleRemBtn).click();
		SWTBotShell warnShell = wabot.shell(Messages.jdkDsblErrTtl);
		warnShell.activate();
		assertTrue("testRmvSrvCmpnt", warnShell.getText().
				equals(Messages.jdkDsblErrTtl));
		wabot.button("OK").click();
		propShell.close();
		if (Utility.isProjExist(Messages.test)) {
			// delete existing project
			Utility.selProjFromExplorer(Messages.test).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 85
	public void testCopyAsNameFile() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt, "\\package.xml"));
		wabot.comboBox(0).setSelection(Messages.cmbValZip);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		// According to our new implementation we do not show error.
		// if we change import method and as name is not valid
		// w.r.t new method then we make as name empty
		assertTrue("testCopyAsNameFile",
				wabot.textWithLabel(Messages.asNameLbl).
				getText().equals(""));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 86
	public void testZipAsNameFile() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt, "\\package.xml"));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.comboBox(0).setSelection(Messages.cmbValZip);
		// According to our new implementation we do not show error.
		// if we change import method and as name is not valid
		// w.r.t new method then we make as name empty
		assertTrue("testZipAsNameFile",
				wabot.textWithLabel(Messages.asNameLbl).
				getText().equals(""));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 87
	public void testZipFromNoneAsNameFile() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt, "\\package.xml"));
		wabot.comboBox(0).setSelection(Messages.cmbValNone);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.comboBox(0).setSelection(Messages.cmbValZip);
		// According to our new implementation we do not show error.
		// if we change import method and as name is not valid
		// w.r.t new method then we make as name empty
		assertTrue("testZipAsNameFile",
				wabot.textWithLabel(Messages.asNameLbl).
				getText().equals(""));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 88
	public void testZipFromNoneAsNameDir() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.comboBox(0).setSelection(Messages.cmbValNone);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.comboBox(0).setSelection(Messages.cmbValZip);
		// According to our new implementation we do not show error.
		// if we change import method and as name is not valid
		// w.r.t new method then we make as name empty
		assertTrue("testZipAsNameFile",
				wabot.textWithLabel(Messages.asNameLbl).
				getText().equals(""));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 89
	public void testCopyFromZipAsNameDir() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.comboBox(0).setSelection(Messages.cmbValZip);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		// According to our new implementation we do not show error.
		// if we change import method and as name is not valid
		// w.r.t new method then we make as name empty
		assertTrue("testZipAsNameFile",
				wabot.textWithLabel(Messages.asNameLbl).
				getText().equals(""));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 90
	public void testZipFromCopyAsNameDir() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.comboBox(0).setSelection(Messages.cmbValZip);
		// According to our new implementation we do not show error.
		// if we change import method and as name is not valid
		// w.r.t new method then we make as name empty
		assertTrue("testZipAsNameFile",
				wabot.textWithLabel(Messages.asNameLbl).
				getText().equals(""));
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 169
	public void testRnmCmpnt() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		Utility.createProject(Messages.projWithCmpnt);
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.button(Messages.wrkSpcButton).click();
		wabot.shell(Messages.projSelTtl).activate();
		wabot.activeShell().bot().table().select(Messages.javaProjName);
		wabot.button("OK").click();
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.button("OK").click();
		wabot.button("OK").click();
		// Require sleep otherwise JAR is not created correctly
		wabot.sleep(5000);
		SWTBotTreeItem proj = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		Boolean valPresent = approotCntnts.
				contains(String.format("%s%s%s", Messages.javaProjName,
						".", Messages.cmbValJar.toLowerCase()));
		// Rename component
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntEdtShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntEdtShell.activate();
		wabot.textWithLabel(Messages.asNameLbl).setText(Messages.javaRnm);
		wabot.button("OK").click();
		wabot.button("OK").click();
		// Require sleep for rename operation to take place
		wabot.sleep(1000);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode1 = proj1.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode1 = workerRoleNode1.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts1 = appRootNode1.expand().getNodes();
		Boolean valRnm = approotCntnts1.contains(Messages.javaRnm);
		assertTrue("testRnmCmpnt", valPresent
				&& valRnm);
		// Remove component
		Utility.selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.table().select(1);
		wabot.button(Messages.roleRemBtn).click();
		wabot.shell(Messages.cmpntRemTtl).activate();
		wabot.button("Yes").click();
		wabot.button("OK").click();
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 170
	public void testRnmFileCmpnt() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		wabot.sleep(1000);
		Utility.
		selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\package.xml");
		wabot.sleep(1000);
		wabot.button("OK").click();
		wabot.sleep(1000);
		// Build project
		Utility.selProjFromExplorer(Messages.projWithCmpnt);
		wabot.menu("Project", 1).menu("Build Project").click();
		wabot.sleep(1000);
		// Require wait for building project
		wabot.shell("Build Project").activate();
		wabot.waitUntil(shellCloses(wabot.shell("Build Project")));
		SWTBotTreeItem proj = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		Boolean valPresent = approotCntnts.contains("package.xml");
		// Rename component
		Utility.
		selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.sleep(1000);
		wabot.table().select(1);
		wabot.sleep(1000);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntEdtShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntEdtShell.activate();
		wabot.textWithLabel(Messages.asNameLbl).setText("packageRnm.xml");
		wabot.sleep(1000);
		wabot.button("OK").click();
		wabot.button("OK").click();
		// Require sleep for rename operation to take place
		wabot.sleep(1000);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode1 = proj1.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode1 = workerRoleNode1.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts1 = appRootNode1.expand().getNodes();
		Boolean valRnm = approotCntnts1.contains("packageRnm.xml");
		wabot.sleep(1000);
		assertTrue("testRnmFileCmpnt", valPresent
				&& valRnm);
	}

	@Test
	// (New Test Cases for 1.7) test case 171
	public void testRnmDirCmpnt() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		wabot.sleep(1000);
		Utility.
		selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		// add directory component
		wabot.sleep(1000);
		wabot.button(Messages.roleAddBtn).click();
		wabot.sleep(1000);
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt, "\\cert"));
		String output = wabot.textWithLabel(Messages.frmPathLbl).getText().
				substring(wabot.textWithLabel(Messages.frmPathLbl).getText().
						lastIndexOf("\\") + 1);
		wabot.comboBox(0).setSelection(Messages.cmbValCopy);
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.sleep(1000);
		wabot.button("OK").click();
		wabot.sleep(1000);
		wabot.button("OK").click();
		// Build project
		wabot.sleep(1000);
		Utility.selProjFromExplorer(Messages.projWithCmpnt);
		wabot.menu("Project", 1).menu("Build Project").click();
		wabot.sleep(1000);
		// Require wait for building project
		wabot.shell("Build Project").activate();
		wabot.waitUntil(shellCloses(wabot.shell("Build Project")));
		wabot.sleep(1000);
		SWTBotTreeItem proj = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		Boolean valPresent = approotCntnts.contains(output);
		wabot.sleep(1000);
		// Rename component
		Utility.
		selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.table().select(1);
		wabot.sleep(1000);
		wabot.button(Messages.roleEditBtn).click();
		SWTBotShell cmpntEdtShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntEdtShell.activate();
		wabot.textWithLabel(Messages.asNameLbl).setText("certRnm");
		wabot.sleep(1000);
		wabot.button("OK").click();
		wabot.sleep(1000);
		wabot.button("OK").click();
		// Require sleep for rename operation to take place
		wabot.sleep(1000);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode1 = proj1.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode1 = workerRoleNode1.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts1 = appRootNode1.expand().getNodes();
		Boolean valRnm = approotCntnts1.contains("certRnm");
		wabot.sleep(1000);
		assertTrue("testRnmDirCmpnt", valPresent
				&& valRnm);
	}

	@Test
	// (New Test Cases for 1.7) test case 172
	public void testDelCmpntChangeFrmPath() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		wabot.sleep(1000);
		Utility.
		selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\package.xml");
		wabot.sleep(1000);
		wabot.button("OK").click();
		wabot.sleep(1000);
		// Build project
		Utility.selProjFromExplorer(Messages.projWithCmpnt);
		wabot.menu("Project", 1).menu("Build Project").click();
		// Require wait for building project
		wabot.shell("Build Project").activate();
		wabot.waitUntil(shellCloses(wabot.shell("Build Project")));
		wabot.sleep(1000);
		SWTBotTreeItem proj = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		Boolean valPresent = approotCntnts.contains("package.xml");
		wabot.sleep(1000);
		// Rename component
		Utility.
		selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.table().select(1);
		wabot.sleep(1000);
		wabot.button(Messages.roleEditBtn).click();
		wabot.sleep(1000);
		SWTBotShell cmpntEdtShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntEdtShell.activate();
		wabot.textWithLabel(Messages.asNameLbl).setText("");
		wabot.textWithLabel(Messages.frmPathLbl).setText(
				Utility.getLoc(Messages.projWithCmpnt,
						String.format("%s%s%s", "\\", Messages.role1,
								Messages.startupFile)));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.sleep(1000);
		wabot.button("OK").click();
		wabot.sleep(1000);
		wabot.button("OK").click();
		// Require sleep for delete operation to take place
		wabot.sleep(2000);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode1 = proj1.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode1 = workerRoleNode1.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts1 = appRootNode1.expand().getNodes();
		Boolean valDel = !approotCntnts1.contains("package.xml");
		wabot.sleep(1000);
		assertTrue("testDelCmpntChangeFrmPath", valPresent
				&& valDel);
	}

	@Test
	// (New Test Cases for 1.7) test case 173
	public void testDelCmpntChangeImpMthd() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		wabot.sleep(1000);
		Utility.
		selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		Utility.addCmpnt(Messages.projWithCmpnt, "\\package.xml");
		wabot.sleep(1000);
		wabot.button("OK").click();
		wabot.sleep(1000);
		// Build project
		Utility.selProjFromExplorer(Messages.projWithCmpnt);
		wabot.menu("Project", 1).menu("Build Project").click();
		wabot.sleep(1000);
		// Require wait for building project
		wabot.shell("Build Project").activate();
		wabot.waitUntil(shellCloses(wabot.shell("Build Project")));
		wabot.sleep(1000);
		SWTBotTreeItem proj = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode = proj.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode = workerRoleNode.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts = appRootNode.expand().getNodes();
		Boolean valPresent = approotCntnts.contains("package.xml");
		wabot.sleep(1000);
		// Rename component
		Utility.
		selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
				Messages.role1);
		wabot.sleep(1000);
		wabot.table().select(1);
		wabot.button(Messages.roleEditBtn).click();
		wabot.sleep(1000);
		SWTBotShell cmpntEdtShell = wabot.shell(Messages.cmpntEditTtl);
		cmpntEdtShell.activate();
		wabot.comboBox(0).setSelection(Messages.cmbValNone);
		wabot.sleep(1000);
		wabot.button("OK").click();
		wabot.sleep(1000);
		wabot.button("OK").click();
		// Require sleep for delete operation to take place
		wabot.sleep(2000);
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode1 = proj1.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode1 = workerRoleNode1.expand().
				getNode(Messages.appRoot);
		List<String> approotCntnts1 = appRootNode1.expand().getNodes();
		Boolean valDel = !approotCntnts1.contains("package.xml");
		wabot.sleep(1000);
		assertTrue("testDelCmpntChangeImpMthd", valPresent
				&& valDel);
	}

	@Test
	// (New Test Cases for 1.7) test case 174
	public void testAsNameCnstrErr() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		wabot.sleep(1000);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.sleep(1000);
		wabot.button(Messages.roleAddBtn).click();
		wabot.shell(Messages.cmpntAddTtl).activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\package.xml"));
		wabot.textWithLabel(Messages.toDirLbl).setFocus();
		wabot.sleep(1000);
		wabot.button("OK").click();
		wabot.sleep(1000);
		// Add once again
		wabot.button(Messages.roleAddBtn).click();
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\package.xml"));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.sleep(1000);
		wabot.button("OK").click();
		SWTBotShell errShell = wabot.shell(Messages.dplyNameErrTtl);
		errShell.activate();
		String errMsg = errShell.getText();
		wabot.sleep(1000);
		assertTrue("testAsNameCnstrErr",
				errMsg.equals(Messages.dplyNameErrTtl));
		errShell.close();
		cmpntShell.close();
		propShell.close();
	}

	@Test
	// (New Test Cases for 1.7) test case 175
	public void testAsNameCnstr() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		wabot.sleep(1000);
		SWTBotShell propShell = Utility.
				selCmpntPageUsingCnxtMenu(Messages.projWithCmpnt,
						Messages.role1);
		wabot.sleep(1000);
		wabot.button(Messages.roleAddBtn).click();
		wabot.sleep(1000);
		wabot.shell(Messages.cmpntAddTtl).activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\package.xml"));
		wabot.textWithLabel(Messages.toDirLbl).setFocus();
		wabot.button("OK").click();
		wabot.sleep(1000);
		// Add once again
		wabot.button(Messages.roleAddBtn).click();
		wabot.sleep(1000);
		SWTBotShell cmpntShell = wabot.shell(Messages.cmpntAddTtl);
		cmpntShell.activate();
		wabot.textWithLabel(Messages.frmPathLbl).typeText(
				Utility.getLoc(Messages.projWithCmpnt, "\\package.xml"));
		wabot.textWithLabel(Messages.asNameLbl).setFocus();
		wabot.sleep(1000);
		wabot.button("OK").click();
		wabot.sleep(1000);
		wabot.shell(Messages.dplyNameErrTtl).activate();
		wabot.button("OK").click();
		wabot.sleep(1000);
		wabot.textWithLabel(Messages.asNameLbl).setText("packageRnm.xml");
		wabot.button("OK").click();
		wabot.sleep(1000);
		assertTrue("testAsNameCnstr", wabot.table().cell(1, 2).equalsIgnoreCase("")
				&& wabot.table().cell(2, 2).equalsIgnoreCase("packageRnm.xml"));
		wabot.sleep(1000);
		cmpntShell.close();
		wabot.sleep(1000);
		propShell.close();
	}
}

