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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.client.RequestMethodConstants;
import org.apache.openejb.client.EjbObjectInputStream;
import org.apache.openejb.client.ProtocolMetaData;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

public class EjbDaemon implements org.apache.openejb.spi.ApplicationServer {

    private static final ProtocolMetaData PROTOCOL_VERSION = new ProtocolMetaData("3.0");

    private static final Messages _messages = new Messages("org.apache.openejb.server.util.resources");
    static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE, "org.apache.openejb.server.util.resources");

    private ClientObjectFactory clientObjectFactory;
//    DeploymentIndex deploymentIndex;
    private EjbRequestHandler ejbHandler;
    private JndiRequestHandler jndiHandler;
    private AuthRequestHandler authHandler;

    boolean stop = false;

    static EjbDaemon thiss;
    private ContainerSystem containerSystem;

    private EjbDaemon() {
    }

    public static EjbDaemon getEjbDaemon() {
        if (thiss == null) {
            thiss = new EjbDaemon();
        }
        return thiss;
    }

    public void init(Properties props) throws Exception {
        containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
//        deploymentIndex = new DeploymentIndex(containerSystem.deployments());

        clientObjectFactory = new ClientObjectFactory(this, props);

        ejbHandler = new EjbRequestHandler(this);
        jndiHandler = new JndiRequestHandler(this);
        authHandler = new AuthRequestHandler(this);
    }

    public void service(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        try {
            service(in, out);
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (Throwable t) {
                logger.error("Encountered problem while closing connection with client: " + t.getMessage());
            }
        }
    }

    public void service(InputStream in, OutputStream out) throws IOException {
        ProtocolMetaData protocolMetaData = new ProtocolMetaData();
        String requestTypeName = null;

        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        try {

            protocolMetaData.readExternal(in);

            PROTOCOL_VERSION.writeExternal(out);

            byte requestType = (byte) in.read();

            if (requestType == -1) {
                return;
            }

            ois = new EjbObjectInputStream(in);
            oos = new ObjectOutputStream(out);

            // Exceptions should not be thrown from these methods
            // They should handle their own exceptions and clean
            // things up with the client accordingly.
            switch (requestType) {
                case RequestMethodConstants.EJB_REQUEST:
                    requestTypeName = "EJB_REQUEST";
                    processEjbRequest(ois, oos);
                    break;
                case RequestMethodConstants.JNDI_REQUEST:
                    requestTypeName = "JNDI_REQUEST";
                    processJndiRequest(ois, oos);
                    break;
                case RequestMethodConstants.AUTH_REQUEST:
                    requestTypeName = "AUTH_REQUEST";
                    processAuthRequest(ois, oos);
                    break;
                default:
                    requestTypeName = requestType+" (UNKNOWN)";
                    logger.error("\"" + requestTypeName + " " + protocolMetaData.getSpec() + "\" FAIL \"Unknown request type " + requestType);

            }
        } catch (SecurityException e) {
            logger.error("\""+requestTypeName +" "+ protocolMetaData.getSpec() + "\" FAIL \"Security error - "+e.getMessage()+"\"",e);
        } catch (Throwable e) {
            logger.error("\""+requestTypeName +" "+ protocolMetaData.getSpec() + "\" FAIL \"Unexpected error - "+e.getMessage()+"\"",e);
        } finally {
            try {
                if (oos != null) {
                    oos.flush();
                    oos.close();
                } else if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (Throwable t) {
                logger.error("\""+requestTypeName +" "+ protocolMetaData.getSpec() + "\" FAIL \""+t.getMessage()+"\"");
            }
        }
    }

    protected DeploymentInfo getDeployment(EJBRequest req) throws RemoteException {
        String deploymentId = req.getDeploymentId();
        DeploymentInfo deploymentInfo = containerSystem.getDeploymentInfo(deploymentId);
        if (deploymentInfo == null) throw new RemoteException("No deployment: "+deploymentId);
        return deploymentInfo;
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

