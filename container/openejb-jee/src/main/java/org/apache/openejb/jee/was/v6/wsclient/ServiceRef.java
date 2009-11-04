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
package org.apache.openejb.jee.was.v6.wsclient;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.openejb.jee.was.v6.common.CompatibilityDescriptionGroup;
import org.apache.openejb.jee.was.v6.common.QName;
import org.apache.openejb.jee.was.v6.java.JavaClass;

/**
 * The service-ref element declares a reference to a Web service. It contains
 * optional description, display name and icons, a declaration of the required
 * Service interface, an optional WSDL document location, an optional set of
 * JAX-RPC mappings, an optional QName for the service element, an optional set
 * of Service Endpoint Interfaces to be resolved by the container to a WSDL
 * port, and an optional set of handlers.
 * 
 * 
 * <p>
 * Java class for ServiceRef complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceRef">
 *   &lt;complexContent>
 *     &lt;extension base="{common.xmi}CompatibilityDescriptionGroup">
 *       &lt;choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="serviceInterface" type="{java.xmi}JavaClass"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="portComponentRefs" type="{webservice_client.xmi}PortComponentRef"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="handlers" type="{webservice_client.xmi}Handler"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="serviceQname" type="{common.xmi}QName"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *       &lt;attribute name="jaxrpcMappingFile" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="serviceInterface" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="serviceRefName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="wsdlFile" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceRef", propOrder = { "serviceInterfaces",
		"portComponentRefs", "handlers", "serviceQnames" })
public class ServiceRef extends CompatibilityDescriptionGroup {

	@XmlElement(name = "serviceInterface")
	protected List<JavaClass> serviceInterfaces;
	protected List<PortComponentRef> portComponentRefs;
	protected List<Handler> handlers;
	@XmlElement(name = "serviceQname")
	protected List<QName> serviceQnames;
	@XmlAttribute
	protected String jaxrpcMappingFile;
	@XmlAttribute(name = "serviceInterface")
	protected String serviceInterfaceString;
	@XmlAttribute
	protected String serviceRefName;
	@XmlAttribute
	protected String wsdlFile;

	/**
	 * Gets the value of the serviceInterfaces property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the serviceInterfaces property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getServiceInterfaces().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JavaClass }
	 * 
	 * 
	 */
	public List<JavaClass> getServiceInterfaces() {
		if (serviceInterfaces == null) {
			serviceInterfaces = new ArrayList<JavaClass>();
		}
		return this.serviceInterfaces;
	}

	/**
	 * Gets the value of the portComponentRefs property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the portComponentRefs property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPortComponentRefs().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link PortComponentRef }
	 * 
	 * 
	 */
	public List<PortComponentRef> getPortComponentRefs() {
		if (portComponentRefs == null) {
			portComponentRefs = new ArrayList<PortComponentRef>();
		}
		return this.portComponentRefs;
	}

	/**
	 * Gets the value of the handlers property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the handlers property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getHandlers().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Handler }
	 * 
	 * 
	 */
	public List<Handler> getHandlers() {
		if (handlers == null) {
			handlers = new ArrayList<Handler>();
		}
		return this.handlers;
	}

	/**
	 * Gets the value of the serviceQnames property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the serviceQnames property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getServiceQnames().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link QName }
	 * 
	 * 
	 */
	public List<QName> getServiceQnames() {
		if (serviceQnames == null) {
			serviceQnames = new ArrayList<QName>();
		}
		return this.serviceQnames;
	}

	/**
	 * Gets the value of the jaxrpcMappingFile property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getJaxrpcMappingFile() {
		return jaxrpcMappingFile;
	}

	/**
	 * Sets the value of the jaxrpcMappingFile property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setJaxrpcMappingFile(String value) {
		this.jaxrpcMappingFile = value;
	}

	/**
	 * Gets the value of the serviceInterfaceString property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getServiceInterfaceString() {
		return serviceInterfaceString;
	}

	/**
	 * Sets the value of the serviceInterfaceString property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setServiceInterfaceString(String value) {
		this.serviceInterfaceString = value;
	}

	/**
	 * Gets the value of the serviceRefName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getServiceRefName() {
		return serviceRefName;
	}

	/**
	 * Sets the value of the serviceRefName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setServiceRefName(String value) {
		this.serviceRefName = value;
	}

	/**
	 * Gets the value of the wsdlFile property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getWsdlFile() {
		return wsdlFile;
	}

	/**
	 * Sets the value of the wsdlFile property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setWsdlFile(String value) {
		this.wsdlFile = value;
	}

}
