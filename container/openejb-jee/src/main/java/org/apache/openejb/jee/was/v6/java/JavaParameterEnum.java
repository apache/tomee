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

import javax.xml.bind.annotation.XmlEnum;

/**
 * <p/>
 * Java class for JavaParameterKind.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <p/>
 * <pre>
 * &lt;simpleType name="JavaParameterKind">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName">
 *     &lt;enumeration value="IN"/>
 *     &lt;enumeration value="OUT"/>
 *     &lt;enumeration value="INOUT"/>
 *     &lt;enumeration value="RETURN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlEnum
public enum JavaParameterEnum {

    IN, OUT, INOUT, RETURN;

    public String value() {
        return name();
    }

    public static JavaParameterEnum fromValue(final String v) {
        return valueOf(v);
    }

}
