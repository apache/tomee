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

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.transport.http.WSDLQueryHandler;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.xmlsoap.schemas.wsdl.http.AddressType;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WsdlQueryHandler extends WSDLQueryHandler {
    private static final Logger logger = Logger.getInstance(LogCategory.CXF, WsdlQueryHandler.class);

    public WsdlQueryHandler(Bus bus) {
        super(bus);
    }

    protected void updateDefinition(Definition def, Map<String, Definition> done, Map<String, SchemaReference> doneSchemas, String base, EndpointInfo ei) {
        if (done.get("") == def) {
            QName serviceName = ei.getService().getName();
            String portName = ei.getName().getLocalPart();
            updateServices(serviceName, portName, def, base);
        }
        super.updateDefinition(def, done, doneSchemas, base, ei);
    }

    private void updateServices(QName serviceName, String portName, Definition def, String baseUri) {
        boolean updated = false;
        Map services = def.getServices();
        if (services != null) {
            ArrayList<QName> servicesToRemove = new ArrayList<QName>();

            Iterator serviceIterator = services.entrySet().iterator();
            while (serviceIterator.hasNext()) {
                Map.Entry serviceEntry = (Map.Entry) serviceIterator.next();
                QName currServiceName = (QName) serviceEntry.getKey();
                if (currServiceName.equals(serviceName)) {
                    Service service = (Service) serviceEntry.getValue();
                    updatePorts(portName, service, baseUri);
                    updated = true;
                } else {
                    servicesToRemove.add(currServiceName);
                }
            }

            for (QName serviceToRemove : servicesToRemove) {
                def.removeService(serviceToRemove);
            }
        }
        if (!updated) {
            logger.warning("WSDL '" + serviceName.getLocalPart() + "' service not found.");
        }
    }

    private void updatePorts(String portName, Service service, String baseUri) {
        boolean updated = false;
        Map ports = service.getPorts();
        if (ports != null) {
            ArrayList<String> portsToRemove = new ArrayList<String>();

            Iterator portIterator = ports.entrySet().iterator();
            while (portIterator.hasNext()) {
                Map.Entry portEntry = (Map.Entry) portIterator.next();
                String currPortName = (String) portEntry.getKey();
                if (currPortName.equals(portName)) {
                    Port port = (Port) portEntry.getValue();
                    updatePortLocation(port, baseUri);
                    updated = true;
                } else {
                    portsToRemove.add(currPortName);
                }
            }

            for (String portToRemove : portsToRemove) {
                service.removePort(portToRemove);
            }
        }
        if (!updated) {
            logger.warning("WSDL '" + portName + "' port not found.");
        }
    }

    private void updatePortLocation(Port port, String baseUri) {
        List<?> exts = port.getExtensibilityElements();
        if (exts != null && exts.size() > 0) {
            ExtensibilityElement el = (ExtensibilityElement) exts.get(0);
            if (SOAPBindingUtil.isSOAPAddress(el)) {
                SoapAddress add = SOAPBindingUtil.getSoapAddress(el);
                add.setLocationURI(baseUri);
            }
            if (el instanceof AddressType) {
                AddressType add = (AddressType) el;
                add.setLocation(baseUri);
            }
        }
    }

}
