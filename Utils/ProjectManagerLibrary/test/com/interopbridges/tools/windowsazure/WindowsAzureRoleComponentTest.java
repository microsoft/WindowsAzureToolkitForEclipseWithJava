package com.interopbridges.tools.windowsazure;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class WindowsAzureRoleComponentTest {
    private WindowsAzureProjectManager waProjectManager;
    private WindowsAzureRole waRole;
    private WindowsAzureRoleComponent waXcopyComp;
    private WindowsAzureRoleComponent waHwComp;
    private WindowsAzureRoleComponent waJdkComp;
    private WindowsAzureRole waErrRole;
    @Before
    public final void setUp() {
        File projectDirectoryPath = new File(
                Messages.getString(
                        "WinAzureTestConstants.WAProj"));
        try {
            waProjectManager = WindowsAzureProjectManager
                    .load(projectDirectoryPath);
            waRole = waProjectManager.getRoles().get(1);
            waErrRole = waProjectManager.getRoles().get(2);
            waXcopyComp = waRole.getComponents().get(1);
            waJdkComp = waRole.getComponents().get(0);
            waHwComp = waProjectManager.getRoles().get(0).getComponents().get(0);
        }  catch (WindowsAzureInvalidProjectOperationException e) {
            fail("Exception occured in setup " + e.getMessage());
        }
    }


    //getDeployName
    @Test
    public final void testGetDeployName() {
        assertEquals("xcopy.zip", waXcopyComp.getDeployName());
    }

    //setDeployname
    @Test
    public final void testSetDeployName()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setDeployname("new.zip");
        assertEquals("new.zip", waXcopyComp.getDeployName());
    }

    @Test(expected=IllegalArgumentException.class)
    public final void testSetDeployNameWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setDeployname(null);
    }

    @Test
    public final void testsetDeploynameForErr()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureRoleComponent comp = waErrRole.getComponents().get(0);
        comp.setImportPath("c:\\");
        comp.setDeployname("");
        comp.setDeployname("");
    }

    //getImportPath
    @Test
    public final void testGetImportPath() {
        assertEquals("D:\\xcopy.zip", waXcopyComp.getImportPath());
    }

    @Test
    public final void testGetImportPathWhenAttrNotPresent() {
        assertEquals("", waHwComp.getImportPath());
    }

    //setImportPath
    @Test
    public final void testSetImportPath()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setImportPath("C:\\xcopy.zip");
        assertEquals("C:\\xcopy.zip", waXcopyComp.getImportPath());
    }

    @Test (expected=IllegalArgumentException.class)
    public final void testSetImportPathWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setImportPath(null);
    }

    @Test
    public final void testSetImportPathWhenAttrNotPresent()
            throws WindowsAzureInvalidProjectOperationException {
        waHwComp.setImportPath("C:\\xcopy.zip");
        assertEquals("C:\\xcopy.zip", waHwComp.getImportPath());
    }


    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public final void testSetImportPathForErr()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureRoleComponent comp = waErrRole.getComponents().get(0);
        comp.setDeployname("");
        comp.setImportPath("");
    }

    //getImportMethod
    @Test
    public final void testGetImportMethod() {
        assertEquals(WindowsAzureRoleComponentImportMethod.copy,
                waXcopyComp.getImportMethod());
    }

    @Test
    public final void testGetImportMethodWhenAttrNotPresent() {
        assertEquals(null, waHwComp.getImportMethod());
    }

    //setImportMethod
    @Test
    public final void testSetImportMethod()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setImportMethod(WindowsAzureRoleComponentImportMethod.zip);
        assertEquals(WindowsAzureRoleComponentImportMethod.zip, waXcopyComp.getImportMethod());
    }

    @Test (expected=IllegalArgumentException.class)
    public final void testSetMethodPathWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setImportMethod(null);
    }

    @Test
    public final void testSetImportMethodWhenAttrNotPresent()
            throws WindowsAzureInvalidProjectOperationException {
        waHwComp.setImportMethod(WindowsAzureRoleComponentImportMethod.copy);
        assertEquals(WindowsAzureRoleComponentImportMethod.copy,
                waHwComp.getImportMethod());
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public final void testSetImportMethodForErr()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureRoleComponent comp = waErrRole.getComponents().get(0);
        comp.setDeployname("");
        comp.setImportMethod(WindowsAzureRoleComponentImportMethod.copy);
    }


    //getDeployMethod
    @Test
    public final void testGetDeployMethod() {
        assertEquals(WindowsAzureRoleComponentDeployMethod.unzip,
                waXcopyComp.getDeployMethod());
    }

    @Test
    public final void testGetDeployMethodWhenAttrNotPresent() {
        assertEquals(null, waHwComp.getDeployMethod());
    }


    //setDeployMethod
    @Test
    public final void testSetDeployMethod()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setDeployMethod(WindowsAzureRoleComponentDeployMethod.unzip);
        assertEquals(WindowsAzureRoleComponentDeployMethod.unzip,
                waXcopyComp.getDeployMethod());
    }

    @Test (expected=IllegalArgumentException.class)
    public final void testSetDeployMethodPathWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setDeployMethod(null);
    }

    @Test
    public final void testSetDeployMethodWhenAttrNotPresent()
            throws WindowsAzureInvalidProjectOperationException {
        waHwComp.setDeployMethod(WindowsAzureRoleComponentDeployMethod.copy);
        assertEquals(WindowsAzureRoleComponentDeployMethod.copy,
                waHwComp.getDeployMethod());
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public final void testSetDeployMethodForErr()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureRoleComponent comp = waErrRole.getComponents().get(0);
        comp.setDeployname("");
        comp.setDeployMethod(WindowsAzureRoleComponentDeployMethod.copy);
    }


    //getDeployDir
    @Test
    public final void testGetDeployDir() {
        assertEquals(".\\", waXcopyComp.getDeployDir());
    }

    @Test
    public final void testGetDeployDirWhenAttrNotPresent() {
        assertEquals("", waHwComp.getDeployDir());
    }

    //setDeployDir
    @Test
    public final void testSetDeployDir()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setDeployDir("c:\\");
        assertEquals("c:\\", waXcopyComp.getDeployDir());
    }

    @Test (expected=IllegalArgumentException.class)
    public final void testSetDeployDirWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setDeployDir(null);
    }

    @Test
    public final void testSetDeployDirWhenAttrNotPresent()
            throws WindowsAzureInvalidProjectOperationException {
        waHwComp.setDeployDir("C:\\");
        assertEquals("C:\\", waHwComp.getDeployDir());
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public final void testSetDeployDirForErr()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureRoleComponent comp = waErrRole.getComponents().get(0);
        comp.setDeployname("");
        comp.setDeployDir("c:\\aa");
    }


    //getType
    public final void testGetType() {
        assertEquals("jdk.deploy", waJdkComp.getType());
    }

    public final void testGetTypeWithEmpty() {
        assertEquals("", waXcopyComp.getType());
    }

    //setType
    @Test (expected=IllegalArgumentException.class)
    public final void testSetTypeWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setType(null);
    }

    @Test
    public final void testSetType()
            throws WindowsAzureInvalidProjectOperationException {
        waXcopyComp.setType("server.app");
        assertEquals("server.app", waXcopyComp.getType());
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public final void testSetTypeForErr()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureRoleComponent comp = waErrRole.getComponents().get(0);
        comp.setDeployname("");
        comp.setType("aa");
    }

    //getIsPreconfigured
    @Test
    public final void testGetIsPreconfigured()
            throws WindowsAzureInvalidProjectOperationException {
        assertEquals(false, waXcopyComp.getIsPreconfigured());
    }

    @Test
    public final void testGetIsPreconfiguredForServerApp()
            throws WindowsAzureInvalidProjectOperationException {
        assertEquals(true, waRole.getComponents().get(0).getIsPreconfigured());
    }

    @Test
    public final void testGetIsPreconfiguredForJdkApp()
            throws WindowsAzureInvalidProjectOperationException {
        assertEquals(true, waRole.getComponents().get(2).getIsPreconfigured());
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public final void testGetIsPreconfiguredForErr()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureRoleComponent comp = waErrRole.getComponents().get(0);
        comp.setDeployname("");
        comp.getIsPreconfigured();
    }

    //delete
    @Test
    public final void testDelete()
            throws WindowsAzureInvalidProjectOperationException {
        String waFirstComName = waRole.getComponents().get(0).getDeployName();
        waJdkComp.delete();
        String waNewFirstComName = waRole.getComponents().get(0).getDeployName();
        assertEquals(2, waRole.getComponents().size());
        assertNotSame(waFirstComName, waNewFirstComName);
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public final void testDeleteForErr()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureRoleComponent comp = waErrRole.getComponents().get(0);
        comp.setDeployname("");
        comp.delete();
    }

}