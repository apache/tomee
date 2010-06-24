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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Map;

/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for interceptorType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="interceptorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="interceptor-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="around-invoke" type="{http://java.sun.com/xml/ns/javaee}around-invokeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="around-timeout" type="{http://java.sun.com/xml/ns/javaee}around-timeoutType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}jndiEnvironmentRefsGroup"/>
 *         &lt;element name="post-activate" type="{http://java.sun.com/xml/ns/javaee}lifecycle-callbackType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="pre-passivate" type="{http://java.sun.com/xml/ns/javaee}lifecycle-callbackType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "interceptorType", propOrder = {
        "descriptions",
        "interceptorClass",
        "aroundInvoke",
        "aroundTimeout",
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
        "dataSource",
        "postActivate",
        "prePassivate",
        "afterBegin",
        "beforeCompletion",
        "afterCompletion"
        })
public class Interceptor implements JndiConsumer, Session {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "interceptor-class", required = true)
    protected String interceptorClass;
    @XmlElement(name = "around-invoke", required = true)
    protected List<AroundInvoke> aroundInvoke;
    @XmlElement(name = "around-timeout")
    protected List<AroundTimeout> aroundTimeout;
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
    @XmlElement(name = "data-source", required = true)
    protected KeyedCollection<String,DataSource> dataSource;
    @XmlElement(name = "post-construct", required = true)
    protected List<LifecycleCallback> postConstruct;
    @XmlElement(name = "pre-destroy", required = true)
    protected List<LifecycleCallback> preDestroy;
    @XmlElement(name = "post-activate", required = true)
    protected List<LifecycleCallback> postActivate;
    @XmlElement(name = "pre-passivate", required = true)
    protected List<LifecycleCallback> prePassivate;
    @XmlElement(name = "after-begin", required = true)
    protected List<LifecycleCallback> afterBegin;
    @XmlElement(name = "before-completion", required = true)
    protected List<LifecycleCallback> beforeCompletion;
    @XmlElement(name = "after-completion", required = true)
    protected List<LifecycleCallback> afterCompletion;
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

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
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

    public List<AroundTimeout> getAroundTimeout() {
        if (aroundTimeout == null) {
            aroundTimeout = new ArrayList<AroundTimeout>();
        }
        return this.aroundTimeout;
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

    @Override
    public Collection<DataSource> getDataSource() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String,DataSource>();
        }
        return this.dataSource;
    }

    @Override
    public Map<String,DataSource> getDataSourceMap() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String,DataSource>();
        }
        return this.dataSource.toMap();
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

    public void addAfterBegin(String method) {
        assert interceptorClass != null : "Set the interceptorClass before calling this method";
        getAfterBegin().add(new LifecycleCallback(interceptorClass, method));
    }

    public void addAfterCompletion(String method) {
        assert interceptorClass != null : "Set the interceptorClass before calling this method";
        getAfterCompletion().add(new LifecycleCallback(interceptorClass, method));
    }

    public void addBeforeCompletion(String method) {
        assert interceptorClass != null : "Set the interceptorClass before calling this method";
        getBeforeCompletion().add(new LifecycleCallback(interceptorClass, method));
    }

    public List<LifecycleCallback> getAfterBegin() {
        if (afterBegin == null) {
            afterBegin = new ArrayList<LifecycleCallback>();
        }
        return this.afterBegin;
    }

    public List<LifecycleCallback> getAfterCompletion() {
        if (afterCompletion == null) {
            afterCompletion = new ArrayList<LifecycleCallback>();
        }
        return this.afterCompletion;
    }

    public List<LifecycleCallback> getBeforeCompletion() {
        if (beforeCompletion == null) {
            beforeCompletion = new ArrayList<LifecycleCallback>();
        }
        return this.beforeCompletion;
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

    public void addAroundTimeout(String method) {
        assert interceptorClass != null : "Set the interceptorClass before calling this method";
        getAroundTimeout().add(new AroundTimeout(interceptorClass, method));
    }
}
