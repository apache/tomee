/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee.oej2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for resource-env-refType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="resource-env-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ref-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;element name="pattern" type="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType"/>
 *           &lt;element name="message-destination-link" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;sequence>
 *             &lt;element name="admin-object-module" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *             &lt;element name="admin-object-link" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;/sequence>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resource-env-refType", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", propOrder = {
    "refName",
    "pattern",
    "messageDestinationLink",
    "adminObjectModule",
    "adminObjectLink"
})
public class ResourceEnvRefType {

    @XmlElement(name = "ref-name", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", required = true)
    protected java.lang.String refName;
    @XmlElement(name = "pattern", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected PatternType pattern;
    @XmlElement(name = "message-destination-link", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected java.lang.String messageDestinationLink;
    @XmlElement(name = "admin-object-module", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected java.lang.String adminObjectModule;
    @XmlElement(name = "admin-object-link", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected java.lang.String adminObjectLink;

    /**
     * Gets the value of the refName property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getRefName() {
        return refName;
    }

    /**
     * Sets the value of the refName property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setRefName(java.lang.String value) {
        this.refName = value;
    }

    /**
     * Gets the value of the pattern property.
     * 
     * @return
     *     possible object is
     *     {@link PatternType }
     *     
     */
    public PatternType getPattern() {
        return pattern;
    }

    /**
     * Sets the value of the pattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link PatternType }
     *     
     */
    public void setPattern(PatternType value) {
        this.pattern = value;
    }

    /**
     * Gets the value of the messageDestinationLink property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getMessageDestinationLink() {
        return messageDestinationLink;
    }

    /**
     * Sets the value of the messageDestinationLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setMessageDestinationLink(java.lang.String value) {
        this.messageDestinationLink = value;
    }

    /**
     * Gets the value of the adminObjectModule property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getAdminObjectModule() {
        return adminObjectModule;
    }

    /**
     * Sets the value of the adminObjectModule property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setAdminObjectModule(java.lang.String value) {
        this.adminObjectModule = value;
    }

    /**
     * Gets the value of the adminObjectLink property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getAdminObjectLink() {
        return adminObjectLink;
    }

    /**
     * Sets the value of the adminObjectLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setAdminObjectLink(java.lang.String value) {
        this.adminObjectLink = value;
    }

}
