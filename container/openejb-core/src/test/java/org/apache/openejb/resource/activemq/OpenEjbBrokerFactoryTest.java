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
package org.apache.openejb.resource.activemq;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;

import junit.framework.TestCase;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.network.jms.JmsConnector;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.apache.openejb.util.URISupport;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.xbean.naming.context.ImmutableContext;
import org.hsqldb.jdbc.jdbcDataSource;

public class OpenEjbBrokerFactoryTest extends TestCase {
    public void testBrokerUri() throws Exception {
        assertEquals("openejb:broker:(tcp://localhost:61616)?persistent=false",
                getBrokerUri("broker:(tcp://localhost:61616)"));
        assertEquals("openejb:broker:(tcp://localhost:61616)?useJmx=false&persistent=false",
                getBrokerUri("broker:(tcp://localhost:61616)?useJmx=false"));
        assertEquals("openejb:broker:(tcp://localhost:61616)?useJmx=false&persistent=false",
                getBrokerUri("broker:(tcp://localhost:61616)?useJmx=false&persistent=true"));
        assertEquals("openejb:broker:(tcp://localhost:61616)?useJmx=false&persistent=false",
                getBrokerUri("broker:(tcp://localhost:61616)?useJmx=false&persistent=false"));   
    }

    private String getBrokerUri(String brokerUri) throws URISyntaxException {
        URISupport.CompositeData compositeData = URISupport.parseComposite(new URI(brokerUri));
        compositeData.getParameters().put("persistent", "false");
        return "openejb:" + compositeData.toURI();
    }

    public void testBrokerDoubleCreate() throws Exception {
        BrokerService broker = BrokerFactory.createBroker(new URI(getBrokerUri( "broker:(tcp://localhost:61616)?useJmx=false")));
        stopBroker(broker);
        
        broker = BrokerFactory.createBroker(new URI(getBrokerUri("broker:(tcp://localhost:61616)?useJmx=false")));
        stopBroker(broker);

    }

    public void testNoDataSource() throws Exception {
        BrokerService broker = BrokerFactory.createBroker(new URI(getBrokerUri(
                "broker:(tcp://localhost:61616)?useJmx=false")));
        assertNotNull("broker is null", broker);

        PersistenceAdapter persistenceAdapter = broker.getPersistenceAdapter();
        assertNotNull("persistenceAdapter is null", persistenceAdapter);

        assertTrue("persistenceAdapter should be an instance of MemoryPersistenceAdapter",
                persistenceAdapter instanceof MemoryPersistenceAdapter);

        stopBroker(broker);
    }

    public void testDirectDataSource() throws Exception {
        Properties properties = new Properties();

        DataSource dataSource = new jdbcDataSource();
        properties.put("DataSource", dataSource);

        OpenEjbBrokerFactory.setThreadProperties(properties);
        BrokerService broker = null;
        try {
            broker = BrokerFactory.createBroker(new URI(getBrokerUri(
                    "broker:(tcp://localhost:61616)?useJmx=false")));
            assertNotNull("broker is null", broker);

            PersistenceAdapter persistenceAdapter = broker.getPersistenceAdapter();
            assertNotNull("persistenceAdapter is null", persistenceAdapter);

            assertTrue("persistenceAdapter should be an instance of JDBCPersistenceAdapter",
                    persistenceAdapter instanceof JDBCPersistenceAdapter);
            JDBCPersistenceAdapter jdbcPersistenceAdapter = (JDBCPersistenceAdapter)persistenceAdapter;

            assertSame(dataSource, jdbcPersistenceAdapter.getDataSource());
        } finally {
            stopBroker(broker);
            OpenEjbBrokerFactory.setThreadProperties(null);
        }
    }

    public void testLookupDataSource() throws Exception {
        Properties properties = new Properties();

        DataSource dataSource = new jdbcDataSource();
        MockInitialContextFactory.install(Collections.singletonMap("java:openejb/Resource/TestDs", dataSource));
        assertSame(dataSource, new InitialContext().lookup("java:openejb/Resource/TestDs"));

        CoreContainerSystem containerSystem = new CoreContainerSystem();
        containerSystem.getJNDIContext().bind("java:openejb/Resource/TestDs", dataSource);
        SystemInstance.get().setComponent(ContainerSystem.class, containerSystem);

        properties.put("DataSource", "TestDs");

        OpenEjbBrokerFactory.setThreadProperties(properties);
        BrokerService broker = null;
        try {
            broker = BrokerFactory.createBroker(new URI(getBrokerUri(
                    "broker:(tcp://localhost:61616)?useJmx=false")));
            assertNotNull("broker is null", broker);

            PersistenceAdapter persistenceAdapter = broker.getPersistenceAdapter();
            assertNotNull("persistenceAdapter is null", persistenceAdapter);

            assertTrue("persistenceAdapter should be an instance of JDBCPersistenceAdapter",
                    persistenceAdapter instanceof JDBCPersistenceAdapter);
            JDBCPersistenceAdapter jdbcPersistenceAdapter = (JDBCPersistenceAdapter)persistenceAdapter;

            assertSame(dataSource, jdbcPersistenceAdapter.getDataSource());
        } finally {
            stopBroker(broker);
            OpenEjbBrokerFactory.setThreadProperties(null);
        }
    }

    public static class MockInitialContextFactory implements InitialContextFactory {
        private static ImmutableContext immutableContext;

        public static void install(Map bindings) throws NamingException {
            immutableContext = new ImmutableContext(bindings);
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockInitialContextFactory.class.getName());
            new InitialContext();
        }

        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            return immutableContext;
        }
    }


    private void stopBroker(BrokerService broker) throws Exception {
        if (broker == null) return;

        if (broker.getJmsBridgeConnectors() != null) {
            for (JmsConnector connector : broker.getJmsBridgeConnectors()) {
                connector.stop();
            }
        }
        for (Object o : broker.getTransportConnectors()) {
            TransportConnector tc = (TransportConnector) o;
            tc.stop();

        }
        broker.stop();
    }

}
