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

import org.apache.geronimo.naming.reference.SimpleAwareReference;

/**
 * @version $Revision$ $Date$
 */
public class EJBProxyReference extends SimpleAwareReference {
    public static EJBProxyReference createRemote(String containerId, boolean sessionBean, String homeInterfaceName, String remoteInterfaceName) {
        return new EJBProxyReference(containerId, sessionBean, homeInterfaceName, remoteInterfaceName, null, null, false);
    }

    public static EJBProxyReference createLocal(String containerId, boolean sessionBean, String localHomeInterfaceName, String localInterfaceName) {
        return new EJBProxyReference(containerId, sessionBean, null, null, localHomeInterfaceName, localInterfaceName, true);
    }

    private final String containerId;
    private final boolean isSessionBean;
    private final String remoteInterfaceName;
    private final String homeInterfaceName;
    private final String localInterfaceName;
    private final String localHomeInterfaceName;
    private final boolean isLocal;

    private transient EJBProxyFactory proxyFactory;

    private EJBProxyReference(String containerId, boolean sessionBean, String homeInterfaceName, String remoteInterfaceName, String localHomeInterfaceName, String localInterfaceName, boolean local) {
        this.containerId = containerId;
        isSessionBean = sessionBean;
        this.remoteInterfaceName = remoteInterfaceName;
        this.homeInterfaceName = homeInterfaceName;
        this.localInterfaceName = localInterfaceName;
        this.localHomeInterfaceName = localHomeInterfaceName;
        isLocal = local;
    }

    public Object getContent() {
        EJBProxyFactory proxyFactory = getEJBProxyFactory();
        if (isLocal) {
            return proxyFactory.getEJBLocalHome();
        } else {
            return proxyFactory.getEJBHome();
        }
    }

    private EJBProxyFactory getEJBProxyFactory() {
        if (proxyFactory == null) {
            ClassLoader cl = getClassLoader();
            Class remoteInterface = loadClass(cl, remoteInterfaceName);
            Class homeInterface = loadClass(cl, homeInterfaceName);
            Class localInterface = loadClass(cl, localInterfaceName);
            Class localHomeInterface = loadClass(cl, localHomeInterfaceName);

            proxyFactory = new EJBProxyFactory(containerId,
                    isSessionBean,
                    remoteInterface,
                    homeInterface,
                    localInterface,
                    localHomeInterface);
        }
        return proxyFactory;
    }

    private Class loadClass(ClassLoader cl, String name) {
        if (name == null) {
            return null;
        }
        try {
            return cl.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("ejb" + (isLocal ? "-local" : "") + "-ref class not found: " + name);
        }
    }

    public String getContainerId() {
        return containerId;
    }
}
