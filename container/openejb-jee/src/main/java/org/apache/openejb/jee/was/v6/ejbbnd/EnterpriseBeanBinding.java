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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import org.apache.openejb.jee.was.v6.commonbnd.EjbRefBinding;
import org.apache.openejb.jee.was.v6.commonbnd.MessageDestinationRefBinding;
import org.apache.openejb.jee.was.v6.commonbnd.ResourceEnvRefBinding;
import org.apache.openejb.jee.was.v6.commonbnd.ResourceRefBinding;
import org.apache.openejb.jee.was.v6.ejb.EnterpriseBean;
import org.apache.openejb.jee.was.v6.webservice.clientbnd.ServiceRefBinding;
import org.apache.openejb.jee.was.v6.xmi.Extension;

/**
 * <p>
 * Java class for EnterpriseBeanBinding complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="EnterpriseBeanBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="enterpriseBean" type="{ejb.xmi}EnterpriseBean"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="datasource" type="{commonbnd.xmi}ResourceRefBinding"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="resRefBindings" type="{commonbnd.xmi}ResourceRefBinding"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="ejbRefBindings" type="{commonbnd.xmi}EjbRefBinding"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="resourceEnvRefBindings" type="{commonbnd.xmi}ResourceEnvRefBinding"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="cmpConnectionFactory" type="{ejbbnd.xmi}CMPConnectionFactoryBinding"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="serviceRefBindings" type="{webservice_clientbnd.xmi}ServiceRefBinding"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="messageDestinationRefBindings" type="{commonbnd.xmi}MessageDestinationRefBinding"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.omg.org/XMI}Extension"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.omg.org/XMI}ObjectAttribs"/>
 *       &lt;attribute name="ejbName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="enterpriseBean" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="jndiName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{http://www.omg.org/XMI}id"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnterpriseBeanBinding", propOrder = { "enterpriseBeans",
		"datasources", "resRefBindings", "ejbRefBindings",
		"resourceEnvRefBindings", "cmpConnectionFactories",
		"serviceRefBindings", "messageDestinationRefBindings", "extensions" })
public class EnterpriseBeanBinding {

	@XmlElement(name = "enterpriseBean")
	protected List<EnterpriseBean> enterpriseBeans;
	@XmlElement(name = "datasource")
	protected List<ResourceRefBinding> datasources;
	protected List<ResourceRefBinding> resRefBindings;
	protected List<EjbRefBinding> ejbRefBindings;
	protected List<ResourceEnvRefBinding> resourceEnvRefBindings;
	@XmlElement(name = "cmpConnectionFactory")
	protected List<CMPConnectionFactoryBinding> cmpConnectionFactories;
	protected List<ServiceRefBinding> serviceRefBindings;
	protected List<MessageDestinationRefBinding> messageDestinationRefBindings;
	@XmlElement(name = "Extension", namespace = "http://www.omg.org/XMI")
	protected List<Extension> extensions;
	@XmlAttribute
	protected String ejbName;
	@XmlAttribute
	protected String enterpriseBean;
	@XmlAttribute
	protected String jndiName;
	@XmlAttribute(namespace = "http://www.omg.org/XMI")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	protected String id;
	@XmlAttribute(namespace = "http://www.omg.org/XMI")
	protected QName type;
	@XmlAttribute(namespace = "http://www.omg.org/XMI")
	protected String version;
	@XmlAttribute
	protected String href;
	@XmlAttribute(namespace = "http://www.omg.org/XMI")
	@XmlIDREF
	protected Object idref;
	@XmlAttribute(namespace = "http://www.omg.org/XMI")
	protected String label;
	@XmlAttribute(namespace = "http://www.omg.org/XMI")
	protected String uuid;

	/**
	 * Gets the value of the enterpriseBeans property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the enterpriseBeans property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getEnterpriseBeans().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link EnterpriseBean }
	 * 
	 * 
	 */
	public List<EnterpriseBean> getEnterpriseBeans() {
		if (enterpriseBeans == null) {
			enterpriseBeans = new ArrayList<EnterpriseBean>();
		}
		return this.enterpriseBeans;
	}

	/**
	 * Gets the value of the datasources property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the datasources property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDatasources().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ResourceRefBinding }
	 * 
	 * 
	 */
	public List<ResourceRefBinding> getDatasources() {
		if (datasources == null) {
			datasources = new ArrayList<ResourceRefBinding>();
		}
		return this.datasources;
	}

	/**
	 * Gets the value of the resRefBindings property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the resRefBindings property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getResRefBindings().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ResourceRefBinding }
	 * 
	 * 
	 */
	public List<ResourceRefBinding> getResRefBindings() {
		if (resRefBindings == null) {
			resRefBindings = new ArrayList<ResourceRefBinding>();
		}
		return this.resRefBindings;
	}

	/**
	 * Gets the value of the ejbRefBindings property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the ejbRefBindings property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getEjbRefBindings().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link EjbRefBinding }
	 * 
	 * 
	 */
	public List<EjbRefBinding> getEjbRefBindings() {
		if (ejbRefBindings == null) {
			ejbRefBindings = new ArrayList<EjbRefBinding>();
		}
		return this.ejbRefBindings;
	}

	/**
	 * Gets the value of the resourceEnvRefBindings property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the resourceEnvRefBindings property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getResourceEnvRefBindings().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ResourceEnvRefBinding }
	 * 
	 * 
	 */
	public List<ResourceEnvRefBinding> getResourceEnvRefBindings() {
		if (resourceEnvRefBindings == null) {
			resourceEnvRefBindings = new ArrayList<ResourceEnvRefBinding>();
		}
		return this.resourceEnvRefBindings;
	}

	/**
	 * Gets the value of the cmpConnectionFactories property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the cmpConnectionFactories property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCmpConnectionFactories().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link CMPConnectionFactoryBinding }
	 * 
	 * 
	 */
	public List<CMPConnectionFactoryBinding> getCmpConnectionFactories() {
		if (cmpConnectionFactories == null) {
			cmpConnectionFactories = new ArrayList<CMPConnectionFactoryBinding>();
		}
		return this.cmpConnectionFactories;
	}

	/**
	 * Gets the value of the serviceRefBindings property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the serviceRefBindings property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getServiceRefBindings().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link ServiceRefBinding }
	 * 
	 * 
	 */
	public List<ServiceRefBinding> getServiceRefBindings() {
		if (serviceRefBindings == null) {
			serviceRefBindings = new ArrayList<ServiceRefBinding>();
		}
		return this.serviceRefBindings;
	}

	/**
	 * Gets the value of the messageDestinationRefBindings property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the messageDestinationRefBindings property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMessageDestinationRefBindings().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link MessageDestinationRefBinding }
	 * 
	 * 
	 */
	public List<MessageDestinationRefBinding> getMessageDestinationRefBindings() {
		if (messageDestinationRefBindings == null) {
			messageDestinationRefBindings = new ArrayList<MessageDestinationRefBinding>();
		}
		return this.messageDestinationRefBindings;
	}

	/**
	 * Gets the value of the extensions property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the extensions property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getExtensions().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Extension }
	 * 
	 * 
	 */
	public List<Extension> getExtensions() {
		if (extensions == null) {
			extensions = new ArrayList<Extension>();
		}
		return this.extensions;
	}

	/**
	 * Gets the value of the ejbName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEjbName() {
		return ejbName;
	}

	/**
	 * Sets the value of the ejbName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEjbName(String value) {
		this.ejbName = value;
	}

	/**
	 * Gets the value of the enterpriseBean property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEnterpriseBean() {
		return enterpriseBean;
	}

	/**
	 * Sets the value of the enterpriseBean property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEnterpriseBean(String value) {
		this.enterpriseBean = value;
	}

	/**
	 * Gets the value of the jndiName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getJndiName() {
		return jndiName;
	}

	/**
	 * Sets the value of the jndiName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setJndiName(String value) {
		this.jndiName = value;
	}

	/**
	 * Gets the value of the id property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setId(String value) {
		this.id = value;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link QName }
	 * 
	 */
	public QName getType() {
		return type;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value
	 *            allowed object is {@link QName }
	 * 
	 */
	public void setType(QName value) {
		this.type = value;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getVersion() {
		if (version == null) {
			return "2.0";
		} else {
			return version;
		}
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setVersion(String value) {
		this.version = value;
	}

	/**
	 * Gets the value of the href property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getHref() {
		return href;
	}

	/**
	 * Sets the value of the href property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setHref(String value) {
		this.href = value;
	}

	/**
	 * Gets the value of the idref property.
	 * 
	 * @return possible object is {@link Object }
	 * 
	 */
	public Object getIdref() {
		return idref;
	}

	/**
	 * Sets the value of the idref property.
	 * 
	 * @param value
	 *            allowed object is {@link Object }
	 * 
	 */
	public void setIdref(Object value) {
		this.idref = value;
	}

	/**
	 * Gets the value of the label property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the value of the label property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLabel(String value) {
		this.label = value;
	}

	/**
	 * Gets the value of the uuid property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Sets the value of the uuid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUuid(String value) {
		this.uuid = value;
	}

}
