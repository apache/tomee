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

package org.apache.openejb.resource.jdbc.logging;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class LoggingSqlConnection implements InvocationHandler {
    private static final Class<?>[] INTERFACES_STATEMENT = new Class<?>[]{Statement.class};
    private static final Class<?>[] INTERFACES_PREPARED = new Class<?>[]{PreparedStatement.class};
    private static final Class<?>[] INTERFACES_CALLABLE = new Class<?>[]{CallableStatement.class};

    private final Connection delegate;
    private final String[] packages;

    public LoggingSqlConnection(final Connection connection, final String[] debugPackages) {
        this.delegate = connection;
        this.packages = debugPackages;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Object result;
        try {
            result = method.invoke(delegate, args);
        } catch (final InvocationTargetException ite) {
            throw ite.getCause();
        }

        final String mtd = method.getName();

        if ("createStatement".equals(mtd)) {
            return Proxy.newProxyInstance(delegate.getClass().getClassLoader(), INTERFACES_STATEMENT,
                new LoggingSqlStatement((Statement) result, packages));
        }

        if ("prepareStatement".equals(mtd)) {
            return Proxy.newProxyInstance(delegate.getClass().getClassLoader(), INTERFACES_PREPARED,
                new LoggingPreparedSqlStatement((PreparedStatement) result, (String) args[0], packages));
        }

        if ("prepareCall".equals(mtd)) {
            return Proxy.newProxyInstance(delegate.getClass().getClassLoader(), INTERFACES_CALLABLE,
                new LoggingCallableSqlStatement((CallableStatement) result, (String) args[0], packages));
        }

        return result;
    }
}
