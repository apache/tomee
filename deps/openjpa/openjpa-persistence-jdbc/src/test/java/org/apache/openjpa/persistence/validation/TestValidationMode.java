/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.openjpa.persistence.validation;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.ValidationMode;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.PersistListener;
import org.apache.openjpa.event.StoreListener;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.PersistenceException;
import org.apache.openjpa.persistence.query.SimpleEntity;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests the new Bean Validation Mode support in the JPA 2.0 spec.
 * Basic (no provider) Validation scenarios being tested:
 *   1) By default, validation mode is AUTO
 *   2) Validation mode of AUTO in persistence.xml is the same as default
 *   3) Validation mode of NONE in persistence.xml overrides default AUTO
 *   4) Validation mode of CALLBACK in persistence.xml overrides default AUTO
 *   5) Validation mode in createEMF(Map props) overrides no persistence.xml
 *   6) Validation mode in createEMF(Map props) overrides persistence.xml
 *   7) Validation mode in createEMF(Map props) can be a ValidationMode enum
 * 
 * @version $Rev$ $Date$
 */
public class TestValidationMode extends SingleEMFTestCase {

    @Override
    public void setUp() {
        super.setUp(CLEAR_TABLES, SimpleEntity.class);
    }

    /**
     * Scenario being tested:
     *   1) By default, validation mode == AUTO
     */
    public void testValidationMode1() {
        getLog().trace("testValidationMode1() - Default mode is AUTO");
        // create our EMF
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "simple",
                "org/apache/openjpa/persistence/validation/persistence.xml");
        assertNotNull(emf);
        try {
            // verify default validation mode
            OpenJPAConfiguration conf = emf.getConfiguration();
            assertNotNull(conf);
            assertEquals("Default validation mode", 
                String.valueOf(ValidationMode.AUTO),
                conf.getValidationMode());
        } finally {
            cleanup(emf);
        }
    }

    /**
     * Scenario being tested:
     *   2) Validation mode of AUTO in persistence.xml is the same as default
     */
    public void testValidationMode2() {
        getLog().trace("testValidationMode1() - AUTO in persistence.xml");
        // create our EMF
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "simple-auto-mode",
                "org/apache/openjpa/persistence/validation/persistence.xml");
        assertNotNull(emf);
        try {
            // verify expected validation mode
            OpenJPAConfiguration conf = emf.getConfiguration();
            assertNotNull(conf);
            assertEquals("Validation mode", 
                String.valueOf(ValidationMode.AUTO),
                conf.getValidationMode());
        } finally {
            cleanup(emf);
        }
    }

    /**
     * Scenario being tested:
     *   3) Validation mode of NONE in persistence.xml overrides default
     */
    public void testValidationMode3() {
        getLog().trace("testValidationMode3() - persistence.xml overrides " +
            "Default");
        // create our EMF
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "simple-none-mode",
                "org/apache/openjpa/persistence/validation/persistence.xml");
        assertNotNull(emf);
        try {
            // verify validation mode
            OpenJPAConfiguration conf = emf.getConfiguration();
            assertNotNull(conf);
            assertEquals("Validation mode", 
                String.valueOf(ValidationMode.NONE),
                conf.getValidationMode());
        } finally {
            cleanup(emf);
        }
    }

    /**
     * Scenario being tested:
     *   4) Validation mode of CALLBACK in persistence.xml overrides default
     *   and causes an exception when neither the Validation APIs nor
     *   a provider are available
     */
    public void testValidationMode4() {
        getLog().trace("testValidationMode4() - persistence.xml overrides " +
            "Default");
        OpenJPAEntityManagerFactorySPI emf = null;
        try {
            // create our EMF
            emf = (OpenJPAEntityManagerFactorySPI)
                OpenJPAPersistence.createEntityManagerFactory(
                    "simple-callback-mode",
                    "org/apache/openjpa/persistence/validation/persistence.xml");
            assertNotNull(emf);
            // verify validation mode
            OpenJPAConfiguration conf = emf.getConfiguration();
            assertNotNull(conf);
            assertEquals("Validation mode", 
                String.valueOf(ValidationMode.CALLBACK),
                conf.getValidationMode());
        } catch (PersistenceException e) {
            // expected when no Validation APIs or provider are available
            getLog().trace("testValidationMode4() - caught expected " +
                "exception", e);
        } finally {
            cleanup(emf);
        }
    }

    /**
     * Scenario being tested:
     *   5) Validation mode in createEMF(Map props) overrides no persistence.xml
     */
    public void testValidationMode5() {
        getLog().trace("testValidationMode5() - Map(NONE) overrides default");

        // create the Map to test overrides
        Map<String,String> props = new HashMap<String,String>();
        props.put("javax.persistence.validation.mode",
            String.valueOf(ValidationMode.NONE));

        // create our EMF
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "simple",
                "org/apache/openjpa/persistence/validation/persistence.xml",
            props);
        assertNotNull(emf);
        try {
            // verify validation mode
            OpenJPAConfiguration conf = emf.getConfiguration();
            assertNotNull(conf);
            assertEquals("Validation mode", 
                String.valueOf(ValidationMode.NONE),
                conf.getValidationMode());
        } finally {
            cleanup(emf);
        }
    }

    /**
     * Scenario being tested:
     *   6) Validation mode in createEMF(Map props) overrides persistence.xml
     */
    public void testValidationMode6() {
        getLog().trace("testValidationMode6() - Map(NONE) overrides PU " +
            "provided mode=callback");

        // create the Map to test overrides
        Map<String,String> props = new HashMap<String,String>();
        props.put("javax.persistence.validation.mode",
            String.valueOf(ValidationMode.NONE));

        // create our EMF
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "simple-callback-mode",
                "org/apache/openjpa/persistence/validation/persistence.xml",
            props);
        assertNotNull(emf);
        try {
            // verify validation mode
            OpenJPAConfiguration conf = emf.getConfiguration();
            assertNotNull(conf);
            assertEquals("Validation mode", 
                String.valueOf(ValidationMode.NONE),
                conf.getValidationMode());
        } finally {
            cleanup(emf);
        }
    }

    /**
     * Scenario being tested:
     *   7) Validation mode in createEMF(Map props) can be a ValidationMode enum
     */
    public void testValidationMode7() {
        getLog().trace("testValidationMode7() - Map(ValidationMode.NONE) " +
            "overrides PU provided mode=callback");

        // create the Map to test overrides
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("javax.persistence.validation.mode",
            ValidationMode.NONE);

        // create our EMF
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "simple-callback-mode",
                "org/apache/openjpa/persistence/validation/persistence.xml",
            props);
        assertNotNull(emf);
        try {
            // verify validation mode
            OpenJPAConfiguration conf = emf.getConfiguration();
            assertNotNull(conf);
            assertEquals("Validation mode", 
                String.valueOf(ValidationMode.NONE),
                conf.getValidationMode());
        } finally {
            cleanup(emf);
        }
    }

    /**
     * Scenario being tested:
     *   8) Life cycle event should be entity manager (Broker) specific.
     */
    public void testUniqueLifecycleManager() {
        getLog().trace("testUniqueLifecycleManager() - Life cycle event tests");
        // create our EMF
        Map<String,String> prop = new HashMap<String,String>();
        prop.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
//        prop.put("openjpa.Compatibility", "SingletonLifecycleEventManager=true");

        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "simple",
                "org/apache/openjpa/persistence/validation/persistence.xml",
                prop);
        assertNotNull(emf);
        try {
            final OpenJPAEntityManagerSPI em = emf.createEntityManager();
            final OpenJPAEntityManagerSPI em2 = emf.createEntityManager();
            UniqueLifecycleListener l1 = new UniqueLifecycleListener();
            UniqueLifecycleListener l2 = new UniqueLifecycleListener();
            em.addLifecycleListener(l1, (Class<?>[])null);
            em2.addLifecycleListener(l2, (Class<?>[])null);

            l1.assertCounts(0, 0, 0, 0);
            l2.assertCounts(0, 0, 0, 0);

            em.getTransaction().begin();
            SimpleEntity e1 = new SimpleEntity();
            em.persist(e1);
            l1.assertCounts(1, 1, 0, 0);
            l2.assertCounts(0, 0, 0, 0);

            em2.getTransaction().begin();
            SimpleEntity e2 = new SimpleEntity();
            em2.persist(e2);
            l1.assertCounts(1, 1, 0, 0);
            l2.assertCounts(1, 1, 0, 0);

            em2.getTransaction().commit();
            l1.assertCounts(1, 1, 0, 0);
            l2.assertCounts(1, 1, 1, 1);

            em.getTransaction().commit();
            l1.assertCounts(1, 1, 1, 1);
            l2.assertCounts(1, 1, 1, 1);
        } finally {
            cleanup(emf);
        }
    }

    class UniqueLifecycleListener implements PersistListener, StoreListener {

        public int beforePersistCount;
        public int afterPersistCount;
        public int beforeStoreCount;
        public int afterStoreCount;

        @Override
        public void beforePersist(LifecycleEvent event) {
            beforePersistCount++;
        }

        @Override
        public void afterPersist(LifecycleEvent event) {
            afterPersistCount++;
        }

        @Override
        public void beforeStore(LifecycleEvent event) {
            beforeStoreCount++;
        }

        @Override
        public void afterStore(LifecycleEvent event) {
            afterStoreCount++; 
        }

        public void assertCounts(int beforePersist, int afterPersist, int beforeStore, int afterStore) {
            assertEquals(beforePersist, beforePersistCount);
            assertEquals(afterPersist, afterPersistCount);
            assertEquals(beforeStore, beforeStoreCount);
            assertEquals(afterStore, afterStoreCount);
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
