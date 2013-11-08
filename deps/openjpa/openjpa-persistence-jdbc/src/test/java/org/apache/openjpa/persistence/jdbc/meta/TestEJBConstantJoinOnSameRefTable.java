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

import java.util.Iterator;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.jdbc.common.apps.ConstantJoinPC4;
import org.apache.openjpa.persistence.jdbc.common.apps.ConstantJoinPC5;

public class TestEJBConstantJoinOnSameRefTable
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
	private int oid;
	
	public TestEJBConstantJoinOnSameRefTable(String name)
	{
		super(name);
	}

	@SuppressWarnings("unchecked")
	public void setUp ()
	{
		deleteAll(ConstantJoinPC4.class);
		deleteAll(ConstantJoinPC5.class);

		ConstantJoinPC4 pc4 = new ConstantJoinPC4 ("pc4-1", 1);
		pc4.setOneToOne1 (new ConstantJoinPC5 ("pc5-one-to-one-1"));
		//### see note in testConstantOneToOne2
        //### pc4.setOneToOne2 (new ConstantJoinPC5 ("pc5-one-to-one-2"));
        pc4.getManyToMany().add (new ConstantJoinPC5 ("pc5-many-to-many-1"));
        pc4.getManyToMany().add (new ConstantJoinPC5 ("pc5-many-to-many-2"));

		EntityManager pm = currentEntityManager();
		startTx(pm);
		pm.persist(pc4);
		endTx(pm);
		oid = pc4.getId();
		endEm(pm);
	}

	public void testConstantManyToMany ()
	{
		EntityManager pm = currentEntityManager();
		startTx(pm);
		ConstantJoinPC4 pc4 = pm.find(ConstantJoinPC4.class, oid);
		assertEquals (2, pc4.getManyToMany ().size ());
		Iterator iter = pc4.getManyToMany ().iterator ();
		ConstantJoinPC5 pc5_1 = (ConstantJoinPC5) iter.next ();
		ConstantJoinPC5 pc5_2 = (ConstantJoinPC5) iter.next ();
		if ("pc5-many-to-many-2".equals (pc5_1.getName ()))
		{
			ConstantJoinPC5 other = pc5_1;
			pc5_1 = pc5_2;
			pc5_2 = other;
		}
		assertEquals ("pc5-many-to-many-1", pc5_1.getName ());
		assertEquals ("pc5-many-to-many-2", pc5_2.getName ());
		endTx(pm);
		endEm(pm);
	}


	public void testConstantOneToOne1 ()
	{
		EntityManager pm = currentEntityManager();
		startTx(pm);
		ConstantJoinPC4 pc4 =  pm.find(ConstantJoinPC4.class, oid);
		assertEquals (2, pc4.getManyToMany ().size ());
        assertEquals ("pc5-one-to-one-1", pc4.getOneToOne1().getName());

		endTx(pm);
		endEm(pm);
	}

	public void testSharedJoinTableModifications ()
	{
		EntityManager pm = currentEntityManager();
		ConstantJoinPC4 pc4 = pm.find(ConstantJoinPC4.class, oid);
		startTx(pm);
		pc4.getManyToMany ().clear ();
		endTx(pm);

		EntityManager pm2 = currentEntityManager();
		pc4 = pm2.find(ConstantJoinPC4.class, oid);
		assertNotNull (pc4.getOneToOne1 ());
		assertEquals (0, pc4.getManyToMany ().size ());
	}

}
