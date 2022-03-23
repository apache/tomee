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
package org.apache.openejb.jee.was.v6.java;

import jakarta.xml.bind.annotation.XmlEnum;

/**
 *
 * Java class for JavaVisibilityKind.
 *
 *
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 *
 * <pre>
 * &lt;simpleType name="JavaVisibilityKind"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName"&gt;
 *     &lt;enumeration value="PUBLIC"/&gt;
 *     &lt;enumeration value="PRIVATE"/&gt;
 *     &lt;enumeration value="PROTECTED"/&gt;
 *     &lt;enumeration value="PACKAGE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlEnum
public enum JavaVisibilityEnum {

    PUBLIC, PRIVATE, PROTECTED, PACKAGE;

    public String value() {
        return name();
    }

    public static JavaVisibilityEnum fromValue(final String v) {
        return valueOf(v);
    }

}
