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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.spi.LoadState;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.ProviderUtil;

import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestProviderUtil extends SingleEMFTestCase{
    
    public void setUp() {
        setUp(CLEAR_TABLES, EagerEntity.class, LazyEmbed.class,
            LazyEntity.class, EagerEmbed.class, EagerEmbedRel.class,
            RelEntity.class);
    }

    /*
     * Verifies an entity and its persistent attributes are in the proper 
     * LOADED state.
     */
    public void testIsLoadedEager() {        
        verifyIsLoadedEagerState(LoadState.LOADED);
    }

    /*
     * Verifies an entity and its persistent attributes are in the proper 
     * NOT_LOADED state.
     */
    public void testNotLoadedLazy() {
        verifyIsLoadedEagerState(LoadState.NOT_LOADED);       
    }

    /*
     * Verifies an entity and its persistent attributes are in the proper 
     * LOADED state.
     */
    public void testIsLoadedLazy() {        
        verifyIsLoadedLazyState(LoadState.LOADED);
    }

    /*
     * Verifies an entity and its persistent attributes are in the proper 
     * NOT_LOADED state.
     */
    public void testNotLoadedEager() {
        verifyIsLoadedEagerState(LoadState.NOT_LOADED);       
    }

    
    private void verifyIsLoadedEagerState(LoadState state) {
        ProviderUtil pu = getProviderUtil();
        EntityManager em = emf.createEntityManager();
        EagerEntity ee = createEagerEntity(true);
        
        // Vfy LoadState is unknown for the unmanaged entity
        assertEquals(LoadState.UNKNOWN, pu.isLoaded(ee));
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithReference(ee, 
            "id"));
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithoutReference(ee,
            "id"));
        
        em.getTransaction().begin();
        em.persist(ee);
        em.getTransaction().commit();
        em.clear();
        
        if (state == LoadState.LOADED)
            ee = em.find(EagerEntity.class, ee.getId());
        else
            ee = em.getReference(EagerEntity.class, ee.getId());
        
        assertEquals(state, pu.isLoaded(ee));
        assertEquals(state, pu.isLoadedWithReference(ee, 
            "id"));
        assertEquals(state, pu.isLoadedWithoutReference(ee,
            "id"));
        assertEquals(state, pu.isLoadedWithReference(ee, 
            "name"));
        assertEquals(state, pu.isLoadedWithoutReference(ee,
            "name"));
        assertEquals(state, pu.isLoadedWithReference(ee, 
            "eagerEmbed"));
        assertEquals(state, pu.isLoadedWithoutReference(ee,
            "eagerEmbed"));
        assertEquals(state, pu.isLoadedWithReference(ee, 
            "eagerEmbedColl"));
        assertEquals(state, pu.isLoadedWithoutReference(ee,
            "eagerEmbedColl"));
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithReference(ee, 
            "transField"));
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithoutReference(ee,
            "transField"));
        
        em.close();
    }

    private void verifyIsLoadedLazyState(LoadState state) {
        ProviderUtil pu = getProviderUtil();
        EntityManager em = emf.createEntityManager();
        LazyEntity le = createLazyEntity();
        
        // Vfy LoadState is unknown for the unmanaged entity
        assertEquals(LoadState.UNKNOWN, pu.isLoaded(le));
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithReference(le, 
            "id"));
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithoutReference(le,
            "id"));
        
        em.getTransaction().begin();
        em.persist(le);
        em.getTransaction().commit();
        em.clear();
        
        // Use find or getReference based upon expected state
        if (state == LoadState.LOADED)
            le = em.find(LazyEntity.class, le.getId());
        else
            le = em.getReference(LazyEntity.class, le.getId());
        
        assertEquals(state, pu.isLoaded(le));
        assertEquals(state, pu.isLoadedWithReference(le, 
            "id"));
        assertEquals(state, pu.isLoadedWithoutReference(le,
            "id"));
        // Name is lazy fetch so it should not be loaded
        assertEquals(LoadState.NOT_LOADED, pu.isLoadedWithReference(le, 
            "name"));
        assertEquals(LoadState.NOT_LOADED, pu.isLoadedWithoutReference(le,
            "name"));
        assertEquals(state, pu.isLoadedWithReference(le, 
            "lazyEmbed"));
        assertEquals(state, pu.isLoadedWithoutReference(le,
            "lazyEmbed"));
        // lazyEmbedColl is lazy fetch so it should not be loaded
        assertEquals(LoadState.NOT_LOADED, pu.isLoadedWithReference(le, 
            "lazyEmbedColl"));
        assertEquals(LoadState.NOT_LOADED, pu.isLoadedWithoutReference(le,
            "lazyEmbedColl"));        
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithReference(le, 
            "transField"));
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithoutReference(le,
            "transField"));
        
        em.close();
    }

    /*
     * Verifies that an entity and attributes are considered loaded if they
     * are assigned by the application.
     */
    public void testIsApplicationLoaded() {
        ProviderUtil pu = getProviderUtil();
        EntityManager em = emf.createEntityManager();
        EagerEntity ee = createEagerEntity(true);
        
        em.getTransaction().begin();
        em.persist(ee);
        em.getTransaction().commit();
        em.clear();
        
        ee = em.getReference(EagerEntity.class, ee.getId());
        assertNotNull(ee);
        assertEagerLoadState(pu, ee, LoadState.NOT_LOADED);
        
        ee.setName("AppEagerName");
        EagerEmbed emb = createEagerEmbed();
        ee.setEagerEmbed(emb);
        // Assert fields are loaded via application loading
        assertEagerLoadState(pu, ee, LoadState.LOADED);
        // Vfy the set values are applied to the entity
        assertEquals("AppEagerName", ee.getName());
        assertEquals(emb, ee.getEagerEmbed());
        
        em.close();
    }
        
    /*
     * Verifies that an entity not managed by a PU 
     */
    public void testIsLoadedUnknown() {
        ProviderUtil pu = getProviderUtil();
        
        EagerEntity ee = new EagerEntity();
        
        assertEquals(LoadState.UNKNOWN, pu.isLoaded(ee));
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithReference(ee, 
            "id"));
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithoutReference(ee,
            "id"));        
    }

    private EagerEntity createEagerEntity(boolean createRels) {
        EagerEntity ee = new EagerEntity();
        ee.setId(new Random().nextInt());
        ee.setName("EagerEntity");
        EagerEmbed emb = createEagerEmbed();
        List<EagerEmbed> embcoll = createEagerEmbedColl();
        ee.setEagerEmbed(emb);
        ee.setEagerEmbedColl(embcoll);
        if (createRels) {
            EagerEmbedRel eer = createEagerEmbedRel(createRels);
            ee.setEagerEmbedRel(eer);
        }
        ee.setEagerEmbedRel(null);
        return ee;
    }

    private List<EagerEmbed> createEagerEmbedColl() {
        ArrayList<EagerEmbed> al = new ArrayList<EagerEmbed>();
        for (int i = 0; i < 5; i++) {
            al.add(createEagerEmbed());
        }
        return al;
    }
    
    private EagerEmbed createEagerEmbed() {
        EagerEmbed emb = new EagerEmbed();
        emb.setEndDate(new Date(System.currentTimeMillis()));
        emb.setStartDate(new Date(System.currentTimeMillis()));
        return emb;
    }

    private EagerEmbedRel createEagerEmbedRel(boolean createRels) {
        EagerEmbedRel emb = new EagerEmbedRel();
        Set<EagerEntity> ee = new HashSet<EagerEntity>();
        if (createRels) {
            ee.add(createEagerEntity(false));
            ee.add(createEagerEntity(false));
            ee.add(createEagerEntity(false));
        }
        Set<Integer> ints = new HashSet<Integer>();
        for (int i = 0; i < 12; i++) {
            ints.add(new Integer(i));
        }
        emb.setIntVals(ints);
        
        emb.setEagerEnts(ee);
        return emb;
    }

    private LazyEntity createLazyEntity() {
        LazyEntity le = new LazyEntity();
        le.setId(new Random().nextInt());
        le.setName("LazyEntity");
        LazyEmbed emb = createLazyEmbed();
        le.setLazyEmbed(emb);
        le.setLazyEmbedColl(createLazyEmbedColl());
        return le;
    }

    private LazyEmbed createLazyEmbed() {
        LazyEmbed emb = new LazyEmbed();
        emb.setEndDate(new Date(System.currentTimeMillis()));
        emb.setStartDate(new Date(System.currentTimeMillis()));
        return emb;
    }

    private List<LazyEmbed> createLazyEmbedColl() {
        ArrayList<LazyEmbed> al = new ArrayList<LazyEmbed>();
        for (int i = 0; i < 5; i++) {
            al.add(createLazyEmbed());
        }
        return al;
    }

    private void assertEagerLoadState(ProviderUtil pu, Object ent, 
        LoadState state) {
        assertEquals(state, pu.isLoaded(ent));
        assertEquals(state, pu.isLoadedWithReference(ent, 
            "id"));
        assertEquals(state, pu.isLoadedWithoutReference(ent,
            "id"));
        assertEquals(state, pu.isLoadedWithReference(ent, 
            "name"));
        assertEquals(state, pu.isLoadedWithoutReference(ent,
            "name"));
        assertEquals(state, pu.isLoadedWithReference(ent, 
            "eagerEmbed"));
        assertEquals(state, pu.isLoadedWithoutReference(ent,
            "eagerEmbed"));
        assertEquals(state, pu.isLoadedWithReference(ent, 
            "eagerEmbedRel"));
        assertEquals(state, pu.isLoadedWithoutReference(ent,
            "eagerEmbedRel"));
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithReference(ent, 
            "transField"));
        assertEquals(LoadState.UNKNOWN, pu.isLoadedWithoutReference(ent,
            "transField"));        
    }
    
    private ProviderUtil getProviderUtil() {
        PersistenceProvider pp = new PersistenceProviderImpl(); 
        ProviderUtil pu = pp.getProviderUtil();
        return pu;
    }

}
