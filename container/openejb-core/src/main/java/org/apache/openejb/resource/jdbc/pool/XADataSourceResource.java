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

package org.apache.openejb.resource.jdbc.pool;

import javax.naming.InitialContext;
import javax.sql.XADataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

public final class XADataSourceResource {
    private static final Class<?>[] XA_DATASOURCE_API = new Class<?>[]{XADataSource.class};

    public static XADataSource proxy(final ClassLoader loader, final String xaDataSource) {
        return XADataSource.class.cast(Proxy.newProxyInstance(loader, XA_DATASOURCE_API, new LazyXADataSourceHandler(xaDataSource)));
    }

    private static class LazyXADataSourceHandler implements InvocationHandler {
        private final String name;
        private final AtomicReference<XADataSource> ref = new AtomicReference<>();

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
