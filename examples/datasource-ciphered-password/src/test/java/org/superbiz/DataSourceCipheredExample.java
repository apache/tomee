package org.superbiz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;
import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.openejb.resource.jdbc.PasswordCipher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

/**
 * @author Romain Manni-Bucau
 */
public class DataSourceCipheredExample {
    private static final String USER = DataSourceCipheredExample.class.getSimpleName();
    private static final String PASSWORD = "YouLLN3v3rFindM3";
    private static final String DATASOURCE_URL = "jdbc:hsqldb:mem:protected";

    @Resource private DataSource dataSource;

    @BeforeClass public static void addDatabaseUserWithPassword() throws Exception {
        sql("CREATE USER " + USER + " PASSWORD " + PASSWORD + ";");
    }

    @Test public void accessDatasource() throws Exception{
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
        Context context = EJBContainer.createEJBContainer(properties).getContext();
        context.bind("inject", this);

        // test the datasource
        assertNotNull(dataSource);
        assertNotNull(dataSource.getConnection());

        // closing the context
        context.close();
    }

    @Test public void accessDatasourceWithMyImplementation() throws Exception{
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
        Context context = EJBContainer.createEJBContainer(properties).getContext();
        context.bind("inject", this);

        // test the datasource
        assertNotNull(dataSource);
        assertNotNull(dataSource.getConnection());

        // closing the context
        context.close();
    }

    public static class ReverseEncryption implements PasswordCipher {
        @Override public char[] encrypt(String plainPassword) {
            return StringUtils.reverse(plainPassword).toCharArray();
        }

        @Override public String decrypt(char[] encryptedPassword) {
            return new String(encrypt(new String(encryptedPassword)));
        }
    }

    private static void sql(String query) throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        Connection conn = DriverManager.getConnection(DATASOURCE_URL, "sa", "");
        Statement st = conn.createStatement();
        st.executeUpdate(query);
        st.close();
        conn.close();
    }
}
