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

import org.apache.openjpa.persistence.kernel.common.apps.DateVersion;

public class TestEJBDateVersion
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
	private int oid;

	public TestEJBDateVersion(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		deleteAll(DateVersion.class);

		DateVersion pc = new DateVersion ("pc1", 1);

		EntityManager pm = currentEntityManager();
		startTx(pm);
		pm.persist(pc);

		oid = pc.getId();

		endTx(pm);
		endEm(pm);
	}

	public void testOptLock ()	throws InterruptedException
{
	EntityManager pm1 = currentEntityManager();
	EntityManager pm2 = currentEntityManager();


	startTx(pm1);
	startTx(pm2);
	DateVersion pc1 = (DateVersion) pm1.find(DateVersion.class, oid);
	DateVersion pc2 = (DateVersion) pm2.find(DateVersion.class, oid);

	pc1.setString ("pc-2-1");
	pc2.setString ("pc-2-2");

	// some DBs do not distinguish dates unless there is 1 sec diff
	Thread.currentThread ().sleep (1 * 1000);
	endTx(pm1);
	try
	{
		Thread.currentThread ().sleep (1 * 1000);
		endTx(pm2);
		fail ("Should have caused OL exception.");
	}
	catch (Exception jfe)
	{
		startTx(pm2);
		pm2.refresh (pc2);
		pc2.setString ("pc-3-2");
		endTx(pm2);
	}

	// make sure the next transaction works too
	startTx(pm2);
	pc2.setString ("pc-string-4-2");
	endTx(pm2);

	startTx(pm1);
	pm1.refresh (pc1);
	pc1.setString ("pc-string-3-1");

	startTx(pm2);
	pc2.setString ("pc-string-5-2");

	Thread.currentThread ().sleep (1 * 1000);
	endTx(pm1);
	try
	{
		Thread.currentThread ().sleep (1 * 1000);
		endTx(pm2);
		fail ("Should have caused OL exception2.");
	}
	catch (Exception jfe)
	{
		startTx(pm2);
		pm2.refresh (pc2);
		pc2.setString ("pc-string-6-2");
		endTx(pm2);
	}
	endEm(pm1);
	endEm(pm2);

	EntityManager pm = currentEntityManager();
	DateVersion pc = pm.find(DateVersion.class, oid);
	assertEquals ("pc-string-6-2", pc.toString ());
	endEm(pm);
  }
}
