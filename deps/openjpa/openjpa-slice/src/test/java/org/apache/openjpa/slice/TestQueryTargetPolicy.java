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

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Tests query ordering.
 * 
 * @author Pinaki Poddar 
 *
 */
public class TestQueryTargetPolicy extends SliceTestCase {

    private int POBJECT_COUNT = 2;
    private int VALUE_MIN = 100;
    
    protected String getPersistenceUnitName() {
        return "ordering";
    }

    public void setUp() throws Exception {
        super.setUp(PObject.class, Person.class, Address.class, Country.class,
                Car.class, Manufacturer.class,
                "openjpa.slice.QueryTargetPolicy", 
                "org.apache.openjpa.slice.policy.SampleQueryTargetPolicy",
        		CLEAR_TABLES);
        int count = count(PObject.class);
        if (count == 0) {
            create(POBJECT_COUNT);
        }
    }
    
    
    
    void create(int N) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (int i=0;i < POBJECT_COUNT;i++) {
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
    
    public static final String QueryPersonByName = "select p from Person p where p.name=:name";
    public static final String QueryPersonByNameSwap = "select q from Person q where q.name=:name";
    
    public void testTargetSingleSlice() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query q1 = em.createQuery(QueryPersonByName);
        List<?> result1 = q1.setParameter("name", "Even").getResultList();
        String[] targets1 = getTargetSlices(q1);
        assertArrayEquals(new String[]{"Even"}, targets1);
        assertFalse(result1.isEmpty());

        Query q2 = em.createQuery(QueryPersonByNameSwap);
        List<?> result2 = q2.setParameter("name", "Even").getResultList();
        String[] targets2 = getTargetSlices(q2);
        assertArrayEquals(new String[]{"Odd"}, targets2);
        assertTrue(result2.isEmpty());
    }
    
    <T> void assertArrayEquals(T[] a, T[] b) {
        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++)
            assertEquals(a[i], b[i]);
    }
    
    
    /**
     * Gets the slice names on which the given query is targeted. 
     * @param q
     * @return
     */
    public static String[] getTargetSlices(Query q) {
        Object targets = q.unwrap(org.apache.openjpa.kernel.Query.class)
         .getFetchConfiguration()
         .getHint(SlicePersistence.HINT_TARGET);
        if (targets == null)
            return null;
        if (targets instanceof String) 
            return new String[]{targets.toString()};
        if (targets instanceof String[]) {
            return (String[])targets;
        }
        return null;
    }    
    
}
