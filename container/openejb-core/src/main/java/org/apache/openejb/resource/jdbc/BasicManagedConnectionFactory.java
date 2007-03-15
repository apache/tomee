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

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.ResourceException;
import javax.resource.NotSupportedException;
import javax.security.auth.Subject;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.io.PrintWriter;
import java.io.Serializable;

public class BasicManagedConnectionFactory implements ManagedConnectionFactory, Serializable {
    private static final long serialVersionUID = 4339732142876149245L;
    private final String jdbcDriver;
    private final String jdbcUrl;
    private final String defaultUserName;
    private final String defaultPassword;
    private PrintWriter logWriter;
    private final int hashCode;
    private final JdbcManagedConnectionFactory managedConnectionFactory;

    public BasicManagedConnectionFactory(JdbcManagedConnectionFactory factory, String jdbcDriver, String jdbcUrl, String defaultUserName, String defaultPassword) {
        this.managedConnectionFactory = factory;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
        this.defaultUserName = defaultUserName;
        this.defaultPassword = defaultPassword;
        hashCode = jdbcDriver.hashCode() ^ jdbcUrl.hashCode() ^ defaultUserName.hashCode() ^ defaultPassword.hashCode();
    }

    public Object createConnectionFactory() throws ResourceException {
        throw new NotSupportedException("This connector must be used with an application server connection manager");
    }

    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        return new JdbcConnectionFactory(managedConnectionFactory, cxManager, jdbcUrl, jdbcDriver, defaultPassword, defaultUserName);
    }

    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
        try {
            JdbcConnectionRequestInfo request = (JdbcConnectionRequestInfo) connectionRequestInfo;
            Connection connection = DriverManager.getConnection(jdbcUrl, request.getUserName(), request.getPassword());
            if (managedConnectionFactory.isUnmanaged()){
                return new JdbcUnmanagedConnection(managedConnectionFactory, connection, request);
            }
            return new JdbcManagedConnection(managedConnectionFactory, connection, request);
        } catch (SQLException e) {
            throw (EISSystemException) new EISSystemException("Could not obtain a physical JDBC connection from the DriverManager").initCause(e);
        }
    }

    public boolean equals(Object object) {
        if (!(object instanceof BasicManagedConnectionFactory)) {
            return false;
        }
        BasicManagedConnectionFactory that = (BasicManagedConnectionFactory) object;
        return jdbcDriver.equals(that.jdbcDriver) && jdbcUrl.equals(that.jdbcUrl) && defaultUserName.equals(that.defaultUserName) && defaultPassword.equals(that.defaultPassword);
    }

    public PrintWriter getLogWriter() {
        return logWriter;
    }

    public int hashCode() {
        return hashCode;
    }

    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo connectionInfo) throws ResourceException {
        if (!(connectionInfo instanceof JdbcConnectionRequestInfo)) {
            return null;
        }
        for (Iterator iterator = connectionSet.iterator(); iterator.hasNext();) {
            JdbcManagedConnection connection = (JdbcManagedConnection) iterator.next();
            if (connection.getRequestInfo().equals(connectionInfo)) {
                return connection;
            }
        }
        return null;
    }

    public void setLogWriter(PrintWriter out) {
        logWriter = out;
    }

    public ResourceAdapter getResourceAdapter() {
        return null;
    }

    public void setResourceAdapter(ResourceAdapter ra) {

    }

}
