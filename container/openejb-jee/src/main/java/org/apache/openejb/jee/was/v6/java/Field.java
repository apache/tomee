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
package org.apache.openejb.jee.was.v6.java;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.openejb.jee.was.v6.ecore.ETypedElement;

/**
 * <p>
 * Java class for Field complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="Field">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}ETypedElement">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="initializer" type="{java.xmi}Block"/>
 *       &lt;/choice>
 *       &lt;attribute name="final" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="javaVisibility" type="{java.xmi}JavaVisibilityKind" />
 *       &lt;attribute name="static" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="transient" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="volatile" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Field", propOrder = { "initializers" })
public class Field extends ETypedElement {

	@XmlElement(name = "initializer")
	protected List<Block> initializers;
	@XmlAttribute(name = "final")
	protected Boolean isFinal;
	@XmlAttribute
	protected JavaVisibilityEnum javaVisibility;
	@XmlAttribute(name = "static")
	protected Boolean isStatic;
	@XmlAttribute(name = "transient")
	protected Boolean isTransient;
	@XmlAttribute(name = "volatile")
	protected Boolean isVolatile;

	/**
	 * Gets the value of the initializers property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the initializers property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getInitializers().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Block }
	 * 
	 * 
	 */
	public List<Block> getInitializers() {
		if (initializers == null) {
			initializers = new ArrayList<Block>();
		}
		return this.initializers;
	}

	/**
	 * Gets the value of the isFinal property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isIsFinal() {
		return isFinal;
	}

	/**
	 * Sets the value of the isFinal property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setIsFinal(Boolean value) {
		this.isFinal = value;
	}

	/**
	 * Gets the value of the javaVisibility property.
	 * 
	 * @return possible object is {@link JavaVisibilityEnum }
	 * 
	 */
	public JavaVisibilityEnum getJavaVisibility() {
		return javaVisibility;
	}

	/**
	 * Sets the value of the javaVisibility property.
	 * 
	 * @param value
	 *            allowed object is {@link JavaVisibilityEnum }
	 * 
	 */
	public void setJavaVisibility(JavaVisibilityEnum value) {
		this.javaVisibility = value;
	}

	/**
	 * Gets the value of the isStatic property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isIsStatic() {
		return isStatic;
	}

	/**
	 * Sets the value of the isStatic property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setIsStatic(Boolean value) {
		this.isStatic = value;
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
