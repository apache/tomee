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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee.bval;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for executable-validationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="executable-validationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="default-validated-executable-types" type="{http://jboss.org/xml/ns/javax/validation/configuration}default-validated-executable-typesType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "executable-validationType", namespace = "http://jboss.org/xml/ns/javax/validation/configuration", propOrder = {
    "defaultValidatedExecutableTypes"
})
public class ExecutableValidationType {

    @XmlElement(name = "default-validated-executable-types")
    protected DefaultValidatedExecutableTypesType defaultValidatedExecutableTypes;
    @XmlAttribute(name = "enabled")
    protected Boolean enabled;

    /**
     * Gets the value of the defaultValidatedExecutableTypes property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultValidatedExecutableTypesType }
     *     
     */
    public DefaultValidatedExecutableTypesType getDefaultValidatedExecutableTypes() {
        return defaultValidatedExecutableTypes;
    }

    /**
     * Sets the value of the defaultValidatedExecutableTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultValidatedExecutableTypesType }
     *     
     */
    public void setDefaultValidatedExecutableTypes(DefaultValidatedExecutableTypesType value) {
        this.defaultValidatedExecutableTypes = value;
    }

    /**
     * Gets the value of the enabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean getEnabled() {
        if (enabled == null) {
            return true;
        } else {
            return enabled;
        }
    }

    /**
     * Sets the value of the enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

}
