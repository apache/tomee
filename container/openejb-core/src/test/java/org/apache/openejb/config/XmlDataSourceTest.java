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

package org.apache.openejb.config;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class XmlDataSourceTest {
    @EJB
    private Bean bean;

    @Resource(name = "java:comp/env/foo")
    private DataSource ds;

    @Test
    public void run() throws SQLException {
        bean.run();

        /* TODO: support it? this is kind of weird as a usage with jpa etc, can break a lot of things
        try (final Connection c = ds.getConnection()) {
            assertEquals("jdbc:hsqldb:mem:fake", c.getMetaData().getURL());
        }
        */
    }

    @Module
    public EjbJar app() {
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean statelessBean = ejbJar.addEnterpriseBean(new StatelessBean(Bean.class));
        final org.apache.openejb.jee.DataSource ds = new org.apache.openejb.jee.DataSource();
        ds.setName("java:comp/env/foo");
        ds.setUrl("jdbc:hsqldb:mem:override");
        ds.setClassName("org.hsqldb.jdbcDriver");
        statelessBean.getDataSourceMap().put("foo", ds);
        return ejbJar;
    }

    @Stateless
    @DataSourceDefinition(name = "java:comp/env/foo", url = "jdbc:hsqldb:mem:fake", className = "org.hsqldb.jdbcDriver")
    public static class Bean {
        @Resource(name = "java:comp/env/foo")
        private DataSource ds;

        public void run() throws SQLException {
            try (final Connection c = ds.getConnection()) {
                assertEquals("jdbc:hsqldb:mem:override", c.getMetaData().getURL());
            }
        }
    }
}
