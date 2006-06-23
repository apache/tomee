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

import org.openejb.jee.webservice.Handler;
import org.openejb.jee.webservice.HandlerChain;
import org.openejb.jee.common.Icon;
import org.openejb.jee.common.JndiEnvironmentRef;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class ServiceRef extends JndiEnvironmentRef {
    private List<String> displayName = new ArrayList<String>();
    private List<Icon> icons = new ArrayList<Icon>();
    private String serviceRefName;
    private String serviceInterface;
    private String serviceRefType;
    private String wsdlFile;
    private String jaxrpcMappingFile;
    private String serviceQname;
    private String portComponentRef;
    private List<Handler> handlers = new ArrayList<Handler>();
    private List<HandlerChain> handlerChains = new ArrayList<HandlerChain>();

    public ServiceRef() {
    }

    public ServiceRef(String serviceRefName, String serviceInterface) {
        this.serviceRefName = serviceRefName;
        this.serviceInterface = serviceInterface;
    }

    public List<String> getDisplayName() {
        return displayName;
    }

    public void setDisplayName(List<String> displayName) {
        this.displayName = displayName;
    }

    public List<Icon> getIcons() {
        return icons;
    }

    public void setIcons(List<Icon> icons) {
        this.icons = icons;
    }

    public String getServiceRefName() {
        return serviceRefName;
    }

    public void setServiceRefName(String serviceRefName) {
        this.serviceRefName = serviceRefName;
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getServiceRefType() {
        return serviceRefType;
    }

    public void setServiceRefType(String serviceRefType) {
        this.serviceRefType = serviceRefType;
    }

    public String getWsdlFile() {
        return wsdlFile;
    }

    public void setWsdlFile(String wsdlFile) {
        this.wsdlFile = wsdlFile;
    }

    public String getJaxrpcMappingFile() {
        return jaxrpcMappingFile;
    }

    public void setJaxrpcMappingFile(String jaxrpcMappingFile) {
        this.jaxrpcMappingFile = jaxrpcMappingFile;
    }

    public String getServiceQname() {
        return serviceQname;
    }

    public void setServiceQname(String serviceQname) {
        this.serviceQname = serviceQname;
    }

    public String getPortComponentRef() {
        return portComponentRef;
    }

    public void setPortComponentRef(String portComponentRef) {
        this.portComponentRef = portComponentRef;
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
