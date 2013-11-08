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
package org.apache.openjpa.persistence.jdbc;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.openjpa.persistence.simple.EntityWithTimestampPK;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestTimestampPKDeletion extends SQLListenerTestCase {

    public void setUp() {
        setUp(EntityWithTimestampPK.class);
    }

    public void testTimestampPKDeletion() {
        EntityWithTimestampPK testEntity = new EntityWithTimestampPK("test");        

        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(testEntity);
        em.getTransaction().commit();
        em.close();
        
        em = emf.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        tx.begin();       
        final Query q = em.createQuery("SELECT testEntity FROM EntityWithTimestampPK testEntity ");
       
        final List<EntityWithTimestampPK> testEntities = q.getResultList();
        for (EntityWithTimestampPK t : testEntities) {
              em.remove(t);
        }         
        tx.commit();
        em.close();
    }
}
