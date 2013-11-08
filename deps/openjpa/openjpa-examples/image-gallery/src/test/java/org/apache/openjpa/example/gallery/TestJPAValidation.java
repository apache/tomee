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

package org.apache.openjpa.example.gallery;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.openjpa.example.gallery.model.Image;
import org.apache.openjpa.example.gallery.model.Location;

public class TestJPAValidation extends junit.framework.TestCase {

    /**
     * Shows usage of BV constraints with JPA at pre-update, pre-remove,
     * and pre-persist lifecycle events.
     */
    public void testValidation() {
        EntityManagerFactory emf = 
            Persistence.createEntityManagerFactory("BeanValidation");
        EntityManager em = emf.createEntityManager();

        // Create a valid location
        Location loc = new Location();
        loc.setCity("Rochester");
        loc.setState("MN");
        loc.setZipCode("55901");
        loc.setCountry("USA");

        // Create an Image with non-matching type and file extension
        Image img = new Image();
        img.setType(ImageType.JPEG);
        img.setFileName("Winter_01.gif");
        loadImage(img);
        img.setLocation(loc);
        
        // *** PERSIST ***
        try {
            em.getTransaction().begin();
            System.out.println("Persisting an entity with non-matching extension and type");
            em.persist(img);
            fail();
        } catch (ConstraintViolationException cve) {
            // Transaction was marked for rollback, roll it back and
            // start a new TX
            em.getTransaction().rollback();
            handleConstraintViolation(cve);
            em.getTransaction().begin();
            System.out.println("Fixing the file type and re-attempting the persist.");
            img.setType(ImageType.GIF);
            em.persist(img);
            em.getTransaction().commit();
            System.out.println("Persist was successful");
        }

        // *** UPDATE ***
        try {
            em.getTransaction().begin();
            // Modify the file name to a non-matching file name 
            // and commit to trigger an update
            System.out.println("Modifying file name to use an extension that does not");
            System.out.println("match the file type.  This will cause a CVE.");
            img.setFileName("Winter_01.jpg");
            em.getTransaction().commit();
            fail();
        }  catch (ConstraintViolationException cve) {
            // Handle the exception.  The commit failed so the transaction
            // was already rolled back.
            System.out.println("Update failed as expected");
            handleConstraintViolation(cve);
        }
        // The update failure caused img to be detached. It must be merged back 
        // into the persistence context.
        img = em.merge(img);

        // *** REMOVE ***
        em.getTransaction().begin();
        try {
            // Remove the type and commit to trigger removal
            System.out.println("Setting the type to an invalid type.  This will cause a");
            System.out.println("validation exception upon removal");
            img.setType(null);
            em.remove(img);
            fail();
        }  catch (ConstraintViolationException cve) {
            // Rollback the active transaction and handle the exception
            em.getTransaction().rollback();
            System.out.println("Remove failed as expected");
            handleConstraintViolation(cve);
        }
        em.close();
        emf.close();
        System.out.println("Done");
    }

    // Handles constraint violations by printing out violation information
    private static void handleConstraintViolation(ConstraintViolationException cve) {
      Set<ConstraintViolation<?>> cvs = cve.getConstraintViolations();
      for (ConstraintViolation<?> cv : cvs) {
          System.out.println("------------------------------------------------");
          System.out.println("Violation: " + cv.getMessage());
          System.out.println("Entity: " + cv.getRootBeanClass().getSimpleName());
          // The violation occurred on a leaf bean (embeddable)
          if (cv.getLeafBean() != null && cv.getRootBean() != cv.getLeafBean()) {
              System.out.println("Embeddable: " + cv.getLeafBean().getClass().getSimpleName());
          }
          System.out.println("Attribute: " + cv.getPropertyPath());
          System.out.println("Invalid value: " + cv.getInvalidValue());
          System.out.println("------------------------------------------------");
      }
    }

    // Mock image loading utility...  simply loads the GIF89a header to satisfy the 
    // constraint validator.
    private static void loadImage(Image img) {
        img.setData(new byte[] { 'G', 'I', 'F', '8', '9', 'a' });
    }
    
}
