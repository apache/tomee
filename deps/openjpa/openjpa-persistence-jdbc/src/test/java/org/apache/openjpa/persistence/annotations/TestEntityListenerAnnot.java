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
package org.apache.openjpa.persistence.annotations;

import java.util.List;

import org.apache.openjpa.persistence.test.AllowFailure;

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

@AllowFailure(message="excluded")
public class TestEntityListenerAnnot extends AnnotationTestCase
{

	public TestEntityListenerAnnot(String name)
	{
		super(name, "annotationcactusapp");
	}

	public void setUp()
	{
		deleteAll(Employee.class);
		deleteAll(ContractEmployee.class);
		CallbackStorage.clearStore();
	}

	public void testPrePersist()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();

		ContractEmployee cemp = new ContractEmployee(21, "afam", 25, 5);

		em.persist(cemp);
		CallbackStorage store = CallbackStorage.getInstance();

		assertNotNull(store.getClist());
        assertEquals("@pre/post persist callback is over/under-firing", 2,
                store.getClist().size());
		assertEquals("longnamevalidatorprr", store.getClist().get(0));
		assertEquals("contractemployee", store.getClist().get(1));

		endEm(em);
	}

	public void testPostPersist()
	{
		OpenJPAEntityManager em = null;	
	  try{	
		em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		Employee emp = new Employee(1, "john");

		em.persist(emp);
		CallbackStorage store = CallbackStorage.getInstance();

		assertNotNull(store.getClist());
        assertEquals("@pre/post persist callback is over/under-firing", 4,
                store.getClist().size());
		assertEquals("namevalidator", store.getClist().get(0));
		assertEquals("longnamevalidatorprr", store.getClist().get(1));
		assertEquals("employeepop", store.getClist().get(2));
        assertEquals("We expected 'gen#" +  emp.getCheck() + " : " 
                + emp.getCheck() + "'. However, we got '"
                + store.getClist().get(3) + "'", "gen#" + emp.getCheck(),
                store.getClist().get(3));
	  }
      finally {
		endTx(em);
		endEm(em);
      }
	}

	public void testPre_PostRemove()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		Employee emp = new Employee(8, "Jonathan");
		em.persist(emp);

		endTx(em);
		endEm(em);
		//--------------------------------------------------------------
		em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		emp = em.find(Employee.class, 8);

		CallbackStorage.clearStore(); //clear the store
		assertTrue(CallbackStorage.isEmpty());

		em.remove(emp);

		assertTrue(!CallbackStorage.isEmpty());
        assertEquals("callback is under/over-firing...", 2,
                CallbackStorage.size());
        assertEquals("namevalidatorprr",
                CallbackStorage.getInstance().getClist().get(0));
        assertEquals("namevalidatorpor",
                CallbackStorage.getInstance().getClist().get(1));

		endTx(em);
		endEm(em);
	}

	public void testPreUpdate()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		Employee emp = new Employee(5, "Abraham");
		em.persist(emp);

		CallbackStorage.clearStore();

        String query =
            "Update Employee e SET e.name = 'Joseph' WHERE e.id = :id";

		int result = em.createQuery(query)
		               .setParameter("id", 5)
		               .executeUpdate();

		List store = CallbackStorage.getInstance().getClist();

		assertNotNull(result);
		assertEquals(1, result);
		assertNotNull(store);
		assertEquals(3, store.size());
		assertEquals("namevalidatorpou", store.get(0));
		assertEquals("longnamevalidatorpou", store.get(1));
		assertEquals("employeepou", store.get(2));

		endTx(em);
		endEm(em);
	}

	public void testPreUpdate2()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		Employee emp = new Employee(5, "Abraham");
		em.persist(emp);

		CallbackStorage.clearStore();
		endTx(em);

		startTx(em);
		emp = em.find(Employee.class, 5);

		CallbackStorage.clearStore();
		assertEquals("Abraham", emp.getName());

		emp.setName("Abrahamovich");
		em.flush();

		List store = CallbackStorage.getInstance().getClist();

		assertNotNull(store);
        assertEquals("update callback is either underfiring or overfiring...",
                3, store.size());
		assertEquals("namevalidatorpou", store.get(0));
		assertEquals("longnamevalidatorpou", store.get(1));
		assertEquals("employeepou", store.get(2));

		endTx(em);
		endEm(em);
	}

	public void testPostLoad()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		Employee emp = new Employee(6, "Jefferson");

		em.persist(emp);
		CallbackStorage.clearStore();

		endTx(em);

		startTx(em);
		CallbackStorage.clearStore();

		assertTrue(CallbackStorage.getInstance().getClist().isEmpty());

		emp = em.find(Employee.class, 6);
		em.refresh(emp);

		assertNotNull(emp);
		assertNotNull(CallbackStorage.getInstance().getClist());
        assertEquals("PostLoad is overfiring...not accurate", 2,
                CallbackStorage.getInstance().getClist().size());
        assertEquals("employeepol",
                CallbackStorage.getInstance().getClist().get(0));
        assertEquals("employeepol",
                CallbackStorage.getInstance().getClist().get(1));

		endTx(em);
		endEm(em);
	}

	public void testGenPriKeyAvailInPostPersist()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		assertNotNull(em);

		Employee emp = new Employee(7, "Maxwell");

		assertEquals(0, emp.getCheck());

		em.persist(emp);
		int check = emp.getCheck();

		assertNotNull(check);
        assertTrue(CallbackStorage.getInstance().getClist().contains(
                "gen#" + check));

		endTx(em);
		endEm(em);
	}
	/*Fix Me: aokeke - should fail when persisting with invalid id*/
//	public void testExceptionCauseTxRollback2()
//	{
//      OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
//		startTx(em);
//		
//		Employee emp = new Employee(-1, "failure");
//		
//		try
//		{
//          //persisting an entity with an invalid id throws an exception
//			em.persist(emp);
//			endTx(em);
//          fail("Should have failed..persisting an entity with invalid id");
//		}
//		catch(RuntimeException e)
//		{			
//			assertFalse(em.isPersistent(emp));
//          assertTrue("transaction was not marked for rollback",
//                  em.getRollbackOnly());
//			e.printStackTrace();
//			if(em.getRollbackOnly() == true)
//				endEm(em);
//		}
//		catch(Exception e)
//		{
//			assertFalse(em.isPersistent(emp));
//          assertTrue("transaction was not marked for rollback",
//                  em.getRollbackOnly());
//			e.printStackTrace();
//			if(em.getRollbackOnly() == true)
//				endEm(em);
//		}
//		
//		if(em.isActive())
//			endEm(em);
//	}
}
