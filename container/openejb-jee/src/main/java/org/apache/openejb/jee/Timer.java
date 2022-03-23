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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for timerType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="timerType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="schedule" type="{http://java.sun.com/xml/ns/javaee}timer-scheduleType"/&gt;
 *         &lt;element name="start" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/&gt;
 *         &lt;element name="end" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/&gt;
 *         &lt;element name="timeout-method" type="{http://java.sun.com/xml/ns/javaee}named-methodType"/&gt;
 *         &lt;element name="persistent" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/&gt;
 *         &lt;element name="timezone" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="info" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "timerType", propOrder = {
    "descriptions",
    "schedule",
    "start",
    "end",
    "timeoutMethod",
    "persistent",
    "timezone",
    "info"
})
public class Timer {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(required = true)
    protected TimerSchedule schedule;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar start;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar end;
    @XmlElement(name = "timeout-method", required = true)
    protected NamedMethod timeoutMethod;
    protected Boolean persistent;
    protected String timezone;
    protected String info;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }


    /**
     * Gets the value of the schedule property.
     *
     * @return possible object is
     * {@link TimerSchedule }
     */
    public TimerSchedule getSchedule() {
        return schedule;
    }

    /**
     * Sets the value of the schedule property.
     *
     * @param value allowed object is
     *              {@link TimerSchedule }
     */
    public void setSchedule(final TimerSchedule value) {
        this.schedule = value;
    }

    /**
     * Gets the value of the start property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setStart(final XMLGregorianCalendar value) {
        this.start = value;
    }

    /**
     * Gets the value of the end property.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getEnd() {
        return end;
    }

    /**
     * Sets the value of the end property.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setEnd(final XMLGregorianCalendar value) {
        this.end = value;
    }

    /**
     * Gets the value of the timeoutMethod property.
     *
     * @return possible object is
     * {@link NamedMethod }
     */
    public NamedMethod getTimeoutMethod() {
        return timeoutMethod;
    }

    /**
     * Sets the value of the timeoutMethod property.
     *
     * @param value allowed object is
     *              {@link NamedMethod }
     */
    public void setTimeoutMethod(final NamedMethod value) {
        this.timeoutMethod = value;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    public void setPersistent(final Boolean value) {
        this.persistent = value;
    }

    /**
     * Gets the value of the timezone property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Sets the value of the timezone property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTimezone(final String value) {
        this.timezone = value;
    }

    /**
     * Gets the value of the info property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getInfo() {
        return info;
    }

    /**
     * Sets the value of the info property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setInfo(final String value) {
        this.info = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link java.lang.String }
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link java.lang.String }
     */
    public void setId(final java.lang.String value) {
        this.id = value;
    }
}
