/**
 *
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
public class Interceptor implements JndiConsumer, Session {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "interceptor-class", required = true)
    protected String interceptorClass;
    @XmlElement(name = "around-invoke", required = true)
    protected List<AroundInvoke> aroundInvoke;
    @XmlElement(name = "env-entry", required = true)
    protected List<EnvEntry> envEntry;
    @XmlElement(name = "ejb-ref", required = true)
    protected List<EjbRef> ejbRef;
    @XmlElement(name = "ejb-local-ref", required = true)
    protected List<EjbLocalRef> ejbLocalRef;
    @XmlElement(name = "service-ref", required = true)
    protected List<ServiceRef> serviceRef;
    @XmlElement(name = "resource-ref", required = true)
    protected List<ResourceRef> resourceRef;
    @XmlElement(name = "resource-env-ref", required = true)
    protected List<ResourceEnvRef> resourceEnvRef;
    @XmlElement(name = "message-destination-ref", required = true)
    protected List<MessageDestinationRef> messageDestinationRef;
    @XmlElement(name = "persistence-context-ref", required = true)
    protected List<PersistenceContextRef> persistenceContextRef;
    @XmlElement(name = "persistence-unit-ref", required = true)
    protected List<PersistenceUnitRef> persistenceUnitRef;
    @XmlElement(name = "post-construct", required = true)
    protected List<LifecycleCallback> postConstruct;
    @XmlElement(name = "pre-destroy", required = true)
    protected List<LifecycleCallback> preDestroy;
    @XmlElement(name = "post-activate", required = true)
    protected List<LifecycleCallback> postActivate;
    @XmlElement(name = "pre-passivate", required = true)
    protected List<LifecycleCallback> prePassivate;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public Interceptor() {
    }

    public Interceptor(String interceptorClass) {
        this.interceptorClass = interceptorClass;
    }

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

    public List<AroundInvoke> getAroundInvoke() {
        if (aroundInvoke == null) {
            aroundInvoke = new ArrayList<AroundInvoke>();
        }
        return this.aroundInvoke;
    }

    public void addAroundInvoke(String method){
        assert interceptorClass != null: "Set the interceptorClass before calling this method";
        getAroundInvoke().add(new AroundInvoke(interceptorClass, method));
    }

    public List<EnvEntry> getEnvEntry() {
        if (envEntry == null) {
            envEntry = new ArrayList<EnvEntry>();
        }
        return this.envEntry;
    }

    public List<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRef>();
        }
        return this.ejbRef;
    }

    public List<EjbLocalRef> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new ArrayList<EjbLocalRef>();
        }
        return this.ejbLocalRef;
    }

    public List<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRef>();
        }
        return this.serviceRef;
    }

    public List<ResourceRef> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new ArrayList<ResourceRef>();
        }
        return this.resourceRef;
    }

    public List<ResourceEnvRef> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new ArrayList<ResourceEnvRef>();
        }
        return this.resourceEnvRef;
    }

    public List<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new ArrayList<MessageDestinationRef>();
        }
        return this.messageDestinationRef;
    }

    public List<PersistenceContextRef> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new ArrayList<PersistenceContextRef>();
        }
        return this.persistenceContextRef;
    }

    public List<PersistenceUnitRef> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new ArrayList<PersistenceUnitRef>();
        }
        return this.persistenceUnitRef;
    }

    public List<LifecycleCallback> getPostConstruct() {
        if (postConstruct == null) {
            postConstruct = new ArrayList<LifecycleCallback>();
        }
        return this.postConstruct;
    }

    public void addPostConstruct(String method){
        assert interceptorClass != null: "Set the interceptorClass before calling this method";
        getPostConstruct().add(new LifecycleCallback(interceptorClass, method));
    }

    public List<LifecycleCallback> getPreDestroy() {
        if (preDestroy == null) {
            preDestroy = new ArrayList<LifecycleCallback>();
        }
        return this.preDestroy;
    }

    public void addPreDestroy(String method){
        assert interceptorClass != null: "Set the interceptorClass before calling this method";
        getPreDestroy().add(new LifecycleCallback(interceptorClass, method));
    }

    public List<LifecycleCallback> getPostActivate() {
        if (postActivate == null) {
            postActivate = new ArrayList<LifecycleCallback>();
        }
        return this.postActivate;
    }

    public void addPostActivate(String method){
        assert interceptorClass != null: "Set the interceptorClass before calling this method";
        getPostActivate().add(new LifecycleCallback(interceptorClass, method));
    }

    public List<LifecycleCallback> getPrePassivate() {
        if (prePassivate == null) {
            prePassivate = new ArrayList<LifecycleCallback>();
        }
        return this.prePassivate;
    }

    public void addPrePassivate(String method){
        assert interceptorClass != null: "Set the interceptorClass before calling this method";
        getPrePassivate().add(new LifecycleCallback(interceptorClass, method));
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
