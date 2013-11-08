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
import org.apache.openjpa.persistence.annotations.common.apps.annotApp.ddtype.*;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

@AllowFailure(message="excluded")
public class TestDDCallbackMethods extends AnnotationTestCase
{

	public TestDDCallbackMethods(String name)
	{
		super(name, "ddcactusapp");
	}

	public void setUp()
	{
		deleteAll(LifeCycleDDEntity.class);
		deleteAll(LifeCycleDDEntity2.class);
		CallbackStorage.clearStore();
	}

	public void testDDPrpPop()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		LifeCycleDDEntity lcd = new LifeCycleDDEntity("afam", "okeke");

		em.persist(lcd);
		CallbackStorage store = CallbackStorage.getInstance();

		assertNotNull(store.getClist());
		assertEquals(2, store.getClist().size());
		assertEquals("def-prepersist", store.getClist().get(0));
		assertEquals("def-postpersist", store.getClist().get(1));

		endTx(em);
		endEm(em);
	}

	public void testDDPrrPor()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		LifeCycleDDEntity lcd = new LifeCycleDDEntity("john", "rash");

		em.persist(lcd);
		int id = lcd.getId();

		lcd = em.find(LifeCycleDDEntity.class, id);

		assertNotNull(lcd);
		CallbackStorage store = CallbackStorage.getInstance();
		store.clearStore();

		em.remove(lcd);

		assertNotNull(store.getClist());
		assertEquals(2, store.getClist().size());
		assertEquals("def-preremove", store.getClist().get(0));
		assertEquals("def-postremove", store.getClist().get(1) );

		endTx(em);
		endEm(em);
	}

	public void testDDPouPru()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		LifeCycleDDEntity lcd = new LifeCycleDDEntity("Don", "Shiddle");

		em.persist(lcd);
		int id = lcd.getId();

		CallbackStorage.clearStore();

        String query = "Update LifeCycleDDEntity e SET e.name = 'Joseph' "
            + "WHERE e.id = :id";

		int result = em.createQuery(query)
		               .setParameter("id", id)
		               .executeUpdate();

		List store = CallbackStorage.getInstance().getClist();

		assertNotNull(result);
		assertEquals(1, result);
		assertNotNull(store);
		assertEquals(2, store.size());
		assertEquals("def-preupdate", store.get(0));
		assertEquals("def-postupdate", store.get(1));

		endTx(em);
		endEm(em);
	}

	public void testDDPol()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		LifeCycleDDEntity lcd = new LifeCycleDDEntity("Julie", "Jolie");

		em.persist(lcd);
		int id = lcd.getId();

        CallbackStorage.clearStore();

		endTx(em);

		startTx(em);
		CallbackStorage.clearStore();

		assertTrue(CallbackStorage.getInstance().getClist().isEmpty());

		lcd = em.find(LifeCycleDDEntity.class, id);
		em.refresh(lcd);

		assertNotNull(lcd);
		assertNotNull(CallbackStorage.getInstance().getClist());
        assertEquals(2, CallbackStorage.getInstance().getClist().size());
        assertEquals("def-postload",
                CallbackStorage.getInstance().getClist().get(0));
        assertEquals("def-postload",
                CallbackStorage.getInstance().getClist().get(1));

		endTx(em);
		endEm(em);

	}

	/**  DEFAULT LISTENER DD TESTING **/

	public void testDefaultPrePostPersistListener()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

        LifeCycleDDEntity2 lc = new LifeCycleDDEntity2("Bill", "Clinton");

		CallbackStorage store = CallbackStorage.getInstance();
		store.clearStore();

		em.persist(lc);

		assertEquals(4, store.getClist().size());
		assertEquals("def-prepersist", store.getClist().get(0));
		assertEquals("verifyprp", store.getClist().get(1));
		assertEquals("def-postpersist", store.getClist().get(2));
		assertEquals("verifypop", store.getClist().get(3));

		endTx(em);
		endEm(em);
	}

    //FIX-ME Default-Entity-listener Impl. is over firing
	public void testDefaultPrePostUpdateListener()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

        LifeCycleDDEntity2 emp = new LifeCycleDDEntity2("lincoln", "Abraham");

		em.persist(emp);

		int id = emp.getId();

		CallbackStorage.clearStore();

        String query = "Update LifeCycleDDEntity2 e SET e.name = 'Joseph' "
                + "WHERE e.id = :id";

		int result = em.createQuery(query)
		               .setParameter("id", id)
		               .executeUpdate();

		List store = CallbackStorage.getInstance().getClist();

		assertNotNull(result);
		assertEquals(1, result);
		assertNotNull(store);
		assertEquals(2, store.size());
		assertEquals("def-preupdate", store.get(0));
		assertEquals("def-postupdate", store.get(1));
//		assertEquals("def-postupdate", store.get(2));
//		assertEquals("def-postupdate", store.get(3));

		endTx(em);
		endEm(em);
	}

	//FIX-ME Default-Entity-listener Impl. is over firing
	public void testDefaultPostLoadListener()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

        LifeCycleDDEntity2 emp = new LifeCycleDDEntity2("Thomas", "Jefferson");

		em.persist(emp);
		int id = emp.getId();
		CallbackStorage.clearStore();

		endTx(em);

		startTx(em);
		CallbackStorage.clearStore();

		assertTrue("not empty...", CallbackStorage.isEmpty());

		emp = em.find(LifeCycleDDEntity2.class, id);
		em.refresh(emp);

		assertNotNull(emp);
		assertNotNull(CallbackStorage.getInstance().getClist());
        assertEquals(2, CallbackStorage.getInstance().getClist().size());
        assertEquals("def-postload",
                CallbackStorage.getInstance().getClist().get(0));
        assertEquals("def-postload",
                CallbackStorage.getInstance().getClist().get(1));

		endTx(em);
		endEm(em);
	}

	public void testSubClassOverrideSuperCallbacksInh()
	{
       OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

        LifeCycleDDEntity2 emp = new LifeCycleDDEntity2("Thomas", "Jefferson");

		em.persist(emp);

		assertNotNull(emp);
		assertNotNull(CallbackStorage.getInstance().getClist());
        assertEquals(4, CallbackStorage.getInstance().getClist().size());
        assertEquals("def-prepersist",
                CallbackStorage.getInstance().getClist().get(0));
        assertEquals("verifyprp",
                CallbackStorage.getInstance().getClist().get(1));
        assertEquals("def-postpersist",
                CallbackStorage.getInstance().getClist().get(2));
        assertEquals("verifypop",
                CallbackStorage.getInstance().getClist().get(3));

		endTx(em);
		endEm(em);
	}
}
