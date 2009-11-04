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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for CompatibilityDescriptionGroup complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CompatibilityDescriptionGroup">
 *   &lt;complexContent>
 *     &lt;extension base="{common.xmi}DescriptionGroup">
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="largeIcon" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="smallIcon" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompatibilityDescriptionGroup")
public class CompatibilityDescriptionGroup extends DescriptionGroup {

	@XmlAttribute
	protected String description;
	@XmlAttribute
	protected String displayName;
	@XmlAttribute
	protected String largeIcon;
	@XmlAttribute
	protected String smallIcon;

	/**
	 * Gets the value of the description property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the value of the description property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDescription(String value) {
		this.description = value;
	}

	/**
	 * Gets the value of the displayName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the value of the displayName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDisplayName(String value) {
		this.displayName = value;
	}

	/**
	 * Gets the value of the largeIcon property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getLargeIcon() {
		return largeIcon;
	}

	/**
	 * Sets the value of the largeIcon property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setLargeIcon(String value) {
		this.largeIcon = value;
	}

	/**
	 * Gets the value of the smallIcon property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSmallIcon() {
		return smallIcon;
	}

	/**
	 * Sets the value of the smallIcon property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSmallIcon(String value) {
		this.smallIcon = value;
	}

}
