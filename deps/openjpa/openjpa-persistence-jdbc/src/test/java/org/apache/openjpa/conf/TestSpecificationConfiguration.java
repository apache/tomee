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
package org.apache.openjpa.conf;

import javax.persistence.PersistenceException;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests Specification of Configuration.
 * 
 * 
 * @author Pinaki Poddar
 * 
 */
public class TestSpecificationConfiguration extends SingleEMFTestCase {
    
    public void testSpecificationIsSet() {
        Specification spec = getSpecifcation();
        assertNotNull(spec);
    }
    
    public void testSpecificationIsJPA() {
        Specification spec = getSpecifcation();
        assertTrue(spec.isSame("JPA"));
        assertTrue(spec.isSame("jpa"));
    }
    
    public void testSpecificationVersionIsJPA1() {
        Specification spec = getSpecifcation();
        int major = spec.getVersion();
        assertEquals(1, major);
        assertTrue(spec.isSame("JPA"));
    }

    public void testSpecificationVersionIsJPA2() {
        
        OpenJPAEntityManagerFactorySPI emf1 = null;
        try {
          emf1 = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("persistence2_0",
            "org/apache/openjpa/conf/META-INF/" +
            "persistence-2_0-config.xml");

            Specification spec = emf1.getConfiguration().getSpecificationInstance();
            int major = spec.getVersion();
            assertEquals(2, major);
            assertTrue(spec.isSame("JPA"));
        } finally {
            clear(emf1);
            closeEMF(emf1);
        }
    }

    public void testLowerVersionCanBeSet() {
        super.setUp("openjpa.Specification", "JPA 1.0");
        Specification spec = getSpecifcation();
        
        assertNotNull(spec);
        assertEquals(1, spec.getVersion());
    }
    
    /*
     * Slight change for 2.0, in that the geronimo spec does not rethrow
     * all exceptions wrapped as a PersistenceException
     */
    public void testHigherVersionCanNotBeSet() {
        try {
            super.setUp("openjpa.Specification", "jpa 3.0", 
                "openjpa.Log", "DefaultLevel=WARN");
            fail("Expected to fail with higher Spec version");
        } catch (IllegalArgumentException ex) {
            // prior behavior - spec api returned runtime exceptions as-is
            emf.getConfiguration().getLog("Tests").trace(
                "Caught expected IllegalArgumentException = " + ex);
        } catch (PersistenceException pe) {
            // new 2.0 behavior - Geronimo spec api wraps exceptions and tries all providers
            emf.getConfiguration().getLog("Tests").trace(
                "Caught expected PersistenceException = " + pe);
        }
    }
    
    public void testDifferentSpecCanBeSet() {
        super.setUp("openjpa.Specification", "jdo 3.0");
    }
    
    public void testSpecCanBeSetToNullString() {
        Specification spec = getSpecifcation();
        assertNotNull(spec);
        emf.getConfiguration().setSpecification((String)null);
        assertNull(getSpecifcation());
    }
    
    public void testSpecCanBeSetToNullSpecification() {
        Specification spec = getSpecifcation();
        assertNotNull(spec);
        emf.getConfiguration().setSpecification((Specification)null);
        assertNull(getSpecifcation());
    }
    
    public Specification getSpecifcation() {
        return emf.getConfiguration().getSpecificationInstance();
    }
}
