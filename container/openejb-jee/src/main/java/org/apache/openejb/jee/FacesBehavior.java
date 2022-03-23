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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;


/**
 * web-facesconfig_2_0.xsd
 *
 * <p>Java class for faces-config-behaviorType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-behaviorType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="behavior-id" type="{http://java.sun.com/xml/ns/javaee}string"/&gt;
 *         &lt;element name="behavior-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="attribute" type="{http://java.sun.com/xml/ns/javaee}faces-config-attributeType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="property" type="{http://java.sun.com/xml/ns/javaee}faces-config-propertyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="behavior-extension" type="{http://java.sun.com/xml/ns/javaee}faces-config-behavior-extensionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-behaviorType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "behaviorId",
    "behaviorClass",
    "attribute",
    "property",
    "behaviorExtension"
})
public class FacesBehavior {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();
    @XmlElement(name = "behavior-id", required = true)
    protected String behaviorId;
    @XmlElement(name = "behavior-class", required = true)
    protected String behaviorClass;
    protected List<FacesAttribute> attribute;
    protected List<FacesProperty> property;
    @XmlElement(name = "behavior-extension")
    protected List<FacesBehaviorExtension> behaviorExtension;

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
     * Gets the value of the behaviorId property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getBehaviorId() {
        return behaviorId;
    }

    /**
     * Sets the value of the behaviorId property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBehaviorId(final String value) {
        this.behaviorId = value;
    }

    /**
     * Gets the value of the behaviorClass property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getBehaviorClass() {
        return behaviorClass;
    }

    /**
     * Sets the value of the behaviorClass property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBehaviorClass(final String value) {
        this.behaviorClass = value;
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
     * Gets the value of the behaviorExtension property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the behaviorExtension property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBehaviorExtension().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesBehaviorExtension }
     */
    public List<FacesBehaviorExtension> getBehaviorExtension() {
        if (behaviorExtension == null) {
            behaviorExtension = new ArrayList<FacesBehaviorExtension>();
        }
        return this.behaviorExtension;
    }

}
