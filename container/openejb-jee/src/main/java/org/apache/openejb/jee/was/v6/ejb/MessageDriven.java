/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.openejb.jee.was.v6.ejb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.openejb.jee.was.v6.java.JavaClass;

/**
 * 
 * The message-driven element declares a message-driven bean. The declaration
 * consists of:
 * 
 * - an optional description - an optional display name - an optional icon
 * element that contains a small and a large icon file name. - a name assigned
 * to the enterprise bean in the deployment descriptor - the message-driven
 * bean's implementation class - an optional declaration of the bean's messaging
 * type - the message-driven bean's transaction management type - an optional
 * declaration of the bean's message-destination-type - an optional declaration
 * of the bean's message-destination-link - an optional declaration of the
 * message-driven bean's activation configuration properties - an optional
 * declaration of the bean's environment entries - an optional declaration of
 * the bean's EJB references - an optional declaration of the bean's local EJB
 * references - an optional declaration of the bean's web service references -
 * an optional declaration of the security identity to be used for the execution
 * of the bean's methods - an optional declaration of the bean's resource
 * manager connection factory references - an optional declaration of the bean's
 * resource environment references. - an optional declaration of the bean's
 * message destination references
 * 
 * 
 * <p>
 * Java class for MessageDriven complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="MessageDriven">
 *   &lt;complexContent>
 *     &lt;extension base="{ejb.xmi}EnterpriseBean">
 *       &lt;choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="destination" type="{ejb.xmi}MessageDrivenDestination"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="activationConfig" type="{ejb.xmi}ActivationConfig"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="messageDestination" type="{java.xmi}JavaClass"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="messagingType" type="{java.xmi}JavaClass"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *       &lt;attribute name="acknowledgeMode" type="{ejb.xmi}AcknowledgeMode" />
 *       &lt;attribute name="link" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="messageDestination" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="messageSelector" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="messagingType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="transactionType" type="{ejb.xmi}TransactionType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageDriven", propOrder = { "destinations",
		"activationConfigs", "messageDestinations", "messagingTypes" })
public class MessageDriven extends EnterpriseBean {

	@XmlElement(name = "destination")
	protected List<MessageDrivenDestination> destinations;
	@XmlElement(name = "activationConfig")
	protected List<ActivationConfig> activationConfigs;
	@XmlElement(name = "messageDestination")
	protected List<JavaClass> messageDestinations;
	@XmlElement(name = "messagingType")
	protected List<JavaClass> messagingTypes;
	@XmlAttribute
	protected AcknowledgeModeEnum acknowledgeMode;
	@XmlAttribute
	protected String link;
	@XmlAttribute
	protected String messageDestination;
	@XmlAttribute
	protected String messageSelector;
	@XmlAttribute
	protected String messagingType;
	@XmlAttribute
	protected TransactionEnum transactionType;

	/**
	 * Gets the value of the destinations property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the destinations property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDestinations().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link MessageDrivenDestination }
	 * 
	 * 
	 */
	public List<MessageDrivenDestination> getDestinations() {
		if (destinations == null) {
			destinations = new ArrayList<MessageDrivenDestination>();
		}
		return this.destinations;
	}

	/**
	 * Gets the value of the activationConfigs property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the activationConfigs property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getActivationConfigs().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ActivationConfig }
	 * 
	 * 
	 */
	public List<ActivationConfig> getActivationConfigs() {
		if (activationConfigs == null) {
			activationConfigs = new ArrayList<ActivationConfig>();
		}
		return this.activationConfigs;
	}

	/**
	 * Gets the value of the messageDestinations property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the messageDestinations property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMessageDestinations().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JavaClass }
	 * 
	 * 
	 */
	public List<JavaClass> getMessageDestinations() {
		if (messageDestinations == null) {
			messageDestinations = new ArrayList<JavaClass>();
		}
		return this.messageDestinations;
	}

	/**
	 * Gets the value of the messagingTypes property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the messagingTypes property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMessagingTypes().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JavaClass }
	 * 
	 * 
	 */
	public List<JavaClass> getMessagingTypes() {
		if (messagingTypes == null) {
			messagingTypes = new ArrayList<JavaClass>();
		}
		return this.messagingTypes;
	}

	/**
	 * Gets the value of the acknowledgeMode property.
	 * 
	 * @return possible object is {@link AcknowledgeModeEnum }
	 * 
	 */
	public AcknowledgeModeEnum getAcknowledgeMode() {
		return acknowledgeMode;
	}

	/**
	 * Sets the value of the acknowledgeMode property.
	 * 
	 * @param value
	 *            allowed object is {@link AcknowledgeModeEnum }
	 * 
	 */
	public void setAcknowledgeMode(AcknowledgeModeEnum value) {
		this.acknowledgeMode = value;
	}

	/**
	 * Gets the value of the link property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Sets the value of the link property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLink(String value) {
		this.link = value;
	}

	/**
	 * Gets the value of the messageDestination property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMessageDestination() {
		return messageDestination;
	}

	/**
	 * Sets the value of the messageDestination property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMessageDestination(String value) {
		this.messageDestination = value;
	}

	/**
	 * Gets the value of the messageSelector property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMessageSelector() {
		return messageSelector;
	}

	/**
	 * Sets the value of the messageSelector property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMessageSelector(String value) {
		this.messageSelector = value;
	}

	/**
	 * Gets the value of the messagingType property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMessagingType() {
		return messagingType;
	}

	/**
	 * Sets the value of the messagingType property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMessagingType(String value) {
		this.messagingType = value;
	}

	/**
	 * Gets the value of the transactionType property.
	 * 
	 * @return possible object is {@link TransactionEnum }
	 * 
	 */
	public TransactionEnum getTransactionType() {
		return transactionType;
	}

	/**
	 * Sets the value of the transactionType property.
	 * 
	 * @param value
	 *            allowed object is {@link TransactionEnum }
	 * 
	 */
	public void setTransactionType(TransactionEnum value) {
		this.transactionType = value;
	}

}
