package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dispatcherType")
@XmlEnum
public enum Dispatcher {

    FORWARD,
    REQUEST,
    INCLUDE,
    ASYNC,
    ERROR;

    public java.lang.String value() {
        return name();
    }

    public static Dispatcher fromValue(java.lang.String v) {
        return valueOf(v);
    }

}
