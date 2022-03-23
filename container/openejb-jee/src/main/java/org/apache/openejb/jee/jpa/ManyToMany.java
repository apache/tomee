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

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlTransient;


/**
 * Target({METHOD, FIELD}) @Retention(RUNTIME)
 * public @interface ManyToMany {
 * Class targetEntity() default void.class;
 * CascadeType[] cascade() default {};
 * FetchType fetch() default LAZY;
 * String mappedBy() default "";
 * }
 *
 *
 *
 * <p>Java class for many-to-many complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="many-to-many"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="order-by" type="{http://java.sun.com/xml/ns/persistence/orm}order-by" minOccurs="0"/&gt;
 *           &lt;element name="order-column" type="{http://java.sun.com/xml/ns/persistence/orm}order-column" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice&gt;
 *           &lt;element name="map-key" type="{http://java.sun.com/xml/ns/persistence/orm}map-key" minOccurs="0"/&gt;
 *           &lt;sequence&gt;
 *             &lt;element name="map-key-class" type="{http://java.sun.com/xml/ns/persistence/orm}map-key-class" minOccurs="0"/&gt;
 *             &lt;choice&gt;
 *               &lt;element name="map-key-temporal" type="{http://java.sun.com/xml/ns/persistence/orm}temporal" minOccurs="0"/&gt;
 *               &lt;element name="map-key-enumerated" type="{http://java.sun.com/xml/ns/persistence/orm}enumerated" minOccurs="0"/&gt;
 *               &lt;element name="map-key-attribute-override" type="{http://java.sun.com/xml/ns/persistence/orm}attribute-override" maxOccurs="unbounded" minOccurs="0"/&gt;
 *             &lt;/choice&gt;
 *             &lt;choice&gt;
 *               &lt;element name="map-key-column" type="{http://java.sun.com/xml/ns/persistence/orm}map-key-column" minOccurs="0"/&gt;
 *               &lt;element name="map-key-join-column" type="{http://java.sun.com/xml/ns/persistence/orm}map-key-join-column" maxOccurs="unbounded" minOccurs="0"/&gt;
 *             &lt;/choice&gt;
 *           &lt;/sequence&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="join-table" type="{http://java.sun.com/xml/ns/persistence/orm}join-table" minOccurs="0"/&gt;
 *         &lt;element name="cascade" type="{http://java.sun.com/xml/ns/persistence/orm}cascade-type" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="target-entity" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="fetch" type="{http://java.sun.com/xml/ns/persistence/orm}fetch-type" /&gt;
 *       &lt;attribute name="access" type="{http://java.sun.com/xml/ns/persistence/orm}access-type" /&gt;
 *       &lt;attribute name="mapped-by" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "many-to-many", propOrder = {
    "orderBy",
    "orderColumn",
    "mapKey",
    "mapKeyClass",
    "mapKeyTemporal",
    "mapKeyEnumerated",
    "mapKeyAttributeOverride",
    "mapKeyColumn",
    "mapKeyJoinColumn",
    "joinTable",
    "cascade"
})
public class ManyToMany implements RelationField {

    @XmlElement(name = "order-by")
    protected String orderBy;
    @XmlElement(name = "order-column")
    protected OrderColumn orderColumn;
    @XmlElement(name = "map-key")
    protected MapKey mapKey;
    @XmlElement(name = "map-key-class")
    protected MapKeyClass mapKeyClass;
    @XmlElement(name = "map-key-temporal")
    protected TemporalType mapKeyTemporal;
    @XmlElement(name = "map-key-enumerated")
    protected EnumType mapKeyEnumerated;
    @XmlElement(name = "map-key-attribute-override")
    protected List<AttributeOverride> mapKeyAttributeOverride;
    @XmlElement(name = "map-key-column")
    protected MapKeyColumn mapKeyColumn;
    @XmlElement(name = "map-key-join-column")
    protected List<MapKeyJoinColumn> mapKeyJoinColumn;
    @XmlElement(name = "join-table")
    protected JoinTable joinTable;
    protected CascadeType cascade;
    @XmlAttribute
    protected FetchType fetch;
    @XmlAttribute
    protected AccessType access;
    @XmlAttribute(name = "mapped-by")
    protected String mappedBy;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(name = "target-entity")
    protected String targetEntity;
    @XmlTransient
    protected RelationField relatedField;
    @XmlTransient
    protected boolean syntheticField;

    /**
     * Gets the value of the orderBy property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * Sets the value of the orderBy property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setOrderBy(final String value) {
        this.orderBy = value;
    }

    /**
     * Gets the value of the orderColumn property.
     *
     * @return possible object is
     * {@link OrderColumn }
     */
    public OrderColumn getOrderColumn() {
        return orderColumn;
    }

    /**
     * Sets the value of the orderColumn property.
     *
     * @param value allowed object is
     *              {@link OrderColumn }
     */
    public void setOrderColumn(final OrderColumn value) {
        this.orderColumn = value;
    }

    /**
     * Gets the value of the mapKey property.
     *
     * @return possible object is
     * {@link MapKey }
     */
    public MapKey getMapKey() {
        return mapKey;
    }

    /**
     * Sets the value of the mapKey property.
     *
     * @param value allowed object is
     *              {@link MapKey }
     */
    public void setMapKey(final MapKey value) {
        this.mapKey = value;
    }

    /**
     * Gets the value of the mapKeyClass property.
     *
     * @return possible object is
     * {@link MapKeyClass }
     */
    public MapKeyClass getMapKeyClass() {
        return mapKeyClass;
    }

    /**
     * Sets the value of the mapKeyClass property.
     *
     * @param value allowed object is
     *              {@link MapKeyClass }
     */
    public void setMapKeyClass(final MapKeyClass value) {
        this.mapKeyClass = value;
    }

    /**
     * Gets the value of the mapKeyTemporal property.
     *
     * @return possible object is
     * {@link TemporalType }
     */
    public TemporalType getMapKeyTemporal() {
        return mapKeyTemporal;
    }

    /**
     * Sets the value of the mapKeyTemporal property.
     *
     * @param value allowed object is
     *              {@link TemporalType }
     */
    public void setMapKeyTemporal(final TemporalType value) {
        this.mapKeyTemporal = value;
    }

    /**
     * Gets the value of the mapKeyEnumerated property.
     *
     * @return possible object is
     * {@link EnumType }
     */
    public EnumType getMapKeyEnumerated() {
        return mapKeyEnumerated;
    }

    /**
     * Sets the value of the mapKeyEnumerated property.
     *
     * @param value allowed object is
     *              {@link EnumType }
     */
    public void setMapKeyEnumerated(final EnumType value) {
        this.mapKeyEnumerated = value;
    }

    /**
     * Gets the value of the mapKeyAttributeOverride property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapKeyAttributeOverride property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapKeyAttributeOverride().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link AttributeOverride }
     */
    public List<AttributeOverride> getMapKeyAttributeOverride() {
        if (mapKeyAttributeOverride == null) {
            mapKeyAttributeOverride = new ArrayList<AttributeOverride>();
        }
        return this.mapKeyAttributeOverride;
    }

    /**
     * Gets the value of the mapKeyColumn property.
     *
     * @return possible object is
     * {@link MapKeyColumn }
     */
    public MapKeyColumn getMapKeyColumn() {
        return mapKeyColumn;
    }

    /**
     * Sets the value of the mapKeyColumn property.
     *
     * @param value allowed object is
     *              {@link MapKeyColumn }
     */
    public void setMapKeyColumn(final MapKeyColumn value) {
        this.mapKeyColumn = value;
    }

    /**
     * Gets the value of the mapKeyJoinColumn property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapKeyJoinColumn property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapKeyJoinColumn().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link MapKeyJoinColumn }
     */
    public List<MapKeyJoinColumn> getMapKeyJoinColumn() {
        if (mapKeyJoinColumn == null) {
            mapKeyJoinColumn = new ArrayList<MapKeyJoinColumn>();
        }
        return this.mapKeyJoinColumn;
    }

    /**
     * Gets the value of the joinTable property.
     *
     * @return possible object is
     * {@link JoinTable }
     */
    public JoinTable getJoinTable() {
        return joinTable;
    }

    /**
     * Sets the value of the joinTable property.
     *
     * @param value allowed object is
     *              {@link JoinTable }
     */
    public void setJoinTable(final JoinTable value) {
        this.joinTable = value;
    }

    /**
     * Gets the value of the cascade property.
     *
     * @return possible object is
     * {@link CascadeType }
     */
    public CascadeType getCascade() {
        return cascade;
    }

    /**
     * Sets the value of the cascade property.
     *
     * @param value allowed object is
     *              {@link CascadeType }
     */
    public void setCascade(final CascadeType value) {
        this.cascade = value;
    }

    /**
     * Gets the value of the fetch property.
     *
     * @return possible object is
     * {@link FetchType }
     */
    public FetchType getFetch() {
        return fetch;
    }

    /**
     * Sets the value of the fetch property.
     *
     * @param value allowed object is
     *              {@link FetchType }
     */
    public void setFetch(final FetchType value) {
        this.fetch = value;
    }

    /**
     * Gets the value of the mappedBy property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMappedBy() {
        return mappedBy;
    }

    /**
     * Sets the value of the mappedBy property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMappedBy(final String value) {
        this.mappedBy = value;
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
     * Gets the value of the targetEntity property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTargetEntity() {
        return targetEntity;
    }

    /**
     * Sets the value of the targetEntity property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTargetEntity(final String value) {
        this.targetEntity = value;
    }

    public List<JoinColumn> getJoinColumn() {
        throw new UnsupportedOperationException("Many to many element can not have join columns");
    }

    /**
     * This is only used for xml converters and will normally return null.
     * Gets the field on the target entity for this relationship.
     *
     * @return the field on the target entity for this relationship.
     */
    public RelationField getRelatedField() {
        return relatedField;
    }

    /**
     * Gets the field on the target entity for this relationship.
     *
     * @param value field on the target entity for this relationship.
     */
    public void setRelatedField(final RelationField value) {
        this.relatedField = value;
    }

    /**
     * This is only used for xml converters and will normally return false.
     * A true value indicates that this field was generated for CMR back references.
     *
     * @return true if this field was generated for CMR back references.
     */
    public boolean isSyntheticField() {
        return syntheticField;
    }

    /**
     * This is only used for xml converters and will normally return false.
     * A true value indicates that this field was generated for CMR back references.
     *
     * @return true if this field was generated for CMR back references.
     */
    public void setSyntheticField(final boolean syntheticField) {
        this.syntheticField = syntheticField;
    }

    public Object getKey() {
        return name;
    }
}
