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
package org.apache.openjpa.slice;

import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

/**
 * Tests that if any of the slices fail then none of the slices are committed.
 * 
 * The test setup assumes that configuration is created for each slice to store
 * cars from a particular Manufacturer. 
 * 
 * @author Pinaki Poddar
 *
 */
public class TestTransaction extends SliceTestCase {
    private static final Random rng = new Random(System.currentTimeMillis());
    Manufacturer[] manufacturers;
    
    protected String getPersistenceUnitName() {
        return System.getProperty("unit","car");
    }


    public void setUp() throws Exception {
        super.setUp(CLEAR_TABLES, 
                Car.class, Manufacturer.class, 
                "openjpa.slice.DistributionPolicy", CarDistributorPolicy.class.getName());
        DistributedConfiguration conf = (DistributedConfiguration)emf.getConfiguration();
        List<Slice> slices = conf.getSlices((Slice.Status[])null);
        assertFalse(slices.isEmpty());
        manufacturers = persistManufacturers(slices);
    }
    
    public void testCommitsAreAtomic() {
        int nCarStart = count(Car.class);
        persistCars(false);
        int nCarStage1 = count(Car.class);
        assertEquals(nCarStart+manufacturers.length, nCarStage1);
        
        for (int i = 0; i < 10; i++) {
            persistCars(true);
            int nCarStage2 = count(Car.class);
            assertEquals(nCarStage1, nCarStage2);
        }
    }
    
    Manufacturer getManufacturer(EntityManager em, Slice slice) {
        Manufacturer m = em.find(Manufacturer.class, slice.getName());
        if (m == null) {
            m = new Manufacturer();
            m.setName(slice.getName());
            em.persist(m);
        }
        return m;
    }
    
    /**
     * Creates the manufacturers each per given slice. 
     */
    Manufacturer[] persistManufacturers(List<Slice> slices) {
        Manufacturer[] manufacturers = new Manufacturer[slices.size()];
        int i = 0;
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (Slice slice : slices) {
            manufacturers[i++] = getManufacturer(em, slice);
        }
        em.getTransaction().commit();
        return manufacturers;
    }
    
    /**
     * Create new car for each manufacture. 
     * @param introduceError if true then one of the car will carry null VIN.
     */
    void persistCars(boolean introduceError) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        int i = introduceError ? rng.nextInt(manufacturers.length) : -1;
        for (int j = 0; j < manufacturers.length; j++) {
            Manufacturer maker = em.find(Manufacturer.class, manufacturers[j].getName());
            Car car = newCar(maker);
            em.persist(car);
            if (i == j)
                car.setVin(null); // this will make the commit fail
        }
        try {
            em.getTransaction().commit();
            if (introduceError) {
                fail("Expected " + RollbackException.class.getName());
            }
        } catch (RollbackException e) {
            if (!introduceError) {
                fail("Expected " + RollbackException.class.getName());
            } else {
                // this is expected
            }
        }
    }
    
    Car newCar(Manufacturer maker) {
        Car car = new Car();
        car.setVin(maker.getName().charAt(0) + randomString(6));
        car.setMaker(maker);
        return car;
    }
    
    String randomString(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            s.append(rng.nextInt(10));
        }
        return s.toString();
    }
    
    /**
     * A distribution policy that selects the slice based on Manufacturer.
     * @author Pinaki Poddar
     *
     */
    public static class CarDistributorPolicy implements DistributionPolicy {
        public String distribute(Object pc, List<String> slices, Object context) {
            if (pc instanceof Manufacturer) {
                return ((Manufacturer)pc).getName();
            } else if (pc instanceof Car) {
                if (((Car) pc).getMaker() == null) {
                    throw new RuntimeException("New " + pc + " must have a non-null Manufacturer");
                }
                return distribute(((Car)pc).getMaker(), slices, context);
            }
            throw new RuntimeException("No policy for " + pc.getClass().getName());
        }
    }
}
