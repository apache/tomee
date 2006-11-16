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
import org.apache.openejb.client.RequestMethods;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

public class EjbDaemon implements org.apache.openejb.spi.ApplicationServer, ResponseCodes, RequestMethods {

    Messages _messages = new Messages("org.apache.openejb.server.util.resources");
    Logger logger = Logger.getInstance("OpenEJB.server.remote", "org.apache.openejb.server.util.resources");

    ClientObjectFactory clientObjectFactory;
    DeploymentIndex deploymentIndex;
    EjbRequestHandler ejbHandler;
    JndiRequestHandler jndiHandler;
    AuthRequestHandler authHandler;

    boolean stop = false;

    static EjbDaemon thiss;

    private EjbDaemon() {
    }

    public static EjbDaemon getEjbDaemon() {
        if (thiss == null) {
            thiss = new EjbDaemon();
        }
        return thiss;
    }

    public void init(Properties props) throws Exception {
        // TODO: DMB: Naughty naugty, static badness
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        deploymentIndex = new DeploymentIndex(containerSystem.deployments());

        clientObjectFactory = new ClientObjectFactory(this);

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
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        try {

            byte requestType = (byte) in.read();

            if (requestType == -1) {
                return;
            }

            ois = new EJBObjectInputStream(in);
            oos = new ObjectOutputStream(out);

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
                    logger.error("Unknown request type " + requestType);
            }
            try {
                if (oos != null) {
                    oos.flush();
                }
            } catch (Throwable t) {
                logger.error("Encountered problem while communicating with client: " + t.getMessage());
            }

        } catch (SecurityException e) {
            logger.error("Security error: " + e.getMessage());
        } catch (Throwable e) {
            logger.error("Unexpected error", e);
        } finally {
            try {
                if (oos != null) {
                    oos.flush();
                }
            } catch (Throwable t) {
                logger.error("Encountered problem while flushing connection with client: " + t.getMessage());
            }
        }
    }

    protected DeploymentInfo getDeployment(EJBRequest req) throws RemoteException {
        return deploymentIndex.getDeployment(req);
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

    public javax.ejb.EJBHome getEJBHome(ProxyInfo info) {
        return clientObjectFactory.getEJBHome(info);
    }

}

