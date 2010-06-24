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
import java.util.Collection;
import java.util.Map;


/**
 * ejb-jar_3_1.xsd
 * 
 * <p>Java class for message-driven-beanType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="message-driven-beanType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="ejb-name" type="{http://java.sun.com/xml/ns/javaee}ejb-nameType"/>
 *         &lt;element name="mapped-name" type="{http://java.sun.com/xml/ns/javaee}xsdStringType" minOccurs="0"/>
 *         &lt;element name="ejb-class" type="{http://java.sun.com/xml/ns/javaee}ejb-classType" minOccurs="0"/>
 *         &lt;element name="messaging-type" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="timeout-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType" minOccurs="0"/>
 *         &lt;element name="timer" type="{http://java.sun.com/xml/ns/javaee}timerType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="transaction-type" type="{http://java.sun.com/xml/ns/javaee}transaction-typeType" minOccurs="0"/>
 *         &lt;element name="message-destination-type" type="{http://java.sun.com/xml/ns/javaee}message-destination-typeType" minOccurs="0"/>
 *         &lt;element name="message-destination-link" type="{http://java.sun.com/xml/ns/javaee}message-destination-linkType" minOccurs="0"/>
 *         &lt;element name="activation-config" type="{http://java.sun.com/xml/ns/javaee}activation-configType" minOccurs="0"/>
 *         &lt;element name="around-invoke" type="{http://java.sun.com/xml/ns/javaee}around-invokeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="around-timeout" type="{http://java.sun.com/xml/ns/javaee}around-timeoutType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}jndiEnvironmentRefsGroup"/>
 *         &lt;element name="security-role-ref" type="{http://java.sun.com/xml/ns/javaee}security-role-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-identity" type="{http://java.sun.com/xml/ns/javaee}security-identityType" minOccurs="0"/>
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
@XmlType(name = "message-driven-beanType", propOrder = {
        "descriptions",
        "displayNames",
        "icon",
        "ejbName",
        "mappedName",
        "ejbClass",
        "messagingType",
        "timeoutMethod",
        "timer",
        "transactionType",
        "messageSelector",
        "acknowledgeMode",
        "messageDrivenDestination",
        "messageDestinationType",
        "messageDestinationLink",
        "activationConfig",
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
        "securityRoleRef",
        "securityIdentity"
        })
public class MessageDrivenBean implements EnterpriseBean, TimerConsumer  {

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
    @XmlElement(name = "ejb-class")
    protected String ejbClass;
    @XmlElement(name = "messaging-type")
    protected String messagingType;
    @XmlElement(name = "timeout-method")
    protected NamedMethod timeoutMethod;
    protected List<Timer> timer;
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
    @XmlElement(name = "security-role-ref", required = true)
    protected List<SecurityRoleRef> securityRoleRef;
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

    public MessageDrivenBean(String ejbName, String ejbClass) {
        this.ejbName = ejbName;
        this.ejbClass = ejbClass;
    }

    public MessageDrivenBean(Class ejbClass) {
        this(ejbClass.getSimpleName(), ejbClass.getName());
    }

    public MessageDrivenBean(String ejbName, Class ejbClass) {
        this(ejbName, ejbClass.getName());
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

    public String getEjbClass() {
        return ejbClass;
    }

    public void setEjbClass(String value) {
        this.ejbClass = value;
    }

    public void setEjbClass(Class value) {
        this.ejbClass = value.getName();
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

    public List<Timer> getTimer() {
        if (timer == null) {
            timer = new ArrayList<Timer>();
        }
        return this.timer;
    }

    public MessageDrivenDestination getMessageDrivenDestination() {
        return null;
    }

    @XmlElement(name = "message-driven-destination")
    public void setMessageDrivenDestination(MessageDrivenDestination value) {
        if (activationConfig == null) activationConfig = new ActivationConfig();
        DestinationType destinationType = value.getDestinationType();
        if (destinationType != null) {
            activationConfig.addProperty("destinationType", destinationType.getvalue());
        }
        SubscriptionDurability subscriptionDurability = value.getSubscriptionDurability();
        if (subscriptionDurability != null) {
            activationConfig.addProperty("subscriptionDurability", subscriptionDurability.getvalue());
        }
    }

    @XmlElement(name = "message-selector")
    public String getMessageSelector() {
        return null;
    }

    public void setMessageSelector(String messageSelector) {
        if (messageSelector != null) {
            if (activationConfig == null) activationConfig = new ActivationConfig();
            activationConfig.addProperty("messageSelector", messageSelector);
        }
    }

    @XmlElement(name = "acknowledge-mode")
    public String getAcknowledgeMode() {
        return null;
    }

    public void setAcknowledgeMode(String acknowledgeMode) {
        if (acknowledgeMode != null) {
            if (activationConfig == null) activationConfig = new ActivationConfig();
            activationConfig.addProperty("acknowledgeMode", acknowledgeMode);
        }
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

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public void addAroundTimeout(String method) {
        assert ejbClass != null : "Set the ejbClass before calling this method";
        getAroundTimeout().add(new AroundTimeout(ejbClass, method));
    }
}
