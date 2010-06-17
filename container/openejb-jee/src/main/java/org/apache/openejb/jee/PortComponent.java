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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.util.List;

/**
 * javaee_web_services_1_3.xsd
 * 
 * <p>Java class for port-componentType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="port-componentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://java.sun.com/xml/ns/javaee}descriptionType" minOccurs="0"/>
 *         &lt;element name="display-name" type="{http://java.sun.com/xml/ns/javaee}display-nameType" minOccurs="0"/>
 *         &lt;element name="icon" type="{http://java.sun.com/xml/ns/javaee}iconType" minOccurs="0"/>
 *         &lt;element name="port-component-name" type="{http://java.sun.com/xml/ns/javaee}string"/>
 *         &lt;element name="wsdl-service" type="{http://java.sun.com/xml/ns/javaee}xsdQNameType" minOccurs="0"/>
 *         &lt;element name="wsdl-port" type="{http://java.sun.com/xml/ns/javaee}xsdQNameType" minOccurs="0"/>
 *         &lt;element name="enable-mtom" type="{http://java.sun.com/xml/ns/javaee}true-falseType" minOccurs="0"/>
 *         &lt;element name="mtom-threshold" type="{http://java.sun.com/xml/ns/javaee}xsdNonNegativeIntegerType" minOccurs="0"/>
 *         &lt;element name="addressing" type="{http://java.sun.com/xml/ns/javaee}addressingType" minOccurs="0"/>
 *         &lt;element name="respect-binding" type="{http://java.sun.com/xml/ns/javaee}respect-bindingType" minOccurs="0"/>
 *         &lt;element name="protocol-binding" type="{http://java.sun.com/xml/ns/javaee}protocol-bindingType" minOccurs="0"/>
 *         &lt;element name="service-endpoint-interface" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="service-impl-bean" type="{http://java.sun.com/xml/ns/javaee}service-impl-beanType"/>
 *         &lt;choice>
 *           &lt;element name="handler" type="{http://java.sun.com/xml/ns/javaee}handlerType" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="handler-chains" type="{http://java.sun.com/xml/ns/javaee}handler-chainsType" minOccurs="0"/>
 *         &lt;/choice>
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
@XmlType(name = "port-componentType", propOrder = {
    "description",
    "displayName",
    "icon",
    "portComponentName",
    "wsdlService",
    "wsdlPort",
    "enableMtom",
        "mtomThreshold",
        "addressing",
        "respectBinding",
    "protocolBinding",
    "serviceEndpointInterface",
    "serviceImplBean",
    "handler",
    "handlerChains"
})
public class PortComponent implements Keyable<String> {
    protected String description;
    @XmlElement(name = "display-name")
    protected String displayName;
    protected Icon icon;
    @XmlElement(name = "port-component-name", required = true)
    protected String portComponentName;
    @XmlElement(name = "wsdl-service")
    protected QName wsdlService;
    @XmlElement(name = "wsdl-port")
    protected QName wsdlPort;
    @XmlElement(name = "enable-mtom")
    protected boolean enableMtom;
    @XmlElement(name = "mtom-threshold")
    protected Integer mtomThreshold;
    protected Addressing addressing;
    @XmlElement(name = "respect-binding")
    protected Boolean respectBinding;
    @XmlElement(name = "protocol-binding")
    protected String protocolBinding;
    @XmlElement(name = "service-endpoint-interface")
    protected String serviceEndpointInterface;
    @XmlElement(name = "service-impl-bean", required = true)
    protected ServiceImplBean serviceImplBean;
    protected List<Handler> handler;
    @XmlElement(name = "handler-chains")
    protected HandlerChains handlerChains;
    @XmlTransient
    protected String location;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getKey() {
        return portComponentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String value) {
        this.displayName = value;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon value) {
        this.icon = value;
    }

    public String getPortComponentName() {
        return portComponentName;
    }

    public void setPortComponentName(String value) {
        this.portComponentName = value;
    }

    public QName getWsdlService() {
        return wsdlService;
    }

    public void setWsdlService(QName value) {
        this.wsdlService = value;
    }

    public QName getWsdlPort() {
        return wsdlPort;
    }

    public void setWsdlPort(QName value) {
        this.wsdlPort = value;
    }

    public boolean isEnableMtom() {
        return enableMtom;
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

    public String getProtocolBinding() {
        return protocolBinding;
    }

    public void setProtocolBinding(String value) {
        this.protocolBinding = value;
    }

    public String getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public void setServiceEndpointInterface(String value) {
        this.serviceEndpointInterface = value;
    }

    public ServiceImplBean getServiceImplBean() {
        return serviceImplBean;
    }

    public void setServiceImplBean(ServiceImplBean value) {
        this.serviceImplBean = value;
    }

    public HandlerChains getHandlerChains() {
        // convert the handlers to handler chain
        if (handlerChains == null && handler != null) {
            handlerChains = new HandlerChains();
            HandlerChain handlerChain = new HandlerChain();
            handlerChain.getHandler().addAll(handler);
            handler = null;
            handlerChains.getHandlerChain().add(handlerChain);
        }
        return handlerChains;
    }

    public void setHandlerChains(HandlerChains value) {
        this.handlerChains = value;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
