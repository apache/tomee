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
package org.apache.openjpa.persistence.jdbc.query;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;

public class TestEJBQLSelectNPlusOne
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
	public TestEJBQLSelectNPlusOne(String name)
	{
		super(name);
	}

	public void setUp()
	{
		deleteAll (RuntimeTest1.class);
		EntityManager pm = currentEntityManager();
		pm.getTransaction ().begin ();
		pm.persist(new RuntimeTest1("foo", 3));
		endTx(pm);
		endEm(pm);
	}

	public void testSimpleEJBQLQuery() throws Exception
	{
		EntityManager pm = currentEntityManager();
        // run a JDOQL query first to take care of any class-loading issues
        List l = pm.createQuery("select object(o) from "
                + RuntimeTest1.class.getSimpleName() + " o").getResultList();

		// now run the actual test.
		assertEquals(1, l.size());
		endEm(pm);
	}

}
