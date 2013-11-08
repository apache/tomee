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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;

import org.apache.openjpa.persistence.annotations.common.apps.annotApp.ddtype.
        CallbackStorage;

@Entity
public class LifeCycleDDEntity2
{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@Basic
	private String name;
	
	@Basic
	private String surname;
	
	public LifeCycleDDEntity2()
	{		
	}
	
	public LifeCycleDDEntity2(String name, String surname)
	{
		this.name = name;
		this.surname = surname;
	}
	
	@PrePersist
	public void verifyPrePersist()
	{
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("verifyprp");
	}
	
	@PostPersist
	public void verifyPostPersist()
	{
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("verifypop");
	}
	
	@PreRemove
    public void verifyPreRemove()
    {
		CallbackStorage store = CallbackStorage.getInstance();
		store.getClist().add("verifyprr");
    }
    
    public int getId()
    {
    	return id;
    }
    
    public void setName(String name)
    {
    	this.name = name;
    }
    
    public String getName()
    {
    	return name;
    }
    
    public void setSurName(String name)
    {
    	this.surname = name;
    }
    
    public String getSurName()
    {
    	return surname;
    }
}
