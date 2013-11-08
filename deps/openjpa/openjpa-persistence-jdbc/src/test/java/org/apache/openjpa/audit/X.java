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
package org.apache.openjpa.audit;

import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * A simple persistent entity used to test audit facility.
 * An entity is annotated with {@link Auditable} annotation to qualify for audit.
 *  
 * @author Pinaki Poddar
 *
 */
@Entity
@Auditable
public class X {
	@Id
	private long id;
	
	private String name;
	private int price;
	
	private static AtomicLong ID_GENERATOR = new AtomicLong(System.currentTimeMillis());
	
	public X() {
		id = ID_GENERATOR.getAndIncrement();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public long getId() {
		return id;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		X other = (X) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	public String toString() {
		return "X[" + id + "]";
	}
}
