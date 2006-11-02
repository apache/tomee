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


/**
 * The message-driven element declares a message-driven
 * bean. The declaration consists of:
 * <p/>
 * - an optional description
 * - an optional display name
 * - an optional icon element that contains a small and a large
 * icon file name.
 * - a name assigned to the enterprise bean in
 * the deployment descriptor
 * - an optional mapped-name element that can be used to provide
 * vendor-specific deployment information such as the physical
 * jndi-name of destination from which this message-driven bean
 * should consume.  This element is not required to be supported
 * by all implementations.  Any use of this element is non-portable.
 * - the message-driven bean's implementation class
 * - an optional declaration of the bean's messaging
 * type
 * - an optional declaration of the bean's timeout method.
 * - the optional message-driven bean's transaction management
 * type. If it is not defined, it is defaulted to Container.
 * - an optional declaration of the bean's
 * message-destination-type
 * - an optional declaration of the bean's
 * message-destination-link
 * - an optional declaration of the message-driven bean's
 * activation configuration properties
 * - an optional list of the message-driven bean class and/or
 * superclass around-invoke methods.
 * - an optional declaration of the bean's environment
 * entries
 * - an optional declaration of the bean's EJB references
 * - an optional declaration of the bean's local EJB
 * references
 * - an optional declaration of the bean's web service
 * references
 * - an optional declaration of the security
 * identity to be used for the execution of the bean's
 * methods
 * - an optional declaration of the bean's
 * resource manager connection factory
 * references
 * - an optional declaration of the bean's resource
 * environment references.
 * - an optional declaration of the bean's message
 * destination references
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message-driven-beanType", propOrder = {
        "descriptions",
        "displayNames",
        "icons",
        "ejbName",
        "mappedName",
        "ejbClass",
        "messagingType",
        "timeoutMethod",
        "transactionType",
        "messageDestinationType",
        "messageDestinationLink",
        "activationConfig",
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
        "securityIdentity"
        })
public class MessageDrivenBean implements EnterpriseBean {

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
    @XmlElement(name = "ejb-class")
    protected String ejbClass;
    @XmlElement(name = "messaging-type")
    protected String messagingType;
    @XmlElement(name = "timeout-method")
    protected NamedMethod timeoutMethod;
    @XmlElement(name = "transaction-type")
    protected TransactionType transactionType;
    @XmlElement(name = "message-destination-type")
    protected String messageDestinationType;
    @XmlElement(name = "message-destination-link")
    protected String messageDestinationLink;
    @XmlElement(name = "activation-config")
    protected ActivationConfig activationConfig;
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
    @XmlElement(name = "security-identity")
    protected SecurityIdentity securityIdentity;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public MessageDrivenBean() {
    }

    public MessageDrivenBean(String ejbName) {
        this.ejbName = ejbName;
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

    public String getEjbClass() {
        return ejbClass;
    }

    public void setEjbClass(String value) {
        this.ejbClass = value;
    }

    public String getMessagingType() {
        return messagingType;
    }

    public void setMessagingType(String value) {
        this.messagingType = value;
    }

    public NamedMethod getTimeoutMethod() {
        return timeoutMethod;
    }

    public void setTimeoutMethod(NamedMethod value) {
        this.timeoutMethod = value;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType value) {
        this.transactionType = value;
    }

    public String getMessageDestinationType() {
        return messageDestinationType;
    }

    public void setMessageDestinationType(String value) {
        this.messageDestinationType = value;
    }

    /**
     * The Assembler sets the value to reflect the flow of messages
     * between producers and consumers in the application.
     * <p/>
     * The value must be the message-destination-name of a message
     * destination in the same Deployment File or in another
     * Deployment File in the same Java EE application unit.
     * <p/>
     * Alternatively, the value may be composed of a path name
     * specifying a Deployment File containing the referenced
     * message destination with the message-destination-name of the
     * destination appended and separated from the path name by
     * "#". The path name is relative to the Deployment File
     * containing Deployment Component that is referencing the
     * message destination.  This allows multiple message
     * destinations with the same name to be uniquely identified.
     */
    public String getMessageDestinationLink() {
        return messageDestinationLink;
    }

    public void setMessageDestinationLink(String value) {
        this.messageDestinationLink = value;
    }

    public ActivationConfig getActivationConfig() {
        return activationConfig;
    }

    public void setActivationConfig(ActivationConfig value) {
        this.activationConfig = value;
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


    public SecurityIdentity getSecurityIdentity() {
        return securityIdentity;
    }

    public void setSecurityIdentity(SecurityIdentity value) {
        this.securityIdentity = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
