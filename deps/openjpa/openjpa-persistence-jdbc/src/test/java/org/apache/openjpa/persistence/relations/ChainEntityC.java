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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class ChainEntityC {

	@Id
	@GeneratedValue
	private long cId;

	@Version
	private Integer optLock;

	@ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
	protected ChainEntityB chainEntityB;

	@Basic
	protected long chainEntityBId;

	public void setChainEntityB (ChainEntityB b) {
		this.chainEntityB = b;
//		this.chainEntityBId = null == b ? 0 : b.getId ();
	}

	private String name;

	public String getName () {
		return name;
	}

	public void setName (String name) {
		this.name = name;
	}

	public long getId () {
		return cId;
	}

	public void setId (long id) {
		this.cId = id;
	}

}
