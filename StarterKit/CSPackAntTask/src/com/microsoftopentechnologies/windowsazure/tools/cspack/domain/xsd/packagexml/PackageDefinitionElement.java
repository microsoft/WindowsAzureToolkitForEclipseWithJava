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


package com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.packagexml;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for PackageDefinitionElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PackageDefinitionElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PackageMetaData" type="{http://schemas.microsoft.com/windowsazure}PackageMetaDataElement"/>
 *         &lt;element name="PackageContents" type="{http://schemas.microsoft.com/windowsazure}PackageContentsElement"/>
 *         &lt;element name="PackageLayouts" type="{http://schemas.microsoft.com/windowsazure}PackageLayoutsElement"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement(name="PackageDefinition", namespace = "http://schemas.microsoft.com/windowsazure")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PackageDefinitionElement", namespace = "http://schemas.microsoft.com/windowsazure", propOrder = {
    "packageMetaData",
    "packageContents",
    "packageLayouts"
})
public class PackageDefinitionElement {

    @XmlElement(name = "PackageMetaData", required = true)
    protected PackageMetaDataElement packageMetaData;
    @XmlElement(name = "PackageContents", required = true)
    protected PackageContentsElement packageContents;
    @XmlElement(name = "PackageLayouts", required = true)
    protected PackageLayoutsElement packageLayouts;

    /**
     * Gets the value of the packageMetaData property.
     * 
     * @return
     *     possible object is
     *     {@link PackageMetaDataElement }
     *     
     */
    public PackageMetaDataElement getPackageMetaData() {
        return packageMetaData;
    }

    /**
     * Sets the value of the packageMetaData property.
     * 
     * @param value
     *     allowed object is
     *     {@link PackageMetaDataElement }
     *     
     */
    public void setPackageMetaData(PackageMetaDataElement value) {
        this.packageMetaData = value;
    }

    /**
     * Gets the value of the packageContents property.
     * 
     * @return
     *     possible object is
     *     {@link PackageContentsElement }
     *     
     */
    public PackageContentsElement getPackageContents() {
        return packageContents;
    }

    /**
     * Sets the value of the packageContents property.
     * 
     * @param value
     *     allowed object is
     *     {@link PackageContentsElement }
     *     
     */
    public void setPackageContents(PackageContentsElement value) {
        this.packageContents = value;
    }

    /**
     * Gets the value of the packageLayouts property.
     * 
     * @return
     *     possible object is
     *     {@link PackageLayoutsElement }
     *     
     */
    public PackageLayoutsElement getPackageLayouts() {
        return packageLayouts;
    }

    /**
     * Sets the value of the packageLayouts property.
     * 
     * @param value
     *     allowed object is
     *     {@link PackageLayoutsElement }
     *     
     */
    public void setPackageLayouts(PackageLayoutsElement value) {
        this.packageLayouts = value;
    }

}
