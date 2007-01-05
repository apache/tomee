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
package org.apache.openejb.resource.jdbc;

import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.LocalTransaction;
import javax.resource.ResourceException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.sql.Connection;
import java.io.PrintWriter;

public class JdbcUnmanagedConnection implements ManagedConnection {

    private final JdbcManagedConnectionMetaData metaData;

    private Connection sqlConn;
    private PrintWriter logWriter;
    private final UnmanagedConnectionRequestInfo connectionRequestInfo;

    public JdbcUnmanagedConnection(ManagedConnectionFactory managedFactory, Connection sqlConn, JdbcConnectionRequestInfo rxInfo)
            throws javax.resource.spi.ResourceAdapterInternalException {
        connectionRequestInfo = new UnmanagedConnectionRequestInfo(rxInfo);
        this.sqlConn = sqlConn;
        try {
            logWriter = managedFactory.getLogWriter();
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
        try {
            metaData = new JdbcManagedConnectionMetaData(sqlConn.getMetaData());
        } catch (java.sql.SQLException sqlE) {
            throw new javax.resource.spi.ResourceAdapterInternalException("Problem while attempting to access meta data from physical connection", ErrorCode.JDBC_0004);
        }
    }

    public static class UnmanagedConnectionRequestInfo extends JdbcConnectionRequestInfo{
        public UnmanagedConnectionRequestInfo(JdbcConnectionRequestInfo i) {
            super(i.getUserName(),i.getPassword(), i.getJdbcDriver(), i.getJdbcUrl());
        }

        public boolean equals(Object other) {
            return false;
        }
    }

    protected Connection getSQLConnection() {
        return sqlConn;
    }

    protected JdbcConnectionRequestInfo getRequestInfo() {
        return connectionRequestInfo;
    }

    public void addConnectionEventListener(ConnectionEventListener listener) {
    }

    public void associateConnection(Object connection) throws ResourceException {
    }

    public void cleanup() throws ResourceException {
    }

    public void destroy() throws ResourceException {
    }

    /*
    * Returns an application level connection handle in the form of a JdbcConnection object
    * which implements the java.sql.Connection interface and wrappers the physical JDBC connection.
    *
    */
    public Object getConnection(javax.security.auth.Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        return sqlConn;
    }

    public javax.resource.spi.LocalTransaction getLocalTransaction() throws ResourceException {
        return new LocalTransaction(){
            public void begin() throws ResourceException {
            }

            public void commit() throws ResourceException {
            }

            public void rollback() throws ResourceException {
            }
        };
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return metaData;
    }

    public javax.transaction.xa.XAResource getXAResource() throws ResourceException {
        throw new javax.resource.NotSupportedException("Method not implemented");
    }

    public void removeConnectionEventListener(ConnectionEventListener listener) {
    }

    public void setLogWriter(PrintWriter out) throws ResourceException {
        logWriter = out;
    }

    protected void localTransactionCommitted() {
    }

    protected void localTransactionRolledback() {
    }

    protected void localTransactionStarted() {
    }

    protected void connectionErrorOccurred(JdbcConnection jdbcConn, java.sql.SQLException sqlE) {
    }

    protected void connectionClose(JdbcConnection jdbcConn) {
    }

    public String toString() {
        return "JdbcUnmanagedConnection (" + sqlConn.toString() + ")";
    }
}
