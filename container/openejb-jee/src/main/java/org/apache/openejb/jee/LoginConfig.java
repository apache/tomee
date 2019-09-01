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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * web-common_3_0.xsd
 *
 * <p>Java class for login-configType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="login-configType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="auth-method" type="{http://java.sun.com/xml/ns/javaee}auth-methodType" minOccurs="0"/&gt;
 *         &lt;element name="realm-name" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="form-login-config" type="{http://java.sun.com/xml/ns/javaee}form-login-configType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "login-configType", propOrder = {
    "authMethod",
    "realmName",
    "formLoginConfig"
})
public class LoginConfig {

    @XmlElement(name = "auth-method")
    protected String authMethod;
    @XmlElement(name = "realm-name")
    protected String realmName;
    @XmlElement(name = "form-login-config")
    protected FormLoginConfig formLoginConfig;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(final String value) {
        this.authMethod = value;
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
     * Gets the value of the formLoginConfig property.
     *
     * @return possible object is
     * {@link FormLoginConfig }
     */
    public FormLoginConfig getFormLoginConfig() {
        return formLoginConfig;
    }

    /**
     * Sets the value of the formLoginConfig property.
     *
     * @param value allowed object is
     *              {@link FormLoginConfig }
     */
    public void setFormLoginConfig(final FormLoginConfig value) {
        this.formLoginConfig = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setId(final java.lang.String value) {
        this.id = value;
    }

}
