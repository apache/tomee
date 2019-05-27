/*
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

package org.apache.openejb.core.singleton;

import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.util.proxy.ProxyManager;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.List;

public class SingletonEjbObjectHandler extends EjbObjectProxyHandler {
    public Object registryId;

    public SingletonEjbObjectHandler(final BeanContext beanContext, final Object pk, final InterfaceType interfaceType, final List<Class> interfaces, final Class mainInterface) {
        super(beanContext, pk, interfaceType, interfaces, mainInterface);
    }

    public static Object createRegistryId(final Object primKey, final Object deployId, final Container contnr) {
        return String.valueOf(deployId) + contnr.getContainerID();
    }

    public Object getRegistryId() {
        if (registryId == null) {
            registryId = createRegistryId(primaryKey, deploymentID, container);
        }
        return registryId;
    }

    protected Object getPrimaryKey(final Method method, final Object[] args, final Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected Object isIdentical(final Method method, final Object[] args, final Object proxy) throws Throwable {
        try {
            final EjbObjectProxyHandler handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(args[0]);
            return deploymentID.equals(handler.deploymentID);
        } catch (final Throwable t) {
            return Boolean.FALSE;
        }
    }

    public void invalidateReference() {
        // stateless can't be removed
    }

    protected Object remove(final Class interfce, final Method method, final Object[] args, final Object proxy) throws Throwable {
        // stateless can't be removed
        return null;
    }
}
