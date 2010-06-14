
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addressing-responsesType")
@XmlEnum
public enum AddressingResponses
{
        @XmlEnumValue("ANONYMOUS")
        ANONYMOUS,
        @XmlEnumValue("NON_ANONYMOUS")
        NON_ANONYMOUS,
        @XmlEnumValue("ALL")
        ALL;

//        private final String value;

//        BodyContent(String v) {
//            value = v;
//        }
//
//        public String value() {
//            return value;
//        }
//
//        public static BodyContent fromValue(String v) {
//            for (BodyContent c : values()) {
//                if (c.value.equals(v)) {
//                    return c;
//                }
//            }
//            throw new IllegalArgumentException(v.toString());
//        }


}
