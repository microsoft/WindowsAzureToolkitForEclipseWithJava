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
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "setting"
})
public class ConfigurationSettings {
    @XmlElement(name = "Setting")
    protected List<Setting> setting;

    public List<Setting> getSetting() {
        if (setting == null) {
            setting = new ArrayList<Setting>();
        }
        return this.setting;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "name",
            "value"
    })
    public static class Setting {
        @XmlAttribute
        protected String name;
        @XmlAttribute
        protected String value;

        public Setting() {
        }

        public Setting(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public String getShortName() {
            return name.substring(name.lastIndexOf(".") + 1, name.length());
        }

        public String getPrefix() {
            return name.substring(0, name.lastIndexOf("."));
        }
    }
}
