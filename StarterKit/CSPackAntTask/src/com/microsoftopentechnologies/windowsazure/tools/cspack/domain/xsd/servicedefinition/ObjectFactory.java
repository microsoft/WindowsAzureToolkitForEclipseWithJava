/*
 Copyright 2015 Microsoft Open Technologies, Inc.

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


package com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.servicedefinition;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private final static QName _ServiceDefinition_QNAME = new QName("http://schemas.microsoft.com/ServiceHosting/2008/10/ServiceDefinition", "ServiceDefinition");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.packagexml
     *
     */
    public ObjectFactory() {
    }

    public ServiceDefinition createServiceDefinition() {
        return new ServiceDefinition();
    }

    @XmlElementDecl(namespace = "http://schemas.microsoft.com/ServiceHosting/2008/10/ServiceDefinition", name = "ServiceDefinition")
    public JAXBElement<ServiceDefinition> createPackageDefinition(ServiceDefinition value) {
        return new JAXBElement<ServiceDefinition>(_ServiceDefinition_QNAME, ServiceDefinition.class, null, value);
    }
}

