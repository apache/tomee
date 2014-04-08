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
package org.apache.openejb.resource.jdbc.dbcp;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.openejb.api.internal.Internal;
import org.apache.openejb.api.jmx.Description;
import javax.management.MBeanServer;
import org.apache.openejb.api.jmx.ManagedAttribute;
import org.apache.openejb.api.jmx.ManagedOperation;
import javax.management.ObjectName;
import org.apache.openejb.monitoring.DynamicMBeanWrapper;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;

// @MBean: don't put it since it is not a pojo
@Internal
@SuppressWarnings("UnusedDeclaration")
@Description("describe a datasource pool")
public class JMXBasicDataSource {
    private transient final org.apache.commons.dbcp.BasicDataSource ds;

    private transient ObjectName objectName;

    public JMXBasicDataSource(final String name, final org.apache.commons.dbcp.BasicDataSource ds) {
        this.ds = ds;

        if (LocalMBeanServer.isJMXActive()) {
            objectName = ObjectNameBuilder.uniqueName("datasources", name, ds);
            final MBeanServer server = LocalMBeanServer.get();
            try {
                if (server.isRegistered(objectName)) {
                    server.unregisterMBean(objectName);
                }
                server.registerMBean(new DynamicMBeanWrapper(this), objectName);
            } catch (Exception e) {
                e.printStackTrace(); // TODO
            }
        }
    }

    public void unregister() {
        if (objectName == null) {
            return;
        }

        try {
            LocalMBeanServer.get().unregisterMBean(objectName);
        } catch (Exception e) {
            // ignored
        }
    }

    @ManagedAttribute
    @Description("The class driver name.")
    public String getDriverClassName() {
        return ds.getDriverClassName();
    }

    @ManagedAttribute
    @Description("The connection URL to be passed to our JDBC driver to establish a connection.")
    public String getUrl() {
        return ds.getUrl();
    }

    @ManagedAttribute
    @Description("The SQL query that will be used to validate connections from this pool before returning them to the caller.")
    public String getValidationQuery() {
        return ds.getValidationQuery();
    }

    @ManagedAttribute
    @Description("The connection username to be passed to our JDBC driver to establish a connection.")
    public String getUsername() {
        return ds.getUsername();
    }

    @ManagedAttribute
    @Description("Timeout in seconds before connection validation queries fail.")
    public int getValidationQueryTimeout() {
        return ds.getValidationQueryTimeout();
    }

    @ManagedAttribute
    @Description("The initial number of connections that are created when the pool is started.")
    public int getInitialSize() {
        return ds.getInitialSize();
    }

    @ManagedAttribute
    @Description("The maximum number of active connections that can be allocated from this pool at the same time,"
            + " or negative for no limit.")
    public int getMaxActive() {
        return ds.getMaxActive();
    }

    @ManagedAttribute
    @Description("The maximum number of connections that can remain idle in the pool, without extra ones being"
            + "destroyed, or negative for no limit.")
    public int getMaxIdle() {
        return ds.getMaxIdle();
    }

    @ManagedAttribute
    @Description("The minimum number of active connections that can remain idle in the pool, without extra ones"
            + " being created when the evictor runs, or 0 to create none.")
    public int getMinIdle() {
        return ds.getMinIdle();
    }

    @ManagedAttribute
    @Description("The minimum number of active connections that can remain idle in the pool, without extra ones"
            + " being created when the evictor runs, or 0 to create none.")
    public int getNumTestsPerEvictionRun() {
        return ds.getNumTestsPerEvictionRun();
    }

    @ManagedAttribute
    @Description("The minimum amount of time an object may sit idle in the pool before it is eligible for eviction"
            + " by the idle object evictor (if any).")
    public long getMinEvictableIdleTimeMillis() {
        return ds.getMinEvictableIdleTimeMillis();
    }

    @ManagedAttribute
    @Description("The number of milliseconds to sleep between runs of the idle object evictor thread.")
    public long getTimeBetweenEvictionRunsMillis() {
        return ds.getTimeBetweenEvictionRunsMillis();
    }

    @ManagedAttribute
    @Description("The maximum number of open statements that can be allocated from the statement pool at the same time,"
            + " or non-positive for no limit.")
    public int getMaxOpenPreparedStatements() {
        return ds.getMaxOpenPreparedStatements();
    }

    @ManagedAttribute
    @Description("The maximum number of milliseconds that the pool will wait (when there are no available connections) "
            + "for a connection to be returned before throwing an exception, or <= 0 to wait indefinitely.")
    public long getMaxWait() {
        return ds.getMaxWait();
    }

    @ManagedAttribute
    @Description("The default auto-commit state of connections created by this pool.")
    public boolean getDefaultAutoCommit() {
        return ds.getDefaultAutoCommit();
    }

    @ManagedAttribute
    @Description("Prepared statement pooling for this pool.")
    public boolean getPoolPreparedStatements() {
        return ds.isPoolPreparedStatements();
    }

    @ManagedAttribute
    @Description("The indication of whether objects will be validated before being borrowed from the pool.")
    public boolean getTestOnBorrow() {
        return ds.getTestOnBorrow();
    }

    @ManagedAttribute
    @Description("The indication of whether objects will be validated before being returned to the pool.")
    public boolean getTestOnReturn() {
        return ds.getTestOnReturn();
    }

    @ManagedAttribute
    @Description("The indication of whether objects will be validated by the idle object evictor (if any).")
    public boolean getTestWhileIdle() {
        return ds.getTestWhileIdle();
    }

    @ManagedAttribute
    @Description("The default \"catalog\" of connections created by this pool.")
    public String getDefaultCatalog() {
        return ds.getDefaultCatalog();
    }

    @ManagedAttribute
    @Description("The default read-only state of connections created by this pool.")
    public boolean getDefaultReadOnly() {
        return ds.getDefaultReadOnly();
    }

    @ManagedAttribute
    @Description("The default TransactionIsolation state of connections created by this pool.")
    public int getDefaultTransactionIsolation() {
        return ds.getDefaultTransactionIsolation();
    }

    @ManagedOperation
    @Description("Execute the validation query.")
    public String executeValidationQuery() {
        final String query = ds.getValidationQuery();
        if (query == null || query.trim().isEmpty()) {
            return "no validation query defined";
        }

        final Connection conn;
        try {
            conn = ds.getConnection();
        } catch (SQLException e) {
            return e.getMessage();
        }

        Statement statement = null;
        try {
            statement = conn.createStatement();
            if (statement.execute(query)) {
                return "OK";
            }
            return "KO";
        } catch (SQLException e) {
            return e.getMessage();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // no-op
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // no-op
                }
            }
        }
    }

    @ManagedAttribute
    @Description("Set the class driver.")
    public void setDriverClassName(final String name) {
        ds.setDriverClassName(name);
    }

    @ManagedAttribute
    @Description("Set the connection URL.")
    public void setUrl(final String url) {
        ds.setUrl(url);
    }

    @ManagedAttribute
    @Description("Set the SQL validation query.")
    public void setValidationQuery(final String query) {
        ds.setValidationQuery(query);
    }

    @ManagedAttribute
    @Description("Set the connection username to be passed to our JDBC driver to establish a connection.")
    public void setUsername(final String user) {
        ds.setUsername(user);
    }

    @ManagedAttribute
    @Description("Set the timeout in seconds before connection validation queries fail.")
    public void setValidationQueryTimeout(final int timeout) {
        ds.setValidationQueryTimeout(timeout);
    }

    @ManagedAttribute
    @Description("Set the initial number of connections that are created when the pool is started.")
    public void setInitialSize(final int size) {
        ds.setInitialSize(size);
    }

    @ManagedAttribute
    @Description("Set the maximum number of active connections that can be allocated from this pool at the same time,"
            + " or negative for no limit.")
    public void setMaxActive(final int max) {
        ds.setMaxActive(max);
    }

    @ManagedAttribute
    @Description("Set the maximum number of connections that can remain idle in the pool, without extra ones being"
            + "destroyed, or negative for no limit.")
    public void setMaxIdle(final int max) {
        ds.setMaxIdle(max);
    }

    @ManagedAttribute
    @Description("Set the minimum number of active connections that can remain idle in the pool, without extra ones"
            + " being created when the evictor runs, or 0 to create none.")
    public void setMinIdle(final int min) {
        ds.setMinIdle(min);
    }

    @ManagedAttribute
    @Description("Set the minimum number of active connections that can remain idle in the pool, without extra ones"
            + " being created when the evictor runs, or 0 to create none.")
    public void setNumTestsPerEvictionRun(final int num) {
        ds.setNumTestsPerEvictionRun(num);
    }

    @ManagedAttribute
    @Description("Set the minimum amount of time an object may sit idle in the pool before it is eligible for eviction"
            + " by the idle object evictor (if any).")
    public void setMinEvictableIdleTimeMillis(final long time) {
        ds.setMinEvictableIdleTimeMillis(time);
    }

    @ManagedAttribute
    @Description("Set the number of milliseconds to sleep between runs of the idle object evictor thread.")
    public void setTimeBetweenEvictionRunsMillis(final long time) {
        ds.setTimeBetweenEvictionRunsMillis(time);
    }

    @ManagedAttribute
    @Description("Set the maximum number of open statements that can be allocated from the statement pool at the same time,"
            + " or non-positive for no limit.")
    public void setMaxOpenPreparedStatements(final int max) {
        ds.setMaxOpenPreparedStatements(max);
    }

    @ManagedAttribute
    @Description("Set the maximum number of milliseconds that the pool will wait (when there are no available connections) "
            + "for a connection to be returned before throwing an exception, or <= 0 to wait indefinitely.")
    public void setMaxWait(final long max) {
        ds.setMaxWait(max);
    }

    @ManagedAttribute
    @Description("Set the default auto-commit state of connections created by this pool.")
    public void setDefaultAutoCommit(final boolean auto) {
        ds.setDefaultAutoCommit(auto);
    }

    @ManagedAttribute
    @Description("Set the prepared statement pooling for this pool.")
    public void setPoolPreparedStatements(final boolean pool) {
        ds.setPoolPreparedStatements(pool);
    }

    @ManagedAttribute
    @Description("Set the indication of whether objects will be validated before being borrowed from the pool.")
    public void setTestOnBorrow(final boolean test) {
        ds.setTestOnBorrow(test);
    }

    @ManagedAttribute
    @Description("Set the indication of whether objects will be validated before being returned to the pool.")
    public void setTestOnReturn(final boolean test) {
        ds.setTestOnReturn(test);
    }

    @ManagedAttribute
    @Description("Set the indication of whether objects will be validated by the idle object evictor (if any).")
    public void setTestWhileIdle(final boolean test) {
        ds.setTestWhileIdle(test);
    }

    @ManagedAttribute
    @Description("The default \"catalog\" of connections created by this pool.")
    public void setDefaultCatalog(final String catalog) {
        ds.setDefaultCatalog(catalog);
    }

    @ManagedAttribute
    @Description("Set the default read-only state of connections created by this pool.")
    public void setDefaultReadOnly(final boolean ro) {
        ds.setDefaultReadOnly(ro);
    }

    @ManagedAttribute
    @Description("Set the default TransactionIsolation state of connections created by this pool ([NONE: 0, READ_COMMITTED: 2, READ_UNCOMMITTED: 4, SERIALIZABLE: 8]).")
    public void setDefaultTransactionIsolation(final int level) {
        ds.setDefaultTransactionIsolation(level);
    }
}
