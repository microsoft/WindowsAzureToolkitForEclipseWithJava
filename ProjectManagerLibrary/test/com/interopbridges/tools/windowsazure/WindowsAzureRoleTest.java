/**
* Copyright 2011 Persistent Systems Ltd.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.interopbridges.tools.windowsazure;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;


/**
 */
public class WindowsAzureRoleTest {

    private WindowsAzureProjectManager waProjectManager;
    private WindowsAzureProjectManager waCompMgr;
    private WindowsAzureProjectManager waProjErr;
    private WindowsAzureRole waRole;
    private WindowsAzureRole waCompRole;
    private List<WindowsAzureRole> listRoles;
    private List<WindowsAzureRole> listRolesErr;

    @Before
    public final void setUp() {
        File projectDirectoryPath = new File(
                Messages.getString(
                "WinAzureTestConstants.WindowsAzureProj"));
        try {
            waProjectManager = WindowsAzureProjectManager
                                .load(projectDirectoryPath);
            waCompMgr = WindowsAzureProjectManager
                    .load(new File(Messages.getString("WinAzureTestConstants.WAProj")));
            listRoles = waProjectManager.getRoles();

            projectDirectoryPath = new File(
                    Messages.getString(
                    "WinAzureTestConstants.WindowsAzureProjErr"));
            waProjErr = WindowsAzureProjectManager
            .load(projectDirectoryPath);
            listRolesErr = waProjErr.getRoles();

        } catch (WindowsAzureInvalidProjectOperationException e) {
            fail("Exception occured");
        }
    }

    /**
     * Method testGetWindowsAzureProjMgr.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public final void testGetWindowsAzureProjMgr()
    throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureRole role = new WindowsAzureRole(null);
        role.getWinProjMgr();
    }

    @Test
    public final void testGetName() {
        List<String> roleNames = new ArrayList<String>();
        List<String> roleNamesExp = new ArrayList<String>();
        roleNamesExp.add("WorkerRole1");
        roleNamesExp.add("Role1");
        roleNamesExp.add("WorkerRole2");
        roleNamesExp.add("WorkerRole3");
        for (Iterator<WindowsAzureRole> iterator =
            listRoles.iterator(); iterator.hasNext();) {
            WindowsAzureRole windowsAzureRole = (WindowsAzureRole) iterator
                    .next();
            roleNames.add(windowsAzureRole.getName());
        }
        assertEquals("getName", roleNamesExp, roleNames);
    }


    /**
     * Method testSetName.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testSetName()
        throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(1);
            waRole.setName("NewName");
            //waRole.setName("NewName1");
            //waProjectManager.save();
            assertEquals("NewName", waRole.getName());
    }

    /**
     * Method testSetNameWithEmpty.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testSetNameWithEmpty()
        throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(1);
            waRole.setName("");
    }

    /**
     * Method testSetNameWithNull.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testSetNameWithNull()
        throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(1);
            waRole.setName(null);
    }

    /**
     * Method testGetInstances.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetInstances()
    throws WindowsAzureInvalidProjectOperationException {
            List<String> instances = new ArrayList<String>();
            List<String> instancesExp = new ArrayList<String>();
            instancesExp.add("1");
            instancesExp.add("2");
            instancesExp.add("1");
            instancesExp.add("1");
            for (Iterator<WindowsAzureRole> iterator =
                listRoles.iterator(); iterator.hasNext();) {
                WindowsAzureRole windowsAzureRole = (WindowsAzureRole) iterator
                        .next();
                instances.add(windowsAzureRole.getInstances());
            }
            assertEquals("getInstances", instancesExp, instances);
    }

    /**
     * Method testSetInstances.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testSetInstances()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(1);
            waRole.setInstances("4");
            assertEquals("4", waRole.getInstances());
    }

    /**
     * Method testSetInstancesWithEmpty.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testSetInstancesWithEmpty()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(1);
            waRole.setInstances("");
    }

    /**
     * Method testSetInstancesWithNull.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testSetInstancesWithNull()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(1);
            waRole.setInstances(null);
    }

    @Test
    public final void testGetVMSize() {
        List<String> vmSize = new ArrayList<String>();
        List<String> vmSizeExp = new ArrayList<String>();
        vmSizeExp.add("Small");
        vmSizeExp.add("Medium");
        vmSizeExp.add("Small");
        vmSizeExp.add("Small");
        for (Iterator<WindowsAzureRole> iterator =
            listRoles.iterator(); iterator.hasNext();) {
            WindowsAzureRole windowsAzureRole = (WindowsAzureRole) iterator
                    .next();
            vmSize.add(windowsAzureRole.getVMSize());
        }
        assertEquals("getVMSize", vmSizeExp, vmSize);
    }

    /**
     * Method testSetVMSize.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testSetVMSize()
        throws WindowsAzureInvalidProjectOperationException {
        waRole = (WindowsAzureRole) listRoles.get(1);
        waRole.setVMSize("Large");
        assertEquals("Large", waRole.getVMSize());
    }

    /**
     * Method testSetVMSizeWithMissingAttr.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testSetVMSizeWithMissingAttr()
        throws WindowsAzureInvalidProjectOperationException {
        waRole = (WindowsAzureRole) listRolesErr.get(0);
        waRole.setVMSize("Large");
        assertEquals("Large", waRole.getVMSize());
    }

    /**
     * Method testSetVMSizeWithEmpty.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testSetVMSizeWithEmpty()
        throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(1);
            waRole.setVMSize("");
    }

    /**
     * Method testSetVMSizeWithNull.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testSetVMSizeWithNull()
        throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(1);
            waRole.setVMSize(null);
    }

    /**
     * Method testGetEndpoints.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetEndpoints()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(0);
            String []arr = new String[]{"http", "Input", "8080", "80"};
            String []arr1 = new String[]{"InternalEp", "Internal", "34", ""};

            List<WindowsAzureEndpoint> endpt = waRole.getEndpoints();
            WindowsAzureEndpoint waEndpt = endpt.get(0);
            String []arr2 = new String[]{waEndpt.getName(),
                    waEndpt.getEndPointType().toString(),
                    waEndpt.getPrivatePort(),
                    waEndpt.getPort()};
            waEndpt = endpt.get(1);
            String []arr3 = new String[]{waEndpt.getName(),
                    waEndpt.getEndPointType().toString(),
                    waEndpt.getPrivatePort(),
                    waEndpt.getPort()};

            assertArrayEquals(arr, arr2);
            assertArrayEquals(arr1, arr3);
    }

    /**
     * Method testAddEndpoint.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testAddEndpoint()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(1);
            WindowsAzureEndpoint waEndPt = waRole.addEndpoint("endpt",
                    WindowsAzureEndpointType.Input, "9999", "9999");

            List<String> actualEndPt = new ArrayList<String>();
            actualEndPt.add("endpt");
            actualEndPt.add(WindowsAzureEndpointType.Input.toString());
            actualEndPt.add("9999");
            actualEndPt.add("9999");

            List<String> expEndPt = new ArrayList<String>();
            expEndPt.add(waEndPt.getName());
            expEndPt.add(waEndPt.getEndPointType().toString());
            expEndPt.add(waEndPt.getPrivatePort());
            expEndPt.add(waEndPt.getPort());

            assertEquals(expEndPt, actualEndPt);

            actualEndPt.clear();
            expEndPt.clear();
            waEndPt = waRole.addEndpoint("endptInternal",
                    WindowsAzureEndpointType.Internal, "8888", "");

            actualEndPt = new ArrayList<String>();
            actualEndPt.add("endptInternal");
            actualEndPt.add(WindowsAzureEndpointType.Internal.toString());
            actualEndPt.add("8888");
            actualEndPt.add("");

            expEndPt = new ArrayList<String>();
            expEndPt.add(waEndPt.getName());
            expEndPt.add(waEndPt.getEndPointType().toString());
            expEndPt.add(waEndPt.getPrivatePort());
            expEndPt.add(waEndPt.getPort());

            assertEquals(expEndPt, actualEndPt);

            actualEndPt.clear();
            expEndPt.clear();
            waEndPt = waRole.addEndpoint("endptInstance",
                    WindowsAzureEndpointType.InstanceInput, "8081", "99-100");

            actualEndPt = new ArrayList<String>();
            actualEndPt.add("endptInstance");
            actualEndPt.add(WindowsAzureEndpointType.InstanceInput.toString());
            actualEndPt.add("8081");
            actualEndPt.add("99-100");

            expEndPt = new ArrayList<String>();
            expEndPt.add(waEndPt.getName());
            expEndPt.add(waEndPt.getEndPointType().toString());
            expEndPt.add(waEndPt.getPrivatePort());
            expEndPt.add(waEndPt.getPort());

            assertEquals(expEndPt, actualEndPt);
    }

    /**
     * Method testAddEndpointWithMissingEndpoints.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testAddEndpointWithMissingEndpoints()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRolesErr.get(1);
            WindowsAzureEndpoint waEndPt = waRole.addEndpoint("endpt",
                    WindowsAzureEndpointType.Input, "9999", "9999");
            List<String> actualEndPt = new ArrayList<String>();
            actualEndPt.add("endpt");
            actualEndPt.add(WindowsAzureEndpointType.Input.toString());
            actualEndPt.add("9999");
            actualEndPt.add("9999");

            List<String> expEndPt = new ArrayList<String>();
            expEndPt.add(waEndPt.getName());
            expEndPt.add(waEndPt.getEndPointType().toString());
            expEndPt.add(waEndPt.getPrivatePort());
            expEndPt.add(waEndPt.getPort());

            assertEquals(expEndPt, actualEndPt);

            actualEndPt.clear();
            expEndPt.clear();
            waEndPt = waRole.addEndpoint("endptInternal",
                    WindowsAzureEndpointType.Internal, "8888", "");

            actualEndPt = new ArrayList<String>();
            actualEndPt.add("endptInternal");
            actualEndPt.add(WindowsAzureEndpointType.Internal.toString());
            actualEndPt.add("8888");
            actualEndPt.add("");

            expEndPt = new ArrayList<String>();
            expEndPt.add(waEndPt.getName());
            expEndPt.add(waEndPt.getEndPointType().toString());
            expEndPt.add(waEndPt.getPrivatePort());
            expEndPt.add(waEndPt.getPort());

            assertEquals(expEndPt, actualEndPt);
    }

    /**
     * Method testAddEndpointWithNull.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testAddEndpointWithNull()
    throws WindowsAzureInvalidProjectOperationException {
        waRole = (WindowsAzureRole) listRoles.get(1);
        waRole.addEndpoint("endpt", null, "9999", "9999");
        fail("Argument to addEndpoint method can not be null");
    }

    /**
     * Method testIsAvailableEndpointName.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testIsAvailableEndpointName()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(0);
            assertTrue("Check for valid new endpoint name",
                    waRole.isAvailableEndpointName("endpt"));

    }

    /**
     * Method testIsAvailableEndpointNameWithEmpty.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testIsAvailableEndpointNameWithEmpty()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(0);
            assertFalse("Check for empty endpoint name",
                    waRole.isAvailableEndpointName(""));
    }


    /**
     * Method testIsAvailableEndpointNameWithNull.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testIsAvailableEndpointNameWithNull()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(0);
            assertFalse("Check for null endpoint name",
                    waRole.isAvailableEndpointName(null));
    }

    /**
     * Method testIsAvailableEndpointNameWithExistingName.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testIsAvailableEndpointNameWithExistingName()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(0);
            assertFalse("Check for existing endpoint name",
                    waRole.isAvailableEndpointName("InternalEp"));
    }

    /**
     * Method testIsValidEndpointNullInputs.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testIsValidEndpointNullInputs()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(0);
            assertFalse("Endpoint cannot have null as inputs",
                    waRole.isValidEndpoint(null, null, "8899", "8899"));

        }

    /**
     * Method testIsValidEndpointSameIntPublicAndPrivatePort.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testIsValidEndpointSameIntPublicAndPrivatePort()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(0);
            assertFalse("Internal endpoint cannot have same public & private port",
                    waRole.isValidEndpoint("endpt",
                            WindowsAzureEndpointType.Internal, "8899", "8899"));

        }
    /**
     * Method testIsValidEndpointInputPublicAndPrivatePort.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testIsValidEndpointInputPublicAndPrivatePort()
    throws WindowsAzureInvalidProjectOperationException{
            waRole = (WindowsAzureRole) listRoles.get(0);
            assertTrue("Input endpoint can have same public & private port",
                    waRole.isValidEndpoint("endpt",
                            WindowsAzureEndpointType.Input, "8899", "8899"));
        }
    /**
     * Method testIsValidEndpointPortAlreadyInUse.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testIsValidEndpointPortAlreadyInUse()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(0);
            assertFalse("Port already in use cannot be used for new endpoint",
                    waRole.isValidEndpoint("ep",
                            WindowsAzureEndpointType.Input, "21", "34"));
        }

    /**
     * Method testIsValidEndpointWithEmptyPublicPort.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testIsValidEndpointWithEmptyPublicPort()
    throws WindowsAzureInvalidProjectOperationException {
            waRole = (WindowsAzureRole) listRoles.get(0);
            assertTrue("Internal endpoint can have empty public port",
                    waRole.isValidEndpoint("endpt",
                            WindowsAzureEndpointType.Internal, "8899", ""));
     }

    @Test
    public final void testIsValidEndpointInputInstancePort()
    throws WindowsAzureInvalidProjectOperationException{
            waRole = (WindowsAzureRole) listRoles.get(0);
            assertTrue("Input endpoint can have same public & private port",
                    waRole.isValidEndpoint("test1",WindowsAzureEndpointType.InstanceInput, "67", "10-13"));

     }

    @Test
    public final void testIsValidEndpointInputInstancePortEdit()
    throws WindowsAzureInvalidProjectOperationException{
            waRole = (WindowsAzureRole) listRoles.get(0);
            waRole.addEndpoint("test1", WindowsAzureEndpointType.InstanceInput, "76", "77-80");
            assertTrue(waRole.isValidEndpoint("test1",WindowsAzureEndpointType.InstanceInput, "67", "78-83"));
     }

    /**
     * Method testEnableDebugging.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testEnableDebugging()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).enableDebugging(listRoles.get(0)
                .getEndpoints().get(0), true);
        assertTrue(listRoles.get(0).getDebuggingEnabled());
    }

    /**
     * Method testEnableDebuggingWithFalse.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testEnableDebuggingWithFalse()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).enableDebugging(listRoles.get(0)
                .getEndpoints().get(0), false);
        assertTrue(listRoles.get(0).getDebuggingEnabled());
    }


    /**
     * Method testEnableDebuggingWithExistingJavaOption.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testEnableDebuggingWithExistingJavaOption()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(2).enableDebugging(listRoles.get(2)
                .getEndpoints().get(0), true);
        assertTrue(listRoles.get(2).getDebuggingEnabled());
    }

    /**
     * Method testEnableDebuggingWithNullEp.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testEnableDebuggingWithNullEp()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).enableDebugging(null, false);
    }

    /**
     * Method testEnableDebuggingWithNullSuspendval.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testEnableDebuggingWithNullSuspendval()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).enableDebugging(listRoles.get(0)
                .getEndpoints().get(0), null);
    }

    /**
     * Method testDisableDebugging.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testDisableDebugging()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(1).disableDebugging();
        assertFalse(listRoles.get(0).getDebuggingEnabled());
    }

    /**
     * Method testDisableDebuggingWithExistingJavaOption.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testDisableDebuggingWithExistingJavaOption()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(3).disableDebugging();
        assertFalse(listRoles.get(3).getDebuggingEnabled());
    }


    /**
     * Method testGetDebuggingEnabled.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetDebuggingEnabled()
    throws WindowsAzureInvalidProjectOperationException {
        assertTrue(listRoles.get(1).getDebuggingEnabled());
    }

    /**
     * Method testGetDebuggingEnabledWithOtherJavaOptExist.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetDebuggingEnabledWithOtherJavaOptExist()
    throws WindowsAzureInvalidProjectOperationException {
        assertTrue(listRoles.get(3).getDebuggingEnabled());
    }

    /**
     * Method testGetDebuggingEnabledWithFalse.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetDebuggingEnabledWithFalse()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(listRoles.get(0).getDebuggingEnabled());
    }

    /**
     * Method testGetDebuggingEndpoint.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetDebuggingEndpoint()
    throws WindowsAzureInvalidProjectOperationException {
        assertEquals("InputEp3", listRoles.get(1).getDebuggingEndpoint()
                .getName());
    }

    /**
     * Method testGetDebuggingEndpointWithOtherJavaOptExist.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetDebuggingEndpointWithOtherJavaOptExist()
    throws WindowsAzureInvalidProjectOperationException {
        assertEquals("Debugging1", listRoles.get(3).getDebuggingEndpoint()
                .getName());
    }

    /**
     * Method testSetDebuggingEndpoint.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testSetDebuggingEndpoint()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(1).setDebuggingEndpoint(listRoles.get(1)
                .getEndpoints().get(1));
        assertEquals("InputEp2", listRoles.get(1).getDebuggingEndpoint()
                .getName());
    }

    /**
     * Method testSetDebuggingEndpointWithJavaOptExist.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testSetDebuggingEndpointWithJavaOptExist()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(3).setDebuggingEndpoint(listRoles.get(3)
                .getEndpoints().get(1));
        assertEquals("DbgEp", listRoles.get(3).getDebuggingEndpoint()
                .getName());
    }

    /**
     * Method testGetStartSuspended.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetStartSuspended()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(listRoles.get(1).getStartSuspended());
    }

    /**
     * Method testGetStartSuspendedWithJavaOptExist.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetStartSuspendedWithJavaOptExist()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(listRoles.get(1).getStartSuspended());
    }

    /**
     * Method testGetStartSuspendedWithError.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public final void testGetStartSuspendedWithError()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).getStartSuspended();
    }

    /**
     * Method testSetStartSuspended.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testSetStartSuspended()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(1).setStartSuspended(true);
        assertTrue(listRoles.get(1).getStartSuspended());
    }

    /**
     * Method testSetStartSuspendedWithOtherJavaOptExist.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testSetStartSuspendedWithOtherJavaOptExist()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(3).setStartSuspended(false);
        assertFalse(listRoles.get(3).getStartSuspended());
    }

    /**
     * Method testSetStartSuspendedWithFalse.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testSetStartSuspendedWithFalse()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(1).setStartSuspended(false);
        assertFalse(listRoles.get(1).getStartSuspended());
    }

    /**
     * Method testSetStartSuspendedWithNull.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testSetStartSuspendedWithNull()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).setStartSuspended(null);
    }

    /**
     * Method testSetStartSuspendedWithError.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public final void testSetStartSuspendedWithError()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).setStartSuspended(true);
    }

    /**
     * Method testDelete.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testDelete()
    throws WindowsAzureInvalidProjectOperationException {
            listRoles.get(0).delete();
            assertEquals(3, listRoles.size());
    }

    /**
     * Method testGetRuntimeEnv.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetRuntimeEnv()
    throws WindowsAzureInvalidProjectOperationException {
            Map <String, String> map = listRoles.get(1).getRuntimeEnv();
            assertTrue(map.containsKey("_JAVA_OPTIONS")
                    && map.containsKey("ECLIPSE_HOME"));
    }
    /**
     * Method testGetRuntimeEnvWhereVariableNotExist.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetRuntimeEnvWhereVariableNotExist()
    throws WindowsAzureInvalidProjectOperationException {
            Map <String, String> map = listRoles.get(0).getRuntimeEnv();
            assertTrue(map.isEmpty());
    }

    /**
     * Method testGetRuntimeEnvWithNull.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testGetRuntimeEnvWithNull()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).getRuntimeEnv(null);
    }

    /**
     * Method testGetRuntimeEnvWithEmpty.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testGetRuntimeEnvWithEmpty()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).getRuntimeEnv("");
    }

    /**
     * Method testGetRuntimeEnvForEclipseHome.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetRuntimeEnvForEclipseHome()
    throws WindowsAzureInvalidProjectOperationException {
        assertEquals("c:\\eclipse",
                listRoles.get(1).getRuntimeEnv("ECLIPSE_HOME"));
    }

    /**
     * Method testGetRuntimeEnvWhenVarNotExist.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetRuntimeEnvWhenVarNotExist()
    throws WindowsAzureInvalidProjectOperationException {
        assertEquals("", listRoles.get(1).getRuntimeEnv("JAVA_HOME"));
    }

//    /**
//     * Method testSetRuntimeEnvWithNull.
//     * @throws WindowsAzureInvalidProjectOperationException
//     */
//    @Test(expected=IllegalArgumentException.class)
//    public final void testSetRuntimeEnvWithNull()
//    throws WindowsAzureInvalidProjectOperationException {
//        //listRoles.get(0).setEnv(null, null);
//    }
//
//    /**
//     * Method testSetRuntimeEnvWithEmpty.
//     * @throws WindowsAzureInvalidProjectOperationException
//     */
//    @Test(expected=IllegalArgumentException.class)
//    public final void testSetRuntimeEnvWithEmpty()
//    throws WindowsAzureInvalidProjectOperationException {
//        //listRoles.get(0).setEnv("", "");
//    }

    /**
     * Method testSetRuntimeEnvForEclipseHome.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testSetRuntimeEnvForEclipseHome()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(1).getRuntimeEnv();
        listRoles.get(1).setRuntimeEnv("ECLIPSE_HOME_1", "C:\\test");
        assertEquals("C:\\test", listRoles.get(1).getRuntimeEnv(
                "ECLIPSE_HOME_1"));
    }

    /**
     * Method testSetRuntimeEnvWhenRunTimeTagNotExist.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testSetRuntimeEnvWhenRunTimeTagNotExist()
    throws WindowsAzureInvalidProjectOperationException {
        listRolesErr.get(1).getRuntimeEnv();
        listRolesErr.get(1).setRuntimeEnv("ECLIPSE_HOME", "C:\\test");
        assertEquals("C:\\test",
                listRolesErr.get(1).getRuntimeEnv("ECLIPSE_HOME"));
    }

    /**
     * Method testDeleteRuntimeEnv.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testDeleteRuntimeEnv()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(1).getRuntimeEnv();
        listRoles.get(1).renameRuntimeEnv("JBOSS_HOME", "");
        assertEquals("", listRoles.get(1).getRuntimeEnv(
                "JBOSS_HOME"));
    }

    /**
     * Method testDeleteRuntimeEnvWhenVarNotExist.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public final void testDeleteRuntimeEnvWhenVarNotExist()
    throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(1).renameRuntimeEnv("TOMCAT_HOME", "");
        assertEquals("", listRoles.get(1).getRuntimeEnv(
                "TOMCAT_HOME"));
    }

    /**
     * Method testGetLocalStorageWhenLSNotExist.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    @Test
    public final void testGetLocalStorageWhenLSNotExist()
            throws WindowsAzureInvalidProjectOperationException {
        Map<String, WindowsAzureLocalStorage> map =
                listRoles.get(0).getLocalStorage();
        assertTrue(map.isEmpty());
    }

    /**
     * Method testGetLocalStorageWithMap.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    @Test
    public final void testGetLocalStorageWithMap()
            throws WindowsAzureInvalidProjectOperationException {
        Map<String, WindowsAzureLocalStorage> map =
                listRoles.get(1).getLocalStorage();
        assertTrue(map.containsKey("MyStorage1"));
    }

    /**
     * Method testGetLocalStorageWithNull.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testGetLocalStorageWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureLocalStorage winLs = listRoles.get(1).getLocalStorage(null);
        assertEquals("MyStorage1", winLs.getName());
    }

    /**
     * Method testGetLocalStorageWithEmpty.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    @Test(expected=IllegalArgumentException.class)
    public final void testGetLocalStorageWithEmpty()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureLocalStorage winLs = listRoles.get(1).getLocalStorage("");
        assertEquals("MyStorage1", winLs.getName());
        //waProjectManager.save();
    }

    /**
     * Method testGetLocalStorage.
     * @throws WindowsAzureInvalidProjectOperationException
     */
    @Test
    public final void testGetLocalStorage()
            throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureLocalStorage winLs =
                listRoles.get(1).getLocalStorage("MyStorage1");
        assertEquals("MyStorage1", winLs.getName());
    }

    /**
     * Method testAddLocalStorage.
     * @throws WindowsAzureInvalidProjectOperationException .
     */
    @Test
    public final void testAddLocalStorage()
            throws WindowsAzureInvalidProjectOperationException {
       listRoles.get(2).addLocalStorage(
                "MyStorage4", 2, true, "MyStorage4_Path");

    }

    @Test(expected=IllegalArgumentException.class)
    public final void testAddLocalStorageWithInvalidArgsName()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(2).getLocalStorage();
        WindowsAzureLocalStorage winLs = listRoles.get(2).addLocalStorage(
              null, 2, true, "MyStorage_Path");
      assertEquals("MyStorage2", winLs.getName());
    }

    @Test(expected=IllegalArgumentException.class)
    public final void testAddLocalStorageWithInvalidArgsSize()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(2).getLocalStorage();
        WindowsAzureLocalStorage winLs = listRoles.get(2).addLocalStorage(
              "MyStorage2", -1, true, "MyStorage_Path");
      assertEquals("MyStorage2", winLs.getName());
    }

    @Test(expected=IllegalArgumentException.class)
    public final void testAddLocalStorageWithInvalidArgsPath()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(2).getLocalStorage();
        WindowsAzureLocalStorage winLs = listRoles.get(2).addLocalStorage(
              "MyStorage2", 2, true, "");
      assertEquals("MyStorage2", winLs.getName());
    }

    @Test()
    public final void testGetJDKSourcePath()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).setJDKSourcePath("c:\\jdk", new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertEquals("c:\\jdk",listRoles.get(0).getJDKSourcePath());
    }


    @Test()
    public final void testSetJDKSourcePath()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).setJDKSourcePath("c:\\jdk", new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertEquals("c:\\jdk",listRoles.get(0).getJDKSourcePath());
    }

    @Test()
    public final void testSetJDKSourcePathWhenJDKExist()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).setJDKSourcePath("c:\\jdk", new File(Messages.getString("WinAzureTestConstants.Comp")));
        listRoles.get(0).setJDKSourcePath("c:\\jdk1", new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertEquals("c:\\jdk1",listRoles.get(0).getJDKSourcePath());
    }


    @Test(expected=IllegalArgumentException.class)
    public final void testSetJDKSourcePathWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).setJDKSourcePath(null, null);
    }

    @Test()
    public final void testSetJDKSourcePathWithPathNull()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).setJDKSourcePath(null, new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertEquals(null,listRoles.get(0).getJDKSourcePath());
    }


    @Test
    public final void testAddComponent() throws WindowsAzureInvalidProjectOperationException {
        waRole = (WindowsAzureRole) listRoles.get(0);
        List<WindowsAzureRoleComponent> Comp = waRole.getComponents();
        waRole.addComponent("importas", "myApp.war");
        boolean isExist = true;
        for (int i = 0; i < Comp.size(); i++) {
            if(Comp.get(i).getDeployName().equalsIgnoreCase("myApp.war")){
                isExist = true;
            }
        assertTrue(isExist);
        }
    }

    @Test
    public final void testAddComponentWithIPath() throws WindowsAzureInvalidProjectOperationException {
        waRole = (WindowsAzureRole) listRoles.get(0);
        List<WindowsAzureRoleComponent> Comp = waRole.getComponents();
        waRole.addComponent("importsrc", "c:\\myApp.war");
        boolean isExist = true;
        for (int i = 0; i < Comp.size(); i++) {
            if(Comp.get(i).getImportPath().equalsIgnoreCase("c:\\myApp.war")){
                isExist = true;
            }
        assertTrue(isExist);
        }
    }


    @Test
    public final void testGetIsEnvPreconfigured() throws WindowsAzureInvalidProjectOperationException {
        waCompRole = waCompMgr.getRoles().get(1);
        waCompRole.getIsEnvPreconfigured("JAVA_HOME");
        }

    @Test(expected=IllegalArgumentException.class)
    public final void testGetIsEnvPreconfiguredWithNull() throws WindowsAzureInvalidProjectOperationException {
        waCompRole = waCompMgr.getRoles().get(1);
        waCompRole.getIsEnvPreconfigured(null);
        }

    @Test(expected=IllegalArgumentException.class)
    public final void testGetIsEnvPreconfiguredWithEmpty() throws WindowsAzureInvalidProjectOperationException {
        waCompRole = waCompMgr.getRoles().get(1);
        waCompRole.getIsEnvPreconfigured("");
    }



    @Test(expected=IllegalArgumentException.class)
    public final void testGetRuntimeEnvTypeWithNull() throws WindowsAzureInvalidProjectOperationException {
        waCompRole = waCompMgr.getRoles().get(1);
        waCompRole.getRuntimeEnvType(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public final void testGetRuntimeEnvTypeWithEmpty() throws WindowsAzureInvalidProjectOperationException {
        waCompRole = waCompMgr.getRoles().get(1);
        waCompRole.getRuntimeEnvType("");
    }


    @Test()
    public final void testsetServer()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).setServer("Apache Tomcat 7", "c:\\tomcat", new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertEquals("Apache Tomcat 7", listRoles.get(0).getServerName());
    }

    @Test()
    public final void testsetServerwithNull()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).setServer("Apache Tomcat 7", "c:\\tomcat", new File(Messages.getString("WinAzureTestConstants.Comp")));
        listRoles.get(0).setServer(null, "c:\\tomcat", new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertNull(listRoles.get(0).getServerName());
    }

    @Test(expected=IllegalArgumentException.class)
    public final void testSetServerWithPathNull()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).setServer("Apache Tomcat 7", null, new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertEquals("Apache Tomcat 7", listRoles.get(0).getServerName());
    }

    @Test()
    public final void testGetServerSourcePath()
            throws WindowsAzureInvalidProjectOperationException {
        listRoles.get(0).setServer("Apache Tomcat 7", "c:\\tomcat", new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertEquals("c:\\tomcat", listRoles.get(0).getServerSourcePath());
    }

    @Test()
    public final void testGetServerApplications()
            throws WindowsAzureInvalidProjectOperationException {
        waCompMgr.getRoles().get(0).setServer("Apache Tomcat 7", "C:\\tomcat", new File(Messages.getString("WinAzureTestConstants.Comp")));
        waCompMgr.getRoles().get(0).addServerApplication("c:\\xcopy", "xcopy.exe", "copy", new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertEquals(1, waCompMgr.getRoles().get(0).getServerApplications().size());
    }

    @Test()
    public final void testaddServerApplication()
            throws WindowsAzureInvalidProjectOperationException {
        waCompMgr.getRoles().get(0).setServer("Apache Tomcat 7", "C:\\tomcat", new File(Messages.getString("WinAzureTestConstants.Comp")));
        waCompMgr.getRoles().get(0).addServerApplication("c:\\xcopy", "xcopy.exe", "copy", new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertEquals(1, waCompMgr.getRoles().get(0).getServerApplications().size());
    }

    @Test(expected=IllegalArgumentException.class)
    public final void testaddServerApplicationWithNull()
            throws WindowsAzureInvalidProjectOperationException {
        waCompMgr.getRoles().get(0).setServer("Apache Tomcat 7", "C:\\tomcat", new File(Messages.getString("WinAzureTestConstants.Comp")));
        waCompMgr.getRoles().get(0).addServerApplication(null, null, null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public final void testaddServerApplicationWithEmpty()
            throws WindowsAzureInvalidProjectOperationException {
        waCompMgr.getRoles().get(0).setServer("Apache Tomcat 7", "C:\\tomcat", new File(Messages.getString("WinAzureTestConstants.Comp")));
        waCompMgr.getRoles().get(0).addServerApplication("", "", "", null);
    }


    @Test()
    public final void testremoveServerApplication()
            throws WindowsAzureInvalidProjectOperationException {
        waCompMgr.getRoles().get(0).setServer("Apache Tomcat 7", "C:\\tomcat", new File(Messages.getString("WinAzureTestConstants.Comp")));
        waCompMgr.getRoles().get(0).addServerApplication("c:\\xcopy", "xcopy.exe", "copy", new File(Messages.getString("WinAzureTestConstants.Comp")));
        waCompMgr.getRoles().get(0).removeServerApplication("xcopy.exe");
        assertEquals(0, waCompMgr.getRoles().get(0).getServerApplications().size());
    }

    @Test()
    public final void testSetServerSourcePath()
            throws WindowsAzureInvalidProjectOperationException {
        waCompMgr.getRoles().get(3).setServerSourcePath("GlassFish OSE 3", "D:\\testServerPath", new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertEquals("D:\\testServerPath", waCompMgr.getRoles().get(3).getServerSourcePath());
    }


    @Test()
    public final void testIsValidDeployNameWithEmpty()
            throws WindowsAzureInvalidProjectOperationException {
        assertFalse(waCompMgr.getRoles().get(0).isValidDeployName(""));
    }


    @Test()
    public final void testIsValidDeployName()
            throws WindowsAzureInvalidProjectOperationException {
        waCompMgr.getRoles().get(0).addServerApplication("c:\\xcopy", "xcopy.exe", "copy", new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertFalse(waCompMgr.getRoles().get(0).isValidDeployName("xcopy.exe"));
    }

    @Test()
    public final void testIsValidDeployNameTrue()
            throws WindowsAzureInvalidProjectOperationException {
        waCompMgr.getRoles().get(0).addServerApplication("c:\\xcopy", "xcopy.exe", "copy", new File(Messages.getString("WinAzureTestConstants.Comp")));
        assertTrue(waCompMgr.getRoles().get(0).isValidDeployName("xcopy1.exe"));
    }

}

