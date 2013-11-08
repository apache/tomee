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
package org.apache.openjpa.persistence.inheritance;

import java.util.Random;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.inheritance.entity.BaseCallback;
import org.apache.openjpa.persistence.inheritance.entity.XMLCallback;
import org.apache.openjpa.persistence.inheritance.entity.XMLSuperCallback;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestCallbackEntity 
    extends SingleEMFTestCase {

    @Override
    protected String getPersistenceUnitName() {
        return "AbstractCallbackPU";
    }
    
    /*
     * This test verifies that the persistence metadata factory can create
     * and use separate copies of the XML metadata parser when a domain model
     * contains a base class with unknown access type and multi-level 
     * inheritance of XML defined types.  Per JPA spec, the superclass must
     * be queried to determine the access type.  
     */
    public void testCallbackEntity() {
        
        EntityManager em = emf.createEntityManager();
        BaseCallback bc = new BaseCallback();
        bc.setId(new Random().nextInt());
        bc.setName("BaseCallback");
        
        // Persist the entity
        em.getTransaction().begin();
        em.persist(bc);
        em.getTransaction().commit();   
        
        // Assert callbacks fire expected # of times
        assertEquals(1, BaseCallback.postPersistCount);
        assertEquals(1, XMLCallback.prePersistCount);
        assertEquals(1, XMLSuperCallback.postPersistCount);
        
        // Remove the entity
        em.getTransaction().begin();
        em.remove(bc);
        em.getTransaction().commit();
        
        em.close();
    }    
}
