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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for persistence-context-refType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="persistence-context-refType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://geronimo.apache.org/xml/ns/naming-1.2}abstract-naming-entryType">
 *       &lt;sequence>
 *         &lt;element name="persistence-context-ref-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice>
 *           &lt;element name="persistence-unit-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="pattern" type="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType"/>
 *         &lt;/choice>
 *         &lt;element name="persistence-context-type" type="{http://geronimo.apache.org/xml/ns/naming-1.2}persistence-context-typeType" minOccurs="0"/>
 *         &lt;element name="property" type="{http://geronimo.apache.org/xml/ns/naming-1.2}propertyType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "persistence-context-refType", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", propOrder = {
    "persistenceContextRefName",
    "persistenceUnitName",
    "pattern",
    "persistenceContextType",
    "property"
})
public class PersistenceContextRefType
    extends AbstractNamingEntryType
{

    @XmlElement(name = "persistence-context-ref-name", required = true)
    protected java.lang.String persistenceContextRefName;
    @XmlElement(name = "persistence-unit-name")
    protected java.lang.String persistenceUnitName;
    protected PatternType pattern;
    @XmlElement(name = "persistence-context-type")
    protected PersistenceContextTypeType persistenceContextType;
    protected List<PropertyType> property;

    /**
     * Gets the value of the persistenceContextRefName property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getPersistenceContextRefName() {
        return persistenceContextRefName;
    }

    /**
     * Sets the value of the persistenceContextRefName property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setPersistenceContextRefName(java.lang.String value) {
        this.persistenceContextRefName = value;
    }

    /**
     * Gets the value of the persistenceUnitName property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    /**
     * Sets the value of the persistenceUnitName property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setPersistenceUnitName(java.lang.String value) {
        this.persistenceUnitName = value;
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
     * Gets the value of the persistenceContextType property.
     * 
     * @return
     *     possible object is
     *     {@link PersistenceContextTypeType }
     *     
     */
    public PersistenceContextTypeType getPersistenceContextType() {
        return persistenceContextType;
    }

    /**
     * Sets the value of the persistenceContextType property.
     * 
     * @param value
     *     allowed object is
     *     {@link PersistenceContextTypeType }
     *     
     */
    public void setPersistenceContextType(PersistenceContextTypeType value) {
        this.persistenceContextType = value;
    }

    /**
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyType }
     * 
     * 
     */
    public List<PropertyType> getProperty() {
        if (property == null) {
            property = new ArrayList<PropertyType>();
        }
        return this.property;
    }

}
