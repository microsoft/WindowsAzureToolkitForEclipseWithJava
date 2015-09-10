/**
 * Copyright (c) Microsoft Corporation
 * 
 * All rights reserved. 
 * 
 * MIT License
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.microsoftopentechnologies.acs.util; 

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class NamespaceContextImpl implements NamespaceContext {
	private static final Logger LOG = Logger.getLogger(NamespaceContextImpl.class.getName());

	private Map<String, String> prefixesToNamespaces;
	private String defaultNamespaceURI = null;

	public NamespaceContextImpl(String defaultNamespaceURI)	{
		this.defaultNamespaceURI = defaultNamespaceURI;
		prefixesToNamespaces = new HashMap<String, String>();
	}

	public NamespaceContextImpl(String defaultNamespaceURI, Map<String, String> prefixToNamespaceURIMappings) {
		this.defaultNamespaceURI = defaultNamespaceURI;
		prefixesToNamespaces = new HashMap<String, String>(prefixToNamespaceURIMappings);
	}

	@Override
	public String getNamespaceURI(String prefix) {
		if (prefix == null) {
			throw new IllegalArgumentException("Prefix can not be null");
		}

		String namespaceURI = null;
		if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
			namespaceURI = this.defaultNamespaceURI;
		} else if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
			namespaceURI = XMLConstants.XML_NS_URI;
		} else if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
			namespaceURI = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		} else {
			namespaceURI = prefixesToNamespaces.get(prefix);
		}
		
		if (namespaceURI == null) {
			Utils.logDebug(String.format("No NamespaceURI mapping found for prefix '%s'. So returning %s", prefix, XMLConstants.NULL_NS_URI), LOG);
			namespaceURI = XMLConstants.NULL_NS_URI; // Standard
		}
		return namespaceURI;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		// Not required during xpath eval
		return null;
	}

	@Override
	public Iterator<?> getPrefixes(String namespaceURI) {
		// Not required during xpath eval
		return null;
	}
}
