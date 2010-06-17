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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * web-facesconfig_2_0.xsd
 *
 * <p>Java class for faces-config-client-behavior-rendererType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="faces-config-client-behavior-rendererType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="client-behavior-renderer-type" type="{http://java.sun.com/xml/ns/javaee}string"/>
 *         &lt;element name="client-behavior-renderer-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-client-behavior-rendererType", propOrder = {
    "clientBehaviorRendererType",
    "clientBehaviorRendererClass"
})
public class FacesClientBehaviorRenderer {

    @XmlElement(name = "client-behavior-renderer-type", required = true)
    protected String clientBehaviorRendererType;
    @XmlElement(name = "client-behavior-renderer-class", required = true)
    protected String clientBehaviorRendererClass;

    /**
     * Gets the value of the clientBehaviorRendererType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientBehaviorRendererType() {
        return clientBehaviorRendererType;
    }

    /**
     * Sets the value of the clientBehaviorRendererType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientBehaviorRendererType(String value) {
        this.clientBehaviorRendererType = value;
    }

    /**
     * Gets the value of the clientBehaviorRendererClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientBehaviorRendererClass() {
        return clientBehaviorRendererClass;
    }

    /**
     * Sets the value of the clientBehaviorRendererClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientBehaviorRendererClass(String value) {
        this.clientBehaviorRendererClass = value;
    }

}
