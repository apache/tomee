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
    "steadyPoolSize",
    "resizeQuantity",
    "maxPoolSize",
    "poolIdleTimeoutInSeconds",
    "maxWaitTimeInMillis"
})
@XmlRootElement(name = "bean-pool")
public class BeanPool {

    @XmlElement(name = "steady-pool-size")
    protected String steadyPoolSize;
    @XmlElement(name = "resize-quantity")
    protected String resizeQuantity;
    @XmlElement(name = "max-pool-size")
    protected String maxPoolSize;
    @XmlElement(name = "pool-idle-timeout-in-seconds")
    protected String poolIdleTimeoutInSeconds;
    @XmlElement(name = "max-wait-time-in-millis")
    protected String maxWaitTimeInMillis;

    /**
     * Gets the value of the steadyPoolSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSteadyPoolSize() {
        return steadyPoolSize;
    }

    /**
     * Sets the value of the steadyPoolSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSteadyPoolSize(String value) {
        this.steadyPoolSize = value;
    }

    /**
     * Gets the value of the resizeQuantity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResizeQuantity() {
        return resizeQuantity;
    }

    /**
     * Sets the value of the resizeQuantity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResizeQuantity(String value) {
        this.resizeQuantity = value;
    }

    /**
     * Gets the value of the maxPoolSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * Sets the value of the maxPoolSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxPoolSize(String value) {
        this.maxPoolSize = value;
    }

    /**
     * Gets the value of the poolIdleTimeoutInSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPoolIdleTimeoutInSeconds() {
        return poolIdleTimeoutInSeconds;
    }

    /**
     * Sets the value of the poolIdleTimeoutInSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPoolIdleTimeoutInSeconds(String value) {
        this.poolIdleTimeoutInSeconds = value;
    }

    /**
     * Gets the value of the maxWaitTimeInMillis property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxWaitTimeInMillis() {
        return maxWaitTimeInMillis;
    }

    /**
     * Sets the value of the maxWaitTimeInMillis property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxWaitTimeInMillis(String value) {
        this.maxWaitTimeInMillis = value;
    }

}
