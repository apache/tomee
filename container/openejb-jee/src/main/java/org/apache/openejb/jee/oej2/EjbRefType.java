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

package org.apache.openejb.jee.oej2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ejb-refType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ejb-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ref-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;element name="pattern" type="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType"/>
 *           &lt;group ref="{http://geronimo.apache.org/xml/ns/naming-1.2}corbaNameGroup"/>
 *           &lt;element name="ejb-link" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "ejb-refType", propOrder = {
    "refName",
    "pattern",
    "nsCorbaloc",
    "name",
    "css",
    "cssLink",
    "ejbLink"
})
public class EjbRefType {

    @XmlElement(name = "ref-name", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", required = true)
    protected java.lang.String refName;
    @XmlElement(name = "pattern", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected PatternType pattern;
    @XmlElement(name = "ns-corbaloc", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected java.lang.String nsCorbaloc;
    @XmlElement(name = "name", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected java.lang.String name;
    @XmlElement(name = "css", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected PatternType css;
    @XmlElement(name = "css-link", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected java.lang.String cssLink;
    @XmlElement(name = "ejb-link", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2")
    protected java.lang.String ejbLink;

    /**
     * Gets the value of the refName property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getRefName() {
        return refName;
    }

    /**
     * Sets the value of the refName property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setRefName(java.lang.String value) {
        this.refName = value;
    }

    /**
     * Gets the value of the pattern property.
     * 
     * @return
     *     possible object is
     *     {@link PatternType }
     *     
     */
    public PatternType getPattern() {
        return pattern;
    }

    /**
     * Sets the value of the pattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link PatternType }
     *     
     */
    public void setPattern(PatternType value) {
        this.pattern = value;
    }

    /**
     * Gets the value of the nsCorbaloc property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getNsCorbaloc() {
        return nsCorbaloc;
    }

    /**
     * Sets the value of the nsCorbaloc property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setNsCorbaloc(java.lang.String value) {
        this.nsCorbaloc = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setName(java.lang.String value) {
        this.name = value;
    }

    /**
     * Gets the value of the css property.
     * 
     * @return
     *     possible object is
     *     {@link PatternType }
     *     
     */
    public PatternType getCss() {
        return css;
    }

    /**
     * Sets the value of the css property.
     * 
     * @param value
     *     allowed object is
     *     {@link PatternType }
     *     
     */
    public void setCss(PatternType value) {
        this.css = value;
    }

    /**
     * Gets the value of the cssLink property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getCssLink() {
        return cssLink;
    }

    /**
     * Sets the value of the cssLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setCssLink(java.lang.String value) {
        this.cssLink = value;
    }

    /**
     * Gets the value of the ejbLink property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getEjbLink() {
        return ejbLink;
    }

    /**
     * Sets the value of the ejbLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setEjbLink(java.lang.String value) {
        this.ejbLink = value;
    }

}
