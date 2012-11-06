/*
 Copyright 2012 Microsoft Open Technologies, Inc. 

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
package com.microsoftopentechnologies.acs.federation; 

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.microsoftopentechnologies.acs.saml.AssertionNotFoundException;
import com.microsoftopentechnologies.acs.saml.InvalidAssertionException;
import com.microsoftopentechnologies.acs.saml.SAMLAssertion;
import com.microsoftopentechnologies.acs.util.Base64;
import com.microsoftopentechnologies.acs.util.DeflaterUtils;
import com.microsoftopentechnologies.acs.util.Utils;
import com.microsoftopentechnologies.acs.xmldsig.TrustParameters;

public class StatelessFilterCommand {
	private static final Logger LOG = Logger.getLogger(StatelessFilterCommand.class.getName());
	/*
	 * Cookie size limit is in bytes. But the value we put in the cookie is Base64 encoded string, in which
	 * each character maps to a byte. So, we can treat the same limit for number of character as well.
	 * 4096 bytes is the standard size limit of cookie. This limit is including the name, value and other
	 * attributes of a cookie. So limiting the size of the value to 4000 bytes.
	 */
	private static final int MAX_COOKIE_SIZE = 4000;

	// Cookie names should be unique, should not clash with other cookie names
	public static final String COOKIE_PREFIX = "ACSFedAuth";
	
	private final ACSFederationAuthFilter fedAuthFilter;

	public StatelessFilterCommand(ACSFederationAuthFilter fedAuthFilter) {
		this.fedAuthFilter = fedAuthFilter;
	}

	public void execute(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain filterChain) throws IOException, ServletException {
		// Check if this an ACS redirect by looking for a POST request with wresult and wctx parameters
		if (httpRequest.getMethod().equalsIgnoreCase("POST")) {
			String wresult = httpRequest.getParameter("wresult");
			String wctx = httpRequest.getParameter("wctx");
			if (wresult != null && wctx != null) {
				// It is a redirect from ACS
				SAMLAssertion assertion = fedAuthFilter.getSAMLAssertionFromACSResponse(httpRequest);
				if (assertion == null) {
					Utils.logError("SAML Assertion not found in the response from ACS.", null, LOG);
					fedAuthFilter.sendLogOnFailureResponse(httpResponse, "SAML Assertion not found in the response from ACS.");
					return;
				}
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "Assertion attibutes :");
					for (SAMLAssertion.Attribute attribute : assertion.getAttributes()) {
						LOG.log(Level.FINE, attribute.getName() + ":" + Arrays.toString(attribute.getValues()));
					}
				}
				try {
					// Validate assertion
					assertion.checkAssertionValidity(this.fedAuthFilter.trustParams,true);

					// Put the assertion in cookie(s)..
					if(httpRequest.isSecure() ||  this.fedAuthFilter.trustParams.getAllowHttp())
						putAssertionInCookie(assertion, httpResponse,this.fedAuthFilter.trustParams);
					else {
						Utils.logInfo("Protocol is not secure. Consider using AllowHttp flag in web.xml for unsecured protocols",LOG);
						String cause = "Cannot process the request over unsecured protocols. " ;
						this.fedAuthFilter.sendLogOnFailureResponse(httpResponse, cause);
						return;
					}
				
					/*
					 *  Authenticated. Now redirect to the original request found in wctx.
					 *  Redirect always gets redirected as GET. So, this is a limitation with this approach..
					 *  Only GET requests work when redirected to ACS..
					 */
					httpResponse.sendRedirect(wctx);
					return;
				} catch (InvalidAssertionException e) {
					Utils.logError("Invalid SAML assertion", e, LOG);
					fedAuthFilter.sendLogOnFailureResponse(httpResponse, "SAML assertion not valid.");
					return;
				}catch (Exception e) {
					Utils.logError("Error occured while processing SAML assertion", e, LOG);
					e.printStackTrace();
					fedAuthFilter.sendLogOnFailureResponse(httpResponse, "Error occured while processing SAML assertion");
					return;
				}
			}
		}

		// Not ACS response, but normal request
		// Check for assertion in the cookie(s)
		Cookie[] cookies = httpRequest.getCookies();
		List<Cookie> assertionCookies = null;
		if (cookies != null && cookies.length > 0) {
			assertionCookies = new ArrayList<Cookie>(); // Sorting uses arrays anyway
			for (Cookie cookie : cookies) {
				if (cookie.getName().startsWith(COOKIE_PREFIX)) {
					assertionCookies.add(cookie);
				}
			}
		}

		if (assertionCookies != null && assertionCookies.size() > 0) {
			//Assertion present in cookies
			Utils.logDebug(String.format("Assertion present in cookies. Number of assertion cookies is %s. Building assertion from cookie content...", assertionCookies.size()), LOG);

			// inflate it and get assertion
			SAMLAssertion assertion;
			try {
				// Sort these assertion cookies in the right order and extract content from all of them
				String deflatedAssertionContent = extractAssertionContentFromCookies(assertionCookies);
				assertion = getAssertionFromDefaltedContent(deflatedAssertionContent,this.fedAuthFilter.trustParams,httpRequest);
			} catch (Exception e) {
				Utils.logError("Exception occured while building SAML Assertion from the cookie content", e, LOG);
				// Send error
				String cause = "Cookie content is not a valid SAML Assertion. " + e.getMessage();
				this.fedAuthFilter.sendLogOnFailureResponse(httpResponse, cause);
				return;
			}
			// Validate the assertion
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Assertion attibutes :");
				for (SAMLAssertion.Attribute attribute : assertion.getAttributes()) {
					LOG.log(Level.FINE,attribute.getName() + ":" + Arrays.toString(attribute.getValues()));
				}
			}
			try {
				assertion.checkAssertionValidity(this.fedAuthFilter.trustParams,false);

				// Authenticated. Invoke the resource
				fedAuthFilter.invokeChainWithRemoteUser(filterChain, httpRequest, httpResponse, assertion);
				return;
			} catch (InvalidAssertionException e) {
				// Assertion present in the cookies is either expired or tampered
				Utils.logInfo("Invalid SAML assertion. Redirecting to ACS...", LOG);
				// Remove assertion cookies..
				for (Cookie cookie: assertionCookies) {
					// Max-Age 0 delete the cookie
					cookie.setMaxAge(0);
					httpResponse.addCookie(cookie);
				}
				// Redirect to ACS
				redirectToACS(httpRequest, httpResponse);
				return;
			}
		} else {
			// No assertion in cookies.. Redirect to ACS
			Utils.logDebug("No assertion present in the cookies. Redirecting to ACS", LOG);
			redirectToACS(httpRequest, httpResponse);
			return;
		}
	}

	private void redirectToACS(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException	{
		// Redirect to ACS page
		Utils.logDebug("Redirecting to ACS...", LOG);
		// Using wctx parameter..
		StringBuilder redirectURL = new StringBuilder(this.fedAuthFilter.passiveRequestorEndPoint);
		redirectURL.append("?wa=wsignin1.0&wtrealm=");
		redirectURL.append(this.fedAuthFilter.relyingPartyRealm);
		redirectURL.append("&wctx=");
		redirectURL.append(this.fedAuthFilter.getCompleteRequestURL(httpRequest));

		Utils.logDebug("Redirecting to " + redirectURL.toString(), LOG);
		httpResponse.sendRedirect(redirectURL.toString());
	}

	protected SAMLAssertion getAssertionFromDefaltedContent(String deflatedAssertionXML, TrustParameters trustParams, HttpServletRequest httpRequest) 
			throws IOException, DataFormatException, ParserConfigurationException, SAXException, AssertionNotFoundException, Exception {
		byte[] assertionXML = inflateAssertionXML(deflatedAssertionXML);
		assertionXML        = Utils.decrypt(trustParams.getSecretKey(), assertionXML);
		Utils.logDebug("Assertion received in the cookie is :" + Utils.getStringFromUTF8Bytes(assertionXML), LOG);
		
		// load into xml
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true); // very important
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(new ByteArrayInputStream(assertionXML));
		return SAMLAssertion.getAssertionFromAssertionDocument(doc);
	}

	private String deflateAssertionXML(byte[] assertionXMLContent)	{
		// Deflate it
		byte[] deflatedBytes = DeflaterUtils.deflate(assertionXMLContent);
		
		String base64EncodedDeflated = Base64.encode(deflatedBytes);
//		Remove all \r\n characters.. they are illegal in cookie values
		base64EncodedDeflated = Utils.removeCRLFsInBase64EncodedText(base64EncodedDeflated);
		return base64EncodedDeflated;
	}

	private byte[] inflateAssertionXML(String deflatedAssertionXML) throws IOException, DataFormatException	{
		// Base64 decode the deflated content
		byte[] deflatedBytes = Base64.decode(deflatedAssertionXML);
		// Inflate it
		return DeflaterUtils.inflate(deflatedBytes);
	}

	private void putAssertionInCookie(SAMLAssertion assertion, HttpServletResponse httpResponse, TrustParameters trustParams) throws Exception {
		Utils.logDebug("Putting SAML Assertion in the cookie(s)", LOG);
		byte[] serializedAssertionContent = assertion.getSerializedContent();
		serializedAssertionContent        = Utils.encrypt(trustParams.getSecretKey(), serializedAssertionContent);
		String deflatedAssertionContent   = deflateAssertionXML(serializedAssertionContent);
		
		// Split it into blocks of size limit
		String[] splitPartsOfAssertion = Utils.splitText(deflatedAssertionContent, MAX_COOKIE_SIZE);
		Utils.logDebug(String.format("Assertion is split into %s cookies.", splitPartsOfAssertion.length), LOG);
		
		boolean setHttpOnlyFlag=false;
		try {
			Method method = Cookie.class.getMethod("setHttpOnly", boolean.class);
			if("setHttpOnly".equalsIgnoreCase(method.getName()))
				setHttpOnlyFlag=true;
		}catch(NoSuchMethodException nsme) {
			setHttpOnlyFlag = false;
			Utils.logDebug("Got NoSuchMethodException , hence not setting httponly attribue on cookie", LOG);
		}catch(SecurityException se) {
			setHttpOnlyFlag = false;
			Utils.logDebug("Got SecurityException , hence not setting httponly attribue", LOG);
		}

		for (int i = 0; i < splitPartsOfAssertion.length; i++) {
			// Create one cookie for each part
			String cookieName = COOKIE_PREFIX + i;
			String cookieValue = splitPartsOfAssertion[i];
			Cookie cookie = new Cookie(cookieName, cookieValue);
			if(setHttpOnlyFlag)
				cookie.setHttpOnly(true); //Setting http Only to prevent client side java script attacks
			httpResponse.addCookie(cookie);			
		}
		
		//set no. of cookies that are set , can use this as additional check during verification
		int cookiesCount = splitPartsOfAssertion.length;
		Cookie cookie = new Cookie(COOKIE_PREFIX+cookiesCount, (cookiesCount+1)+"");
		if(setHttpOnlyFlag)
			cookie.setHttpOnly(true);
		httpResponse.addCookie(cookie);		
		Utils.logDebug("SAML Assertion put in the cookie(s)", LOG);
	}

	protected String extractAssertionContentFromCookies(List<Cookie> assertionCookies) throws Exception	{
		// Sort all the cookies in the right order
		CookieComparator comparator = new CookieComparator();
		Collections.sort(assertionCookies, comparator);
		
		//Check if all the cookies that are set are retrieved
		if(!assertionCookies.isEmpty()) {
			Cookie cookieCount = assertionCookies.get(assertionCookies.size()-1);
			String expectedCookiesCount = cookieCount.getValue();
			if(Integer.parseInt(expectedCookiesCount) == assertionCookies.size()) {
				// All the cookies that are set are retrieved , removing content of last cookie
				// 1. check cookie names
				for(int i = 0 ; i < assertionCookies.size();i++) {
					Cookie cookie = assertionCookies.get(i);
					if(!cookie.getName().equalsIgnoreCase(COOKIE_PREFIX+i)) {
						throw new Exception("Cookie content is either not valid or malformed");						
					}					
				}				
				assertionCookies.remove(assertionCookies.size()-1);				
			}else{				
				throw new Exception("Cookie content is not valid");
			}
		}
		
		// Append all cookie values in the sorted order
		StringBuffer contentBuffer = new StringBuffer();
		for (Cookie cookie: assertionCookies) {
			contentBuffer.append(cookie.getValue());
		}
		return contentBuffer.toString();
	}
	/**
	 * To sort cookies.. Compares by comparing cookie values based on the number appended to the prefix.
	 * Cookies with smaller numbers come first in the ordering.
	 */
	private static class CookieComparator implements Comparator<Cookie>	{
		private static final Logger LOG = Logger.getLogger(CookieComparator.class.getName());

		@Override
		public int compare(Cookie cookie1, Cookie cookie2) {
			int firstCookieNumber = getNumberAfterPrefix(cookie1.getName());
			int secondCookieNumber = getNumberAfterPrefix(cookie2.getName());
			return (firstCookieNumber - secondCookieNumber);
		}
		private int getNumberAfterPrefix(String cookieName) {
			String appendedValue = cookieName.substring(COOKIE_PREFIX.length());
			try {
				return Integer.parseInt(appendedValue);
			} catch (NumberFormatException nfe) {
				/*
				 *  Return 0 for such cookies. One such cookie is allowed. If more than one is present, only the last one
				 *  is considered. Others are ignored.
				 */
				Utils.logWarn("An assertion cookie has no number..", nfe, LOG);
				return 0;
			}
		}

	}
}
