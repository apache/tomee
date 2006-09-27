/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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
