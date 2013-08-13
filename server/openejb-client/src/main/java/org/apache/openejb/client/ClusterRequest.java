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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @version $Rev$ $Date$
 */
public class ClusterRequest implements Request {

    private static final long serialVersionUID = 2188352573904048149L;
    private transient long clusterMetaDataVersion;
    private transient ProtocolMetaData metaData;

    public ClusterRequest() {
    }

    public ClusterRequest(final ClusterMetaData clusterMetaData) {
        clusterMetaDataVersion = clusterMetaData.getVersion();
    }

    @Override
    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.CLUSTER_REQUEST;
    }

    public long getClusterMetaDataVersion() {
        return clusterMetaDataVersion;
    }

    /**
     * Changes to this method must observe the optional {@link #metaData} version
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        clusterMetaDataVersion = in.readLong();
    }

    /**
     * Changes to this method must observe the optional {@link #metaData} version
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeLong(clusterMetaDataVersion);
    }
}
