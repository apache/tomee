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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * connector_1_6.xsd
 *
 * <p>Java class for config-propertyType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="config-propertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="config-property-name" type="{http://java.sun.com/xml/ns/javaee}config-property-nameType"/>
 *         &lt;element name="config-property-type" type="{http://java.sun.com/xml/ns/javaee}config-property-typeType"/>
 *         &lt;element name="config-property-value" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="config-property-ignore" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *         &lt;element name="config-property-supports-dynamic-updates" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *         &lt;element name="config-property-confidential" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
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
@XmlType(name = "config-propertyType", propOrder = {
        "descriptions",
        "configPropertyName",
        "configPropertyType",
        "configPropertyValue",
        "configPropertyIgnore",
        "configPropertySupportsDynamicUpdates",
        "configPropertyConfidential"
})
public class ConfigProperty {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "config-property-name", required = true)
    protected String configPropertyName;
    @XmlElement(name = "config-property-type", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String configPropertyType;
    @XmlElement(name = "config-property-value")
    protected String configPropertyValue;
    @XmlElement(name = "config-property-ignore")
    protected Boolean configPropertyIgnore;
    @XmlElement(name = "config-property-supports-dynamic-updates")
    protected Boolean configPropertySupportsDynamicUpdates;
    @XmlElement(name = "config-property-confidential")
    protected Boolean configPropertyConfidential;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public String getConfigPropertyName() {
        return configPropertyName;
    }

    public void setConfigPropertyName(String value) {
        this.configPropertyName = value;
    }

    public String getConfigPropertyType() {
        return configPropertyType;
    }

    public void setConfigPropertyType(String value) {
        this.configPropertyType = value;
    }

    public String getConfigPropertyValue() {
        return configPropertyValue;
    }

    public void setConfigPropertyValue(String value) {
        this.configPropertyValue = value;
    }

    public Boolean isConfigPropertyConfidential() {
        return configPropertyConfidential;
    }

    public void setConfigPropertyConfidential(Boolean configPropertyConfidential) {
        this.configPropertyConfidential = configPropertyConfidential;
    }

    public Boolean isConfigPropertyIgnore() {
        return configPropertyIgnore;
    }

    public void setConfigPropertyIgnore(Boolean configPropertyIgnore) {
        this.configPropertyIgnore = configPropertyIgnore;
    }

    public Boolean isConfigPropertySupportsDynamicUpdates() {
        return configPropertySupportsDynamicUpdates;
    }

    public void setConfigPropertySupportsDynamicUpdates(Boolean configPropertySupportsDynamicUpdates) {
        this.configPropertySupportsDynamicUpdates = configPropertySupportsDynamicUpdates;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
