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

import org.apache.activemq.broker.BrokerService;
import org.apache.openejb.OpenEJBRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActiveMQFactory {

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static Method setThreadProperties;
    private static Method createBroker;
    private static Method getBrokers;
    private static Object instance;
    private static String brokerPrefix;

    private static void init() {

        synchronized (initialized) {

            if (!initialized.getAndSet(true)) {

                Class tmp;

                try {
                    tmp = Class.forName("org.apache.openejb.resource.activemq.ActiveMQ5Factory");
                    brokerPrefix = "amq5factory:";
                } catch (final Throwable t1) {
                    try {
                        tmp = Class.forName("org.apache.openejb.resource.activemq.ActiveMQ4Factory");
                        brokerPrefix = "amq4factory:";
                    } catch (final Throwable t2) {
                        throw new OpenEJBRuntimeException("Unable to load ActiveMQFactory: Check ActiveMQ jar files are on classpath", t1);
                    }
                }

                final Class clazz = tmp;

                try {
                    instance = clazz.newInstance();
                } catch (final InstantiationException e) {
                    throw new OpenEJBRuntimeException("Unable to create ActiveMQFactory instance", e);
                } catch (final IllegalAccessException e) {
                    throw new OpenEJBRuntimeException("Unable to access ActiveMQFactory instance", e);
                }

                try {
                    setThreadProperties = clazz.getDeclaredMethod("setThreadProperties", new Class[]{Properties.class});
                } catch (final NoSuchMethodException e) {
                    throw new OpenEJBRuntimeException("Unable to create ActiveMQFactory setThreadProperties method", e);
                }

                try {
                    createBroker = clazz.getDeclaredMethod("createBroker", new Class[]{URI.class});
                } catch (final NoSuchMethodException e) {
                    throw new OpenEJBRuntimeException("Unable to create ActiveMQFactory createBroker method", e);
                }

                try {
                    getBrokers = clazz.getDeclaredMethod("getBrokers", (Class[]) null);
                } catch (final NoSuchMethodException e) {
                    throw new OpenEJBRuntimeException("Unable to create ActiveMQFactory createBroker method", e);
                }
            }
        }
    }

    /**
     * Returns the prefix metafile name of the poperties file that ActiveMQ should be
     * provided with. This file is located at META-INF/services/org/apache/activemq/broker/
     * and defines the BrokerFactoryHandler to load.
     *
     * @return String name - will be either 'amq5factory:' or 'amq4factory:' - note the trailing ':'
     */
    public static String getBrokerMetaFile() {
        ActiveMQFactory.init();
        return brokerPrefix;
    }

    public static void setThreadProperties(final Properties p) {

        ActiveMQFactory.init();

        try {
            setThreadProperties.invoke(instance, p);
        } catch (final IllegalAccessException e) {
            throw new OpenEJBRuntimeException("ActiveMQFactory.setThreadProperties.IllegalAccessException", e);
        } catch (final IllegalArgumentException e) {
            throw new OpenEJBRuntimeException("ActiveMQFactory.setThreadProperties.IllegalArgumentException", e);
        } catch (final InvocationTargetException e) {
            throw new OpenEJBRuntimeException("ActiveMQFactory.setThreadProperties.InvocationTargetException", e);
        }
    }

    public static BrokerService createBroker(final URI brokerURI) throws Exception {

        ActiveMQFactory.init();

        try {
            return (BrokerService) createBroker.invoke(instance, brokerURI);
        } catch (final IllegalAccessException e) {
            throw new Exception("ActiveMQFactory.createBroker.IllegalAccessException", e);
        } catch (final IllegalArgumentException e) {
            throw new Exception("ActiveMQFactory.createBroker.IllegalArgumentException", e);
        } catch (final InvocationTargetException e) {
            throw new Exception("ActiveMQFactory.createBroker.InvocationTargetException", e);
        }
    }

    /**
     * Returns a map of configured brokers.
     * This intended for access upon RA shutdown in order to wait for the brokers to finish.
     *
     * @return Map(URI, BrokerService)
     * @throws Exception On error
     */
    public static Collection<BrokerService> getBrokers() throws Exception {

        ActiveMQFactory.init();

        try {
            //noinspection unchecked
            return (Collection<BrokerService>) getBrokers.invoke(instance, (Object[]) null);
        } catch (final IllegalAccessException e) {
            throw new Exception("ActiveMQFactory.createBroker.IllegalAccessException", e);
        } catch (final IllegalArgumentException e) {
            throw new Exception("ActiveMQFactory.createBroker.IllegalArgumentException", e);
        } catch (final InvocationTargetException e) {
            throw new Exception("ActiveMQFactory.createBroker.InvocationTargetException", e);
        }
    }
}
