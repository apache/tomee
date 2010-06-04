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

import static org.apache.openejb.client.Exceptions.newIOException;

import javax.ejb.ConcurrentAccessTimeoutException;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URI;

public class Client {
    private static final Logger logger = Logger.getLogger("OpenEJB.client");

    public static final ThreadLocal<Set<URI>> failed = new ThreadLocal<Set<URI>>();

    private static final ProtocolMetaData PROTOCOL_VERSION = new ProtocolMetaData("3.1");

    private List<Class<? extends Throwable>> retryConditions = new CopyOnWriteArrayList();
    private static Client client = new Client();
    private boolean retry = false;

    public Client() {
        String retryValue = System.getProperty("openejb.client.requestretry", getRetry() + "");
        retry = new Boolean(retryValue);
    }

    public static boolean addRetryCondition(Class<? extends Throwable> throwable) {
        return client.retryConditions.add(throwable);
    }

    public static boolean removeRetryCondition(Class<? extends Throwable> throwable) {
        return client.retryConditions.remove(throwable);
    }

    // This lame hook point if only of testing
    public static void setClient(Client client) {
        Client.client = client;
    }

    public static Response request(Request req, Response res, ServerMetaData server) throws RemoteException {
        try {
            return client.processRequest(req, res, server);
        } finally {
            failed.remove();
        }
    }

    protected Response processRequest(Request req, Response res, ServerMetaData server) throws RemoteException {
//        System.out.println("req = " + req);
        if (server == null)
            throw new IllegalArgumentException("Server instance cannot be null");

        ClusterMetaData cluster = getClusterMetaData(server);

        /*----------------------------*/
        /* Get a connection to server */
        /*----------------------------*/

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection(cluster, server, req);
        } catch (IOException e) {
            throw new RemoteException("Unable to connect",e);
        }

        try {


            /*----------------------------------*/
            /* Get output streams */
            /*----------------------------------*/
            OutputStream out;
            try {

                out = conn.getOuputStream();

            } catch (IOException e) {
                throw newIOException("Cannot open output stream to server: ", e);
            }

            /*----------------------------------*/
            /* Write the protocol magic         */
            /*----------------------------------*/
            try {

                PROTOCOL_VERSION.writeExternal(out);

            } catch (IOException e) {
                throw newIOException("Cannot write the protocol metadata to the server: ", e);
            }

            /*----------------------------------*/
            /* Get output streams */
            /*----------------------------------*/
            ObjectOutput objectOut;
            try {

                objectOut = new ObjectOutputStream(out);

            } catch (IOException e) {
                throw newIOException("Cannot open object output stream to server: ", e);
            }

            /*----------------------------------*/
            /* Write ServerMetaData */
            /*----------------------------------*/
            try {

                server.writeExternal(objectOut);

            } catch (IOException e) {
                throw newIOException("Cannot write the ServerMetaData to the server: ", e);
            }

            /*----------------------------------*/
            /* Write ClusterMetaData */
            /*----------------------------------*/
            try {

                ClusterRequest clusterRequest = new ClusterRequest(cluster);
                objectOut.write(clusterRequest.getRequestType());
                clusterRequest.writeExternal(objectOut);

            } catch (Throwable e) {
                throw newIOException("Cannot write the ClusterMetaData to the server: ", e);
            }

            /*----------------------------------*/
            /* Write request type */
            /*----------------------------------*/
            try {

                objectOut.write(req.getRequestType());

            } catch (IOException e) {
                throw newIOException("Cannot write the request type to the server: ", e);
            }

            /*----------------------------------*/
            /* Write request */
            /*----------------------------------*/
            try {

                req.writeExternal(objectOut);
                objectOut.flush();
                out.flush();

            } catch (java.io.NotSerializableException e) {

                throw new IllegalArgumentException("Object is not serializable: " + e.getMessage());

            } catch (IOException e) {

                throw newIOException("Cannot write the request to the server: ", e);
            }

            /*----------------------------------*/
            /* Get input streams               */
            /*----------------------------------*/
            InputStream in;
            try {

                in = conn.getInputStream();


            } catch (IOException e) {
                throw newIOException("Cannot open input stream to server: ", e);
            }

            ProtocolMetaData protocolMetaData = null;
            try {

                protocolMetaData = new ProtocolMetaData();
                protocolMetaData.readExternal(in);

            } catch (EOFException e) {

                throw newIOException("Prematurely reached the end of the stream.  " + protocolMetaData.getSpec(), e);

            } catch (IOException e) {

                throw newIOException("Cannot deternmine server protocol version: Received " + protocolMetaData.getSpec(), e);
            }

            ObjectInput objectIn;
            try {

                objectIn = new EjbObjectInputStream(in);

            } catch (IOException e) {
                throw newIOException("Cannot open object input stream to server (" + protocolMetaData.getSpec() + ") : " + e.getMessage(), e);
            }

            /*----------------------------------*/
            /* Read response */
            /*----------------------------------*/
            try {
                ClusterResponse clusterResponse = new ClusterResponse();
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
            } catch (ClassNotFoundException e) {
                throw new RemoteException("Cannot read the response from the server.  The class for an object being returned is not located in this system:", e);

            } catch (IOException e) {
                throw newIOException("Cannot read the response from the server (" + protocolMetaData.getSpec() + ") : " + e.getMessage(), e);

            } catch (Throwable e) {
                throw new RemoteException("Error reading response from server (" + protocolMetaData.getSpec() + ") : " + e.getMessage(), e);
            }

            /*----------------------------------*/
            /* Read response */
            /*----------------------------------*/
            try {

                res.readExternal(objectIn);
            } catch (ClassNotFoundException e) {
                throw new RemoteException("Cannot read the response from the server.  The class for an object being returned is not located in this system:", e);

            } catch (IOException e) {
                throw newIOException("Cannot read the response from the server (" + protocolMetaData.getSpec() + ") : " + e.getMessage(), e);

            } catch (Throwable e) {
                throw new RemoteException("Error reading response from server (" + protocolMetaData.getSpec() + ") : " + e.getMessage(), e);
            }

            if (retryConditions.size() > 0) {
                if (res instanceof EJBResponse) {
                    EJBResponse ejbResponse = (EJBResponse) res;
                    if (ejbResponse.getResult() instanceof ThrowableArtifact) {
                        ThrowableArtifact artifact = (ThrowableArtifact) ejbResponse.getResult();
                        if (retryConditions.contains(artifact.getThrowable().getClass())) {
                            throw new RetryException(res);
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            throw e;
        } catch (IOException e){
            Set<URI> failed = getFailed();
            failed.add(conn.getURI());
            conn.discard();
            if (e instanceof RetryException || getRetry()){
                try {
                    processRequest(req, res, server);
                } catch (RemoteFailoverException re) {
                    throw re;
                } catch (RemoteException re) {
                    if (e instanceof RetryException) {
                        return ((RetryException) e).getResponse();
                    }
                    throw new RemoteFailoverException("Cannot complete request.  Retry attempted on " + failed.size() + " servers", e);
                }
            }
        } catch (Throwable error) {
            throw new RemoteException("Error while communicating with server: ", error);

        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Error closing connection with server: " + t.getMessage(), t);
            }
        }
        return res;
    }

    public static Set<URI> getFailed() {
        Set<URI> set = failed.get();
        if (set == null){
            set = new HashSet<URI>();
            failed.set(set);
        }
        return set;
    }

    private static final Map<ServerMetaData, ClusterMetaData> clusters = new ConcurrentHashMap<ServerMetaData, ClusterMetaData>();

    private static void setClusterMetaData(ServerMetaData server, ClusterMetaData cluster) {
        clusters.put(server, cluster);
    }

    private static ClusterMetaData getClusterMetaData(ServerMetaData server) {
        ClusterMetaData cluster = clusters.get(server);
        if (cluster == null) {
            cluster = new ClusterMetaData(0, server.getLocation());
            clusters.put(server, cluster);
        }

        return cluster;
    }

    private boolean getRetry() {
        return retry = new Boolean(System.getProperty("openejb.client.requestretry", retry + ""));
    }
}
