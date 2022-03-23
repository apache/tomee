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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.client.ClusterResponse;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.client.EjbObjectInputStream;
import org.apache.openejb.client.FlushableGZIPOutputStream;
import org.apache.openejb.client.ProtocolMetaData;
import org.apache.openejb.client.RequestType;
import org.apache.openejb.client.Response;
import org.apache.openejb.client.ServerMetaData;
import org.apache.openejb.client.serializer.EJBDSerializer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.DiscoveryAgent;
import org.apache.openejb.server.context.RequestInfos;
import org.apache.openejb.server.stream.CountingInputStream;
import org.apache.openejb.server.stream.CountingOutputStream;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Exceptions;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

public class EjbDaemon implements org.apache.openejb.spi.ApplicationServer {

    static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE, "org.apache.openejb.server.util.resources");

    private ClientObjectFactory clientObjectFactory;
    //    DeploymentIndex deploymentIndex;
    private RequestHandler ejbHandler;
    private JndiRequestHandler jndiHandler;
    private RequestHandler authHandler;
    private RequestHandler logoutHandler;
    private ClusterRequestHandler clusterHandler;

    private ContainerSystem containerSystem;
    private boolean gzip;
    private EJBDSerializer serializer = null;

    //Four hours
    private int timeout = 14400000;
    private boolean countStreams;
    private SecurityService securityService;

    public void init(final Properties props) throws Exception {
        containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        //        deploymentIndex = new DeploymentIndex(containerSystem.deployments());

        clientObjectFactory = new ClientObjectFactory(this, props);

        ejbHandler = new EjbRequestHandler(this);
        jndiHandler = new JndiRequestHandler(this);
        authHandler = new AuthRequestHandler(this);
        logoutHandler = new LogoutRequestHandler(this);
        clusterHandler = new ClusterRequestHandler(this);
        gzip = "true".equalsIgnoreCase(props.getProperty("gzip", "false"));

        try {
            this.timeout = Integer.parseInt(props.getProperty("timeout", "14400000"));
        } catch (Exception e) {
            //Ignore
        }

        final String serializer = props.getProperty("serializer", null);
        if (serializer != null) {
            try {
                this.serializer = EJBDSerializer.class.cast(Thread.currentThread().getContextClassLoader().loadClass(serializer).newInstance());
            } catch (final ClassNotFoundException | NoClassDefFoundError cnfe) { // let's try later with app classloader
                this.serializer = new ContextualSerializer(serializer);
            }
        }

        final DiscoveryAgent discovery = SystemInstance.get().getComponent(DiscoveryAgent.class);
        if (discovery != null) {
            discovery.setDiscoveryListener(clusterHandler);
        }

        countStreams = Boolean.parseBoolean(props.getProperty("stream.count", Boolean.toString(jndiHandler.isDebug())));

        securityService = SystemInstance.get().getComponent(SecurityService.class);
    }

    public void service(final Socket socket) throws IOException {

        InputStream in = null;
        OutputStream out = null;

        try {

            if (socket.isClosed()) {
                return;
            }

            socket.setSoTimeout(this.timeout);

            if (gzip) {
                in = new GZIPInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new BufferedOutputStream(new FlushableGZIPOutputStream(socket.getOutputStream()));
            } else {
                in = new BufferedInputStream(socket.getInputStream());
                out = new BufferedOutputStream(socket.getOutputStream());
            }

            RequestInfos.initRequestInfo(socket);

            service(in, out);
        } finally {

            if (null != out) {

                try {
                    out.flush();
                } catch (Throwable e) {
                    //Ignore
                }

                try {
                    out.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }

            if (null != in) {
                try {
                    in.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }

            if (null != socket) {
                try {
                    socket.close();
                } catch (Throwable t) {
                    //Ignore
                }
            }

            RequestInfos.clearRequestInfo();
        }
    }

    public void service(final InputStream rawIn, final OutputStream rawOut) throws IOException {

        final ProtocolMetaData clientProtocol = new ProtocolMetaData();

        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        RequestType requestType = null;
        byte requestTypeByte = RequestType.NOP_REQUEST.getCode();

        try {

            final RequestInfos.RequestInfo info = RequestInfos.info();
            info.setInputStream(countStreams ? new CountingInputStream(rawIn) : rawIn);

            // Read client Protocol Version
            final InputStream cis = info.getInputStream();
            clientProtocol.readExternal(cis);
            ois = new EjbObjectInputStream(cis);

            // Read ServerMetaData
            final ServerMetaData serverMetaData = new ServerMetaData();
            serverMetaData.readExternal(ois);
            ClientObjectFactory.SERVER_META_DATA.set(serverMetaData);

            // Read request type
            requestTypeByte = ois.readByte();
            requestType = RequestType.valueOf(requestTypeByte);

            if (requestType == RequestType.NOP_REQUEST) {
                return;
            }

            ClusterResponse clusterResponse = null;

            if (requestType == RequestType.CLUSTER_REQUEST) {
                clusterResponse = clusterHandler.processRequest(ois, clientProtocol);

                //Check for immediate failure
                final Throwable failure = clusterResponse.getFailure();
                if (null != clusterResponse && null != failure) {

                    clusterHandler.getLogger().debug("Failed to write to ClusterResponse", failure);

                    try {
                        info.setOutputStream(countStreams ? new CountingOutputStream(rawOut) : rawOut);
                        oos = new ObjectOutputStream(info.getOutputStream());
                        clusterResponse.setMetaData(clientProtocol);
                        clusterResponse.writeExternal(oos);
                        oos.flush();
                    } catch (IOException ie) {
                        final String m = "Failed to write to ClusterResponse: " + ie.getMessage();
                        clusterHandler.getLogger().error(m, ie);
                        throw Exceptions.newIOException(m, ie);
                    }

                    throw failure;
                }
            }

            requestTypeByte = ois.readByte();
            requestType = RequestType.valueOf(requestTypeByte);

            if (requestType == RequestType.NOP_REQUEST) {
                return;
            }

            // Exceptions should not be thrown from these methods
            // They should handle their own exceptions and clean
            // things up with the client accordingly.
            final Response response;
            switch (requestType) {
                case EJB_REQUEST:
                    response = processEjbRequest(ois, clientProtocol);
                    break;
                case JNDI_REQUEST:
                    response = processJndiRequest(ois, clientProtocol);
                    break;
                case AUTH_REQUEST:
                    response = processAuthRequest(ois, clientProtocol);
                    break;
                case LOGOUT_REQUEST:
                    response = processLogoutRequest(ois, clientProtocol);
                    break;
                default:
                    LOGGER.error("\"" + requestType + " " + clientProtocol.getSpec() + "\" FAIL \"Unknown request type " + requestType);
                    return;
            }

            try {
                info.setOutputStream(countStreams ? new CountingOutputStream(rawOut) : rawOut);

                final OutputStream cos = info.getOutputStream();

                //Let client know we are using the requested protocol to respond
                clientProtocol.writeExternal(cos);
                cos.flush();

                oos = new ObjectOutputStream(cos);
                clusterHandler.processResponse(clusterResponse, oos, clientProtocol);
                oos.flush();

            } finally {
                switch (requestType) {
                    case EJB_REQUEST:
                        processEjbResponse(response, oos, clientProtocol);
                        break;
                    case JNDI_REQUEST:
                        processJndiResponse(response, oos, clientProtocol);
                        break;
                    case AUTH_REQUEST:
                        processAuthResponse(response, oos, clientProtocol);
                        break;
                    case LOGOUT_REQUEST:
                        processLogoutResponse(response, oos, clientProtocol);
                        break;
                    default:
                        //Should never get here...
                        LOGGER.error("\"" + requestType + " " + clientProtocol.getSpec() + "\" FAIL \"Unknown response type " + requestType);
                }
            }

        } catch (IllegalArgumentException iae) {
            final String msg = "\"" + clientProtocol.getSpec() + "\" FAIL \"Unknown request type " + requestTypeByte;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(msg, iae);
            } else {
                LOGGER.warning(msg + " - Debug for StackTrace");
            }
        } catch (SecurityException e) {
            final String msg = "\"" + requestType + " " + clientProtocol.getSpec() + "\" FAIL \"Security error - " + e.getMessage() + "\"";
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(msg, e);
            } else {
                LOGGER.warning(msg + " - Debug for StackTrace");
            }
        } catch (Throwable e) {
            final String msg = "\"" + requestType + " " + clientProtocol.getSpec() + "\" FAIL \"Unexpected error - " + e.getMessage() + "\"";
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(msg, e);
            } else {
                LOGGER.warning(msg + " - Debug for StackTrace");
            }
        } finally {
            try {
                ClientObjectFactory.SERVER_META_DATA.remove();
            } finally {
                if (null != oos) {

                    try {
                        oos.flush();
                    } catch (Throwable e) {
                        //Ignore
                    }

                    try {
                        oos.close();
                    } catch (Throwable e) {
                        //Ignore
                    }
                }

                if (null != ois) {
                    try {
                        ois.close();
                    } catch (Throwable e) {
                        //Ignore
                    }
                }

                // enforced in case of exception
                securityService.disassociate();
            }
        }
    }

    protected BeanContext getDeployment(final EJBRequest req) throws RemoteException {
        final String deploymentId = req.getDeploymentId();
        final BeanContext beanContext = containerSystem.getBeanContext(deploymentId);
        if (beanContext == null) {
            throw new RemoteException("No deployment: " + deploymentId);
        }
        return beanContext;
    }

    public Response processEjbRequest(final ObjectInputStream in, final ProtocolMetaData metaData) throws Exception {
        return ejbHandler.processRequest(in, metaData);
    }

    public Response processJndiRequest(final ObjectInputStream in, final ProtocolMetaData metaData) throws Exception {
        return jndiHandler.processRequest(in, metaData);
    }

    public Response processAuthRequest(final ObjectInputStream in, final ProtocolMetaData metaData) throws Exception {
        return authHandler.processRequest(in, metaData);
    }

    public Response processLogoutRequest(final ObjectInputStream in, final ProtocolMetaData metaData) throws Exception {
        return logoutHandler.processRequest(in, metaData);
    }

    public void processEjbResponse(final Response response, final ObjectOutputStream out, final ProtocolMetaData metaData) throws Exception {
        ejbHandler.processResponse(response, out, metaData);
    }

    public void processJndiResponse(final Response response, final ObjectOutputStream out, final ProtocolMetaData metaData) throws Exception {
        jndiHandler.processResponse(response, out, metaData);
    }

    public void processAuthResponse(final Response response, final ObjectOutputStream out, final ProtocolMetaData metaData) throws Exception {
        authHandler.processResponse(response, out, metaData);
    }

    public void processLogoutResponse(final Response response, final ObjectOutputStream out, final ProtocolMetaData metaData) throws Exception {
        logoutHandler.processResponse(response, out, metaData);
    }

    @Override
    public jakarta.ejb.EJBMetaData getEJBMetaData(final ProxyInfo info) {
        return clientObjectFactory.getEJBMetaData(info);
    }

    @Override
    public jakarta.ejb.Handle getHandle(final ProxyInfo info) {
        return clientObjectFactory.getHandle(info);
    }

    @Override
    public jakarta.ejb.HomeHandle getHomeHandle(final ProxyInfo info) {
        return clientObjectFactory.getHomeHandle(info);
    }

    @Override
    public jakarta.ejb.EJBObject getEJBObject(final ProxyInfo info) {
        return clientObjectFactory.getEJBObject(info);
    }

    @Override
    public Object getBusinessObject(final ProxyInfo info) {
        return clientObjectFactory.getBusinessObject(info);
    }

    @Override
    public jakarta.ejb.EJBHome getEJBHome(final ProxyInfo info) {
        return clientObjectFactory.getEJBHome(info);
    }

    public boolean isGzip() {
        return gzip;
    }

    public EJBDSerializer getSerializer() {
        return serializer;
    }

    private static class ContextualSerializer implements EJBDSerializer {

        private final String classname;

        public ContextualSerializer(final String serializer) {
            this.classname = serializer;
        }

        @Override
        public Serializable serialize(final Object o) {
            return instance().serialize(o);
        }

        @Override
        public Object deserialize(final Serializable o, final Class<?> clazz) {
            return instance().deserialize(o, clazz);
        }

        private EJBDSerializer instance() {
            try {
                return EJBDSerializer.class.cast(Thread.currentThread().getContextClassLoader().loadClass(classname).newInstance());
            } catch (final Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
    }
}

