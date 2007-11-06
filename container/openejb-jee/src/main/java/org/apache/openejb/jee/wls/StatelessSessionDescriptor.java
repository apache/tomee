
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
 * <p>Java class for stateless-session-descriptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="stateless-session-descriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pool" type="{http://www.bea.com/ns/weblogic/90}pool" minOccurs="0"/>
 *         &lt;element name="timer-descriptor" type="{http://www.bea.com/ns/weblogic/90}timer-descriptor" minOccurs="0"/>
 *         &lt;element name="stateless-clustering" type="{http://www.bea.com/ns/weblogic/90}stateless-clustering" minOccurs="0"/>
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
@XmlType(name = "stateless-session-descriptor", propOrder = {
    "pool",
    "timerDescriptor",
    "statelessClustering"
})
public class StatelessSessionDescriptor {

    protected Pool pool;
    @XmlElement(name = "timer-descriptor")
    protected TimerDescriptor timerDescriptor;
    @XmlElement(name = "stateless-clustering")
    protected StatelessClustering statelessClustering;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the pool property.
     * 
     * @return
     *     possible object is
     *     {@link Pool }
     *     
     */
    public Pool getPool() {
        return pool;
    }

    /**
     * Sets the value of the pool property.
     * 
     * @param value
     *     allowed object is
     *     {@link Pool }
     *     
     */
    public void setPool(Pool value) {
        this.pool = value;
    }

    /**
     * Gets the value of the timerDescriptor property.
     * 
     * @return
     *     possible object is
     *     {@link TimerDescriptor }
     *     
     */
    public TimerDescriptor getTimerDescriptor() {
        return timerDescriptor;
    }

    /**
     * Sets the value of the timerDescriptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimerDescriptor }
     *     
     */
    public void setTimerDescriptor(TimerDescriptor value) {
        this.timerDescriptor = value;
    }

    /**
     * Gets the value of the statelessClustering property.
     * 
     * @return
     *     possible object is
     *     {@link StatelessClustering }
     *     
     */
    public StatelessClustering getStatelessClustering() {
        return statelessClustering;
    }

    /**
     * Sets the value of the statelessClustering property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatelessClustering }
     *     
     */
    public void setStatelessClustering(StatelessClustering value) {
        this.statelessClustering = value;
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
