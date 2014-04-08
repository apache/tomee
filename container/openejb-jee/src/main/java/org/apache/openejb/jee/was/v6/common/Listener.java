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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.openejb.jee.was.v6.java.JavaClass;

/**
 * 
 * @since J2EE1.4 moved from webapp
 * 
 *        Declares a class in the application must be registered as a web
 *        application listener bean.
 * 
 * 
 *        <p>
 *        Java class for Listener complex type.
 * 
 *        <p>
 *        The following schema fragment specifies the expected content contained
 *        within this class.
 * 
 *        <pre>
 * &lt;complexType name="Listener">
 *   &lt;complexContent>
 *     &lt;extension base="{common.xmi}CompatibilityDescriptionGroup">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="listenerClass" type="{java.xmi}JavaClass"/>
 *       &lt;/choice>
 *       &lt;attribute name="listenerClass" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Listener", propOrder = { "listenerClasses" })
public class Listener extends CompatibilityDescriptionGroup {

	@XmlElement(name = "listenerClass")
	protected List<JavaClass> listenerClasses;
	@XmlAttribute(name = "listenerClass")
	protected String listenerClassString;

	/**
	 * Gets the value of the listenerClasses property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the listenerClasses property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getListenerClasses().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link JavaClass }
	 * 
	 * 
	 */
	public List<JavaClass> getListenerClasses() {
		if (listenerClasses == null) {
			listenerClasses = new ArrayList<JavaClass>();
		}
		return this.listenerClasses;
	}

	/**
	 * Gets the value of the listenerClassString property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getListenerClassString() {
		return listenerClassString;
	}

	/**
	 * Sets the value of the listenerClassString property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setListenerClassString(String value) {
		this.listenerClassString = value;
	}

}
