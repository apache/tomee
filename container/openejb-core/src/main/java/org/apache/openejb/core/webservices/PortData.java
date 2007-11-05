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
package org.apache.openejb.core.webservices;

import org.apache.openejb.Injection;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.PortInfo;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

public class PortData implements PortInfo {
    private String portId;
    private QName serviceName;
    private QName portName;
    private String bindingId;

    private URL wsdlUrl;
    private final List<HandlerChainData> handlerChains = new ArrayList<HandlerChainData>();
    private final List<Injection> injections = new ArrayList<Injection>();
    private boolean mtomEnabled;
    private QName wsdlPort;
    private QName wsdlService;
    private String location;

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    public QName getPortName() {
        return portName;
    }

    public void setPortName(QName portName) {
        this.portName = portName;
    }

    public String getBindingID() {
        return bindingId;
    }

    public void setBindingID(String bindingId) {
        this.bindingId = bindingId;
    }

    public URL getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(URL wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public List<HandlerChainData> getHandlerChains() {
        return handlerChains;
    }

    public List<Injection> getInjections() {
        return injections;
    }

    public boolean isMtomEnabled() {
        return mtomEnabled;
    }

    public void setMtomEnabled(boolean mtomEnabled) {
        this.mtomEnabled = mtomEnabled;
    }

    public QName getWsdlPort() {
        return wsdlPort;
    }

    public void setWsdlPort(QName wsdlPort) {
        this.wsdlPort = wsdlPort;
    }

    public QName getWsdlService() {
        return wsdlService;
    }

    public void setWsdlService(QName wsdlService) {
        this.wsdlService = wsdlService;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
