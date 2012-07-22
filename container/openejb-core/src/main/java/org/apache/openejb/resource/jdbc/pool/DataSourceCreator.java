package org.apache.openejb.resource.jdbc.pool;

import org.apache.openejb.resource.XAResourceWrapper;

import javax.sql.DataSource;
import java.util.Properties;

public interface DataSourceCreator {
    DataSource managed(String name, DataSource ds);
    DataSource poolManaged(String name, DataSource ds);
    DataSource pool(String name, DataSource ds);
    DataSource poolManagedWithRecovery(String name, XAResourceWrapper xaResourceWrapper, String driver, Properties properties);
    DataSource poolManaged(String name, String driver, Properties properties);
    DataSource pool(String name, String driver, Properties properties);

    boolean hasCreated(Object object);
    void destroy(Object object) throws Throwable;
}
