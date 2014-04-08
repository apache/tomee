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

package org.apache.openejb.jee.oejb2;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for openejb-clustering-wadiType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="openejb-clustering-wadiType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://geronimo.apache.org/xml/ns/j2ee/application-2.0}abstract-clusteringType">
 *       &lt;sequence>
 *         &lt;element name="sweepInterval" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="numPartitions" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="cluster" type="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType" minOccurs="0"/>
 *         &lt;element name="disableReplication" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="deltaReplication" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="backing-strategy-factory" type="{http://geronimo.apache.org/xml/ns/naming-1.2}patternType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "openejb-clustering-wadiType", propOrder = {
    "sweepInterval",
    "numPartitions",
    "cluster",
    "disableReplication",
    "deltaReplication",
    "backingStrategyFactory"
}, namespace = "http://geronimo.apache.org/xml/ns/openejb-clustering-wadi-1.2")
public class OpenejbClusteringWadiType
    extends AbstractClusteringType
{

    protected BigInteger sweepInterval;
    protected BigInteger numPartitions;
    protected PatternType cluster;
    protected Boolean disableReplication;
    protected Boolean deltaReplication;
    @XmlElement(name = "backing-strategy-factory")
    protected PatternType backingStrategyFactory;

    /**
     * Gets the value of the sweepInterval property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSweepInterval() {
        return sweepInterval;
    }

    /**
     * Sets the value of the sweepInterval property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSweepInterval(BigInteger value) {
        this.sweepInterval = value;
    }

    /**
     * Gets the value of the numPartitions property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumPartitions() {
        return numPartitions;
    }

    /**
     * Sets the value of the numPartitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumPartitions(BigInteger value) {
        this.numPartitions = value;
    }

    /**
     * Gets the value of the cluster property.
     * 
     * @return
     *     possible object is
     *     {@link PatternType }
     *     
     */
    public PatternType getCluster() {
        return cluster;
    }

    /**
     * Sets the value of the cluster property.
     * 
     * @param value
     *     allowed object is
     *     {@link PatternType }
     *     
     */
    public void setCluster(PatternType value) {
        this.cluster = value;
    }

    /**
     * Gets the value of the disableReplication property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDisableReplication() {
        return disableReplication;
    }

    /**
     * Sets the value of the disableReplication property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDisableReplication(Boolean value) {
        this.disableReplication = value;
    }

    /**
     * Gets the value of the deltaReplication property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDeltaReplication() {
        return deltaReplication;
    }

    /**
     * Sets the value of the deltaReplication property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDeltaReplication(Boolean value) {
        this.deltaReplication = value;
    }

    /**
     * Gets the value of the backingStrategyFactory property.
     * 
     * @return
     *     possible object is
     *     {@link PatternType }
     *     
     */
    public PatternType getBackingStrategyFactory() {
        return backingStrategyFactory;
    }

    /**
     * Sets the value of the backingStrategyFactory property.
     * 
     * @param value
     *     allowed object is
     *     {@link PatternType }
     *     
     */
    public void setBackingStrategyFactory(PatternType value) {
        this.backingStrategyFactory = value;
    }

}
