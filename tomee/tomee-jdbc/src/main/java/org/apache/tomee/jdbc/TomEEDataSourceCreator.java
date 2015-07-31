/**
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
package org.apache.tomee.jdbc;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.openejb.cipher.PasswordCipherFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.resource.jdbc.dbcp.DataSourceSerialization;
import org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.SuperProperties;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.jdbc.pool.PooledConnection;

import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Properties;

public class TomEEDataSourceCreator extends PoolDataSourceCreator {
    @Override
    public DataSource pool(final String name, final DataSource ds, final Properties properties) {
        final PoolConfiguration config = build(TomEEPoolProperties.class, createProperties(name, properties));
        config.setDataSource(ds);
        final ConnectionPool pool;
        try {
            pool = new ConnectionPool(config);
        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }

        final TomEEDataSource dataSource = new TomEEDataSource(config, pool, name);
        recipes.put(dataSource, recipes.remove(config)); // transfer unset props for correct logging
        return dataSource;
    }

    @Override
    public CommonDataSource pool(final String name, final String driver, final Properties properties) {
        final PoolConfiguration config = build(TomEEPoolProperties.class, createProperties(name, properties));
        final TomEEDataSource ds = new TomEEDataSource(config, name);
        recipes.put(ds, recipes.remove(config));
        return ds;
    }

    @Override
    protected void doDestroy(final CommonDataSource dataSource) throws Throwable {
        final org.apache.tomcat.jdbc.pool.DataSource ds = (org.apache.tomcat.jdbc.pool.DataSource) dataSource;
        if (ds instanceof TomEEDataSource) {
            ((TomEEDataSource) ds).internalJMXUnregister();
        }
        ds.close(true);
    }

    @Override
    protected boolean trackRecipeFor(final Object value) {
        return super.trackRecipeFor(value) || TomEEPoolProperties.class.isInstance(value);
    }

    private SuperProperties createProperties(final String name, final Properties properties) {
        final SuperProperties converted = new SuperProperties() {
            @Override
            public Object setProperty(final String name, final String value) {
                if (value == null) {
                    return super.getProperty(name);
                }
                return super.setProperty(name, value);
            }
        }.caseInsensitive(true);

        converted.setProperty("name", name);
        // very few properties have default = connection ones, so ensure to translate them with priority to specific ones
        converted.setProperty("url", properties.getProperty("url", (String) properties.remove("JdbcUrl")));
        converted.setProperty("driverClassName", properties.getProperty("driverClassName", (String) properties.remove("JdbcDriver")));
        converted.setProperty("username", (String) properties.remove("username"));
        converted.setProperty("password", (String) properties.remove("password"));
        converted.putAll(properties);

        final String passwordCipher = (String) converted.remove("PasswordCipher");
        if (passwordCipher != null && !"PlainText".equals(passwordCipher)) {
            converted.setProperty("password", PasswordCipherFactory.getPasswordCipher(passwordCipher).decrypt(converted.getProperty("Password").toCharArray()));
        }

        return converted;
    }

    public static class TomEEDataSource extends org.apache.tomcat.jdbc.pool.DataSource implements Serializable {
        private static final Log LOGGER = LogFactory.getLog(TomEEDataSource.class);
        private static final Class<?>[] CONNECTION_POOL_CLASS = new Class<?>[]{ PoolConfiguration.class };

        private final String name;
        private ObjectName internalOn;

        public TomEEDataSource(final PoolConfiguration properties, final ConnectionPool pool, final String name) {
            super(readOnly(properties));
            this.pool = pool;
            initJmx(name);
            this.name = name;
        }

        public TomEEDataSource(final PoolConfiguration poolConfiguration, final String name) {
            super(readOnly(poolConfiguration));
            try { // just to force the pool to be created and be able to register the mbean
                createPool();
                initJmx(name);
            } catch (final Throwable e) {
                LOGGER.error("Can't create DataSource", e);
            }
            this.name = name;
        }

        @Override
        protected void registerJmx() {
            // no-op
        }

        @Override
        protected void unregisterJmx() {
            // no-op
        }

        @Override
        public ConnectionPool createPool() throws SQLException {
            if (pool != null) {
                return pool;
            } else {
                pool = new TomEEConnectionPool(poolProperties, Thread.currentThread().getContextClassLoader()); // to force to init the driver with TCCL
                return pool;
            }
        }

        private static PoolConfiguration readOnly(final PoolConfiguration pool) {
            try {
                return (PoolConfiguration) Proxy.newProxyInstance(TomEEDataSourceCreator.class.getClassLoader(), CONNECTION_POOL_CLASS, new ReadOnlyConnectionpool(pool));
            } catch (final Throwable e) {
                return (PoolConfiguration) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), CONNECTION_POOL_CLASS, new ReadOnlyConnectionpool(pool));
            }
        }

        private void initJmx(final String name) {
            try {
                internalOn = ObjectNameBuilder.uniqueName("datasources", name.replace("/", "_"), this);
                try {
                    if (pool.getJmxPool() != null) {
                        LocalMBeanServer.get().registerMBean(pool.getJmxPool(), internalOn);
                    }
                } catch (final Exception e) {
                    LOGGER.error("Unable to register JDBC pool with JMX", e);
                }
            } catch (final Exception ignored) {
                // no-op
            }
        }

        public void internalJMXUnregister() {
            if (internalOn != null) {
                try {
                    LocalMBeanServer.get().unregisterMBean(internalOn);
                } catch (final Exception e) {
                    LOGGER.error("Unable to unregister JDBC pool with JMX", e);
                }
            }
        }

        Object writeReplace() throws ObjectStreamException {
            return new DataSourceSerialization(name);
        }
    }

    private static class ReadOnlyConnectionpool implements InvocationHandler {
        private final PoolConfiguration delegate;

        public ReadOnlyConnectionpool(final PoolConfiguration pool) {
            this.delegate = pool;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final String name = method.getName();
            if (!(name.startsWith("set") && args != null && args.length == 1 && Void.TYPE.equals(method.getReturnType()))) {
                return method.invoke(delegate, args);
            }
            if (name.equals("setDataSource")) {
                delegate.setDataSource(args[0]);
            }
            return null;
        }
    }

    private static class TomEEConnectionPool extends ConnectionPool {
        private final ClassLoader creationLoader;

        public TomEEConnectionPool(final PoolConfiguration poolProperties, final ClassLoader creationLoader) throws SQLException {
            super(poolProperties);
            this.creationLoader = creationLoader;
        }

        @Override
        protected PooledConnection create(final boolean incrementCounter) {
            final PooledConnection con = super.create(incrementCounter);
            if (getPoolProperties().getDataSource() == null) { // using driver
                // init driver with TCCL
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null) {
                    cl = TomEEConnectionPool.class.getClassLoader();
                }
                try {
                    Reflections.set(con, "driver", Class.forName(getPoolProperties().getDriverClassName(), true, cl).newInstance());
                } catch (final ClassNotFoundException cnfe) {
                    try { // custom resource classloader
                        Reflections.set(con, "driver", Class.forName(getPoolProperties().getDriverClassName(), true, creationLoader).newInstance());
                    } catch (final Exception e) {
                        // will fail later, no worry
                    }
                } catch (final Exception cn) {
                    // will fail later, no worry
                }
            }
            return con;
        }
    }

    // enhanced API/setters
    public static class TomEEPoolProperties extends PoolProperties {
        public void setMinEvictableIdleTime(final String minEvictableIdleTime) {
            final Duration duration = new Duration(minEvictableIdleTime);
            super.setMinEvictableIdleTimeMillis((int) duration.getUnit().toMillis(duration.getTime()));
        }

        public void setTimeBetweenEvictionRuns(final String timeBetweenEvictionRuns) {
            final Duration duration = new Duration(timeBetweenEvictionRuns);
            super.setMinEvictableIdleTimeMillis((int) duration.getUnit().toMillis(duration.getTime()));
        }

        public void setXaDataSource(final String jndi) {
            // we should do setDataSourceJNDI(jndi); but ATM tomcat doesnt do the lookup so using this as correct impl
            try {
                setDataSource(SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup("openejb:Resource/" + jndi));
            } catch (final NamingException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void setDataSourceJNDI(final String jndi) {
            super.setDataSourceJNDI("openejb:Resource/" + jndi);
        }
    }
}
