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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.util.Properties;


/**
 * javaee_web_services_client_1_3.xsd
 *
 * <p>Java class for port-component-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="port-component-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="service-endpoint-interface" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="enable-mtom" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *         &lt;element name="mtom-threshold" type="{http://java.sun.com/xml/ns/javaee}xsdNonNegativeIntegerType" minOccurs="0"/>
 *         &lt;element name="addressing" type="{http://java.sun.com/xml/ns/javaee}addressingType" minOccurs="0"/>
 *         &lt;element name="respect-binding" type="{http://java.sun.com/xml/ns/javaee}respect-bindingType" minOccurs="0"/>
 *         &lt;element name="port-component-link" type="{http://java.sun.com/xml/ns/javaee}string" minOccurs="0"/>
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
@XmlType(name = "port-component-refType", propOrder = {
        "serviceEndpointInterface",
        "enableMtom",
        "mtomThreshold",
        "addressing",
        "respectBinding",
        "portComponentLink"
        })
public class PortComponentRef {

    @XmlElement(name = "service-endpoint-interface", required = true)
    protected String serviceEndpointInterface;
    @XmlElement(name = "enable-mtom")
    protected Boolean enableMtom;
    @XmlElement(name = "mtom-threshold")
    protected Integer mtomThreshold;
    protected Addressing addressing;
    @XmlElement(name = "respect-binding")
    protected Boolean respectBinding;
    @XmlElement(name = "port-component-link")
    protected String portComponentLink;
    @XmlTransient
    protected QName qname;
    @XmlTransient
    protected Properties properties;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public void setServiceEndpointInterface(String value) {
        this.serviceEndpointInterface = value;
    }

    public boolean isEnableMtom() {
        return enableMtom != null && enableMtom;
    }

    public void setEnableMtom(boolean value) {
        this.enableMtom = value;
    }

    public Integer getMtomThreshold() {
        return mtomThreshold;
    }

    public void setMtomThreshold(Integer value) {
        this.mtomThreshold = value;
    }

    public Addressing getAddressing() {
        return addressing;
    }

    public void setAddressing(Addressing value) {
        this.addressing = value;
    }

    public Boolean getRespectBinding() {
        return respectBinding;
    }

    public void setRespectBinding(Boolean value) {
        this.respectBinding = value;
    }

    public String getPortComponentLink() {
        return portComponentLink;
    }

    public void setPortComponentLink(String value) {
        this.portComponentLink = value;
    }

    public QName getQName() {
        return qname;
    }

    public void setQName(QName qname) {
        this.qname = qname;
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
