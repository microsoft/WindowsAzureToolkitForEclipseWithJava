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
package com.microsoftopentechnologies.acs.xmldsig; 

import java.util.List;
import java.util.logging.Logger;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.microsoftopentechnologies.acs.util.Utils;

/**
 * This class encapsulates the functionality of validating an XML digital
 * signature. This class makes some assumptions. They are 1. The signature is an
 * enveloped signature 2. The enveloping xml element has only one signature
 * contained in it. 3. The enveloping xml element contains all the elements
 * referred in the xml signature, no external URI references 4. KeyInfo element
 * is present in the xml signature.
 */
public class SignatureValidator {
	private static final Logger LOG = Logger.getLogger(SignatureValidator.class.getName());
	private static final String SIGNATURE_NODE = "Signature";

	// Both the methods can be static.. Made instance methods for logging
	public SignatureValidationResult validateSignature(TrustParameters certTrustParams, Element containingElement) throws SignatureValidationException {
		return validateSignature(certTrustParams, containingElement, (CustomIdentifiedElement[]) null);
	}

	public SignatureValidationResult validateSignature(TrustParameters certTrustParams, Element containingElement, CustomIdentifiedElement... custElements) throws SignatureValidationException {
		Utils.logDebug("Validating signature...", LOG);
		SignatureValidationResult validationResult = null;

		// Find the signature element
		NodeList signatureElements = containingElement.getElementsByTagNameNS(XMLSignature.XMLNS, SIGNATURE_NODE);

		// Consider the first element alone
		Element signatureElement = (Element) signatureElements.item(0);
		if (signatureElement == null) {
			Utils.logError("XML Digital Signature not found in the document", null, LOG);
			throw new SignatureValidationException("XML Digital Signature not found in the document");
		}

		// Create a DOMValidateContext
		DOMValidateContext validateContext = new DOMValidateContext(certTrustParams.getPublicKey(), signatureElement);
		
		// Set ID attributes for element which are identified with some other attributes
		if (custElements != null) {
			for (CustomIdentifiedElement differentlyIdentifiedElement : custElements) {
				Utils.logDebug("Element with a non-default identifer in the signature ..." + differentlyIdentifiedElement.getElement().getNodeName(), LOG);
				validateContext.setIdAttributeNS(differentlyIdentifiedElement.getElement(), differentlyIdentifiedElement.getIdAttribute().getNamespaceURI(), differentlyIdentifiedElement.getIdAttribute().getLocalPart());
			}
		}
		// Unmarshall the signature
		XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");
		try {
			XMLSignature signature = signatureFactory.unmarshalXMLSignature(validateContext);

			// Now validate the signature
			boolean isSignatureValid = signature.validate(validateContext);
			Utils.logDebug("signature validation result is " + isSignatureValid, LOG);
			if (isSignatureValid) {
				validationResult = new SignatureValidationResult(true, null);
			} else {
				// Validation failed. Find the reason for failure
				String reasonForFailure = null;

				// First check if signature value is valid
				if (signature.getSignatureValue().validate(validateContext)) {
					Utils.logDebug("SignatureValue of the XML signature is valid. Checking if all the digest values are valid...", LOG);

					// then check if the digests of references are matching
					@SuppressWarnings("rawtypes")
					List references = signature.getSignedInfo().getReferences();
					for (Object listItem : references) {
						Reference reference = (Reference) listItem;
						if (!reference.validate(validateContext)) {
							reasonForFailure = "Digest of the reference with URI - " + reference.getURI() + " failed validation.";
							Utils.logDebug(reasonForFailure, LOG);
							break;
						}
					}
				} else {
					reasonForFailure = "SignatureValue of the XML signature is invalid.";
					Utils.logDebug(reasonForFailure, LOG);
				}
				if (reasonForFailure == null) {
					// References are valid, signature value is also valid. Some
					// unexpected issue. Very unlikely.
					reasonForFailure = "Unxpected reason for failure. Both SignatureValue and References are valid.";

				}

				validationResult = new SignatureValidationResult(false, reasonForFailure);
			}
		} catch (MarshalException e) {
			e.printStackTrace();
			Utils.logError("Unmarshalling the signature from the xml document failed.", e, LOG);
			throw new SignatureValidationException("Unmarshalling the signature from the xml document failed.", e);
		} catch (XMLSignatureException e) {
			Utils.logError("An unexpected exception occured while validating the XML signature.", e, LOG);
			throw new SignatureValidationException("An unexpected exception occured while validating the XML signature.", e);
		}

		return validationResult;
	}
}
