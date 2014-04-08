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
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import org.apache.openejb.jee.was.v6.xmi.Extension;

/**
 * 
 * @since J2EE1.4 This group keeps the usage of the contained description
 *        related elements consistent across J2EE deployment descriptors.
 * 
 * 
 *        <p>
 *        Java class for DescriptionGroup complex type.
 * 
 *        <p>
 *        The following schema fragment specifies the expected content contained
 *        within this class.
 * 
 *        <pre>
 * &lt;complexType name="DescriptionGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="icons" type="{common.xmi}IconType"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="displayNames" type="{common.xmi}DisplayName"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="descriptions" type="{common.xmi}Description"/>
 *         &lt;/choice>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.omg.org/XMI}Extension"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.omg.org/XMI}ObjectAttribs"/>
 *       &lt;attribute ref="{http://www.omg.org/XMI}id"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescriptionGroup", propOrder = { "icons", "displayNames",
		"descriptions", "extensions" })
public class DescriptionGroup {

	protected List<IconType> icons;
	protected List<DisplayName> displayNames;
	protected List<Description> descriptions;
	@XmlElement(name = "Extension", namespace = "http://www.omg.org/XMI")
	protected List<Extension> extensions;
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
	 * Gets the value of the icons property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the icons property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getIcons().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link IconType }
	 * 
	 * 
	 */
	public List<IconType> getIcons() {
		if (icons == null) {
			icons = new ArrayList<IconType>();
		}
		return this.icons;
	}

	/**
	 * Gets the value of the displayNames property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the displayNames property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDisplayNames().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DisplayName }
	 * 
	 * 
	 */
	public List<DisplayName> getDisplayNames() {
		if (displayNames == null) {
			displayNames = new ArrayList<DisplayName>();
		}
		return this.displayNames;
	}

	/**
	 * Gets the value of the descriptions property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the descriptions property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDescriptions().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Description }
	 * 
	 * 
	 */
	public List<Description> getDescriptions() {
		if (descriptions == null) {
			descriptions = new ArrayList<Description>();
		}
		return this.descriptions;
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
