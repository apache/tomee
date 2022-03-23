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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * <p class="changed_added_2_2">Defines a case that will
 * be considered in the <code>&lt;switch&gt;</code&gt;.</p&gt;
 *
 *
 *
 *
 * <p>Java class for faces-config-flow-definition-switch-caseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-flow-definition-switch-caseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://xmlns.jcp.org/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="if" type="{http://xmlns.jcp.org/xml/ns/javaee}faces-config-ifType" minOccurs="0"/&gt;
 *         &lt;element name="from-outcome" type="{http://xmlns.jcp.org/xml/ns/javaee}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-flow-definition-switch-caseType", propOrder = {
    "description",
    "displayName",
    "icon",
    "_if",
    "fromOutcome"
})
public class FacesConfigFlowDefinitionSwitchCase {

    protected List<Description> description;
    @XmlElement(name = "display-name")
    protected List<DisplayName> displayName;
    protected List<Icon> icon;
    @XmlElement(name = "if")
    protected FacesConfigIf _if;
    @XmlElement(name = "from-outcome")
    protected XmlString fromOutcome;
    @XmlAttribute(name = "id")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    /**
     * Gets the value of the description property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDescription().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Description }
     */
    public List<Description> getDescription() {
        if (description == null) {
            description = new ArrayList<Description>();
        }
        return this.description;
    }

    /**
     * Gets the value of the displayName property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the displayName property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDisplayName().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link DisplayName }
     */
    public List<DisplayName> getDisplayName() {
        if (displayName == null) {
            displayName = new ArrayList<DisplayName>();
        }
        return this.displayName;
    }

    /**
     * Gets the value of the icon property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the icon property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIcon().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link Icon }
     */
    public List<Icon> getIcon() {
        if (icon == null) {
            icon = new ArrayList<Icon>();
        }
        return this.icon;
    }

    /**
     * Gets the value of the if property.
     *
     * @return possible object is
     * {@link FacesConfigIf }
     */
    public FacesConfigIf getIf() {
        return _if;
    }

    /**
     * Sets the value of the if property.
     *
     * @param value allowed object is
     *              {@link FacesConfigIf }
     */
    public void setIf(final FacesConfigIf value) {
        this._if = value;
    }

    /**
     * Gets the value of the fromOutcome property.
     *
     * @return possible object is
     * {@link XmlString }
     */
    public XmlString getFromOutcome() {
        return fromOutcome;
    }

    /**
     * Sets the value of the fromOutcome property.
     *
     * @param value allowed object is
     *              {@link XmlString }
     */
    public void setFromOutcome(final XmlString value) {
        this.fromOutcome = value;
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
