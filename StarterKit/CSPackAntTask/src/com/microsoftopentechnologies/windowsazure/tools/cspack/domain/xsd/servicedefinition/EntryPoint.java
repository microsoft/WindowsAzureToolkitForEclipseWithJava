/*
 * Copyright Microsoft Corp.
 * 
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

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "programEntryPoint",
        "netFxEntryPoint"
})
public final class EntryPoint {
    @XmlElement(name = "ProgramEntryPoint")
    protected ProgramEntryPoint programEntryPoint;
    @XmlElement(name = "NetFxEntryPoint")
    protected NetFxEntryPoint netFxEntryPoint;

    public ProgramEntryPoint getProgramEntryPoint() {
        return programEntryPoint;
    }

    public NetFxEntryPoint getNetFxEntryPoint() {
        return netFxEntryPoint;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "commandLine"
    })
    public static class ProgramEntryPoint {
        @XmlAttribute
        protected String commandLine;
        @XmlAttribute
        protected String setReadyOnProcessStart;

        public String getCommandLine() {
            return commandLine;
        }

        public String getSetReadyOnProcessStart() {
            return setReadyOnProcessStart;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "assemblyName",
            "targetFrameworkVersion"
    })
    public static class NetFxEntryPoint {
        @XmlAttribute
        protected String assemblyName;
        @XmlAttribute
        protected String targetFrameworkVersion;

        public String getAssemblyName() {
            return assemblyName;
        }

        public String getTargetFrameworkVersion() {
            return targetFrameworkVersion;
        }
    }
}
