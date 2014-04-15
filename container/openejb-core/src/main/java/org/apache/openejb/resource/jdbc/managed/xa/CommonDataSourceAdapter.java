/**
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
