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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 
 *         Defines the settings and mappings for a mapped superclass. Is 
 *         allowed to be sparsely populated and used in conjunction with 
 *         the annotations. Alternatively, the metadata-complete attribute 
 *         can be used to indicate that no annotations are to be processed 
 *         If this is the case then the defaulting rules will be recursively 
 *         applied.
 * 
 *         @Target(TYPE) @Retention(RUNTIME)
 *         public @interface MappedSuperclass{}
 * 
 *       
 * 
 * <p>Java class for mapped-superclass complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mapped-superclass">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="id-class" type="{http://java.sun.com/xml/ns/persistence/orm}id-class" minOccurs="0"/>
 *         &lt;element name="exclude-default-listeners" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/>
 *         &lt;element name="exclude-superclass-listeners" type="{http://java.sun.com/xml/ns/persistence/orm}emptyType" minOccurs="0"/>
 *         &lt;element name="entity-listeners" type="{http://java.sun.com/xml/ns/persistence/orm}entity-listeners" minOccurs="0"/>
 *         &lt;element name="pre-persist" type="{http://java.sun.com/xml/ns/persistence/orm}pre-persist" minOccurs="0"/>
 *         &lt;element name="post-persist" type="{http://java.sun.com/xml/ns/persistence/orm}post-persist" minOccurs="0"/>
 *         &lt;element name="pre-remove" type="{http://java.sun.com/xml/ns/persistence/orm}pre-remove" minOccurs="0"/>
 *         &lt;element name="post-remove" type="{http://java.sun.com/xml/ns/persistence/orm}post-remove" minOccurs="0"/>
 *         &lt;element name="pre-update" type="{http://java.sun.com/xml/ns/persistence/orm}pre-update" minOccurs="0"/>
 *         &lt;element name="post-update" type="{http://java.sun.com/xml/ns/persistence/orm}post-update" minOccurs="0"/>
 *         &lt;element name="post-load" type="{http://java.sun.com/xml/ns/persistence/orm}post-load" minOccurs="0"/>
 *         &lt;element name="attributes" type="{http://java.sun.com/xml/ns/persistence/orm}attributes" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="access" type="{http://java.sun.com/xml/ns/persistence/orm}access-type" />
 *       &lt;attribute name="class" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="metadata-complete" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mapped-superclass", propOrder = {
    "description",
    "idClass",
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
    "attributes"
})
public class MappedSuperclass implements Mapping {

    protected String description;
    @XmlElement(name = "id-class")
    protected IdClass idClass;
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
    protected Attributes attributes;
    @XmlAttribute
    protected AccessType access;
    @XmlAttribute(name = "class", required = true)
    protected String clazz;
    @XmlAttribute(name = "metadata-complete")
    protected Boolean metadataComplete;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the idClass property.
     * 
     * @return
     *     possible object is
     *     {@link IdClass }
     *     
     */
    public IdClass getIdClass() {
        return idClass;
    }

    /**
     * Sets the value of the idClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdClass }
     *     
     */
    public void setIdClass(IdClass value) {
        this.idClass = value;
    }

    /**
     * Gets the value of the excludeDefaultListeners property.
     * 
     * @return
     *     possible object is
     *     {@link boolean }
     *     
     */
    public boolean isExcludeDefaultListeners() {
        return excludeDefaultListeners != null;
    }

    /**
     * Sets the value of the excludeDefaultListeners property.
     * 
     * @param value
     *     allowed object is
     *     {@link boolean }
     *     
     */
    public void setExcludeDefaultListeners(boolean value) {
        this.excludeDefaultListeners = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the excludeSuperclassListeners property.
     * 
     * @return
     *     possible object is
     *     {@link boolean }
     *     
     */
    public boolean isExcludeSuperclassListeners() {
        return excludeSuperclassListeners != null;
    }

    /**
     * Sets the value of the excludeSuperclassListeners property.
     * 
     * @param value
     *     allowed object is
     *     {@link boolean }
     *     
     */
    public void setExcludeSuperclassListeners(boolean value) {
        this.excludeSuperclassListeners = value ? new EmptyType() : null;
    }

    /**
     * Gets the value of the entityListeners property.
     * 
     * @return
     *     possible object is
     *     {@link EntityListeners }
     *     
     */
    public EntityListeners getEntityListeners() {
        return entityListeners;
    }

    /**
     * Sets the value of the entityListeners property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntityListeners }
     *     
     */
    public void setEntityListeners(EntityListeners value) {
        this.entityListeners = value;
    }

    /**
     * Gets the value of the prePersist property.
     * 
     * @return
     *     possible object is
     *     {@link PrePersist }
     *     
     */
    public PrePersist getPrePersist() {
        return prePersist;
    }

    /**
     * Sets the value of the prePersist property.
     * 
     * @param value
     *     allowed object is
     *     {@link PrePersist }
     *     
     */
    public void setPrePersist(PrePersist value) {
        this.prePersist = value;
    }

    /**
     * Gets the value of the postPersist property.
     * 
     * @return
     *     possible object is
     *     {@link PostPersist }
     *     
     */
    public PostPersist getPostPersist() {
        return postPersist;
    }

    /**
     * Sets the value of the postPersist property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostPersist }
     *     
     */
    public void setPostPersist(PostPersist value) {
        this.postPersist = value;
    }

    /**
     * Gets the value of the preRemove property.
     * 
     * @return
     *     possible object is
     *     {@link PreRemove }
     *     
     */
    public PreRemove getPreRemove() {
        return preRemove;
    }

    /**
     * Sets the value of the preRemove property.
     * 
     * @param value
     *     allowed object is
     *     {@link PreRemove }
     *     
     */
    public void setPreRemove(PreRemove value) {
        this.preRemove = value;
    }

    /**
     * Gets the value of the postRemove property.
     * 
     * @return
     *     possible object is
     *     {@link PostRemove }
     *     
     */
    public PostRemove getPostRemove() {
        return postRemove;
    }

    /**
     * Sets the value of the postRemove property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostRemove }
     *     
     */
    public void setPostRemove(PostRemove value) {
        this.postRemove = value;
    }

    /**
     * Gets the value of the preUpdate property.
     * 
     * @return
     *     possible object is
     *     {@link PreUpdate }
     *     
     */
    public PreUpdate getPreUpdate() {
        return preUpdate;
    }

    /**
     * Sets the value of the preUpdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link PreUpdate }
     *     
     */
    public void setPreUpdate(PreUpdate value) {
        this.preUpdate = value;
    }

    /**
     * Gets the value of the postUpdate property.
     * 
     * @return
     *     possible object is
     *     {@link PostUpdate }
     *     
     */
    public PostUpdate getPostUpdate() {
        return postUpdate;
    }

    /**
     * Sets the value of the postUpdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostUpdate }
     *     
     */
    public void setPostUpdate(PostUpdate value) {
        this.postUpdate = value;
    }

    /**
     * Gets the value of the postLoad property.
     * 
     * @return
     *     possible object is
     *     {@link PostLoad }
     *     
     */
    public PostLoad getPostLoad() {
        return postLoad;
    }

    /**
     * Sets the value of the postLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostLoad }
     *     
     */
    public void setPostLoad(PostLoad value) {
        this.postLoad = value;
    }

    /**
     * Gets the value of the attributes property.
     * 
     * @return
     *     possible object is
     *     {@link Attributes }
     *     
     */
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Attributes }
     *     
     */
    public void setAttributes(Attributes value) {
        this.attributes = value;
    }

    /**
     * Gets the value of the access property.
     * 
     * @return
     *     possible object is
     *     {@link AccessType }
     *     
     */
    public AccessType getAccess() {
        return access;
    }

    /**
     * Sets the value of the access property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessType }
     *     
     */
    public void setAccess(AccessType value) {
        this.access = value;
    }

    /**
     * Gets the value of the clazz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the metadataComplete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMetadataComplete() {
        return metadataComplete;
    }

    /**
     * Sets the value of the metadataComplete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMetadataComplete(Boolean value) {
        this.metadataComplete = value;
    }


    public void addField(Field field) {
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
            throw new IllegalArgumentException("Mapped super class does not support attribute override");
        } else {
            throw new IllegalArgumentException("Unknown field type " + field.getClass());
        }
    }
}
