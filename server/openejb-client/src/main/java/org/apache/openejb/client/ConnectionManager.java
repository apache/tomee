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

import org.apache.openejb.client.event.ConnectionFactoryAdded;
import org.apache.openejb.client.event.ConnectionFactoryRemoved;
import org.apache.openejb.client.event.ConnectionFailed;
import org.apache.openejb.client.event.ConnectionStrategyAdded;
import org.apache.openejb.client.event.ConnectionStrategyFailed;
import org.apache.openejb.client.event.Log;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

public class ConnectionManager {

    //Do not leave this in production code
    //private static final Logger logger = Logger.getLogger("OpenEJB.client");

    private static Registry<ConnectionFactory> factories = Registry.create(ConnectionFactory.class);
    private static Registry<ConnectionStrategy> strategies = Registry.create(ConnectionStrategy.class);

    static {
        final SocketConnectionFactory ejbdFactory = new SocketConnectionFactory();

        registerFactory("default", ejbdFactory);
        registerFactory("ejbd", ejbdFactory);
        registerFactory("ejbds", ejbdFactory);

        final HttpConnectionFactory httpFactory = new HttpConnectionFactory();
        registerFactory("http", httpFactory);
        registerFactory("https", httpFactory);

        registerFactory("multicast", new MulticastConnectionFactory());
        registerFactory("failover", new FailoverConnectionFactory());

        registerStrategy("sticky", new StickyConnectionStrategy());
        registerStrategy("sticky+random", new StickyConnectionStrategy(new RandomConnectionStrategy()));
        registerStrategy("sticky+round", new StickyConnectionStrategy(new RoundRobinConnectionStrategy()));
        registerStrategy("random", new RandomConnectionStrategy());
        registerStrategy("roundrobin", new RoundRobinConnectionStrategy());
        registerStrategy("round-robin", strategies.get("roundrobin"));
        registerStrategy("default", strategies.get("sticky"));
    }


    public static Connection getConnection(final ClusterMetaData cluster, final ServerMetaData server, final Request req) throws IOException {
        if (cluster == null) throw new IllegalArgumentException("cluster cannot be null");
        if (server == null) throw new IllegalArgumentException("server cannot be null");

        String name = cluster.getConnectionStrategy();

        if (req instanceof EJBRequest) {
            final EJBRequest ejbRequest = (EJBRequest) req;
            final Properties p = ejbRequest.getEjbMetaData().getProperties();
            name = p.getProperty("openejb.client.connection.strategy", name);
        }

        if (name == null) name = "default";

        final ConnectionStrategy strategy = strategies.get(name);
        if (strategy == null) {
            throw new UnsupportedConnectionStrategyException(name);
        }

        try {
            //Do not leave this in production code
            //logger.finest("connect: strategy=" + name + ", uri=" + server.getLocation() + ", strategy-impl=" + strategy.getClass().getName());

            return strategy.connect(cluster, server);
        } catch (Throwable e) {
            Client.fireEvent(new ConnectionStrategyFailed(strategy, cluster, server, e));

            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException(e);
            }
        }
    }

    public static Connection getConnection(final URI uri) throws IOException {
        if (uri == null) throw new IllegalArgumentException("uri cannot be null");

        final String scheme = uri.getScheme();
        final ConnectionFactory factory = factories.get(scheme);
        if (factory == null) {
            throw new UnsupportedConnectionFactoryException(scheme);
        }

        try {
            //Do not leave this in production code
            //logger.finest("connect: scheme=" + scheme + ", uri=" + uri + ", factory-impl=" + factory.getClass().getName());

            return factory.getConnection(uri);
        } catch (Throwable e) {
            Client.fireEvent(new ConnectionFailed(uri, e));

            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException(e);
            }
        }
    }

    public static void registerFactory(final String scheme, final ConnectionFactory factory) {
        factories.register(scheme, factory);
        Client.fireEvent(new ConnectionFactoryAdded(scheme, factory));
    }

    public static ConnectionFactory unregisterFactory(final String scheme) {
        final ConnectionFactory factory = factories.unregister(scheme);

        if (factory != null) {
            Client.fireEvent(new ConnectionFactoryRemoved(scheme, factory));
        }

        return factory;
    }

    public static void registerStrategy(final String scheme, final ConnectionStrategy factory) {
        strategies.register(scheme, factory);
        Client.fireEvent(new ConnectionStrategyAdded(scheme, factory));
    }

    public static ConnectionStrategy unregisterStrategy(final String scheme) {
        final ConnectionStrategy strategy = strategies.unregister(scheme);

        if (strategy != null) {
            Client.fireEvent(new ConnectionStrategyAdded(scheme, strategy));
        }

        return strategy;
    }

    /**
     * @param factory ConnectionFactory
     * @throws IOException On error
     * @deprecated Use register("default", factory);
     */
    @Deprecated
    public static void setFactory(final ConnectionFactory factory) throws IOException {
        registerFactory("default", factory);
    }

    @Log(Log.Level.SEVERE)
    public static class UnsupportedConnectionStrategyException extends IOException {
        public UnsupportedConnectionStrategyException(final String message) {
            super(message);
        }
    }

    @Log(Log.Level.SEVERE)
    public static class UnsupportedConnectionFactoryException extends IOException {
        public UnsupportedConnectionFactoryException(final String message) {
            super(message);
        }
    }
}
