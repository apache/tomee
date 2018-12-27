/*
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
    private final Map<String, PortAddress> portsById = new TreeMap<>();
    private final Map<String, Map<String, PortAddress>> portsByInterface = new TreeMap<>();
    private final Map<String, Map<String, PortAddress>> portsByServiceId = new TreeMap<>();
    private final Map<QName, Map<String, PortAddress>> portsByServiceQName = new HashMap<>();

    public synchronized void addPort(final String serviceId, final QName serviceQName, final String portId, final QName portQName, final String portInterface, final String address) throws OpenEJBException {
        if (serviceId == null) {
            throw new NullPointerException("serviceId is null");
        }
        if (serviceQName == null) {
            throw new NullPointerException("serviceQName is null");
        }
        if (portId == null) {
            throw new NullPointerException("portId is null");
        }
        if (portQName == null) {
            throw new NullPointerException("portQName is null");
        }
        if (address == null) {
            throw new NullPointerException("address is null");
        }

        // create portAddress
        PortAddress portAddress = portsById.get(portId);
        if (portAddress != null) { // shouldn't happen but better to avoid NPE here
            throw new OpenEJBException("A webservice port with qname " + portAddress.getPortQName() + " is already registered to the portId " + portId);
        }
        portAddress = new PortAddress(portId, serviceQName, portQName, address, portInterface);
        portsById.put(portId, portAddress);

        // portsByInterface
        Map<String, PortAddress> ports = null;
        if (portInterface != null) { // localbean have no interface
            ports = portsByInterface.computeIfAbsent(portInterface, k -> new TreeMap<>());
            ports.put(portId, portAddress);
        }

        // portsByServiceId
        ports = portsByServiceId.computeIfAbsent(serviceId, k -> new TreeMap<>());
        ports.put(portId, portAddress);

        // portsByServiceQName
        ports = portsByServiceQName.computeIfAbsent(serviceQName, k -> new TreeMap<>());
        ports.put(portId, portAddress);
    }

    public synchronized void removePort(final String serviceId, final QName serviceQName, final String portId, final String portInterface) {
        if (serviceId == null) {
            throw new NullPointerException("serviceId is null");
        }
        if (serviceQName == null) {
            throw new NullPointerException("serviceQName is null");
        }
        if (portId == null) {
            throw new NullPointerException("portId is null");
        }

        // remove from portById
        final PortAddress portAddress = portsById.remove(portId);
        if (portAddress != null) {
            // port was not registered
            return;
        }

        Map<String, PortAddress> ports = null;

        // remove from portsByInterface
        if (portInterface != null) {
            ports = portsByInterface.get(portInterface);
            if (ports != null) {
                ports.remove(portId);
                if (ports.isEmpty()) {
                    portsByInterface.remove(portInterface);
                }
            }
        }

        // remove from portsByServiceId
        ports = portsByServiceId.get(serviceId);
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

    public synchronized Set<PortAddress> getPorts(final String id, final QName serviceQName, final String referenceClassName) {
        if (serviceQName == null) {
            throw new NullPointerException("serviceQName is null");
        }

        // check if there is a port with the id
        if (id != null) {
            final PortAddress portAddress = portsById.get(id);
            if (portAddress != null) {
                return Collections.singleton(portAddress);
            }
        }

        // check if there is a unique port with the specifiec interface
        if (referenceClassName != null) {
            final Map<String, PortAddress> interfacePorts = portsByInterface.get(referenceClassName);
            if (interfacePorts != null && interfacePorts.size() == 1) {
                final PortAddress portAddress = interfacePorts.values().iterator().next();
                return Collections.singleton(portAddress);
            }
        }

        // find matching ports by id
        final Map<String, PortAddress> ports = new TreeMap<>();
        if (id != null) {
            final Map<String, PortAddress> idPorts = portsByServiceId.get(id);
            if (idPorts != null) {
                ports.putAll(idPorts);
            }
        }

        // find matching ports  by serviceQName
        if (ports.isEmpty()) {
            final Map<String, PortAddress> qnamePorts = portsByServiceQName.get(serviceQName);
            if (qnamePorts != null) {
                ports.putAll(qnamePorts);
            }
        }

        final Set<PortAddress> portAddresses = new HashSet<>(ports.values());
        return portAddresses;
    }

}
