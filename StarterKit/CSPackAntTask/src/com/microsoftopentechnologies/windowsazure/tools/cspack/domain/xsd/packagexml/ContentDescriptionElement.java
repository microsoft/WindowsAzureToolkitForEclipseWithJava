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


package com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.packagexml;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ContentDescriptionElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ContentDescriptionElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LengthInBytes" type="{http://www.w3.org/2001/XMLSchema}unsignedLong"/>
 *         &lt;element name="IntegrityCheckHashAlgortihm" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="IntegrityCheckHash" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="DataStorePath" type="{http://schemas.microsoft.com/windowsazure}relativeUri"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContentDescriptionElement", propOrder = {
    "lengthInBytes",
    "integrityCheckHashAlgortihm",
    "integrityCheckHash",
    "dataStorePath"
})
public class ContentDescriptionElement {

    @XmlElement(name = "LengthInBytes", required = true)
    @XmlSchemaType(name = "unsignedLong")
    protected BigInteger lengthInBytes;
    @XmlElement(name = "IntegrityCheckHashAlgortihm", required = true)
    protected String integrityCheckHashAlgortihm;
    @XmlElement(name = "IntegrityCheckHash", required = true, nillable = true)
    protected String integrityCheckHash;
    @XmlElement(name = "DataStorePath", required = true)
    protected String dataStorePath;

    /**
     * Gets the value of the lengthInBytes property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLengthInBytes() {
        return lengthInBytes;
    }

    /**
     * Sets the value of the lengthInBytes property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLengthInBytes(BigInteger value) {
        this.lengthInBytes = value;
    }

    /**
     * Gets the value of the integrityCheckHashAlgortihm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIntegrityCheckHashAlgortihm() {
        return integrityCheckHashAlgortihm;
    }

    /**
     * Sets the value of the integrityCheckHashAlgortihm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIntegrityCheckHashAlgortihm(String value) {
        this.integrityCheckHashAlgortihm = value;
    }

    /**
     * Gets the value of the integrityCheckHash property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public String getIntegrityCheckHash() {
        return integrityCheckHash;
    }

    /**
     * Sets the value of the integrityCheckHash property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setIntegrityCheckHash(String value) {
        this.integrityCheckHash = value;
    }

    /**
     * Gets the value of the dataStorePath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataStorePath() {
        return dataStorePath;
    }

    /**
     * Sets the value of the dataStorePath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataStorePath(String value) {
        this.dataStorePath = value;
    }

}
