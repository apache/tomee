
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
 * <p>Java class for pool complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="pool">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="max-beans-in-free-pool" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="initial-beans-in-free-pool" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="idle-timeout-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
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
@XmlType(name = "pool", propOrder = {
    "maxBeansInFreePool",
    "initialBeansInFreePool",
    "idleTimeoutSeconds"
})
public class Pool {

    @XmlElement(name = "max-beans-in-free-pool")
    protected BigInteger maxBeansInFreePool;
    @XmlElement(name = "initial-beans-in-free-pool")
    protected BigInteger initialBeansInFreePool;
    @XmlElement(name = "idle-timeout-seconds")
    protected BigInteger idleTimeoutSeconds;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the maxBeansInFreePool property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxBeansInFreePool() {
        return maxBeansInFreePool;
    }

    /**
     * Sets the value of the maxBeansInFreePool property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxBeansInFreePool(BigInteger value) {
        this.maxBeansInFreePool = value;
    }

    /**
     * Gets the value of the initialBeansInFreePool property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getInitialBeansInFreePool() {
        return initialBeansInFreePool;
    }

    /**
     * Sets the value of the initialBeansInFreePool property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setInitialBeansInFreePool(BigInteger value) {
        this.initialBeansInFreePool = value;
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
