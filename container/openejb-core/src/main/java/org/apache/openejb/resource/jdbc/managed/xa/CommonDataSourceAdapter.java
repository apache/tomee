package org.apache.openejb.resource.jdbc.managed.xa;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class CommonDataSourceAdapter implements InvocationHandler {
    private final CommonDataSource delegate;

    public CommonDataSourceAdapter(final CommonDataSource ds) {
        this.delegate = ds;
    }

    public static DataSource wrap(final CommonDataSource ds) {
        return DataSource.class.cast(Proxy.newProxyInstance(ds.getClass().getClassLoader(), new Class<?>[] { DataSource.class }, new CommonDataSourceAdapter(ds)));
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return method.invoke(delegate, args); // we suppose missing methods are not called - it is the case thanks to ManagedXADataSource
    }
}
