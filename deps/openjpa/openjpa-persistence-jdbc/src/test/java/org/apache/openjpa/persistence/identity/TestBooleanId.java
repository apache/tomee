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
package org.apache.openjpa.persistence.identity;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Test that entities can use boolean ids
 *
 * @author Dianne Richards
 * @since 2.1.0
 */
public class TestBooleanId extends SQLListenerTestCase {
    private EntityManager em;
    private BooleanIdEntity se;
    private CompoundBooleanIdEntity ce;
    
    @Override
    public void setUp() throws Exception {
        super.setUp(BooleanIdEntity.class,
            CompoundBooleanIdEntity.class,
            DROP_TABLES);
        assertTrue(emf != null);
    }
    
    @Override
    public void tearDown() throws Exception {
        closeEM(em);
        super.tearDown();
    }
    
    public void testSimpleBooleanIdEntity() {
        se = new BooleanIdEntity(true,"name");
        
        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(se);
        em.getTransaction().commit();
        assertEquals(true, se.getId());
        em.close();
        
        em = emf.createEntityManager();
        se = em.find(BooleanIdEntity.class, true);
        assertNotNull(se);
    }
    
    public void testCompoundIdEntity() {
        em = emf.createEntityManager();
        testCompoundIdEntity("string1", true, true);
        testCompoundIdEntity("string1", true, false); // Expect duplicate key exception
        testCompoundIdEntity("string1", false, true);
        testCompoundIdEntity("string2", true, true);
        testCompoundIdEntity("string2", false, true);
        createCompoundIdEntityWithoutBoolean("string3");
        em.close();
    }
    
    private void testCompoundIdEntity(String stringId, boolean booleanId, boolean expectSuccess) {
        // create and persist the entity
        try {
            em.getTransaction().begin();
            ce = new CompoundBooleanIdEntity();
            ce.setStringId(stringId);
            ce.setBooleanId(booleanId);
            em.persist(ce);
            em.getTransaction().commit();
        } catch (Throwable e) {
            assertFalse(expectSuccess);
            em.getTransaction().rollback();
        }
        ce = null;
        em.clear();
        
        if (expectSuccess) {
            // get the entity
            em.getTransaction().begin();
            CompoundBooleanId cpdId = new CompoundBooleanId();
            cpdId.setBooleanId(booleanId);
            cpdId.setStringId(stringId);
            ce = em.find(CompoundBooleanIdEntity.class, cpdId);
            assertNotNull(ce);
            em.getTransaction().commit();
        }
    }
    
    private void createCompoundIdEntityWithoutBoolean(String stringId) {
        // create and persist the entity
        em.getTransaction().begin();
        ce = new CompoundBooleanIdEntity();
        ce.setStringId(stringId);
        em.persist(ce);
        em.getTransaction().commit();
        ce = null;
        em.clear();
        
        // get the entity
        em.getTransaction().begin();
        CompoundBooleanId cpdId = new CompoundBooleanId();
        cpdId.setStringId(stringId);
        ce = em.find(CompoundBooleanIdEntity.class, cpdId);
        assertNotNull(ce);
        em.getTransaction().commit();
    }
}
