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

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.jdbc.query.domain.Applicant;
import org.apache.openjpa.persistence.jdbc.query.domain.Application;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests that update query can set single-valued association field to non-null
 * or null values.
 * 
 * Originally reported in 
 * <A HRE="http://issues.apache.org/jira/browse/OPENJPA-533>OPENJPA-533</A>
 *  
 * @author Pinaki Poddar
 *
 */
public class TestUpdateSingleValuedAssociation extends SingleEMFTestCase {
	private static boolean MUST_BE_NULL = true;
	
	@Override
	public void setUp() throws Exception {
		super.setUp(CLEAR_TABLES, Application.class, Applicant.class);
	}
	
	public void testUpdateSingleValuedAssociationToNullViaParameter() {
		createApplicationWithNonNullApplicant();
		assertUserNullity(!MUST_BE_NULL);
		
		String jpql = "UPDATE Application a SET a.user = :user";
		updateByQuery(jpql, "user", null);
		
		assertUserNullity(MUST_BE_NULL);
	}
	
	public void testUpdateSingleValuedAssociationToNullViaLiteral() {
		createApplicationWithNonNullApplicant();
		assertUserNullity(!MUST_BE_NULL);
		
		String jpql = "UPDATE Application a SET a.user = NULL";
		updateByQuery(jpql);
		
		assertUserNullity(MUST_BE_NULL);
	}
	
	public void testUpdateSingleValuedAssociationToNonNullViaParameter() {
		Application pc = createApplicationWithNullApplicant();
		assertNull(pc.getUser());
		
		String jpql = "UPDATE Application a SET a.user = :user";
		Applicant newUser = createApplicant();
		updateByQuery(jpql, "user", newUser);
		
		assertUserNullity(!MUST_BE_NULL);
	}
	
	void assertUserNullity(boolean shouldBeNull) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
        List<Application> result = em.createQuery(
                "SELECT a FROM Application a").getResultList();
		assertFalse(result.isEmpty());
		for (Application pc : result) {
			Applicant user = pc.getUser();
			if (shouldBeNull)
				assertNull(user);
			else
				assertNotNull(user);
		}
		em.getTransaction().rollback();
	}
	
	Application createApplicationWithNonNullApplicant() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Application app = new Application();
		Applicant user = new Applicant();
		user.setName("Non-Null User");
		app.setUser(user);
		em.persist(app);
		em.persist(user);
		em.getTransaction().commit();
		return app;
	}
	
	Application createApplicationWithNullApplicant() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Application app = new Application();
		em.persist(app);
		em.getTransaction().commit();
		return app;
	}
	
	Applicant createApplicant() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Applicant user = new Applicant();
		user.setName("Non-Null User");
		em.persist(user);
		em.getTransaction().commit();
		return user;
	}
	
	public void updateByQuery(String jpql, Object...params) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Query query = em.createQuery(jpql);
		for (int i=0; params != null && i<params.length; i=+2) {
			query.setParameter(params[i].toString(), params[i+1]);
		}
		query.executeUpdate();
		em.getTransaction().commit();
		Cache cache = emf.getCache();
		if (cache != null) {
		    cache.evictAll();
		}
	}
}
