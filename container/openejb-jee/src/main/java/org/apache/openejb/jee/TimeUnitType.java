
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import java.util.concurrent.TimeUnit;


/**
 * 
 * 
 *         The time-unit-typeType represents a time duration at a given
 *         unit of granularity.  
 *         
 *         The time unit type must be one of the following :
 *         
 *         Days
 *         Hours
 *         Minutes
 *         Seconds
 *         Milliseconds
 *         Microseconds
 *         Nanoseconds
 *         
 *       
 * 
 * <p>Java class for time-unit-typeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="time-unit-typeType">
 *   &lt;simpleContent>
 *     &lt;restriction base="&lt;http://java.sun.com/xml/ns/javaee>string">
 *     &lt;/restriction>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "time-unit-typeType")
@XmlEnum
public enum TimeUnitType {

    Days,
    Hours,
    Minutes,
    Seconds,
    Milliseconds,
    Microseconds,
    Nanoseconds;

    public java.lang.String value() {
        return name();
    }

    public static TimeUnitType fromValue(java.lang.String v) {
        return valueOf(v);
    }

    public TimeUnit toTimeUnit() {
        return TimeUnit.valueOf(name().toUpperCase());
    }

}
