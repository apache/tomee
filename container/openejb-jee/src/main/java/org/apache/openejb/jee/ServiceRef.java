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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * javaee6.xsd
 * 
 * <p>Java class for service-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="service-refType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/>
 *         &lt;element name="service-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/>
 *         &lt;element name="service-interface" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/>
 *         &lt;element name="service-ref-type" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/>
 *         &lt;element name="wsdl-file" type="{http://java.sun.com/xml/ns/javaee}xsdAnyURIType" minOccurs="0"/>
 *         &lt;element name="jaxrpc-mapping-file" type="{http://java.sun.com/xml/ns/javaee}pathType" minOccurs="0"/>
 *         &lt;element name="service-qname" type="{http://java.sun.com/xml/ns/javaee}xsdQNameType" minOccurs="0"/>
 *         &lt;element name="port-component-ref" type="{http://java.sun.com/xml/ns/javaee}port-component-refType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="handler" type="{http://java.sun.com/xml/ns/javaee}handlerType" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="handler-chains" type="{http://java.sun.com/xml/ns/javaee}handler-chainsType" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/>
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
@XmlType(name = "service-refType", propOrder = {
        "descriptions",
        "displayNames",
        "icon",
        "serviceRefName",
        "serviceInterface",
        "serviceRefType",
        "wsdlFile",
        "jaxrpcMappingFile",
        "serviceQname",
        "portComponentRef",
        "handler",
        "handlerChains",
        "mappedName",
        "injectionTarget",
        "lookupName"
        })
public class ServiceRef implements JndiReference {

    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlTransient
    protected TextMap displayName = new TextMap();
    @XmlElement(name = "icon", required = true)
    protected LocalCollection<Icon> icon = new LocalCollection<Icon>();
    @XmlElement(name = "service-ref-name", required = true)
    protected String serviceRefName;
    @XmlElement(name = "service-interface", required = true)
    protected String serviceInterface;
    @XmlElement(name = "service-ref-type")
    protected String serviceRefType;
    @XmlElement(name = "wsdl-file")
    protected String wsdlFile;
    @XmlElement(name = "jaxrpc-mapping-file")
    protected String jaxrpcMappingFile;
    @XmlElement(name = "service-qname")
    protected QName serviceQname;
    @XmlElement(name = "port-component-ref", required = true)
    protected List<PortComponentRef> portComponentRef;
    @XmlElement(required = true)
    protected List<Handler> handler;
    @XmlElement(name = "handler-chains")
    protected HandlerChains handlerChains;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "lookup-name")
    protected String lookupName;
    @XmlElement(name = "injection-target", required = true)
    protected List<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getName() {
        return getServiceRefName();
    }

    public String getKey() {
        return getName();
    }

    public String getType() {
        return getServiceRefType();
    }

    public void setName(String name) {
        setServiceRefName(name);
    }

    public void setType(String type) {
    }

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

    public Map<String,Icon> getIconMap() {
        if (icon == null) {
            icon = new LocalCollection<Icon>();
        }
        return icon.toMap();
    }

    public Icon getIcon() {
        return icon.getLocal();
    }

    public String getServiceRefName() {
        return serviceRefName;
    }

    public void setServiceRefName(String value) {
        this.serviceRefName = value;
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(String value) {
        this.serviceInterface = value;
    }

    public String getServiceRefType() {
        return serviceRefType;
    }

    public void setServiceRefType(String value) {
        this.serviceRefType = value;
    }

    public String getWsdlFile() {
        return wsdlFile;
    }

    public void setWsdlFile(String value) {
        this.wsdlFile = value;
    }

    public String getJaxrpcMappingFile() {
        return jaxrpcMappingFile;
    }

    public void setJaxrpcMappingFile(String value) {
        this.jaxrpcMappingFile = value;
    }

    /**
     * Gets the value of the serviceQname property.
     */
    public QName getServiceQname() {
        return serviceQname;
    }

    /**
     * Sets the value of the serviceQname property.
     */
    public void setServiceQname(QName value) {
        this.serviceQname = value;
    }

    public List<PortComponentRef> getPortComponentRef() {
        if (portComponentRef == null) {
            portComponentRef = new ArrayList<PortComponentRef>();
        }
        return this.portComponentRef;
    }

    public HandlerChains getHandlerChains() {
        return handlerChains;
    }

    public void setHandlerChains(HandlerChains value) {
        this.handlerChains = value;
    }

    public List<Handler> getHandler() {
        if (handler == null) {
            handler = new ArrayList<Handler>();
        }
        return this.handler;
    }
    
    public HandlerChains getAllHandlers() {
        // convert the handlers to handler chain
        if (handlerChains == null && handler != null) {
            HandlerChains handlerChains = new HandlerChains();
            HandlerChain handlerChain = new HandlerChain();
            handlerChain.getHandler().addAll(handler);
            handlerChains.getHandlerChain().add(handlerChain);
            return handlerChains;
        } else {
            return handlerChains;
        }
    }
    
    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String value) {
        this.mappedName = value;
    }

    public String getLookupName() {
        return lookupName;
    }

    public void setLookupName(String lookupName) {
        this.lookupName = lookupName;
    }

    public List<InjectionTarget> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new ArrayList<InjectionTarget>();
        }
        return this.injectionTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
