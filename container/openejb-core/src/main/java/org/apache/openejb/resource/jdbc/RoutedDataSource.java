package org.apache.openejb.resource.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

public class RoutedDataSource implements DataSource {
    private static final String OPENEJB_RESOURCE_PREFIX = "openejb:Resource/";

    private Router delegate;

    public RoutedDataSource() {
        // no-op
    }

    public RoutedDataSource(String router) {
        setRouter(router);
    }

    public void setRouter(String router) {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(
                ContainerSystem.class);

        Object o = null;
        Context ctx = containerSystem.getJNDIContext();
        try {
            o = ctx.lookup(OPENEJB_RESOURCE_PREFIX + router);
        } catch (NamingException e) {
            throw new IllegalArgumentException("Can't find router [" + router
                    + "]", e);
        }

        if (o instanceof Router) {
            delegate = (Router) o;
        } else {
            throw new IllegalArgumentException(o + " is not a router");
        }
    }

    public PrintWriter getLogWriter() throws SQLException {
        if (ds() == null) {
            return null;
        }
        return ds().getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        if (ds() != null) {
            ds().setLogWriter(out);
        }
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        if (ds() != null) {
            ds().setLoginTimeout(seconds);
        }
    }

    public int getLoginTimeout() throws SQLException {
        if (ds() == null) {
            return -1;
        }
        return ds().getLoginTimeout();
    }

    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (ds() == null) {
            return null;
        }
        return (T) callByReflection(ds(), "unwrap",
                new Class<?>[] { Class.class },  new Object[] { iface });
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (ds() == null) {
            return false;
        }
        return (Boolean) callByReflection(ds(), "isWrapperFor",
                new Class<?>[] { Class.class },  new Object[] { iface });
    }

    public Connection getConnection() throws SQLException {
        return ds().getConnection();
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        return ds().getConnection(username, password);
    }

    public Router getDelegate() {
        if (delegate == null) {
            throw new IllegalStateException("a router has to be defined");
        }
        return delegate;
    }

    private DataSource ds() {
        return getDelegate().getDataSource();
    }

    /**
     * This method is used to avoir to fork two projects to implement
     * datasource of the jre5 and jre6
     *
     * @param ds the wrapped datasource
     * @param mtdName the method to call
     * @param paramTypes the parameter type
     * @param args the arguments
     * @return the return value of the method
     */
    private Object callByReflection(DataSource ds, String mtdName,
            Class<?>[] paramTypes, Object[] args) {
        Method mtd;
        try {
            mtd = ds.getClass().getDeclaredMethod(mtdName, paramTypes);
            return mtd.invoke(ds, args);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
