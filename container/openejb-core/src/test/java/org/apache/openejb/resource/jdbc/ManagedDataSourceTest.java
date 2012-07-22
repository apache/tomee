package org.apache.openejb.resource.jdbc;

import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.apache.openejb.resource.jdbc.pool.DbcpDataSourceCreator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore("to implement")
@RunWith(ApplicationComposer.class)
public class ManagedDataSourceTest {
    private static final String URL = "jdbc:hsqldb:mem:managed";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final String TABLE = "PUBLIC.MANAGED_DATASOURCE_TEST";

    @EJB
    private PersistManager persistManager;

    @BeforeClass
    public static void createTable() throws SQLException, ClassNotFoundException {
        Class.forName("org.hsqldb.jdbcDriver");

        final Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        final Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE " + TABLE + "(ID INTEGER)");
        statement.close();
        connection.commit();
        connection.close();
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("openejb.jdbc.datasource-creator", DbcpDataSourceCreator.class.getName());

        p.put("managed", "new://Resource?type=DataSource");
        p.put("managed.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("managed.JdbcUrl", URL);
        p.put("managed.UserName", USER);
        p.put("managed.Password", PASSWORD);
        p.put("managed.JtaManaged", "true");
        return p;
    }

    @Module public SingletonBean app() throws Exception {
        final SingletonBean bean = new SingletonBean(PersistManager.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @LocalBean
    @Singleton
    public static class PersistManager {
        @Resource(name = "managed")
        private DataSource ds;

        public void save() throws SQLException {
            save(1);
        }

        public void saveAndRollback() throws SQLException {
            save(2);
            throw new RuntimeException();
        }

        private void save(int id) throws SQLException {
            execute("INSERT INTO " + TABLE + "(ID) VALUES(" + id + ")");
        }

        private void execute(final String sql) throws SQLException {
            final Connection connection = ds.getConnection();
            final Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            connection.close();
        }
    }

    @Test
    public void commit() throws SQLException {
        persistManager.save();
        assertTrue(exists(1));
    }

    @Test
    public void rollback() throws SQLException {
        try {
            persistManager.saveAndRollback();
        } catch (Exception ignored) {
            System.out.println(ignored);
        }
        assertFalse(exists(2));
    }

    private static boolean exists(int id) throws SQLException {
        final Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        final Statement statement = connection.createStatement();
        final ResultSet result = statement.executeQuery("SELECT count(*) AS NB FROM " + TABLE + " WHERE ID = " + id);
        try {
            assertTrue(result.next());
            return result.getInt(1) == 1;
        } finally {
            statement.close();
            connection.close();
        }
    }
}
