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
package org.apache.openjpa.persistence.jdbc.maps.update;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * 
 * @author Harald Wellmann
 *
 */
public class TestMapUpdate extends SQLListenerTestCase {

    private MultilingualString entity1;
	private OpenJPAEntityManagerSPI em;

	public void setUp() {
        super.setUp(CLEAR_TABLES,
            MultilingualString.class,
            LocalizedString.class);
        createObj(emf);
        em = emf.createEntityManager();
    }

    public void testUpdateMapKey() throws Exception {
    	em.getTransaction().begin();
    	
    	MultilingualString ms = em.find(MultilingualString.class, entity1.getId());
    	assertNotNull(ms);
    
    	// Overwrite an existing map entry.
    	// The key is now dirty, and OpenJPA will generate an SQL UPDATE.
    	ms.setText("en", "Good evening");    	
    	em.getTransaction().commit();
    	em.clear();
    	
    	em.getTransaction().begin();
    	ms = em.find(MultilingualString.class, entity1.getId());
    	assertEquals("Good evening", ms.getText("en"));
    	em.getTransaction().commit();
    	em.close();
    }
    
    public void testUpdateMapValue() throws Exception {
    	em.getTransaction().begin();
    	
    	MultilingualString ms = em.find(MultilingualString.class, entity1.getId());
    	assertNotNull(ms);
    	
    	// Change an existing map value. This makes the map dirty,
    	// but OpenJPA does not recognize it. No SQL UPDATE is generated.
    	ms.getMap().get("en").setString("Good evening");
    	em.getTransaction().commit();
    	em.clear();
    	
    	em.getTransaction().begin();
    	ms = em.find(MultilingualString.class, entity1.getId());
    	
    	// This assertion fails, the entity still has the old value.
    	assertEquals("Good evening", ms.getText("en"));
    	em.getTransaction().commit();
    	em.close();
    }
    
    private void createObj(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        
        entity1 = new MultilingualString("de", "Guten Tag");
        entity1.setText("en", "Good morning");
        em.persist(entity1);
        
        em.flush();
        tran.commit();
        em.close();
    }
}

