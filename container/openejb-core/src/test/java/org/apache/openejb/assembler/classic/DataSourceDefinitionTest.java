package org.apache.openejb.assembler.classic;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.annotation.sql.DataSourceDefinitions;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.lang.reflect.Field;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Note: to make this test work under JavaSE 6 you should add geronimo-annotation_1.1_spec in your endorsed dir.
 * It is automatically done with maven.
 *
 * TODO: use Resource.lookup instead of name.
 *
 * @author rmannibucau
 */
@RunWith(ApplicationComposer.class)
public class DataSourceDefinitionTest {
    @EJB private DatasourceDefinitionBean uniqueDataSource;
    @EJB private DatasourceDefinitionsBean multipleDatasources;

    @Module public Class<?>[] app() throws Exception {
        return new Class<?>[]{ DatasourceDefinitionBean.class, DatasourceDefinitionsBean.class };
    }

    @DataSourceDefinition(
        name = "java:comp/env/superDS",
        className = "org.hsqldb.jdbc.jdbcDataSource",
        user = "sa",
        password = "",
        url = "jdbc:hsqldb:mem:superDS"
    )
    @Singleton
    public static class DatasourceDefinitionBean {
        @Resource(lookup = "java:comp/env/superDS") private DataSource ds;

        public DataSource getDs() {
            return ds;
        }
    }

    @DataSourceDefinitions({
        @DataSourceDefinition(
            name = "java:comp/env/superMegaDS",
            className = "org.hsqldb.jdbc.jdbcDataSource",
            user = "foo1",
            password = "bar1",
            url = "jdbc:hsqldb:mem:superDS"
        ),
        @DataSourceDefinition(
            name = "java:comp/env/superGigaDS",
            className = "org.hsqldb.jdbc.jdbcDataSource",
            user = "foo2",
            password = "bar2",
            url = "jdbc:hsqldb:mem:superDS"
        )
    })
    @Stateless
    public static class DatasourceDefinitionsBean {
        @Resource(lookup = "java:comp/env/superMegaDS") private DataSource mega;
        @Resource(lookup = "java:comp/env/superGigaDS") private DataSource giga;

        public DataSource getMega() {
            return mega;
        }

        public DataSource getGiga() {
            return giga;
        }
    }

    @Test public void assertDataSourceDefinition() throws Exception {
        assertDataSourceDefinitionValues(uniqueDataSource.getDs(), "org.hsqldb.jdbc.jdbcDataSource", "sa", "");
    }

    @Test public void assertDatasourceDefinitions() throws Exception {
        assertDataSourceDefinitionValues(multipleDatasources.getMega(), "org.hsqldb.jdbc.jdbcDataSource", "foo1", "bar1");
        assertDataSourceDefinitionValues(multipleDatasources.getGiga(), "org.hsqldb.jdbc.jdbcDataSource", "foo2", "bar2");
    }

    private void assertDataSourceDefinitionValues(Object value, String clazz, String user, String password) throws Exception {
        assertNotNull("injection should work", value);
        assertEquals("configuration should be ok - class", "org.hsqldb.jdbc.jdbcDataSource", value.getClass().getName());
        assertEqualsByReflection("configuration should be ok - user", value, "user", user);
        assertEqualsByReflection("configuration should be ok - password", value, "password", password);
    }

    private void assertEqualsByReflection(String message, Object value, String name, Object expected) throws Exception {
        Class<?> clazz = value.getClass();
        Field field = clazz.getDeclaredField(name);
        boolean acc = field.isAccessible();
        if (!acc) {
            field.setAccessible(true);
        }
        try {
            Object fieldValue = field.get(value);
            assertEquals(message, expected, fieldValue);
        } finally {
            field.setAccessible(acc);
        }

    }
}
