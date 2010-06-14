
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 
 *         The tracking modes for sessions created by this web
 *         application
 *         
 *         Used in: session-config
 *         
 *       
 * 
 * <p>Java class for tracking-modeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tracking-modeType">
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
@XmlType(name = "tracking-modeType")
@XmlEnum
public enum TrackingMode {

    COOKIE,
    URL,
    SSL;

    public java.lang.String value() {
        return name();
    }

    public static TrackingMode fromValue(java.lang.String v) {
        return valueOf(v);
    }

}
