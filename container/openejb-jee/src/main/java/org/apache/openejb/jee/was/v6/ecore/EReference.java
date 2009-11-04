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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for EReference complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="EReference">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}EStructuralFeature">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="eOpposite" type="{http://www.eclipse.org/emf/2002/Ecore}EReference"/>
 *       &lt;/choice>
 *       &lt;attribute name="containment" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="eOpposite" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="resolveProxies" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EReference", propOrder = { "eReferenceEOpposites" })
public class EReference extends EStructuralFeature {

	@XmlElement(name = "eOpposite")
	protected List<EReference> eReferenceEOpposites;
	@XmlAttribute
	protected Boolean containment;
	@XmlAttribute
	protected String eOpposite;
	@XmlAttribute
	protected Boolean resolveProxies;

	/**
	 * Gets the value of the eReferenceEOpposites property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the eReferenceEOpposites property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getEReferenceEOpposites().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link EReference }
	 * 
	 * 
	 */
	public List<EReference> getEReferenceEOpposites() {
		if (eReferenceEOpposites == null) {
			eReferenceEOpposites = new ArrayList<EReference>();
		}
		return this.eReferenceEOpposites;
	}

	/**
	 * Gets the value of the containment property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isContainment() {
		return containment;
	}

	/**
	 * Sets the value of the containment property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setContainment(Boolean value) {
		this.containment = value;
	}

	/**
	 * Gets the value of the eOpposite property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getEOpposite() {
		return eOpposite;
	}

	/**
	 * Sets the value of the eOpposite property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setEOpposite(String value) {
		this.eOpposite = value;
	}

	/**
	 * Gets the value of the resolveProxies property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public Boolean isResolveProxies() {
		return resolveProxies;
	}

	/**
	 * Sets the value of the resolveProxies property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setResolveProxies(Boolean value) {
		this.resolveProxies = value;
	}

}
