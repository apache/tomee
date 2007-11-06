
package org.apache.openejb.jee.wls;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for stateful-session-descriptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="stateful-session-descriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="stateful-session-cache" type="{http://www.bea.com/ns/weblogic/90}stateful-session-cache" minOccurs="0"/>
 *         &lt;element name="persistent-store-dir" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stateful-session-clustering" type="{http://www.bea.com/ns/weblogic/90}stateful-session-clustering" minOccurs="0"/>
 *         &lt;element name="allow-concurrent-calls" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
 *         &lt;element name="allow-remove-during-transaction" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
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
@XmlType(name = "stateful-session-descriptor", propOrder = {
    "statefulSessionCache",
    "persistentStoreDir",
    "statefulSessionClustering",
    "allowConcurrentCalls",
    "allowRemoveDuringTransaction"
})
public class StatefulSessionDescriptor {

    @XmlElement(name = "stateful-session-cache")
    protected StatefulSessionCache statefulSessionCache;
    @XmlElement(name = "persistent-store-dir")
    protected String persistentStoreDir;
    @XmlElement(name = "stateful-session-clustering")
    protected StatefulSessionClustering statefulSessionClustering;
    @XmlElement(name = "allow-concurrent-calls")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean allowConcurrentCalls;
    @XmlElement(name = "allow-remove-during-transaction")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean allowRemoveDuringTransaction;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the statefulSessionCache property.
     * 
     * @return
     *     possible object is
     *     {@link StatefulSessionCache }
     *     
     */
    public StatefulSessionCache getStatefulSessionCache() {
        return statefulSessionCache;
    }

    /**
     * Sets the value of the statefulSessionCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatefulSessionCache }
     *     
     */
    public void setStatefulSessionCache(StatefulSessionCache value) {
        this.statefulSessionCache = value;
    }

    /**
     * Gets the value of the persistentStoreDir property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPersistentStoreDir() {
        return persistentStoreDir;
    }

    /**
     * Sets the value of the persistentStoreDir property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPersistentStoreDir(String value) {
        this.persistentStoreDir = value;
    }

    /**
     * Gets the value of the statefulSessionClustering property.
     * 
     * @return
     *     possible object is
     *     {@link StatefulSessionClustering }
     *     
     */
    public StatefulSessionClustering getStatefulSessionClustering() {
        return statefulSessionClustering;
    }

    /**
     * Sets the value of the statefulSessionClustering property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatefulSessionClustering }
     *     
     */
    public void setStatefulSessionClustering(StatefulSessionClustering value) {
        this.statefulSessionClustering = value;
    }

    /**
     * Gets the value of the allowConcurrentCalls property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getAllowConcurrentCalls() {
        return allowConcurrentCalls;
    }

    /**
     * Sets the value of the allowConcurrentCalls property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAllowConcurrentCalls(Boolean value) {
        this.allowConcurrentCalls = value;
    }

    /**
     * Gets the value of the allowRemoveDuringTransaction property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getAllowRemoveDuringTransaction() {
        return allowRemoveDuringTransaction;
    }

    /**
     * Sets the value of the allowRemoveDuringTransaction property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAllowRemoveDuringTransaction(Boolean value) {
        this.allowRemoveDuringTransaction = value;
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
