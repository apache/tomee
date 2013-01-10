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
import org.apache.openejb.resource.jdbc.dbcp.DbcpDataSourceCreator;
import org.apache.openejb.resource.jdbc.logging.LoggingSqlDataSource;
import org.apache.openejb.resource.jdbc.pool.DataSourceCreator;
import org.apache.openejb.resource.jdbc.pool.DefaultDataSourceCreator;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.SuperProperties;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class DataSourceFactory {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, DataSourceFactory.class);

    public static final String LOG_SQL_PROPERTY = "LogSql";
    public static final String GLOBAL_LOG_SQL_PROPERTY = "openejb.jdbc.log";
    public static final String POOL_PROPERTY = "openejb.datasource.pool";
    public static final String DATA_SOURCE_CREATOR_PROP = "DataSourceCreator";

    private static final Map<DataSource, DataSourceCreator> creatorByDataSource = new HashMap<DataSource, DataSourceCreator>();
    private static final Map<String, String> KNOWN_CREATORS = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER){{
        put("dbcp", "org.apache.openejb.resource.jdbc.pool.DefaultDataSourceCreator"); // the original one
        put("dbcp-alternative", "org.apache.openejb.resource.jdbc.dbcp.DbcpDataSourceCreator"); // dbcp for the ds pool only
        put("tomcat", "org.apache.tomee.jdbc.TomEEDataSourceCreator"); // tomee
        put("bonecp", "org.apache.openejb.bonecp.BoneCPDataSourceCreator"); // bonecp
    }};

    public static DataSource create(final String name, final boolean configuredManaged, final Class impl, final String definition, Duration maxWaitTime, Duration timeBetweenEvictionRuns, Duration minEvictableIdleTime) throws IllegalAccessException, InstantiationException, IOException {
        final Properties properties = asProperties(definition);

        convert(properties, maxWaitTime, "maxWaitTime", "maxWait");
        convert(properties, timeBetweenEvictionRuns, "timeBetweenEvictionRuns", "timeBetweenEvictionRunsMillis");
        convert(properties, minEvictableIdleTime, "minEvictableIdleTime", "minEvictableIdleTimeMillis");

        // these can be added and are managed by OpenEJB and not the DataSource itself
        properties.remove("Definition");
        properties.remove("JtaManaged");
        properties.remove("ServiceId");

        boolean managed = configuredManaged;
        if (properties.containsKey("transactional")) {
            managed = Boolean.parseBoolean((String) properties.remove("transactional")) || managed;
        }

        boolean logSql = SystemInstance.get().getOptions().get(GLOBAL_LOG_SQL_PROPERTY,
                "true".equalsIgnoreCase((String) properties.remove(LOG_SQL_PROPERTY)));
        final DataSourceCreator creator = creator(properties.remove(DATA_SOURCE_CREATOR_PROP), logSql);


        DataSource ds;
        if (createDataSourceFromClass(impl)) { // opposed to "by driver"
            trimNotSupportedDataSourceProperties(properties);

            final ObjectRecipe recipe = new ObjectRecipe(impl);
            recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
            recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            recipe.allow(Option.NAMED_PARAMETERS);
            recipe.setAllProperties(properties);
            if (!properties.containsKey("url") && properties.containsKey("JdbcUrl")) { // depend on the datasource class so add all well known keys
                recipe.setProperty("url", properties.getProperty("JdbcUrl"));
            }

            final DataSource dataSource = (DataSource) recipe.create();

            if (managed) {
                if (usePool(properties)) {
                    ds = creator.poolManaged(name, dataSource, properties);
                } else {
                    ds = creator.managed(name, dataSource);
                }
            } else {
                if (usePool(properties)) {
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

        // ds and creator are associated here, not after the proxying of the next if if active
        creatorByDataSource.put(ds, creator);

        if (logSql) {
            ds = (DataSource) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[] { DataSource.class }, new LoggingSqlDataSource(ds));
        }

        return ds;
    }

    private static void convert(Properties properties, Duration duration, String key, String oldKey) {
        properties.remove(key);

        // If someone is using the legacy property, use it
        if (properties.contains(oldKey)) return;
        properties.remove(oldKey);

        if (duration == null) return;
        if (duration.getUnit() == null) {
            duration.setUnit(TimeUnit.MILLISECONDS);
        }

        final long milliseconds = TimeUnit.MILLISECONDS.convert(duration.getTime(), duration.getUnit());
        properties.put(oldKey, milliseconds + "");
    }

    public static DataSourceCreator creator(final Object creatorName, boolean willBeProxied) {
        final DataSourceCreator defaultCreator = SystemInstance.get().getComponent(DataSourceCreator.class);
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (creatorName != null && creatorName instanceof String
                && (defaultCreator == null || !creatorName.equals(defaultCreator.getClass().getName()))) {
            String clazz = KNOWN_CREATORS.get(creatorName);
            if (clazz == null) {
                clazz = (String) creatorName;
            }
            if (willBeProxied && clazz.equals(DefaultDataSourceCreator.class.getName())) {
                clazz = DbcpDataSourceCreator.class.getName();
            }
            try {
                return (DataSourceCreator) loader.loadClass(clazz).newInstance();
            } catch (Throwable e) {
                LOGGER.error("can't create '" + creatorName + "', the default one will be used: " + defaultCreator, e);
            }
        }
        if (defaultCreator instanceof DefaultDataSourceCreator && willBeProxied) {
            try { // this one is proxiable, not the default one (legacy)
                return (DataSourceCreator) loader.loadClass(DbcpDataSourceCreator.class.getName()).newInstance();
            } catch (Throwable e) {
                LOGGER.error("can't create '" + creatorName + "', the default one will be used: " + defaultCreator, e);
            }
        }
        return defaultCreator;
    }

    private static boolean createDataSourceFromClass(final Class<?> impl) {
        return DataSource.class.isAssignableFrom(impl) && !SystemInstance.get().getOptions().get("org.apache.openejb.resource.jdbc.hot.deploy", false);
    }

    private static boolean usePool(final Properties properties) {
        return "true".equalsIgnoreCase(properties.getProperty(POOL_PROPERTY, "true"));
    }

    private static Properties asProperties(final String definition) throws IOException {
        final SuperProperties properties = new SuperProperties();
        properties.caseInsensitive(true);
        properties.putAll(IO.readProperties(IO.read(definition), new Properties()));
        return properties;
    }

    public static void trimNotSupportedDataSourceProperties(Properties properties) {
        properties.remove("LoginTimeout");
    }

    public static boolean knows(final Object object) {
        return object instanceof DataSource && creatorByDataSource.containsKey(object);
    }

    // TODO: should we get a get and a clear method instead of a single one?
    public static ObjectRecipe forgetRecipe(final Object rawObject, final ObjectRecipe defaultValue) {
        final Object object = realInstance(rawObject);
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
        final Object instance = realInstance(o);
        final DataSourceCreator remove = creatorByDataSource.remove(instance);
        remove.destroy(instance);
    }

    // remove proxy added by us in front of the datasource returned by the creator
    private static Object realInstance(final Object o) {
        if (o == null || !(o instanceof DataSource)) {
            return o;
        }

        if (Proxy.isProxyClass(o.getClass())) {
            final InvocationHandler handler = Proxy.getInvocationHandler(o);
            if (handler instanceof LoggingSqlDataSource) {
                return ((LoggingSqlDataSource) handler).getDelegate();
            }
        }

        return o;
    }
}
