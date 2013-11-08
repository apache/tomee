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
package org.apache.openjpa.slice;

import java.util.List;

import javax.persistence.EntityManager;


import org.apache.openjpa.slice.SlicePersistence;
import org.apache.openjpa.slice.policy.UniformDistributionPolicy;

/**
 * Tests delete-by-query.
 * 
 * @author Pinaki Poddar
 *
 */
public class TestBulkDelete extends SliceTestCase {
	private static int SLICES = 3;
	private static List<String> SLICE_NAMES;
	
	@Override
	protected String getPersistenceUnitName() {
		return "slice";
	}
	public void setUp() throws Exception {
		super.setUp(PObject.class, CLEAR_TABLES,
				"openjpa.slice.DistributionPolicy", UniformDistributionPolicy.class.getName());
		
	}

	public void tearDown() throws Exception {
		System.err.println("Delete all instances from all slices");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		String delete = "delete from PObject p";
		int m = em.createQuery(delete).executeUpdate();
		em.getTransaction().commit();
		super.tearDown();
	}
	
	/**
	 * Creates N instances that are distributed in 3 slices.
	 * Deletes all instances from only one slice.
	 */
	public void testBulkDelete() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		DistributedConfiguration conf = (DistributedConfiguration)emf.getConfiguration();
		SLICE_NAMES = conf.getActiveSliceNames();
		SLICES = SLICE_NAMES.size();
		assertTrue(SLICES > 1);
		int M = 4; // no of instances in each slice
		int N = SLICES*M; // total number of instances in all 3 slices
		
		for (int i = 0; i < N; i++) {
			PObject pc = new PObject();
			em.persist(pc);
		}
		em.getTransaction().commit();
		String jpql = "select count(p) from PObject p";
		long total = em.createQuery(jpql, Long.class).getSingleResult();
		assertEquals(N, total);
		
		for (int i = 0; i < SLICES; i++) {
			System.err.println("Query only on slice [" + SLICE_NAMES.get(i) + "]");
			long count = em.createQuery(jpql,Long.class)
			               .setHint(SlicePersistence.HINT_TARGET, SLICE_NAMES.get(i))
			               .getSingleResult();
			assertEquals(M, count);
		}
		
		em.getTransaction().begin();
		System.err.println("Delete only from slice [" + SLICE_NAMES.get(0) + "]");
		String delete = "delete from PObject p";
		int m = em.createQuery(delete)
		  .setHint(SlicePersistence.HINT_TARGET, SLICE_NAMES.get(0))
		  .executeUpdate();
		assertEquals(M, m);
		em.getTransaction().commit();
	}
}
