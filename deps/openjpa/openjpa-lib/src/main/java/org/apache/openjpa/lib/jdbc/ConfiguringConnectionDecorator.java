/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connection decorator that can configure some properties of the
 * underlying connection. Understands the following properties:
 * <ul>
 * <li>QueryTimeout</li>
 * <li>TransactionIsolation({@link Connection} constants)</li>
 * <li>AutoCommit</li>
 * </ul>
 *
 * @author Abe White
 * @nojavadoc
 */
public class ConfiguringConnectionDecorator implements ConnectionDecorator {

    private int _isolation = -1;
    private int _queryTimeout = -1;
    private Boolean _autoCommit = null;

    /**
     * The number of seconds to wait for a query to execute before
     * terminating it.
     */
    public int getQueryTimeout() {
        return _queryTimeout;
    }

    /**
     * The number of seconds to wait for a query to execute before
     * terminating it.
     */
    public void setQueryTimeout(int timeout) {
        _queryTimeout = timeout;
    }

    /**
     * The transaction isolation level.
     */
    public int getTransactionIsolation() {
        return _isolation;
    }

    /**
     * The transaction isolation level.
     */
    public void setTransactionIsolation(int isolation) {
        _isolation = isolation;
    }

    /**
     * Whether auto commit should be on. Use null to keep
     * the connection's default setting. Defaults to null.
     */
    public Boolean getAutoCommit() {
        return _autoCommit;
    }

    /**
     * Whether auto commit should be on. Use null to keep
     * the connection's default setting. Defaults to null.
     */
    public void setAutoCommit(Boolean autoCommit) {
        _autoCommit = autoCommit;
    }

    public Connection decorate(Connection conn) throws SQLException {
        if (_isolation == Connection.TRANSACTION_NONE || _queryTimeout != -1
            || _autoCommit != null)
            conn = new ConfiguringConnection(conn);
        if (_isolation != -1 && _isolation != Connection.TRANSACTION_NONE)
            conn.setTransactionIsolation(_isolation);
        return conn;
    }

    /**
     * Decorator to configure connection components correctly.
     */
    public class ConfiguringConnection extends DelegatingConnection {

        private boolean _curAutoCommit = false;

        public ConfiguringConnection(Connection conn) throws SQLException {
            super(conn);
            if (_autoCommit != null) {
                _curAutoCommit = ConfiguringConnection.this.getAutoCommit();
                if (_curAutoCommit != _autoCommit.booleanValue())
                    setAutoCommit(_autoCommit.booleanValue());
            }
        }

        public void setAutoCommit(boolean auto) throws SQLException {
            if (_isolation != TRANSACTION_NONE) {
                super.setAutoCommit(auto);
                _curAutoCommit = auto;
            }
        }

        public void commit() throws SQLException {
            if (_isolation != TRANSACTION_NONE)
                super.commit();
            if (_autoCommit != null
                && _autoCommit.booleanValue() != _curAutoCommit)
                setAutoCommit(_autoCommit.booleanValue());
        }

        public void rollback() throws SQLException {
            if (_isolation != TRANSACTION_NONE)
                super.rollback();
            if (_autoCommit != null
                && _autoCommit.booleanValue() != _curAutoCommit)
                setAutoCommit(_autoCommit.booleanValue());
        }

        protected PreparedStatement prepareStatement(String sql, boolean wrap)
            throws SQLException {
            PreparedStatement stmnt = super.prepareStatement(sql, wrap);
            if (_queryTimeout != -1)
                stmnt.setQueryTimeout(_queryTimeout);
            return stmnt;
        }

        protected PreparedStatement prepareStatement(String sql, int rsType,
            int rsConcur, boolean wrap) throws SQLException {
            PreparedStatement stmnt = super.prepareStatement(sql, rsType,
                rsConcur, wrap);
            if (_queryTimeout != -1)
                stmnt.setQueryTimeout(_queryTimeout);
            return stmnt;
        }

        protected Statement createStatement(boolean wrap) throws SQLException {
            Statement stmnt = super.createStatement(wrap);
            if (_queryTimeout != -1)
                stmnt.setQueryTimeout(_queryTimeout);
            return stmnt;
        }

        protected Statement createStatement(int rsType, int rsConcur,
            boolean wrap) throws SQLException {
            Statement stmnt = super.createStatement(rsType, rsConcur, wrap);
            if (_queryTimeout != -1)
                stmnt.setQueryTimeout(_queryTimeout);
            return stmnt;
        }
    }
}
