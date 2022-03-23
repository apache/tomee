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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * web-facesconfig_2_0.xsd
 *
 * <p>Java class for faces-config-managed-beanType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-managed-beanType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="managed-bean-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="managed-bean-class" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="managed-bean-scope" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="managed-property" type="{http://java.sun.com/xml/ns/javaee}faces-config-managed-propertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="map-entries" type="{http://java.sun.com/xml/ns/javaee}faces-config-map-entriesType"/&gt;
 *           &lt;element name="list-entries" type="{http://java.sun.com/xml/ns/javaee}faces-config-list-entriesType"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="managed-bean-extension" type="{http://java.sun.com/xml/ns/javaee}faces-config-managed-bean-extensionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-managed-beanType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "managedBeanName",
    "managedBeanClass",
    "managedBeanScope",
    "managedProperty",
    "mapEntries",
    "listEntries",
    "managedBeanExtension"
})
public class FacesManagedBean {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();
    @XmlElement(name = "managed-bean-name", required = true)
    protected java.lang.String managedBeanName;
    @XmlElement(name = "managed-bean-class", required = true)
    protected java.lang.String managedBeanClass;
    @XmlElement(name = "managed-bean-scope", required = true)
    protected java.lang.String managedBeanScope;
    @XmlElement(name = "managed-property")
    protected List<FacesManagedProperty> managedProperty;
    @XmlElement(name = "map-entries")
    protected FacesMapEntries mapEntries;
    @XmlElement(name = "list-entries")
    protected FacesListEntries listEntries;
    @XmlElement(name = "managed-bean-extension")
    protected List<FacesManagedBeanExtension> managedBeanExtension;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    public void setDisplayNames(final Text[] text) {
        displayName.set(text);
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public Collection<Icon> getIcons() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon;
    }

    public Map<String, Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    /**
     * Gets the value of the managedBeanName property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getManagedBeanName() {
        return managedBeanName;
    }

    /**
     * Sets the value of the managedBeanName property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setManagedBeanName(final java.lang.String value) {
        this.managedBeanName = value;
    }

    /**
     * Gets the value of the managedBeanClass property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getManagedBeanClass() {
        return managedBeanClass;
    }

    /**
     * Sets the value of the managedBeanClass property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setManagedBeanClass(final java.lang.String value) {
        this.managedBeanClass = value;
    }

    /**
     * Gets the value of the managedBeanScope property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getManagedBeanScope() {
        return managedBeanScope;
    }

    /**
     * Sets the value of the managedBeanScope property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setManagedBeanScope(final java.lang.String value) {
        this.managedBeanScope = value;
    }

    /**
     * Gets the value of the managedProperty property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the managedProperty property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getManagedProperty().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesManagedProperty }
     */
    public List<FacesManagedProperty> getManagedProperty() {
        if (managedProperty == null) {
            managedProperty = new ArrayList<FacesManagedProperty>();
        }
        return this.managedProperty;
    }

    /**
     * Gets the value of the mapEntries property.
     *
     * @return possible object is
     * {@link FacesMapEntries }
     */
    public FacesMapEntries getMapEntries() {
        return mapEntries;
    }

    /**
     * Sets the value of the mapEntries property.
     *
     * @param value allowed object is
     *              {@link FacesMapEntries }
     */
    public void setMapEntries(final FacesMapEntries value) {
        this.mapEntries = value;
    }

    /**
     * Gets the value of the listEntries property.
     *
     * @return possible object is
     * {@link FacesListEntries }
     */
    public FacesListEntries getListEntries() {
        return listEntries;
    }

    /**
     * Sets the value of the listEntries property.
     *
     * @param value allowed object is
     *              {@link FacesListEntries }
     */
    public void setListEntries(final FacesListEntries value) {
        this.listEntries = value;
    }

    /**
     * Gets the value of the managedBeanExtension property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the managedBeanExtension property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getManagedBeanExtension().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesManagedBeanExtension }
     */
    public List<FacesManagedBeanExtension> getManagedBeanExtension() {
        if (managedBeanExtension == null) {
            managedBeanExtension = new ArrayList<FacesManagedBeanExtension>();
        }
        return this.managedBeanExtension;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setId(final java.lang.String value) {
        this.id = value;
    }

}
