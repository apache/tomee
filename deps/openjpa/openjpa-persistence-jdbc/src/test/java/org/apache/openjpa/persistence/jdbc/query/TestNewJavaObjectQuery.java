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
package org.apache.openjpa.persistence.jdbc.query;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.jdbc.query.domain.DtaSrc;
import org.apache.openjpa.persistence.jdbc.query.domain.DtaSrcField;
import org.apache.openjpa.persistence.jdbc.query.domain.Game;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests "openjpa.jdbc.DriverDataSource=dbcp" option.
 *  
 * Further details can be found in
 * <A HREF="https://issues.apache.org/jira/browse/OPENJPA-2153">OPENJPA-2153</A>
 * 
 */
public class TestNewJavaObjectQuery extends SingleEMFTestCase {   
    public void setUp() {
        super.setUp(CLEAR_TABLES, Game.class,
                DtaSrc.class, DtaSrcField.class, 
                "openjpa.jdbc.DriverDataSource", "dbcp");
    }

//	void createData() throws Exception {
//		EntityManager em = emf.createEntityManager();
//		em.getTransaction().begin();
//        Class[] classes = { Game.class, IndoorGame.class, Scrabble.class,
//				Chess.class };
//		for (Class cls : classes) {
//			for (int i = 1; i <= 4; i++) {
//				Game p = (Game) cls.newInstance();
//				p.setName(cls.getSimpleName() + "-" + i);
//				em.persist(p);
//			}
//		}
//		em.getTransaction().commit();
//	}

    public void testNewJavaObjectQueryResultList() {
        String jpql = "SELECT new org.apache.openjpa.persistence.jdbc.query.model.GameBean(g) FROM Game g";
        EntityManager em = emf.createEntityManager();
       
        List<?> names = em.createQuery(jpql).getResultList();
        assertNotNull(names);
        assertEquals(0, names.size());
    }

    public void testNewJavaObjectNamedQueryResultList() {
        EntityManager em = emf.createEntityManager();

        List<?> names = em.createNamedQuery("getDataSourceFieldById").getResultList();
        assertNotNull(names);
        assertEquals(0, names.size());
    }
}
