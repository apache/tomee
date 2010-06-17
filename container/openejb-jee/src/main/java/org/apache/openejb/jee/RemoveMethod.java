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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * ejb-jar_3_1.xsd
 * 
 * <p>Java class for remove-methodType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="remove-methodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bean-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType"/>
 *         &lt;element name="retain-if-exception" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
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
@XmlType(name = "remove-methodType", propOrder = {
        "beanMethod",
        "retainIfException"
        })
public class RemoveMethod {

    @XmlElement(name = "bean-method", required = true)
    protected NamedMethod beanMethod;
    @XmlElement(name = "retain-if-exception", required = true)
    protected Boolean retainIfException;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public RemoveMethod() {
    }

    public RemoveMethod(java.lang.reflect.Method beanMethod) {
        this(beanMethod, false);
    }

    public RemoveMethod(java.lang.reflect.Method beanMethod, boolean retainIfException) {
        this.beanMethod = new NamedMethod(beanMethod);
        this.retainIfException = retainIfException;
    }

    public NamedMethod getBeanMethod() {
        return beanMethod;
    }

    public void setBeanMethod(NamedMethod value) {
        this.beanMethod = value;
    }

    public boolean isExplicitlySet() {
        return retainIfException != null;
    }

    public boolean getRetainIfException() {
        return retainIfException != null && retainIfException;
    }

    public void setRetainIfException(boolean value) {
        this.retainIfException = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RemoveMethod that = (RemoveMethod) o;

        if (beanMethod != null ? !beanMethod.equals(that.beanMethod) : that.beanMethod != null) return false;

        return true;
    }

    public int hashCode() {
        return (beanMethod != null ? beanMethod.hashCode() : 0);
    }
}
