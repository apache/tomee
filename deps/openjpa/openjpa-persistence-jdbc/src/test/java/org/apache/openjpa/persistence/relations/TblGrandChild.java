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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.apache.openjpa.persistence.jdbc.ForeignKey;

@Entity
public class TblGrandChild {

	@Id
	@Column(name = "GC_ID",nullable=false)
	private Integer grandChildId;   

	@Version
	@Column(name = "VRS_NBR")
	private Integer vrsNbr;   

	@ManyToOne(fetch = FetchType.LAZY,
		cascade = {CascadeType.PERSIST,CascadeType.MERGE })
	@JoinColumns({@JoinColumn(name =
		"CHILD_ID",referencedColumnName="CHILD_ID")})   
	@ForeignKey
	private TblChild tblChild;
	
	public Integer getGrandChildId() {
		return grandChildId;
	}
	
	public void setGrandChildId(Integer grandChildId) {
		this.grandChildId = grandChildId;
	}
	
	public Integer getVrsNbr() {
		return vrsNbr;
	}
	
	public void setVrsNbr(Integer vrsNbr) {
		this.vrsNbr = vrsNbr;
	}

	public TblChild getTblChild() {
		return tblChild;
	}
	
	public void setTblChild(TblChild tblChild) {
		this.tblChild = tblChild;
	}
}


