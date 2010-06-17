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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * javaee_web_services_client_1_3.xsd
 *
 * <p>Java class for handlerType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="handlerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="handler-name" type="{http://java.sun.com/xml/ns/javaee}string"/>
 *         &lt;element name="handler-class" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="init-param" type="{http://java.sun.com/xml/ns/javaee}param-valueType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="soap-header" type="{http://java.sun.com/xml/ns/javaee}xsdQNameType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="soap-role" type="{http://java.sun.com/xml/ns/javaee}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="port-name" type="{http://java.sun.com/xml/ns/javaee}string" maxOccurs="unbounded" minOccurs="0"/>
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
//@XmlType(name = "port-component_handlerType", propOrder = {
@XmlType(name = "handlerType", propOrder = {
    "descriptions",
    "displayNames",
    "icon",
    "handlerName",
    "handlerClass",
    "initParam",
    "soapHeader",
    "soapRole",
    "portName"
})
public class Handler {
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon")
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();
    @XmlElement(name = "handler-name", required = true)
    protected String handlerName;
    @XmlElement(name = "handler-class", required = true)
    protected String handlerClass;
    @XmlElement(name = "init-param")
    protected List<ParamValue> initParam;
    @XmlElement(name = "soap-header")
    protected List<QName> soapHeader;
    @XmlElement(name = "soap-role")
    protected List<String> soapRole;
    // only used by service-refs
    @XmlElement(name = "port-name", required = true)
    protected List<String> portName;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    public void setDisplayNames(Text[] text) {
        displayName.set(text);
    }

    public String getDisplayName() {
        return displayName.get();
    }

    public Collection<Icon> getIcons() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon;
    }

    public Map<String, Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String value) {
        this.handlerName = value;
    }

    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(String value) {
        this.handlerClass = value;
    }

    public List<ParamValue> getInitParam() {
        if (initParam == null) {
            initParam = new ArrayList<ParamValue>();
        }
        return this.initParam;
    }

    public List<QName> getSoapHeader() {
        if (soapHeader == null) {
            soapHeader = new ArrayList<QName>();
        }
        return this.soapHeader;
    }

    public List<String> getSoapRole() {
        if (soapRole == null) {
            soapRole = new ArrayList<String>();
        }
        return this.soapRole;
    }

    public List<String> getPortName() {
        if (portName == null) {
            portName = new ArrayList<String>();
        }
        return this.portName;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
