package org.apache.openejb.resource.jdbc.dbcp;

import org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator;

import javax.sql.DataSource;
import java.util.Properties;

// just a sample showing how to implement a datasourcecreator
// this one will probably not be used since dbcp has already the integration we need
public class DbcpDataSourceCreator extends PoolDataSourceCreator {
    @Override
    public DataSource pool(final String name, final DataSource ds, Properties properties) {
        return build(DbcpDataSource.class, new DbcpDataSource(name, ds), properties);
    }

    @Override
    public DataSource pool(final String name, final String driver, final Properties properties) {
        if (!properties.containsKey("JdbcDriver")) {
            properties.setProperty("driverClassName", driver);
        }
        properties.setProperty("name", name);

        final BasicDataSource ds = build(BasicDataSource.class, properties);
        ds.setDriverClassName(driver);
        return ds;
    }

    @Override
    protected void doDestroy(DataSource dataSource) throws Throwable {
        ((org.apache.commons.dbcp.BasicDataSource) dataSource).close();
    }
}
