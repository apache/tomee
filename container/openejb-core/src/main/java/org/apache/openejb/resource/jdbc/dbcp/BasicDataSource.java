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
import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.cipher.PasswordCipher;
import org.apache.openejb.cipher.PasswordCipherFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.BasicDataSourceUtil;
import org.apache.openejb.resource.jdbc.IsolationLevels;
import org.apache.openejb.resource.jdbc.plugin.DataSourcePlugin;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.reflection.Reflections;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

@SuppressWarnings({"UnusedDeclaration"})
public class BasicDataSource extends org.apache.commons.dbcp2.BasicDataSource implements Serializable {
    private volatile Logger logger;
    private volatile DataSource dsRef = null;

    /**
     * The password codec to be used to retrieve the plain text password from a
     * ciphered value.
     * <p/>
     * <em>The default is no codec.</em>. In other words, it means password is
     * not ciphered. The {@link org.apache.openejb.cipher.PlainTextPasswordCipher} can also be used.
     */
    private String passwordCipher;

    // keep tracking the user configured password in case we need it to be decrypted again
    private String initialPassword;

    private JMXBasicDataSource jmxDs;
    private CommonDataSource delegate;
    private String name;

    public BasicDataSource() {
        // no-op
    }

    public BasicDataSource(final String name) {
        setName(name);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public void setDelegate(final CommonDataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    protected ConnectionFactory createConnectionFactory() throws SQLException {
        if (delegate != null) {
            if (XADataSource.class.isInstance(delegate)) {
                return new DataSourceXAConnectionFactory(OpenEJB.getTransactionManager(), XADataSource.class.cast(delegate), getUsername(), getPassword());
            }
            return new DataSourceConnectionFactory(DataSource.class.cast(delegate), getUsername(), getPassword());
        }
        return super.createConnectionFactory();
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
    public synchronized String getPasswordCipher() {
        return this.passwordCipher;
    }

    /**
     * <p>
     * Sets the {@link #passwordCipher}.
     * </p>
     *
     * @param passwordCipher password codec value
     */
    public synchronized void setPasswordCipher(final String passwordCipher) {
        this.passwordCipher = passwordCipher;
    }

    @Override
    public synchronized void setPassword(final String password) {
        // keep the encrypted value if it's encrypted
        this.initialPassword = password;
        super.setPassword(password);
    }

    public synchronized String getUserName() {
        return super.getUsername();
    }

    public synchronized void setUserName(final String string) {
        super.setUsername(string);
    }

    public synchronized String getJdbcDriver() {
        return super.getDriverClassName();
    }

    public synchronized void setJdbcDriver(final String string) {
        super.setDriverClassName(string);
    }

    public synchronized String getJdbcUrl() {
        return super.getUrl();
    }

    public void setJdbcUrl(final String string) {
        super.setUrl(string);
    }

    public synchronized void setDefaultTransactionIsolation(final String s) {
        if (s == null || s.isEmpty()) {
            return;
        }
        final int level = IsolationLevels.getIsolationLevel(s);
        super.setDefaultTransactionIsolation(level);
    }

    public synchronized void setMaxWait(final int maxWait) {
        super.setMaxWaitMillis((long) maxWait);
    }

    @Override
    protected DataSource createDataSource() throws SQLException {
        if (dsRef != null) {
            return dsRef;
        }
        synchronized (this) {
            if (dsRef != null) {
                return dsRef;
            }

            // check password codec if available
            if (null != passwordCipher && !"PlainText".equals(passwordCipher)) {
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

            // create the data source
            if (helper == null || !helper.enableUserDirHack()) {
                try {
                    super.createDataSource();
                } catch (final Throwable e) {
                    throw toSQLException(e);
                }
            } else {
                // wrap super call with code that sets user.dir to openejb.base and then resets it
                final Properties systemProperties = JavaSecurityManagers.getSystemProperties();

                final String userDir = systemProperties.getProperty("user.dir");
                try {
                    final File base = SystemInstance.get().getBase().getDirectory();
                    systemProperties.setProperty("user.dir", base.getAbsolutePath());
                    try {
                        super.createDataSource();
                    } catch (final Throwable e) {
                        throw toSQLException(e);
                    }
                } finally {
                    systemProperties.setProperty("user.dir", userDir);
                }
            }
            return dsRef = DataSource.class.cast(Reflections.get(this, "dataSource"));
        }
    }

    public static SQLException toSQLException(final Throwable e) {
        if (e instanceof SQLException) {
            return (SQLException) e;
        }
        return new SQLException("Failed to create DataSource", e);
    }

    @Override
    public synchronized void close() throws SQLException {
        try {
            unregisterMBean();
        } catch (final Exception ignored) {
            // no-op
        }

        super.close();
        dsRef = null;
        logger = null;
    }

    private void unregisterMBean() {
        if (jmxDs != null) {
            jmxDs.unregister();
        }
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        try {
            if (null == this.logger) {
                synchronized (this) {
                    if (null == this.logger) {
                        this.logger = (Logger) Reflections.invokeByReflection(createDataSource(), "getParentLogger", new Class<?>[0], null);
                    }
                }
            }
            return this.logger;
        } catch (final Throwable e) {
            throw new SQLFeatureNotSupportedException();
        }
    }

    public void setName(final String name) {
        registerAsMbean(name);
        this.name = name;
    }

    @Override
    public ObjectName preRegister(final MBeanServer server, final ObjectName name) {
        return name;
    }

    Object writeReplace() throws ObjectStreamException {
        return new DataSourceSerialization(name);
    }
}
