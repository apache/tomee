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

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Singleton;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Properties;

@RunWith(ApplicationComposer.class)
public class AlternateDriverJarEmbeddedDemo { // using AppComposer and Arquillian in the same build needs more config, not the purpose of this sample

    private static final String USER = "SA";
    private static final String PASSWORD = "";

    @Configuration
    public Properties config() {

        final File drivers = new File(new File("target"), "drivers").getAbsoluteFile();

        final Properties p = new Properties();
        p.put("openejb.jdbc.datasource-creator", "dbcp-alternative");

        File file = new File(drivers, "derby-10.10.1.1.jar");
        Assert.assertTrue("Failed to find: " + file, file.exists());

        p.put("JdbcOne", "new://Resource?type=DataSource&classpath="
                + file.getAbsolutePath().replace("\\", "/"));
        p.put("JdbcOne.JdbcDriver", "org.apache.derby.jdbc.EmbeddedDriver");
        p.put("JdbcOne.JdbcUrl", "jdbc:derby:memory:JdbcOne;create=true");
        p.put("JdbcOne.UserName", USER);
        p.put("JdbcOne.Password", PASSWORD);
        p.put("JdbcOne.JtaManaged", "false");

        file = new File(drivers, "derby-10.9.1.0.jar");
        Assert.assertTrue("Failed to find: " + file, file.exists());

        p.put("JdbcTwo", "new://Resource?type=DataSource&classpath="
                + file.getAbsolutePath().replace("\\", "/"));
        p.put("JdbcTwo.JdbcDriver", "org.apache.derby.jdbc.EmbeddedDriver");
        p.put("JdbcTwo.JdbcUrl", "jdbc:derby:memory:JdbcTwo;create=true");
        p.put("JdbcTwo.UserName", USER);
        p.put("JdbcTwo.Password", PASSWORD);
        p.put("JdbcTwo.JtaManaged", "false");
        return p;
    }

    @Module
    public EjbJar app() throws Exception {
        return new EjbJar()
                .enterpriseBean(new SingletonBean(JdbcOne.class).localBean())
                .enterpriseBean(new SingletonBean(JdbcTwo.class).localBean());
    }

    @EJB
    private JdbcOne one;

    @EJB
    private JdbcTwo two;

    @Test
    public void testBoth() throws Exception {
        Assert.assertEquals("Should be using 10.10.1.1 - (1458268)", "10.10.1.1 - (1458268)", one.getDriverVersion());
        Assert.assertEquals("Should be using 10.9.1.0 - (1344872)", "10.9.1.0 - (1344872)", two.getDriverVersion());
    }

    @LocalBean
    @Singleton
    public static class JdbcOne {

        @Resource(name = "JdbcOne")
        private DataSource ds;

        public String getDriverVersion() throws Exception {

            final Connection con = ds.getConnection();
            final DatabaseMetaData md = con.getMetaData();
            return md.getDriverVersion();
        }
    }

    @LocalBean
    @Singleton
    public static class JdbcTwo {

        @Resource(name = "JdbcTwo")
        private DataSource ds;

        public String getDriverVersion() throws Exception {

            final Connection con = ds.getConnection();
            final DatabaseMetaData md = con.getMetaData();
            return md.getDriverVersion();
        }
    }
}
