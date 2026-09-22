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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.webservices;

import org.junit.Test;

import javax.xml.namespace.QName;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PortAddressRegistryImplTest {
    private static final QName SERVICE = new QName("urn:test", "Service");
    private static final QName PORT = new QName("urn:test", "Port");

    @Test
    public void removedPortIsNotReturned() throws Exception {
        final PortAddressRegistryImpl registry = new PortAddressRegistryImpl();
        registry.addPort("service", SERVICE, "first", PORT, "org.acme.Api", "http://localhost/first");
        registry.removePort("service", SERVICE, "first", "org.acme.Api");

        assertTrue(registry.getPorts("first", SERVICE, "org.acme.Api").isEmpty());
        assertTrue(registry.getPorts(null, SERVICE, null).isEmpty());
    }

    @Test
    public void redeployedPortIsTheOnlyOne() throws Exception {
        final PortAddressRegistryImpl registry = new PortAddressRegistryImpl();
        registry.addPort("service", SERVICE, "first", PORT, "org.acme.Api", "http://localhost/first");
        registry.removePort("service", SERVICE, "first", "org.acme.Api");
        registry.addPort("service", SERVICE, "second", PORT, "org.acme.Api", "http://localhost/second");

        final Set<PortAddress> ports = registry.getPorts(null, SERVICE, "org.acme.Api");
        assertEquals(1, ports.size());
        assertEquals("http://localhost/second", ports.iterator().next().getAddress());
        assertEquals(1, registry.getPorts(null, SERVICE, null).size());
    }

    @Test
    public void removingUnknownPortIsANoop() throws Exception {
        final PortAddressRegistryImpl registry = new PortAddressRegistryImpl();
        registry.addPort("service", SERVICE, "first", PORT, "org.acme.Api", "http://localhost/first");
        registry.removePort("service", SERVICE, "unknown", "org.acme.Api");

        assertEquals(1, registry.getPorts(null, SERVICE, "org.acme.Api").size());
    }
}
