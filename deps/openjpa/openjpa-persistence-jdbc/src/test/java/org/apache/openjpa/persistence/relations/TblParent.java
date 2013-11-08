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

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
@Entity
public class TblParent {

	@Id
	@Column(name = "PARENT_ID")
	private Integer parentId;
	
	@OneToMany(mappedBy="tblParent",fetch = FetchType.LAZY,cascade = {
		CascadeType.PERSIST,CascadeType.MERGE })
	private Collection<TblChild> tblChildren = new ArrayList<TblChild>();	
	
	public Integer getParentId() {
		return parentId;
	}
	
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Collection<TblChild> getTblChildren() {
		return tblChildren;
	}
	
	public void setTblChildren(Collection<TblChild> tblChildren) {
		this.tblChildren = tblChildren;
	}
	
	public void addTblChild(TblChild tblChild) {
		tblChildren.add(tblChild);
	} 
}
