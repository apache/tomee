/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.openejb.jee.webservice;

import org.openejb.jee.javaee.Icon;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class PortComponent {
    private String id;
    private String description;
    private String displayName;
    private Icon icon;
    private String portComponentName;
    private QName wsdlService;
    private QName wsdlPort;
    private boolean enableMtom;
    private String protocolBinding;
    private String serviceEndpointInterface;
    private ServiceImplBean serviceImplBean;
    private List<Handler> handlers = new ArrayList<Handler>();
    private List<HandlerChain> handlerChains = new ArrayList<HandlerChain>();

    public PortComponent() {
    }

    public PortComponent(String portComponentName, ServiceImplBean serviceImplBean) {
        this.portComponentName = portComponentName;
        this.serviceImplBean = serviceImplBean;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public String getPortComponentName() {
        return portComponentName;
    }

    public void setPortComponentName(String portComponentName) {
        this.portComponentName = portComponentName;
    }

    public QName getWsdlService() {
        return wsdlService;
    }

    public void setWsdlService(QName wsdlService) {
        this.wsdlService = wsdlService;
    }

    public QName getWsdlPort() {
        return wsdlPort;
    }

    public void setWsdlPort(QName wsdlPort) {
        this.wsdlPort = wsdlPort;
    }

    public boolean isEnableMtom() {
        return enableMtom;
    }

    public void setEnableMtom(boolean enableMtom) {
        this.enableMtom = enableMtom;
    }

    public String getProtocolBinding() {
        return protocolBinding;
    }

    public void setProtocolBinding(String protocolBinding) {
        this.protocolBinding = protocolBinding;
    }

    public String getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public void setServiceEndpointInterface(String serviceEndpointInterface) {
        this.serviceEndpointInterface = serviceEndpointInterface;
    }

    public ServiceImplBean getServiceImplBean() {
        return serviceImplBean;
    }

    public void setServiceImplBean(ServiceImplBean serviceImplBean) {
        this.serviceImplBean = serviceImplBean;
    }

    public List<Handler> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }

    public List<HandlerChain> getHandlerChains() {
        return handlerChains;
    }

    public void setHandlerChains(List<HandlerChain> handlerChains) {
        this.handlerChains = handlerChains;
    }
}
