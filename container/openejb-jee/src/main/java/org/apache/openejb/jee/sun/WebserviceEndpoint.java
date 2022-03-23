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

import org.apache.openejb.jee.Keyable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "portComponentName",
    "endpointAddressUri",
    "loginConfigOrMessageSecurityBinding",
    "transportGuarantee",
    "serviceQname",
    "tieClass",
    "servletImplClass",
    "debuggingEnabled"
})
public class WebserviceEndpoint implements Keyable<String> {
    @XmlElement(name = "port-component-name", required = true)
    protected String portComponentName;
    @XmlElement(name = "endpoint-address-uri")
    protected String endpointAddressUri;
    @XmlElements({
        @XmlElement(name = "login-config", type = LoginConfig.class),
        @XmlElement(name = "message-security-binding", type = MessageSecurityBinding.class)
    })
    protected List<Object> loginConfigOrMessageSecurityBinding;
    @XmlElement(name = "transport-guarantee")
    protected String transportGuarantee;
    @XmlElement(name = "service-qname")
    protected ServiceQname serviceQname;
    @XmlElement(name = "tie-class")
    protected String tieClass;
    @XmlElement(name = "servlet-impl-class")
    protected String servletImplClass;
    @XmlElement(name = "debugging-enabled")
    protected String debuggingEnabled;

    public String getKey() {
        return portComponentName;
    }

    public String getPortComponentName() {
        return portComponentName;
    }

    public void setPortComponentName(final String value) {
        this.portComponentName = value;
    }

    public String getEndpointAddressUri() {
        return endpointAddressUri;
    }

    public void setEndpointAddressUri(final String value) {
        this.endpointAddressUri = value;
    }

    public List<Object> getLoginConfigOrMessageSecurityBinding() {
        if (loginConfigOrMessageSecurityBinding == null) {
            loginConfigOrMessageSecurityBinding = new ArrayList<Object>();
        }
        return this.loginConfigOrMessageSecurityBinding;
    }

    public String getTransportGuarantee() {
        return transportGuarantee;
    }

    public void setTransportGuarantee(final String value) {
        this.transportGuarantee = value;
    }

    public ServiceQname getServiceQname() {
        return serviceQname;
    }

    public void setServiceQname(final ServiceQname value) {
        this.serviceQname = value;
    }

    public String getTieClass() {
        return tieClass;
    }

    public void setTieClass(final String value) {
        this.tieClass = value;
    }

    public String getServletImplClass() {
        return servletImplClass;
    }

    public void setServletImplClass(final String value) {
        this.servletImplClass = value;
    }

    public String getDebuggingEnabled() {
        return debuggingEnabled;
    }

    public void setDebuggingEnabled(final String value) {
        this.debuggingEnabled = value;
    }
}
