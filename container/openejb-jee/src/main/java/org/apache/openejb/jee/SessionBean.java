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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for session-beanType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="session-beanType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/&gt;
 *         &lt;element name="mapped-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/&gt;
 *         &lt;element name="home" type="{http://java.sun.com/xml/ns/javaee}homeType" minOccurs="0"/&gt;
 *         &lt;element name="remote" type="{http://java.sun.com/xml/ns/javaee}remoteType" minOccurs="0"/&gt;
 *         &lt;element name="local-home" type="{http://java.sun.com/xml/ns/javaee}local-homeType" minOccurs="0"/&gt;
 *         &lt;element name="local" type="{http://java.sun.com/xml/ns/javaee}localType" minOccurs="0"/&gt;
 *         &lt;element name="business-local" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="business-remote" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="local-bean" type="{http://java.sun.com/xml/ns/javaee}emptyType" minOccurs="0"/&gt;
 *         &lt;element name="service-endpoint" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/&gt;
 *         &lt;element name="ejb-class" type="{http://java.sun.com/xml/ns/javaee}ejb-classType" minOccurs="0"/&gt;
 *         &lt;element name="session-type" type="{http://java.sun.com/xml/ns/javaee}session-typeType" minOccurs="0"/&gt;
 *         &lt;element name="stateful-timeout" type="{http://java.sun.com/xml/ns/javaee}stateful-timeoutType" minOccurs="0"/&gt;
 *         &lt;element name="timeout-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType" minOccurs="0"/&gt;
 *         &lt;element name="timer" type="{http://java.sun.com/xml/ns/javaee}timerType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="init-on-startup" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="concurrency-management-type" type="{http://java.sun.com/xml/ns/javaee}concurrency-management-typeType" minOccurs="0"/&gt;
 *         &lt;element name="concurrent-method" type="{http://java.sun.com/xml/ns/javaee}concurrent-methodType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="depends-on" type="{http://java.sun.com/xml/ns/javaee}depends-onType" minOccurs="0"/&gt;
 *         &lt;element name="init-method" type="{http://java.sun.com/xml/ns/javaee}init-methodType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="remove-method" type="{http://java.sun.com/xml/ns/javaee}remove-methodType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="async-method" type="{http://java.sun.com/xml/ns/javaee}async-methodType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="transaction-type" type="{http://java.sun.com/xml/ns/javaee}transaction-typeType" minOccurs="0"/&gt;
 *         &lt;element name="after-begin-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType" minOccurs="0"/&gt;
 *         &lt;element name="before-completion-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType" minOccurs="0"/&gt;
 *         &lt;element name="after-completion-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType" minOccurs="0"/&gt;
 *         &lt;element name="around-invoke" type="{http://java.sun.com/xml/ns/javaee}around-invokeType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="around-timeout" type="{http://java.sun.com/xml/ns/javaee}around-timeoutType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}jndiEnvironmentRefsGroup"/&gt;
 *         &lt;element name="post-activate" type="{http://java.sun.com/xml/ns/javaee}lifecycle-callbackType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="pre-passivate" type="{http://java.sun.com/xml/ns/javaee}lifecycle-callbackType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="security-role-ref" type="{http://java.sun.com/xml/ns/javaee}security-role-refType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="security-identity" type="{http://java.sun.com/xml/ns/javaee}security-identityType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "session-beanType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "ejbName",
    "mappedName",
    "home",
    "remote",
    "localHome",
    "local",
    "businessLocal",
    "businessRemote",
    "localBean",
    "serviceEndpoint",
    "ejbClass",
    "sessionType",
    "statefulTimeout",
    "timeoutMethod",
    "timer",
    "initOnStartup",
    "concurrencyManagementType",
    "concurrentMethod",
    "dependsOn",
    "initMethod",
    "removeMethod",
    "asyncMethod",
    "transactionType",
    "afterBeginMethod",
    "beforeCompletionMethod",
    "afterCompletionMethod",
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
    "jmsConnectionFactories",
    "jmsDestinations",
    "postActivate",
    "prePassivate",
    "securityRoleRef",
    "securityIdentity",
    "passivationCapable"
})
public class SessionBean implements RemoteBean, Session, TimerConsumer {
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();

    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    protected String home;
    protected String remote;
    @XmlElement(name = "local-home")
    protected String localHome;
    protected String local;
    @XmlElement(name = "business-local")
    protected LinkedHashSet<String> businessLocal;
    @XmlElement(name = "business-remote")
    protected LinkedHashSet<String> businessRemote;
    @XmlElement(name = "local-bean")
    protected Empty localBean;
    @XmlElement(name = "service-endpoint")
    protected String serviceEndpoint;
    @XmlElement(name = "ejb-class")
    protected String ejbClass;
    @XmlElement(name = "session-type")
    protected SessionType sessionType;
    @XmlElement(name = "stateful-timeout")
    protected Timeout statefulTimeout;
    @XmlElement(name = "timeout-method")
    protected NamedMethod timeoutMethod;
    @XmlElement(name = "timer")
    protected List<Timer> timer;
    @XmlElement(name = "init-on-startup")
    protected Boolean initOnStartup;
    @XmlElement(name = "passivation-capable")
    protected Boolean passivationCapable;
    @XmlElement(name = "concurrency-management-type")
    protected ConcurrencyManagementType concurrencyManagementType;
    @XmlElement(name = "concurrent-method")
    protected List<ConcurrentMethod> concurrentMethod;
    @XmlElementWrapper(name = "depends-on")
    @XmlElement(name = "ejb-name")
    protected List<String> dependsOn;
    @XmlElement(name = "init-method")
    protected List<InitMethod> initMethod;
    @XmlElement(name = "remove-method")
    protected List<RemoveMethod> removeMethod;
    @XmlElement(name = "async-method")
    protected List<AsyncMethod> asyncMethod;
    @XmlElement(name = "transaction-type")
    protected TransactionType transactionType;
    @XmlTransient
    protected NamedMethod afterBeginMethod;
    @XmlTransient
    protected NamedMethod beforeCompletionMethod;
    @XmlTransient
    protected NamedMethod afterCompletionMethod;
    @XmlTransient
    private Set<String> asynchronousClasses;
    @XmlElement(name = "around-invoke", required = true)
    protected List<AroundInvoke> aroundInvoke;
    @XmlElement(name = "around-timeout")
    protected List<AroundTimeout> aroundTimeout;
    @XmlElement(name = "env-entry", required = true)
    protected KeyedCollection<String, EnvEntry> envEntry;
    @XmlElement(name = "ejb-ref", required = true)
    protected KeyedCollection<String, EjbRef> ejbRef;
    @XmlElement(name = "ejb-local-ref", required = true)
    protected KeyedCollection<String, EjbLocalRef> ejbLocalRef;
    @XmlElement(name = "service-ref", required = true)
    protected KeyedCollection<String, ServiceRef> serviceRef;
    @XmlElement(name = "resource-ref", required = true)
    protected KeyedCollection<String, ResourceRef> resourceRef;
    @XmlElement(name = "resource-env-ref", required = true)
    protected KeyedCollection<String, ResourceEnvRef> resourceEnvRef;
    @XmlElement(name = "message-destination-ref", required = true)
    protected KeyedCollection<String, MessageDestinationRef> messageDestinationRef;
    @XmlElement(name = "persistence-context-ref", required = true)
    protected KeyedCollection<String, PersistenceContextRef> persistenceContextRef;
    @XmlElement(name = "persistence-unit-ref", required = true)
    protected KeyedCollection<String, PersistenceUnitRef> persistenceUnitRef;
    @XmlElement(name = "post-construct", required = true)
    protected List<LifecycleCallback> postConstruct;
    @XmlElement(name = "pre-destroy", required = true)
    protected List<LifecycleCallback> preDestroy;
    @XmlElement(name = "data-source")
    protected KeyedCollection<String, DataSource> dataSource;
    @XmlElement(name = "jms-connection-factory", required = true)
    protected KeyedCollection<String, JMSConnectionFactory> jmsConnectionFactories;
    @XmlElement(name = "jms-destination")
    protected KeyedCollection<String, JMSDestination> jmsDestinations;
    @XmlElement(name = "post-activate", required = true)
    protected List<LifecycleCallback> postActivate;
    @XmlElement(name = "pre-passivate", required = true)
    protected List<LifecycleCallback> prePassivate;
    @XmlElement(name = "security-role-ref", required = true)
    protected List<SecurityRoleRef> securityRoleRef;
    @XmlElement(name = "security-identity")
    protected SecurityIdentity securityIdentity;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlTransient
    private List<LifecycleCallback> afterBegin;
    @XmlTransient
    private List<LifecycleCallback> beforeCompletion;
    @XmlTransient
    private List<LifecycleCallback> afterCompletion;

    @XmlTransient
    private boolean restService = false;

    @XmlTransient
    private String proxy;

    @XmlTransient
    private final Collection<String> parents = new ArrayList<String>(); // always needed so initialize it early

    public SessionBean() {
    }

    public SessionBean(final String ejbName, final String ejbClass, final SessionType sessionType) {
        this.ejbName = ejbName;
        this.ejbClass = ejbClass;
        this.sessionType = sessionType;
    }

    public String getJndiConsumerName() {
        return ejbName;
    }

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

    public String getEjbName() {
        return ejbName;
    }

    /**
     * The ejb-nameType specifies an enterprise bean's name. It is
     * used by ejb-name elements. This name is assigned by the
     * ejb-jar file producer to name the enterprise bean in the
     * ejb-jar file's deployment descriptor. The name must be
     * unique among the names of the enterprise beans in the same
     * ejb-jar file.
     *
     * There is no architected relationship between the used
     * ejb-name in the deployment descriptor and the JNDI name that
     * the Deployer will assign to the enterprise bean's home.
     *
     * The name for an entity bean must conform to the lexical
     * rules for an NMTOKEN.
     *
     * Example:
     *
     * <ejb-name>EmployeeService</ejb-name>
     */
    public void setEjbName(final String value) {
        this.ejbName = value;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(final String value) {
        this.mappedName = value;
    }

    public String getHome() {
        return home;
    }

    public void setHome(final String value) {
        this.home = value;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(final String value) {
        this.remote = value;
    }

    public void setHomeAndRemote(final String home, final String remote) {
        this.remote = remote;
        this.home = home;
    }

    public void setHomeAndRemote(final Class<?> home, final Class<?> remote) {
        this.remote = remote.getName();
        this.home = home.getName();
    }

    public void setHomeAndLocal(final String localHome, final String local) {
        this.local = local;
        this.localHome = localHome;
    }

    public void setHomeAndLocal(final Class<?> localHome, final Class<?> local) {
        this.local = local.getName();
        this.localHome = localHome.getName();
    }

    public String getLocalHome() {
        return localHome;
    }

    public void setLocalHome(final String value) {
        this.localHome = value;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(final String value) {
        this.local = value;
    }

    public Collection<String> getBusinessLocal() {
        if (businessLocal == null) {
            businessLocal = new LinkedHashSet<String>();
        }
        return businessLocal;
    }

    public void addBusinessLocal(final String businessLocal) {
        if (businessLocal == null) return;
        getBusinessLocal().add(businessLocal);
    }

    public void addBusinessLocal(final Class businessLocal) {
        addBusinessLocal(businessLocal.getName());
    }

    public Collection<String> getBusinessRemote() {
        if (businessRemote == null) {
            businessRemote = new LinkedHashSet<String>();
        }
        return businessRemote;
    }

    public void addBusinessRemote(final String businessRemote) {
        if (businessRemote == null) return;
        getBusinessRemote().add(businessRemote);
    }

    public void addBusinessRemote(final Class businessRemote) {
        addBusinessRemote(businessRemote.getName());
    }

    public Empty getLocalBean() {
        return localBean;
    }

    public void setLocalBean(final Empty localBean) {
        this.localBean = localBean;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(final String value) {
        this.serviceEndpoint = value;
    }

    public String getEjbClass() {
        return ejbClass;
    }

    public void setEjbClass(final String value) {
        this.ejbClass = value;
    }

    public void setEjbClass(final Class value) {
        this.ejbClass = value.getName();
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(final SessionType value) {
        this.sessionType = value;
    }

    public NamedMethod getTimeoutMethod() {
        return timeoutMethod;
    }

    public void setTimeoutMethod(final NamedMethod value) {
        this.timeoutMethod = value;
    }

    public List<InitMethod> getInitMethod() {
        if (initMethod == null) {
            initMethod = new ArrayList<InitMethod>();
        }
        return this.initMethod;
    }

    public List<RemoveMethod> getRemoveMethod() {
        if (removeMethod == null) {
            removeMethod = new ArrayList<RemoveMethod>();
        }
        return this.removeMethod;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

//    public ConcurrencyType getConcurrencyType() {
//        return concurrencyType;
//    }

    public void setTransactionType(final TransactionType value) {
        this.transactionType = value;
    }

//    public void setConcurrencyType(ConcurrencyType value) {
//        this.concurrencyType = value;
//    }

    public NamedMethod getAfterBeginMethod() {
        return afterBeginMethod;
    }

    @XmlElement(name = "after-begin-method")
    public void setAfterBeginMethod(final NamedMethod afterBeginMethod) {
        this.afterBeginMethod = afterBeginMethod;
        getAfterBegin().clear();
        getAfterBegin().add(new LifecycleCallback(afterBeginMethod));
    }

    public NamedMethod getBeforeCompletionMethod() {
        return beforeCompletionMethod;
    }

    @XmlElement(name = "before-completion-method")
    public void setBeforeCompletionMethod(final NamedMethod beforeCompletionMethod) {
        this.beforeCompletionMethod = beforeCompletionMethod;
        getBeforeCompletion().clear();
        getBeforeCompletion().add(new LifecycleCallback(beforeCompletionMethod));
    }

    public NamedMethod getAfterCompletionMethod() {
        return afterCompletionMethod;
    }

    @XmlElement(name = "after-completion-method")
    public void setAfterCompletionMethod(final NamedMethod afterCompletionMethod) {
        this.afterCompletionMethod = afterCompletionMethod;
        getAfterCompletion().clear();
        getAfterCompletion().add(new LifecycleCallback(afterCompletionMethod));
    }

    public List<AroundInvoke> getAroundInvoke() {
        if (aroundInvoke == null) {
            aroundInvoke = new ArrayList<AroundInvoke>();
        }
        return this.aroundInvoke;
    }

    public void addAroundInvoke(final String method) {
        assert ejbClass != null : "Set the ejbClass before calling this method";
        getAroundInvoke().add(new AroundInvoke(ejbClass, method));
    }

    public Collection<EnvEntry> getEnvEntry() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<String, EnvEntry>();
        }
        return this.envEntry;
    }

    public Map<String, EnvEntry> getEnvEntryMap() {
        if (envEntry == null) {
            envEntry = new KeyedCollection<String, EnvEntry>();
        }
        return this.envEntry.toMap();
    }

    public Collection<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<String, EjbRef>();
        }
        return this.ejbRef;
    }

    public Map<String, EjbRef> getEjbRefMap() {
        if (ejbRef == null) {
            ejbRef = new KeyedCollection<String, EjbRef>();
        }
        return this.ejbRef.toMap();
    }

    public Collection<EjbLocalRef> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<String, EjbLocalRef>();
        }
        return this.ejbLocalRef;
    }

    public Map<String, EjbLocalRef> getEjbLocalRefMap() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new KeyedCollection<String, EjbLocalRef>();
        }
        return this.ejbLocalRef.toMap();
    }

    public Collection<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<String, ServiceRef>();
        }
        return this.serviceRef;
    }

    public Map<String, ServiceRef> getServiceRefMap() {
        if (serviceRef == null) {
            serviceRef = new KeyedCollection<String, ServiceRef>();
        }
        return this.serviceRef.toMap();
    }

    public Collection<ResourceRef> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<String, ResourceRef>();
        }
        return this.resourceRef;
    }

    public Map<String, ResourceRef> getResourceRefMap() {
        if (resourceRef == null) {
            resourceRef = new KeyedCollection<String, ResourceRef>();
        }
        return this.resourceRef.toMap();
    }

    public Collection<ResourceEnvRef> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<String, ResourceEnvRef>();
        }
        return this.resourceEnvRef;
    }

    public Map<String, ResourceEnvRef> getResourceEnvRefMap() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new KeyedCollection<String, ResourceEnvRef>();
        }
        return this.resourceEnvRef.toMap();
    }

    public Collection<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<String, MessageDestinationRef>();
        }
        return this.messageDestinationRef;
    }

    public Map<String, MessageDestinationRef> getMessageDestinationRefMap() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new KeyedCollection<String, MessageDestinationRef>();
        }
        return this.messageDestinationRef.toMap();
    }

    public Collection<PersistenceContextRef> getPersistenceContextRef() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<String, PersistenceContextRef>();
        }
        return this.persistenceContextRef;
    }

    public Map<String, PersistenceContextRef> getPersistenceContextRefMap() {
        if (persistenceContextRef == null) {
            persistenceContextRef = new KeyedCollection<String, PersistenceContextRef>();
        }
        return this.persistenceContextRef.toMap();
    }

    public Collection<PersistenceUnitRef> getPersistenceUnitRef() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<String, PersistenceUnitRef>();
        }
        return this.persistenceUnitRef;
    }

    public Map<String, PersistenceUnitRef> getPersistenceUnitRefMap() {
        if (persistenceUnitRef == null) {
            persistenceUnitRef = new KeyedCollection<String, PersistenceUnitRef>();
        }
        return this.persistenceUnitRef.toMap();
    }

    public List<LifecycleCallback> getPostConstruct() {
        if (postConstruct == null) {
            postConstruct = new ArrayList<LifecycleCallback>();
        }
        return this.postConstruct;
    }

    public void addPostConstruct(final String method) {
        assert ejbClass != null : "Set the ejbClass before calling this method";
        getPostConstruct().add(new LifecycleCallback(ejbClass, method));
    }

    public List<LifecycleCallback> getPreDestroy() {
        if (preDestroy == null) {
            preDestroy = new ArrayList<LifecycleCallback>();
        }
        return this.preDestroy;
    }

    public void addPreDestroy(final String method) {
        assert ejbClass != null : "Set the ejbClass before calling this method";
        getPreDestroy().add(new LifecycleCallback(ejbClass, method));
    }

    public List<LifecycleCallback> getPostActivate() {
        if (postActivate == null) {
            postActivate = new ArrayList<LifecycleCallback>();
        }
        return this.postActivate;
    }

    public void addPostActivate(final String method) {
        assert ejbClass != null : "Set the ejbClass before calling this method";
        getPostActivate().add(new LifecycleCallback(ejbClass, method));
    }

    public List<LifecycleCallback> getPrePassivate() {
        if (prePassivate == null) {
            prePassivate = new ArrayList<LifecycleCallback>();
        }
        return this.prePassivate;
    }

    public void addPrePassivate(final String method) {
        assert ejbClass != null : "Set the ejbClass before calling this method";
        getPrePassivate().add(new LifecycleCallback(ejbClass, method));
    }


    public List<SecurityRoleRef> getSecurityRoleRef() {
        if (securityRoleRef == null) {
            securityRoleRef = new ArrayList<SecurityRoleRef>();
        }
        return this.securityRoleRef;
    }

    public SecurityIdentity getSecurityIdentity() {
        return securityIdentity;
    }

    public void setSecurityIdentity(final SecurityIdentity value) {
        this.securityIdentity = value;
    }

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(final String... ejbNames) {
        setDependsOn(Arrays.asList(ejbNames));
    }

    public void setDependsOn(final List<String> ejbNames) {
        this.dependsOn = new ArrayList(ejbNames);
    }

    public boolean hasInitOnStartup() {
        return initOnStartup != null;
    }

    public boolean getInitOnStartup() {
        return initOnStartup != null && initOnStartup;
    }

    public void setInitOnStartup(final boolean initOnStartup) {
        this.initOnStartup = initOnStartup;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

    public Timeout getStatefulTimeout() {
        return statefulTimeout;
    }

    public void setStatefulTimeout(final Timeout statefulTimeout) {
        this.statefulTimeout = statefulTimeout;
    }

    public List<AroundTimeout> getAroundTimeout() {
        if (aroundTimeout == null) {
            aroundTimeout = new ArrayList<AroundTimeout>();
        }
        return this.aroundTimeout;
    }

    public List<AsyncMethod> getAsyncMethod() {
        if (asyncMethod == null) {
            asyncMethod = new ArrayList<AsyncMethod>();
        }
        return this.asyncMethod;
    }

    public ConcurrencyManagementType getConcurrencyManagementType() {
        return concurrencyManagementType;
    }

    public void setConcurrencyManagementType(final ConcurrencyManagementType concurrencyManagementType) {
        this.concurrencyManagementType = concurrencyManagementType;
    }

    public List<ConcurrentMethod> getConcurrentMethod() {
        if (concurrentMethod == null) {
            concurrentMethod = new ArrayList<ConcurrentMethod>();
        }
        return this.concurrentMethod;
    }

    public void addAfterBegin(final String method) {
        assert ejbClass != null : "Set the ejbClass before calling this method";
        getAfterBegin().add(new LifecycleCallback(ejbClass, method));
    }

    public void addAfterCompletion(final String method) {
        assert ejbClass != null : "Set the ejbClass before calling this method";
        getAfterCompletion().add(new LifecycleCallback(ejbClass, method));
    }

    public void addBeforeCompletion(final String method) {
        assert ejbClass != null : "Set the ejbClass before calling this method";
        getBeforeCompletion().add(new LifecycleCallback(ejbClass, method));
    }

    public List<LifecycleCallback> getAfterBegin() {
        if (afterBegin == null) {
            afterBegin = new ArrayList<LifecycleCallback>();
        }
        return afterBegin;
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

    public Collection<DataSource> getDataSource() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String, DataSource>();
        }
        return this.dataSource;
    }

    public Map<String, DataSource> getDataSourceMap() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String, DataSource>();
        }
        return this.dataSource.toMap();
    }


    public List<Timer> getTimer() {
        if (timer == null) {
            timer = new ArrayList<Timer>();
        }
        return this.timer;
    }

    public void addAroundTimeout(final String method) {
        assert ejbClass != null : "Set the ejbClass before calling this method";
        getAroundTimeout().add(new AroundTimeout(ejbClass, method));
    }

    public Set<String> getAsynchronousClasses() {
        if (asynchronousClasses == null) {
            asynchronousClasses = new HashSet<String>();
        }
        return asynchronousClasses;
    }

    public void addAsynchronousClass(final String cls) {
        getAsynchronousClasses().add(cls);
    }

    public String getTimerConsumerName() {
        return ejbName;
    }

    public boolean isRestService() {
        return restService;
    }

    public void setRestService(final boolean restService) {
        this.restService = restService;
    }

    public void setProxy(final String name) {
        proxy = name;
    }

    public String getProxy() {
        return proxy;
    }

    public Collection<String> getParents() {
        return parents;
    }

    public EnterpriseBean localBean() {
        setLocalBean(new Empty());
        return this;
    }

    public Boolean getPassivationCapable() {
        return passivationCapable;
    }

    public void setPassivationCapable(final Boolean passivationCapable) {
        this.passivationCapable = passivationCapable;
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
}
