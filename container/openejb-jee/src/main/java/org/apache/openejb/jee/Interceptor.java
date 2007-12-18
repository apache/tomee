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
import java.util.Collection;
import java.util.Map;


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
    protected KeyedCollection<String,EnvEntry> envEntry;
    @XmlElement(name = "ejb-ref", required = true)
    protected KeyedCollection<String,EjbRef> ejbRef;
    @XmlElement(name = "ejb-local-ref", required = true)
    protected KeyedCollection<String,EjbLocalRef> ejbLocalRef;
    @XmlElement(name = "service-ref", required = true)
    protected KeyedCollection<String,ServiceRef> serviceRef;
    @XmlElement(name = "resource-ref", required = true)
    protected KeyedCollection<String,ResourceRef> resourceRef;
    @XmlElement(name = "resource-env-ref", required = true)
    protected KeyedCollection<String,ResourceEnvRef> resourceEnvRef;
    @XmlElement(name = "message-destination-ref", required = true)
    protected KeyedCollection<String,MessageDestinationRef> messageDestinationRef;
    @XmlElement(name = "persistence-context-ref", required = true)
    protected KeyedCollection<String,PersistenceContextRef> persistenceContextRef;
    @XmlElement(name = "persistence-unit-ref", required = true)
    protected KeyedCollection<String,PersistenceUnitRef> persistenceUnitRef;
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

    public Interceptor(Class<?> interceptorClass) {
        this.interceptorClass = interceptorClass.getName();
    }

    public Interceptor(String interceptorClass) {
        this.interceptorClass = interceptorClass;
    }

    public String getJndiConsumerName() {
        if (interceptorClass == null) {
            return null;
        }
        return interceptorClass.replaceAll(".*\\.","");
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

    public Collection<EnvEntry> getEnvEntry() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<String,EnvEntry>();
        }
        return this.envEntry;
    }

    public Map<String,EnvEntry> getEnvEntryMap() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<String,EnvEntry>();
        }
        return this.envEntry.toMap();
    }

    public Collection<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<String,EjbRef>();
        }
        return this.ejbRef;
    }

    public Map<String,EjbRef> getEjbRefMap() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<String,EjbRef>();
        }
        return this.ejbRef.toMap();
    }

    public Collection<EjbLocalRef> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<String,EjbLocalRef>();
        }
        return this.ejbLocalRef;
    }

    public Map<String,EjbLocalRef> getEjbLocalRefMap() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<String,EjbLocalRef>();
        }
        return this.ejbLocalRef.toMap();
    }

    public Collection<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<String,ServiceRef>();
        }
        return this.serviceRef;
    }

    public Map<String,ServiceRef> getServiceRefMap() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<String,ServiceRef>();
        }
        return this.serviceRef.toMap();
    }

    public Collection<ResourceRef> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<String,ResourceRef>();
        }
        return this.resourceRef;
    }

    public Map<String,ResourceRef> getResourceRefMap() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<String,ResourceRef>();
        }
        return this.resourceRef.toMap();
    }

    public Collection<ResourceEnvRef> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<String,ResourceEnvRef>();
        }
        return this.resourceEnvRef;
    }

    public Map<String,ResourceEnvRef> getResourceEnvRefMap() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<String,ResourceEnvRef>();
        }
        return this.resourceEnvRef.toMap();
    }

    public Collection<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<String,MessageDestinationRef>();
        }
        return this.messageDestinationRef;
    }

    public Map<String,MessageDestinationRef> getMessageDestinationRefMap() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<String,MessageDestinationRef>();
        }
        return this.messageDestinationRef.toMap();
    }

    public Collection<PersistenceContextRef> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<String,PersistenceContextRef>();
        }
        return this.persistenceContextRef;
    }

    public Map<String,PersistenceContextRef> getPersistenceContextRefMap() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<String,PersistenceContextRef>();
        }
        return this.persistenceContextRef.toMap();
    }

    public Collection<PersistenceUnitRef> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<String,PersistenceUnitRef>();
        }
        return this.persistenceUnitRef;
    }

    public Map<String,PersistenceUnitRef> getPersistenceUnitRefMap() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<String,PersistenceUnitRef>();
        }
        return this.persistenceUnitRef.toMap();
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

    public List<InitMethod> getInitMethod() {
        return new ArrayList<InitMethod>();
    }

    public List<RemoveMethod> getRemoveMethod() {
        return new ArrayList<RemoveMethod>();
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
