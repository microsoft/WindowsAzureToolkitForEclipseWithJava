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
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)

public class WAToolbarTest {

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
	// (New Test Cases for 1.7) test case 91
	public void testWATlbrVisibleAllPrspctv() throws Exception {
		// currently resource perspective
		SWTBotToolbarButton runWAE = wabot.
				toolbarButtonWithTooltip(Messages.runBtn);
		SWTBotToolbarButton rstWAE = wabot.
				toolbarButtonWithTooltip(Messages.rstBtn);
		SWTBotToolbarButton bldCloud = wabot.
				toolbarButtonWithTooltip(Messages.bldBtn);
		SWTBotToolbarButton crtWAProj = wabot.
				toolbarButtonWithTooltip(Messages.newProjBtn);
		Boolean rsrcPrsp = !runWAE.isEnabled()
				&& rstWAE.isVisible()
				&& rstWAE.isEnabled()
				&& !bldCloud.isEnabled()
				&& crtWAProj.isVisible()
				&& crtWAProj.isEnabled();
		// Change perspective to Java EE
		wabot.menu(Messages.window).menu(Messages.openPrsp).menu(Messages.other).click();
		SWTBotShell sh1 = wabot.shell(Messages.openPrsp).activate();
		wabot.table().select(Messages.jEE);
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh1));
		Boolean jEEPrsp = !runWAE.isEnabled()
				&& rstWAE.isVisible()
				&& rstWAE.isEnabled()
				&& !bldCloud.isEnabled()
				&& crtWAProj.isVisible()
				&& crtWAProj.isEnabled();
		assertTrue("testWATlbrVisibleAllPrspctv", rsrcPrsp
				&& jEEPrsp);
		// Change perspective to Resource
		wabot.menu(Messages.window).menu(Messages.openPrsp).menu(Messages.other).click();
		SWTBotShell sh2 = wabot.shell(Messages.openPrsp).activate();
		wabot.table().select(Messages.rsc);
		wabot.button("OK").click();
		wabot.waitUntil(shellCloses(sh2));
	}

	@Test
	// (New Test Cases for 1.7) test case 92
	public void testWATlbrVisibleWAProj() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotToolbarButton runWAE = wabot.
				toolbarButtonWithTooltip(Messages.runBtn);
		SWTBotToolbarButton rstWAE = wabot.
				toolbarButtonWithTooltip(Messages.rstBtn);
		SWTBotToolbarButton bldCloud = wabot.
				toolbarButtonWithTooltip(Messages.bldBtn);
		SWTBotToolbarButton crtWAProj = wabot.
				toolbarButtonWithTooltip(Messages.newProjBtn);
		Boolean beforeProjSel = !runWAE.isEnabled()
				&& !bldCloud.isEnabled();
		Utility.selProjFromExplorer(Messages.projWithCmpnt).select();
		assertTrue("testWATlbrVisibleWAProj", beforeProjSel
				&& rstWAE.isEnabled()
				&& crtWAProj.isEnabled()
				&& runWAE.isEnabled()
				&& bldCloud.isEnabled());
	}

	@Test
	// (New Test Cases for 1.7) test case 93
	public void testWATlbrVisibleWADirFile() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotToolbarButton runWAE = wabot.
				toolbarButtonWithTooltip(Messages.runBtn);
		SWTBotToolbarButton rstWAE = wabot.
				toolbarButtonWithTooltip(Messages.rstBtn);
		SWTBotToolbarButton bldCloud = wabot.
				toolbarButtonWithTooltip(Messages.bldBtn);
		SWTBotToolbarButton crtWAProj = wabot.
				toolbarButtonWithTooltip(Messages.newProjBtn);
		Boolean beforeSel = !runWAE.isEnabled()
				&& !bldCloud.isEnabled();
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem workerRoleNode1 = proj1.expand().
				getNode(Messages.role1);
		SWTBotTreeItem appRootNode1 = workerRoleNode1.expand().
				getNode(Messages.appRoot).select();
		Boolean dirSel = runWAE.isEnabled()
				&& bldCloud.isEnabled()
				&& rstWAE.isEnabled()
				&& crtWAProj.isEnabled();
		appRootNode1.expand().getNode(Messages.runFile).select();
		Boolean fileSel = runWAE.isEnabled()
				&& bldCloud.isEnabled()
				&& rstWAE.isEnabled()
				&& crtWAProj.isEnabled();
		assertTrue("testWATlbrVisibleWADirFile", beforeSel
				&& dirSel
				&& fileSel);
	}

	@Test
	// (New Test Cases for 1.7) test case 94
	public void testWATlbrNotVisible() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		SWTBotToolbarButton runWAE = wabot.
				toolbarButtonWithTooltip(Messages.runBtn);
		SWTBotToolbarButton rstWAE = wabot.
				toolbarButtonWithTooltip(Messages.rstBtn);
		SWTBotToolbarButton bldCloud = wabot.
				toolbarButtonWithTooltip(Messages.bldBtn);
		SWTBotToolbarButton crtWAProj = wabot.
				toolbarButtonWithTooltip(Messages.newProjBtn);
		SWTBotTreeItem proj = Utility.selProjFromExplorer(Messages.javaProjName).select();
		Boolean projSel = !runWAE.isEnabled()
				&& rstWAE.isEnabled()
				&& !bldCloud.isEnabled()
				&& crtWAProj.isEnabled();
		proj.expand().getNode("src").select();
		Boolean dirSel = !runWAE.isEnabled()
				&& rstWAE.isEnabled()
				&& !bldCloud.isEnabled()
				&& crtWAProj.isEnabled();
		assertTrue("testWATlbrNotVisible", projSel
				&& dirSel);
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (New Test Cases for 1.7) test case 95
	public void testRunEmltrSingleSlctn() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		Utility.selProjFromExplorer(Messages.projWithCmpnt).select();
		SWTBotToolbarButton runWAE = wabot.
				toolbarButtonWithTooltip(Messages.runBtn);
		SWTBotToolbarButton rstWAE = wabot.
				toolbarButtonWithTooltip(Messages.rstBtn);
		SWTBotToolbarButton bldCloud = wabot.
				toolbarButtonWithTooltip(Messages.bldBtn);
		SWTBotToolbarButton crtWAProj = wabot.
				toolbarButtonWithTooltip(Messages.newProjBtn);
		assertTrue("testRunEmltrSingleSlctn", runWAE.isEnabled()
				&& rstWAE.isEnabled()
				&& bldCloud.isEnabled()
				&& crtWAProj.isEnabled());
	}

	@Test
	// (New Test Cases for 1.7) test case 96
	public void testRunEmltrMultipleSlctn() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		Utility.createProject(Messages.test);
		wabot.menu("Edit").menu(Messages.selAll).click();
		SWTBotToolbarButton runWAE = wabot.
				toolbarButtonWithTooltip(Messages.runBtn);
		SWTBotToolbarButton rstWAE = wabot.
				toolbarButtonWithTooltip(Messages.rstBtn);
		SWTBotToolbarButton bldCloud = wabot.
				toolbarButtonWithTooltip(Messages.bldBtn);
		SWTBotToolbarButton crtWAProj = wabot.
				toolbarButtonWithTooltip(Messages.newProjBtn);
		assertTrue("testRunEmltrMultipleSlctn", !runWAE.isEnabled()
				&& rstWAE.isEnabled()
				&& !bldCloud.isEnabled()
				&& crtWAProj.isEnabled());
		if (Utility.isProjExist(Messages.test)) {
			Utility.selProjFromExplorer(Messages.test).select();
			Utility.deleteSelectedProject();
		}

	}

	@Test
	// (New Test Cases for 1.7) test case 98
	public void testCrtWAProjClick() throws Exception {
		SWTBotToolbarButton crtWAProj = wabot.
				toolbarButtonWithTooltip(Messages.newProjBtn);
		crtWAProj.click();
		SWTBotShell sh = wabot.shell(Messages.newWAProjTtl);
		sh.activate();
		assertTrue("testCrtWAProjClick", wabot.textWithLabel("Project name:").isEnabled()
				&& !wabot.button("Finish").isEnabled()
				&& wabot.button("Cancel").isEnabled());
		wabot.button("Cancel").click();
	}

	@Test
	// (New Test Cases for 1.7) test case 99
	public void testCrtWAProj() throws Exception {
		SWTBotToolbarButton crtWAProj = wabot.
				toolbarButtonWithTooltip(Messages.newProjBtn);
		crtWAProj.click();
		SWTBotShell sh = wabot.shell(Messages.newWAProjTtl);
		sh.activate();
		wabot.textWithLabel("Project name:").setText(Messages.projWithCmpnt);
		wabot.button("Finish").click();
		wabot.waitUntil(shellCloses(sh));
		wabot.sleep(10000);
		assertTrue("testCrtWAProj", Utility.isProjExist(Messages.projWithCmpnt));
	}

	@Test
	// (New Test Cases for 1.7) test case 249
	public void testWATlbrVisibleEditor() throws Exception {
		Utility.createProject(Messages.projWithCmpnt);
		SWTBotToolbarButton runWAE = wabot.
				toolbarButtonWithTooltip(Messages.runBtn);
		SWTBotToolbarButton rstWAE = wabot.
				toolbarButtonWithTooltip(Messages.rstBtn);
		SWTBotToolbarButton bldCloud = wabot.
				toolbarButtonWithTooltip(Messages.bldBtn);
		SWTBotToolbarButton crtWAProj = wabot.
				toolbarButtonWithTooltip(Messages.newProjBtn);
		Boolean beforeOpen = !runWAE.isEnabled()
				&& !bldCloud.isEnabled();
		SWTBotTreeItem proj1 = Utility.
				selProjFromExplorer(Messages.projWithCmpnt);
		SWTBotTreeItem file = proj1.expand().getNode("package.xml").select();
		file.contextMenu(Messages.open).click();
		// require sleep to open file in editor
		wabot.sleep(3000);
		Boolean fileOpen = runWAE.isEnabled()
				&& bldCloud.isEnabled()
				&& rstWAE.isEnabled()
				&& crtWAProj.isEnabled();
		assertTrue("testWATlbrVisibleEditor", beforeOpen
				&& fileOpen);
	}

	@Test
	// (New Test Cases for 1.7) test case 250
	public void testWATlbrNotVisibleEditor() throws Exception {
		Utility.createJavaProject(Messages.javaProjName);
		SWTBotToolbarButton runWAE = wabot.
				toolbarButtonWithTooltip(Messages.runBtn);
		SWTBotToolbarButton rstWAE = wabot.
				toolbarButtonWithTooltip(Messages.rstBtn);
		SWTBotToolbarButton bldCloud = wabot.
				toolbarButtonWithTooltip(Messages.bldBtn);
		SWTBotToolbarButton crtWAProj = wabot.
				toolbarButtonWithTooltip(Messages.newProjBtn);
		Boolean beforeOpen = !runWAE.isEnabled()
				&& !bldCloud.isEnabled();
		Utility.selProjFromExplorer(Messages.javaProjName);
		wabot.menu("File").menu("New").menu("File").click();
		wabot.shell("New File").activate();
		wabot.textWithLabel("File name:").setText(String.format("%s%s",
				Messages.test, ".txt"));
		wabot.button("Finish").click();
		// require sleep to open file in editor
		wabot.sleep(3000);
		Boolean fileOpen = !runWAE.isEnabled()
				&& !bldCloud.isEnabled()
				&& rstWAE.isEnabled()
				&& crtWAProj.isEnabled();
		assertTrue("testWATlbrNotVisibleEditor", beforeOpen
				&& fileOpen);
		if (Utility.isProjExist(Messages.javaProjName)) {
			Utility.selProjFromExplorer(Messages.javaProjName).select();
			Utility.deleteSelectedProject();
		}
	}
}
