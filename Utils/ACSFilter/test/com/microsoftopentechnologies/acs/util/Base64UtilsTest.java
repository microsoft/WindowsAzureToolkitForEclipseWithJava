package com.microsoftopentechnologies.acs.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.microsoftopentechnologies.acs.util.Base64;

public class Base64UtilsTest {

	@Test
	public void testBase64Encode() throws UnsupportedEncodingException {
		String input = "secret";
		String expectedOutput = "c2VjcmV0";
		String base64EncodedString = Base64.encode(input.getBytes("UTF-8"));
		// Always trim the base64 encoded string, Java encoder as \r\n at the end
		base64EncodedString = base64EncodedString.trim();
		assertEquals("Base64 encoding failed.", expectedOutput, base64EncodedString);
		
	}

	@Test
	public void testBase64Decode() throws IOException {
		String input = "c2VjcmV0";
		String expectedOutput = "secret";
		String base64DecodedString = new String(Base64.decode(input), "UTF-8");
		assertEquals("Base64 encoding failed.", expectedOutput, base64DecodedString);
	}

}
