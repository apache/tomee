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

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlList;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

/**
 * javaee_web_services_client_1_3.xsd
 *
 * <p>Java class for handler-chainType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="handler-chainType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="service-name-pattern" type="{http://java.sun.com/xml/ns/javaee}qname-pattern"/&gt;
 *           &lt;element name="port-name-pattern" type="{http://java.sun.com/xml/ns/javaee}qname-pattern"/&gt;
 *           &lt;element name="protocol-bindings" type="{http://java.sun.com/xml/ns/javaee}protocol-bindingListType"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="handler" type="{http://java.sun.com/xml/ns/javaee}handlerType" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "handler-chainType", propOrder = {
    "serviceNamePattern",
    "portNamePattern",
    "protocolBindings",
    "handler"
})
public class HandlerChain {
    @XmlJavaTypeAdapter(HandlerChainsStringQNameAdapter.class)
    @XmlElement(name = "service-name-pattern")
    protected QName serviceNamePattern;
    @XmlJavaTypeAdapter(HandlerChainsStringQNameAdapter.class)
    @XmlElement(name = "port-name-pattern")
    protected QName portNamePattern;
    @XmlList
    @XmlElement(name = "protocol-bindings")
    protected List<String> protocolBindings;
    @XmlElement(required = true)
    protected List<Handler> handler;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public QName getServiceNamePattern() {
        return serviceNamePattern;
    }

    public void setServiceNamePattern(final QName value) {
        this.serviceNamePattern = value;
    }

    public QName getPortNamePattern() {
        return portNamePattern;
    }

    public void setPortNamePattern(final QName value) {
        this.portNamePattern = value;
    }

    public List<String> getProtocolBindings() {
        if (protocolBindings == null) {
            protocolBindings = new ArrayList<String>();
        }
        return this.protocolBindings;
    }

    public List<Handler> getHandler() {
        if (handler == null) {
            handler = new ArrayList<Handler>();
        }
        return this.handler;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }
}
