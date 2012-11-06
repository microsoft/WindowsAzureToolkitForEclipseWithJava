package com.microsoftopentechnologies.acs.util;

import static org.junit.Assert.assertEquals;

import java.util.zip.DataFormatException;

import org.junit.Test;

import com.microsoftopentechnologies.acs.util.DeflaterUtils;
import com.microsoftopentechnologies.acs.util.Utils;

public class DeflaterUtilsTest {

	@Test
	public void testDeflateAndInflateEmptyInput() throws DataFormatException {
		String inputText = "";
		String reInflatedString = testDeflateAndInflate(inputText);		
		assertEquals("Deflate a sample input and inflating it back failed", inputText, reInflatedString);		
	}
	
	@Test
	public void testDeflateAndInflateSmallInput() throws DataFormatException {
		String inputText = "Deflater test input";
		String reInflatedString = testDeflateAndInflate(inputText);		
		assertEquals("Deflate a sample input and inflating it back failed", inputText, reInflatedString);		
	}
	
	@Test
	public void testDeflateAndInflateLargeInput() throws DataFormatException {
		String inputText = "<Assertion ID=\"_54c2c8ae-df27-44f3-bd74-3038ad845ced\" IssueInstant=\"2011-08-25T20:12:19.497Z\" " +
				"Version=\"2.0\" xmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\"><Issuer>https://vijayspace.accesscontrol.appfabriclabs.com/</Issuer>" +
				"<Subject><NameID>https://www.google.com/accounts/o8/id?id=AItOawla5n4ugcg7_B0NndI2oj-cTLx3VnfqkWM</NameID><SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\">" +
				"<SubjectConfirmationData InResponseTo=\"_0ddd93d0-635f-4a6d-8668-cff8b6597476\" NotOnOrAfter=\"2011-08-25T20:17:19.512Z\" " +
				"Recipient=\"http://localhost:8888/ACSTrial/\" /></SubjectConfirmation></Subject><Conditions NotBefore=\"2011-08-25T20:12:19.497Z\" " +
				"NotOnOrAfter=\"2011-08-25T20:22:19.497Z\"><AudienceRestriction><Audience>http://localhost:8888/ACSTrial/</Audience></AudienceRestriction>" +
				"</Conditions><AttributeStatement><Attribute Name=\"http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress\">" +
				"<AttributeValue>ygopal@gmail.com</AttributeValue></Attribute><Attribute Name=\"http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name\">" +
				"<AttributeValue>Vijaya Gopal Yarramneni</AttributeValue></Attribute>" +
				"<Attribute Name=\"http://schemas.microsoft.com/accesscontrolservice/2010/07/claims/identityprovider\">" +
				"<AttributeValue>Google</AttributeValue></Attribute></AttributeStatement></Assertion>";
		String reInflatedString = testDeflateAndInflate(inputText);		
		assertEquals("Deflate a sample input and inflating it back failed", inputText, reInflatedString);		
	}
	
	
	private String testDeflateAndInflate(String inputText) throws DataFormatException
	{
		// Deflate it first
		byte[] inputBytes = Utils.getUTF8Bytes(inputText);
		//System.out.println("Size of input is " + inputBytes.length + " bytes");
		byte[] deflatedBytes = DeflaterUtils.deflate(inputBytes);
		//System.out.println(Base64.encode(deflatedBytes).length());
		//System.out.println(Base64.encode(deflatedBytes));		
		//System.out.println("Size of deflated data is " + deflatedBytes.length + " bytes");		
		// Inflate it back now
		byte[] reInflatedBytes = DeflaterUtils.inflate(deflatedBytes);		
		//System.out.println("Size of reinflated data is " + reInflatedBytes.length + " bytes");		
		String reInflatedString = Utils.getStringFromUTF8Bytes(reInflatedBytes);
		return reInflatedString;
	}
}
