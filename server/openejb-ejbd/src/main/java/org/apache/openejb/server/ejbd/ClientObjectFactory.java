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

import java.net.URI;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.client.ClientMetaData;
import org.apache.openejb.client.EJBHomeHandle;
import org.apache.openejb.client.EJBHomeHandler;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.EJBObjectHandle;
import org.apache.openejb.client.EJBObjectHandler;
import org.apache.openejb.client.ServerMetaData;

class ClientObjectFactory implements org.apache.openejb.spi.ApplicationServer {
    private final EjbDaemon daemon;

    protected ServerMetaData sMetaData;

    public ClientObjectFactory(EjbDaemon daemon) {

        try {
            this.sMetaData = new ServerMetaData(new URI[]{new URI("foo://"+"127.0.0.1" +":"+4201)});
        } catch (Exception e) {

            e.printStackTrace();
        }
        this.daemon = daemon;
    }

    public javax.ejb.EJBMetaData getEJBMetaData(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getEJBMetaData(call, info);
    }

    public javax.ejb.Handle getHandle(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getHandle(call, info);
    }

    public javax.ejb.HomeHandle getHomeHandle(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getHomeHandle(call, info);
    }

    public javax.ejb.EJBObject getEJBObject(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getEJBObject(call, info);
    }

    public javax.ejb.EJBHome getEJBHome(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        return _getEJBHome(call, info);
    }

    protected javax.ejb.EJBMetaData _getEJBMetaData(CallContext call, ProxyInfo info) {

        DeploymentInfo deployment = info.getDeploymentInfo();
        int idCode = -1;

        EJBMetaDataImpl metaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                deployment.getRemoteInterface(),
                deployment.getPrimaryKeyClass(),
                deployment.getComponentType().toString(),
                deployment.getDeploymentID().toString(),
                idCode, null);
        return metaData;
    }

    protected javax.ejb.Handle _getHandle(CallContext call, ProxyInfo info) {
        DeploymentInfo deployment = info.getDeploymentInfo();

        int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {

        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                deployment.getRemoteInterface(),
                deployment.getPrimaryKeyClass(),
                deployment.getComponentType().toString(),
                deployment.getDeploymentID().toString(),
                idCode, null);
        Object primKey = info.getPrimaryKey();

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData, sMetaData, cMetaData, primKey);

        return new EJBObjectHandle(hanlder.createEJBObjectProxy());
    }

    protected javax.ejb.HomeHandle _getHomeHandle(CallContext call, ProxyInfo info) {
        DeploymentInfo deployment = info.getDeploymentInfo();

        int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                deployment.getRemoteInterface(),
                deployment.getPrimaryKeyClass(),
                deployment.getComponentType().toString(),
                deployment.getDeploymentID().toString(),
                idCode, null);

        EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData, sMetaData, cMetaData);

        return new EJBHomeHandle(hanlder.createEJBHomeProxy());
    }

    protected javax.ejb.EJBObject _getEJBObject(CallContext call, ProxyInfo info) {
        DeploymentInfo deployment = info.getDeploymentInfo();

        int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                deployment.getRemoteInterface(),
                deployment.getPrimaryKeyClass(),
                deployment.getComponentType().toString(),
                deployment.getDeploymentID().toString(),
                idCode, null);
        Object primKey = info.getPrimaryKey();

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData, sMetaData, cMetaData, primKey);

        return (javax.ejb.EJBObject) hanlder.createEJBObjectProxy();
    }

    protected javax.ejb.EJBHome _getEJBHome(CallContext call, ProxyInfo info) {
        DeploymentInfo deployment = info.getDeploymentInfo();

        int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                deployment.getRemoteInterface(),
                deployment.getPrimaryKeyClass(),
                deployment.getComponentType().toString(),
                deployment.getDeploymentID().toString(),
                idCode, null);

        EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData, sMetaData, cMetaData);

        return hanlder.createEJBHomeProxy();
    }
}