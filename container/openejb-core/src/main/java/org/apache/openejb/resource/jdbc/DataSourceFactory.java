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

import org.apache.openejb.cipher.PasswordCipherException;
import org.apache.openejb.cipher.PasswordCipherFactory;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.resource.jdbc.dbcp.DbcpDataSourceCreator;
import org.apache.openejb.resource.jdbc.driver.AlternativeDriver;
import org.apache.openejb.resource.jdbc.logging.LoggingSqlDataSource;
import org.apache.openejb.resource.jdbc.plugin.AbstractDataSourcePlugin;
import org.apache.openejb.resource.jdbc.plugin.DataSourcePlugin;
import org.apache.openejb.resource.jdbc.pool.DataSourceCreator;
import org.apache.openejb.resource.jdbc.pool.DefaultDataSourceCreator;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.SuperProperties;
import org.apache.xbean.recipe.ExecutionContext;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.Recipe;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.io.Flushable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

/**
 * @version $Rev$ $Date$
 */
public class DataSourceFactory {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, DataSourceFactory.class);

    public static final String LOG_SQL_PROPERTY = "LogSql";
    public static final String LOG_SQL_PACKAGE_PROPERTY = "LogSqlPackages";
    public static final String FLUSHABLE_PROPERTY = "Flushable";
    public static final String RESET_PROPERTY = "ResetOnError";
    public static final String RESET_METHODS_PROPERTY = "ResetOnErrorMethods";
    public static final String GLOBAL_LOG_SQL_PROPERTY = "openejb.jdbc.log";
    public static final String GLOBAL_LOG_SQL_PACKAGE_PROPERTY = "openejb.jdbc.log.packages";
    public static final String GLOBAL_FLUSH_PROPERTY = "openejb.jdbc.flushable";
    public static final String POOL_PROPERTY = "openejb.datasource.pool";
    public static final String DATA_SOURCE_CREATOR_PROP = "DataSourceCreator";
    public static final String XA_GLOBAL_FORCE_DIFFERENT = "openejb.datasource.xa.force-different-xaresource";
    public static final String XA_FORCE_DIFFERENT = "XAForceDifferent";
    public static final String HANDLER_PROPERTY = "TomEEProxyHandler";
    public static final String GLOBAL_HANDLER_PROPERTY = "openejb.jdbc.handler";

    private static final Map<CommonDataSource, AlternativeDriver> driverByDataSource = new HashMap<>();

    private static final Map<CommonDataSource, DataSourceCreator> creatorByDataSource = new HashMap<>();
    private static final Map<String, String> KNOWN_CREATORS = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {{
        put("simple", "org.apache.openejb.resource.jdbc.SimpleDataSourceCreator"); // use user provided DS, pooling not supported
        put("dbcp", "org.apache.openejb.resource.jdbc.pool.DefaultDataSourceCreator"); // the original one
        put("dbcp-alternative", "org.apache.openejb.resource.jdbc.dbcp.DbcpDataSourceCreator"); // dbcp for the ds pool only
        put("tomcat", "org.apache.tomee.jdbc.TomEEDataSourceCreator"); // tomee
        put("bonecp", "org.apache.openejb.bonecp.BoneCPDataSourceCreator"); // bonecp
    }};

    public static CommonDataSource create(final String name,
                                          final boolean configuredManaged,
                                          final Class impl,
                                          final String definition,
                                          final Duration maxWaitTime,
                                          final Duration timeBetweenEvictionRuns,
                                          final Duration minEvictableIdleTime,
                                          final boolean useAlternativeDriver) throws IllegalAccessException, InstantiationException, IOException {
        final Properties properties = asProperties(definition);
        final Set<String> originalKeys = properties.stringPropertyNames();

        final String handler = SystemInstance.get().getOptions().get(GLOBAL_HANDLER_PROPERTY, (String) properties.remove(HANDLER_PROPERTY));
        boolean flushable = SystemInstance.get().getOptions().get(GLOBAL_FLUSH_PROPERTY,
            "true".equalsIgnoreCase((String) properties.remove(FLUSHABLE_PROPERTY)));
        final String forceDifferent = SystemInstance.get().getOptions().get(XA_GLOBAL_FORCE_DIFFERENT, String.class.cast(properties.remove(XA_FORCE_DIFFERENT)));

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

        normalizeJdbcUrl(properties);

        final String jdbcUrl = properties.getProperty("JdbcUrl");

        final AlternativeDriver driver;
        if (Driver.class.isAssignableFrom(impl) && jdbcUrl != null && useAlternativeDriver) {
            try {
                driver = new AlternativeDriver((Driver) impl.newInstance(), jdbcUrl);
                driver.register();
            } catch (final SQLException e) {
                throw new IllegalStateException(e);
            }
        } else {
            driver = null;
        }

        final boolean logSql = SystemInstance.get().getOptions().get(GLOBAL_LOG_SQL_PROPERTY,
            "true".equalsIgnoreCase((String) properties.remove(LOG_SQL_PROPERTY)));
        final String logPackages = SystemInstance.get().getProperty(GLOBAL_LOG_SQL_PACKAGE_PROPERTY, (String) properties.remove(LOG_SQL_PACKAGE_PROPERTY));
        final DataSourceCreator creator = creator(properties.remove(DATA_SOURCE_CREATOR_PROP), logSql);
        final String resetOnError = (String) properties.remove(RESET_PROPERTY);
        final String resetMethods = (String) properties.remove(RESET_METHODS_PROPERTY); // before setProperties()

        boolean useContainerLoader = "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.resources.use-container-loader", "true")) && impl.getClassLoader() == DataSourceFactory.class.getClassLoader();
        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        if (useContainerLoader) {
            final ClassLoader containerLoader = DataSourceFactory.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(containerLoader);
            try {
                useContainerLoader = basicChecksThatDataSourceCanBeCreatedFromContainerLoader(properties, containerLoader);
            } finally {
                Thread.currentThread().setContextClassLoader(oldLoader);
            }
            if (useContainerLoader) {
                Thread.currentThread().setContextClassLoader(containerLoader);
            } else {
                LOGGER.info("Can't use container loader to create datasource " + name + " so using application one");
            }
        }

        try {
            CommonDataSource ds;
            if (createDataSourceFromClass(impl)) { // opposed to "by driver"
                trimNotSupportedDataSourceProperties(properties);

                final ObjectRecipe recipe = new ObjectRecipe(impl);
                recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
                recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
                recipe.allow(Option.NAMED_PARAMETERS);
                recipe.allow(Option.PRIVATE_PROPERTIES);
                recipe.setAllProperties(properties);
                if (!properties.containsKey("url") && properties.containsKey("JdbcUrl")) { // depend on the datasource class so add all well known keys
                    recipe.setProperty("url", properties.getProperty("JdbcUrl"));
                }

                CommonDataSource dataSource = (CommonDataSource) recipe.create();
                final boolean isDs = DataSource.class.isInstance(dataSource);
                if (!isDs && XADataSource.class.isInstance(dataSource) && forceDifferent != null) {
                    try {
                        dataSource = CommonDataSource.class.cast(Thread.currentThread().getContextClassLoader()
                                        .loadClass("true".equals(forceDifferent) ? "org.apache.openejb.resource.jdbc.xa.IsDifferentXaDataSourceWrapper" : forceDifferent)
                                        .getConstructor(XADataSource.class)
                                        .newInstance(dataSource));
                    } catch (InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
                        throw new IllegalArgumentException(e);
                    }
                }

                if (managed) {
                    if (isDs && usePool(properties)) {
                        ds = creator.poolManaged(name, DataSource.class.cast(dataSource), properties);
                    } else {
                        ds = creator.managed(name, dataSource);
                    }
                } else {
                    if (isDs && usePool(properties)) {
                        ds = creator.pool(name, DataSource.class.cast(dataSource), properties);
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
            setCreatedWith(creator, ds);
            if (driver != null) {
                driverByDataSource.put(ds, driver);
            }

            final boolean doResetOnError = resetOnError != null && !"false".equals(resetOnError);
            if (doResetOnError || logSql || flushable) { // will get proxied
                ObjectRecipe objectRecipe = null;
                ResettableDataSourceHandler existingResettableHandler = null;
                FlushableDataSourceHandler flushableDataSourceHandler = null;
                if (ExecutionContext.isContextSet()) {
                    final ExecutionContext context = ExecutionContext.getContext();
                    final List<Recipe> stack = context.getStack();
                    if (stack.size() > 0) {
                        objectRecipe = ObjectRecipe.class.cast(stack.get(0));
                        existingResettableHandler = ResettableDataSourceHandler.class.cast(objectRecipe.getProperty("resettableHandler"));
                        flushableDataSourceHandler = FlushableDataSourceHandler.class.cast(objectRecipe.getProperty("flushableHandler"));

                        final Map<String, Object> props = objectRecipe.getProperties();
                        for (final String key : originalKeys) {
                            props.remove(key);
                        }

                        // meta properties, not needed here so gain few cycles removing them
                        props.remove("properties");
                        props.remove("Definition");
                        props.remove("ServiceId");
                        props.remove("resettableHandler");
                        props.remove("flushableHandler");

                        //we create a proxy so we cant get txmgr etc in another manner or we cant extend (= break) this method
                        new ObjectRecipe(ds.getClass()) {{
                            allow(Option.CASE_INSENSITIVE_PROPERTIES);
                            allow(Option.IGNORE_MISSING_PROPERTIES);
                            allow(Option.NAMED_PARAMETERS);
                            allow(Option.PRIVATE_PROPERTIES);
                            setAllProperties(props);
                        }}.setProperties(ds);
                    }
                }

                ds = wrapIfNeeded(handler, ds);

                if (logSql) {
                    ds = makeItLogging(ds, logPackages);
                }

                final ResettableDataSourceHandler resettableDataSourceHandler;
                if (doResetOnError) { // needs to be done after flushable
                    // ensure we reuse the same handle instance otherwise we loose state
                    resettableDataSourceHandler = existingResettableHandler != null ?
                        existingResettableHandler :
                        new ResettableDataSourceHandler(ds, resetOnError, resetMethods);
                } else {
                    resettableDataSourceHandler = null;
                }

                if (flushable || doResetOnError) {
                    if (flushableDataSourceHandler == null) {
                        final FlushableDataSourceHandler.FlushConfig flushConfig;
                        properties.remove("flushable"); // don't let it wrap the delegate again

                        final Map<String, Object> recipeProps = new HashMap<>(objectRecipe == null ? new HashMap<>() : objectRecipe.getProperties());
                        recipeProps.remove("properties");
                        recipeProps.put("OpenEJBResourceClasspath", String.valueOf(useAlternativeDriver));

                        flushConfig = new FlushableDataSourceHandler.FlushConfig(recipeProps);
                        flushableDataSourceHandler = new FlushableDataSourceHandler(ds, flushConfig, resettableDataSourceHandler);
                    } else {
                        flushableDataSourceHandler.updateDataSource(ds);
                    }
                    ds = makeSerializableFlushableDataSourceProxy(ds, flushableDataSourceHandler);
                }
                if (doResetOnError) { // needs to be done after flushable
                    // ensure we reuse the same handle instance otherwise we loose state
                    resettableDataSourceHandler.updateDelegate(ds);
                    ds = makeSerializableFlushableDataSourceProxy(ds, resettableDataSourceHandler);
                }
            } else {
                ds = wrapIfNeeded(handler, ds);
            }

            return ds;
        } finally {
            if (useContainerLoader) {
                Thread.currentThread().setContextClassLoader(oldLoader);
            }
        }
    }

    private static CommonDataSource wrapIfNeeded(final String handler, final CommonDataSource ds) throws InstantiationException, IllegalAccessException {
        if (handler != null) {
            try {
                final Class<?> handlerClass = Thread.currentThread().getContextClassLoader().loadClass(handler);
                InvocationHandler instance;
                try {
                    instance = InvocationHandler.class.cast(handlerClass.getConstructor(DataSource.class).newInstance(ds));
                } catch (final InvocationTargetException e) {
                    throw new IllegalStateException(e.getCause());
                } catch (final NoSuchMethodException e) {
                    try {
                        instance = InvocationHandler.class.cast(handlerClass.getConstructor(CommonDataSource.class).newInstance(ds));
                    } catch (final InvocationTargetException e2) {
                        throw new IllegalStateException(e.getCause());
                    } catch (final NoSuchMethodException e2) {
                        instance = InvocationHandler.class.cast(handlerClass.newInstance());
                    }
                }
                return wrapWithHandler(ds, instance);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Can't find handler: " + handler, e);
            }
        }
        return ds;
    }

    public static CommonDataSource makeSerializableFlushableDataSourceProxy(final CommonDataSource ds, final InvocationHandler handler) {
        return (CommonDataSource) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class<?>[]{DataSource.class.isInstance(ds) ? DataSource.class : XADataSource.class, Serializable.class, Flushable.class},
            handler);
    }

    private static boolean basicChecksThatDataSourceCanBeCreatedFromContainerLoader(final Properties properties, final ClassLoader containerLoader) {
        // check basic some classes can be loaded from container otherwise don't force it
        try {
            for (final String property : asList("JdbcDriver", "driverClassName")) {
                final String value = properties.getProperty(property);
                if (value != null) {
                    Class.forName(value, false, containerLoader);
                }
            }
        } catch (final ClassNotFoundException | NoClassDefFoundError cnfe) {
            return false;
        }

        // also password cipher can be loaded from apps
        try {
            final String cipher = properties.getProperty("PasswordCipher");
            if (cipher != null && !"PlainText".equals(cipher) && !"Static3DES".equals(cipher)) {
                PasswordCipherFactory.getPasswordCipher(cipher);
            }
        } catch (final PasswordCipherException cnfe) {
            return false;
        }

        return true;
    }

    public static void setCreatedWith(final DataSourceCreator creator, final CommonDataSource ds) {
        creatorByDataSource.put(ds, creator);
    }

    private static CommonDataSource wrapWithHandler(final CommonDataSource ds, final InvocationHandler instance) {
        return (CommonDataSource) Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class<?>[]{DataSource.class.isInstance(ds) ? DataSource.class : XADataSource.class, Serializable.class},
            instance);
    }

    public static DataSource makeItLogging(final CommonDataSource ds, final String packagesStr) {
        final String[] pck = packagesStr == null ? null : packagesStr.split(" *, *");
        return (DataSource) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
            new Class<?>[]{DataSource.class, Serializable.class}, new LoggingSqlDataSource(ds, pck));
    }

    private static void normalizeJdbcUrl(final Properties properties) {
        final String key = "JdbcUrl";
        final String jdbcUrl = properties.getProperty(key);

        if (jdbcUrl == null) {
            return;
        }

        try {
            // get the plugin
            final DataSourcePlugin helper = BasicDataSourceUtil.getDataSourcePlugin(jdbcUrl);

            // configure this
            if (AbstractDataSourcePlugin.isActive(helper)) {
                final String newUrl = helper.updatedUrl(jdbcUrl);
                properties.setProperty(key, newUrl);
            }
        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void convert(final Properties properties, final Duration duration, final String key, final String oldKey) {
        properties.remove(key);

        // If someone is using the legacy property, use it
        if (properties.contains(oldKey)) {
            return;
        }
        properties.remove(oldKey);

        if (duration == null) {
            return;
        }
        if (duration.getUnit() == null) {
            duration.setUnit(TimeUnit.MILLISECONDS);
        }

        final long milliseconds = TimeUnit.MILLISECONDS.convert(duration.getTime(), duration.getUnit());
        properties.put(oldKey, String.valueOf(milliseconds));
    }

    public static DataSourceCreator creator(final Object creatorName, final boolean willBeProxied) {
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
            } catch (final Throwable e) {
                LOGGER.error("can't create '" + creatorName + "', the default one will be used: " + defaultCreator, e);
            }
        }
        if (defaultCreator instanceof DefaultDataSourceCreator && willBeProxied) {
            // this one is proxiable, not the default one (legacy)
            return new DbcpDataSourceCreator();
        }
        return defaultCreator;
    }

    private static boolean createDataSourceFromClass(final Class<?> impl) {
        return isDataSource(impl) && !SystemInstance.get().getOptions().get("org.apache.openejb.resource.jdbc.hot.deploy", false);
    }

    private static boolean isDataSource(final Class<?> impl) {
        return DataSource.class.isAssignableFrom(impl) || XADataSource.class.isAssignableFrom(impl) ||
                ConnectionPoolDataSource.class.isAssignableFrom(impl);
    }

    private static boolean usePool(final Properties properties) {
        String property = properties.getProperty(POOL_PROPERTY, SystemInstance.get().getProperty(POOL_PROPERTY));
        if (property != null) {
            properties.remove(POOL_PROPERTY);
        } else { // defined from @DataSourceDefinition and doesn't need pooling
            final String initialPoolSize = properties.getProperty("initialPoolSize");
            final String maxPoolSize = properties.getProperty("maxPoolSize");
            if ((null == initialPoolSize || "-1".equals(initialPoolSize))
                && ("-1".equals(maxPoolSize) || maxPoolSize == null)) {
                property = "false";
            }
        }
        return "true".equalsIgnoreCase(property) || null == property;
    }

    private static Properties asProperties(final String definition) throws IOException {
        final SuperProperties properties = new SuperProperties();
        properties.caseInsensitive(true);
        properties.putAll(IO.readProperties(IO.read(definition), new Properties()));
        return properties;
    }

    public static void trimNotSupportedDataSourceProperties(final Properties properties) {
        properties.remove("LoginTimeout");
    }

    public static boolean knows(final Object object) {
        return object instanceof CommonDataSource && creatorByDataSource.containsKey(CommonDataSource.class.cast(realInstance(object)));
    }

    // TODO: should we get a get and a clear method instead of a single one?
    @SuppressWarnings("SuspiciousMethodCalls")
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

    @SuppressWarnings("SuspiciousMethodCalls")
    public static void destroy(final Object o) throws Throwable {
        final Object instance = realInstance(o);
        if (instance == null) {
            return;
        }
        final DataSourceCreator remove = creatorByDataSource.remove(instance);
        remove.destroy(instance);

        final AlternativeDriver driver = driverByDataSource.remove(instance);
        if (driver != null) {
            driver.deregister();
        }
    }

    // remove proxy added by us in front of the datasource returned by the creator
    private static Object realInstance(final Object o) {
        if (o == null || !(o instanceof DataSource)) {
            return o;
        }

        Object ds = o;
        while (Proxy.isProxyClass(ds.getClass())) {
            final InvocationHandler handler = Proxy.getInvocationHandler(ds);
            if (DelegatableHandler.class.isInstance(handler)) {
                ds = DelegatableHandler.class.cast(handler).getDelegate();
            } else {
                break;
            }
        }

        return ds;
    }
}
