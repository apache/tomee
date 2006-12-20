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
    "cachePolicy",
    "cachePolicyConf",
    "cachePolicyConfOther"
})
@XmlRootElement(name = "container-cache-conf")
public class ContainerCacheConf {

    @XmlElement(name = "cache-policy")
    protected String cachePolicy;
    @XmlElement(name = "cache-policy-conf")
    protected CachePolicyConf cachePolicyConf;
    @XmlElement(name = "cache-policy-conf-other")
    protected CachePolicyConfOther cachePolicyConfOther;

    /**
     * Gets the value of the cachePolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCachePolicy() {
        return cachePolicy;
    }

    /**
     * Sets the value of the cachePolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCachePolicy(String value) {
        this.cachePolicy = value;
    }

    /**
     * Gets the value of the cachePolicyConf property.
     * 
     * @return
     *     possible object is
     *     {@link CachePolicyConf }
     *     
     */
    public CachePolicyConf getCachePolicyConf() {
        return cachePolicyConf;
    }

    /**
     * Sets the value of the cachePolicyConf property.
     * 
     * @param value
     *     allowed object is
     *     {@link CachePolicyConf }
     *     
     */
    public void setCachePolicyConf(CachePolicyConf value) {
        this.cachePolicyConf = value;
    }

    /**
     * Gets the value of the cachePolicyConfOther property.
     * 
     * @return
     *     possible object is
     *     {@link CachePolicyConfOther }
     *     
     */
    public CachePolicyConfOther getCachePolicyConfOther() {
        return cachePolicyConfOther;
    }

    /**
     * Sets the value of the cachePolicyConfOther property.
     * 
     * @param value
     *     allowed object is
     *     {@link CachePolicyConfOther }
     *     
     */
    public void setCachePolicyConfOther(CachePolicyConfOther value) {
        this.cachePolicyConfOther = value;
    }

}
