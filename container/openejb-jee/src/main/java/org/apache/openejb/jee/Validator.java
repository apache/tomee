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
 * web-jsptaglibrary_2_1.xsd
 *
 * <p>Java class for validatorType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="validatorType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="validator-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="init-param" type="{http://java.sun.com/xml/ns/javaee}param-valueType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "validatorType", propOrder = {
    "descriptions",
    "validatorClass",
    "initParam"
})
public class Validator {
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "validator-class", required = true)
    protected String validatorClass;
    @XmlElement(name = "init-param")
    protected List<ParamValue> initParam;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public String getValidatorClass() {
        return validatorClass;
    }

    public void setValidatorClass(final String value) {
        this.validatorClass = value;
    }

    public List<ParamValue> getInitParam() {
        if (initParam == null) {
            initParam = new ArrayList<ParamValue>();
        }
        return this.initParam;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }
}
