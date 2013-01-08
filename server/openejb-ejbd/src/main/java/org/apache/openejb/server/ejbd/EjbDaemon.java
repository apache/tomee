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
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.client.EjbObjectInputStream;
import org.apache.openejb.client.FlushableGZIPOutputStream;
import org.apache.openejb.client.ProtocolMetaData;
import org.apache.openejb.client.RequestType;
import org.apache.openejb.client.ServerMetaData;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.DiscoveryAgent;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

public class EjbDaemon implements org.apache.openejb.spi.ApplicationServer {

    private static final ProtocolMetaData PROTOCOL_VERSION = new ProtocolMetaData("3.1");

    static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE, "org.apache.openejb.server.util.resources");

    private ClientObjectFactory clientObjectFactory;
    //    DeploymentIndex deploymentIndex;
    private EjbRequestHandler ejbHandler;
    private JndiRequestHandler jndiHandler;
    private AuthRequestHandler authHandler;
    private ClusterRequestHandler clusterHandler;

    static EjbDaemon instance;

    private ContainerSystem containerSystem;
    private boolean gzip;

    private EjbDaemon() {
    }

    public static EjbDaemon getEjbDaemon() {
        if (instance == null) {
            instance = new EjbDaemon();
        }
        return instance;
    }

    public void init(final Properties props) throws Exception {
        containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
//        deploymentIndex = new DeploymentIndex(containerSystem.deployments());

        clientObjectFactory = new ClientObjectFactory(this, props);

        ejbHandler = new EjbRequestHandler(this);
        jndiHandler = new JndiRequestHandler(this);
        authHandler = new AuthRequestHandler(this);
        clusterHandler = new ClusterRequestHandler(this);
        gzip = "true".equalsIgnoreCase(props.getProperty("gzip", "false"));

        final DiscoveryAgent discovery = SystemInstance.get().getComponent(DiscoveryAgent.class);
        if (discovery != null) {
            discovery.setDiscoveryListener(clusterHandler);
        }
    }

    public void service(final Socket socket) throws IOException {

        InputStream in = null;
        OutputStream out = null;

        try {

            if (socket.isClosed()) {
                return;
            }

            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
            if (gzip) {
                in = new GZIPInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new BufferedOutputStream(new FlushableGZIPOutputStream(socket.getOutputStream()));
            }

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
        }
    }

    public void service(final InputStream in, final OutputStream out) throws IOException {
        final ProtocolMetaData protocolMetaData = new ProtocolMetaData();

        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        RequestType requestType = null;
        byte requestTypeByte = RequestType.NOP_REQUEST.getCode();

        try {

            // Read Protocol Version
            protocolMetaData.readExternal(in);
            PROTOCOL_VERSION.writeExternal(out);

            ois = new EjbObjectInputStream(in);
            oos = new ObjectOutputStream(out);

            // Read ServerMetaData
            final ServerMetaData serverMetaData = new ServerMetaData();
            serverMetaData.readExternal(ois);
            ClientObjectFactory.serverMetaData.set(serverMetaData);

            // Read request type
            requestTypeByte = (byte) ois.read();
            requestType = RequestType.valueOf(requestTypeByte);

            if (requestType == RequestType.NOP_REQUEST) {
                return;
            }

            if (requestType == RequestType.CLUSTER_REQUEST) {
                processClusterRequest(ois, oos);
            }

            requestTypeByte = (byte) ois.read();
            requestType = RequestType.valueOf(requestTypeByte);

            if (requestType == RequestType.NOP_REQUEST) {
                return;
            }

            // Exceptions should not be thrown from these methods
            // They should handle their own exceptions and clean
            // things up with the client accordingly.
            switch (requestType) {
                case EJB_REQUEST:
                    processEjbRequest(ois, oos);
                    break;
                case JNDI_REQUEST:
                    processJndiRequest(ois, oos);
                    break;
                case AUTH_REQUEST:
                    processAuthRequest(ois, oos);
                    break;
                default:
                    logger.error("\"" + requestType + " " + protocolMetaData.getSpec() + "\" FAIL \"Unknown request type " + requestType);
                    break;
            }
        } catch (IllegalArgumentException iae) {
            final String msg = "\"" + protocolMetaData.getSpec() + "\" FAIL \"Unknown request type " + requestTypeByte;
            if (logger.isDebugEnabled()) {
                logger.debug(msg, iae);
            } else {
                logger.warning(msg + " - Debug for StackTrace");
            }
        } catch (SecurityException e) {
            final String msg = "\"" + requestType + " " + protocolMetaData.getSpec() + "\" FAIL \"Security error - " + e.getMessage() + "\"";
            if (logger.isDebugEnabled()) {
                logger.debug(msg, e);
            } else {
                logger.warning(msg + " - Debug for StackTrace");
            }
        } catch (Throwable e) {
            final String msg = "\"" + requestType + " " + protocolMetaData.getSpec() + "\" FAIL \"Unexpected error - " + e.getMessage() + "\"";
            if (logger.isDebugEnabled()) {
                logger.debug(msg, e);
            } else {
                logger.warning(msg + " - Debug for StackTrace");
            }
        } finally {

            try {
                ClientObjectFactory.serverMetaData.remove();
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
            }
        }
    }

    private void processClusterRequest(final ObjectInputStream in, final ObjectOutputStream out) throws IOException {
        clusterHandler.processRequest(in, out);
    }

    protected BeanContext getDeployment(final EJBRequest req) throws RemoteException {
        final String deploymentId = req.getDeploymentId();
        final BeanContext beanContext = containerSystem.getBeanContext(deploymentId);
        if (beanContext == null) throw new RemoteException("No deployment: " + deploymentId);
        return beanContext;
    }

    public void processEjbRequest(final ObjectInputStream in, final ObjectOutputStream out) {
        ejbHandler.processRequest(in, out);
    }

    public void processJndiRequest(final ObjectInputStream in, final ObjectOutputStream out) throws Exception {
        jndiHandler.processRequest(in, out);
    }

    public void processAuthRequest(final ObjectInputStream in, final ObjectOutputStream out) {
        authHandler.processRequest(in, out);
    }

    @Override
    public javax.ejb.EJBMetaData getEJBMetaData(final ProxyInfo info) {
        return clientObjectFactory.getEJBMetaData(info);
    }

    @Override
    public javax.ejb.Handle getHandle(final ProxyInfo info) {
        return clientObjectFactory.getHandle(info);
    }

    @Override
    public javax.ejb.HomeHandle getHomeHandle(final ProxyInfo info) {
        return clientObjectFactory.getHomeHandle(info);
    }

    @Override
    public javax.ejb.EJBObject getEJBObject(final ProxyInfo info) {
        return clientObjectFactory.getEJBObject(info);
    }

    @Override
    public Object getBusinessObject(final ProxyInfo info) {
        return clientObjectFactory.getBusinessObject(info);
    }

    @Override
    public javax.ejb.EJBHome getEJBHome(final ProxyInfo info) {
        return clientObjectFactory.getEJBHome(info);
    }

    public boolean isGzip() {
        return gzip;
    }
}

