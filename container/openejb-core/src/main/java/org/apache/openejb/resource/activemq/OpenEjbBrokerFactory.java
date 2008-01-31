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

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter;
import org.apache.activemq.store.memory.MemoryPersistenceAdapter;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

public class OpenEjbBrokerFactory implements BrokerFactory.BrokerFactoryHandler {
    private static final ThreadLocal<Properties> threadProperties = new ThreadLocal<Properties>();

    public static void setThreadProperties(Properties value) {
        threadProperties.set(value);
    }

    public BrokerService createBroker(URI brokerURI) throws Exception {
        URI uri = new URI(brokerURI.getRawSchemeSpecificPart());
        BrokerService broker = BrokerFactory.createBroker(uri);

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
                    Object obj = context.lookup("java:openejb/Resource/" + resouceId);
                    if (!(obj instanceof DataSource)) {
                        throw new IllegalArgumentException("Resource with id " + resouceId +
                                " is not a DataSource, but is " + obj.getClass().getName());
                    }
                    dataSource = (DataSource) obj;
                } catch (NamingException e) {
                    throw new IllegalArgumentException("Unknown datasource " + resouceId);
                }
            }

            JDBCPersistenceAdapter persistenceAdapter = new JDBCPersistenceAdapter();
            persistenceAdapter.setDataSource(dataSource);
            broker.setPersistenceAdapter(persistenceAdapter);
        } else {
            MemoryPersistenceAdapter persistenceAdapter = new MemoryPersistenceAdapter();
            broker.setPersistenceAdapter(persistenceAdapter);
        }

        return broker;
    }


    private Properties getLowerCaseProperties() {
        Properties properties = threadProperties.get();
        Properties newProperties = new Properties();
        if (properties != null) {
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                Object key = entry.getKey();
                if (key instanceof String) {
                    key = ((String) key).toLowerCase();
                }
                newProperties.put(key, entry.getValue());
            }
        }
        return newProperties;
    }
}
