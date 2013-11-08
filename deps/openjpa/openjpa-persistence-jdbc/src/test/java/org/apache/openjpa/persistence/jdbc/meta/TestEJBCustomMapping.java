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

import org.apache.openjpa.persistence.jdbc.common.apps.CustomMappingPC;


public class TestEJBCustomMapping
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
	private int _oid;

	public TestEJBCustomMapping(String name)
	{
		super(name);
	}

	public void setUp ()
	{
		deleteAll(CustomMappingPC.class);

		CustomMappingPC pc = new CustomMappingPC (2);
		pc.setName ("name");
		pc.setFemale (true);

		EntityManager pm = currentEntityManager();
		startTx(pm);
		pm.persist(pc);
		endTx(pm);
		_oid = pc.getId();
		endEm(pm);
	}

	public void testInsert ()
	{
		EntityManager pm = currentEntityManager();
		CustomMappingPC pc = pm.find(CustomMappingPC.class, _oid);
		assertNotNull (pc);

		assertEquals ("name", pc.getName ());
		assertEquals (true, pc.isFemale ());
		endEm(pm);
	}

	public void testUpdate ()
	{
		EntityManager pm = currentEntityManager();
		startTx(pm);
		CustomMappingPC pc = pm.find(CustomMappingPC.class, _oid);

		pc.setName ("name2");
		pc.setFemale (false);
		endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		startTx(pm);
		pc = pm.find(CustomMappingPC.class, _oid);
		assertEquals ("name2", pc.getName ());
		assertTrue (!pc.isFemale ());
		endTx(pm);
		endEm(pm);
	}

}
