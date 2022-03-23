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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
@Classes
@ContainerProperties({
    @ContainerProperties.Property(
        name = "DataSourceFactoryCustomHandlerTest",
        value = "new://Resource?type=DataSource"),
    @ContainerProperties.Property(
        name = "DataSourceFactoryCustomHandlerTest.TomEEProxyHandler",
        value = "org.apache.openejb.resource.jdbc.DataSourceFactoryCustomHandlerTest$TheHandler")
})
public class DataSourceFactoryCustomHandlerTest {
    @Resource
    private DataSource ds;

    @Test
    public void run() {
        assertTrue(Proxy.isProxyClass(ds.getClass()) && TheHandler.class.isInstance(Proxy.getInvocationHandler(ds)));
    }
    public static class TheHandler implements InvocationHandler {
        private final DataSource ds;

        public TheHandler(final DataSource ds) {
            this.ds = ds;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            return method.invoke(ds, args);
        }
    }
}
