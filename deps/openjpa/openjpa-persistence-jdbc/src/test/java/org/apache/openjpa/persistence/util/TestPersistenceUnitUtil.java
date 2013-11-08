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
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestPersistenceUnitUtil extends SingleEMFTestCase{
    
    public void setUp() {
        setUp(CLEAR_TABLES, EagerEntity.class, LazyEmbed.class,
            LazyEntity.class, EagerEmbed.class, RelEntity.class,
            EagerEmbedRel.class, MapEntity.class,
            MapKeyEmbed.class, MapValEntity.class,
            OneToEntity.class, ToManyLazy.class, ToManyEager.class);
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
    
    /**
     * Verifies the use of PersistenceUnitUtil with multiple PU's.
     */
    public void testMultiplePUs() {
        OpenJPAEntityManagerFactorySPI emf1 =
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("PUtil1",
                "org/apache/openjpa/persistence/util/" +
                "persistence.xml");
        assertNotNull(emf1);

        OpenJPAEntityManagerFactorySPI emf2 =
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("PUtil2",
                "org/apache/openjpa/persistence/util/" +
                "persistence.xml");
        
        assertNotNull(emf2);
        assertNotSame(emf, emf1);
        assertNotSame(emf1, emf2);

        PersistenceUnitUtil puu = emf.getPersistenceUnitUtil();
        PersistenceUnitUtil puu1 = emf1.getPersistenceUnitUtil();
        PersistenceUnitUtil puu2 = emf2.getPersistenceUnitUtil();

        assertNotSame(puu, puu1);
        assertNotSame(puu, puu2);
        assertNotSame(puu1, puu2);

        EntityManager em = emf.createEntityManager();
        EntityManager em1 = emf1.createEntityManager();
        EntityManager em2 = emf2.createEntityManager();

        verifyPULoadState(em, puu, puu1, puu2);
        verifyPULoadState(em1, puu1, puu, puu2);
        verifyPULoadState(em2, puu2, puu, puu1);
        
        em.close();
        em1.close();
        em2.close();
        
        if (emf1 != null) {
            emf1.close();
        }
        if (emf2 != null) {
            emf2.close();
        }
        
    }

    private void verifyPULoadState(EntityManager em,
        PersistenceUnitUtil...puu) {

        EagerEntity ee = createEagerEntity();       
        assertEquals(false, puu[0].isLoaded(ee));
        assertEquals(false, puu[0].isLoaded(ee, 
            "id"));
        assertEquals(false, puu[1].isLoaded(ee));
        assertEquals(false, puu[1].isLoaded(ee, 
            "id"));
        assertEquals(false, puu[2].isLoaded(ee));
        assertEquals(false, puu[2].isLoaded(ee, 
            "id"));
        
        em.getTransaction().begin();
        em.persist(ee);
        em.getTransaction().commit();

        assertEquals(true, puu[0].isLoaded(ee));
        assertEquals(true, puu[0].isLoaded(ee, 
            "id"));
        assertEquals(false, puu[1].isLoaded(ee));
        assertEquals(false, puu[1].isLoaded(ee, 
            "id"));
        assertEquals(false, puu[2].isLoaded(ee));
        assertEquals(false, puu[2].isLoaded(ee, 
            "id"));
    }

    
    private void verifyIsLoadedEagerState(boolean loaded) {
        PersistenceUnitUtil puu = emf.getPersistenceUnitUtil();
        assertSame(emf, puu);
        EntityManager em = emf.createEntityManager();
        EagerEntity ee = createEagerEntity();
        
        // Vfy LoadState is false for the unmanaged entity
        assertEquals(false, puu.isLoaded(ee));
        assertEquals(false, puu.isLoaded(ee, 
            "id"));
        
        em.getTransaction().begin();
        em.persist(ee);
        em.getTransaction().commit();
        em.clear();
        
        if (loaded)
            ee = em.find(EagerEntity.class, ee.getId());
        else
            ee = em.getReference(EagerEntity.class, ee.getId());
        
        assertEquals(loaded, puu.isLoaded(ee));
        assertEquals(loaded, puu.isLoaded(ee, "id"));
        assertEquals(loaded, puu.isLoaded(ee, "name"));
        assertEquals(loaded, puu.isLoaded(ee, "eagerEmbed"));
        assertEquals(false, puu.isLoaded(ee, "transField"));
        
        em.close();
    }

    private void verifyIsLoadedLazyState(boolean loaded) {
        PersistenceUnitUtil puu = emf.getPersistenceUnitUtil();
        assertSame(emf, puu);
        EntityManager em = emf.createEntityManager();
        LazyEntity le = createLazyEntity();
        
        // Vfy LoadState is false for the unmanaged entity
        assertEquals(false, puu.isLoaded(le));
        assertEquals(false, puu.isLoaded(le,"id"));
        
        em.getTransaction().begin();
        em.persist(le);
        em.getTransaction().commit();
        em.clear();
        
        // Use find or getReference based upon expected state
        if (loaded)
            le = em.find(LazyEntity.class, le.getId());
        else
            le = em.getReference(LazyEntity.class, le.getId());
        
        assertEquals(loaded, puu.isLoaded(le));
        assertEquals(loaded, puu.isLoaded(le, "id"));

        // Name is lazy fetch so it should not be loaded
        assertEquals(false, puu.isLoaded(le, "name"));
        assertEquals(loaded, puu.isLoaded(le, "lazyEmbed"));
        assertEquals(false, puu.isLoaded(le, "transField"));
        
        em.close();
    }

    /*
     * Verifies that an entity and attributes are considered loaded if they
     * are assigned by the application.
     */
    public void testIsApplicationLoaded() {
        PersistenceUnitUtil puu = emf.getPersistenceUnitUtil();
        assertSame(emf, puu);
        EntityManager em = emf.createEntityManager();
        EagerEntity ee = createEagerEntity();
        
        em.getTransaction().begin();
        em.persist(ee);
        em.getTransaction().commit();
        em.clear();
        
        ee = em.getReference(EagerEntity.class, ee.getId());
        assertNotNull(ee);
        assertEagerLoadState(puu, ee, false);
        
        ee.setName("AppEagerName");
        EagerEmbed emb = createEagerEmbed();
        ee.setEagerEmbed(emb);
        // Assert fields are loaded via application loading
        assertEagerLoadState(puu, ee, true);
        // Vfy the set values are applied to the entity
        assertEquals("AppEagerName", ee.getName());
        assertEquals(emb, ee.getEagerEmbed());
        
        em.close();
    }

    public void testPCMapEager() {
        PersistenceUnitUtil puu = emf.getPersistenceUnitUtil();
        EntityManager em = emf.createEntityManager();
        
        MapValEntity mve = new MapValEntity();
        mve.setIntVal(10);
        MapKeyEmbed mke = new MapKeyEmbed();
        mke.setFirstName("Jane");
        mke.setLastName("Doe");
        
        MapEntity me = new MapEntity();

        assertEquals(false, puu.isLoaded(me));
        assertEquals(false, puu.isLoaded(me, 
            "mapValEntity"));
        assertEquals(false, puu.isLoaded(me, 
            "mapEntities"));

        assertEquals(false, puu.isLoaded(mve));

        // Create a circular ref
        me.setMapValEntity(mve);
        mve.setMapEntity(me);

        HashMap<MapKeyEmbed, MapValEntity> hm = 
            new HashMap<MapKeyEmbed, MapValEntity>();
        
        hm.put(mke, mve);
        me.setMapEntities(hm);

        em.getTransaction().begin();
        em.persist(me);
        em.getTransaction().commit();
        
        assertEquals(true, puu.isLoaded(me));
        assertEquals(true, puu.isLoaded(me, 
            "mapValEntity"));
        assertEquals(true, puu.isLoaded(me, 
            "mapEntities"));

        assertEquals(true, puu.isLoaded(mve));
        
        em.close();
    }

    /*
     * Verify load state is not loaded for null relationships or relationships
     * set to null.
     */
    public void testSetNullLazyRelationship() {

        PersistenceUnitUtil puu = emf.getPersistenceUnitUtil();
        EntityManager em = emf.createEntityManager();

        try {
            OneToEntity ote = new OneToEntity();
            assertFalse(puu.isLoaded(ote, "toManyLazy"));
            em.getTransaction().begin();
            em.persist(ote);
            em.getTransaction().commit();
            em.clear();
            ote = em.find(OneToEntity.class, ote.getId());
            // Field is lazy and not immediately loaded by the application
            assertFalse(puu.isLoaded(ote, "toManyLazy"));
            // Force load the lazy field
            ote.getToManyLazy();
            assertTrue(puu.isLoaded(ote, "toManyLazy"));
            
            OneToEntity ote2 = new OneToEntity();
            em.getTransaction().begin();
            em.persist(ote2);
            em.getTransaction().commit();
            // Field gets set to loaded upon commit
            assertTrue(puu.isLoaded(ote2, "toManyLazy"));
            em.clear();
            ote2 = em.find(OneToEntity.class, ote2.getId());
            
            // Field is lazy and not immediately loaded by the application
            assertFalse(puu.isLoaded(ote2, "toManyLazy"));
            
            // Load by application
            List<ToManyLazy> tmes = new ArrayList<ToManyLazy>();
            for (int i = 0; i < 5; i++) {
                tmes.add(new ToManyLazy("ToMany" + i));
            }
            em.getTransaction().begin();
            ote2.setToManyLazy(tmes);
            // App loaded before commit
            assertTrue(puu.isLoaded(ote2, "toManyLazy"));
            em.getTransaction().commit();
            // Still loaded after commit
            assertTrue(puu.isLoaded(ote2, "toManyLazy"));
            
            // Set to null - still loaded per spec.
            em.getTransaction().begin();
            ote2.setToManyLazy(null);
            // Considered loaded before commit
            assertTrue(puu.isLoaded(ote2, "toManyLazy"));
            em.getTransaction().commit();
            //Loaded after commit
            assertTrue(puu.isLoaded(ote2, "toManyLazy"));
        }
        finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
        em.close();
    }

    public void testSetNullEagerRelationship() {

        PersistenceUnitUtil puu = emf.getPersistenceUnitUtil();
        EntityManager em = emf.createEntityManager();

        try {
            OneToEntity ote = new OneToEntity();
            assertFalse(puu.isLoaded(ote, "toManyEager"));
            em.getTransaction().begin();
            em.persist(ote);
            em.getTransaction().commit();
            em.clear();
            ote = em.find(OneToEntity.class, ote.getId());
            // Field is eager and is immediately loaded by the application
            assertTrue(puu.isLoaded(ote, "toManyEager"));
            
            OneToEntity ote2 = new OneToEntity();
            em.getTransaction().begin();
            em.persist(ote2);
            // Field is null by default, but after persist, it is treated as loaded.
            assertTrue(puu.isLoaded(ote2, "toManyEager"));
            em.getTransaction().commit();
            // Field gets set to loaded upon commit
            assertTrue(puu.isLoaded(ote2, "toManyEager"));
            em.clear();
            ote2 = em.find(OneToEntity.class, ote2.getId());
            
            // Field is eager and is immediately loaded by the application
            assertTrue(puu.isLoaded(ote2, "toManyEager"));
            
            // Load by application
            List<ToManyEager> tmes = new ArrayList<ToManyEager>();
            for (int i = 0; i < 5; i++) {
                tmes.add(new ToManyEager("ToMany" + i));
            }
            em.getTransaction().begin();
            ote2.setToManyEager(tmes);
            // App loaded before commit
            assertTrue(puu.isLoaded(ote2, "toManyEager"));
            em.getTransaction().commit();
            // Still loaded after commit
            assertTrue(puu.isLoaded(ote2, "toManyEager"));
            
            // Set to null - still loaded per spec.
            em.getTransaction().begin();
            ote2.setToManyEager(null);
            // Entity is considered loaded before commit
            assertTrue(puu.isLoaded(ote2));
            // Attribute is considered loaded before commit
            assertTrue(puu.isLoaded(ote2, "toManyEager"));
            em.getTransaction().commit();
            //Loaded after commit
            assertTrue(puu.isLoaded(ote2, "toManyEager"));
        }
        finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
        em.close();
    }

    public void testBasicTypeNotLoaded() {
        PersistenceUnitUtil puu = emf.getPersistenceUnitUtil();
        EntityManager em = emf.createEntityManager();
        EagerEntity ee = createEagerEntity();
        int id = ee.getId();
        
        em.getTransaction().begin();
        em.persist(ee);
        em.getTransaction().commit();
        em.clear();
        // name is not eagerly loaded, only eagerEmbed is eagerly loaded
        OpenJPAEntityManager kem = OpenJPAPersistence.cast(em);
        kem.getFetchPlan().resetFetchGroups().removeFetchGroup("default")
            .addField(EagerEntity.class, "eagerEmbed");
        ee = em.find(EagerEntity.class, id);
        assertEquals(true, puu.isLoaded(ee));
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
        return le;
    }
    
    private void assertEagerLoadState(PersistenceUnitUtil pu, Object ent, 
        boolean state) {
        assertEquals(state, pu.isLoaded(ent));
        assertEquals(state, pu.isLoaded(ent, "id"));
        assertEquals(state, pu.isLoaded(ent, "name"));
        assertEquals(state, pu.isLoaded(ent, "eagerEmbed"));
        assertEquals(false, pu.isLoaded(ent, "transField"));
    }
}
