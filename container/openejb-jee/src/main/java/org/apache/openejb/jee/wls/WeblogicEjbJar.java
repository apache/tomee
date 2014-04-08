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
package org.apache.openejb.jee.wls;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for weblogic-ejb-jar complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="weblogic-ejb-jar">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://www.bea.com/ns/weblogic/90}description" minOccurs="0"/>
 *         &lt;element name="weblogic-enterprise-bean" type="{http://www.bea.com/ns/weblogic/90}weblogic-enterprise-bean" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-role-assignment" type="{http://www.bea.com/ns/weblogic/90}security-role-assignment" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="run-as-role-assignment" type="{http://www.bea.com/ns/weblogic/90}run-as-role-assignment" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-permission" type="{http://www.bea.com/ns/weblogic/90}security-permission" minOccurs="0"/>
 *         &lt;element name="transaction-isolation" type="{http://www.bea.com/ns/weblogic/90}transaction-isolation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="message-destination-descriptor" type="{http://www.bea.com/ns/weblogic/90}message-destination-descriptor" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="idempotent-methods" type="{http://www.bea.com/ns/weblogic/90}idempotent-methods" minOccurs="0"/>
 *         &lt;element name="retry-methods-on-rollback" type="{http://www.bea.com/ns/weblogic/90}retry-methods-on-rollback" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="enable-bean-class-redeploy" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="disable-warning" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="work-manager" type="{http://www.bea.com/ns/weblogic/90}work-manager" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="weblogic-compatibility" type="{http://www.bea.com/ns/weblogic/90}weblogic-compatibility" minOccurs="0"/>
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
@XmlType(name = "weblogic-ejb-jar", propOrder = {
    "description",
    "weblogicEnterpriseBean",
    "securityRoleAssignment",
    "runAsRoleAssignment",
    "securityPermission",
    "transactionIsolation",
    "messageDestinationDescriptor",
    "idempotentMethods",
    "retryMethodsOnRollback",
    "enableBeanClassRedeploy",
    "disableWarning",
    "workManager",
    "weblogicCompatibility"
})
public class WeblogicEjbJar {

    protected Description description;
    @XmlElement(name = "weblogic-enterprise-bean")
    protected List<WeblogicEnterpriseBean> weblogicEnterpriseBean;
    @XmlElement(name = "security-role-assignment")
    protected List<SecurityRoleAssignment> securityRoleAssignment;
    @XmlElement(name = "run-as-role-assignment")
    protected List<RunAsRoleAssignment> runAsRoleAssignment;
    @XmlElement(name = "security-permission")
    protected SecurityPermission securityPermission;
    @XmlElement(name = "transaction-isolation")
    protected List<TransactionIsolation> transactionIsolation;
    @XmlElement(name = "message-destination-descriptor")
    protected List<MessageDestinationDescriptor> messageDestinationDescriptor;
    @XmlElement(name = "idempotent-methods")
    protected IdempotentMethods idempotentMethods;
    @XmlElement(name = "retry-methods-on-rollback")
    protected List<RetryMethodsOnRollback> retryMethodsOnRollback;
    @XmlElement(name = "enable-bean-class-redeploy")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean enableBeanClassRedeploy;
    @XmlElement(name = "disable-warning")
    protected List<String> disableWarning;
    @XmlElement(name = "work-manager")
    protected List<WorkManager> workManager;
    @XmlElement(name = "weblogic-compatibility")
    protected WeblogicCompatibility weblogicCompatibility;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link Description }
     *     
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link Description }
     *     
     */
    public void setDescription(Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the weblogicEnterpriseBean property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the weblogicEnterpriseBean property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWeblogicEnterpriseBean().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WeblogicEnterpriseBean }
     * 
     * 
     */
    public List<WeblogicEnterpriseBean> getWeblogicEnterpriseBean() {
        if (weblogicEnterpriseBean == null) {
            weblogicEnterpriseBean = new ArrayList<WeblogicEnterpriseBean>();
        }
        return this.weblogicEnterpriseBean;
    }

    /**
     * Gets the value of the securityRoleAssignment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the securityRoleAssignment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSecurityRoleAssignment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SecurityRoleAssignment }
     * 
     * 
     */
    public List<SecurityRoleAssignment> getSecurityRoleAssignment() {
        if (securityRoleAssignment == null) {
            securityRoleAssignment = new ArrayList<SecurityRoleAssignment>();
        }
        return this.securityRoleAssignment;
    }

    /**
     * Gets the value of the runAsRoleAssignment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the runAsRoleAssignment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRunAsRoleAssignment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RunAsRoleAssignment }
     * 
     * 
     */
    public List<RunAsRoleAssignment> getRunAsRoleAssignment() {
        if (runAsRoleAssignment == null) {
            runAsRoleAssignment = new ArrayList<RunAsRoleAssignment>();
        }
        return this.runAsRoleAssignment;
    }

    /**
     * Gets the value of the securityPermission property.
     * 
     * @return
     *     possible object is
     *     {@link SecurityPermission }
     *     
     */
    public SecurityPermission getSecurityPermission() {
        return securityPermission;
    }

    /**
     * Sets the value of the securityPermission property.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityPermission }
     *     
     */
    public void setSecurityPermission(SecurityPermission value) {
        this.securityPermission = value;
    }

    /**
     * Gets the value of the transactionIsolation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transactionIsolation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransactionIsolation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TransactionIsolation }
     * 
     * 
     */
    public List<TransactionIsolation> getTransactionIsolation() {
        if (transactionIsolation == null) {
            transactionIsolation = new ArrayList<TransactionIsolation>();
        }
        return this.transactionIsolation;
    }

    /**
     * Gets the value of the messageDestinationDescriptor property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageDestinationDescriptor property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageDestinationDescriptor().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MessageDestinationDescriptor }
     * 
     * 
     */
    public List<MessageDestinationDescriptor> getMessageDestinationDescriptor() {
        if (messageDestinationDescriptor == null) {
            messageDestinationDescriptor = new ArrayList<MessageDestinationDescriptor>();
        }
        return this.messageDestinationDescriptor;
    }

    /**
     * Gets the value of the idempotentMethods property.
     * 
     * @return
     *     possible object is
     *     {@link IdempotentMethods }
     *     
     */
    public IdempotentMethods getIdempotentMethods() {
        return idempotentMethods;
    }

    /**
     * Sets the value of the idempotentMethods property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdempotentMethods }
     *     
     */
    public void setIdempotentMethods(IdempotentMethods value) {
        this.idempotentMethods = value;
    }

    /**
     * Gets the value of the retryMethodsOnRollback property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the retryMethodsOnRollback property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRetryMethodsOnRollback().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RetryMethodsOnRollback }
     * 
     * 
     */
    public List<RetryMethodsOnRollback> getRetryMethodsOnRollback() {
        if (retryMethodsOnRollback == null) {
            retryMethodsOnRollback = new ArrayList<RetryMethodsOnRollback>();
        }
        return this.retryMethodsOnRollback;
    }

    /**
     * Gets the value of the enableBeanClassRedeploy property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getEnableBeanClassRedeploy() {
        return enableBeanClassRedeploy;
    }

    /**
     * Sets the value of the enableBeanClassRedeploy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableBeanClassRedeploy(Boolean value) {
        this.enableBeanClassRedeploy = value;
    }

    /**
     * Gets the value of the disableWarning property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the disableWarning property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDisableWarning().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDisableWarning() {
        if (disableWarning == null) {
            disableWarning = new ArrayList<String>();
        }
        return this.disableWarning;
    }

    /**
     * Gets the value of the workManager property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the workManager property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWorkManager().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WorkManager }
     * 
     * 
     */
    public List<WorkManager> getWorkManager() {
        if (workManager == null) {
            workManager = new ArrayList<WorkManager>();
        }
        return this.workManager;
    }

    /**
     * Gets the value of the weblogicCompatibility property.
     * 
     * @return
     *     possible object is
     *     {@link WeblogicCompatibility }
     *     
     */
    public WeblogicCompatibility getWeblogicCompatibility() {
        return weblogicCompatibility;
    }

    /**
     * Sets the value of the weblogicCompatibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link WeblogicCompatibility }
     *     
     */
    public void setWeblogicCompatibility(WeblogicCompatibility value) {
        this.weblogicCompatibility = value;
    }

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

}
