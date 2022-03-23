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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *
 */

package org.apache.openejb.assembler.classic;

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
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class DataSourceDefinitionWithEjbJarXmlTest {
    private static final String DATASOURCE_NAME = "jdbc/database";
    private static final String EXPECTED_USER_NAME = "expected_user_name";
    private static final String DRIVER_CLASS = "org.hsqldb.jdbcDriver";

    @Resource(name = DATASOURCE_NAME)
    private javax.sql.DataSource dataSource;

    @EJB
    private DataSourceBean dataSourceBean;

    @Test
    public void testCorrectDefinitionFromEjb() throws Exception {
        final String userName = dataSourceBean.getDataSourceUserName();
        assertEquals(EXPECTED_USER_NAME, userName);
    }

    @Test
    public void testCorrectDefinitionInjected() throws Exception {
        final String userName;
        try (Connection connection = dataSource.getConnection()) {
            userName = connection.getMetaData().getUserName();
        }
        assertEquals(EXPECTED_USER_NAME, userName);
    }

    @Module
    public EjbJar initModule() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean statelessBean = ejbJar.addEnterpriseBean(new StatelessBean(DataSourceBean.class));
        final org.apache.openejb.jee.DataSource ds = new org.apache.openejb.jee.DataSource();
        ds.setUser(EXPECTED_USER_NAME);
        ds.setName("java:comp/env/" + DATASOURCE_NAME);
        ds.setUrl("jdbc:hsqldb:mem:test");
        ds.setClassName(DRIVER_CLASS);
        statelessBean.getDataSourceMap().put(DATASOURCE_NAME, ds);
        return ejbJar;
    }


    @Stateless
    @DataSourceDefinition(name = DATASOURCE_NAME, className = DRIVER_CLASS, user = "replace_me_from_ejbjar_xml")
    public static class DataSourceBean {
        @Resource(name = "jdbc/database")
        private javax.sql.DataSource dataSource;

        public String getDataSourceUserName() throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                return connection.getMetaData().getUserName();
            }
        }
    }

}
