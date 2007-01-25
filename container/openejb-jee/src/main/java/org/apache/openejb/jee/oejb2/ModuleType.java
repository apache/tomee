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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee.oejb2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * 
 * 	Mirrors the moduleType defined by application_1_4.xsd and adds an
 * 	optional alt-dd element defining a Geronimo specific deployment descriptor.
 *             
 * 
 * <p>Java class for moduleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="moduleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="connector" type="{http://geronimo.apache.org/xml/ns/j2ee/application-1.2}pathType"/>
 *           &lt;element name="ejb" type="{http://geronimo.apache.org/xml/ns/j2ee/application-1.2}pathType"/>
 *           &lt;element name="java" type="{http://geronimo.apache.org/xml/ns/j2ee/application-1.2}pathType"/>
 *           &lt;element name="web" type="{http://geronimo.apache.org/xml/ns/j2ee/application-1.2}pathType"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="alt-dd" type="{http://geronimo.apache.org/xml/ns/j2ee/application-1.2}pathType"/>
 *           &lt;any/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "moduleType", namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2", propOrder = {
    "connector",
    "ejb",
    "java",
    "web",
    "altDd",
    "any"
})
public class ModuleType {

    @XmlElement(name="connector", namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2")
    protected String connector;
    @XmlElement(name="ejb", namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2")
    protected String ejb;
    @XmlElement(name="java", namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2")
    protected String java;
    @XmlElement(name="web", namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2")
    protected String web;
    @XmlElement(name = "alt-dd", namespace = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2")
    protected String altDd;
    @XmlAnyElement(lax = true)
    protected Object any;

    /**
     * Gets the value of the connector property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnector() {
        return connector;
    }

    /**
     * Sets the value of the connector property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnector(String value) {
        this.connector = value;
    }

    /**
     * Gets the value of the ejb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEjb() {
        return ejb;
    }

    /**
     * Sets the value of the ejb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEjb(String value) {
        this.ejb = value;
    }

    /**
     * Gets the value of the java property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJava() {
        return java;
    }

    /**
     * Sets the value of the java property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJava(String value) {
        this.java = value;
    }

    /**
     * Gets the value of the web property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWeb() {
        return web;
    }

    /**
     * Sets the value of the web property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWeb(String value) {
        this.web = value;
    }

    /**
     * Gets the value of the altDd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAltDd() {
        return altDd;
    }

    /**
     * Sets the value of the altDd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAltDd(String value) {
        this.altDd = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     {@link Element }
     *     
     */
    public Object getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     {@link Element }
     *     
     */
    public void setAny(Object value) {
        this.any = value;
    }

}
