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
    "minCapacity",
    "maxCapacity",
    "removerPeriod",
    "maxBeanLife",
    "overagerPeriod",
    "maxBeanAge",
    "resizerPeriod",
    "maxCacheMissPeriod",
    "minCacheMissPeriod",
    "cacheLoadFactor",
    "flushEnabled"
})
@XmlRootElement(name = "cache-policy-conf")
public class CachePolicyConf {

    @XmlElement(name = "min-capacity")
    protected String minCapacity;
    @XmlElement(name = "max-capacity")
    protected String maxCapacity;
    @XmlElement(name = "remover-period")
    protected String removerPeriod;
    @XmlElement(name = "max-bean-life")
    protected String maxBeanLife;
    @XmlElement(name = "overager-period")
    protected String overagerPeriod;
    @XmlElement(name = "max-bean-age")
    protected String maxBeanAge;
    @XmlElement(name = "resizer-period")
    protected String resizerPeriod;
    @XmlElement(name = "max-cache-miss-period")
    protected String maxCacheMissPeriod;
    @XmlElement(name = "min-cache-miss-period")
    protected String minCacheMissPeriod;
    @XmlElement(name = "cache-load-factor")
    protected String cacheLoadFactor;
    @XmlElement(name = "flush-enabled")
    protected String flushEnabled;

    /**
     * Gets the value of the minCapacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinCapacity() {
        return minCapacity;
    }

    /**
     * Sets the value of the minCapacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinCapacity(String value) {
        this.minCapacity = value;
    }

    /**
     * Gets the value of the maxCapacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Sets the value of the maxCapacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxCapacity(String value) {
        this.maxCapacity = value;
    }

    /**
     * Gets the value of the removerPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoverPeriod() {
        return removerPeriod;
    }

    /**
     * Sets the value of the removerPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoverPeriod(String value) {
        this.removerPeriod = value;
    }

    /**
     * Gets the value of the maxBeanLife property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxBeanLife() {
        return maxBeanLife;
    }

    /**
     * Sets the value of the maxBeanLife property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxBeanLife(String value) {
        this.maxBeanLife = value;
    }

    /**
     * Gets the value of the overagerPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOveragerPeriod() {
        return overagerPeriod;
    }

    /**
     * Sets the value of the overagerPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOveragerPeriod(String value) {
        this.overagerPeriod = value;
    }

    /**
     * Gets the value of the maxBeanAge property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxBeanAge() {
        return maxBeanAge;
    }

    /**
     * Sets the value of the maxBeanAge property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxBeanAge(String value) {
        this.maxBeanAge = value;
    }

    /**
     * Gets the value of the resizerPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResizerPeriod() {
        return resizerPeriod;
    }

    /**
     * Sets the value of the resizerPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResizerPeriod(String value) {
        this.resizerPeriod = value;
    }

    /**
     * Gets the value of the maxCacheMissPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxCacheMissPeriod() {
        return maxCacheMissPeriod;
    }

    /**
     * Sets the value of the maxCacheMissPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxCacheMissPeriod(String value) {
        this.maxCacheMissPeriod = value;
    }

    /**
     * Gets the value of the minCacheMissPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinCacheMissPeriod() {
        return minCacheMissPeriod;
    }

    /**
     * Sets the value of the minCacheMissPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinCacheMissPeriod(String value) {
        this.minCacheMissPeriod = value;
    }

    /**
     * Gets the value of the cacheLoadFactor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCacheLoadFactor() {
        return cacheLoadFactor;
    }

    /**
     * Sets the value of the cacheLoadFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCacheLoadFactor(String value) {
        this.cacheLoadFactor = value;
    }

    /**
     * Gets the value of the flushEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFlushEnabled() {
        return flushEnabled;
    }

    /**
     * Sets the value of the flushEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFlushEnabled(String value) {
        this.flushEnabled = value;
    }

}
