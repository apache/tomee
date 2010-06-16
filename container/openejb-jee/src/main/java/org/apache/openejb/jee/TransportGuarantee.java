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
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 
 *         The transport-guaranteeType specifies that the communication
 *         between client and server should be NONE, INTEGRAL, or
 *         CONFIDENTIAL. NONE means that the application does not
 *         require any transport guarantees. A value of INTEGRAL means
 *         that the application requires that the data sent between the
 *         client and server be sent in such a way that it can't be
 *         changed in transit. CONFIDENTIAL means that the application
 *         requires that the data be transmitted in a fashion that
 *         prevents other entities from observing the contents of the
 *         transmission. In most cases, the presence of the INTEGRAL or
 *         CONFIDENTIAL flag will indicate that the use of SSL is
 *         required.
 *         
 *         Used in: user-data-constraint
 *         
 *       
 * 
 * <p>Java class for transport-guaranteeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="transport-guaranteeType">
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
@XmlType(name = "transport-guaranteeType")
@XmlEnum
public enum TransportGuarantee {

    NONE,
    INTEGRAL,
    CONFIDENTIAL;

    public java.lang.String value() {
        return name();
    }

    public static TransportGuarantee fromValue(java.lang.String v) {
        return valueOf(v);
    }

}
