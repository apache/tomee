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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

/**
 * Tests basic create, read, update and delete operations.
 * 
 * @author Pinaki Poddar 
 *
 */
public class TestBasic extends SliceTestCase {
    /**
     * Specify persistence unit name as System property <code>-Dunit</code> or
     * use the default value as <code>"slice"</code>.
     */
    protected String getPersistenceUnitName() {
        return System.getProperty("unit","slice");
    }


    public void setUp() throws Exception {
        super.setUp(PObject.class, Person.class, Address.class, Country.class, 
        	CLEAR_TABLES);
    }

    /**
     * Persist N independent objects.
     */
    List<PObject> createIndependentObjects(int N) {
        List<PObject> pcs = new ArrayList<PObject>();
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (int i = 0; i < N; i++) {
            PObject pc = new PObject();
            pcs.add(pc);
            em.persist(pc);
            pc.setValue(10+i);
        }
        em.getTransaction().commit();
        em.clear();
        return pcs;
    }

    /**
     * Create a single object.
     */
    PObject createIndependentObject() {
        return createIndependentObjects(1).get(0);
    }

    /**
     * Delete a single object by EntityManager.remove()
     */
    public void testDelete() {
        int N = 10;
        createIndependentObjects(N);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        int before = count(PObject.class);
        List all = em.createQuery("SELECT p FROM PObject p").getResultList();
        assertFalse(all.isEmpty());
        em.remove(all.get(0));
        em.getTransaction().commit();

        int after = count(PObject.class);
        assertEquals(before - 1, after);
    }

    /**
     * Delete in bulk by query.
     */
    public void testBulkDelete() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        int c = count(PObject.class);
        int d = em.createQuery("DELETE FROM PObject p").executeUpdate();
        assertEquals(c, d);
        em.getTransaction().commit();
        c = count(PObject.class);
        assertEquals(0, c);

    }

    /**
     * Store and find the same object.
     */
    public void testFind() {
        PObject pc = createIndependentObject();
        int value = pc.getValue();

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        PObject pc2 = em.find(PObject.class, pc.getId());
        assertNotNull(pc2);
        assertNotEquals(pc, pc2);
        assertEquals(pc.getId(), pc2.getId());
        assertEquals(value, pc2.getValue());
    }
    
    /**
     * Store and find the same object via reference.
     */
    public void testReference() {
        PObject pc = createIndependentObject();
        int value = pc.getValue();

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        PObject ref = em.getReference(PObject.class, pc.getId());
        assertNotNull(ref);
        assertNotEquals(pc, ref);
        assertEquals(ref.getId(), pc.getId());
        pc.setValue(value+1);
        em.merge(pc);
        em.getTransaction().commit();
        em.clear();
        
        em.getTransaction().begin();
        PObject pc3 = em.find(PObject.class, pc.getId());
        assertEquals(value+1, pc3.getValue());
        em.getTransaction().commit();
        
    }


    public void testPersistIndependentObjects() {
        int before = count(PObject.class);
        EntityManager em = emf.createEntityManager();
        int N = 2;
        em.getTransaction().begin();
        for (int i = 0; i < N; i++)
            em.persist(new PObject());
        em.getTransaction().commit();
        em.clear();
        int after = count(PObject.class);
        assertEquals(before + N, after);
    }

    public void testPersistConnectedObjectGraph() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Person p1 = new Person("A");
        Person p2 = new Person("B");
        Person p3 = new Person("C");
        Address a1 = new Address("Rome", 12345);
        Address a2 = new Address("San Francisco", 23456);
        Address a3 = new Address("New York", 34567);
        Country c1 = em.find(Country.class, "Italy");
        if (c1 == null) {
        	c1 = new Country();
        	c1.setName("Italy");
        	em.persist(c1);
        }
    	a1.setCountry(c1);
        p1.setAddress(a1);
        p2.setAddress(a2);
        p3.setAddress(a3);

        em.persist(p1);
        em.persist(p2);
        em.persist(p3);
        em.getTransaction().commit();

        em.clear();

        em = emf.createEntityManager();
        em.getTransaction().begin();
        List<Person> persons =
                em.createQuery("SELECT p FROM Person p WHERE p.name=?1")
                        .setParameter(1, "A").getResultList();
        List<Address> addresses =
                em.createQuery("SELECT a FROM Address a").getResultList();
        for (Address pc : addresses) {
            assertNotNull(pc.getCity());
            assertNotNull(pc.getOwner().getName());
        }
        for (Person pc : persons) {
            assertNotNull(pc.getName());
            assertNotNull(pc.getAddress().getCity());
        }
        em.getTransaction().rollback();
    }

    /**
     * Merge only works if the distribution policy assigns the correct slice
     * from which the instance was fetched.
     */
    public void testMerge() {
        PObject pc = createIndependentObjects(1).get(0);
        int value = pc.getValue();
        pc.setValue(value + 1);
        assertNotNull(pc);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        PObject pc2 = em.merge(pc);
        em.getTransaction().commit();
        em.clear();

        assertNotNull(pc2);
        assertNotEquals(pc, pc2);
        assertEquals(pc.getId(), pc2.getId());
        assertEquals(value + 1, pc2.getValue());
    }
    
    public void testPersistReplicatedObjects() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        String[] names = {"USA", "India", "China"};
        for (String name : names) {
        	Country country = new Country();
        	country.setName(name);
        	em.persist(country);
        }
        em.getTransaction().commit();
        assertEquals(names.length, count(Country.class));
        
        em.getTransaction().begin();
        Country india = em.find(Country.class, "India");
        assertNotNull(india);
        assertEquals("India", india.getName());
        assertTrue(SlicePersistence.isReplicated(india));
        assertTrue(SlicePersistence.getSlice(india).indexOf("One") != -1);
        assertTrue(SlicePersistence.getSlice(india).indexOf("Two") != -1);
    }
    
    /**
     * Disable this test temporarily as we undergo changes in internal slice 
     * information structure.
     */
    public void testUpdateReplicatedObjects() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        String[] names = {"USA", "India", "China"};
        long[] population = {300,1200,1400};
        for (int i = 0; i < names.length; i++) {
        	Country country = new Country();
        	country.setName(names[i]);
        	country.setPopulation(population[i]);
        	em.persist(country);
        }
        em.getTransaction().commit();
        em.clear();
        
        assertEquals(names.length, count(Country.class));
        Country india = em.find(Country.class, "India");

        assertNotNull(india);
        assertEquals("India", india.getName());
        india.setPopulation(1201);
        em.getTransaction().begin();
        em.merge(india);
        em.getTransaction().commit();
        
        String[] hints = new String[] {"One", "Two"};
        String jpql = "select c from Country c where c.name=:name";
        em.getTransaction().begin();
        for (String hint : hints) {
            em.clear();
            Query query = em.createQuery(jpql).setParameter("name", "India");
            query.setHint(SlicePersistence.HINT_TARGET, hint);
            india = (Country)query.getSingleResult();
            assertEquals(india.getPopulation(), 1201);
            assertTrue(SlicePersistence.isReplicated(india));
            assertTrue(SlicePersistence.getSlice(india).indexOf("One") != -1);
            assertTrue(SlicePersistence.getSlice(india).indexOf("Two") != -1);
        }
        em.getTransaction().rollback();
    }
    
    public void testQuerySingleObject() {
    	PObject pc = createIndependentObject();
    	long pid = pc.getId();
        int value = pc.getValue();

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        String jpql = "select p from PObject p where p.id=:id";
        PObject pc2 = (PObject)em.createQuery(jpql).setParameter("id", pid)
        			.getSingleResult();
        assertNotNull(pc2);
        assertNotEquals(pc, pc2);
        assertEquals(pc.getId(), pc2.getId());
        assertEquals(value, pc2.getValue());
    }
    
    public void testGetConnection() {
        OpenJPAEntityManager em = emf.createEntityManager();
        Object con = em.getConnection();
        assertTrue(con instanceof Connection);
    }
    
    public void testDynamicSlice() {
        DistributedConfiguration conf = (DistributedConfiguration)emf.getConfiguration();
        conf.setDistributionPolicyInstance(new DistributionPolicy() {
            public String distribute(Object pc, List<String> slices,
                    Object context) {
                if (PObject.class.isInstance(pc)) {
                    PObject o = (PObject)pc;
                    if (o.getValue() > 50) {
                        DistributedBrokerFactory bf = (DistributedBrokerFactory)
                            ((DistributedBroker)context).getBrokerFactory();
                        Map newProps = new HashMap();
                        newProps.put("openjpa.slice.newslice.ConnectionURL",
                            "jdbc:derby:target/database/newslice;create=true");
                        newProps.put(
                            "openjpa.slice.newslice.ConnectionDriverName",
                            "org.apache.derby.jdbc.EmbeddedDriver");
                        bf.addSlice("newslice", newProps);
                        return "newslice";
                    } else {
                        return slices.get(o.getValue()%slices.size());
                    }
                }
                return null;
            }
        
        });
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        PObject pc1 = new PObject(); pc1.setValue(25);
        PObject pc2 = new PObject(); pc2.setValue(55);
        em.persist(pc1);
        em.persist(pc2);
        em.getTransaction().commit();
        Object newId = em.getObjectId(pc2);
        em.clear();
        
        PObject newP = em.find(PObject.class, newId);
        assertNotNull(newP);
        assertEquals("newslice", SlicePersistence.getSlice(newP));
    }

}
