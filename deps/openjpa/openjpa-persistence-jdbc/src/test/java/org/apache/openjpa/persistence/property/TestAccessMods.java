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

public class TestAccessMods 
    extends SingleEMFTestCase {

        public void setUp() {
            setUp("openjpa.Compatibility", "PrivatePersistentProperties=false",
                DROP_TABLES, AccessModsEntity.class);
        }
        
        /**
         * Verifies that when the PrivatePersistentProperties compat option 
         * is false, non-transient public and protected properties should be 
         * persistent.  Private should not be persistent.
         */
        public void testAccessMods() {
            EntityManager em = emf.createEntityManager();            
            
            AccessModsEntity ame = new AccessModsEntity();
            
            // Set all properties
            ame.setPubString("Public");
            ame.setProtString("Protected");
            ame.setPubPrivString("Private"); 
            // Persist the entity.  Public and protected properties should
            // get persisted.  Private should not.
            em.getTransaction().begin();
            em.persist(ame);
            em.getTransaction().commit();
            em.clear();
            
            ame = em.find(AccessModsEntity.class, ame.getId());
            assertNotNull(ame);
            assertEquals("Public", ame.getPubString());
            assertEquals("Protected", ame.getProtString());
            assertNull(ame.getPubPrivString());
            em.close();
        }
}
