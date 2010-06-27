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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import java.sql.Connection;

/**
 * javaee6.xsd
 *
 * <p>Java class for isolation-levelType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="isolation-levelType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="TRANSACTION_READ_UNCOMMITTED"/>
 *     &lt;enumeration value="TRANSACTION_READ_COMMITTED"/>
 *     &lt;enumeration value="TRANSACTION_REPEATABLE_READ"/>
 *     &lt;enumeration value="TRANSACTION_SERIALIZABLE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 *
 */

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

    public static IsolationLevel fromFlag(int flag) {
        if (flag == Connection.TRANSACTION_READ_UNCOMMITTED) {
            return TRANSACTION_READ_UNCOMMITTED;
        }
        if (flag == Connection.TRANSACTION_READ_COMMITTED) {
            return TRANSACTION_READ_COMMITTED;
        }
        if (flag == Connection.TRANSACTION_REPEATABLE_READ) {
            return TRANSACTION_REPEATABLE_READ;
        }
        if (flag == Connection.TRANSACTION_SERIALIZABLE) {
            return TRANSACTION_SERIALIZABLE;
        }
        //-1 is the annotation default
        if (flag == Connection.TRANSACTION_NONE || flag == -1) {
            return null;
        }
        throw new IllegalArgumentException("Invalid isolation level flag: " + flag);
    }

}
