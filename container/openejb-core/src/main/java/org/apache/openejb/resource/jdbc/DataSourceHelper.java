package org.apache.openejb.resource.jdbc;

import javax.sql.DataSource;
import java.lang.reflect.Method;

public final class DataSourceHelper {
    private DataSourceHelper() {
        // no-op
    }

    public static void setUrl(final DataSource dataSource, final String url, final ClassLoader classLoader, final String clazz, final String method) throws Exception {
        final Class<?> loadedClass = classLoader.loadClass(clazz);
        final Method setUrl = loadedClass.getMethod(method, String.class);
        setUrl.setAccessible(true);
        setUrl.invoke(dataSource, url);
    }

    public static void setUrl(final DataSource dataSource, final String url) throws Exception {
        // TODO This is a big whole and we will need to rework this
        if (url.contains("jdbc:derby:")) {
            DataSourceHelper.setUrl(dataSource, url.replace("jdbc:derby:", ""), dataSource.getClass().getClassLoader(), "org.apache.derby.jdbc.EmbeddedDataSource", "setDatabaseName");
        } else {
            DataSourceHelper.setUrl(dataSource, url, dataSource.getClass().getClassLoader(), "org.hsqldb.jdbc.JDBCDataSource", "setDatabase");
        }
    }
}
