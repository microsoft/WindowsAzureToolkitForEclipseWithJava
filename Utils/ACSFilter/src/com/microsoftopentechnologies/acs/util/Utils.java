/*
 Copyright 2013 Microsoft Open Technologies, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.microsoftopentechnologies.acs.util; 

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.spec.AlgorithmParameterSpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Utils {
	private static final Logger LOG = Logger.getLogger(Utils.class.getName());
	public static final String UTF8_ENCODING = "UTF-8";
	private static final String ACS_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final SimpleDateFormat GMT_DATE_FMT = new SimpleDateFormat(ACS_TIME_FORMAT);
	
	public static final void logError(String text, Throwable error, Logger logger) {
		if (logger != null && logger.isLoggable(Level.SEVERE)) {
			logger.log(Level.SEVERE, text);
		}
	}

	public static final void logDebug(String text, Logger logger) {
		if (logger != null && logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, text);
		}
	}

	public static final void logInfo(String text, Logger logger) {
		if (logger != null && logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, text);
		}
	}

	public static final void logWarn(String text, Throwable error, Logger logger)	{
		if (logger != null && logger.isLoggable(Level.WARNING)) {
			logger.log(Level.WARNING, text);
		}
	}

	static {
		// Use GMT time zone, instead of the local time zone
		GMT_DATE_FMT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/*
	 * Just to move the try-catch to a separate method and keep the code clean at
	 * other places where URL decoding is required.
	 */
	public static final String urlDecode(String encodedString)	{
		String decodedString = null;
		try {
			decodedString =  URLDecoder.decode(encodedString, UTF8_ENCODING);
		} catch (UnsupportedEncodingException e) {
			//Ignore it.. Never arise...
			logError("UTF-8 charset not found on the JRE.", e, LOG);
		}
		return decodedString;
	}

	public static final String urlEncode(String input) {
		String encString = null;
		try {
			encString =  URLEncoder.encode(input, UTF8_ENCODING);
		} catch (UnsupportedEncodingException e) {
			//Ignore it.. Never arise...
			logError("UTF-8 charset not found on the JRE.", e, LOG);
		}
		return encString;
	}

	public static final byte[] getUTF8Bytes(String input) {
		byte[] utf8Bytes = null;
		try {
			utf8Bytes = input.getBytes(UTF8_ENCODING);
		} catch (UnsupportedEncodingException e) {
			// Ignore it.. Never arise...
			logError("UTF-8 charset not found on the JRE.", e, LOG);
		}
		return utf8Bytes;
	}

	public static final String getStringFromUTF8Bytes(byte[] input)	{
		String utf8Msg = null;
		try {
			utf8Msg =  new String(input, UTF8_ENCODING);
		} catch (UnsupportedEncodingException e) {
			logError("UTF-8 charset not found on the JRE.", e, LOG);
		}
		return utf8Msg;
	}

	// Synchronized as SimpleDateFormat is not thread safe..shall I create an instance on demand?
	public synchronized static Date parseTimeFromXSDTIMEString(String timeAsString) throws ParseException {
		return GMT_DATE_FMT.parse(timeAsString);
	}

	// Using Transformer.. Just in case.. we need to log XML element
	public static void writeXMLToStream(Node node, OutputStream outputStream, boolean indented) throws TransformerException	{
		TransformerFactory transFact = TransformerFactory.newInstance();
		Transformer transformer = transFact.newTransformer();
		String indent = indented ? "yes" : "no";
		transformer.setOutputProperty(OutputKeys.INDENT, indent);
		transformer.transform(new DOMSource(node), new StreamResult(outputStream));
	}
	

	public static String getXMLStringFromNode(Element node) throws Exception {
		DOMSource domSource = new DOMSource(node);
		
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.transform(domSource, result);
		return result.getWriter().toString();
	}
	
	/*
	 * If the inputText size is larger than the size limit, this method splits the input text into blocks of size equal to the
	 * limit. The last part may be smaller than the size limit. Value of 0 for size limit is not expected, it produces an exception.
	 */
	public static String[] splitText(String inputText, int blockSizeLimit)	{
		int inputTextSize = inputText.length();
		// Dividing two times.. is there an easier way to find both the remaider and quotient?
		int number_parts = ((inputTextSize % blockSizeLimit) == 0) ? (inputTextSize / blockSizeLimit) : ((inputTextSize / blockSizeLimit) + 1);
		String[] splitParts = new String[number_parts];
		for (int i = 0; i < (number_parts - 1); i++) {
			int beginIndex = blockSizeLimit * i;
			int endIndex = beginIndex + blockSizeLimit;
			splitParts[i] = inputText.substring(beginIndex, endIndex);
		}
		splitParts[number_parts - 1] = inputText.substring(blockSizeLimit * (number_parts - 1));
		return splitParts;
	}

	public static String removeCRLFsInBase64EncodedText(String base64EncodedText)	{
		return base64EncodedText.replaceAll("\\s", ""); // Replace all white spaces
	}
	
	public static SecretKey getSecretKey(String key) {
		byte[] keyInBytes = Base64.decode(key);;
		SecretKey secretKey = new SecretKeySpec(keyInBytes, "AES");
		return secretKey;
	}
	
	public static byte[] encrypt(SecretKey secretKey,byte[] assertionContent) throws Exception {
		byte[] initializationVector = new byte[] {0, 9, 8, 7, 6, 2, 3, 4, 5, 0,1, 5, 7, 8, 9, 4};
		AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);
		byte[] encryptedText = null;
		
		try {		
			Cipher encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
			encrypter.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
			encryptedText = encrypter.doFinal(assertionContent);			
		}catch(Exception e) {
			//There is possibility of NoSuchPaddingException , NoSuchAlgorithmException , InvalidKeyException , InvalidAlgorithmParameterException,
			//IllegalBlockSizeException , BadPaddingException. For this method any such exception is considered has failure and more over not taking 
			//specific action on type of exception , hence catching all in one block
			logError("NoSuchPaddingException", e, LOG);
			throw e;
		}
		return encryptedText;		
	}
	
	public static byte[] decrypt(SecretKey secretKey,byte[] encryptedText) throws Exception {
		byte[] decryptedText = null;
		byte[] initializationVector = new byte[] {0, 9, 8, 7, 6, 2, 3, 4, 5, 0,1, 5, 7, 8, 9, 4};
		AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);
		
		try {		
			Cipher decrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
			decrypter.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
			decryptedText = decrypter.doFinal(encryptedText);				
		}catch(Exception e) {
			//There is possibility of NoSuchPaddingException , NoSuchAlgorithmException , InvalidKeyException , InvalidAlgorithmParameterException,
			//IllegalBlockSizeException , BadPaddingException. For this method any such exception is considered has failure and more over not taking 
			//specific action on type of exception , hence catching all in one block
			logError("NoSuchPaddingException", e, LOG);
			throw e;
		}
		return decryptedText;
		
	}

	

}
