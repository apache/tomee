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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.ejbd;

import org.apache.openejb.client.ClusterRequest;
import org.apache.openejb.client.ClusterResponse;
import org.apache.openejb.client.ClusterMetaData;
import org.apache.openejb.server.DiscoveryListener;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Exceptions;
import org.apache.openejb.util.Join;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @version $Rev$ $Date$
 */
public class ClusterRequestHandler implements DiscoveryListener {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE.createChild("cluster"), ClusterRequestHandler.class);

    private final Data data = new Data();

    public ClusterRequestHandler(EjbDaemon daemon) {
    }


    public void processRequest(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        ClusterRequest req = new ClusterRequest();
        ClusterResponse res = new ClusterResponse();

        try {
            req.readExternal(in);
        } catch (IOException e) {
            res.setFailure(e);
            sendErrorResponse("Cannot read ClusterRequest", e, res, out);
            throw e;
        } catch (ClassNotFoundException e) {
            res.setFailure(e);
            sendErrorResponse("Cannot read ClusterRequest", e, res, out);
            throw (IOException) new IOException().initCause(e);
        }

        ClusterMetaData currentClusterMetaData = data.current();

        if (req.getClusterMetaDataVersion() < currentClusterMetaData.getVersion()){
            if (logger.isDebugEnabled()) {
                URI[] locations = currentClusterMetaData.getLocations();
                if (locations.length < 10) {
                    logger.debug("Sending client updated cluster locations: [" + Join.join(", ", locations) + "]");
                } else {
                    logger.debug("Sending client updated cluster locations: " + locations.length + " locations total");
                }
            }
            res.setUpdatedMetaData(currentClusterMetaData);
        } else {
            res.setCurrent();
        }

        try {
            res.writeExternal(out);
        } catch (IOException e) {
            logger.error("Failed to write to ClusterResponse", e);
            throw e;
        }
    }

    private void sendErrorResponse(String message, Throwable t, ClusterResponse res, ObjectOutputStream out) throws IOException {
        logger.fatal(message, t);
        t = new IOException("The server has encountered a fatal error: " + message + " " + t).initCause(t);
        try {
            res.writeExternal(out);
        } catch (IOException ie) {
            String m = "Failed to write to ClusterResponse";
            logger.error(m, ie);
            throw Exceptions.newIOException(m, ie);
        }
    }

    public void serviceAdded(final URI uri) {
        try {
            URI type = uri;
            URI service = unwrap(type);

            if ("ejb".equals(type.getScheme())) {
                logger.info("Peer discovered: " + service.toString());
                data.add(service);
            }
        } catch (URISyntaxException e) {
            logger.error("serviceAdded: Invalid service URI format.  Expected <group>:<type>:<serverURI> but found '" + uri.toString() + "'");
        }
    }

    private URI unwrap(URI uri) throws URISyntaxException {
        return new URI(uri.getSchemeSpecificPart());
    }


    public void serviceRemoved(final URI uri) {
        try {
            URI type = uri;
            URI service = unwrap(type);

            if ("ejb".equals(type.getScheme())) {
                logger.info("Peer removed: " + service.toString());
                data.remove(service);
            }
        } catch (URISyntaxException e) {
            logger.error("serviceAdded: Invalid service URI format.  Expected <group>:<type>:<serverURI> but found '" + uri.toString() + "'");
        }
    }

    private static class Data {
        private ClusterMetaData current;
        private ReadWriteLock sync = new ReentrantReadWriteLock();
        private final java.util.Set set = new LinkedHashSet();

        public Data() {
            this.current = new ClusterMetaData(0);
        }

        public boolean add(URI o) {
            Lock lock = sync.writeLock();
            lock.lock();
            ClusterMetaData nextVersion = null;
            try {
                if (set.add(o)) {
                    nextVersion = newClusterMetaData(set, current);
                    return true;
                } else {
                    return false;
                }
            } finally {
                if (nextVersion != null) current = nextVersion;
                lock.unlock();
            }
        }

        public boolean remove(Object o) {
            Lock lock = sync.writeLock();
            lock.lock();
            ClusterMetaData nextVersion = null;
            try {
                if (set.remove(o)) {
                    nextVersion = newClusterMetaData(set, current);
                    return true;
                } else {
                    return false;
                }
            } finally {
                if (nextVersion != null) current = nextVersion;
                lock.unlock();
            }
        }

        private static ClusterMetaData newClusterMetaData(Set set, ClusterMetaData current) {
            URI[] locations = new URI[set.size()];
            set.toArray(locations);
            return new ClusterMetaData(System.currentTimeMillis(), locations);
        }

        public ClusterMetaData current() {
            Lock lock = sync.readLock();
            lock.lock();
            try {
                return current;
            } finally {
                lock.unlock();
            }
        }
    }

}
