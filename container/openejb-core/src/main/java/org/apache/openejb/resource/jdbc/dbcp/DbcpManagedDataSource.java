package org.apache.openejb.resource.jdbc.dbcp;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp.managed.LocalXAConnectionFactory;
import org.apache.commons.dbcp.managed.TransactionRegistry;
import org.apache.commons.dbcp.managed.XAConnectionFactory;
import org.apache.openejb.resource.jdbc.DataSourceHelper;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.lang.reflect.Field;
import java.sql.SQLException;

public class DbcpManagedDataSource extends BasicManagedDataSource {

    private final DataSource dataSource;

    public DbcpManagedDataSource(final String name, final DataSource dataSource) {
        super(name);
        this.dataSource = dataSource;
    }

    @Override
    public void setJdbcUrl(String url) {
        try {
            DataSourceHelper.setUrl(this, url);
        } catch (Throwable e1) {
            super.setUrl(url);
        }
    }

    @Override
    protected ConnectionFactory createConnectionFactory() throws SQLException {

        if (dataSource instanceof XADataSource) {

            // Create the XAConectionFactory using the XA data source
            XADataSource xaDataSourceInstance = (XADataSource) dataSource;
            XAConnectionFactory xaConnectionFactory = new DataSourceXAConnectionFactory(getTransactionManager(), xaDataSourceInstance, username, password);
            setTransactionRegistry(xaConnectionFactory.getTransactionRegistry());
            return xaConnectionFactory;

        } else {

            // If xa data source is not specified a DriverConnectionFactory is created and wrapped with a LocalXAConnectionFactory
            ConnectionFactory connectionFactory = new DataSourceConnectionFactory(dataSource, username, password);
            XAConnectionFactory xaConnectionFactory = new LocalXAConnectionFactory(getTransactionManager(), connectionFactory);
            setTransactionRegistry(xaConnectionFactory.getTransactionRegistry());
            return xaConnectionFactory;
        }
    }

    public void setTransactionRegistry(TransactionRegistry registry) {
        try {
            final Field field = org.apache.commons.dbcp.managed.BasicManagedDataSource.class.getDeclaredField("transactionRegistry");
            field.setAccessible(true);
            field.set(this, registry);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}
