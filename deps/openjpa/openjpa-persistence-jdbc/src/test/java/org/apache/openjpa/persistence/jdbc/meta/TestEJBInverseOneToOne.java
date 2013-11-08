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
import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.jdbc.common.apps.InvertA;
import org.apache.openjpa.persistence.jdbc.common.apps.InvertB;

public class TestEJBInverseOneToOne
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
	public TestEJBInverseOneToOne(String name)
	{
		super(name);
	}

	public void setUp ()
	{
		deleteAll(InvertA.class);
		deleteAll(InvertB.class);
	}


	private static Object insertData (EntityManager pm)
	{
		InvertA a;
		InvertB b;
		a = new InvertA (1);
		b = new InvertB (2);
		a.setTest ("testA");
		b.setTest ("testB");
		b.setInvertA (a);

		EntityTransaction t = pm.getTransaction ();
		t.begin ();
		pm.persist(b);
		//pm.makeTransactional (b);

		return (pm.find(InvertB.class, 2));
	}


	public void testLoad ()
	{
		InvertA a;
		InvertB b;
		Object aId;
		Object bId;

		EntityManager pm = currentEntityManager();
		aId = insertData (pm);
		endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		startTx(pm);
		b = (InvertB) aId;

		assertNotNull(b.getInvertA().getTest());
		assertEquals (b.getInvertA().getTest (), "testA");
	}

}
