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

package org.apache.openejb.jee.oejb2;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for resource-env-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="resource-env-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ref-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="pattern" type="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType"/&gt;
 *           &lt;element name="message-destination-link" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *           &lt;sequence&gt;
 *             &lt;element name="admin-object-module" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *             &lt;element name="admin-object-link" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *           &lt;/sequence&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resource-env-refType", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", propOrder = {
    "refName",
    "pattern",
    "messageDestinationLink",
    "adminObjectModule",
    "adminObjectLink",
    "referenceClass",
    "stringAddrType",
    "stringAddr",
    "objectFactory",
    "objectFactoryLocation"
})
public class ResourceEnvRefType {

    @XmlElement(name = "ref-name", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", required = true)
    protected String refName;
    @XmlElement(name = "pattern", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected PatternType pattern;
    @XmlElement(name = "message-destination-link", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected String messageDestinationLink;
    @XmlElement(name = "admin-object-module", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected String adminObjectModule;
    @XmlElement(name = "admin-object-link", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected String adminObjectLink;
    @XmlElement(name = "reference-class", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected String referenceClass;
    @XmlElement(name = "string-addr-type", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected String stringAddrType;
    @XmlElement(name = "string-addr", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected String stringAddr;
    @XmlElement(name = "object-factory", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected String objectFactory;
    @XmlElement(name = "object-factory-location", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected String objectFactoryLocation;

    /**
     * Gets the value of the refName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRefName() {
        return refName;
    }

    /**
     * Sets the value of the refName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRefName(final String value) {
        this.refName = value;
    }

    /**
     * Gets the value of the pattern property.
     *
     * @return possible object is
     * {@link PatternType }
     */
    public PatternType getPattern() {
        return pattern;
    }

    /**
     * Sets the value of the pattern property.
     *
     * @param value allowed object is
     *              {@link PatternType }
     */
    public void setPattern(final PatternType value) {
        this.pattern = value;
    }

    /**
     * Gets the value of the messageDestinationLink property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMessageDestinationLink() {
        return messageDestinationLink;
    }

    /**
     * Sets the value of the messageDestinationLink property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMessageDestinationLink(final String value) {
        this.messageDestinationLink = value;
    }

    /**
     * Gets the value of the adminObjectModule property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAdminObjectModule() {
        return adminObjectModule;
    }

    /**
     * Sets the value of the adminObjectModule property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAdminObjectModule(final String value) {
        this.adminObjectModule = value;
    }

    /**
     * Gets the value of the adminObjectLink property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getAdminObjectLink() {
        return adminObjectLink;
    }

    /**
     * Sets the value of the adminObjectLink property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAdminObjectLink(final String value) {
        this.adminObjectLink = value;
    }

    public String getObjectFactory() {
        return objectFactory;
    }

    public void setObjectFactory(final String objectFactory) {
        this.objectFactory = objectFactory;
    }

    public String getObjectFactoryLocation() {
        return objectFactoryLocation;
    }

    public void setObjectFactoryLocation(final String objectFactoryLocation) {
        this.objectFactoryLocation = objectFactoryLocation;
    }

    public String getReferenceClass() {
        return referenceClass;
    }

    public void setReferenceClass(final String referenceClass) {
        this.referenceClass = referenceClass;
    }

    public String getStringAddr() {
        return stringAddr;
    }

    public void setStringAddr(final String stringAddr) {
        this.stringAddr = stringAddr;
    }

    public String getStringAddrType() {
        return stringAddrType;
    }

    public void setStringAddrType(final String stringAddrType) {
        this.stringAddrType = stringAddrType;
    }
}
