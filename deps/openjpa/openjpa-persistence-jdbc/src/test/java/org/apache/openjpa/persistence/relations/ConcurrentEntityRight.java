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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

@Entity
public class ConcurrentEntityRight {
	@Id
	private int id;
	
	private String strData;
	
	@OneToMany(mappedBy="rightEntity", targetEntity=ConcurrentEntityLeft.class, 
			cascade={javax.persistence.CascadeType.ALL})
	@MapKey(name="strData")
	private Map<String, ConcurrentEntityLeft> leftEntityMap;
	
	public ConcurrentEntityRight() {
		leftEntityMap = new ConcurrentHashMap<String, ConcurrentEntityLeft>();	
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStrData() {
		return strData;
	}

	public void setStrData(String strData) {
		this.strData = strData;
	}

	public Map<String, ConcurrentEntityLeft> getLeftEntityMap() {
		return leftEntityMap;
	}
}
