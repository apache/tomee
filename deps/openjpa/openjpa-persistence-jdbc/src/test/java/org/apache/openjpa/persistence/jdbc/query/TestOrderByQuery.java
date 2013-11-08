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

import org.apache.openjpa.persistence.jdbc.query.domain.IndoorGame;
import org.apache.openjpa.persistence.jdbc.query.domain.Scrabble;
import org.apache.openjpa.persistence.jdbc.query.domain.Chess;
import org.apache.openjpa.persistence.jdbc.query.domain.Game;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests ORDER BY clause can work with TABLE_PER_CLASS strategy.
 *  
 * Further details can be found in
 * <A HREF="https://issues.apache.org/jira/browse/OPENJPA-485">OPENJPA-485</A>
 * 
 * @author Pinaki Poddar
 *
 */
public class TestOrderByQuery extends SingleEMFTestCase {   
    public void setUp() {
    	super.setUp(DROP_TABLES, Game.class, IndoorGame.class, 
    			Scrabble.class, Chess.class);
		try {
			createData();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
    
	void createData() throws Exception {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
        Class[] classes = { Game.class, IndoorGame.class, Scrabble.class,
				Chess.class };
		for (Class cls : classes) {
			for (int i = 1; i <= 4; i++) {
				Game p = (Game) cls.newInstance();
				p.setName(cls.getSimpleName() + "-" + i);
				em.persist(p);
			}
		}
		em.getTransaction().commit();
	}
    
    public void testOrderByAliasAscending() {
        String jpql = "SELECT p.name as name FROM Game p ORDER BY name ASC";
        EntityManager em = emf.createEntityManager();
        
        List<String> names = em.createQuery(jpql).getResultList();
        assertOrdering(names.toArray(new String[names.size()]), true);
    }
    
    public void testOrderByConcatAliasDescending() {
        String jpql = "SELECT CONCAT(p.name, '123') as cname " +
            "FROM Game p ORDER BY cname DESC";
        EntityManager em = emf.createEntityManager();
        
        List<String> names = em.createQuery(jpql).getResultList();
        assertOrdering(names.toArray(new String[names.size()]), false);
    }
    
    public void testOrderByAliasDescending() {
        String jpql = "SELECT p.name as name FROM Game p ORDER BY name DESC";
        EntityManager em = emf.createEntityManager();
        
        List<String> names = em.createQuery(jpql).getResultList();
        assertOrdering(names.toArray(new String[names.size()]), false);
    }
    
    public void testOrderByQueryAscending() {
    	String jpql = "SELECT p FROM Game p ORDER BY p.name ASC";
    	EntityManager em = emf.createEntityManager();
    	
    	List<Game> persons = em.createQuery(jpql).getResultList();
    	assertOrdering(getNames(persons), true);
    }
    
    public void testOrderByQueryDescending() {
    	String jpql = "SELECT p FROM Game p ORDER BY p.name DESC";
    	EntityManager em = emf.createEntityManager();
    	
    	List<Game> persons = em.createQuery(jpql).getResultList();
    	assertOrdering(getNames(persons), false);
    }
    
    public void testOrderByQueryProjectionAscending() {
    	String jpql = "SELECT p.name FROM Game p ORDER BY p.name ASC";
    	EntityManager em = emf.createEntityManager();
    	
    	List<String> names = em.createQuery(jpql).getResultList();
    	assertOrdering(names.toArray(new String[names.size()]), true);
    }
    
    public void testOrderByQueryProjectionDescending() {
    	String jpql = "SELECT p.name FROM Game p ORDER BY p.name DESC";
    	EntityManager em = emf.createEntityManager();
    	
    	List<String> names = em.createQuery(jpql).getResultList();
    	assertOrdering(names.toArray(new String[names.size()]), false);
    }
    

    
    public String[] getNames(List<Game> persons) {
    	assertNotNull(persons);
    	String[] names = new String[persons.size()];
    	int i = 0;
    	for (Game p : persons) {
    		names[i++] = p.getName();
    	}
    	return names;
    }

    public void assertOrdering(String[] names, boolean ascending) {
    	assertNotNull(names);
    	assertTrue(names.length>0);
    	for (int i=1; i<names.length; i++) {
    		if (ascending) {
    			assertTrue(names[i].compareTo(names[i-1]) >= 0);
    		} else {
    			assertTrue(names[i].compareTo(names[i-1]) <= 0);
    		}
    	}
    }
}
