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
import java.util.HashSet;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestDetachNoCascade extends SingleEMFTestCase {
    OpenJPAEntityManager em;
    int id = 0;
    Compatibility compat;
    
    Entity1 e1;
    Entity7 e7;
    Entity14 e14;
    Collection<Object> allEntities = new HashSet<Object>();
    
    public void setUp() throws Exception {
        setUp(Entity1.class,    // references Entity14
            Entity7.class,
            Entity14.class,
            CLEAR_TABLES);
        assertNotNull(emf);
        em = emf.createEntityManager();
        assertNotNull(em);
        compat = emf.getConfiguration().getCompatibilityInstance();
        compat.setFlushBeforeDetach(true);
        compat.setCopyOnDetach(false);
        compat.setCascadeWithDetach(false);

    }
    
    private void create(int id) {
        allEntities.clear();
        e1 = new Entity1(id,"entity1");
        allEntities.add(e1);
        e7 = new Entity7(id, "entity7");
        allEntities.add(e7);
        e14 = new Entity14(id, "entity14");
        allEntities.add(e14);
        e1.setE14(e14);
    }
    
    // Make sure the default values are the same
    public void testDefaults() {
        assertTrue(compat.getFlushBeforeDetach());
        assertFalse(compat.getCopyOnDetach());
        assertFalse(compat.getCascadeWithDetach());
    }
    
    // Make sure all the fields in Entity1 are CASCADE_NONE
    public void testCascadeValues() {
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        em.getTransaction().commit();
        
        MetaDataRepository repos = emf.getConfiguration()
            .getMetaDataRepositoryInstance();
        ClassMetaData meta = repos.getCachedMetaData(Entity1.class);
        assertNotNull(meta);
        assertEquals(ValueMetaData.CASCADE_NONE,
                meta.getField("id").getCascadeDetach());
        assertEquals(ValueMetaData.CASCADE_NONE,
                meta.getField("name").getCascadeDetach());
        assertEquals(ValueMetaData.CASCADE_NONE,
                meta.getField("e14").getCascadeDetach());
    }
    
    // Test clear() to clear all entities.
    public void testClearAll() {
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        assertEquals(allEntities.size(),em.getManagedObjects().size());
        em.clear();
        assertEquals(0,em.getManagedObjects().size());
        assertFalse(em.contains(e1));
        em.getTransaction().commit();
    }
    
    // Test clear(Object entity) to clear 1 entity. Do not
    // cascade and make sure contained entities and other entities
    // are still managed.
    public void testClearOne() {
        id++;
        boolean cwd = compat.getCascadeWithDetach();
        boolean cod = compat.getCopyOnDetach();
        compat.setCascadeWithDetach(false);
        compat.setCopyOnDetach(false);
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        assertTrue(em.contains(e1));
        em.detach(e1);
        assertFalse(em.contains(e1)); 
        assertTrue(em.contains(e14));
        assertTrue(em.contains(e7));
        em.getTransaction().commit();
        compat.setCascadeWithDetach(cwd);
        compat.setCopyOnDetach(cod);
    }
    
    // Test clear on a new, unmanaged object. Nothing should happen. After
    // persist it should still become a managed entity.
    public void testClearNew() {
        id++;
        em.getTransaction().begin();
        create(id);
        assertFalse(em.contains(e1));
        em.detach(e1);
        em.persistAll(allEntities);
        assertTrue(em.contains(e1));
        em.getTransaction().commit();
    }
    
    // Test clear on dirty object. Make sure the change is not flushed.
    public void testClearDirty() {

        boolean cod = compat.getCopyOnDetach();
        boolean fbd = compat.getFlushBeforeDetach();
        compat.setCopyOnDetach(false);
        compat.setFlushBeforeDetach(false);
        
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        e1 = em.find(Entity1.class, id);
        assertEquals("entity1",e1.getName());
        e1.setName("new name");
        em.detach(e1);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        em.clear();
        e1 = em.find(Entity1.class, id);
        assertNotEquals("new name", e1.getName());
        assertEquals("entity1", e1.getName());
        em.getTransaction().commit();
        
        compat.setCopyOnDetach(cod);
        compat.setFlushBeforeDetach(fbd);
    }
    
    // Remove an Entity before clearing it. Make sure it is still in the
    // DB after the commit.
    public void testClearRemove() {

        boolean cwd = compat.getCascadeWithDetach();
        boolean cod = compat.getCopyOnDetach();
        compat.setCascadeWithDetach(false);
        compat.setCopyOnDetach(false);
        
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        e1 = em.find(Entity1.class, id);
        em.remove(e1);
        em.detach(e1);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        em.clear();
        e1 = em.find(Entity1.class, id);
        assertNotNull(e1); 
        em.getTransaction().commit();
        
        compat.setCascadeWithDetach(cwd);
        compat.setCopyOnDetach(cod);
    }
    
    // Try to clear an entity that has already been cleared. There should be
    // no exception and the entity should still not be there.
    public void testClearOnClearedEntity() {
        boolean cwd = compat.getCascadeWithDetach();
        boolean cod = compat.getCopyOnDetach();
        compat.setCascadeWithDetach(false);
        compat.setCopyOnDetach(false);

        id++;
        em.getTransaction().begin();
        create(id);
        em.persist(e1);
        em.detach(e1);
        em.detach(e1);
        assertFalse(em.contains(e1));
        em.getTransaction().commit();

        compat.setCascadeWithDetach(cwd);
        compat.setCopyOnDetach(cod);
    }
    
    // Test that no copy is done by default
    public void testNoCopy() {
        boolean cwd = compat.getCascadeWithDetach();
        boolean cod = compat.getCopyOnDetach();
        compat.setCascadeWithDetach(false);
        compat.setCopyOnDetach(false);

        id++;
        em.getTransaction().begin();
        create(id);
        em.persist(e1);
        Entity14 e14PreDetach = e1.getE14();
        
        // Flip on cascade to detach e14
        compat.setCascadeWithDetach(true);
        
        em.detach(e1);
        // Assert that e14 was not copied on detachment
        assertEquals(e14, e14PreDetach);
        assertFalse(em.contains(e1));
        assertFalse(em.contains(e14));
        em.getTransaction().commit();

        compat.setCascadeWithDetach(false);

        compat.setCascadeWithDetach(cwd);
        compat.setCopyOnDetach(cod);
    }
    
    // Change copy option and validate
    public void testCopy() {
        id++;
        em.getTransaction().begin();
        create(id);
        em.persist(e1);
        
        Entity1 e1Det = em.detachCopy(e1);
        assertNotEquals(e1, e1Det);
        assertTrue(em.contains(e1));
        em.getTransaction().commit();
        
        compat.setCopyOnDetach(false);
    }
    
    // Change flush option and validate
    public void testFlush() {
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        em.getTransaction().commit();
        
        compat.setFlushBeforeDetach(true);
        
        em.getTransaction().begin();
        e1 = em.find(Entity1.class, id);
        assertEquals("entity1",e1.getName());
        e1.setName("new name");
        em.detach(e1);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        em.clear();
        e1 = em.find(Entity1.class, id);
        assertEquals("new name", e1.getName()); 
        em.getTransaction().commit();
        
        compat.setFlushBeforeDetach(false);
    }
    
    // Clear an entity, then recreate it in the same transaction.
    public void testClearRecreate() {
        id++;
        em.getTransaction().begin();
        create(id);
        em.persistAll(allEntities);
        em.detach(e1);
        em.merge(e1);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        em.clear();
        Entity1 newE1 = em.find(Entity1.class, id);
        assertNotNull(newE1);
        em.getTransaction().commit();
    }
}
