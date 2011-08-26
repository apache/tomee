package org.apache.openejb.assembler.classic;

import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.sql.DataSource;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author rmannibucau
 */
@RunWith(ApplicationComposer.class)
public class DataSourceDefinitionTest {
    @EJB private DatasourceDefinitionBean bean;

    @Module public Class<?>[] app() throws Exception {
        return new Class<?>[]{ DatasourceDefinitionBean.class };
    }

    @DataSourceDefinition(
        name = "superDS",
        className = "org.hsqldb.jdbc.jdbcDataSource",
        url = "jdbc:hsqldb:mem:superDS"
    )
    @Stateless
    public static class DatasourceDefinitionBean {
//         @Resource(lookup = "java:openjeb/Resource/superDS") private DataSource ds;
        @Resource private DataSource ds;

        public DataSource getDs() {
            return ds;
        }
    }

    @Test @Ignore("Resource annotation needs lookup to make it work") public void assertDs() {
        assertNotNull(bean.getDs());
        assertEquals("org.hsqldb.jdbc.jdbcDataSource", bean.getDs().getClass().getName());
    }
}
