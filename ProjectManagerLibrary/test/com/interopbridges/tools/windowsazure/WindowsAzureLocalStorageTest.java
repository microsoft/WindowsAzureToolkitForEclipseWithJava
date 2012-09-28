package com.interopbridges.tools.windowsazure;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>WindowsAzureLocalStorageTest</code> contains tests for the
 * class {@link <code>WindowsAzureLocalStorage</code>}
 */

public class WindowsAzureLocalStorageTest  {


    private WindowsAzureProjectManager wProj = null;
    private WindowsAzureRole role = null;
    private WindowsAzureLocalStorage wLoSto= null;

    @Before
    public final void setUp() {
        try {
            wProj = WindowsAzureProjectManager.load(
                    new File(Messages.getString(
                            "WinAzureTestConstants.WindowsAzureProj")));
            role = wProj.getRoles().get(2);
            wLoSto = role.getLocalStorage().get("WAStorage");

        } catch (WindowsAzureInvalidProjectOperationException e) {
            fail("test case failed");
        }
    }

    @Test
    public final void testGetName() {
        assertEquals("WAStorage",wLoSto.getName());
    }



    @Test(expected=IllegalArgumentException.class)
    public final void testSetNameWithEmpty()
            throws WindowsAzureInvalidProjectOperationException {
        wLoSto.setName("");
        assertEquals("WAStorage",wLoSto.getName());
    }

    @Test(expected=IllegalArgumentException.class)
    public final void testSetNameWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        wLoSto.setName("");
        assertEquals("WAStorage",wLoSto.getName());
    }
    @Test
    public final void testSetName()
            throws WindowsAzureInvalidProjectOperationException {
        role.getLocalStorage();
        wLoSto.setName("WAStorage_new");
        assertEquals("WAStorage_new", wLoSto.getName());
    }

    @Test
    public final void testGetSize() {
        assertEquals(2, wLoSto.getSize());
    }


    @Test
    public final void testSetSize() throws WindowsAzureInvalidProjectOperationException {
        wLoSto.setSize(3);
        assertEquals(3, wLoSto.getSize());
    }


    @Test(expected=IllegalArgumentException.class)
    public final void testSetSizeWithLessThan1Value()
            throws WindowsAzureInvalidProjectOperationException {
        wLoSto.setSize(0);
        assertEquals(0, wLoSto.getSize());
    }


    @Test
    public final void testGetCleanOnRecycle() {
        assertEquals(true, wLoSto.getCleanOnRecycle());
    }

    @Test
    public final void testSetCleanOnRecycle()
            throws WindowsAzureInvalidProjectOperationException {
        wLoSto.setCleanOnRecycle(false);
        assertEquals(false, wLoSto.getCleanOnRecycle());
    }

//    @Test
//    public final void testGetPathenv()
//            throws WindowsAzureInvalidProjectOperationException {
//        assertEquals("WAStorage_Path", wLoSto.getPathEnv());
//    }

    @Test
    public final void testSetPathenv()
            throws WindowsAzureInvalidProjectOperationException {
        wLoSto.setPathEnv("WAStorage_Path_new");
        assertEquals("WAStorage_Path_new", wLoSto.getPathEnv());
    }

    @Test
    public final void testSetPathenvWithEmpty()
            throws WindowsAzureInvalidProjectOperationException {
        wLoSto.setPathEnv("");
        assertEquals("", wLoSto.getPathEnv());
    }

    @Test(expected=IllegalArgumentException.class)
    public final void testSetPathenvWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        wLoSto.setPathEnv(null);
        assertEquals("", wLoSto.getPathEnv());
    }

//    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
//    public final void testSetPathenvWithExistingVarName()
//            throws WindowsAzureInvalidProjectOperationException {
//        role.getLocalStorage();
//        wLoSto.setPathEnv("_JAVA_OPTIONS");
//        assertEquals("", wLoSto.getPathEnv());
//    }


    @Test
    public final void testGetPathenvWithNoPathExist()
            throws WindowsAzureInvalidProjectOperationException {
        assertEquals("", role.getLocalStorage().get("WAStorage1").getPathEnv());
    }

    @Test
    public final void testDelete()
            throws WindowsAzureInvalidProjectOperationException {
        wLoSto.delete();
        assertEquals(null, role.getLocalStorage().get("WAStorage"));
    }

    @Test(expected=IllegalArgumentException.class)
    public final void testDeleteLsEnvWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        wLoSto.deleteLsEnv(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public final void testDeleteLsEnvWithEmpty()
            throws WindowsAzureInvalidProjectOperationException {
        wLoSto.deleteLsEnv(null);
    }
}
