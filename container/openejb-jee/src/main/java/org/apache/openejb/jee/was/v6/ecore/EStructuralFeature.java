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
package org.apache.openejb.jee.was.v6.ecore;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for EStructuralFeature complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="EStructuralFeature">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}ETypedElement">
 *       &lt;attribute name="changeable" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="defaultValueLiteral" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="derived" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="transient" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="unsettable" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="volatile" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EStructuralFeature")
public class EStructuralFeature extends ETypedElement {

	@XmlAttribute
	protected Boolean changeable;
	@XmlAttribute
	protected String defaultValueLiteral;
	@XmlAttribute
	protected Boolean derived;
	@XmlAttribute(name = "transient")
	protected Boolean isTransient;
	@XmlAttribute
	protected Boolean unsettable;
	@XmlAttribute(name = "volatile")
	protected Boolean isVolatile;

	/**
	 * Gets the value of the changeable property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isChangeable() {
		return changeable;
	}

	/**
	 * Sets the value of the changeable property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setChangeable(Boolean value) {
		this.changeable = value;
	}

	/**
	 * Gets the value of the defaultValueLiteral property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDefaultValueLiteral() {
		return defaultValueLiteral;
	}

	/**
	 * Sets the value of the defaultValueLiteral property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDefaultValueLiteral(String value) {
		this.defaultValueLiteral = value;
	}

	/**
	 * Gets the value of the derived property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isDerived() {
		return derived;
	}

	/**
	 * Sets the value of the derived property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setDerived(Boolean value) {
		this.derived = value;
	}

	/**
	 * Gets the value of the isTransient property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isIsTransient() {
		return isTransient;
	}

	/**
	 * Sets the value of the isTransient property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setIsTransient(Boolean value) {
		this.isTransient = value;
	}

	/**
	 * Gets the value of the unsettable property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isUnsettable() {
		return unsettable;
	}

	/**
	 * Sets the value of the unsettable property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setUnsettable(Boolean value) {
		this.unsettable = value;
	}

	/**
	 * Gets the value of the isVolatile property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isIsVolatile() {
		return isVolatile;
	}

	/**
	 * Sets the value of the isVolatile property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setIsVolatile(Boolean value) {
		this.isVolatile = value;
	}

}
