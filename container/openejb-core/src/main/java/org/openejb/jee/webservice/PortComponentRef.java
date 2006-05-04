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

/**
 * @version $Revision$ $Date$
 */
public class PortComponentRef {
    private String id;
    private String serviceEndpointInterface;
    private boolean enableMtom;
    private String portComponentLink;


    public PortComponentRef() {
    }

    public PortComponentRef(String serviceEndpointInterface) {
        this.serviceEndpointInterface = serviceEndpointInterface;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public void setServiceEndpointInterface(String serviceEndpointInterface) {
        this.serviceEndpointInterface = serviceEndpointInterface;
    }

    public boolean isEnableMtom() {
        return enableMtom;
    }

    public void setEnableMtom(boolean enableMtom) {
        this.enableMtom = enableMtom;
    }

    public String getPortComponentLink() {
        return portComponentLink;
    }

    public void setPortComponentLink(String portComponentLink) {
        this.portComponentLink = portComponentLink;
    }

}
