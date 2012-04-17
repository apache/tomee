package org.apache.openejb.resource.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.management.Description;
import javax.management.MBeanServer;
import javax.management.ManagedAttribute;
import javax.management.ManagedOperation;
import javax.management.ObjectName;
import org.apache.openejb.monitoring.DynamicMBeanWrapper;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;

// @MBean: don't put it since it is not a pojo
@Description("describe a datasource pool")
public class JMXBasicDataSource {
    private transient final org.apache.commons.dbcp.BasicDataSource ds;

    private transient ObjectName objectName;

    public JMXBasicDataSource(final String name, final org.apache.commons.dbcp.BasicDataSource ds) {
        this.ds = ds;

        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("ObjectType", "datasources");
        jmxName.set("DataSource", name);
        objectName = jmxName.build();

        final MBeanServer server = LocalMBeanServer.get();
        if (server.isRegistered(objectName)) {
            jmxName.set("DataSource", name + "(" + System.identityHashCode(this) + ")");
            objectName = jmxName.build();
        }
        try {
            server.registerMBean(new DynamicMBeanWrapper(this), objectName);
        } catch (Exception e) {
            e.printStackTrace(); // TODO
        }
    }

    public void unregister() {
        try {
            LocalMBeanServer.get().unregisterMBean(objectName);
        } catch (Exception e) {
            // ignored
        }
    }

    @ManagedAttribute
    @Description("The class loader instance to use to load the JDBC driver.")
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
}
