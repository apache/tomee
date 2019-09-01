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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for service-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="service-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="service-ref-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="service-completion" type="{http://geronimo.apache.org/xml/ns/naming-1.2}service-completionType"/&gt;
 *           &lt;element name="port" type="{http://geronimo.apache.org/xml/ns/naming-1.2}portType" maxOccurs="unbounded"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "service-refType", namespace = "http://geronimo.apache.org/xml/ns/naming-1.2", propOrder = {
    "serviceRefName",
    "wsdlFile",
    "serviceCompletion",
    "port"
})
public class ServiceRefType {

    @XmlElement(name = "service-ref-name", required = true)
    protected String serviceRefName;
    @XmlElement(name = "wsdl-file")
    protected String wsdlFile;
    @XmlElement(name = "service-completion")
    protected ServiceCompletionType serviceCompletion;
    @XmlElement(name = "port")
    protected List<PortType> port;

    /**
     * Gets the value of the serviceRefName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getServiceRefName() {
        return serviceRefName;
    }

    /**
     * Sets the value of the serviceRefName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setServiceRefName(final String value) {
        this.serviceRefName = value;
    }

    /**
     * Gets the value of the serviceCompletion property.
     *
     * @return possible object is
     * {@link ServiceCompletionType }
     */
    public ServiceCompletionType getServiceCompletion() {
        return serviceCompletion;
    }

    /**
     * Sets the value of the serviceCompletion property.
     *
     * @param value allowed object is
     *              {@link ServiceCompletionType }
     */
    public void setServiceCompletion(final ServiceCompletionType value) {
        this.serviceCompletion = value;
    }

    /**
     * Gets the value of the port property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the port property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPort().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link PortType }
     */
    public List<PortType> getPort() {
        if (port == null) {
            port = new ArrayList<PortType>();
        }
        return this.port;
    }

    /**
     * Gets the value of the wsdlFile property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getWsdlFile() {
        return wsdlFile;
    }

    /**
     * Sets the value of the wsdlFile property.
     *
     * @param wsdlFile allowed object is
     *                 {@link String }
     */
    public void setWsdlFile(final String wsdlFile) {
        this.wsdlFile = wsdlFile;
    }
}
