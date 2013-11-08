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
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public class TestConstraintViolation extends AbstractPersistenceTestCase {

    EntityManagerFactory emf2 = null;
    Log log = null;

    public void setUp() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("javax.persistence.validation.group.pre-remove", "javax.validation.groups.Default");

        // This test case uses a different persistence xml file because validation require 2.0 xsd.
        emf2 = OpenJPAPersistence.createEntityManagerFactory("ConstraintViolationTestPU",
                "org/apache/openjpa/integration/validation/persistence.xml", props);
        log = ((OpenJPAEntityManagerFactorySPI)emf2).getConfiguration().getLog("Tests");

        EntityManager em = emf2.createEntityManager();
        Image img = em.find(Image.class, 1);
        if (img != null) {
            em.getTransaction().begin();
            em.remove(img);
            em.getTransaction().commit();
        }
        em.close();
    }

    public void testPersistNormalValidation() {
        EntityManager em = emf2.createEntityManager();

        // Persist with successful validations
        Location loc = new Location();
        loc.setCity("Rochester");
        loc.setStreet(null);
        loc.setState("MN");
        loc.setZipCode("55901");
        loc.setCountry("USA");

        Image img = new Image();
        img.setId(1);
        img.setFileName("Winter_01.gif");
        img.setLocation(loc);

        try {
            em.getTransaction().begin();
            log.trace("------------------------------------------------");
            log.trace("** Persist with successful validations");
            em.persist(img);
        } catch (ConstraintViolationException cve) {
            // Transaction was marked for rollback, roll it back and start a new TX
            Set<ConstraintViolation<?>> cvs = cve.getConstraintViolations();
            for (ConstraintViolation<?> cv : cvs) {
                log.trace("Message: " + cv.getMessage());
                log.trace("RootBean: " + cv.getRootBean());
                log.trace("LeafBean: " + cv.getLeafBean());
                log.trace("PropertyPath: " + cv.getPropertyPath());
                log.trace("Invalid value: " + cv.getInvalidValue());
            }
            fail();
        } finally {
            em.getTransaction().rollback();
            em.close();
            emf2.close();
        }
    }

    public void testPersistImageNullValidation() {
        EntityManager em = emf2.createEntityManager();

        // Persist with null filename in Image
        Location loc = new Location();
        loc.setCity("Rochester");
        loc.setStreet("3605 Hwy 52 N");
        loc.setState("MN");
        loc.setZipCode("55901");
        loc.setCountry("USA");

        Image img = new Image();
        img.setId(1);
        img.setFileName(null);
        img.setLocation(loc);

        try {
            em.getTransaction().begin();
            log.trace("------------------------------------------------");
            log.trace("** Persist with null filename in Image");
            em.persist(img);
            fail();
        } catch (ConstraintViolationException cve) {
            // Transaction was marked for rollback, roll it back and start a new TX
            Set<ConstraintViolation<?>> cvs = cve.getConstraintViolations();
            assertEquals(1, cvs.size());
            for (ConstraintViolation<?> cv : cvs) {
                log.trace("Message: " + cv.getMessage());
                log.trace("RootBean: " + cv.getRootBean());
                log.trace("LeafBean: " + cv.getLeafBean());
                log.trace("PropertyPath: " + cv.getPropertyPath());
                log.trace("Invalid value: " + cv.getInvalidValue());

                assertEquals("Image file name must not be null.", cv.getMessage());
                assertEquals("Image", cv.getRootBeanClass().getSimpleName());
                assertEquals("Image", cv.getLeafBean().getClass().getSimpleName());
                assertTrue(cv.getLeafBean().getClass() == cv.getRootBeanClass());
                assertEquals("fileName", cv.getPropertyPath().toString());
                assertNull(cv.getInvalidValue());
            }
        } finally {
            em.getTransaction().rollback();
            em.close();
            emf2.close();
        }
    }

    public void testPersistEmbedCityNullValidation() {
        EntityManager em = emf2.createEntityManager();

        // Persist with null city name in location
        Location loc = new Location();
        loc.setCity(null);
        loc.setStreet("3605 Hwy 52 N");
        loc.setState("MN");
        loc.setZipCode("55901");
        loc.setCountry("USA");

        Image img = new Image();
        img.setId(1);
        img.setFileName("Winter_01.gif");
        img.setLocation(loc);

        try {
            em.getTransaction().begin();
            log.trace("------------------------------------------------");
            log.trace("** Persist with null city name in location" );
            em.persist(img);
            fail();
        } catch (ConstraintViolationException cve) {
            // Transaction was marked for rollback, roll it back and start a new TX
            Set<ConstraintViolation<?>> cvs = cve.getConstraintViolations();
            assertEquals(1, cvs.size());
            for (ConstraintViolation<?> cv : cvs) {
                log.trace("Message: " + cv.getMessage());
                log.trace("RootBean: " + cv.getRootBean());
                log.trace("LeafBean: " + cv.getLeafBean());
                log.trace("PropertyPath: " + cv.getPropertyPath());
                log.trace("Invalid value: " + cv.getInvalidValue());

                assertEquals("City must be specified.", cv.getMessage());
                assertEquals("Image", cv.getRootBeanClass().getSimpleName());
                // The violation occurred on a leaf bean (embeddable)
                assertEquals("Location", cv.getLeafBean().getClass().getSimpleName());
                assertTrue(cv.getLeafBean().getClass() != cv.getRootBeanClass());
                assertEquals("location.city", cv.getPropertyPath().toString());
                assertNull(cv.getInvalidValue());
            }
        } finally {
            em.getTransaction().rollback();
            em.close();
            emf2.close();
        }
    }

    public void testRemoveEmbedCityNullValidation() {
        EntityManager em = emf2.createEntityManager();

        // Remove with null city name in location
        Location loc = new Location();
        loc.setCity("Rochester");
        loc.setStreet("3605 Hwy 52 N");
        loc.setState("MN");
        loc.setZipCode("55901");
        loc.setCountry("USA");

        Image img = new Image();
        img.setId(1);
        img.setFileName("Winter_01.gif");
        img.setLocation(loc);

        try {
            em.getTransaction().begin();
            log.trace("------------------------------------------------");
            log.trace("** Create normal Image/location" );
            em.persist(img);
            em.getTransaction().commit();
        } catch (ConstraintViolationException cve) {
            fail();
        }
            
        try {
            em.getTransaction().begin();
            log.trace("** set null city name in location and remove" );
            img.getLocation().setCity(null);
            em.remove(img);
            fail();
        } catch (ConstraintViolationException cve) {
            // Transaction was marked for rollback, roll it back and
            // start a new TX
            Set<ConstraintViolation<?>> cvs = cve.getConstraintViolations();
            assertEquals(1, cvs.size());
            for (ConstraintViolation<?> cv : cvs) {
                log.trace("Message: " + cv.getMessage());
                log.trace("RootBean: " + cv.getRootBean());
                log.trace("LeafBean: " + cv.getLeafBean());
                log.trace("PropertyPath: " + cv.getPropertyPath());
                log.trace("Invalid value: " + cv.getInvalidValue());

                assertEquals("City must be specified.", cv.getMessage());
                assertEquals("Image", cv.getRootBeanClass().getSimpleName());
                // The violation occurred on a leaf bean (embeddable)
                assertEquals("Location", cv.getLeafBean().getClass().getSimpleName());
                assertTrue(cv.getLeafBean().getClass() != cv.getRootBeanClass());
                assertEquals("location.city", cv.getPropertyPath().toString());
                assertNull(cv.getInvalidValue());
            }
        } finally {
            em.getTransaction().rollback();
            em.close();
            emf2.close();
        }
    }

    public void testFlushEmbedCityNullValidation() {
        EntityManager em = emf2.createEntityManager();

        // set invalid zipCode in location and flush testing pre-update
        Location loc = new Location();
        loc.setCity("Rochester");
        loc.setStreet("3605 Hwy 52 N");
        loc.setState("MN");
        loc.setZipCode("55901");
        loc.setCountry("USA");

        Image img = new Image();
        img.setId(1);
        img.setFileName("Winter_01.gif");
        img.setLocation(loc);

        try {
            em.getTransaction().begin();
            log.trace("------------------------------------------------");
            log.trace("** Create normal Image/location" );
            em.persist(img);
            em.getTransaction().commit();
        } catch (ConstraintViolationException cve) {
            fail();
        }

        try {
            em.getTransaction().begin();
            log.trace("** set invalid zipCode and flush testing pre-update" );
            img.getLocation().setZipCode("abcde");
            em.flush();
            fail();
        } catch (ConstraintViolationException cve) {
            // Transaction was marked for rollback, roll it back and start a new TX
            Set<ConstraintViolation<?>> cvs = cve.getConstraintViolations();
            assertEquals(1, cvs.size());
            for (ConstraintViolation<?> cv : cvs) {
                log.trace("Message: " + cv.getMessage());
                log.trace("RootBean: " + cv.getRootBean());
                log.trace("LeafBean: " + cv.getLeafBean());
                log.trace("PropertyPath: " + cv.getPropertyPath());
                log.trace("Invalid value: " + cv.getInvalidValue());

                assertEquals("Zip code must be 5 digits or use the 5+4 format.", cv.getMessage());
                assertEquals("Image", cv.getRootBeanClass().getSimpleName());
                // The violation occurred on a leaf bean (embeddable)
                assertEquals("Location", cv.getLeafBean().getClass().getSimpleName());
                assertTrue(cv.getLeafBean().getClass() != cv.getRootBeanClass());
                assertEquals("location.zipCode", cv.getPropertyPath().toString());
                assertEquals("abcde", cv.getInvalidValue());
            }
        } finally {
            em.getTransaction().rollback();
            em.close();
            emf2.close();
        }
    }
}
