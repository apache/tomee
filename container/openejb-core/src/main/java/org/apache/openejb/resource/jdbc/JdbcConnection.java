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

import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class JdbcConnection implements java.sql.Connection {
    private java.sql.Connection physicalConn;
    private JdbcManagedConnection managedConn;
    protected boolean isClosed = false;

    protected JdbcConnection(JdbcManagedConnection managedConn, java.sql.Connection physicalConn) {
        this.physicalConn = physicalConn;
        this.managedConn = managedConn;
    }

    protected java.sql.Connection getPhysicalConnection() {
        return physicalConn;
    }

    protected JdbcManagedConnection getManagedConnection() {
        return managedConn;
    }

    protected void invalidate() {
        isClosed = true;
        physicalConn = null;
        managedConn = null;
    }

    protected void associate(JdbcManagedConnection mngdConn) {
        isClosed = false;
        managedConn = mngdConn;
        physicalConn = mngdConn.getSQLConnection();
    }

    public Statement createStatement() throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.createStatement();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public PreparedStatement prepareStatement(String sql)
            throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.prepareStatement(sql);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.prepareCall(sql);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public String nativeSQL(String sql) throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.nativeSQL(sql);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        throw new java.sql.SQLException("Method not supported. Commit is managed automatically by container provider");
    }

    public boolean getAutoCommit() throws SQLException {
        throw new java.sql.SQLException("Method not supported. Commit is managed automatically by container provider");
    }

    public void commit() throws SQLException {
        throw new java.sql.SQLException("Method not supported. Commit is managed automatically by container provider");
    }

    public void rollback() throws SQLException {
        throw new java.sql.SQLException("Method not supported. Rollback is managed automatically by container provider");
    }

    public void close() throws SQLException {
        if (isClosed)
            return;
        else {

            managedConn.connectionClose(this);
        }
    }

    public boolean isClosed() throws SQLException {
        return isClosed;
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.getMetaData();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                physicalConn.setReadOnly(readOnly);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public boolean isReadOnly() throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.isReadOnly();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void setCatalog(String catalog) throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                physicalConn.setCatalog(catalog);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public String getCatalog() throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.getCatalog();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void setTransactionIsolation(int level) throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                physicalConn.setTransactionIsolation(level);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public int getTransactionIsolation() throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.getTransactionIsolation();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.getWarnings();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void clearWarnings() throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                physicalConn.clearWarnings();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.createStatement(resultSetType, resultSetConcurrency);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.prepareStatement(sql, resultSetType, resultSetConcurrency);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.prepareCall(sql, resultSetType, resultSetConcurrency);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public java.util.Map getTypeMap() throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                return physicalConn.getTypeMap();
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void setTypeMap(java.util.Map map) throws SQLException {
        if (isClosed) throw new SQLException("Connection is closed");
        try {
            synchronized (physicalConn) {
                physicalConn.setTypeMap(map);
            }
        } catch (SQLException sqlE) {
            managedConn.connectionErrorOccurred(this, sqlE);
            throw sqlE;
        }
    }

    public void setHoldability(int holdability) throws java.sql.SQLException {
        throw new SQLException("method setHoldability not implemented");
    }

    public int getHoldability() throws java.sql.SQLException {
        throw new SQLException("method getHoldability not implemented");
    }

    public java.sql.Savepoint setSavepoint() throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.Savepoint setSavepoint(String name) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public void rollback(java.sql.Savepoint savepoint) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public void releaseSavepoint(java.sql.Savepoint savepoint) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }

    public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws java.sql.SQLException {
        throw new SQLException("method not implemented");
    }
}

