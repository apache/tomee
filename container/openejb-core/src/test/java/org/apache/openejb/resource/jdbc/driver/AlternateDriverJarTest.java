/**
 * Tomitribe Confidential
 *
 * Copyright(c) Tomitribe Corporation. 2014
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 *
 * Author: agumbrecht@tomitribe.com
 * Date: 28.03.2014
 * Time: 17:27
 */
package org.apache.openejb.resource.jdbc.driver;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Properties;

@RunWith(ApplicationComposer.class)
public class AlternateDriverJarTest {

    private static final String USER = "SA";
    private static final String PASSWORD = "";

    @Configuration
    public Properties config() {

        final File drivers = new File(new File(System.getProperty("openejb.home")).getParentFile(), "drivers").getAbsoluteFile();

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

        final String oneDriverVersion = one.getDriverVersion();
        System.out.println("oneDriverVersion = " + oneDriverVersion);
        Assert.assertEquals("Should be using 10.10.1.1 - (1458268)", "10.10.1.1 - (1458268)", oneDriverVersion);

        final String twoDriverVersion = two.getDriverVersion();
        System.out.println("twoDriverVersion = " + twoDriverVersion);
        Assert.assertEquals("Should be using 10.9.1.0 - (1344872)", "10.9.1.0 - (1344872)", twoDriverVersion);
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
