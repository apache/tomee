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
package org.apache.openjpa.persistence.annotations.common.apps.annotApp.ddtype;

import java.util.ArrayList;

import javax.persistence.Basic;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Entity;
import javax.persistence.PostRemove;
import javax.persistence.Table;

@Entity
@Table(name="TX_ROLLBACK_ENT")
public class TxRollbackEntity
{
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Id
	private int id;

	@Basic
	private String name;

	public TxRollbackEntity(String name)
	{
		this.name = name;
	}

	@PostLoad
	public void rollBackException()
	{
        //should throw a null pointer exception causing the tx to be rolled back
		ArrayList<Integer> list = null;
		list.add(1);
	}

	@PostRemove
	public void bomb()
	{
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("rollbackpor1");
	}

	public void bomb2()
	{
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("rollbackpor");
	}

	public int getId() {
		return id;
	}
}
