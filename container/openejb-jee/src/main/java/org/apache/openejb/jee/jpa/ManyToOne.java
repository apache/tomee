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
 * public @interface ManyToOne {
 * Class targetEntity() default void.class;
 * CascadeType[] cascade() default {};
 * FetchType fetch() default EAGER;
 * boolean optional() default true;
 * }
 *
 *
 *
 * <p>Java class for many-to-one complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="many-to-one"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="join-column" type="{http://java.sun.com/xml/ns/persistence/orm}join-column" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="join-table" type="{http://java.sun.com/xml/ns/persistence/orm}join-table" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="cascade" type="{http://java.sun.com/xml/ns/persistence/orm}cascade-type" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="target-entity" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="fetch" type="{http://java.sun.com/xml/ns/persistence/orm}fetch-type" /&gt;
 *       &lt;attribute name="optional" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="access" type="{http://java.sun.com/xml/ns/persistence/orm}access-type" /&gt;
 *       &lt;attribute name="maps-id" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "many-to-one", propOrder = {
    "joinColumn",
    "joinTable",
    "cascade"
})
public class ManyToOne implements RelationField {

    @XmlElement(name = "join-column")
    protected List<JoinColumn> joinColumn;
    @XmlElement(name = "join-table")
    protected JoinTable joinTable;
    protected CascadeType cascade;
    @XmlAttribute
    protected FetchType fetch;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected Boolean optional;
    @XmlAttribute(name = "target-entity")
    protected String targetEntity;
    @XmlTransient
    protected RelationField relatedField;
    @XmlTransient
    protected boolean syntheticField;
    @XmlAttribute
    protected AccessType access;
    @XmlAttribute(name = "maps-id")
    protected String mapsId;
    @XmlAttribute
    protected Boolean id;

    /**
     * Gets the value of the joinColumn property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the joinColumn property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJoinColumn().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JoinColumn }
     */
    public List<JoinColumn> getJoinColumn() {
        if (joinColumn == null) {
            joinColumn = new ArrayList<JoinColumn>();
        }
        return this.joinColumn;
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
     * Gets the value of the optional property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isOptional() {
        return optional;
    }

    /**
     * Sets the value of the optional property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setOptional(final Boolean value) {
        this.optional = value;
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

    public String getMappedBy() {
        throw new UnsupportedOperationException("Many to one element can not have mapped-by");
    }

    public void setMappedBy(final String value) {
        throw new UnsupportedOperationException("Many to one element can not have mapped-by");
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
     * Gets the value of the mapsId property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMapsId() {
        return mapsId;
    }

    /**
     * Sets the value of the mapsId property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMapsId(final String value) {
        this.mapsId = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setId(final Boolean value) {
        this.id = value;
    }
}
