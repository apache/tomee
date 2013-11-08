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
package org.apache.openjpa.persistence.strategy.value;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestValueStrategy extends SQLListenerTestCase {
    public void setUp(){
      setUp(ValueStrategyEntity.class, DROP_TABLES);
      assertNotNull(emf);
      
      create();
}
    
    public void testIt() {
        EntityManager em = emf.createEntityManager();
        ValueStrategyEntity se = em.find(ValueStrategyEntity.class, "id1");
        assertNotNull(se);
        
        em.close();
    }

        private void create() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            
            ValueStrategyEntity stratEnt = new ValueStrategyEntity();
            stratEnt.setId("id1");
            stratEnt.setName("name1");
            
            em.persist(stratEnt);
            
            em.getTransaction().commit();
            em.close();
        }
}
