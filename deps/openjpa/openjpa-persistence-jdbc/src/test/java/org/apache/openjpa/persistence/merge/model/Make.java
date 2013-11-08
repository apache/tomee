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
package org.apache.openjpa.persistence.merge.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.openjpa.persistence.DetachedState;

@Entity
@Table(name="MRG_MAKE")
public class Make {

	@Id
	@GeneratedValue
	@Column(name="MAKE_ID")
	private long id;

	@OneToMany(cascade=CascadeType.ALL)
	private List<Model> models;
		
    @SuppressWarnings("unused")
    @Version
    private int version;
    
	@SuppressWarnings("unused")
	@DetachedState
	private Object state;

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setModels(List<Model> models) {
		this.models = models;
	}

	public List<Model> getModels() {
		return models;
	}
}
