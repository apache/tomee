/**
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
package org.apache.openejb.client;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TransportSecurityPolicyTest {

    @Test
    public void plaintextLocationsAreDroppedForSecureBaseline() {
        final URI baseline = URI.create("ejbds://app1:4203");
        final URI[] pushed = {
            URI.create("ejbd://app2:4201"),
            URI.create("ejbds://app3:4203"),
            URI.create("http://app4:80/ejb"),
        };
        final URI[] filtered = TransportSecurityPolicy.filter(baseline, pushed);
        assertArrayEquals(new URI[]{URI.create("ejbds://app3:4203")}, filtered);
    }

    @Test
    public void plaintextBaselineIsNotFiltered() {
        final URI baseline = URI.create("ejbd://app1:4201");
        final URI[] pushed = {URI.create("ejbd://app2:4201"), URI.create("ejbds://app3:4203")};
        assertSame(pushed, TransportSecurityPolicy.filter(baseline, pushed));
    }

    @Test
    public void accepts() {
        assertTrue(TransportSecurityPolicy.accepts(URI.create("ejbds://a:4203"), URI.create("https://b:8443")));
        assertTrue(TransportSecurityPolicy.accepts(URI.create("ejbd://a:4201"), URI.create("ejbd://b:4201")));
        assertFalse(TransportSecurityPolicy.accepts(URI.create("ejbds://a:4203"), URI.create("ejbd://b:4201")));
        assertFalse(TransportSecurityPolicy.accepts(URI.create("zejbds://a:4203"), URI.create("zejbd://b:4201")));
    }

    @Test
    public void clusterUpdateKeepsTransportOfSecureBaseline() {
        final ServerMetaData server = new ServerMetaData(URI.create("ejbds://transport-test-1:4203"));
        final Client.Context context = Client.getContext(server);

        context.setClusterMetaData(new ClusterMetaData(2, URI.create("ejbd://member1:4201")));
        assertArrayEquals(new URI[]{URI.create("ejbds://transport-test-1:4203")},
            context.getClusterMetaData().getLocations());

        context.setClusterMetaData(new ClusterMetaData(3,
            URI.create("ejbd://member1:4201"), URI.create("ejbds://member2:4203")));
        assertArrayEquals(new URI[]{URI.create("ejbds://member2:4203")},
            context.getClusterMetaData().getLocations());
    }

    @Test
    public void mergeKeepsTransportOfSecureBaseline() {
        final ServerMetaData server = new ServerMetaData(URI.create("ejbds://transport-test-2:4203"));
        server.merge(new ServerMetaData(URI.create("ejbd://member1:4201")));
        assertArrayEquals(new URI[]{URI.create("ejbds://transport-test-2:4203")}, server.getLocations());

        server.merge(new ServerMetaData(URI.create("ejbds://member2:4203"), URI.create("ejbd://member1:4201")));
        assertArrayEquals(new URI[]{URI.create("ejbds://member2:4203")}, server.getLocations());
    }
}
