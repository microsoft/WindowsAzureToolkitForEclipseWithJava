package com.microsoftopentechnologies.acs.federation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoftopentechnologies.acs.federation.ACSFederationAuthFilter;
import com.microsoftopentechnologies.acs.federation.HttpServletRequestDetails;
import com.microsoftopentechnologies.acs.saml.SAMLAssertion;

public class ACSFederationAuthFilterTest {

	private static DummyHTTPServletRequest oldTestRequest;
	private static DummyHTTPServletRequest newTestRequest;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Create dummy test requests
		HashMap<String, String[]> oldParamsMap = new HashMap<String, String[]>();
		oldParamsMap.put("key1", new String[]{"key1value1"});
		oldParamsMap.put("key2", new String[]{"key2value1", "key2value2", "key2value3"});
		oldTestRequest = new DummyHTTPServletRequest();
		oldTestRequest.setMethod("GET");
		oldTestRequest.setParameterMap(oldParamsMap);
		
		HashMap<String, String[]> newParamsMap = new HashMap<String, String[]>();
		newParamsMap.put("key1", new String[]{"newkey1value1"});
		newParamsMap.put("key3", new String[]{"newkey3value1", "newkey3value2"});
		newTestRequest = new DummyHTTPServletRequest();
		newTestRequest.setMethod("POST");
		newTestRequest.setParameterMap(oldParamsMap);
	}

	@Test
	public void testSetRemoteUserInServletRequest() {
		String remoteUser = "acsTestUser";		
		HttpServletRequest modifiedRequest = new ACSFederationAuthFilter().setRemoteUserInServletRequest(newTestRequest, remoteUser);
		assertEquals("Setting remote user on HTTPServletRequest failed.", remoteUser, modifiedRequest.getRemoteUser());
	}
	
	@Test
	public void testSetRemoteUserAndOldRequestDetailsInServletRequest() {
		String remoteUser = "acsTestUser2";
		HttpServletRequestDetails requestDetails = HttpServletRequestDetails.extractDetailsFromRequest(oldTestRequest);
		HttpServletRequest modifiedRequest = new ACSFederationAuthFilter().setRemoteUserAndOldRequestDetailsInServletRequest(newTestRequest, remoteUser, requestDetails);
		// test modified method
		assertEquals("Setting new METHOD on HTTPServletRequest failed.", oldTestRequest.getMethod(), modifiedRequest.getMethod());
		// test modified param values
		assertFalse("Setting new parameters on HTTPServletRequest failed.", "newkey1value1".equals(modifiedRequest.getParameter("key1"))); 
		assertNull("Setting new parameters on HTTPServletRequest failed.", modifiedRequest.getParameter("key3"));
		assertEquals("Setting new parameters on HTTPServletRequest failed.", "key2value2", modifiedRequest.getParameterValues("key2")[1]);
		Enumeration<String> paramNames = modifiedRequest.getParameterNames();
		// Can't rely on the order of elements in the enumerator
		String firstParamName = paramNames.nextElement();
		String secondParamName = paramNames.nextElement();
		boolean passCondition = (firstParamName.equals("key1") && secondParamName.equals("key2"))
				|| (firstParamName.equals("key2") && secondParamName.equals("key1"));				
		assertTrue("Setting new parameters on HTTPServletRequest failed.", passCondition);
		
	}
	
	@Test
	public void testGetSAMLAssertionFromRequest() throws IOException {		
		String wresult = "<t:RequestSecurityTokenResponse xmlns:t=\"http://schemas.xmlsoap.org/ws/2005/02/trust\"><t:Lifetime><wsu:Created xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">"
			+ "2011-12-06T02:41:11.697Z</wsu:Created><wsu:Expires xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">2011-12-06T02:51:11.697Z</wsu:Expires></t:Lifetime><wsp:AppliesTo"
			+ " xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"><EndpointReference xmlns=\"http://www.w3.org/2005/08/addressing\"><Address>http://localhost:8080/jaasapp</Address></EndpointReference></wsp:AppliesTo>"
			+ "<t:RequestedSecurityToken><Assertion ID=\"_a97ff228-579c-474f-8aad-74f82965c30a\" IssueInstant=\"2011-12-06T02:41:11.712Z\" Version=\"2.0\" xmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\"><Issuer>"
			+ "https://vijayspace.accesscontrol.appfabriclabs.com/</Issuer><ds:Signature xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"><ds:SignedInfo><ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\" />" 
			+ "<ds:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\" /><ds:Reference URI=\"#_a97ff228-579c-474f-8aad-74f82965c30a\"><ds:Transforms><ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\" />" 
			+ "<ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\" /></ds:Transforms><ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" /><ds:DigestValue>CKZXY3dx6EpY5jkg194vuVn0LLq5SvZR/GZceqhMro0=</ds:DigestValue>" 
			+ "</ds:Reference></ds:SignedInfo><ds:SignatureValue>PV8UK9dSAGVWE8cNAiPN3poPJAyPjYEOvLaS2HVJYOABk0QZS52uZA/PL5XUHEEWD686rsU/HchE /7wdH/Icux0TG8ordG2TXwnNQ1hKgYDAseTCSlEYQOBIkfFjNgR1waEyhyIYmqHayCw lb5aNtO2d 5rmYGNRIKGyl1Ya8=</ds:SignatureValue>" 
			+ "<KeyInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><X509Data><X509Certificate>MIICGDCCAYWgAwIBAgIQsErGOfo2q4REUZ1V95twrTAJBgUrDgMCHQUAMCIxIDAeBgNVBAMTF1ZpamF5YSBHb3BhbCBZYXJyYW1uZW5pMB4XDTExMTEyMzIyNTI0NFoXDTM5MTIzMTIzNTk1OVowIjEgMB4GA1UEAxMXVmlqYXlhIEdvcGFsIFlhcnJhbW5lbmkwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBALZrPHnJ7LeAbttH1ukqibQZvhQNsqquHbavxAdx0PIr/Coj1rQA8rLnCxJ0VOiXnVu75PUHgNaNHeMrujSJoysbZgZfJ nRDUXKFBt3MgA3MoXQ0C2W8G0z1Ii2CGe8KxFHigJ7SPavg/G 3dOZZoUCYIuZmOPLRYAQo0wu7YFVAgMBAAGjVzBVMFMGA1UdAQRMMEqAEEMZuOoDoFi7DgBtIThDW1GhJDAiMSAwHgYDVQQDExdWaWpheWEgR29wYWwgWWFycmFtbmVuaYIQsErGOfo2q4REUZ1V95twrTAJBgUrDgMCHQUAA4GBAJLtvVPZZfulzFhl Wcipy1PYckkC0JoZC38Ja1BUTmSENxqjOc7tnkC5gCcI7V I9eSJkLwhZjMdL9SuCbpmFSDzZX1j182f4AIfUOL3DGTvLmprk0z4SXaigY ZDi60yHk3PD5poHI WBqFHSGyONLiH5/8ICF5PlfSz5VKa/X" 
			+ "</X509Certificate></X509Data></KeyInfo></ds:Signature><Subject><NameID>https://www.google.com/accounts/o8/id?id=AItOawla5n4ugcg7_B0NndI2oj-cTLx3VnfqkWM</NameID>" 
			+ "<SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\" /></Subject><Conditions NotBefore=\"2011-12-06T02:41:11.697Z\" NotOnOrAfter=\"2011-12-06T02:51:11.697Z\"><AudienceRestriction><Audience>http://localhost:8080/jaasapp</Audience></AudienceRestriction></Conditions><AttributeStatement>" 
			+ "<Attribute Name=\"http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress\"><AttributeValue>ygopal@gmail.com</AttributeValue></Attribute><Attribute Name=\"http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name\"><AttributeValue>Vijaya Gopal Yarramneni</AttributeValue></Attribute><" 
			+ "Attribute Name=\"http://schemas.microsoft.com/accesscontrolservice/2010/07/claims/identityprovider\"><AttributeValue>Google</AttributeValue></Attribute></AttributeStatement></Assertion></t:RequestedSecurityToken><t:RequestedAttachedReference>" 
			+ "<SecurityTokenReference d3p1:TokenType=\"http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0\" xmlns:d3p1=\"http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd\" xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" 
			+ "<KeyIdentifier ValueType=\"http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLID\">_a97ff228-579c-474f-8aad-74f82965c30a</KeyIdentifier></SecurityTokenReference></t:RequestedAttachedReference>" 
			+ "<t:RequestedUnattachedReference><SecurityTokenReference d3p1:TokenType=\"http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0\" xmlns:d3p1=\"http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd\" xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" 
			+ "<KeyIdentifier ValueType=\"http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLID\">_a97ff228-579c-474f-8aad-74f82965c30a</KeyIdentifier></SecurityTokenReference></t:RequestedUnattachedReference><t:TokenType>urn:oasis:names:tc:SAML:2.0:assertion</t:TokenType><t:RequestType>" 
			+ "http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</t:RequestType><t:KeyType>http://schemas.xmlsoap.org/ws/2005/05/identity/NoProofKey</t:KeyType></t:RequestSecurityTokenResponse>";
		HashMap<String, String[]> paramsMap = new HashMap<String, String[]>();
		paramsMap.put("wresult", new String[]{wresult});
		DummyHTTPServletRequest request = new DummyHTTPServletRequest();
		request.setParameterMap(paramsMap);
		HttpServletRequestDetails requestDetails = HttpServletRequestDetails.extractDetailsFromRequest(request);
		HttpServletRequest modifiedRequest = new ACSFederationAuthFilter().setRemoteUserAndOldRequestDetailsInServletRequest(newTestRequest, "unused", requestDetails);
		System.out.println(modifiedRequest.getParameter("wresult"));
		SAMLAssertion assertion = new ACSFederationAuthFilter().getSAMLAssertionFromACSResponse(modifiedRequest);
		assertNotNull("Extracing SAML Assertion from ACS response failed..", assertion);
		
	}

}
