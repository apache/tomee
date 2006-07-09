/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.jee2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * The service-ref element declares a reference to a Web
 * service. It contains optional description, display name and
 * icons, a declaration of the required Service interface,
 * an optional WSDL document location, an optional set
 * of JAX-RPC mappings, an optional QName for the service element,
 * an optional set of Service Endpoint Interfaces to be resolved
 * by the container to a WSDL port, and an optional set of handlers.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "service-refType", propOrder = {
        "description",
        "displayName",
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
        "injectionTarget"
        })
public class ServiceRefType {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "display-name", required = true)
    protected List<Text> displayName;
    @XmlElement(required = true)
    protected List<IconType> icon;
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
    protected String serviceQname;
    @XmlElement(name = "port-component-ref", required = true)
    protected List<PortComponentRefType> portComponentRef;
    @XmlElement(required = true)
    protected List<ServiceRefHandlerType> handler;
    @XmlElement(name = "handler-chains")
    protected ServiceRefHandlerChainsType handlerChains;
    @XmlElement(name = "mapped-name")
    protected String mappedName;
    @XmlElement(name = "injection-target", required = true)
    protected List<InjectionTargetType> injectionTarget;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the description property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the description property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDescription().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    /**
     * Gets the value of the displayName property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the displayName property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getDisplayName().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Text }
     */
    public List<Text> getDisplayName() {
        if (displayName == null) {
            displayName = new ArrayList<Text>();
        }
        return this.displayName;
    }

    /**
     * Gets the value of the icon property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the icon property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getIcon().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link IconType }
     */
    public List<IconType> getIcon() {
        if (icon == null) {
            icon = new ArrayList<IconType>();
        }
        return this.icon;
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
    public String getServiceQname() {
        return serviceQname;
    }

    /**
     * Sets the value of the serviceQname property.
     */
    public void setServiceQname(String value) {
        this.serviceQname = value;
    }

    /**
     * Gets the value of the portComponentRef property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the portComponentRef property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getPortComponentRef().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link PortComponentRefType }
     */
    public List<PortComponentRefType> getPortComponentRef() {
        if (portComponentRef == null) {
            portComponentRef = new ArrayList<PortComponentRefType>();
        }
        return this.portComponentRef;
    }

    /**
     * Gets the value of the handler property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the handler property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getHandler().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceRefHandlerType }
     */
    public List<ServiceRefHandlerType> getHandler() {
        if (handler == null) {
            handler = new ArrayList<ServiceRefHandlerType>();
        }
        return this.handler;
    }

    public ServiceRefHandlerChainsType getHandlerChains() {
        return handlerChains;
    }

    public void setHandlerChains(ServiceRefHandlerChainsType value) {
        this.handlerChains = value;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String value) {
        this.mappedName = value;
    }

    /**
     * Gets the value of the injectionTarget property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the injectionTarget property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * getInjectionTarget().add(newItem);
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link InjectionTargetType }
     */
    public List<InjectionTargetType> getInjectionTarget() {
        if (injectionTarget == null) {
            injectionTarget = new ArrayList<InjectionTargetType>();
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
