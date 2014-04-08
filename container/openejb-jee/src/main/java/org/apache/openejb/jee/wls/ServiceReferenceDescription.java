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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for service-reference-description complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="service-reference-description">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="service-ref-name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="wsdl-url" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="call-property" type="{http://www.bea.com/ns/weblogic/90}property-namevalue" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="port-info" type="{http://www.bea.com/ns/weblogic/90}port-info" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "service-reference-description", propOrder = {
    "serviceRefName",
    "wsdlUrl",
    "callProperty",
    "portInfo"
})
public class ServiceReferenceDescription {

    @XmlElement(name = "service-ref-name", required = true)
    protected String serviceRefName;
    @XmlElement(name = "wsdl-url", required = true)
    protected String wsdlUrl;
    @XmlElement(name = "call-property")
    protected List<PropertyNamevalue> callProperty;
    @XmlElement(name = "port-info")
    protected List<PortInfo> portInfo;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the serviceRefName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceRefName() {
        return serviceRefName;
    }

    /**
     * Sets the value of the serviceRefName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceRefName(String value) {
        this.serviceRefName = value;
    }

    /**
     * Gets the value of the wsdlUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWsdlUrl() {
        return wsdlUrl;
    }

    /**
     * Sets the value of the wsdlUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWsdlUrl(String value) {
        this.wsdlUrl = value;
    }

    /**
     * Gets the value of the callProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the callProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCallProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyNamevalue }
     * 
     * 
     */
    public List<PropertyNamevalue> getCallProperty() {
        if (callProperty == null) {
            callProperty = new ArrayList<PropertyNamevalue>();
        }
        return this.callProperty;
    }

    /**
     * Gets the value of the portInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the portInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPortInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PortInfo }
     * 
     * 
     */
    public List<PortInfo> getPortInfo() {
        if (portInfo == null) {
            portInfo = new ArrayList<PortInfo>();
        }
        return this.portInfo;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
