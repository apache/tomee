package org.apache.openejb.resource.jdbc.dbcp;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.openejb.resource.jdbc.DataSourceHelper;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DbcpDataSource extends BasicDataSource {

    private final DataSource dataSource;

    public DbcpDataSource(final String name, final DataSource dataSource) {
        super(name);
        this.dataSource = dataSource;
    }

    @Override
    protected ConnectionFactory createConnectionFactory() throws SQLException {
        return new DataSourceConnectionFactory(dataSource, username, password);
    }

    @Override
    public void setJdbcUrl(String url) {
        try {
            DataSourceHelper.setUrl(this, url);
        } catch (Throwable e1) {
            super.setUrl(url);
        }
    }
}
