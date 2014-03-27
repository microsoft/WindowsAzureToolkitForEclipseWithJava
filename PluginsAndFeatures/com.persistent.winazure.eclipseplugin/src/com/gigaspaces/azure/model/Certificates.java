/**
* Copyright 2013 Persistent Systems Ltd.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

package com.gigaspaces.azure.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="Certificates",namespace="http://schemas.microsoft.com/windowsazure")
public class Certificates implements Iterable<Certificate> {
	
	private List<Certificate> certificates;

	/**
	 * @return the certificates
	 */
	@XmlElement(name = "Certificate",namespace="http://schemas.microsoft.com/windowsazure")
	public List<Certificate> getCertificates() {
		return certificates;
	}

	/**
	 * @param certificates
	 * the certificates to set
	 */
	public void setCertificates(List<Certificate> certificates) {
		this.certificates = certificates;
	}
	
	public Iterator<Certificate> iterator() {
		return certificates.iterator();
	}
	
	public int size() {
		if (isEmpty()) return 0;
		return certificates.size();
	}
	
	public boolean isEmpty() {
		return (certificates == null);
	}
	
}
