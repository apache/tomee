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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @since J2EE1.4 The message-destinationType specifies a message destination.
 * The logical destination described by this element is mapped to a
 * physical destination by the Deployer.
 *
 * The message destination element contains:
 *
 * - an optional description - an optional display-name - an optional
 * icon - a message destination name which must be unique among message
 * destination names within the same Deployment File.
 *
 * Example:
 *
 *
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;message-destination xmlns:com.ibm.etools.j2ee.common="common.xmi" xmlns:com.ibm.etools.webservice.wsclient="webservice_client.xmi" xmlns:jaxb="http://java.sun.com/xml/ns/jaxb" xmlns:org.eclipse.jem.java="java.xmi" xmlns:xmi="http://www.omg.org/XMI" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;&lt;message-destination-name&gt;
 *
 * 						CorporateStocks
 *
 * 					&lt;/message-destination-name&gt;
 *
 * 				&lt;/message-destination&gt;
 * </pre>
 *
 *
 *
 *
 * Java class for MessageDestination complex type.
 *
 *
 * The following schema fragment specifies the expected content contained
 * within this class.
 *
 * <pre>
 * &lt;complexType name="MessageDestination"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{common.xmi}CompatibilityDescriptionGroup"&gt;
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageDestination")
public class MessageDestination extends CompatibilityDescriptionGroup {

    @XmlAttribute
    protected String name;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

}
