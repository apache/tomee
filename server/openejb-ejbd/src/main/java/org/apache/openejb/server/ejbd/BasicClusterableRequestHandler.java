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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.ejbd;

import java.net.URI;

import org.apache.openejb.ClusteredRPCContainer;
import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.client.ClusterableRequest;
import org.apache.openejb.client.ClusterableResponse;
import org.apache.openejb.client.ServerMetaData;

public class BasicClusterableRequestHandler implements ClusterableRequestHandler {

    public void updateServer(DeploymentInfo deploymentInfo, ClusterableRequest req, ClusterableResponse res) {
        Container container = deploymentInfo.getContainer();
        if (container instanceof ClusteredRPCContainer) {
            ClusteredRPCContainer clusteredContainer = (ClusteredRPCContainer) container;
            URI[] locations = clusteredContainer.getLocations(deploymentInfo);
            if (null != locations) {
                ServerMetaData server = new ServerMetaData(locations);
                if (req.getServerHash() != server.buildHash()) {
                    res.setServer(server);
                }
            }
        }
    }

}
