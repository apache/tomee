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
import java.util.Properties;
import java.util.logging.Logger;

public class ConnectionManager {

    private static final Logger logger = Logger.getLogger("OpenEJB.client");
    
    private static Registry<ConnectionFactory> factories = Registry.create(ConnectionFactory.class);
    private static Registry<ConnectionStrategy> strategies = Registry.create(ConnectionStrategy.class);

    static {
        SocketConnectionFactory ejbdFactory = new SocketConnectionFactory();

        factories.register("default", ejbdFactory);
        factories.register("ejbd", ejbdFactory);
        factories.register("ejbds", ejbdFactory);

        HttpConnectionFactory httpFactory = new HttpConnectionFactory();
        factories.register("http", httpFactory);
        factories.register("https", httpFactory);

        factories.register("multicast", new MulticastConnectionFactory());
        factories.register("failover", new FailoverConnectionFactory());
        
        strategies.register("sticky", new StickyConnectionStrategy());
        strategies.register("random", new RandomConnectionStrategy());
        strategies.register("roundrobin", new RoundRobinConnectionStrategy());
        strategies.register("round-robin", strategies.get("roundrobin"));
        strategies.register("default", strategies.get("sticky"));
    }


    public static Connection getConnection(ClusterMetaData cluster, ServerMetaData server, Request req) throws IOException {
        String name = cluster.getConnectionStrategy();

        if (req instanceof EJBRequest) {
            EJBRequest ejbRequest = (EJBRequest) req;
            final Properties p = ejbRequest.getEjbMetaData().getProperties();
            name = p.getProperty("openejb.client.connection.strategy", name);
        }
        if (name == null) name = "default";

        ConnectionStrategy strategy = strategies.get(name);

        if (strategy == null) throw new IOException("Unsupported ConnectionStrategy  \"" + name + "\"");

        logger.fine("connect: strategy=" + name + ", uri=" + server.getLocation() + ", strategy-impl=" + strategy.getClass().getName());
        return strategy.connect(cluster, server);
    }

    public static Connection getConnection(URI uri) throws IOException {
        String scheme = uri.getScheme();

        ConnectionFactory factory = factories.get(scheme);

        if (factory == null) throw new IOException("Unsupported ConnectionFactory URI scheme  \"" + scheme + "\"");

        logger.fine("connect: scheme=" + scheme + ", uri=" + uri + ", factory-impl=" + factory.getClass().getName());
        return factory.getConnection(uri);
    }

    public static void registerFactory(String scheme, ConnectionFactory factory) {
        factories.register(scheme, factory);
    }

    public static ConnectionFactory unregisterFactory(String scheme) {
        return factories.unregister(scheme);
    }

    public static void registerStrategy(String scheme, ConnectionStrategy factory) {
        strategies.register(scheme, factory);
    }

    public static ConnectionStrategy unregisterStrategy(String scheme) {
        return strategies.unregister(scheme);
    }

    /**
     * @param factory
     * @throws IOException
     * @Depricated use register("default", factory);
     */
    public static void setFactory(ConnectionFactory factory) throws IOException {
        registerFactory("default", factory);
    }
}
