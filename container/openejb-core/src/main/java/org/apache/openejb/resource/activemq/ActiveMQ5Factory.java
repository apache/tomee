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
package org.apache.openejb.resource.activemq;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerFactoryHandler;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ActiveMQ5Factory implements BrokerFactoryHandler {

    private static final ThreadLocal<Properties> threadProperties = new ThreadLocal<Properties>();
    private static final Map<URI,BrokerService> brokers = new HashMap<URI, BrokerService>();
    private static Throwable throwable = null;

    public static void setThreadProperties(Properties value) {
        threadProperties.set(value);
    }

    @Override
    public synchronized BrokerService createBroker(final URI brokerURI) throws Exception {

        BrokerService broker = brokers.get(brokerURI);

        if (null == broker || !broker.isStarted()) {

            final URI uri = new URI(brokerURI.getRawSchemeSpecificPart());
            broker = BrokerFactory.createBroker(uri);
            brokers.put(brokerURI, broker);

            if (!uri.getScheme().toLowerCase().startsWith("xbean")) {

                Properties properties = getLowerCaseProperties();

                Object value = properties.get("datasource");
                if (value instanceof String && value.toString().length() == 0) {
                    value = null;
                }

                if (value != null) {
                    DataSource dataSource;
                    if (value instanceof DataSource) {
                        dataSource = (DataSource) value;
                    } else {
                        String resouceId = (String) value;

                        try {
                            ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
                            Context context = containerSystem.getJNDIContext();
                            Object obj = context.lookup("openejb/Resource/" + resouceId);
                            if (!(obj instanceof DataSource)) {
                                throw new IllegalArgumentException("Resource with id " + resouceId
                                                                   + " is not a DataSource, but is " + obj.getClass().getName());
                            }
                            dataSource = (DataSource) obj;
                        } catch (NamingException e) {
                            throw new IllegalArgumentException("Unknown datasource " + resouceId);
                        }
                    }

                    JDBCPersistenceAdapter persistenceAdapter = new JDBCPersistenceAdapter();

                    if (properties.containsKey("usedatabaselock")) {
                        //This must be false for hsqldb
                        persistenceAdapter.setUseDatabaseLock(Boolean.parseBoolean(properties.getProperty("usedatabaselock", "true")));
                    }

                    persistenceAdapter.setDataSource(dataSource);
                    broker.setPersistenceAdapter(persistenceAdapter);
                } else {
                    MemoryPersistenceAdapter persistenceAdapter = new MemoryPersistenceAdapter();
                    broker.setPersistenceAdapter(persistenceAdapter);
                }

                try {
                    //TODO - New in 5.4.x
                    broker.setSchedulerSupport(false);
                } catch (Throwable t) {
                    //Ignore
                }

                //Notify when an error occurs on shutdown.
                broker.setUseLoggingForShutdownErrors(org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").isErrorEnabled());
            }

            //We must close the broker
            broker.setUseShutdownHook(false);
            broker.setSystemExitOnShutdown(false);

            if (!broker.isStarted()) {

                final BrokerService bs = broker;

                final Thread start = new Thread("ActiveMQFactory start and checkpoint") {

                    @Override
                    public void run() {
                        try {
                            //Start before returning - this is known to be safe.
                            bs.start();
                            bs.waitUntilStarted();

                            //Force a checkpoint to initialize pools
                            bs.getPersistenceAdapter().checkpoint(true);
                        } catch (Throwable t) {
                            throwable = t;
                        }
                    }
                };

                start.setDaemon(true);
                start.start();

                try {
                    start.join(5000);
                } catch (InterruptedException e) {
                    //Ignore
                }

                if (null != throwable) {
                    org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources").error("ActiveMQ failed to start within 5 seconds - It may not be usable", throwable);
                }
            }
        }

        return broker;
    }

    private Properties getLowerCaseProperties() {
        final Properties properties = threadProperties.get();
        final Properties newProperties = new Properties();
        if (properties != null) {
            Object key;
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                key = entry.getKey();
                if (key instanceof String) {
                    key = ((String) key).toLowerCase();
                }
                newProperties.put(key, entry.getValue());
            }
        }
        return newProperties;
    }

    public Collection<BrokerService> getBrokers() {
        return brokers.values();
    }
}
