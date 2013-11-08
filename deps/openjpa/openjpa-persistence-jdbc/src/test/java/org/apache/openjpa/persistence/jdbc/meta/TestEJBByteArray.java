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

public class TestEJBByteArray
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
	private int _null;
	private int _empty;
	private int _small;
	private int _large;


	public TestEJBByteArray(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		deleteAll(ByteArray.class);

		EntityManager pm = currentEntityManager();
		startTx(pm);

		ByteArray pc = new ByteArray ("Null", 1);
		pm.persist (pc);
		_null = 1;

		pc = new ByteArray ("Empty", 2);
		pc.setBytes (new byte[0]);
		pm.persist (pc);
		_empty = 2;

		pc = new ByteArray ("Small", 3);
		pc.setBytes (pc.getString ().getBytes ());
		pm.persist (pc);
		_small = 3;

		byte[] bytes = new byte [10000];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) (i % 8);

		pc = new ByteArray ("Large", 4);
		pc.setBytes (bytes);
		pm.persist (pc);
		_large = 4;

		endTx(pm);
		endEm(pm);
	}

	public void testNull ()
	{
		EntityManager pm = currentEntityManager();
		startTx(pm);

		ByteArray pc = pm.find(ByteArray.class, _null);
		assertNull (pc.getBytes ());


		pc.setBytes ("Not Null".getBytes ());
		endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		startTx(pm);
		pc = pm.find(ByteArray.class, _null);

		assertEquals ("Not Null", new String (pc.getBytes ()));

		endTx(pm);
		endEm(pm);
	}

	public void testEmpty ()
	{
		EntityManager pm = currentEntityManager();
		startTx(pm);

		ByteArray pc = pm.find(ByteArray.class, _empty);
		byte [] bytes = pc.getBytes ();
		assertNotNull (bytes);
		assertEquals (0, bytes.length);

		pc.setBytes ("Not Empty".getBytes ());
		endTx(pm);
		endEm(pm);

		pm = currentEntityManager();
		startTx(pm);
		pc = pm.find(ByteArray.class, _empty);
		assertEquals ("Not Empty", new String (pc.getBytes ()));
		endTx(pm);
		endEm(pm);
	}

	public void testLarge ()
	{
		EntityManager pm = currentEntityManager();
		startTx(pm);
		ByteArray pc = pm.find(ByteArray.class, _large);
		byte [] bytes = pc.getBytes ();
		assertNotNull (bytes);
		assertEquals (10000, bytes.length);
		for (int i = 0; i < bytes.length; i++)
			assertEquals (bytes[i], (byte) (i % 8));
		endTx(pm);
		endEm(pm);
	}






}
