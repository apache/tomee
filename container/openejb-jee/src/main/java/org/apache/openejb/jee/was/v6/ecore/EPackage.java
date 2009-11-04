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
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for EPackage complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="EPackage">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.eclipse.org/emf/2002/Ecore}ENamedElement">
 *       &lt;choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="eClassifiers" type="{http://www.eclipse.org/emf/2002/Ecore}EClassifier"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="eSubpackages" type="{http://www.eclipse.org/emf/2002/Ecore}EPackage"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *       &lt;attribute name="nsPrefix" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="nsURI" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EPackage", propOrder = { "eClassifiers", "eSubpackages" })
public class EPackage extends ENamedElement {

	protected List<EClassifier> eClassifiers;
	protected List<EPackage> eSubpackages;
	@XmlAttribute
	protected String nsPrefix;
	@XmlAttribute
	protected String nsURI;

	/**
	 * Gets the value of the eClassifiers property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the eClassifiers property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getEClassifiers().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link EClassifier }
	 * 
	 * 
	 */
	public List<EClassifier> getEClassifiers() {
		if (eClassifiers == null) {
			eClassifiers = new ArrayList<EClassifier>();
		}
		return this.eClassifiers;
	}

	/**
	 * Gets the value of the eSubpackages property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the eSubpackages property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getESubpackages().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link EPackage }
	 * 
	 * 
	 */
	public List<EPackage> getESubpackages() {
		if (eSubpackages == null) {
			eSubpackages = new ArrayList<EPackage>();
		}
		return this.eSubpackages;
	}

	/**
	 * Gets the value of the nsPrefix property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNsPrefix() {
		return nsPrefix;
	}

	/**
	 * Sets the value of the nsPrefix property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNsPrefix(String value) {
		this.nsPrefix = value;
	}

	/**
	 * Gets the value of the nsURI property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getNsURI() {
		return nsURI;
	}

	/**
	 * Sets the value of the nsURI property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setNsURI(String value) {
		this.nsURI = value;
	}

}
