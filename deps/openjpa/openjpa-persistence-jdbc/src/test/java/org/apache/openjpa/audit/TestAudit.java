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
package org.apache.openjpa.audit;


import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;

/**
 * A test for audit facility.
 * 
 * @author Pinaki Poddar
 *
 */
public class TestAudit extends TestCase {
	private static OpenJPAEntityManagerFactory emf;
	private static Auditor auditor;
	private static Object oid;

	EntityManager em;
	
    public void setUp() {
    	if (emf == null) {
    		emf = OpenJPAPersistence.cast(Persistence.createEntityManagerFactory("audit"));
    		assertNotNull(emf);
    		auditor = emf.getConfiguration().getAuditorInstance();
            em = emf.createEntityManager();
    		clearAuditedEntries();
    		oid = createManagedObject();
    	} else {
    		em = emf.createEntityManager();
    	}
    }
    
    private Object createManagedObject() {
    	em.getTransaction().begin();
    	X x = new X();
    	x.setName("New Object");
    	x.setPrice(100);
    	em.persist(x);
    	em.getTransaction().commit();
    	
    	return emf.getPersistenceUnitUtil().getIdentifier(x);
    }
    
    private void clearAuditedEntries() {
    	em.getTransaction().begin();
    	em.createQuery("delete from AuditedEntry a").executeUpdate();
    	em.getTransaction().commit();
    }
    
    public void testAuditorIsConfigured() {
    	assertNotNull(auditor);
    }
    
    public void testIsEntityAuditable() {
    	assertNotNull(X.class.getAnnotation(Auditable.class));
    }
    
    public void testNewInstancesAreAudited() {
    	X x = em.find(X.class, oid);
    	assertNotNull(x);
    	
    	AuditedEntry entry = findLastAuditedEntry(AuditableOperation.CREATE);

    	assertNotNull(entry);
    	assertEquals(x, entry.getAudited());
    	assertEquals(AuditableOperation.CREATE, entry.getOperation());
    	assertEquals(X.class, entry.getAudited().getClass());
    	assertTrue(entry.getUpdatedFields().isEmpty());
    }
    
    public void testUpdateOutsideTransactionAreAudited() {
    	X x = em.find(X.class, oid);
    	assertNotNull(x);
    	
    	x.setName("Updated Object outside transaction");
    	
    	em.getTransaction().begin();
    	x = em.merge(x);
    	em.getTransaction().commit();
    	
    	AuditedEntry entry = findLastAuditedEntry(AuditableOperation.UPDATE);
   	
    	assertNotNull(entry);
    	assertEquals(x, entry.getAudited());
    	assertEquals(AuditableOperation.UPDATE, entry.getOperation());
    	assertTrue(entry.getUpdatedFields().contains("name"));
    	assertFalse(entry.getUpdatedFields().contains("price"));
    }
    
    public void testUpdateInsideTransactionAreAudited() {
    	X x = em.find(X.class, oid);
    	assertNotNull(x);
    	
    	
    	em.getTransaction().begin();
    	x.setPrice(x.getPrice()+100);
    	x = em.merge(x);
    	em.getTransaction().commit();
    	
    	AuditedEntry entry = findLastAuditedEntry(AuditableOperation.UPDATE);
    	
    	
    	assertNotNull(entry);
    	assertEquals(x, entry.getAudited());
    	assertEquals(AuditableOperation.UPDATE, entry.getOperation());
    	assertFalse(entry.getUpdatedFields().contains("name"));
    	assertTrue(entry.getUpdatedFields().contains("price"));
    }
    
    public void testAuditDoesNotLeakMemory() {
    	int N = 1000;
    	EntityManager em = emf.createEntityManager();
	   	long m2 = insert(N, em);
    	em = Persistence.createEntityManagerFactory("no-audit").createEntityManager();
    	assertNull(OpenJPAPersistence.cast(em).getEntityManagerFactory().getConfiguration().getAuditorInstance());
		long m0 = insert(N, em);
    	System.err.println("Memory used with no auditor " + m0);
    	System.err.println("Memory used with auditor " + m2);
		double pct = 100.0*(m2-m0)/m0;
		System.err.println("Extra memory with auditor " + pct);
    	assertTrue(pct < 10.0);
    }
    
    private long insert(int N, EntityManager em) {
    	assertTrue(ensureGarbageCollection());
    	long m1 = Runtime.getRuntime().freeMemory();
    	em.getTransaction().begin();
    	for (int i = 0; i < N; i++) {
    		X x = new X();
    		x.setName("X"+System.currentTimeMillis());
    		em.persist(x);
    	}
    	em.getTransaction().commit();
    	assertTrue(ensureGarbageCollection());
    	long m2 = Runtime.getRuntime().freeMemory();
    	long mused = m1-m2;
    	
    	return mused;
    }
    
    /**
     * Finds the latest audit entry of the given operation type.
     * The <em>latest</em> is determined by a sort on identifier which is assumed to be monotonically ascending.
     *  
     */
    AuditedEntry findLastAuditedEntry(AuditableOperation op) {
        List<AuditedEntry> entry =
            em.createQuery("select a from AuditedEntry a where a.operation=:op order by a.id desc", AuditedEntry.class)
                .setMaxResults(1).setParameter("op", op).getResultList();
        return entry.get(0);
    }
    
	public boolean ensureGarbageCollection() {
		ReferenceQueue<Object> detector = new ReferenceQueue<Object>();
		Object marker = new Object();
		WeakReference<Object> ref = new WeakReference<Object>(marker, detector);
		marker = null;
		System.gc();
		try {
			return detector.remove() == ref;
		} catch (InterruptedException e) {
			
		}
		return false;
	}



}
