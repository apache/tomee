/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jee.wls;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for iiop-security-descriptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="iiop-security-descriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="transport-requirements" type="{http://www.bea.com/ns/weblogic/90}transport-requirements" minOccurs="0"/>
 *         &lt;element name="client-authentication" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="identity-assertion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "iiop-security-descriptor", propOrder = {
    "transportRequirements",
    "clientAuthentication",
    "identityAssertion"
})
public class IiopSecurityDescriptor {

    @XmlElement(name = "transport-requirements")
    protected TransportRequirements transportRequirements;
    @XmlElement(name = "client-authentication")
    protected String clientAuthentication;
    @XmlElement(name = "identity-assertion")
    protected String identityAssertion;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the transportRequirements property.
     * 
     * @return
     *     possible object is
     *     {@link TransportRequirements }
     *     
     */
    public TransportRequirements getTransportRequirements() {
        return transportRequirements;
    }

    /**
     * Sets the value of the transportRequirements property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransportRequirements }
     *     
     */
    public void setTransportRequirements(TransportRequirements value) {
        this.transportRequirements = value;
    }

    /**
     * Gets the value of the clientAuthentication property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientAuthentication() {
        return clientAuthentication;
    }

    /**
     * Sets the value of the clientAuthentication property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientAuthentication(String value) {
        this.clientAuthentication = value;
    }

    /**
     * Gets the value of the identityAssertion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentityAssertion() {
        return identityAssertion;
    }

    /**
     * Sets the value of the identityAssertion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentityAssertion(String value) {
        this.identityAssertion = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
