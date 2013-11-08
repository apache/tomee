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
package org.apache.openjpa.persistence.datacache.stats;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.datacache.CacheStatistics;
import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.EntityManagerImpl;
import org.apache.openjpa.persistence.jdbc.query.domain.Customer;
import org.apache.openjpa.persistence.jdbc.query.domain.Order;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestStatistics extends SingleEMFTestCase {
	private CacheStatistics stats;

	public void setUp() {
		super.setUp(CLEAR_TABLES, Customer.class, Order.class,
                "openjpa.DataCache", "true", "openjpa.RemoteCommitProvider",
				"sjvm");
		startCaching(Customer.class);
		startCaching(Order.class);
        assertTrue(((EntityManagerImpl) emf.createEntityManager()).getBroker()
				.getPopulateDataCache());
		stats = emf.getStoreCache().getStatistics();
		assertNotNull(stats);
	}

	void startCaching(Class<?> cls) {
		ClassMetaData meta = emf.getConfiguration()
                .getMetaDataRepositoryInstance().getMetaData(cls, null, true);
		meta.setDataCacheName(DataCache.NAME_DEFAULT);
	}

	/**
	 * Tests that statistics captures correct data under perfect caching
	 * condition.
	 */
	@SuppressWarnings("unchecked")
    public void testPerfectCache() {
		// populate a bunch of customer and order
		int nCustomer = 20;
		int nOrder    = 10;
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		for (int j = 0; j < nCustomer; j++) {
			Customer customer = new Customer();
			customer.setName("Customer-" + j);
			for (int k = 0; k < nOrder; k++) {
				Order order = new Order();
				order.setAmount(100);
				customer.addOrder(order);
				em.persist(order);
			}
			em.persist(customer);
		}
		em.getTransaction().commit();
		em.clear();
		
//		print(stats);
		
		em.getTransaction().begin();
		Query query = em.createQuery("select c from Customer c");
		for (int i = 0; i < 10; i++) {
			em.clear();
			stats.reset();
			List<Customer> result = query.getResultList();
			for (Customer c : result) {
				c.getOrders();
			}
//			print(stats);
			assertEquals(stats.getReadCount(), stats.getHitCount());
			assertEquals(0, stats.getWriteCount());
		}
        em.getTransaction().commit();
        em.close();
	}

	void assertStatistics(CacheStatistics stats, long[] expected) {
		assertEquals(expected[0], stats.getReadCount());
		assertEquals(expected[1], stats.getHitCount());
		assertEquals(expected[2], stats.getWriteCount());
	}
	
	void print(CacheStatistics stats) {
		// TODO log instead of printing to stderr.
	}

}
