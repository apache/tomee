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
import java.util.Map;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * This element contains the entity field or property mappings.
 * It may be sparsely populated to include only a subset of the
 * fields or properties. If metadata-complete for the entity is true
 * then the remainder of the attributes will be defaulted according
 * to the default rules.
 *
 *
 *
 * <p>Java class for attributes complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="attributes"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="id" type="{http://java.sun.com/xml/ns/persistence/orm}id" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="embedded-id" type="{http://java.sun.com/xml/ns/persistence/orm}embedded-id" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="basic" type="{http://java.sun.com/xml/ns/persistence/orm}basic" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="version" type="{http://java.sun.com/xml/ns/persistence/orm}version" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="many-to-one" type="{http://java.sun.com/xml/ns/persistence/orm}many-to-one" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="one-to-many" type="{http://java.sun.com/xml/ns/persistence/orm}one-to-many" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="one-to-one" type="{http://java.sun.com/xml/ns/persistence/orm}one-to-one" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="many-to-many" type="{http://java.sun.com/xml/ns/persistence/orm}many-to-many" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="element-collection" type="{http://java.sun.com/xml/ns/persistence/orm}element-collection" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="embedded" type="{http://java.sun.com/xml/ns/persistence/orm}embedded" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="transient" type="{http://java.sun.com/xml/ns/persistence/orm}transient" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "attributes", propOrder = {
    "description",
    "id",
    "embeddedId",
    "basic",
    "version",
    "manyToOne",
    "oneToMany",
    "oneToOne",
    "manyToMany",
    "elementCollection",
    "embedded",
    "_transient"
})
public class Attributes {

    protected String description;
    protected List<Id> id;
    @XmlElement(name = "embedded-id")
    protected EmbeddedId embeddedId;
    protected List<Basic> basic;
    protected List<Version> version;
    @XmlElement(name = "many-to-one")
    protected List<ManyToOne> manyToOne;
    @XmlElement(name = "one-to-many")
    protected List<OneToMany> oneToMany;
    @XmlElement(name = "one-to-one")
    protected List<OneToOne> oneToOne;
    @XmlElement(name = "many-to-many")
    protected List<ManyToMany> manyToMany;
    @XmlElement(name = "element-collection")
    protected List<ElementCollection> elementCollection;
    protected List<Embedded> embedded;
    @XmlElement(name = "transient")
    protected List<Transient> _transient;

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
     * Gets the value of the id property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the id property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getId().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Id }
     */
    public List<Id> getId() {
        if (id == null) {
            id = new ArrayList<Id>();
        }
        return this.id;
    }

    /**
     * Gets the value of the embeddedId property.
     *
     * @return possible object is
     * {@link EmbeddedId }
     */
    public EmbeddedId getEmbeddedId() {
        return embeddedId;
    }

    /**
     * Sets the value of the embeddedId property.
     *
     * @param value allowed object is
     *              {@link EmbeddedId }
     */
    public void setEmbeddedId(final EmbeddedId value) {
        this.embeddedId = value;
    }

    /**
     * Gets the value of the basic property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the basic property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBasic().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Basic }
     */
    public List<Basic> getBasic() {
        if (basic == null) {
            basic = new ArrayList<Basic>();
        }
        return this.basic;
    }

    /**
     * Gets the value of the version property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the version property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVersion().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Version }
     */
    public List<Version> getVersion() {
        if (version == null) {
            version = new ArrayList<Version>();
        }
        return this.version;
    }

    /**
     * Gets the value of the manyToOne property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the manyToOne property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getManyToOne().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ManyToOne }
     */
    public List<ManyToOne> getManyToOne() {
        if (manyToOne == null) {
            manyToOne = new ArrayList<ManyToOne>();
        }
        return this.manyToOne;
    }

    /**
     * Gets the value of the oneToMany property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the oneToMany property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOneToMany().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link OneToMany }
     */
    public List<OneToMany> getOneToMany() {
        if (oneToMany == null) {
            oneToMany = new ArrayList<OneToMany>();
        }
        return this.oneToMany;
    }

    /**
     * Gets the value of the oneToOne property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the oneToOne property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOneToOne().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link OneToOne }
     */
    public List<OneToOne> getOneToOne() {
        if (oneToOne == null) {
            oneToOne = new ArrayList<OneToOne>();
        }
        return this.oneToOne;
    }

    /**
     * Gets the value of the manyToMany property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the manyToMany property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getManyToMany().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ManyToMany }
     */
    public List<ManyToMany> getManyToMany() {
        if (manyToMany == null) {
            manyToMany = new ArrayList<ManyToMany>();
        }
        return this.manyToMany;
    }

    public Map<String, RelationField> getRelationshipFieldMap() {
        final KeyedCollection<String, RelationField> fields = new KeyedCollection<String, RelationField>();
        if (oneToOne != null) fields.addAll(oneToOne);
        if (oneToMany != null) fields.addAll(oneToMany);
        if (manyToMany != null) fields.addAll(manyToMany);
        if (manyToOne != null) fields.addAll(manyToOne);
        return fields.toMap();
    }

    /**
     * Gets the value of the elementCollection property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the elementCollection property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getElementCollection().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link ElementCollection }
     */
    public List<ElementCollection> getElementCollection() {
        if (elementCollection == null) {
            elementCollection = new ArrayList<ElementCollection>();
        }
        return this.elementCollection;
    }

    /**
     * Gets the value of the embedded property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the embedded property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEmbedded().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Embedded }
     */
    public List<Embedded> getEmbedded() {
        if (embedded == null) {
            embedded = new ArrayList<Embedded>();
        }
        return this.embedded;
    }

    /**
     * Gets the value of the transient property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transient property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransient().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Transient }
     */
    public List<Transient> getTransient() {
        if (_transient == null) {
            _transient = new ArrayList<Transient>();
        }
        return this._transient;
    }


    // TODO: This should not be necessary, but having an empty <attributes/> tag
    // causes some of the unit tests to fail.  Not sure why.  Should be fixed.
    public boolean isEmpty() {
        if (id != null) return false;
        if (embeddedId != null) return false;
        if (basic != null) return false;
        if (version != null) return false;
        if (manyToOne != null) return false;
        if (oneToMany != null) return false;
        if (oneToOne != null) return false;
        if (manyToMany != null) return false;
        if (embedded != null) return false;
        if (_transient != null) return false;
        return true;
    }
}
