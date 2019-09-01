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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * web-facesconfig_2_0.xsd
 *
 * <p>Java class for faces-config-factoryType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-factoryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="application-factory" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="exception-handler-factory" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="external-context-factory" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="faces-context-factory" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="partial-view-context-factory" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="lifecycle-factory" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="view-declaration-language-factory" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="tag-handler-delegate-factory" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="render-kit-factory" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="visit-context-factory" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="factory-extension" type="{http://java.sun.com/xml/ns/javaee}faces-config-factory-extensionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 5 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-factoryType", propOrder = {
    "applicationFactory",
    "exceptionHandlerFactory",
    "externalContextFactory",
    "facesContextFactory",
    "partialViewContextFactory",
    "lifecycleFactory",
    "viewDeclarationLanguageFactory",
    "tagHandlerDelegateFactory",
    "renderKitFactory",
    "visitContextFactory",
    "factoryExtension"
})
public class FacesFactory {

    @XmlElement(name = "application-factory")
    protected List<java.lang.String> applicationFactory;
    @XmlElement(name = "exception-handler-factory")
    protected List<java.lang.String> exceptionHandlerFactory;
    @XmlElement(name = "external-context-factory")
    protected List<java.lang.String> externalContextFactory;
    @XmlElement(name = "faces-context-factory")
    protected List<java.lang.String> facesContextFactory;
    @XmlElement(name = "partial-view-context-factory")
    protected List<java.lang.String> partialViewContextFactory;
    @XmlElement(name = "lifecycle-factory")
    protected List<java.lang.String> lifecycleFactory;
    @XmlElement(name = "view-declaration-language-factory")
    protected List<java.lang.String> viewDeclarationLanguageFactory;
    @XmlElement(name = "tag-handler-delegate-factory")
    protected List<java.lang.String> tagHandlerDelegateFactory;
    @XmlElement(name = "render-kit-factory")
    protected List<java.lang.String> renderKitFactory;
    @XmlElement(name = "visit-context-factory")
    protected List<java.lang.String> visitContextFactory;
    @XmlElement(name = "factory-extension")
    protected List<FacesFactoryExtension> factoryExtension;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    /**
     * Gets the value of the applicationFactory property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the applicationFactory property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApplicationFactory().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getApplicationFactory() {
        if (applicationFactory == null) {
            applicationFactory = new ArrayList<java.lang.String>();
        }
        return this.applicationFactory;
    }

    public List<String> getExceptionHandlerFactory() {
        if (exceptionHandlerFactory == null) {
            exceptionHandlerFactory = new ArrayList<String>();
        }
        return exceptionHandlerFactory;
    }

    public List<String> getExternalContextFactory() {
        if (externalContextFactory == null) {
            externalContextFactory = new ArrayList<String>();
        }
        return externalContextFactory;
    }

    /**
     * Gets the value of the facesContextFactory property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the facesContextFactory property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFacesContextFactory().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getFacesContextFactory() {
        if (facesContextFactory == null) {
            facesContextFactory = new ArrayList<java.lang.String>();
        }
        return this.facesContextFactory;
    }

    /**
     * Gets the value of the lifecycleFactory property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lifecycleFactory property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLifecycleFactory().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getLifecycleFactory() {
        if (lifecycleFactory == null) {
            lifecycleFactory = new ArrayList<java.lang.String>();
        }
        return this.lifecycleFactory;
    }

    public List<String> getViewDeclarationLanguageFactory() {
        if (viewDeclarationLanguageFactory == null) {
            viewDeclarationLanguageFactory = new ArrayList<String>();
        }
        return viewDeclarationLanguageFactory;
    }

    public List<String> getPartialViewContextFactory() {
        if (partialViewContextFactory == null) {
            partialViewContextFactory = new ArrayList<String>();
        }
        return partialViewContextFactory;
    }

    public List<String> getTagHandlerDelegateFactory() {
        if (tagHandlerDelegateFactory == null) {
            tagHandlerDelegateFactory = new ArrayList<String>();
        }
        return tagHandlerDelegateFactory;
    }

    /**
     * Gets the value of the renderKitFactory property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the renderKitFactory property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRenderKitFactory().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.String }
     */
    public List<java.lang.String> getRenderKitFactory() {
        if (renderKitFactory == null) {
            renderKitFactory = new ArrayList<java.lang.String>();
        }
        return this.renderKitFactory;
    }

    public List<String> getVisitContextFactory() {
        if (visitContextFactory == null) {
            visitContextFactory = new ArrayList<String>();
        }
        return visitContextFactory;
    }

    /**
     * Gets the value of the factoryExtension property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the factoryExtension property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFactoryExtension().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link FacesFactoryExtension }
     */
    public List<FacesFactoryExtension> getFactoryExtension() {
        if (factoryExtension == null) {
            factoryExtension = new ArrayList<FacesFactoryExtension>();
        }
        return this.factoryExtension;
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
