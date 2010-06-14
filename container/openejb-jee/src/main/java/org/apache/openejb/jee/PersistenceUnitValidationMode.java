
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for persistence-unit-validation-mode-type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="persistence-unit-validation-mode-type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="AUTO"/>
 *     &lt;enumeration value="CALLBACK"/>
 *     &lt;enumeration value="NONE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "persistence-unit-validation-mode-type", namespace = "http://java.sun.com/xml/ns/persistence")
@XmlEnum
public enum PersistenceUnitValidationMode {

    AUTO,
    CALLBACK,
    NONE;

    public java.lang.String value() {
        return name();
    }

    public static PersistenceUnitValidationMode fromValue(java.lang.String v) {
        return valueOf(v);
    }

}
