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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * The security-identity element specifies whether the caller's security
 * identity is to be used for the execution of the methods of the enterprise
 * bean or whether a specific run-as identity is to be used. It contains an
 * optional description and a specification of the security identity to be used.
 * 
 * 
 * <p>
 * Java class for RunAsSpecifiedIdentity complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="RunAsSpecifiedIdentity">
 *   &lt;complexContent>
 *     &lt;extension base="{common.xmi}SecurityIdentity">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="identity" type="{common.xmi}Identity"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RunAsSpecifiedIdentity", propOrder = { "identities" })
public class RunAsSpecifiedIdentity extends SecurityIdentity {

	@XmlElement(name = "identity")
	protected List<Identity> identities;

	/**
	 * Gets the value of the identities property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the identities property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getIdentities().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Identity }
	 * 
	 * 
	 */
	public List<Identity> getIdentities() {
		if (identities == null) {
			identities = new ArrayList<Identity>();
		}
		return this.identities;
	}

}
