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

import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
import org.apache.geronimo.transaction.manager.ExponentialtIntervalRetryScheduler;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.NamedXAResourceFactory;
import org.apache.geronimo.transaction.manager.RecoverTask;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.apache.geronimo.transaction.manager.Recovery;
import org.apache.geronimo.transaction.manager.RecoveryImpl;
import org.apache.geronimo.transaction.manager.RetryScheduler;
import org.apache.geronimo.transaction.manager.TransactionImpl;
import org.apache.geronimo.transaction.manager.XidImpl;

import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    public int getConnectionMaxIdleMinutes() {
        return connectionMaxIdleMinutes;
    }

    public void setConnectionMaxIdleMinutes(int connectionMaxIdleMinutes) {
        this.connectionMaxIdleMinutes = connectionMaxIdleMinutes;
    }

    public GenericConnectionManager create() {
        PoolingSupport poolingSupport = createPoolingSupport();

        ClassLoader classLoader = this.classLoader;
        if (classLoader == null) Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = getClass().getClassLoader();
        if (classLoader == null) classLoader = ClassLoader.getSystemClassLoader();
        RecoverableTransactionManager tm;
        if (transactionManager instanceof RecoverableTransactionManager) {
            tm = (RecoverableTransactionManager) transactionManager;
        } else {
            tm = new SimpleRecoverableTransactionManager(transactionManager);
        }
        return new GenericConnectionManager(
                createTransactionSupport(),
                poolingSupport,
                null,
                new AutoConnectionTracker(),
                tm,
                mcf,
                name,
                classLoader);
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
        private final Recovery recovery;
        private final List<Exception> recoveryErrors = new ArrayList<Exception>();
        private final RetryScheduler retryScheduler = new ExponentialtIntervalRetryScheduler();
        private final Map<String, NamedXAResourceFactory> namedXAResourceFactories = new ConcurrentHashMap<String, NamedXAResourceFactory>();

        public SimpleRecoverableTransactionManager(final TransactionManager transactionManager) {
            delegate = transactionManager;
            recovery = new Recovery() { // TODO
                @Override
                public void recoverLog() throws XAException {
                    // no-op
                }

                @Override
                public void recoverResourceManager(final NamedXAResource namedXAResource) throws XAException {
                    // no-op
                }

                @Override
                public boolean hasRecoveryErrors() {
                    return !recoveryErrors.isEmpty();
                }

                @Override
                public List getRecoveryErrors() {
                    return recoveryErrors;
                }

                @Override
                public boolean localRecoveryComplete() {
                    return true;
                }

                @Override
                public int localUnrecoveredCount() {
                    return 0;
                }

                @Override
                public Map<Xid, TransactionImpl> getExternalXids() {
                    return Collections.emptyMap();
                }
            };
        }

        @Override
        public void recoveryError(final Exception e) {
            recoveryErrors.add(e);
        }

        public void registerNamedXAResourceFactory(final NamedXAResourceFactory namedXAResourceFactory) {
            namedXAResourceFactories.put(namedXAResourceFactory.getName(), namedXAResourceFactory);
            new RecoverTask(retryScheduler, namedXAResourceFactory, recovery, this).run();
        }

        public void unregisterNamedXAResourceFactory(final String namedXAResourceFactoryName) {
            namedXAResourceFactories.remove(namedXAResourceFactoryName);
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
}
