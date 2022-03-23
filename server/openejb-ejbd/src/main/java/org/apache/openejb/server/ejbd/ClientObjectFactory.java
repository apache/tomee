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
import org.apache.openejb.client.ClientMetaData;
import org.apache.openejb.client.EJBHomeHandle;
import org.apache.openejb.client.EJBHomeHandler;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.EJBObjectHandle;
import org.apache.openejb.client.EJBObjectHandler;
import org.apache.openejb.client.InterfaceType;
import org.apache.openejb.client.JNDIContext;
import org.apache.openejb.client.ServerMetaData;

import java.net.URI;
import java.util.Properties;

class ClientObjectFactory implements org.apache.openejb.spi.ApplicationServer {

    public static final ThreadLocal<ServerMetaData> SERVER_META_DATA = new ThreadLocal<ServerMetaData>();

    protected ServerMetaData defaultServerMetaData;

    public ClientObjectFactory(final EjbDaemon daemon, final Properties props) {

        String uriString = "foo://127.0.0.1:4201";
        try {
            uriString = (props.getProperty("openejb.ejbd.uri", uriString));
            this.defaultServerMetaData = new ServerMetaData(new URI(uriString));
        } catch (Exception e) {
            EjbDaemon.LOGGER.error("Failed to read 'openejb.ejbd.uri': " + uriString, e);
        }
    }

    @Override
    public jakarta.ejb.EJBMetaData getEJBMetaData(final ProxyInfo info) {
        final CallContext call = CallContext.getCallContext();

        final BeanContext beanContext = info.getBeanContext();
        final int idCode = -1;

        return buildEjbMetaData(info, beanContext, idCode);
    }

    @Override
    public jakarta.ejb.Handle getHandle(final ProxyInfo info) {
        final CallContext call = CallContext.getCallContext();
        final BeanContext beanContext = info.getBeanContext();

        final int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            //Ignore
        }
        final ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        final EJBMetaDataImpl eMetaData = buildEjbMetaData(info, beanContext, idCode);
        final Object primKey = info.getPrimaryKey();

        final EJBObjectHandler handler = EJBObjectHandler.createEJBObjectHandler(JNDIContext.globalExecutor(), eMetaData, getServerMetaData(), cMetaData, primKey, null);

        return new EJBObjectHandle(handler.createEJBObjectProxy());
    }

    private ServerMetaData getServerMetaData() {
        ServerMetaData serverMetaData = ClientObjectFactory.SERVER_META_DATA.get();
        if (serverMetaData == null) {
            serverMetaData = defaultServerMetaData;
        }
        return serverMetaData;
    }

    @Override
    public jakarta.ejb.HomeHandle getHomeHandle(final ProxyInfo info) {
        final CallContext call = CallContext.getCallContext();
        final BeanContext beanContext = info.getBeanContext();

        final int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        final EJBMetaDataImpl eMetaData = buildEjbMetaData(info, beanContext, idCode);

        final EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(JNDIContext.globalExecutor(), eMetaData, getServerMetaData(), cMetaData, call.get(JNDIContext.AuthenticationInfo.class));

        return new EJBHomeHandle(hanlder.createEJBHomeProxy());
    }

    @Override
    public jakarta.ejb.EJBObject getEJBObject(final ProxyInfo info) {
        final CallContext call = CallContext.getCallContext();
        final BeanContext beanContext = info.getBeanContext();

        final int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        final EJBMetaDataImpl eMetaData = buildEjbMetaData(info, beanContext, idCode);
        final Object primKey = info.getPrimaryKey();

        final EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(JNDIContext.globalExecutor(), eMetaData, getServerMetaData(), cMetaData, primKey, null);

        return (jakarta.ejb.EJBObject) hanlder.createEJBObjectProxy();
    }

    @Override
    public Object getBusinessObject(final ProxyInfo info) {
        final CallContext call = CallContext.getCallContext();
        final BeanContext beanContext = info.getBeanContext();

        final int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        final EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(null, null,
            beanContext.getPrimaryKeyClass(),
            beanContext.getComponentType().toString(),
            beanContext.getDeploymentID().toString(),
            idCode,
            convert(info.getInterfaceType()),
            info.getInterfaces(),
            beanContext.getAsynchronousMethodSignatures());
        eMetaData.loadProperties(beanContext.getProperties());

        final Object primKey = info.getPrimaryKey();

        final EJBObjectHandler hanlder = EJBObjectHandler.createEJBObjectHandler(JNDIContext.globalExecutor(), eMetaData, getServerMetaData(), cMetaData, primKey, null);

        return hanlder.createEJBObjectProxy();
    }

    public static InterfaceType convert(final org.apache.openejb.InterfaceType type) {
        switch (type) {
            case EJB_HOME:
                return InterfaceType.EJB_HOME;
            case EJB_OBJECT:
                return InterfaceType.EJB_OBJECT;
            case EJB_LOCAL_HOME:
                return InterfaceType.EJB_LOCAL_HOME;
            case EJB_LOCAL:
                return InterfaceType.EJB_LOCAL;
            case BUSINESS_LOCAL:
                return InterfaceType.BUSINESS_LOCAL;
            case BUSINESS_LOCAL_HOME:
                return InterfaceType.BUSINESS_LOCAL_HOME;
            case BUSINESS_REMOTE:
                return InterfaceType.BUSINESS_REMOTE;
            case BUSINESS_REMOTE_HOME:
                return InterfaceType.BUSINESS_REMOTE_HOME;
        }
        return null;
    }

    @Override
    public jakarta.ejb.EJBHome getEJBHome(final ProxyInfo info) {
        final CallContext call = CallContext.getCallContext();
        final BeanContext beanContext = info.getBeanContext();

        final int idCode = -1;

        Object securityIdentity = null;
        try {
            securityIdentity = call.getEJBRequest().getClientIdentity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final ClientMetaData cMetaData = new ClientMetaData(securityIdentity);
        final EJBMetaDataImpl eMetaData = buildEjbMetaData(info, beanContext, idCode);

        final EJBHomeHandler hanlder = EJBHomeHandler.createEJBHomeHandler(JNDIContext.globalExecutor(), eMetaData, getServerMetaData(), cMetaData, null);

        return hanlder.createEJBHomeProxy();
    }

    private EJBMetaDataImpl buildEjbMetaData(final ProxyInfo info, final BeanContext beanContext, final int idCode) {
        final EJBMetaDataImpl eMetaData = new EJBMetaDataImpl(beanContext.getHomeInterface(),
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