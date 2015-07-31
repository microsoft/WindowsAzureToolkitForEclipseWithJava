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
