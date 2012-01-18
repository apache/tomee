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
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActiveMQ5Factory implements BrokerFactoryHandler {

    private static Properties properties;
    private static final Map<URI, BrokerService> brokers = new HashMap<URI, BrokerService>();
    private static Throwable throwable = null;
    private static final AtomicBoolean started = new AtomicBoolean(false);

    public static void setThreadProperties(final Properties p) {
        properties = p;
    }

    @Override
    public synchronized BrokerService createBroker(final URI brokerURI) throws Exception {

        BrokerService broker = brokers.get(brokerURI);

        if (null == broker || !broker.isStarted()) {

            final Properties properties = getLowerCaseProperties();
            final URI uri = new URI(brokerURI.getRawSchemeSpecificPart());
            broker = BrokerFactory.createBroker(uri);
            brokers.put(brokerURI, broker);

            if (!uri.getScheme().toLowerCase().startsWith("xbean")) {

                Object value = properties.get("datasource");
                if (value instanceof String && value.toString().length() == 0) {
                    value = null;
                }

                if (value != null) {
                    final DataSource dataSource;
                    if (value instanceof DataSource) {
                        dataSource = (DataSource) value;
                    } else {
                        final String resouceId = (String) value;

                        try {
                            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
                            final Context context = containerSystem.getJNDIContext();
                            final Object obj = context.lookup("openejb/Resource/" + resouceId);
                            if (!(obj instanceof DataSource)) {
                                throw new IllegalArgumentException("Resource with id " + resouceId
                                        + " is not a DataSource, but is " + obj.getClass().getName());
                            }
                            dataSource = (DataSource) obj;
                        } catch (NamingException e) {
                            throw new IllegalArgumentException("Unknown datasource " + resouceId);
                        }
                    }

                    final JDBCPersistenceAdapter persistenceAdapter = new JDBCPersistenceAdapter();

                    if (properties.containsKey("usedatabaselock")) {
                        //This must be false for hsqldb
                        persistenceAdapter.setUseDatabaseLock(Boolean.parseBoolean(properties.getProperty("usedatabaselock", "true")));
                    }

                    persistenceAdapter.setDataSource(dataSource);
                    broker.setPersistenceAdapter(persistenceAdapter);
                } else {
                    final MemoryPersistenceAdapter persistenceAdapter = new MemoryPersistenceAdapter();
                    broker.setPersistenceAdapter(persistenceAdapter);
                }

                //New since 5.4.x
                disableScheduler(broker);

                //Notify when an error occurs on shutdown.
                broker.setUseLoggingForShutdownErrors(org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources").isErrorEnabled());
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
                            started.set(true);
                        } catch (Throwable t) {
                            throwable = t;
                        }
                    }
                };

                /*
                 * An application may require immediate access to JMS. So we need to block here until the service
                 * has started. How long ActiveMQ requires to actually create a broker is unpredictable.
                 *
                 * A broker in OpenEJB is usually a wrapper for an embedded ActiveMQ server service. The broker configuration
                 * allows the definition of a remote ActiveMQ server, in which case startup is not an issue as the broker is
                 * basically a client.
                 *
                 * If the broker is local and the message store contains millions of messages then the startup time is obviously going to
                 * be longer as these need to be indexed by ActiveMQ.
                 *
                 * A balanced timeout will always be use case dependent.
                */

                int timeout = 60000;

                try {
                    timeout = Integer.parseInt(properties.getProperty("startuptimeout", "60000"));
                    org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources").info("Using ActiveMQ startup timeout of " + timeout + "ms");
                } catch (Throwable e) {
                    //Ignore
                }

                start.setDaemon(true);
                start.start();

                try {
                    start.join(timeout);
                } catch (InterruptedException e) {
                    //Ignore
                }

                if (null != throwable) {
                    org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources").error("ActiveMQ failed to start broker", throwable);
                } else if (started.get()) {
                    org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources").info("ActiveMQ broker started");
                } else {
                    org.apache.openejb.util.Logger.getInstance(LogCategory.OPENEJB_STARTUP, "org.apache.openejb.util.resources").warning("ActiveMQ failed to start broker within " + timeout + " seconds - It may be unusable");
                }
            }
        }

        return broker;
    }

    private static void disableScheduler(final BrokerService broker) {
        try {
            final Class<?> clazz = Class.forName("org.apache.activemq.broker.BrokerService");
            final Method method = clazz.getMethod("setSchedulerSupport", new Class[]{Boolean.class});
            method.invoke(broker, Boolean.FALSE);
        } catch (Throwable e) {
            //Ignore
        }
    }

    private Properties getLowerCaseProperties() {
        final Properties newProperties = new Properties();
        if (properties != null) {
            Object key;
            for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
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
