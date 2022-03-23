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
 *  limitations under the License.
 */
package org.superbiz.dynamicdatasourcerouting;

import org.apache.openejb.core.LocalInitialContextFactory;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * If you are using openejb.xml the test properties are:
 *
 *
 * <!-- Router and datasource -->
 * <Resource id="My Router" type="org.apache.openejb.router.test.DynamicDataSourceTest$DeterminedRouter" provider="org.routertest:DeterminedRouter">
 * DatasourceNames = database1 database2 database3
 * DefaultDataSourceName = database1
 * </Resource>
 * <Resource id="Routed Datasource" type="org.apache.openejb.resource.jdbc.Router" provider="org.router:RoutedDataSource">
 * Router = My Router
 * </Resource>
 *
 * <!-- real datasources -->
 * <Resource id="database1" type="DataSource">
 * JdbcDriver = org.hsqldb.jdbcDriver
 * JdbcUrl = jdbc:hsqldb:mem:db1
 * UserName = sa
 * Password
 * JtaManaged = true
 * </Resource>
 * <Resource id="database2" type="DataSource">
 * JdbcDriver = org.hsqldb.jdbcDriver
 * JdbcUrl = jdbc:hsqldb:mem:db2
 * UserName = sa
 * Password
 * JtaManaged = true
 * </Resource>
 * <Resource id="database3" type="DataSource">
 * JdbcDriver = org.hsqldb.jdbcDriver
 * JdbcUrl = jdbc:hsqldb:mem:db3
 * UserName = sa
 * Password
 * JtaManaged = true
 * </Resource>
 */
public class DynamicDataSourceTest {

    @Test
    public void route() throws Exception {
        String[] databases = new String[]{"database1", "database2", "database3"};

        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        // resources
        // datasources
        for (int i = 1; i <= databases.length; i++) {
            String dbName = databases[i - 1];
            properties.setProperty(dbName, "new://Resource?type=DataSource");
            dbName += ".";
            properties.setProperty(dbName + "JdbcDriver", "org.hsqldb.jdbcDriver");
            properties.setProperty(dbName + "JdbcUrl", "jdbc:hsqldb:mem:db" + i);
            properties.setProperty(dbName + "UserName", "sa");
            properties.setProperty(dbName + "Password", "");
            properties.setProperty(dbName + "JtaManaged", "true");
        }

        // router
        properties.setProperty("My Router", "new://Resource?provider=org.router:DeterminedRouter&type=" + DeterminedRouter.class.getName());
        properties.setProperty("My Router.DatasourceNames", "database1 database2 database3");
        properties.setProperty("My Router.DefaultDataSourceName", "database1");

        // routed datasource
        properties.setProperty("Routed Datasource", "new://Resource?provider=RoutedDataSource&type=" + DataSource.class.getName());
        properties.setProperty("Routed Datasource.Router", "My Router");

        Context ctx = EJBContainer.createEJBContainer(properties).getContext();
        RoutedPersister ejb = (RoutedPersister) ctx.lookup("java:global/dynamic-datasource-routing/RoutedPersister");
        for (int i = 0; i < 18; i++) {
            // persisting a person on database db -> kind of manual round robin
            String name = "record " + i;
            String db = databases[i % 3];
            ejb.persist(i, name, db);
        }

        // assert database records number using jdbc
        for (int i = 1; i <= databases.length; i++) {
            Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:db" + i, "sa", "");
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("select count(*) from PERSON");
            rs.next();
            assertEquals(6, rs.getInt(1));
            st.close();
            connection.close();
        }

        ctx.close();
    }
}

