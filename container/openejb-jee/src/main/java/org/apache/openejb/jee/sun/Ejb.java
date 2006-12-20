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

package org.apache.openejb.jee.sun;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ejbName",
    "jndiName",
    "ejbRef",
    "resourceRef",
    "resourceEnvRef",
    "serviceRef",
    "messageDestinationRef",
    "passByReference",
    "cmp",
    "principal",
    "mdbConnectionFactory",
    "jmsDurableSubscriptionName",
    "jmsMaxMessagesLoad",
    "iorSecurityConfig",
    "isReadOnlyBean",
    "refreshPeriodInSeconds",
    "commitOption",
    "cmtTimeoutInSeconds",
    "useThreadPoolId",
    "genClasses",
    "beanPool",
    "beanCache",
    "mdbResourceAdapter",
    "webserviceEndpoint",
    "flushAtEndOfMethod",
    "checkpointedMethods",
    "checkpointAtEndOfMethod"
})
@XmlRootElement(name = "ejb")
public class Ejb {

    @XmlAttribute(name = "availability-enabled")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String availabilityEnabled;
    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "jndi-name")
    protected String jndiName;
    @XmlElement(name = "ejb-ref")
    protected List<EjbRef> ejbRef;
    @XmlElement(name = "resource-ref")
    protected List<ResourceRef> resourceRef;
    @XmlElement(name = "resource-env-ref")
    protected List<ResourceEnvRef> resourceEnvRef;
    @XmlElement(name = "service-ref")
    protected List<ServiceRef> serviceRef;
    @XmlElement(name = "message-destination-ref")
    protected List<MessageDestinationRef> messageDestinationRef;
    @XmlElement(name = "pass-by-reference")
    protected String passByReference;
    protected Cmp cmp;
    protected Principal principal;
    @XmlElement(name = "mdb-connection-factory")
    protected MdbConnectionFactory mdbConnectionFactory;
    @XmlElement(name = "jms-durable-subscription-name")
    protected String jmsDurableSubscriptionName;
    @XmlElement(name = "jms-max-messages-load")
    protected String jmsMaxMessagesLoad;
    @XmlElement(name = "ior-security-config")
    protected IorSecurityConfig iorSecurityConfig;
    @XmlElement(name = "is-read-only-bean")
    protected String isReadOnlyBean;
    @XmlElement(name = "refresh-period-in-seconds")
    protected String refreshPeriodInSeconds;
    @XmlElement(name = "commit-option")
    protected String commitOption;
    @XmlElement(name = "cmt-timeout-in-seconds")
    protected String cmtTimeoutInSeconds;
    @XmlElement(name = "use-thread-pool-id")
    protected String useThreadPoolId;
    @XmlElement(name = "gen-classes")
    protected GenClasses genClasses;
    @XmlElement(name = "bean-pool")
    protected BeanPool beanPool;
    @XmlElement(name = "bean-cache")
    protected BeanCache beanCache;
    @XmlElement(name = "mdb-resource-adapter")
    protected MdbResourceAdapter mdbResourceAdapter;
    @XmlElement(name = "webservice-endpoint")
    protected List<WebserviceEndpoint> webserviceEndpoint;
    @XmlElement(name = "flush-at-end-of-method")
    protected FlushAtEndOfMethod flushAtEndOfMethod;
    @XmlElement(name = "checkpointed-methods")
    protected String checkpointedMethods;
    @XmlElement(name = "checkpoint-at-end-of-method")
    protected CheckpointAtEndOfMethod checkpointAtEndOfMethod;

    /**
     * Gets the value of the availabilityEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAvailabilityEnabled() {
        return availabilityEnabled;
    }

    /**
     * Sets the value of the availabilityEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAvailabilityEnabled(String value) {
        this.availabilityEnabled = value;
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
     * Gets the value of the jndiName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJndiName() {
        return jndiName;
    }

    /**
     * Sets the value of the jndiName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJndiName(String value) {
        this.jndiName = value;
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
     * Gets the value of the passByReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassByReference() {
        return passByReference;
    }

    /**
     * Sets the value of the passByReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassByReference(String value) {
        this.passByReference = value;
    }

    /**
     * Gets the value of the cmp property.
     * 
     * @return
     *     possible object is
     *     {@link Cmp }
     *     
     */
    public Cmp getCmp() {
        return cmp;
    }

    /**
     * Sets the value of the cmp property.
     * 
     * @param value
     *     allowed object is
     *     {@link Cmp }
     *     
     */
    public void setCmp(Cmp value) {
        this.cmp = value;
    }

    /**
     * Gets the value of the principal property.
     * 
     * @return
     *     possible object is
     *     {@link Principal }
     *     
     */
    public Principal getPrincipal() {
        return principal;
    }

    /**
     * Sets the value of the principal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Principal }
     *     
     */
    public void setPrincipal(Principal value) {
        this.principal = value;
    }

    /**
     * Gets the value of the mdbConnectionFactory property.
     * 
     * @return
     *     possible object is
     *     {@link MdbConnectionFactory }
     *     
     */
    public MdbConnectionFactory getMdbConnectionFactory() {
        return mdbConnectionFactory;
    }

    /**
     * Sets the value of the mdbConnectionFactory property.
     * 
     * @param value
     *     allowed object is
     *     {@link MdbConnectionFactory }
     *     
     */
    public void setMdbConnectionFactory(MdbConnectionFactory value) {
        this.mdbConnectionFactory = value;
    }

    /**
     * Gets the value of the jmsDurableSubscriptionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJmsDurableSubscriptionName() {
        return jmsDurableSubscriptionName;
    }

    /**
     * Sets the value of the jmsDurableSubscriptionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJmsDurableSubscriptionName(String value) {
        this.jmsDurableSubscriptionName = value;
    }

    /**
     * Gets the value of the jmsMaxMessagesLoad property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJmsMaxMessagesLoad() {
        return jmsMaxMessagesLoad;
    }

    /**
     * Sets the value of the jmsMaxMessagesLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJmsMaxMessagesLoad(String value) {
        this.jmsMaxMessagesLoad = value;
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
     * Gets the value of the isReadOnlyBean property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsReadOnlyBean() {
        return isReadOnlyBean;
    }

    /**
     * Sets the value of the isReadOnlyBean property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsReadOnlyBean(String value) {
        this.isReadOnlyBean = value;
    }

    /**
     * Gets the value of the refreshPeriodInSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefreshPeriodInSeconds() {
        return refreshPeriodInSeconds;
    }

    /**
     * Sets the value of the refreshPeriodInSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefreshPeriodInSeconds(String value) {
        this.refreshPeriodInSeconds = value;
    }

    /**
     * Gets the value of the commitOption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommitOption() {
        return commitOption;
    }

    /**
     * Sets the value of the commitOption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommitOption(String value) {
        this.commitOption = value;
    }

    /**
     * Gets the value of the cmtTimeoutInSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCmtTimeoutInSeconds() {
        return cmtTimeoutInSeconds;
    }

    /**
     * Sets the value of the cmtTimeoutInSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCmtTimeoutInSeconds(String value) {
        this.cmtTimeoutInSeconds = value;
    }

    /**
     * Gets the value of the useThreadPoolId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUseThreadPoolId() {
        return useThreadPoolId;
    }

    /**
     * Sets the value of the useThreadPoolId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUseThreadPoolId(String value) {
        this.useThreadPoolId = value;
    }

    /**
     * Gets the value of the genClasses property.
     * 
     * @return
     *     possible object is
     *     {@link GenClasses }
     *     
     */
    public GenClasses getGenClasses() {
        return genClasses;
    }

    /**
     * Sets the value of the genClasses property.
     * 
     * @param value
     *     allowed object is
     *     {@link GenClasses }
     *     
     */
    public void setGenClasses(GenClasses value) {
        this.genClasses = value;
    }

    /**
     * Gets the value of the beanPool property.
     * 
     * @return
     *     possible object is
     *     {@link BeanPool }
     *     
     */
    public BeanPool getBeanPool() {
        return beanPool;
    }

    /**
     * Sets the value of the beanPool property.
     * 
     * @param value
     *     allowed object is
     *     {@link BeanPool }
     *     
     */
    public void setBeanPool(BeanPool value) {
        this.beanPool = value;
    }

    /**
     * Gets the value of the beanCache property.
     * 
     * @return
     *     possible object is
     *     {@link BeanCache }
     *     
     */
    public BeanCache getBeanCache() {
        return beanCache;
    }

    /**
     * Sets the value of the beanCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link BeanCache }
     *     
     */
    public void setBeanCache(BeanCache value) {
        this.beanCache = value;
    }

    /**
     * Gets the value of the mdbResourceAdapter property.
     * 
     * @return
     *     possible object is
     *     {@link MdbResourceAdapter }
     *     
     */
    public MdbResourceAdapter getMdbResourceAdapter() {
        return mdbResourceAdapter;
    }

    /**
     * Sets the value of the mdbResourceAdapter property.
     * 
     * @param value
     *     allowed object is
     *     {@link MdbResourceAdapter }
     *     
     */
    public void setMdbResourceAdapter(MdbResourceAdapter value) {
        this.mdbResourceAdapter = value;
    }

    /**
     * Gets the value of the webserviceEndpoint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the webserviceEndpoint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWebserviceEndpoint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WebserviceEndpoint }
     * 
     * 
     */
    public List<WebserviceEndpoint> getWebserviceEndpoint() {
        if (webserviceEndpoint == null) {
            webserviceEndpoint = new ArrayList<WebserviceEndpoint>();
        }
        return this.webserviceEndpoint;
    }

    /**
     * Gets the value of the flushAtEndOfMethod property.
     * 
     * @return
     *     possible object is
     *     {@link FlushAtEndOfMethod }
     *     
     */
    public FlushAtEndOfMethod getFlushAtEndOfMethod() {
        return flushAtEndOfMethod;
    }

    /**
     * Sets the value of the flushAtEndOfMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlushAtEndOfMethod }
     *     
     */
    public void setFlushAtEndOfMethod(FlushAtEndOfMethod value) {
        this.flushAtEndOfMethod = value;
    }

    /**
     * Gets the value of the checkpointedMethods property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheckpointedMethods() {
        return checkpointedMethods;
    }

    /**
     * Sets the value of the checkpointedMethods property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheckpointedMethods(String value) {
        this.checkpointedMethods = value;
    }

    /**
     * Gets the value of the checkpointAtEndOfMethod property.
     * 
     * @return
     *     possible object is
     *     {@link CheckpointAtEndOfMethod }
     *     
     */
    public CheckpointAtEndOfMethod getCheckpointAtEndOfMethod() {
        return checkpointAtEndOfMethod;
    }

    /**
     * Sets the value of the checkpointAtEndOfMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link CheckpointAtEndOfMethod }
     *     
     */
    public void setCheckpointAtEndOfMethod(CheckpointAtEndOfMethod value) {
        this.checkpointAtEndOfMethod = value;
    }

}
