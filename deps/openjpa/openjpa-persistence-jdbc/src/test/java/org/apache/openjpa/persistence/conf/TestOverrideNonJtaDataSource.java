/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.conf;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.RollbackException;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestOverrideNonJtaDataSource extends SingleEMFTestCase {
    private String defaultJndiName = "jdbc/mocked";
    private String[] jndiNames = { "jdbc/mocked1" };

    protected void init(String cfName) {
        EntityManagerFactory emf1 = getEmf("openjpa.ConnectionFactoryName", cfName, true);
        EntityManager em = emf1.createEntityManager();
        em.getTransaction().begin();
        em.createQuery("Delete from confPerson").executeUpdate();
        em.getTransaction().commit();
        em.close();
        closeEMF(emf1);
    }

    public void setUp() throws Exception {
        super.setUp(Person.class, CLEAR_TABLES);
        OpenJPAEntityManager em = emf.createEntityManager();
        JDBCConfiguration conf = (JDBCConfiguration) em.getConfiguration();
        String user = conf.getConnectionUserName();
        if (user != null && !user.equals("")) {
            // Disable for non-Derby, due to connectionUserName to schema mapping failures
            setTestsDisabled(true);
            getLog().trace("TestOverrideNonJtaDataSource can only be executed against Derby w/o a schema");
        } else {
            // create an EMF for each database.
            init(defaultJndiName);
            init(jndiNames[0]);
        }
    }
    
    protected EntityManagerFactory getEmf(String cfPropertyName, String cfPropertyValue) {
        return getEmf(cfPropertyName, cfPropertyValue, false);
    }

    protected EntityManagerFactory getEmf(String cfPropertyName, String cfPropertyValue, boolean syncMappings) {
        // null out the driver to prevent system properties from taking effect.
        if (syncMappings) {
            return createEMF(
                "openjpa.jdbc.SynchronizeMappings", "buildSchema",
                "openjpa.ConnectionDriverName", "", 
                "openjpa.ConnectionFactoryMode", "managed",
                "openjpa.ConnectionFactoryName", defaultJndiName,  // must have a cf1, to initialize configuration
                cfPropertyName,cfPropertyValue, 
                Person.class);
        }
        return createEMF(
            "openjpa.ConnectionDriverName", "", 
            "openjpa.ConnectionFactoryMode", "managed",
            "openjpa.ConnectionFactoryName", defaultJndiName, // must have a cf1, to initialize configuration
            cfPropertyName,cfPropertyValue, 
            Person.class);
    }

    protected EntityManager getEm(EntityManagerFactory emf, String name, String value) {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(name, value);
        return emf.createEntityManager(props);
    }

    public String getPersistenceUnitName() {
        return "TestCfSwitching";
    }

    public void testConnectionFactoryName() {
        // Disable for non-Derby.
        // split out so that we can try javax.persistence.jtaDataSource in the future.
        overridePropertyOnEM("openjpa.ConnectionFactory2Name", jndiNames[0]);
    }

    public void testJtaDataSource() {
        // Disable for non-Derby.
        // split out so that we can try javax.persistence.jtaDataSource in the future.
        overridePropertyOnEM("javax.persistence.nonJtaDataSource", jndiNames[0]);
    }

    public void overridePropertyOnEM(String name, String value) {
        // use the default JndiName for the base EntityManagerFactory
        OpenJPAEntityManagerFactorySPI emf1 = (OpenJPAEntityManagerFactorySPI)getEmf(name, defaultJndiName);
        assertNotNull(emf1);
        try {
            OpenJPAEntityManagerSPI em = emf1.createEntityManager();
            assertNotNull(em);

            EntityManager em1 = getEm(emf1, name, value);
            assertNotNull(em1);

            // 'prove' that we're using a different database by inserting the same row
            em.getTransaction().begin();
            em.persist(new Person(1, "em"));
            em.getTransaction().commit();

            em1.getTransaction().begin();
            em1.persist(new Person(1, "em1"));
            em1.getTransaction().commit();

            em.clear();
            em1.clear();

            Person p = em.find(Person.class, 1);
            Person p1 = em1.find(Person.class, 1);
            assertNotSame(p, p1);
            assertEquals("em", p.getName());
            assertEquals("em1", p1.getName());

            em.clear();
            em1.clear();

            // make sure inserting the same row again fails.
            em.getTransaction().begin();
            em.persist(new Person(1));
            try {
                em.getTransaction().commit();
                fail("Should not be able to commit the same row a second time");
            } catch (RollbackException rbe) {
                assertTrue("Expected EntityExistsException but found " + rbe.getCause(),
                    rbe.getCause() instanceof EntityExistsException);
                // expected
            }

            em1.getTransaction().begin();
            em1.persist(new Person(1));
            try {
                em1.getTransaction().commit();
                fail("Should not be able to commit the same row a second time");
            } catch (RollbackException rbe) {
                assertTrue(rbe.getCause() instanceof EntityExistsException);
                // expected
            }
            em.close();
            em1.close();
        } finally {
            closeEMF(emf1);
        }
    }

    public void testInvalidCfName() throws Exception {
        // ensure EM creation fails - when provided an invalid JNDI name
        EntityManagerFactory emf1 = null;
        try {
            emf1 = getEmf("openjpa.ConnectionFactory2Name", defaultJndiName);
            getEm(emf1, "openjpa.ConnectionFactory2Name", "jdbc/NotReal");
            fail("Expected an excepton when creating an EM with a bogus JNDI name");
        } catch (ArgumentException e) {
            assertTrue(e.isFatal());
            System.out.println(e);
            assertTrue(e.getMessage().contains("jdbc/NotReal")); // ensure failing JNDI name is in the message
            assertTrue(e.getMessage().contains("EntityManager")); // ensure where the JNDI name came from is in message
        } finally {
            closeEMF(emf1);
        }
    }
    
    public void testDataCache() { 
        EntityManagerFactory emf1 = null;
    
        try {
            emf1 = getEmf("openjpa.DataCache", "true");
            getEm(emf1, "openjpa.ConnectionFactoryName", "jdbc/NotReal");
            fail("Expected an excepton when creating an EM with a bogus JNDI name");
        } catch (ArgumentException e) {
            assertTrue(e.isFatal());
            assertTrue(e.getMessage().contains("jdbc/NotReal")); 
            assertTrue(e.getMessage().contains("L2 Cache")); 
        } finally {
            closeEMF(emf1);
        }
    }
    
    public void testQueryCache() { 
        EntityManagerFactory emf1 = null;
    
        try {
            emf1 = getEmf("openjpa.QueryCache", "true");
            getEm(emf1, "openjpa.ConnectionFactoryName", "jdbc/NotReal");
            fail("Expected an excepton when creating an EM with a bogus JNDI name");
        } catch (ArgumentException e) {
            assertTrue(e.isFatal());
            assertTrue(e.getMessage().contains("jdbc/NotReal")); 
            assertTrue(e.getMessage().contains("openjpa.QueryCache")); 
        } finally {
            closeEMF(emf1);
        }
    }
    
    public void testSyncMappings() { 
        EntityManagerFactory emf1 = null;
    
        try {
            emf1 = getEmf("openjpa.jdbc.SynchronizeMappings", "buildSchema");
            getEm(emf1, "openjpa.ConnectionFactoryName", "jdbc/NotReal");
            fail("Expected an excepton when creating an EM with a bogus JNDI name");
        } catch (ArgumentException e) {
            assertTrue(e.isFatal());
            assertTrue(e.getMessage().contains("jdbc/NotReal")); 
            assertTrue(e.getMessage().contains("openjpa.jdbc.SynchronizeMappings")); 
        } finally {
            closeEMF(emf1);
        }
    }
}
