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

import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBContext;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class ManagedDataSourceIsClosedTest {
    private static final String URL = "jdbc:hsqldb:mem:isclosed";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    @EJB
    private Persister persistManager;

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("openejb.jdbc.datasource-creator", "dbcp-alternative");

        p.put("managed", "new://Resource?type=DataSource");
        p.put("managed.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("managed.JdbcUrl", URL);
        p.put("managed.UserName", USER);
        p.put("managed.Password", PASSWORD);
        p.put("managed.JtaManaged", "true");
        return p;
    }

    @Module
    public SingletonBean app() throws Exception {
        return (SingletonBean) new SingletonBean(Persister.class).localBean();
    }

    @LocalBean
    @Singleton
    public static class Persister {
        @Resource(name = "managed")
        private DataSource ds;

        @Resource
        private EJBContext context;

        public void isClosed() throws SQLException {
            final Connection c = ds.getConnection();
            c.close();
            assertTrue(c.isClosed());
        }
    }

    @Test
    public void isClosed() throws SQLException {
        persistManager.isClosed();
    }
}
