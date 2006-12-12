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
package org.apache.openejb.core.entity;

import java.lang.reflect.Method;
import java.util.Vector;

import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.ivm.EjbHomeProxyHandler;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.util.proxy.ProxyManager;


public class EntityEjbHomeHandler extends EjbHomeProxyHandler {

    public EntityEjbHomeHandler(RpcContainer container, Object pk, Object depID, InterfaceType interfaceType) {
        super(container, pk, depID, interfaceType);
    }

    public Object createProxy(ProxyInfo proxyInfo) {
        Object proxy = super.createProxy(proxyInfo);
        EjbObjectProxyHandler handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(proxy);

        /* 
        * Register the handle with the BaseEjbProxyHandler.liveHandleRegistry
        * If the bean is removed by its home or by an identical proxy, then the 
        * this proxy will be automatically invalidated because its properly registered
        * with the liveHandleRegistry.
        */
        registerHandler(handler.getRegistryId(), handler);

        return proxy;

    }

    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        Object retValue = null;
        try {
            retValue = container.invoke(deploymentID, method, args, null, getThreadSpecificSecurityIdentity());
        } catch (OpenEJBException e) {
            e.printStackTrace();
            throw e;
        }

        if (retValue instanceof java.util.Collection) {
            Object [] proxyInfos = ((java.util.Collection) retValue).toArray();
            Vector proxies = new Vector();
            for (int i = 0; i < proxyInfos.length; i++) {
                proxies.addElement(createProxy((ProxyInfo) proxyInfos[i]));
            }
            return proxies;
        } else if (retValue instanceof org.apache.openejb.util.ArrayEnumeration) {
            org.apache.openejb.util.ArrayEnumeration enumeration = (org.apache.openejb.util.ArrayEnumeration) retValue;
            for (int i = enumeration.size() - 1; i >= 0; --i) {
                enumeration.set(i, createProxy((ProxyInfo) enumeration.get(i)));
            }
            return enumeration;
        } else if (retValue instanceof java.util.Enumeration) {
            java.util.Enumeration enumeration = (java.util.Enumeration) retValue;

            java.util.List proxies = new java.util.ArrayList();
            while (enumeration.hasMoreElements()) {
                proxies.add(createProxy((ProxyInfo) enumeration.nextElement()));
            }
            return new org.apache.openejb.util.ArrayEnumeration(proxies);
        } else {
            org.apache.openejb.ProxyInfo proxyInfo = (org.apache.openejb.ProxyInfo) retValue;


            return createProxy(proxyInfo);
        }

    }

    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        Object primKey = args[0];
        container.invoke(deploymentID, method, args, primKey, getThreadSpecificSecurityIdentity());

        /* 
        * This operation takes care of invalidating all the EjbObjectProxyHanders associated with 
        * the same RegistryId. See this.createProxy().
        */
        invalidateAllHandlers(EntityEjbObjectHandler.getRegistryId(primKey, deploymentID, container));
        return null;
    }

    protected EjbObjectProxyHandler newEjbObjectHandler(RpcContainer container, Object pk, Object depID, InterfaceType interfaceType) {
        return new EntityEjbObjectHandler(container, pk, depID, interfaceType);
    }

}
