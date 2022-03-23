/**
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
package org.apache.tomee.jdbc;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.resource.jdbc.cipher.PasswordCipher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(ApplicationComposer.class)
public class TomcatPoolCipherPasswordTest {
    private static final String URL = "jdbc:hsqldb:mem:cipher;hsqldb.tx=MVCC";
    private static final String USER = "sa";
    private static final String PASSWORD = "this one will be overriden if it works";
    private static final String TABLE = "PUBLIC.MANAGED_DATASOURCE_TEST";

    @Resource(name = "ciphered")
    private DataSource ds;

    public static class MockCipher implements PasswordCipher {
        @Override
        public char[] encrypt(final String plainPassword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String decrypt(final char[] encryptedPassword) {
            return "";
        }
    }

    @BeforeClass
    public static void createTable() throws SQLException, ClassNotFoundException {
        Class.forName("org.hsqldb.jdbcDriver");

        final Connection connection = DriverManager.getConnection(URL, USER, "");
        final Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE " + TABLE + "(ID INTEGER)");
        statement.close();
        connection.commit();
        connection.close();
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("openejb.jdbc.datasource-creator", TomEEDataSourceCreator.class.getName());

        p.put("ciphered", "new://Resource?type=DataSource");
        p.put("ciphered.DriverClassName", "org.hsqldb.jdbcDriver");
        p.put("ciphered.Url", URL);
        p.put("ciphered.UserName", USER);
        p.put("ciphered.Password", PASSWORD);
        p.put("ciphered.PasswordCipher", "Mock");
        p.put("ciphered.JtaManaged", "false");
        p.put("ciphered.JdbcInterceptors", "StatementCache(prepared=true)");
        return p;
    }

    @Module
    public EjbJar app() throws Exception {
        return new EjbJar();
    }

    @Test
    public void validConfig() throws SQLException {
        assertThat(ds, instanceOf(TomEEDataSourceCreator.TomEEDataSource.class));
        assertEquals(new MockCipher().decrypt(null), ((TomEEDataSourceCreator.TomEEDataSource) ds).getPoolProperties().getPassword());
    }
}

