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
package com.interopbridges.tools.windowsazure;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class WindowsAzureEndpointSATest {

    private WindowsAzureProjectManager wProj = null;
    private WindowsAzureRole role = null;
    private WindowsAzureRole saRole = null;
    @Before
    public final void setUp() {
        try {
            wProj = WindowsAzureProjectManager.load(
                    new File(Messages.getString("WinAzureTestConstants.SAWindowsAzureProj")));
            role = wProj.getRoles().get(0);
            saRole = wProj.getRoles().get(1);

        } catch (WindowsAzureInvalidProjectOperationException e) {
            e.printStackTrace();
            fail("test case failed");
        }
    }

    @Test
    public void testConfigureSessionAffinity()
    throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureEndpoint windowsAzureEndpoint = role.getEndpoint("http") ;
        role.setSessionAffinityInputEndpoint(windowsAzureEndpoint) ;
        WindowsAzureEndpoint saEndPt = role.getSessionAffinityInputEndpoint() ;
        assertEquals(windowsAzureEndpoint.getName(),saEndPt.getName());
    }


    @Test
    public void testGetSessionAffinityInputEndPoint()
    throws WindowsAzureInvalidProjectOperationException {

        WindowsAzureEndpoint saEndPt =  saRole.getSessionAffinityInputEndpoint() ;
        assertEquals("http1",saEndPt.getName());
    }

    @Test
    public void testGetSessionAffinityInternalEndPoint()
    throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureEndpoint windowsAzureEndpoint = saRole.getSessionAffinityInternalEndpoint() ;
        assertEquals("http1_SESSION_AFFINITY",windowsAzureEndpoint.getName());
    }

    @Test
    public void testSetInputEndPointName()
    throws WindowsAzureInvalidProjectOperationException {
           WindowsAzureEndpoint windowsAzureEndpoint = saRole.getEndpoint("http1") ;
           windowsAzureEndpoint.setName("http2") ;
           WindowsAzureEndpoint saEndPt = saRole.getSessionAffinityInputEndpoint() ;
           assertEquals(windowsAzureEndpoint.getName(),saEndPt.getName());
     }

    @Test
    public void testSetInternalEndPointName()
    throws WindowsAzureInvalidProjectOperationException {
           WindowsAzureEndpoint windowsAzureEndpoint = saRole.getEndpoint("http1_SESSION_AFFINITY") ;
           windowsAzureEndpoint.setName("http2_SESSION_AFFINITY") ;
           assertEquals("http2_SESSION_AFFINITY",windowsAzureEndpoint.getName());
     }

    @Test
    public void testDisableSessionAffinity()
    throws WindowsAzureInvalidProjectOperationException {
         saRole.setSessionAffinityInputEndpoint(null);
         assertNull(saRole.getSessionAffinityInputEndpoint());
     }

     @Test
     public void testChangeRoleName()
     throws WindowsAzureInvalidProjectOperationException {
         saRole.setName("newworkerrole");
         assertEquals("newworkerrole",saRole.getName());
      }
}
