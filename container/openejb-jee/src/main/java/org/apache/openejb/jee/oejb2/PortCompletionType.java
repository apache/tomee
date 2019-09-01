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

package org.apache.openejb.jee.oejb2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for port-completionType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="port-completionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="port" type="{http://geronimo.apache.org/xml/ns/naming-1.2}portType"/&gt;
 *         &lt;element name="binding-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "port-completionType", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", propOrder = {
    "port",
    "bindingName"
})
public class PortCompletionType {

    @XmlElement(required = true)
    protected PortType port;
    @XmlElement(name = "binding-name", required = true)
    protected String bindingName;

    /**
     * Gets the value of the port property.
     *
     * @return possible object is
     * {@link PortType }
     */
    public PortType getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     *
     * @param value allowed object is
     *              {@link PortType }
     */
    public void setPort(final PortType value) {
        this.port = value;
    }

    /**
     * Gets the value of the bindingName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getBindingName() {
        return bindingName;
    }

    /**
     * Sets the value of the bindingName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setBindingName(final String value) {
        this.bindingName = value;
    }

}
