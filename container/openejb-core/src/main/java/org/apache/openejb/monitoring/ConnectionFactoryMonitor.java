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
package org.apache.openejb.monitoring;

import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;

public class ConnectionFactoryMonitor {

    private final String name;
    private final GenericConnectionManager connectionManager;
    private final String txSupport;

    public ConnectionFactoryMonitor(final String name, final GenericConnectionManager connectionManager, final String txSupport) {
        this.name = name;
        this.connectionManager = connectionManager;
        this.txSupport = txSupport;
    }

    private PoolingSupport getPooling() {
        return connectionManager.getPooling();
    }

    @Managed
    public int getMaxSize() {
        final PoolingSupport pooling = getPooling();

        if (PartitionedPool.class.isInstance(pooling)) {
            return PartitionedPool.class.cast(pooling).getMaxSize();
        } else if (SinglePool.class.isInstance(pooling)) {
            return SinglePool.class.cast(pooling).getMaxSize();
        } else if (NoPool.class.isInstance(pooling)) {
            return 0;
        } else {
            return 0;
        }
    }

    @Managed
    public int getMinSize() {
        final PoolingSupport pooling = getPooling();

        if (PartitionedPool.class.isInstance(pooling)) {
            return 0;
        } else if (SinglePool.class.isInstance(pooling)) {
            return SinglePool.class.cast(pooling).getMinSize();
        } else if (NoPool.class.isInstance(pooling)) {
            return 0;
        } else {
            return 0;
        }
    }

    @Managed
    public int getBlockingTimeoutMilliseconds() {
        return connectionManager.getBlockingTimeoutMilliseconds();
    }

    @Managed
    public int getIdleTimeoutMinutes() {
        return connectionManager.getIdleTimeoutMinutes();
    }

    @Managed
    public boolean isMatchAll() {
        final PoolingSupport pooling = getPooling();

        if (PartitionedPool.class.isInstance(pooling)) {
            return PartitionedPool.class.cast(pooling).isMatchAll();
        } else if (SinglePool.class.isInstance(pooling)) {
            return SinglePool.class.cast(pooling).isMatchAll();
        } else {
            return false;
        }
    }

    @Managed
    public String getPartitionStrategy() {
        final PoolingSupport pooling = getPooling();

        if (PartitionedPool.class.isInstance(pooling)) {
            if (PartitionedPool.class.cast(pooling).isPartitionByConnectionRequestInfo()) {
                return PartitionStrategy.BY_CONNECTOR_PROPERTIES.toString();
            }

            if (PartitionedPool.class.cast(pooling).isPartitionBySubject()) {
                return PartitionStrategy.BY_SUBJECT.toString();
            }

            return PartitionStrategy.UNKNOWN.toString();
        } else if (SinglePool.class.isInstance(pooling)) {
            return PartitionStrategy.NONE.toString();
        } else if (NoPool.class.isInstance(pooling)) {
            return PartitionStrategy.NONE.toString();
        } else {
            return PartitionStrategy.NONE.toString();
        }
    }

    @Managed
    public String getTxSupport() {
        return txSupport;
    }

    @Managed
    public int getPartitionCount() {
        return connectionManager.getPartitionCount();
    }

    @Managed
    public int getPartitionMaxSize() {
        return connectionManager.getPartitionMaxSize();
    }

    @Managed
    public int getPartitionMinSize() {
        return connectionManager.getPartitionMinSize();
    }

    @Managed
    public int getIdleConnectionCount() {
        return connectionManager.getIdleConnectionCount();
    }

    @Managed
    public int getConnectionCount() {
        return connectionManager.getConnectionCount();
    }

    @Managed
    public String getName() {
        return name;
    }

    public enum PartitionStrategy {
        NONE("none"), BY_SUBJECT("by-subject"), BY_CONNECTOR_PROPERTIES("by-connector-properties"), UNKNOWN("unknown");

        private final String name;

        PartitionStrategy(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
