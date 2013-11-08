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

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.NoneLogFactory;
import org.apache.openjpa.lib.util.Localizer;

/**
 * Provies basic logging facilities to a DataSource.
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class DataSourceLogs {

    private static final Localizer _loc =
        Localizer.forPackage(DataSourceLogs.class);

    private Log _jdbcLog = null;
    private Log _sqlLog = null;

    public DataSourceLogs() {
    }

    /**
     * The log to write JDBC messages to.
     */
    public Log getJDBCLog() {
        return (_jdbcLog == null) ? NoneLogFactory.NoneLog.getInstance()
            : _jdbcLog;
    }

    /**
     * The log to write JDBC messages to.
     */
    public void setJDBCLog(Log log) {
        _jdbcLog = log;
    }

    /**
     * Return true if JDBC logging is enabled.
     */
    public boolean isJDBCEnabled() {
        return (_jdbcLog != null && _jdbcLog.isTraceEnabled());
    }

    /**
     * The log to write SQL messages to.
     */
    public Log getSQLLog() {
        return (_sqlLog == null) ? NoneLogFactory.NoneLog.getInstance()
            : _sqlLog;
    }

    /**
     * The log to write SQL messages to.
     */
    public void setSQLLog(Log log) {
        _sqlLog = log;
    }

    /**
     * Return true if SQL logging is enabled.
     */
    public boolean isSQLEnabled() {
        return (_sqlLog != null && _sqlLog.isTraceEnabled());
    }

    /**
     * Log a JDBC message on behalf of the given connection.
     */
    public void logJDBC(String msg, Connection conn) {
        log(msg, conn, _jdbcLog);
    }

    /**
     * Log a JDBC message on behalf of the given connection.
     */
    public void logJDBC(String msg, long startTime, Connection conn) {
        log(msg, conn, _jdbcLog, startTime);
    }

    /**
     * Log a SQL message on behalf of the given connection.
     */
    public void logSQL(String msg, Connection conn) {
        log(msg, conn, _sqlLog);
    }

    /**
     * Log a SQL message on behalf of the given connection.
     */
    public void logSQL(String msg, long startTime, Connection conn) {
        log(msg, conn, _sqlLog, startTime);
    }

    /**
     * Log a message to the given logger.
     */
    private static void log(String msg, Connection conn, Log log) {
        log(msg, conn, log, -1);
    }

    /**
     * Log a message to the given logger.
     */
    private static void log(String msg, Connection conn,
        Log log, long startTime) {
        if (log == null || !log.isTraceEnabled())
            return;

        long totalTime = -1;
        if (startTime != -1)
            totalTime = System.currentTimeMillis() - startTime;

        StringBuilder buf = new StringBuilder(25 + msg.length());
        buf.append("<t ").append(Thread.currentThread().hashCode());
        if (conn != null)
            buf.append(", ").append(conn);
        buf.append("> ");

        // if the time != -1, append time profiling information
        if (totalTime != -1)
            buf.append("[").append(totalTime).append(" ms] ");

        buf.append(msg);
        log.trace(_loc.get("datasource-trace-data", buf.toString()));
    }
}

