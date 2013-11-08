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

import org.apache.openjpa.persistence.kernel.common.apps.OuterJoinValuePC;

public class TestEJBOuterJoinValues
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
	public int oid = 0;

	public TestEJBOuterJoinValues(String name)
	{
		super(name);
	}

	public void setUp()
	{
		deleteAll(OuterJoinValuePC.class);

		OuterJoinValuePC pc = new OuterJoinValuePC (10);
		pc.setStringField ("pc");
		pc.setValue1 (1);
		pc.setValue2 (2);

		EntityManager pm = currentEntityManager();
		startTx(pm);
		pm.persist(pc);
		endTx(pm);

		oid = pc.getId();
		endEm(pm);
	}

	public void testNull ()
	{
		EntityManager pm = currentEntityManager();
		startTx(pm);
		OuterJoinValuePC pc = pm.find(OuterJoinValuePC.class, oid);

		pc.setValue1 (0);
		pc.setValue2 (0);
		endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		pc = pm.find(OuterJoinValuePC.class, oid);
		assertEquals (0, pc.getValue1 ());
		assertEquals (0, pc.getValue2 ());
		endEm(pm);
	}

	public void testUpdate ()
	{
		EntityManager pm = currentEntityManager();
		startTx(pm);
		OuterJoinValuePC pc = pm.find(OuterJoinValuePC.class, oid);
		pc.setValue1 (3);
		pc.setValue2 (0);
		endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		pc = pm.find(OuterJoinValuePC.class, oid);
		assertEquals (3, pc.getValue1 ());
		assertEquals (0, pc.getValue2 ());
		endEm(pm);
	}

	public void testInsert ()
	{
		// just tests the values inserted in the setup method
		EntityManager pm = currentEntityManager();
		OuterJoinValuePC pc = pm.find(OuterJoinValuePC.class, oid);
		assertEquals ("pc", pc.getStringField ());
		assertEquals (1, pc.getValue1 ());
		assertEquals (2, pc.getValue2 ());
		endEm(pm);
	}

}
