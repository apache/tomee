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

package org.apache.openejb.jee.oejb2;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.openejb.jee.oejb3.PropertiesAdapter;


/**
 * <p>Java class for web-service-securityType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="web-service-securityType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="security-realm-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="realm-name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="transport-guarantee" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}transport-guaranteeType"/&gt;
 *         &lt;element name="auth-method" type="{http://tomee.apache.org/xml/ns/openejb-jar-2.2}auth-methodType"/&gt;
 *         &lt;element name="http-method" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "web-service-securityType", propOrder = {
    "securityRealmName",
    "realmName",
    "transportGuarantee",
    "authMethod",
    "httpMethod",
    "properties"
})
public class WebServiceSecurityType {

    @XmlElement(name = "security-realm-name", required = true)
    protected String securityRealmName;
    @XmlElement(name = "realm-name")
    protected String realmName;
    @XmlElement(name = "transport-guarantee", required = true)
    protected TransportGuaranteeType transportGuarantee;
    @XmlElement(name = "auth-method", required = true)
    protected AuthMethodType authMethod;
    @XmlElement(name = "http-method")
    protected List<String> httpMethod;
    @XmlElement(name = "properties", required = false)
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    protected Properties properties;

    /**
     * Gets the value of the securityRealmName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSecurityRealmName() {
        return securityRealmName;
    }

    /**
     * Sets the value of the securityRealmName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSecurityRealmName(final String value) {
        this.securityRealmName = value;
    }

    /**
     * Gets the value of the realmName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRealmName() {
        return realmName;
    }

    /**
     * Sets the value of the realmName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRealmName(final String value) {
        this.realmName = value;
    }

    /**
     * Gets the value of the transportGuarantee property.
     *
     * @return possible object is
     * {@link TransportGuaranteeType }
     */
    public TransportGuaranteeType getTransportGuarantee() {
        return transportGuarantee;
    }

    /**
     * Sets the value of the transportGuarantee property.
     *
     * @param value allowed object is
     *              {@link TransportGuaranteeType }
     */
    public void setTransportGuarantee(final TransportGuaranteeType value) {
        this.transportGuarantee = value;
    }

    /**
     * Gets the value of the authMethod property.
     *
     * @return possible object is
     * {@link AuthMethodType }
     */
    public AuthMethodType getAuthMethod() {
        return authMethod;
    }

    /**
     * Sets the value of the authMethod property.
     *
     * @param value allowed object is
     *              {@link AuthMethodType }
     */
    public void setAuthMethod(final AuthMethodType value) {
        this.authMethod = value;
    }

    /**
     * Gets the value of the httpMethod property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the httpMethod property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHttpMethod().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getHttpMethod() {
        if (httpMethod == null) {
            httpMethod = new ArrayList<String>();
        }
        return this.httpMethod;
    }

    public Properties getProperties() {
        if (null == properties) {
            properties = new Properties();
        }
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }


}
