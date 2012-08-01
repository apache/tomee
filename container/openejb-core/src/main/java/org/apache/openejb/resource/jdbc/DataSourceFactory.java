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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.resource.jdbc.pool.DataSourceCreator;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * @version $Rev$ $Date$
 */
public class DataSourceFactory {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, DataSourceFactory.class);

    public static final String POOL_PROPERTY = "openejb.datasource.pool";
    public static final String DATA_SOURCE_CREATOR_PROP = "DataSourceCreator";

    private static final Map<DataSource, DataSourceCreator> creatorByDataSource = new HashMap<DataSource, DataSourceCreator>();
    private static final Map<String, String> KNOWN_CREATORS = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER){{
        put("dbcp", "org.apache.openejb.resource.jdbc.pool.DefaultDataSourceCreator"); // the original one
        put("dbcp-alternative", "org.apache.openejb.resource.jdbc.dbcp.DbcpDataSourceCreator"); // dbcp for the ds pool only
        put("tomcat", "org.apache.tomee.jdbc.TomEEDataSourceCreator"); // tomee
        put("bonecp", "org.apache.openejb.bonecp.BoneCPDataSourceCreator"); // bonecp
    }};

    public static DataSource create(final String name, final boolean managed, final Class impl, final String definition) throws IllegalAccessException, InstantiationException, IOException {
        final Properties properties = asProperties(definition);

        // these can be added and are managed by OpenEJB and not the DataSource itself
        properties.remove("Definition");
        properties.remove("JtaManaged");
        properties.remove("ServiceId");

        final DataSourceCreator creator = creator(properties.remove(DATA_SOURCE_CREATOR_PROP));


        final DataSource ds;
        if (createDataSourceFromClass(impl)) { // opposed to "by driver"
            trimNotSupportedDataSourceProperties(properties);

            final ObjectRecipe recipe = new ObjectRecipe(impl);
            recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
            recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            recipe.allow(Option.NAMED_PARAMETERS);
            recipe.setAllProperties(properties);

            final DataSource dataSource = (DataSource) recipe.create();

            if (managed) {
                if (useDbcp(properties)) {
                    ds = creator.poolManaged(name, dataSource, properties);
                } else {
                    ds = creator.managed(name, dataSource);
                }
            } else {
                if (useDbcp(properties)) {
                    ds = creator.pool(name, dataSource, properties);
                } else {
                    ds = dataSource;
                }
            }
        } else { // by driver
            if (managed) {
                final XAResourceWrapper xaResourceWrapper = SystemInstance.get().getComponent(XAResourceWrapper.class);
                if (xaResourceWrapper != null) {
                    ds = creator.poolManagedWithRecovery(name, xaResourceWrapper, impl.getName(), properties);
                } else {
                    ds = creator.poolManaged(name, impl.getName(), properties);
                }
            } else {
                ds = creator.pool(name, impl.getName(), properties);
            }
        }

        creatorByDataSource.put(ds, creator);
        return ds;
    }

    public static DataSourceCreator creator(final Object creatorName) {
        final DataSourceCreator defaultCreator = SystemInstance.get().getComponent(DataSourceCreator.class);
        if (creatorName != null && creatorName instanceof String
                && (defaultCreator == null || !creatorName.equals(defaultCreator.getClass().getName()))) {
            String clazz = KNOWN_CREATORS.get(creatorName);
            if (clazz == null) {
                clazz = (String) creatorName;
            }
            try {
                return (DataSourceCreator) Thread.currentThread().getContextClassLoader().loadClass(clazz).newInstance();
            } catch (Throwable e) {
                LOGGER.error("can't create '" + creatorName + "', the default one will be used: " + defaultCreator, e);
            }
        }
        return defaultCreator;
    }

    private static boolean createDataSourceFromClass(final Class<?> impl) {
        return DataSource.class.isAssignableFrom(impl) && !SystemInstance.get().getOptions().get("org.apache.openejb.resource.jdbc.hot.deploy", false);
    }

    private static boolean useDbcp(final Properties properties) {
        return "true".equalsIgnoreCase(properties.getProperty(POOL_PROPERTY, "true"));
    }

    private static Properties asProperties(final String definition) throws IOException {
        return IO.readProperties(IO.read(definition), new Properties());
    }

    public static void trimNotSupportedDataSourceProperties(Properties properties) {
        properties.remove("LoginTimeout");
    }

    public static boolean knows(final Object object) {
        return object instanceof DataSource && creatorByDataSource.containsKey(object);
    }

    // TODO: should we get a get and a clear method instead of a single one?
    public static ObjectRecipe forgetRecipe(final Object object, final ObjectRecipe defaultValue) {
        final DataSourceCreator creator = creatorByDataSource.get(object);
        ObjectRecipe recipe = null;
        if (creator != null) {
            recipe = creator.clearRecipe(object);
        }
        if (recipe == null) {
            return defaultValue;
        }
        return recipe;
    }

    public static void destroy(final Object o) throws Throwable {
        creatorByDataSource.remove(o).destroy(o);
    }
}
