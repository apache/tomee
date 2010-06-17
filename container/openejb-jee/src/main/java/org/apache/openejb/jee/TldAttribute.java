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
 * <p>Java class for tld-attributeType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="tld-attributeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="name" type="{http://java.sun.com/xml/ns/javaee}java-identifierType"/>
 *         &lt;element name="required" type="{http://java.sun.com/xml/ns/javaee}generic-booleanType" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;sequence>
 *             &lt;sequence minOccurs="0">
 *               &lt;element name="rtexprvalue" type="{http://java.sun.com/xml/ns/javaee}generic-booleanType"/>
 *               &lt;element name="type" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *             &lt;/sequence>
 *             &lt;choice>
 *               &lt;element name="deferred-value" type="{http://java.sun.com/xml/ns/javaee}tld-deferred-valueType" minOccurs="0"/>
 *               &lt;element name="deferred-method" type="{http://java.sun.com/xml/ns/javaee}tld-deferred-methodType" minOccurs="0"/>
 *             &lt;/choice>
 *           &lt;/sequence>
 *           &lt;element name="fragment" type="{http://java.sun.com/xml/ns/javaee}generic-booleanType" minOccurs="0"/>
 *         &lt;/choice>
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
@XmlType(name = "tld-attributeType", propOrder = {
    "descriptions",
    "name",
    "required",
    "rtexprvalue",
    "type",
    "deferredValue",
    "deferredMethod",
    "fragment"
})
public class TldAttribute {
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(required = true)
    protected String name;
    protected String required;
    protected String rtexprvalue;
    protected String type;
    @XmlElement(name = "deferred-value")
    protected TldDeferredValue deferredValue;
    @XmlElement(name = "deferred-method")
    protected TldDeferredMethod deferredMethod;
    protected String fragment;
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

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String value) {
        this.required = value;
    }

    public String getRtexprvalue() {
        return rtexprvalue;
    }

    public void setRtexprvalue(String value) {
        this.rtexprvalue = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public TldDeferredValue getDeferredValue() {
        return deferredValue;
    }

    public void setDeferredValue(TldDeferredValue value) {
        this.deferredValue = value;
    }

    public TldDeferredMethod getDeferredMethod() {
        return deferredMethod;
    }

    public void setDeferredMethod(TldDeferredMethod value) {
        this.deferredMethod = value;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String value) {
        this.fragment = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
