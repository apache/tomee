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
import org.apache.openejb.resource.jdbc.router.FailOverRouter;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import javax.sql.DataSource;

import static org.apache.openejb.resource.jdbc.FailOverRouters.datasource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class JtaFailOverRouterTest {
    @EJB
    private JtaWrapper wrapper;

    @Test
    public void test() throws SQLException {
        int i = 2;
        for (int it = 0; it < 6; it++) {
            wrapper.inTx("jdbc:hsqldb:mem:fo" + i);
            i = 1 + (i % 2);
        }
    }

    @Configuration
    public Properties configuration() {
        return datasource(datasource(new PropertiesBuilder(), "fo1"), "fo2")
            .property("fo1.JtaManaged", "true")
            .property("fo1.accessToUnderlyingConnectionAllowed", "true")
            .property("fo2.JtaManaged", "true")
            .property("fo2.accessToUnderlyingConnectionAllowed", "true")
            .property("router", "new://Resource?class-name=" + FailOverRouter.class.getName())
            .property("router.datasourceNames", "fo1,fo2")
            .property("router.strategy", "reverse")
            .property("routedDs", "new://Resource?provider=RoutedDataSource&type=DataSource")
            .property("routedDs.router", "router")
            .build();
    }

    @Module
    public Class<?>[] classes() {
        return new Class<?>[]{JtaWrapper.class};
    }

    @Singleton
    public static class JtaWrapper {
        @Resource(name = "routedDs")
        private DataSource ds;

        public void inTx(final String url) {
            Connection firstConnection;
            try {
                firstConnection = org.apache.commons.dbcp2.managed.ManagedConnection.class.cast(ds.getConnection()).getInnermostDelegate();
                assertEquals(url, firstConnection.getMetaData().getURL());
                for (int i = 0; i < 4; i++) { // 4 is kind of random, > 2 is enough
                    final Connection anotherConnection = org.apache.commons.dbcp2.managed.ManagedConnection.class.cast(ds.getConnection()).getInnermostDelegate();
                    assertSame(firstConnection, anotherConnection); // in tx so should be the same ds and if the ds is JtaManaged same anotherConnection
                    assertEquals(url, anotherConnection.getMetaData().getURL());
                }
            } catch (final SQLException e) {
                fail(e.getMessage());
            }
        }
    }
}
