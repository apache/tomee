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
package org.apache.openejb.server.cxf;

import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.openejb.core.webservices.PortData;

import javax.xml.namespace.QName;

/**
 * Used to overwrite serviceName and portName values of WebService annotation.
 */
public class CxfServiceConfiguration extends AbstractServiceConfiguration {
    private PortData port;

    public CxfServiceConfiguration(PortData portData) {
        this.port = portData;
    }

    public String getServiceName() {
        if (port.getWsdlService() != null) {
            return port.getWsdlService().getLocalPart();
        } else {
            return null;
        }
    }

    public String getServiceNamespace() {
        if (port.getWsdlService() != null) {
            return port.getWsdlService().getNamespaceURI();
        } else {
            return null;
        }
    }

    public QName getEndpointName() {
        return this.port.getWsdlPort();
    }

    public String getWsdlURL() {
        return (port.getWsdlUrl() == null) ? null : port.getWsdlUrl().toString();
    }
}
