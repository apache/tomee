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
package org.apache.openjpa.persistence.jdbc.mapping.bidi;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.persistence.jdbc.ForeignKey;

/**
 * Child in a logically bidirectional but actually unidirectional parent-child 
 * relationship where Child holds reference to Parent via primary key and not 
 * via object reference.
 * 
 * @author Pinaki Poddar
 *
 */
@Entity
@Table(name="CHILD_693")
public class Child {
	@Id
	private long id;
	
	private String name;

	@Column(name="FK_PARENT_SEQ_ID", nullable=true)
	@ForeignKey(implicit=true)
	private long seqParentId;
	
	@Column(name="FK_PARENT_AUTO_ID", nullable=true)
	@ForeignKey(implicit=true)
	private long autoParentId;
	
	@Column(name="FK_PARENT_APP_ID", nullable=true)
	@ForeignKey(implicit=true)
	private long appParentId;

	public Child() {
		
	}
	
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

	public long getSeqParentId() {
		return seqParentId;
	}

	void setSeqParentId(long parentId) {
		this.seqParentId = parentId;
	}
	
	public long getAutoParentId() {
		return autoParentId;
	}

	void setAutoParentId(long parentId) {
		this.autoParentId = parentId;
	}
	public long getAppParentId() {
		return appParentId;
	}

	void setAppParentId(long parentId) {
		this.appParentId = parentId;
	}
	
	public long getParentIdType(int idType) {
		switch (idType) {
		case ValueStrategies.NONE : return getAppParentId();
		case ValueStrategies.AUTOASSIGN : return getAutoParentId();
		case ValueStrategies.SEQUENCE : return getSeqParentId();
		default :
            throw new IllegalArgumentException("No parent with id strategy " + 
					idType);
		}
	}
}
