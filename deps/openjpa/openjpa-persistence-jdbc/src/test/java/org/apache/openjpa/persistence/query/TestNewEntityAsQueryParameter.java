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
package org.apache.openjpa.persistence.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.query.common.apps.Osoba;
import org.apache.openjpa.persistence.query.common.apps.Projekt;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests that managed, unmanaged or even transient Entity can be used as a 
 * query parameter.
 * 
 * Originally raised as a JIRA Issue
 * <A HREF="https://issues.apache.org/jira/browse/OPENJPA-187">OPENJPA-187</A>
 * 
 * @author Pinaki Poddar
 *
 */
public class TestNewEntityAsQueryParameter extends SingleEMFTestCase {
	
	public static final String ID_PROJEKT1 = "OpenJPA";
	public static final String ID_PROJEKT2 = "Tomcat";
	
	public static final int MEMBER_COUNT_PROJEKT1 = 6;
	public static final int MEMBER_COUNT_PROJEKT2 = 4;
	public static final int MEMBER_COUNT_TOTAL    = 8;
	
	public void setUp() throws Exception {
		super.setUp(CLEAR_TABLES, Osoba.class, Projekt.class);
		createData();
	}
	
	private void createData() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		Osoba[] osoby = new Osoba[MEMBER_COUNT_TOTAL];
		for (int i=0; i<MEMBER_COUNT_TOTAL; i++) {
			osoby[i] = new Osoba("Osoba-"+(i+1), 20+i);
			em.persist(osoby[i]);
		}
		
		
		Projekt projekt1 = new Projekt(ID_PROJEKT1);
		Projekt projekt2 = new Projekt(ID_PROJEKT2);
		em.persist(projekt1);
		em.persist(projekt2);
		
		for (int i=0; i<MEMBER_COUNT_PROJEKT1; i++)
			link(osoby[i], projekt1);
		for (int i=0; i<MEMBER_COUNT_PROJEKT2; i++)
			link(osoby[osoby.length-i-1], projekt2);
				
		em.getTransaction().commit();
		em.clear();
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testQueryWithTransientInstanceAsParameter() {
		Projekt projekt1 = new Projekt(ID_PROJEKT1);
		Projekt projekt2 = new Projekt(ID_PROJEKT2);
		
		EntityManager em = emf.createEntityManager();
		queryWithParameter(em, projekt1, projekt2);
	}
	
	public void testQueryWithUnmanagedPersistentInstanceAsParameter() {
		EntityManager em = emf.createEntityManager();
		Projekt projekt1 = em.find(Projekt.class, ID_PROJEKT1);
		Projekt projekt2 = em.find(Projekt.class, ID_PROJEKT2);
		em.clear();
		assertFalse(em.contains(projekt1));
		assertFalse(em.contains(projekt2));
		
		queryWithParameter(em, projekt1, projekt2);
	}

	public void testQueryWithManagedPersistentInstanceAsParameter() {
		EntityManager em = emf.createEntityManager();
		Projekt projekt1 = em.find(Projekt.class, ID_PROJEKT1);
		Projekt projekt2 = em.find(Projekt.class, ID_PROJEKT2);
		assertTrue(em.contains(projekt1));
		assertTrue(em.contains(projekt2));
		
		queryWithParameter(em, projekt1, projekt2);
	}
	
	@SuppressWarnings("unchecked")
    void queryWithParameter(EntityManager em, Projekt projekt1,
            Projekt projekt2) {
		String jpql =
           "SELECT DISTINCT o FROM Osoba o WHERE :projekt MEMBER OF o.projekty";
		em.getTransaction().begin();
		Query query = em.createQuery(jpql);
		
		query.setParameter("projekt", projekt1);
		List<Osoba> osoby = query.getResultList();
		assertEquals(MEMBER_COUNT_PROJEKT1, osoby.size()); 
		
		query.setParameter("projekt", projekt2);
		osoby = query.getResultList();
		assertEquals(MEMBER_COUNT_PROJEKT2, osoby.size());
		em.getTransaction().rollback();
	}
	
	void link(Osoba o, Projekt p) {
		o.addProjekty(p);
		p.addOsoba(o);
	}

}
