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
package org.superbiz;

import org.apache.commons.lang3.StringUtils;
import org.apache.openejb.resource.jdbc.cipher.PasswordCipher;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.annotation.Resource;
import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public class DataSourceCipheredExampleTest {

    private static final String USER = DataSourceCipheredExampleTest.class.getSimpleName().toUpperCase();
    private static final String PASSWORD = "YouLLN3v3rFindM3";
    private static final String DATASOURCE_URL = "jdbc:hsqldb:mem:protected";

    @Resource
    private DataSource dataSource;

    @BeforeClass
    public static void addDatabaseUserWithPassword() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        Connection conn = DriverManager.getConnection(DATASOURCE_URL, "sa", "");
        conn.setAutoCommit(true);
        Statement st = conn.createStatement();
        st.executeUpdate("CREATE USER " + USER + " PASSWORD '" + PASSWORD + "';");
        st.close();
        conn.commit();
        conn.close();
    }

    @Test
    public void accessDatasource() throws Exception {
        // define the datasource
        Properties properties = new Properties();
        properties.setProperty("ProtectedDatasource", "new://Resource?type=DataSource");
        properties.setProperty("ProtectedDatasource.JdbcDriver", "org.hsqldb.jdbcDriver");
        properties.setProperty("ProtectedDatasource.JdbcUrl", DATASOURCE_URL);
        properties.setProperty("ProtectedDatasource.UserName", USER);
        properties.setProperty("ProtectedDatasource.Password", "fEroTNXjaL5SOTyRQ92x3DNVS/ksbtgs");
        properties.setProperty("ProtectedDatasource.PasswordCipher", "Static3DES");
        properties.setProperty("ProtectedDatasource.JtaManaged", "true");

        // start the context and makes junit test injections
        EJBContainer container = EJBContainer.createEJBContainer(properties);
        Context context = container.getContext();
        context.bind("inject", this);

        // test the datasource
        assertNotNull(dataSource);
        assertNotNull(dataSource.getConnection());

        // closing the context
        container.close();
    }

    @Test
    public void accessDatasourceWithMyImplementation() throws Exception {
        // define the datasource
        Properties properties = new Properties();
        properties.setProperty("ProtectedDatasource", "new://Resource?type=DataSource");
        properties.setProperty("ProtectedDatasource.JdbcDriver", "org.hsqldb.jdbcDriver");
        properties.setProperty("ProtectedDatasource.JdbcUrl", "jdbc:hsqldb:mem:protected");
        properties.setProperty("ProtectedDatasource.UserName", USER);
        properties.setProperty("ProtectedDatasource.Password", "3MdniFr3v3NLLuoY");
        properties.setProperty("ProtectedDatasource.PasswordCipher", "reverse");
        properties.setProperty("ProtectedDatasource.JtaManaged", "true");

        // start the context and makes junit test injections
        EJBContainer container = EJBContainer.createEJBContainer(properties);
        Context context = container.getContext();
        context.bind("inject", this);

        // test the datasource
        assertNotNull(dataSource);
        assertNotNull(dataSource.getConnection());

        // closing the context
        container.close();
    }

    public static class ReverseEncryption implements PasswordCipher {

        @Override
        public char[] encrypt(String plainPassword) {
            return StringUtils.reverse(plainPassword).toCharArray();
        }

        @Override
        public String decrypt(char[] encryptedPassword) {
            return new String(encrypt(new String(encryptedPassword)));
        }
    }
}
