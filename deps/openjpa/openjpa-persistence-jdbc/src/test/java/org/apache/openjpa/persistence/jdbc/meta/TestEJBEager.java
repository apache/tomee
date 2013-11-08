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
package org.apache.openjpa.persistence.jdbc.meta;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.jdbc.common.apps.*;

public class TestEJBEager
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
	private int _oid;

	public TestEJBEager(String name)
	{
		super(name);
	}

	@SuppressWarnings("unchecked")
	public void setUp()
	{
		deleteAll(EagerPCSub.class);
		deleteAll(EagerPC.class);
		deleteAll(HelperPC.class);
		deleteAll(HelperPC2.class);
		deleteAll(HelperPC4.class);
		deleteAll(HelperPC3.class);

		EagerPCSub pc = new EagerPCSub (1);
		pc.setStringField ("string1");
		EagerPCSub pc2 = new EagerPCSub (2);
		pc2.setStringField ("string2");

		HelperPC helper1 = new HelperPC (4);
		helper1.setStringField ("helper1");
		pc.setHelper (helper1);

		HelperPC2 helperCollection1 = new HelperPC2 ();
		helperCollection1.setStringField ("helperCollection1");
		helperCollection1.getHelperCollection ().add (new HelperPC (5));
		HelperPC2 helperCollection2 = new HelperPC2 (6);
		helperCollection2.setStringField ("helperCollection2");
		pc.getHelperCollection ().add (helperCollection1);
		pc.getHelperCollection ().add (helperCollection2);

		HelperPC eager = new HelperPC (7);
		eager.setStringField ("eager");
		pc.setEager (eager);

		HelperPC4 eagerSub = new HelperPC4 ();
		eagerSub.setStringField ("eagerSub");
		eagerSub.setIntField (1);
		pc.setEagerSub (eagerSub);

		HelperPC eagerCollection1 = new HelperPC (8);
		eagerCollection1.setStringField ("eagerCollection1");
		HelperPC eagerCollection2 = new HelperPC (9);
		eagerCollection2.setStringField ("eagerCollection2");
		pc.getEagerCollection ().add (eagerCollection1);
		pc.getEagerCollection ().add (eagerCollection2);
		eagerCollection1.setEager (pc);
		eagerCollection2.setEager (pc);
		pc.getEagerCollection2 ().add (eagerCollection1);
		pc.getEagerCollection2 ().add (eagerCollection2);

		HelperPC eagerCollection3 = new HelperPC (10);
		eagerCollection3.setStringField ("eagerCollection3");
		pc2.getEagerCollection ().add (eagerCollection3);

		HelperPC2 recurse = new HelperPC2 (11);
		recurse.setStringField ("recurse");
		HelperPC3 helper3 = new HelperPC3 (12);
		helper3.setStringField ("helper3");
		recurse.setHelper (helper3);
		eager.setHelper (helper1);
		pc.setRecurse (recurse);

		HelperPC2 recurseCollection1 = new HelperPC2 (13);
		recurseCollection1.setStringField ("recurseCollection1");
		HelperPC2 recurseCollection2 = new HelperPC2 (14);
		recurseCollection2.setStringField ("recurseCollection2");
		pc.getRecurseCollection ().add (recurseCollection1);
		pc.getRecurseCollection ().add (recurseCollection2);
		recurseCollection1.getHelperCollection ().add (helper1);
		HelperPC helper2 = new HelperPC (15);
		helper2.setStringField ("helper2");
		recurseCollection1.getHelperCollection ().add (helper2);

		EntityManager pm = currentEntityManager();
		startTx(pm);
		pm.persist(pc);
		pm.persist(pc2);
		endTx(pm);
		_oid = pc.getId();
		endEm(pm);
	}

	public void testOuterJoin ()
	{
		EntityManager pm = currentEntityManager();
		startTx(pm);
		EagerPCSub pc = pm.find(EagerPCSub.class, _oid);
		pc.setEager (null);
		endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		pc = pm.find(EagerPCSub.class, _oid);
		assertNull (pc.getEager());
		assertNotNull (pc.getRecurse ());
		assertEquals ("helper3", pc.getRecurse ().getHelper ().
			getStringField ());
		endEm(pm);
	}

	public void testOuterJoinToSubclass ()
	{
		EntityManager pm = currentEntityManager();
		startTx(pm);
		EagerPCSub pc = pm.find(EagerPCSub.class, _oid);
		pc.setEagerSub (null);
		endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		startTx(pm);
		pc = pm.find(EagerPCSub.class, _oid);
		assertNull (pc.getEagerSub ());
		assertNotNull (pc.getRecurse ());
        assertEquals ("helper3", pc.getRecurse().getHelper().getStringField());
		endTx(pm);
		endEm(pm);
	}

}
