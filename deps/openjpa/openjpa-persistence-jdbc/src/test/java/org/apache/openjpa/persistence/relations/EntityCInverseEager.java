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
package org.apache.openjpa.persistence.relations;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity

public class EntityCInverseEager  {
	@GeneratedValue
	@Id private int id;
    
    private String name;
    
    private int age;
    
    private int balance;
    
	@OneToOne(fetch=FetchType.EAGER, mappedBy="entityC")
	private EntityDInverseEager entityD = null;
	
	public EntityCInverseEager() {}
	
	public EntityCInverseEager(String name, int age, int balance) {
	    this.name = name;
	    this.age = age;
	    this.balance = balance;
	}
	
	public EntityDInverseEager getD() {
		return entityD;
	}
	
	public void setD(EntityDInverseEager entityD) {
		this.entityD = entityD;
	}
	
	public int getId() {
		return id;
	}
	
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }

    public int getBalance() {
        return balance;
    }
    
    public void setBalance(int balance) {
        this.balance = balance;
    }
    
    
    

}
