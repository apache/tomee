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

import org.apache.geronimo.connector.outbound.AbstractSinglePoolConnectionInterceptor;
import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionInterceptor;
import org.apache.geronimo.connector.outbound.ConnectionReturnAction;
import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.geronimo.connector.outbound.MultiPoolConnectionInterceptor;
import org.apache.geronimo.connector.outbound.SinglePoolConnectionInterceptor;
import org.apache.geronimo.connector.outbound.SinglePoolMatchAllConnectionInterceptor;
import org.apache.geronimo.connector.outbound.SubjectSource;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
import org.apache.geronimo.transaction.manager.NamedXAResourceFactory;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.monitoring.ConnectionFactoryMonitor;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ManagedMBean;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.reflection.Reflections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

public class GeronimoConnectionManagerFactory {
    private final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, GeronimoConnectionManagerFactory.class);
    private String name;
    private ClassLoader classLoader;

    private TransactionManager transactionManager;

    // Type of transaction used by the ConnectionManager
    // local, none, or xa
    private String transactionSupport;

    // pooling properties
    private boolean pooling = true;
    private String partitionStrategy; //: none, by-subject, by-connector-properties
    private int poolMaxSize = 10;
    private int poolMinSize;
    private boolean allConnectionsEqual = true;
    private boolean assumeOneMatch = false;
    private int connectionMaxWaitMilliseconds = 5000;
    private int connectionMaxIdleMinutes = 15;
    private ManagedConnectionFactory mcf;
    private int validationIntervalMs = -1;
    private boolean cleanupLeakedConnections = true;

    public boolean isAssumeOneMatch() {
        return assumeOneMatch;
    }

    public void setAssumeOneMatch(final boolean assumeOneMatch) {
        this.assumeOneMatch = assumeOneMatch;
    }

    public ManagedConnectionFactory getMcf() {
        return mcf;
    }

    public void setMcf(final ManagedConnectionFactory mcf) {
        this.mcf = mcf;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public String getTransactionSupport() {
        return transactionSupport;
    }

    public void setTransactionSupport(final String transactionSupport) {
        this.transactionSupport = transactionSupport;
    }

    public boolean isPooling() {
        return pooling;
    }

    public void setPooling(final boolean pooling) {
        this.pooling = pooling;
    }

    public String getPartitionStrategy() {
        return partitionStrategy;
    }

    public void setPartitionStrategy(final String partitionStrategy) {
        this.partitionStrategy = partitionStrategy;
    }

    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    public void setPoolMaxSize(final int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
    }

    public int getPoolMinSize() {
        return poolMinSize;
    }

    public void setPoolMinSize(final int poolMinSize) {
        this.poolMinSize = poolMinSize;
    }

    public boolean isAllConnectionsEqual() {
        return allConnectionsEqual;
    }

    public void setAllConnectionsEqual(final boolean allConnectionsEqual) {
        this.allConnectionsEqual = allConnectionsEqual;
    }

    public int getConnectionMaxWaitMilliseconds() {
        return connectionMaxWaitMilliseconds;
    }

    public void setConnectionMaxWaitMilliseconds(final int connectionMaxWaitMilliseconds) {
        this.connectionMaxWaitMilliseconds = connectionMaxWaitMilliseconds;
    }

    public void setConnectionMaxWaitTime(final Duration connectionMaxWait) {
        if (connectionMaxWait.getUnit() == null) {
            connectionMaxWait.setUnit(TimeUnit.MILLISECONDS);
        }
        final long milleseconds = TimeUnit.MILLISECONDS.convert(connectionMaxWait.getTime(), connectionMaxWait.getUnit());
        setConnectionMaxWaitMilliseconds((int) milleseconds);
    }

    public int getConnectionMaxIdleMinutes() {
        return connectionMaxIdleMinutes;
    }

    public void setConnectionMaxIdleMinutes(final int connectionMaxIdleMinutes) {
        this.connectionMaxIdleMinutes = connectionMaxIdleMinutes;
    }

    public void setConnectionMaxIdleTime(final Duration connectionMaxIdle) {
        if (connectionMaxIdle.getUnit() == null) {
            connectionMaxIdle.setUnit(TimeUnit.MINUTES);
        }
        final long minutes = TimeUnit.MINUTES.convert(connectionMaxIdle.getTime(), connectionMaxIdle.getUnit());
        setConnectionMaxIdleMinutes((int) minutes);
    }

    public int getValidationInterval() {
        return validationIntervalMs < 0 ? -1 : (int) TimeUnit.MILLISECONDS.toMinutes(validationIntervalMs);
    }

    public void setValidationInterval(final int validationInterval) {
        this.validationIntervalMs = validationInterval < 0 ? -1 : (int) TimeUnit.MINUTES.toMillis(validationInterval);
    }

    public void setValidationInterval(final Duration validationInterval) {
        if (validationInterval.getUnit() == null) {
            validationInterval.setUnit(TimeUnit.MINUTES);
        }
        validationIntervalMs = (int) validationInterval.getUnit().toMillis(validationInterval.getTime());
    }

    public boolean isCleanupLeakedConnections() {
        return cleanupLeakedConnections;
    }

    public void setCleanupLeakedConnections(final boolean cleanupLeakedConnections) {
        this.cleanupLeakedConnections = cleanupLeakedConnections;
    }

    public GenericConnectionManager create() {
        final PoolingSupport poolingSupport = createPoolingSupport();

        ClassLoader classLoader = this.classLoader;
        if (classLoader == null) {
            Thread.currentThread().getContextClassLoader();
        }
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        final TransactionSupport txSupport = createTransactionSupport();

        final RecoverableTransactionManager tm;
        if (transactionManager instanceof RecoverableTransactionManager) {
            tm = (RecoverableTransactionManager) transactionManager;
        } else {
            if (txSupport.isRecoverable()) {
                throw new OpenEJBRuntimeException("currently recoverable tx support (xa) needs a geronimo tx manager");
            }
            tm = new SimpleRecoverableTransactionManager(transactionManager, name);
        }

        final GenericConnectionManager mgr;
        if (validationIntervalMs >= 0 && mcf instanceof ValidatingManagedConnectionFactory) {
            if (name == null) {
                name = getClass().getSimpleName();
            }
            mgr = new ValidatingGenericConnectionManager(txSupport, poolingSupport,
                    null, new AutoConnectionTracker(cleanupLeakedConnections), tm,
                    mcf, name, classLoader, validationIntervalMs);
        } else {
            mgr = new GenericConnectionManager(txSupport, poolingSupport,
                    null, new AutoConnectionTracker(cleanupLeakedConnections), tm,
                    mcf, name, classLoader);
        }


        final ConnectionFactoryMonitor cfm = new ConnectionFactoryMonitor(name, mgr, transactionSupport);
        final MBeanServer server = LocalMBeanServer.get();

        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("J2EEApplication", null);
        jmxName.set("j2eeType", "");
        jmxName.set("name", name);

        try {
            final ObjectName objectName = jmxName.set("j2eeType", "ConnectionFactory").build();
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
            }

            server.registerMBean(new ManagedMBean(cfm), objectName);
        } catch (final Exception e) {
            logger.error("Unable to register MBean ", e);
        }

        return mgr;
    }

    private TransactionSupport createTransactionSupport() {
        if (transactionSupport == null || "local".equalsIgnoreCase(transactionSupport)) {
            return LocalTransactions.INSTANCE;
        } else if ("none".equalsIgnoreCase(transactionSupport)) {
            return NoTransactions.INSTANCE;
        } else if ("xa".equalsIgnoreCase(transactionSupport)) {
            return new XATransactions(true, false);
        } else {
            throw new IllegalArgumentException("Unknown transaction type " + transactionSupport);
        }
    }


    private PoolingSupport createPoolingSupport() {
        // pooling off?
        if (!pooling) {
            return new NoPool();
        }

        if (partitionStrategy == null || "none".equalsIgnoreCase(partitionStrategy)) {

            // unpartitioned pool
            return new SinglePool(poolMaxSize,
                    poolMinSize,
                    connectionMaxWaitMilliseconds,
                    connectionMaxIdleMinutes,
                    allConnectionsEqual,
                    !allConnectionsEqual,
                    assumeOneMatch);

        } else if ("by-connector-properties".equalsIgnoreCase(partitionStrategy)) {

            // partition by contector properties such as username and password on a jdbc connection
            return new PartitionedPool(poolMaxSize,
                    poolMinSize,
                    connectionMaxWaitMilliseconds,
                    connectionMaxIdleMinutes,
                    allConnectionsEqual,
                    !allConnectionsEqual,
                    assumeOneMatch,
                    true,
                    false);
        } else if ("by-subject".equalsIgnoreCase(partitionStrategy)) {

            // partition by caller subject
            return new PartitionedPool(poolMaxSize,
                    poolMinSize,
                    connectionMaxWaitMilliseconds,
                    connectionMaxIdleMinutes,
                    allConnectionsEqual,
                    !allConnectionsEqual,
                    assumeOneMatch,
                    false,
                    true);
        }

        throw new IllegalArgumentException("Unknown partition strategy " + partitionStrategy);
    }

    private class SimpleRecoverableTransactionManager implements RecoverableTransactionManager {
        private final TransactionManager delegate;
        private final String name;

        public SimpleRecoverableTransactionManager(final TransactionManager transactionManager, final String name) {
            this.delegate = transactionManager;
            this.name = name;
        }

        @Override
        public void recoveryError(final Exception e) {
            throw new UnsupportedOperationException();
        }

        public void registerNamedXAResourceFactory(final NamedXAResourceFactory namedXAResourceFactory) {
            if ((name == null && namedXAResourceFactory == null || (namedXAResourceFactory != null && namedXAResourceFactory.getName() == null)) ||
                    (name != null && namedXAResourceFactory != null && name.equals(namedXAResourceFactory.getName()))) {
                return;
            }
            throw new UnsupportedOperationException();
        }

        public void unregisterNamedXAResourceFactory(final String namedXAResourceFactoryName) {
            if ((name == null && namedXAResourceFactoryName == null) || (name != null && name.equals(namedXAResourceFactoryName))) {
                return;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public void begin() throws NotSupportedException, SystemException {
            delegate.begin();
        }

        @Override
        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            delegate.commit();
        }

        @Override
        public int getStatus() throws SystemException {
            return delegate.getStatus();
        }

        @Override
        public Transaction getTransaction() throws SystemException {
            return delegate.getTransaction();
        }

        @Override
        public void resume(final Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
            delegate.resume(transaction);
        }

        @Override
        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            delegate.rollback();
        }

        @Override
        public void setRollbackOnly() throws IllegalStateException, SystemException {
            delegate.setRollbackOnly();
        }

        @Override
        public void setTransactionTimeout(final int i) throws SystemException {
            delegate.setTransactionTimeout(i);
        }

        @Override
        public Transaction suspend() throws SystemException {
            return delegate.suspend();
        }
    }

    private static class ValidatingGenericConnectionManager extends GenericConnectionManager {
        private static final Timer TIMER = new Timer("ValidatingGenericConnectionManagerTimer", true);

        private final TimerTask validatingTask;
        private final long validationInterval;

        private final ReadWriteLock lock;
        private final Object pool;

        public ValidatingGenericConnectionManager(final TransactionSupport txSupport, final PoolingSupport poolingSupport, final SubjectSource o,
                                                  final AutoConnectionTracker autoConnectionTracker, final RecoverableTransactionManager tm,
                                                  final ManagedConnectionFactory mcf, final String name, final ClassLoader classLoader, final long interval) {
            super(txSupport, poolingSupport, o, autoConnectionTracker, tm, mcf, name, classLoader);
            validationInterval = interval;

            final ConnectionInterceptor stack = interceptors.getStack();

            ReadWriteLock foundLock = null;
            ConnectionInterceptor current = stack;
            do {
                if (current instanceof AbstractSinglePoolConnectionInterceptor) {
                    try {
                        final Field resizeLock = AbstractSinglePoolConnectionInterceptor.class.getDeclaredField("resizeLock");
                        if (!resizeLock.isAccessible()) {
                            resizeLock.setAccessible(true);
                        }
                        foundLock = (ReadWriteLock) resizeLock.get(current);
                    } catch (final IllegalAccessException | NoSuchFieldException e) {
                        // no-op
                    }
                    break;
                }

                // look next
                try {
                    current = (ConnectionInterceptor) Reflections.get(current, "next");
                } catch (final Exception e) {
                    current = null;
                }
            } while (current != null);

            this.lock = foundLock;

            Object foundPool = null;
            if (current instanceof AbstractSinglePoolConnectionInterceptor) {
                foundPool = Reflections.get(current, "pool");
            } else if (current instanceof MultiPoolConnectionInterceptor) {
                log.warn("validation on stack " + stack + " not supported");
            }
            this.pool = foundPool;

            if (pool != null) {
                validatingTask = new ValidatingTask(current, lock, pool, autoConnectionTracker);
            } else {
                validatingTask = null;
            }
        }

        @Override
        public void doStart() throws Exception {
            super.doStart();
            if (validatingTask != null) {
                TIMER.schedule(validatingTask, validationInterval, validationInterval);
            }
        }

        @Override
        public void doStop() throws Exception {
            if (validatingTask != null) {
                validatingTask.cancel();
            }
            super.doStop();
        }

        private class ValidatingTask extends TimerTask {
            private final ConnectionInterceptor stack;
            private final ReadWriteLock lock;
            private final Object pool;
            private final AutoConnectionTracker autoConnectionTracker;

            public ValidatingTask(final ConnectionInterceptor stack, final ReadWriteLock lock, final Object pool,
                                  final AutoConnectionTracker autoConnectionTracker) {
                this.stack = stack;
                this.lock = lock;
                this.pool = pool == null ? new Object() : pool;
                this.autoConnectionTracker = autoConnectionTracker;

                if (!SinglePoolConnectionInterceptor.class.isInstance(stack) && !SinglePoolMatchAllConnectionInterceptor.class.isInstance(stack)) {
                    log.info("stack " + stack + " currently not supported, only AutoConnectionTracker ref will be used for validation");
                }
            }

            @Override
            public void run() {
                synchronized (pool) {
                    if (lock != null) {
                        lock.writeLock().lock();
                    }

                    try {
                        final Map<ManagedConnection, ManagedConnectionInfo> connections;
                        if (stack instanceof SinglePoolConnectionInterceptor) {
                            connections = new HashMap<>();
                            for (final ManagedConnectionInfo info : (List<ManagedConnectionInfo>) pool) {
                                connections.put(info.getManagedConnection(), info);
                            }
                        } else if (stack instanceof SinglePoolMatchAllConnectionInterceptor) {
                            connections = (Map<ManagedConnection, ManagedConnectionInfo>) pool;
                        } else {
                            connections = new HashMap<>();
                        }
                        for (final ManagedConnectionInfo info : autoConnectionTracker.connections()) {
                            connections.put(info.getManagedConnection(), info);
                        }

                        // destroy invalid connections
                        try {
                            final Set<ManagedConnection> invalids = ValidatingManagedConnectionFactory.class.cast(getManagedConnectionFactory())
                                    .getInvalidConnections(connections.keySet());
                            if (invalids != null) {
                                for (final ManagedConnection invalid : invalids) {
                                    final ManagedConnectionInfo mci = connections.get(invalid);
                                    if (mci != null) {
                                        stack.returnConnection(new ConnectionInfo(mci), ConnectionReturnAction.DESTROY);
                                        continue;
                                    }
                                    log.error("Can't find " + invalid + " in " + pool);
                                }
                            }
                        } catch (final ResourceException e) {
                            log.error(e.getMessage(), e);
                        }
                    } finally {
                        if (lock != null) {
                            lock.writeLock().unlock();
                        }
                    }
                }
            }
        }
    }
}
