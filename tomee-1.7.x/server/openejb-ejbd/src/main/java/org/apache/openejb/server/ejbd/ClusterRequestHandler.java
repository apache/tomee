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

import org.apache.openejb.client.ClusterMetaData;
import org.apache.openejb.client.ClusterRequest;
import org.apache.openejb.client.ClusterResponse;
import org.apache.openejb.client.ProtocolMetaData;
import org.apache.openejb.client.Response;
import org.apache.openejb.server.DiscoveryListener;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @version $Rev$ $Date$
 */
public class ClusterRequestHandler extends RequestHandler implements DiscoveryListener {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE.createChild("cluster"), ClusterRequestHandler.class);

    private final Data data = new Data();

    public ClusterRequestHandler(final EjbDaemon daemon) {
        super(daemon);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public ClusterResponse processRequest(final ObjectInputStream in, final ProtocolMetaData metaData) throws Exception {

        final ClusterRequest req = new ClusterRequest();
        req.setMetaData(metaData);

        final ClusterResponse res = new ClusterResponse();
        res.setMetaData(metaData);

        try {
            req.readExternal(in);
        } catch (IOException e) {
            res.setFailure(e);
            return res;
        } catch (ClassNotFoundException e) {
            res.setFailure(new IOException().initCause(e));
            return res;
        }

        final ClusterMetaData currentClusterMetaData = data.current();

        if (req.getClusterMetaDataVersion() < currentClusterMetaData.getVersion()) {
            if (logger.isDebugEnabled()) {
                final URI[] locations = currentClusterMetaData.getLocations();
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

        return res;
    }

    @Override
    public String getName() {
        return "Cluster";
    }

    @Override
    public void processResponse(final Response response, final ObjectOutputStream out, final ProtocolMetaData metaData) throws Exception {
        if (null != response) {

            if (ClusterResponse.class.isInstance(response)) {

                final ClusterResponse res = (ClusterResponse) response;

                try {
                    res.setMetaData(metaData);
                    res.writeExternal(out);
                } catch (IOException e) {
                    logger.error("Failed to write to ClusterResponse", e);
                    throw e;
                }
            } else {
                logger.error("ClusterRequestHandler cannot process an instance of: " + response.getClass().getName());
            }
        }
    }

    @Override
    public void serviceAdded(final URI uri) {
        try {
            final URI service = unwrap(uri);

            if ("ejb".equals(uri.getScheme())) {
                logger.info("Peer discovered: " + service.toString());
                data.add(service);
            }
        } catch (URISyntaxException e) {
            logger.error("serviceAdded: Invalid service URI format.  Expected <group>:<type>:<serverURI> but found '" + uri.toString() + "'");
        }
    }

    private URI unwrap(final URI uri) throws URISyntaxException {
        return new URI(uri.getSchemeSpecificPart());
    }

    @Override
    public void serviceRemoved(final URI uri) {
        try {
            final URI service = unwrap(uri);

            if ("ejb".equals(uri.getScheme())) {
                logger.info("Peer removed: " + service.toString());
                data.remove(service);
            }
        } catch (URISyntaxException e) {
            logger.error("serviceAdded: Invalid service URI format.  Expected <group>:<type>:<serverURI> but found '" + uri.toString() + "'");
        }
    }

    private static class Data {

        private final AtomicReference<ClusterMetaData> current = new AtomicReference<ClusterMetaData>();
        private final ReadWriteLock sync = new ReentrantReadWriteLock();
        private final java.util.Set set = new LinkedHashSet();

        public Data() {
            this.current.set(new ClusterMetaData(0));
        }

        @SuppressWarnings("unchecked")
        public boolean add(final URI o) {

            final Lock lock = sync.writeLock();
            lock.lock();

            try {
                ClusterMetaData nextVersion = null;
                try {
                    if (set.add(o)) {
                        nextVersion = newClusterMetaData(set, current.get());
                        return true;
                    } else {
                        return false;
                    }
                } finally {
                    if (nextVersion != null) {
                        current.set(nextVersion);
                    }
                }
            } finally {

                lock.unlock();
            }
        }

        public boolean remove(final Object o) {
            final Lock lock = sync.writeLock();
            lock.lock();

            try {
                ClusterMetaData nextVersion = null;
                try {
                    if (set.remove(o)) {
                        nextVersion = newClusterMetaData(set, current.get());
                        return true;
                    } else {
                        return false;
                    }
                } finally {
                    if (nextVersion != null) {
                        current.set(nextVersion);
                    }
                }
            } finally {

                lock.unlock();
            }
        }

        private static ClusterMetaData newClusterMetaData(final Set set, final ClusterMetaData current) {
            final URI[] locations = new URI[set.size()];
            set.toArray(locations);
            return new ClusterMetaData(System.currentTimeMillis(), locations);
        }

        public ClusterMetaData current() {
            final Lock lock = sync.readLock();
            lock.lock();
            try {
                return current.get();
            } finally {
                lock.unlock();
            }
        }
    }

}
