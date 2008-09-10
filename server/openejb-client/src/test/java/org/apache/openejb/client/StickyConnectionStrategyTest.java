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
package org.apache.openejb.client;

import java.io.IOException;
import java.net.URI;

import com.agical.rmock.extension.junit.RMockTestCase;


public class StickyConnectionStrategyTest extends RMockTestCase {

    private StickyConnectionStrategy factoryStrategy;
    private ConnectionFactory connectionFactory;
    private URI[] locations;

    @Override
    protected void setUp() throws Exception {
        connectionFactory = (ConnectionFactory) mock(ConnectionFactory.class);
        ConnectionManager.registerFactory("ejbd", connectionFactory);

        factoryStrategy = new StickyConnectionStrategy();
        
        URI uri1 = new URI("ejbd://localhost:4201");
        URI uri2 = new URI("ejbd://localhost:4202");
        locations = new URI[] {uri1, uri2};
    }
    
    public void testThrowsRemoteExceptionIfCannotConnectToAllURIs() throws Exception {
        connectionFactory.getConnection(locations[0]);
        modify().throwException(new IOException());
        
        connectionFactory.getConnection(locations[1]);
        modify().throwException(new IOException());
        
        startVerification();

        try {
            ServerMetaData server = new ServerMetaData(locations[0]);
            factoryStrategy.connect(new ClusterMetaData(1, locations), server);
            fail();
        } catch (IOException e) {
        }
    }
    
    public void _testReturnFirstSuccessfulConnection() throws Exception {
        connectionFactory.getConnection(locations[0]);
        modify().throwException(new IOException());
        
        Connection expectedConnection = connectionFactory.getConnection(locations[1]);
        
        startVerification();

        ServerMetaData server = new ServerMetaData(locations[0]);
        ClusterMetaData cluster = new ClusterMetaData(1, locations);
        Connection actualConnection = factoryStrategy.connect(cluster, server);
        assertSame(expectedConnection, actualConnection);
    }
    
    public void _testReConnectToLastServerFirst() throws Exception {
        connectionFactory.getConnection(locations[0]);
        modify().throwException(new IOException());
        
        connectionFactory.getConnection(locations[1]);
        connectionFactory.getConnection(locations[1]);
        
        startVerification();

        ServerMetaData server = new ServerMetaData(locations[0]);
        ClusterMetaData cluster = new ClusterMetaData(1, locations);
        factoryStrategy.connect(cluster, server);
        factoryStrategy.connect(cluster, server);
    }
    
}
