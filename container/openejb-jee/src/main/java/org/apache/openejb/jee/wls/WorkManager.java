
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
 * <p>Java class for work-manager complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="work-manager">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="response-time-request-class" type="{http://www.bea.com/ns/weblogic/90}response-time-request-class"/>
 *           &lt;element name="fair-share-request-class" type="{http://www.bea.com/ns/weblogic/90}fair-share-request-class"/>
 *           &lt;element name="context-request-class" type="{http://www.bea.com/ns/weblogic/90}context-request-class"/>
 *           &lt;element name="request-class-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="min-threads-constraint" type="{http://www.bea.com/ns/weblogic/90}min-threads-constraint"/>
 *           &lt;element name="min-threads-constraint-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="max-threads-constraint" type="{http://www.bea.com/ns/weblogic/90}max-threads-constraint"/>
 *           &lt;element name="max-threads-constraint-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="capacity" type="{http://www.bea.com/ns/weblogic/90}capacity"/>
 *           &lt;element name="capacity-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="work-manager-shutdown-trigger" type="{http://www.bea.com/ns/weblogic/90}work-manager-shutdown-trigger"/>
 *           &lt;element name="ignore-stuck-threads" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;/choice>
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
@XmlType(name = "work-manager", propOrder = {
    "name",
    "responseTimeRequestClass",
    "fairShareRequestClass",
    "contextRequestClass",
    "requestClassName",
    "minThreadsConstraint",
    "minThreadsConstraintName",
    "maxThreadsConstraint",
    "maxThreadsConstraintName",
    "capacity",
    "capacityName",
    "workManagerShutdownTrigger",
    "ignoreStuckThreads"
})
public class WorkManager {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(name = "response-time-request-class")
    protected ResponseTimeRequestClass responseTimeRequestClass;
    @XmlElement(name = "fair-share-request-class")
    protected FairShareRequestClass fairShareRequestClass;
    @XmlElement(name = "context-request-class")
    protected ContextRequestClass contextRequestClass;
    @XmlElement(name = "request-class-name")
    protected String requestClassName;
    @XmlElement(name = "min-threads-constraint")
    protected MinThreadsConstraint minThreadsConstraint;
    @XmlElement(name = "min-threads-constraint-name")
    protected String minThreadsConstraintName;
    @XmlElement(name = "max-threads-constraint")
    protected MaxThreadsConstraint maxThreadsConstraint;
    @XmlElement(name = "max-threads-constraint-name")
    protected String maxThreadsConstraintName;
    protected Capacity capacity;
    @XmlElement(name = "capacity-name")
    protected String capacityName;
    @XmlElement(name = "work-manager-shutdown-trigger")
    protected WorkManagerShutdownTrigger workManagerShutdownTrigger;
    @XmlElement(name = "ignore-stuck-threads")
    protected Boolean ignoreStuckThreads;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the responseTimeRequestClass property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseTimeRequestClass }
     *     
     */
    public ResponseTimeRequestClass getResponseTimeRequestClass() {
        return responseTimeRequestClass;
    }

    /**
     * Sets the value of the responseTimeRequestClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseTimeRequestClass }
     *     
     */
    public void setResponseTimeRequestClass(ResponseTimeRequestClass value) {
        this.responseTimeRequestClass = value;
    }

    /**
     * Gets the value of the fairShareRequestClass property.
     * 
     * @return
     *     possible object is
     *     {@link FairShareRequestClass }
     *     
     */
    public FairShareRequestClass getFairShareRequestClass() {
        return fairShareRequestClass;
    }

    /**
     * Sets the value of the fairShareRequestClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link FairShareRequestClass }
     *     
     */
    public void setFairShareRequestClass(FairShareRequestClass value) {
        this.fairShareRequestClass = value;
    }

    /**
     * Gets the value of the contextRequestClass property.
     * 
     * @return
     *     possible object is
     *     {@link ContextRequestClass }
     *     
     */
    public ContextRequestClass getContextRequestClass() {
        return contextRequestClass;
    }

    /**
     * Sets the value of the contextRequestClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContextRequestClass }
     *     
     */
    public void setContextRequestClass(ContextRequestClass value) {
        this.contextRequestClass = value;
    }

    /**
     * Gets the value of the requestClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestClassName() {
        return requestClassName;
    }

    /**
     * Sets the value of the requestClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestClassName(String value) {
        this.requestClassName = value;
    }

    /**
     * Gets the value of the minThreadsConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link MinThreadsConstraint }
     *     
     */
    public MinThreadsConstraint getMinThreadsConstraint() {
        return minThreadsConstraint;
    }

    /**
     * Sets the value of the minThreadsConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link MinThreadsConstraint }
     *     
     */
    public void setMinThreadsConstraint(MinThreadsConstraint value) {
        this.minThreadsConstraint = value;
    }

    /**
     * Gets the value of the minThreadsConstraintName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinThreadsConstraintName() {
        return minThreadsConstraintName;
    }

    /**
     * Sets the value of the minThreadsConstraintName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinThreadsConstraintName(String value) {
        this.minThreadsConstraintName = value;
    }

    /**
     * Gets the value of the maxThreadsConstraint property.
     * 
     * @return
     *     possible object is
     *     {@link MaxThreadsConstraint }
     *     
     */
    public MaxThreadsConstraint getMaxThreadsConstraint() {
        return maxThreadsConstraint;
    }

    /**
     * Sets the value of the maxThreadsConstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link MaxThreadsConstraint }
     *     
     */
    public void setMaxThreadsConstraint(MaxThreadsConstraint value) {
        this.maxThreadsConstraint = value;
    }

    /**
     * Gets the value of the maxThreadsConstraintName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxThreadsConstraintName() {
        return maxThreadsConstraintName;
    }

    /**
     * Sets the value of the maxThreadsConstraintName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxThreadsConstraintName(String value) {
        this.maxThreadsConstraintName = value;
    }

    /**
     * Gets the value of the capacity property.
     * 
     * @return
     *     possible object is
     *     {@link Capacity }
     *     
     */
    public Capacity getCapacity() {
        return capacity;
    }

    /**
     * Sets the value of the capacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Capacity }
     *     
     */
    public void setCapacity(Capacity value) {
        this.capacity = value;
    }

    /**
     * Gets the value of the capacityName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCapacityName() {
        return capacityName;
    }

    /**
     * Sets the value of the capacityName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCapacityName(String value) {
        this.capacityName = value;
    }

    /**
     * Gets the value of the workManagerShutdownTrigger property.
     * 
     * @return
     *     possible object is
     *     {@link WorkManagerShutdownTrigger }
     *     
     */
    public WorkManagerShutdownTrigger getWorkManagerShutdownTrigger() {
        return workManagerShutdownTrigger;
    }

    /**
     * Sets the value of the workManagerShutdownTrigger property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkManagerShutdownTrigger }
     *     
     */
    public void setWorkManagerShutdownTrigger(WorkManagerShutdownTrigger value) {
        this.workManagerShutdownTrigger = value;
    }

    /**
     * Gets the value of the ignoreStuckThreads property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIgnoreStuckThreads() {
        return ignoreStuckThreads;
    }

    /**
     * Sets the value of the ignoreStuckThreads property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIgnoreStuckThreads(Boolean value) {
        this.ignoreStuckThreads = value;
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
