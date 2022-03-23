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
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * web-facesconfig_2_0.xsd
 *
 * <p>Java class for faces-config-applicationType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-applicationType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="action-listener" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="default-render-kit-id" type="{http://java.sun.com/xml/ns/javaee}string"/&gt;
 *         &lt;element name="message-bundle" type="{http://java.sun.com/xml/ns/javaee}string"/&gt;
 *         &lt;element name="navigation-handler" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="view-handler" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="state-manager" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="el-resolver" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="property-resolver" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="variable-resolver" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="resource-handler" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="system-event-listener" type="{http://java.sun.com/xml/ns/javaee}faces-config-system-event-listenerType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="locale-config" type="{http://java.sun.com/xml/ns/javaee}faces-config-locale-configType"/&gt;
 *         &lt;element name="resource-bundle" type="{http://java.sun.com/xml/ns/javaee}faces-config-application-resource-bundleType"/&gt;
 *         &lt;element name="application-extension" type="{http://java.sun.com/xml/ns/javaee}faces-config-application-extensionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="default-validators" type="{http://java.sun.com/xml/ns/javaee}faces-config-default-validatorsType"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-applicationType", propOrder = {
    "actionListener",
    "defaultRenderKitId",
    "messageBundle",
    "navigationHandler",
    "viewHandler",
    "stateManager",
    "elResolver",
    "propertyResolver",
    "variableResolver",
    "resourceHandler",
    "systemEventListener",
    "localeConfig",
    "resourceBundle",
    "applicationExtension",
    "defaultValidators"
})
public class FacesApplication {

    @XmlElement(name = "action-listener")
    protected List<java.lang.String> actionListener;
    @XmlElement(name = "default-render-kit-id")
    protected List<java.lang.String> defaultRenderKitId;
    @XmlElement(name = "message-bundle")
    protected List<java.lang.String> messageBundle;
    @XmlElement(name = "navigation-handler")
    protected List<java.lang.String> navigationHandler;
    @XmlElement(name = "view-handler")
    protected List<java.lang.String> viewHandler;
    @XmlElement(name = "state-manager")
    protected List<java.lang.String> stateManager;
    @XmlElement(name = "el-resolver")
    protected List<java.lang.String> elResolver;
    @XmlElement(name = "property-resolver")
    protected List<java.lang.String> propertyResolver;
    @XmlElement(name = "variable-resolver")
    protected List<java.lang.String> variableResolver;
    @XmlElement(name = "resource-handler")
    protected List<java.lang.String> resourceHandler;
    @XmlElement(name = "system-event-listener")
    protected List<FacesSystemEventListener> systemEventListener;
    @XmlElement(name = "locale-config")
    protected List<FacesLocaleConfig> localeConfig;
    @XmlElement(name = "resource-bundle", required = true)
    protected FacesApplicationResourceBundle resourceBundle;
    @XmlElement(name = "application-extension")
    protected List<FacesApplicationExtension> applicationExtension;
    @XmlElement(name = "default-validators")
    protected List<FacesValidator> defaultValidators;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    /**
     * Gets the value of the actionListener property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the actionListener property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActionListener().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getActionListener() {
        if (actionListener == null) {
            actionListener = new ArrayList<java.lang.String>();
        }
        return this.actionListener;
    }

    /**
     * Gets the value of the defaultRenderKitId property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the defaultRenderKitId property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDefaultRenderKitId().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getDefaultRenderKitId() {
        if (defaultRenderKitId == null) {
            defaultRenderKitId = new ArrayList<java.lang.String>();
        }
        return this.defaultRenderKitId;
    }

    /**
     * Gets the value of the messageBundle property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageBundle property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageBundle().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getMessageBundle() {
        if (messageBundle == null) {
            messageBundle = new ArrayList<java.lang.String>();
        }
        return this.messageBundle;
    }

    /**
     * Gets the value of the navigationHandler property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the navigationHandler property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNavigationHandler().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getNavigationHandler() {
        if (navigationHandler == null) {
            navigationHandler = new ArrayList<java.lang.String>();
        }
        return this.navigationHandler;
    }

    /**
     * Gets the value of the viewHandler property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the viewHandler property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getViewHandler().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getViewHandler() {
        if (viewHandler == null) {
            viewHandler = new ArrayList<java.lang.String>();
        }
        return this.viewHandler;
    }

    /**
     * Gets the value of the stateManager property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stateManager property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStateManager().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getStateManager() {
        if (stateManager == null) {
            stateManager = new ArrayList<java.lang.String>();
        }
        return this.stateManager;
    }

    /**
     * Gets the value of the elResolver property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the elResolver property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getElResolver().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getElResolver() {
        if (elResolver == null) {
            elResolver = new ArrayList<java.lang.String>();
        }
        return this.elResolver;
    }

    /**
     * Gets the value of the propertyResolver property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyResolver property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyResolver().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getPropertyResolver() {
        if (propertyResolver == null) {
            propertyResolver = new ArrayList<java.lang.String>();
        }
        return this.propertyResolver;
    }

    /**
     * Gets the value of the variableResolver property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the variableResolver property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVariableResolver().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getVariableResolver() {
        if (variableResolver == null) {
            variableResolver = new ArrayList<java.lang.String>();
        }
        return this.variableResolver;
    }


    public List<String> getResourceHandler() {
        if (resourceHandler == null) {
            resourceHandler = new ArrayList<String>();
        }
        return resourceHandler;
    }

    public List<FacesSystemEventListener> getSystemEventListener() {
        if (systemEventListener == null) {
            systemEventListener = new ArrayList<FacesSystemEventListener>();
        }
        return systemEventListener;
    }

    /**
     * Gets the value of the localeConfig property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the localeConfig property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocaleConfig().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesLocaleConfig }
     */
    public List<FacesLocaleConfig> getLocaleConfig() {
        if (localeConfig == null) {
            localeConfig = new ArrayList<FacesLocaleConfig>();
        }
        return this.localeConfig;
    }

    /**
     * Gets the value of the resourceBundle property.
     *
     * @return possible object is
     * {@link FacesApplicationResourceBundle }
     */
    public FacesApplicationResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * Sets the value of the resourceBundle property.
     *
     * @param value allowed object is
     *              {@link FacesApplicationResourceBundle }
     */
    public void setResourceBundle(final FacesApplicationResourceBundle value) {
        this.resourceBundle = value;
    }

    /**
     * Gets the value of the applicationExtension property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the applicationExtension property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApplicationExtension().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesApplicationExtension }
     */
    public List<FacesApplicationExtension> getApplicationExtension() {
        if (applicationExtension == null) {
            applicationExtension = new ArrayList<FacesApplicationExtension>();
        }
        return this.applicationExtension;
    }

    public List<FacesValidator> getDefaultValidators() {
        if (defaultValidators == null) {
            defaultValidators = new ArrayList<FacesValidator>();
        }
        return defaultValidators;
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
