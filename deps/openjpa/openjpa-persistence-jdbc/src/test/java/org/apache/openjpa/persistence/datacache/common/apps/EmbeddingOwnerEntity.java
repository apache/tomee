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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.openjpa.persistence.PersistentCollection;
import org.apache.openjpa.persistence.datacache.TestEmbeddedCollection;

/**
 * Persistent entity that embeds a persistent collection of embeddable.
 *  
 * Used in {@link TestEmbeddedCollection}. 
 * 
 * @author Pinaki Poddar
 *
 */
@Entity
public class EmbeddingOwnerEntity {
	@Id
	@GeneratedValue
	private long id;
	
	@PersistentCollection(elementEmbedded = true, 
			elementType = EmbeddedEntity.class, 
			fetch = FetchType.LAZY)
	private List<EmbeddedEntity> members;

	public List<EmbeddedEntity> getMembers() {
		return members;
	}

	public void addMember(EmbeddedEntity member) {
		if (members == null)
			members = new ArrayList<EmbeddedEntity>();
		this.members.add(member);
	}
	
	public void removeMember(EmbeddedEntity member) {
		if (members != null)
			members.remove(member);
	}
	
	public EmbeddedEntity removeMember(int member) {
		return (members != null) ? members.remove(member) : null;
	}

	public long getId() {
		return id;
	} 
}
