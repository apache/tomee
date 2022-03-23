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
 * <p>Java class for faces-config-render-kitType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-render-kitType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="render-kit-id" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="render-kit-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/&gt;
 *         &lt;element name="renderer" type="{http://java.sun.com/xml/ns/javaee}faces-config-rendererType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="client-behavior-renderer" type="{http://java.sun.com/xml/ns/javaee}faces-config-client-behavior-rendererType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="render-kit-extension" type="{http://java.sun.com/xml/ns/javaee}faces-config-render-kit-extensionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-render-kitType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "renderKitId",
    "renderKitClass",
    "renderer",
    "clientBehaviorRenderer",
    "renderKitExtension"
})
public class FacesRenderKit {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();
    @XmlElement(name = "render-kit-id")
    protected java.lang.String renderKitId;
    @XmlElement(name = "render-kit-class")
    protected java.lang.String renderKitClass;
    protected List<FacesRenderer> renderer;
    @XmlElement(name = "client-behavior-renderer")
    protected List<FacesClientBehaviorRenderer> clientBehaviorRenderer;
    @XmlElement(name = "render-kit-extension")
    protected List<FacesRenderKitExtension> renderKitExtension;
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
     * Gets the value of the renderKitId property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getRenderKitId() {
        return renderKitId;
    }

    /**
     * Sets the value of the renderKitId property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setRenderKitId(final java.lang.String value) {
        this.renderKitId = value;
    }

    /**
     * Gets the value of the renderKitClass property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getRenderKitClass() {
        return renderKitClass;
    }

    /**
     * Sets the value of the renderKitClass property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setRenderKitClass(final java.lang.String value) {
        this.renderKitClass = value;
    }

    /**
     * Gets the value of the renderer property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the renderer property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRenderer().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesRenderer }
     */
    public List<FacesRenderer> getRenderer() {
        if (renderer == null) {
            renderer = new ArrayList<FacesRenderer>();
        }
        return this.renderer;
    }

    public List<FacesClientBehaviorRenderer> getClientBehaviorRenderer() {
        if (clientBehaviorRenderer == null) {
            clientBehaviorRenderer = new ArrayList<FacesClientBehaviorRenderer>();
        }
        return this.clientBehaviorRenderer;
    }

    /**
     * Gets the value of the renderKitExtension property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the renderKitExtension property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRenderKitExtension().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesRenderKitExtension }
     */
    public List<FacesRenderKitExtension> getRenderKitExtension() {
        if (renderKitExtension == null) {
            renderKitExtension = new ArrayList<FacesRenderKitExtension>();
        }
        return this.renderKitExtension;
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
