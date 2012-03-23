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
package org.apache.openejb.client;

import org.apache.openejb.client.event.BootstrappingConnection;
import org.apache.openejb.client.event.FailoverSelection;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractConnectionStrategy implements ConnectionStrategy {
    public Connection connect(ClusterMetaData cluster, ServerMetaData server) throws IOException {
        final Set<URI> failed = Client.getFailed();
        final Set<URI> remaining = new HashSet<URI>();

        boolean failover = false;

        final Iterable<URI> iterable = getIterable(cluster);
        for (URI uri : iterable) {
            if (failed.contains(uri)) continue;

            if (failover) Client.fireEvent(createFailureEvent(remaining, failed, uri));

            try {
                return connect(cluster, uri);
            } catch (IOException e) {

                if (!failover) {
                    Collections.addAll(remaining, cluster.getLocations());
                    remaining.removeAll(failed);
                }

                failed.add(uri);
                remaining.remove(uri);
                failover = true;
            }
        }

        final URI uri = server.getLocation();

        if (uri == null) throw new RemoteFailoverException("Attempted to connect to " + failed.size() + " servers.");

        Client.fireEvent(new BootstrappingConnection(uri));

        return connect(cluster, uri);
    }

    private Iterable<URI> getIterable(ClusterMetaData cluster) {
        final Context context = cluster.getContext();
        final StrategyData data = context.getComponent(StrategyData.class);

        if (data != null) return data.getIterable();

        context.setComponent(StrategyData.class, new StrategyData(createIterable(cluster)));

        return getIterable(cluster);
    }

    protected abstract FailoverSelection createFailureEvent(Set<URI> remaining, Set<URI> failed, URI uri);

    protected abstract Iterable<URI> createIterable(ClusterMetaData cluster);

    protected Connection connect(ClusterMetaData cluster, URI uri) throws IOException {
        Connection connection = ConnectionManager.getConnection(uri);

        // Grabbing the URI from the associated connection allows the ConnectionFactory to
        // employ discovery to find and connect to a server.  We then attempt to connect
        // to the discovered server rather than repeat the discovery process again.
        cluster.setLastLocation(connection.getURI());
        return connection;
    }

    private static class StrategyData {
        private final Iterable<URI> iterable;

        private StrategyData(Iterable<URI> iterable) {
            this.iterable = iterable;
        }

        public Iterable<URI> getIterable() {
            return iterable;
        }
    }
}
