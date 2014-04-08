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

import java.math.BigInteger;
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
 * <p>Java class for weblogic-enterprise-bean complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="weblogic-enterprise-bean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ejb-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="entity-descriptor" type="{http://www.bea.com/ns/weblogic/90}entity-descriptor"/>
 *           &lt;element name="stateless-session-descriptor" type="{http://www.bea.com/ns/weblogic/90}stateless-session-descriptor"/>
 *           &lt;element name="stateful-session-descriptor" type="{http://www.bea.com/ns/weblogic/90}stateful-session-descriptor"/>
 *           &lt;element name="message-driven-descriptor" type="{http://www.bea.com/ns/weblogic/90}message-driven-descriptor"/>
 *         &lt;/choice>
 *         &lt;element name="transaction-descriptor" type="{http://www.bea.com/ns/weblogic/90}transaction-descriptor" minOccurs="0"/>
 *         &lt;element name="iiop-security-descriptor" type="{http://www.bea.com/ns/weblogic/90}iiop-security-descriptor" minOccurs="0"/>
 *         &lt;group ref="{http://www.bea.com/ns/weblogic/90}reference-descriptorGroup"/>
 *         &lt;element name="enable-call-by-reference" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="network-access-point" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="clients-on-same-server" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="run-as-principal-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="create-as-principal-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remove-as-principal-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="passivate-as-principal-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="jndi-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="local-jndi-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dispatch-policy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remote-client-timeout" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
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
@XmlType(name = "weblogic-enterprise-bean", propOrder = {
    "ejbName",
    "entityDescriptor",
    "statelessSessionDescriptor",
    "statefulSessionDescriptor",
    "messageDrivenDescriptor",
    "transactionDescriptor",
    "iiopSecurityDescriptor",
    "referenceDescriptor",
    "resourceDescription",
    "resourceEnvDescription",
    "ejbReferenceDescription",
    "serviceReferenceDescription",
    "enableCallByReference",
    "networkAccessPoint",
    "clientsOnSameServer",
    "runAsPrincipalName",
    "createAsPrincipalName",
    "removeAsPrincipalName",
    "passivateAsPrincipalName",
    "jndiName",
    "localJndiName",
    "dispatchPolicy",
    "remoteClientTimeout"
})
public class WeblogicEnterpriseBean {

    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;

    @XmlElement(name = "entity-descriptor")
    protected EntityDescriptor entityDescriptor;

    @XmlElement(name = "stateless-session-descriptor")
    protected StatelessSessionDescriptor statelessSessionDescriptor;

    @XmlElement(name = "stateful-session-descriptor")
    protected StatefulSessionDescriptor statefulSessionDescriptor;

    @XmlElement(name = "message-driven-descriptor")
    protected MessageDrivenDescriptor messageDrivenDescriptor;

    @XmlElement(name = "transaction-descriptor")
    protected TransactionDescriptor transactionDescriptor;

    @XmlElement(name = "iiop-security-descriptor")
    protected IiopSecurityDescriptor iiopSecurityDescriptor;

    @XmlElement(name = "reference-descriptor")
    protected ReferenceDescriptor referenceDescriptor;

    @XmlElement(name = "resource-description")
    protected List<ResourceDescription> resourceDescription;

    @XmlElement(name = "resource-env-description")
    protected List<ResourceEnvDescription> resourceEnvDescription;

    @XmlElement(name = "ejb-reference-description")
    protected List<EjbReferenceDescription> ejbReferenceDescription;

    @XmlElement(name = "service-reference-description")
    protected List<ServiceReferenceDescription> serviceReferenceDescription;

    @XmlElement(name = "enable-call-by-reference")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean enableCallByReference;

    @XmlElement(name = "network-access-point")
    protected String networkAccessPoint;

    @XmlElement(name = "clients-on-same-server")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean clientsOnSameServer;

    @XmlElement(name = "run-as-principal-name")
    protected String runAsPrincipalName;

    @XmlElement(name = "create-as-principal-name")
    protected String createAsPrincipalName;

    @XmlElement(name = "remove-as-principal-name")
    protected String removeAsPrincipalName;

    @XmlElement(name = "passivate-as-principal-name")
    protected String passivateAsPrincipalName;

    @XmlElement(name = "jndi-name")
    protected String jndiName;

    @XmlElement(name = "local-jndi-name")
    protected String localJndiName;

    @XmlElement(name = "dispatch-policy")
    protected String dispatchPolicy;

    @XmlElement(name = "remote-client-timeout")
    protected BigInteger remoteClientTimeout;

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

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
     * Gets the value of the entityDescriptor property.
     * 
     * @return
     *     possible object is
     *     {@link EntityDescriptor }
     *     
     */
    public EntityDescriptor getEntityDescriptor() {
        return entityDescriptor;
    }

    /**
     * Sets the value of the entityDescriptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntityDescriptor }
     *     
     */
    public void setEntityDescriptor(EntityDescriptor value) {
        this.entityDescriptor = value;
    }

    /**
     * Gets the value of the statelessSessionDescriptor property.
     * 
     * @return
     *     possible object is
     *     {@link StatelessSessionDescriptor }
     *     
     */
    public StatelessSessionDescriptor getStatelessSessionDescriptor() {
        return statelessSessionDescriptor;
    }

    /**
     * Sets the value of the statelessSessionDescriptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatelessSessionDescriptor }
     *     
     */
    public void setStatelessSessionDescriptor(StatelessSessionDescriptor value) {
        this.statelessSessionDescriptor = value;
    }

    /**
     * Gets the value of the statefulSessionDescriptor property.
     * 
     * @return
     *     possible object is
     *     {@link StatefulSessionDescriptor }
     *     
     */
    public StatefulSessionDescriptor getStatefulSessionDescriptor() {
        return statefulSessionDescriptor;
    }

    /**
     * Sets the value of the statefulSessionDescriptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatefulSessionDescriptor }
     *     
     */
    public void setStatefulSessionDescriptor(StatefulSessionDescriptor value) {
        this.statefulSessionDescriptor = value;
    }

    /**
     * Gets the value of the messageDrivenDescriptor property.
     * 
     * @return
     *     possible object is
     *     {@link MessageDrivenDescriptor }
     *     
     */
    public MessageDrivenDescriptor getMessageDrivenDescriptor() {
        return messageDrivenDescriptor;
    }

    /**
     * Sets the value of the messageDrivenDescriptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageDrivenDescriptor }
     *     
     */
    public void setMessageDrivenDescriptor(MessageDrivenDescriptor value) {
        this.messageDrivenDescriptor = value;
    }

    /**
     * Gets the value of the transactionDescriptor property.
     * 
     * @return
     *     possible object is
     *     {@link TransactionDescriptor }
     *     
     */
    public TransactionDescriptor getTransactionDescriptor() {
        return transactionDescriptor;
    }

    /**
     * Sets the value of the transactionDescriptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionDescriptor }
     *     
     */
    public void setTransactionDescriptor(TransactionDescriptor value) {
        this.transactionDescriptor = value;
    }

    /**
     * Gets the value of the iiopSecurityDescriptor property.
     * 
     * @return
     *     possible object is
     *     {@link IiopSecurityDescriptor }
     *     
     */
    public IiopSecurityDescriptor getIiopSecurityDescriptor() {
        return iiopSecurityDescriptor;
    }

    /**
     * Sets the value of the iiopSecurityDescriptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link IiopSecurityDescriptor }
     *     
     */
    public void setIiopSecurityDescriptor(IiopSecurityDescriptor value) {
        this.iiopSecurityDescriptor = value;
    }

    public ReferenceDescriptor getReferenceDescriptor() {
        return referenceDescriptor;
    }

    public void setReferenceDescriptor(ReferenceDescriptor referenceDescriptor) {
        this.referenceDescriptor = referenceDescriptor;
    }

    /**
     * Gets the value of the resourceDescription property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceDescription property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceDescription }
     * 
     * 
     */
    public List<ResourceDescription> getResourceDescription() {
        if (resourceDescription == null) {
            resourceDescription = new ArrayList<ResourceDescription>();
        }
        return this.resourceDescription;
    }

    /**
     * Gets the value of the resourceEnvDescription property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resourceEnvDescription property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResourceEnvDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceEnvDescription }
     * 
     * 
     */
    public List<ResourceEnvDescription> getResourceEnvDescription() {
        if (resourceEnvDescription == null) {
            resourceEnvDescription = new ArrayList<ResourceEnvDescription>();
        }
        return this.resourceEnvDescription;
    }

    /**
     * Gets the value of the ejbReferenceDescription property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ejbReferenceDescription property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEjbReferenceDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EjbReferenceDescription }
     * 
     * 
     */
    public List<EjbReferenceDescription> getEjbReferenceDescription() {
        if (ejbReferenceDescription == null) {
            ejbReferenceDescription = new ArrayList<EjbReferenceDescription>();
        }
        return this.ejbReferenceDescription;
    }

    /**
     * Gets the value of the serviceReferenceDescription property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceReferenceDescription property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceReferenceDescription().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceReferenceDescription }
     * 
     * 
     */
    public List<ServiceReferenceDescription> getServiceReferenceDescription() {
        if (serviceReferenceDescription == null) {
            serviceReferenceDescription = new ArrayList<ServiceReferenceDescription>();
        }
        return this.serviceReferenceDescription;
    }

    /**
     * Gets the value of the enableCallByReference property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getEnableCallByReference() {
        return enableCallByReference;
    }

    /**
     * Sets the value of the enableCallByReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableCallByReference(Boolean value) {
        this.enableCallByReference = value;
    }

    /**
     * Gets the value of the networkAccessPoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNetworkAccessPoint() {
        return networkAccessPoint;
    }

    /**
     * Sets the value of the networkAccessPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNetworkAccessPoint(String value) {
        this.networkAccessPoint = value;
    }

    /**
     * Gets the value of the clientsOnSameServer property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getClientsOnSameServer() {
        return clientsOnSameServer;
    }

    /**
     * Sets the value of the clientsOnSameServer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setClientsOnSameServer(Boolean value) {
        this.clientsOnSameServer = value;
    }

    /**
     * Gets the value of the runAsPrincipalName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRunAsPrincipalName() {
        return runAsPrincipalName;
    }

    /**
     * Sets the value of the runAsPrincipalName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRunAsPrincipalName(String value) {
        this.runAsPrincipalName = value;
    }

    /**
     * Gets the value of the createAsPrincipalName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreateAsPrincipalName() {
        return createAsPrincipalName;
    }

    /**
     * Sets the value of the createAsPrincipalName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreateAsPrincipalName(String value) {
        this.createAsPrincipalName = value;
    }

    /**
     * Gets the value of the removeAsPrincipalName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoveAsPrincipalName() {
        return removeAsPrincipalName;
    }

    /**
     * Sets the value of the removeAsPrincipalName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoveAsPrincipalName(String value) {
        this.removeAsPrincipalName = value;
    }

    /**
     * Gets the value of the passivateAsPrincipalName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassivateAsPrincipalName() {
        return passivateAsPrincipalName;
    }

    /**
     * Sets the value of the passivateAsPrincipalName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassivateAsPrincipalName(String value) {
        this.passivateAsPrincipalName = value;
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
     * Gets the value of the dispatchPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDispatchPolicy() {
        return dispatchPolicy;
    }

    /**
     * Sets the value of the dispatchPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDispatchPolicy(String value) {
        this.dispatchPolicy = value;
    }

    /**
     * Gets the value of the remoteClientTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRemoteClientTimeout() {
        return remoteClientTimeout;
    }

    /**
     * Sets the value of the remoteClientTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRemoteClientTimeout(BigInteger value) {
        this.remoteClientTimeout = value;
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
