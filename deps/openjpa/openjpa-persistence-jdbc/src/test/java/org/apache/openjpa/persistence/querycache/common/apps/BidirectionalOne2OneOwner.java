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

package org.apache.openjpa.persistence.querycache.common.apps;

import javax.persistence.*;

/**
 * A persistent entity that owns bidirectional single-valued relationship.
 * A bidirectional relationship has only an owning side, which is this receiver.
 * Given the following relationship between Entity A and Entity B:
 *   Entity A refers to a single instance of Entity B 
 *   Entity B refers to a single instance of Entity A 
 * If Entity B qualifies its relation to the Entity A with mappedBy 
 * annotation qualifier then Entity B is called owned and Entity A is called 
 * owner with respect to the above relationship.
 * 
 * Used to test identical application behavior with or without DataCache.
 * 
 * @see BidirectionalOne2OneOwned
 * 
 * @author Pinaki Poddar
 *
 */
@Entity
public class BidirectionalOne2OneOwner {
	@Id
	private long id;
	
	private String name;
	
	@OneToOne
	private BidirectionalOne2OneOwned owned;
	
	@Version
	private int version;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BidirectionalOne2OneOwned getOwned() {
		return owned;
	}

	public void setOwned(BidirectionalOne2OneOwned owned) {
		this.owned = owned;
	}
	
	public int getVersion() {
		return version;
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + ":" + id + ":" + name;
	}

}
