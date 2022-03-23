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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee.jpa;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for lock-mode-type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;simpleType name="lock-mode-type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="READ"/&gt;
 *     &lt;enumeration value="WRITE"/&gt;
 *     &lt;enumeration value="OPTIMISTIC"/&gt;
 *     &lt;enumeration value="OPTIMISTIC_FORCE_INCREMENT"/&gt;
 *     &lt;enumeration value="PESSIMISTIC_READ"/&gt;
 *     &lt;enumeration value="PESSIMISTIC_WRITE"/&gt;
 *     &lt;enumeration value="PESSIMISTIC_FORCE_INCREMENT"/&gt;
 *     &lt;enumeration value="NONE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlType(name = "lock-mode-type")
@XmlEnum
public enum LockModeType {

    READ,
    WRITE,
    OPTIMISTIC,
    OPTIMISTIC_FORCE_INCREMENT,
    PESSIMISTIC_READ,
    PESSIMISTIC_WRITE,
    PESSIMISTIC_FORCE_INCREMENT,
    NONE;

    public String value() {
        return name();
    }

    public static LockModeType fromValue(final String v) {
        return valueOf(v);
    }

}
