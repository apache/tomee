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
 * <p>Java class for application-exceptionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="application-exceptionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="exception-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="rollback" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="inherited" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "application-exceptionType", propOrder = {
    "exceptionClass",
    "rollback",
    "inherited"
})
public class ApplicationException implements Keyable<String> {

    @XmlElement(name = "exception-class", required = true)
    protected String exceptionClass;
    protected Boolean rollback;
    protected Boolean inherited;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public ApplicationException() {
    }

    public ApplicationException(final String exceptionClass, final boolean rollback) {
        this.exceptionClass = exceptionClass;
        this.rollback = rollback;
    }

    public ApplicationException(final Class exceptionClass, final boolean rollback) {
        this(exceptionClass.getName(), rollback);
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(final String value) {
        this.exceptionClass = value;
    }

    public Boolean getRollback() {
        return rollback;
    }

    public boolean isRollback() {
        return rollback == null ? false : rollback;
    }

    public void setRollback(final Boolean value) {
        this.rollback = value;
    }

    public Boolean getInherited() {
        return inherited;
    }

    public boolean isInherited() {
        return inherited == null ? true : inherited;
    }

    public void setInherited(final Boolean inherited) {
        this.inherited = inherited;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

    public String getKey() {
        return exceptionClass;
    }
}
