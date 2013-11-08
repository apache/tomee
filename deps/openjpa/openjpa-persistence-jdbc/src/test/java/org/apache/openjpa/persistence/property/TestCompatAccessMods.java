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
package org.apache.openjpa.persistence.property;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestCompatAccessMods extends SingleEMFTestCase {

    @Override
    public void setUp() {
        setUp("openjpa.Compatibility", "PrivatePersistentProperties=true", 
            DROP_TABLES, PrivAccessModsEntity.class);
    }

    /**
     * Verifies that with the PrivatePersistentProperties compatibility option
     * enabled, non-transient private properties are persistent.
     * 
     * Note: PrivAccessModsEntity must also be enhanced with the compatibility
     * option set to true.  This is currently handled in the test suite by
     * a separate enhancement task for PrivAccessModsEntity.
     */
    public void testAccessMods() {
        EntityManager em = emf.createEntityManager();            
        
        PrivAccessModsEntity pame = new PrivAccessModsEntity();
        
        // Set all properties
        pame.setPubString("Public");
        pame.setProtString("Protected");
        pame.setPubPrivString("Private"); 
        // Persist the entity.  All properties should be persisted.
        em.getTransaction().begin();
        em.persist(pame);
        em.getTransaction().commit();
        em.clear();
        
        pame = em.find(PrivAccessModsEntity.class, pame.getId());
        assertNotNull(pame);
        assertEquals("Public", pame.getPubString());
        assertEquals("Protected", pame.getProtString());
        assertEquals("Private", pame.getPubPrivString());
        em.close();
    }

}
