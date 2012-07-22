package org.apache.openejb.resource.jdbc.pool;

import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.resource.jdbc.managed.ManagedDataSource;
import org.apache.openejb.resource.jdbc.managed.ManagedXADataSource;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.util.Properties;

public abstract class PoolDataSourceCreator implements DataSourceCreator {
    @Override
    public DataSource managed(final String name, final DataSource ds) {
        if (ds instanceof XADataSource) {
            return new ManagedXADataSource(ds);
        }
        return new ManagedDataSource(ds);
    }

    @Override // TODO: manage recovery
    public DataSource poolManagedWithRecovery(final String name, final XAResourceWrapper xaResourceWrapper, final String driver, final Properties properties) {
        throw new UnsupportedOperationException("TODO: implement it");
    }

    @Override
    public DataSource poolManaged(final String name, final DataSource ds) {
        return managed(name, pool(name, ds));
    }

    @Override
    public DataSource poolManaged(final String name, final String driver, final Properties properties) {
        return managed(name, pool(name, driver, properties));
    }
}
