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
import java.sql.*;
import java.sql.Connection;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @version $Rev$ $Date$
 */
public class ClientDataSource implements DataSource {
    private final String jdbcUrl;
    private final String jdbcDriver;
    private final String defaultPassword;
    private final String defaultUserName;

    public static void main(String[] args) throws URISyntaxException {
        URI uri1;
        uri1 = new URI("datasource", null, "/path",null, null);
        uri1 = new URI("datasource", null, "/path",null, null);
        System.out.println("uri = " + uri1);
        uri1 = new URI("datasource", "host", "/path",null, null);
        System.out.println("uri = " + uri1);
        uri1 = new URI("datasource", "host", "/path", "query", "fragment");
        System.out.println("uri = " + uri1);
        uri1 = new URI("jdbc:derby://localhost:8080/databaseName");
        print(uri1);
        print(new URI(uri1.getSchemeSpecificPart()));
    }

    private static void print(URI uri1) {
        System.out.println("uri = " + uri1);
        System.out.println("  scheme = " + uri1.getScheme());
        System.out.println("  part   = " + uri1.getSchemeSpecificPart());
        System.out.println("  host   = " + uri1.getHost());
        System.out.println("  path   = " + uri1.getPath());
        System.out.println("  query  = " + uri1.getQuery());
    }

    public ClientDataSource(DataSourceMetaData d) {
        this(d.getJdbcDriver(), d.getJdbcUrl(), d.getDefaultUserName(), d.getDefaultPassword());
    }

    public ClientDataSource(String jdbcDriver, String jdbcUrl, String defaultUserName, String defaultPassword) {
        this.defaultPassword = defaultPassword;
        this.defaultUserName = defaultUserName;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class.forName(jdbcDriver, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot use DataSource in client VM without the JDBC Driver in classpath: "+jdbcDriver, e);
        } catch (NoClassDefFoundError e) {
            throw new IllegalStateException("Cannot use DataSource in client VM without the JDBC Driver in classpath: "+jdbcDriver, e);
        }
    }

    public Connection getConnection() throws SQLException {
        return getConnection(defaultUserName, defaultPassword);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
        return connection;
    }

    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    public void setLoginTimeout(int seconds) throws SQLException {
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
    }
}
