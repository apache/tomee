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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for timer-scheduleType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="timer-scheduleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="second" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="minute" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="hour" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="day-of-month" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="month" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="day-of-week" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *         &lt;element name="year" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "timer-scheduleType", propOrder = {
    "second",
    "minute",
    "hour",
    "dayOfMonth",
    "month",
    "dayOfWeek",
    "year"
})
public class TimerSchedule {

    protected String second;
    protected String minute;
    protected String hour;
    @XmlElement(name = "day-of-month")
    protected String dayOfMonth;
    protected String month;
    @XmlElement(name = "day-of-week")
    protected String dayOfWeek;
    protected String year;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected java.lang.String id;

    public String getSecond() {
        return second;
    }

    public void setSecond(final String value) {
        this.second = value;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(final String value) {
        this.minute = value;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(final String value) {
        this.hour = value;
    }

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(final String value) {
        this.dayOfMonth = value;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(final String value) {
        this.month = value;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(final String value) {
        this.dayOfWeek = value;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String value) {
        this.year = value;
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(final java.lang.String value) {
        this.id = value;
    }
}
