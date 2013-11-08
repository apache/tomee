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

import java.util.Iterator;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;


public class TestMappedSuper extends AbstractPersistenceTestCase {

    /*
     * Verify constraints defined via XML on a mapped superclass are validated.
     */
    public void testMappedSuperXMLConstraint() {
        OpenJPAEntityManagerFactorySPI emf = (OpenJPAEntityManagerFactorySPI) 
        OpenJPAPersistence.createEntityManagerFactory(
                "XMLConstraintPU",
                "org/apache/openjpa/integration/validation/persistence.xml");
        assertNotNull(emf);
        OpenJPAEntityManager em = emf.createEntityManager();
        assertNotNull(em);
        
        XMLBase be = new XMLBase();
        try {
            em.getTransaction().begin();
            em.persist(be);
            em.getTransaction().commit();
            fail("Should have caught a ConstraintViolationException");
        }
        catch (ConstraintViolationException e) {
            checkCVE(e, "superStrValue", "strValue");
            if (em.getTransaction().isActive()) {
                em.getTransaction().setRollbackOnly();
            }
        }
        catch (Exception e) {
            fail("Should have caught a ConstraintViolationException, but instead caught Exception=" + e);
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

}
