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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Arrays;


/**
 * The session-beanType declares an session bean. The
 * declaration consists of:
 * <p/>
 * - an optional description
 * - an optional display name
 * - an optional icon element that contains a small and a large
 * icon file name
 * - a name assigned to the enterprise bean
 * in the deployment description
 * - an optional mapped-name element that can be used to provide
 * vendor-specific deployment information such as the physical
 * jndi-name of the session bean's remote home/business interface.
 * This element is not required to be supported by all
 * implementations. Any use of this element is non-portable.
 * - the names of all the remote or local business interfaces,
 * if any
 * - the names of the session bean's remote home and
 * remote interfaces, if any
 * - the names of the session bean's local home and
 * local interfaces, if any
 * - the name of the session bean's web service endpoint
 * interface, if any
 * - the session bean's implementation class
 * - the session bean's state management type
 * - an optional declaration of the session bean's timeout method.
 * - the optional session bean's transaction management type.
 * If it is not present, it is defaulted to Container.
 * - an optional list of the session bean class and/or
 * superclass around-invoke methods.
 * - an optional declaration of the bean's
 * environment entries
 * - an optional declaration of the bean's EJB references
 * - an optional declaration of the bean's local
 * EJB references
 * - an optional declaration of the bean's web
 * service references
 * - an optional declaration of the security role
 * references
 * - an optional declaration of the security identity
 * to be used for the execution of the bean's methods
 * - an optional declaration of the bean's resource
 * manager connection factory references
 * - an optional declaration of the bean's resource
 * environment references.
 * - an optional declaration of the bean's message
 * destination references
 * <p/>
 * The elements that are optional are "optional" in the sense
 * that they are omitted when if lists represented by them are
 * empty.
 * <p/>
 * Either both the local-home and the local elements or both
 * the home and the remote elements must be specified for the
 * session bean.
 * <p/>
 * The service-endpoint element may only be specified if the
 * bean is a stateless session bean.
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
        "loadOnStartup",
        "timeoutMethod",
        "initMethod",
        "removeMethod",
        "transactionType",
        "concurrencyType",
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
        "prePassivate",
        "securityRoleRef",
        "securityIdentity",
        "dependsOn"
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
    protected EmptyType localBean;
    @XmlElement(name = "service-endpoint")
    protected String serviceEndpoint;
    @XmlElement(name = "ejb-class")
    protected String ejbClass;
    @XmlElement(name = "session-type")
    protected SessionType sessionType = SessionType.STATELESS;
    @XmlElement(name = "timeout-method")
    protected NamedMethod timeoutMethod;
    @XmlElement(name = "init-method", required = true)
    protected List<InitMethod> initMethod;
    @XmlElement(name = "remove-method", required = true)
    protected List<RemoveMethod> removeMethod;
    @XmlElement(name = "transaction-type")
    protected TransactionType transactionType;
    @XmlElement(name = "concurrency-type")
    protected ConcurrencyType concurrencyType;
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
    @XmlElement(name = "security-role-ref", required = true)
    protected List<SecurityRoleRef> securityRoleRef;
    @XmlElement(name = "security-identity")
    protected SecurityIdentity securityIdentity;

    @XmlElement(name = "load-on-startup")
    protected Boolean loadOnStartup;

    @XmlElementWrapper(name = "depends-on")
    @XmlElement(name = "ejb-name")
    protected List<String> dependsOn;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public SessionBean() {
    }

    public SessionBean(String ejbName, String ejbClass, SessionType sessionType) {
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

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    public void setDisplayNames(Text[] text) {
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

    public Map<String,Icon> getIconMap() {
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
     * <p/>
     * There is no architected relationship between the used
     * ejb-name in the deployment descriptor and the JNDI name that
     * the Deployer will assign to the enterprise bean's home.
     * <p/>
     * The name for an entity bean must conform to the lexical
     * rules for an NMTOKEN.
     * <p/>
     * Example:
     * <p/>
     * <ejb-name>EmployeeService</ejb-name>
     */
    public void setEjbName(String value) {
        this.ejbName = value;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String value) {
        this.mappedName = value;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String value) {
        this.home = value;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String value) {
        this.remote = value;
    }

    public void setHomeAndRemote(String home, String remote) {
        this.remote = remote;
        this.home = home;
    }

    public void setHomeAndRemote(Class<?> home, Class<?> remote) {
        this.remote = remote.getName();
        this.home = home.getName();
    }

    public void setHomeAndLocal(String localHome, String local) {
        this.local = local;
        this.localHome = localHome;
    }

    public void setHomeAndLocal(Class<?> localHome, Class<?> local) {
        this.local = local.getName();
        this.localHome = localHome.getName();
    }

    public String getLocalHome() {
        return localHome;
    }

    public void setLocalHome(String value) {
        this.localHome = value;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String value) {
        this.local = value;
    }

    public Collection<String> getBusinessLocal() {
        if (businessLocal == null){
            businessLocal = new LinkedHashSet<String>();
        }
        return businessLocal;
    }

    public void addBusinessLocal(String businessLocal) {
        if (businessLocal == null) return;
        getBusinessLocal().add(businessLocal);
    }

    public void addBusinessLocal(Class businessLocal) {
        addBusinessLocal(businessLocal.getName());
    }

    public Collection<String> getBusinessRemote() {
        if (businessRemote == null){
            businessRemote = new LinkedHashSet<String>();
        }
        return businessRemote;
    }

    public void addBusinessRemote(String businessRemote) {
        if (businessRemote == null) return;
        getBusinessRemote().add(businessRemote);
    }

    public void addBusinessRemote(Class businessRemote) {
        addBusinessRemote(businessRemote.getName());
    }

    public EmptyType getLocalBean() {
        return localBean;
    }

    public void setLocalBean(EmptyType localBean) {
        this.localBean = localBean;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String value) {
        this.serviceEndpoint = value;
    }

    public String getEjbClass() {
        return ejbClass;
    }

    public void setEjbClass(String value) {
        this.ejbClass = value;
    }

    public void setEjbClass(Class value) {
        this.ejbClass = value.getName();
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType value) {
        this.sessionType = value;
    }

    public NamedMethod getTimeoutMethod() {
        return timeoutMethod;
    }

    public void setTimeoutMethod(NamedMethod value) {
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

    public ConcurrencyType getConcurrencyType() {
        return concurrencyType;
    }

    public void setTransactionType(TransactionType value) {
        this.transactionType = value;
    }

    public void setConcurrencyType(ConcurrencyType value) {
        this.concurrencyType = value;
    }

    public List<AroundInvoke> getAroundInvoke() {
        if (aroundInvoke == null) {
            aroundInvoke = new ArrayList<AroundInvoke>();
        }
        return this.aroundInvoke;
    }

    public void addAroundInvoke(String method){
        assert ejbClass != null: "Set the ejbClass before calling this method";
        getAroundInvoke().add(new AroundInvoke(ejbClass, method));
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
        assert ejbClass != null: "Set the ejbClass before calling this method";
        getPostConstruct().add(new LifecycleCallback(ejbClass, method));
    }

    public List<LifecycleCallback> getPreDestroy() {
        if (preDestroy == null) {
            preDestroy = new ArrayList<LifecycleCallback>();
        }
        return this.preDestroy;
    }

    public void addPreDestroy(String method){
        assert ejbClass != null: "Set the ejbClass before calling this method";
        getPreDestroy().add(new LifecycleCallback(ejbClass, method));
    }

    public List<LifecycleCallback> getPostActivate() {
        if (postActivate == null) {
            postActivate = new ArrayList<LifecycleCallback>();
        }
        return this.postActivate;
    }

    public void addPostActivate(String method){
        assert ejbClass != null: "Set the ejbClass before calling this method";
        getPostActivate().add(new LifecycleCallback(ejbClass, method));
    }

    public List<LifecycleCallback> getPrePassivate() {
        if (prePassivate == null) {
            prePassivate = new ArrayList<LifecycleCallback>();
        }
        return this.prePassivate;
    }

    public void addPrePassivate(String method){
        assert ejbClass != null: "Set the ejbClass before calling this method";
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

    public void setSecurityIdentity(SecurityIdentity value) {
        this.securityIdentity = value;
    }

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(String... ejbNames) {
        setDependsOn(Arrays.asList(ejbNames));
    }

    public void setDependsOn(List<String> ejbNames) {
        this.dependsOn = new ArrayList(ejbNames);
    }

    public boolean hasLoadOnStartup() {
        return loadOnStartup != null;
    }

    public boolean getLoadOnStartup() {
        return loadOnStartup != null && loadOnStartup;
    }

    public void setLoadOnStartup(boolean loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
