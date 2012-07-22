package org.apache.openejb.resource.jdbc.pool;

import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.resource.jdbc.dbcp.BasicDataSource;
import org.apache.openejb.resource.jdbc.dbcp.BasicManagedDataSource;
import org.apache.openejb.resource.jdbc.dbcp.ManagedDataSourceWithRecovery;
import org.apache.openejb.resource.jdbc.dbcp.DbcpDataSource;
import org.apache.openejb.resource.jdbc.dbcp.DbcpManagedDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class DefaultDataSourceCreator implements DataSourceCreator {
    @Override // TODO: remove dbcp from here
    public DataSource managed(final String name, final DataSource ds) {
        return new DbcpManagedDataSource(name, ds);
    }

    @Override
    public DataSource poolManaged(final String name, final DataSource ds) {
        return new DbcpManagedDataSource(name, ds);
    }

    @Override
    public DataSource poolManaged(final String name, final String driver, final Properties properties) {
        final BasicManagedDataSource ds = new BasicManagedDataSource(name);
        ds.setDriverClassName(driver);
        return ds;
    }

    @Override
    public DataSource poolManagedWithRecovery(final String name, final XAResourceWrapper xaResourceWrapper, final String driver, final Properties properties) {
        final BasicManagedDataSource ds = new ManagedDataSourceWithRecovery(name, xaResourceWrapper);
        ds.setDriverClassName(driver);
        return ds;
    }

    @Override
    public DataSource pool(final String name, final DataSource ds) {
        return new DbcpDataSource(name, ds);
    }

    @Override
    public DataSource pool(String name, String driver) {
        final BasicDataSource ds = new BasicDataSource(name);
        ds.setDriverClassName(driver);
        return ds;
    }
}
