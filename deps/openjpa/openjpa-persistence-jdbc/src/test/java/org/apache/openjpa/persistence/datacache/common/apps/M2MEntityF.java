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
package org.apache.openjpa.persistence.datacache.common.apps;

import javax.persistence.*;
import java.util.*;

@Entity
public class M2MEntityF  {
	@Id private int id;

	@ManyToMany(mappedBy="entityf")
	@MapKey(name="name")
	private Map<String, M2MEntityE> entitye;
	
	public M2MEntityF() {
		entitye = new HashMap<String,M2MEntityE>();
	}
	public Map<String, M2MEntityE> getEntityE() {
		return entitye;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String toString(){
		return "EntityF:"+id;
	}
	public void print(){
		System.out.println("EntityF id="+id+" entitye="+ entitye);
	}

}
