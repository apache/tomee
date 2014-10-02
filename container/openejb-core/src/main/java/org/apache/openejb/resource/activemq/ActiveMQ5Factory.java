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
import org.apache.activemq.ra.ActiveMQResourceAdapter;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.PersistenceAdapterFactory;
import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.apache.activemq.util.URISupport;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.propertyeditor.PropertyEditorException;
import org.apache.xbean.propertyeditor.PropertyEditors;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActiveMQ5Factory implements BrokerFactoryHandler {

    private static Properties properties;
    private static final Map<URI, BrokerService> brokers = new HashMap<URI, BrokerService>();
    private static Throwable throwable;
    private static final AtomicBoolean started = new AtomicBoolean(false);

    public static void setThreadProperties(final Properties p) {
        properties = p;
    }

    @Override
    public synchronized BrokerService createBroker(final URI brokerURI) throws Exception {

        Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQ5Factory.class).getChildLogger("service").info("ActiveMQ5Factory creating broker");

        BrokerService broker = brokers.get(brokerURI);

        if (null == broker || !broker.isStarted()) {

            final Properties properties = getLowerCaseProperties();

            final URISupport.CompositeData compositeData = URISupport.parseComposite(new URI(brokerURI.getRawSchemeSpecificPart()));
            final Map<String, String> params = new HashMap<String, String>(compositeData.getParameters());
            final PersistenceAdapter persistenceAdapter;
            if ("true".equals(params.remove("usekahadb"))) {
                persistenceAdapter = createPersistenceAdapter("org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter", "kahadb", params);
            } else if ("true".equals(params.remove("useleveldb"))) {
                persistenceAdapter = createPersistenceAdapter("org.apache.activemq.store.leveldb.LevelDBPersistenceAdapter", "leveldb", params);
            } else if (params.get("persistenceadapter") != null) {
                final String adapter = params.remove("persistenceadapter");
                persistenceAdapter = createPersistenceAdapter(adapter, "persistence", params);
            } else {
                persistenceAdapter = null;
            }

            final URI uri = new URI(cleanUpUri(brokerURI.getSchemeSpecificPart(), compositeData.getParameters(), params));
            broker = BrokerFactory.createBroker(uri);
            brokers.put(brokerURI, broker);

            if (persistenceAdapter != null) {
                broker.setPersistenceAdapter(persistenceAdapter);
                // if user didn't set persistent to true then setPersistenceAdapter() alone is ignored so forcing it with the factory
                broker.setPersistenceFactory(new ProvidedPersistenceAdapterPersistenceAdapterFactory(persistenceAdapter));
                broker.setPersistent(true);
                tomeeConfig(broker);
            } else {
                final boolean notXbean = !uri.getScheme().toLowerCase().startsWith("xbean");
                if (notXbean) {

                    Object value = properties.get("datasource");

                    if (String.class.isInstance(value) && value.toString().length() == 0) {
                        value = null;
                    }

                    final DataSource dataSource;

                    if (value != null) {

                        if (DataSource.class.isInstance(value)) {
                            dataSource = DataSource.class.cast(value);
                        } else if (String.class.isInstance(value)) {
                            final String resouceId = String.class.cast(value);

                            try {
                                final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
                                final Context context = containerSystem.getJNDIContext();
                                final Object obj = context.lookup("openejb/Resource/" + resouceId);
                                if (!(obj instanceof DataSource)) {
                                    throw new IllegalArgumentException("Resource with id " + resouceId
                                        + " is not a DataSource, but is " + obj.getClass().getName());
                                }
                                dataSource = (DataSource) obj;
                            } catch (final NamingException e) {
                                throw new IllegalArgumentException("Unknown datasource " + resouceId);
                            }
                        } else {
                            throw new IllegalArgumentException("Unexpected datasource definition: " + value);
                        }

                    } else {
                        dataSource = null;
                    }

                    if (null != dataSource) {
                        final JDBCPersistenceAdapter adapter = new JDBCPersistenceAdapter();

                        if (properties.containsKey("usedatabaselock")) {
                            //This must be false for hsqldb
                            adapter.setUseLock(Boolean.parseBoolean(properties.getProperty("usedatabaselock", "true")));
                        }

                        adapter.setDataSource(dataSource);
                        broker.setPersistent(true);
                        broker.setPersistenceAdapter(adapter);
                    } else {
                        broker.setPersistenceAdapter(new MemoryPersistenceAdapter());
                    }

                    tomeeConfig(broker);
                }
            }

            //We must close the broker
            broker.setUseShutdownHook(false);
            broker.setSystemExitOnShutdown(false);

            broker.setStartAsync(false);

            final BrokerService bs = broker;

            final Thread start = new Thread("ActiveMQFactory start and checkpoint") {

                @Override
                public void run() {

                    Thread.currentThread().setContextClassLoader(ActiveMQResourceAdapter.class.getClassLoader());

                    try {
                        //Start before returning - this is known to be safe.
                        if (!bs.isStarted()) {
                            Logger
                                .getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQ5Factory.class)
                                .getChildLogger("service")
                                .info("Starting ActiveMQ BrokerService");
                            bs.start();
                        }

                        bs.waitUntilStarted();

                        //Force a checkpoint to initialize pools
                        Logger
                            .getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQ5Factory.class)
                            .getChildLogger("service")
                            .info("Starting ActiveMQ checkpoint");
                        bs.getPersistenceAdapter().checkpoint(true);
                        started.set(true);

                    } catch (final Throwable t) {
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

            int timeout = 30000;

            try {
                timeout = Integer.parseInt(properties.getProperty("startuptimeout", "30000"));
                Logger
                    .getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQ5Factory.class)
                    .getChildLogger("service")
                    .info("Using ActiveMQ startup timeout of " + timeout + "ms");
            } catch (final Throwable e) {
                //Ignore
            }

            start.setDaemon(true);
            start.start();

            try {
                start.join(timeout);
            } catch (final InterruptedException e) {
                //Ignore
            }

            if (null != throwable) {
                Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQ5Factory.class).getChildLogger("service").error("ActiveMQ failed to start broker",
                    throwable);
            } else if (started.get()) {
                Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQ5Factory.class).getChildLogger("service").info("ActiveMQ broker started");
            } else {
                Logger
                    .getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQ5Factory.class)
                    .getChildLogger("service")
                    .warning("ActiveMQ failed to start broker within " + timeout + " seconds - It may be unusable");
            }

        }

        return broker;
    }

    private static String cleanUpUri(final String schemeSpecificPart, final Map<String, String> parameters, final Map<String, String> params) {
        String uri = schemeSpecificPart;
        for (final Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!params.containsKey(entry.getKey())) {
                final String kv = entry.getKey() + "=" + entry.getValue();
                final int idx = uri.indexOf(kv);
                if (idx >= 0) {
                    final int andIdx = idx + kv.length();
                    if (andIdx < uri.length() && uri.charAt(andIdx) == '&') {
                        uri = uri.replace(kv + "&", "");
                    } else {
                        uri = uri.replace(kv, "");
                    }
                }
            }
        }
        return uri;
    }

    private static PersistenceAdapter createPersistenceAdapter(final String clazz, final String prefix, final Map<String, String> params) throws IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        final PersistenceAdapter persistenceAdapter = PersistenceAdapter.class.cast(Thread.currentThread().getContextClassLoader().loadClass(clazz).newInstance());
        for (final Method m : KahaDBPersistenceAdapter.class.getDeclaredMethods()) {
            if (m.getName().startsWith("set") && m.getParameterTypes().length == 1 && Modifier.isPublic(m.getModifiers())) {
                final String key = prefix + "." + m.getName().substring(3).toLowerCase(Locale.ENGLISH);
                final Object field = params.remove(key);
                if (field != null) {
                    try {
                        final Object toSet = PropertyEditors.getValue(m.getParameterTypes()[0], field.toString());
                        m.invoke(persistenceAdapter, toSet);
                    } catch (final PropertyEditorException cantConvertException) {
                        throw new IllegalArgumentException("can't convert " + field + " for " + m.getName(), cantConvertException);
                    }
                }
            }
        }
        return persistenceAdapter;
    }

    private void tomeeConfig(final BrokerService broker) {
        //New since 5.4.x
        disableScheduler(broker);

        //Notify when an error occurs on shutdown.
        broker.setUseLoggingForShutdownErrors(Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQ5Factory.class).isErrorEnabled());
    }

    private static void disableScheduler(final BrokerService broker) {
        try {
            final Class<?> clazz = Class.forName("org.apache.activemq.broker.BrokerService");
            final Method method = clazz.getMethod("setSchedulerSupport", new Class[]{Boolean.class});
            method.invoke(broker, Boolean.FALSE);
        } catch (final Throwable e) {
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

    private static class ProvidedPersistenceAdapterPersistenceAdapterFactory implements PersistenceAdapterFactory {
        private final PersistenceAdapter instance;

        public ProvidedPersistenceAdapterPersistenceAdapterFactory(final PersistenceAdapter persistenceAdapter) {
            this.instance = persistenceAdapter;
        }

        @Override
        public PersistenceAdapter createPersistenceAdapter() throws IOException {
            return instance;
        }
    }
}
