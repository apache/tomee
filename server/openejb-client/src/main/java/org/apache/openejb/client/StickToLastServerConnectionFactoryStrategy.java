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

public class StickToLastServerConnectionFactoryStrategy implements ConnectionFactoryStrategy {
    private static final Logger LOGGER = Logger.getLogger("OpenEJB.client");

    private URI lastLocation;
    
    public Connection connect(URI[] locations, Request request) throws RemoteException {
        if (null != lastLocation) {
            for (int i = 0; i < locations.length; i++) {
                if (locations[i].equals(lastLocation)) {
                    try {
                        return connect(lastLocation);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Cannot connect to last server: " + lastLocation.getHost() + ":" + lastLocation.getPort() + " Exception: ", e);
                    }
                }
            }
        }
        
        Connection connection = null;
        for (int i = 0; i < locations.length; i++) {
            URI uri = locations[i];
            try {
                connection = connect(uri);
                lastLocation = uri;
                break;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Cannot connect to server(s): " + uri.getHost() + ":" + uri.getPort() + " Exception: ", e);
            } catch (Throwable e) {
                throw new RemoteException("Cannot connect to server: " + uri.getHost() + ":" + uri.getPort() + " due to an unkown exception in the OpenEJB client: ", e);
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
