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

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType=DiscriminatorType.STRING)
@FetchGroups(
    {
        @FetchGroup(name="nothing", attributes={}),
        @FetchGroup(name="children", attributes={@FetchAttribute(name="children")})
    }
)
public abstract class InheritanceHierarchyAbstract implements Serializable {

    @Id
    @GeneratedValue
    private long id;
   
    @ManyToOne
    @JoinColumn
    private InheritanceHierarchyAbstract parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, 
			fetch=FetchType.EAGER)
    private Set<InheritanceHierarchyAbstract> children;


    public long getId() { 
        return id; 
    }

	public Set<InheritanceHierarchyAbstract> getChildren() {
		return this.children;
	}

	public void setChildren(Set<InheritanceHierarchyAbstract> children) {
		this.children = children;
	}

	public InheritanceHierarchyAbstract getParent() {
		return this.parent;
	}

	public void setParent(InheritanceHierarchyAbstract parent) {
		this.parent = parent;
	}

}

