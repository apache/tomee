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
package org.apache.openejb.core.stateful;

import org.apache.openejb.InterfaceType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.Container;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.util.proxy.ProxyManager;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.List;
import java.io.Serializable;

public class StatefulEjbObjectHandler extends EjbObjectProxyHandler {

    public StatefulEjbObjectHandler(DeploymentInfo deploymentInfo, Object pk, InterfaceType interfaceType, List<Class> interfaces) {
        super(deploymentInfo, pk, interfaceType, interfaces);
    }

    public Object getRegistryId() {
        return new RegistryId(container, deploymentID, primaryKey);
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new RemoteException("Session objects are private resources and do not have primary keys");
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);

        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one argument to isIdentical, but received " + args.length);
        }

        Object that = args[0];
        Object invocationHandler = ProxyManager.getInvocationHandler(that);

        if (invocationHandler instanceof StatefulEjbObjectHandler) {
            StatefulEjbObjectHandler handler = (StatefulEjbObjectHandler) invocationHandler;

            /*
            * The registry id is a compound key composed of the bean's primary key, deployment id, and
            * container id.  It uniquely identifies the entity bean that is proxied by the EntityEjbObjectHandler
            * within the IntraVM.
            */
            return this.getRegistryId().equals(handler.getRegistryId());
        }

        return false;
    }

    protected Object remove(Class interfce, Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        Object value = container.invoke(deploymentID, interfce, method, args, primaryKey);

        invalidateAllHandlers(getRegistryId());
        return value;
    }

    private static class RegistryId implements Serializable {
        private static final long serialVersionUID = 5037368364299042022L;

        private final Object containerId;
        private final Object deploymentId;
        private final Object primaryKey;

        public RegistryId(Container container, Object deploymentId, Object primaryKey) {
            if (container == null) throw new NullPointerException("container is null");
            if (deploymentId == null) throw new NullPointerException("deploymentId is null");

            this.containerId = container.getContainerID();
            this.deploymentId = deploymentId;
            this.primaryKey = primaryKey;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RegistryId that = (RegistryId) o;

            return containerId.equals(that.containerId) &&
                    deploymentId.equals(that.deploymentId) &&
                    !(primaryKey != null ? !primaryKey.equals(that.primaryKey) : that.primaryKey != null);
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
    }
}
