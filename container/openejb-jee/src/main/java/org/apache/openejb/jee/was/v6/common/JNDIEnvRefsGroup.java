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
package org.apache.openejb.jee.was.v6.common;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.apache.openejb.jee.was.v6.wsclient.ServiceRef;

/**
 * 
 * @since J2EE1.4 This group keeps the usage of the contained JNDI environment
 *        reference elements consistent across J2EE deployment descriptors.
 * 
 * 
 *        <p>
 *        Java class for JNDIEnvRefsGroup complex type.
 * 
 *        <p>
 *        The following schema fragment specifies the expected content contained
 *        within this class.
 * 
 *        <pre>
 * &lt;complexType name="JNDIEnvRefsGroup">
 *   &lt;complexContent>
 *     &lt;extension base="{common.xmi}CompatibilityDescriptionGroup">
 *       &lt;choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="environmentProperties" type="{common.xmi}EnvEntry"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="resourceRefs" type="{common.xmi}ResourceRef"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="ejbRefs" type="{common.xmi}EjbRef"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="resourceEnvRefs" type="{common.xmi}ResourceEnvRef"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="ejbLocalRefs" type="{common.xmi}EJBLocalRef"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="messageDestinationRefs" type="{common.xmi}MessageDestinationRef"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="serviceRefs" type="{webservice_client.xmi}ServiceRef"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JNDIEnvRefsGroup", propOrder = { "environmentProperties",
		"resourceRefs", "ejbRefs", "resourceEnvRefs", "ejbLocalRefs",
		"messageDestinationRefs", "serviceRefs" })
public class JNDIEnvRefsGroup extends CompatibilityDescriptionGroup {

	protected List<EnvEntry> environmentProperties;
	protected List<ResourceRef> resourceRefs;
	protected List<EjbRef> ejbRefs;
	protected List<ResourceEnvRef> resourceEnvRefs;
	protected List<EJBLocalRef> ejbLocalRefs;
	protected List<MessageDestinationRef> messageDestinationRefs;
	protected List<ServiceRef> serviceRefs;

	/**
	 * Gets the value of the environmentProperties property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the environmentProperties property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getEnvironmentProperties().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link EnvEntry }
	 * 
	 * 
	 */
	public List<EnvEntry> getEnvironmentProperties() {
		if (environmentProperties == null) {
			environmentProperties = new ArrayList<EnvEntry>();
		}
		return this.environmentProperties;
	}

	/**
	 * Gets the value of the resourceRefs property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the resourceRefs property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getResourceRefs().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ResourceRef }
	 * 
	 * 
	 */
	public List<ResourceRef> getResourceRefs() {
		if (resourceRefs == null) {
			resourceRefs = new ArrayList<ResourceRef>();
		}
		return this.resourceRefs;
	}

	/**
	 * Gets the value of the ejbRefs property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the ejbRefs property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getEjbRefs().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link EjbRef }
	 * 
	 * 
	 */
	public List<EjbRef> getEjbRefs() {
		if (ejbRefs == null) {
			ejbRefs = new ArrayList<EjbRef>();
		}
		return this.ejbRefs;
	}

	/**
	 * Gets the value of the resourceEnvRefs property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the resourceEnvRefs property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getResourceEnvRefs().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ResourceEnvRef }
	 * 
	 * 
	 */
	public List<ResourceEnvRef> getResourceEnvRefs() {
		if (resourceEnvRefs == null) {
			resourceEnvRefs = new ArrayList<ResourceEnvRef>();
		}
		return this.resourceEnvRefs;
	}

	/**
	 * Gets the value of the ejbLocalRefs property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the ejbLocalRefs property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getEjbLocalRefs().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link EJBLocalRef }
	 * 
	 * 
	 */
	public List<EJBLocalRef> getEjbLocalRefs() {
		if (ejbLocalRefs == null) {
			ejbLocalRefs = new ArrayList<EJBLocalRef>();
		}
		return this.ejbLocalRefs;
	}

	/**
	 * Gets the value of the messageDestinationRefs property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the messageDestinationRefs property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMessageDestinationRefs().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link MessageDestinationRef }
	 * 
	 * 
	 */
	public List<MessageDestinationRef> getMessageDestinationRefs() {
		if (messageDestinationRefs == null) {
			messageDestinationRefs = new ArrayList<MessageDestinationRef>();
		}
		return this.messageDestinationRefs;
	}

	/**
	 * Gets the value of the serviceRefs property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the serviceRefs property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getServiceRefs().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ServiceRef }
	 * 
	 * 
	 */
	public List<ServiceRef> getServiceRefs() {
		if (serviceRefs == null) {
			serviceRefs = new ArrayList<ServiceRef>();
		}
		return this.serviceRefs;
	}

}
