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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

/**
 * Tests the Bean Validation groups support as defined in the JPA 2.0 spec
 * via the following scenarios:
 *      
 * Verify default validation group on lifecycle events:
 * 1a) PrePersist and PreUpdate validate with default validation group
 * 1b) PreRemove does not validate with default validation group
 * 1c) Specify the default group for PreRemove and verify that it validates with
 *     the default group.
 * 1d) Verify validation for constraints using non-default validation groups 
 *     does not occur.
 * 1e) PrePersist does not validate with no validation group defined.
 * 1f) PreUpdate does not validate when no validation group defined.
 * 1g) PreUpdate only called when a pre-update validation group defined 
 *     (ie. per-persist and pre-remove are disabled).
 * 1h) PrePersist only called when pre-persist validation group defined
 *     (ie. per-persist and pre-remove are disabled).
 *    
 * Verify validation occurs when specific validation groups are specified:
 * 2a) Specify a non-default group for all lifecycle events.
 * 2b) Specify multiple/mixed non-default groups for lifecycle events.
 * 
 * Verify validation does not occur when no validation groups are specified:
 * 3a) Specify an empty validation group for PrePersist and PreUpdate and
 *     verify validation does not occur on these events.
 *
 * @version $Rev$ $Date$
 */
public class TestValidationGroups extends AbstractPersistenceTestCase {

    /**
     * 1a) verify validation occurs using the default validation groups
     * on pre-persist and pre-update on commit
     */
    public void testDefaultValidationGroup() {
        verifyDefaultValidationGroup(false);
    }

    /**
     * 1af) verify validation occurs using the default validation groups
     * on pre-persist and pre-update on flush
     */
    public void testDefaultValidationGroupFlush() {
        verifyDefaultValidationGroup(true);
    }
    

    /**
     * 1b) verify validation does not occur using the default validation group
     * on the PreRemove lifecycle event on commit.  
     */
    public void testDefaultPreRemove() {
        verifyDefaultPreRemove(false);
    }

    /**
     * 1bf) verify validation does not occur using the default validation group
     * on the PreRemove lifecycle event on flush.  
     */
    public void testDefaultPreRemoveFlush() {
        verifyDefaultPreRemove(true);
    }
    
    /**
	 * 1c) verify validation occurs on the default group when default is 
	 *  specified for pre-remove on commit
	 */
	public void testSpecifiedDefaultPreRemove() {
	    verifySpecifiedDefaultPreRemove(true);
	}

    /**
     * 1cf) verify validation occurs on the default group when default is 
     *  specified for pre-remove on flush
     */
    public void testSpecifiedDefaultPreRemoveFlush() {
        verifySpecifiedDefaultPreRemove(false);
    }
    
    /** 
     * 1e) PrePersist does not validate with no validation group defined.
     */
    public void testPersistNoValidationGroup() {
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "no-pre-persist-default-validation-group",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);

        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            DefGrpEntity dge = new DefGrpEntity();
            dge.setDgName(null);  // If default group was enabled for pre-persist, this would cause a CVE.
            try {
                em.getTransaction().begin();
                em.persist(dge);
                em.getTransaction().commit();
            } catch (ConstraintViolationException e) {
                fail("A ConstraintViolationException should not have been thrown " +
                "on pre-persist");
            } finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
        } finally {
            closeEM(em);
            closeEMF(emf);
        }
    }
    
    /**
     *  1f) PreUpdate does not validate when no validation group defined.
     */
    public void testUpdateNoValidationGroup() {
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "no-pre-update-default-validation-group",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);

        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            DefGrpEntity dge = new DefGrpEntity();
            dge.setDgName("NotNull");
            try {
                em.getTransaction().begin();
                em.persist(dge);
                em.getTransaction().commit();
            } catch (ConstraintViolationException e) {
                fail("A ConstraintViolationException should not have been thrown " +
                "on pre-persist");
            } 
            try {
                em.getTransaction().begin();
                dge.setDgName(null);
                em.getTransaction().commit();
            } catch (ConstraintViolationException e) {
                fail("A ConstraintViolationException should not have been thrown " +
                "on pre-update");
            } finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
        } finally {
            closeEM(em);
            closeEMF(emf);
        }
    }

    /** 
     * 1g) PreUpdate only called when a pre-update validation group defined 
     *     (ie. per-persist and pre-remove are disabled).
     */
    public void testUpdateOnlyValidationGroup() {
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "no-pre-persist-default-validation-group",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);

        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            DefGrpEntity dge = new DefGrpEntity();
            dge.setDgName(null);  // If default group enabled for pre-persist, this would cause a CVE.
            try {
                em.getTransaction().begin();
                em.persist(dge);
                em.getTransaction().commit();
            } catch (ConstraintViolationException e) {
                fail("A ConstraintViolationException should not have been thrown " +
                "on pre-persist");
            } 
            try {
                em.getTransaction().begin();
                dge.setDgName("NotNull");
                dge.setDgName(null);
                em.getTransaction().commit();
                fail("A ConstraintViolationException should have been thrown " +
                "on pre-update");
            } catch (ConstraintViolationException e) {
                // expected
            } 
            finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
        } finally {
            closeEM(em);
            closeEMF(emf);
        }
    }
    /**
     *  1h) PrePersist only called when pre-persist validation group defined
     *      (ie. per-persist and pre-remove are disabled).
     */
    public void testPersistOnlyValidationGroup() {
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "no-pre-update-default-validation-group",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);

        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            DefGrpEntity dge = new DefGrpEntity();
            dge.setDgName(null);
            try {
                em.getTransaction().begin();
                em.persist(dge);
                em.getTransaction().commit();
                fail("A ConstraintViolationException should have been thrown " +
                "on pre-persist");
            } catch (ConstraintViolationException e) {
                // Expected
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
            // Fix the entity, persist with no CVE
            try {
                em.getTransaction().begin();
                dge.setDgName("NotNull");
                em.getTransaction().commit();
            } catch (Exception e) {
                fail("An Exception should not have been thrown " +
                "on update");
            }
            // Update the entity with null value, should not case a CVE
            try {
                em.getTransaction().begin();
                dge.setDgName(null);
                em.getTransaction().commit();
            } catch (ConstraintViolationException e) {
                fail("A ConstraintViolationException should not have been thrown " +
                "on pre-update");
            } 
            finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
            
        } finally {
            closeEM(em);
            closeEMF(emf);
        }
    }
    
    /**
     * 2a) verify non-default validation group for all lifecycle events on
     * commit.  default validation group constraints should not validate
     */
    public void testNonDefaultValidationGroup() {
        verifyNonDefaultValidationGroup(false);
    }

    /**
     * 2af) verify non-default validation group for all lifecycle events on
     * flush.  default validation group constraints should not validate
     */
    public void testNonDefaultValidationGroupFlush() {
        verifyNonDefaultValidationGroup(true);
    }
    
    /**
     * 2b1) verify multiple/mixed validation groups via persistence.xml
     * @param flush
     */
    public void testPesistenceXMLMultipleValidationGroups() {

        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "multi-validation-group-xml",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);
        verifyMultipleValidationGroups(emf);
    }
    /**
     * 2b2) verify multiple/mixed validation groups via properties
     * @param flush
     */
    public void testMultipleValidationGroups() {

        // Configure persistence properties via map
        Map<String, Object> propMap = new HashMap<String, Object>();
        propMap.put("javax.persistence.validation.group.pre-persist",
            "org.apache.openjpa.integration.validation.ValGroup1," +
            "org.apache.openjpa.integration.validation.ValGroup2");

        propMap.put("javax.persistence.validation.group.pre-update",
            "");

        propMap.put("javax.persistence.validation.group.pre-remove",
            "org.apache.openjpa.integration.validation.ValGroup2");

        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "multi-validation-group",
                "org/apache/openjpa/integration/validation/persistence.xml",
                propMap);
        assertNotNull(emf);
        verifyMultipleValidationGroups(emf);
    }
        
    private void verifyMultipleValidationGroups(OpenJPAEntityManagerFactorySPI emf) {
        // create EM
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        
        try {
            MixedGrpEntity mge = new MixedGrpEntity();
            
            // Assert vg1 and vg2 fire on pre-persist
            try
            {
                em.getTransaction().begin();
                em.persist(mge);
                em.getTransaction().commit();
            } catch (ConstraintViolationException e) {
                checkCVE(e, 
                    "vg1NotNull",
                    "vg2NotNull",
                    "vg12NotNull");
            }
            catch (Exception e) {
                fail("Should have caught a ConstraintViolationException");
            }
            finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }            
            // Assert no validation occurs on pre-update
            // Persist an entity.  Default should not validate on pre-persist
            em.getTransaction().begin();
            mge.setVg1NotNull("Vg1");
            mge.setVg2NotNull("Vg2");
            mge.setVg12NotNull("Vg1&2");
            em.persist(mge);
            em.getTransaction().commit();

            
            try {
                em.getTransaction().begin();
                mge.setDefNotNull(null);
                mge.setVg12NotNull(null);
                mge.setVg1NotNull(null);
                mge.setVg2NotNull(null);
                em.getTransaction().commit(); 
            } catch (ConstraintViolationException e) {
                fail("Update should have been successful." +
                     " Caught unexpected ConstraintViolationException.");
            }
            catch (Exception e) {
                fail("Update should have been successful." +
                     " Caught unexpected exception.");
            }
            
            // Update the entity again so that it can be cleaned up by the
            // emf cleanup facility.  The update should not validate
            em.getTransaction().begin();
            mge.setVg2NotNull("Vg2NotNull");
            mge.setVg12NotNull("Vg12NotNull");
            em.getTransaction().commit();   

            // Assert vg2 and default groups validate on pre-remove
            try {
                em.getTransaction().begin();
                mge.setDefNotNull(null);
                mge.setVg1NotNull(null);
                mge.setVg2NotNull(null);
                mge.setVg12NotNull(null);
                em.remove(mge);
                em.getTransaction().commit();                
            } catch (ConstraintViolationException e) {
                checkCVE(e, 
                    "vg2NotNull",
                    "vg12NotNull");
            }
            catch (Exception e) {
                fail("Should have caught a ConstraintViolationException");
            }
            finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
            
        }
        finally {
            closeEM(em);
            closeEMF(emf);
        }        
    }

    /**
     * 3a) No validation groups for pre-persist and pre-update and none for
     * pre-remove by default.  No validation should occur.
     */
    public void testNoValidationGroups() {

        // Configure persistence properties via map
        Map<String, Object> propMap = new HashMap<String, Object>();
        propMap.put("javax.persistence.validation.group.pre-persist","");

        propMap.put("javax.persistence.validation.group.pre-update","");

        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "multi-validation-group",
                "org/apache/openjpa/integration/validation/persistence.xml",
                propMap);
        assertNotNull(emf);
        // create EM
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);

        try {
            MixedGrpEntity mge = new MixedGrpEntity();
            
            try
            {
                // No validation on pre-persist
                em.getTransaction().begin();
                em.persist(mge);
                em.getTransaction().commit();                
                
                // No validation on pre-update
                em.getTransaction().begin();
                mge.setVg12NotNull(null);
                em.getTransaction().commit();

                // No validation on pre-remove
                em.getTransaction().begin();
                em.remove(mge);
                em.getTransaction().commit();
            } catch (ConstraintViolationException e) {
                fail("Operations should have been successful." +
                     " Caught unexpected ConstraintViolationException.");
            }
            catch (Exception e) {
                fail("Operations should have been successful." +
                     " Caught unexpected exception.");
            }            
        }
        finally {
            closeEM(em);
            closeEMF(emf);
        }        
    }

    private void verifyDefaultValidationGroup(boolean flush) {
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "default-validation-group",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);
        getLog(emf).trace("verifyDefaultValidationGroup(" + flush + ")");
        // create EM
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            DefGrpEntity dge = new DefGrpEntity();
            // Test pre-persist with default group with flush after persist
            // 1a) pre-persist
            try {
                em.getTransaction().begin();
                em.persist(dge);
                if (flush)
                    em.flush();
                else
                    em.getTransaction().commit();
                fail("A ConstraintViolationException should have been thrown " +
                    "on pre-persist");
            } catch (ConstraintViolationException e) {
                checkCVE(e, "dgName");
                // If flushing, tx should be marked for rollback
                if (flush) {
                    assertTrue(em.getTransaction().isActive());
                    assertTrue(em.getTransaction().getRollbackOnly());
                }
            }
            catch (Exception e) {
                fail("Should have caught a ConstraintViolationException");
            }
            finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }

            // 1a) test pre-update with default group
            // Add an entity with valid data (validation passes)
            dge.setDgName("NonNullName");
            em.getTransaction().begin();
            em.persist(dge);
            em.getTransaction().commit();
            try {
                // Update the entity with null value.  pre-update 
                // validation should fail on flush or commit
                em.getTransaction().begin();
                dge.setDgName(null);
                if (flush)
                    em.flush();
                else
                    em.getTransaction().commit();
                fail("A ConstraintViolationException should have been thrown " +
                    "on pre-update");
            } catch (ConstraintViolationException e) {
                checkCVE(e, "dgName");
                // If flushing, tx should be marked for rollback
                if (flush) {
                    assertTrue(em.getTransaction().isActive());
                    assertTrue(em.getTransaction().getRollbackOnly());
                }
            }
            catch (Exception e) {
                fail("Should have caught a ConstraintViolationException");
            }
            finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
        }
        finally {
            closeEM(em);
            closeEMF(emf);
        }
    }

    private void verifyNonDefaultValidationGroup(boolean flush) {
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "non-default-validation-group",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);
        getLog(emf).trace("verifyNonDefaultValidationGroup(" + flush + ")");
        // create EM
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        try {
            NonDefGrpEntity ndge = new NonDefGrpEntity();
            // Test pre-persist with non-default group with flush after persist
            try {
                em.getTransaction().begin();
                em.persist(ndge);
                if (flush)
                    em.flush();
                else
                    em.getTransaction().commit();
                fail("A ConstraintViolationException should have been thrown " +
                    "on pre-persist");
            } catch (ConstraintViolationException e) {
                checkCVE(e, "ndgName");
                getLog(emf).trace("Caught expected exception");
                // If flushing, tx should be marked for rollback
                if (flush) {
                    assertTrue(em.getTransaction().isActive());
                    assertTrue(em.getTransaction().getRollbackOnly());
                }
            }
            catch (Exception e) {
                fail("Should have caught a ConstraintViolationException");
            }
            finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }

            // pre-update with non-default group.  default group
            // validation should not occur
            // Add an entity with valid data (validation passes)
            try {
                ndge.setNdgName("NonNullName");
                ndge.setDgName(null);
                em.getTransaction().begin();
                em.persist(ndge);
                em.getTransaction().commit();
                getLog(emf).trace("Entity was persisted. As expected, no " +
                "validation took place on pre-persist with default group.");
            }
            catch (ConstraintViolationException e) {
                fail("Caught unexpected exception");
                if (em.getTransaction().isActive())
                    em.getTransaction().rollback();
            }                
            try {
                // Update the entity with null value.  pre-update 
                // validation should fail on flush or commit
                
                em.getTransaction().begin();
                ndge.setNdgName(null);
                if (flush)
                    em.flush();
                else
                    em.getTransaction().commit();
                fail("A ConstraintViolationException should have been thrown " +
                    "on pre-update");
            } catch (ConstraintViolationException e) {
                checkCVE(e, "ndgName");
                // If flushing, tx should be marked for rollback
                if (flush) {
                    assertTrue(em.getTransaction().isActive());
                    assertTrue(em.getTransaction().getRollbackOnly());
                }
            }
            catch (Exception e) {
                fail("Should have caught a ConstraintViolationException");
            }
            finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }

            // Merge the entity so that it can be removed.
            em.getTransaction().begin();
            ndge.setDgName(null);
            ndge.setNdgName("Some name");
            ndge = em.merge(ndge);
            em.getTransaction().commit();
            
            try {
                // Update the entity with null value and remove the entity.  
                // validation should not fail on pre-remove
                em.getTransaction().begin();
                ndge.setNdgName(null);
                em.remove(ndge);
                if (flush)
                    em.flush();
                else
                    em.getTransaction().commit();
                fail("A ConstraintViolationException should have been thrown " +
                    "on pre-remove");
            } catch (ConstraintViolationException e) {
                checkCVE(e, "ndgName");
                // If flushing, tx should be marked for rollback
                if (flush) {
                    assertTrue(em.getTransaction().isActive());
                    assertTrue(em.getTransaction().getRollbackOnly());
                }
            }
            catch (Exception e) {
                fail("Should have caught a ConstraintViolationException");
            }

        }
        finally {
            closeEM(em);
            closeEMF(emf);
        }
    }

    /**
     * verify validation does not occur using the default validation group
     * on the PreRemove lifecycle event.  
     */
    public void verifyDefaultPreRemove(boolean flush) {
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "default-validation-group",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);
        getLog(emf).trace("verifyDefaultPreRemove(" + flush + ")");
        // create EM
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);

        try {
            // Add an entity 
            DefGrpEntity dge = new DefGrpEntity();
            dge.setDgName("NonNullName");
            em.getTransaction().begin();
            em.persist(dge);
            em.getTransaction().commit();
            try {
                // Update the entity with null value and remove the entity.  
                // validation should not fail on pre-remove
                em.getTransaction().begin();
                dge.setDgName(null);
                em.remove(dge);
                if (flush)
                    em.flush();
                else
                    em.getTransaction().commit();
                getLog(emf).trace("Entity was removed. As expected, no " +
                    "validation took place on pre-remove.");
            } catch (ConstraintViolationException e) {
                fail("Should not have caught a ConstraintViolationException");
                getLog(emf).trace("Caught expected exception");
            }
            catch (Exception e) {
                fail("Should not have caught an Exception");
            }
            finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
        }
        finally {
            closeEM(em);
            closeEMF(emf);
        }
    }

    /**
     * verify validation occurs when the default validation group
     * is specified for the PreRemove lifecycle event via the 
     * "javax.persistence.validation.group.pre-remove" property.
     */
    public void verifySpecifiedDefaultPreRemove(boolean flush) {
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "pre-remove-default-validation-group",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);
        getLog(emf).trace("verifySpecifiedDefaultPreRemove(" + flush + ")");
        // create EM
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);

        try {
            // Add an entity 
            DefGrpEntity dge = new DefGrpEntity();
            dge.setDgName("NonNullName");
            em.getTransaction().begin();
            em.persist(dge);
            em.getTransaction().commit();
            try {
                // Update the entity with null value and remove the entity.  
                // validation should not fail on pre-remove
                em.getTransaction().begin();
                dge.setDgName(null);
                em.remove(dge);
                if (flush)
                    em.flush();
                else
                    em.getTransaction().commit();
                fail("A ConstraintViolationException should have been thrown " +
                    "on pre-remove");
            } catch (ConstraintViolationException e) {
                checkCVE(e, "dgName");
                // If flushing, tx should be marked for rollback
                if (flush) {
                    assertTrue(em.getTransaction().isActive());
                    assertTrue(em.getTransaction().getRollbackOnly());
                }
            }
            catch (Exception e) {
                fail("Should have caught a ConstraintViolationException");
            }
            finally {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
        }
        finally {
            closeEM(em);
            closeEMF(emf);
        }
    }

    private void checkCVE(ConstraintViolationException e,
        String... vioProperties) {
        Set<ConstraintViolation<?>>cvs = e.getConstraintViolations();
        if (vioProperties.length == 0 && cvs == null)
            return;
        assertEquals(vioProperties.length, cvs.size());
        Iterator<ConstraintViolation<?>> i = 
            (Iterator<ConstraintViolation<?>>) cvs.iterator();
        while (i.hasNext()) {
            ConstraintViolation<?> v = (ConstraintViolation<?>)i.next();
            boolean found = false;
            for (String vio : vioProperties) {
                if (v.getPropertyPath().toString().compareTo(vio) == 0) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fail("Unexpected ConstraintViolation for: " + 
                    v.getPropertyPath());
            }
        }
    }
    
    /**
     * Internal convenience method for getting the OpenJPA logger
     * 
     * @return Log
     */
    private Log getLog(OpenJPAEntityManagerFactorySPI emf) {
        return emf.getConfiguration().getLog("Tests");
    }
}
