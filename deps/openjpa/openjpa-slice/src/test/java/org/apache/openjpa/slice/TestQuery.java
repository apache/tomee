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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Tests query ordering.
 * 
 * @author Pinaki Poddar 
 *
 */
public class TestQuery extends SliceTestCase {

    private int POBJECT_COUNT = 25;
    private int VALUE_MIN = 100;
    private int VALUE_MAX = VALUE_MIN + POBJECT_COUNT - 1;
    
    protected String getPersistenceUnitName() {
        return "ordering";
    }

    public void setUp() throws Exception {
        super.setUp(PObject.class, Person.class, Address.class, Country.class,
                Car.class, Manufacturer.class,
        		CLEAR_TABLES);
        int count = count(PObject.class);
        if (count == 0) {
            create(POBJECT_COUNT);
        }
    }
    
    void create(int N) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (int i=0;i<POBJECT_COUNT;i++) {
            PObject pc = new PObject();
            pc.setValue(VALUE_MIN + i);
            em.persist(pc);
            String slice = SlicePersistence.getSlice(pc);
            String expected = (pc.getValue()%2 == 0) ? "Even" : "Odd";
            assertEquals(expected, slice);
        }
        Person p1 = new Person();
        Person p2 = new Person();
        Address a1 = new Address();
        Address a2 = new Address();
        p1.setName("Even");
        p2.setName("Odd");
        a1.setCity("San Francisco");
        a2.setCity("Rome");
        p1.setAddress(a1);
        p2.setAddress(a2);
        em.persist(p1);
        em.persist(p2);
        assertEquals("Even", SlicePersistence.getSlice(p1));
        assertEquals("Odd", SlicePersistence.getSlice(p2));
        
        em.getTransaction().commit();
    }
    
    public void testOrderedQueryResultWhenOrderableItemSelected() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery(
                "SELECT p.value,p FROM PObject p ORDER BY p.value ASC");
        List result = query.getResultList();
        assertValidResult(result);
        Integer old = Integer.MIN_VALUE;
        for (Object row : result) {
            Object[] line = (Object[])row;
            int value = ((Integer)line[0]).intValue();
            PObject pc = (PObject)line[1];
            assertTrue(value >= old);
            old = value;
            assertEquals(value, pc.getValue());
        }
        em.getTransaction().rollback();
    }
    
    public void testOrderedQueryResultWhenOrderableItemNotSelected() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query =
            em.createQuery("SELECT p FROM PObject p ORDER BY p.value ASC");
        List<PObject> result = query.getResultList();
        assertValidResult(result);
        Integer old = Integer.MIN_VALUE;
        for (PObject pc : result) {
            int value = pc.getValue();
            assertTrue(value >= old);
            old = value;
        }
        em.getTransaction().rollback();
    }
    
    public void testOrderedQueryResultWhenNavigatedOrderableItemNotSelected() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery(
     "SELECT p FROM Person p JOIN p.address a ORDER BY a.zip ASC, a.city DESC");
        List<Person> result = query.getResultList();
        assertValidResult(result);
        Integer oldZip = Integer.MIN_VALUE;
        String oldCity = null;
        for (Person pc : result) {
            int zip = pc.getAddress().getZip();
            String city = pc.getAddress().getCity();
            assertTrue(zip >= oldZip);
            assertTrue(oldCity == null || oldCity.compareTo(city) >= 0);
            oldZip = zip;
            oldCity = city;
        }
        em.getTransaction().rollback();
    }
    
    public void testAggregateQuery() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Object count = em.createQuery("SELECT COUNT(p) FROM PObject p")
                .getSingleResult();
        Object max = em.createQuery("SELECT MAX(p.value) FROM PObject p")
                .getSingleResult();
        Object min = em.createQuery("SELECT MIN(p.value) FROM PObject p")
                .getSingleResult();
        Object sum = em.createQuery("SELECT SUM(p.value) FROM PObject p")
                .getSingleResult();
        Object minmax = em.createQuery(
                "SELECT MIN(p.value),MAX(p.value) FROM PObject p")
                .getSingleResult();
        Object min1 = ((Object[])minmax)[0];
        Object max1 = ((Object[])minmax)[1];
        em.getTransaction().rollback();
        
        assertEquals(POBJECT_COUNT, ((Number)count).intValue());
        assertEquals(VALUE_MAX, ((Number)max).intValue());
        assertEquals(VALUE_MIN, ((Number)min).intValue());
        assertEquals((VALUE_MIN + VALUE_MAX) * POBJECT_COUNT,
                2 * ((Number)sum).intValue());
        assertEquals(min, min1);
        assertEquals(max, max1);
    }
    
    public void testAggregateQueryWithMissingValueFromSlice() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Object max = em.createQuery(
                "SELECT MAX(p.value) FROM PObject p WHERE MOD(p.value,2)=0")
                .getSingleResult();
        em.getTransaction().rollback();
        
        assertEquals(VALUE_MAX, ((Number)max).intValue());
    }

    public void testSetMaxResult() {
        EntityManager em = emf.createEntityManager();
        int limit = 3;
        em.getTransaction().begin();
        List<PObject> result =
            em.createQuery("SELECT p FROM PObject p ORDER BY p.value ASC")
                .setMaxResults(limit).getResultList();
        assertValidResult(result);
        Integer old = Integer.MIN_VALUE;
        for (PObject pc : result) {
            int value = pc.getValue();
            assertTrue(value >= old);
            old = value;
        }
        assertEquals(limit, result.size());
        em.getTransaction().rollback();
    }
    
    public void testHint() {
        List<String> targets = new ArrayList<String>();
        targets.add("Even");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery("SELECT p FROM PObject p");
        query.setHint(SlicePersistence.HINT_TARGET, "Even");
        List result = query.getResultList();
        for (Object pc : result) {
            String slice = SlicePersistence.getSlice(pc);
            assertTrue("Expected original slice " + slice + " in " + targets, targets.contains(slice));
        }
        em.getTransaction().rollback();
    }
    
    public void testQueryTargetPolicy() {
        List<String> targets = new ArrayList<String>();
        targets.add("Even");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery("SELECT p FROM PObject p");
        query.setHint(SlicePersistence.HINT_TARGET, "Even");
        List result = query.getResultList();
        for (Object pc : result) {
            String slice = SlicePersistence.getSlice(pc);
            assertTrue(targets.contains(slice));
        }
        em.getTransaction().rollback();
    }
    
    public void testInMemoryOrderBy() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query =
            em.createQuery("SELECT p FROM PObject p ORDER BY p.value");
        List result = query.getResultList();
        em.getTransaction().rollback();
    }
    
    public void testQueryParameter() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createQuery(
                "SELECT p FROM PObject p WHERE p.value > :v")
        	    .setParameter("v", 200);
        List result = query.getResultList();
        em.getTransaction().rollback();
    }
    
    public void testQueryParameterEntity() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Address a = (Address)em.createQuery(
                "select a from Address a where a.city = :city")
        	    .setParameter("city", "Rome").getSingleResult();
        assertNotNull(a);
        assertEquals("Odd", SlicePersistence.getSlice(a));
        Query query =
            em.createQuery("SELECT p FROM Person p WHERE p.address = :a")
            .setParameter("a", a);
        List<Person> result = query.getResultList();
        assertEquals(1, result.size());
        Person p = result.get(0);
        assertEquals("Odd", SlicePersistence.getSlice(p));
        assertEquals("Rome", p.getAddress().getCity());
        em.getTransaction().rollback();
    }
    
    /**
     * Verifies that a lazy relation can be stored across different slices i.e.
     * collocation constraint can be violated under some restrictions.
     * 
     * Car refers to Manufacturer. The relationship is uni-directional, no
     * cascade and most importantly lazy. 
     * The distribution policy is designed to store Car and Manufacturer 
     * *always* in different slices. 
     * 
     */
    public void testCollocationConstraintViolation() {
        // This transaction will store Manufacturer only
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Manufacturer bmw = new Manufacturer();
        bmw.setName("BMW");
        em.persist(bmw);
        em.getTransaction().commit();
        
        // This transaction will store a Car in a slice but the Car is related 
        // to a Manufacturer that *always* reside in a different slice. 
        em.getTransaction().begin();
        Car z4 = new Car(); 
        z4.setVin("1234V56789");
        z4.setMaker(bmw);
        z4.setModel("Z4");
        em.persist(z4);
        em.getTransaction().commit();
        em.clear();
        
        // Verify that all cars are stored in "Even" slice
        List cars = em.createQuery("select c from Car c").getResultList();
        assertFalse(cars.isEmpty());
        for (Object c : cars)
            assertEquals("Even", SlicePersistence.getSlice(c));
        
        // While all Manufacturers are stored in "Odd" slice.
        List makers =
            em.createQuery("select m from Manufacturer m").getResultList();
        assertFalse(makers.isEmpty());
        for (Object m : makers)
            assertEquals("Odd", SlicePersistence.getSlice(m));
        em.clear();
        
        // Now query for cars. The related manufacturer will be fetched
        // correctly, though it resides in a different slice because the
        // relationship is lazy and hence two separate SQLs are issued for
        // Car and Manufacturer rather than a single SQL with JOIN.
        cars = em.createQuery("select c from Car c").getResultList();
        assertFalse(cars.isEmpty());
        for (Object c : cars)
            assertNotNull(((Car)c).getMaker());
    }
    
    void assertValidResult(List result) {
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() > 1);
    }
}
