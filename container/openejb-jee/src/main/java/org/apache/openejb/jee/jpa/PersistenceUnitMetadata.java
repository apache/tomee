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

package org.apache.openejb.jee.jpa;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Metadata that applies to the persistence unit and not just to
 * the mapping file in which it is contained.
 *
 * If the xml-mapping-metadata-complete element is specified,
 * the complete set of mapping metadata for the persistence unit
 * is contained in the XML mapping files for the persistence unit.
 *
 *
 *
 * <p>Java class for persistence-unit-metadata complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="persistence-unit-metadata"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="xml-mapping-metadata-complete" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/&gt;
 *         &lt;element name="persistence-unit-defaults" type="{http://java.sun.com/xml/ns/persistence/orm}persistence-unit-defaults" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "persistence-unit-metadata", propOrder = {
    "description",
    "xmlMappingMetadataComplete",
    "persistenceUnitDefaults"
})
public class PersistenceUnitMetadata {

    protected String description;
    @XmlElement(name = "xml-mapping-metadata-complete")
    protected EmptyType xmlMappingMetadataComplete;
    @XmlElement(name = "persistence-unit-defaults")
    protected PersistenceUnitDefaults persistenceUnitDefaults;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDescription(final String value) {
        this.description = value;
    }

    /**
     * Gets the value of the xmlMappingMetadataComplete property.
     *
     * @return possible object is
     * {@link boolean }
     */
    public boolean isXmlMappingMetadataComplete() {
        return xmlMappingMetadataComplete != null;
    }

    /**
     * Sets the value of the xmlMappingMetadataComplete property.
     *
     * @param value allowed object is
     *              {@link boolean }
     */
    public void setXmlMappingMetadataComplete(final boolean value) {
        this.xmlMappingMetadataComplete = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the persistenceUnitDefaults property.
     *
     * @return possible object is
     * {@link PersistenceUnitDefaults }
     */
    public PersistenceUnitDefaults getPersistenceUnitDefaults() {
        return persistenceUnitDefaults;
    }

    /**
     * Sets the value of the persistenceUnitDefaults property.
     *
     * @param value allowed object is
     *              {@link PersistenceUnitDefaults }
     */
    public void setPersistenceUnitDefaults(final PersistenceUnitDefaults value) {
        this.persistenceUnitDefaults = value;
    }

}
