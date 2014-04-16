/**
 * Tomitribe Confidential
 * <p/>
 * Copyright(c) Tomitribe Corporation. 2014
 * <p/>
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 * <p/>
 */
package org.superbiz;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@RunWith(Arquillian.class)
public class AlternateDataSourceTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClasses(DataSourceTester.class)
            .addAsResource(new ClassLoaderAsset("META-INF/ejb-jar.xml"), "META-INF/ejb-jar.xml");
        //.addAsResource(new ClassLoaderAsset("META-INF/persistence.xml"), "META-INF/persistence.xml");
    }

    @EJB
    private DataSourceTester tester;

    @Test
    public void testDataSourceOne() throws Exception {
        System.out.println("tester = " + tester.getBoth());
    }


    @Stateless
    public static class DataSourceTester {

        @Resource(name = "DatabaseOne")
        DataSource dataSourceOne;

        @Resource(name = "DatabaseOne")
        DataSource dataSourceTwo;

        public String getOne() throws Exception {
            return getVersion(dataSourceOne);
        }

        public String getTwo() throws Exception {
            return getVersion(dataSourceTwo);
        }

        public String getBoth() throws Exception {
            return getOne() + "|" + getTwo();
        }

        private static String getVersion(final DataSource ds) throws SQLException {
            Connection con = null;
            try {
                con = ds.getConnection();
                final DatabaseMetaData md = con.getMetaData();
                return md.getDriverVersion();
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        }
    }
}
