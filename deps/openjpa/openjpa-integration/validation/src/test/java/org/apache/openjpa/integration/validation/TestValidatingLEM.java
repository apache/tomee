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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.ValidationMode;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.integration.validation.SimpleEntity;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.validation.ValidatingLifecycleEventManager;

/**
 * Tests the new Bean Validation Factory support in the JPA 2.0 spec by
 * focusing on the following Validation Provider scenarios:
 *   1) Mode of NONE will create a LifecycleEventManager
 *   2) Mode of AUTO will create a ValidatingLifecycleEventManager
 *   3) Map mode of CALLBACK will create a ValidatingLifecycleEventManager
 *   4) Verify a passed in ValidatorFactory is used
 * 
 * @version $Rev$ $Date$
 */
public class TestValidatingLEM extends SingleEMFTestCase {

    @Override
    public void setUp() {
        super.setUp(CLEAR_TABLES, SimpleEntity.class);

        EntityManager em = null;
        // create some initial entities
        try {
            em = emf.createEntityManager();
            assertNotNull(em);
            getLog().trace("setup() - creating 1 SimpleEntity");
            em.getTransaction().begin();
            SimpleEntity se = new SimpleEntity("entity","1");
            em.persist(se);
            em.getTransaction().commit();
        } catch (Exception e) {
            fail("setup() - Unexpected Exception - " + e);
        } finally {
            if ((em != null) && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Scenario being tested:
     *   1) Mode of NONE will create a LifecycleEventManager
     */
    public void testValidatingLEM1() {
        getLog().trace("testValidatingLEM1() - NONE");
        // create our EMF
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "simple-none-mode",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);
        OpenJPAEntityManager em = null;
        try {
            // create EM
            em = emf.createEntityManager();
            assertNotNull(em);
            // verify created LifecycleEventManager type
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("NONE"));
            Class<?> lem = conf.getLifecycleEventManagerInstance().getClass();
            assertNotNull(lem);
            assertFalse("Expected a LifecycleEventManager instance", 
                ValidatingLifecycleEventManager.class.isAssignableFrom(lem));
        } catch (Exception e) {
            fail("Unexpected testValidatingLEM1() exception = " + e);
        } finally {
            if ((em != null) && em.isOpen()) {
                em.close();
            }
            cleanup(emf);
        }
    }

    /**
     * Scenario being tested:
     *   2) Mode of AUTO will create a ValidatingLifecycleEventManager
     */
    public void testValidatingLEM2() {
        getLog().trace("testValidatingLEM2() - AUTO");
        // create our EMF
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "simple-auto-mode",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);
        OpenJPAEntityManager em = null;
        try {
            // create EM
            em = emf.createEntityManager();
            assertNotNull(em);
            // verify created LifecycleEventManager type
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            Class<?> lem = conf.getLifecycleEventManagerInstance().getClass();
            assertNotNull(lem);
            assertTrue("Expected a ValidatingLifecycleEventManager instance", 
                ValidatingLifecycleEventManager.class.isAssignableFrom(lem));
        } catch (Exception e) {
            fail("Unexpected testValidatingLEM2() exception = " + e);
        } finally {
            if ((em != null) && em.isOpen()) {
                em.close();
            }
            cleanup(emf);
        }
    }

    /**
     * Scenario being tested:
     *   3) Map mode of CALLBACK will create a ValidatingLifecycleEventManager
     */
    public void testValidatingLEM3() {
        getLog().trace("testValidatingLEM3() - CALLBACK");
        // create the Map to test overrides
        //   Just use current class object, as we have no provider to test with
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("javax.persistence.validation.mode",
            String.valueOf(ValidationMode.CALLBACK));
        // create our EMF w/ props
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "simple-none-mode",
                "org/apache/openjpa/integration/validation/persistence.xml",
                props);
        assertNotNull(emf);
        OpenJPAEntityManager em = null;
        try {
            // create EM
            em = emf.createEntityManager();
            assertNotNull(em);
            // verify created LifecycleEventManager type
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("CALLBACK"));
            Class<?> lem = conf.getLifecycleEventManagerInstance().getClass();
            assertNotNull(lem);
            assertTrue("Expected a ValidatingLifecycleEventManager instance", 
                ValidatingLifecycleEventManager.class.isAssignableFrom(lem));
        } catch (Exception e) {
            fail("Unexpected testValidatingLEM3() exception = " + e);
        } finally {
            if ((em != null) && em.isOpen()) {
                em.close();
            }
            cleanup(emf);
        }
    }

    /**
     * Scenario being tested:
     *   4) Verify a passed in ValidatorFactory is used
     */
    public void testValidatingLEM4() {
        getLog().trace("testValidatingLEM4() - provided ValidatorFactory");
        // create a default factory to pass in
        ValidatorFactory factory = null;
        try {
            factory = Validation.buildDefaultValidatorFactory();
        } catch (javax.validation.ValidationException e) {
            fail("testValidatingLEM4() - no validation providers found" + e);
        }
        assertNotNull(factory);
        // create the Map to test overrides
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("javax.persistence.validation.factory", factory);
        // create our EMF w/ props
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "simple-auto-mode",
                "org/apache/openjpa/integration/validation/persistence.xml",
                props);
        assertNotNull(emf);
        OpenJPAEntityManager em = null;
        try {
            // create EM
            em = emf.createEntityManager();
            assertNotNull(em);
            // verify expected validation config items
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            Class<?> lem = conf.getLifecycleEventManagerInstance().getClass();
            assertNotNull(lem);
            assertTrue("Expected a ValidatingLifecycleEventManager instance", 
                ValidatingLifecycleEventManager.class.isAssignableFrom(lem));
            // verify factory matches
            assertEquals("Expected same ValidatorFactory instance",
                factory, conf.getValidationFactoryInstance());
        } catch (Exception e) {
            fail("Unexpected testValidatingLEM4() exception = " + e);
        } finally {
            if ((em != null) && em.isOpen()) {
                em.close();
            }
            cleanup(emf);
        }
    }

    
    /**
     * Helper method to remove entities and close the emf an any open em's.
     * @param emf
     */
    private void cleanup(OpenJPAEntityManagerFactorySPI emf) {
        clear(emf);
        closeEMF(emf);
    }    

}
