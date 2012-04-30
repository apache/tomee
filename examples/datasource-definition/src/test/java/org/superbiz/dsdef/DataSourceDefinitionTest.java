package org.superbiz.dsdef;

import org.apache.openejb.resource.jdbc.DataSourceFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.sql.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class DataSourceDefinitionTest {
    private static EJBContainer container;

    @Inject
    private Persister persister;

    @BeforeClass
    public static void start() {
        container = EJBContainer.createEJBContainer();
    }

    @Before
    public void inject() throws NamingException {
        container.getContext().bind("inject", this);
    }

    @AfterClass
    public static void stop() {
        container.close();
    }

    @Test
    public void checkDs() throws SQLException {
        final DataSource ds = persister.getDs();
        assertNotNull(ds);
        assertThat(ds, instanceOf(DataSourceFactory.DbcpManagedDataSource.class));

        final DataSourceFactory.DbcpManagedDataSource castedDs = (DataSourceFactory.DbcpManagedDataSource) ds;

        final String driver = castedDs.getDriverClassName();
        assertNull(null, driver);

        final String user = castedDs.getUserName();
        assertEquals("sa", user);

        final String url = castedDs.getUrl();
        assertEquals("jdbc:h2:mem:persister", url);

        final int initPoolSize = castedDs.getInitialSize();
        assertEquals(1, initPoolSize);

        final int maxIdle = castedDs.getMaxIdle();
        assertEquals(3, maxIdle);

        final Connection connection = ds.getConnection();
        assertNotNull(connection);

        execute(connection, "CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))");
        execute(connection, "INSERT INTO TEST(ID, NAME) VALUES(1, 'foo')");
        connection.commit();

        final PreparedStatement statement = ds.getConnection().prepareStatement("SELECT NAME FROM TEST");
        statement.execute();
        final ResultSet set = statement.getResultSet();

        assertTrue(set.next());
        assertEquals("foo", set.getString("NAME"));
    }

    @Test
    public void lookup() throws NamingException {
        final Object o = container.getContext().lookup("java:app/jdbc/persister");
        assertNotNull(o);
        assertEquals(persister.getDs(), o);
    }

    private void execute(final Connection connection, final String sql) throws SQLException {
        connection.prepareStatement(sql).execute();
    }
}
