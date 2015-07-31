/*
 Copyright Microsoft Corp.

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
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "commandLine",
        "executionContext",
        "environment"
})
public class Task {
    @XmlAttribute
    protected String commandLine;
    @XmlAttribute
    protected String executionContext;
    @XmlElement(name = "Environment")
    protected Environment environment;

    public String getCommandLine() {
        return commandLine;
    }

    public String getExecutionContext() {
        return executionContext;
    }

    public Environment getEnvironment() {
        return environment;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "variable"
    })
    public static class Environment {
        @XmlElement(name = "Variable")
        protected List<Variable> variable;

        public List<Variable> getVariable() {
            return variable;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "name",
            "roleInstanceValue"
    })
    public static class Variable {
        @XmlAttribute
        protected String name;
        @XmlElement(name = "RoleInstanceValue")
        protected RoleInstanceValue roleInstanceValue;

        public String getName() {
            return name;
        }

        public RoleInstanceValue getRoleInstanceValue() {
            return roleInstanceValue;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "xpath"
    })
    public static class RoleInstanceValue {
        @XmlAttribute
        protected String xpath;

        public String getXpath() {
            return xpath;
        }
    }
}
