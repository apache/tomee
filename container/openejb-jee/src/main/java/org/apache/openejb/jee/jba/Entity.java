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
    "jndiName",
    "localJndiName",
    "callByValue",
    "readOnly",
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
    "methodAttributes",
    "clustered",
    "clusterConfig",
    "cacheInvalidation",
    "cacheInvalidationConfig",
    "depends",
    "iorSecurityConfig"
})
@XmlRootElement(name = "entity")
public class Entity {

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "jndi-name")
    protected JndiName jndiName;
    @XmlElement(name = "local-jndi-name")
    protected String localJndiName;
    @XmlElement(name = "call-by-value")
    protected String callByValue;
    @XmlElement(name = "read-only")
    protected String readOnly;
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
    @XmlElement(name = "method-attributes")
    protected MethodAttributes methodAttributes;
    protected String clustered;
    @XmlElement(name = "cluster-config")
    protected ClusterConfig clusterConfig;
    @XmlElement(name = "cache-invalidation")
    protected String cacheInvalidation;
    @XmlElement(name = "cache-invalidation-config")
    protected CacheInvalidationConfig cacheInvalidationConfig;
    protected List<Depends> depends;
    @XmlElement(name = "ior-security-config")
    protected IorSecurityConfig iorSecurityConfig;

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
     * Gets the value of the jndiName property.
     * 
     * @return
     *     possible object is
     *     {@link JndiName }
     *     
     */
    public JndiName getJndiName() {
        return jndiName;
    }

    /**
     * Sets the value of the jndiName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JndiName }
     *     
     */
    public void setJndiName(JndiName value) {
        this.jndiName = value;
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
     * Gets the value of the callByValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallByValue() {
        return callByValue;
    }

    /**
     * Sets the value of the callByValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallByValue(String value) {
        this.callByValue = value;
    }

    /**
     * Gets the value of the readOnly property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReadOnly() {
        return readOnly;
    }

    /**
     * Sets the value of the readOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReadOnly(String value) {
        this.readOnly = value;
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
     * Gets the value of the methodAttributes property.
     * 
     * @return
     *     possible object is
     *     {@link MethodAttributes }
     *     
     */
    public MethodAttributes getMethodAttributes() {
        return methodAttributes;
    }

    /**
     * Sets the value of the methodAttributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link MethodAttributes }
     *     
     */
    public void setMethodAttributes(MethodAttributes value) {
        this.methodAttributes = value;
    }

    /**
     * Gets the value of the clustered property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClustered() {
        return clustered;
    }

    /**
     * Sets the value of the clustered property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClustered(String value) {
        this.clustered = value;
    }

    /**
     * Gets the value of the clusterConfig property.
     * 
     * @return
     *     possible object is
     *     {@link ClusterConfig }
     *     
     */
    public ClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    /**
     * Sets the value of the clusterConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClusterConfig }
     *     
     */
    public void setClusterConfig(ClusterConfig value) {
        this.clusterConfig = value;
    }

    /**
     * Gets the value of the cacheInvalidation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCacheInvalidation() {
        return cacheInvalidation;
    }

    /**
     * Sets the value of the cacheInvalidation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCacheInvalidation(String value) {
        this.cacheInvalidation = value;
    }

    /**
     * Gets the value of the cacheInvalidationConfig property.
     * 
     * @return
     *     possible object is
     *     {@link CacheInvalidationConfig }
     *     
     */
    public CacheInvalidationConfig getCacheInvalidationConfig() {
        return cacheInvalidationConfig;
    }

    /**
     * Sets the value of the cacheInvalidationConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link CacheInvalidationConfig }
     *     
     */
    public void setCacheInvalidationConfig(CacheInvalidationConfig value) {
        this.cacheInvalidationConfig = value;
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

}
