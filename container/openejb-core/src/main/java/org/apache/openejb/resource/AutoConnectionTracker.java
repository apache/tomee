/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.resource;

import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionReturnAction;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;

import javax.resource.ResourceException;
import javax.resource.spi.DissociatableManagedConnection;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AutoConnectionTracker implements ConnectionTracker {
    private final ConcurrentMap<ManagedConnectionInfo, ProxyPhantomReference> references = new ConcurrentHashMap<ManagedConnectionInfo,ProxyPhantomReference>();
    private final ReferenceQueue referenceQueue = new ReferenceQueue();

    /**
     * Releases any managed connections held by a garbage collected connection proxy.
     * @param connectionInfo the connection to be obtained
     * @param key the unique id of the connection manager
     */
    public void setEnvironment(ConnectionInfo connectionInfo, String key) {
        ProxyPhantomReference reference = (ProxyPhantomReference) referenceQueue.poll();
        while (reference != null) {
            reference.clear();
            references.remove(reference.managedConnectionInfo);

            ConnectionInfo released = new ConnectionInfo(reference.managedConnectionInfo);
            reference.interceptor.returnConnection(released, ConnectionReturnAction.DESTROY);
            reference = (ProxyPhantomReference) referenceQueue.poll();
        }
    }

    /**
     * Proxies new connection handles so we can detect when they have been garbage collected.
     *
     * @param interceptor the interceptor used to release the managed connection when the handled is garbage collected.
     * @param connectionInfo the connection that was obtained
     * @param reassociate should always be false
     */
    public void handleObtained(ConnectionTrackingInterceptor interceptor, ConnectionInfo connectionInfo, boolean reassociate) throws ResourceException {
        if (!reassociate) {
            proxyConnection(interceptor, connectionInfo);
        }
    }

    /**
     * Removes the released collection from the garbage collection reference tracker, since this
     * connection is being release via a normal close method.
     *
     * @param interceptor ignored
     * @param connectionInfo the connection that was released
     * @param action ignored
     */
    public void handleReleased(ConnectionTrackingInterceptor interceptor, ConnectionInfo connectionInfo, ConnectionReturnAction action) {
        PhantomReference phantomReference = references.remove(connectionInfo.getManagedConnectionInfo());
        if (phantomReference != null) {
            phantomReference.clear();
        }
    }

    private void proxyConnection(ConnectionTrackingInterceptor interceptor, ConnectionInfo connectionInfo) throws ResourceException {
        // if this connection already has a proxy no need to create another
        if (connectionInfo.getConnectionProxy() != null) return;

        // DissociatableManagedConnection do not need to be proxied
        if (connectionInfo.getManagedConnectionInfo().getManagedConnection() instanceof DissociatableManagedConnection) {
            return;
        }

        try {
            Object handle = connectionInfo.getConnectionHandle();
            ConnectionInvocationHandler invocationHandler = new ConnectionInvocationHandler(handle);
            Object proxy = Proxy.newProxyInstance(handle.getClass().getClassLoader(), handle.getClass().getInterfaces(), invocationHandler);
            connectionInfo.setConnectionProxy(proxy);
            ProxyPhantomReference reference = new ProxyPhantomReference(interceptor, connectionInfo.getManagedConnectionInfo(), invocationHandler, referenceQueue);
            references.put(connectionInfo.getManagedConnectionInfo(), reference);
        } catch (Throwable e) {
            throw new ResourceException("Unable to construct connection proxy", e);
        }
    }

    public static class ConnectionInvocationHandler implements InvocationHandler {
        private final Object handle;

        public ConnectionInvocationHandler(Object handle) {
            this.handle = handle;
        }

        public Object invoke(Object object, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                if (method.getName().equals("finalize")) {
                    // ignore the handle will get called if it implemented the method
                    return null;
                }
                if (method.getName().equals("clone")) {
                    throw new CloneNotSupportedException();
                }
            }

            try {
                Object value = method.invoke(handle, args);
                return value;
            } catch (InvocationTargetException ite) {
                // catch InvocationTargetExceptions and turn them into the target exception (if there is one)
                Throwable t = ite.getTargetException();
                if (t != null) {
                    throw t;
                }
                throw ite;
            }
        }
    }

    private static class ProxyPhantomReference extends PhantomReference<ConnectionInvocationHandler> {
        private ConnectionTrackingInterceptor interceptor;
        private ManagedConnectionInfo managedConnectionInfo;

        @SuppressWarnings({"unchecked"})
        public ProxyPhantomReference(ConnectionTrackingInterceptor interceptor,
                ManagedConnectionInfo managedConnectionInfo,
                ConnectionInvocationHandler handler,
                ReferenceQueue referenceQueue) {
            super(handler, referenceQueue);
            this.interceptor = interceptor;
            this.managedConnectionInfo = managedConnectionInfo;
        }
    }
}
