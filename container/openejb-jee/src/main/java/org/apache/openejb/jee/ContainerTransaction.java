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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for container-transactionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="container-transactionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="method" type="{http://java.sun.com/xml/ns/javaee}methodType" maxOccurs="unbounded"/&gt;
 *         &lt;element name="trans-attribute" type="{http://java.sun.com/xml/ns/javaee}trans-attributeType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "container-transactionType", propOrder = {
    "descriptions",
    "method",
    "transAttribute"
})
public class ContainerTransaction implements AttributeBinding<TransAttribute> {

    @XmlElement(required = true)
    protected List<Method> method;
    @XmlElement(name = "trans-attribute", required = true)
    protected TransAttribute transAttribute;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlTransient
    protected TextMap description = new TextMap();

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public ContainerTransaction() {
    }

    public ContainerTransaction(final TransAttribute transAttribute, final String className, final String ejbName, final String methodName) {
        this(transAttribute, new Method(ejbName, className, methodName));
    }

    public ContainerTransaction(final TransAttribute transAttribute, final String ejbName, final java.lang.reflect.Method method) {
        this(transAttribute, new Method(ejbName, method));
    }

    public ContainerTransaction(final TransAttribute transAttribute, final Method method) {
        this.transAttribute = transAttribute;
        getMethod().add(method);
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public List<Method> getMethod() {
        if (method == null) {
            method = new ArrayList<Method>();
        }
        return this.method;
    }

    public TransAttribute getAttribute() {
        return transAttribute;
    }

    public TransAttribute getTransAttribute() {
        return transAttribute;
    }

    public void setTransAttribute(final TransAttribute value) {
        this.transAttribute = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }


}
