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

import org.apache.openejb.jee.KeyedCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * The entity-mappings element is the root element of a mapping
 * file. It contains the following four types of elements:
 *
 * 1. The persistence-unit-metadata element contains metadata
 * for the entire persistence unit. It is undefined if this element
 * occurs in multiple mapping files within the same persistence unit.
 *
 * 2. The package, schema, catalog and access elements apply to all of
 * the entity, mapped-superclass and embeddable elements defined in
 * the same file in which they occur.
 *
 * 3. The sequence-generator, table-generator, named-query,
 * named-native-query and sql-result-set-mapping elements are global
 * to the persistence unit. It is undefined to have more than one
 * sequence-generator or table-generator of the same name in the same
 * or different mapping files in a persistence unit. It is also
 * undefined to have more than one named-query, named-native-query, or
 * result-set-mapping of the same name in the same or different mapping
 * files in a persistence unit.
 *
 * 4. The entity, mapped-superclass and embeddable elements each define
 * the mapping information for a managed persistent class. The mapping
 * information contained in these elements may be complete or it may
 * be partial.
 *
 *
 *
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="persistence-unit-metadata" type="{http://java.sun.com/xml/ns/persistence/orm}persistence-unit-metadata" minOccurs="0"/&gt;
 *         &lt;element name="package" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="schema" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="catalog" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="access" type="{http://java.sun.com/xml/ns/persistence/orm}access-type" minOccurs="0"/&gt;
 *         &lt;element name="sequence-generator" type="{http://java.sun.com/xml/ns/persistence/orm}sequence-generator" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="table-generator" type="{http://java.sun.com/xml/ns/persistence/orm}table-generator" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="named-query" type="{http://java.sun.com/xml/ns/persistence/orm}named-query" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="named-native-query" type="{http://java.sun.com/xml/ns/persistence/orm}named-native-query" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="sql-result-set-mapping" type="{http://java.sun.com/xml/ns/persistence/orm}sql-result-set-mapping" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="mapped-superclass" type="{http://java.sun.com/xml/ns/persistence/orm}mapped-superclass" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="entity" type="{http://java.sun.com/xml/ns/persistence/orm}entity" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="embeddable" type="{http://java.sun.com/xml/ns/persistence/orm}embeddable" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="version" use="required" type="{http://java.sun.com/xml/ns/persistence/orm}versionType" fixed="2.0" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "persistenceUnitMetadata",
    "_package",
    "schema",
    "catalog",
    "access",
    "sequenceGenerator",
    "tableGenerator",
    "namedQuery",
    "namedNativeQuery",
    "sqlResultSetMapping",
    "mappedSuperclass",
    "entity",
    "embeddable"
})
@XmlRootElement(name = "entity-mappings")
public class EntityMappings {

    protected String description;
    @XmlElement(name = "persistence-unit-metadata")
    protected PersistenceUnitMetadata persistenceUnitMetadata;
    @XmlElement(name = "package")
    protected String _package;
    protected String schema;
    protected String catalog;
    protected AccessType access;
    @XmlElement(name = "sequence-generator")
    protected List<SequenceGenerator> sequenceGenerator;
    @XmlElement(name = "table-generator")
    protected List<TableGenerator> tableGenerator;
    @XmlElement(name = "named-query")
    protected List<NamedQuery> namedQuery;
    @XmlElement(name = "named-native-query")
    protected List<NamedNativeQuery> namedNativeQuery;
    @XmlElement(name = "sql-result-set-mapping")
    protected List<SqlResultSetMapping> sqlResultSetMapping;
    @XmlElement(name = "mapped-superclass")
    protected KeyedCollection<String, MappedSuperclass> mappedSuperclass;
    protected KeyedCollection<String, Entity> entity;
    protected List<Embeddable> embeddable;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String version = "2.0";

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
     * Gets the value of the persistenceUnitMetadata property.
     *
     * @return possible object is
     * {@link PersistenceUnitMetadata }
     */
    public PersistenceUnitMetadata getPersistenceUnitMetadata() {
        return persistenceUnitMetadata;
    }

    /**
     * Sets the value of the persistenceUnitMetadata property.
     *
     * @param value allowed object is
     *              {@link PersistenceUnitMetadata }
     */
    public void setPersistenceUnitMetadata(final PersistenceUnitMetadata value) {
        this.persistenceUnitMetadata = value;
    }

    /**
     * Gets the value of the package property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPackage() {
        return _package;
    }

    /**
     * Sets the value of the package property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPackage(final String value) {
        this._package = value;
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
     * Gets the value of the sequenceGenerator property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sequenceGenerator property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSequenceGenerator().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link SequenceGenerator }
     */
    public List<SequenceGenerator> getSequenceGenerator() {
        if (sequenceGenerator == null) {
            sequenceGenerator = new ArrayList<SequenceGenerator>();
        }
        return this.sequenceGenerator;
    }

    /**
     * Gets the value of the tableGenerator property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tableGenerator property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTableGenerator().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link TableGenerator }
     */
    public List<TableGenerator> getTableGenerator() {
        if (tableGenerator == null) {
            tableGenerator = new ArrayList<TableGenerator>();
        }
        return this.tableGenerator;
    }

    /**
     * Gets the value of the namedQuery property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the namedQuery property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNamedQuery().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link NamedQuery }
     */
    public List<NamedQuery> getNamedQuery() {
        if (namedQuery == null) {
            namedQuery = new ArrayList<NamedQuery>();
        }
        return this.namedQuery;
    }

    /**
     * Gets the value of the namedNativeQuery property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the namedNativeQuery property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNamedNativeQuery().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link NamedNativeQuery }
     */
    public List<NamedNativeQuery> getNamedNativeQuery() {
        if (namedNativeQuery == null) {
            namedNativeQuery = new ArrayList<NamedNativeQuery>();
        }
        return this.namedNativeQuery;
    }

    /**
     * Gets the value of the sqlResultSetMapping property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sqlResultSetMapping property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSqlResultSetMapping().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link SqlResultSetMapping }
     */
    public List<SqlResultSetMapping> getSqlResultSetMapping() {
        if (sqlResultSetMapping == null) {
            sqlResultSetMapping = new ArrayList<SqlResultSetMapping>();
        }
        return this.sqlResultSetMapping;
    }

    /**
     * Gets the value of the mappedSuperclass property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mappedSuperclass property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMappedSuperclass().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link MappedSuperclass }
     */
    public Collection<MappedSuperclass> getMappedSuperclass() {
        if (mappedSuperclass == null) {
            mappedSuperclass = new KeyedCollection<String, MappedSuperclass>();
        }
        return this.mappedSuperclass;
    }

    public Map<String, MappedSuperclass> getMappedSuperclassMap() {
        return ((KeyedCollection) getMappedSuperclass()).toMap();
    }

    /**
     * Gets the value of the entity property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the entity property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEntity().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Entity }
     */
    public Collection<Entity> getEntity() {
        if (entity == null) {
            entity = new KeyedCollection<String, Entity>();
        }
        return this.entity;
    }

    public Map<String, Entity> getEntityMap() {
        return ((KeyedCollection) getEntity()).toMap();
    }

    /**
     * Gets the value of the embeddable property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the embeddable property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEmbeddable().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Embeddable }
     */
    public List<Embeddable> getEmbeddable() {
        if (embeddable == null) {
            embeddable = new ArrayList<Embeddable>();
        }
        return this.embeddable;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVersion() {
        if (version == null) {
            return "2.0";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVersion(final String value) {
        this.version = value;
    }

}
