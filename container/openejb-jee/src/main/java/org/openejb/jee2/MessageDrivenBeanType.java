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
        "description",
        "displayName",
        "icon",
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
public class MessageDrivenBeanType {

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
    @XmlElement(name = "ejb-class")
    protected String ejbClass;
    @XmlElement(name = "messaging-type")
    protected String messagingType;
    @XmlElement(name = "timeout-method")
    protected NamedMethodType timeoutMethod;
    @XmlElement(name = "transaction-type")
    protected TransactionTypeType transactionType;
    @XmlElement(name = "message-destination-type")
    protected String messageDestinationType;
    @XmlElement(name = "message-destination-link")
    protected String messageDestinationLink;
    @XmlElement(name = "activation-config")
    protected ActivationConfigType activationConfig;
    @XmlElement(name = "around-invoke", required = true)
    protected List<AroundInvokeType> aroundInvoke;
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
    @XmlElement(name = "security-identity")
    protected SecurityIdentityType securityIdentity;
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

    public NamedMethodType getTimeoutMethod() {
        return timeoutMethod;
    }

    public void setTimeoutMethod(NamedMethodType value) {
        this.timeoutMethod = value;
    }

    public TransactionTypeType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionTypeType value) {
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

    public ActivationConfigType getActivationConfig() {
        return activationConfig;
    }

    public void setActivationConfig(ActivationConfigType value) {
        this.activationConfig = value;
    }

    /**
     * Gets the value of the aroundInvoke property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aroundInvoke property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getAroundInvoke().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link AroundInvokeType }
     */
    public List<AroundInvokeType> getAroundInvoke() {
        if (aroundInvoke == null) {
            aroundInvoke = new ArrayList<AroundInvokeType>();
        }
        return this.aroundInvoke;
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

    public SecurityIdentityType getSecurityIdentity() {
        return securityIdentity;
    }

    public void setSecurityIdentity(SecurityIdentityType value) {
        this.securityIdentity = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
