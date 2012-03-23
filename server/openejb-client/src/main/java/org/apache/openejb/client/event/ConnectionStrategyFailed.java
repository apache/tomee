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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client.event;

import org.apache.openejb.client.ClusterMetaData;
import org.apache.openejb.client.ConnectionStrategy;
import org.apache.openejb.client.ServerMetaData;

/**
 * @version $Rev$ $Date$
 */
@Log(Log.Level.SEVERE)
public class ConnectionStrategyFailed {

    private final ConnectionStrategy strategy;
    private final ClusterMetaData cluster;
    private final ServerMetaData server;
    private final Throwable cause;

    public ConnectionStrategyFailed(ConnectionStrategy strategy, ClusterMetaData cluster, ServerMetaData server, Throwable cause) {
        this.strategy = strategy;
        this.cluster = cluster;
        this.server = server;
        this.cause = cause;
    }

    public ConnectionStrategy getStrategy() {
        return strategy;
    }

    public ClusterMetaData getCluster() {
        return cluster;
    }

    public ServerMetaData getServer() {
        return server;
    }

    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return "ConnectionStrategyFailed{" +
                "strategy=" + strategy.getClass().getSimpleName() +
                ", cluster=" + cluster +
                ", server=" + server.getLocation() +
                '}';
    }
}
