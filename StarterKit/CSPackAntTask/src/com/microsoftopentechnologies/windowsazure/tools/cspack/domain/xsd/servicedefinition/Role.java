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

import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.serviceconfiguration.ConfigurationSettings;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "name",
        "vmsize",
        "startup",
        "runtime",
        "imports",
        "endpoints",
        "localResources",
        "certificates"
})
public class Role {
    public static final String REMOTE_ACCESS = "RemoteAccess";
    public static final String REMOTE_FORWARDER = "RemoteForwarder";
    public static final String CACHING = "Caching";
    public static final String WEB_DEPLOY = "WebDeploy";

    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "vmsize")
    protected String vmsize;
    @XmlElement(name = "Startup")
    protected Startup startup;
    @XmlElement(name = "Runtime")
    protected Runtime runtime;
    @XmlElement(name = "Imports")
    protected Imports imports;
    @XmlElement(name = "Endpoints")
    protected Endpoints endpoints;
    @XmlElement(name = "LocalResources")
    protected LocalResources localResources;
    @XmlElement(name = "Certificates")
    protected Certificates certificates;

    @XmlTransient
    private List<ConfigurationSettings.Setting> settings = new ArrayList<ConfigurationSettings.Setting>();
    @XmlTransient
    private String filename;

    public String getName() {
        return name;
    }

    public String getVmsize() {
        return vmsize;
    }

    public Startup getStartup() {
        return startup;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public Imports getImports() {
        if (imports == null) {
            return new Imports();
        }
        return imports;
    }

    public Endpoints getEndpoints() {
        return endpoints;
    }

    public Certificates getCertificates() {
        return certificates;
    }

    public LocalResources getLocalResources() {
        return localResources;
    }

    public List<ConfigurationSettings.Setting> getSettings() {
        return settings;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isRemoteAccess() {

        if (getImports() == null || getImports().getImportElements().isEmpty())
            return false;
        for (ImportElement importElement : getImports().getImportElements()) {
            if (REMOTE_ACCESS.equals(importElement.getModuleName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isRemoteForwarder() {
        if (getImports() == null || getImports().getImportElements().isEmpty())
            return false;
        for (ImportElement importElement : getImports().getImportElements()) {
            if (REMOTE_FORWARDER.equals(importElement.getModuleName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isCaching() {
        if (getImports() == null || getImports().getImportElements().isEmpty())
            return false;
        for (ImportElement importElement : getImports().getImportElements()) {
            if (CACHING.equals(importElement.getModuleName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isWebDeploy() {
        if (getImports() == null || getImports().getImportElements().isEmpty())
            return false;
        for (ImportElement importElement : getImports().getImportElements()) {
            if (WEB_DEPLOY.equals(importElement.getModuleName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isHasEndpoints() {
        return getEndpoints() != null && !getEndpoints().getInputEndpoint().isEmpty();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "task"
    })
    public static class Startup {
        @XmlElement(name = "Task")
        protected List<Task> task;

        public List<Task> getTask() {
            return task;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "importElements"
    })
    public static class Imports {
        @XmlElement(name = "Import")
        protected List<ImportElement> importElements;

        public List<ImportElement> getImportElements() {
            if (importElements == null) {
                importElements = new ArrayList<ImportElement>();
            }
            return this.importElements;
        }
    }
}
