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
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * connector_1_6.xsd
 *
 * <p>Java class for connection-definitionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="connection-definitionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="managedconnectionfactory-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="config-property" type="{http://java.sun.com/xml/ns/javaee}config-propertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="connectionfactory-interface" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="connectionfactory-impl-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="connection-interface" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="connection-impl-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "connection-definitionType", propOrder = {
    "managedConnectionFactoryClass",
    "configProperty",
    "connectionFactoryInterface",
    "connectionFactoryImplClass",
    "connectionInterface",
    "connectionImplClass"
})
public class ConnectionDefinition {

    @XmlElement(name = "managedconnectionfactory-class", required = true)
    protected String managedConnectionFactoryClass;
    @XmlElement(name = "config-property")
    protected List<ConfigProperty> configProperty;
    @XmlElement(name = "connectionfactory-interface", required = true)
    protected String connectionFactoryInterface;
    @XmlElement(name = "connectionfactory-impl-class", required = true)
    protected String connectionFactoryImplClass;
    @XmlElement(name = "connection-interface", required = true)
    protected String connectionInterface;
    @XmlElement(name = "connection-impl-class", required = true)
    protected String connectionImplClass;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getManagedConnectionFactoryClass() {
        return managedConnectionFactoryClass;
    }

    public void setManagedConnectionFactoryClass(final String value) {
        this.managedConnectionFactoryClass = value;
    }

    public List<ConfigProperty> getConfigProperty() {
        if (configProperty == null) {
            configProperty = new ArrayList<ConfigProperty>();
        }
        return this.configProperty;
    }

    public String getConnectionFactoryInterface() {
        return connectionFactoryInterface;
    }

    public void setConnectionFactoryInterface(final String value) {
        this.connectionFactoryInterface = value;
    }

    public String getConnectionFactoryImplClass() {
        return connectionFactoryImplClass;
    }

    public void setConnectionFactoryImplClass(final String value) {
        this.connectionFactoryImplClass = value;
    }

    public String getConnectionInterface() {
        return connectionInterface;
    }

    public void setConnectionInterface(final String value) {
        this.connectionInterface = value;
    }

    public String getConnectionImplClass() {
        return connectionImplClass;
    }

    public void setConnectionImplClass(final String value) {
        this.connectionImplClass = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

}
