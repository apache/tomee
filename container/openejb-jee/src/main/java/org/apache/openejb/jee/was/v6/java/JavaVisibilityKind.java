
package org.apache.openejb.jee.was.v6.java;

import javax.xml.bind.annotation.XmlEnum;


/**
 * <p>Java class for JavaVisibilityKind.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="JavaVisibilityKind">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName">
 *     &lt;enumeration value="PUBLIC"/>
 *     &lt;enumeration value="PRIVATE"/>
 *     &lt;enumeration value="PROTECTED"/>
 *     &lt;enumeration value="PACKAGE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum JavaVisibilityKind {

    PUBLIC,
    PRIVATE,
    PROTECTED,
    PACKAGE;

    public String value() {
        return name();
    }

    public static JavaVisibilityKind fromValue(String v) {
        return valueOf(v);
    }

}
