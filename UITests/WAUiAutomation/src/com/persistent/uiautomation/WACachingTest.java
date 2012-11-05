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
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)

public class WACachingTest {
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
		if (Utility.isProjExist(Messages.projCache)) {
			// delete existing project
			Utility.selProjFromExplorer(Messages.projCache).select();
			Utility.deleteSelectedProject();
		}
	}

	@After
	public void cleanUp() throws Exception {
		if (Utility.isProjExist(Messages.projCache)) {
			Utility.selProjFromExplorer(Messages.projCache).select();
			Utility.deleteSelectedProject();
		}
	}

	@Test
	// (Test Cases for 1.8) test case 1
	public void testCachingPagePresent() throws Exception {
		Utility.createProject(Messages.projCache);
		SWTBotShell propShell = Utility.
				selCacheUsingCnxtMenu(Messages.projCache,
						Messages.role1);
		assertTrue("testCachingPagePresent",
				wabot.checkBox().isEnabled()
				&& !wabot.checkBox().isChecked()
				&& wabot.button("OK").isEnabled()
				&& wabot.button("Cancel").isEnabled());
		propShell.close();
	}
	
	@Test
	// (Test Cases for 1.8) test case 2
	public void testEnableCaching() throws Exception {
		Utility.createProject(Messages.projCache);
		SWTBotShell propShell = Utility.
				selCacheUsingCnxtMenu(Messages.projCache,
						Messages.role1);
		wabot.checkBox().select();
		Boolean isEnabled = wabot.checkBox().isChecked()
				&& wabot.label(Messages.cachScaleLbl).isEnabled()
				&& wabot.label(Messages.hostLbl).isEnabled()
				&& wabot.button(Messages.roleAddBtn).isEnabled()
				&& !wabot.button(Messages.roleEditBtn).isEnabled()
				&& wabot.label(Messages.keyLbl).isEnabled()
				&& wabot.label(Messages.nameLbl).isEnabled()
				&& wabot.table().isEnabled();
		Boolean cacheSizeVal = wabot.textWithLabel(
				Messages.cachScaleLbl).getText().
				equals(Messages.dfltSizeVal)
				&& (wabot.scale().getValue() == 30);
		Boolean dfltCacheVal = wabot.table().
				containsItem(Messages.dfltCachName)
				&& wabot.table().cell(0, 1).equals(Messages.cachBckNo)
				&& wabot.table().cell(0, 2).equals(Messages.expPolAbs)
				&& wabot.table().cell(0, 3).equals("10")
				&& wabot.table().cell(0, 4).equals("11211");
		Boolean hostNmVal = wabot.
				textWithLabel(Messages.hostLbl).
				getText().equals(String.format("%s%s",
						Messages.hostNm, Messages.role1.toLowerCase()));
		Boolean StorageVal = wabot.textWithLabel(Messages.keyLbl).
				getText().equals("")
				&& wabot.textWithLabel(Messages.nameLbl).
				getText().equals("");
		/*
		 * To avoid missing storage account error,
		 * set some value
		 */
		wabot.textWithLabel(Messages.keyLbl).setText("a");
		wabot.textWithLabel(Messages.nameLbl).setText("a");
		Utility.selEndPtPage();
		Boolean endPtPresent = wabot.table().
				cell(1, 0).equals(String.format("%s%s",
						Messages.cachEndPtName, Messages.dfltCachName))
						&& wabot.table().cell(1, 1).
						equals(Messages.typeIntrnl)
						&& wabot.table().cell(1, 3).equals("11211");
		propShell = Utility.selLclStrPage(Messages.role1);
		Boolean LclStrPresent = wabot.table().
				cell(0, 0).equals(Messages.cachLclStr)
				&& wabot.table().
				cell(0, 1).equals("20000");
		assertTrue("testEnableCaching", isEnabled
				&& cacheSizeVal
				&& dfltCacheVal
				&& hostNmVal
				&& StorageVal
				&& endPtPresent
				&& LclStrPresent);
		propShell.close();
	}
	
	@Test
	// (Test Cases for 1.8) test case 3
	public void testDisableCaching() throws Exception {
		Utility.createProject(Messages.projCache);
		SWTBotShell propShell = Utility.
				selCacheUsingCnxtMenu(Messages.projCache,
						Messages.role1);
		// check
		wabot.checkBox().select();
		/*
		 * To avoid missing storage account error,
		 * type some value. Requires typeText only.
		 */
		wabot.textWithLabel(Messages.keyLbl).typeText("a");
		wabot.textWithLabel(Messages.nameLbl).typeText("a");
		Utility.selEndPtPage();
		Boolean endPtPresent = wabot.table().
				cell(1, 0).equals(String.format("%s%s",
						Messages.cachEndPtName, Messages.dfltCachName))
						&& wabot.table().cell(1, 1).
						equals(Messages.typeIntrnl)
						&& wabot.table().cell(1, 3).equals("11211");
		Utility.selLclStrPage(Messages.role1);
		Boolean LclStrPresent = wabot.table().
				cell(0, 0).equals(Messages.cachLclStr)
				&& wabot.table().
				cell(0, 1).equals("20000");
		Utility.selCachePage(Messages.role1);
		// un-check
		wabot.checkBox().click();
		Boolean isDisabled = !wabot.checkBox().isChecked()
				&& !wabot.label(Messages.cachScaleLbl).isEnabled()
				&& !wabot.label(Messages.hostLbl).isEnabled()
				&& !wabot.button(Messages.roleAddBtn).isEnabled()
				&& !wabot.button(Messages.roleEditBtn).isEnabled()
				&& !wabot.label(Messages.keyLbl).isEnabled()
				&& !wabot.label(Messages.nameLbl).isEnabled()
				&& !wabot.table().isEnabled()
				&& !wabot.textWithLabel(Messages.cachScaleLbl).isEnabled()
				&& !wabot.scale().isEnabled();
		// Once caching disabled, no storage account warning
		Utility.selEndPtPage();
		Boolean endPtRmv = !wabot.table().containsItem(String.format("%s%s",
						Messages.cachEndPtName, Messages.dfltCachName));
		propShell = Utility.selLclStrPage(Messages.role1);
		Boolean lclStrRmv = !wabot.table().containsItem(Messages.cachLclStr);
		assertTrue("testDisableCaching", endPtPresent
				&& LclStrPresent
				&& isDisabled
				&& endPtRmv
				&& lclStrRmv);
		propShell.close();
	}
	
	@Test
	// (Test Cases for 1.8) test case 4
	public void testInvalidCacheSizeOKPressed() throws Exception {
		Utility.createProject(Messages.projCache);
		SWTBotShell propShell = Utility.
				selCacheUsingCnxtMenu(Messages.projCache,
						Messages.role1);
		wabot.checkBox().select();
		// Cache size = 0
		// typeText and setting focus on OK is IMP.
		wabot.textWithLabel(
				Messages.cachScaleLbl).setText("");
		wabot.textWithLabel(
				Messages.cachScaleLbl).typeText("0");
		wabot.button("OK").setFocus();
		wabot.button("OK").click();
		SWTBotShell errorShell = wabot.shell(
				Messages.cachPerErrTtl).activate();
		Boolean zeroErr = errorShell.getText().
				equals(Messages.cachPerErrTtl);
		wabot.button("OK").click();
		// Cache size < 0 i.e. Negative
		wabot.textWithLabel(
				Messages.cachScaleLbl).setText("");
		wabot.textWithLabel(
				Messages.cachScaleLbl).typeText("-2");
		wabot.button("OK").setFocus();
		wabot.button("OK").click();
		errorShell = wabot.shell(
				Messages.cachPerErrTtl).activate();
		Boolean negErr = errorShell.getText().
				equals(Messages.cachPerErrTtl);
		wabot.button("OK").click();
		// Cache size > 100
		wabot.textWithLabel(
				Messages.cachScaleLbl).setText("");
		wabot.textWithLabel(
				Messages.cachScaleLbl).typeText("105%");
		wabot.button("OK").setFocus();
		wabot.button("OK").click();
		errorShell = wabot.shell(
				Messages.cachPerErrTtl).activate();
		Boolean grtErr = errorShell.getText().
				equals(Messages.cachPerErrTtl);
		wabot.button("OK").click();
		assertTrue("testInvalidCacheSizeOKPressed",
				zeroErr
				&& negErr
				&& grtErr);
		propShell.close();
	}
	
	@Test
	// (Test Cases for 1.8) test case 5
	public void testInvalidCacheSizeOKToLeave() throws Exception {
		Utility.createProject(Messages.projCache);
		SWTBotShell propShell = Utility.
				selCacheUsingCnxtMenu(Messages.projCache,
						Messages.role1);
		wabot.checkBox().select();
		/*
		 * To avoid missing storage account error,
		 * type some value. Requires typeText only.
		 */
		wabot.textWithLabel(Messages.keyLbl).typeText("a");
		wabot.textWithLabel(Messages.nameLbl).typeText("a");
		// Cache size = 0
		wabot.textWithLabel(
				Messages.cachScaleLbl).setText("");
		wabot.textWithLabel(
				Messages.cachScaleLbl).typeText("0");
		SWTBotTree properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).
		getNode(Messages.endptPage).select();
		SWTBotShell errorShell = wabot.shell(String.format(
				"%s%s", Messages.okToLeaveTtl, " ")).activate();
		Boolean zeroErr = errorShell.getText().
				equals(String.format("%s%s",
						Messages.okToLeaveTtl, " "));
		wabot.button("OK").click();
		// Cache size < 0 i.e. Negative
		wabot.textWithLabel(
				Messages.cachScaleLbl).setText("");
		wabot.textWithLabel(
				Messages.cachScaleLbl).typeText("-2");
		properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).
		getNode(Messages.endptPage).select();
		errorShell = wabot.shell(String.format(
				"%s%s", Messages.okToLeaveTtl, " ")).activate();
		Boolean negErr = errorShell.getText().
				equals(String.format("%s%s",
						Messages.okToLeaveTtl, " "));
		wabot.button("OK").click();
		// Cache size > 100
		wabot.textWithLabel(
				Messages.cachScaleLbl).setText("");
		wabot.textWithLabel(
				Messages.cachScaleLbl).typeText("105%");
		properties = propShell.bot().tree();
		properties.getTreeItem(Messages.roleTreeRoot).
		getNode(Messages.endptPage).select();
		errorShell = wabot.shell(String.format(
				"%s%s", Messages.okToLeaveTtl, " ")).activate();
		Boolean grtErr = errorShell.getText().
				equals(String.format("%s%s",
						Messages.okToLeaveTtl, " "));
		wabot.button("OK").click();
		assertTrue("testInvalidCacheSizeOKToLeave",
				zeroErr
				&& negErr
				&& grtErr);
		propShell.close();
	}
	
	@Test
	// (Test Cases for 1.8) test case 6
	public void testNtNumericCacheSizeOKPressed() throws Exception {
		Utility.createProject(Messages.projCache);
		SWTBotShell propShell = Utility.
				selCacheUsingCnxtMenu(Messages.projCache,
						Messages.role1);
		wabot.checkBox().select();
		// Cache size = alphabet
		// typeText and setting focus on OK is IMP.
		wabot.textWithLabel(
				Messages.cachScaleLbl).setText("");
		wabot.textWithLabel(
				Messages.cachScaleLbl).typeText("ab");
		wabot.button("OK").setFocus();
		wabot.button("OK").click();
		SWTBotShell errorShell = wabot.shell(
				Messages.cachPerErrTtl).activate();
		Boolean alphabtErr = errorShell.getText().
				equals(Messages.cachPerErrTtl);
		wabot.button("OK").click();
		// Cache size = special character
		wabot.textWithLabel(
				Messages.cachScaleLbl).setText("");
		wabot.textWithLabel(
				Messages.cachScaleLbl).typeText("#*");
		wabot.button("OK").setFocus();
		wabot.button("OK").click();
		errorShell = wabot.shell(
				Messages.cachPerErrTtl).activate();
		Boolean splCharErr = errorShell.getText().
				equals(Messages.cachPerErrTtl);
		wabot.button("OK").click();
		assertTrue("testNtNumericCacheSizeOKPressed",
				alphabtErr
				&& splCharErr);
		propShell.close();
	}
}
