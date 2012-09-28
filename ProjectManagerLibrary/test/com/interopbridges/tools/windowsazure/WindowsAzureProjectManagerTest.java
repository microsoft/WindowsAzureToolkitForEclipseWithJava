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
package com.interopbridges.tools.windowsazure;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WindowsAzureProjectManagerTest {

    static WindowsAzureProjectManager wProj = null;
    static WindowsAzureProjectManager wProjErr = null;
    static WindowsAzureProjectManager wProjErr1 = null;
    static WindowsAzureProjectManager wProjSa = null;

    @Before
    public void setUp() throws Exception {

        wProj = WindowsAzureProjectManager.load(new File(
                Messages.getString("WinAzureTestConstants.WindowsAzureProj")));
        wProjErr = WindowsAzureProjectManager.load(new File(
                Messages.getString("WinAzureTestConstants.WindowsAzureProjErr")));
        wProjErr1 = WindowsAzureProjectManager.load(new File(
                Messages.getString("WinAzureTestConstants.WindowsAzureProjErr1")));
        wProjSa = WindowsAzureProjectManager.load(
                new File(Messages.getString("WinAzureTestConstants.SAWindowsAzureProj")));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateWithEmpty()
    throws WindowsAzureInvalidProjectOperationException, IOException {
        WindowsAzureProjectManager.create("");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testCreateWithNull()
        throws WindowsAzureInvalidProjectOperationException, IOException {
        WindowsAzureProjectManager.create(null);
    }

    @Test(expected=IOException.class)
    public void testCreateWithInvalidPath()
        throws WindowsAzureInvalidProjectOperationException, IOException {
        String zipName = Messages.getString("WinAzureTestConstants.WindowsAzureProjErr")
        + Messages.getString("WinAzureTestConstants.errZip");
        WindowsAzureProjectManager.create(zipName);
    }

    @Test
    public void testCreate()
    throws WindowsAzureInvalidProjectOperationException, IOException {
        WindowsAzureProjectManager wProjMgr = null;
        wProjMgr = WindowsAzureProjectManager.create(
                Messages.getString("WinAzureTestConstants.StarterKit"));
        assertNotNull(wProjMgr);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testLoadWithNull()
    throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureProjectManager.load(null);
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testLoadWithInvalidPackageXML()
    throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureProjectManager.load(new File(
                Messages.getString("WinAzureTestConstants.WindowsAzureProjErr2")));
    }

    @Test
    public void testLoad()
    throws WindowsAzureInvalidProjectOperationException {
        wProj = WindowsAzureProjectManager.load(new File(
                Messages.getString("WinAzureTestConstants.WindowsAzureProj")));
        assertNotNull(wProj);
    }

    @Test
    public void testIsAvailableRoleNameWithEmpty()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj.isAvailableRoleName(""));
    }

    @Test
    public void testIsAvailableRoleNameWithNull()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj.isAvailableRoleName(null));
    }

    @Test
    public void testIsAvailableRoleNameWithExistingRole()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj.isAvailableRoleName("WorkerRole1"));
    }

    @Test
    public void testIsAvailableRoleNameWithNewRole()
    throws WindowsAzureInvalidProjectOperationException {
        assertTrue(wProj.isAvailableRoleName("newRole"));
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testIsAvailableRoleNameWithException()
    throws WindowsAzureInvalidProjectOperationException {
        wProjErr1.isAvailableRoleName("newRole");
    }

    @Test
    public void testIsValidServiceNameWithEmpty()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj.isValidServiceName(""));
    }

    @Test
    public void testIsValidServiceNameWithNull()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj.isValidServiceName(null));
    }

    @Test
    public void testIsValidServiceNameWithExistingName()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj
                .isValidServiceName("WindowsAzureProject"));
    }

    @Test
    public void testIsValidServiceNameWithNewName()
    throws WindowsAzureInvalidProjectOperationException {
        assertTrue(wProj.isValidServiceName("newService"));
    }

    @Test
    public void testIsValidPortWithEmptyInputPort()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj.isValidPort("", WindowsAzureEndpointType.Input));
    }

    @Test
    public void testIsValidPortWithEmptyNonNumaricValue()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj.isValidPort("ggg", WindowsAzureEndpointType.Input));
    }

    @Test
    public void testIsValidPortWithEmptyInternal()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj
                .isValidPort("", WindowsAzureEndpointType.Internal));
    }

    @Test
    public void testIsValidPortWithNullPortType()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj.isValidPort("8080", null));
    }

    @Test
    public void testIsValidPortWithBothNull()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj.isValidPort(null, null));
    }

    @Test
    public void testIsValidPortWithExistingInputPort()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj.isValidPort("80",
                WindowsAzureEndpointType.Input));
    }

    @Test
    public void testIsValidPortWithNewInputPort()
    throws WindowsAzureInvalidProjectOperationException {
        assertTrue(wProj.isValidPort("20", WindowsAzureEndpointType.Input));
    }

    @Test
    public void testIsValidPortWithExistingInternalPort()
    throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProj.isValidPort("34",
                WindowsAzureEndpointType.Internal));
    }

    @Test
    public void testIsValidPortWithNewInternalPort()
    throws WindowsAzureInvalidProjectOperationException {
        assertTrue(wProj.isValidPort("20", WindowsAzureEndpointType.Internal));
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testIsValidPortWithException()
    throws WindowsAzureInvalidProjectOperationException {
        assertTrue(wProjErr1.isValidPort("20", WindowsAzureEndpointType.Internal));
    }


    @Test
    public void testGetServiceName()
    throws WindowsAzureInvalidProjectOperationException {
        assertEquals("WindowsAzureProject",
                wProj.getServiceName());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetServiceNameWithEmpty()
    throws WindowsAzureInvalidProjectOperationException {
        wProj.setServiceName("");
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testSetServiceNameWithException()
    throws WindowsAzureInvalidProjectOperationException {
        wProjErr1.setServiceName("newService");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetServiceNameWithNull()
    throws WindowsAzureInvalidProjectOperationException {
        wProj.setServiceName(null);
    }

    @Test
    public void testSetServiceName()
    throws WindowsAzureInvalidProjectOperationException {
        wProj.setServiceName("NewService");
        assertEquals("NewService", wProj.getServiceName());
    }

    @Test
    public void testGetPackageType()
    throws WindowsAzureInvalidProjectOperationException {
        assertEquals(WindowsAzurePackageType.LOCAL,
                wProj.getPackageType());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetPackageTypeWithNull()
    throws WindowsAzureInvalidProjectOperationException {
        wProj.setPackageType(null);
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testSetPackageTypeWithException()
    throws WindowsAzureInvalidProjectOperationException {
        wProjErr1.setPackageType(WindowsAzurePackageType.CLOUD);
    }

    @Test
    public void testSetPackageType()
    throws WindowsAzureInvalidProjectOperationException {
        wProj.setPackageType(WindowsAzurePackageType.CLOUD);
        assertEquals(WindowsAzurePackageType.CLOUD,
                wProj.getPackageType());
    }

    @Test
    public void testGetRoles()
    throws WindowsAzureInvalidProjectOperationException {
        List<WindowsAzureRole> roles = wProj.getRoles();
        assertEquals(4, wProj.getRoles().size());
        assertEquals("WorkerRole1", roles.get(0).getName());
        assertEquals("Role1", roles.get(1).getName());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddRoleWithEmpty()
    throws WindowsAzureInvalidProjectOperationException {
        wProj.addRole("","");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddRoleWithNull()
    throws WindowsAzureInvalidProjectOperationException {
        wProj.addRole(null, null);
    }

    @Test
    public void testAddRole()
    throws WindowsAzureInvalidProjectOperationException {
            assertEquals(4, wProj.getRoles().size());
            wProj.addRole("NewRole", Messages.getString("WinAzureTestConstants.StarterKit"));
            assertEquals(5, wProj.getRoles().size());
            assertEquals("NewRole", wProj.getRoles().get(4).getName());
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testAddRoleWithException()
    throws WindowsAzureInvalidProjectOperationException {
        wProjErr1.addRole("NewRole",  Messages.getString("WinAzureTestConstants.StarterKit"));
    }

    @Test
    public void testRoleFromPath()
        throws WindowsAzureInvalidProjectOperationException {
        File path = new File(Messages.getString("WinAzureTestConstants.WindowsAzureProj")+ "\\Role1");
        List<WindowsAzureRole> roles = wProj.getRoles();
        WindowsAzureRole roleExp = roles.get(1);
        WindowsAzureRole roleActual = wProj.roleFromPath(path);
        assertEquals(roleExp.getName(), roleActual.getName());
        assertEquals(roleExp.getInstances(), roleActual.getInstances());
        assertEquals(roleExp.getVMSize(), roleActual.getVMSize());
    }

    @Test
    public void testRoleFromPathNonRoleFolder()
        throws WindowsAzureInvalidProjectOperationException {
        File path = new File("C:\\WindowsAzureProject\\role1\\approot");
        assertEquals(null, wProj.roleFromPath(path));
    }

    @Test
    public void testRoleFromPathNoParentFolder()
        throws WindowsAzureInvalidProjectOperationException {
        File path = new File("C:\\WindowsAzureProject");
        assertEquals(null, wProj.roleFromPath(path));
    }

    @Test
    public void testRoleFromPathFolderDoesNotExist()
        throws WindowsAzureInvalidProjectOperationException {
        File path = new File("C:\\WindowsAzureProject\\doesntexist");
        assertEquals(null, wProj.roleFromPath(path));
    }

    @Test
    public void testRoleFromPathEmptyPath()
        throws WindowsAzureInvalidProjectOperationException {
        File path = new File("");
        assertEquals(null, wProj.roleFromPath(path));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRoleFromPathWithNull()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.roleFromPath(null);
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testRoleFromPathWithException()
        throws WindowsAzureInvalidProjectOperationException {
        //File path = new File(Messages.getString("WinAzureTestConstants.WindowsAzureProjErr1")+ "\\role1");
        File path = new File("C:\\WindowsAzureProjectErr1\\role1");

        wProjErr1.roleFromPath(path);
    }

    @Test
    public void testSetRemoteAccessAllRolesWithTrue()
        throws WindowsAzureInvalidProjectOperationException, XPathExpressionException {
        wProj.setRemoteAccessAllRoles(false);
        wProj.setRemoteAccessAllRoles(true);

        //Check in package.xml
        assertTrue(wProj.getRemoteAccessAllRoles());

        //Check entries in configuration file
        XPath xPath = XPathFactory.newInstance().newXPath();
        Document doc = wProj.getConfigFileDoc();
        NodeList listRoles = (NodeList) xPath.evaluate(
                WindowsAzureConstants.ROLE, doc,
                XPathConstants.NODESET);
        NodeList listSetting = (NodeList) xPath.evaluate(
                WindowsAzureConstants.ROLE
                + "/ConfigurationSettings/Setting[@name='"
                + WindowsAzureConstants.REMOTEACCESS_ENABLED
                + "']", doc, XPathConstants.NODESET);
        assertEquals(listRoles.getLength(), listSetting.getLength());

        listSetting = (NodeList) xPath.evaluate(
                WindowsAzureConstants.ROLE
                + "/ConfigurationSettings/Setting[@name='"
                + WindowsAzureConstants.REMOTEFORWARDER_ENABLED
                + "']", doc, XPathConstants.NODESET);
        assertEquals(1, listSetting.getLength());

        //Check entries in definition file
        xPath = XPathFactory.newInstance().newXPath();
        doc = wProj.getdefinitionFileDoc();
        listRoles = (NodeList) xPath.evaluate(
                WindowsAzureConstants.WORKER_ROLE, doc,
                XPathConstants.NODESET);

        listSetting = (NodeList) xPath.evaluate(
                WindowsAzureConstants.WORKER_ROLE
                + "/Imports/Import[@moduleName='RemoteAccess']"
                , doc, XPathConstants.NODESET);
        assertEquals(listRoles.getLength(), listSetting.getLength());
        listSetting = (NodeList) xPath.evaluate(
                WindowsAzureConstants.WORKER_ROLE
                + "/Imports/Import[@moduleName='RemoteForwarder']"
                , doc, XPathConstants.NODESET);
        assertEquals(1, listSetting.getLength());
    }

    @Test
    public void testSetRemoteAccessAllRolesWithEmptyNodes()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr1.setRemoteAccessAllRoles(true);
    }

    @Test
    public void testSetRemoteAccessAllRolesWithFalse()
        throws WindowsAzureInvalidProjectOperationException, XPathExpressionException {
        wProj.setRemoteAccessAllRoles(false);

        //Check package.xml
        assertFalse(wProj.getRemoteAccessAllRoles());

        //Check entries in configuration file
        XPath xPath = XPathFactory.newInstance().newXPath();
        Document doc = wProj.getConfigFileDoc();
        NodeList listSetting = (NodeList) xPath.evaluate(
                WindowsAzureConstants.ROLE
                + "/ConfigurationSettings/Setting[@name='"
                + WindowsAzureConstants.REMOTEACCESS_ENABLED
                + "']", doc, XPathConstants.NODESET);
        assertEquals(0, listSetting.getLength());

        listSetting = (NodeList) xPath.evaluate(
                WindowsAzureConstants.ROLE
                + "/ConfigurationSettings/Setting[@name='"
                + WindowsAzureConstants.REMOTEACCESS_USERNAME
                + "']", doc, XPathConstants.NODESET);
        assertEquals(0, listSetting.getLength());
        listSetting = (NodeList) xPath.evaluate(
                WindowsAzureConstants.ROLE
                + "/ConfigurationSettings/Setting[@name='"
                + WindowsAzureConstants.REMOTEACCESS_PASSWORD
                + "']", doc, XPathConstants.NODESET);
        assertEquals(0, listSetting.getLength());
        listSetting = (NodeList) xPath.evaluate(
                WindowsAzureConstants.ROLE
                + "/ConfigurationSettings/Setting[@name='"
                + WindowsAzureConstants.REMOTEACCESS_EXPIRY
                + "']", doc, XPathConstants.NODESET);
        assertEquals(0, listSetting.getLength());
        listSetting = (NodeList) xPath.evaluate(
                WindowsAzureConstants.ROLE
                + "/ConfigurationSettings/Setting[@name='"
                + WindowsAzureConstants.REMOTEFORWARDER_ENABLED
                + "']", doc, XPathConstants.NODESET);
        assertEquals(0, listSetting.getLength());
        Element eleCertificate = (Element) xPath.evaluate(
                WindowsAzureConstants.ROLE
                + "/Certificates/Certificate[@name='"
                + WindowsAzureConstants.REMOTEACCESS_FINGERPRINT
                + "']", doc, XPathConstants.NODE);
        assertEquals(null, eleCertificate);
        //Check entries in definition file
        xPath = XPathFactory.newInstance().newXPath();
        doc = wProj.getdefinitionFileDoc();
        listSetting = (NodeList) xPath.evaluate(
                WindowsAzureConstants.WORKER_ROLE
                + "/Imports/Import[@moduleName='RemoteAccess']"
                , doc, XPathConstants.NODESET);
        assertEquals(0, listSetting.getLength());
        listSetting = (NodeList) xPath.evaluate(
                WindowsAzureConstants.WORKER_ROLE
                + "/Imports/Import[@moduleName='RemoteForwarder']"
                , doc, XPathConstants.NODESET);
        assertEquals(0, listSetting.getLength());
    }

    @Test
    public void testSetRemoteAccessAllRolesWithTrueAndIncompleteData()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr.setRemoteAccessAllRoles(true);
        assertTrue(wProjErr.getRemoteAccessAllRoles());
    }

    @Test
    public void testSetRemoteAccessAllRolesWithFalseAndIncompleteData()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr.setRemoteAccessAllRoles(false);
        assertFalse(wProjErr.getRemoteAccessAllRoles());
    }

    @Test
    public void testGetRemoteAccessAllRolesWithTrue()
        throws WindowsAzureInvalidProjectOperationException {
        assertTrue(wProj.getRemoteAccessAllRoles());
    }

    @Test
    public void testGetRemoteAccessAllRolesWithFalse()
        throws WindowsAzureInvalidProjectOperationException {
        assertFalse(wProjErr.getRemoteAccessAllRoles());
    }

    @Test
    public void testGetRemoteAccessUsername()
        throws WindowsAzureInvalidProjectOperationException {
        assertEquals("test", wProj.getRemoteAccessUsername());
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testGetRemoteAccessUsernameWithException()
        throws WindowsAzureInvalidProjectOperationException {
        assertEquals("test", wProjErr.getRemoteAccessUsername());
    }

    @Test
    public void testSetRemoteAccessUsername()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.setRemoteAccessUsername("testUsername");
        assertEquals("testUsername", wProj.getRemoteAccessUsername());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetRemoteAccessUsernameWithNull()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.setRemoteAccessUsername(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetRemoteAccessUsernameWithEmpty()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.setRemoteAccessUsername("");
    }

    @Test
    public void testSetRemoteAccessUsernameWithEmptySetting()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr.setRemoteAccessUsername("testUsername");
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testSetRemoteAccessUsernameWithException()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr1.setRemoteAccessUsername("testUsername");
    }

    @Test
    public void testGetRemoteAccessEncryptedPassword()
        throws WindowsAzureInvalidProjectOperationException {
        assertEquals("$todo:replaceWithYourOwn", wProj.getRemoteAccessEncryptedPassword());
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testGetRemoteAccessEncryptedPasswordWithException()
        throws WindowsAzureInvalidProjectOperationException {
        assertEquals("$todo:replaceWithYourOwn", wProjErr.getRemoteAccessEncryptedPassword());
    }

    @Test
    public void testSetRemoteAccessEncryptedPassword()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.setRemoteAccessEncryptedPassword("testPassword");
        assertEquals("testPassword", wProj.getRemoteAccessEncryptedPassword());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetRemoteAccessEncryptedPasswordWithNull()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.setRemoteAccessEncryptedPassword(null);
    }

    @Test
    public void testSetRemoteAccessEncryptedPasswordWithEmpty()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.setRemoteAccessEncryptedPassword("");
        assertEquals("", wProj.getRemoteAccessEncryptedPassword());
    }

    @Test
    public void testSetRemoteAccessEncryptedPasswordWithEmptySetting()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr.setRemoteAccessEncryptedPassword("testPassword");
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testSetRemoteAccessEncryptedPasswordWithException()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr1.setRemoteAccessEncryptedPassword("testPassword");
    }

    @Test
    public void testGetRemoteAccessAccountExpiration()
        throws WindowsAzureInvalidProjectOperationException {
        Date date = wProj.getRemoteAccessAccountExpiration();
        String fDate = DateFormat.getDateTimeInstance().format(date);
        assertEquals("Dec 31, 2039 12:00:00 AM", fDate);
    }

    @Test
    public void testSetRemoteAccessAccountExpiration()
        throws WindowsAzureInvalidProjectOperationException {
        Calendar cal = new GregorianCalendar(2011, Calendar.MAY, 12, 0, 0, 0);
        Date date = cal.getTime();
        wProj.setRemoteAccessAccountExpiration(date);
        assertEquals(date.toString(), wProj.getRemoteAccessAccountExpiration().toString());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetRemoteAccessAccountExpirationWithNull()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.setRemoteAccessAccountExpiration(null);
    }

    @Test
    public void testSetRemoteAccessAccountExpirationWithEmptySetting()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr.setRemoteAccessAccountExpiration(new Date());
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testSetRemoteAccessAccountExpirationWithException()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr1.setRemoteAccessAccountExpiration(new Date());
    }

    @Test
    public void testGetRemoteAccessCertificatePath()
        throws WindowsAzureInvalidProjectOperationException {
        assertEquals("${basedir}\\cert\\SampleRemoteAccessPublic.cer", wProj.getRemoteAccessCertificatePath());
    }

    @Test
    public void testGetRemoteAccessCertificatePathWithBlank()
        throws WindowsAzureInvalidProjectOperationException {
        assertEquals("", wProjErr.getRemoteAccessCertificatePath());
    }

    @Test
    public void testSetRemoteAccessCertificatePath()
        throws WindowsAzureInvalidProjectOperationException, XPathExpressionException {
        wProj.setRemoteAccessCertificatePath("testPath");
        assertEquals("testPath", wProj.getRemoteAccessCertificatePath());
        Document doc = wProj.getPackageFileDoc();
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xPath.evaluate(
                WindowsAzureConstants.PROJ_PROPERTY + "/comment()"
                + "[contains(.,'"
                + WindowsAzureConstants.THUMBPRINT_NOTE
                + "')]", doc, XPathConstants.NODE);
        assertEquals(null, node);
    }

    @Test
    public void testSetRemoteAccessCertificatePathWithMissingProperty()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr1.setRemoteAccessCertificatePath("testPath");
        assertEquals("testPath", wProjErr1.getRemoteAccessCertificatePath());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetRemoteAccessCertificatePathWithNull()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.setRemoteAccessCertificatePath(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetRemoteAccessCertificatePathWithEmpty()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.setRemoteAccessCertificatePath("");
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testSetRemoteAccessCertificatePathWithException()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr.setRemoteAccessCertificatePath("testPath");
    }

    @Test
    public void testGetRemoteAccessCertificateFingerprint()
        throws WindowsAzureInvalidProjectOperationException {
        assertEquals("875F1656A34D93B266E71BF19C116C39F16B6987", wProj.getRemoteAccessCertificateFingerprint());
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testGetRemoteAccessCertificateFingerprintWithException()
        throws WindowsAzureInvalidProjectOperationException {
        assertEquals("875F1656A34D93B266E71BF19C116C39F16B6987", wProjErr.getRemoteAccessCertificateFingerprint());
    }

    @Test
    public void testSetRemoteAccessCertificateFingerprint()
        throws WindowsAzureInvalidProjectOperationException, XPathExpressionException {
        wProj.setRemoteAccessCertificateFingerprint("testFingerprint");
        assertEquals("testFingerprint", wProj.getRemoteAccessCertificateFingerprint());
        Document doc = wProj.getConfigFileDoc();
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xPath.evaluate(
                WindowsAzureConstants.ROLE + "/Certificates/comment()"
                + "[contains(.,'"
                + WindowsAzureConstants.THUMBPRINT_NOTE
                + "')]", doc, XPathConstants.NODE);
        assertEquals(null, node);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetRemoteAccessCertificateFingerprintWithNull()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.setRemoteAccessCertificateFingerprint(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetRemoteAccessCertificateFingerprintWithEmpty()
        throws WindowsAzureInvalidProjectOperationException {
        wProj.setRemoteAccessCertificateFingerprint("");
    }

    @Test
    public void testSetRemoteAccessCertificateFingerprintWithEmptySetting()
        throws WindowsAzureInvalidProjectOperationException, XPathExpressionException {
        wProjErr.setRemoteAccessCertificateFingerprint("testPath");
        Document doc = wProjErr.getConfigFileDoc();
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = (Node) xPath.evaluate(
                WindowsAzureConstants.ROLE + "/Certificates/comment()"
                + "[contains(.,'"
                + WindowsAzureConstants.THUMBPRINT_NOTE
                + "')]", doc, XPathConstants.NODE);
        assertEquals(null, node);
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testSetRemoteAccessCertificateFingerprintWithException()
        throws WindowsAzureInvalidProjectOperationException {
        wProjErr1.setRemoteAccessCertificateFingerprint("testPath");
    }


    @Test(expected=IllegalArgumentException.class)
    public void testSetProjectNameWithEmpty()
    throws WindowsAzureInvalidProjectOperationException {
            wProj.setProjectName("");
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testSetProjectNameWithException()
    throws WindowsAzureInvalidProjectOperationException {
            wProjErr1.setProjectName("project1");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetProjectNameWithNull()
    throws WindowsAzureInvalidProjectOperationException {
            wProj.setProjectName(null);
    }

    @Test
    public void testSetProjectName()
    throws WindowsAzureInvalidProjectOperationException {
        wProj.setProjectName("NewProject");
        assertEquals("NewProject", wProj.getProjectName());
    }

    @Test
    public void testGetProjectName()
    throws WindowsAzureInvalidProjectOperationException {
        assertEquals("WindowsAzureProject", wProj.getProjectName());
    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testGetProjectNameWithException()
    throws WindowsAzureInvalidProjectOperationException {
        wProjErr1.getProjectName();
    }

    @Test
    public void testSaveWithAddRole()
    throws WindowsAzureInvalidProjectOperationException {
        wProj.getRoles();
        WindowsAzureRole role = wProj.addRole("NewRole", Messages.getString("WinAzureTestConstants.StarterKit"));
        role.setInstances("1");
        role.setVMSize("Small");
        wProj.save();
        String path = Messages.getString("WinAzureTestConstants.WindowsAzureProj")
                        + File.separator + "NewRole";
        File file = new File(path);
        assertTrue(file.exists());
    }

    @Test
    public void testSaveWithEditRole()
    throws WindowsAzureInvalidProjectOperationException {
        wProj.getRoles();
        WindowsAzureRole role = wProj.getRoles().get(4);
        role.setName("EditRole");
        wProj.save();
        String path = Messages.getString("WinAzureTestConstants.WindowsAzureProj")
                        + File.separator + "EditRole";
        File file = new File(path);
        assertTrue(file.exists());
    }

    @Test
    public void testSaveWithDeleteRole()
    throws WindowsAzureInvalidProjectOperationException {
        wProj.getRoles();
        WindowsAzureRole role = wProj.getRoles().get(4);
        role.delete();
        wProj.save();
        String path = Messages.getString("WinAzureTestConstants.WindowsAzureProj")
                        + File.separator + "EditRole";
        File file = new File(path);
        assertFalse(file.exists());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMoveProjFromTempWithEmptyProjName()
    throws Exception {
       WindowsAzureProjectManager.moveProjFromTemp("", "c:\\");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMoveProjFromTempWithNullProjName()
    throws Exception {
        WindowsAzureProjectManager.moveProjFromTemp(null, "c:\\");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMoveProjFromTempWithEmptyProjLocation()
    throws Exception {
        WindowsAzureProjectManager.moveProjFromTemp(
            "WindowsAzureProject", "");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMoveProjFromTempWithNullProjLocation()
    throws Exception {
        WindowsAzureProjectManager.moveProjFromTemp(
                "WindowsAzureProject", null);
    }

    @Test
    public void testMoveProjFromTemp()
    throws Exception {
    	File f = new File("c:\\test");
    	if(f.exists()) {
    		deleteDirectory(f);
    		f.mkdir();
    	}
        WindowsAzureProjectManager.moveProjFromTemp(
                "WindowsAzureProject", "c:\\test\\");
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
               //directory is empty, then delete it
               if(dir.list().length == 0) {
                     dir.delete();
               } else {
                     //list all the directory contents
                     String[] subFiles = dir.list();
                     for (int i = 0; i < subFiles.length; i++) {
                            //construct the file structure
                            File fileDelete = new File(dir, subFiles[i]);
                            //recursive delete
                            deleteDirectory(fileDelete);
                     }
                     //check the directory again, if empty then delete it
                     if(dir.list().length == 0){
                            dir.delete();
                     }
               }
        } else {
               dir.delete();
        }
 }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testMoveProjFromTempWithException()
    throws Exception {
        WindowsAzureProjectManager.moveProjFromTemp(
                "WindowsAzureProject", "drive:\\");
    }

    @Test()
    public void testGetMaxLocalStorageSizeExtraSmall() {
        assertEquals(WindowsAzureConstants.MAX_LS_SIZE_EXTRASMALL,
                WindowsAzureProjectManager.getMaxLocalStorageSize("extrasmall"));
    }

    @Test()
    public void testGetMaxLocalStorageSizeSmall() {
        assertEquals(WindowsAzureConstants.MAX_LS_SIZE_SMALL,
                WindowsAzureProjectManager.getMaxLocalStorageSize("small"));
    }

    @Test()
    public void testGetMaxLocalStorageSizeMedium() {
        assertEquals(WindowsAzureConstants.MAX_LS_SIZE_MEDIUM,
                WindowsAzureProjectManager.getMaxLocalStorageSize("medium"));
    }

    @Test()
    public void testGetMaxLocalStorageSizeLarge() {
        assertEquals(WindowsAzureConstants.MAX_LS_SIZE_LARGE,
                WindowsAzureProjectManager.getMaxLocalStorageSize("large"));
    }

    @Test()
    public void testGetMaxLocalStorageSizeExtraLarge() {
        assertEquals(WindowsAzureConstants.MAX_LS_SIZE_EXTRALARGE,
                WindowsAzureProjectManager.getMaxLocalStorageSize("extraLarge"));
    }

    @Test()
    public void testgetSessionAffinityStatusWithFalse() {
        assertFalse( wProj.getSessionAffinityStatus());
    }

    @Test()
    public void testgetSessionAffinityStatus() {
        assertTrue(wProjSa.getSessionAffinityStatus());
    }

    @Test()
    public void testDisableSessionAffinity()
            throws WindowsAzureInvalidProjectOperationException
    {
        wProjSa.disableSessionAffinity();
        assertFalse(wProjSa.getSessionAffinityStatus());
    }

    @Test()
    public void testGetServerTemplateNames()
            throws WindowsAzureInvalidProjectOperationException
    {
        String[] actualSerNames = WindowsAzureProjectManager.getServerTemplateNames(
                new File(Messages.getString("WinAzureTestConstants.Comp")));
        String[] expectedSerNames = new String[] {"Jetty 7", "Jetty 8", "Apache Tomcat 7",
                "Apache Tomcat 6", "JBoss AS 6", "JBoss AS 7 (Standalone)", "GlassFish OSE 3"};
        assertArrayEquals(expectedSerNames, actualSerNames);
    }

    @Test()
    public void testGetPackageDir()
            throws WindowsAzureInvalidProjectOperationException
    {
        String dirName = wProj.getPackageDir();
        assertEquals(".\\deploy", dirName);
    }

    @Test()
    public void testGetLatestAzureSdkDir()
            throws WindowsAzureInvalidProjectOperationException, IOException
    {
        String dirName = null;
        dirName = WindowsAzureProjectManager.getLatestAzureSdkDir();
        assertEquals("C:\\Program Files\\Microsoft SDKs\\Windows Azure\\.NET SDK\\2012-06\\bin", dirName);
    }

    @Test()
    public void testIsCurrVersion()
            throws WindowsAzureInvalidProjectOperationException, IOException
    {
        assertTrue(wProj.isCurrVersion());
    }

    @Test()
    public void testGetOSFamily()
    		throws WindowsAzureInvalidProjectOperationException {
    	 assertEquals(OSFamilyType.WINDOWS_SERVER_2008_R2, wProj.getOSFamily());
    }

    @Test()
    public void testSetOSFamily()
    		throws WindowsAzureInvalidProjectOperationException {
    	wProj.setOSFamily(OSFamilyType.WINDOWS_SERVER_2012);
    	assertEquals(OSFamilyType.WINDOWS_SERVER_2012, wProj.getOSFamily());
    }
    
    @Test()
    public void testVersion() throws WindowsAzureInvalidProjectOperationException {
    	String newVersion = "1.8.0";
    	String oldVersion = wProj.getVersion();
    	wProj.setVersion(newVersion);
    	wProj.save();
    	assertEquals(newVersion, wProj.getVersion());
    	
    	//Resetting back test data
    	wProj.setVersion(oldVersion);
    	wProj.save();
    }
    

}
