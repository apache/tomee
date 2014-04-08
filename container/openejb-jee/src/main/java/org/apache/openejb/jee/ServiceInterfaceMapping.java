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
 * The service-interface-mapping element defines how a Java type for
 * the service interface maps to a WSDL service.
 * <p/>
 * Used in: java-wsdl-mapping
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "service-interface-mappingType", propOrder = {
    "serviceInterface",
    "wsdlServiceName",
    "portMapping"
})
public class ServiceInterfaceMapping {
    @XmlElement(name = "service-interface", required = true)
    protected String serviceInterface;
    @XmlElement(name = "wsdl-service-name", required = true)
    protected QName wsdlServiceName;
    @XmlElement(name = "port-mapping")
    protected List<PortMapping> portMapping;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public String getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(String value) {
        this.serviceInterface = value;
    }

    public QName getWsdlServiceName() {
        return wsdlServiceName;
    }

    public void setWsdlServiceName(QName value) {
        this.wsdlServiceName = value;
    }

    public List<PortMapping> getPortMapping() {
        if (portMapping == null) {
            portMapping = new ArrayList<PortMapping>();
        }
        return this.portMapping;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
