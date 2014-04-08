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

/**
 * <p>
 * Java class for ContainerManagedEntity complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="ContainerManagedEntity">
 *   &lt;complexContent>
 *     &lt;extension base="{ejb.xmi}Entity">
 *       &lt;choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="persistentAttributes" type="{ejb.xmi}CMPAttribute"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="keyAttributes" type="{ejb.xmi}CMPAttribute"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="queries" type="{ejb.xmi}Query"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="CMPAttribute" type="{ejb.xmi}CMPAttribute"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="primKeyField" type="{ejb.xmi}CMPAttribute"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *       &lt;attribute name="CMPAttribute" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="abstractSchemaName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="keyAttributes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="primKeyField" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContainerManagedEntity", propOrder = { "persistentAttributes",
		"keyAttributes", "queries", "cmpAttributes", "primKeyFields" })
public class ContainerManagedEntity extends Entity {

	protected List<CMPAttribute> persistentAttributes;
	protected List<CMPAttribute> keyAttributes;
	protected List<Query> queries;
	@XmlElement(name = "CMPAttribute")
	protected List<CMPAttribute> cmpAttributes;
	@XmlElement(name = "primKeyField")
	protected List<CMPAttribute> primKeyFields;
	@XmlAttribute(name = "CMPAttribute")
	protected String cmpAttribute;
	@XmlAttribute
	protected String abstractSchemaName;
	@XmlAttribute(name = "keyAttributes")
	protected String keyAttributesString;
	@XmlAttribute
	protected String primKeyField;
	@XmlAttribute(name = "version")
	protected String entityBeanVersion;

	/**
	 * Gets the value of the persistentAttributes property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the persistentAttributes property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPersistentAttributes().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link CMPAttribute }
	 * 
	 * 
	 */
	public List<CMPAttribute> getPersistentAttributes() {
		if (persistentAttributes == null) {
			persistentAttributes = new ArrayList<CMPAttribute>();
		}
		return this.persistentAttributes;
	}

	/**
	 * Gets the value of the keyAttributes property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the keyAttributes property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getKeyAttributes().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link CMPAttribute }
	 * 
	 * 
	 */
	public List<CMPAttribute> getKeyAttributes() {
		if (keyAttributes == null) {
			keyAttributes = new ArrayList<CMPAttribute>();
		}
		return this.keyAttributes;
	}

	/**
	 * Gets the value of the queries property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the queries property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getQueries().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Query }
	 * 
	 * 
	 */
	public List<Query> getQueries() {
		if (queries == null) {
			queries = new ArrayList<Query>();
		}
		return this.queries;
	}

	/**
	 * Gets the value of the cmpAttributes property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the cmpAttributes property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCMPAttributes().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link CMPAttribute }
	 * 
	 * 
	 */
	public List<CMPAttribute> getCMPAttributes() {
		if (cmpAttributes == null) {
			cmpAttributes = new ArrayList<CMPAttribute>();
		}
		return this.cmpAttributes;
	}

	/**
	 * Gets the value of the primKeyFields property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the primKeyFields property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPrimKeyFields().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link CMPAttribute }
	 * 
	 * 
	 */
	public List<CMPAttribute> getPrimKeyFields() {
		if (primKeyFields == null) {
			primKeyFields = new ArrayList<CMPAttribute>();
		}
		return this.primKeyFields;
	}

	/**
	 * Gets the value of the cmpAttribute property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCMPAttribute() {
		return cmpAttribute;
	}

	/**
	 * Sets the value of the cmpAttribute property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCMPAttribute(String value) {
		this.cmpAttribute = value;
	}

	/**
	 * Gets the value of the abstractSchemaName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getAbstractSchemaName() {
		return abstractSchemaName;
	}

	/**
	 * Sets the value of the abstractSchemaName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setAbstractSchemaName(String value) {
		this.abstractSchemaName = value;
	}

	/**
	 * Gets the value of the keyAttributesString property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getKeyAttributesString() {
		return keyAttributesString;
	}

	/**
	 * Sets the value of the keyAttributesString property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setKeyAttributesString(String value) {
		this.keyAttributesString = value;
	}

	/**
	 * Gets the value of the primKeyField property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPrimKeyField() {
		return primKeyField;
	}

	/**
	 * Sets the value of the primKeyField property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPrimKeyField(String value) {
		this.primKeyField = value;
	}

	/**
	 * Gets the value of the entityBeanVersion property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEntityBeanVersion() {
		return entityBeanVersion;
	}

	/**
	 * Sets the value of the entityBeanVersion property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEntityBeanVersion(String value) {
		this.entityBeanVersion = value;
	}

}
