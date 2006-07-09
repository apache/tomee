/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.jee2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The interceptorType element declares information about a single
 * interceptor class.  It consists of :
 * <p/>
 * - An optional description.
 * - The fully-qualified name of the interceptor class.
 * - An optional list of around invoke methods declared on the
 * interceptor class and/or its super-classes.
 * - An optional list environment dependencies for the interceptor
 * class and/or its super-classes.
 * - An optional list of post-activate methods declared on the
 * interceptor class and/or its super-classes.
 * - An optional list of pre-passivate methods declared on the
 * interceptor class and/or its super-classes.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "interceptorType", propOrder = {
        "description",
        "interceptorClass",
        "aroundInvoke",
        "envEntry",
        "ejbRef",
        "ejbLocalRef",
        "serviceRef",
        "resourceRef",
        "resourceEnvRef",
        "messageDestinationRef",
        "persistenceContextRef",
        "persistenceUnitRef",
        "postConstruct",
        "preDestroy",
        "postActivate",
        "prePassivate"
        })
public class InterceptorType {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "interceptor-class", required = true)
    protected String interceptorClass;
    @XmlElement(name = "around-invoke", required = true)
    protected List<AroundInvokeType> aroundInvoke;
    @XmlElement(name = "env-entry", required = true)
    protected List<EnvEntryType> envEntry;
    @XmlElement(name = "ejb-ref", required = true)
    protected List<EjbRefType> ejbRef;
    @XmlElement(name = "ejb-local-ref", required = true)
    protected List<EjbLocalRefType> ejbLocalRef;
    @XmlElement(name = "service-ref", required = true)
    protected List<ServiceRefType> serviceRef;
    @XmlElement(name = "resource-ref", required = true)
    protected List<ResourceRefType> resourceRef;
    @XmlElement(name = "resource-env-ref", required = true)
    protected List<ResourceEnvRefType> resourceEnvRef;
    @XmlElement(name = "message-destination-ref", required = true)
    protected List<MessageDestinationRefType> messageDestinationRef;
    @XmlElement(name = "persistence-context-ref", required = true)
    protected List<PersistenceContextRefType> persistenceContextRef;
    @XmlElement(name = "persistence-unit-ref", required = true)
    protected List<PersistenceUnitRefType> persistenceUnitRef;
    @XmlElement(name = "post-construct", required = true)
    protected List<LifecycleCallbackType> postConstruct;
    @XmlElement(name = "pre-destroy", required = true)
    protected List<LifecycleCallbackType> preDestroy;
    @XmlElement(name = "post-activate", required = true)
    protected List<LifecycleCallbackType> postActivate;
    @XmlElement(name = "pre-passivate", required = true)
    protected List<LifecycleCallbackType> prePassivate;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDescription().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public String getInterceptorClass() {
        return interceptorClass;
    }

    public void setInterceptorClass(String value) {
        this.interceptorClass = value;
    }

    /**
     * Gets the value of the aroundInvoke property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aroundInvoke property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getAroundInvoke().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link AroundInvokeType }
     */
    public List<AroundInvokeType> getAroundInvoke() {
        if (aroundInvoke == null) {
            aroundInvoke = new ArrayList<AroundInvokeType>();
        }
        return this.aroundInvoke;
    }

    /**
     * Gets the value of the envEntry property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the envEntry property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getEnvEntry().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link EnvEntryType }
     */
    public List<EnvEntryType> getEnvEntry() {
        if (envEntry == null) {
            envEntry = new ArrayList<EnvEntryType>();
        }
        return this.envEntry;
    }

    /**
     * Gets the value of the ejbRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getEjbRef().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbRefType }
     */
    public List<EjbRefType> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRefType>();
        }
        return this.ejbRef;
    }

    /**
     * Gets the value of the ejbLocalRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbLocalRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getEjbLocalRef().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbLocalRefType }
     */
    public List<EjbLocalRefType> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new ArrayList<EjbLocalRefType>();
        }
        return this.ejbLocalRef;
    }

    /**
     * Gets the value of the serviceRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getServiceRef().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceRefType }
     */
    public List<ServiceRefType> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRefType>();
        }
        return this.serviceRef;
    }

    /**
     * Gets the value of the resourceRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getResourceRef().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceRefType }
     */
    public List<ResourceRefType> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new ArrayList<ResourceRefType>();
        }
        return this.resourceRef;
    }

    /**
     * Gets the value of the resourceEnvRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceEnvRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getResourceEnvRef().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceEnvRefType }
     */
    public List<ResourceEnvRefType> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new ArrayList<ResourceEnvRefType>();
        }
        return this.resourceEnvRef;
    }

    /**
     * Gets the value of the messageDestinationRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageDestinationRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getMessageDestinationRef().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link MessageDestinationRefType }
     */
    public List<MessageDestinationRefType> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new ArrayList<MessageDestinationRefType>();
        }
        return this.messageDestinationRef;
    }

    /**
     * Gets the value of the persistenceContextRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the persistenceContextRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getPersistenceContextRef().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link PersistenceContextRefType }
     */
    public List<PersistenceContextRefType> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new ArrayList<PersistenceContextRefType>();
        }
        return this.persistenceContextRef;
    }

    /**
     * Gets the value of the persistenceUnitRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the persistenceUnitRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getPersistenceUnitRef().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link PersistenceUnitRefType }
     */
    public List<PersistenceUnitRefType> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new ArrayList<PersistenceUnitRefType>();
        }
        return this.persistenceUnitRef;
    }

    /**
     * Gets the value of the postConstruct property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the postConstruct property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getPostConstruct().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link LifecycleCallbackType }
     */
    public List<LifecycleCallbackType> getPostConstruct() {
        if (postConstruct == null) {
            postConstruct = new ArrayList<LifecycleCallbackType>();
        }
        return this.postConstruct;
    }

    /**
     * Gets the value of the preDestroy property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the preDestroy property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getPreDestroy().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link LifecycleCallbackType }
     */
    public List<LifecycleCallbackType> getPreDestroy() {
        if (preDestroy == null) {
            preDestroy = new ArrayList<LifecycleCallbackType>();
        }
        return this.preDestroy;
    }

    /**
     * Gets the value of the postActivate property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the postActivate property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getPostActivate().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link LifecycleCallbackType }
     */
    public List<LifecycleCallbackType> getPostActivate() {
        if (postActivate == null) {
            postActivate = new ArrayList<LifecycleCallbackType>();
        }
        return this.postActivate;
    }

    /**
     * Gets the value of the prePassivate property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the prePassivate property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getPrePassivate().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link LifecycleCallbackType }
     */
    public List<LifecycleCallbackType> getPrePassivate() {
        if (prePassivate == null) {
            prePassivate = new ArrayList<LifecycleCallbackType>();
        }
        return this.prePassivate;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
