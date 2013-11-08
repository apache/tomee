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

import java.util.Set;

import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

/**
 * Tests the new Bean Validation constraint support in the JPA 2.0 spec by
 * focusing on the following Validation scenarios:
 *      
 *   Check special update/delete/ignore cases once:
 *   1)  Update @Null constraint exception on variables in mode=AUTO
 *       Tests that a constraint violation will occur on invalid update.
 *   2)  No invalid Delete @Null constraint exception when mode=AUTO
 *       Tests that a violation will not occur when deleting invalid entity.
 *   3)  No invalid Persist constraint exception when mode=NONE
 *       Tests that no Validation Providers are used when disabled.
 *   
 *   Basic constraint tests for violation exceptions:
 *   4)  Persist @Null constraint exception on variables in mode=AUTO
 *   5)  Persist @NotNull constraint exception on getter in mode=AUTO
 *   7)  Test @AssertTrue constraint exception on variables in mode=AUTO
 *   8)  Test @AssertFalse constraint exception on getter in mode=AUTO
 *   10) Test @DecimalMin constraint exception on variables in mode=AUTO
 *   11) Test @DecimalMax constraint exception on getter in mode=AUTO
 *   13) Test @Min constraint exception on variables in mode=AUTO
 *   14) Test @Max constraint exception on getter in mode=AUTO
 *   16) Test @Digits constraint exception on variables in mode=AUTO
 *   17) Test @Digits constraint exception on getter in mode=AUTO
 *   19) Test @Size constraint exception on variables in mode=AUTO
 *   20) Test @Size constraint exception on getter in mode=AUTO
 *   22) Test @Future constraint exception on variables in mode=AUTO
 *   23) Test @Past constraint exception on getter in mode=AUTO
 *   25) Test @Pattern constraint exception on variables in mode=AUTO
 *   26) Test @Pattern constraint exception on getter in mode=AUTO
 *   28) Test @Valid constraint exceptions in mode=AUTO
 *   
 *   Basic constraint test for no violations:
 *   6)  Persist @NotNull and @Null constraints pass in mode=AUTO
 *   9)  Test @AssertFalse and @AssertTrue constraints pass in mode=AUTO
 *   12) Test @DecimalMin and @DecimalMax constraints pass in mode=AUTO
 *   15) Test @Min and @Max constraints pass in mode=AUTO
 *   18) Test @Digits constraints pass in mode=AUTO
 *   21) Test @Size constraints pass in mode=AUTO
 *   24) Test @Past and @Future constraints pass in mode=AUTO
 *   27) Test @Pattern constraints pass in mode=AUTO
 *   29) Test @Valid constraints pass in mode=AUTO
 *
 * @version $Rev$ $Date$
 */
public class TestConstraints extends AbstractPersistenceTestCase {

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
     * Scenario being tested:
     *   1) Update @Null constraint exception on variables in mode=AUTO
     *      Tests that a constraint violation will occur on invalid update.
     */
    public void testNullUpdateConstraint() {
        getLog().trace("testNullUpdateConstraint() started");
        
        long id = 0;
        // Part 1 - Create and persist a valid entity
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create valid ConstraintNull instance
            em.getTransaction().begin();
            ConstraintNull c = ConstraintNull.createValid();
            em.persist(c);
            em.getTransaction().commit();
            id = c.getId();
            getLog().trace("testNullUpdateConstraint() Part 1 of 2 passed");
        } catch (Exception e) {
            // unexpected
            getLog().trace("testNullUpdateConstraint() Part 1 of 2 failed");
            fail("Caught unexpected exception = " + e);
        } finally {
            closeEM(em);
        }
        
        // Part 2 - Verify that invalid properties are caught on an update
        // create EM from default EMF
        em = emf.createEntityManager();
        assertNotNull(em);        
        try {
            // update entity to be invalid
            ConstraintNull c = em.find(ConstraintNull.class, id);
            em.getTransaction().begin();
            c.setNullRequired(new String("not null"));
            em.flush();
            em.getTransaction().commit();            
            getLog().trace("testNullUpdateConstraint() Part 2 of 2 failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testNullUpdateConstraint() Part 2 of 2 passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   2) No invalid Delete @Null constraint exception when mode=AUTO
     *      Tests that a violation will not occur when deleting invalid entity.
     */
    public void testNullDeleteIgnored() {
        getLog().trace("testNullDeleteIgnored() started");
        
        // Part 1 - Create an invalid entity
        // create our EMF w/ props
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
        OpenJPAPersistence.createEntityManagerFactory(
                "null-none-mode",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);
        // create EM
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("NONE"));
            // create invalid ConstraintNull instance
            em.getTransaction().begin();
            ConstraintNull c = ConstraintNull.createInvalidNull();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testNullDeleteIgnored() Part 1 of 2 passed");
        } finally {
            closeEM(em);
            closeEMF(emf);
        }

        // Part 2 - Verify delete using default group does not cause Validation
        // create our EMF w/ validation mode=CALLBACK
        emf = (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence
            .createEntityManagerFactory(
                "null-callback-mode",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);
        // create EM
        em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("CALLBACK"));
            // get the invalid entity to delete
            Query q = em.createQuery("DELETE FROM VNULL c WHERE c.id = 1");
            em.getTransaction().begin();
            int count = q.executeUpdate();
            em.getTransaction().commit();
            getLog().trace("testNullDeleteIgnored() Part 2 of 2 passed");
        } finally {
            closeEM(em);
            closeEMF(emf);
        }
    }
    
    /**
     * Scenario being tested:
     *   3) No invalid Persist constraint exception when mode=NONE
     *      Tests that no Validation Providers are used when disabled.
     */
    public void testNullConstraintIgnored() {
        getLog().trace("testNullConstraintIgnored() started");
        // create our EMF w/ props
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
            OpenJPAPersistence.createEntityManagerFactory(
                "null-none-mode",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);
        // create EM
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("NONE"));
            // create invalid ConstraintNull instance
            em.getTransaction().begin();
            ConstraintNull c = ConstraintNull.createInvalidNull();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testNullConstraintIgnored() passed");
        } finally {
            closeEM(em);
            closeEMF(emf);
        }
    }

    /**
     * Scenario being tested:
     *   4) Test @Null constraint exception on variables in mode=AUTO
     *      Basic constraint test for a violation exception.
     */
    public void testNullConstraint() {
        getLog().trace("testNullConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintNull instance
            em.getTransaction().begin();
            ConstraintNull c = ConstraintNull.createInvalidNull();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testNullConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testNullConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   5) Test @NotNull constraint exception on getter in mode=AUTO
     *      Basic constraint test for a violation exception.
     */
    public void testNotNullConstraint() {
        getLog().trace("testNotNullConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintNull instance
            em.getTransaction().begin();
            ConstraintNull c = ConstraintNull.createInvalidNotNull();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testNotNullConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testNotNullConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   6) Test @NotNull and @Null constraints pass in mode=AUTO
     *      Basic constraint test for no violations.
     */
    public void testNullNotNullConstraint() {
        getLog().trace("testNullNotNullConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintNull instance
            em.getTransaction().begin();
            ConstraintNull c = ConstraintNull.createValid();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testNullNotNullConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   7) Test @AssertTrue constraint exception on variables in mode=AUTO
     *      Basic constraint test for a violation exception.
     */
    public void testAssertTrueConstraint() {
        getLog().trace("testAssertTrueConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintBoolean c = ConstraintBoolean.createInvalidTrue();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testAssertTrueConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testAssertTrueConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   8) Test @AssertFalse constraint exception on getter in mode=AUTO
     *      Basic constraint test for a violation exception.
     */
    public void testAssertFalseConstraint() {
        getLog().trace("testAssertFalseConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintBoolean c = ConstraintBoolean.createInvalidFalse();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testAssertFalseConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testAssertFalseConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   9) Test @AssertFalse and @AssertTrue constraints pass in mode=AUTO
     *      Basic constraint test for no violations.
     */
    public void testAssertTrueFalseConstraint() {
        getLog().trace("testAssertTrueFalseConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create valid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintBoolean c = ConstraintBoolean.createValid();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testAssertTrueFalseConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   10) Test @DecimalMin constraint exception on variables in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testDecimalMinConstraint() {
        getLog().trace("testDecimalMinConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintDecimal c = ConstraintDecimal.createInvalidMin();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testDecimalMinConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testDecimalMinConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   11) Test @DecimalMax constraint exception on getter in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testDecimalMaxConstraint() {
        getLog().trace("testDecimalMaxConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintDecimal c = ConstraintDecimal.createInvalidMax();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testDecimalMaxConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testDecimalMaxConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   12) Test @DecimalMin and @DecimalMax constraints pass in mode=AUTO
     *       Basic constraint test for no violations.
     */
    public void testDecimalMinMaxConstraint() {
        getLog().trace("testDecimalMinMaxConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create valid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintDecimal c = ConstraintDecimal.createValid();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testDecimalMinMaxConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   13) Test @Min constraint exception on variables in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testMinConstraint() {
        getLog().trace("testMinConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintNumber c = ConstraintNumber.createInvalidMin();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testMinConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testMinConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   14) Test @Max constraint exception on getter in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testMaxConstraint() {
        getLog().trace("testMaxConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintNumber c = ConstraintNumber.createInvalidMax();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testMaxConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testMaxConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   15) Test @Min and @Max constraints pass in mode=AUTO
     *       Basic constraint test for no violations.
     */
    public void testMinMaxConstraint() {
        getLog().trace("testMinMaxConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create valid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintNumber c = ConstraintNumber.createValid();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testMinMaxConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   16) Test @Digits constraint exception on variables in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testDigitsTwoConstraint() {
        getLog().trace("testDigitsTwoConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintDigits c = ConstraintDigits.createInvalidTwoDigits();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testDigitsTwoConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testDigitsTwoConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   17) Test @Digits constraint exception on getter in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testDigitsFiveConstraint() {
        getLog().trace("testDigitsFiveConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintDigits c = ConstraintDigits.createInvalidFiveDigits();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testDigitsFiveConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testDigitsFiveConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   18) Test @Digits constraints pass in mode=AUTO
     *       Basic constraint test for no violations.
     */
    public void testDigitsConstraint() {
        getLog().trace("testDigitsConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create valid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintDigits c = ConstraintDigits.createValid();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testDigitsConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   19) Test @Size constraint exception on variables in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testSizeStringConstraint() {
        getLog().trace("testSizeStringConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintSize c = ConstraintSize.createInvalidString();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testSizeStringConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testSizeStringConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   20) Test @Size constraint exception on getter in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testSizeMapConstraint() {
        getLog().trace("testSizeMapConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintSize c = ConstraintSize.createInvalidMap();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testSizeMapConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testSizeMapConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   21) Test @Size constraints pass in mode=AUTO
     *       Basic constraint test for no violations.
     */
    public void testSizeConstraint() {
        getLog().trace("testSizeConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create valid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintSize c = ConstraintSize.createValid();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testSizeConstraint() passed");
        } catch (Exception e) {
            // unexpected
            getLog().trace("testSizeConstraint() failed");
            fail("Caught unexpected exception = " + e);
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   22) Test @Future constraint exception on variables in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testDatesFutureConstraint() {
        getLog().trace("testDatesFutureConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintDates c = ConstraintDates.createInvalidFuture();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testDatesFutureConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testDatesFutureConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   23) Test @Past constraint exception on getter in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testDatesPastConstraint() {
        getLog().trace("testDatesPastConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintDates c = ConstraintDates.createInvalidPast();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testDatesPastConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testDatesPastConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   24) Test @Past and @Future constraints pass in mode=AUTO
     *       Basic constraint test for no violations.
     */
    public void testDatesConstraint() {
        getLog().trace("testDatesConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create valid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintDates c = ConstraintDates.createValid();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testDatesConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   25) Test @Pattern constraint exception on variables in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testPatternAlphaConstraint() {
        getLog().trace("testPatternAlphaConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintPattern c = ConstraintPattern.createInvalidString();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testPatternAlphaConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testPatternAlphaConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   26) Test @Pattern constraint exception on getter in mode=AUTO
     *       Basic constraint test for a violation exception.
     */
    public void testPatternNumericConstraint() {
        getLog().trace("testPatternNumericConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create invalid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintPattern c = ConstraintPattern.createInvalidZipcode();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testPatternNumericConstraint() failed");
            fail("Expected a ConstraintViolationException");
        } catch (ConstraintViolationException e) {
            // expected
            getLog().trace("Caught expected ConstraintViolationException = " + e);
            getLog().trace("testPatternNumericConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   27) Test @Pattern constraints pass in mode=AUTO
     *       Basic constraint test for no violations.
     */
    public void testPatternConstraint() {
        getLog().trace("testPatternConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // verify Validation Mode
            OpenJPAConfiguration conf = em.getConfiguration();
            assertNotNull(conf);
            assertTrue("ValidationMode",
                conf.getValidationMode().equalsIgnoreCase("AUTO"));
            // create valid ConstraintBoolean instance
            em.getTransaction().begin();
            ConstraintPattern c = ConstraintPattern.createValid();
            em.persist(c);
            em.getTransaction().commit();
            getLog().trace("testPatternConstraint() passed");
        } finally {
            closeEM(em);
        }
    }

    /**
     * Scenario being tested:
     *   28) Test @Valid constraint exceptions in mode=AUTO
     *       Basic constraint test for violation exceptions.
     */
    public void testValidFailuresConstraint() {
        Address a = new Address();
        getLog().trace("testValidFailuresConstraint() started");
        
        // Part 1 - Create an invalid Address entity
        {
            OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI)
                            OpenJPAPersistence.createEntityManagerFactory(
                                    "address-none-mode",
                                    "org/apache/openjpa/integration/validation/persistence.xml");
            assertNotNull(emf);
            // create EM
            OpenJPAEntityManager em = emf.createEntityManager();
            assertNotNull(em);
            try{
                // verify Validation Mode
                OpenJPAConfiguration conf = em.getConfiguration();
                assertNotNull(conf);
                assertTrue("ValidationMode",
                    conf.getValidationMode().equalsIgnoreCase("NONE"));
                // provide an invalid Address (every value is invalid)
                em.getTransaction().begin();
                a.setStreetAddress(null);
                a.setCity("a1!b2@c3#");
                a.setState("00");
                a.setPostalCode("a1b2c3");
                // persist, which should NOT cause a CVE
                em.persist(a);
                em.getTransaction().commit();
                getLog().trace("testValidFailuresConstraint() Part 1 of 2 passed");
            } finally {
                closeEM(em);
                closeEMF(emf);
            }
        }


        // Part 2 - Create a Person entity that uses the invalid address above
        {
            OpenJPAEntityManager em = emf.createEntityManager();
            assertNotNull(em);
            try {
                // verify Validation Mode
                OpenJPAConfiguration conf = em.getConfiguration();
                assertNotNull(conf);
                assertTrue("ValidationMode",
                    conf.getValidationMode().equalsIgnoreCase("AUTO"));
                // create invalid Person instance
                em.getTransaction().begin();
                // create a valid Person, minus the address
                Person p = new Person();
                p.setFirstName("Java");
                p.setLastName("Joe");
                // use invalid Address, which should cause CVEs due to @Valid
                //a = em.getReference(Address.class, a.getId());
                assertNotNull(a);
                p.setHomeAddress(a);
                // persist, which should cause a CVE
                em.persist(p);
                em.getTransaction().commit();
                getLog().trace("testValidFailuresConstraint() Part 2 of 2 failed");
                fail("Expected a ConstraintViolationException");
            } catch (ConstraintViolationException e) {
                // expected
                getLog().trace("Caught expected ConstraintViolationException = " + e);
                Set<ConstraintViolation<?>> cves = e.getConstraintViolations();
                assertNotNull(cves);
                for (ConstraintViolation<?> cv: cves) {
                    getLog().trace("CVE Contains ConstraintViolation = " + cv.getMessage());
                }
                assertEquals("Wrong number of embedded ConstraintViolation failures",
                    5, cves.size());
                getLog().trace("testValidFailuresConstraint() Part 2 of 2 passed");
            } finally {
                closeEM(em);
            }
        }
    }
    
    /**
     * Scenario being tested:
     *   29) Test @Pattern constraints pass in mode=AUTO
     *       Basic constraint test for no violations.
     */
    public void testValidPassConstraint() {
        getLog().trace("testValidPassConstraint() started");
        // create EM from default EMF
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            // provide a valid Address
            em.getTransaction().begin();
            Address a = new Address();
            a.setStreetAddress("4205 South Miami Blvd.");
            a.setCity("R.T.P.");
            a.setState("NC");
            a.setPostalCode("27709");
            // persist, which should NOT cause a CVE
            em.persist(a);
            em.getTransaction().commit();

            // create a valid Person
            em.getTransaction().begin();
            Person p = new Person();
            p.setFirstName("Java");
            p.setLastName("Joe");
            p.setHomeAddress(a);
            // persist, which should NOT cause a CVE
            em.persist(p);
            em.getTransaction().commit();
            getLog().trace("testValidPassConstraint() passed");
        } finally {
            closeEM(em);
        }
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
