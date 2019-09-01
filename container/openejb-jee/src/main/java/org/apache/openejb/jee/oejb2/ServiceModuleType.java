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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for serviceModuleType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="serviceModuleType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/deployment-1.2}environment"/&gt;
 *         &lt;element ref="{http://geronimo.apache.org/xml/ns/deployment-1.2}service" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "serviceModuleType", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", propOrder = {
    "environment",
    "service"
})
public class ServiceModuleType {

    @XmlElement(name = "environment", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", required = true)
    protected EnvironmentType environment;
    @XmlElementRef(name = "service", namespace = "http://geronimo.apache.org/xml/ns/deployment-1.2", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractServiceType>> service;

    /**
     * Gets the value of the environment property.
     *
     * @return possible object is
     * {@link EnvironmentType }
     */
    public EnvironmentType getEnvironment() {
        return environment;
    }

    /**
     * Sets the value of the environment property.
     *
     * @param value allowed object is
     *              {@link EnvironmentType }
     */
    public void setEnvironment(final EnvironmentType value) {
        this.environment = value;
    }

    /**
     * Gets the value of the service property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the service property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getService().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link GbeanType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractServiceType }{@code >}
     */
    public List<JAXBElement<? extends AbstractServiceType>> getService() {
        if (service == null) {
            service = new ArrayList<JAXBElement<? extends AbstractServiceType>>();
        }
        return this.service;
    }

}
