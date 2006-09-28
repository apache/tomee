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
package org.apache.openejb.proxy;

import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.rmi.RemoteException;

import org.apache.openejb.util.ClassLoading;
import org.apache.openejb.EJBComponentType;

/**
 * @version $Revision$ $Date$
 */
public class ProxyMemento implements Serializable {
    private static final int EJB_OBJECT = 0;
    private static final int EJB_HOME = 1;
    private static final int HANDLE = 2;
    private static final int HOME_HANDLE = 3;
    private static final int EJB_META_DATA = 4;

    private final String containerId;
    private final boolean isSessionBean;
    private final String remoteInterfaceName;
    private final String homeInterfaceName;
    private final Object primayKey;
    private final int type;

    public static ProxyMemento createEjbObject(ProxyInfo proxyInfo) {
        return new ProxyMemento(proxyInfo, EJB_OBJECT);
    }

    public static ProxyMemento createEjbHome(ProxyInfo proxyInfo) {
        return new ProxyMemento(proxyInfo, EJB_HOME);
    }

    public static ProxyMemento createHandle(ProxyInfo proxyInfo) {
        return new ProxyMemento(proxyInfo, HANDLE);
    }

    public static ProxyMemento createHomeHanldle(ProxyInfo proxyInfo) {
        return new ProxyMemento(proxyInfo, HOME_HANDLE);
    }

    public static ProxyMemento createEjbMetaData(ProxyInfo proxyInfo) {
        return new ProxyMemento(proxyInfo, EJB_META_DATA);
    }

    private ProxyMemento(ProxyInfo proxyInfo, int type) {
        this.type = type;
        this.containerId = proxyInfo.getContainerID();
        int componentType = proxyInfo.getComponentType();
        isSessionBean = (componentType == EJBComponentType.STATELESS || componentType == EJBComponentType.STATEFUL);
        this.remoteInterfaceName = proxyInfo.getRemoteInterface().getName();
        this.homeInterfaceName = proxyInfo.getHomeInterface().getName();
        this.primayKey = proxyInfo.getPrimaryKey();
    }

    private Object readResolve() throws ObjectStreamException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class remoteInterface = null;
        try {
            remoteInterface = ClassLoading.loadClass(remoteInterfaceName, cl);
        } catch (ClassNotFoundException e) {
            throw new InvalidClassException("Could not load remote interface: " + remoteInterfaceName);
        }
        Class homeInterface = null;
        try {
            homeInterface = ClassLoading.loadClass(homeInterfaceName, cl);
        } catch (ClassNotFoundException e) {
            throw new InvalidClassException("Could not load home interface: " + remoteInterfaceName);
        }

        EJBProxyFactory proxyFactory = new EJBProxyFactory(containerId,
                isSessionBean,
                remoteInterface,
                homeInterface,
                null,
                null);

        switch (type) {
            case EJB_OBJECT:
                return proxyFactory.getEJBObject(primayKey);
            case EJB_HOME:
                return proxyFactory.getEJBHome();
            case HANDLE:
                try {
                    return proxyFactory.getEJBObject(primayKey).getHandle();
                } catch (RemoteException e) {
                    throw (InvalidObjectException) new InvalidObjectException("Error getting handle from ejb object").initCause(e);
                }
            case HOME_HANDLE:
                try {
                    return proxyFactory.getEJBHome().getHomeHandle();
                } catch (RemoteException e) {
                    throw (InvalidObjectException) new InvalidObjectException("Error getting handle from home").initCause(e);
                }
            case EJB_META_DATA:
                try {
                    return proxyFactory.getEJBHome().getEJBMetaData();
                } catch (RemoteException e) {
                    throw (InvalidObjectException) new InvalidObjectException("Error getting ejb meta data from home").initCause(e);
                }
            default:
                throw new InvalidObjectException("Unknown type" + type);
        }
    }
}
