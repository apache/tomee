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

import org.apache.openejb.cipher.PasswordCipher;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.dbcp.BasicManagedDataSource;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.util.Strings;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBContext;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

@RunWith(ApplicationComposer.class)
public class CipheredPasswordDataSourceTest {
    private static final String URL = "jdbc:fake://sthg:3306";
    private static final String USER = "pete";
    private static final String ENCRYPTED_PASSWORD = "This is the encrypted value.";

    @EJB
    private Persister persistManager;

    @Resource
    private DataSource ds;

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("openejb.jdbc.datasource-creator", "dbcp");

        p.put("managed", "new://Resource?type=DataSource");
        p.put("managed.JdbcDriver", FakeDriver.class.getName());
        p.put("managed.JdbcUrl", URL);
        p.put("managed.UserName", USER);
        p.put("managed.Password", ENCRYPTED_PASSWORD);
        p.put("managed.PasswordCipher", EmptyPasswordCipher.class.getName());
        p.put("managed.JtaManaged", "true");
        p.put("managed.initialSize", "10");
        p.put("managed.maxActive", "10");
        p.put("managed.maxIdle", "10");
        p.put("managed.minIdle", "10");
        p.put("managed.maxWait", "200");
        p.put("managed.defaultAutoCommit", "false");
        p.put("managed.defaultReadOnly", "false");
        p.put("managed.testOnBorrow", "true");
        p.put("managed.testOnReturn", "true");
        p.put("managed.testWhileIdle", "true");

        return p;
    }

    @Module
    public EjbJar app() throws Exception {
        return new EjbJar()
                .enterpriseBean(new SingletonBean(Persister.class).localBean());

    }

    @Before
    public void createDatasource(){
        // This is to make sure TomEE created the data source.
        persistManager.save();
    }

    @Test
    public void rebuild() {
        // because of the exception at startup, the pool creating aborted
        // before fixing this bug, the password was already decrypted, but the data source field
        // wasn't yet initialized, so this.data source == null
        // but the next time we try to initialize it, it tries to decrypt and already decrypted password

        try {
            ds.getConnection();

        } catch (final SQLException e) {
            System.out.println(e.getMessage());
            // success - it throws the SQLException from the connect
            // but it does not fail with the IllegalArgumentException from the Password Cipher
        }
    }

    public static class EmptyPasswordCipher implements PasswordCipher {

        @Override
        public char[] encrypt(String plainPassword) {
            throw new RuntimeException("Should never be called in this test.");
        }

        @Override
        public String decrypt(char[] encryptedPassword) {
            System.out.println(String.format(">>> Decrypt password '%s'", new String(encryptedPassword)));

            // we want to know if the password as already been called
            if (encryptedPassword.length == 0) {
                throw new IllegalArgumentException("Can only decrypt a non empty string.");
            }

            return "";
        }
    }

    public static class FakeDriver implements java.sql.Driver {
        public boolean acceptsURL(final String url) throws SQLException {
            return false;
        }

        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }

        public Connection connect(final String url, final Properties info) throws SQLException {
            throw new SQLException("Pete said it first fails here!");
        }

        public int getMajorVersion() {
            return 0;
        }

        public int getMinorVersion() {
            return 0;
        }

        public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
            return new DriverPropertyInfo[0];
        }

        public boolean jdbcCompliant() {
            return false;
        }
    }

    @LocalBean
    @Singleton
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public static class Persister {

        @Resource(name = "managed")
        private DataSource ds;

        public void save() {
            try {
                ds.getConnection();

            } catch (SQLException e) {
                System.err.println("Generated SQL error > " + e.getMessage());
            }
        }
    }


}
