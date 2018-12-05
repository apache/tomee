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

package org.apache.openejb.resource.jdbc.driver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @version $Rev$ $Date$
 */
public class AlternativeDriver implements Driver {

    private final Driver delegate;
    private final String url;
    private final Method getParentLogger;

    public AlternativeDriver(final Driver driver, final String url) {
        this.delegate = driver;
        this.url = url;

        final Class<? extends Driver> clazz = delegate.getClass();


        this.getParentLogger = getMethod(clazz);

    }

    private Method getMethod(final Class<? extends Driver> clazz) {
        try {
            return clazz.getMethod("getParentLogger");
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    public Driver getDelegate() {
        return delegate;
    }

    public String getUrl() {
        return url;
    }

    public void register() throws SQLException {
        if (!isRegistered()) {
            DriverManager.registerDriver(this);
        }

        ensureFirst(new LinkedHashSet<>());
    }

    public void deregister() throws SQLException {
        if (isRegistered()) {
            DriverManager.deregisterDriver(this);
        }
    }

    private void ensureFirst(final Set<Driver> seen) throws SQLException {
        final Driver driver = DriverManager.getDriver(this.url);

        if (this == driver) {
            return;
        }

        if (!seen.add(driver)) {
            // Prevents infinite loop and detects situations
            // where the DriverManager may not allow this trick to work
            throw new SQLException(String.format("Competing driver found for URL '%s' '%s'", this.url, driver));
        }

        DriverManager.deregisterDriver(driver);
        DriverManager.registerDriver(driver);
        ensureFirst(seen);
    }

    private boolean isRegistered() {
        final Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {

            final Driver driver = drivers.nextElement();

            if (driver == this) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        if (acceptsURL(url)) {
            return getDelegate().connect(url, info);
        }
        return null;
    }

    @Override
    public boolean acceptsURL(final String url) throws SQLException {
        return this.url.equals(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
        return getDelegate().getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return getDelegate().getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return getDelegate().getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return getDelegate().jdbcCompliant();
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        if (getParentLogger != null) {
            try {

                return (Logger) getParentLogger.invoke(delegate);

            } catch (final IllegalAccessException e) {

                throw new SQLFeatureNotSupportedException(e);

            } catch (final InvocationTargetException e) {

                if (e.getCause() instanceof SQLFeatureNotSupportedException) {
                    throw (SQLFeatureNotSupportedException) e.getCause();
                }

                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }

                throw new SQLFeatureNotSupportedException(e.getCause());
            }
        }

        throw new SQLFeatureNotSupportedException();
    }
}
