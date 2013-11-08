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
package org.apache.openjpa.persistence.datacache;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.StoreCacheImpl;
import org.apache.openjpa.persistence.datacache.common.apps.EmbeddedEntity;
import org.apache.openjpa.persistence.datacache.common.apps.
        EmbeddingOwnerEntity;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test removing members of embedded collection with active DataCache.
 * 
 * Originally reported in 
 * <HREF="http://issues.apache.org/jira/browse/OPENJPA-625">OPENJPA-625</A>
 * 
 * @author Pinaki Poddar
 *
 */
public class TestEmbeddedCollection extends SingleEMFTestCase {

	private static final int SIZE = 4;
 
	/**
	 * Sets up EntityManagerFactory: with DataCache.
	 */
	@Override
	public void setUp() throws Exception {
        super.setUp("openjpa.jdbc.SynchronizeMappings", "buildSchema",
                "openjpa.RuntimeUnenhancedClasses", "unsupported",
                "openjpa.DataCache", "true", "openjpa.RemoteCommitProvider",
                "sjvm", "openjpa.jdbc.UpdateManager", "constraint",
				EmbeddingOwnerEntity.class, 
				EmbeddedEntity.class,
				CLEAR_TABLES);
		createData();
		assertNotNull(emf);
		assertNotNull(emf.getStoreCache());
		assertTrue(isDataCacheActive(emf));
	}

	boolean isDataCacheActive(OpenJPAEntityManagerFactorySPI emf) {
        return ((StoreCacheImpl) emf.getStoreCache()).getDelegate() != null
                && emf.getConfiguration().getDataCacheManagerInstance()
						.getSystemDataCache() != null;
	}
	
	public void createData() {
		EmbeddingOwnerEntity owner = new EmbeddingOwnerEntity();
		for (int i = 0; i < SIZE; i++) {
			EmbeddedEntity member = new EmbeddedEntity();
			member.setMarker("Member-" + i);
			owner.addMember(member);
		}
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(owner);
		em.getTransaction().commit();
		Object id = OpenJPAPersistence.cast(em).getObjectId(owner);
		em.clear();

        EmbeddingOwnerEntity test = em.find(EmbeddingOwnerEntity.class, id);
		assertNotNull(test);
		List<EmbeddedEntity> members = test.getMembers();
		assertNotNull(members);
		assertEquals(SIZE, members.size());
		for (int i = 0; i < SIZE; i++)
			members.get(i).getMarker().equals("Member-" + i);
	}

	public void testRemoveMemberFromEmbeddedCollection() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		List<EmbeddingOwnerEntity> result = em.createQuery(
                "SELECT p FROM EmbeddingOwnerEntity p").getResultList();

		assertNotNull(result);
		assertFalse(result.isEmpty());

		EmbeddingOwnerEntity owner = result.get(0);
		Object id = owner.getId();
		
        assertTrue(emf.getStoreCache().contains(EmbeddingOwnerEntity.class,
                id));
		
		List<EmbeddedEntity> members = owner.getMembers();
		members.remove(0);
		owner.removeMember(0);
		owner.removeMember(members.get(0));
		em.getTransaction().commit();
		
		assertEquals(owner.getMembers().size(), SIZE-3); 
	}

}
