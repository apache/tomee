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
package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "serviceRefName",
    "portInfo",
    "callProperty",
    "wsdlOverride",
    "serviceImplClass",
    "serviceQname"
})
public class ServiceRef {
    @XmlElement(name = "service-ref-name", required = true)
    protected String serviceRefName;
    @XmlElement(name = "port-info")
    protected List<PortInfo> portInfo;
    @XmlElement(name = "call-property")
    protected List<CallProperty> callProperty;
    @XmlElement(name = "wsdl-override")
    protected String wsdlOverride;
    @XmlElement(name = "service-impl-class")
    protected String serviceImplClass;
    @XmlElement(name = "service-qname")
    protected ServiceQname serviceQname;

    public String getServiceRefName() {
        return serviceRefName;
    }

    public void setServiceRefName(String value) {
        this.serviceRefName = value;
    }

    public List<PortInfo> getPortInfo() {
        if (portInfo == null) {
            portInfo = new ArrayList<PortInfo>();
        }
        return this.portInfo;
    }

    public List<CallProperty> getCallProperty() {
        if (callProperty == null) {
            callProperty = new ArrayList<CallProperty>();
        }
        return this.callProperty;
    }

    public String getWsdlOverride() {
        return wsdlOverride;
    }

    public void setWsdlOverride(String value) {
        this.wsdlOverride = value;
    }

    public String getServiceImplClass() {
        return serviceImplClass;
    }

    public void setServiceImplClass(String value) {
        this.serviceImplClass = value;
    }

    public ServiceQname getServiceQname() {
        return serviceQname;
    }

    public void setServiceQname(ServiceQname value) {
        this.serviceQname = value;
    }
}
