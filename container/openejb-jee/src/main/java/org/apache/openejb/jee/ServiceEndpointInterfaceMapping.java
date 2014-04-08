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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * The service-endpoint-interface-mapping defines a tuple
 * to specify Service Endpoint Interfaces to
 * WSDL port types and WSDL bindings.
 * <p/>
 * An interface may be mapped to a port-type and binding multiple
 * times. This happens rarely.
 * <p/>
 * Used in: java-wsdl-mapping
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "service-endpoint-interface-mappingType", propOrder = {
    "serviceEndpointInterface",
    "wsdlPortType",
    "wsdlBinding",
    "serviceEndpointMethodMapping"
})
public class ServiceEndpointInterfaceMapping implements Keyable<String> {
    @XmlElement(name = "service-endpoint-interface", required = true)
    protected String serviceEndpointInterface;
    @XmlElement(name = "wsdl-port-type", required = true)
    protected QName wsdlPortType;
    @XmlElement(name = "wsdl-binding", required = true)
    protected QName wsdlBinding;
    @XmlElement(name = "service-endpoint-method-mapping")
    protected List<ServiceEndpointMethodMapping> serviceEndpointMethodMapping;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getKey() {
        return serviceEndpointInterface;
    }

    public String getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public void setServiceEndpointInterface(String value) {
        this.serviceEndpointInterface = value;
    }

    public QName getWsdlPortType() {
        return wsdlPortType;
    }

    public void setWsdlPortType(QName value) {
        this.wsdlPortType = value;
    }

    public QName getWsdlBinding() {
        return wsdlBinding;
    }

    public void setWsdlBinding(QName value) {
        this.wsdlBinding = value;
    }

    public List<ServiceEndpointMethodMapping> getServiceEndpointMethodMapping() {
        if (serviceEndpointMethodMapping == null) {
            serviceEndpointMethodMapping = new ArrayList<ServiceEndpointMethodMapping>();
        }
        return this.serviceEndpointMethodMapping;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
