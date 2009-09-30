
package org.apache.openejb.jee.was.v6.java;

import javax.xml.bind.annotation.XmlEnum;


/**
 * <p>Java class for TypeKind.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TypeKind">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName">
 *     &lt;enumeration value="UNDEFINED"/>
 *     &lt;enumeration value="CLASS"/>
 *     &lt;enumeration value="INTERFACE"/>
 *     &lt;enumeration value="EXCEPTION"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum TypeKind {

    UNDEFINED,
    CLASS,
    INTERFACE,
    EXCEPTION;

    public String value() {
        return name();
    }

    public static TypeKind fromValue(String v) {
        return valueOf(v);
    }

}
