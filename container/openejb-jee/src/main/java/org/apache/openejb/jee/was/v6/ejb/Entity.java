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
 * The entity element declares an entity bean. The declaration consists of: an
 * optional description; optional display name; optional small icon file name;
 * optional large icon file name; a unique name assigned to the enterprise bean
 * in the deployment descriptor; the names of the entity bean's home and remote
 * interfaces, if any; the names of the entity bean's local home and local
 * interface, if any; the entity bean's implementation class; the entity bean's
 * persistence management type; the entity bean's primary key class name; an
 * indication of the entity bean's reentrancy; an optional specification of the
 * entity bean's cmp-version; an optional specification of the entity bean's
 * abstract schema name; an optional list of container-managed fields; an
 * optional specification of the primary key field; an optional declaration of
 * the bean's environment entries; an optional declaration of the bean's EJB
 * references; an optional declaration of the bean's local EJB references; an
 * optional declaration of the security role references; an optional declaration
 * of the security identity to be used for the execution of the bean's methods;
 * an optional declaration of the bean's resource manager connection factory
 * references; an optional declaration of the bean's resource environment
 * references; an optional set of query declarations for finder and select
 * methods for an entity bean with cmp-version 2.x. The optional
 * abstract-schema-name element must be specified for an entity bean with
 * container managed persistence and cmp-version 2.x. The optional primkey-field
 * may be present in the descriptor if the entity's persistence-type is
 * Container. The optional cmp-version element may be present in the descriptor
 * if the entity's persistence-type is Container. If the persistence-type is
 * Container and the cmp-version element is not specified, its value defaults to
 * 2.x. The optional home and remote elements must be specified if the entity
 * bean cmp-version is 1.x. The optional local-home and local elements must be
 * specified if the entity bean has a local home and local interface. The
 * optional query elements must be present if the persistence-type is Container
 * and the cmp-version is 2.x and query methods other than findByPrimaryKey have
 * been defined for the entity bean. The other elements that are optional are
 * "optional" in the sense that they are omitted if the lists represented by
 * them are empty. At least one cmp-field element must be present in the
 * descriptor if the entity's persistence-type is Container and the cmp-version
 * is 1.x, and none must not be present if the entity's persistence-type is
 * Bean.
 * 
 * 
 * <p>
 * Java class for Entity complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="Entity">
 *   &lt;complexContent>
 *     &lt;extension base="{ejb.xmi}EnterpriseBean">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="primaryKey" type="{java.xmi}JavaClass"/>
 *       &lt;/choice>
 *       &lt;attribute name="primaryKey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="reentrant" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Entity", propOrder = { "primaryKeys" })
public class Entity extends EnterpriseBean {

	@XmlElement(name = "primaryKey")
	protected List<JavaClass> primaryKeys;
	@XmlAttribute
	protected String primaryKey;
	@XmlAttribute
	protected Boolean reentrant;

	/**
	 * Gets the value of the primaryKeys property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the primaryKeys property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getPrimaryKeys().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JavaClass }
	 * 
	 * 
	 */
	public List<JavaClass> getPrimaryKeys() {
		if (primaryKeys == null) {
			primaryKeys = new ArrayList<JavaClass>();
		}
		return this.primaryKeys;
	}

	/**
	 * Gets the value of the primaryKey property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * Sets the value of the primaryKey property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPrimaryKey(String value) {
		this.primaryKey = value;
	}

	/**
	 * Gets the value of the reentrant property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isReentrant() {
		return reentrant;
	}

	/**
	 * Sets the value of the reentrant property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setReentrant(Boolean value) {
		this.reentrant = value;
	}

}
