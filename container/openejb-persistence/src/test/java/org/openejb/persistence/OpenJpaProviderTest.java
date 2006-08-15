/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.persistence;

import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Map;


public class OpenJpaProviderTest extends TestCase {

    public void testOpenJpaProvider() throws Exception {
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, "org.openejb.persistence");

        PersistenceDeployer deployer = new PersistenceDeployer(new TestDataSourceResolver());

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
            ds.setUrl("jdbc:derby:target/database/openjpa-test-database;create=true");
            ds.setMaxActive(100);
            ds.setMaxWait(10000);
            ds.setTestOnBorrow(true);
            return ds;
        }
    }

}
