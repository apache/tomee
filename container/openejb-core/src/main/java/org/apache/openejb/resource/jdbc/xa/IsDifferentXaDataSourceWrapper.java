/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.resource.jdbc.xa;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class IsDifferentXaDataSourceWrapper implements XADataSource {
    private static final Class<?>[] API_CONNECTION = {XAConnection.class};
    private static final Class<?>[] API_RESOURCE = {XAResource.class};
    private final XADataSource delegate;

    public IsDifferentXaDataSourceWrapper(final XADataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return wrap(delegate.getXAConnection());
    }

    @Override
    public XAConnection getXAConnection(final String user, final String password) throws SQLException {
        return wrap(delegate.getXAConnection(user, password));
    }

    private XAConnection wrap(final XAConnection xaConnection) {
        return XAConnection.class.cast(Proxy.newProxyInstance(xaConnection.getClass().getClassLoader(), API_CONNECTION, new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                if ("getXAResource".equals(method.getName())) {
                    try {
                        final Object xaResource = method.invoke(xaConnection, args);
                        return Proxy.newProxyInstance(xaResource.getClass().getClassLoader(), API_RESOURCE, new InvocationHandler() {
                            @Override
                            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                                if ("isSameRM".equals(method.getName())) {
                                    return false; // that's the goal!
                                }
                                try {
                                    return method.invoke(xaResource, args);
                                } catch (final InvocationTargetException ite) {
                                    throw ite.getCause();
                                }
                            }
                        });
                    } catch (final InvocationTargetException ite) {
                        throw ite.getCause();
                    }
                }
                try {
                    return method.invoke(xaConnection, args);
                } catch (final InvocationTargetException ite) {
                    throw ite.getCause();
                }
            }
        }));
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }
}
