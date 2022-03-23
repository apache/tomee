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
package org.apache.openejb.jee;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * connector_1_6.xsd
 *
 * <p>Java class for activationspecType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="activationspecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="activationspec-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="required-config-property" type="{http://java.sun.com/xml/ns/javaee}required-config-propertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="config-property" type="{http://java.sun.com/xml/ns/javaee}config-propertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "activationspecType", propOrder = {
    "activationSpecClass",
    "requiredConfigProperty",
    "configProperty"
})
public class ActivationSpec {

    @XmlElement(name = "activationspec-class", required = true)
    protected String activationSpecClass;
    @XmlElement(name = "required-config-property")
    protected List<RequiredConfigProperty> requiredConfigProperty;
    @XmlElement(name = "config-property")
    protected List<ConfigProperty> configProperty;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;

    public ActivationSpec() {
    }

    public ActivationSpec(final String activationSpecClass) {
        this.activationSpecClass = activationSpecClass;
    }

    public ActivationSpec(final Class activationSpecClass) {
        this(activationSpecClass.getName());
    }

    public String getActivationSpecClass() {
        return activationSpecClass;
    }

    public void setActivationSpecClass(final String value) {
        this.activationSpecClass = value;
    }

    public List<RequiredConfigProperty> getRequiredConfigProperty() {
        if (requiredConfigProperty == null) {
            requiredConfigProperty = new ArrayList<RequiredConfigProperty>();
        }
        return this.requiredConfigProperty;
    }

    public RequiredConfigProperty addRequiredConfigProperty(final String name) {
        final RequiredConfigProperty property = new RequiredConfigProperty(name);
        getRequiredConfigProperty().add(property);
        return property;
    }

    public List<ConfigProperty> getConfigProperty() {
        if (configProperty == null) {
            configProperty = new ArrayList<ConfigProperty>();
        }
        return configProperty;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

}
