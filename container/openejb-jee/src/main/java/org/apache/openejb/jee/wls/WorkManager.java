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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for work-manager complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="work-manager"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="response-time-request-class" type="{http://www.bea.com/ns/weblogic/90}response-time-request-class"/&gt;
 *           &lt;element name="fair-share-request-class" type="{http://www.bea.com/ns/weblogic/90}fair-share-request-class"/&gt;
 *           &lt;element name="context-request-class" type="{http://www.bea.com/ns/weblogic/90}context-request-class"/&gt;
 *           &lt;element name="request-class-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="min-threads-constraint" type="{http://www.bea.com/ns/weblogic/90}min-threads-constraint"/&gt;
 *           &lt;element name="min-threads-constraint-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="max-threads-constraint" type="{http://www.bea.com/ns/weblogic/90}max-threads-constraint"/&gt;
 *           &lt;element name="max-threads-constraint-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="capacity" type="{http://www.bea.com/ns/weblogic/90}capacity"/&gt;
 *           &lt;element name="capacity-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="work-manager-shutdown-trigger" type="{http://www.bea.com/ns/weblogic/90}work-manager-shutdown-trigger"/&gt;
 *           &lt;element name="ignore-stuck-threads" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Gets the value of the responseTimeRequestClass property.
     *
     * @return possible object is
     * {@link ResponseTimeRequestClass }
     */
    public ResponseTimeRequestClass getResponseTimeRequestClass() {
        return responseTimeRequestClass;
    }

    /**
     * Sets the value of the responseTimeRequestClass property.
     *
     * @param value allowed object is
     *              {@link ResponseTimeRequestClass }
     */
    public void setResponseTimeRequestClass(final ResponseTimeRequestClass value) {
        this.responseTimeRequestClass = value;
    }

    /**
     * Gets the value of the fairShareRequestClass property.
     *
     * @return possible object is
     * {@link FairShareRequestClass }
     */
    public FairShareRequestClass getFairShareRequestClass() {
        return fairShareRequestClass;
    }

    /**
     * Sets the value of the fairShareRequestClass property.
     *
     * @param value allowed object is
     *              {@link FairShareRequestClass }
     */
    public void setFairShareRequestClass(final FairShareRequestClass value) {
        this.fairShareRequestClass = value;
    }

    /**
     * Gets the value of the contextRequestClass property.
     *
     * @return possible object is
     * {@link ContextRequestClass }
     */
    public ContextRequestClass getContextRequestClass() {
        return contextRequestClass;
    }

    /**
     * Sets the value of the contextRequestClass property.
     *
     * @param value allowed object is
     *              {@link ContextRequestClass }
     */
    public void setContextRequestClass(final ContextRequestClass value) {
        this.contextRequestClass = value;
    }

    /**
     * Gets the value of the requestClassName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRequestClassName() {
        return requestClassName;
    }

    /**
     * Sets the value of the requestClassName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRequestClassName(final String value) {
        this.requestClassName = value;
    }

    /**
     * Gets the value of the minThreadsConstraint property.
     *
     * @return possible object is
     * {@link MinThreadsConstraint }
     */
    public MinThreadsConstraint getMinThreadsConstraint() {
        return minThreadsConstraint;
    }

    /**
     * Sets the value of the minThreadsConstraint property.
     *
     * @param value allowed object is
     *              {@link MinThreadsConstraint }
     */
    public void setMinThreadsConstraint(final MinThreadsConstraint value) {
        this.minThreadsConstraint = value;
    }

    /**
     * Gets the value of the minThreadsConstraintName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMinThreadsConstraintName() {
        return minThreadsConstraintName;
    }

    /**
     * Sets the value of the minThreadsConstraintName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMinThreadsConstraintName(final String value) {
        this.minThreadsConstraintName = value;
    }

    /**
     * Gets the value of the maxThreadsConstraint property.
     *
     * @return possible object is
     * {@link MaxThreadsConstraint }
     */
    public MaxThreadsConstraint getMaxThreadsConstraint() {
        return maxThreadsConstraint;
    }

    /**
     * Sets the value of the maxThreadsConstraint property.
     *
     * @param value allowed object is
     *              {@link MaxThreadsConstraint }
     */
    public void setMaxThreadsConstraint(final MaxThreadsConstraint value) {
        this.maxThreadsConstraint = value;
    }

    /**
     * Gets the value of the maxThreadsConstraintName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMaxThreadsConstraintName() {
        return maxThreadsConstraintName;
    }

    /**
     * Sets the value of the maxThreadsConstraintName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxThreadsConstraintName(final String value) {
        this.maxThreadsConstraintName = value;
    }

    /**
     * Gets the value of the capacity property.
     *
     * @return possible object is
     * {@link Capacity }
     */
    public Capacity getCapacity() {
        return capacity;
    }

    /**
     * Sets the value of the capacity property.
     *
     * @param value allowed object is
     *              {@link Capacity }
     */
    public void setCapacity(final Capacity value) {
        this.capacity = value;
    }

    /**
     * Gets the value of the capacityName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCapacityName() {
        return capacityName;
    }

    /**
     * Sets the value of the capacityName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCapacityName(final String value) {
        this.capacityName = value;
    }

    /**
     * Gets the value of the workManagerShutdownTrigger property.
     *
     * @return possible object is
     * {@link WorkManagerShutdownTrigger }
     */
    public WorkManagerShutdownTrigger getWorkManagerShutdownTrigger() {
        return workManagerShutdownTrigger;
    }

    /**
     * Sets the value of the workManagerShutdownTrigger property.
     *
     * @param value allowed object is
     *              {@link WorkManagerShutdownTrigger }
     */
    public void setWorkManagerShutdownTrigger(final WorkManagerShutdownTrigger value) {
        this.workManagerShutdownTrigger = value;
    }

    /**
     * Gets the value of the ignoreStuckThreads property.
     *
     * @return possible object is
     * {@link Boolean }
     */
    public Boolean isIgnoreStuckThreads() {
        return ignoreStuckThreads;
    }

    /**
     * Sets the value of the ignoreStuckThreads property.
     *
     * @param value allowed object is
     *              {@link Boolean }
     */
    public void setIgnoreStuckThreads(final Boolean value) {
        this.ignoreStuckThreads = value;
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
