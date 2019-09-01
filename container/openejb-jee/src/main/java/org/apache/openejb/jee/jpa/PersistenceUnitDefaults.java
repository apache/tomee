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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * These defaults are applied to the persistence unit as a whole
 * unless they are overridden by local annotation or XML
 * element settings.
 *
 * schema - Used as the schema for all tables, secondary tables, join
 * tables, collection tables, sequence generators, and table
 * generators that apply to the persistence unit
 * catalog - Used as the catalog for all tables, secondary tables, join
 * tables, collection tables, sequence generators, and table
 * generators that apply to the persistence unit
 * delimited-identifiers - Used to treat database identifiers as
 * delimited identifiers.
 * access - Used as the access type for all managed classes in
 * the persistence unit
 * cascade-persist - Adds cascade-persist to the set of cascade options
 * in all entity relationships of the persistence unit
 * entity-listeners - List of default entity listeners to be invoked
 * on each entity in the persistence unit.
 *
 *
 *
 * <p>Java class for persistence-unit-defaults complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="persistence-unit-defaults"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="schema" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="catalog" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="delimited-identifiers" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/&gt;
 *         &lt;element name="access" type="{http://java.sun.com/xml/ns/persistence/orm}access-type" minOccurs="0"/&gt;
 *         &lt;element name="cascade-persist" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/&gt;
 *         &lt;element name="entity-listeners" type="{http://java.sun.com/xml/ns/persistence/orm}entity-listeners" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "persistence-unit-defaults", propOrder = {
    "description",
    "schema",
    "catalog",
    "delimitedIdentifiers",
    "access",
    "cascadePersist",
    "entityListeners"
})
public class PersistenceUnitDefaults {

    protected String description;
    protected String schema;
    protected String catalog;
    @XmlElement(name = "delimited-identifiers")
    protected EmptyType delimitedIdentifiers;
    protected AccessType access;
    @XmlElement(name = "cascade-persist")
    protected EmptyType cascadePersist;
    @XmlElement(name = "entity-listeners")
    protected EntityListeners entityListeners;

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
     * Gets the value of the schema property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the value of the schema property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSchema(final String value) {
        this.schema = value;
    }

    /**
     * Gets the value of the catalog property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Sets the value of the catalog property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCatalog(final String value) {
        this.catalog = value;
    }

    /**
     * Gets the value of the delimitedIdentifiers property.
     *
     * @return possible object is
     * {@link EmptyType }
     */
    public EmptyType getDelimitedIdentifiers() {
        return delimitedIdentifiers;
    }

    /**
     * Sets the value of the delimitedIdentifiers property.
     *
     * @param value allowed object is
     *              {@link EmptyType }
     */
    public void setDelimitedIdentifiers(final EmptyType value) {
        this.delimitedIdentifiers = value;
    }

    /**
     * Gets the value of the access property.
     *
     * @return possible object is
     * {@link AccessType }
     */
    public AccessType getAccess() {
        return access;
    }

    /**
     * Sets the value of the access property.
     *
     * @param value allowed object is
     *              {@link AccessType }
     */
    public void setAccess(final AccessType value) {
        this.access = value;
    }

    /**
     * Gets the value of the cascadePersist property.
     *
     * @return possible object is
     * {@link boolean }
     */
    public boolean isCascadePersist() {
        return cascadePersist != null;
    }

    /**
     * Sets the value of the cascadePersist property.
     *
     * @param value allowed object is
     *              {@link boolean }
     */
    public void setCascadePersist(final boolean value) {
        this.cascadePersist = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the entityListeners property.
     *
     * @return possible object is
     * {@link EntityListeners }
     */
    public EntityListeners getEntityListeners() {
        return entityListeners;
    }

    /**
     * Sets the value of the entityListeners property.
     *
     * @param value allowed object is
     *              {@link EntityListeners }
     */
    public void setEntityListeners(final EntityListeners value) {
        this.entityListeners = value;
    }

}
