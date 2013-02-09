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
package com.microsoftopentechnologies.acs.saml; 

import java.io.Serializable;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.microsoftopentechnologies.acs.util.Utils;
import com.microsoftopentechnologies.acs.xmldsig.TrustParameters;

public abstract class SAMLAssertion implements Serializable {
	private static final Logger LOG = Logger.getLogger(SAMLAssertion.class.getName());
	private static final long serialVersionUID = -5245722638457163959L;
	// Setting clockSkew to default value of 5 minutes.
	private static final long clockSkew = 300000;

	static final String ELEMENT_NAME_ASSERTION = "Assertion";
	static final String ASSERTION_20_NAMESPACE = "urn:oasis:names:tc:SAML:2.0:assertion";
	static final String ASSERTION_11_NAMESPACE = "urn:oasis:names:tc:SAML:1.0:assertion";	

	public abstract String getIssuer();
	public abstract Attribute[] getAttributes();
	public abstract Subject getSubject();
	public abstract Conditions getConditions();
	public abstract byte[] getSerializedContent();
	
	public static SAMLAssertion getAssertionFromSecurityToken(Element securityToken) throws AssertionNotFoundException, InvalidAssertionException {
		// First check for 2.0 assertion
		NodeList assertionNodes = securityToken.getElementsByTagNameNS(ASSERTION_20_NAMESPACE, ELEMENT_NAME_ASSERTION);
		// If not found, check for 1.1 assertion
		if (assertionNodes != null && assertionNodes.getLength() == 1) {			
			// Consider the first assertion element
			return new SAML20Assertion((Element) assertionNodes.item(0));
		} else {
			assertionNodes = securityToken.getElementsByTagNameNS(ASSERTION_11_NAMESPACE, ELEMENT_NAME_ASSERTION);
			if (assertionNodes != null && assertionNodes.getLength() == 1) {
				return new SAML11Assertion((Element) assertionNodes.item(0));
			} else {
				// Handling assertionNotFound case
				throw new AssertionNotFoundException("Invalid Assertion or Assertion not found in the security token.");
			}
		}
	}
	
	public static SAMLAssertion getAssertionFromAssertionDocument(Document assertionDocument) throws AssertionNotFoundException {
		// First check for 2.0 assertion
		NodeList assertionNodes = assertionDocument.getElementsByTagNameNS(ASSERTION_20_NAMESPACE, ELEMENT_NAME_ASSERTION);
		// If not found, check for 1.1 assertion
		if (assertionNodes != null && assertionNodes.getLength() == 1) {
			// Consider the first assertion element
			return new SAML20Assertion((Element) assertionNodes.item(0));
		} else {
			assertionNodes = assertionDocument.getElementsByTagNameNS(ASSERTION_11_NAMESPACE, ELEMENT_NAME_ASSERTION);
			if (assertionNodes != null && assertionNodes.getLength() == 1) {
				return new SAML11Assertion((Element) assertionNodes.item(0));
			} else {
				// Handling assertionNotFound case
				throw new AssertionNotFoundException("Invalid Assertion or Assertion not found in the security token.");
			}
		}
	}

	
	public static class Attribute {
		private String name;
		private String[] values;
		private String frinedelyName;
		private String nameFormat;

		Attribute(String name, String[] values) {
			this(name, values, null, null);
		}

		Attribute(String name, String[] values, String friendlyName, String nameFormat) {
			this.name = name;
			this.values = values;
			this.frinedelyName = friendlyName;
			this.nameFormat = nameFormat;
		}

		public String getName() {
			return name;
		}

		public String[] getValues() {
			return values;
		}

		public String getFrinedelyName() {
			return frinedelyName;
		}

		public String getNameFormat() {
			return nameFormat;
		}
	}

	public static class Subject {
		private String nameIdentifier;
		private String[] subjectConfirmationMethods;

		Subject(String nameIdentifier, String[] subjectConfirmationMethods) {
			this.nameIdentifier = nameIdentifier;
			this.subjectConfirmationMethods = subjectConfirmationMethods;
		}

		public String getNameIdentifier() {
			return nameIdentifier;
		}

		public String[] getSubjectConfirmationMethods() {
			return subjectConfirmationMethods;
		}
	}

	public static class Conditions {
		private static final Logger LOG = Logger.getLogger(Conditions.class.getName());
		/*
		 * notBefor and notOnOrAfter are in milliseconds..
		 *
		 * When notBeofore is not specified, the assertion is valid anytime
		 * before notOnOrAfter When notOnOrAfter is not specified, the assertion
		 * is valid anytime after notBefore. If both are not specified, the
		 * assertion is always valid.
		 */
		private long notBefore = Long.MIN_VALUE;
		private long notOnOrAfter = Long.MAX_VALUE;
		private String audienceRestriction = null ;
		

		Conditions() {
		}

		public void setNotBefore(long notBefore) {
			this.notBefore = notBefore;
		}

		public void setNotOnOrAfter(long notOnOrAfter) {
			this.notOnOrAfter = notOnOrAfter;
		}
		public String getAudienceRestriction() {
			return audienceRestriction;
		}
		public void setAudienceRestriction(String audienceRestriction) {
			this.audienceRestriction = audienceRestriction;
		}

		/*
		 * To test if the token is valid now based on the NotBefore and
		 * NotOnOrAfter attributes
		 */
		public boolean isValidInTime() {
			long currentTime = System.currentTimeMillis();
			Utils.logDebug("NotBefore time in the assertion is " + this.notBefore + " milliseconds",LOG);
			Utils.logDebug("NotOnOrAfter time in the assertion is" + this.notOnOrAfter + " milliseconds",LOG);
			Utils.logDebug("Current system time is " + System.currentTimeMillis() + " milliseconds",LOG);
			Utils.logDebug("Checking the assertion validity with 5 minutes margin for notBefore time...",LOG);
			// Setting clock skew to synchronize
			long notBefore2Check = (this.notBefore >= clockSkew) ? this.notBefore - clockSkew : this.notBefore;
			return (notBefore2Check <= currentTime) && (currentTime < this.notOnOrAfter);
		}

		/*
		 * A token is valid only when all the conditions in it are valid. For
		 * now, we are validating only with respect to time.
		 */
		public boolean areValid() {
			return isValidInTime();
		}
	}

	/*
	 * Checks the validity of the assertion. Like if the signature is valid or
	 * not, if the assertion expired or not... If the assertion is valid, the
	 * method simply returns. If the assertion is not valid, it throws exception
	 */
	public void checkAssertionValidity(TrustParameters certTrustParams,boolean checkDigitalSignature) throws InvalidAssertionException {
		// First check signature if flag is set
		if(checkDigitalSignature) {
			Utils.logDebug("Validating the signature in the assertion...", LOG);
			checkAssertionSignatureValidity(certTrustParams);
		}		
		// Then check wrt time
		Utils.logDebug("Validating the conditions in the assertion...", LOG);
		// This does time validations and also audience restriction
		if (!areConditionsValidNow(certTrustParams)) {
			Utils.logError("Conditions on the assertion are not valid now, at the current system time.", null, LOG);
			throw new InvalidAssertionException("Conditions on the assertion are not valid now, at the current system time.");
		}
		
		//Check subject confirmation
		checkSubjectConfirmationMethod();

	}

	public abstract void checkAssertionSignatureValidity(TrustParameters certTrustParams) throws InvalidAssertionException;
	
	public abstract void checkSubjectConfirmationMethod() throws InvalidAssertionException;
	
	public abstract Element getAssertionXMLElement();

	public boolean areConditionsValidNow(TrustParameters certTrustParams) {
		return (this.getConditions().areValid() && (this.getConditions().getAudienceRestriction().equals(certTrustParams.getRelyingPartyRealm())));
	}
}
