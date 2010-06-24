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

import org.apache.activemq.broker.BrokerService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Properties;

public class ActiveMQFactory {

    private static final Method setThreadProperties;
    private static final Method createBroker;
    private static final Object instance;

    private static Class clazz;
    private static String brokerPrefix;

    static {

        try {
            clazz = Class.forName("org.apache.openejb.resource.activemq.ActiveMQ5Factory");
            brokerPrefix = "amq5factory:";
        } catch (java.lang.Throwable t1) {
            try {
                clazz = Class.forName("org.apache.openejb.resource.activemq.ActiveMQ4Factory");
                brokerPrefix = "amq4factory:";
            } catch (java.lang.Throwable t2) {
                    throw new RuntimeException("Unable to load ActiveMQFactory: Check ActiveMQ jar files are on classpath", t1);
            }
        }

        try {
            instance = clazz.newInstance();
        } catch (InstantiationException e) {
                throw new RuntimeException("Unable to create ActiveMQFactory instance", e);
        } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to access ActiveMQFactory instance", e);
        }

        try {
            setThreadProperties = clazz.getDeclaredMethod("setThreadProperties", new Class[]{Properties.class});
        } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unable to create ActiveMQFactory setThreadProperties method", e);
        }

        try {
            createBroker = clazz.getDeclaredMethod("createBroker", new Class[]{URI.class});
        } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unable to create ActiveMQFactory setThreadProperties method", e);
        }
    }

    /**
     * Returns the prefix metafile name of the poperties file that ActiveMQ should be
     * provided with. This file is located at META-INF/services/org/apache/activemq/broker/
     * and defines the BrokerFactoryHandler to load.
     * @return String name - will be either 'amq5factory:' or 'amq4factory:' - note the trailing ':'
     */
    public static String getBrokerMetaFile() {
        return brokerPrefix;
    }

    public static void setThreadProperties(final Properties p) {
        try {
            setThreadProperties.invoke(instance, p);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("ActiveMQFactory.setThreadProperties.IllegalAccessException", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("ActiveMQFactory.setThreadProperties.IllegalArgumentException", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("ActiveMQFactory.setThreadProperties.InvocationTargetException", e);
        }
    }

    public BrokerService createBroker(final URI brokerURI) throws Exception {
        try {
            return (BrokerService) createBroker.invoke(instance, brokerURI);
        } catch (IllegalAccessException e) {
            throw new Exception("ActiveMQFactory.createBroker.IllegalAccessException", e);
        } catch (IllegalArgumentException e) {
            throw new Exception("ActiveMQFactory.createBroker.IllegalArgumentException", e);
        } catch (InvocationTargetException e) {
            throw new Exception("ActiveMQFactory.createBroker.InvocationTargetException", e);
        }
    }
}
