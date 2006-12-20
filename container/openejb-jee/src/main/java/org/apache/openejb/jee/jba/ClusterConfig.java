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
    "partitionName",
    "homeLoadBalancePolicy",
    "beanLoadBalancePolicy",
    "sessionStateManagerJndiName"
})
@XmlRootElement(name = "cluster-config")
public class ClusterConfig {

    @XmlElement(name = "partition-name")
    protected String partitionName;
    @XmlElement(name = "home-load-balance-policy")
    protected String homeLoadBalancePolicy;
    @XmlElement(name = "bean-load-balance-policy")
    protected String beanLoadBalancePolicy;
    @XmlElement(name = "session-state-manager-jndi-name")
    protected String sessionStateManagerJndiName;

    /**
     * Gets the value of the partitionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartitionName() {
        return partitionName;
    }

    /**
     * Sets the value of the partitionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartitionName(String value) {
        this.partitionName = value;
    }

    /**
     * Gets the value of the homeLoadBalancePolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHomeLoadBalancePolicy() {
        return homeLoadBalancePolicy;
    }

    /**
     * Sets the value of the homeLoadBalancePolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHomeLoadBalancePolicy(String value) {
        this.homeLoadBalancePolicy = value;
    }

    /**
     * Gets the value of the beanLoadBalancePolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBeanLoadBalancePolicy() {
        return beanLoadBalancePolicy;
    }

    /**
     * Sets the value of the beanLoadBalancePolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBeanLoadBalancePolicy(String value) {
        this.beanLoadBalancePolicy = value;
    }

    /**
     * Gets the value of the sessionStateManagerJndiName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionStateManagerJndiName() {
        return sessionStateManagerJndiName;
    }

    /**
     * Sets the value of the sessionStateManagerJndiName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionStateManagerJndiName(String value) {
        this.sessionStateManagerJndiName = value;
    }

}
