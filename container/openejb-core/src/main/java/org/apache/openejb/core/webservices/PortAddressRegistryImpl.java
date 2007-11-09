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

import org.apache.openejb.OpenEJBException;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PortAddressRegistryImpl implements PortAddressRegistry {
    private Map<String, PortAddress> portsById = new TreeMap<String, PortAddress>();
    private Map<String, Map<String, PortAddress>> portsByServiceId = new TreeMap<String, Map<String, PortAddress>>();
    private Map<QName, Map<String, PortAddress>> portsByServiceQName = new HashMap<QName, Map<String, PortAddress>>();

    public synchronized void addPort(String serviceId, QName serviceQName, String portId, QName portQName, String portInterface, String address) throws OpenEJBException {
        if (serviceId == null) throw new NullPointerException("serviceId is null");
        if (serviceQName == null) throw new NullPointerException("serviceQName is null");
        if (portId == null) throw new NullPointerException("portId is null");
        if (portQName == null) throw new NullPointerException("portQName is null");
        if (address == null) throw new NullPointerException("address is null");

        // create portAddress
        PortAddress portAddress = portsById.get(portId);
        if (portAddress != null) {
            throw new OpenEJBException("A webservice port with qname " + portAddress.getQName() + " is already registered to the portId " + portId);
        }
        portAddress = new PortAddress(portId, portQName, address, portInterface);
        portsById.put(portId, portAddress);


        Map<String, PortAddress> ports = portsByServiceId.get(serviceId);
        if (ports == null) {
            ports = new TreeMap<String, PortAddress>();
            portsByServiceId.put(serviceId, ports);
        }
        ports.put(portId, portAddress);

        ports = portsByServiceQName.get(serviceQName);
        if (ports == null) {
            ports = new TreeMap<String, PortAddress>();
            portsByServiceQName.put(serviceQName, ports);
        }
        ports.put(portId, portAddress);
    }

    public synchronized void removePort(String serviceId, QName serviceQName, String portId) {
        if (serviceId == null) throw new NullPointerException("serviceId is null");
        if (serviceQName == null) throw new NullPointerException("serviceQName is null");
        if (portId == null) throw new NullPointerException("portId is null");

        // remove from portById
        PortAddress portAddress = portsById.remove(portId);
        if (portAddress != null) {
            // port was not registered
            return;
        }

        // remove from portsByServiceId
        Map<String, PortAddress> ports = portsByServiceId.get(serviceId);
        if (ports != null) {
            ports.remove(portId);
            if (ports.isEmpty()) {
                portsByServiceId.remove(serviceId);
            }
        }

        // remove from portsByServiceQName
        ports = portsByServiceQName.get(serviceQName);
        if (ports != null) {
            ports.remove(portId);
            if (ports.isEmpty()) {
                portsByServiceId.remove(serviceId);
            }
        }
    }

    public synchronized Set<PortAddress> getPorts(String id, QName serviceQName) {
        if (serviceQName == null) throw new NullPointerException("serviceQName is null");

        // check if there is a port with the id
        if (id != null) {
            PortAddress portAddress = portsById.get(id);
            if (portAddress != null) {
                return Collections.singleton(portAddress);
            }
        }

        // find matching ports by id
        Map<String, PortAddress> ports = new TreeMap<String, PortAddress>();
        if (id != null) {
            Map<String, PortAddress> idPorts = portsByServiceId.get(id);
            if (idPorts != null) ports.putAll(idPorts);
        }

        // find matching ports  by serviceQName
        if (ports.isEmpty()) {
            Map<String, PortAddress> qnamePorts = portsByServiceQName.get(serviceQName);
            if (qnamePorts != null) ports.putAll(qnamePorts);
        }

        Set<PortAddress> portAddresses = new HashSet<PortAddress>(ports.values());
        return portAddresses;
    }

}
