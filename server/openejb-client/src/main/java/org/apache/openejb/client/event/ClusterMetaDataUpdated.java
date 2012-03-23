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
import org.apache.openejb.client.ServerMetaData;

/**
 * @version $Rev$ $Date$
 */
public class ClusterMetaDataUpdated {

    private final ServerMetaData serverMetaData;
    private final ClusterMetaData clusterMetaData;
    private final ClusterMetaData previousClusterMetaData;

    public ClusterMetaDataUpdated(ServerMetaData serverMetaData, ClusterMetaData clusterMetaData, ClusterMetaData previousClusterMetaData) {
        this.serverMetaData = serverMetaData;
        this.clusterMetaData = clusterMetaData;
        this.previousClusterMetaData = previousClusterMetaData;
    }

    public ServerMetaData getServerMetaData() {
        return serverMetaData;
    }

    public ClusterMetaData getClusterMetaData() {
        return clusterMetaData;
    }

    public ClusterMetaData getPreviousClusterMetaData() {
        return previousClusterMetaData;
    }

    @Override
    public String toString() {
        return "ClusterMetaDataUpdated{" +
                "provider=" + serverMetaData.getLocation() +
                ", version=" + clusterMetaData.getVersion() +
                ", uris=" + clusterMetaData.getLocations().length +
                '}';
    }
}
