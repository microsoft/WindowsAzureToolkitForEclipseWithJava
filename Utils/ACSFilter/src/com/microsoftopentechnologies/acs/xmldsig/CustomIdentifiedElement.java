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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Class to represent those elements which are referred inside an XML signature,
 * but have an attribute other than 'ID' as the identifying attribute. Used only
 * in rare cases like SMAL 1.1 assertions.
 */
public class CustomIdentifiedElement {
	private Element element;
	private QName idAttribute;

	public CustomIdentifiedElement(Element element, QName idAttribute) {
		this.element = element;
		this.idAttribute = idAttribute;
	}

	public Element getElement() {
		return element;
	}

	public QName getIdAttribute() {
		return idAttribute;
	}
}
