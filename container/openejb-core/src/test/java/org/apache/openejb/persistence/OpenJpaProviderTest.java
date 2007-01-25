/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.persistence;

import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;


public class OpenJpaProviderTest extends TestCase {

    public void testNothing(){}

    public void _testOpenJpaProvider() throws Exception {
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, "org.apache.openejb.persistence");
        // m2 executes tests in a module home directory (e.g. container/openejb-persistence)
        // Derby creates derby.log file in derby.system.home
        // @see http://publib.boulder.ibm.com/infocenter/cscv/v10r1/index.jsp?topic=/com.ibm.cloudscape.doc/cdevdvlp25889.html
        System.setProperty("derby.system.home", "target");

        PersistenceDeployer deployer = new PersistenceDeployer(new TestDataSourceResolver(), null);

        Map<String, EntityManagerFactory> map = deployer.deploy(Thread.currentThread().getContextClassLoader());

        EntityManagerFactory emf = map.get("openjpa-test-unit");
        assertNotNull("EntityManagerFactory", emf);

        EntityManager em = emf.createEntityManager();
        assertNotNull("EntityManager", em);

        em.getTransaction().begin();
        em.persist(new AllFieldTypes());
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("delete from AllFieldTypes").executeUpdate();
        em.getTransaction().commit();
        em.close();
        emf.close();
    }

    public class TestDataSourceResolver implements DataSourceResolver {

        public DataSource getDataSource(String name) throws Exception {
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
            ds.setUrl("jdbc:derby:database/openjpa-test-database;create=true");
            ds.setMaxActive(100);
            ds.setMaxWait(10000);
            ds.setTestOnBorrow(true);
            return ds;
        }
    }

}
