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
import java.sql.Statement;
import java.util.EventObject;

/**
 * A JDBC event. The event source will be the connection.
 *
 * @author Marc Prud'hommeaux
 * @author Abe White
 * @see JDBCListener
 */
@SuppressWarnings("serial")
public class JDBCEvent extends EventObject {

    /**
     * Type code indicating that a {@link Statement} is being prepared.
     */
    public static final short BEFORE_PREPARE_STATEMENT = 1;

    /**
     * Type code indicating that a {@link Statement} is being prepared.
     */
    public static final short AFTER_PREPARE_STATEMENT = 2;

    /**
     * Type code indicating that a {@link Statement} is being created.
     */
    public static final short BEFORE_CREATE_STATEMENT = 3;

    /**
     * Type code indicating that a {@link Statement} is being created.
     */
    public static final short AFTER_CREATE_STATEMENT = 4;

    /**
     * Type code indicating that a {@link Statement} is about to be executed.
     */
    public static final short BEFORE_EXECUTE_STATEMENT = 5;

    /**
     * Type code indicating that a {@link Statement} completed execution.
     */
    public static final short AFTER_EXECUTE_STATEMENT = 6;

    /**
     * Type code indicating that a {@link Connection} is about to be committed.
     */
    public static final short BEFORE_COMMIT = 7;

    /**
     * Type code indicating that a {@link Connection} was just committed.
     */
    public static final short AFTER_COMMIT = 8;

    /**
     * Type code indicating that a rollback is about to occur.
     */
    public static final short BEFORE_ROLLBACK = 9;

    /**
     * Type code indicating that a rollback just occured.
     */
    public static final short AFTER_ROLLBACK = 10;

    /**
     * Type code indicating that a connection was obtained. This does
     * not necessarily mean that the connection is new if pooling is enabled.
     */
    public static final short AFTER_CONNECT = 11;

    /**
     * Type code indicating that a connection was closed. This does
     * not necessarily mean that the underlying database connection was
     * severed if pooling is enabled.
     */
    public static final short BEFORE_CLOSE = 12;

    private final short type;
    private final long time;
    private final String sql;
    private final JDBCEvent associatedEvent;
    private final transient Statement statement;

    /**
     * Constructor.
     */
    public JDBCEvent(Connection source, short type, JDBCEvent associatedEvent,
        Statement statement, String sql) {
        super(source);
        this.type = type;
        this.time = System.currentTimeMillis();
        this.associatedEvent = associatedEvent;
        this.sql = sql;
        this.statement = statement;
    }

    /**
     * Return the event's type code.
     */
    public final short getType() {
        return type;
    }

    /**
     * Return the Connection for this event.
     */
    public final Connection getConnection() {
        return (Connection) getSource();
    }

    /**
     * Return the time the event was constructed.
     */
    public final long getTime() {
        return time;
    }

    /**
     * Return the associated {@link JDBCEvent} for this event.
     * For AFTER_XXX events, this will typically be the JDBCEvent
     * that was created in the BEFORE_XXX stage. This may be null when
     * an association is not appropriate for the event.
     */
    public final JDBCEvent getAssociatedEvent() {
        return associatedEvent;
    }

    /**
     * Return the SQL associated with this event; may be null.
     */
    public final String getSQL() {
        return sql;
    }

    /**
     * Return the Statement for this event, may be null for events
     * unrelated to Statement execution.
     */
    public final Statement getStatement() {
        return statement;
    }
}

