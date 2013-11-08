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

import org.apache.openjpa.persistence.annotations.common.apps.annotApp.ddtype.
        CallbackStorage;

public class DefaultCallbackListener 
{
	public DefaultCallbackListener()
	{}
	
//	@PrePersist
	public void prePersist(Object obj)
	{
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("def-prepersist");
	}
	
//	@PostPersist
	public void postPersist(Object obj)
	{
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("def-postpersist");
	}
	
//	@PostRemove
	public void postRemove(Object obj)
	{
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("def-postremove");
	}
	
//	@PreRemove
	public void preRemove(Object obj)
	{
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("def-preremove");
	}
	
//	@PostUpdate	
	public void postUpdate(Object obj)
	{
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("def-postupdate");
	}
	
//	@PreUpdate
	public void preUpdate(Object obj)
	{
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("def-preupdate");
	}
	
//	@PostLoad
	public void postLoad(Object obj)
	{
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("def-postload");
	}

}
