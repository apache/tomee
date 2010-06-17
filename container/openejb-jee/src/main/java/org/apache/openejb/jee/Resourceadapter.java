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

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * connector_1_6.xsd
 *
 * <p>Java class for resourceadapterType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="resourceadapterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="resourceadapter-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="config-property" type="{http://java.sun.com/xml/ns/javaee}config-propertyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="outbound-resourceadapter" type="{http://java.sun.com/xml/ns/javaee}outbound-resourceadapterType" minOccurs="0"/>
 *         &lt;element name="inbound-resourceadapter" type="{http://java.sun.com/xml/ns/javaee}inbound-resourceadapterType" minOccurs="0"/>
 *         &lt;element name="adminobject" type="{http://java.sun.com/xml/ns/javaee}adminobjectType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="security-permission" type="{http://java.sun.com/xml/ns/javaee}security-permissionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "resourceadapterType", propOrder = {
//    "resourceAdapterClass",
//    "configProperty",
//    "outboundResourceAdapter",
//    "inboundResourceAdapter",
//    "adminObject",
//    "securityPermission"
//})
public class Resourceadapter extends ResourceadapterBase {

    public Resourceadapter() {
    }

    public Resourceadapter(Class resourceAdapterClass) {
        super(resourceAdapterClass);
    }

    public Resourceadapter(String resourceAdapterClass) {
        super(resourceAdapterClass);
    }

    @XmlElement(name = "config-property")
    public List<ConfigProperty> getConfigProperty() {
        return super.getConfigProperty();
    }

}