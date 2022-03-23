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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The port-mapping defines the mapping of the WSDL port name attribute
 * to the Java name used to generate the Generated Service Interface
 * method get{java-name}.
 *
 * Used in: service-interface-mapping
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "port-mappingType", propOrder = {"portName", "javaPortName"})
public class PortMapping {
    @XmlElement(name = "port-name", required = true)
    protected String portName;
    @XmlElement(name = "java-port-name", required = true)
    protected String javaPortName;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getPortName() {
        return portName;
    }

    public void setPortName(final String value) {
        this.portName = value;
    }

    public String getJavaPortName() {
        return javaPortName;
    }

    public void setJavaPortName(final String value) {
        this.javaPortName = value;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }
}
