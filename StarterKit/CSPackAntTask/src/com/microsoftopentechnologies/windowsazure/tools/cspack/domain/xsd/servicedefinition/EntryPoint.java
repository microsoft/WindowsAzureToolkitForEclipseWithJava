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
