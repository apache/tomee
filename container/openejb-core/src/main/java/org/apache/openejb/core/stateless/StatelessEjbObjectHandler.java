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
package org.apache.openejb.core.stateless;

import org.apache.openejb.Container;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.util.proxy.ProxyManager;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class StatelessEjbObjectHandler extends EjbObjectProxyHandler {
    public Object registryId;

    public StatelessEjbObjectHandler(DeploymentInfo deploymentInfo, Object pk, InterfaceType interfaceType, List<Class> interfaces) {
        super(deploymentInfo, pk, interfaceType, interfaces);
    }

    public static Object createRegistryId(Object primKey, Object deployId, Container contnr) {
        return "" + deployId + contnr.getContainerID();
    }

    public Object getRegistryId() {
        if (registryId == null)
            registryId = createRegistryId(primaryKey, deploymentID, container);
        return registryId;
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {
        try {
            EjbObjectProxyHandler handler = (EjbObjectProxyHandler) ProxyManager.getInvocationHandler(args[0]);
            return new Boolean(deploymentID.equals(handler.deploymentID));
        } catch (Throwable t) {
            return Boolean.FALSE;

        }
    }

    public void invalidateReference() {
        // stateless can't be removed
    }

    protected Object remove(Method method, Object[] args, Object proxy) throws Throwable {
        // stateless can't be removed
        return null;
    }
}
