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
package org.apache.openjpa.persistence.jdbc.mapping.bidi;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Tests basic persistence operations on bi-directional mapping that uses a 
 * JoinTable.
 * 
 * Originally reported as an error which shows that 
 * a) rows in join table get repeated insert as bi-directional mapping is
 *    essentially modeled as two uni-directional mapping
 * b) update/delete fails with OptimisticExecption because of repeated operation
 *    on the join table row 
 *    
 * Further details available at
 * <A HREF="https://issues.apache.org/jira/browse/OPENJPA-692">OPENJPA-692</A> 
 * and <A HREF="http://n2.nabble.com/bidirectional-one-to-many-relationship-with
 * -join-table-tc678479.html">Nabble posts</A>
 * 
 * @author Pinaki Poddar
 *
 */
public class TestBiDirectionalJoinTable extends SQLListenerTestCase {
	private static long SSN          = 123456789;
    private static String[] PHONES   = {"+1-23-456", "+2-34-567", "+3-45-678"};
	private static int ADDRESS_COUNT = PHONES.length;
	private static String[] CITIS    = {"Berlin", "Paris", "Rome"};
	private static int[] ZIPS        = {123456, 234567, 345678};
	
	public void setUp() {
		super.setUp(CLEAR_TABLES, Person.class, Address.class);
		createData(SSN);
		sql.clear();
	}
	
	public void testPersist() {
		EntityManager em = emf.createEntityManager();
		Person person = em.find(Person.class, SSN);
		assertNotNull(person);
		
		assertEquals(ADDRESS_COUNT, person.getAddresses().size());
	}
	
	public void testQuery() {
		EntityManager em = emf.createEntityManager();
        String jpql =
            "select distinct a.city from Person as p, in(p.addresses) a";
		Query query = em.createQuery(jpql);
		assertEquals(ADDRESS_COUNT, query.getResultList().size());
	}
	
	public void testUpdate() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Person person = em.find(Person.class, SSN);
		Address newAddress = new Address();
		newAddress.setPhone("+4-56-789");
		newAddress.setCity("San Francisco");
		person.addAddress(newAddress);
		person.setName("Frank");
		em.merge(person);
		em.getTransaction().commit();
		
		em = emf.createEntityManager();
		Person updated = em.find(Person.class, SSN);
		assertEquals("Frank", updated.getName());
		assertEquals(ADDRESS_COUNT+1, updated.getAddresses().size());
	}
	
	public void testRemove() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Person person = em.find(Person.class, SSN);
		em.remove(person);
		em.getTransaction().commit();
		
		assertEquals(0, count(Person.class));
		assertEquals(0, count(Address.class));
		assertSQL("DELETE FROM .*J_PERSON_ADDRESSES .*");
	}

	public void testSingleDelete() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		String jpql = "delete from Person p where p.ssn=:ssn";
		em.createQuery(jpql).setParameter("ssn", SSN).executeUpdate();
		em.getTransaction().commit();
		
		assertEquals(0, count(Person.class));
		assertEquals(0, count(Address.class));
		assertSQL("DELETE FROM .*J_PERSON_ADDRESSES .*");
	}
	
	public void testBulkDelete() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		String jpql = "delete from Person p";
		em.createQuery(jpql).executeUpdate();
		em.getTransaction().commit();
		
		assertEquals(0, count(Person.class));
		assertEquals(0, count(Address.class));
		assertSQL("DELETE FROM .*J_PERSON_ADDRESSES .*");
	}
	
	public void testBreakingRelationCausesDeleteFromJoinTable() {
		EntityManager em = emf.createEntityManager();
		Person person = em.find(Person.class, SSN);
		em.getTransaction().begin();
		Set<Address> addresses = person.getAddresses();
		assertFalse(addresses.isEmpty());
		Address address = addresses.iterator().next();
		addresses.remove(address);
		address.setPerson(null);
		em.getTransaction().commit();
		
		assertSQL("DELETE FROM .*J_PERSON_ADDRESSES .*");
	}

	/**
	 * Create a Person with given SSN and fixed number of addresses.
	 * 
	 * @param ssn
	 */
	void createData(long ssn) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		sql.clear();
		Person person = new Person();
		person.setSsn(SSN);
		person.setName("Pinaki");
		for (int i=0; i<PHONES.length; i++) {
			Address address = new Address();
			address.setPhone(PHONES[i]);
			address.setCity(CITIS[i]);
			address.setZip(ZIPS[i]);
			person.addAddress(address);
		}
		em.persist(person);
		em.getTransaction().commit();
		assertEquals(1+2*ADDRESS_COUNT, sql.size());
	}
	
}
