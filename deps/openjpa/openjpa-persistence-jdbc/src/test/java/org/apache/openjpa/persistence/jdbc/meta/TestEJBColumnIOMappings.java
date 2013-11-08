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

import org.apache.openjpa.persistence.kernel.common.apps.*;

public class TestEJBColumnIOMappings
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
	public TestEJBColumnIOMappings(String name)
	{
		super(name);
	}

	public void setUp()
	{
		deleteAll(ColumnIOPC.class);
	}

	public void testIgnoreInsert ()
	{
		ColumnIOPC pc = new ColumnIOPC (2);
		pc.setName ("pc");
		pc.setIgnoreInsert (10);

		EntityManager pm = currentEntityManager();
		startTx(pm);
		pm.persist(pc);
		assertEquals (10, pc.getIgnoreInsert ());
		endTx(pm);

		startTx(pm);
		assertEquals (10, pc.getIgnoreInsert ());
		int oid = pc.getId();
        endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		startTx(pm);
		pc = pm.find(ColumnIOPC.class, oid);
		assertNotNull (pc);
		assertEquals (10, pc.getIgnoreInsert ());
		pc.setIgnoreInsert (10);
		endTx(pm);
		startTx(pm);
		assertEquals (10, pc.getIgnoreInsert ());
		endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		startTx(pm);
		pc = pm.find(ColumnIOPC.class, oid);
		assertNotNull (pc);
		assertEquals (10, pc.getIgnoreInsert ());
		endTx(pm);
		endEm(pm);
	}

	public void testIgnoreUpdate ()
	{
		ColumnIOPC pc = new ColumnIOPC (3);
		pc.setName ("pc");
		pc.setIgnoreUpdate (10);

		EntityManager pm = currentEntityManager();
		startTx(pm);
		pm.persist(pc);
		assertEquals (10, pc.getIgnoreUpdate ());
		endTx(pm);

		Object oid = pc.getId();
		endEm(pm);

		pm = currentEntityManager();
		startTx(pm);
		pc = pm.find(ColumnIOPC.class, oid);
		assertNotNull (pc);
		assertEquals (10, pc.getIgnoreUpdate ());
		endTx(pm);

		startTx(pm);
		pc.setIgnoreUpdate (100);
		assertEquals (100, pc.getIgnoreUpdate ());
		endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		startTx(pm);
		pc = pm.find(ColumnIOPC.class, oid);
		assertNotNull (pc);
		assertEquals (10, pc.getIgnoreUpdate ());
		endTx(pm);
		endEm(pm);
	}


	public void testPrimitiveMappedToFieldOfForeignKey ()
	{
        // have to make rel persistent before setting ident field b/c not
		// insertable
		ColumnIOPC rel = new ColumnIOPC (1);
		rel.setName ("rel");
		EntityManager pm = currentEntityManager();
		startTx(pm);
		pm.persist(rel);
		endTx(pm);

		startTx(pm);
		ColumnIOPC pc = new ColumnIOPC (2);
		pc.setName ("pc");
		rel = pm.find(ColumnIOPC.class, 1);
		pc.setRel (rel);
		rel.setIdent (10);
		pm.persist(pc);
		endTx(pm);

		endEm(pm);

		pm = currentEntityManager();
		startTx(pm);
		pc = pm.find(ColumnIOPC.class, 2);
		assertNotNull (pc);
		assertEquals (0, pc.getIdent ());
		assertNotNull (pc.getRel ());
		assertEquals ("rel", pc.getRel ().getName ());

		pc.setIdent (50);
		pc.setRel (new ColumnIOPC (3));
		endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		startTx(pm);
		pc = pm.find(ColumnIOPC.class, 2);
		assertNotNull (pc);
		assertEquals (50, pc.getIdent());
		assertNotNull (pc.getRel());
		endTx(pm);
		endEm(pm);
	}




}
