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

/**
 * application_6.xsd
 *
 * <p>Java class for moduleType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="moduleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="connector" type="{http://java.sun.com/xml/ns/javaee}pathType"/&gt;
 *           &lt;element name="ejb" type="{http://java.sun.com/xml/ns/javaee}pathType"/&gt;
 *           &lt;element name="java" type="{http://java.sun.com/xml/ns/javaee}pathType"/&gt;
 *           &lt;element name="web" type="{http://java.sun.com/xml/ns/javaee}webType"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="alt-dd" type="{http://java.sun.com/xml/ns/javaee}pathType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "moduleType", propOrder = {
    "connector",
    "ejb",
    "java",
    "web",
    "altDd"
})
public class Module {

    protected String connector;
    protected String ejb;
    protected String java;
    protected Web web;
    @XmlElement(name = "alt-dd")
    protected String altDd;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getAltDd() {
        return altDd;
    }

    public void setAltDd(final String altDd) {
        this.altDd = altDd;
    }

    public String getConnector() {
        return connector;
    }

    public void setConnector(final String connector) {
        this.connector = connector;
    }

    public String getEjb() {
        return ejb;
    }

    public void setEjb(final String ejb) {
        this.ejb = ejb;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getJava() {
        return java;
    }

    public void setJava(final String java) {
        this.java = java;
    }

    public Web getWeb() {
        return web;
    }

    public void setWeb(final Web web) {
        this.web = web;
    }

}
