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
package org.apache.openjpa.persistence.util;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/*
 * These variations indirectly test OpenJPA's ProviderUtil through the 
 * spec API implementation of PersistenceUtil. 
 */
public class TestPersistenceUtil extends SingleEMFTestCase{
    
    public void setUp() {
        setUp(CLEAR_TABLES, EagerEntity.class, LazyEmbed.class,
            LazyEntity.class, EagerEmbed.class, EagerEmbedRel.class,
            RelEntity.class);
    }

    /*
     * Verifies an entity and its persistent attributes are in the proper 
     * load state.
     */
    public void testIsLoadedEager() {        
        verifyIsLoadedEagerState(true);
    }

    /*
     * Verifies an entity and its persistent attributes are in the proper 
     * not loaded state.
     */
    public void testNotLoadedLazy() {
        verifyIsLoadedEagerState(false);       
    }

    /*
     * Verifies an entity and its persistent attributes are in the proper 
     * loaded state.
     */
    public void testIsLoadedLazy() {        
        verifyIsLoadedLazyState(true);
    }

    /*
     * Verifies an entity and its persistent attributes are in the proper 
     * NOT_LOADED state.
     */
    public void testNotLoadedEager() {
        verifyIsLoadedEagerState(false);       
    }

    /*
     * Verifies that an entity and attributes are considered loaded if they
     * are assigned by the application.
     */
    public void testIsApplicationLoaded() {
        PersistenceUtil putil = Persistence.getPersistenceUtil();
        EntityManager em = emf.createEntityManager();
        EagerEntity ee = createEagerEntity();
        
        em.getTransaction().begin();
        em.persist(ee);
        em.getTransaction().commit();
        em.clear();
        
        ee = em.getReference(EagerEntity.class, ee.getId());
        assertNotNull(ee);
        assertEagerLoadState(putil, ee, false);
        
        ee.setName("AppEagerName");
        EagerEmbed emb = createEagerEmbed();
        ee.setEagerEmbed(emb);
        // Assert fields are loaded via application loading
        assertEagerLoadState(putil, ee, true);
        // Vfy the set values are applied to the entity
        assertEquals("AppEagerName", ee.getName());
        assertEquals(emb, ee.getEagerEmbed());
        
        em.close();
    }
        
    /*
     * Verifies that an entity and attributes are considered loaded if they
     * are in the detached state.
     */
    public void testIsDetachLoaded() {
        PersistenceUtil putil = Persistence.getPersistenceUtil();
        EntityManager em = emf.createEntityManager();
        EagerEntity ee = createEagerEntity();
        
        em.getTransaction().begin();
        em.persist(ee);
        em.getTransaction().commit();
        em.clear();
        
        // should be true, as detached is treated as LoadState.UNKNOWN
        assertEquals(true, putil.isLoaded(ee));
        
        em.close();
    }

    private void verifyIsLoadedEagerState(boolean loaded) {
        PersistenceUtil putil = Persistence.getPersistenceUtil();
        EntityManager em = emf.createEntityManager();
        EagerEntity ee = createEagerEntity();
        
        // Vfy state is true for the unmanaged entity via
        // PeristenceUtil
        assertEquals(true, putil.isLoaded(ee));
        assertEquals(true, putil.isLoaded(ee, 
            "id"));
        
        em.getTransaction().begin();
        em.persist(ee);
        em.getTransaction().commit();
        em.clear();
        
        if (loaded)
            ee = em.find(EagerEntity.class, ee.getId());
        else
            ee = em.getReference(EagerEntity.class, ee.getId());
        
        assertEquals(loaded, putil.isLoaded(ee));
        assertEquals(loaded, putil.isLoaded(ee, "id"));
        assertEquals(loaded, putil.isLoaded(ee, "name"));
        assertEquals(loaded, putil.isLoaded(ee, "eagerEmbed"));
        assertEquals(true, putil.isLoaded(ee, "transField"));
        
        em.close();
    }

    private void verifyIsLoadedLazyState(boolean loaded) {
        PersistenceUtil putil = Persistence.getPersistenceUtil();
        EntityManager em = emf.createEntityManager();
        LazyEntity le = createLazyEntity();
        
        // Vfy LoadState is true for the unmanaged entity via
        // PersistenceUtil
        assertEquals(true, putil.isLoaded(le));
        assertEquals(true, putil.isLoaded(le,"id"));
        
        em.getTransaction().begin();
        em.persist(le);
        em.getTransaction().commit();
        em.clear();
        
        // Use find or getReference based upon expected state
        if (loaded)
            le = em.find(LazyEntity.class, le.getId());
        else
            le = em.getReference(LazyEntity.class, le.getId());
        
        assertEquals(loaded, putil.isLoaded(le));
        assertEquals(loaded, putil.isLoaded(le, "id"));

        // Name is lazy fetch so it should not be loaded
        assertEquals(false, putil.isLoaded(le, "name"));
        assertEquals(loaded, putil.isLoaded(le, "lazyEmbed"));
        assertEquals(true, putil.isLoaded(le, "transField"));
        
        em.close();
    }

    private EagerEntity createEagerEntity() {
        EagerEntity ee = new EagerEntity();
        ee.setId(new Random().nextInt());
        ee.setName("EagerEntity");
        EagerEmbed emb = createEagerEmbed();
        ee.setEagerEmbed(emb);
        return ee;
    }

    private EagerEmbed createEagerEmbed() {
        EagerEmbed emb = new EagerEmbed();
        emb.setEndDate(new Date(System.currentTimeMillis()));
        emb.setStartDate(new Date(System.currentTimeMillis()));
        return emb;
    }

    private LazyEntity createLazyEntity() {
        LazyEntity le = new LazyEntity();
        le.setId(new Random().nextInt());
        le.setName("LazyEntity");
        LazyEmbed emb = new LazyEmbed();
        emb.setEndDate(new Date(System.currentTimeMillis()));
        emb.setStartDate(new Date(System.currentTimeMillis()));
        le.setLazyEmbed(emb);
        RelEntity re = new RelEntity();
        re.setName("My ent");
        ArrayList<RelEntity> rel = new ArrayList<RelEntity>();
        rel.add(new RelEntity());
        le.setRelEntities(rel);
        return le;
    }
    
    private void assertEagerLoadState(PersistenceUtil putil, Object ent, 
        boolean state) {
        assertEquals(state, putil.isLoaded(ent));
        assertEquals(state, putil.isLoaded(ent, "id"));
        assertEquals(state, putil.isLoaded(ent, "name"));
        assertEquals(state, putil.isLoaded(ent, "eagerEmbed"));
        assertEquals(true, putil.isLoaded(ent, "transField"));
    }    
}
