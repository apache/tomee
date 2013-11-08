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
package org.apache.openjpa.integration.validation;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolationException;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

/**
 * Tests the Bean Validation support when using the em.merge() 
 * operation.
 *      
 * @version $Rev$ $Date$
 */
public class TestMerge extends AbstractPersistenceTestCase {

    private static OpenJPAEntityManagerFactorySPI emf = null;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "ConstraintPU",
                "org/apache/openjpa/integration/validation/persistence.xml");
    }
    
    @Override
    public void tearDown() throws Exception {
        closeEMF(emf);
        emf = null;
        super.tearDown();
    }

    /**
     * Verifies constraint validation occurs on a "new" merged entity only after 
     * the state of the persistent entity is properly set. 
     */
    public void testMergeNew() {
        getLog().trace("testMergeNew() started");
        
        // Part 1 - Create and persist a valid entity
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            @SuppressWarnings("deprecation")
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));

            Person p = createPerson(em);
            em.getTransaction().begin();
            p = em.merge(p);
            em.getTransaction().commit();
            getLog().trace("testMergeNew() Part 1 of 2 passed");
        } catch (Exception e) {
            // unexpected
            getLog().trace("testMergeNew() Part 1 of 2 failed");
            fail("Caught unexpected exception = " + e);
        } finally {
            closeEM(em);
        }
        
        // Part 2 - Verify that merge throws a CVE when a constraint is not met.
        em = emf.createEntityManager();
        assertNotNull(em);        
        try {
            Person p = createPerson(em);
            em.getTransaction().begin();
            p.setLastName(null);  // Force a CVE
            p = em.merge(p);
            getLog().trace("testMergeNew() Part 2 of 2 failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testMergeNew() Part 2 of 2 passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Verifies constraint validation occurs on a "new" merged entity only after 
     * the state of the persistent entity is properly set. 
     */
    public void testMergeExisting() {
        getLog().trace("testMergeExisting() started");
        
        // Part 1 - Create and persist a valid entity
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            @SuppressWarnings("deprecation")
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));

            // Create and persist a new entity
            Person p = createPerson(em);
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();
            em.clear();

            // find the entity
            p = em.find(Person.class, p.getId());
            
            // modify the entity and merge
            em.getTransaction().begin();
            p.setFirstName("NewFirst");
            // merge should not throw a CVE
            p = em.merge(p);
            em.getTransaction().commit();
            em.clear();
            p = em.find(Person.class, p.getId());
            assertEquals("NewFirst", p.getFirstName());
            getLog().trace("testMergeExisting() Part 1 of 2 passed");
        } catch (Exception e) {
            // unexpected
            getLog().trace("testMergeExisting() Part 1 of 2 failed");
            fail("Caught unexpected exception = " + e);
        } finally {
            closeEM(em);
        }
        
        // Part 2 - Verify that merge throws a CVE when a constraint is not met.
        em = emf.createEntityManager();
        assertNotNull(em);        
        try {

            // Create and persist a new entity
            Person p = createPerson(em);
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();
            em.clear();

            // find the entity
            p = em.find(Person.class, p.getId());
            // detach the entity
            em.detach(p);
            assertFalse(em.contains(p));

            // Set name to an invalid value (contains a space) to force a CVE upon merge+update
            p.setFirstName("First Name");
            em.getTransaction().begin();
            try {
                p = em.merge(p);
            } catch (Throwable t) {
                fail("Did not expect a CVE upon merge.");
            }
            // Commit should throw a CVE
            em.getTransaction().commit();
            getLog().trace("testMergeExisting() Part 2 of 2 failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testMergeExisting() Part 2 of 2 passed");
        } finally {
            closeEM(em);
        }
    }
    
    
    private Person createPerson(EntityManager em) {
        Person p = new Person();
        p.setFirstName("First");
        p.setLastName("Last");
        p.setHomeAddress(createAddress(em));
        return p;
    }

    private IAddress createAddress(EntityManager em) {
        Address addr = new Address();
        addr.setCity("City");
        addr.setPhoneNumber("555-555-5555");
        addr.setPostalCode("55555");
        addr.setState("ST");
        addr.setStreetAddress("Some Street");
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        em.persist(addr);
        em.getTransaction().commit();
        return addr;
    }

    /**
     * Internal convenience method for getting the OpenJPA logger
     * 
     * @return
     */
    private Log getLog() {
        return emf.getConfiguration().getLog("Tests");
    }
}
