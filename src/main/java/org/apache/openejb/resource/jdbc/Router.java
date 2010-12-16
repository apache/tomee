package org.apache.openejb.resource.jdbc;

import javax.sql.DataSource;

public interface Router {
    DataSource getDataSource();
}
