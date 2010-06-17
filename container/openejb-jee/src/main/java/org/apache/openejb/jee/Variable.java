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

/**
 * web-jsptaglibrary_2_1.xsd
 *
 * <p>Java class for variableType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="variableType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="name-given" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/>
 *           &lt;element name="name-from-attribute" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/>
 *         &lt;/choice>
 *         &lt;element name="variable-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="declare" type="{http://java.sun.com/xml/ns/javaee}generic-booleanType" minOccurs="0"/>
 *         &lt;element name="scope" type="{http://java.sun.com/xml/ns/javaee}variable-scopeType" minOccurs="0"/>
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
@XmlType(name = "variableType", propOrder = {
    "descriptions",
    "nameGiven",
    "nameFromAttribute",
    "variableClass",
    "declare",
    "scope"
})
public class Variable {
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "name-given")
    protected String nameGiven;
    @XmlElement(name = "name-from-attribute")
    protected String nameFromAttribute;
    @XmlElement(name = "variable-class")
    protected String variableClass;
    protected String declare;
    protected String scope;
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

    public String getNameGiven() {
        return nameGiven;
    }

    public void setNameGiven(String value) {
        this.nameGiven = value;
    }

    public String getNameFromAttribute() {
        return nameFromAttribute;
    }

    public void setNameFromAttribute(String value) {
        this.nameFromAttribute = value;
    }

    public String getVariableClass() {
        return variableClass;
    }

    public void setVariableClass(String value) {
        this.variableClass = value;
    }

    public String getDeclare() {
        return declare;
    }

    public void setDeclare(String value) {
        this.declare = value;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String value) {
        this.scope = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
