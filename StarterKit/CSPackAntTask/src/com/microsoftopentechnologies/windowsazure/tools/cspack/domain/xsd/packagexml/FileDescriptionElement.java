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


package com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.packagexml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for FileDescriptionElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FileDescriptionElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DataContentReference" type="{http://schemas.microsoft.com/windowsazure}relativeUri"/>
 *         &lt;element name="CreatedTimeUtc" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="ModifiedTimeUtc" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="ReadOnly" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FileDescriptionElement", propOrder = {
    "dataContentReference",
    "createdTimeUtc",
    "modifiedTimeUtc",
    "readOnly"
})
public class FileDescriptionElement {

    @XmlElement(name = "DataContentReference", required = true)
    protected String dataContentReference;
    @XmlElement(name = "CreatedTimeUtc", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createdTimeUtc;
    @XmlElement(name = "ModifiedTimeUtc", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar modifiedTimeUtc;
    @XmlElement(name = "ReadOnly")
    protected boolean readOnly;

    /**
     * Gets the value of the dataContentReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataContentReference() {
        return dataContentReference;
    }

    /**
     * Sets the value of the dataContentReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataContentReference(String value) {
        this.dataContentReference = value;
    }

    /**
     * Gets the value of the createdTimeUtc property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreatedTimeUtc() {
        return createdTimeUtc;
    }

    /**
     * Sets the value of the createdTimeUtc property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreatedTimeUtc(XMLGregorianCalendar value) {
        this.createdTimeUtc = value;
    }

    /**
     * Gets the value of the modifiedTimeUtc property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getModifiedTimeUtc() {
        return modifiedTimeUtc;
    }

    /**
     * Sets the value of the modifiedTimeUtc property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setModifiedTimeUtc(XMLGregorianCalendar value) {
        this.modifiedTimeUtc = value;
    }

    /**
     * Gets the value of the readOnly property.
     * 
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets the value of the readOnly property.
     * 
     */
    public void setReadOnly(boolean value) {
        this.readOnly = value;
    }

}
