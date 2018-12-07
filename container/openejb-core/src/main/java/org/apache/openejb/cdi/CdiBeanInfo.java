/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.cdi;

import org.apache.openejb.Injection;
import org.apache.openejb.jee.DataSource;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.JMSConnectionFactory;
import org.apache.openejb.jee.JMSDestination;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.KeyedCollection;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.SecurityIdentity;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.ServiceRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CdiBeanInfo implements JndiConsumer {

    protected KeyedCollection<String, EnvEntry> envEntry;
    protected KeyedCollection<String, EjbRef> ejbRef;
    protected KeyedCollection<String, EjbLocalRef> ejbLocalRef;
    protected KeyedCollection<String, ServiceRef> serviceRef;
    protected KeyedCollection<String, ResourceRef> resourceRef;
    protected KeyedCollection<String, ResourceEnvRef> resourceEnvRef;
    protected KeyedCollection<String, MessageDestinationRef> messageDestinationRef;
    protected KeyedCollection<String, PersistenceContextRef> persistenceContextRef;
    protected KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef;
    protected List<LifecycleCallback> postConstruct;
    protected List<LifecycleCallback> preDestroy;
    protected KeyedCollection<String, DataSource> dataSource;
    protected KeyedCollection<String, JMSConnectionFactory> jmsConnectionFactories;
    protected KeyedCollection<String, JMSDestination> jmsDestinations;
    protected List<LifecycleCallback> postActivate;
    protected List<LifecycleCallback> prePassivate;
    protected List<SecurityRoleRef> securityRoleRef;
    protected SecurityIdentity securityIdentity;
    private String beanName;
    private ClassLoader classLoader;
    private List<Injection> injections;

    public String getBeanName() {
        return beanName;
    }

    public List<Injection> getInjections() {
        return injections;
    }


    public void setInjections(final List<Injection> injections) {
        this.injections = injections;
    }


    public void setBeanName(final String beanName) {
        this.beanName = beanName;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private List<LifecycleCallback> afterBegin;
    private List<LifecycleCallback> beforeCompletion;
    private List<LifecycleCallback> afterCompletion;
    private Class<?> beanClass;

    public void setBeanClass(final Class<?> beanClass) {
        this.beanClass = beanClass;
    }


    public Collection<EnvEntry> getEnvEntry() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<>();
        }
        return this.envEntry;
    }

    public Map<String, EnvEntry> getEnvEntryMap() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<>();
        }
        return this.envEntry.toMap();
    }

    public Collection<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<>();
        }
        return this.ejbRef;
    }

    public Map<String, EjbRef> getEjbRefMap() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<>();
        }
        return this.ejbRef.toMap();
    }

    public Collection<EjbLocalRef> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<>();
        }
        return this.ejbLocalRef;
    }

    public Map<String, EjbLocalRef> getEjbLocalRefMap() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<>();
        }
        return this.ejbLocalRef.toMap();
    }

    public Collection<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<>();
        }
        return this.serviceRef;
    }

    public Map<String, ServiceRef> getServiceRefMap() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<>();
        }
        return this.serviceRef.toMap();
    }

    public Collection<ResourceRef> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<>();
        }
        return this.resourceRef;
    }

    public Map<String, ResourceRef> getResourceRefMap() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<>();
        }
        return this.resourceRef.toMap();
    }

    public Collection<ResourceEnvRef> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<>();
        }
        return this.resourceEnvRef;
    }

    public Map<String, ResourceEnvRef> getResourceEnvRefMap() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<>();
        }
        return this.resourceEnvRef.toMap();
    }

    public Collection<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<>();
        }
        return this.messageDestinationRef;
    }

    public Map<String, MessageDestinationRef> getMessageDestinationRefMap() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<>();
        }
        return this.messageDestinationRef.toMap();
    }

    public Collection<PersistenceContextRef> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<>();
        }
        return this.persistenceContextRef;
    }

    public Map<String, PersistenceContextRef> getPersistenceContextRefMap() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<>();
        }
        return this.persistenceContextRef.toMap();
    }

    public Collection<PersistenceUnitRef> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<>();
        }
        return this.persistenceUnitRef;
    }

    public Map<String, PersistenceUnitRef> getPersistenceUnitRefMap() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<>();
        }
        return this.persistenceUnitRef.toMap();
    }

    public List<LifecycleCallback> getPostConstruct() {
        if (postConstruct == null) {
            postConstruct = new ArrayList<>();
        }
        return this.postConstruct;
    }


    public List<SecurityRoleRef> getSecurityRoleRef() {
        if (securityRoleRef == null) {
            securityRoleRef = new ArrayList<>();
        }
        return this.securityRoleRef;
    }

    public SecurityIdentity getSecurityIdentity() {
        return securityIdentity;
    }

    public void setSecurityIdentity(final SecurityIdentity value) {
        this.securityIdentity = value;
    }


    public List<LifecycleCallback> getAfterBegin() {
        if (afterBegin == null) {
            afterBegin = new ArrayList<>();
        }
        return afterBegin;
    }

    public List<LifecycleCallback> getAfterCompletion() {
        if (afterCompletion == null) {
            afterCompletion = new ArrayList<>();
        }
        return this.afterCompletion;
    }

    public List<LifecycleCallback> getBeforeCompletion() {
        if (beforeCompletion == null) {
            beforeCompletion = new ArrayList<>();
        }
        return this.beforeCompletion;
    }

    public Collection<DataSource> getDataSource() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<>();
        }
        return this.dataSource;
    }

    public Map<String, DataSource> getDataSourceMap() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<>();
        }
        return this.dataSource.toMap();
    }

    @Override
    public Collection<JMSConnectionFactory> getJMSConnectionFactories() {
        return jmsConnectionFactories == null ? (jmsConnectionFactories = new KeyedCollection<>()) : jmsConnectionFactories;
    }

    @Override
    public Map<String, JMSConnectionFactory> getJMSConnectionFactoriesMap() {
        return KeyedCollection.class.cast(getJMSConnectionFactories()).toMap();
    }

    @Override
    public Collection<JMSDestination> getJMSDestination() {
        return jmsDestinations == null ? (jmsDestinations = new KeyedCollection<>()) : jmsDestinations;
    }

    @Override
    public Map<String, JMSDestination> getJMSDestinationMap() {
        return KeyedCollection.class.cast(getJMSDestination()).toMap();
    }

    public String getJndiConsumerName() {
        return beanName;
    }

    public Class<?> getBeanClass() {
        return this.beanClass;
    }
}
