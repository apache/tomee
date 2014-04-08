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
package org.apache.openejb.jee.wls;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for entity-cache complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="entity-cache">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="max-beans-in-cache" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="max-queries-in-cache" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="idle-timeout-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="read-timeout-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="concurrency-strategy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cache-between-transactions" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity-cache", propOrder = {
    "maxBeansInCache",
    "maxQueriesInCache",
    "idleTimeoutSeconds",
    "readTimeoutSeconds",
    "concurrencyStrategy",
    "cacheBetweenTransactions"
})
public class EntityCache {

    @XmlElement(name = "max-beans-in-cache")
    protected BigInteger maxBeansInCache;
    @XmlElement(name = "max-queries-in-cache")
    protected BigInteger maxQueriesInCache;
    @XmlElement(name = "idle-timeout-seconds")
    protected BigInteger idleTimeoutSeconds;
    @XmlElement(name = "read-timeout-seconds")
    protected BigInteger readTimeoutSeconds;
    @XmlElement(name = "concurrency-strategy")
    protected String concurrencyStrategy;
    @XmlElement(name = "cache-between-transactions")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean cacheBetweenTransactions;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the maxBeansInCache property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxBeansInCache() {
        return maxBeansInCache;
    }

    /**
     * Sets the value of the maxBeansInCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxBeansInCache(BigInteger value) {
        this.maxBeansInCache = value;
    }

    /**
     * Gets the value of the maxQueriesInCache property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxQueriesInCache() {
        return maxQueriesInCache;
    }

    /**
     * Sets the value of the maxQueriesInCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxQueriesInCache(BigInteger value) {
        this.maxQueriesInCache = value;
    }

    /**
     * Gets the value of the idleTimeoutSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getIdleTimeoutSeconds() {
        return idleTimeoutSeconds;
    }

    /**
     * Sets the value of the idleTimeoutSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setIdleTimeoutSeconds(BigInteger value) {
        this.idleTimeoutSeconds = value;
    }

    /**
     * Gets the value of the readTimeoutSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    /**
     * Sets the value of the readTimeoutSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setReadTimeoutSeconds(BigInteger value) {
        this.readTimeoutSeconds = value;
    }

    /**
     * Gets the value of the concurrencyStrategy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConcurrencyStrategy() {
        return concurrencyStrategy;
    }

    /**
     * Sets the value of the concurrencyStrategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConcurrencyStrategy(String value) {
        this.concurrencyStrategy = value;
    }

    /**
     * Gets the value of the cacheBetweenTransactions property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getCacheBetweenTransactions() {
        return cacheBetweenTransactions;
    }

    /**
     * Sets the value of the cacheBetweenTransactions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCacheBetweenTransactions(Boolean value) {
        this.cacheBetweenTransactions = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
