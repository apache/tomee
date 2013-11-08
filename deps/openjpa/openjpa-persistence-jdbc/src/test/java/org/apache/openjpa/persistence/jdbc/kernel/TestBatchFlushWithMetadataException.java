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
package org.apache.openjpa.persistence.jdbc.kernel;


import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.PersistenceException;
import org.apache.openjpa.persistence.RollbackException;
import org.apache.openjpa.persistence.jdbc.common.apps.EntityWithFailedExternalizer;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/*
 * when there is a metadata exception during the flushing for a batch job, the AbstractUpdateManager
 *  should capture the exception and skip the flushing of the failed object.
 */
public class TestBatchFlushWithMetadataException extends SQLListenerTestCase {   
    
    @Override
    public void setUp() throws Exception {
        setUp(DROP_TABLES, EntityWithFailedExternalizer.class);
    }
    
    public void testCreate(){
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        
        tx.begin();
        EntityWithFailedExternalizer item1 = new EntityWithFailedExternalizer(1001, "MyName1", "description1");
        EntityWithFailedExternalizer item2 = new EntityWithFailedExternalizer(1002, "MyName2", "description2");
        item1.getExt().throwEx=true;
        EntityWithFailedExternalizer item3 = new EntityWithFailedExternalizer(1003, "MyName3", "description3");  
        
        em.persist(item1);
        em.persist(item2);
        em.persist(item3);
        commitAndValidate(tx);
        em.close();
    }
    
    private void commitAndValidate(EntityTransaction tx){
        try {   
            resetSQL();
            tx.commit();
            fail("RollbackException should have been thrown from the externalizer");
        }catch (RollbackException rollBackException) {
            Throwable[] throwables = rollBackException.getNestedThrowables();
            assertTrue(throwables[0] instanceof PersistenceException);
            PersistenceException persistentException = (PersistenceException) throwables[0];
            assertNotNull(persistentException);
            assertEquals(1, persistentException.getNestedThrowables().length); 
        }
    }
}
