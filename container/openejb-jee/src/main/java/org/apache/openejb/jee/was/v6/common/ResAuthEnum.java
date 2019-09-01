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
package org.apache.openejb.jee.was.v6.common;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 *
 * Java class for ResAuthTypeBase.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 *
 * <pre>
 * &lt;simpleType name="ResAuthTypeBase"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName"&gt;
 *     &lt;enumeration value="Container"/&gt;
 *     &lt;enumeration value="Application"/&gt;
 *     &lt;enumeration value="SERVLET"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlEnum
public enum ResAuthEnum {

    @XmlEnumValue("Container")
    CONTAINER("Container"), @XmlEnumValue("Application")
    APPLICATION("Application"), SERVLET("SERVLET");
    private final String value;

    ResAuthEnum(final String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ResAuthEnum fromValue(final String v) {
        for (final ResAuthEnum c : ResAuthEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
