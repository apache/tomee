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
package org.apache.openejb.jee.was.v6.ejbbnd;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * Binding information for MessageDrivenBeans.
 * 
 * 
 * <p>
 * Java class for MessageDrivenBeanBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="MessageDrivenBeanBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{ejbbnd.xmi}EnterpriseBeanBinding">
 *       &lt;attribute name="activationSpecAuthAlias" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="activationSpecJndiName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="destinationJndiName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="listenerInputPortName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageDrivenBeanBinding")
public class MessageDrivenBeanBinding extends EnterpriseBeanBinding {

	@XmlAttribute
	protected String activationSpecAuthAlias;
	@XmlAttribute
	protected String activationSpecJndiName;
	@XmlAttribute
	protected String destinationJndiName;
	@XmlAttribute
	protected String listenerInputPortName;

	/**
	 * Gets the value of the activationSpecAuthAlias property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getActivationSpecAuthAlias() {
		return activationSpecAuthAlias;
	}

	/**
	 * Sets the value of the activationSpecAuthAlias property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setActivationSpecAuthAlias(String value) {
		this.activationSpecAuthAlias = value;
	}

	/**
	 * Gets the value of the activationSpecJndiName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getActivationSpecJndiName() {
		return activationSpecJndiName;
	}

	/**
	 * Sets the value of the activationSpecJndiName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setActivationSpecJndiName(String value) {
		this.activationSpecJndiName = value;
	}

	/**
	 * Gets the value of the destinationJndiName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDestinationJndiName() {
		return destinationJndiName;
	}

	/**
	 * Sets the value of the destinationJndiName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDestinationJndiName(String value) {
		this.destinationJndiName = value;
	}

	/**
	 * Gets the value of the listenerInputPortName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getListenerInputPortName() {
		return listenerInputPortName;
	}

	/**
	 * Sets the value of the listenerInputPortName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setListenerInputPortName(String value) {
		this.listenerInputPortName = value;
	}

}
