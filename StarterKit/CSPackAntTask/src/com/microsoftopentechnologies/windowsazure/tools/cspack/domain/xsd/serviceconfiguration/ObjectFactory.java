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


package com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.serviceconfiguration;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private final static QName _ServiceConfiguration_QNAME = new QName("http://schemas.microsoft.com/ServiceHosting/2008/10/ServiceConfiguration", "ServiceConfiguration");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.packagexml
     *
     */
    public ObjectFactory() {
    }

    public ServiceConfiguration createServiceConfiguration() {
        return new ServiceConfiguration();
    }

    @XmlElementDecl(namespace = "http://schemas.microsoft.com/ServiceHosting/2008/10/ServiceConfiguration", name = "ServiceConfiguration")
    public JAXBElement<ServiceConfiguration> createServiceConfiguration(ServiceConfiguration value) {
        return new JAXBElement<ServiceConfiguration>(_ServiceConfiguration_QNAME, ServiceConfiguration.class, null, value);
    }
}

