package org.apache.openjpa.persistence.querycache.common.apps;

import javax.persistence.*;
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

/**
 * A persistent entity that is owned by unidirectional single-valued 
 * relationship.
 * A unidirectional relationship has only one owning side and the other side
 * called as owned side is this receiver.
 * Given the following relationship between Entity A and Entity B:
 *   Entity A refers a single instance of Entity B 
 *   Entity B does not refer Entity A (owner)
 * Entity A is called owner and Entity B is called owned with respect
 * to the above relationship.
 * 
 * Used to test identical application behavior with or without DataCache.
 * 
 * @see BidirectionalOne2OneOwned
 * @see TestDataCacheBehavesIdentical
 * @see Section 2.1.8.3 of JPA Specification Version 1.0
 * 
 * @author Pinaki Poddar
 *
 */

@Entity
public class BidirectionalOne2OneOwned {
	@Id
	private long id;
	
	private String name;
	
	@OneToOne(mappedBy="owned")
	private BidirectionalOne2OneOwner owner;
	
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

	public BidirectionalOne2OneOwner getOwner() {
		return owner;
	}

	public void setOwner(BidirectionalOne2OneOwner owner) {
		this.owner = owner;
	}

	public int getVersion() {
		return version;
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + ":" + id + ":" + name;
	}

}
