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

import java.io.IOException;
import java.net.URI;

import org.apache.openejb.ClusteredRPCContainer;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.client.ClusterableRequest;
import org.apache.openejb.client.ClusterableResponse;
import org.apache.openejb.client.ServerMetaData;

import com.agical.rmock.core.describe.ExpressionDescriber;
import com.agical.rmock.core.match.operator.AbstractExpression;
import com.agical.rmock.extension.junit.RMockTestCase;


public class BasicClusterableRequestHandlerTest extends RMockTestCase {

    private BasicClusterableRequestHandler requestHandler;
    private ClusterableRequest request;
    private ClusterableResponse response;
    private DeploymentInfo deploymentInfo;
    private ClusteredRPCContainer clusteredContainer;

    @Override
    protected void setUp() throws Exception {
        requestHandler = new BasicClusterableRequestHandler();
        request = (ClusterableRequest) mock(ClusterableRequest.class);
        response = (ClusterableResponse) mock(ClusterableResponse.class);
        deploymentInfo = (DeploymentInfo) mock(DeploymentInfo.class);
        clusteredContainer = (ClusteredRPCContainer) mock(ClusteredRPCContainer.class);
    }
    
    public void testNoOpWhenNotAClusteredContainer() throws Exception {
        deploymentInfo.getContainer();
        
        startVerification();
        
        requestHandler.updateServer(deploymentInfo, request, response);
    }

    public void testUpdateServerWhenRequestHashDiffersFromServerSideHash() throws Exception {
        final URI[] locations = new URI[] {new URI("ejbd://localhost:4201")};
        ServerMetaData server = new ServerMetaData(locations);

        deploymentInfo.getContainer();
        modify().returnValue(clusteredContainer);

        request.getServerHash();
        modify().returnValue(server.buildHash() + 1);

        response.setServer(null);
        modify().args(new AbstractExpression() {
            public void describeWith(ExpressionDescriber arg0) throws IOException {
            }

            public boolean passes(Object arg0) {
                ServerMetaData actualServer = (ServerMetaData) arg0;
                assertSame(locations, actualServer.getLocations());
                return true;
            }
        });
        
        clusteredContainer.getLocations(deploymentInfo);
        modify().returnValue(locations);
        
        startVerification();
        
        requestHandler.updateServer(deploymentInfo, request, response);
    }

    public void testServerIsNotUpdatedWhenRequestHashEqualsServerSideHash() throws Exception {
        URI[] locations = new URI[] {new URI("ejbd://localhost:4201")};
        ServerMetaData server = new ServerMetaData(locations);

        deploymentInfo.getContainer();
        modify().returnValue(clusteredContainer);

        request.getServerHash();
        modify().returnValue(server.buildHash());

        clusteredContainer.getLocations(deploymentInfo);
        modify().returnValue(locations);

        startVerification();
        
        requestHandler.updateServer(deploymentInfo, request, response);
    }

}
