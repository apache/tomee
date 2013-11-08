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
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("A1")
public class EntityA1InverseEager extends EntityAInverseEager {
    private String name1;

	@OneToMany(fetch=FetchType.EAGER, mappedBy="entityA")
    private List<EntityBInverseEager> listB =
        new ArrayList<EntityBInverseEager>();
	
	public EntityA1InverseEager() {}
	
	public EntityA1InverseEager(String name) {
	    super(name);
	    this.name1 = name;
	}

	public String getName1() {
        return name1;
    }
    
    public void setName1(String name1) {
        this.name1 = name1;
    }
}
