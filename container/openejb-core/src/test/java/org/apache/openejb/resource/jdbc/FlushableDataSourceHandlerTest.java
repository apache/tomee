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

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.dbcp.BasicDataSource;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class FlushableDataSourceHandlerTest {
    @Resource
    private DataSource ds;

    @Test
    public void checkIt() throws IOException {
        assertThat(ds, instanceOf(Flushable.class));
        assertThat(ds, instanceOf(DataSource.class));
        final FlushableDataSourceHandler handler = FlushableDataSourceHandler.class.cast(Proxy.getInvocationHandler(ds));
        final CommonDataSource delegate = handler.getDelegate();
        assertNotNull(delegate);
        assertFalse(BasicDataSource.class.cast(delegate).isClosed());
        Flushable.class.cast(ds).flush();
        assertTrue(BasicDataSource.class.cast(delegate).isClosed());
        final CommonDataSource newDelegate = handler.getDelegate();
        assertFalse(BasicDataSource.class.cast(newDelegate).isClosed());
        assertNotSame(newDelegate, delegate);
    }

    @Configuration
    public Properties configuration() {
        return new PropertiesBuilder()
            .p("ds", "new://Resource?type=DataSource")
            .p("ds.flushable", "true")
            .p("ds.jtaManaged", "false")
            .build();
    }

    @Module
    public EjbJar jar() {
        return new EjbJar();
    }
}
