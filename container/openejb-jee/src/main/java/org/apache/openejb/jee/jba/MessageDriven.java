/**
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

package org.apache.openejb.jee.jba;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ejbName",
    "activationConfig",
    "destinationJndiName",
    "localJndiName",
    "mdbUser",
    "mdbPasswd",
    "mdbClientId",
    "mdbSubscriptionId",
    "resourceAdapterName",
    "exceptionOnRollback",
    "timerPersistence",
    "configurationName",
    "invokerBindings",
    "securityProxy",
    "ejbRef",
    "ejbLocalRef",
    "serviceRef",
    "securityIdentity",
    "resourceRef",
    "resourceEnvRef",
    "messageDestinationRef",
    "depends",
    "iorSecurityConfig",
    "ejbTimeoutIdentity"
})
@XmlRootElement(name = "message-driven")
public class MessageDriven {

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "activation-config")
    protected ActivationConfig activationConfig;
    @XmlElement(name = "destination-jndi-name")
    protected String destinationJndiName;
    @XmlElement(name = "local-jndi-name")
    protected String localJndiName;
    @XmlElement(name = "mdb-user")
    protected String mdbUser;
    @XmlElement(name = "mdb-passwd")
    protected String mdbPasswd;
    @XmlElement(name = "mdb-client-id")
    protected String mdbClientId;
    @XmlElement(name = "mdb-subscription-id")
    protected String mdbSubscriptionId;
    @XmlElement(name = "resource-adapter-name")
    protected String resourceAdapterName;
    @XmlElement(name = "exception-on-rollback")
    protected String exceptionOnRollback;
    @XmlElement(name = "timer-persistence")
    protected String timerPersistence;
    @XmlElement(name = "configuration-name")
    protected String configurationName;
    @XmlElement(name = "invoker-bindings")
    protected InvokerBindings invokerBindings;
    @XmlElement(name = "security-proxy")
    protected String securityProxy;
    @XmlElement(name = "ejb-ref")
    protected List<EjbRef> ejbRef;
    @XmlElement(name = "ejb-local-ref")
    protected List<EjbLocalRef> ejbLocalRef;
    @XmlElement(name = "service-ref")
    protected List<ServiceRef> serviceRef;
    @XmlElement(name = "security-identity")
    protected SecurityIdentity securityIdentity;
    @XmlElement(name = "resource-ref")
    protected List<ResourceRef> resourceRef;
    @XmlElement(name = "resource-env-ref")
    protected List<ResourceEnvRef> resourceEnvRef;
    @XmlElement(name = "message-destination-ref")
    protected List<MessageDestinationRef> messageDestinationRef;
    protected List<Depends> depends;
    @XmlElement(name = "ior-security-config")
    protected IorSecurityConfig iorSecurityConfig;
    @XmlElement(name = "ejb-timeout-identity")
    protected EjbTimeoutIdentity ejbTimeoutIdentity;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the ejbName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEjbName() {
        return ejbName;
    }

    /**
     * Sets the value of the ejbName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEjbName(String value) {
        this.ejbName = value;
    }

    /**
     * Gets the value of the activationConfig property.
     * 
     * @return
     *     possible object is
     *     {@link ActivationConfig }
     *     
     */
    public ActivationConfig getActivationConfig() {
        return activationConfig;
    }

    /**
     * Sets the value of the activationConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivationConfig }
     *     
     */
    public void setActivationConfig(ActivationConfig value) {
        this.activationConfig = value;
    }

    /**
     * Gets the value of the destinationJndiName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationJndiName() {
        return destinationJndiName;
    }

    /**
     * Sets the value of the destinationJndiName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationJndiName(String value) {
        this.destinationJndiName = value;
    }

    /**
     * Gets the value of the localJndiName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalJndiName() {
        return localJndiName;
    }

    /**
     * Sets the value of the localJndiName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalJndiName(String value) {
        this.localJndiName = value;
    }

    /**
     * Gets the value of the mdbUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMdbUser() {
        return mdbUser;
    }

    /**
     * Sets the value of the mdbUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMdbUser(String value) {
        this.mdbUser = value;
    }

    /**
     * Gets the value of the mdbPasswd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMdbPasswd() {
        return mdbPasswd;
    }

    /**
     * Sets the value of the mdbPasswd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMdbPasswd(String value) {
        this.mdbPasswd = value;
    }

    /**
     * Gets the value of the mdbClientId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMdbClientId() {
        return mdbClientId;
    }

    /**
     * Sets the value of the mdbClientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMdbClientId(String value) {
        this.mdbClientId = value;
    }

    /**
     * Gets the value of the mdbSubscriptionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMdbSubscriptionId() {
        return mdbSubscriptionId;
    }

    /**
     * Sets the value of the mdbSubscriptionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMdbSubscriptionId(String value) {
        this.mdbSubscriptionId = value;
    }

    /**
     * Gets the value of the resourceAdapterName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResourceAdapterName() {
        return resourceAdapterName;
    }

    /**
     * Sets the value of the resourceAdapterName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResourceAdapterName(String value) {
        this.resourceAdapterName = value;
    }

    /**
     * Gets the value of the exceptionOnRollback property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExceptionOnRollback() {
        return exceptionOnRollback;
    }

    /**
     * Sets the value of the exceptionOnRollback property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExceptionOnRollback(String value) {
        this.exceptionOnRollback = value;
    }

    /**
     * Gets the value of the timerPersistence property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimerPersistence() {
        return timerPersistence;
    }

    /**
     * Sets the value of the timerPersistence property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimerPersistence(String value) {
        this.timerPersistence = value;
    }

    /**
     * Gets the value of the configurationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConfigurationName() {
        return configurationName;
    }

    /**
     * Sets the value of the configurationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConfigurationName(String value) {
        this.configurationName = value;
    }

    /**
     * Gets the value of the invokerBindings property.
     * 
     * @return
     *     possible object is
     *     {@link InvokerBindings }
     *     
     */
    public InvokerBindings getInvokerBindings() {
        return invokerBindings;
    }

    /**
     * Sets the value of the invokerBindings property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvokerBindings }
     *     
     */
    public void setInvokerBindings(InvokerBindings value) {
        this.invokerBindings = value;
    }

    /**
     * Gets the value of the securityProxy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecurityProxy() {
        return securityProxy;
    }

    /**
     * Sets the value of the securityProxy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecurityProxy(String value) {
        this.securityProxy = value;
    }

    /**
     * Gets the value of the ejbRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbRef }
     * 
     * 
     */
    public List<EjbRef> getEjbRef() {
        if (ejbRef == null) {
            ejbRef = new ArrayList<EjbRef>();
        }
        return this.ejbRef;
    }

    /**
     * Gets the value of the ejbLocalRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbLocalRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbLocalRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbLocalRef }
     * 
     * 
     */
    public List<EjbLocalRef> getEjbLocalRef() {
        if (ejbLocalRef == null) {
            ejbLocalRef = new ArrayList<EjbLocalRef>();
        }
        return this.ejbLocalRef;
    }

    /**
     * Gets the value of the serviceRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceRef }
     * 
     * 
     */
    public List<ServiceRef> getServiceRef() {
        if (serviceRef == null) {
            serviceRef = new ArrayList<ServiceRef>();
        }
        return this.serviceRef;
    }

    /**
     * Gets the value of the securityIdentity property.
     * 
     * @return
     *     possible object is
     *     {@link SecurityIdentity }
     *     
     */
    public SecurityIdentity getSecurityIdentity() {
        return securityIdentity;
    }

    /**
     * Sets the value of the securityIdentity property.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityIdentity }
     *     
     */
    public void setSecurityIdentity(SecurityIdentity value) {
        this.securityIdentity = value;
    }

    /**
     * Gets the value of the resourceRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceRef }
     * 
     * 
     */
    public List<ResourceRef> getResourceRef() {
        if (resourceRef == null) {
            resourceRef = new ArrayList<ResourceRef>();
        }
        return this.resourceRef;
    }

    /**
     * Gets the value of the resourceEnvRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceEnvRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceEnvRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceEnvRef }
     * 
     * 
     */
    public List<ResourceEnvRef> getResourceEnvRef() {
        if (resourceEnvRef == null) {
            resourceEnvRef = new ArrayList<ResourceEnvRef>();
        }
        return this.resourceEnvRef;
    }

    /**
     * Gets the value of the messageDestinationRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageDestinationRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageDestinationRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MessageDestinationRef }
     * 
     * 
     */
    public List<MessageDestinationRef> getMessageDestinationRef() {
        if (messageDestinationRef == null) {
            messageDestinationRef = new ArrayList<MessageDestinationRef>();
        }
        return this.messageDestinationRef;
    }

    /**
     * Gets the value of the depends property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the depends property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDepends().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Depends }
     * 
     * 
     */
    public List<Depends> getDepends() {
        if (depends == null) {
            depends = new ArrayList<Depends>();
        }
        return this.depends;
    }

    /**
     * Gets the value of the iorSecurityConfig property.
     * 
     * @return
     *     possible object is
     *     {@link IorSecurityConfig }
     *     
     */
    public IorSecurityConfig getIorSecurityConfig() {
        return iorSecurityConfig;
    }

    /**
     * Sets the value of the iorSecurityConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link IorSecurityConfig }
     *     
     */
    public void setIorSecurityConfig(IorSecurityConfig value) {
        this.iorSecurityConfig = value;
    }

    /**
     * Gets the value of the ejbTimeoutIdentity property.
     * 
     * @return
     *     possible object is
     *     {@link EjbTimeoutIdentity }
     *     
     */
    public EjbTimeoutIdentity getEjbTimeoutIdentity() {
        return ejbTimeoutIdentity;
    }

    /**
     * Sets the value of the ejbTimeoutIdentity property.
     * 
     * @param value
     *     allowed object is
     *     {@link EjbTimeoutIdentity }
     *     
     */
    public void setEjbTimeoutIdentity(EjbTimeoutIdentity value) {
        this.ejbTimeoutIdentity = value;
    }

}
