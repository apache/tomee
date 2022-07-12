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

package org.apache.openejb.resource.jdbc.dbcp;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.dbcp2.Utils;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.LocalXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedConnection;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.dbcp2.managed.TransactionRegistry;
import org.apache.commons.dbcp2.managed.XAConnectionFactory;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.cipher.PasswordCipher;
import org.apache.openejb.cipher.PasswordCipherFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.BasicDataSourceUtil;
import org.apache.openejb.resource.jdbc.IsolationLevels;
import org.apache.openejb.resource.jdbc.plugin.DataSourcePlugin;
import org.apache.openejb.resource.jdbc.pool.XADataSourceResource;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.reflection.Reflections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

@SuppressWarnings({"UnusedDeclaration"})
public class BasicManagedDataSource extends org.apache.commons.dbcp2.managed.BasicManagedDataSource implements Serializable {

    private static final ReentrantLock lock = new ReentrantLock();
    private final String name;

    private Logger logger;

    /**
     * The password codec to be used to retrieve the plain text password from a
     * ciphered value.
     *
     * <em>The default is no codec.</em>. In other words, it means password is
     * not ciphered. The {@link org.apache.openejb.cipher.PlainTextPasswordCipher} can also be used.
     */
    private String passwordCipher;

    // keep tracking the user configured password in case we need it to be decrypted again
    private String initialPassword;

    private JMXBasicDataSource jmxDs;

    public BasicManagedDataSource(final String name) {
        registerAsMbean(name);
        this.name = name;
    }

    @Override
    protected DataSource createDataSourceInstance() throws SQLException {
        final TransactionRegistry transactionRegistry = getTransactionRegistry();
        if (transactionRegistry == null) {
            throw new IllegalStateException("TransactionRegistry has not been set");
        }
        if (getConnectionPool() == null) {
            throw new IllegalStateException("Pool has not been set");
        }
        final PoolingDataSource<PoolableConnection> pds = new ManagedDataSource<PoolableConnection>(getConnectionPool(), transactionRegistry) {
            @Override
            public Connection getConnection() throws SQLException {
                return new ManagedConnection<PoolableConnection>(getPool(), transactionRegistry, isAccessToUnderlyingConnectionAllowed()) {
                    @Override
                    public void close() throws SQLException {
                        if (!isClosedInternal()) {
                            try {
                                if (null != getDelegateInternal()) {
                                    super.close();
                                }
                            } finally {
                                setClosedInternal(true);
                            }
                        }
                    }

                    @Override
                    public boolean isClosed() throws SQLException {
                        return isClosedInternal() || null != getDelegateInternal() && getDelegateInternal().isClosed();
                    }
                };
            }
        };
        pds.setAccessToUnderlyingConnectionAllowed(isAccessToUnderlyingConnectionAllowed());
        return pds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    protected ConnectionFactory createConnectionFactory() throws SQLException {
        final String xaDataSource = getXADataSource();
        if (xaDataSource != null & getXaDataSourceInstance() == null) {
            try {
                try {
                    Thread.currentThread().getContextClassLoader().loadClass(xaDataSource);
                } catch (final ClassNotFoundException | NoClassDefFoundError cnfe) {
                    setJndiXaDataSource(xaDataSource);
                }
            } catch (final Throwable th) {
                // no-op
            }
        }

        if (getTransactionManager() == null) {
            throw new SQLException("Transaction manager must be set before a connection can be created");
        } else if (getXADataSource() == null) {
            ConnectionFactory connectionFactory = super.createConnectionFactory();
            XAConnectionFactory xaConnectionFactory = new LocalXAConnectionFactory(this.getTransactionManager(), this.getTransactionSynchronizationRegistry(), connectionFactory);
            setTransactionRegistry(xaConnectionFactory, new DbcpTransactionRegistry(getTransactionManager()));
            setTransactionRegistry(xaConnectionFactory.getTransactionRegistry());
            return xaConnectionFactory;
        } else {
            XADataSource xaDataSourceInstance = getXaDataSourceInstance();
            if (xaDataSourceInstance == null) {
                Class xaDataSourceClass;

                String message;
                try {
                    xaDataSourceClass = Class.forName(xaDataSource);
                } catch (Exception var5) {
                    message = "Cannot load XA data source class '" + xaDataSource + "'";
                    throw new SQLException(message, var5);
                }

                try {
                   setXaDataSourceInstance((XADataSource) xaDataSourceClass.getConstructor().newInstance());
                } catch (Exception var4) {
                    message = "Cannot create XA data source of class '" + xaDataSource + "'";
                    throw new SQLException(message, var4);
                }
            }

            XAConnectionFactory xaConnectionFactory = new DataSourceXAConnectionFactory(this.getTransactionManager(), getXaDataSourceInstance(), this.getUsername(), Utils.toCharArray(this.getPassword()), this.getTransactionSynchronizationRegistry());
            setTransactionRegistry(xaConnectionFactory, new DbcpTransactionRegistry(getTransactionManager()));
            setTransactionRegistry(xaConnectionFactory.getTransactionRegistry());
            return xaConnectionFactory;
        }
    }

    private void setTransactionRegistry(final TransactionRegistry registry) {
        try {
            final Field field = org.apache.commons.dbcp2.managed.BasicManagedDataSource.class.getDeclaredField("transactionRegistry");
            field.setAccessible(true);
            field.set(this, registry);
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private void setTransactionRegistry(XAConnectionFactory xaConnectionFactory, final TransactionRegistry registry) {
        try {
            final Field field = xaConnectionFactory.getClass().getDeclaredField("transactionRegistry");
            field.setAccessible(true);
            field.set(xaConnectionFactory, registry);
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
    }


    private void setJndiXaDataSource(final String xaDataSource) {
        setXaDataSourceInstance( // proxy cause we don't know if this datasource was created before or not the delegate
                XADataSourceResource.proxy(getDriverClassLoader() != null ? getDriverClassLoader() : Thread.currentThread().getContextClassLoader(), xaDataSource));

        if (getTransactionManager() == null) {
            setTransactionManager(OpenEJB.getTransactionManager());
        }
    }

    private void registerAsMbean(final String name) {
        try {
            jmxDs = new JMXBasicDataSource(name, this);
        } catch (final Exception | NoClassDefFoundError e) {
            jmxDs = null;
        }
    }

    /**
     * Returns the password codec class name to use to retrieve plain text
     * password.
     *
     * @return the password codec class
     */
    public String getPasswordCipher() {
        final ReentrantLock l = lock;
        l.lock();
        try {
            return this.passwordCipher;
        } finally {
            l.unlock();
        }
    }

    /**
     * <p>
     * Sets the {@link #passwordCipher}.
     *
     *
     * @param passwordCipher password codec value
     */
    public void setPasswordCipher(final String passwordCipher) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            this.passwordCipher = passwordCipher;
        } finally {
            l.unlock();
        }
    }

    @Override
    public void setPassword(final String password) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            // keep the encrypted value if it's encrypted
            this.initialPassword = password;
            super.setPassword(password);
        } finally {
            l.unlock();
        }
    }

    public String getUserName() {
        final ReentrantLock l = lock;
        l.lock();
        try {
            return super.getUsername();
        } finally {
            l.unlock();
        }
    }

    public void setUserName(final String string) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            super.setUsername(string);
        } finally {
            l.unlock();
        }
    }

    public String getJdbcDriver() {
        final ReentrantLock l = lock;
        l.lock();
        try {
            return super.getDriverClassName();
        } finally {
            l.unlock();
        }
    }

    public void setJdbcDriver(final String string) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            super.setDriverClassName(string);
        } finally {
            l.unlock();
        }
    }

    public String getJdbcUrl() {
        final ReentrantLock l = lock;
        l.lock();
        try {
            return super.getUrl();
        } finally {
            l.unlock();
        }
    }

    public void setJdbcUrl(final String string) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            super.setUrl(string);
        } finally {
            l.unlock();
        }
    }

    public void setDefaultTransactionIsolation(final String s) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            if (s == null || s.isEmpty()) {
                return;
            }
            final int level = IsolationLevels.getIsolationLevel(s);
            super.setDefaultTransactionIsolation(level);
        } finally {
            l.unlock();
        }
    }

    public void setMaxWait(final int maxWait) {
        final ReentrantLock l = lock;
        l.lock();
        try {
            super.setMaxWaitMillis((long) maxWait);
        } finally {
            l.unlock();
        }
    }

    protected DataSource createDataSource() throws SQLException {
        final ReentrantLock l = lock;
        l.lock();
        try {
            final Object dataSource = Reflections.get(this, "dataSource");
            if (dataSource != null) {
                return DataSource.class.cast(dataSource);
            }

            // check password codec if available
            if (null != passwordCipher) {
                final PasswordCipher cipher = PasswordCipherFactory.getPasswordCipher(passwordCipher);

                // always use the initial encrypted value
                final String plainPwd = cipher.decrypt(initialPassword.toCharArray());

                // override previous password value
                super.setPassword(plainPwd);
            }

            // get the plugin
            final DataSourcePlugin helper = BasicDataSourceUtil.getDataSourcePlugin(getUrl());

            // configure this
            if (helper != null) {
                final String currentUrl = getUrl();
                final String newUrl = helper.updatedUrl(currentUrl);
                if (!currentUrl.equals(newUrl)) {
                    super.setUrl(newUrl);
                }
            }

            wrapTransactionManager();
            // create the data source
            if (helper == null || !helper.enableUserDirHack()) {
                try {
                    return super.createDataSource();
                } catch (final Throwable e) {
                    throw BasicDataSource.toSQLException(e);
                }
            } else {
                // wrap super call with code that sets user.dir to openejb.base and then resets it
                final Properties systemProperties = JavaSecurityManagers.getSystemProperties();

                final String userDir = systemProperties.getProperty("user.dir");
                try {
                    final File base = SystemInstance.get().getBase().getDirectory();
                    systemProperties.setProperty("user.dir", base.getAbsolutePath());
                    try {
                        return super.createDataSource();
                    } catch (final Throwable e) {
                        throw BasicDataSource.toSQLException(e);
                    }
                } finally {
                    systemProperties.setProperty("user.dir", userDir);
                }

            }
        } finally {
            l.unlock();
        }
    }

    protected void wrapTransactionManager() {
        //TODO?
    }

    public void close() throws SQLException {
        //TODO - Prevent unuathorized call
        final ReentrantLock l = lock;
        l.lock();
        try {
            try {
                unregisterMBean();
            } catch (final Exception ignored) {
                // no-op
            }

            super.close();
        } finally {
            l.unlock();
        }
    }

    private void unregisterMBean() {
        if (jmxDs != null) {
            jmxDs.unregister();
        }
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        final ReentrantLock l = lock;
        l.lock();
        try {

            if (null == this.logger) {
                this.logger = (Logger) DataSource.class.getDeclaredMethod("getParentLogger").invoke(createDataSource());
            }

            return this.logger;
        } catch (final Throwable e) {
            throw new SQLFeatureNotSupportedException();
        } finally {
            l.unlock();
        }
    }

    @Override
    public ObjectName preRegister(final MBeanServer server, final ObjectName name) {
        return name;
    }

    Object writeReplace() throws ObjectStreamException {
        return new DataSourceSerialization(name);
    }
}
