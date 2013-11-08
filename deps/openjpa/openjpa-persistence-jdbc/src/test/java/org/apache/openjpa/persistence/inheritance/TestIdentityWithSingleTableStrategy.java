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

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.inheritance.entity.Admin;
import org.apache.openjpa.persistence.inheritance.entity.ComputerUser;
import org.apache.openjpa.persistence.inheritance.entity.RegularUser;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests entities obtained via find(), getReference() or navigated to via
 * relation traversal refers to the same instances.
 * 
 * Original reported in the context of entities of a inheritance hierarchy with
 * SINGLE_TABLE strategy.
 * 
 * <A HREF="http://issues.apache.org/jira/browse/OPENJPA-677">OPENJPA-677</A>
 * 
 * @author Przemek Koprowski
 * @author Pinaki Poddar
 * 
 */
public class TestIdentityWithSingleTableStrategy extends SingleEMFTestCase {
	private Admin admin;
	private RegularUser user;

	public void setUp() {
		super.setUp(CLEAR_TABLES, Admin.class, RegularUser.class,
				ComputerUser.class);

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		admin = new Admin();
		user = new RegularUser();
		user.setAdmin(admin);
		admin.addRegularUser(user);
		em.persist(admin);
		em.persist(user);
		em.getTransaction().commit();
		em.close();
	}

	public void testFindAndNaviagtedEntityIdential() {
		EntityManager em1 = emf.createEntityManager();
		RegularUser regularUserFromFind = (RegularUser) em1.find(
				RegularUser.class, user.getOid());
		Admin adminFromFind = em1.find(Admin.class, admin.getOid());
		Admin adminFromMethodBean = regularUserFromFind.getAdmin();
		assertTrue(adminFromFind == adminFromMethodBean);
		em1.close();
	}

	public void testReferenceAndNaviagtedEntityIdential() {
		EntityManager em1 = emf.createEntityManager();
		RegularUser regularUserFromFind = (RegularUser) em1.find(
				RegularUser.class, user.getOid());
        Admin adminFromGetReference = em1.getReference(Admin.class, admin
				.getOid());
		Admin adminFromMethodBean = regularUserFromFind.getAdmin();
		assertTrue(adminFromGetReference == adminFromMethodBean);
		em1.close();
	}
}
