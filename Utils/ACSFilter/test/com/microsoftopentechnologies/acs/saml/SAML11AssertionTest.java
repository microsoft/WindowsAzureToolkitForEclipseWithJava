package com.microsoftopentechnologies.acs.saml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.microsoftopentechnologies.acs.saml.AssertionNotFoundException;
import com.microsoftopentechnologies.acs.saml.InvalidAssertionException;
import com.microsoftopentechnologies.acs.saml.SAML11Assertion;
import com.microsoftopentechnologies.acs.saml.SAMLAssertion;
import com.microsoftopentechnologies.acs.util.UtilsTest;
import com.microsoftopentechnologies.acs.xmldsig.TrustParameters;

public class SAML11AssertionTest {

	private static DocumentBuilder docBuilder;
	String certificatePath="c://ssl/AzureIaas.cer";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		docBuilder = docBuilderFactory.newDocumentBuilder();
	}
	
	@Test
	public void testGetAssertionForValidAssertion() throws SAXException, IOException, InvalidAssertionException, AssertionNotFoundException, ServletException {
		Document tokenDocument = docBuilder.parse("test/samltokens/saml11_valid_token.xml");	
		
		TrustParameters certTrustParams = new TrustParameters(null);
		
		SAMLAssertion assertion = SAMLAssertion.getAssertionFromSecurityToken(tokenDocument.getDocumentElement());
		// Check signature validity
		assertion.checkAssertionSignatureValidity(certTrustParams);
		String expectedIssuer = "https://vijayspace.accesscontrol.appfabriclabs.com/";
		String issuer = assertion.getIssuer();
		assertEquals("SAML11Assertion not parsed properly. Wrong Issuer returned.", expectedIssuer, issuer);
		String expectedNameIdentifier = "https://www.google.com/accounts/o8/id?id=AItOawla5n4ugcg7_B0NndI2oj-cTLx3VnfqkWM";
		String nameIdentifier = assertion.getSubject().getNameIdentifier();
		assertEquals("SAML11Assertion not parsed properly. Wrong subject name identifier returned.", expectedNameIdentifier, nameIdentifier);
		// Old assertion, already expired
		boolean areCondionsValid = assertion.areConditionsValidNow(certTrustParams);
		assertFalse("SAML11Assertion not parsed properly. Conditions wrongly evaluated to valid.", areCondionsValid);
		SAMLAssertion.Attribute[] attributes = assertion.getAttributes();
		int expected_number_attributes = 3;
		assertEquals("SAML11Assertion not parsed properly. Returned wrong number of attributes.", expected_number_attributes, attributes.length);
		String expected_first_attribute_name = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
		String actual_first_attribute_name = attributes[0].getName();
		assertEquals("SAML11Assertion not parsed properly. Returned wrong attribute name.", expected_first_attribute_name, actual_first_attribute_name);
		String actual_second_attribute_nameformat = attributes[1].getNameFormat();
		// nameformat should be empty string
		assertTrue("SAML11Assertion not parsed properly. Returned wrong attribute nameformat.", actual_second_attribute_nameformat.isEmpty());
		String expected_third_attribute_value = "Google";
		String acutal_third_attribute_value = attributes[2].getValues()[0];
		assertEquals("SAML11Assertion not parsed properly. Returned wrong attribute value.", expected_third_attribute_value, acutal_third_attribute_value);
			
		
	}
	
	@Test(expected = InvalidAssertionException.class)
	public void testGetAssertionForTamperedAssertion() throws SAXException, IOException, InvalidAssertionException, AssertionNotFoundException, ServletException {
		Document tokenDocument = docBuilder.parse("test/samltokens/saml11_tampered_token.xml");		
		SAMLAssertion assertion = SAMLAssertion.getAssertionFromSecurityToken(tokenDocument.getDocumentElement());
		TrustParameters certTrustParams = new TrustParameters(null);
		// Expected to throw InvalidAssertionException
		assertion.checkAssertionSignatureValidity(certTrustParams);		
	}	
	
	@Test
	public void testGetAssertionFromAssertionElement() throws SAXException, IOException, AssertionNotFoundException
	{
		Document tokenDocument = docBuilder.parse("test/samltokens/saml11_valid_assertion.xml");		
		SAMLAssertion assertion = SAMLAssertion.getAssertionFromAssertionDocument(tokenDocument);
		assertTrue("GetAssertion from assertion element failed.", assertion instanceof SAML11Assertion);
	}

}
