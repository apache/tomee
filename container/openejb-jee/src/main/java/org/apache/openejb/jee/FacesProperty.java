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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * web-facesconfig_2_0.xsd
 *
 * <p>Java class for faces-config-propertyType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-propertyType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="property-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="property-class" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="default-value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="suggested-value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="property-extension" type="{http://java.sun.com/xml/ns/javaee}faces-config-property-extensionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-propertyType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "propertyName",
    "propertyClass",
    "defaultValue",
    "suggestedValue",
    "propertyExtension"
})
public class FacesProperty {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();
    @XmlElement(name = "property-name", required = true)
    protected java.lang.String propertyName;
    @XmlElement(name = "property-class", required = true)
    protected java.lang.String propertyClass;
    @XmlElement(name = "default-value")
    protected java.lang.String defaultValue;
    @XmlElement(name = "suggested-value")
    protected java.lang.String suggestedValue;
    @XmlElement(name = "property-extension")
    protected List<FacesPropertyExtension> propertyExtension;
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
     * Gets the value of the propertyName property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getPropertyName() {
        return propertyName;
    }

    /**
     * Sets the value of the propertyName property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setPropertyName(final java.lang.String value) {
        this.propertyName = value;
    }

    /**
     * Gets the value of the propertyClass property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getPropertyClass() {
        return propertyClass;
    }

    /**
     * Sets the value of the propertyClass property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setPropertyClass(final java.lang.String value) {
        this.propertyClass = value;
    }

    /**
     * Gets the value of the defaultValue property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the value of the defaultValue property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setDefaultValue(final java.lang.String value) {
        this.defaultValue = value;
    }

    /**
     * Gets the value of the suggestedValue property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getSuggestedValue() {
        return suggestedValue;
    }

    /**
     * Sets the value of the suggestedValue property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setSuggestedValue(final java.lang.String value) {
        this.suggestedValue = value;
    }

    /**
     * Gets the value of the propertyExtension property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyExtension property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyExtension().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesPropertyExtension }
     */
    public List<FacesPropertyExtension> getPropertyExtension() {
        if (propertyExtension == null) {
            propertyExtension = new ArrayList<FacesPropertyExtension>();
        }
        return this.propertyExtension;
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
