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
 * <p>Java class for faces-config-navigation-ruleType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-navigation-ruleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="from-view-id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="navigation-case" type="{http://java.sun.com/xml/ns/javaee}faces-config-navigation-caseType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="navigation-rule-extension" type="{http://java.sun.com/xml/ns/javaee}faces-config-navigation-rule-extensionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-navigation-ruleType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "fromViewId",
    "navigationCase",
    "navigationRuleExtension"
})
public class FacesNavigationRule {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();
    @XmlElement(name = "from-view-id")
    protected java.lang.String fromViewId;
    @XmlElement(name = "navigation-case")
    protected List<FacesNavigationCase> navigationCase;
    @XmlElement(name = "navigation-rule-extension")
    protected List<FacesNavigationRuleExtension> navigationRuleExtension;
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
     * Gets the value of the fromViewId property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getFromViewId() {
        return fromViewId;
    }

    /**
     * Sets the value of the fromViewId property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setFromViewId(final java.lang.String value) {
        this.fromViewId = value;
    }

    /**
     * Gets the value of the navigationCase property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the navigationCase property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNavigationCase().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesNavigationCase }
     */
    public List<FacesNavigationCase> getNavigationCase() {
        if (navigationCase == null) {
            navigationCase = new ArrayList<FacesNavigationCase>();
        }
        return this.navigationCase;
    }

    /**
     * Gets the value of the navigationRuleExtension property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the navigationRuleExtension property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNavigationRuleExtension().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesNavigationRuleExtension }
     */
    public List<FacesNavigationRuleExtension> getNavigationRuleExtension() {
        if (navigationRuleExtension == null) {
            navigationRuleExtension = new ArrayList<FacesNavigationRuleExtension>();
        }
        return this.navigationRuleExtension;
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
