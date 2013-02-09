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
