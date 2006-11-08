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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.ivm.BaseEjbProxyHandler;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.JNDIRequest;
import org.apache.openejb.client.JNDIResponse;
import org.apache.openejb.client.RequestMethods;
import org.apache.openejb.client.ResponseCodes;

class JndiRequestHandler implements ResponseCodes, RequestMethods {
    private final EjbDaemon daemon;

    private Context ejbJndiTree;
    private Context clientJndiTree;

    JndiRequestHandler(EjbDaemon daemon) throws Exception {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        ejbJndiTree = (Context) containerSystem.getJNDIContext().lookup("openejb/ejb");
        try {
            clientJndiTree = (Context) containerSystem.getJNDIContext().lookup("openejb/client");
        } catch (NamingException e) {
        }
        this.daemon = daemon;
    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) throws Exception {
        JNDIRequest req = new JNDIRequest();
        JNDIResponse res = new JNDIResponse();
        req.readExternal(in);

        String name = req.getRequestString();
        if (name.startsWith("/")) name = name.substring(1);

        Object object = null;
        try {
            if (req.getModuleId()!= null && clientJndiTree != null){
                Context moduleContext = (Context) clientJndiTree.lookup(req.getModuleId());
                object = moduleContext.lookup(name);
            } else {
                object = ejbJndiTree.lookup(name);
            }

            if (object instanceof Context) {
                res.setResponseCode(JNDI_CONTEXT);
                res.writeExternal(out);
                return;
            } else if (object == null) {
                throw new NullPointerException("lookup of '"+name+"' returned null");
            }
        } catch (NameNotFoundException e) {
            res.setResponseCode(JNDI_NOT_FOUND);
            res.writeExternal(out);
            return;
        } catch (NamingException e) {
            res.setResponseCode(JNDI_NAMING_EXCEPTION);
            res.setResult(e);
            res.writeExternal(out);
            return;
        }


        BaseEjbProxyHandler handler = null;
        try {
            handler = (BaseEjbProxyHandler) ProxyManager.getInvocationHandler(object);
        } catch (ClassCastException e) {
            res.setResponseCode(JNDI_NAMING_EXCEPTION);
            res.setResult(new NamingException("Expected an ejb proxy, found unknown object: type="+object.getClass().getName() + ", toString="+object));
        }

        ProxyInfo proxyInfo = handler.getProxyInfo();
        DeploymentInfo deployment = proxyInfo.getDeploymentInfo();
        String deploymentID = deployment.getDeploymentID().toString();

        switch(proxyInfo.getInterfaceType()){
            case EJB_HOME: {
                res.setResponseCode(JNDI_EJBHOME);
                EJBMetaDataImpl metaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                        deployment.getRemoteInterface(),
                        deployment.getPrimaryKeyClass(),
                        deployment.getComponentType().toString(),
                        deploymentID,
                        this.daemon.deploymentIndex.getDeploymentIndex(deploymentID));
                res.setResult(metaData);
                break;
            }
            case EJB_LOCAL_HOME: {
                res.setResponseCode(JNDI_NAMING_EXCEPTION);
                res.setResult(new NamingException("Not remotable: '"+name+"'. EJBLocalHome interfaces are not remotable as per the EJB specification."));
                break;
            }
            case BUSINESS_REMOTE: {
                res.setResponseCode(JNDI_BUSINESS_OBJECT);
                EJBMetaDataImpl metaData = new EJBMetaDataImpl(null,
                        deployment.getBusinessRemoteInterface(),
                        deployment.getPrimaryKeyClass(),
                        deployment.getComponentType().toString(),
                        deploymentID,
                        this.daemon.deploymentIndex.getDeploymentIndex(deploymentID));
                Object[] data = {metaData, proxyInfo.getPrimaryKey()};
                res.setResult(data);
                break;
            }
            case BUSINESS_LOCAL: {
                String property = SystemInstance.get().getProperty("openejb.businessLocal", "remotable");
                if (property.equalsIgnoreCase("remotable")) {
                    res.setResponseCode(JNDI_BUSINESS_OBJECT);
                    EJBMetaDataImpl metaData = new EJBMetaDataImpl(null,
                            deployment.getBusinessLocalInterface(),
                            deployment.getPrimaryKeyClass(),
                            deployment.getComponentType().toString(),
                            deploymentID,
                            this.daemon.deploymentIndex.getDeploymentIndex(deploymentID));
                    Object[] data = {metaData, proxyInfo.getPrimaryKey()};
                    res.setResult(data);
                } else {
                    res.setResponseCode(JNDI_NAMING_EXCEPTION);
                    res.setResult(new NamingException("Not remotable: '"+name+"'. Business Local interfaces are not remotable as per the EJB specification.  To disable this restriction, set the system property 'openejb.businessLocal=remotable' in the server."));
                }
                break;
            }
            default: {
                res.setResponseCode(JNDI_NAMING_EXCEPTION);
                res.setResult(new NamingException("Not remotable: '"+name+"'."));
            }
        }

        res.writeExternal(out);
    }
}