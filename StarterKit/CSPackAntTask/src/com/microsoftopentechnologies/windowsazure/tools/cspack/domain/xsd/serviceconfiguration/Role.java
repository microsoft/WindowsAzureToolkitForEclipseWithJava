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


package com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.serviceconfiguration;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "name",
        "configurationSettings",
        "certificates"
})
public class Role {
    @XmlAttribute
    protected String name;
    @XmlElement(name = "ConfigurationSettings")
    protected ConfigurationSettings configurationSettings;
    @XmlElement(name = "Certificates")
    protected Certificates certificates;

    public String getName() {
        return name;
    }

    public ConfigurationSettings getConfigurationSettings() {
        return configurationSettings;
    }

    public Certificates getCertificates() {
        return certificates;
    }
}
