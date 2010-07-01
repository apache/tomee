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
import java.util.Set;
import java.util.Collection;
import java.util.Map;

/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for entity-beanType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="entity-beanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/>
 *         &lt;element name="mapped-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="home" type="{http://java.sun.com/xml/ns/javaee}homeType" minOccurs="0"/>
 *         &lt;element name="remote" type="{http://java.sun.com/xml/ns/javaee}remoteType" minOccurs="0"/>
 *         &lt;element name="local-home" type="{http://java.sun.com/xml/ns/javaee}local-homeType" minOccurs="0"/>
 *         &lt;element name="local" type="{http://java.sun.com/xml/ns/javaee}localType" minOccurs="0"/>
 *         &lt;element name="ejb-class" type="{http://java.sun.com/xml/ns/javaee}ejb-classType"/>
 *         &lt;element name="persistence-type" type="{http://java.sun.com/xml/ns/javaee}persistence-typeType"/>
 *         &lt;element name="prim-key-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="reentrant" type="{http://java.sun.com/xml/ns/javaee}true-falseType"/>
 *         &lt;element name="cmp-version" type="{http://java.sun.com/xml/ns/javaee}cmp-versionType" minOccurs="0"/>
 *         &lt;element name="abstract-schema-name" type="{http://java.sun.com/xml/ns/javaee}java-identifierType" minOccurs="0"/>
 *         &lt;element name="cmp-field" type="{http://java.sun.com/xml/ns/javaee}cmp-fieldType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="primkey-field" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}jndiEnvironmentRefsGroup"/>
 *         &lt;element name="security-role-ref" type="{http://java.sun.com/xml/ns/javaee}security-role-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-identity" type="{http://java.sun.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
 *         &lt;element name="query" type="{http://java.sun.com/xml/ns/javaee}queryType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "entity-beanType", propOrder = {
        "descriptions",
        "displayNames",
        "icon",
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
        "dataSource",
        "securityRoleRef",
        "securityIdentity",
        "query"
        })
public class EntityBean implements RemoteBean {

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
    @XmlElement(name = "ejb-class", required = true)
    protected String ejbClass;
    @XmlElement(name = "persistence-type", required = true)
    protected PersistenceType persistenceType;
    @XmlElement(name = "prim-key-class", required = true)
    protected String primKeyClass;
    @XmlJavaTypeAdapter(type = boolean.class, value = BooleanAdapter.class)
    @XmlElement(required = true)
    protected boolean reentrant;
    @XmlElement(name = "cmp-version", defaultValue = "2.x")
    protected CmpVersion cmpVersion;
    @XmlElement(name = "abstract-schema-name")
    protected String abstractSchemaName;
    @XmlElement(name = "cmp-field", required = true)
    protected List<CmpField> cmpField;
    @XmlElement(name = "primkey-field")
    protected String primkeyField;
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
    @XmlElement(name = "data-source")
    protected KeyedCollection<String,DataSource> dataSource;
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

    public EntityBean() {
        Set<String> publicIds = JaxbJavaee.currentPublicId.get();
        if (publicIds != null && publicIds.contains("-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN")) {
            cmpVersion = CmpVersion.CMP1;
        } else {
            cmpVersion = CmpVersion.CMP2;
        }
    }


    public EntityBean(String ejbName, String ejbClass, PersistenceType persistenceType) {
        this();
        this.ejbName = ejbName;
        this.ejbClass = ejbClass;
        this.persistenceType = persistenceType;
    }

    public EntityBean(Class<?> ejbClass, PersistenceType persistenceType) {
        this(ejbClass.getSimpleName(), ejbClass.getName(), persistenceType);
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

    public void setEjbClass(Class value) {
        this.ejbClass = value.getName();
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

    public void addCmpField(String fieldName) {
        if (fieldName == null) return;

        for (CmpField field : cmpField) {
            if (fieldName.equals(field.getFieldName())) return;
        }

        cmpField.add(new CmpField(fieldName));
    }

    public String getPrimkeyField() {
        return primkeyField;
    }

    public void setPrimkeyField(String value) {
        this.primkeyField = value;
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

    public Collection<DataSource> getDataSource() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String,DataSource>();
        }
        return this.dataSource;
    }

    public Map<String,DataSource> getDataSourceMap() {
        if (dataSource == null) {
            dataSource = new KeyedCollection<String,DataSource>();
        }
        return this.dataSource.toMap();
    }

    public List<SecurityRoleRef> getSecurityRoleRef() {
        if (securityRoleRef == null) {
            securityRoleRef = new ArrayList<SecurityRoleRef>();
        }
        return this.securityRoleRef;
    }

    public Collection<String> getBusinessLocal() {
        return Collections.emptySet();
    }

    public Collection<String> getBusinessRemote() {
        return Collections.emptySet();
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
        return Collections.emptyList();
    }


    public void addAroundInvoke(String method){
    }

    public TransactionType getTransactionType() {
        return TransactionType.CONTAINER;
    }

    public void setTransactionType(TransactionType type){
    }

    public void addAroundTimeout(String method) {
    }

    public List<AroundTimeout> getAroundTimeout() {
        return Collections.emptyList();
    }
}
