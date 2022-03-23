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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class CustomPoolDataSourceTest {
    @Resource
    private DataSource ds;


    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("managed", "new://Resource?type=DataSource");
        p.put("managed.DataSourceCreator", CustomCreator.class.getName());
        p.put("managed.JtaManaged", "false");
        p.put("managed.Name", "custom");
        return p;
    }

    @Module
    public EjbJar app() throws Exception {
        return new EjbJar();
    }

    @Test
    public void checkCustomCreatorIsUsed() throws SQLException {
        assertNotNull(ds);
        assertTrue(Proxy.isProxyClass(ds.getClass()));
        assertTrue(ds instanceof CustomDataSource);
        assertEquals("custom", ((CustomDataSource) ds).name());
    }

    public static class CustomCreator extends PoolDataSourceCreator {
        @Override
        protected void doDestroy(final CommonDataSource dataSource) throws Throwable {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataSource pool(final String name, final DataSource ds, final Properties properties) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CommonDataSource pool(final String name, final String driver, final Properties properties) {
            return (CustomDataSource) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{CustomDataSource.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        if (method.getName().equals("name")) {
                            return properties.getProperty("Name");
                        }
                        if ("hashCode".equals(method.getName())) {
                            return properties.hashCode(); // don't care
                        }
                        return null;
                    }
                });
        }
    }

    public static interface CustomDataSource extends DataSource {
        String name();
    }
}
