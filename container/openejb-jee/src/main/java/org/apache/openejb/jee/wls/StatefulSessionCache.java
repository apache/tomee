
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
 * <p>Java class for stateful-session-cache complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="stateful-session-cache">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="max-beans-in-cache" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="idle-timeout-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="session-timeout-seconds" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="cache-type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "stateful-session-cache", propOrder = {
    "maxBeansInCache",
    "idleTimeoutSeconds",
    "sessionTimeoutSeconds",
    "cacheType"
})
public class StatefulSessionCache {

    @XmlElement(name = "max-beans-in-cache")
    protected BigInteger maxBeansInCache;
    @XmlElement(name = "idle-timeout-seconds")
    protected BigInteger idleTimeoutSeconds;
    @XmlElement(name = "session-timeout-seconds")
    protected BigInteger sessionTimeoutSeconds;
    @XmlElement(name = "cache-type")
    protected String cacheType;
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
     * Gets the value of the sessionTimeoutSeconds property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    /**
     * Sets the value of the sessionTimeoutSeconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSessionTimeoutSeconds(BigInteger value) {
        this.sessionTimeoutSeconds = value;
    }

    /**
     * Gets the value of the cacheType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCacheType() {
        return cacheType;
    }

    /**
     * Sets the value of the cacheType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCacheType(String value) {
        this.cacheType = value;
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
