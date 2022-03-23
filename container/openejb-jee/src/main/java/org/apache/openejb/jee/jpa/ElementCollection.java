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


/**
 * Target({METHOD, FIELD}) @Retention(RUNTIME)
 * public @interface ElementCollection {
 * Class targetClass() default void.class;
 * FetchType fetch() default LAZY;
 * }
 *
 *
 *
 * <p>Java class for element-collection complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="element-collection"&gt;
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
 *         &lt;choice&gt;
 *           &lt;sequence&gt;
 *             &lt;element name="column" type="{http://java.sun.com/xml/ns/persistence/orm}column" minOccurs="0"/&gt;
 *             &lt;choice&gt;
 *               &lt;element name="temporal" type="{http://java.sun.com/xml/ns/persistence/orm}temporal" minOccurs="0"/&gt;
 *               &lt;element name="enumerated" type="{http://java.sun.com/xml/ns/persistence/orm}enumerated" minOccurs="0"/&gt;
 *               &lt;element name="lob" type="{http://java.sun.com/xml/ns/persistence/orm}lob" minOccurs="0"/&gt;
 *             &lt;/choice&gt;
 *           &lt;/sequence&gt;
 *           &lt;sequence&gt;
 *             &lt;element name="attribute-override" type="{http://java.sun.com/xml/ns/persistence/orm}attribute-override" maxOccurs="unbounded" minOccurs="0"/&gt;
 *             &lt;element name="association-override" type="{http://java.sun.com/xml/ns/persistence/orm}association-override" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;/sequence&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="collection-table" type="{http://java.sun.com/xml/ns/persistence/orm}collection-table" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="target-class" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="fetch" type="{http://java.sun.com/xml/ns/persistence/orm}fetch-type" /&gt;
 *       &lt;attribute name="access" type="{http://java.sun.com/xml/ns/persistence/orm}access-type" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "element-collection", propOrder = {
    "orderBy",
    "orderColumn",
    "mapKey",
    "mapKeyClass",
    "mapKeyTemporal",
    "mapKeyEnumerated",
    "mapKeyAttributeOverride",
    "mapKeyColumn",
    "mapKeyJoinColumn",
    "column",
    "temporal",
    "enumerated",
    "lob",
    "attributeOverride",
    "associationOverride",
    "collectionTable"
})
public class ElementCollection {

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
    protected Column column;
    protected TemporalType temporal;
    protected EnumType enumerated;
    protected Lob lob;
    @XmlElement(name = "attribute-override")
    protected List<AttributeOverride> attributeOverride;
    @XmlElement(name = "association-override")
    protected List<AssociationOverride> associationOverride;
    @XmlElement(name = "collection-table")
    protected CollectionTable collectionTable;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(name = "target-class")
    protected String targetClass;
    @XmlAttribute
    protected FetchType fetch;
    @XmlAttribute
    protected AccessType access;

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
     * {@link org.apache.openejb.jee.jpa.MapKey }
     */
    public MapKey getMapKey() {
        return mapKey;
    }

    /**
     * Sets the value of the mapKey property.
     *
     * @param value allowed object is
     *              {@link org.apache.openejb.jee.jpa.MapKey }
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
     * {@link org.apache.openejb.jee.jpa.TemporalType }
     */
    public TemporalType getMapKeyTemporal() {
        return mapKeyTemporal;
    }

    /**
     * Sets the value of the mapKeyTemporal property.
     *
     * @param value allowed object is
     *              {@link org.apache.openejb.jee.jpa.TemporalType }
     */
    public void setMapKeyTemporal(final TemporalType value) {
        this.mapKeyTemporal = value;
    }

    /**
     * Gets the value of the mapKeyEnumerated property.
     *
     * @return possible object is
     * {@link org.apache.openejb.jee.jpa.EnumType }
     */
    public EnumType getMapKeyEnumerated() {
        return mapKeyEnumerated;
    }

    /**
     * Sets the value of the mapKeyEnumerated property.
     *
     * @param value allowed object is
     *              {@link org.apache.openejb.jee.jpa.EnumType }
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
     * {@link org.apache.openejb.jee.jpa.AttributeOverride }
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
     * Gets the value of the column property.
     *
     * @return possible object is
     * {@link org.apache.openejb.jee.jpa.Column }
     */
    public Column getColumn() {
        return column;
    }

    /**
     * Sets the value of the column property.
     *
     * @param value allowed object is
     *              {@link org.apache.openejb.jee.jpa.Column }
     */
    public void setColumn(final Column value) {
        this.column = value;
    }

    /**
     * Gets the value of the temporal property.
     *
     * @return possible object is
     * {@link org.apache.openejb.jee.jpa.TemporalType }
     */
    public TemporalType getTemporal() {
        return temporal;
    }

    /**
     * Sets the value of the temporal property.
     *
     * @param value allowed object is
     *              {@link org.apache.openejb.jee.jpa.TemporalType }
     */
    public void setTemporal(final TemporalType value) {
        this.temporal = value;
    }

    /**
     * Gets the value of the enumerated property.
     *
     * @return possible object is
     * {@link org.apache.openejb.jee.jpa.EnumType }
     */
    public EnumType getEnumerated() {
        return enumerated;
    }

    /**
     * Sets the value of the enumerated property.
     *
     * @param value allowed object is
     *              {@link org.apache.openejb.jee.jpa.EnumType }
     */
    public void setEnumerated(final EnumType value) {
        this.enumerated = value;
    }

    /**
     * Gets the value of the lob property.
     *
     * @return possible object is
     * {@link org.apache.openejb.jee.jpa.Lob }
     */
    public Lob getLob() {
        return lob;
    }

    /**
     * Sets the value of the lob property.
     *
     * @param value allowed object is
     *              {@link org.apache.openejb.jee.jpa.Lob }
     */
    public void setLob(final Lob value) {
        this.lob = value;
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
     * {@link org.apache.openejb.jee.jpa.AttributeOverride }
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
     * {@link org.apache.openejb.jee.jpa.AssociationOverride }
     */
    public List<AssociationOverride> getAssociationOverride() {
        if (associationOverride == null) {
            associationOverride = new ArrayList<AssociationOverride>();
        }
        return this.associationOverride;
    }

    /**
     * Gets the value of the collectionTable property.
     *
     * @return possible object is
     * {@link CollectionTable }
     */
    public CollectionTable getCollectionTable() {
        return collectionTable;
    }

    /**
     * Sets the value of the collectionTable property.
     *
     * @param value allowed object is
     *              {@link CollectionTable }
     */
    public void setCollectionTable(final CollectionTable value) {
        this.collectionTable = value;
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
     * Gets the value of the targetClass property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTargetClass() {
        return targetClass;
    }

    /**
     * Sets the value of the targetClass property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTargetClass(final String value) {
        this.targetClass = value;
    }

    /**
     * Gets the value of the fetch property.
     *
     * @return possible object is
     * {@link org.apache.openejb.jee.jpa.FetchType }
     */
    public FetchType getFetch() {
        return fetch;
    }

    /**
     * Sets the value of the fetch property.
     *
     * @param value allowed object is
     *              {@link org.apache.openejb.jee.jpa.FetchType }
     */
    public void setFetch(final FetchType value) {
        this.fetch = value;
    }

    /**
     * Gets the value of the access property.
     *
     * @return possible object is
     * {@link org.apache.openejb.jee.jpa.AccessType }
     */
    public AccessType getAccess() {
        return access;
    }

    /**
     * Sets the value of the access property.
     *
     * @param value allowed object is
     *              {@link org.apache.openejb.jee.jpa.AccessType }
     */
    public void setAccess(final AccessType value) {
        this.access = value;
    }

}
