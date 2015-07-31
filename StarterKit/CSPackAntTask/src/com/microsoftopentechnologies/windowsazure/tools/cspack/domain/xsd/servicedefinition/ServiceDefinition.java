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

@XmlRootElement(name = "ServiceDefinition", namespace = "http://schemas.microsoft.com/ServiceHosting/2008/10/ServiceDefinition")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceDefinition", namespace = "http://schemas.microsoft.com/ServiceHosting/2008/10/ServiceDefinition", propOrder = {
        "name",
        "workerRole"
})
public class ServiceDefinition {
    @XmlAttribute(name = "name", required = true)
    protected String name;

    @XmlElement(name = "WorkerRole")
    protected List<Role> workerRole;

    public String getName() {
        return name;
    }

    public List<Role> getWorkerRole() {
        return workerRole;
    }

    public boolean isHasEndpoints() {
        for (Role role : workerRole) {
            if (role.isHasEndpoints()) {
                return true;
            }
        }
        return false;
    }

    public boolean isRemoteAccess() {
        for (Role role : workerRole) {
            if (role.isRemoteAccess()) {
                return true;
            }
        }
        return false;
    }

    public boolean isRemoteForwarder() {
        for (Role role : workerRole) {
            if (role.isRemoteForwarder()) {
                return true;
            }
        }
        return false;
    }

    public boolean isCaching() {
        for (Role role : workerRole) {
            if (role.isCaching()) {
                return true;
            }
        }
        return false;
    }

    public boolean isWebDeploy() {
        for (Role role : workerRole) {
            if (role.isWebDeploy()) {
                return true;
            }
        }
        return false;
    }
}
