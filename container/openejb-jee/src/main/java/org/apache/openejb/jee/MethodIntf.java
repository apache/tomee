/**
 *
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

import javax.xml.bind.annotation.XmlEnumValue;


/**
 * ejb-jar_3_1.xsd
 *
 * <p>Java class for method-intfType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="method-intfType"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;restriction base="&lt;http://java.sun.com/xml/ns/javaee&gt;string"&gt;
 *     &lt;/restriction&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
public enum MethodIntf {
    @XmlEnumValue("Home")HOME("Home"),
    @XmlEnumValue("Remote")REMOTE("Remote"),
    @XmlEnumValue("LocalHome")LOCALHOME("LocalHome"),
    @XmlEnumValue("Local")LOCAL("Local"),
    @XmlEnumValue("ServiceEndpoint")SERVICEENDPOINT("ServiceEndpoint"),
    @XmlEnumValue("Timer")TIMER("Timer"),
    @XmlEnumValue("MessageEndpoint")MESSAGEENDPOINT("MessageEndpoint");

    private final String name;

    private MethodIntf(final String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

}
