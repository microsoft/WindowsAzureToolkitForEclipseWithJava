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
