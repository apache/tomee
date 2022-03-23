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
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import jakarta.annotation.Resource;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SimpleLog
@Classes(cdi = true, value = JBossLikeDataSourceInjectionTest.IWantInjection.class)
@RunWith(ApplicationComposer.class)
public class JBossLikeDataSourceInjectionTest {
    private static final String URL = "jdbc:hsqldb:mem:jbosslike";

    @Inject
    private IWantInjection persistManager;

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("openejb.log.async", "false");
        p.put("noise", "new://Resource?type=DataSource"); // to potential fail auto adjusting
        p.put("java:/jdbc/DefaultDS", "new://Resource?type=DataSource");
        p.put("java:/jdbc/DefaultDS.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("java:/jdbc/DefaultDS.JdbcUrl", URL);
        p.put("java:/jdbc/DefaultDS.UserName", "sa");
        return p;
    }

    @LocalBean
    @Singleton
    public static class IWantInjection {
        @Resource(name = "java:/jdbc/DefaultDS")
        private DataSource ds;


        public void check() throws SQLException {
            assertNotNull(ds);
            final Connection c = ds.getConnection();
            assertEquals(URL, c.getMetaData().getURL());
            c.close();
            assertTrue(c.isClosed());
        }
    }

    @Test
    public void isClosed() throws SQLException {
        persistManager.check();
    }
}
