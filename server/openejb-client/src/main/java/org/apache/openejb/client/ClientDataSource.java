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
package org.apache.openejb.client;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * @version $Rev$ $Date$
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class ClientDataSource implements DataSource {

    private final String jdbcUrl;
    private final String defaultPassword;
    private final String defaultUserName;

    public static void main(final String[] args) throws URISyntaxException {
        URI uri1;
        uri1 = new URI("datasource", null, "/path", null, null);
        System.out.println("uri = " + uri1);
        uri1 = new URI("datasource", "host", "/path", null, null);
        System.out.println("uri = " + uri1);
        uri1 = new URI("datasource", "host", "/path", "query", "fragment");
        System.out.println("uri = " + uri1);
        uri1 = new URI("jdbc:derby://localhost:8080/databaseName");
        print(uri1);
        print(new URI(uri1.getSchemeSpecificPart()));
    }

    private static void print(final URI uri1) {
        System.out.println("uri = " + uri1);
        System.out.println("  scheme = " + uri1.getScheme());
        System.out.println("  part   = " + uri1.getSchemeSpecificPart());
        System.out.println("  host   = " + uri1.getHost());
        System.out.println("  path   = " + uri1.getPath());
        System.out.println("  query  = " + uri1.getQuery());
    }

    public ClientDataSource(final DataSourceMetaData d) {
        this(d.getJdbcDriver(), d.getJdbcUrl(), d.getDefaultUserName(), d.getDefaultPassword());
    }

    public ClientDataSource(final String jdbcDriver, final String jdbcUrl, final String defaultUserName, final String defaultPassword) {
        this.defaultPassword = defaultPassword;
        this.defaultUserName = defaultUserName;
        this.jdbcUrl = jdbcUrl;
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class.forName(jdbcDriver, true, classLoader);
        } catch (final NoClassDefFoundError e) {
            throw new IllegalStateException("Cannot use DataSource in client VM without the JDBC Driver in classpath: " + jdbcDriver, e);
        } catch (final ClassNotFoundException cnfe) {
            throw new IllegalStateException("Cannot use DataSource in client VM without the JDBC Driver in classpath: " + jdbcDriver, cnfe);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(defaultUserName, defaultPassword);
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
    }

    @Override
    public boolean isWrapperFor(final java.lang.Class<?> iface) {
        if (iface == null) {
            throw new NullPointerException("iface is null");
        }
        return iface.isInstance(this);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (iface == null) {
            throw new NullPointerException("iface is null");
        }
        if (iface.isInstance(this)) {
            return (T) this;
        }
        throw new SQLException(getClass().getName() + " does not implement " + iface.getName());
    }

    @SuppressWarnings("override")
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
