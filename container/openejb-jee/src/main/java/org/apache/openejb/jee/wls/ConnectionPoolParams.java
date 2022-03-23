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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for connection-pool-params complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="connection-pool-params"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="initial-capacity" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="max-capacity" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="capacity-increment" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="shrinking-enabled" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="shrink-frequency-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="highest-num-waiters" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="highest-num-unavailable" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="connection-creation-retry-frequency-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="connection-reserve-timeout-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="test-frequency-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="test-connections-on-create" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="test-connections-on-release" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="test-connections-on-reserve" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *         &lt;element name="profile-harvest-frequency-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="ignore-in-use-connections-enabled" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "connection-pool-params", propOrder = {
    "initialCapacity",
    "maxCapacity",
    "capacityIncrement",
    "shrinkingEnabled",
    "shrinkFrequencySeconds",
    "highestNumWaiters",
    "highestNumUnavailable",
    "connectionCreationRetryFrequencySeconds",
    "connectionReserveTimeoutSeconds",
    "testFrequencySeconds",
    "testConnectionsOnCreate",
    "testConnectionsOnRelease",
    "testConnectionsOnReserve",
    "profileHarvestFrequencySeconds",
    "ignoreInUseConnectionsEnabled"
})
public class ConnectionPoolParams {

    @XmlElement(name = "initial-capacity")
    protected BigInteger initialCapacity;
    @XmlElement(name = "max-capacity")
    protected BigInteger maxCapacity;
    @XmlElement(name = "capacity-increment")
    protected BigInteger capacityIncrement;
    @XmlElement(name = "shrinking-enabled")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean shrinkingEnabled;
    @XmlElement(name = "shrink-frequency-seconds")
    protected BigInteger shrinkFrequencySeconds;
    @XmlElement(name = "highest-num-waiters")
    protected BigInteger highestNumWaiters;
    @XmlElement(name = "highest-num-unavailable")
    protected BigInteger highestNumUnavailable;
    @XmlElement(name = "connection-creation-retry-frequency-seconds")
    protected BigInteger connectionCreationRetryFrequencySeconds;
    @XmlElement(name = "connection-reserve-timeout-seconds")
    protected BigInteger connectionReserveTimeoutSeconds;
    @XmlElement(name = "test-frequency-seconds")
    protected BigInteger testFrequencySeconds;
    @XmlElement(name = "test-connections-on-create")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean testConnectionsOnCreate;
    @XmlElement(name = "test-connections-on-release")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean testConnectionsOnRelease;
    @XmlElement(name = "test-connections-on-reserve")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean testConnectionsOnReserve;
    @XmlElement(name = "profile-harvest-frequency-seconds")
    protected BigInteger profileHarvestFrequencySeconds;
    @XmlElement(name = "ignore-in-use-connections-enabled")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean ignoreInUseConnectionsEnabled;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the initialCapacity property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getInitialCapacity() {
        return initialCapacity;
    }

    /**
     * Sets the value of the initialCapacity property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setInitialCapacity(final BigInteger value) {
        this.initialCapacity = value;
    }

    /**
     * Gets the value of the maxCapacity property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Sets the value of the maxCapacity property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setMaxCapacity(final BigInteger value) {
        this.maxCapacity = value;
    }

    /**
     * Gets the value of the capacityIncrement property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getCapacityIncrement() {
        return capacityIncrement;
    }

    /**
     * Sets the value of the capacityIncrement property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setCapacityIncrement(final BigInteger value) {
        this.capacityIncrement = value;
    }

    /**
     * Gets the value of the shrinkingEnabled property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getShrinkingEnabled() {
        return shrinkingEnabled;
    }

    /**
     * Sets the value of the shrinkingEnabled property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setShrinkingEnabled(final Boolean value) {
        this.shrinkingEnabled = value;
    }

    /**
     * Gets the value of the shrinkFrequencySeconds property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getShrinkFrequencySeconds() {
        return shrinkFrequencySeconds;
    }

    /**
     * Sets the value of the shrinkFrequencySeconds property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setShrinkFrequencySeconds(final BigInteger value) {
        this.shrinkFrequencySeconds = value;
    }

    /**
     * Gets the value of the highestNumWaiters property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getHighestNumWaiters() {
        return highestNumWaiters;
    }

    /**
     * Sets the value of the highestNumWaiters property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setHighestNumWaiters(final BigInteger value) {
        this.highestNumWaiters = value;
    }

    /**
     * Gets the value of the highestNumUnavailable property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getHighestNumUnavailable() {
        return highestNumUnavailable;
    }

    /**
     * Sets the value of the highestNumUnavailable property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setHighestNumUnavailable(final BigInteger value) {
        this.highestNumUnavailable = value;
    }

    /**
     * Gets the value of the connectionCreationRetryFrequencySeconds property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getConnectionCreationRetryFrequencySeconds() {
        return connectionCreationRetryFrequencySeconds;
    }

    /**
     * Sets the value of the connectionCreationRetryFrequencySeconds property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setConnectionCreationRetryFrequencySeconds(final BigInteger value) {
        this.connectionCreationRetryFrequencySeconds = value;
    }

    /**
     * Gets the value of the connectionReserveTimeoutSeconds property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getConnectionReserveTimeoutSeconds() {
        return connectionReserveTimeoutSeconds;
    }

    /**
     * Sets the value of the connectionReserveTimeoutSeconds property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setConnectionReserveTimeoutSeconds(final BigInteger value) {
        this.connectionReserveTimeoutSeconds = value;
    }

    /**
     * Gets the value of the testFrequencySeconds property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getTestFrequencySeconds() {
        return testFrequencySeconds;
    }

    /**
     * Sets the value of the testFrequencySeconds property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setTestFrequencySeconds(final BigInteger value) {
        this.testFrequencySeconds = value;
    }

    /**
     * Gets the value of the testConnectionsOnCreate property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getTestConnectionsOnCreate() {
        return testConnectionsOnCreate;
    }

    /**
     * Sets the value of the testConnectionsOnCreate property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setTestConnectionsOnCreate(final Boolean value) {
        this.testConnectionsOnCreate = value;
    }

    /**
     * Gets the value of the testConnectionsOnRelease property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getTestConnectionsOnRelease() {
        return testConnectionsOnRelease;
    }

    /**
     * Sets the value of the testConnectionsOnRelease property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setTestConnectionsOnRelease(final Boolean value) {
        this.testConnectionsOnRelease = value;
    }

    /**
     * Gets the value of the testConnectionsOnReserve property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getTestConnectionsOnReserve() {
        return testConnectionsOnReserve;
    }

    /**
     * Sets the value of the testConnectionsOnReserve property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setTestConnectionsOnReserve(final Boolean value) {
        this.testConnectionsOnReserve = value;
    }

    /**
     * Gets the value of the profileHarvestFrequencySeconds property.
     *
     * @return possible object is
     * {@link BigInteger }
     */
    public BigInteger getProfileHarvestFrequencySeconds() {
        return profileHarvestFrequencySeconds;
    }

    /**
     * Sets the value of the profileHarvestFrequencySeconds property.
     *
     * @param value allowed object is
     *              {@link BigInteger }
     */
    public void setProfileHarvestFrequencySeconds(final BigInteger value) {
        this.profileHarvestFrequencySeconds = value;
    }

    /**
     * Gets the value of the ignoreInUseConnectionsEnabled property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean getIgnoreInUseConnectionsEnabled() {
        return ignoreInUseConnectionsEnabled;
    }

    /**
     * Sets the value of the ignoreInUseConnectionsEnabled property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setIgnoreInUseConnectionsEnabled(final Boolean value) {
        this.ignoreInUseConnectionsEnabled = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(final String value) {
        this.id = value;
    }

}
