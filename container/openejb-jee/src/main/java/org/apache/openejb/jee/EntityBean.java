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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;


/**
 * The entity-beanType declares an entity bean. The declaration
 * consists of:
 * <p/>
 * - an optional description
 * - an optional display name
 * - an optional icon element that contains a small and a large
 * icon file name
 * - a unique name assigned to the enterprise bean
 * in the deployment descriptor
 * - an optional mapped-name element that can be used to provide
 * vendor-specific deployment information such as the physical
 * jndi-name of the entity bean's remote home interface. This
 * element is not required to be supported by all implementations.
 * Any use of this element is non-portable.
 * - the names of the entity bean's remote home
 * and remote interfaces, if any
 * - the names of the entity bean's local home and local
 * interfaces, if any
 * - the entity bean's implementation class
 * - the optional entity bean's persistence management type. If
 * this element is not specified it is defaulted to Container.
 * - the entity bean's primary key class name
 * - an indication of the entity bean's reentrancy
 * - an optional specification of the
 * entity bean's cmp-version
 * - an optional specification of the entity bean's
 * abstract schema name
 * - an optional list of container-managed fields
 * - an optional specification of the primary key
 * field
 * - an optional declaration of the bean's environment
 * entries
 * - an optional declaration of the bean's EJB
 * references
 * - an optional declaration of the bean's local
 * EJB references
 * - an optional declaration of the bean's web
 * service references
 * - an optional declaration of the security role
 * references
 * - an optional declaration of the security identity
 * to be used for the execution of the bean's methods
 * - an optional declaration of the bean's
 * resource manager connection factory references
 * - an optional declaration of the bean's
 * resource environment references
 * - an optional declaration of the bean's message
 * destination references
 * - an optional set of query declarations
 * for finder and select methods for an entity
 * bean with cmp-version 2.x.
 * <p/>
 * The optional abstract-schema-name element must be specified
 * for an entity bean with container-managed persistence and
 * cmp-version 2.x.
 * <p/>
 * The optional primkey-field may be present in the descriptor
 * if the entity's persistence-type is Container.
 * <p/>
 * The optional cmp-version element may be present in the
 * descriptor if the entity's persistence-type is Container. If
 * the persistence-type is Container and the cmp-version
 * element is not specified, its value defaults to 2.x.
 * <p/>
 * The optional home and remote elements must be specified if
 * the entity bean cmp-version is 1.x.
 * <p/>
 * The optional home and remote elements must be specified if
 * the entity bean has a remote home and remote interface.
 * <p/>
 * The optional local-home and local elements must be specified
 * if the entity bean has a local home and local interface.
 * <p/>
 * Either both the local-home and the local elements or both
 * the home and the remote elements must be specified.
 * <p/>
 * The optional query elements must be present if the
 * persistence-type is Container and the cmp-version is 2.x and
 * query methods other than findByPrimaryKey have been defined
 * for the entity bean.
 * <p/>
 * The other elements that are optional are "optional" in the
 * sense that they are omitted if the lists represented by them
 * are empty.
 * <p/>
 * At least one cmp-field element must be present in the
 * descriptor if the entity's persistence-type is Container and
 * the cmp-version is 1.x, and none must not be present if the
 * entity's persistence-type is Bean.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity-beanType", propOrder = {
        "descriptions",
        "displayNames",
        "icons",
        "ejbName",
        "mappedName",
        "home",
        "remote",
        "localHome",
        "local",
        "ejbClass",
        "persistenceType",
        "primKeyClass",
        "reentrant",
        "cmpVersion",
        "abstractSchemaName",
        "cmpField",
        "primkeyField",
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
        "securityRoleRef",
        "securityIdentity",
        "query"
        })
public class EntityBean implements EnterpriseBean, RemoteBean {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlTransient
    protected LocalList<String,Icon> icon = new LocalList<String,Icon>(Icon.class);

    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    protected String home;
    protected String remote;
    @XmlElement(name = "local-home")
    protected String localHome;
    protected String local;
    @XmlElement(name = "ejb-class", required = true)
    protected String ejbClass;
    @XmlElement(name = "persistence-type", required = true)
    protected PersistenceType persistenceType;
    @XmlElement(name = "prim-key-class", required = true)
    protected String primKeyClass;
    @XmlElement(required = true)
    protected boolean reentrant;
    @XmlElement(name = "cmp-version")
    protected CmpVersion cmpVersion;
    @XmlElement(name = "abstract-schema-name")
    protected String abstractSchemaName;
    @XmlElement(name = "cmp-field", required = true)
    protected List<CmpField> cmpField;
    @XmlElement(name = "primkey-field")
    protected String primkeyField;
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
    @XmlElement(name = "security-role-ref", required = true)
    protected List<SecurityRoleRef> securityRoleRef;
    @XmlElement(name = "security-identity")
    protected SecurityIdentity securityIdentity;
    @XmlElement(required = true)
    protected List<Query> query;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

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

    @XmlElement(name = "icon", required = true)
    public Icon[] getIcons() {
        return icon.toArray();
    }

    public void setIcons(Icon[] text) {
        icon.set(text);
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

    public String getEjbClass() {
        return ejbClass;
    }

    public void setEjbClass(String value) {
        this.ejbClass = value;
    }

    public PersistenceType getPersistenceType() {
        return persistenceType;
    }

    public void setPersistenceType(PersistenceType value) {
        this.persistenceType = value;
    }

    public String getPrimKeyClass() {
        return primKeyClass;
    }

    public void setPrimKeyClass(String value) {
        this.primKeyClass = value;
    }

    public boolean getReentrant() {
        return reentrant;
    }

    public void setReentrant(boolean value) {
        this.reentrant = value;
    }

    public CmpVersion getCmpVersion() {
        return cmpVersion;
    }

    public void setCmpVersion(CmpVersion value) {
        this.cmpVersion = value;
    }

    public String getAbstractSchemaName() {
        return abstractSchemaName;
    }

    public void setAbstractSchemaName(String value) {
        this.abstractSchemaName = value;
    }

    public List<CmpField> getCmpField() {
        if (cmpField == null) {
            cmpField = new ArrayList<CmpField>();
        }
        return this.cmpField;
    }

    public String getPrimkeyField() {
        return primkeyField;
    }

    public void setPrimkeyField(String value) {
        this.primkeyField = value;
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

    public List<Query> getQuery() {
        if (query == null) {
            query = new ArrayList<Query>();
        }
        return this.query;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public List<AroundInvoke> getAroundInvoke() {
        return Collections.EMPTY_LIST;
    }


    public void addAroundInvoke(String method){
    }

    public TransactionType getTransactionType() {
        return TransactionType.CONTAINER;
    }

    public void setTransactionType(TransactionType type){
    }



}
