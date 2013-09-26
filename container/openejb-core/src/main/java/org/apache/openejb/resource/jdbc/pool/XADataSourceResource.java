package org.apache.openejb.resource.jdbc.pool;

import javax.naming.InitialContext;
import javax.sql.XADataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

public final class XADataSourceResource {
    private static final Class<?>[] XA_DATASOURCE_API = new Class<?>[]{ XADataSource.class };

    public static XADataSource proxy(final ClassLoader loader, final String xaDataSource) {
        return javax.sql.XADataSource.class.cast(Proxy.newProxyInstance(loader, XA_DATASOURCE_API, new LazyXADataSourceHandler(xaDataSource)));
    }

    private static class LazyXADataSourceHandler implements InvocationHandler {
        private final String name;
        private final AtomicReference<XADataSource> ref = new AtomicReference<XADataSource>();

        public LazyXADataSourceHandler(final String xaDataSource) {
            if (xaDataSource.startsWith("openejb:") || xaDataSource.startsWith("java:global")) {
                name = xaDataSource;
            } else {
                name = "openejb:Resource/" + xaDataSource;
            }
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            XADataSource instance = ref.get();
            if (instance == null) {
                synchronized (this) {
                    instance = ref.get();
                    if (instance == null) {
                        instance = XADataSource.class.cast(new InitialContext().lookup(name));
                        ref.set(instance);
                    }
                }
            }
            return method.invoke(instance, args);
        }
    }

    private XADataSourceResource() {
        // no-op
    }
}
