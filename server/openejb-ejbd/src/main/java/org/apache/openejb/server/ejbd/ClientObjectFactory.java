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
import java.util.Properties;

import org.apache.openejb.BeanContext;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.client.ClientMetaData;
import org.apache.openejb.client.EJBHomeHandle;
import org.apache.openejb.client.EJBHomeHandler;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.EJBObjectHandle;
import org.apache.openejb.client.EJBObjectHandler;
import org.apache.openejb.client.ServerMetaData;
import org.apache.openejb.client.InterfaceType;

class ClientObjectFactory implements org.apache.openejb.spi.ApplicationServer {

    public static final ThreadLocal<ServerMetaData> serverMetaData = new ThreadLocal<ServerMetaData>();

    protected ServerMetaData defaultServerMetaData;

    public ClientObjectFactory(EjbDaemon daemon, Properties props) {

        try {
            String uriString = props.getProperty("openejb.ejbd.uri", "foo://127.0.0.1:4201");
            this.defaultServerMetaData = new ServerMetaData(new URI(uriString));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public javax.ejb.EJBMetaData getEJBMetaData(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();

        BeanContext beanContext = info.getBeanContext();
        int idCode = -1;

        EJBMetaDataImpl metaData = buildEjbMetaData(info, beanContext, idCode);
        return metaData;
    }

    public javax.ejb.Handle getHandle(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        BeanContext beanContext = info.getBeanContext();

        int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {

        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = buildEjbMetaData(info, beanContext, idCode);
        Object primKey = info.getPrimaryKey();

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData, getServerMetaData(), cMetaData, primKey);

        return new EJBObjectHandle(hanlder.createEJBObjectProxy());
    }

    private ServerMetaData getServerMetaData() {
        ServerMetaData serverMetaData = ClientObjectFactory.serverMetaData.get();
        if (serverMetaData == null){
            serverMetaData = defaultServerMetaData;
        }
        return serverMetaData;
    }

    public javax.ejb.HomeHandle getHomeHandle(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        BeanContext beanContext = info.getBeanContext();

        int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = buildEjbMetaData(info, beanContext, idCode);

        EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData, getServerMetaData(), cMetaData);

        return new EJBHomeHandle(hanlder.createEJBHomeProxy());
    }

    public javax.ejb.EJBObject getEJBObject(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        BeanContext beanContext = info.getBeanContext();

        int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = buildEjbMetaData(info, beanContext, idCode);
        Object primKey = info.getPrimaryKey();

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData, getServerMetaData(), cMetaData, primKey);

        return (javax.ejb.EJBObject) hanlder.createEJBObjectProxy();
    }

    public Object getBusinessObject(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        BeanContext beanContext = info.getBeanContext();

        int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(null, null,
                beanContext.getPrimaryKeyClass(),
                beanContext.getComponentType().toString(),
                beanContext.getDeploymentID().toString(),
                idCode,
                convert(info.getInterfaceType()),
                info.getInterfaces(),
                beanContext.getAsynchronousMethodSignatures());
        eMetaData.loadProperties(beanContext.getProperties());
        
        Object primKey = info.getPrimaryKey();

        EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(eMetaData, getServerMetaData(), cMetaData, primKey);

        return hanlder.createEJBObjectProxy();
    }

    public static InterfaceType convert(org.apache.openejb.InterfaceType type) {
        switch (type) {
            case EJB_HOME: return InterfaceType.EJB_HOME;
            case EJB_OBJECT: return InterfaceType.EJB_OBJECT;
            case EJB_LOCAL_HOME: return InterfaceType.EJB_LOCAL_HOME;
            case EJB_LOCAL: return InterfaceType.EJB_LOCAL;
            case BUSINESS_LOCAL: return InterfaceType.BUSINESS_LOCAL;
            case BUSINESS_LOCAL_HOME: return InterfaceType.BUSINESS_LOCAL_HOME;
            case BUSINESS_REMOTE: return InterfaceType.BUSINESS_REMOTE;
            case BUSINESS_REMOTE_HOME: return InterfaceType.BUSINESS_REMOTE_HOME;
        }
        return null;
    }

    public javax.ejb.EJBHome getEJBHome(ProxyInfo info) {
        CallContext call = CallContext.getCallContext();
        BeanContext beanContext = info.getBeanContext();

        int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        EJBMetaDataImpl eMetaData = buildEjbMetaData(info, beanContext, idCode);

        EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(eMetaData, getServerMetaData(), cMetaData);

        return hanlder.createEJBHomeProxy();
    }

    private EJBMetaDataImpl buildEjbMetaData(ProxyInfo info, BeanContext beanContext, int idCode) {
        EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(beanContext.getHomeInterface(),
                beanContext.getRemoteInterface(),
                beanContext.getPrimaryKeyClass(),
                beanContext.getComponentType().toString(),
                beanContext.getDeploymentID().toString(),
                idCode,
                convert(info.getInterfaceType()),
                info.getInterfaces(),
                beanContext.getAsynchronousMethodSignatures());
        eMetaData.loadProperties(beanContext.getProperties());
        return eMetaData;
    }
}