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
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.reflection.Reflections;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

public class GeronimoConnectionManagerFactory   {
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
    private int poolMinSize = 0;
    private boolean allConnectionsEqual = true;
    private int connectionMaxWaitMilliseconds = 5000;
    private int connectionMaxIdleMinutes = 15;
    private int validationInterval = -1;
    private ManagedConnectionFactory mcf;

    public ManagedConnectionFactory getMcf() {
		return mcf;
	}

	public void setMcf(ManagedConnectionFactory mcf) {
		this.mcf = mcf;
	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public String getTransactionSupport() {
        return transactionSupport;
    }

    public void setTransactionSupport(String transactionSupport) {
        this.transactionSupport = transactionSupport;
    }

    public boolean isPooling() {
        return pooling;
    }

    public void setPooling(boolean pooling) {
        this.pooling = pooling;
    }

    public String getPartitionStrategy() {
        return partitionStrategy;
    }

    public void setPartitionStrategy(String partitionStrategy) {
        this.partitionStrategy = partitionStrategy;
    }

    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    public void setPoolMaxSize(int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
    }

    public int getPoolMinSize() {
        return poolMinSize;
    }

    public void setPoolMinSize(int poolMinSize) {
        this.poolMinSize = poolMinSize;
    }

    public boolean isAllConnectionsEqual() {
        return allConnectionsEqual;
    }

    public void setAllConnectionsEqual(boolean allConnectionsEqual) {
        this.allConnectionsEqual = allConnectionsEqual;
    }

    public int getConnectionMaxWaitMilliseconds() {
        return connectionMaxWaitMilliseconds;
    }

    public void setConnectionMaxWaitMilliseconds(int connectionMaxWaitMilliseconds) {
        this.connectionMaxWaitMilliseconds = connectionMaxWaitMilliseconds;
    }

    public void setConnectionMaxWaitTime(Duration connectionMaxWait) {
        if (connectionMaxWait.getUnit() == null) {
            connectionMaxWait.setUnit(TimeUnit.MILLISECONDS);
        }
        final long milleseconds = TimeUnit.MILLISECONDS.convert(connectionMaxWait.getTime(), connectionMaxWait.getUnit());
        setConnectionMaxWaitMilliseconds((int) milleseconds);
    }

    public int getConnectionMaxIdleMinutes() {
        return connectionMaxIdleMinutes;
    }

    public void setConnectionMaxIdleMinutes(int connectionMaxIdleMinutes) {
        this.connectionMaxIdleMinutes = connectionMaxIdleMinutes;
    }

    public void setConnectionMaxIdleTime(Duration connectionMaxIdle) {
        if (connectionMaxIdle.getUnit() == null) {
            connectionMaxIdle.setUnit(TimeUnit.MINUTES);
        }
        final long minutes = TimeUnit.MINUTES.convert(connectionMaxIdle.getTime(), connectionMaxIdle.getUnit());
        setConnectionMaxIdleMinutes((int) minutes);
    }

    public int getValidationInterval() {
        return validationInterval;
    }

    public void setValidationInterval(int validationInterval) {
        this.validationInterval = validationInterval;
    }

    public void setValidationInterval(final Duration validationInterval) {
        if (validationInterval.getUnit() == null) {
            validationInterval.setUnit(TimeUnit.MINUTES);
        }
        final long minutes = TimeUnit.MINUTES.convert(validationInterval.getTime(), validationInterval.getUnit());
        setValidationInterval((int) minutes);
    }

    public GenericConnectionManager create() {
        PoolingSupport poolingSupport = createPoolingSupport();

        ClassLoader classLoader = this.classLoader;
        if (classLoader == null) Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = getClass().getClassLoader();
        if (classLoader == null) classLoader = ClassLoader.getSystemClassLoader();

        TransactionSupport txSupport = createTransactionSupport();

        RecoverableTransactionManager tm;
        if (transactionManager instanceof RecoverableTransactionManager) {
            tm = (RecoverableTransactionManager) transactionManager;
        } else {
            if (txSupport.isRecoverable()) {
                throw new OpenEJBRuntimeException("currently recoverable tx support (xa) needs a geronimo tx manager");
            }
            tm = new SimpleRecoverableTransactionManager(transactionManager);
        }

        if (validationInterval >= 0 && mcf instanceof ValidatingManagedConnectionFactory) {
            return new ValidatingGenericConnectionManager(txSupport, poolingSupport,
                    null, new AutoConnectionTracker(), tm,
                    mcf, name, classLoader, validationInterval);
        }

        return new GenericConnectionManager(txSupport, poolingSupport,
                        null, new AutoConnectionTracker(), tm,
                        mcf, name, classLoader);
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
                    false);

        } else if ("by-connector-properties".equalsIgnoreCase(partitionStrategy)) {

            // partition by contector properties such as username and password on a jdbc connection
            return new PartitionedPool(poolMaxSize,
                    poolMinSize,
                    connectionMaxWaitMilliseconds,
                    connectionMaxIdleMinutes,
                    allConnectionsEqual,
                    !allConnectionsEqual,
                    false,
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
                    false,
                    false,
                    true);
        } else {
            throw new IllegalArgumentException("Unknown partition strategy " + partitionStrategy);
        }
    }

    private class SimpleRecoverableTransactionManager implements RecoverableTransactionManager {
        private final TransactionManager delegate;

        public SimpleRecoverableTransactionManager(final TransactionManager transactionManager) {
            delegate = transactionManager;
        }

        @Override
        public void recoveryError(final Exception e) {
            throw new UnsupportedOperationException();
        }

        public void registerNamedXAResourceFactory(final NamedXAResourceFactory namedXAResourceFactory) {
            throw new UnsupportedOperationException();
        }

        public void unregisterNamedXAResourceFactory(final String namedXAResourceFactoryName) {
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
        public void setTransactionTimeout(int i) throws SystemException {
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

        public ValidatingGenericConnectionManager(final TransactionSupport txSupport, final PoolingSupport poolingSupport, final SubjectSource o, final AutoConnectionTracker autoConnectionTracker, final RecoverableTransactionManager tm, final ManagedConnectionFactory mcf, final String name, final ClassLoader classLoader, final long interval) {
            super(txSupport, poolingSupport, o, autoConnectionTracker, tm, mcf, name, classLoader);
            validationInterval = interval;

            final ConnectionInterceptor stack = interceptors.getStack();

            ReadWriteLock foundLock = null;
            ConnectionInterceptor current = stack;
            do {
                if (current instanceof AbstractSinglePoolConnectionInterceptor) {
                    try {
                        foundLock = (ReadWriteLock) AbstractSinglePoolConnectionInterceptor.class.getField("resizeLock").get(current);
                    } catch (IllegalAccessException e) {
                        // no-op
                    } catch (NoSuchFieldException e) {
                        // no-op
                    }
                    break;
                }

                // look next
                try {
                    current = (ConnectionInterceptor) Reflections.get(current, "next");
                } catch (Exception e) {
                    current = null;
                }
            } while (current != null);

            this.lock = foundLock;

            Object foundPool = null;
            if (current instanceof AbstractSinglePoolConnectionInterceptor) {
                foundPool = Reflections.get(stack, "pool");
            } else if (current instanceof MultiPoolConnectionInterceptor) {
                log.warn("validation on stack " + stack + " not supported");
            }
            this.pool = foundPool;

            if (pool != null) {
                validatingTask = new ValidatingTask(current, lock, pool);
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

            public ValidatingTask(final ConnectionInterceptor stack, final ReadWriteLock lock, final Object pool) {
                this.stack = stack;
                this.lock = lock;
                this.pool = pool;
            }

            @Override
            public void run() {
                if (lock != null) {
                    lock.writeLock().lock();
                }

                try {
                    final Map<ManagedConnection, ManagedConnectionInfo> connections;
                    if (stack instanceof SinglePoolConnectionInterceptor) {
                        connections = new HashMap<ManagedConnection, ManagedConnectionInfo>();
                        for (final ManagedConnectionInfo info : (List<ManagedConnectionInfo>) pool) {
                            connections.put(info.getManagedConnection(), info);
                        }
                    } else if (stack instanceof SinglePoolMatchAllConnectionInterceptor) {
                        connections = (Map<ManagedConnection, ManagedConnectionInfo>) pool;
                    } else {
                        log.warn("stack " + stack + " currently not supported");
                        return;
                    }

                    // destroy invalid connections
                    try {
                        final Set<ManagedConnection> invalids = ValidatingManagedConnectionFactory.class.cast(getManagedConnectionFactory())
                                .getInvalidConnections(connections.keySet());
                        if (invalids != null) {
                            for (final ManagedConnection invalid : invalids) {
                                stack.returnConnection(new ConnectionInfo(connections.get(invalid)), ConnectionReturnAction.DESTROY);
                            }
                        }
                    } catch (ResourceException e) {
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
