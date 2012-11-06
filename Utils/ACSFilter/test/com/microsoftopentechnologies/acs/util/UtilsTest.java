package com.microsoftopentechnologies.acs.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.xml.bind.DatatypeConverter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.microsoftopentechnologies.acs.util.Base64;
import com.microsoftopentechnologies.acs.util.Utils;

public class UtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParseTimeFromXSDTIMEString() throws ParseException {
		String timeAsString = "2011-08-04T00:41:01.862Z";
		Date date = Utils.parseTimeFromXSDTIMEString(timeAsString);
		Date expectedDate = new Date(1312418461862l);
		assertEquals("Date conversion from standard XSD datetime format failed", expectedDate, date);
	}
	
	@Test
	public void testParseTimeAgainstJAXB() throws ParseException {
		String timeAsString = "2011-08-25T20:22:19.497Z";
		Date date = Utils.parseTimeFromXSDTIMEString(timeAsString);
		Date expectedDate = DatatypeConverter.parseDateTime(timeAsString).getTime();
		assertEquals("Date conversion from standard XSD datetime format failed", expectedDate, date);
	}
	
	@Test
	public void testSplitText1() {
		String inputText = "Split text input";
		String[] splitParts = Utils.splitText(inputText, 100);
		assertEquals("Split Text utility failed..", inputText, splitParts[0]);
		
	}
	
	@Test
	public void testSplitText2() {
		String inputText = "Split text input";
		String[] splitParts = Utils.splitText(inputText, 10);
		assertEquals("Split Text utility failed..", 10, splitParts[0].length());
		assertEquals("Split Text utility failed..", inputText.substring(10), splitParts[1]);
		
	}
	
	@Test
	public void testSplitText3() {
		String inputText = "Split text input";
		String[] splitParts = Utils.splitText(inputText, 3);
		for(String part : splitParts) {
			assertTrue("Split Text utility failed..", 3 >= part.length());
		}
		assertEquals("Split Text utility failed..", 6, splitParts.length);		
	}
	
	@Test
	public void testSplitText4() {
		String inputText = "rZRvb5swEMa/CuI9mBAIBBG2tJWqSP0jNVGm7U112AdxBzazTdN8+5msSVq1yl5siBfoOD/34547\\n8rnWqAyXwllc" +
				"zdzHOKIhTQE9VoWJF0XV2CtZEnnjYJwCS6OYInOdhdY9LoQ2IMzMDYPRyAtSL4xX\\nYZCNwmw09aNp8sN11qi0lbYpfuA6L20j9Mztl" +
				"cgkaK4zAS3qzNBsOb+9yWxOBgcat8j3RVSxMabT\\nGSHP/Al2ugOKPlCKWlMpjJKND11XQak4baDUPpUtycnr2XzZl09ITZHf2VKLq6PY" +
				"drv1aynrBvcH\\nrKDshdFEpoSzL5zN5gtzD9sGYhH1Na2Tx4vgTrBFKJ88urp5Ga9F9evnt9ucvAofKl1KUXHVwr6j\\nt2g2kp3/YtpmJ" +
				"YJC5X6qcQUGnIV4QN1JoXElrUcBY2w6ZoE3GceVF8GEeelkknq0qtJyEk+TKJm4\\nzp009+JezSuD6oNHyeBRPAqtRw9IecdxMHLojm1OIy" +
				"k0G6lNltqLzC+XK8WhIa5Dipx8AnmKFrmN\\nMz4E9UBwgZVUeG5EzmGGp7win/fMUlK0rTDW7D91D8HiL+g5OWaeHt8pkRO4lTX2RdkbX" +
				"Bow2Nrm\\nvIk5g+XHZmm6wRa0b4dbS+h8qWqy1SQMgpjYmzN7mJsdsdPJW01sLm+AMWUH2H0juoamx2JXyw6a\\nr/WQNAymZX2f8Cbwv4iGk" +
				"fxIsh62DZzrgcf5DkpBK1DwfwJqOVVSy8ocdu60xHbrnzlFCzkKSJAc\\n2A6snZLP9ll95LzeL/F5LPKZneT44yt+Aw==";
		// Append the same thing 10 times
		StringBuffer inputBuffer = new StringBuffer();
		for(int i=0; i<10; i++)
			inputBuffer.append(inputText);
		inputText = inputBuffer.toString();
		// Testing for multiple size limits
		int[] limits2Test = {1, 111, 1024, 2048, 4095, 4096};
		for(int limit : limits2Test) {
			String[] splitParts = Utils.splitText(inputText, limit);
			int expectedNumberOfParts = (inputText.length() % limit)==0 ? inputText.length() / limit : (inputText.length() / limit) + 1;
			assertEquals("Split Text utility failed for size limit " + limit, expectedNumberOfParts, splitParts.length);
			for(String part : splitParts) {
				assertTrue("Split Text utility failed for size limit " + limit, limit >= part.length());
			}
			assertEquals("Split Text utility failed for size limit " + limit, limit, splitParts[0].length());
			assertEquals("Split Text utility failed for size limit " + limit, inputText.substring(limit * (expectedNumberOfParts - 1)), splitParts[expectedNumberOfParts - 1]);
		}		
	}
	
	@Test
	public void testRemoveCRLFsInBase64EncodedText() {
		String largeInput =  "<Assertion ID=\"_54c2c8ae-df27-44f3-bd74-3038ad845ced\" IssueInstant=\"2011-08-25T20:12:19.497Z\" " +
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
		String base64EncodedText = Base64.encode(Utils.getUTF8Bytes(largeInput));
		base64EncodedText = Utils.removeCRLFsInBase64EncodedText(base64EncodedText);
		System.out.println(base64EncodedText);
		assertTrue("CR LF s in base 64 encoded text are not removed.", base64EncodedText.indexOf('\r') == -1);
		assertTrue("CR LF s in base 64 encoded text are not removed.", base64EncodedText.indexOf('\n') == -1);
	}
	
	public static KeyStore createKeyStore(String certificatesPath) throws ServletException {
		KeyStore keyStore = null;
		try	{
			keyStore = KeyStore.getInstance("JKS");
			keyStore.load(null, null);

			String[] cerPath = certificatesPath.split(";");
			for (int x = 0; x < cerPath.length; x++) {
				BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(getCertificatePath(cerPath[x])));
				CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
				Certificate certificate = null;
				while (bufferedInputStream.available() > 0)	{
					certificate = certificateFactory.generateCertificate(bufferedInputStream);
					keyStore.setCertificateEntry(x + "", certificate);
				}
			}
		} catch (Throwable t)	{
			throw new ServletException("Error while creating keystore");
		}
		return keyStore;
	}
	
	public static String getCertificatePath(String rawPath) {
		String certPath = null;
		if (rawPath != null && rawPath.length() > 0) {
			String[] result = rawPath.split("\\" + File.separator);
			StringBuilder  path = new StringBuilder();

			for (int x = 0; x < result.length; x++) {
				if (result[x].startsWith("${env")) {
					String envValue = System.getenv(result[x].substring("${env.".length(), (result[x].length() - 1)));
					path.append(envValue).append(File.separator);
				} else {
					path.append(result[x]).append(File.separator);
				}
			}

			//Delete last trailing slash
			path = path.deleteCharAt(path.length() - 1);
			certPath = path.toString();
		}
		return certPath;
	}
}
