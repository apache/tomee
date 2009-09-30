
package org.apache.openejb.jee.was.v6.java;

import javax.xml.bind.annotation.XmlEnum;


/**
 * <p>Java class for JavaParameterKind.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="JavaParameterKind">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName">
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="OUT"/>
 *     &lt;enumeration value="INOUT"/>
 *     &lt;enumeration value="RETURN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum JavaParameterKind {

    IN,
    OUT,
    INOUT,
    RETURN;

    public String value() {
        return name();
    }

    public static JavaParameterKind fromValue(String v) {
        return valueOf(v);
    }

}
