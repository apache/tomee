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

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnectionMetaData;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcManagedConnection implements ManagedConnection {

    private final JdbcConnectionRequestInfo requestInfo;
    private final JdbcManagedConnectionMetaData metaData;
    private final JdbcLocalTransaction localTransaction;
    private final List jdbcConnections = new ArrayList();
    private final Set listeners;

    private Connection sqlConn;
    private PrintWriter logWriter;

    public JdbcManagedConnection(ManagedConnectionFactory managedFactory, java.sql.Connection sqlConn, JdbcConnectionRequestInfo rxInfo)
            throws javax.resource.spi.ResourceAdapterInternalException {
        listeners = java.util.Collections.synchronizedSet(new HashSet());
        this.requestInfo = rxInfo;
        this.sqlConn = sqlConn;
        try {
            logWriter = managedFactory.getLogWriter();
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
        try {
            metaData = new JdbcManagedConnectionMetaData(sqlConn.getMetaData());
        } catch (java.sql.SQLException sqlE) {
            throw (javax.resource.spi.ResourceAdapterInternalException)new javax.resource.spi.ResourceAdapterInternalException("Problem while attempting to access meta data from physical connection", ErrorCode.JDBC_0004).initCause(sqlE);
        }
        localTransaction = new JdbcLocalTransaction(this);
    }

    protected java.sql.Connection getSQLConnection() {
        return sqlConn;
    }

    protected JdbcConnectionRequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void addConnectionEventListener(ConnectionEventListener listener) {
        listeners.add(listener);
    }

    public void associateConnection(java.lang.Object connection) throws javax.resource.ResourceException {
        if (connection instanceof JdbcConnection) {
            JdbcConnection jdbcConn = (JdbcConnection) connection;
            jdbcConn.associate(this);
        } else {
            throw new javax.resource.ResourceException("Connection object is the wrong type. It must be an instance of JdbcConnection");
        }
    }

    public void cleanup() throws javax.resource.ResourceException {
        synchronized (jdbcConnections) {
            Object[] connectionHandles = jdbcConnections.toArray();
            for (int i = 0; i < connectionHandles.length; i++) {
                JdbcConnection handle = (JdbcConnection) connectionHandles[i];
                handle.invalidate();
            }
            jdbcConnections.clear();
            localTransaction.cleanup();
        }
    }

    public void destroy() throws javax.resource.ResourceException {
        cleanup();
        try {
            sqlConn.close();
        } catch (java.sql.SQLException sqlE) {
            throw (javax.resource.spi.ResourceAdapterInternalException)new javax.resource.spi.ResourceAdapterInternalException("Problem attempting to close physical JDBC connection", ErrorCode.JDBC_0003).initCause(sqlE);
        }
        sqlConn = null;
        listeners.clear();
    }

    /*
    * Returns an application level connection handle in the form of a JdbcConnection object
    * which implements the java.sql.Connection interface and wrappers the physical JDBC connection.
    *
    */
    public java.lang.Object getConnection(javax.security.auth.Subject subject, ConnectionRequestInfo cxRequestInfo) throws javax.resource.ResourceException {
        synchronized (jdbcConnections) {
            JdbcConnection jdbcCon = new JdbcConnection(this, sqlConn);
            jdbcConnections.add(jdbcCon);
            return jdbcCon;
        }
    }

    public javax.resource.spi.LocalTransaction getLocalTransaction() throws javax.resource.ResourceException {
        return localTransaction;
    }

    public java.io.PrintWriter getLogWriter() throws javax.resource.ResourceException {
        return logWriter;
    }

    public ManagedConnectionMetaData getMetaData() throws javax.resource.ResourceException {
        return metaData;
    }

    public javax.transaction.xa.XAResource getXAResource() throws javax.resource.ResourceException {
        throw new javax.resource.NotSupportedException("Method not implemented");
    }

    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    public void setLogWriter(java.io.PrintWriter out) throws javax.resource.ResourceException {
        logWriter = out;
    }

    protected void localTransactionCommitted() {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
        Object[] elements = listeners.toArray();
        for (int i = 0; i < elements.length; i++) {
            ConnectionEventListener eventListener = (ConnectionEventListener) elements[i];
            eventListener.localTransactionCommitted(event);
        }
    }

    protected void localTransactionRolledback() {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);
        Object[] elements = listeners.toArray();
        for (int i = 0; i < elements.length; i++) {
            ConnectionEventListener eventListener = (ConnectionEventListener) elements[i];
            eventListener.localTransactionRolledback(event);
        }
    }

    protected void localTransactionStarted() {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED);
        Object[] elements = listeners.toArray();
        for (int i = 0; i < elements.length; i++) {
            ConnectionEventListener eventListener = (ConnectionEventListener) elements[i];
            eventListener.localTransactionStarted(event);
        }
    }

    protected void connectionErrorOccurred(JdbcConnection jdbcConn, java.sql.SQLException sqlE) {

        if (logWriter != null) {
            logWriter.print("\nJdbcConnection Error: On java.sql.Connection (");
            logWriter.print(jdbcConn);
            logWriter.println(")");
            logWriter.println("Exception Stack trace follows:");
            sqlE.printStackTrace(logWriter);
            java.sql.SQLException temp = sqlE;
            while ((temp = sqlE.getNextException()) != null) {
                temp.printStackTrace(logWriter);
            }
        }

        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, sqlE);
        Object[] elements = listeners.toArray();
        for (int i = 0; i < elements.length; i++) {
            ConnectionEventListener eventListener = (ConnectionEventListener) elements[i];
            eventListener.connectionErrorOccurred(event);
        }
    }

    protected void connectionClose(JdbcConnection jdbcConn) {
        synchronized (jdbcConnections) {
            jdbcConn.invalidate();
            jdbcConnections.remove(jdbcConn);
            ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
            Object[] elements = listeners.toArray();
            for (int i = 0; i < elements.length; i++) {
                ConnectionEventListener eventListener = (ConnectionEventListener) elements[i];
                eventListener.connectionClosed(event);
            }
        }
    }

    public String toString() {
        return "JdbcManagedConnection (" + sqlConn.toString() + ")";
    }
}