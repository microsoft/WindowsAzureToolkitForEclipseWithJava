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

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "inputEndpoint",
        "internalEndpoint",
        "instanceInputEndpoint"
})
public class Endpoints {
    @XmlElement(name = "InputEndpoint")
    protected List<InputEndpoint> inputEndpoint = new ArrayList<InputEndpoint>();
    @XmlElement(name = "InternalEndpoint")
    protected List<InternalEndpoint> internalEndpoint = new ArrayList<InternalEndpoint>();
    @XmlElement(name = "InstanceInputEndpoint")
    protected List<InstanceInputEndpoint> instanceInputEndpoint = new ArrayList<InstanceInputEndpoint>();

    public List<InputEndpoint> getInputEndpoint() {
        return inputEndpoint;
    }

    public List<InternalEndpoint> getInternalEndpoint() {
        return internalEndpoint;
    }

    public List<InstanceInputEndpoint> getInstanceInputEndpoint() {
        return instanceInputEndpoint;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "localPort",
            "port",
            "certificate"
    })
    public static class InputEndpoint extends AbstractEndpoint {
        @XmlAttribute(name = "localPort")
        protected String localPort;
        @XmlAttribute(name = "port")
        protected String port;
        @XmlAttribute(name = "certificate")
        protected String certificate;

        public String getLocalPort() {
            return localPort;
        }

        public String getPort() {
            return port;
        }

        public String getCertificate() {
            return certificate;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "fixedPort",
            "fixedPortRange"
    })
    public static class InternalEndpoint extends AbstractEndpoint {
        @XmlElement(name = "FixedPort")
        protected FixedPort fixedPort;
        @XmlElement(name = "FixedPortRange")
        protected FixedPortRange fixedPortRange;

        public InternalEndpoint() {
        }

        public InternalEndpoint(String name, String protocol) {
            this.name = name;
            this.protocol = protocol;
        }

        public FixedPort getFixedPort() {
            return fixedPort;
        }

        public FixedPortRange getFixedPortRange() {
            return fixedPortRange;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "localPort",
            "allocatePublicPortFrom"
    })
    public static class InstanceInputEndpoint extends AbstractEndpoint {
        @XmlAttribute(name = "localPort")
        protected String localPort;
        @XmlElement(name = "AllocatePublicPortFrom")
        protected AllocatePublicPortFrom allocatePublicPortFrom;

        public String getLocalPort() {
            return localPort;
        }

        public AllocatePublicPortFrom getAllocatePublicPortFrom() {
            return allocatePublicPortFrom;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "name",
            "protocol"
    })
    public static abstract class AbstractEndpoint {
        @XmlAttribute(name = "name")
        protected String name;
        @XmlAttribute(name = "protocol")
        protected String protocol;

        public String getName() {
            return name;
        }

        public String getProtocol() {
            return protocol;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "fixedPortRange"
    })
    public static class AllocatePublicPortFrom {
        @XmlElement(name = "FixedPortRange")
        protected FixedPortRange fixedPortRange;

        public FixedPortRange getFixedPortRange() {
            return fixedPortRange;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "port"
    })
    public static class FixedPort {
        @XmlAttribute(name = "port")
        protected String port;

        public String getPort() {
            return port;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "min",
            "max"
    })
    public static class FixedPortRange {
        @XmlAttribute(name = "min")
        protected String min;
        @XmlAttribute(name = "max")
        protected String max;

        public String getMin() {
            return min;
        }

        public String getMax() {
            return max;
        }
    }
}
