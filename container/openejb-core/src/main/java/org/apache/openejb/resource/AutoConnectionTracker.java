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

package org.apache.openejb.resource;

import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionReturnAction;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.transaction.manager.TransactionImpl;
import org.apache.openejb.dyni.DynamicSubclass;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.proxy.LocalBeanProxyFactory;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.DissociatableManagedConnection;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;

public class AutoConnectionTracker implements ConnectionTracker {

    private static final String KEY = "AutoConnectionTracker_Connections";
    private final TransactionSynchronizationRegistry registry;
    private final TransactionManager txMgr;
    private final Logger logger = Logger.getInstance(LogCategory.OPENEJB_CONNECTOR, "org.apache.openejb.resource");
    private final ConcurrentMap<ManagedConnectionInfo, ProxyPhantomReference> references = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private final ReferenceQueue referenceQueue = new ReferenceQueue();
    private final ConcurrentMap<Class<?>, Class<?>> proxies = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Class<?>[]> interfaces = new ConcurrentHashMap<>();

    private final boolean cleanupLeakedConnections;

    public AutoConnectionTracker(final boolean cleanupLeakedConnections) {
        this.cleanupLeakedConnections = cleanupLeakedConnections;
        registry = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);
        txMgr = SystemInstance.get().getComponent(TransactionManager.class);
    }

    public Set<ManagedConnectionInfo> connections() {
        return references.keySet();
    }

    /**
     * Releases any managed connections held by a garbage collected connection proxy.
     *
     * @param connectionInfo the connection to be obtained
     * @param key            the unique id of the connection manager
     */
    @Override
    public void setEnvironment(final ConnectionInfo connectionInfo, final String key) {
        ProxyPhantomReference reference = (ProxyPhantomReference) referenceQueue.poll();
        while (reference != null) {
            reference.clear();
            references.remove(reference.managedConnectionInfo);

            if (cleanupLeakedConnections) {
                final ConnectionInfo released = new ConnectionInfo(reference.managedConnectionInfo);
                reference.interceptor.returnConnection(released, ConnectionReturnAction.DESTROY);
            }

            logger.warning("Detected abandoned connection " + reference.managedConnectionInfo + " opened at " + stackTraceToString(reference.stackTrace));
            reference = (ProxyPhantomReference) referenceQueue.poll();
        }
    }

    /**
     * Proxies new connection handles so we can detect when they have been garbage collected.
     *
     * @param interceptor    the interceptor used to release the managed connection when the handled is garbage collected.
     * @param connectionInfo the connection that was obtained
     * @param reassociate    should always be false
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handleObtained(final ConnectionTrackingInterceptor interceptor, final ConnectionInfo connectionInfo, final boolean reassociate) throws ResourceException {
        if (txMgr != null && registry != null) {
            try {
                final TransactionImpl currentTx = (TransactionImpl) txMgr.getTransaction();
                if (currentTx != null) {
                    Map<ManagedConnectionInfo, Map<ConnectionInfo, Object>> txConnections = (Map<ManagedConnectionInfo, Map<ConnectionInfo, Object>>) registry
                            .getResource(KEY);
                    if (txConnections == null) {
                        txConnections = new HashMap<ManagedConnectionInfo, Map<ConnectionInfo, Object>>();
                        registry.putResource(KEY, txConnections);
                    }

                    Map<ConnectionInfo, Object> connectionObjects = txConnections.get(connectionInfo.getManagedConnectionInfo());
                    if (connectionObjects == null) {
                        connectionObjects = new HashMap<ConnectionInfo, Object>();
                        txConnections.put(connectionInfo.getManagedConnectionInfo(), connectionObjects);
                    }

                    connectionObjects.put(connectionInfo, connectionInfo.getConnectionProxy());

                    registry.registerInterposedSynchronization(new Synchronization() {
                        @Override
                        public void beforeCompletion() {
                        }

                        @Override
                        public void afterCompletion(final int status) {
                            final Map<ManagedConnectionInfo, Map<ConnectionInfo, Object>> txConnections = (Map<ManagedConnectionInfo, Map<ConnectionInfo, Object>>) currentTx
                                    .getResource(KEY);
                            if (txConnections != null && txConnections.size() > 0) {
                                for (final ManagedConnectionInfo managedConnectionInfo : txConnections.keySet()) {
                                    final StringBuilder sb = new StringBuilder();
                                    final Collection<ConnectionInfo> connectionInfos = txConnections.get(managedConnectionInfo)
                                            .keySet();
                                    for (final ConnectionInfo connectionInfo : connectionInfos) {
                                        sb.append("\n  ").append("Connection handle opened at ")
                                                .append(stackTraceToString(connectionInfo.getTrace().getStackTrace()));
                                    }
                                    logger.warning("Transaction complete, but connection still has handles associated: "
                                            + managedConnectionInfo + "\nAbandoned connection information: " + sb.toString());
                                }
                            }
                        }
                    });
                }
            } catch (SystemException | ClassCastException e) {
                // ignore
            }
        }

        if (!reassociate) {
            proxyConnection(interceptor, connectionInfo);
        }
    }

    /**
     * Removes the released collection from the garbage collection reference tracker, since this
     * connection is being release via a normal close method.
     *
     * @param interceptor    ignored
     * @param connectionInfo the connection that was released
     * @param action         ignored
     */
    @Override
    @SuppressWarnings("unchecked")
    public void handleReleased(final ConnectionTrackingInterceptor interceptor, final ConnectionInfo connectionInfo, final ConnectionReturnAction action) {
        TransactionImpl currentTx = null;
        try {
            currentTx = (TransactionImpl) txMgr.getTransaction();
        } catch (SystemException | ClassCastException e) {
            //ignore
        }

        if (currentTx != null) {
            Map<ManagedConnectionInfo, Map<ConnectionInfo, Object>> txConnections = (Map<ManagedConnectionInfo, Map<ConnectionInfo, Object>>) currentTx.getResource(KEY);
            if (txConnections == null) {
                txConnections = new HashMap<>();
                registry.putResource(KEY, txConnections);
            }

            Map<ConnectionInfo, Object> connectionObjects = txConnections.computeIfAbsent(connectionInfo.getManagedConnectionInfo(), k -> new HashMap<>());

            connectionObjects.remove(connectionInfo);
            if (connectionObjects.size() == 0) {
                txConnections.remove(connectionInfo.getManagedConnectionInfo());
            }
        }

        @SuppressWarnings("rawtypes")
        final PhantomReference phantomReference = references.remove(connectionInfo.getManagedConnectionInfo());
        if (phantomReference != null) {
            phantomReference.clear();
        }
    }

    private void proxyConnection(final ConnectionTrackingInterceptor interceptor, final ConnectionInfo connectionInfo) throws ResourceException {
        // no-op if we have opted to not use proxies
        if (connectionInfo.getConnectionProxy() != null) {
            return;
        }

        // DissociatableManagedConnection do not need to be proxied
        if (connectionInfo.getManagedConnectionInfo().getManagedConnection() instanceof DissociatableManagedConnection) {
            return;
        }

        try {
            final Object handle = connectionInfo.getConnectionHandle();
            final ConnectionInvocationHandler invocationHandler = new ConnectionInvocationHandler(handle);
            final Object proxy = newProxy(handle, invocationHandler);
            connectionInfo.setConnectionProxy(proxy);
            final ProxyPhantomReference reference = new ProxyPhantomReference(interceptor, connectionInfo.getManagedConnectionInfo(), invocationHandler, referenceQueue);
            references.put(connectionInfo.getManagedConnectionInfo(), reference);
        } catch (final Throwable e) {
            throw new ResourceException("Unable to construct connection proxy", e);
        }
    }

    private Object newProxy(final Object handle, final InvocationHandler invocationHandler) {
        ClassLoader loader = handle.getClass().getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        if (!Proxy.isProxyClass(handle.getClass())) {
            final Object proxy = LocalBeanProxyFactory.Unsafe.allocateInstance(getProxy(handle.getClass(), loader));
            DynamicSubclass.setHandler(proxy, invocationHandler);
            return proxy;
        }

        return Proxy.newProxyInstance(loader, getAPi(handle.getClass()), invocationHandler);
    }

    private Class<?>[] getAPi(final Class<?> aClass) {
        Class<?>[] found = interfaces.get(aClass);
        if (found == null) {
            synchronized (this) {
                found = interfaces.get(aClass);
                if (found == null) {
                    final List<Class<?>> allInterfaces = getAllInterfaces(aClass);
                    final Class<?>[] asArray = allInterfaces.toArray(new Class<?>[allInterfaces.size()]);
                    interfaces.put(aClass, asArray);
                    found = interfaces.get(aClass);
                }
            }
        }
        return found;
    }

    private Class<?> getProxy(final Class<?> aClass, final ClassLoader loader) {
        Class<?> found = proxies.get(aClass);
        if (found == null) {
            synchronized (this) {
                found = proxies.get(aClass);
                if (found == null) {
                    proxies.put(aClass, DynamicSubclass.createSubclass(aClass, loader, true));
                    found = proxies.get(aClass);
                }
            }
        }
        return found;
    }

    public static String stackTraceToString(final StackTraceElement[] stackTrace) {
        if (stackTrace == null) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < stackTrace.length; i++) {
            final StackTraceElement element = stackTrace[i];
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(element.toString());
        }

        return sb.toString();
    }

    public static class ConnectionInvocationHandler implements InvocationHandler {
        private final Object handle;

        public ConnectionInvocationHandler(final Object handle) {
            this.handle = handle;
        }

        @Override
        public Object invoke(final Object object, final Method method, final Object[] args) throws Throwable {
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
                return method.invoke(handle, args);
            } catch (final InvocationTargetException ite) {
                // catch InvocationTargetExceptions and turn them into the target exception (if there is one)
                final Throwable t = ite.getTargetException();
                if (AbstractMethodError.class.isInstance(t)) {
                    // "debug" info
                    Logger.getInstance(LogCategory.OPENEJB, AutoConnectionTracker.class)
                        .error("Missing method: " + method + " on " + handle);
                }
                if (t != null) {
                    throw t;
                }
                throw ite;
            }
        }
    }

    private static class ProxyPhantomReference extends PhantomReference<ConnectionInvocationHandler> {
        private final ConnectionTrackingInterceptor interceptor;
        private final ManagedConnectionInfo managedConnectionInfo;
        private StackTraceElement[] stackTrace;

        @SuppressWarnings({"unchecked"})
        public ProxyPhantomReference(final ConnectionTrackingInterceptor interceptor,
                                     final ManagedConnectionInfo managedConnectionInfo,
                                     final ConnectionInvocationHandler handler,
                                     @SuppressWarnings("rawtypes") final ReferenceQueue referenceQueue) {
            super(handler, referenceQueue);
            this.interceptor = interceptor;
            this.managedConnectionInfo = managedConnectionInfo;
            this.stackTrace = Thread.currentThread().getStackTrace();
        }
    }
}
