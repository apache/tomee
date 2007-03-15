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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource.jdbc;

import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.Referenceable;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.ResourceAllocationException;
import javax.resource.spi.SecurityException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

public class JdbcConnectionFactory implements DataSource, Referenceable, Serializable {
    private static final long serialVersionUID = 7846198858025323860L;
    private Reference jndiReference;

    private final transient JdbcManagedConnectionFactory managedConnectionFactory;
    private final transient ConnectionManager connectionManager;
    private final String jdbcUrl;
    private final String jdbcDriver;
    private final String defaultPassword;
    private final String defaultUserName;
    private transient PrintWriter logWriter;
    private int logTimeout = 0;

    public JdbcConnectionFactory(JdbcManagedConnectionFactory managedConnectionFactory,
                                 ConnectionManager connectionManager, String jdbcUrl,
                                 String jdbcDriver, String defaultPassword, String defaultUserName) throws ResourceException {
        this.managedConnectionFactory = managedConnectionFactory;
        this.connectionManager = connectionManager;
        this.logWriter = managedConnectionFactory.getLogWriter();
        this.jdbcUrl = jdbcUrl;
        this.jdbcDriver = jdbcDriver;
        this.defaultPassword = defaultPassword;
        this.defaultUserName = defaultUserName;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public String getDefaultUserName() {
        return defaultUserName;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setReference(Reference jndiReference) {
        this.jndiReference = jndiReference;
    }

    public Reference getReference() {
        return jndiReference;
    }

    public Connection getConnection() throws SQLException {
        return getConnection(defaultUserName, defaultPassword);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        JdbcConnectionRequestInfo requestInfo = new JdbcConnectionRequestInfo(username, password, jdbcDriver, jdbcUrl);
        if (managedConnectionFactory.isUnmanaged()) {
            requestInfo = new JdbcUnmanagedConnection.UnmanagedConnectionRequestInfo(requestInfo);
        }
        return getConnection(requestInfo);
    }

    protected Connection getConnection(JdbcConnectionRequestInfo connectionRequestInfo) throws SQLException {

        try {
            return (Connection) connectionManager.allocateConnection(managedConnectionFactory, connectionRequestInfo);
        } catch (ApplicationServerInternalException e) {
            throw convertToSQLException(e, "Application error in ContainerManager");
        } catch (SecurityException e) {
            throw convertToSQLException(e, "Authentication error. Invalid credentials");
        } catch (ResourceAdapterInternalException e) {
            throw convertToSQLException(e, "JDBC Connection problem");
        } catch (ResourceAllocationException e) {
            throw convertToSQLException(e, "JDBC Connection could not be obtained");
        } catch (ResourceException e) {
            throw convertToSQLException(e, "JDBC Connection Factory problem");
        }
    }

    private SQLException convertToSQLException(ResourceException e, String error) {
        Throwable cause = e.getCause();
        if (cause instanceof SQLException) {
            return (SQLException) cause;
        } else {
            String message = ((cause != null) ? cause.getMessage() : "");
            return (SQLException) new SQLException("Error code: " + e.getErrorCode() + error + message).initCause(e);
        }
    }

    public int getLoginTimeout() {
        return logTimeout;
    }

    public PrintWriter getLogWriter() {
        return logWriter;
    }

    public void setLoginTimeout(int seconds) {

        logTimeout = seconds;
    }

    public void setLogWriter(PrintWriter out) {
        logWriter = out;
    }
}
