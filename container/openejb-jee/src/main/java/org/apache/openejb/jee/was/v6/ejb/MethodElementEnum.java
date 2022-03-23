/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.openejb.jee.was.v6.ejb;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;

/**
 *
 * Java class for MethodElementKind.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 *
 * <pre>
 * &lt;simpleType name="MethodElementKind"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName"&gt;
 *     &lt;enumeration value="Unspecified"/&gt;
 *     &lt;enumeration value="Remote"/&gt;
 *     &lt;enumeration value="Home"/&gt;
 *     &lt;enumeration value="Local"/&gt;
 *     &lt;enumeration value="LocalHome"/&gt;
 *     &lt;enumeration value="ServiceEndpoint"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlEnum
public enum MethodElementEnum {

    @XmlEnumValue("Unspecified")
    UNSPECIFIED("Unspecified"), @XmlEnumValue("Remote")
    REMOTE("Remote"), @XmlEnumValue("Home")
    HOME("Home"), @XmlEnumValue("Local")
    LOCAL("Local"), @XmlEnumValue("LocalHome")
    LOCAL_HOME("LocalHome"), @XmlEnumValue("ServiceEndpoint")
    SERVICE_ENDPOINT("ServiceEndpoint");
    private final String value;

    MethodElementEnum(final String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MethodElementEnum fromValue(final String v) {
        for (final MethodElementEnum c : MethodElementEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
