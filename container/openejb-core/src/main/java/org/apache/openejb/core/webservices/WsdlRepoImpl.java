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

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class WsdlRepoImpl implements WsdlRepo {
    private Map<String, Set<String>> addressesByPortId = new TreeMap<String, Set<String>>();
    private Map<QName, Set<String>> addressesByQNames = new HashMap<QName, Set<String>>();
    private Map<String, Set<String>> addressesByInterface = new TreeMap<String, Set<String>>();

    public synchronized void addWsdl(String portId, QName qname, String intf, String address) {
        addAddress(addressesByPortId, portId, address);
        addAddress(addressesByQNames, qname, address);
        addAddress(addressesByInterface, intf, address);
    }

    @SuppressWarnings({"unchecked"})
    private synchronized void addAddress(Map addressesMap, Object key, String address) {
        if (key == null) return;
        Set<String> strings = (Set<String>) addressesMap.get(key);
        if (strings == null) {
            strings = new TreeSet<String>();
            addressesMap.put(key, strings);
        }
        strings.add(address);
    }

    public synchronized void removeWsdl(String portId, QName qname, String intf, String address) {
        removeAddress(addressesByPortId, portId, address);
        removeAddress(addressesByQNames, qname, address);
        removeAddress(addressesByInterface, intf, address);
    }

    @SuppressWarnings({"unchecked"})
    private synchronized void removeAddress(Map addressesMap, Object key, String address) {
        if (key == null) return;
        Set<String> strings = (Set<String>) addressesMap.get(key);
        if (strings != null) {
            strings.remove(address);
            if (strings.isEmpty()) {
                addressesMap.remove(key);
            }
        }
    }

    public synchronized String getWsdl(String portId, QName qname, String intf) {
        String address = null;

        // PortId
        Set<String> portIdAddresses = null;
        if (portId != null) {
            portIdAddresses = addressesByPortId.get(portId);
            address = getSingleAddress(portIdAddresses);
            if (address != null) {
                return address;
            }
        }

        // ServiceInterface
        Set<String> intfAddresses = null;
        if (intf != null) {
            intfAddresses = addressesByInterface.get(intf);
            address = getSingleAddress(intfAddresses);
            if (address != null) {
                return address;
            }
        }

        // ServiceQName
        Set<String> qnameAddresses = null;
        if (qname != null) {
            qnameAddresses = addressesByQNames.get(qname);
            address = getSingleAddress(qnameAddresses);
            if (address != null) {
                return address;
            }
        }

        // PortId + ServiceInterface
        address = getUniqueAddress(portIdAddresses, intfAddresses);
        if (address != null) {
            return address;
        }

        // PortId + ServiceQName
        address = getUniqueAddress(portIdAddresses, qnameAddresses);
        if (address != null) {
            return address;
        }

        // ServiceInterface + ServiceQName
        address = getUniqueAddress(intfAddresses, qnameAddresses);
        if (address != null) {
            return address;
        }

        // PortId + ServiceInterface + ServiceQName
        address = getUniqueAddress(portIdAddresses, intfAddresses, qnameAddresses);
        if (address != null) {
            return address;
        }

        return null;
    }

    private String getSingleAddress(Set<String> addresses) {
        if (addresses != null && addresses.size() == 1) {
            return addresses.iterator().next();
        }
        return null;
    }

    private String getUniqueAddress(Set<String>... addressSets) {
        if (addressSets.length < 2) return null;
        if (addressSets[0] == null) return null;

        Set<String> addresses = new HashSet<String>();
        addresses.addAll(addressSets[0]);
        for (Set<String> addressSet : addressSets) {
            if (addressSet != null) {
                addresses.retainAll(addressSet);
            }
        }
        return getSingleAddress(addresses);
    }
}
