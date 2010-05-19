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

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class StickyConnectionStrategy implements ConnectionStrategy {
    private static final Logger LOGGER = Logger.getLogger("OpenEJB.client");

    public Connection connect(ClusterMetaData cluster, ServerMetaData server) throws IOException {
        Set<URI> failed = Client.getFailed();

        URI[] locations = cluster.getLocations();

        if (locations.length == 0){
            return connect(cluster, server.getLocation());
        }

        URI lastLocation = cluster.getLastLocation();
        if (null != lastLocation && !failed.contains(lastLocation)) {
            try {
                return connect(cluster, lastLocation);
            } catch (IOException e) {
                if (locations.length > 1){
                    LOGGER.log(Level.WARNING, "Failing over.  Cannot connect to last server: " + lastLocation.toString() + " Exception: " + e.getClass().getName() +" " + e.getMessage());
                }
            }
        }


        Set<URI> remaining = new LinkedHashSet<URI>(Arrays.asList(locations));
        remaining.remove(lastLocation);
        remaining.removeAll(failed);

        for (URI uri : remaining) {
            try {
                return connect(cluster, uri);
            } catch (IOException e) {
                failed.add(uri);
                LOGGER.log(Level.WARNING, "Failover: Cannot connect to server(s): " + uri.toString() + " Exception: " + e.getMessage()+".  Trying next.");
            } catch (Throwable e) {
                failed.add(uri);
                throw new RemoteException("Failover: Cannot connect to server: " +  uri.toString() + " due to an unkown exception in the OpenEJB client: ", e);
            }
        }

        remaining.removeAll(failed);

        if (remaining.size() == 0 && server.getLocation() != null && !failed.contains(server.getLocation())){
            return connect(cluster, server.getLocation());
        }

        // If no servers responded, throw an error
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < locations.length; i++) {
            URI uri = locations[i];
            buffer.append((i != 0 ? ", " : "") + "Server #" + i + ": " + uri);
        }
        throw new RemoteException("Cannot connect to any servers: " + buffer.toString());
    }

    protected Connection connect(ClusterMetaData cluster, URI uri) throws IOException {
        Connection connection = ConnectionManager.getConnection(uri);

        // Grabbing the URI from the associated connection allows the ConnectionFactory to
        // employ discovery to find and connect to a server.  We then attempt to connect
        // to the discovered server rather than repeat the discovery process again.
        cluster.setLastLocation(connection.getURI());
        return connection;
    }

}
