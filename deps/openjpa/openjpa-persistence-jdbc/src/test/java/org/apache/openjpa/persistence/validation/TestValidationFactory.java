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
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.PersistenceException;
import org.apache.openjpa.persistence.query.SimpleEntity;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests the new Bean Validation Factory support in the JPA 2.0 spec.
 * Basic (no provider) Validation scenarios being tested:
 *   1) By default, ValidationFactory is null
 *   2) An invalid ValidationFactory with mode of NONE will not cause exception
 *   3) An invalid ValidationFactory with mode of AUTO will not cause exception
 *   4) An invalid ValidationFactory with mode of CALLBACK will cause exception
 * 
 * @version $Rev$ $Date$
 */
public class TestValidationFactory extends SingleEMFTestCase {

    @Override
    public void setUp() {
        super.setUp(CLEAR_TABLES, SimpleEntity.class);
    }

    /**
     * Scenario being tested:
     *   1) By default, ValidationFactory is null
     */
    public void testValidationFactory1() {
        getLog().trace("testValidationFactory1() - Default is null");
        OpenJPAEntityManagerFactory emf = null;

        // create our EMF
        emf = OpenJPAPersistence.createEntityManagerFactory(
            "simple-none-mode",
            "org/apache/openjpa/persistence/validation/persistence.xml");
        assertNotNull(emf);
        // verify default validation mode
        OpenJPAConfiguration conf = emf.getConfiguration();
        assertNotNull(conf);
        assertEquals("Default ValidationFactory", 
            null,
            conf.getValidationFactoryInstance());
        closeEMF(emf);
    }

    /**
     * Scenario being tested:
     *   2) An invalid ValidationFactory with a mode of NONE will not
     *      cause an exception
     */
    public void testValidationFactory2() {
        getLog().trace("testValidationFactory2() - ignored invalid factory");
        OpenJPAEntityManagerFactory emf = null;

        // create the Map to test overrides
        //   Just use current class object, as we have no provider to test with
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("javax.persistence.validation.factory",
            this.getClass());

        // create our EMF
        emf = OpenJPAPersistence.createEntityManagerFactory(
            "simple-none-mode",
            "org/apache/openjpa/persistence/validation/persistence.xml",
            props);
        assertNotNull(emf);
        // verify same "validation factory" object is returned
        OpenJPAConfiguration conf = emf.getConfiguration();
        assertNotNull(conf);
        assertEquals("ValidationFactory", 
            this.getClass(),
            conf.getValidationFactoryInstance());
        closeEMF(emf);
    }

    /**
     * Scenario being tested:
     *   3) An invalid ValidationFactory with a mode of AUTO will not
     *      cause an exception
     */
    public void testValidationFactory3() {
        getLog().trace("testValidationFactory3() - optional invalid factory");
        OpenJPAEntityManagerFactory emf = null;

        // create the Map to test overrides
        //   Just use current class object, as we have no provider to test with
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("javax.persistence.validation.factory",
            this.getClass());

        // create our EMF
        emf = OpenJPAPersistence.createEntityManagerFactory(
            "simple-auto-mode",
            "org/apache/openjpa/persistence/validation/persistence.xml",
            props);
        assertNotNull(emf);
        // verify same "validation factory" object is returned
        OpenJPAConfiguration conf = emf.getConfiguration();
        assertNotNull(conf);
        assertEquals("ValidationFactory", 
            this.getClass(),
            conf.getValidationFactoryInstance());
        closeEMF(emf);
    }

    /**
     * Scenario being tested:
     *   4) An invalid ValidationFactory with a mode of CALLBACK will
     *      cause a fatal exception
     */
    public void testValidationFactory4() {
        getLog().trace("testValidationFactory4() - required invalid factory");
        OpenJPAEntityManagerFactory emf = null;

        // create the Map to test overrides
        //   Just use current class object, as we have no provider to test with
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("javax.persistence.validation.factory",
            this.getClass());
        props.put("javax.persistence.validation.mode",
            String.valueOf(ValidationMode.CALLBACK));

        try {
            // create our EMF
            emf = OpenJPAPersistence.createEntityManagerFactory(
                "simple-callback-mode",
                "org/apache/openjpa/persistence/validation/persistence.xml",
                props);
            assertNotNull(emf);
            // verify validation mode
            OpenJPAConfiguration conf = emf.getConfiguration();
            assertNotNull(conf);
            assertEquals("Validation mode", 
                String.valueOf(ValidationMode.CALLBACK),
                conf.getValidationMode());
            fail("Expected an exception when ValidationMode=CALLBACK and " +
                "an invalid ValidatorFactory is provided.");
        } catch (PersistenceException e) {
            // expected
            getLog().trace("testValidationFactory4() - caught expected " +
                "PersistenceException", e);
        }
        closeEMF(emf);
    }

}
