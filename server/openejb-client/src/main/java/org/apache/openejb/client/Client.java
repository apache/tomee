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

import org.apache.openejb.client.event.ClientVersion;
import org.apache.openejb.client.event.ClusterMetaDataUpdated;
import org.apache.openejb.client.event.ObserverAdded;
import org.apache.openejb.client.event.RequestFailed;
import org.apache.openejb.client.event.RetryConditionAdded;
import org.apache.openejb.client.event.RetryConditionRemoved;
import org.apache.openejb.client.event.RetryingRequest;
import org.apache.openejb.client.event.ServerAdded;
import org.apache.openejb.client.event.ServerRemoved;

import javax.naming.AuthenticationException;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.openejb.client.Exceptions.newIOException;

public class Client {

    public static final String OPENEJB_CLIENT_RETRY_CONDITION_MAX = "openejb.client.retry.condition.max";
    private static final String OPENEJB_CLIENT_COMPATIBILITY_VERSION = "openejb.client.protocol.version";

    private static final Logger logger = Logger.getLogger("OpenEJB.client");
    private boolean FINEST = logger.isLoggable(Level.FINEST);
    private boolean FINER = logger.isLoggable(Level.FINER);

    public static final ThreadLocal<Set<URI>> failed = new ThreadLocal<Set<URI>>();
    private static final ProtocolMetaData PROTOCOL_META_DATA = new ProtocolMetaData();

    private static final int maxConditionRetry = Integer.parseInt(System.getProperty(OPENEJB_CLIENT_RETRY_CONDITION_MAX, "20"));
    private static Client client = new Client();
    private static final ProtocolMetaData COMPATIBLE_META_DATA;

    static {
        final String version = System.getProperty(OPENEJB_CLIENT_COMPATIBILITY_VERSION);
        COMPATIBLE_META_DATA = (null != version ? new ProtocolMetaData(version) : null);
    }

    private List<Class<? extends Throwable>> retryConditions = new CopyOnWriteArrayList<Class<? extends Throwable>>();
    private boolean retry = false;

    private final Observers observers = new Observers();

    public Client() {
        final String retryValue = System.getProperty("openejb.client.requestretry", getRetry() + "");
        retry = Boolean.valueOf(retryValue);

        observers.addObserver(new EventLogger());
        observers.fireEvent(new ClientVersion());
    }

    public static void addEventObserver(final Object observer) {
        if (observer == null) {
            throw new IllegalArgumentException("observer cannot be null");
        }

        if (client.observers.addObserver(observer)) {
            fireEvent(new ObserverAdded(observer));
        }
    }

    public static void removeEventObserver(final Object observer) {
        if (observer == null) {
            throw new IllegalArgumentException("observer cannot be null");
        }

        if (client.observers.removeObserver(observer)) {
            fireEvent(new ObserverAdded(observer));
        }
    }

    public static void fireEvent(final Object event) {
        client.observers.fireEvent(event);
    }

    public static boolean addRetryCondition(final Class<? extends Throwable> throwable) {
        if (throwable == null) {
            throw new IllegalArgumentException("throwable cannot be null");
        }
        final boolean add = client.retryConditions.add(throwable);
        if (add) {
            fireEvent(new RetryConditionAdded(throwable));
        }
        return add;
    }

    public static boolean removeRetryCondition(final Class<? extends Throwable> throwable) {
        if (throwable == null) {
            throw new IllegalArgumentException("throwable cannot be null");
        }
        final boolean remove = client.retryConditions.remove(throwable);
        if (remove) {
            fireEvent(new RetryConditionRemoved(throwable));
        }
        return remove;
    }

    // This lame hook point if only of testing
    public static void setClient(final Client client) {
        if (client == null) {
            throw new IllegalArgumentException("client cannot be null");
        }
        Client.client = client;
    }

    public static Response request(final Request req, final Response res, final ServerMetaData server) throws RemoteException {
        try {
            return client.processRequest(req, res, server);
        } finally {
            failed.remove();
        }
    }

    protected Response processRequest(final Request req, final Response res, final ServerMetaData server) throws RemoteException {

        if (server == null) {
            throw new IllegalArgumentException("Server instance cannot be null");
        }

        final long start = System.nanoTime();
        final ClusterMetaData cluster = getClusterMetaData(server);

        //Determine which protocol to use for request writes
        final ProtocolMetaData protocolRequest = (null != COMPATIBLE_META_DATA ? COMPATIBLE_META_DATA : PROTOCOL_META_DATA);

        /*----------------------------*/
        /* Get a connection to server */
        /*----------------------------*/

        final Connection conn;
        try {
            conn = ConnectionManager.getConnection(cluster, server, req);
        } catch (final IOException e) {
            throw new RemoteException("Unable to connect", e);
        }

        OutputStream out = null;
        InputStream in = null;

        try {


            /*----------------------------------*/
            /* Get output streams */
            /*----------------------------------*/
            try {

                out = conn.getOutputStream();

            } catch (final IOException e) {
                throw newIOException("Cannot open output stream to server: ", e);
            }

            /*----------------------------------*/
            /* Write the protocol magic         */
            /*----------------------------------*/
            try {
                protocolRequest.writeExternal(out);
                out.flush();
            } catch (final IOException e) {
                throw newIOException("Cannot write the protocol metadata to the server: ", e);
            }

            /*----------------------------------*/
            /* Get output streams */
            /*----------------------------------*/
            final ObjectOutput objectOut;
            try {
                objectOut = new ObjectOutputStream(out);
            } catch (final IOException e) {
                throw newIOException("Cannot open object output stream to server: ", e);
            }

            /*----------------------------------*/
            /* Write ServerMetaData */
            /*----------------------------------*/
            try {
                server.setMetaData(protocolRequest);
                server.writeExternal(objectOut);
            } catch (final IOException e) {
                throw newIOException("Cannot write the ServerMetaData to the server: ", e);
            }

            /*----------------------------------*/
            /* Write ClusterMetaData */
            /*----------------------------------*/
            try {

                final ClusterRequest clusterRequest = new ClusterRequest(cluster);
                clusterRequest.setMetaData(protocolRequest);
                objectOut.write(clusterRequest.getRequestType().getCode());
                clusterRequest.writeExternal(objectOut);
            } catch (final Throwable e) {
                throw newIOException("Cannot write the ClusterMetaData to the server: ", e);
            }

            /*----------------------------------*/
            /* Write request type */
            /*----------------------------------*/
            try {
                objectOut.write(req.getRequestType().getCode());
            } catch (final IOException e) {
                throw newIOException("Cannot write the request type to the server: ", e);
            }

            /*----------------------------------*/
            /* Write request */
            /*----------------------------------*/
            try {

                req.setMetaData(protocolRequest);
                req.writeExternal(objectOut);
                objectOut.flush();
                out.flush();

            } catch (final java.io.NotSerializableException e) {

                throw new IllegalArgumentException("Object is not serializable: " + e.getMessage());

            } catch (final IOException e) {

                throw newIOException("Cannot write the request to the server: " + e.getMessage(), e);
            }

            /*----------------------------------*/
            /* Get input streams               */
            /*----------------------------------*/

            try {
                if (conn instanceof HttpConnectionFactory.HttpConnection) {
                    final HttpConnectionFactory.HttpConnection httpConn = (HttpConnectionFactory.HttpConnection) conn;
                    if (httpConn.getResponseCode() == 401) {
                        throw new AuthenticationException();
                    }
                }

                in = conn.getInputStream();

            } catch (final IOException e) {
                if (AuthenticationException.class.isInstance(e.getCause())) {
                    throw e.getCause();
                }
                throw newIOException("Cannot open input stream to server: ", e);
            }

            //Determine the server response protocol for reading
            final ProtocolMetaData protocolResponse = new ProtocolMetaData();
            try {

                protocolResponse.readExternal(in);

            } catch (final EOFException e) {

                throw newIOException("Prematurely reached the end of the stream.  " + protocolResponse.getSpec() + " : " + e.getMessage(), e);

            } catch (final IOException e) {

                throw newIOException("Cannot determine server protocol version: Received " + protocolResponse.getSpec() + " : " + e.getMessage(), e);
            }

            final ObjectInput objectIn;
            try {

                objectIn = new EjbObjectInputStream(in);

            } catch (final IOException e) {
                throw newIOException("Cannot open object input stream to server (" + protocolResponse.getSpec() + ") : " + e.getMessage(), e);
            }

            /*----------------------------------*/
            /* Read cluster response */
            /*----------------------------------*/
            try {
                final ClusterResponse clusterResponse = new ClusterResponse();
                clusterResponse.setMetaData(protocolResponse);
                clusterResponse.readExternal(objectIn);
                switch (clusterResponse.getResponseCode()) {
                    case UPDATE: {
                        setClusterMetaData(server, clusterResponse.getUpdatedMetaData());
                    }
                    break;
                    case FAILURE: {
                        throw clusterResponse.getFailure();
                    }
                }
            } catch (final ClassNotFoundException e) {
                throw new RemoteException("Cannot read the cluster response from the server.  The class for an object being returned is not located in this system:", e);

            } catch (final IOException e) {
                throw newIOException("Cannot read the cluster response from the server (" + protocolResponse.getSpec() + ") : " + e.getMessage(), e);

            } catch (final Throwable e) {
                throw new RemoteException("Error reading cluster response from server (" + protocolResponse.getSpec() + ") : " + e.getMessage(), e);
            }

            /*----------------------------------*/
            /* Read response */
            /*----------------------------------*/
            try {
                res.setMetaData(protocolResponse);
                res.readExternal(objectIn);
            } catch (final ClassNotFoundException e) {
                throw new RemoteException("Cannot read the response from the server.  The class for an object being returned is not located in this system:", e);

            } catch (final IOException e) {
                throw newIOException("Cannot read the response from the server (" + protocolResponse.getSpec() + ") : " + e.getMessage(), e);

            } catch (final Throwable e) {
                throw new RemoteException("Error reading response from server (" + protocolResponse.getSpec() + ") : " + e.getMessage(), e);
            }

            if (retryConditions.size() > 0) {
                if (res instanceof EJBResponse) {
                    final EJBResponse ejbResponse = (EJBResponse) res;
                    if (ejbResponse.getResult() instanceof ThrowableArtifact) {
                        final ThrowableArtifact artifact = (ThrowableArtifact) ejbResponse.getResult();
                        //noinspection ThrowableResultOfMethodCallIgnored
                        if (retryConditions.contains(artifact.getThrowable().getClass())) {

                            throw new RetryException(res);

                            //                            if (? < maxConditionRetry) {
                            //                                throw new RetryException(res);
                            //                            } else {
                            //                                if (FINER) {
                            //                                    logger.log(Level.FINER, "Giving up on " + artifact.getThrowable().getClass().getName().toString());
                            //                                }
                            //                            }
                        }
                    }
                }
            }

            if (FINEST) {
                final long time = System.nanoTime() - start;
                final String message = String.format("Invocation %sns - %s - Request(%s) - Response(%s)", time, conn.getURI(), req, res);
                logger.log(Level.FINEST, message);
            }

        } catch (final RemoteException e) {
            throw e;
        } catch (final IOException e) {
            final URI uri = conn.getURI();
            final Set<URI> failed = getFailed();

            Client.fireEvent(new RequestFailed(uri, req, e));

            if (FINER) {
                logger.log(Level.FINER, "Add Failed " + uri.toString());
            }
            failed.add(uri);
            conn.discard();

            if (e instanceof RetryException || getRetry()) {
                try {

                    Client.fireEvent(new RetryingRequest(req, server));

                    processRequest(req, res, server);
                } catch (final RemoteFailoverException re) {
                    throw re;
                } catch (final RemoteException re) {
                    if (e instanceof RetryException) {
                        return ((RetryException) e).getResponse();
                    }
                    throw new RemoteFailoverException("Cannot complete request.  Retry attempted on " + failed.size() + " servers", e);
                }
            }

        } catch (final Throwable error) {
            throw new RemoteException("Error while communicating with server: ", error);

        } finally {

            if (null != conn) {
                try {
                    conn.close();
                } catch (final Throwable t) {
                    logger.log(Level.WARNING, "Error closing connection with server: " + t.getMessage(), t);
                }
            }

        }
        return res;
    }

    public static Set<URI> getFailed() {
        Set<URI> set = failed.get();
        if (set == null) {
            set = new HashSet<URI>();
            failed.set(set);
        }
        return set;
    }

    private static void setClusterMetaData(final ServerMetaData server, final ClusterMetaData cluster) {
        final Context context = getContext(server);
        context.setClusterMetaData(cluster);
    }

    private static ClusterMetaData getClusterMetaData(final ServerMetaData server) {
        return getContext(server).getClusterMetaData();
    }

    //openejb.client.connection.strategy

    private boolean getRetry() {
        return retry = Boolean.valueOf(System.getProperty("openejb.client.requestretry", retry + ""));
    }

    private static final Map<ServerMetaData, Context> contexts = new ConcurrentHashMap<ServerMetaData, Context>();

    public static Context getContext(final ServerMetaData serverMetaData) {
        Context context = contexts.get(serverMetaData);
        if (context == null) {
            context = new Context(serverMetaData);
            contexts.put(serverMetaData, context);
        }
        return context;
    }

    public static class Context {

        private final Properties properties = new Properties();
        private final ServerMetaData serverMetaData;
        private ClusterMetaData clusterMetaData;
        private Options options;

        private Context(final ServerMetaData serverMetaData) {
            this.serverMetaData = serverMetaData;
            this.clusterMetaData = new ClusterMetaData(0, serverMetaData.getLocation());

            options = new Options(properties, new Options(System.getProperties()));
        }

        public ServerMetaData getServerMetaData() {
            return serverMetaData;
        }

        public ClusterMetaData getClusterMetaData() {
            return clusterMetaData;
        }

        public void setClusterMetaData(final ClusterMetaData updated) {
            if (updated == null) {
                throw new IllegalArgumentException("clusterMetaData cannot be null");
            }

            final ClusterMetaData previous = this.clusterMetaData;
            this.clusterMetaData = updated;

            if (updated.getConnectionStrategy() == null) {
                updated.setConnectionStrategy(previous.getConnectionStrategy());
            }
            updated.setLastLocation(previous.getLastLocation());
            final ClusterMetaDataUpdated clusterMetaDataUpdated = new ClusterMetaDataUpdated(serverMetaData, updated, previous);

            fireEvent(clusterMetaDataUpdated);

            final Set<URI> found = locations(updated);
            final Set<URI> existing = locations(previous);

            for (final URI uri : diff(existing, found)) {
                fireEvent(new ServerAdded(clusterMetaDataUpdated, uri));
            }

            for (final URI uri : diff(found, existing)) {
                fireEvent(new ServerRemoved(clusterMetaDataUpdated, uri));
            }

        }

        private HashSet<URI> locations(final ClusterMetaData updated) {
            return new HashSet<URI>(Arrays.asList(updated.getLocations()));
        }

        public Properties getProperties() {
            return properties;
        }

        public Options getOptions() {
            return options;
        }

        public Set<URI> diff(final Set<URI> a, final Set<URI> b) {
            final Set<URI> diffs = new HashSet<URI>();
            for (final URI uri : b) {
                if (!a.contains(uri)) {
                    diffs.add(uri);
                }
            }

            return diffs;
        }
    }
}
