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

package org.apache.openejb.jee.oej2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for web-service-securityType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="web-service-securityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="security-realm-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="realm-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transport-guarantee" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}transport-guaranteeType"/>
 *         &lt;element name="auth-method" type="{http://openejb.apache.org/xml/ns/openejb-jar-2.2}auth-methodType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "web-service-securityType", namespace = "http://openejb.apache.org/xml/ns/openejb-jar-2.2", propOrder = {
    "securityRealmName",
    "realmName",
    "transportGuarantee",
    "authMethod"
})
public class WebServiceSecurityType {

    @XmlElement(name = "security-realm-name", namespace = "http://openejb.apache.org/xml/ns/openejb-jar-2.2", required = true)
    protected java.lang.String securityRealmName;
    @XmlElement(name = "realm-name", namespace = "http://openejb.apache.org/xml/ns/openejb-jar-2.2")
    protected java.lang.String realmName;
    @XmlElement(name = "transport-guarantee", namespace = "http://openejb.apache.org/xml/ns/openejb-jar-2.2", required = true)
    protected TransportGuaranteeType transportGuarantee;
    @XmlElement(name = "auth-method", namespace = "http://openejb.apache.org/xml/ns/openejb-jar-2.2", required = true)
    protected AuthMethodType authMethod;

    /**
     * Gets the value of the securityRealmName property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getSecurityRealmName() {
        return securityRealmName;
    }

    /**
     * Sets the value of the securityRealmName property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setSecurityRealmName(java.lang.String value) {
        this.securityRealmName = value;
    }

    /**
     * Gets the value of the realmName property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getRealmName() {
        return realmName;
    }

    /**
     * Sets the value of the realmName property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setRealmName(java.lang.String value) {
        this.realmName = value;
    }

    /**
     * Gets the value of the transportGuarantee property.
     * 
     * @return
     *     possible object is
     *     {@link TransportGuaranteeType }
     *     
     */
    public TransportGuaranteeType getTransportGuarantee() {
        return transportGuarantee;
    }

    /**
     * Sets the value of the transportGuarantee property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransportGuaranteeType }
     *     
     */
    public void setTransportGuarantee(TransportGuaranteeType value) {
        this.transportGuarantee = value;
    }

    /**
     * Gets the value of the authMethod property.
     * 
     * @return
     *     possible object is
     *     {@link AuthMethodType }
     *     
     */
    public AuthMethodType getAuthMethod() {
        return authMethod;
    }

    /**
     * Sets the value of the authMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthMethodType }
     *     
     */
    public void setAuthMethod(AuthMethodType value) {
        this.authMethod = value;
    }

}
