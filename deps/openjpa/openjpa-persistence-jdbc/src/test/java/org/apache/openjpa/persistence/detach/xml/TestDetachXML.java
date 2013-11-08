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
package org.apache.openjpa.persistence.detach.xml;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

/**
 * These test verify the use of cascade-detach via orm.xml through
 * the cascade-detach and cascade-all elements.  Within each variation, a 
 * bi-directional entity graph containing all relationship types is made 
 * persistent.  Detach is called iteratively on a full graph and cascade
 * behavior is verified.
 */
public class TestDetachXML extends AbstractPersistenceTestCase {

    /*
     * Test the use of cascade-detach with all relationship types.  All entities
     * in the graph should be detached when any entity is detached.
     */
    public void testDetach() throws Exception {
        OpenJPAEntityManagerFactorySPI emf = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("DetachXMLPU",
                "org/apache/openjpa/persistence/detach/" +
                "detach-persistence.xml");
        
        try {
            verifyCascadeDetach(emf, true);                        
        } finally {
            cleanupEMF(emf);
        }      
    }

    /*
     * Test the use of no cascade-detach with all relationship types.  The
     * base object should become detached and no other entities should be
     * detached.
     */
    public void testNoDetach()  throws Exception {
        OpenJPAEntityManagerFactorySPI emf = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("NoDetachXMLPU",
                "org/apache/openjpa/persistence/detach/" +
                "detach-persistence.xml");
        
        try {
            verifyCascadeDetach(emf, false);
        } finally {
            cleanupEMF(emf);
        }      
    }

    /*
     * Test the use of cascade-all with all relationship types.  All entities
     * in the graph should be detached when a single entity is detached.
     */
    public void testAllDetach()  throws Exception {
        OpenJPAEntityManagerFactorySPI emf = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("DetachAllXMLPU",
                "org/apache/openjpa/persistence/detach/" +
                "detach-persistence.xml");
        
        try {
            verifyCascadeDetach(emf, true);
        } finally {
            cleanupEMF(emf);
        }      
    }
    
    private void verifyCascadeDetach(EntityManagerFactory emf, boolean cascade) {
        EntityManager em = emf.createEntityManager();
        List<Object> data = createData(em);
        assertNotNull(data);
        int size = data.size();
        assertTrue(data.size() > 0);
        
        // Cycle through all PCs, verifying detach state
        for (int i = 0; i < size; i++) {
            Object entity = data.get(i);
            em.detach(entity);
            for (int j = 0; j < size; j++ ) {
                if (j == i) {
                    // Base entity should always be detached
                    assertFalse(em.contains(entity));
                } else {
                    Object detent = data.get(j);
                    assertEquals(!cascade, em.contains(detent));
                }
            }
            // Populate new data
            data = createData(em);
        }
        
    }
    
    private List<Object> createData(EntityManager em) {
        List<Object> data = new ArrayList<Object>();
        Automobile auto = new Automobile();
        auto.setMake("Ford");
        auto.setModel("Pinto");
        data.add(auto);
                
        Automobile auto2 = new Automobile();
        auto2.setMake("Winnebago");
        auto2.setModel("Sightseer");
        data.add(auto2);
        
        List<Automobile> autos = new ArrayList<Automobile>();
        autos.add(auto);
        autos.add(auto2);

        Passenger p1 = new Passenger();
        p1.setName("Crash test dummy 0");
        p1.setAutos(autos);
        data.add(p1);
        
        Passenger p2 = new Passenger();
        p2.setName("Crash test dummy 1");
        p2.setAutos(autos);
        data.add(p2);
        
        List<Passenger> passengers = new ArrayList<Passenger>();
        passengers.add(p1);
        passengers.add(p2);
        auto.setPassengers(passengers);
        auto2.setPassengers(passengers);
        
        Driver d1 = new Driver();
        d1.setAuto(auto);
        d1.setName("Crash test driver 0");
        auto.setPrimaryDriver(d1);
        data.add(d1);
        
        Driver d2 = new Driver();
        d2.setAuto(auto2);
        d2.setName("Crash test driver 1");
        auto2.setPrimaryDriver(d2);
        data.add(d2);
        
        Owner owner = new Owner();
        owner.setName("DMV");
        owner.setAutos(autos);
        auto.setOwner(owner);
        auto2.setOwner(owner);
        data.add(owner);

        // Persist the object graph
        em.getTransaction().begin();        
        em.persist(owner);
        em.getTransaction().commit();
        
        return data;
    }


    /**
     * Closes a specific entity manager factory and cleans up 
     * associated tables.
     */
    private void cleanupEMF(OpenJPAEntityManagerFactorySPI emf1) 
      throws Exception {

        if (emf1 == null)
            return;

        try {
            clear(emf1);
        } catch (Exception e) {
            // if a test failed, swallow any exceptions that happen
            // during tear-down, as these just mask the original problem.
            if (testResult.wasSuccessful())
                throw e;
        } finally {
            closeEMF(emf1);
        }
    }    

}
