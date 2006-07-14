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
        "description",
        "displayName",
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
        "securityRoleRef",
        "securityIdentity",
        "query"
        })
public class EntityBeanType implements EnterpriseBean {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "display-name", required = true)
    protected List<Text> displayName;
    @XmlElement(required = true)
    protected List<IconType> icon;
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
    protected PersistenceTypeType persistenceType;
    @XmlElement(name = "prim-key-class", required = true)
    protected String primKeyClass;
    @XmlElement(required = true)
    protected boolean reentrant;
    @XmlElement(name = "cmp-version")
    protected CmpVersionType cmpVersion;
    @XmlElement(name = "abstract-schema-name")
    protected String abstractSchemaName;
    @XmlElement(name = "cmp-field", required = true)
    protected List<CmpFieldType> cmpField;
    @XmlElement(name = "primkey-field")
    protected String primkeyField;
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
    @XmlElement(name = "security-role-ref", required = true)
    protected List<SecurityRoleRefType> securityRoleRef;
    @XmlElement(name = "security-identity")
    protected SecurityIdentityType securityIdentity;
    @XmlElement(required = true)
    protected List<QueryType> query;
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

    /**
     * Gets the value of the displayName property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the displayName property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDisplayName().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDisplayName() {
        if (displayName == null) {
            displayName = new ArrayList<Text>();
        }
        return this.displayName;
    }

    /**
     * Gets the value of the icon property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the icon property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getIcon().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link IconType }
     */
    public List<IconType> getIcon() {
        if (icon == null) {
            icon = new ArrayList<IconType>();
        }
        return this.icon;
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

    public PersistenceTypeType getPersistenceType() {
        return persistenceType;
    }

    public void setPersistenceType(PersistenceTypeType value) {
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

    public CmpVersionType getCmpVersion() {
        return cmpVersion;
    }

    public void setCmpVersion(CmpVersionType value) {
        this.cmpVersion = value;
    }

    public String getAbstractSchemaName() {
        return abstractSchemaName;
    }

    public void setAbstractSchemaName(String value) {
        this.abstractSchemaName = value;
    }

    /**
     * Gets the value of the cmpField property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cmpField property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getCmpField().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link CmpFieldType }
     */
    public List<CmpFieldType> getCmpField() {
        if (cmpField == null) {
            cmpField = new ArrayList<CmpFieldType>();
        }
        return this.cmpField;
    }

    public String getPrimkeyField() {
        return primkeyField;
    }

    public void setPrimkeyField(String value) {
        this.primkeyField = value;
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
     * Gets the value of the securityRoleRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the securityRoleRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getSecurityRoleRef().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link SecurityRoleRefType }
     */
    public List<SecurityRoleRefType> getSecurityRoleRef() {
        if (securityRoleRef == null) {
            securityRoleRef = new ArrayList<SecurityRoleRefType>();
        }
        return this.securityRoleRef;
    }

    public SecurityIdentityType getSecurityIdentity() {
        return securityIdentity;
    }

    public void setSecurityIdentity(SecurityIdentityType value) {
        this.securityIdentity = value;
    }

    /**
     * Gets the value of the query property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the query property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getQuery().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link QueryType }
     */
    public List<QueryType> getQuery() {
        if (query == null) {
            query = new ArrayList<QueryType>();
        }
        return this.query;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
