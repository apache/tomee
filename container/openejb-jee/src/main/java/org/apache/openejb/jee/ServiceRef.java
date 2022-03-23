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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlID;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * javaee6.xsd
 *
 * <p>Java class for service-refType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="service-refType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}descriptionGroup"/&gt;
 *         &lt;element name="service-ref-name" type="{http://java.sun.com/xml/ns/javaee}jndi-nameType"/&gt;
 *         &lt;element name="service-interface" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType"/&gt;
 *         &lt;element name="service-ref-type" type="{http://java.sun.com/xml/ns/javaee}fully-qualified-classType" minOccurs="0"/&gt;
 *         &lt;element name="wsdl-file" type="{http://java.sun.com/xml/ns/javaee}xsdAnyURIType" minOccurs="0"/&gt;
 *         &lt;element name="jaxrpc-mapping-file" type="{http://java.sun.com/xml/ns/javaee}pathType" minOccurs="0"/&gt;
 *         &lt;element name="service-qname" type="{http://java.sun.com/xml/ns/javaee}xsdQNameType" minOccurs="0"/&gt;
 *         &lt;element name="port-component-ref" type="{http://java.sun.com/xml/ns/javaee}port-component-refType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="handler" type="{http://java.sun.com/xml/ns/javaee}handlerType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *           &lt;element name="handler-chains" type="{http://java.sun.com/xml/ns/javaee}handler-chainsType" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;group ref="{http://java.sun.com/xml/ns/javaee}resourceGroup"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
    protected Set<InjectionTarget> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public ServiceRef name(final String serviceRefName) {
        this.serviceRefName = serviceRefName;
        return this;
    }

    public ServiceRef type(final String serviceRefType) {
        this.serviceRefType = serviceRefType;
        return this;
    }

    public ServiceRef type(final Class<?> serviceRefType) {
        return type(serviceRefType.getName());
    }

    public ServiceRef wsdl(final String wsdlFile) {
        this.wsdlFile = wsdlFile;
        return this;
    }

    public ServiceRef qname(final QName serviceQname) {
        this.serviceQname = serviceQname;
        return this;
    }

    public ServiceRef jaxrpcMappingFile(final String jaxrpcMappingFile) {
        this.jaxrpcMappingFile = jaxrpcMappingFile;
        return this;
    }

    public ServiceRef mappedName(final String mappedName) {
        this.mappedName = mappedName;
        return this;
    }

    public ServiceRef lookup(final String lookupName) {
        this.lookupName = lookupName;
        return this;
    }

    public ServiceRef injectionTarget(final String className, final String property) {
        getInjectionTarget().add(new InjectionTarget(className, property));

        // TODO move this to getKey()
        if (this.serviceRefName == null) {
            this.serviceRefName = "java:comp/env/" + className + "/" + property;
        }

        return this;
    }

    public ServiceRef injectionTarget(final Class<?> clazz, final String property) {
        return injectionTarget(clazz.getName(), property);
    }

    public String getName() {
        return getServiceRefName();
    }

    public String getKey() {
        final String name = getName();
        if (name == null || name.startsWith("java:")) return name;
        return "java:comp/env/" + name;
    }

    public String getType() {
        return getServiceRefType();
    }

    public void setName(final String name) {
        setServiceRefName(name);
    }

    public void setType(final String type) {
    }

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(final Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    @XmlElement(name = "display-name", required = true)
    public Text[] getDisplayNames() {
        return displayName.toArray();
    }

    public void setDisplayNames(final Text[] text) {
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

    public String getServiceRefName() {
        return serviceRefName;
    }

    public void setServiceRefName(final String value) {
        this.serviceRefName = value;
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(final String value) {
        this.serviceInterface = value;
    }

    public String getServiceRefType() {
        return serviceRefType;
    }

    public void setServiceRefType(final String value) {
        this.serviceRefType = value;
    }

    public String getWsdlFile() {
        return wsdlFile;
    }

    public void setWsdlFile(final String value) {
        this.wsdlFile = value;
    }

    public String getJaxrpcMappingFile() {
        return jaxrpcMappingFile;
    }

    public void setJaxrpcMappingFile(final String value) {
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
    public void setServiceQname(final QName value) {
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

    public void setHandlerChains(final HandlerChains value) {
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
            final HandlerChains handlerChains = new HandlerChains();
            final HandlerChain handlerChain = new HandlerChain();
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

    public void setMappedName(final String value) {
        this.mappedName = value;
    }

    public String getLookupName() {
        return lookupName;
    }

    public void setLookupName(final String lookupName) {
        this.lookupName = lookupName;
    }

    public Set<InjectionTarget> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new HashSet<InjectionTarget>();
        }
        return this.injectionTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(final String value) {
        this.id = value;
    }

    @Override
    public String toString() {
        return "ServiceRef{" +
            "name='" + serviceRefName + '\'' +
            ", interface='" + serviceInterface + '\'' +
            ", type='" + serviceRefType + '\'' +
            ", wsdl='" + wsdlFile + '\'' +
            ", qname=" + serviceQname +
            ", mappedName='" + mappedName + '\'' +
            ", lookupName='" + lookupName + '\'' +
            '}';
    }
}
