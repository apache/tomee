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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
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
                .addAsResource(new ClassLoaderAsset("META-INF/beans.xml"), "META-INF/beans.xml");
        //We are using src/test/conf/tomee.xml, but this also works - .addAsResource(new ClassLoaderAsset("META-INF/resources.xml"), "META-INF/resources.xml");
        //Or even using a persistence context - .addAsResource(new ClassLoaderAsset("META-INF/persistence.xml"), "META-INF/persistence.xml");
    }

    @EJB
    private DataSourceTester tester;

    @Test
    public void testDataSourceOne() throws Exception {
        Assert.assertEquals("Should be using 10.10.1.1 - (1458268)", "10.10.1.1 - (1458268)", tester.getOne());
    }

    @Test
    public void testDataSourceTwo() throws Exception {
        Assert.assertEquals("Should be using 10.9.1.0 - (1344872)", "10.9.1.0 - (1344872)", tester.getTwo());
    }

    @Test
    public void testDataSourceBoth() throws Exception {
        Assert.assertEquals("Should be using 10.10.1.1 - (1458268)|10.9.1.0 - (1344872)", "10.10.1.1 - (1458268)|10.9.1.0 - (1344872)", tester.getBoth());
    }

    @Stateless
    public static class DataSourceTester {

        @Resource(name = "DatabaseOne")
        DataSource dataSourceOne;

        @Resource(name = "DatabaseTwo")
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
