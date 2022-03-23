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
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for init-methodType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="init-methodType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="create-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType"/&gt;
 *         &lt;element name="bean-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "init-methodType", propOrder = {
    "createMethod",
    "beanMethod"
})
public class InitMethod {

    @XmlElement(name = "create-method", required = true)
    protected NamedMethod createMethod;
    @XmlElement(name = "bean-method", required = true)
    protected NamedMethod beanMethod;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public InitMethod() {
    }

    public InitMethod(final java.lang.reflect.Method beanMethod) {
        this.beanMethod = new NamedMethod(beanMethod);
    }

    public NamedMethod getCreateMethod() {
        return createMethod;
    }

    public void setCreateMethod(final NamedMethod value) {
        this.createMethod = value;
    }

    public void setCreateMethod(final String methodName) {
        this.createMethod = new NamedMethod(methodName);
    }

    public NamedMethod getBeanMethod() {
        return beanMethod;
    }

    public void setBeanMethod(final NamedMethod value) {
        this.beanMethod = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

}
