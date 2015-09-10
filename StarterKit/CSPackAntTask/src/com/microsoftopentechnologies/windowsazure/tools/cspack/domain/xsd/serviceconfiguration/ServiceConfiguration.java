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

import javax.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "ServiceConfiguration", namespace = "http://schemas.microsoft.com/ServiceHosting/2008/10/ServiceConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceConfiguration", namespace = "http://schemas.microsoft.com/ServiceHosting/2008/10/ServiceConfiguration", propOrder = {
        "role"
})
public class ServiceConfiguration {
    @XmlElement(name = "Role")
    protected List<Role> role;

    public List<Role> getRole() {
        return role;
    }
    
    public Role getRole(String name) {
    	for (Role role : getRole()) {
    		if (role.getName().equalsIgnoreCase(name)) {
    			return role;
    		}
    	}
    	return null;
    }
}
