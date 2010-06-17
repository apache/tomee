/**
 *
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

package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * ejb-jar_3_1.xsd
 * 
 * <p>Java class for activation-config-propertyType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="activation-config-propertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="activation-config-property-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType"/>
 *         &lt;element name="activation-config-property-value" type="{http://java.sun.com/xml/ns/javaee}xsdStringType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "activation-config-propertyType", propOrder = {
        "activationConfigPropertyName",
        "activationConfigPropertyValue"
        })
public class ActivationConfigProperty {

    @XmlElement(name = "activation-config-property-name", required = true)
    protected String activationConfigPropertyName;
    @XmlElement(name = "activation-config-property-value", required = true)
    protected String activationConfigPropertyValue;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    public ActivationConfigProperty() {
    }

    public ActivationConfigProperty(String activationConfigPropertyName, String activationConfigPropertyValue) {
        this.activationConfigPropertyName = activationConfigPropertyName;
        this.activationConfigPropertyValue = activationConfigPropertyValue;
    }

    public String getActivationConfigPropertyName() {
        return activationConfigPropertyName;
    }

    public void setActivationConfigPropertyName(String value) {
        this.activationConfigPropertyName = value;
    }

    public String getActivationConfigPropertyValue() {
        return activationConfigPropertyValue;
    }

    public void setActivationConfigPropertyValue(String value) {
        this.activationConfigPropertyValue = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
