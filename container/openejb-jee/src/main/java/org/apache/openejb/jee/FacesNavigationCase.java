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
 * <p>Java class for faces-config-navigation-caseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-navigation-caseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="from-action" type="{http://java.sun.com/xml/ns/javaee}faces-config-from-actionType" minOccurs="0"/&gt;
 *         &lt;element name="from-outcome" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="if" type="{http://java.sun.com/xml/ns/javaee}faces-config-ifType" minOccurs="0"/&gt;
 *         &lt;element name="to-view-id" type="{http://java.sun.com/xml/ns/javaee}faces-config-valueType"/&gt;
 *         &lt;element name="redirect" type="{http://java.sun.com/xml/ns/javaee}faces-config-redirectType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 48 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-navigation-caseType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "fromAction",
    "fromOutcome",
    "_if",
    "toViewId",
    "redirect"
})
public class FacesNavigationCase {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();
    @XmlElement(name = "from-action")
    protected FacesFromAction fromAction;
    @XmlElement(name = "from-outcome")
    protected java.lang.String fromOutcome;
    //this is a faces EL expression
    @XmlElement(name = "if")
    protected String _if;
    @XmlElement(name = "to-view-id", required = true)
    protected java.lang.String toViewId;
    protected FacesRedirect redirect;
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
     * Gets the value of the fromAction property.
     *
     * @return possible object is
     * {@link FacesFromAction }
     */
    public FacesFromAction getFromAction() {
        return fromAction;
    }

    /**
     * Sets the value of the fromAction property.
     *
     * @param value allowed object is
     *              {@link FacesFromAction }
     */
    public void setFromAction(final FacesFromAction value) {
        this.fromAction = value;
    }

    /**
     * Gets the value of the fromOutcome property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getFromOutcome() {
        return fromOutcome;
    }

    /**
     * Sets the value of the fromOutcome property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setFromOutcome(final java.lang.String value) {
        this.fromOutcome = value;
    }

    /**
     * Gets the value of the if property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getIf() {
        return _if;
    }

    /**
     * Sets the value of the if property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIf(final String value) {
        this._if = value;
    }

    /**
     * Gets the value of the toViewId property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getToViewId() {
        return toViewId;
    }

    /**
     * Sets the value of the toViewId property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setToViewId(final java.lang.String value) {
        this.toViewId = value;
    }

    /**
     * Gets the value of the redirect property.
     *
     * @return possible object is
     * {@link FacesRedirect }
     */
    public FacesRedirect getRedirect() {
        return redirect;
    }

    /**
     * Sets the value of the redirect property.
     *
     * @param value allowed object is
     *              {@link FacesRedirect }
     */
    public void setRedirect(final FacesRedirect value) {
        this.redirect = value;
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
