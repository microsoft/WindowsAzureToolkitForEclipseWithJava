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

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.microsoftopentechnologies.acs.util.NamespaceContextImpl;
import com.microsoftopentechnologies.acs.util.Utils;
import com.microsoftopentechnologies.acs.xmldsig.SignatureValidationException;
import com.microsoftopentechnologies.acs.xmldsig.SignatureValidationResult;
import com.microsoftopentechnologies.acs.xmldsig.SignatureValidator;
import com.microsoftopentechnologies.acs.xmldsig.TrustParameters;

class SAML20Assertion extends SAMLAssertion {
	private static final Logger LOG = Logger.getLogger(SAML20Assertion.class.getName());

	private static final long serialVersionUID = -6574997353870543257L;

	private static final String ATTR_NAME = "Name";
	private static final String ATTR_NAMEFORMAT = "NameFormat";
	private static final String ATTR_FRIENDLYNAME = "FriendlyName";
	private static final String ATTR_NAME_METHOD = "Method";
	private static final String ATTR_NOTBEFORE = "NotBefore";
	private static final String ATTR_NOTONORAFTER = "NotOnOrAfter";
	private static final String ATTR_SUBJECT_BEARER_CONFIRMATION = "urn:oasis:names:tc:SAML:2.0:cm:bearer";

	private static final String XPATH_ISSUER = "./saml:Issuer";
	private static final String XPATH_ATTRIBUTE = "./saml:AttributeStatement/saml:Attribute";
	private static final String XPATH_ATTRIBUTE_VALUE = "./saml:AttributeValue";
	private static final String XPATH_SUBJECT = "./saml:Subject";
	private static final String XPATH_SUBJECT_NAMEID = "./saml:NameID";
	private static final String XPATH_SUBJECT_CONFIRMATION = "./saml:SubjectConfirmation";
	private static final String XPATH_CONDITIONS = "./saml:Conditions";
	private static final String XPATH_AUDIENCE = "./saml:Conditions/saml:AudienceRestriction/saml:Audience";
	
	
	

	private static final Map<String, String> NAMESPACESURLS_PREFIX = new HashMap<String, String>();
	private static final NamespaceContext NAMESPACE_CONTEXT;

	static {
		NAMESPACESURLS_PREFIX.put("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
		NAMESPACE_CONTEXT = new NamespaceContextImpl(null, NAMESPACESURLS_PREFIX);
	}

	private Element assertionXMLElement;
	private transient String issuer;
	private transient Attribute[] attributes;
	private transient Subject subject;
	private transient Conditions conditions;

	SAML20Assertion(Element assertionXMLElement) {
		super();
		this.assertionXMLElement = assertionXMLElement;
	}

	@Override
	public String getIssuer() {
		if (issuer == null) {
			try {
				XPath xpath = XPathFactory.newInstance().newXPath();
				xpath.setNamespaceContext(NAMESPACE_CONTEXT);

				issuer = xpath.evaluate(XPATH_ISSUER, this.assertionXMLElement);				
			} catch (XPathExpressionException e) {
				// Never occurs...so not handling
				Utils.logError("XPath experession exception while searching for Issuer ", e, LOG);
			}
		}
		return issuer;
	}

	@Override
	public Attribute[] getAttributes() {
		if (attributes != null) {
			return attributes;
		}

		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(NAMESPACE_CONTEXT);

			NodeList attributeNodes = (NodeList) xpath.evaluate(XPATH_ATTRIBUTE, this.assertionXMLElement, XPathConstants.NODESET);
			int number_attributes = attributeNodes.getLength();

			attributes = new Attribute[number_attributes];
			for (int i = 0; i < number_attributes; i++) {
				Element attributeElement = (Element) attributeNodes.item(i);
				String attrName = attributeElement.getAttribute(ATTR_NAME);
				String nameformat = attributeElement.getAttribute(ATTR_NAMEFORMAT);
				String friendlyName = attributeElement.getAttribute(ATTR_FRIENDLYNAME);

				NodeList attributevalueNodes = (NodeList) xpath.evaluate(XPATH_ATTRIBUTE_VALUE, attributeElement, XPathConstants.NODESET);
				int number_values = attributevalueNodes.getLength();
				String[] attributeValues = new String[number_values];
				for (int j = 0; j < number_values; j++) {
					attributeValues[j] = attributevalueNodes.item(j).getTextContent();
				}

				attributes[i] = new Attribute(attrName, attributeValues, friendlyName, nameformat);

			}
		} catch (XPathExpressionException e) {
			// Never occurs...so not handling
			Utils.logError("XPath experession exception while searching for Attributes ", e, LOG);
		}
		return attributes;
	}

	@Override
	public void checkAssertionSignatureValidity(TrustParameters certTrustParams) throws InvalidAssertionException {
		try {
			SignatureValidator signatureValidator = new SignatureValidator();
			assertionXMLElement.setIdAttribute("ID", true);
			SignatureValidationResult validationResult = signatureValidator.validateSignature(certTrustParams, assertionXMLElement);
			if (!validationResult.isSignatureValid()) {
				throw new InvalidAssertionException("XML Signature in the assertion failed validation." + validationResult.getReasonForFailure());
			}
		} catch (SignatureValidationException e) {
			Utils.logError("Exception occured while validating the signature in the assertion", e, LOG);
			throw new InvalidAssertionException("Incomplete assertion data", e);
		}
	}

	@Override
	public Subject getSubject() {
		if (subject != null) {
			return subject;
		}

		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(NAMESPACE_CONTEXT);

			Element subjectElement = (Element) xpath.evaluate(XPATH_SUBJECT, this.assertionXMLElement, XPathConstants.NODE);
			String nameID = xpath.evaluate(XPATH_SUBJECT_NAMEID, subjectElement);

			NodeList subjectConfirmationNodes = (NodeList) xpath.evaluate(XPATH_SUBJECT_CONFIRMATION, subjectElement, XPathConstants.NODESET);
			int number_confirmations = subjectConfirmationNodes.getLength();
			String[] confirmationMethods = new String[number_confirmations];
			for (int i = 0; i < number_confirmations; i++) {
				Element confirmationNode = (Element) subjectConfirmationNodes.item(i);
				confirmationMethods[i] = confirmationNode.getAttribute(ATTR_NAME_METHOD);
			}
			subject = new Subject(nameID, confirmationMethods);
		} catch (XPathExpressionException e) {
			// Never occurs...so not handling
			Utils.logError("XPath experession exception while searching for Subject ", e, LOG);
		}

		return subject;
	}

	@Override
	public Conditions getConditions() {
		if (conditions != null) {
			return conditions;
		}

		conditions = new Conditions();
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(NAMESPACE_CONTEXT);

			Element conditionsElement = (Element) xpath.evaluate(XPATH_CONDITIONS, this.assertionXMLElement, XPathConstants.NODE);
			String notBeforeAsString = conditionsElement.getAttribute(ATTR_NOTBEFORE);
			Utils.logDebug("NotBefore attribute value is " + notBeforeAsString, LOG);
			if (notBeforeAsString != null) {
				try {
					Date notBeforeTime = Utils.parseTimeFromXSDTIMEString(notBeforeAsString);
					conditions.setNotBefore(notBeforeTime.getTime());
				} catch (ParseException e) {
					Utils.logError("NotBefore attribute value of the assertion conditions is not in valid format.", e, LOG);
				}
			}
			String notOnAfterAsString = conditionsElement.getAttribute(ATTR_NOTONORAFTER);
			Utils.logDebug("NotOnOrAfter attribute value is " + notOnAfterAsString, LOG);
			if (notOnAfterAsString != null) {
				try {
					Date notOnAfterTime = Utils.parseTimeFromXSDTIMEString(notOnAfterAsString);
					conditions.setNotOnOrAfter(notOnAfterTime.getTime());
				} catch (ParseException e) {
					Utils.logError("NotOnOrAfter attribute value of the assertion conditions is not in valid format.", e, LOG);
				}
			}
			conditions.setAudienceRestriction(xpath.evaluate(XPATH_AUDIENCE, this.assertionXMLElement));
		} catch (XPathExpressionException e) {
			// Never occurs...so not handling
			Utils.logError("XPath experession exception while searching for Conditions ", e, LOG);
		}

		return conditions;
	}

	@Override
	public byte[] getSerializedContent() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Utils.writeXMLToStream(this.assertionXMLElement, baos, false);
		} catch (TransformerException e) {
			// Won't occur
			Utils.logError("An exception occured while serializing the assertion element into bytes.", e, LOG);
		}

		return baos.toByteArray();
	}
	
	public void checkSubjectConfirmationMethod() throws InvalidAssertionException {
		String subjectConfimationMethod[] =  getSubject().getSubjectConfirmationMethods();
		if(subjectConfimationMethod != null && subjectConfimationMethod.length > 0) {
			
			boolean found = false;
			for (int i = 0;i < subjectConfimationMethod.length;i++) {
				if(subjectConfimationMethod[i].equalsIgnoreCase(ATTR_SUBJECT_BEARER_CONFIRMATION)) {
					found = true;
					break;
				}
			}
			
			if(!found)
				throw new InvalidAssertionException("Subject confirmation method is not Bearer.");
			
		}else{
			Utils.logError("Subject confirmation method is not Bearer", null, LOG);
			throw new InvalidAssertionException("Subject confirmation method is not Bearer.");
		}
		
	}
	
	public Element getAssertionXMLElement() {
		return this.assertionXMLElement;
	}
}
