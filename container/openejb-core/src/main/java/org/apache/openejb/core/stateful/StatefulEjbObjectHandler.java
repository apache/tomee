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

package org.apache.openejb.core.stateful;

import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.util.proxy.ProxyManager;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Objects;

public class StatefulEjbObjectHandler extends EjbObjectProxyHandler {

    public StatefulEjbObjectHandler(final BeanContext beanContext, final Object pk, final InterfaceType interfaceType, final List<Class> interfaces, final Class mainInterface) {
        super(beanContext, pk, interfaceType, interfaces, mainInterface);
    }

    public Object getRegistryId() {
        return new RegistryId(container, deploymentID, primaryKey);
    }

    protected Object getPrimaryKey(final Method method, final Object[] args, final Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected Object isIdentical(final Method method, final Object[] args, final Object proxy) throws Throwable {
        checkAuthorization(method);

        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one argument to isIdentical, but received " + args.length);
        }

        final Object that = args[0];
        final Object invocationHandler = ProxyManager.getInvocationHandler(that);

        if (invocationHandler instanceof StatefulEjbObjectHandler) {
            final StatefulEjbObjectHandler handler = (StatefulEjbObjectHandler) invocationHandler;

            /*
            * The registry id is a compound key composed of the bean's primary key, deployment id, and
            * container id.  It uniquely identifies the entity bean that is proxied by the EntityEjbObjectHandler
            * within the IntraVM.
            */
            return this.getRegistryId().equals(handler.getRegistryId());
        }

        return false;
    }

    protected Object remove(final Class interfce, final Method method, final Object[] args, final Object proxy) throws Throwable {
        checkAuthorization(method);
        final Object value = container.invoke(deploymentID, interfaceType, interfce, method, args, primaryKey);

        invalidateAllHandlers(getRegistryId());
        return value;
    }

    public static class RegistryId implements Serializable {
        private static final long serialVersionUID = 5037368364299042022L;

        private final Object containerId;
        private final Object deploymentId;
        private final Object primaryKey;

        public RegistryId(final Container container, final Object deploymentId, final Object primaryKey) {
            if (container == null) {
                throw new NullPointerException("container is null");
            }
            if (deploymentId == null) {
                throw new NullPointerException("deploymentId is null");
            }

            this.containerId = container.getContainerID();
            this.deploymentId = deploymentId;
            this.primaryKey = primaryKey;
        }

        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final RegistryId that = (RegistryId) o;

            return containerId.equals(that.containerId) &&
                deploymentId.equals(that.deploymentId) &&
                    Objects.equals(primaryKey, that.primaryKey);
        }

        public int hashCode() {
            int result;
            result = containerId.hashCode();
            result = 31 * result + deploymentId.hashCode();
            result = 31 * result + (primaryKey != null ? primaryKey.hashCode() : 0);
            return result;
        }


        public String toString() {
            return "[" + containerId + ", " + deploymentId + ", " + primaryKey + "]";
        }

        public Object getPrimaryKey() {
            return primaryKey;
        }
    }
}
