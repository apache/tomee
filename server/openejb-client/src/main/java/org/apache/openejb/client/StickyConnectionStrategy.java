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

public class StickyConnectionStrategy implements ConnectionStrategy {
    private static final Logger LOGGER = Logger.getLogger("OpenEJB.client");

    private URI lastLocation;
    
    public Connection connect(ClusterMetaData cluster, ServerMetaData server) throws IOException {
        URI[] locations = cluster.getLocations();
        if (locations.length == 0){
            return connect(server.getLocation());
        }
        if (null != lastLocation) {
            for (URI uri : locations) {
                if (uri.equals(lastLocation)) {
                    try {
                        return connect(lastLocation);
                    } catch (IOException e) {
                        if (locations.length > 1){
                            LOGGER.log(Level.WARNING, "Failing over.  Cannot connect to last server: " + lastLocation.toString() + " Exception: " + e.getClass().getName() +" " + e.getMessage());
                        }
                    }
                }
            }
        }

        Connection connection = null;
        for (URI uri : locations) {
            if (uri.equals(lastLocation)) {
                continue;
            }
            try {
                connection = connect(uri);
                lastLocation = uri;
                break;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failover: Cannot connect to server(s): " + uri.toString() + " Exception: " + e.getMessage()+".  Trying next.");
            } catch (Throwable e) {
                throw new RemoteException("Failover: Cannot connect to server: " +  uri.toString() + " due to an unkown exception in the OpenEJB client: ", e);
            }
        }
        
        if (null != connection) {
            return connection;
        }

        // If no servers responded, throw an error
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < locations.length; i++) {
            URI uri = locations[i];
            buffer.append((i != 0 ? ", " : "") + "Server #" + i + ": " + uri);
        }
        throw new RemoteException("Cannot connect to any servers: " + buffer.toString());
    }

    protected Connection connect(URI uri) throws IOException {
        return ConnectionManager.getConnection(uri);
    }

}
