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
package org.apache.openjpa.persistence.jdbc.kernel;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.jdbc.common.apps.AutoIncrementPC1;
import org.apache.openjpa.persistence.jdbc.common.apps.AutoIncrementPC2;
import org.apache.openjpa.persistence.jdbc.common.apps.AutoIncrementPC3;

public class TestEJBAutoIncrement extends BaseJDBCTest
{

//	private boolean  = Boolean.valueOf(bool);

	private int oid2;
	private long oid3;
	private long oid4, oid2a, oid2b, oid3b;
	private long oid3a;

	public TestEJBAutoIncrement(String name)
	{
		super(name);
	}

	public void setUp()
	{
		deleteAll(AutoIncrementPC1.class);
		deleteAll(AutoIncrementPC2.class);
		deleteAll(AutoIncrementPC3.class);
	}

	@SuppressWarnings("unchecked")
	public void testInsert ()
	{
		AutoIncrementPC2 pc1 = new AutoIncrementPC2 (1);
		pc1.setStringField ("pc1");
		pc1.setIntField (1);
		pc1.getSetField ().add ("string1");
		pc1.getSetField ().add ("string2");

		AutoIncrementPC2 pc2 = new AutoIncrementPC2 (2);
		pc2.setStringField ("pc2");
		pc2.setIntField (2);
		pc2.getSetField ().add ("string3");
		pc2.getSetField ().add ("string4");

		AutoIncrementPC3 pc3 = new AutoIncrementPC3 (3);
		pc3.setStringField ("pc3");
		pc3.getSetField ().add ("string5");
		pc3.getSetField ().add ("string6");

		AutoIncrementPC3 pc4 = new AutoIncrementPC3 (4);
		pc4.setStringField ("pc4");
		pc4.getSetField ().add ("string7");
		pc4.getSetField ().add ("string8");

		EntityManager em = currentEntityManager();
		startTx(em);
		em.persist(pc1);
		em.persist(pc2);
		em.persist(pc3);
		em.persist(pc4);
		oid2 = pc2.getId();
		oid4 = pc4.getId();

		assertEquals (pc2, em.find(AutoIncrementPC2.class, oid2));
		assertEquals (pc4, em.find(AutoIncrementPC3.class, oid4));
		endTx(em);
		endEm(em);

		em = currentEntityManager();
		startTx(em);
		pc2 = em.find(AutoIncrementPC2.class, 2);
		pc4 = em.find(AutoIncrementPC3.class, 4);
		assertEquals ("pc2", pc2.getStringField ());
		assertEquals (2, pc2.getIntField ());
		assertEquals ("pc4", pc4.getStringField ());
		endTx(em);
		endEm(em);
	}

	public void testUpdate ()
	{
		AutoIncrementPC2 pc2 = new AutoIncrementPC2 (1);
		pc2.setStringField ("pc2");
		pc2.setIntField (2);
		AutoIncrementPC3 pc3 = new AutoIncrementPC3 (2);
		pc3.setStringField ("pc3");

		EntityManager em = currentEntityManager();
		startTx(em);
		em.persist (pc2);
		em.persist (pc3);
		endTx(em);

		oid2 = pc2.getId();
		oid3 = pc3.getId();
		endEm(em);

		em = currentEntityManager();
		startTx(em);
		pc2 = em.find(AutoIncrementPC2.class, oid2);
		pc3 = em.find(AutoIncrementPC3.class, oid3);
		assertEquals ("pc2", pc2.getStringField ());
		assertEquals (2, pc2.getIntField ());
		assertEquals ("pc3", pc3.getStringField ());
		pc2.setStringField ("pc2a");
		pc2.setIntField (3);
		pc3.setStringField ("pc3a");
		endTx(em);
		endEm(em);

		em = currentEntityManager();
		startTx(em);
		pc2 = em.find(AutoIncrementPC2.class, oid2);
		pc3 = em.find(AutoIncrementPC3.class, oid3);
		assertEquals ("pc2a", pc2.getStringField ());
		assertEquals (3, pc2.getIntField ());
		assertEquals ("pc3a", pc3.getStringField ());
		endTx(em);
		endEm(em);
	}

	public void testCircularReferences ()
	{
		AutoIncrementPC2 pc2a = new AutoIncrementPC2 (1);
		pc2a.setStringField ("pc2a");
		pc2a.setIntField (1);
		AutoIncrementPC2 pc2b = new AutoIncrementPC2 (2);
		pc2b.setStringField ("pc2b");
		pc2b.setIntField (2);
		AutoIncrementPC3 pc3 = new AutoIncrementPC3 (3);
		pc3.setStringField ("pc3");

		pc2a.setOneOne (pc2b);
		pc2b.setOneOne (pc2a);
		pc3.setOneOne (pc3);

		EntityManager em = currentEntityManager();
		startTx(em);
		em.persist (pc2a);
		em.persist (pc2b);
		em.persist (pc3);
		endTx(em);
		oid2a = pc2a.getId();
		oid2b = pc2b.getId();
	    oid3 = pc3.getId();
		endEm(em);

		em = currentEntityManager();
		startTx(em);
		pc2a = em.find(AutoIncrementPC2.class, oid2a);
		pc2b = em.find(AutoIncrementPC2.class, oid2b);
		pc3 = em.find(AutoIncrementPC3.class, oid3);
		assertEquals ("pc2a", pc2a.getStringField ());
		assertEquals (1, pc2a.getIntField ());
		assertEquals ("pc2b", pc2b.getStringField ());
		assertEquals (2, pc2b.getIntField ());
		assertEquals ("pc3", pc3.getStringField ());
		assertEquals (pc2b, pc2a.getOneOne ());
		assertEquals (pc2a, pc2b.getOneOne ());
		assertEquals (pc3, pc3.getOneOne ());
		endTx(em);
		endEm(em);
	}

	public void testMultipleFlushes ()
	{
		AutoIncrementPC2 pc2 = new AutoIncrementPC2 (1);
		pc2.setStringField ("pc2");
		pc2.setIntField (2);
		AutoIncrementPC3 pc3 = new AutoIncrementPC3 (2);
		pc3.setStringField ("pc3");

		EntityManager em = currentEntityManager();
		startTx(em);
		em.persist (pc2);
		em.persist (pc3);
		oid2 = pc2.getId();
		oid3 = pc3.getId();
		em.flush ();

	    oid2a = pc2.getId();
		oid3a = pc3.getId();
		assertEquals (oid2, oid2a);
		assertEquals (oid3, oid3a);
		long id = pc3.getId ();
		assertEquals (pc2, em.find(AutoIncrementPC2.class, oid2a));
		assertEquals (pc3, em.find(AutoIncrementPC3.class, oid3a));
		pc2.setStringField ("pc2a");
		pc2.setIntField (3);
		pc3.setStringField ("pc3a");
		em.flush ();

		oid2b = pc2.getId();
		oid3b = pc3.getId();
		assertEquals (oid2, oid2b);
		assertEquals (oid3, oid3b);
		assertEquals (id, pc3.getId ());
		assertEquals (pc2, em.find(AutoIncrementPC2.class, oid2b));
		assertEquals (pc3, em.find(AutoIncrementPC3.class, oid3b));
		endTx(em);
		endEm(em);

		em = currentEntityManager();
		startTx(em);
		pc2 = em.find(AutoIncrementPC2.class, oid2b);
		pc3 = em.find(AutoIncrementPC3.class, oid3b);
		assertEquals ("pc2a", pc2.getStringField ());
		assertEquals (3, pc2.getIntField ());
		assertEquals ("pc3a", pc3.getStringField ());
		assertEquals (id, pc3.getId ());
		endTx(em);
		endEm(em);
	}
}
