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
 * <p>Java class for faces-config-componentType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-componentType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="component-type" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="component-class" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="facet" type="{http://java.sun.com/xml/ns/javaee}faces-config-facetType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="attribute" type="{http://java.sun.com/xml/ns/javaee}faces-config-attributeType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="property" type="{http://java.sun.com/xml/ns/javaee}faces-config-propertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="component-extension" type="{http://java.sun.com/xml/ns/javaee}faces-config-component-extensionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-componentType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "componentType",
    "componentClass",
    "facet",
    "attribute",
    "property",
    "componentExtension"
})
public class FacesComponent {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();
    @XmlElement(name = "component-type", required = true)
    protected java.lang.String componentType;
    @XmlElement(name = "component-class", required = true)
    protected java.lang.String componentClass;
    protected List<FacesFacet> facet;
    protected List<FacesAttribute> attribute;
    protected List<FacesProperty> property;
    @XmlElement(name = "component-extension")
    protected List<FacesComponentExtension> componentExtension;
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
     * Gets the value of the componentType property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getComponentType() {
        return componentType;
    }

    /**
     * Sets the value of the componentType property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setComponentType(final java.lang.String value) {
        this.componentType = value;
    }

    /**
     * Gets the value of the componentClass property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getComponentClass() {
        return componentClass;
    }

    /**
     * Sets the value of the componentClass property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setComponentClass(final java.lang.String value) {
        this.componentClass = value;
    }

    /**
     * Gets the value of the facet property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the facet property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFacet().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesFacet }
     */
    public List<FacesFacet> getFacet() {
        if (facet == null) {
            facet = new ArrayList<FacesFacet>();
        }
        return this.facet;
    }

    /**
     * Gets the value of the attribute property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attribute property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttribute().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesAttribute }
     */
    public List<FacesAttribute> getAttribute() {
        if (attribute == null) {
            attribute = new ArrayList<FacesAttribute>();
        }
        return this.attribute;
    }

    /**
     * Gets the value of the property property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesProperty }
     */
    public List<FacesProperty> getProperty() {
        if (property == null) {
            property = new ArrayList<FacesProperty>();
        }
        return this.property;
    }

    /**
     * Gets the value of the componentExtension property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the componentExtension property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComponentExtension().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesComponentExtension }
     */
    public List<FacesComponentExtension> getComponentExtension() {
        if (componentExtension == null) {
            componentExtension = new ArrayList<FacesComponentExtension>();
        }
        return this.componentExtension;
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
