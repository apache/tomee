
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "isolation-levelType")
@XmlEnum
public enum IsolationLevel {

    TRANSACTION_READ_UNCOMMITTED,
    TRANSACTION_READ_COMMITTED,
    TRANSACTION_REPEATABLE_READ,
    TRANSACTION_SERIALIZABLE;

    public java.lang.String value() {
        return name();
    }

    public static IsolationLevel fromValue(java.lang.String v) {
        return valueOf(v);
    }

}
