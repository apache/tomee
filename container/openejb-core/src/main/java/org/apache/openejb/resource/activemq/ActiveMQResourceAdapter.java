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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.ra.ActiveMQConnectionRequestInfo;
import org.apache.activemq.ra.MessageActivationSpec;
import org.apache.openejb.resource.activemq.jms2.TomEEConnectionFactory;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URISupport;
import org.apache.openejb.util.URLs;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnusedDeclaration")
public class ActiveMQResourceAdapter extends org.apache.activemq.ra.ActiveMQResourceAdapter {

    private String dataSource;
    private String useDatabaseLock;
    private String startupTimeout = "60000";
    private BootstrapContext bootstrapContext;

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(final String dataSource) {
        this.dataSource = dataSource;
    }

    public void setUseDatabaseLock(final String useDatabaseLock) {
        this.useDatabaseLock = useDatabaseLock;
    }

    public int getStartupTimeout() {
        return Integer.parseInt(this.startupTimeout);
    }

    public void setStartupTimeout(final Duration startupTimeout) {
        if (startupTimeout.getUnit() == null) {
            startupTimeout.setUnit(TimeUnit.MILLISECONDS);
        }
        this.startupTimeout = String.valueOf(TimeUnit.MILLISECONDS.convert(startupTimeout.getTime(), startupTimeout.getUnit()));
    }

    @Override
    public void setServerUrl(final String url) {
        super.setServerUrl(url);
    }

    @Override
    public void start(final BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {

        this.bootstrapContext = bootstrapContext;
        final String brokerXmlConfig = getBrokerXmlConfig();
        super.setBrokerXmlConfig(null);
        super.start(bootstrapContext);

        final Properties properties = new Properties();

        if (null != this.dataSource) {
            properties.put("DataSource", this.dataSource);
        }

        if (null != this.useDatabaseLock) {
            properties.put("UseDatabaseLock", this.useDatabaseLock);
        }

        if (null != this.startupTimeout) {
            properties.put("StartupTimeout", this.startupTimeout);
        }

        // prefix server uri with 'broker:' so our broker factory is used
        if (brokerXmlConfig != null && !brokerXmlConfig.trim().isEmpty()) {

            try {

                if (brokerXmlConfig.startsWith("broker:")) {

                    final URISupport.CompositeData compositeData = URISupport.parseComposite(URLs.uri(brokerXmlConfig));

                    if (!compositeData.getParameters().containsKey("persistent")) {
                        //Override default - Which is 'true'
                        //noinspection unchecked
                        compositeData.getParameters().put("persistent", "false");
                    }

                    if ("false".equalsIgnoreCase(compositeData.getParameters().get("persistent").toString())) {
                        properties.remove("DataSource"); // no need
                    }

                    setBrokerXmlConfig(ActiveMQFactory.getBrokerMetaFile() + compositeData.toURI());
                } else if (brokerXmlConfig.toLowerCase(Locale.ENGLISH).startsWith("xbean:")) {
                    setBrokerXmlConfig(ActiveMQFactory.getBrokerMetaFile() + brokerXmlConfig);
                }

            } catch (final URISyntaxException e) {
                throw new ResourceAdapterInternalException("Invalid BrokerXmlConfig", e);
            }

            createInternalBroker(brokerXmlConfig, properties);
        }
    }

    private void createInternalBroker(final String brokerXmlConfig, final Properties properties) {
        ActiveMQFactory.setThreadProperties(properties);

        try {
            //The returned broker should be started, but calling start is harmless.
            //We do not need to track the instance as the factory takes care of this.
            ActiveMQFactory.createBroker(URLs.uri(getBrokerXmlConfig())).start();
        } catch (final Exception e) {
            Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQResourceAdapter.class).getChildLogger("service").fatal("Failed to start ActiveMQ", e);
        } finally {
            ActiveMQFactory.setThreadProperties(null);

            // reset brokerXmlConfig
            if (brokerXmlConfig != null) {
                setBrokerXmlConfig(brokerXmlConfig);
            }
        }
    }

    @Override
    public BootstrapContext getBootstrapContext() {
        return this.bootstrapContext;
    }

    @Override
    public void stop() {

        Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQResourceAdapter.class).getChildLogger("service").info("Stopping ActiveMQ");

        final Thread stopThread = new Thread("ActiveMQResourceAdapter stop") {

            @Override
            public void run() {
                try {
                    stopImpl();
                } catch (final Throwable t) {
                    Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQResourceAdapter.class).getChildLogger("service").error("ActiveMQ shutdown failed", t);
                }
            }
        };

        stopThread.setDaemon(true);
        stopThread.start();

        int timeout = 60000;

        try {
            timeout = Integer.parseInt(this.startupTimeout);
        } catch (final Throwable e) {
            //Ignore
        }

        try {
            //Block for a maximum of timeout milliseconds waiting for this thread to die.
            stopThread.join(timeout);
        } catch (final InterruptedException ex) {
            Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQResourceAdapter.class).getChildLogger("service").warning("Gave up on ActiveMQ shutdown after " + timeout + "ms", ex);
        }
    }

    @Override
    protected ActiveMQConnectionFactory createConnectionFactory(final ActiveMQConnectionRequestInfo connectionRequestInfo, final MessageActivationSpec activationSpec) {
        final ActiveMQConnectionFactory factory = new TomEEConnectionFactory();
        connectionRequestInfo.configure(factory, activationSpec);
        return factory;
    }

    private void stopImpl() throws Exception {
        super.stop();
        final Collection<BrokerService> brokers = ActiveMQFactory.getBrokers();
        final Iterator<BrokerService> it = brokers.iterator();
        while (it.hasNext()) {
            final BrokerService bs = it.next();
            try {
                bs.stop();
                bs.waitUntilStopped();
            } catch (final Throwable t) {
                //Ignore
            }
            it.remove();
        }
        stopScheduler();
        Logger.getInstance(LogCategory.OPENEJB_STARTUP, ActiveMQResourceAdapter.class).getChildLogger("service").info("Stopped ActiveMQ broker");
    }

    private static void stopScheduler() {
        try {
            final Class<?> clazz = Class.forName("org.apache.kahadb.util.Scheduler");
            final Method method = clazz.getMethod("shutdown");
            method.invoke(null);
        } catch (final Throwable e) {
            //Ignore
        }
    }
}
