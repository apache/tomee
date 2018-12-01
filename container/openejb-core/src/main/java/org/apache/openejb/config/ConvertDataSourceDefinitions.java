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

package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.jee.DataSource;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.KeyedCollection;
import org.apache.openejb.jee.Property;
import org.apache.openejb.util.PropertyPlaceHolderHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ConvertDataSourceDefinitions extends BaseConvertDefinitions {

    @Override
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {

        final List<JndiConsumer> jndiConsumers = collectConsumers(appModule);

        final KeyedCollection<String, DataSource> dataSources = new KeyedCollection<>();
        final KeyedCollection<String, DataSource> dataSourcesFromCompManagedBeans = new KeyedCollection<>();

        for (final JndiConsumer consumer : jndiConsumers) {
            if (consumer == null) {
                continue;
            }

            if (consumer instanceof org.apache.openejb.config.CompManagedBean) {
                /*
                 * TOMEE-2053: It may contain invalid datasource definitions
                 * because it is never updated with content from the ejb-jar.xml
                 * Wait until all other consumers have been processed, to safely
                 * decide which data sources to transfer;
                 */

                dataSourcesFromCompManagedBeans.addAll(consumer.getDataSource());
                continue;
            }
            dataSources.addAll(consumer.getDataSource());
        }

        final Map<String, DataSource> dataSourcesMap = dataSources.toMap();
        for(DataSource dataSource : dataSourcesFromCompManagedBeans){
            //Interested only in DataSources that come from non-JndiConsumers
            if(!dataSourcesMap.containsKey(dataSource.getKey())){
                dataSources.add(dataSource);
            }
        }

        for (final DataSource dataSource : dataSources) {
            appModule.getResources().add(toResource(dataSource));
        }
        return appModule;
    }


    private Resource toResource(final DataSource datasource) {
        final String name = cleanUpName(datasource.getName());

        final Resource def = new Resource(name, javax.sql.DataSource.class.getName());

        def.setJndi(datasource.getName().replaceFirst("java:", ""));
        def.setType("javax.sql.DataSource");

        final Properties p = def.getProperties();
        put(p, "JtaManaged", datasource.getTransactional());
        put(p, "InitialSize", datasource.getInitialPoolSize());
        put(p, "DefaultIsolationLevel", datasource.getIsolationLevel());
        put(p, "LoginTimeout", datasource.getLoginTimeout());
        put(p, "MinEvictableIdleTimeMillis", datasource.getMaxIdleTime());
        put(p, "MaxIdle", datasource.getMaxPoolSize());
        put(p, "MinIdle", datasource.getMinPoolSize());
        put(p, "MaxStatements", datasource.getMaxStatements());
        put(p, "Password", datasource.getPassword());
        put(p, "JdbcUrl", datasource.getUrl());
        put(p, "UserName", datasource.getUser());
        put(p, "JdbcDriver", datasource.getClassName());
        put(p, "PortNumber", datasource.getPortNumber());
        put(p, "DatabaseName", datasource.getDatabaseName());
        put(p, "Description", datasource.getDescription());
        put(p, "ServerName", datasource.getServerName());
        put(p, "Definition", rawDefinition(datasource)); // do we need it??
        put(p, AutoConfig.ORIGIN_FLAG, AutoConfig.ORIGIN_ANNOTATION);

        setProperties(datasource, p);

        // to force it to be bound in JndiEncBuilder
        put(p, "JndiName", def.getJndi());

        return def;
    }

    private String rawDefinition(final DataSource d) {
        try {
            final Properties p = new Properties();

            put(p, "transactional", d.getTransactional());
            put(p, "initialPoolSize", d.getInitialPoolSize());
            put(p, "isolationLevel", d.getIsolationLevel());
            put(p, "loginTimeout", d.getLoginTimeout());
            put(p, "maxIdleTime", d.getMaxIdleTime());
            put(p, "maxPoolSize", d.getMaxPoolSize());
            put(p, "maxStatements", d.getMaxStatements());
            put(p, "minPoolSize", d.getMinPoolSize());
            put(p, "portNumber", d.getPortNumber());
            put(p, "databaseName", d.getDatabaseName());
            put(p, "password", d.getPassword());
            put(p, "serverName", d.getServerName());
            put(p, "url", d.getUrl());
            put(p, "user", d.getUser());

            setProperties(d, p);

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            p.store(out, "");
            return new String(out.toByteArray());
        } catch (final IOException e) {
            throw new OpenEJBRuntimeException(String.format("Cannot canonicalize the @DataSourceDefinition %s as a properties string", d.getName()));
        }
    }

    private void setProperties(final DataSource d, final Properties p) {
        for (final Property property : d.getProperty()) {

            final String key = property.getName();
            final String value = property.getValue();

            put(p, key, value);
        }
    }

    private static void put(final Properties properties, final String key, final Object value) {
        if (key == null) {
            return;
        }
        if (value == null) {
            return;
        }

        properties.put(key, PropertyPlaceHolderHelper.value(String.valueOf(value)));
    }
}
