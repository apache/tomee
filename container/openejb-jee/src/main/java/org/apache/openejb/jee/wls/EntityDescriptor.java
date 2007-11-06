
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
 * <p>Java class for entity-descriptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="entity-descriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pool" type="{http://www.bea.com/ns/weblogic/90}pool" minOccurs="0"/>
 *         &lt;element name="timer-descriptor" type="{http://www.bea.com/ns/weblogic/90}timer-descriptor" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="entity-cache" type="{http://www.bea.com/ns/weblogic/90}entity-cache"/>
 *           &lt;element name="entity-cache-ref" type="{http://www.bea.com/ns/weblogic/90}entity-cache-ref"/>
 *         &lt;/choice>
 *         &lt;element name="persistence" type="{http://www.bea.com/ns/weblogic/90}persistence" minOccurs="0"/>
 *         &lt;element name="entity-clustering" type="{http://www.bea.com/ns/weblogic/90}entity-clustering" minOccurs="0"/>
 *         &lt;element name="invalidation-target" type="{http://www.bea.com/ns/weblogic/90}invalidation-target" minOccurs="0"/>
 *         &lt;element name="enable-dynamic-queries" type="{http://www.bea.com/ns/weblogic/90}true-false" minOccurs="0"/>
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
@XmlType(name = "entity-descriptor", propOrder = {
    "pool",
    "timerDescriptor",
    "entityCache",
    "entityCacheRef",
    "persistence",
    "entityClustering",
    "invalidationTarget",
    "enableDynamicQueries"
})
public class EntityDescriptor {

    protected Pool pool;
    @XmlElement(name = "timer-descriptor")
    protected TimerDescriptor timerDescriptor;
    @XmlElement(name = "entity-cache")
    protected EntityCache entityCache;
    @XmlElement(name = "entity-cache-ref")
    protected EntityCacheRef entityCacheRef;
    protected Persistence persistence;
    @XmlElement(name = "entity-clustering")
    protected EntityClustering entityClustering;
    @XmlElement(name = "invalidation-target")
    protected InvalidationTarget invalidationTarget;
    @XmlElement(name = "enable-dynamic-queries")
    @XmlJavaTypeAdapter(TrueFalseAdapter.class)
    protected Boolean enableDynamicQueries;
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
     * Gets the value of the entityCache property.
     * 
     * @return
     *     possible object is
     *     {@link EntityCache }
     *     
     */
    public EntityCache getEntityCache() {
        return entityCache;
    }

    /**
     * Sets the value of the entityCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntityCache }
     *     
     */
    public void setEntityCache(EntityCache value) {
        this.entityCache = value;
    }

    /**
     * Gets the value of the entityCacheRef property.
     * 
     * @return
     *     possible object is
     *     {@link EntityCacheRef }
     *     
     */
    public EntityCacheRef getEntityCacheRef() {
        return entityCacheRef;
    }

    /**
     * Sets the value of the entityCacheRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntityCacheRef }
     *     
     */
    public void setEntityCacheRef(EntityCacheRef value) {
        this.entityCacheRef = value;
    }

    /**
     * Gets the value of the persistence property.
     * 
     * @return
     *     possible object is
     *     {@link Persistence }
     *     
     */
    public Persistence getPersistence() {
        return persistence;
    }

    /**
     * Sets the value of the persistence property.
     * 
     * @param value
     *     allowed object is
     *     {@link Persistence }
     *     
     */
    public void setPersistence(Persistence value) {
        this.persistence = value;
    }

    /**
     * Gets the value of the entityClustering property.
     * 
     * @return
     *     possible object is
     *     {@link EntityClustering }
     *     
     */
    public EntityClustering getEntityClustering() {
        return entityClustering;
    }

    /**
     * Sets the value of the entityClustering property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntityClustering }
     *     
     */
    public void setEntityClustering(EntityClustering value) {
        this.entityClustering = value;
    }

    /**
     * Gets the value of the invalidationTarget property.
     * 
     * @return
     *     possible object is
     *     {@link InvalidationTarget }
     *     
     */
    public InvalidationTarget getInvalidationTarget() {
        return invalidationTarget;
    }

    /**
     * Sets the value of the invalidationTarget property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvalidationTarget }
     *     
     */
    public void setInvalidationTarget(InvalidationTarget value) {
        this.invalidationTarget = value;
    }

    /**
     * Gets the value of the enableDynamicQueries property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getEnableDynamicQueries() {
        return enableDynamicQueries;
    }

    /**
     * Sets the value of the enableDynamicQueries property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableDynamicQueries(Boolean value) {
        this.enableDynamicQueries = value;
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
