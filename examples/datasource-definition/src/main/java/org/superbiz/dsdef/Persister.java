package org.superbiz.dsdef;

import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.sql.DataSource;

@DataSourceDefinition(transactional = true,
        url = "jdbc:h2:mem:persister",
        className = "org.h2.jdbcx.JdbcDataSource",
        user = "sa",
        password = "",
        name = "java:app/jdbc/persister",
        initialPoolSize = 1,
        maxPoolSize = 3
)
public class Persister {
    @Resource
    private DataSource ds;

    public DataSource getDs() {
        return ds;
    }
}
