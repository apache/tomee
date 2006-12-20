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

package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "transportConfig",
    "asContext",
    "sasContext"
})
@XmlRootElement(name = "ior-security-config")
public class IorSecurityConfig {

    @XmlElement(name = "transport-config")
    protected TransportConfig transportConfig;
    @XmlElement(name = "as-context")
    protected AsContext asContext;
    @XmlElement(name = "sas-context")
    protected SasContext sasContext;

    /**
     * Gets the value of the transportConfig property.
     * 
     * @return
     *     possible object is
     *     {@link TransportConfig }
     *     
     */
    public TransportConfig getTransportConfig() {
        return transportConfig;
    }

    /**
     * Sets the value of the transportConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransportConfig }
     *     
     */
    public void setTransportConfig(TransportConfig value) {
        this.transportConfig = value;
    }

    /**
     * Gets the value of the asContext property.
     * 
     * @return
     *     possible object is
     *     {@link AsContext }
     *     
     */
    public AsContext getAsContext() {
        return asContext;
    }

    /**
     * Sets the value of the asContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link AsContext }
     *     
     */
    public void setAsContext(AsContext value) {
        this.asContext = value;
    }

    /**
     * Gets the value of the sasContext property.
     * 
     * @return
     *     possible object is
     *     {@link SasContext }
     *     
     */
    public SasContext getSasContext() {
        return sasContext;
    }

    /**
     * Sets the value of the sasContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link SasContext }
     *     
     */
    public void setSasContext(SasContext value) {
        this.sasContext = value;
    }

}
