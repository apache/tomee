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

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for persistence-context-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="persistence-context-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://geronimo.apache.org/xml/ns/naming-1.2}abstract-naming-entryType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="persistence-context-ref-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="persistence-unit-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *           &lt;element name="pattern" type="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="persistence-context-type" type="{http://geronimo.apache.org/xml/ns/naming-1.2}persistence-context-typeType" minOccurs="0"/&gt;
 *         &lt;element name="property" type="{http://geronimo.apache.org/xml/ns/naming-1.2}propertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
    extends AbstractNamingEntryType {

    @XmlElement(name = "persistence-context-ref-name", required = true)
    protected String persistenceContextRefName;
    @XmlElement(name = "persistence-unit-name")
    protected String persistenceUnitName;
    protected PatternType pattern;
    @XmlElement(name = "persistence-context-type")
    protected PersistenceContextTypeType persistenceContextType;
    protected List<PropertyType> property;

    /**
     * Gets the value of the persistenceContextRefName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPersistenceContextRefName() {
        return persistenceContextRefName;
    }

    /**
     * Sets the value of the persistenceContextRefName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPersistenceContextRefName(final String value) {
        this.persistenceContextRefName = value;
    }

    /**
     * Gets the value of the persistenceUnitName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    /**
     * Sets the value of the persistenceUnitName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPersistenceUnitName(final String value) {
        this.persistenceUnitName = value;
    }

    /**
     * Gets the value of the pattern property.
     *
     * @return possible object is
     * {@link PatternType }
     */
    public PatternType getPattern() {
        return pattern;
    }

    /**
     * Sets the value of the pattern property.
     *
     * @param value allowed object is
     *              {@link PatternType }
     */
    public void setPattern(final PatternType value) {
        this.pattern = value;
    }

    /**
     * Gets the value of the persistenceContextType property.
     *
     * @return possible object is
     * {@link PersistenceContextTypeType }
     */
    public PersistenceContextTypeType getPersistenceContextType() {
        return persistenceContextType;
    }

    /**
     * Sets the value of the persistenceContextType property.
     *
     * @param value allowed object is
     *              {@link PersistenceContextTypeType }
     */
    public void setPersistenceContextType(final PersistenceContextTypeType value) {
        this.persistenceContextType = value;
    }

    /**
     * Gets the value of the property property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyType }
     */
    public List<PropertyType> getProperty() {
        if (property == null) {
            property = new ArrayList<PropertyType>();
        }
        return this.property;
    }

}
