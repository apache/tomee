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
package org.apache.openjpa.persistence.detach;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestDetachCascade extends SingleEMFTestCase {
    OpenJPAEntityManager em;
    int id = 0;
    Compatibility compat;
    
    Entity1 e1;     // references e14 - no cascade
    Entity3 e3;     // references e4 - cascade ALL
    Entity4 e4;     // references e5 - cascade CLEAR
    Entity5 e5;     // references e6 - no cascade
    Entity6 e6;     // references e7 - cascade CLEAR
    Entity7 e7;
    Entity8 e8;     // references e9 - cascade ALL
    Entity8 e8a;    // same as e8
    Entity9 e9;
    Entity10 e10;   // references a Collection<Entity8> - cascade ALL
    Entity11 e11;
    Entity12 e12;   
    Entity13 e13;   // references a Map<String, Entity11> - cascade ALL
    Entity14 e14;
    
    Collection<Object> allEntities = new HashSet<Object>();
    
    public void setUp() throws Exception {
        setUp(Entity1.class,
            Entity3.class,
            Entity4.class,
            Entity5.class,
            Entity6.class,
            Entity7.class,
            Entity8.class,
            Entity9.class,
            Entity10.class,
            Entity11.class,
            Entity12.class,
            Entity13.class,
            Entity14.class);
        assertNotNull(emf);
        em = emf.createEntityManager();
        assertNotNull(em);
        compat = emf.getConfiguration().getCompatibilityInstance();
        assertNotNull(compat);
        compat.setFlushBeforeDetach(false);
        compat.setCopyOnDetach(false);
        compat.setCascadeWithDetach(true);
    }

    private void create(int id) {
        allEntities.clear();
        e1 = new Entity1(id, "entity1");
        allEntities.add(e1);
        e3 = new Entity3(id, "entity3");
        allEntities.add(e3);
        e4 = new Entity4(id, "entity4");
        allEntities.add(e4);
        e5 = new Entity5(id, "entity5");
        allEntities.add(e5);
        e6 = new Entity6(id, "entity6");
        allEntities.add(e6);
        e7 = new Entity7(id, "entity7");
        allEntities.add(e7);
        e8 = new Entity8(id, "entity8");
        allEntities.add(e8);
        e8a = new Entity8(id + 100, "entity8a");
        allEntities.add(e8a);
        e9 = new Entity9(id, "entity9");
        allEntities.add(e9);
        e10 = new Entity10(id, "entity10");
        allEntities.add(e10);
        e11 = new Entity11(id, "entity11");
        allEntities.add(e11);
        e12 = new Entity12(id, "entity12");
        allEntities.add(e12);
        e13 = new Entity13(id, "entity13");
        allEntities.add(e13);
        e14 = new Entity14(id, "entity14");
        allEntities.add(e14);
        e1.setE14(e14);
        e3.setE4(e4);
        e4.setE5(e5);
        e5.setE6(e6);
        e6.setE7(e7);
        e8.setE9(e9);
        
        Collection<Entity8> collection = new HashSet<Entity8>();
        collection.add(e8);
        collection.add(e8a); // e8a contains a null value for Entity9
        e10.setCollection(collection);
        
        Map<String, Entity11> map = new HashMap<String, Entity11>();
        map.put(e11.getName(), e11);
        e13.setMap(map);
        
    }
    
    // Test that detach cascade values are set correctly
    public void testCascadeValues() {
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        em.getTransaction().commit();
        
        MetaDataRepository repos = emf.getConfiguration()
            .getMetaDataRepositoryInstance();
        // Test CascadeType.ALL
        ClassMetaData meta3 = repos.getCachedMetaData(Entity3.class);
        assertNotNull(meta3);
        assertEquals(ValueMetaData.CASCADE_IMMEDIATE, 
            meta3.getField("e4").getCascadeDetach());
        // Test CascadeType.CLEAR
        ClassMetaData meta4 = repos.getCachedMetaData(Entity4.class);
        assertNotNull(meta4);
        assertEquals(ValueMetaData.CASCADE_IMMEDIATE, 
            meta4.getField("e5").getCascadeDetach());
    }
    
    // Make sure cascade is no longer done by default
    public void testNoCascade() {
        boolean cwd = compat.getCascadeWithDetach();
        compat.setCascadeWithDetach(false);
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        assertTrue(em.contains(e1));
        assertTrue(em.contains(e14));
        em.detach(e1);
        assertFalse(em.contains(e1));
        assertTrue(em.contains(e14));
        em.getTransaction().commit();
        compat.setCascadeWithDetach(cwd);
    }
    
    // Change to the previous behavior to always cascade
    public void testAlwaysCascade() {
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        assertTrue(em.contains(e1));
        assertTrue(em.contains(e14));
        
        compat.setCascadeWithDetach(true);
        
        em.detach(e1);
        assertFalse(em.contains(e1));
        assertFalse(em.contains(e14));
        em.getTransaction().commit();
        
        // reset compatibility option to default
        compat.setCascadeWithDetach(false);
    }
    
    // Test explicit cascade of entities
    public void testCascadeOfEntities() {
        boolean cwd = compat.getCascadeWithDetach();
        compat.setCascadeWithDetach(false);
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        assertTrue(em.contains(e3));
        assertTrue(em.contains(e4));
        em.detach(e3);
        assertTrue(em.contains(e1));
        assertFalse(em.contains(e3));
        assertFalse(em.contains(e4));
        assertFalse(em.contains(e5));
        assertTrue(em.contains(e6));
        assertTrue(em.contains(e7));
        em.getTransaction().commit();
        compat.setCascadeWithDetach(cwd);
    }
    
    // Test always cascade of entities
    public void testAlwaysCascadeOfEntities() {
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        assertTrue(em.contains(e3));
        assertTrue(em.contains(e4));
        compat.setCascadeWithDetach(true);
        em.detach(e3);
        assertTrue(em.contains(e1));
        assertFalse(em.contains(e3));
        assertFalse(em.contains(e4));
        assertFalse(em.contains(e5));
        assertFalse(em.contains(e6));
        assertFalse(em.contains(e7));
        em.getTransaction().commit();
        compat.setCascadeWithDetach(false);
    }
    
    // test single cascade in new transaction with no fetch of e4
    public void testSingleCascadeNoFetch() {
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        assertTrue(em.contains(e3));
        assertTrue(em.contains(e4));
        em.getTransaction().commit();
        
        em.close();
        em = emf.createEntityManager();
        
        em.getTransaction().begin();
        em.clear();
        e3 = em.find(Entity3.class, id);
        assertTrue(em.contains(e3));
        assertFalse(em.contains(e4));
        em.getTransaction().commit();
        em.detach(e3);
        assertFalse(em.contains(e3));
        assertFalse(em.contains(e4));
    }
    
    // test cascade of a simple collection
    public void testCascadeOfCollection() {
        id++;
        em.getTransaction().begin();
        create(id);
        em.persist(e10);
        assertTrue(em.contains(e10));
        assertTrue(em.contains(e8));
        assertTrue(em.contains(e9));
        assertTrue(em.contains(e8a));
        em.detach(e10);
        assertFalse(em.contains(e10));
        assertFalse(em.contains(e8));
        assertFalse(em.contains(e9));
        assertFalse(em.contains(e8a));
        em.getTransaction().commit();
    }
    
    // test cascade of Map
    public void testCascadeOfMap() {
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        assertTrue(em.contains(e11));
        assertTrue(em.contains(e13));
        em.detach(e13);
        assertFalse(em.contains(e13));
        assertFalse(em.contains(e11));
        em.getTransaction().commit();
    }
    
    // Test old detach behavior - flush, copy, and cascade
    public void testOldDetachBehavior() {
        id++;
        
        compat.setFlushBeforeDetach(true);
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        Entity1 e1Det = em.detachCopy(e1);
        assertNotEquals(e1, e1Det); // check for copy
        Entity14 e14Det = e1.getE14();
        assertEquals(e14, e14Det);
        em.getTransaction().commit();
        
        // check for flushed and cascaded flushed
        Entity1 e1Saved = em.find(Entity1.class, id);
        assertNotNull(e1Saved);
        Entity14 e14Saved = e1Saved.getE14();
        assertNotNull(e14Saved);
        
        compat.setFlushBeforeDetach(false);
    }
    
}
