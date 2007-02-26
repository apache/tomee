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

import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.springframework.beans.FatalBeanException;

import javax.transaction.TransactionManager;

public class GeronimoConnectionManagerFactory   {
    private String name;
    private ClassLoader classLoader;

    private TransactionManager transactionManager;

    private boolean containerManagedSecurity;

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

    public boolean isContainerManagedSecurity() {
        return containerManagedSecurity;
    }

    public void setContainerManagedSecurity(boolean containerManagedSecurity) {
        this.containerManagedSecurity = containerManagedSecurity;
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
        GenericConnectionManager connectionManager = new GenericConnectionManager(
                createTransactionSupport(),
                poolingSupport,
                containerManagedSecurity,
                new ConnectionTrackingCoordinator(true),
                transactionManager,
                name,
                classLoader);
        return connectionManager;
    }

    private TransactionSupport createTransactionSupport() {
        if (transactionSupport == null || "local".equalsIgnoreCase(transactionSupport)) {
            return LocalTransactions.INSTANCE;
        } else if ("none".equalsIgnoreCase(transactionSupport)) {
            return NoTransactions.INSTANCE;
        } else if ("xa".equalsIgnoreCase(transactionSupport)) {
            return new XATransactions(true, false);
        } else {
            throw new FatalBeanException("Unknown transaction type " + transactionSupport);
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
            throw new FatalBeanException("Unknown partition strategy " + partitionStrategy);
        }
    }
}
