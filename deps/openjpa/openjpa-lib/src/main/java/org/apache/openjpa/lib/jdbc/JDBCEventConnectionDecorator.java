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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.openjpa.lib.util.concurrent.AbstractConcurrentEventManager;

/**
 * Manages the firing of {@link JDBCEvent}s.
 *
 * @author Abe White
 * @nojavadoc
 */
@SuppressWarnings("serial")
public class JDBCEventConnectionDecorator extends AbstractConcurrentEventManager
    implements ConnectionDecorator {

    public Connection decorate(Connection conn) {
        if (!hasListeners())
            return conn;
        return new EventConnection(conn);
    }

    /**
     * Fire the given event to all listeners. Prevents creation of an
     * event object when there are no listeners.
     */
    private JDBCEvent fireEvent(Connection source, short type,
        JDBCEvent associatedEvent, Statement stmnt, String sql) {
        if (!hasListeners())
            return null;

        JDBCEvent event = new JDBCEvent(source, type, associatedEvent,
            stmnt, sql);
        fireEvent(event);
        return event;
    }

    /**
     * Fire the given event to all listeners.
     */
    protected void fireEvent(Object event, Object listener) {
        JDBCListener listen = (JDBCListener) listener;
        JDBCEvent ev = (JDBCEvent) event;
        switch (ev.getType()) {
            case JDBCEvent.BEFORE_PREPARE_STATEMENT:
                listen.beforePrepareStatement(ev);
                break;
            case JDBCEvent.AFTER_PREPARE_STATEMENT:
                listen.afterPrepareStatement(ev);
                break;
            case JDBCEvent.BEFORE_CREATE_STATEMENT:
                listen.beforeCreateStatement(ev);
                break;
            case JDBCEvent.AFTER_CREATE_STATEMENT:
                listen.afterCreateStatement(ev);
                break;
            case JDBCEvent.BEFORE_EXECUTE_STATEMENT:
                listen.beforeExecuteStatement(ev);
                break;
            case JDBCEvent.AFTER_EXECUTE_STATEMENT:
                listen.afterExecuteStatement(ev);
                break;
            case JDBCEvent.BEFORE_COMMIT:
                listen.beforeCommit(ev);
                break;
            case JDBCEvent.AFTER_COMMIT:
                listen.afterCommit(ev);
                break;
            case JDBCEvent.BEFORE_ROLLBACK:
                listen.beforeRollback(ev);
                break;
            case JDBCEvent.AFTER_ROLLBACK:
                listen.afterRollback(ev);
                break;
            case JDBCEvent.AFTER_CONNECT:
                listen.afterConnect(ev);
                break;
            case JDBCEvent.BEFORE_CLOSE:
                listen.beforeClose(ev);
                break;
        }
    }

    /**
     * Fires events as appropriate.
     */
    private class EventConnection extends DelegatingConnection {

        public EventConnection(Connection conn) {
            super(conn);
            fireEvent(getDelegate(), JDBCEvent.AFTER_CONNECT, null, null, null);
        }

        public void commit() throws SQLException {
            JDBCEvent before = fireEvent(getDelegate(),
                JDBCEvent.BEFORE_COMMIT, null, null, null);
            try {
                super.commit();
            } finally {
                fireEvent(getDelegate(), JDBCEvent.AFTER_COMMIT, before,
                    null, null);
            }
        }

        public void rollback() throws SQLException {
            JDBCEvent before = fireEvent(getDelegate(),
                JDBCEvent.BEFORE_ROLLBACK, null, null, null);
            try {
                super.rollback();
            } finally {
                fireEvent(getDelegate(), JDBCEvent.AFTER_ROLLBACK, before,
                    null, null);
            }
        }

        protected Statement createStatement(boolean wrap) throws SQLException {
            JDBCEvent before = fireEvent(getDelegate(),
                JDBCEvent.BEFORE_CREATE_STATEMENT, null, null, null);
            Statement stmnt = null;
            try {
                stmnt = new EventStatement(super.createStatement(false), 
                    EventConnection.this);
            } finally {
                fireEvent(getDelegate(), JDBCEvent.AFTER_CREATE_STATEMENT,
                    before, stmnt, null);
            }
            return stmnt;
        }

        protected Statement createStatement(int rsType, int rsConcur,
            boolean wrap) throws SQLException {
            JDBCEvent before = fireEvent(getDelegate(),
                JDBCEvent.BEFORE_CREATE_STATEMENT, null, null, null);
            Statement stmnt = null;
            try {
                stmnt = new EventStatement(super.createStatement
                    (rsType, rsConcur, false), EventConnection.this);
            } finally {
                fireEvent(getDelegate(), JDBCEvent.AFTER_CREATE_STATEMENT,
                    before, stmnt, null);
            }
            return stmnt;
        }

        protected PreparedStatement prepareStatement(String sql, boolean wrap)
            throws SQLException {
            JDBCEvent before = fireEvent(getDelegate(),
                JDBCEvent.BEFORE_PREPARE_STATEMENT, null, null, sql);
            PreparedStatement stmnt = null;
            try {
                stmnt = new EventPreparedStatement(super.prepareStatement
                    (sql, false), EventConnection.this, sql);
            } finally {
                fireEvent(getDelegate(), JDBCEvent.AFTER_PREPARE_STATEMENT,
                    before, stmnt, sql);
            }
            return stmnt;
        }

        protected PreparedStatement prepareStatement(String sql, int rsType,
            int rsConcur, boolean wrap) throws SQLException {
            JDBCEvent before = fireEvent(getDelegate(),
                JDBCEvent.BEFORE_PREPARE_STATEMENT, null, null, sql);
            PreparedStatement stmnt = null;
            try {
                stmnt = new EventPreparedStatement(super.prepareStatement
                    (sql, rsType, rsConcur, false), EventConnection.this, sql);
            } finally {
                fireEvent(getDelegate(), JDBCEvent.AFTER_PREPARE_STATEMENT,
                    before, stmnt, sql);
            }
            return stmnt;
        }

        public void close() throws SQLException {
            try {
                fireEvent(getDelegate(), JDBCEvent.BEFORE_CLOSE,
                    null, null, null);
            } finally {
                super.close();
            }
        }
    }

    /**
     * Fires events as appropriate.
     */
    private class EventPreparedStatement extends
            DelegatingPreparedStatement {

        private final EventConnection _conn;
        private final String _sql;

        public EventPreparedStatement(PreparedStatement ps,
            EventConnection conn, String sql) {
            super(ps, conn);
            _conn = conn;
            _sql = sql;
        }

        public int executeUpdate() throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), _sql);
            try {
                return super.executeUpdate();
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), _sql);
            }
        }

        protected ResultSet executeQuery(boolean wrap) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), _sql);
            try {
                return super.executeQuery(wrap);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), _sql);
            }
        }

        public int[] executeBatch() throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), _sql);
            try {
                return super.executeBatch();
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), _sql);
            }
        }

        public boolean execute() throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), _sql);
            try {
                return super.execute();
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), _sql);
            }
        }

        public int executeUpdate(String sql) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.executeUpdate(sql);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public int executeUpdate(String sql, int i) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.executeUpdate(sql, i);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public int executeUpdate(String sql, int[] ia) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.executeUpdate(sql, ia);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public int executeUpdate(String sql, String[] sa) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.executeUpdate(sql, sa);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public boolean execute(String sql) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.execute(sql);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public boolean execute(String sql, int i) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.execute(sql, i);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public boolean execute(String sql, int[] ia) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.execute(sql, ia);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public boolean execute(String sql, String[] sa) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.execute(sql, sa);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }
    }

    /**
     * Fires events as appropriate.
     */
    private class EventStatement extends DelegatingStatement {

        private final EventConnection _conn;

        public EventStatement(Statement stmnt, EventConnection conn) {
            super(stmnt, conn);
            _conn = conn;
        }

        public int executeUpdate(String sql) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.executeUpdate(sql);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        protected ResultSet executeQuery(String sql, boolean wrap)
            throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.executeQuery(sql, wrap);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public boolean execute(String sql) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.execute(sql);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public int executeUpdate(String sql, int i) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.executeUpdate(sql, i);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public int executeUpdate(String sql, int[] ia) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.executeUpdate(sql, ia);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public int executeUpdate(String sql, String[] sa) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.executeUpdate(sql, sa);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public boolean execute(String sql, int i) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.execute(sql, i);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public boolean execute(String sql, int[] ia) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.execute(sql, ia);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }

        public boolean execute(String sql, String[] sa) throws SQLException {
            JDBCEvent before = fireEvent(_conn.getDelegate(),
                JDBCEvent.BEFORE_EXECUTE_STATEMENT, null, getDelegate(), sql);
            try {
                return super.execute(sql, sa);
            } finally {
                fireEvent(_conn.getDelegate(),
                    JDBCEvent.AFTER_EXECUTE_STATEMENT, before,
                    getDelegate(), sql);
            }
        }
    }
}
