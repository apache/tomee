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

package org.apache.openejb.jee.jba;

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
    "reconnectIntervalSec",
    "deliveryActive",
    "dlqConfig"
})
@XmlRootElement(name = "MDBConfig")
public class MDBConfig {

    @XmlElement(name = "ReconnectIntervalSec", required = true)
    protected String reconnectIntervalSec;
    @XmlElement(name = "DeliveryActive")
    protected String deliveryActive;
    @XmlElement(name = "DLQConfig")
    protected DLQConfig dlqConfig;

    /**
     * Gets the value of the reconnectIntervalSec property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReconnectIntervalSec() {
        return reconnectIntervalSec;
    }

    /**
     * Sets the value of the reconnectIntervalSec property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReconnectIntervalSec(String value) {
        this.reconnectIntervalSec = value;
    }

    /**
     * Gets the value of the deliveryActive property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeliveryActive() {
        return deliveryActive;
    }

    /**
     * Sets the value of the deliveryActive property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeliveryActive(String value) {
        this.deliveryActive = value;
    }

    /**
     * Gets the value of the dlqConfig property.
     * 
     * @return
     *     possible object is
     *     {@link DLQConfig }
     *     
     */
    public DLQConfig getDLQConfig() {
        return dlqConfig;
    }

    /**
     * Sets the value of the dlqConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link DLQConfig }
     *     
     */
    public void setDLQConfig(DLQConfig value) {
        this.dlqConfig = value;
    }

}
