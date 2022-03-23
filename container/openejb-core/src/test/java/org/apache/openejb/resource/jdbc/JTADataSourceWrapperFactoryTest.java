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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class JTADataSourceWrapperFactoryTest {
    @Resource(name = "jta")
    private DataSource jta;

    @Resource(name = "raw")
    private DataSource raw;

    @Test
    public void test() throws SQLException {
        assertNotNull(raw);
        assertNotNull(jta);
        assertThat(jta, instanceOf(ManagedDataSource.class));

        boolean found = false;
        for (final ResourceInfo ri : SystemInstance.get().getComponent(OpenEjbConfiguration.class).facilities.resources) {
            if (ri.id.equals("jta")) {
                found = true;
                assertTrue(ri.types.contains("DataSource")); // otherwise jpa integration is broken
                break;
            }
        }
        assertTrue(found);
    }

    @Configuration
    public Properties configuration() {
        return new PropertiesBuilder()
            .p("raw", "new://Resource?class-name=" + MyDataSourceFactory.class.getName() + "&factory-name=create")

            .p("jta", "new://Resource?type=DataSource&class-name=org.apache.openejb.resource.jdbc.managed.JTADataSourceWrapperFactory&factory-name=create")
            .p("jta.delegate", "raw")
            .build();
    }

    @Module
    public Class<?>[] classes() {
        return new Class<?>[]{};
    }

    public static class MyDataSourceFactory {
        public DataSource create() {
            return DataSource.class.cast(
                Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{DataSource.class},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if ("hashCode".equals(method.getName())) {
                                return 0;
                            }
                            return null;
                        }
                    }
                )
            );
        }
    }
}
