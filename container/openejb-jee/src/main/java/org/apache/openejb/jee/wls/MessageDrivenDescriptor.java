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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for message-driven-descriptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="message-driven-descriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pool" type="{http://www.bea.com/ns/weblogic/90}pool" minOccurs="0"/>
 *         &lt;element name="timer-descriptor" type="{http://www.bea.com/ns/weblogic/90}timer-descriptor" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;sequence>
 *             &lt;element name="resource-adapter-jndi-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;/sequence>
 *           &lt;sequence>
 *             &lt;choice>
 *               &lt;sequence>
 *                 &lt;element name="destination-jndi-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;element name="initial-context-factory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;element name="provider-url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;element name="connection-factory-jndi-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *               &lt;/sequence>
 *               &lt;sequence>
 *                 &lt;element name="destination-resource-link" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;element name="connection-factory-resource-link" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *               &lt;/sequence>
 *             &lt;/choice>
 *             &lt;element name="jms-polling-interval-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *             &lt;element name="jms-client-id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *             &lt;element name="generate-unique-jms-client-id" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *             &lt;element name="durable-subscription-deletion" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *             &lt;element name="max-messages-in-transaction" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *             &lt;element name="distributed-destination-connection" type="{http://www.bea.com/ns/weblogic/90}distributed-destination-connection" minOccurs="0"/>
 *           &lt;/sequence>
 *         &lt;/choice>
 *         &lt;element name="init-suspend-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="max-suspend-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="security-plugin" type="{http://www.bea.com/ns/weblogic/90}security-plugin" minOccurs="0"/>
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
@XmlType(name = "message-driven-descriptor", propOrder = {
    "pool",
    "timerDescriptor",
    "resourceAdapterJndiName",
    "destinationJndiName",
    "initialContextFactory",
    "providerUrl",
    "connectionFactoryJndiName",
    "destinationResourceLink",
    "connectionFactoryResourceLink",
    "jmsPollingIntervalSeconds",
    "jmsClientId",
    "generateUniqueJmsClientId",
    "durableSubscriptionDeletion",
    "maxMessagesInTransaction",
    "distributedDestinationConnection",
    "initSuspendSeconds",
    "maxSuspendSeconds",
    "securityPlugin"
})
public class MessageDrivenDescriptor {

    protected Pool pool;
    @XmlElement(name = "timer-descriptor")
    protected TimerDescriptor timerDescriptor;
    @XmlElement(name = "resource-adapter-jndi-name")
    protected String resourceAdapterJndiName;
    @XmlElement(name = "destination-jndi-name")
    protected String destinationJndiName;
    @XmlElement(name = "initial-context-factory")
    protected String initialContextFactory;
    @XmlElement(name = "provider-url")
    protected String providerUrl;
    @XmlElement(name = "connection-factory-jndi-name")
    protected String connectionFactoryJndiName;
    @XmlElement(name = "destination-resource-link")
    protected String destinationResourceLink;
    @XmlElement(name = "connection-factory-resource-link")
    protected String connectionFactoryResourceLink;
    @XmlElement(name = "jms-polling-interval-seconds")
    protected BigInteger jmsPollingIntervalSeconds;
    @XmlElement(name = "jms-client-id")
    protected String jmsClientId;
    @XmlElement(name = "generate-unique-jms-client-id")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean generateUniqueJmsClientId;
    @XmlElement(name = "durable-subscription-deletion")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean durableSubscriptionDeletion;
    @XmlElement(name = "max-messages-in-transaction")
    protected BigInteger maxMessagesInTransaction;
    @XmlElement(name = "distributed-destination-connection")
    protected DistributedDestinationConnection distributedDestinationConnection;
    @XmlElement(name = "init-suspend-seconds")
    protected BigInteger initSuspendSeconds;
    @XmlElement(name = "max-suspend-seconds")
    protected BigInteger maxSuspendSeconds;
    @XmlElement(name = "security-plugin")
    protected SecurityPlugin securityPlugin;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the pool property.
     * 
     * @return
     *     possible object is
     *     {@link Pool }
     *     
     */
    public Pool getPool() {
        return pool;
    }

    /**
     * Sets the value of the pool property.
     * 
     * @param value
     *     allowed object is
     *     {@link Pool }
     *     
     */
    public void setPool(Pool value) {
        this.pool = value;
    }

    /**
     * Gets the value of the timerDescriptor property.
     * 
     * @return
     *     possible object is
     *     {@link TimerDescriptor }
     *     
     */
    public TimerDescriptor getTimerDescriptor() {
        return timerDescriptor;
    }

    /**
     * Sets the value of the timerDescriptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimerDescriptor }
     *     
     */
    public void setTimerDescriptor(TimerDescriptor value) {
        this.timerDescriptor = value;
    }

    /**
     * Gets the value of the resourceAdapterJndiName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResourceAdapterJndiName() {
        return resourceAdapterJndiName;
    }

    /**
     * Sets the value of the resourceAdapterJndiName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResourceAdapterJndiName(String value) {
        this.resourceAdapterJndiName = value;
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
     * Gets the value of the initialContextFactory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    /**
     * Sets the value of the initialContextFactory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInitialContextFactory(String value) {
        this.initialContextFactory = value;
    }

    /**
     * Gets the value of the providerUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderUrl() {
        return providerUrl;
    }

    /**
     * Sets the value of the providerUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderUrl(String value) {
        this.providerUrl = value;
    }

    /**
     * Gets the value of the connectionFactoryJndiName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionFactoryJndiName() {
        return connectionFactoryJndiName;
    }

    /**
     * Sets the value of the connectionFactoryJndiName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionFactoryJndiName(String value) {
        this.connectionFactoryJndiName = value;
    }

    /**
     * Gets the value of the destinationResourceLink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationResourceLink() {
        return destinationResourceLink;
    }

    /**
     * Sets the value of the destinationResourceLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationResourceLink(String value) {
        this.destinationResourceLink = value;
    }

    /**
     * Gets the value of the connectionFactoryResourceLink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionFactoryResourceLink() {
        return connectionFactoryResourceLink;
    }

    /**
     * Sets the value of the connectionFactoryResourceLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionFactoryResourceLink(String value) {
        this.connectionFactoryResourceLink = value;
    }

    /**
     * Gets the value of the jmsPollingIntervalSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getJmsPollingIntervalSeconds() {
        return jmsPollingIntervalSeconds;
    }

    /**
     * Sets the value of the jmsPollingIntervalSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setJmsPollingIntervalSeconds(BigInteger value) {
        this.jmsPollingIntervalSeconds = value;
    }

    /**
     * Gets the value of the jmsClientId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJmsClientId() {
        return jmsClientId;
    }

    /**
     * Sets the value of the jmsClientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJmsClientId(String value) {
        this.jmsClientId = value;
    }

    /**
     * Gets the value of the generateUniqueJmsClientId property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getGenerateUniqueJmsClientId() {
        return generateUniqueJmsClientId;
    }

    /**
     * Sets the value of the generateUniqueJmsClientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGenerateUniqueJmsClientId(Boolean value) {
        this.generateUniqueJmsClientId = value;
    }

    /**
     * Gets the value of the durableSubscriptionDeletion property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getDurableSubscriptionDeletion() {
        return durableSubscriptionDeletion;
    }

    /**
     * Sets the value of the durableSubscriptionDeletion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDurableSubscriptionDeletion(Boolean value) {
        this.durableSubscriptionDeletion = value;
    }

    /**
     * Gets the value of the maxMessagesInTransaction property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxMessagesInTransaction() {
        return maxMessagesInTransaction;
    }

    /**
     * Sets the value of the maxMessagesInTransaction property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxMessagesInTransaction(BigInteger value) {
        this.maxMessagesInTransaction = value;
    }

    /**
     * Gets the value of the distributedDestinationConnection property.
     * 
     * @return
     *     possible object is
     *     {@link DistributedDestinationConnection }
     *     
     */
    public DistributedDestinationConnection getDistributedDestinationConnection() {
        return distributedDestinationConnection;
    }

    /**
     * Sets the value of the distributedDestinationConnection property.
     * 
     * @param value
     *     allowed object is
     *     {@link DistributedDestinationConnection }
     *     
     */
    public void setDistributedDestinationConnection(DistributedDestinationConnection value) {
        this.distributedDestinationConnection = value;
    }

    /**
     * Gets the value of the initSuspendSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getInitSuspendSeconds() {
        return initSuspendSeconds;
    }

    /**
     * Sets the value of the initSuspendSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setInitSuspendSeconds(BigInteger value) {
        this.initSuspendSeconds = value;
    }

    /**
     * Gets the value of the maxSuspendSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxSuspendSeconds() {
        return maxSuspendSeconds;
    }

    /**
     * Sets the value of the maxSuspendSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxSuspendSeconds(BigInteger value) {
        this.maxSuspendSeconds = value;
    }

    /**
     * Gets the value of the securityPlugin property.
     * 
     * @return
     *     possible object is
     *     {@link SecurityPlugin }
     *     
     */
    public SecurityPlugin getSecurityPlugin() {
        return securityPlugin;
    }

    /**
     * Sets the value of the securityPlugin property.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityPlugin }
     *     
     */
    public void setSecurityPlugin(SecurityPlugin value) {
        this.securityPlugin = value;
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
