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
package org.apache.openejb.jee.wls;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for port-info complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="port-info"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="port-name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="stub-property" type="{http://www.bea.com/ns/weblogic/90}property-namevalue" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="call-property" type="{http://www.bea.com/ns/weblogic/90}property-namevalue" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "port-info", propOrder = {
    "portName",
    "stubProperty",
    "callProperty"
})
public class PortInfo {

    @XmlElement(name = "port-name", required = true)
    protected String portName;
    @XmlElement(name = "stub-property")
    protected List<PropertyNamevalue> stubProperty;
    @XmlElement(name = "call-property")
    protected List<PropertyNamevalue> callProperty;

    /**
     * Gets the value of the portName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPortName() {
        return portName;
    }

    /**
     * Sets the value of the portName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPortName(final String value) {
        this.portName = value;
    }

    /**
     * Gets the value of the stubProperty property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stubProperty property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStubProperty().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyNamevalue }
     */
    public List<PropertyNamevalue> getStubProperty() {
        if (stubProperty == null) {
            stubProperty = new ArrayList<PropertyNamevalue>();
        }
        return this.stubProperty;
    }

    /**
     * Gets the value of the callProperty property.
     *
     *
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the callProperty property.
     *
     *
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCallProperty().add(newItem);
     * </pre>
     *
     *
     *
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyNamevalue }
     */
    public List<PropertyNamevalue> getCallProperty() {
        if (callProperty == null) {
            callProperty = new ArrayList<PropertyNamevalue>();
        }
        return this.callProperty;
    }

}
