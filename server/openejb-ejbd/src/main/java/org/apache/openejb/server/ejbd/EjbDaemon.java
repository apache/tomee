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
import org.apache.openejb.client.*;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.DiscoveryAgent;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Properties;

public class EjbDaemon implements org.apache.openejb.spi.ApplicationServer {

    private static final ProtocolMetaData PROTOCOL_VERSION = new ProtocolMetaData("3.1");

    private static final Messages _messages = new Messages("org.apache.openejb.server.util.resources");
    static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE, "org.apache.openejb.server.util.resources");

    private ClientObjectFactory clientObjectFactory;
    //    DeploymentIndex deploymentIndex;
    private EjbRequestHandler ejbHandler;
    private JndiRequestHandler jndiHandler;
    private AuthRequestHandler authHandler;
    private ClusterRequestHandler clusterHandler;

    boolean stop = false;

    static EjbDaemon instance;
    private ContainerSystem containerSystem;

    private EjbDaemon() {
    }

    public static EjbDaemon getEjbDaemon() {
        if (instance == null) {
            instance = new EjbDaemon();
        }
        return instance;
    }

    public void init(Properties props) throws Exception {
        containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
//        deploymentIndex = new DeploymentIndex(containerSystem.deployments());

        clientObjectFactory = new ClientObjectFactory(this, props);

        ejbHandler = new EjbRequestHandler(this);
        jndiHandler = new JndiRequestHandler(this);
        authHandler = new AuthRequestHandler(this);
        clusterHandler = new ClusterRequestHandler(this);

        DiscoveryAgent discovery = SystemInstance.get().getComponent(DiscoveryAgent.class);
        if (discovery != null) {
            discovery.setDiscoveryListener(clusterHandler);
        }
    }

    public void service(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        try {
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
                    logger.error("Error closing client connection: " + t.getMessage());
                }
            }
        }
    }

    public void service(InputStream in, OutputStream out) throws IOException {
        ProtocolMetaData protocolMetaData = new ProtocolMetaData();

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
            logger.error("\"" + protocolMetaData.getSpec() + "\" FAIL \"Unknown request type " + requestTypeByte);
        } catch (SecurityException e) {
            logger.error("\"" + requestType + " " + protocolMetaData.getSpec() + "\" FAIL \"Security error - " + e.getMessage() + "\"", e);
        } catch (Throwable e) {
            logger.error("\"" + requestType + " " + protocolMetaData.getSpec() + "\" FAIL \"Unexpected error - " + e.getMessage() + "\"", e);
        } finally {

            ClientObjectFactory.serverMetaData.remove();

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

    private void processClusterRequest(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        clusterHandler.processRequest(in, out);
    }

    protected BeanContext getDeployment(EJBRequest req) throws RemoteException {
        String deploymentId = req.getDeploymentId();
        BeanContext beanContext = containerSystem.getBeanContext(deploymentId);
        if (beanContext == null) throw new RemoteException("No deployment: " + deploymentId);
        return beanContext;
    }

    public void processEjbRequest(ObjectInputStream in, ObjectOutputStream out) {
        ejbHandler.processRequest(in, out);
    }

    public void processJndiRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception {
        jndiHandler.processRequest(in, out);
    }

    public void processAuthRequest(ObjectInputStream in, ObjectOutputStream out) {
        authHandler.processRequest(in, out);
    }

    public javax.ejb.EJBMetaData getEJBMetaData(ProxyInfo info) {
        return clientObjectFactory.getEJBMetaData(info);
    }

    public javax.ejb.Handle getHandle(ProxyInfo info) {
        return clientObjectFactory.getHandle(info);
    }

    public javax.ejb.HomeHandle getHomeHandle(ProxyInfo info) {
        return clientObjectFactory.getHomeHandle(info);
    }

    public javax.ejb.EJBObject getEJBObject(ProxyInfo info) {
        return clientObjectFactory.getEJBObject(info);
    }

    public Object getBusinessObject(ProxyInfo info) {
        return clientObjectFactory.getBusinessObject(info);
    }

    public javax.ejb.EJBHome getEJBHome(ProxyInfo info) {
        return clientObjectFactory.getEJBHome(info);
    }

}

