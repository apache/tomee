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
    "destinationQueue",
    "maxTimesRedelivered",
    "timeToLive",
    "dlqUser",
    "dlqPassword"
})
@XmlRootElement(name = "DLQConfig")
public class DLQConfig {

    @XmlElement(name = "DestinationQueue", required = true)
    protected String destinationQueue;
    @XmlElement(name = "MaxTimesRedelivered", required = true)
    protected String maxTimesRedelivered;
    @XmlElement(name = "TimeToLive", required = true)
    protected String timeToLive;
    @XmlElement(name = "DLQUser")
    protected String dlqUser;
    @XmlElement(name = "DLQPassword")
    protected String dlqPassword;

    /**
     * Gets the value of the destinationQueue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationQueue() {
        return destinationQueue;
    }

    /**
     * Sets the value of the destinationQueue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationQueue(String value) {
        this.destinationQueue = value;
    }

    /**
     * Gets the value of the maxTimesRedelivered property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxTimesRedelivered() {
        return maxTimesRedelivered;
    }

    /**
     * Sets the value of the maxTimesRedelivered property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxTimesRedelivered(String value) {
        this.maxTimesRedelivered = value;
    }

    /**
     * Gets the value of the timeToLive property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeToLive() {
        return timeToLive;
    }

    /**
     * Sets the value of the timeToLive property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeToLive(String value) {
        this.timeToLive = value;
    }

    /**
     * Gets the value of the dlqUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDLQUser() {
        return dlqUser;
    }

    /**
     * Sets the value of the dlqUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDLQUser(String value) {
        this.dlqUser = value;
    }

    /**
     * Gets the value of the dlqPassword property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDLQPassword() {
        return dlqPassword;
    }

    /**
     * Sets the value of the dlqPassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDLQPassword(String value) {
        this.dlqPassword = value;
    }

}
