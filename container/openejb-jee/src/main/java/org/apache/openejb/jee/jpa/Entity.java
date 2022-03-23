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

import org.apache.openejb.jee.Keyable;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlTransient;


/**
 * Defines the settings and mappings for an entity. Is allowed to be
 * sparsely populated and used in conjunction with the annotations.
 * Alternatively, the metadata-complete attribute can be used to
 * indicate that no annotations on the entity class (and its fields
 * or properties) are to be processed. If this is the case then
 * the defaulting rules for the entity and its subelements will
 * be recursively applied.
 *
 * Target(TYPE) @Retention(RUNTIME)
 * public @interface Entity {
 * String name() default "";
 * }
 *
 *
 *
 * <p>Java class for entity complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="entity"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="table" type="{http://java.sun.com/xml/ns/persistence/orm}table" minOccurs="0"/&gt;
 *         &lt;element name="secondary-table" type="{http://java.sun.com/xml/ns/persistence/orm}secondary-table" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="primary-key-join-column" type="{http://java.sun.com/xml/ns/persistence/orm}primary-key-join-column" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="id-class" type="{http://java.sun.com/xml/ns/persistence/orm}id-class" minOccurs="0"/&gt;
 *         &lt;element name="inheritance" type="{http://java.sun.com/xml/ns/persistence/orm}inheritance" minOccurs="0"/&gt;
 *         &lt;element name="discriminator-value" type="{http://java.sun.com/xml/ns/persistence/orm}discriminator-value" minOccurs="0"/&gt;
 *         &lt;element name="discriminator-column" type="{http://java.sun.com/xml/ns/persistence/orm}discriminator-column" minOccurs="0"/&gt;
 *         &lt;element name="sequence-generator" type="{http://java.sun.com/xml/ns/persistence/orm}sequence-generator" minOccurs="0"/&gt;
 *         &lt;element name="table-generator" type="{http://java.sun.com/xml/ns/persistence/orm}table-generator" minOccurs="0"/&gt;
 *         &lt;element name="named-query" type="{http://java.sun.com/xml/ns/persistence/orm}named-query" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="named-native-query" type="{http://java.sun.com/xml/ns/persistence/orm}named-native-query" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="sql-result-set-mapping" type="{http://java.sun.com/xml/ns/persistence/orm}sql-result-set-mapping" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="exclude-default-listeners" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/&gt;
 *         &lt;element name="exclude-superclass-listeners" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/&gt;
 *         &lt;element name="entity-listeners" type="{http://java.sun.com/xml/ns/persistence/orm}entity-listeners" minOccurs="0"/&gt;
 *         &lt;element name="pre-persist" type="{http://java.sun.com/xml/ns/persistence/orm}pre-persist" minOccurs="0"/&gt;
 *         &lt;element name="post-persist" type="{http://java.sun.com/xml/ns/persistence/orm}post-persist" minOccurs="0"/&gt;
 *         &lt;element name="pre-remove" type="{http://java.sun.com/xml/ns/persistence/orm}pre-remove" minOccurs="0"/&gt;
 *         &lt;element name="post-remove" type="{http://java.sun.com/xml/ns/persistence/orm}post-remove" minOccurs="0"/&gt;
 *         &lt;element name="pre-update" type="{http://java.sun.com/xml/ns/persistence/orm}pre-update" minOccurs="0"/&gt;
 *         &lt;element name="post-update" type="{http://java.sun.com/xml/ns/persistence/orm}post-update" minOccurs="0"/&gt;
 *         &lt;element name="post-load" type="{http://java.sun.com/xml/ns/persistence/orm}post-load" minOccurs="0"/&gt;
 *         &lt;element name="attribute-override" type="{http://java.sun.com/xml/ns/persistence/orm}attribute-override" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="association-override" type="{http://java.sun.com/xml/ns/persistence/orm}association-override" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="attributes" type="{http://java.sun.com/xml/ns/persistence/orm}attributes" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="class" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="access" type="{http://java.sun.com/xml/ns/persistence/orm}access-type" /&gt;
 *       &lt;attribute name="cacheable" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="metadata-complete" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity", propOrder = {
    "description",
    "table",
    "secondaryTable",
    "primaryKeyJoinColumn",
    "idClass",
    "inheritance",
    "discriminatorValue",
    "discriminatorColumn",
    "sequenceGenerator",
    "tableGenerator",
    "namedQuery",
    "namedNativeQuery",
    "sqlResultSetMapping",
    "excludeDefaultListeners",
    "excludeSuperclassListeners",
    "entityListeners",
    "prePersist",
    "postPersist",
    "preRemove",
    "postRemove",
    "preUpdate",
    "postUpdate",
    "postLoad",
    "attributeOverride",
    "associationOverride",
    "attributes"
})
public class Entity implements Mapping, Keyable<String> {

    protected String description;
    protected Table table;
    @XmlElement(name = "secondary-table")
    protected List<SecondaryTable> secondaryTable;
    @XmlElement(name = "primary-key-join-column")
    protected List<PrimaryKeyJoinColumn> primaryKeyJoinColumn;
    @XmlElement(name = "id-class")
    protected IdClass idClass;
    protected Inheritance inheritance;
    @XmlElement(name = "discriminator-value")
    protected String discriminatorValue;
    @XmlElement(name = "discriminator-column")
    protected DiscriminatorColumn discriminatorColumn;
    @XmlElement(name = "sequence-generator")
    protected SequenceGenerator sequenceGenerator;
    @XmlElement(name = "table-generator")
    protected TableGenerator tableGenerator;
    @XmlElement(name = "named-query")
    protected List<NamedQuery> namedQuery;
    @XmlElement(name = "named-native-query")
    protected List<NamedNativeQuery> namedNativeQuery;
    @XmlElement(name = "sql-result-set-mapping")
    protected List<SqlResultSetMapping> sqlResultSetMapping;
    @XmlElement(name = "exclude-default-listeners")
    protected EmptyType excludeDefaultListeners;
    @XmlElement(name = "exclude-superclass-listeners")
    protected EmptyType excludeSuperclassListeners;
    @XmlElement(name = "entity-listeners")
    protected EntityListeners entityListeners;
    @XmlElement(name = "pre-persist")
    protected PrePersist prePersist;
    @XmlElement(name = "post-persist")
    protected PostPersist postPersist;
    @XmlElement(name = "pre-remove")
    protected PreRemove preRemove;
    @XmlElement(name = "post-remove")
    protected PostRemove postRemove;
    @XmlElement(name = "pre-update")
    protected PreUpdate preUpdate;
    @XmlElement(name = "post-update")
    protected PostUpdate postUpdate;
    @XmlElement(name = "post-load")
    protected PostLoad postLoad;
    @XmlElement(name = "attribute-override")
    protected List<AttributeOverride> attributeOverride;
    @XmlElement(name = "association-override")
    protected List<AssociationOverride> associationOverride;
    protected Attributes attributes;
    @XmlAttribute
    protected AccessType access;
    @XmlAttribute(name = "class", required = true)
    protected String clazz;
    @XmlAttribute
    protected Boolean cacheable;
    @XmlAttribute(name = "metadata-complete")
    protected Boolean metadataComplete;
    @XmlAttribute
    protected String name;

    @XmlTransient
    protected String ejbName;

    @XmlTransient
    protected boolean xmlMetadataComplete;

    public Entity() {
    }

    public Entity(final String clazz) {
        this.clazz = clazz;
    }

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
     * Gets the value of the table property.
     *
     * @return possible object is
     * {@link Table }
     */
    public Table getTable() {
        return table;
    }

    /**
     * Sets the value of the table property.
     *
     * @param value allowed object is
     *              {@link Table }
     */
    public void setTable(final Table value) {
        this.table = value;
    }

    /**
     * Gets the value of the secondaryTable property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the secondaryTable property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSecondaryTable().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link SecondaryTable }
     */
    public List<SecondaryTable> getSecondaryTable() {
        if (secondaryTable == null) {
            secondaryTable = new ArrayList<SecondaryTable>();
        }
        return this.secondaryTable;
    }

    /**
     * Gets the value of the primaryKeyJoinColumn property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the primaryKeyJoinColumn property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPrimaryKeyJoinColumn().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link PrimaryKeyJoinColumn }
     */
    public List<PrimaryKeyJoinColumn> getPrimaryKeyJoinColumn() {
        if (primaryKeyJoinColumn == null) {
            primaryKeyJoinColumn = new ArrayList<PrimaryKeyJoinColumn>();
        }
        return this.primaryKeyJoinColumn;
    }

    /**
     * Gets the value of the idClass property.
     *
     * @return possible object is
     * {@link IdClass }
     */
    public IdClass getIdClass() {
        return idClass;
    }

    /**
     * Sets the value of the idClass property.
     *
     * @param value allowed object is
     *              {@link IdClass }
     */
    public void setIdClass(final IdClass value) {
        this.idClass = value;
    }

    /**
     * Gets the value of the inheritance property.
     *
     * @return possible object is
     * {@link Inheritance }
     */
    public Inheritance getInheritance() {
        return inheritance;
    }

    /**
     * Sets the value of the inheritance property.
     *
     * @param value allowed object is
     *              {@link Inheritance }
     */
    public void setInheritance(final Inheritance value) {
        this.inheritance = value;
    }

    /**
     * Gets the value of the discriminatorValue property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDiscriminatorValue() {
        return discriminatorValue;
    }

    /**
     * Sets the value of the discriminatorValue property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDiscriminatorValue(final String value) {
        this.discriminatorValue = value;
    }

    /**
     * Gets the value of the discriminatorColumn property.
     *
     * @return possible object is
     * {@link DiscriminatorColumn }
     */
    public DiscriminatorColumn getDiscriminatorColumn() {
        return discriminatorColumn;
    }

    /**
     * Sets the value of the discriminatorColumn property.
     *
     * @param value allowed object is
     *              {@link DiscriminatorColumn }
     */
    public void setDiscriminatorColumn(final DiscriminatorColumn value) {
        this.discriminatorColumn = value;
    }

    /**
     * Gets the value of the sequenceGenerator property.
     *
     * @return possible object is
     * {@link SequenceGenerator }
     */
    public SequenceGenerator getSequenceGenerator() {
        return sequenceGenerator;
    }

    /**
     * Sets the value of the sequenceGenerator property.
     *
     * @param value allowed object is
     *              {@link SequenceGenerator }
     */
    public void setSequenceGenerator(final SequenceGenerator value) {
        this.sequenceGenerator = value;
    }

    /**
     * Gets the value of the tableGenerator property.
     *
     * @return possible object is
     * {@link TableGenerator }
     */
    public TableGenerator getTableGenerator() {
        return tableGenerator;
    }

    /**
     * Sets the value of the tableGenerator property.
     *
     * @param value allowed object is
     *              {@link TableGenerator }
     */
    public void setTableGenerator(final TableGenerator value) {
        this.tableGenerator = value;
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
     * Gets the value of the excludeDefaultListeners property.
     *
     * @return possible object is
     * {@link boolean }
     */
    public boolean isExcludeDefaultListeners() {
        return excludeDefaultListeners != null;
    }

    /**
     * Sets the value of the excludeDefaultListeners property.
     *
     * @param value allowed object is
     *              {@link boolean }
     */
    public void setExcludeDefaultListeners(final boolean value) {
        this.excludeDefaultListeners = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the excludeSuperclassListeners property.
     *
     * @return possible object is
     * {@link boolean }
     */
    public boolean isExcludeSuperclassListeners() {
        return excludeSuperclassListeners != null;
    }

    /**
     * Sets the value of the excludeSuperclassListeners property.
     *
     * @param value allowed object is
     *              {@link boolean }
     */
    public void setExcludeSuperclassListeners(final boolean value) {
        this.excludeSuperclassListeners = value ? new EmptyType() : null;
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

    /**
     * Gets the value of the prePersist property.
     *
     * @return possible object is
     * {@link PrePersist }
     */
    public PrePersist getPrePersist() {
        return prePersist;
    }

    /**
     * Sets the value of the prePersist property.
     *
     * @param value allowed object is
     *              {@link PrePersist }
     */
    public void setPrePersist(final PrePersist value) {
        this.prePersist = value;
    }

    /**
     * Gets the value of the postPersist property.
     *
     * @return possible object is
     * {@link PostPersist }
     */
    public PostPersist getPostPersist() {
        return postPersist;
    }

    /**
     * Sets the value of the postPersist property.
     *
     * @param value allowed object is
     *              {@link PostPersist }
     */
    public void setPostPersist(final PostPersist value) {
        this.postPersist = value;
    }

    /**
     * Gets the value of the preRemove property.
     *
     * @return possible object is
     * {@link PreRemove }
     */
    public PreRemove getPreRemove() {
        return preRemove;
    }

    /**
     * Sets the value of the preRemove property.
     *
     * @param value allowed object is
     *              {@link PreRemove }
     */
    public void setPreRemove(final PreRemove value) {
        this.preRemove = value;
    }

    /**
     * Gets the value of the postRemove property.
     *
     * @return possible object is
     * {@link PostRemove }
     */
    public PostRemove getPostRemove() {
        return postRemove;
    }

    /**
     * Sets the value of the postRemove property.
     *
     * @param value allowed object is
     *              {@link PostRemove }
     */
    public void setPostRemove(final PostRemove value) {
        this.postRemove = value;
    }

    /**
     * Gets the value of the preUpdate property.
     *
     * @return possible object is
     * {@link PreUpdate }
     */
    public PreUpdate getPreUpdate() {
        return preUpdate;
    }

    /**
     * Sets the value of the preUpdate property.
     *
     * @param value allowed object is
     *              {@link PreUpdate }
     */
    public void setPreUpdate(final PreUpdate value) {
        this.preUpdate = value;
    }

    /**
     * Gets the value of the postUpdate property.
     *
     * @return possible object is
     * {@link PostUpdate }
     */
    public PostUpdate getPostUpdate() {
        return postUpdate;
    }

    /**
     * Sets the value of the postUpdate property.
     *
     * @param value allowed object is
     *              {@link PostUpdate }
     */
    public void setPostUpdate(final PostUpdate value) {
        this.postUpdate = value;
    }

    /**
     * Gets the value of the postLoad property.
     *
     * @return possible object is
     * {@link PostLoad }
     */
    public PostLoad getPostLoad() {
        return postLoad;
    }

    /**
     * Sets the value of the postLoad property.
     *
     * @param value allowed object is
     *              {@link PostLoad }
     */
    public void setPostLoad(final PostLoad value) {
        this.postLoad = value;
    }

    /**
     * Gets the value of the attributeOverride property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attributeOverride property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttributeOverride().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link AttributeOverride }
     */
    public List<AttributeOverride> getAttributeOverride() {
        if (attributeOverride == null) {
            attributeOverride = new ArrayList<AttributeOverride>();
        }
        return this.attributeOverride;
    }

    /**
     * Gets the value of the associationOverride property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the associationOverride property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAssociationOverride().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link AssociationOverride }
     */
    public List<AssociationOverride> getAssociationOverride() {
        if (associationOverride == null) {
            associationOverride = new ArrayList<AssociationOverride>();
        }
        return this.associationOverride;
    }

    /**
     * Gets the value of the attributes property.
     *
     * @return possible object is
     * {@link Attributes }
     */
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     *
     * @param value allowed object is
     *              {@link Attributes }
     */
    public void setAttributes(final Attributes value) {
        this.attributes = value;
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
     * Gets the value of the clazz property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClazz(final String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the cacheable property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isCacheable() {
        return cacheable;
    }

    /**
     * Sets the value of the cacheable property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setCacheable(final Boolean value) {
        this.cacheable = value;
    }

    /**
     * Gets the value of the metadataComplete property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isMetadataComplete() {
        return metadataComplete;
    }

    /**
     * Sets the value of the metadataComplete property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setMetadataComplete(final Boolean value) {
        this.metadataComplete = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(final String ejbName) {
        this.ejbName = ejbName;
    }

    public boolean isXmlMetadataComplete() {
        return xmlMetadataComplete;
    }

    public void setXmlMetadataComplete(final boolean xmlMetadataComplete) {
        this.xmlMetadataComplete = xmlMetadataComplete;
    }

    public void addField(final Field field) {
        if (field == null) throw new NullPointerException("field is null");
        if (field instanceof Id) {
            if (attributes == null) attributes = new Attributes();
            attributes.getId().add((Id) field);
        } else if (field instanceof Basic) {
            if (attributes == null) attributes = new Attributes();
            attributes.getBasic().add((Basic) field);
        } else if (field instanceof Transient) {
            if (attributes == null) attributes = new Attributes();
            attributes.getTransient().add((Transient) field);
        } else if (field instanceof AttributeOverride) {
            getAttributeOverride().add((AttributeOverride) field);
        } else {
            throw new IllegalArgumentException("Unknown field type " + field.getClass());
        }
    }

    public String getKey() {
        return this.clazz;
    }
}
