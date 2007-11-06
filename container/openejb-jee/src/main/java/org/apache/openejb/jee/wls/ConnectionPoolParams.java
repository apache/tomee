
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
 * <p>Java class for connection-pool-params complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="connection-pool-params">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="initial-capacity" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="max-capacity" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="capacity-increment" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="shrinking-enabled" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="shrink-frequency-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="highest-num-waiters" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="highest-num-unavailable" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="connection-creation-retry-frequency-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="connection-reserve-timeout-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="test-frequency-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="test-connections-on-create" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="test-connections-on-release" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="test-connections-on-reserve" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="profile-harvest-frequency-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="ignore-in-use-connections-enabled" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
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
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getInitialCapacity() {
        return initialCapacity;
    }

    /**
     * Sets the value of the initialCapacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setInitialCapacity(BigInteger value) {
        this.initialCapacity = value;
    }

    /**
     * Gets the value of the maxCapacity property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Sets the value of the maxCapacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxCapacity(BigInteger value) {
        this.maxCapacity = value;
    }

    /**
     * Gets the value of the capacityIncrement property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCapacityIncrement() {
        return capacityIncrement;
    }

    /**
     * Sets the value of the capacityIncrement property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCapacityIncrement(BigInteger value) {
        this.capacityIncrement = value;
    }

    /**
     * Gets the value of the shrinkingEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getShrinkingEnabled() {
        return shrinkingEnabled;
    }

    /**
     * Sets the value of the shrinkingEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShrinkingEnabled(Boolean value) {
        this.shrinkingEnabled = value;
    }

    /**
     * Gets the value of the shrinkFrequencySeconds property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getShrinkFrequencySeconds() {
        return shrinkFrequencySeconds;
    }

    /**
     * Sets the value of the shrinkFrequencySeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setShrinkFrequencySeconds(BigInteger value) {
        this.shrinkFrequencySeconds = value;
    }

    /**
     * Gets the value of the highestNumWaiters property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getHighestNumWaiters() {
        return highestNumWaiters;
    }

    /**
     * Sets the value of the highestNumWaiters property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setHighestNumWaiters(BigInteger value) {
        this.highestNumWaiters = value;
    }

    /**
     * Gets the value of the highestNumUnavailable property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getHighestNumUnavailable() {
        return highestNumUnavailable;
    }

    /**
     * Sets the value of the highestNumUnavailable property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setHighestNumUnavailable(BigInteger value) {
        this.highestNumUnavailable = value;
    }

    /**
     * Gets the value of the connectionCreationRetryFrequencySeconds property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getConnectionCreationRetryFrequencySeconds() {
        return connectionCreationRetryFrequencySeconds;
    }

    /**
     * Sets the value of the connectionCreationRetryFrequencySeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setConnectionCreationRetryFrequencySeconds(BigInteger value) {
        this.connectionCreationRetryFrequencySeconds = value;
    }

    /**
     * Gets the value of the connectionReserveTimeoutSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getConnectionReserveTimeoutSeconds() {
        return connectionReserveTimeoutSeconds;
    }

    /**
     * Sets the value of the connectionReserveTimeoutSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setConnectionReserveTimeoutSeconds(BigInteger value) {
        this.connectionReserveTimeoutSeconds = value;
    }

    /**
     * Gets the value of the testFrequencySeconds property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTestFrequencySeconds() {
        return testFrequencySeconds;
    }

    /**
     * Sets the value of the testFrequencySeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTestFrequencySeconds(BigInteger value) {
        this.testFrequencySeconds = value;
    }

    /**
     * Gets the value of the testConnectionsOnCreate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getTestConnectionsOnCreate() {
        return testConnectionsOnCreate;
    }

    /**
     * Sets the value of the testConnectionsOnCreate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTestConnectionsOnCreate(Boolean value) {
        this.testConnectionsOnCreate = value;
    }

    /**
     * Gets the value of the testConnectionsOnRelease property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getTestConnectionsOnRelease() {
        return testConnectionsOnRelease;
    }

    /**
     * Sets the value of the testConnectionsOnRelease property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTestConnectionsOnRelease(Boolean value) {
        this.testConnectionsOnRelease = value;
    }

    /**
     * Gets the value of the testConnectionsOnReserve property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getTestConnectionsOnReserve() {
        return testConnectionsOnReserve;
    }

    /**
     * Sets the value of the testConnectionsOnReserve property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTestConnectionsOnReserve(Boolean value) {
        this.testConnectionsOnReserve = value;
    }

    /**
     * Gets the value of the profileHarvestFrequencySeconds property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getProfileHarvestFrequencySeconds() {
        return profileHarvestFrequencySeconds;
    }

    /**
     * Sets the value of the profileHarvestFrequencySeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setProfileHarvestFrequencySeconds(BigInteger value) {
        this.profileHarvestFrequencySeconds = value;
    }

    /**
     * Gets the value of the ignoreInUseConnectionsEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getIgnoreInUseConnectionsEnabled() {
        return ignoreInUseConnectionsEnabled;
    }

    /**
     * Sets the value of the ignoreInUseConnectionsEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIgnoreInUseConnectionsEnabled(Boolean value) {
        this.ignoreInUseConnectionsEnabled = value;
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
